package pe.edu.upeu.pharmamobil.presentation.login

/**
 * Estado de la pantalla de login.
 *
 * [puedeEnviar] centraliza la validación básica y la prevención de envíos
 * duplicados: mientras [cargando] es `true` el botón queda deshabilitado.
 */
data class LoginUiState(
    val usuario: String = "",
    val contrasena: String = "",
    val cargando: Boolean = false,
    val error: String? = null,
    val mostrarContrasena: Boolean = false
) {

    val puedeEnviar: Boolean
        get() = usuario.isNotBlank() && contrasena.isNotBlank() && !cargando
}
