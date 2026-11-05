package pe.edu.upeu.pharmamobil.data.local

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.delay
import pe.edu.upeu.pharmamobil.domain.usecase.esperaReintentoMillis
import pe.edu.upeu.pharmamobil.domain.usecase.tipoFalloSync
import pe.edu.upeu.pharmamobil.domain.usecase.TipoFalloSync
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

    suspend fun reintentarFallidos(): Result<Unit> = runCatching {
        local.reiniciarIntentosFallidos()
        sincronizar()
    }

    fun ultimoErrores() = local.fallidosDetallados().mapNotNull { it.ultimoError }

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
            falloDeSubida?.let { throw it }
            registroSincronizacion.registrarUltimaSincronizacion(ahoraEnMilisegundos())
        }
    }

    override fun ultimaSincronizacion(): Long? = registroSincronizacion.ultimaSincronizacion()

    private suspend fun subirPendientes(): Throwable? {

        var primerFallo: Throwable? = null

        local.pendientes(MAX_INTENTOS.toLong()).forEach { pendiente ->
            try {
                val subido = remoto.registrar(pendiente.aRemoto()).aDominio()
                local.reemplazarPendiente(pendiente.id, subido)
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (fallo: Throwable) {
                val error = (fallo as? ErrorDeApi)?.error
                val motivo = ((fallo as? ErrorDeApi)?.mensaje ?: fallo.message)
                    ?.take(240) ?: "Error de sincronización"
                when (error?.tipoFalloSync()) {
                    TipoFalloSync.TRANSITORIO, null -> {
                        local.sumarIntento(pendiente.id, motivo)
                        delay(esperaReintentoMillis(pendiente.intentos))
                    }
                    TipoFalloSync.PERMANENTE -> {
                        repeat((MAX_INTENTOS - pendiente.intentos).coerceAtLeast(1)) {
                            local.sumarIntento(pendiente.id, motivo)
                        }
                    }
                }
                if (primerFallo == null) primerFallo = fallo
            }
        }

        return primerFallo
    }

    private companion object { const val MAX_INTENTOS = 5 }

    private suspend fun intentar(bloque: suspend () -> Unit) {
        try {
            bloque()
        } catch (cancelacion: CancellationException) {
            throw cancelacion
        } catch (_: Throwable) {
        }
    }
}
