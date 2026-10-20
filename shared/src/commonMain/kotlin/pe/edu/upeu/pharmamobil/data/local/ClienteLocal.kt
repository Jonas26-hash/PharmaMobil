package pe.edu.upeu.pharmamobil.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
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

    fun pendientes(): List<Cliente> =
        consultas.listarPendientes().executeAsList().map { it.aDominio() }

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
            consultas.borrarSincronizados()
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
            pendiente = if (pendiente) 1L else 0L
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
    pendiente = pendiente != 0L
)
