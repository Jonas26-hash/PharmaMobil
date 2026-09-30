package pe.edu.upeu.pharmamobil.domain.repository

import kotlinx.coroutines.flow.Flow
import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.model.Venta

interface ProductoRepository {

    /** Incorpora el producto al inventario y devuelve el producto ya identificado. */
    suspend fun registrar(producto: Producto): Producto

    /** Actualiza un producto existente y devuelve el producto modificado. */
    suspend fun actualizar(producto: Producto): Producto

    /** Da de baja física un producto del inventario. */
    suspend fun eliminar(productoId: Long)

    /** Entrega el inventario completo. */
    suspend fun listar(): List<Producto>

    /** Entrega un producto puntual del inventario. */
    suspend fun obtenerPorId(productoId: Long): Producto

    /** Corta una venta a un cliente: descuenta stock y devuelve la venta con su total. */
    suspend fun registrarVenta(clienteId: Long, productoId: Long, cantidad: Int): Venta

    /** Reacciona a los cambios del inventario (registro, venta, …). */
    fun observarProductos(): Flow<List<Producto>>
}