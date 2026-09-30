package pe.edu.upeu.pharmamobil.presentation.cliente

/**
 * Estado del formulario de cliente (registro o edición). El id nulo indica
 * que se está registrando; presente, que se está editando el cliente.
 */
data class ClienteFormUiState(
    val cargandoCliente: Boolean = false,
    val formulario: FormularioCliente = FormularioCliente(),
    val guardando: Boolean = false,
    val eliminando: Boolean = false,
    val mensajeExito: String? = null,
    val mensaje: String? = null
)

data class FormularioCliente(
    val id: Long? = null,
    val nombre: String = "",
    val apellido: String = "",
    val dni: String = "",
    val telefono: String = "",
    val email: String = "",
    val direccion: String = "",
    val estadoActivo: Boolean = true,
    val nombreError: String? = null,
    val apellidoError: String? = null,
    val dniError: String? = null,
    val emailError: String? = null,
    val telefonoError: String? = null
)