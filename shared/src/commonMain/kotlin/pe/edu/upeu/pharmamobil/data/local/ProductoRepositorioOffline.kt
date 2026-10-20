package pe.edu.upeu.pharmamobil.data.local

import kotlinx.coroutines.flow.Flow
import pe.edu.upeu.pharmamobil.ahoraEnMilisegundos
import pe.edu.upeu.pharmamobil.data.remote.DetalleVentaRequestDto
import pe.edu.upeu.pharmamobil.data.remote.ProductoApi
import pe.edu.upeu.pharmamobil.data.remote.VentaApi
import pe.edu.upeu.pharmamobil.data.remote.VentaRequestDto
import pe.edu.upeu.pharmamobil.data.remote.aDominio
import pe.edu.upeu.pharmamobil.data.remote.aRemoto
import pe.edu.upeu.pharmamobil.data.remote.alAplicacion
import pe.edu.upeu.pharmamobil.domain.ErrorApi
import pe.edu.upeu.pharmamobil.domain.ErrorDeApi
import pe.edu.upeu.pharmamobil.domain.connectivity.MonitorDeConectividad
import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.model.Venta
import pe.edu.upeu.pharmamobil.domain.repository.ProductoRepository
import kotlin.coroutines.cancellation.CancellationException

class ProductoRepositorioOffline(
    private val local: ProductoLocal,
    private val remoto: ProductoApi,
    private val ventaApi: VentaApi,
    private val conectividad: MonitorDeConectividad,
    private val registroSincronizacion: RegistroDeSincronizacion
) : ProductoRepository {

    override fun observar(): Flow<List<Producto>> = local.observar()

    override suspend fun registrar(producto: Producto): Producto = alAplicacion {
        val guardado = local.guardarPendiente(producto)
        intentar { sincronizar() }
        guardado
    }

    override suspend fun actualizar(producto: Producto): Producto = alAplicacion {
        val actualizado = remoto.actualizar(producto.id, producto.aRemoto()).aDominio()
        local.guardar(actualizado)
        actualizado
    }

    override suspend fun eliminar(productoId: Long): Unit = alAplicacion {
        remoto.eliminar(productoId)
        local.borrarPorId(productoId)
        Unit
    }

    override suspend fun listar(): List<Producto> = alAplicacion {
        intentar { sincronizar() }
        local.todas()
    }

    override suspend fun obtenerPorId(productoId: Long): Producto = alAplicacion {
        val localProducto = local.buscarPorId(productoId)
        if (conectividad.estaConectado()) {
            try {
                val respuesta = remoto.obtenerPorId(productoId).aDominio()
                local.guardar(respuesta)
                return@alAplicacion respuesta
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (fallo: Throwable) {
                if (localProducto == null) throw fallo
            }
        }
        localProducto ?: throw ErrorDeApi(
            ErrorApi.NoEncontrado("No se encontró el producto solicitado.")
        )
    }

    override suspend fun registrarVenta(
        clienteId: Long,
        productoId: Long,
        cantidad: Int
    ): Venta = alAplicacion {
        ventaApi.registrar(
            VentaRequestDto(
                clienteId = clienteId,
                detalles = listOf(
                    DetalleVentaRequestDto(productoId = productoId, cantidad = cantidad)
                )
            )
        ).aDominio()
    }

    override suspend fun sincronizar() {
        alAplicacion {
            val falloDeSubida = subirPendientes()
            val remotos = remoto.listar().map { it.aDominio() }.filter { it.activo }
            local.reemplazarSincronizados(remotos)
            registroSincronizacion.registrarUltimaSincronizacion(ahoraEnMilisegundos())
            falloDeSubida?.let { throw it }
        }
    }

    override fun ultimaSincronizacion(): Long? = registroSincronizacion.ultimaSincronizacion()

    private suspend fun subirPendientes(): Throwable? {

        var primerFallo: Throwable? = null

        local.pendientes().forEach { pendiente ->
            try {
                val subido = remoto.registrar(pendiente.aRemoto()).aDominio()
                local.reemplazarPendiente(pendiente.id, subido)
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (fallo: Throwable) {
                if (primerFallo == null) primerFallo = fallo
            }
        }

        return primerFallo
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
