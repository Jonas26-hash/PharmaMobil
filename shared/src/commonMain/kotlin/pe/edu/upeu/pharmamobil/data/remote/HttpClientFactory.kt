package pe.edu.upeu.pharmamobil.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.accept
import io.ktor.client.request.url
import io.ktor.http.ContentType
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
 * Configuración centralizada del cliente HTTP. Se construye una sola vez y
 * Koin lo comparte como single: nunca se crea un HttpClient por petición.
 *
 * La configuración (negociación de contenido, logs, tiempos de espera y URL
 * base) no depende de la plataforma; solo el motor llega por [crearMotorHttp].
 */
fun crearHttpClient(motor: HttpClientEngine): HttpClient = HttpClient(motor) {

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
    }

    install(HttpTimeout) {
        requestTimeoutMillis = 15_000
        connectTimeoutMillis = 10_000
        socketTimeoutMillis = 15_000
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

    // Los errores HTTP (4xx/5xx) se mapean como excepciones tipadas.
    expectSuccess = true
}