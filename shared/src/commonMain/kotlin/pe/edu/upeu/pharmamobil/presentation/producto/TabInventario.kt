package pe.edu.upeu.pharmamobil.presentation.producto

/** Filtros propios del proyecto sobre el inventario, además de la lista completa. */
enum class TabInventario(val titulo: String) {

    ACTIVOS("Activos"),
    INACTIVOS("Inactivos"),
    BAJO_STOCK("Bajo stock");

    fun filtrar(productos: List<ProductoUi>): List<ProductoUi> = when (this) {
        ACTIVOS -> productos.filter { it.activo }
        INACTIVOS -> productos.filter { !it.activo }
        BAJO_STOCK -> productos.filter { it.requiereReposicion }
    }
}