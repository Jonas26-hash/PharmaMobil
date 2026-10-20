package pe.edu.upeu.pharmamobil.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pe.edu.upeu.pharmamobil.db.PharmaDb
import pe.edu.upeu.pharmamobil.domain.model.Producto

class ProductoLocal(base: PharmaDb) {

    private val consultas = base.productoQueries

    fun observar(): Flow<List<Producto>> =
        consultas.listarTodos()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { filas -> filas.map { it.aDominio() } }

    fun todas(): List<Producto> =
        consultas.listarTodos().executeAsList().map { it.aDominio() }

    fun pendientes(): List<Producto> =
        consultas.listarPendientes().executeAsList().map { it.aDominio() }

    fun buscarPorId(productoId: Long): Producto? =
        consultas.buscarPorId(productoId).executeAsOneOrNull()?.aDominio()

    fun guardarPendiente(producto: Producto): Producto {
        val menor = consultas.siguienteIdLocal().executeAsOne()
        val local = producto.copy(id = minOf(menor, 0L) - 1L, pendiente = true)
        guardar(local, pendiente = true)
        return local
    }

    fun guardar(producto: Producto) {
        guardar(producto, pendiente = false)
    }

    fun reemplazarPendiente(idTemporal: Long, producto: Producto) {
        consultas.transaction {
            consultas.borrarPorId(idTemporal)
            guardar(producto, pendiente = false)
        }
    }

    fun reemplazarSincronizados(productos: List<Producto>) {
        consultas.transaction {
            consultas.borrarSincronizados()
            productos.forEach { producto ->
                guardar(producto, pendiente = false)
            }
        }
    }

    fun borrarPorId(productoId: Long) {
        consultas.borrarPorId(productoId)
    }

    private fun guardar(producto: Producto, pendiente: Boolean) {
        consultas.insertar(
            id = producto.id,
            nombre = producto.nombre,
            descripcion = producto.descripcion,
            precio = producto.precio,
            stock = producto.stock.toLong(),
            requiere_receta = if (producto.requiereReceta) 1L else 0L,
            categoria = producto.categoria,
            categoria_id = producto.categoriaId,
            activo = if (producto.activo) 1L else 0L,
            pendiente = if (pendiente) 1L else 0L
        )
    }
}

internal fun pe.edu.upeu.pharmamobil.db.Producto.aDominio(): Producto = Producto(
    id = id,
    nombre = nombre,
    descripcion = descripcion,
    precio = precio,
    stock = stock.toInt(),
    requiereReceta = requiere_receta != 0L,
    categoria = categoria,
    categoriaId = categoria_id,
    activo = activo != 0L,
    pendiente = pendiente != 0L
)
