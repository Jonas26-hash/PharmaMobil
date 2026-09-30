package pe.edu.upeu.pharmamobil.data.repository

import kotlinx.coroutines.test.runTest
import pe.edu.upeu.pharmamobil.domain.model.Producto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ProductoRepositorioEnMemoriaTest {

    private fun nuevoProducto(
        nombre: String,
        precio: Double = 12.50,
        stock: Int = 5
    ) = Producto(
        id = 0L,
        nombre = nombre,
        precio = precio,
        stock = stock
    )

    @Test
    fun asignaElSiguienteIdTrasLaSemilla() = runTest {

        val repositorio = ProductoRepositorioEnMemoria()

        val guardado = repositorio.registrar(nuevoProducto("Losartán"))

        assertEquals(8L, guardado.id)
    }

    @Test
    fun listarDevuelveLaSemillaYLoRegistrado() = runTest {

        val repositorio = ProductoRepositorioEnMemoria()
        repositorio.registrar(nuevoProducto("Losartán"))

        val resultado = repositorio.listar()

        assertEquals(8, resultado.size)
        assertEquals("Losartán", resultado.last().nombre)
    }

    @Test
    fun rechazaUnNombreDuplicadoSinImportarMayusculas() = runTest {

        val repositorio = ProductoRepositorioEnMemoria()

        val fallo = assertFailsWith<IllegalArgumentException> {
            repositorio.registrar(nuevoProducto("paracetamol"))
        }

        assertTrue(fallo.message!!.startsWith("Ya existe un producto con el nombre"))
    }

    @Test
    fun registrarVentaDescuentaStock() = runTest {

        val repositorio = ProductoRepositorioEnMemoria()

        repositorio.registrarVenta(clienteId = 1L, productoId = 1L, cantidad = 1)

        val paracetamol = repositorio.listar().first { it.id == 1L }
        assertEquals(99, paracetamol.stock)
    }

    @Test
    fun laVentaCalculaElTotalDelDetalle() = runTest {

        val repositorio = ProductoRepositorioEnMemoria()

        val venta = repositorio.registrarVenta(clienteId = 1L, productoId = 1L, cantidad = 2)

        assertEquals(2 * 15.50, venta.total, 0.0001)
    }

    @Test
    fun rechazaVenderMasDeLoQueHayEnStock() = runTest {

        val repositorio = ProductoRepositorioEnMemoria()

        val fallo = assertFailsWith<IllegalArgumentException> {
            repositorio.registrarVenta(clienteId = 1L, productoId = 1L, cantidad = 1000)
        }

        assertTrue(fallo.message!!.startsWith("Stock insuficiente"))
    }

    @Test
    fun rechazaVenderCantidadCero() = runTest {

        val repositorio = ProductoRepositorioEnMemoria()

        assertFailsWith<IllegalArgumentException> {
            repositorio.registrarVenta(clienteId = 1L, productoId = 1L, cantidad = 0)
        }
    }
}