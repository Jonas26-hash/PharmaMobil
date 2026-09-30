package pe.edu.upeu.pharmamobil.presentation.cliente

/**
 * Listado de la cartera: fases excluyentes más el texto de búsqueda.
 */
data class ClienteUiState(
    val fase: Fase = Fase.Cargando,
    val busqueda: String = ""
) {

    sealed interface Fase {

        data object Cargando : Fase

        data object SinClientes : Fase

        data class ConClientes(val clientes: List<ClienteUi>) : Fase

        data class Error(val mensaje: String) : Fase
    }

    /** Aplica el filtro de texto sobre la lista cargada. */
    fun filtrarPorBusqueda(): List<ClienteUi> {
        val texto = busqueda.trim()
        return (fase as? Fase.ConClientes)?.clientes.orEmpty()
            .filter {
                it.nombreCompleto.contains(texto, ignoreCase = true) ||
                    it.dni.contains(texto)
            }
    }
}