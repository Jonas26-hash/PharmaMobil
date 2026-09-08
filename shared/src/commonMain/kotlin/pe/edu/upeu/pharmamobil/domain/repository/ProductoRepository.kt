package pe.edu.upeu.pharmamobil.domain.repository

import kotlinx.coroutines.flow.Flow
import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.model.Venta

interface ProductoRepository {

    /** Incorpora el producto al inventario y devuelve el producto ya identificado. */
    suspend fun registrar(producto: Producto): Producto

    /** Entrega el inventario completo en el orden en que fue registrado. */
    suspend fun listar(): List<Producto>

    /** Corta una venta: descuenta stock y devuelve la venta con su total. */
    suspend fun registrarVenta(productoId: Long, cantidad: Int): Venta

    /** Reacciona a los cambios del inventario (registro, venta, …). */
    fun observarProductos(): Flow<List<Producto>>
}