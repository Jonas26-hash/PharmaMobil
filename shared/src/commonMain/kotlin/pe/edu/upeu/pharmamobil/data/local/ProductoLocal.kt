package pe.edu.upeu.pharmamobil.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
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

    fun pendientes(maxIntentos: Long = 5): List<Producto> =
        consultas.listarPendientes(maxIntentos).executeAsList().map { it.aDominio() }

    fun fallidos(maxIntentos: Long = 5): List<Producto> =
        consultas.listarFallidos(maxIntentos).executeAsList().map { it.aDominio() }

    fun contarPendientes(maxIntentos: Long = 5): Long =
        consultas.contarPendientes(maxIntentos).executeAsOne()

    fun contarFallidas(maxIntentos: Long = 5): Long =
        consultas.contarFallidas(maxIntentos).executeAsOne()

    fun observarPendientes() = consultas.observarPendientes(5).asFlow().mapToOne(Dispatchers.Default)
    fun observarFallidas() = consultas.observarFallidas(5).asFlow().mapToOne(Dispatchers.Default)

    fun sumarIntento(id: Long, error: String) = consultas.sumarIntento(error, id)

    fun reiniciarIntentosFallidos() = consultas.listarFallidos(5).executeAsList().forEach {
        consultas.reiniciarIntentos(it.id)
    }

    fun fallidosDetallados() = consultas.listarFallidos(5).executeAsList()
    fun limpiarFallidos() = consultas.limpiarFallidos()

    fun reiniciarIntentos(id: Long) = consultas.reiniciarIntentos(id)

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
            consultas.listarTodos().executeAsList()
                .filter { it.pendiente == 0L }
                .forEach { consultas.borrarPorId(it.id) }
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
            pendiente = if (pendiente) 1L else 0L,
            intentos = producto.intentos.toLong(),
            ultimoError = producto.ultimoError
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
    pendiente = pendiente != 0L,
    intentos = intentos.toInt(),
    ultimoError = ultimoError
)
