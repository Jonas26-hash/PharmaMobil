package pe.edu.upeu.pharmamobil.presentation.producto

import kotlin.test.Test
import kotlin.test.assertEquals

class TabInventarioTest {

    private fun productoUi(
        id: Long,
        activo: Boolean = true,
        requiereReposicion: Boolean = false,
        stockUnidades: Int = 30
    ) = ProductoUi(
        id = id,
        nombre = "Producto $id",
        descripcion = null,
        categoria = "",
        precio = "S/ 10.00",
        stock = "$stockUnidades u.",
        stockUnidades = stockUnidades,
        requiereReceta = false,
        activo = activo,
        requiereReposicion = requiereReposicion
    )

    private val inventario = listOf(
        productoUi(id = 1L, activo = true, stockUnidades = 30),
        productoUi(id = 2L, activo = true, requiereReposicion = true, stockUnidades = 3),
        productoUi(id = 3L, activo = false, stockUnidades = 0)
    )

    @Test
    fun activosDescartaLosInactivos() {
        assertEquals(
            listOf(1L, 2L),
            TabInventario.ACTIVOS.filtrar(inventario).map { it.id }
        )
    }

    @Test
    fun inactivosSoloContieneLosDesactivados() {
        assertEquals(
            listOf(3L),
            TabInventario.INACTIVOS.filtrar(inventario).map { it.id }
        )
    }

    @Test
    fun bajoStockSoloTomaLosMarcadosParaReponer() {
        assertEquals(
            listOf(2L),
            TabInventario.BAJO_STOCK.filtrar(inventario).map { it.id }
        )
    }

    @Test
    fun activosEInactivosCubrenTodoElInventarioSinRepetir() {

        val cubiertos = TabInventario.ACTIVOS.filtrar(inventario) +
            TabInventario.INACTIVOS.filtrar(inventario)

        assertEquals(inventario.map { it.id }.sorted(), cubiertos.map { it.id }.sorted())
    }

    @Test
    fun losTitulosDescribenLosFiltros() {
        assertEquals("Activos", TabInventario.ACTIVOS.titulo)
        assertEquals("Inactivos", TabInventario.INACTIVOS.titulo)
        assertEquals("Bajo stock", TabInventario.BAJO_STOCK.titulo)
    }
}