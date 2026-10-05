package pe.edu.upeu.pharmamobil.data.remote

import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ResponseException
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.utils.io.errors.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json
import pe.edu.upeu.pharmamobil.domain.ErrorApi
import pe.edu.upeu.pharmamobil.domain.ErrorDeApi

/**
 * Punto único de traducción de fallos de red a [ErrorApi].
 *
 * Es la **única** parte del proyecto que convierte una excepción de Ktor en
 * un error de dominio: ningún repositorio hace su propio `when` sobre el
 * status code. Así el mapeo se puede auditar y probar en un solo lugar.
 *
 * El mapeo está levantado del comportamiento real observado en pharmaSoft:
 *
 * | Origen                                      | ErrorApi           |
 * |---------------------------------------------|--------------------|
 * | 400 con `validationErrors`                  | Validacion         |
 * | 400 sin detalle por campo                   | Validacion         |
 * | 404 (`RecursoNoEncontradoException`)         | NoEncontrado       |
 * | 409 (`ReglaNegocioException`, FK o UNIQUE)   | Conflicto          |
 * | 5xx                                         | Servidor           |
 * | `ConnectTimeoutException`                   | SinConexion        |
 * | `HttpRequestTimeoutException` / socket      | TiempoAgotado      |
 * | `IOException` (sin red, servidor apagado)   | SinConexion        |
 * | cuerpo ilegible o error inesperado          | Servidor           |
 */
private val jsonDelError = Json {
    ignoreUnknownKeys = true
    isLenient = true
}

/**
 * Ejecuta una operación de red y la devuelve, o lanza [ErrorDeApi] si falla.
 *
 * [CancellationException] se relanza siempre y sin convertir: cancelar una
 * corrutina es una señal de control de Structured Concurrency, no un error de
 * negocio, así que nunca debe terminar como [ErrorApi].
 */
internal suspend fun <T> alAplicacion(bloque: suspend () -> T): T = try {
    bloque()
} catch (cancelacion: CancellationException) {
    throw cancelacion
} catch (fallo: Throwable) {
    throw ErrorDeApi(fallo.aErrorApi())
}

/**
 * Traduce una excepción de la capa de datos a [ErrorApi].
 *
 * El orden de las ramas es importante: en Ktor 3 las excepciones de espera
 * heredan de [IOException], de modo que se comprueban antes que esta última.
 */
internal suspend fun Throwable.aErrorApi(): ErrorApi = when (this) {

    is HttpRequestTimeoutException -> ErrorApi.TiempoAgotado(
        "La solicitud tardó demasiado tiempo. Inténtalo de nuevo."
    )

    is ConnectTimeoutException -> ErrorApi.SinConexion(
        "No se pudo conectar con el servidor. Verifica tu conexión."
    )

    is SocketTimeoutException -> ErrorApi.TiempoAgotado(
        "El servidor tardó demasiado en responder. Inténtalo de nuevo."
    )

    // Cubre ClientRequestException (4xx) y ServerResponseException (5xx).
    is ResponseException -> aErrorApiDesdeRespuesta()

    is IOException -> ErrorApi.SinConexion(
        "No se pudo conectar con el servidor. Verifica tu conexión."
    )

    // Cuerpo que no se pudo deserializar o fallo no previsto: el servidor
    // respondió algo inesperado.
    else -> ErrorApi.Servidor(
        message ?: "Se produjo un error inesperado.",
        codigo = null
    )
}

/**
 * Traduce un [ResponseException] (4xx/5xx) leyendo el `ErrorResponseDTO` real
 * que devuelve pharmaSoft, para conservar el mensaje del servidor y el
 * detalle por campo de las validaciones de Bean Validation.
 */
private suspend fun ResponseException.aErrorApiDesdeRespuesta(): ErrorApi {

    val codigo = response.status.value
    val cuerpo = leerCuerpoDeError()

    val mensaje = cuerpo?.message?.takeIf { it.isNotBlank() }
        ?: mensajePorDefecto(codigo)

    return when (codigo) {

        HttpStatusCode.BadRequest.value,
        HttpStatusCode.UnprocessableEntity.value ->
            ErrorApi.Validacion(
                mensaje = mensaje,
                erroresPorCampo = cuerpo?.validationErrors
                    ?.filterValues { it.isNotBlank() }
                    .orEmpty()
            )

        HttpStatusCode.NotFound.value ->
            ErrorApi.NoEncontrado(mensaje)

        HttpStatusCode.Conflict.value ->
            ErrorApi.Conflicto(mensaje)

        else -> if (codigo >= 500) {
            ErrorApi.Servidor(mensaje, codigo)
        } else {
            ErrorApi.Validacion(mensaje)
        }
    }
}

/** Deserializa el cuerpo de error; si no es JSON válido devuelve `null`. */
private suspend fun ResponseException.leerCuerpoDeError(): ErrorResponseDto? = try {
    jsonDelError.decodeFromString<ErrorResponseDto>(response.bodyAsText())
} catch (_: Exception) {
    null
}

/**
 * Mensaje de reserva cuando el cuerpo de error no trae `message`, para que el
 * usuario nunca ve un código HTTP desnudo.
 */
private fun mensajePorDefecto(codigo: Int): String = when {
    codigo == HttpStatusCode.BadRequest.value ||
        codigo == HttpStatusCode.UnprocessableEntity.value ->
        "El servidor rechazó los datos enviados."

    codigo == HttpStatusCode.NotFound.value ->
        "No se encontró el recurso solicitado."

    codigo == HttpStatusCode.Conflict.value ->
        "La operación entra en conflicto con el estado actual."

    codigo >= 500 ->
        "El servidor no pudo completar la operación. Inténtalo más tarde."

    else -> "El servidor devolvió un error ($codigo)."
}