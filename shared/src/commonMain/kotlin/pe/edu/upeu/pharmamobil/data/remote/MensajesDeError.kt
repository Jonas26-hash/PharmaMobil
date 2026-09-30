package pe.edu.upeu.pharmamobil.data.remote

import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.utils.io.errors.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import pe.edu.upeu.pharmamobil.domain.ErrorDeAplicacion

/**
 * Traduce cualquier fallo de la capa de red a un mensaje que el usuario
 * pueda leer. El orden de los `when` importa: las excepciones de tiempo de
 * espera heredan de IOException, así que se comprueban antes.
 */
internal fun Throwable.mensajeLegible(): String = when (this) {

    is CancellationException -> throw this

    is HttpRequestTimeoutException ->
        "La solicitud tardó demasiado tiempo. Inténtalo de nuevo."

    is ConnectTimeoutException ->
        "No se pudo conectar con el servidor. Verifica tu conexión."

    is SocketTimeoutException ->
        "El servidor tardó demasiado en responder. Inténtalo de nuevo."

    is ServerResponseException ->
        "El servidor no pudo completar la operación. Inténtalo más tarde."

    is ClientRequestException -> when (response.status.value) {
        404 -> "No se encontró el recurso solicitado."
        400, 422 -> "El servidor rechazó los datos enviados. Revisa el formulario."
        else -> "El servidor rechazó la solicitud (código ${response.status.value})."
    }

    is SerializationException ->
        "Los datos recibidos no se pudieron interpretar."

    is IOException ->
        "No se pudo conectar con el servidor. Verifica tu conexión a internet."

    else -> message ?: "Se produjo un error inesperado."
}

/**
 * Ejecuta una operación de red y reenvía el fallo como [ErrorDeAplicacion],
 * para que el dominio y la presentación nunca dependan de Ktor.
 */
internal suspend fun <T> alAplicacion(bloque: suspend () -> T): T = try {
    bloque()
} catch (cancelacion: CancellationException) {
    throw cancelacion
} catch (fallo: Throwable) {
    throw ErrorDeAplicacion(fallo.mensajeLegible())
}