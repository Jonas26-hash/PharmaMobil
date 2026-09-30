package pe.edu.upeu.pharmamobil.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

/** Cliente HTTP del catálogo de categorías: GET /api/v1/categorias. */
class CategoriaApi(
    private val client: HttpClient
) {

    suspend fun listar(): List<CategoriaResponseDto> =
        client.get("categorias").body()
}