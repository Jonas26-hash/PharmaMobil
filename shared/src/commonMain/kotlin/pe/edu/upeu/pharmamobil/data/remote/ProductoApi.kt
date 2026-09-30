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
 * Cliente HTTP del inventario. Cada método es la traducción directa de un
 * endpoint de pharmaSoft:
 *  - GET    /api/v1/productos            (paginado)
 *  - GET    /api/v1/productos/{id}
 *  - POST   /api/v1/productos
 *  - PUT    /api/v1/productos/{id}
 *  - DELETE /api/v1/productos/{id}
 */
class ProductoApi(
    private val client: HttpClient
) {

    suspend fun listar(
        pagina: Int = ConfigApi.PAGINA_INICIAL,
        tamanio: Int = ConfigApi.TAMANIO_PAGINA,
        ordenarPor: String = ConfigApi.ORDENAR_POR,
        direccion: String = ConfigApi.DIRECCION
    ): List<ProductoResponseDto> =
        client.get("productos") {
            parameter("pagina", pagina)
            parameter("tamanio", tamanio)
            parameter("ordenarPor", ordenarPor)
            parameter("direccion", direccion)
        }.body<PaginaResponseDto<ProductoResponseDto>>().contenido

    suspend fun obtenerPorId(id: Long): ProductoResponseDto =
        client.get("productos/$id").body()

    suspend fun registrar(dto: ProductoRequestDto): ProductoResponseDto =
        client.post("productos") {
            setBody(dto)
        }.body()

    suspend fun actualizar(id: Long, dto: ProductoRequestDto): ProductoResponseDto =
        client.put("productos/$id") {
            setBody(dto)
        }.body()

    suspend fun eliminar(id: Long) {
        client.delete("productos/$id")
    }
}