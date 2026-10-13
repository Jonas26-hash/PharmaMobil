package pe.edu.upeu.pharmamobil.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class VentaApi(
    private val client: HttpClient
) {

    suspend fun registrar(dto: VentaRequestDto): VentaResponseDto =
        client.post("ventas") {
            setBody(dto)
        }.body()
}
