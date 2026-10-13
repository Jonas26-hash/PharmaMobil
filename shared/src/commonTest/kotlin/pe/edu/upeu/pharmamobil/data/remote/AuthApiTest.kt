package pe.edu.upeu.pharmamobil.data.remote

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Verifica que [AuthApi] habla el contrato de autenticación real de pharmaSoft
 * sin tocar la red:
 *  - POST /api/v1/auth/login con { username, password } y SIN Authorization
 *  - GET /api/v1/auth/me con la cabecera Authorization del token guardado
 */
class AuthApiTest {

    private val json = headersOf(HttpHeaders.ContentType, "application/json")

    private val loginJson = """
        {"token":"tok-123","tipo":"Bearer","expiraEnSegundos":3600,"username":"admin","nombreCompleto":"Ada Admin","roles":["ADMIN"],"permisos":["CATALOGO_LEER","CATALOGO_ESCRIBIR"]}
    """.trimIndent()

    private val perfilJson = """
        {"id":2,"username":"vendedor","empleadoNombre":"Vera Vendedora","cargo":"Vendedora","roles":["VENDEDOR"],"permisos":["CLIENTES_LEER","VENTAS_REGISTRAR"]}
    """.trimIndent()

    @Test
    fun elLoginEsPostAuthLoginConElCuerpoJson() = runTest {

        var ruta = ""
        var metodo = ""
        var cuerpo = ""
        var autorizacion: String? = "cualquiera"

        val client = crearHttpClient(
            MockEngine { request ->
                ruta = request.url.toString()
                metodo = request.method.value
                cuerpo = (request.body as TextContent).text
                autorizacion = request.headers[HttpHeaders.Authorization]
                respond(loginJson, HttpStatusCode.OK, json)
            }
        )

        val resultado = AuthApi(client).iniciarSesion(
            LoginRequestDto(username = "admin", password = "Admin123")
        )

        assertTrue(ruta.endsWith("/auth/login"), "raíz real: $ruta")
        assertEquals(HttpMethod.Post.value, metodo)
        assertTrue(cuerpo.contains("\"username\":\"admin\""))
        assertTrue(cuerpo.contains("\"password\":\"Admin123\""))
        assertTrue(autorizacion.isNullOrBlank(), "el login NO debe llevar token: $autorizacion")
        assertEquals("tok-123", resultado.token)
        assertEquals("Bearer", resultado.tipo)
        assertEquals(3600L, resultado.expiraEnSegundos)
        assertEquals(setOf("CATALOGO_LEER", "CATALOGO_ESCRIBIR"), resultado.permisos.toSet())
    }

    @Test
    fun elMeAdjuntaElTokenGuardadoComoBearer() = runTest {

        var ruta = ""
        var autorizacion: String? = null

        val client = crearHttpClient(
            motor = MockEngine { request ->
                ruta = request.url.toString()
                autorizacion = request.headers[HttpHeaders.Authorization]
                respond(perfilJson, HttpStatusCode.OK, json)
            },
            leerToken = { "tok-9" }
        )

        val resultado = AuthApi(client).perfil()

        assertTrue(ruta.endsWith("/auth/me"), "raíz real: $ruta")
        assertEquals("Bearer tok-9", autorizacion)
        assertEquals("vendedor", resultado.username)
        assertEquals("Vera Vendedora", resultado.empleadoNombre)
        assertEquals(listOf("CLIENTES_LEER", "VENTAS_REGISTRAR"), resultado.permisos)
    }

    @Test
    fun elMeNoPideTokenCuandoNoHaySesion() = runTest {

        var autorizacion: String? = null

        val client = crearHttpClient(
            motor = MockEngine { request ->
                autorizacion = request.headers[HttpHeaders.Authorization]
                respond(perfilJson, HttpStatusCode.OK, json)
            },
            leerToken = { null }
        )

        AuthApi(client).perfil()

        assertTrue(autorizacion.isNullOrBlank())
    }
}
