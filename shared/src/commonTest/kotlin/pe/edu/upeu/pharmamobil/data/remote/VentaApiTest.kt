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
 * POST /ventas con { clienteId, detalles: [{productoId, cantidad}] }: se
 * verifica el cuerpo publicado y la traducción de [VentaResponseDto] al
 * dominio (total y detalle).
 */
class VentaApiTest {

    @Test
    fun registrarPublicaClienteYDetallesYTraduceLaVenta() = runTest {

        var cuerpo = ""

        val client = crearHttpClient(
            MockEngine { request ->
                assertEquals(HttpMethod.Post.value, request.method.value)
                assertTrue(request.url.encodedPath.endsWith("/ventas"))
                cuerpo = (request.body as TextContent).text
                respond(
                    content = """
                        {
                          "id": 501,
                          "fecha": "2026-08-18T10:30:00",
                          "clienteId": 7,
                          "clienteNombre": "María García",
                          "estado": "COMPLETADA",
                          "total": 31.0,
                          "detalles": [
                            {
                              "productoId": 3,
                              "productoNombre": "Paracetamol",
                              "cantidad": 2,
                              "precio": 15.5,
                              "subtotal": 31.0
                            }
                          ]
                        }
                    """.trimIndent(),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                )
            }
        )

        val repositorio = ProductoRepositorioRemoto(
            api = ProductoApi(client),
            ventaApi = VentaApi(client)
        )

        val venta = repositorio.registrarVenta(
            clienteId = 7L,
            productoId = 3L,
            cantidad = 2
        )

        assertTrue(cuerpo.contains("\"clienteId\":7"))
        assertTrue(cuerpo.contains("\"productoId\":3"))
        assertTrue(cuerpo.contains("\"cantidad\":2"))
        assertEquals("501", venta.id)
        assertEquals("Paracetamol", venta.items.single().nombreProducto)
        assertEquals(31.0, venta.total, 0.0001)
    }
}