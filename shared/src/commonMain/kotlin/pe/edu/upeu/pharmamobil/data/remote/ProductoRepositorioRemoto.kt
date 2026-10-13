package pe.edu.upeu.pharmamobil.data.remote

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.model.Venta
import pe.edu.upeu.pharmamobil.domain.repository.ProductoRepository

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
        api.listar().map { it.aDominio() }.filter { it.activo }
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

    override fun observarProductos(): Flow<List<Producto>> = flow {
        emit(listar())
    }
}
