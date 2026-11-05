package pe.edu.upeu.pharmamobil.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pe.edu.upeu.pharmamobil.db.PharmaDb
import pe.edu.upeu.pharmamobil.domain.model.Cliente

class ClienteLocal(base: PharmaDb) {

    private val consultas = base.clienteQueries

    fun observar(): Flow<List<Cliente>> =
        consultas.listarTodos()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { filas -> filas.map { it.aDominio() } }

    fun todas(): List<Cliente> =
        consultas.listarTodos().executeAsList().map { it.aDominio() }

    fun pendientes(maxIntentos: Long = 5): List<Cliente> =
        consultas.listarPendientes(maxIntentos).executeAsList().map { it.aDominio() }

    fun fallidos(maxIntentos: Long = 5): List<Cliente> =
        consultas.listarFallidos(maxIntentos).executeAsList().map { it.aDominio() }

    fun contarPendientes(maxIntentos: Long = 5): Long =
        consultas.contarPendientes(maxIntentos).executeAsOne()

    fun contarFallidas(maxIntentos: Long = 5): Long =
        consultas.contarFallidas(maxIntentos).executeAsOne()

    fun observarPendientes() = consultas.observarPendientes(5).asFlow().mapToOne(Dispatchers.Default)
    fun observarFallidas() = consultas.observarFallidas(5).asFlow().mapToOne(Dispatchers.Default)

    fun sumarIntento(id: Long, error: String) = consultas.sumarIntento(error, id)

    fun reiniciarIntentos(id: Long) = consultas.reiniciarIntentos(id)

    fun reiniciarIntentosFallidos() = consultas.listarFallidos(5).executeAsList().forEach {
        consultas.reiniciarIntentos(it.id)
    }

    fun fallidosDetallados() = consultas.listarFallidos(5).executeAsList()
    fun limpiarFallidos() = consultas.limpiarFallidos()

    fun buscarPorId(clienteId: Long): Cliente? =
        consultas.buscarPorId(clienteId).executeAsOneOrNull()?.aDominio()

    fun guardarPendiente(cliente: Cliente): Cliente {
        val menor = consultas.siguienteIdLocal().executeAsOne()
        val local = cliente.copy(id = minOf(menor, 0L) - 1L, pendiente = true)
        guardar(local, pendiente = true)
        return local
    }

    fun guardar(cliente: Cliente) {
        guardar(cliente, pendiente = false)
    }

    fun reemplazarPendiente(idTemporal: Long, cliente: Cliente) {
        consultas.transaction {
            consultas.borrarPorId(idTemporal)
            guardar(cliente, pendiente = false)
        }
    }

    fun reemplazarSincronizados(clientes: List<Cliente>) {
        consultas.transaction {
            consultas.listarTodos().executeAsList()
                .filter { it.pendiente == 0L }
                .forEach { consultas.borrarPorId(it.id) }
            clientes.forEach { cliente ->
                guardar(cliente, pendiente = false)
            }
        }
    }

    fun borrarPorId(clienteId: Long) {
        consultas.borrarPorId(clienteId)
    }

    private fun guardar(cliente: Cliente, pendiente: Boolean) {
        consultas.insertar(
            id = cliente.id,
            nombre = cliente.nombre,
            apellido = cliente.apellido,
            dni = cliente.dni,
            telefono = cliente.telefono,
            email = cliente.email,
            direccion = cliente.direccion,
            activo = if (cliente.activo) 1L else 0L,
            pendiente = if (pendiente) 1L else 0L,
            intentos = cliente.intentos.toLong(),
            ultimoError = cliente.ultimoError
        )
    }
}

internal fun pe.edu.upeu.pharmamobil.db.Cliente.aDominio(): Cliente = Cliente(
    id = id,
    nombre = nombre,
    apellido = apellido,
    dni = dni,
    telefono = telefono,
    email = email,
    direccion = direccion,
    activo = activo != 0L,
    pendiente = pendiente != 0L,
    intentos = intentos.toInt(),
    ultimoError = ultimoError
)
