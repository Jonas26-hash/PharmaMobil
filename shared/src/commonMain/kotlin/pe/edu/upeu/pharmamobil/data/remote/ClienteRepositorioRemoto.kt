package pe.edu.upeu.pharmamobil.data.remote

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import pe.edu.upeu.pharmamobil.domain.model.Cliente
import pe.edu.upeu.pharmamobil.domain.repository.ClienteRepository

class ClienteRepositorioRemoto(
    private val api: ClienteApi
) : ClienteRepository {

    override suspend fun registrar(cliente: Cliente): Cliente = alAplicacion {
        api.registrar(cliente.aRemoto()).aDominio()
    }

    override suspend fun actualizar(cliente: Cliente): Cliente = alAplicacion {
        api.actualizar(cliente.id, cliente.aRemoto()).aDominio()
    }

    override suspend fun eliminar(clienteId: Long) = alAplicacion {
        api.eliminar(clienteId)
    }

    override suspend fun listar(): List<Cliente> = alAplicacion {
        api.listar().map { it.aDominio() }.filter { it.activo }
    }

    override suspend fun obtenerPorId(clienteId: Long): Cliente = alAplicacion {
        api.obtenerPorId(clienteId).aDominio()
    }

    override suspend fun sincronizar() = alAplicacion {
        Unit
    }

    override fun observar(): Flow<List<Cliente>> = flow {
        emit(listar())
    }
}
