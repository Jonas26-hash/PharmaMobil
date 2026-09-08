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
    var fallaAlListar: Throwable? = null

    private var siguienteId = 1L

    override suspend fun registrar(cliente: Cliente): Cliente {

        fallaAlRegistrar?.let { throw it }

        val guardado = cliente.copy(id = siguienteId++)
        clientes.add(guardado)
        return guardado
    }

    override suspend fun listar(): List<Cliente> {

        fallaAlListar?.let { throw it }

        return clientes.toList()
    }

    override fun observarClientes(): Flow<List<Cliente>> = emptyFlow()
}