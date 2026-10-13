package pe.edu.upeu.pharmamobil.domain.repository

import kotlinx.coroutines.flow.Flow
import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.model.Venta

interface ProductoRepository {

    suspend fun registrar(producto: Producto): Producto

    suspend fun actualizar(producto: Producto): Producto

    suspend fun eliminar(productoId: Long)

    suspend fun listar(): List<Producto>

    suspend fun obtenerPorId(productoId: Long): Producto

    suspend fun registrarVenta(clienteId: Long, productoId: Long, cantidad: Int): Venta

    fun observarProductos(): Flow<List<Producto>>
}
