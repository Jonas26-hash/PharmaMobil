package pe.edu.upeu.pharmamobil.domain

/**
 * Error de la API Representado en el dominio.
 *
 * Vive en `domain` porque es parte del contrato de negocio con el que
 * trabajan los casos de uso y la presentación: ninguno de los dos conoce
 * Ktor ni el formato JSON que devuelve el servidor. La capa de datos es la
 * única que traduce una excepción de red a este tipo (ver
 * `data.remote.alAplicacion`).
 *
 * Los seis casos cubren el contrato real de pharmaSoft:
 *  - 400 y errores de `@Valid`  -> [Validacion]
 *  - 404                          -> [NoEncontrado]
 *  - 409                          -> [Conflicto]
 *  - 5xx                          -> [Servidor]
 *  - sin red                      -> [SinConexion]
 *  - espera agotada               -> [TiempoAgotado]
 */
sealed interface ErrorApi {

    /**
     * Petición rechazada por el servidor. Se conserva el mensaje que envía
     * `GlobalExceptionHandler` y, cuando la respuesta trae el detalle por
     * campo de `MethodArgumentNotValidException`, el mapa campo -> mensaje
     * para poder marcar cada campo del formulario.
     */
    data class Validacion(
        val mensaje: String,
        val erroresPorCampo: Map<String, String> = emptyMap()
    ) : ErrorApi

    /** El recurso solicitado no existe (HTTP 404). */
    data class NoEncontrado(val mensaje: String) : ErrorApi

    /** El servidor rechazó la operación por una regla de negocio (HTTP 409). */
    data class Conflicto(val mensaje: String) : ErrorApi

    /** Fallo del servidor (HTTP 5xx). */
    data class Servidor(
        val mensaje: String,
        val codigo: Int? = null
    ) : ErrorApi

    /** No se pudo alcanzar el servidor: sin red, servidor apagado o DNS. */
    data class SinConexion(val mensaje: String) : ErrorApi

    /** La espera agotó el tiempo configurado en [ErrorApi]. */
    data class TiempoAgotado(val mensaje: String) : ErrorApi

    /**
     * HTTP 401 tal como lo entrega la capa de datos, antes de darle contexto.
     *
     * Es el estado bruto: la autenticación se tradujo al dominio pero todavía
     * no se sabe si el 401 vino de un intento de login (credenciales
     * incorrectas) o de una operación protegida (sesión expirada). El
     * repositorio de autenticación y el manejador de sesión lo convierten en
     * [CredencialesInvalidas] o [SesionExpirada] según el caso.
     */
    data class NoAutorizado(val mensaje: String) : ErrorApi

    /** HTTP 403: autenticado, pero sin permiso para la operación. */
    data class Prohibido(val mensaje: String) : ErrorApi

    /** 401 durante el login: usuario o contraseña incorrectos. */
    data class CredencialesInvalidas(val mensaje: String) : ErrorApi

    /** 401 en una operación protegida: la sesión ya no es válida. */
    data class SesionExpirada(val mensaje: String) : ErrorApi
}

/** `true` si el error representa una autenticación caducada o inválida. */
val ErrorApi.esFalloDeAutenticacion: Boolean
    get() = this is ErrorApi.NoAutorizado ||
        this is ErrorApi.CredencialesInvalidas ||
        this is ErrorApi.SesionExpirada

/**
 * Mensaje listo para mostrar al usuario, con una redacción distinta por caso.
 *
 * Se mantiene aquí, en el dominio, para que la presentación tenga una única
 * fuente de verdad y no tenga que conocer el código HTTP ni el tipo de error.
 */
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

/** `true` si el error viene del detalle por campo del servidor. */
val ErrorApi.tieneErroresPorCampo: Boolean
    get() = this is ErrorApi.Validacion && erroresPorCampo.isNotEmpty()

/**
 * Envoltorio que permite propagar un [ErrorApi] por la pila de llamadas.
 *
 * Kotlin solo admite lanzar subtypes de [Throwable], y `ErrorApi` es una
 * interfaz sellada para poder hacer `when` exhaustivo en la presentación. Esta
 * clase es el puente: la capa de datos lanza `ErrorDeApi` y los casos de uso o
 * la presentación leen `.error` para recuperar el error tipado.
 *
 * `message` devuelve el mensaje legible del error, de modo que los
 * consumidores que solo necesitan el texto no tengan que conocer la jerarquía.
 */
class ErrorDeApi(
    val error: ErrorApi
) : Exception(error.mensajeParaUsuario()) {

    /** Atajo al mensaje del error tipado. */
    val mensaje: String get() = error.mensajeParaUsuario()

    /** Detalle por campo, presente solo en [ErrorApi.Validacion]. */
    val erroresPorCampo: Map<String, String>
        get() = (error as? ErrorApi.Validacion)?.erroresPorCampo.orEmpty()
}