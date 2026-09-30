package pe.edu.upeu.pharmamobil.domain.model

import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ProductoTest {

    private fun producto(stock: Int, activo: Boolean = true) = Producto(
        id = 1L,
        nombre = "Paracetamol",
        precio = 12.50,
        stock = stock,
        activo = activo
    )

    @Test
    fun requiereReposicionPorDebajoDelStockMinimo() {
        assertTrue(producto(stock = Producto.STOCK_MINIMO - 1).requiereReposicion)
    }

    @Test
    fun noRequiereReposicionEnElStockMinimo() {
        assertFalse(producto(stock = Producto.STOCK_MINIMO).requiereReposicion)
    }

    @Test
    fun unProductoInactivoNoRequiereReposicion() {
        assertFalse(producto(stock = 0, activo = false).requiereReposicion)
    }

    @Test
    fun noSeConstruyeConPrecioCero() {

        assertFailsWith<IllegalArgumentException> {
            Producto(id = 1L, nombre = "Paracetamol", precio = 0.0, stock = 5)
        }
    }

    @Test
    fun noSeConstruyeConStockNegativo() {

        assertFailsWith<IllegalArgumentException> {
            Producto(id = 1L, nombre = "Paracetamol", precio = 12.50, stock = -1)
        }
    }

    @Test
    fun noSeConstruyeSinNombre() {

        assertFailsWith<IllegalArgumentException> {
            Producto(id = 1L, nombre = "  ", precio = 12.50, stock = 5)
        }
    }
}