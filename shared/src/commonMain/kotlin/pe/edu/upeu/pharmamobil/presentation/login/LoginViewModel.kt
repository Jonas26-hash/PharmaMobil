package pe.edu.upeu.pharmamobil.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upeu.pharmamobil.data.auth.GestorSesion
import pe.edu.upeu.pharmamobil.domain.ErrorDeApi
import pe.edu.upeu.pharmamobil.domain.auth.AuthRepository
import pe.edu.upeu.pharmamobil.domain.auth.Credenciales

/**
 * Autenticación de la pantalla de login.
 *
 * No conoce Ktor ni el almacén seguro: pide la [pe.edu.upeu.pharmamobil.domain.auth.Sesion]
 * al [AuthRepository] y, si es correcta, la publica en el [GestorSesion], que
 * es lo que hace que [pe.edu.upeu.pharmamobil.App] abandone el login.
 */
class LoginViewModel(
    private val authRepository: AuthRepository,
    private val gestorSesion: GestorSesion
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onUsuarioChange(usuario: String) {
        _uiState.update { it.copy(usuario = usuario, error = null) }
    }

    fun onContrasenaChange(contrasena: String) {
        _uiState.update { it.copy(contrasena = contrasena, error = null) }
    }

    fun alternarContrasena() {
        _uiState.update { it.copy(mostrarContrasena = !it.mostrarContrasena) }
    }

    /**
     * Intenta iniciar sesión. Si ya hay una petición en curso ([LoginUiState.cargando])
     * la ignora, de modo que un doble toque no lanza dos logins.
     */
    fun ingresar() {

        val estado = _uiState.value

        if (estado.cargando) return

        if (estado.usuario.isBlank() || estado.contrasena.isBlank()) {
            _uiState.update { it.copy(error = "Ingresa tu usuario y tu contraseña.") }
            return
        }

        viewModelScope.launch {

            _uiState.update { it.copy(cargando = true, error = null) }

            try {
                val sesion = authRepository.iniciarSesion(
                    Credenciales(
                        usuario = estado.usuario.trim(),
                        contrasena = estado.contrasena
                    )
                )

                // Tras autenticar no se conservan las credenciales en el estado:
                // así, al cerrar sesión, el formulario vuelve a aparecer vacío.
                _uiState.update {
                    it.copy(cargando = false, usuario = "", contrasena = "")
                }
                gestorSesion.establecer(sesion)

            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (fallo: ErrorDeApi) {
                _uiState.update {
                    it.copy(cargando = false, error = fallo.mensaje)
                }
            } catch (fallo: Throwable) {
                _uiState.update {
                    it.copy(
                        cargando = false,
                        error = "No se pudo iniciar sesión. Inténtalo de nuevo."
                    )
                }
            }
        }
    }
}
