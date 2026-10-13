package pe.edu.upeu.pharmamobil.data.auth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import pe.edu.upeu.pharmamobil.domain.auth.Sesion
import pe.edu.upeu.pharmamobil.domain.security.AlmacenSeguro

class GestorSesion(
    private val almacen: AlmacenSeguro
) {

    private val _sesion = MutableStateFlow<Sesion?>(null)

    val sesion: StateFlow<Sesion?> = _sesion.asStateFlow()

    fun sesionActual(): Sesion? = _sesion.value

    fun token(): String? = almacen.leerToken()

    fun puede(permiso: String): Boolean = _sesion.value?.puede(permiso) == true

    fun establecer(sesion: Sesion) {
        almacen.guardarToken(sesion.token)
        _sesion.value = sesion
    }

    fun limpiar() {
        almacen.borrarToken()
        _sesion.value = null
    }
}
