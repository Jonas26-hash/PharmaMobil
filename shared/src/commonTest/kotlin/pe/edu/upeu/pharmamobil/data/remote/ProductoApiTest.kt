package pe.edu.upeu.pharmamobil.data.remote

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
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
 * Verifica que cada método de [ProductoApi] habla el contrato del backend
 * (rutas, query de paginación, verbo y cuerpo JSON) sin tocar la red.
 */
class ProductoApiTest {

    private val json = headersOf(HttpHeaders.ContentType, "application/json")

    private fun clienteCon(
        respuesta: suspend (ruta: String, request: HttpRequestData) -> String
    ) = crearHttpClient(
        MockEngine { request ->
            respond(
                // request.url.toString() incluye el origen y el path final tal
                // como los resolvería el motor real; encodedPath por sí solo no
                // alcanza para comprobar el prefijo de /api/v1.
                content = respuesta(request.url.toString(), request),
                status = HttpStatusCode.OK,
                headers = json
            )
        }
    )

    private val paginaDeUnProducto = """
        {
          "contenido": [
            {
              "id": 1,
              "nombre": "Paracetamol",
              "precio": 15.5,
              "stock": 3,
              "estado": true,
              "categoriaId": 1,
              "categoriaNombre": "Analgésico"
            }
          ],
          "pagina": 0,
          "tamanio": 100,
          "totalElementos": 1,
          "totalPaginas": 1,
          "ultima": true
        }
    """.trimIndent()

    private val productoJson = """
        {
          "id": 41,
          "nombre": "Paracetamol",
          "precio": 12.5,
          "stock": 5,
          "estado": true,
          "categoriaId": 1
        }
    """.trimIndent()

    @Test
    fun listarUsaElPathPaginadoYDesenvuelveElContenido() = runTest {

        var ruta = ""
        var pagina = ""
        var tamanio = ""

        val client = clienteCon { rutaActual, request ->
            ruta = rutaActual
            pagina = request.url.parameters["pagina"].orEmpty()
            tamanio = request.url.parameters["tamanio"].orEmpty()
            paginaDeUnProducto
        }

        val api = ProductoApi(client)

        val resultado = api.listar()

        // Se compara la URL completa, no solo el suffix: una ruta absoluta
        // ("/productos") perdería el prefijo /api/v1 de ConfigApi y el
        // backend respondería 404 en tiempo de ejecución.
        val urlEsperada = "${ConfigApi.BASE_URL.trimEnd('/')}/productos"
        assertTrue(ruta.startsWith(urlEsperada), "se esperaba raíz $urlEsperada pero fue $ruta")
        assertEquals("0", pagina)
        assertEquals("100", tamanio)
        assertEquals("Paracetamol", resultado.single().nombre)
        assertEquals(3, resultado.single().stock)
        assertEquals("Analgésico", resultado.single().categoriaNombre)
    }

    @Test
    fun obtenerPorIdPoneElIdEnElPath() = runTest {

        var ruta = ""

        val client = clienteCon { rutaActual, _ ->
            ruta = rutaActual
            """{"id":99,"nombre":"Paracetamol","precio":12.5,"stock":5,"estado":true}"""
        }

        val api = ProductoApi(client)

        val resultado = api.obtenerPorId(99)

        assertTrue(ruta.endsWith("/productos/99"))
        assertEquals(99, resultado.id)
    }

    @Test
    fun registrarPublicaElCuerpoJsonYDevuelveElProducto() = runTest {

        var ruta = ""
        var metodo = ""
        var cuerpo = ""

        val client = clienteCon { rutaActual, request ->
            ruta = rutaActual
            metodo = request.method.value
            cuerpo = (request.body as TextContent).text
            productoJson
        }

        val api = ProductoApi(client)

        val resultado = api.registrar(
            ProductoRequestDto(
                nombre = "Paracetamol",
                precio = 12.5,
                stock = 5,
                estado = true,
                categoriaId = 1L
            )
        )

        assertTrue(ruta.endsWith("/productos"))
        assertEquals(HttpMethod.Post.value, metodo)
        assertTrue(cuerpo.contains("\"nombre\":\"Paracetamol\""))
        assertTrue(cuerpo.contains("\"categoriaId\":1"))
        assertEquals(41, resultado.id)
    }

    @Test
    fun eliminarUsaElVerboDelete() = runTest {

        var ruta = ""
        var metodo = ""

        val client = clienteCon { rutaActual, request ->
            ruta = rutaActual
            metodo = request.method.value
            "{}"
        }

        ProductoApi(client).eliminar(5)

        assertTrue(ruta.endsWith("/productos/5"))
        assertEquals(HttpMethod.Delete.value, metodo)
    }
}