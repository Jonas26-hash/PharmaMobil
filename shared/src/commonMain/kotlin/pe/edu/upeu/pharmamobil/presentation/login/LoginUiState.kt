package pe.edu.upeu.pharmamobil.presentation.login

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
