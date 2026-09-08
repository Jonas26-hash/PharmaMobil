package pe.edu.upeu.pharmamobil.presentation.cliente

data class ClienteUiState(
    val fase: Fase = Fase.Cargando,
    val formulario: FormularioCliente = FormularioCliente(),
    val registrando: Boolean = false,
    val mensajeExito: String? = null
) {

    /** Fases excluyentes de la cartera: solo una puede estar activa. */
    sealed interface Fase {

        data object Cargando : Fase

        data object SinClientes : Fase

        data class ConClientes(val clientes: List<ClienteUi>) : Fase

        data class Error(val mensaje: String) : Fase
    }
}

data class FormularioCliente(
    val nombre: String = "",
    val apellido: String = "",
    val dni: String = "",
    val telefono: String = "",
    val email: String = "",
    val direccion: String = "",
    val nombreError: String? = null,
    val apellidoError: String? = null,
    val dniError: String? = null,
    val emailError: String? = null,
    val telefonoError: String? = null
)