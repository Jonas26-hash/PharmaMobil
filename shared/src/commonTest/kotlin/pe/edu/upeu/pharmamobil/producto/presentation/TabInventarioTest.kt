package pe.edu.upeu.pharmamobil.producto.presentation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import pe.edu.upeu.pharmamobil.producto.domain.model.Medicamento

class TabInventarioTest {

    private fun medicamento(
        nombre: String,
        stock: Int,
        activo: Boolean
    ) = Medicamento(
        id = "MED-T",
        nombre = nombre,
        descripcion = null,
        precio = 10.0,
        stock = stock,
        activo = activo,
        requiereReceta = false,
        categoria = "Test"
    )

    private val paracetamol = medicamento("Paracetamol", 100, true)
    private val ibuprofeno = medicamento("Ibuprofeno", 50, true)
    private val amoxicilina = medicamento("Amoxicilina", 5, true)
    private val diclofenaco = medicamento("Diclofenaco", 3, true)
    private val loratadina = medicamento("Loratadina", 0, false)

    private val inventario = listOf(paracetamol, ibuprofeno, amoxicilina, diclofenaco, loratadina)

    @Test
    fun activosDevuelveSoloProductosActivos() {
        val resultado = TabInventario.ACTIVOS.filtrar(inventario)
        assertEquals(listOf("Paracetamol", "Ibuprofeno", "Amoxicilina", "Diclofenaco"), resultado.map { it.nombre })
        assertFalse(resultado.any { !it.activo })
    }

    @Test
    fun inactivosDevuelveSoloProductosInactivos() {
        val resultado = TabInventario.INACTIVOS.filtrar(inventario)
        assertEquals(listOf("Loratadina"), resultado.map { it.nombre })
    }

    @Test
    fun bajoStockUsaLimiteCincoIncluyendoExtremo() {
        val resultado = TabInventario.BAJO_STOCK.filtrar(inventario)
        assertEquals(listOf("Amoxicilina", "Diclofenaco"), resultado.map { it.nombre })
    }

    @Test
    fun bajoStockIncluyeStockCinco() {
        assertTrue(TabInventario.esBajoStock(amoxicilina))
    }

    @Test
    fun bajoStockExcluyeProductosInactivosConStockCero() {
        // Loratadina tiene stock 0 pero está inactiva: NO debe clasificarse como bajo stock.
        assertFalse(TabInventario.esBajoStock(loratadina))
        assertFalse(TabInventario.BAJO_STOCK.filtrar(inventario).contains(loratadina))
    }

    @Test
    fun productoActivoConStockCeroEsBajoStock() {
        val activoCero = medicamento("Activo Sin Stock", 0, true)
        assertTrue(TabInventario.esBajoStock(activoCero))
        assertTrue(TabInventario.BAJO_STOCK.filtrar(listOf(activoCero)).contains(activoCero))
    }

    @Test
    fun bajoStockEsSubconjuntoDeActivos() {
        val bajos = TabInventario.BAJO_STOCK.filtrar(inventario)
        assertTrue(bajos.all { it.activo })
    }

    @Test
    fun limiteDeBajoStockEsCinco() {
        assertEquals(5, TabInventario.LIMITE_BAJO_STOCK)
    }
}