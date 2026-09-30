package pe.edu.upeu.pharmamobil.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import pe.edu.upeu.pharmamobil.domain.model.Cliente
import pe.edu.upeu.pharmamobil.domain.repository.ClienteRepository

/** Doble de la cartera de clientes, gemelo de [FakeProductoRepository]. */
class FakeClienteRepository(
    private val clientes: MutableList<Cliente> = mutableListOf()
) : ClienteRepository {

    var fallaAlRegistrar: Throwable? = null
    var fallaAlActualizar: Throwable? = null
    var fallaAlEliminar: Throwable? = null
    var fallaAlListar: Throwable? = null
    var fallaAlObtenerPorId: Throwable? = null

    private var siguienteId = 1L

    override suspend fun registrar(cliente: Cliente): Cliente {

        fallaAlRegistrar?.let { throw it }

        val guardado = cliente.copy(id = siguienteId++)
        clientes.add(guardado)
        return guardado
    }

    override suspend fun actualizar(cliente: Cliente): Cliente {

        fallaAlActualizar?.let { throw it }

        val indice = clientes.indexOfFirst { it.id == cliente.id }
        if (indice < 0) {
            throw NoSuchElementException("Cliente no encontrado con id: ${cliente.id}")
        }

        clientes[indice] = cliente
        return cliente
    }

    override suspend fun eliminar(clienteId: Long) {
        fallaAlEliminar?.let { throw it }
        clientes.removeAll { it.id == clienteId }
    }

    override suspend fun listar(): List<Cliente> {

        fallaAlListar?.let { throw it }

        return clientes.toList()
    }

    override suspend fun obtenerPorId(clienteId: Long): Cliente {

        fallaAlObtenerPorId?.let { throw it }

        return clientes.find { it.id == clienteId }
            ?: throw NoSuchElementException("Cliente no encontrado con id: $clienteId")
    }

    override fun observarClientes(): Flow<List<Cliente>> = emptyFlow()
}