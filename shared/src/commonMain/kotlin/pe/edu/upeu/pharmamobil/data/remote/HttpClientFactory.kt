package pe.edu.upeu.pharmamobil.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.accept
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Motor HTTP de cada plataforma:
 * - Android usa OkHttp.
 * - iOS usa Darwin (NSURLSession).
 */
expect fun crearMotorHttp(): HttpClientEngine

/**
 * Ruta del endpoint de login. El proveedor Bearer nunca adjunta un token a esta
 * petición, porque es justamente la que se ejecuta sin sesión previa.
 */
private fun Url.esLogin(): Boolean = encodedPath.endsWith("/auth/login")

/**
 * Configuración centralizada del cliente HTTP. Se construye una sola vez y
 * Koin lo comparte como single: nunca se crea un HttpClient por petición.
 *
 * @param leerToken devuelve el token guardado (o `null`). El plugin [Auth] lo
 *   usa como `loadTokens`, de modo que **ningún repositorio añade la cabecera
 *   `Authorization` a mano**.
 * @param alExpirarSesion se invoca cuando una operación protegida recibe 401,
 *   para que el manejador de sesión borre el token y la app vuelva al login.
 *   Un 403 no dispara este aviso: la sesión sigue siendo válida, solo falta
 *   permiso para esa operación.
 */
fun crearHttpClient(
    motor: HttpClientEngine,
    leerToken: suspend () -> String? = { null },
    alExpirarSesion: suspend () -> Unit = {}
): HttpClient = HttpClient(motor) {

    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
                isLenient = true
                encodeDefaults = true
                explicitNulls = false
            }
        )
    }

    install(Logging) {
        level = LogLevel.INFO
        // El token NUNCA debe aparecer en los logs: la cabecera Authorization
        // se redacta aunque el nivel incluya cabeceras.
        sanitizeHeader { header -> header.equals(HttpHeaders.Authorization, ignoreCase = true) }
    }

    install(HttpTimeout) {
        requestTimeoutMillis = 15_000
        connectTimeoutMillis = 10_000
        socketTimeoutMillis = 15_000
    }

    install(Auth) {
        bearer {
            // Carga el token desde el almacén seguro en cuanto se necesita.
            loadTokens { leerToken()?.let { BearerTokens(it, null) } }

            // pharmaSoft no ofrece refresco: devolver null obliga a un nuevo
            // login en lugar de reintentar con un token inventado o vacío.
            refreshTokens { null }

            // Adjunta el token salvo en el login (sin sesión previa). Evita el
            // reintento innecesario que dispararía un segundo login por el 401.
            sendWithoutRequest { request -> !request.url.build().esLogin() }
        }
    }

    // La base del backend se declara una sola vez en ConfigApi.
    defaultRequest {
        // OJO con la barra final: si la base no termina en "/", Ktor interpreta
        // el último segmento (v1) como archivo y al resolver una ruta relativa
        // ("productos") lo reemplaza -> /api/productos. Con "/" al final,
        // "v1/" es un directorio y la ruta relativa se resuelve de verdad:
        // /api/v1/productos.
        url("${ConfigApi.BASE_URL.trimEnd('/')}/")
        accept(ContentType.Application.Json)
        contentType(ContentType.Application.Json)
    }

    // Tratamiento central del 401: al expirar, se avisa al manejador de sesión
    // para borrar el token y volver al login. El 403 no cierra la sesión.
    HttpResponseValidator {
        validateResponse { response ->
            if (response.status == HttpStatusCode.Unauthorized &&
                !response.call.request.url.esLogin()
            ) {
                alExpirarSesion()
            }
        }
    }

    // Los errores HTTP (4xx/5xx) se mapean como excepciones tipadas.
    expectSuccess = true
}
