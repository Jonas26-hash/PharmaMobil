package pe.edu.upeu.pharmamobil.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class CategoriaApi(
    private val client: HttpClient
) {

    suspend fun listar(): List<CategoriaResponseDto> =
        client.get("categorias").body()
}
