package pe.edu.upeu.pharmamobil.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody

/**
 * Cliente HTTP de la cartera de clientes. Traduce los endpoints de
 * pharmaSoft:
 *  - GET    /api/v1/clientes            (paginado)
 *  - GET    /api/v1/clientes/{id}
 *  - POST   /api/v1/clientes
 *  - PUT    /api/v1/clientes/{id}
 *  - DELETE /api/v1/clientes/{id}
 */
class ClienteApi(
    private val client: HttpClient
) {

    suspend fun listar(
        pagina: Int = ConfigApi.PAGINA_INICIAL,
        tamanio: Int = ConfigApi.TAMANIO_PAGINA,
        ordenarPor: String = ConfigApi.ORDENAR_POR,
        direccion: String = ConfigApi.DIRECCION
    ): List<ClienteResponseDto> =
        client.get("clientes") {
            parameter("pagina", pagina)
            parameter("tamanio", tamanio)
            parameter("ordenarPor", ordenarPor)
            parameter("direccion", direccion)
        }.body<PaginaResponseDto<ClienteResponseDto>>().contenido

    suspend fun obtenerPorId(id: Long): ClienteResponseDto =
        client.get("clientes/$id").body()

    suspend fun registrar(dto: ClienteRequestDto): ClienteResponseDto =
        client.post("clientes") {
            setBody(dto)
        }.body()

    suspend fun actualizar(id: Long, dto: ClienteRequestDto): ClienteResponseDto =
        client.put("clientes/$id") {
            setBody(dto)
        }.body()

    suspend fun eliminar(id: Long) {
        client.delete("clientes/$id")
    }
}