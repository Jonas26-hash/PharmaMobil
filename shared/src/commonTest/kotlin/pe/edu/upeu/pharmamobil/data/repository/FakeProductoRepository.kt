package pe.edu.upeu.pharmamobil.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import pe.edu.upeu.pharmamobil.domain.model.DetalleVenta
import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.model.Venta
import pe.edu.upeu.pharmamobil.domain.repository.ProductoRepository

/**
 * Doble del inventario para las pruebas: sin delay y capaz de fallar a
 * voluntad. Sin él, probar el camino de error del caso de uso o del ViewModel
 * era imposible, porque el repositorio en memoria nunca falla.
 */
class FakeProductoRepository(
    private val productos: MutableList<Producto> = mutableListOf()
) : ProductoRepository {

    var fallaAlRegistrar: Throwable? = null
    var fallaAlListar: Throwable? = null
    var fallaAlVender: Throwable? = null

    private var siguienteId = 1L

    override suspend fun registrar(producto: Producto): Producto {

        fallaAlRegistrar?.let { throw it }

        val guardado = producto.copy(id = siguienteId++)
        productos.add(guardado)
        return guardado
    }

    override suspend fun listar(): List<Producto> {

        fallaAlListar?.let { throw it }

        return productos.toList()
    }

    override suspend fun registrarVenta(productoId: Long, cantidad: Int): Venta {

        fallaAlVender?.let { throw it }

        val producto = productos.find { it.id == productoId }
            ?: throw NoSuchElementException("Producto no encontrado con id: $productoId")

        if (producto.stock < cantidad) {
            throw IllegalArgumentException("Stock insuficiente para ${producto.nombre}")
        }

        val actualizado = producto.copy(stock = producto.stock - cantidad)
        productos[productos.indexOf(producto)] = actualizado

        val detalle = DetalleVenta.crear(
            productoId = productoId,
            nombreProducto = producto.nombre,
            cantidad = cantidad,
            precioUnitario = producto.precio
        )

        return Venta.crear(
            id = "VTN-TEST",
            fecha = "2026-08-18 08:00",
            items = listOf(detalle)
        )
    }

    override fun observarProductos(): Flow<List<Producto>> = emptyFlow()
}