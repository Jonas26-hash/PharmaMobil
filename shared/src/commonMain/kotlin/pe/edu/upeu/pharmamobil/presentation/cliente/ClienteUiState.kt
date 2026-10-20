package pe.edu.upeu.pharmamobil.presentation.cliente

data class ClienteUiState(
    val fase: Fase = Fase.Cargando,
    val busqueda: String = "",
    val sincronizando: Boolean = false,
    val avisoRed: String? = null
) {

    sealed interface Fase {

        data object Cargando : Fase

        data object SinClientes : Fase

        data class ConClientes(val clientes: List<ClienteUi>) : Fase

        data class Error(val mensaje: String) : Fase
    }

    fun filtrarPorBusqueda(): List<ClienteUi> {
        val texto = busqueda.trim()
        return (fase as? Fase.ConClientes)?.clientes.orEmpty()
            .filter {
                it.nombreCompleto.contains(texto, ignoreCase = true) ||
                    it.dni.contains(texto)
            }
    }
}
