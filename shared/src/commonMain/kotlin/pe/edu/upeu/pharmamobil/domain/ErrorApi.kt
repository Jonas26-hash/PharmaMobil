package pe.edu.upeu.pharmamobil.domain

sealed interface ErrorApi {

    data class Validacion(
        val mensaje: String,
        val erroresPorCampo: Map<String, String> = emptyMap()
    ) : ErrorApi

    data class NoEncontrado(val mensaje: String) : ErrorApi

    data class Conflicto(val mensaje: String) : ErrorApi

    data class Servidor(
        val mensaje: String,
        val codigo: Int? = null
    ) : ErrorApi

    data class SinConexion(val mensaje: String) : ErrorApi

    data class TiempoAgotado(val mensaje: String) : ErrorApi

    data class NoAutorizado(val mensaje: String) : ErrorApi

    data class Prohibido(val mensaje: String) : ErrorApi

    data class CredencialesInvalidas(val mensaje: String) : ErrorApi

    data class SesionExpirada(val mensaje: String) : ErrorApi
}

val ErrorApi.esFalloDeAutenticacion: Boolean
    get() = this is ErrorApi.NoAutorizado ||
        this is ErrorApi.CredencialesInvalidas ||
        this is ErrorApi.SesionExpirada

fun ErrorApi.mensajeParaUsuario(): String = when (this) {

    is ErrorApi.Validacion -> mensaje

    is ErrorApi.NoEncontrado ->
        mensaje.ifBlank { "No se encontró el recurso solicitado." }

    is ErrorApi.Conflicto ->
        mensaje.ifBlank { "La operación entra en conflicto con el estado actual." }

    is ErrorApi.Servidor ->
        mensaje.ifBlank {
            if (codigo != null) {
                "El servidor devolvió un error ($codigo). Inténtalo más tarde."
            } else {
                "El servidor no pudo completar la operación. Inténtalo más tarde."
            }
        }

    is ErrorApi.SinConexion ->
        "No se pudo conectar con el servidor. Verifica tu conexión."

    is ErrorApi.TiempoAgotado ->
        "La solicitud tardó demasiado tiempo. Inténtalo de nuevo."

    is ErrorApi.NoAutorizado ->
        mensaje.ifBlank { "Tu sesión ya no es válida. Inicia sesión de nuevo." }

    is ErrorApi.Prohibido ->
        mensaje.ifBlank { "No tienes permisos para realizar esta operación." }

    is ErrorApi.CredencialesInvalidas ->
        mensaje.ifBlank { "Usuario o contraseña incorrectos." }

    is ErrorApi.SesionExpirada ->
        mensaje.ifBlank { "Tu sesión expiró. Inicia sesión de nuevo." }
}

val ErrorApi.tieneErroresPorCampo: Boolean
    get() = this is ErrorApi.Validacion && erroresPorCampo.isNotEmpty()

class ErrorDeApi(
    val error: ErrorApi
) : Exception(error.mensajeParaUsuario()) {

    val mensaje: String get() = error.mensajeParaUsuario()

    val erroresPorCampo: Map<String, String>
        get() = (error as? ErrorApi.Validacion)?.erroresPorCampo.orEmpty()
}
