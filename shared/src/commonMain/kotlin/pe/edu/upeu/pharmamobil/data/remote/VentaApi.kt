package pe.edu.upeu.pharmamobil.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody

/**
 * Cliente HTTP de ventas. El contrato del backend es
 * POST /api/v1/ventas con { clienteId, detalles: [{productoId, cantidad}] }.
 * La sección de Pedidos de la app aún no lo expone en pantalla, pero el
 * repositorio y el caso de uso ya caminan sobre este endpoint real.
 */
class VentaApi(
    private val client: HttpClient
) {

    suspend fun registrar(dto: VentaRequestDto): VentaResponseDto =
        client.post("ventas") {
            setBody(dto)
        }.body()
}