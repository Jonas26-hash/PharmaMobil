package pe.edu.upeu.pharmamobil.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody

/**
 * Cliente HTTP de autenticación. Cada método es la traducción directa de un
 * endpoint esperado de pharmaSoft:
 *  - POST /api/v1/auth/login
 *  - GET  /api/v1/auth/me
 *
 * Ninguno de los dos añade la cabecera Authorization a mano: de eso se encarga
 * el plugin Auth del [HttpClient] inyectado.
 */
class AuthApi(
    private val client: HttpClient
) {

    /** Inicia sesión sin token previo y devuelve el token y el perfil. */
    suspend fun iniciarSesion(dto: LoginRequestDto): LoginResponseDto =
        client.post("auth/login") {
            setBody(dto)
        }.body()

    /** Perfil y permisos de la sesión vigente (requiere token). */
    suspend fun perfil(): UsuarioResponseDto =
        client.get("auth/me").body()
}
