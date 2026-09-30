package pe.edu.upeu.pharmamobil.presentation.producto

/**
 * Listado del inventario: fases excluyentes (solo una puede estar activa) más
 * el texto de búsqueda, que filtra en la misma pantalla.
 */
data class ProductoUiState(
    val fase: Fase = Fase.Cargando,
    val busqueda: String = ""
) {

    sealed interface Fase {

        data object Cargando : Fase

        data object SinProductos : Fase

        data class ConProductos(val productos: List<ProductoUi>) : Fase

        data class Error(val mensaje: String) : Fase
    }

    /** Aplica el filtro de texto sobre la lista cargada. */
    fun filtrarPorBusqueda(): List<ProductoUi> {
        return (fase as? Fase.ConProductos)?.productos.orEmpty()
            .filter { it.nombre.contains(busqueda.trim(), ignoreCase = true) }
    }
}