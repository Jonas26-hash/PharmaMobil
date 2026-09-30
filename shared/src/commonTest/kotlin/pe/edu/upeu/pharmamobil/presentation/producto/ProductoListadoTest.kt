package pe.edu.upeu.pharmamobil.presentation.producto

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * El inventario dejó de tener pestañas (TabInventario) y ahora se filtra con
 * una caja de búsqueda en la misma pantalla: este es el contrato del filtro.
 */
class ProductoListadoTest {

    private fun productoUi(
        id: Long,
        nombre: String
    ) = ProductoUi(
        id = id,
        nombre = nombre,
        descripcion = null,
        categoria = "",
        categoriaId = 1L,
        precio = "S/ 10.00",
        precioValor = 10.0,
        stock = "5 u.",
        stockUnidades = 5,
        requiereReceta = false,
        activo = true,
        requiereReposicion = false
    )

    private val inventario = ProductoUiState(
        fase = ProductoUiState.Fase.ConProductos(
            listOf(
                productoUi(id = 1L, nombre = "Paracetamol"),
                productoUi(id = 2L, nombre = "Ibuprofeno"),
                productoUi(id = 3L, nombre = "Amoxicilina")
            )
        )
    )

    @Test
    fun sinTextoDeBusquedaDevuelveTodo() {
        assertEquals(listOf(1L, 2L, 3L), inventario.filtrarPorBusqueda().map { it.id })
    }

    @Test
    fun filtraPorNombreSinImportarMayusculas() {
        assertEquals(
            listOf(2L),
            inventario.copy(busqueda = "IBU").filtrarPorBusqueda().map { it.id }
        )
    }

    @Test
    fun recortaLosEspaciosAntesDeBuscar() {
        assertEquals(
            listOf(3L),
            inventario.copy(busqueda = "  amoxi  ").filtrarPorBusqueda().map { it.id }
        )
    }

    @Test
    fun siNoHayCoincidenciasDevuelveVacio() {
        assertTrue(inventario.copy(busqueda = "Vitamina").filtrarPorBusqueda().isEmpty())
    }

    @Test
    fun sinFaseConProductosDevuelveVacio() {
        assertTrue(ProductoUiState().filtrarPorBusqueda().isEmpty())
    }
}