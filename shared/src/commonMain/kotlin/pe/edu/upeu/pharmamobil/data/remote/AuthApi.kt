package pe.edu.upeu.pharmamobil.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class AuthApi(
    private val client: HttpClient
) {

    suspend fun iniciarSesion(dto: LoginRequestDto): LoginResponseDto =
        client.post("auth/login") {
            setBody(dto)
        }.body()

    suspend fun perfil(): UsuarioResponseDto =
        client.get("auth/me").body()
}
