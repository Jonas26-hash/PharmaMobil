package pe.edu.upeu.pharmamobil.data.remote

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.model.Venta
import pe.edu.upeu.pharmamobil.domain.repository.ProductoRepository

/**
 * Inventario servido por el backend REST. Implementa la misma interfaz [ProductoRepository]
 * que antes solo tenía la versión en memoria: la presentación no sabe desde
 * dónde llegan los datos. Cada llamada envuelve los fallos de red en
 * [pe.edu.upeu.pharmamobil.domain.ErrorDeAplicacion].
 */
class ProductoRepositorioRemoto(
    private val api: ProductoApi,
    private val ventaApi: VentaApi
) : ProductoRepository {

    override suspend fun registrar(producto: Producto): Producto = alAplicacion {
        api.registrar(producto.aRemoto()).aDominio()
    }

    override suspend fun actualizar(producto: Producto): Producto = alAplicacion {
        api.actualizar(producto.id, producto.aRemoto()).aDominio()
    }

    override suspend fun eliminar(productoId: Long) = alAplicacion {
        api.eliminar(productoId)
    }

    override suspend fun listar(): List<Producto> = alAplicacion {
        api.listar().map { it.aDominio() }
    }

    override suspend fun obtenerPorId(productoId: Long): Producto = alAplicacion {
        api.obtenerPorId(productoId).aDominio()
    }

    override suspend fun registrarVenta(clienteId: Long, productoId: Long, cantidad: Int): Venta =
        alAplicacion {
            ventaApi.registrar(
                VentaRequestDto(
                    clienteId = clienteId,
                    detalles = listOf(
                        DetalleVentaRequestDto(productoId = productoId, cantidad = cantidad)
                    )
                )
            ).aDominio()
        }

    /**
     * El backend no publica eventos en tiempo real: el flujo emite el estado
     * vigente cada vez que alguien lo recolecta (la UI prefiere los casos de
     * uso con listado explícito).
     */
    override fun observarProductos(): Flow<List<Producto>> = flow {
        emit(listar())
    }
}