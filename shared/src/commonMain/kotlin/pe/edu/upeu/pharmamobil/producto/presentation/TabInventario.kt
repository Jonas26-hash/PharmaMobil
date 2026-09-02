package pe.edu.upeu.pharmamobil.producto.presentation

import pe.edu.upeu.pharmamobil.producto.domain.model.Medicamento

/**
 * Clasificación del inventario en pestañas.
 *
 * Estructura un solo límite de Bajo Stock para toda la aplicación y lo deja
 * explícito: un producto es de bajo stock únicamente si está activo y su
 * stock es <= LIMITE_BAJO_STOCK. Esto garantiza que las pestañas Activos e
 * Inactivos particionan el inventario y que Bajo Stock es un subconjunto de
 * Activos (los productos inactivos nunca aparecen como de bajo stock, incluso
 * si su stock llega a 0).
 */
enum class TabInventario(val titulo: String) {

    ACTIVOS("Activos"),
    INACTIVOS("Inactivos"),
    BAJO_STOCK("Bajo stock");

    fun filtrar(productos: List<Medicamento>): List<Medicamento> = when (this) {
        ACTIVOS -> productos.filter { it.activo }
        INACTIVOS -> productos.filter { !it.activo }
        BAJO_STOCK -> productos.filter { esBajoStock(it) }
    }

    companion object {
        const val LIMITE_BAJO_STOCK = 5

        fun esBajoStock(producto: Medicamento): Boolean =
            producto.activo && producto.stock <= LIMITE_BAJO_STOCK
    }
}