package pe.edu.upeu.pharmamobil.data.local

import kotlinx.coroutines.flow.Flow
import pe.edu.upeu.pharmamobil.data.remote.ClienteApi
import pe.edu.upeu.pharmamobil.data.remote.aDominio
import pe.edu.upeu.pharmamobil.data.remote.aRemoto
import pe.edu.upeu.pharmamobil.data.remote.alAplicacion
import pe.edu.upeu.pharmamobil.domain.ErrorApi
import pe.edu.upeu.pharmamobil.domain.ErrorDeApi
import pe.edu.upeu.pharmamobil.domain.connectivity.MonitorDeConectividad
import pe.edu.upeu.pharmamobil.domain.model.Cliente
import pe.edu.upeu.pharmamobil.domain.repository.ClienteRepository
import kotlin.coroutines.cancellation.CancellationException

class ClienteRepositorioOffline(
    private val local: ClienteLocal,
    private val remoto: ClienteApi,
    private val conectividad: MonitorDeConectividad
) : ClienteRepository {

    override fun observar(): Flow<List<Cliente>> = local.observar()

    override suspend fun registrar(cliente: Cliente): Cliente = alAplicacion {
        val guardado = local.guardarPendiente(cliente)
        intentar { sincronizar() }
        guardado
    }

    override suspend fun actualizar(cliente: Cliente): Cliente = alAplicacion {
        val actualizado = remoto.actualizar(cliente.id, cliente.aRemoto()).aDominio()
        local.guardar(actualizado)
        actualizado
    }

    override suspend fun eliminar(clienteId: Long): Unit = alAplicacion {
        remoto.eliminar(clienteId)
        local.borrarPorId(clienteId)
        Unit
    }

    override suspend fun listar(): List<Cliente> = alAplicacion {
        intentar { sincronizar() }
        local.todas()
    }

    override suspend fun obtenerPorId(clienteId: Long): Cliente = alAplicacion {
        val localCliente = local.buscarPorId(clienteId)
        if (conectividad.estaConectado()) {
            try {
                val respuesta = remoto.obtenerPorId(clienteId).aDominio()
                local.guardar(respuesta)
                return@alAplicacion respuesta
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (fallo: Throwable) {
                if (localCliente == null) throw fallo
            }
        }
        localCliente ?: throw ErrorDeApi(
            ErrorApi.NoEncontrado("No se encontró el cliente solicitado.")
        )
    }

    override suspend fun sincronizar() {
        alAplicacion {
            subirPendientes()
            val remotos = remoto.listar().map { it.aDominio() }.filter { it.activo }
            local.reemplazarSincronizados(remotos)
        }
    }

    private suspend fun subirPendientes() {
        local.pendientes().forEach { pendiente ->
            val subido = remoto.registrar(pendiente.aRemoto()).aDominio()
            local.reemplazarPendiente(pendiente.id, subido)
        }
    }

    private suspend fun intentar(bloque: suspend () -> Unit) {
        try {
            bloque()
        } catch (cancelacion: CancellationException) {
            throw cancelacion
        } catch (_: Throwable) {
        }
    }
}
