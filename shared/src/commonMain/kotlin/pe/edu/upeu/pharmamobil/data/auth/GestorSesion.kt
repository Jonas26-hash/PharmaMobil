package pe.edu.upeu.pharmamobil.data.auth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import pe.edu.upeu.pharmamobil.domain.auth.Sesion
import pe.edu.upeu.pharmamobil.domain.security.AlmacenSeguro

/**
 * Estado de sesión compartido por toda la aplicación.
 *
 * Guarda la [Sesion] vigente en memoria (observable por la interfaz) y delega
 * la persistencia del token en el [AlmacenSeguro]. El plugin Auth de Ktor lee
 * el token con [token] y el tratamiento central del 401 llama a [limpiar]
 * cuando la sesión expira.
 *
 * Se registra como `single` en Koin para que el cliente HTTP y la presentación
 * vean exactamente el mismo estado.
 */
class GestorSesion(
    private val almacen: AlmacenSeguro
) {

    private val _sesion = MutableStateFlow<Sesion?>(null)

    /** Sesión vigente, observable. `null` significa "sin autenticar". */
    val sesion: StateFlow<Sesion?> = _sesion.asStateFlow()

    /** Sesión actual sin suscribirse a cambios. */
    fun sesionActual(): Sesion? = _sesion.value

    /** Token persistido, o `null`. Lo consume `loadTokens` de Ktor. */
    fun token(): String? = almacen.leerToken()

    /** `true` si la sesión vigente incluye [permiso]. */
    fun puede(permiso: String): Boolean = _sesion.value?.puede(permiso) == true

    /** Fija la sesión vigente y persiste su token de forma segura. */
    fun establecer(sesion: Sesion) {
        almacen.guardarToken(sesion.token)
        _sesion.value = sesion
    }

    /**
     * Cierra la sesión: borra el token del almacén y vacía el estado en
     * memoria. Es idempotente (seguro de llamar dos veces).
     */
    fun limpiar() {
        almacen.borrarToken()
        _sesion.value = null
    }
}
