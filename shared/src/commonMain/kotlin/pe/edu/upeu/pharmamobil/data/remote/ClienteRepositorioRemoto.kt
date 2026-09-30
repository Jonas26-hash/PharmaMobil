package pe.edu.upeu.pharmamobil.data.remote

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import pe.edu.upeu.pharmamobil.domain.model.Cliente
import pe.edu.upeu.pharmamobil.domain.repository.ClienteRepository

/**
 * Cartera servida por el backend REST. Gemelo de [ProductoRepositorioRemoto]:
 * implementa la interfaz de dominio y traduce los fallos de red a
 * [pe.edu.upeu.pharmamobil.domain.ErrorDeAplicacion].
 */
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

    /**
     * Igual que en productos, el backend ignora el filtro `?estado=`, así que
     * se descartan aquí los clientes dados de baja (`DELETE` es baja lógica).
     */
    override suspend fun listar(): List<Cliente> = alAplicacion {
        api.listar().map { it.aDominio() }.filter { it.activo }
    }

    override suspend fun obtenerPorId(clienteId: Long): Cliente = alAplicacion {
        api.obtenerPorId(clienteId).aDominio()
    }

    /** El backend no publica eventos en tiempo real: emite el estado vigente. */
    override fun observarClientes(): Flow<List<Cliente>> = flow {
        emit(listar())
    }
}