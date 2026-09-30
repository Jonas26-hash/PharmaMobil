package pe.edu.upeu.pharmamobil.data.remote

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * El backend aplica la baja de forma lógica: `DELETE` devuelve 204 y deja el
 * registro con `estado = false`, y ni `GET /productos` ni el parámetro
 * `?estado=` lo descartan. Estos tests fijan ese comportamiento para que el
 * inventario que ve el usuario no muestre lo que ya dio de baja.
 */
class ListadoDeActivosTest {

    private val json = headersOf(HttpHeaders.ContentType, "application/json")

    private val paginaConActivoYInactivo = """
        {
          "contenido": [
            {
              "id": 1,
              "nombre": "Paracetamol 500mg",
              "precio": 8.5,
              "stock": 120,
              "estado": true,
              "categoriaId": 1,
              "categoriaNombre": "Analgesicos"
            },
            {
              "id": 21,
              "nombre": "Producto dado de baja",
              "precio": 99.9,
              "stock": 42,
              "estado": false,
              "categoriaId": 1,
              "categoriaNombre": "Analgesicos"
            }
          ],
          "pagina": 0,
          "totalElementos": 2
        }
    """.trimIndent()

    private val paginaDeClientes = """
        {
          "contenido": [
            {
              "id": 1,
              "dni": "71234567",
              "nombres": "Ana",
              "apellidos": "Rodriguez",
              "email": "ana@upeu.edu.pe",
              "telefono": "987654321",
              "direccion": "Av. Castilla 123",
              "estado": true
            },
            {
              "id": 9,
              "dni": "99999999",
              "nombres": "Cliente",
              "apellidos": "De Baja",
              "email": "baja@upeu.edu.pe",
              "telefono": "900000000",
              "direccion": "Sin domicilio",
              "estado": false
            }
          ]
        }
    """.trimIndent()

    private fun clienteMock(cuerpo: String) = crearHttpClient(
        MockEngine { respond(cuerpo, HttpStatusCode.OK, json) }
    )

    @Test
    fun elListadoDeProductosOcultaLosDadosDeBaja() = runTest {
        val repositorio = ProductoRepositorioRemoto(
            ProductoApi(clienteMock(paginaConActivoYInactivo)),
            VentaApi(clienteMock(paginaConActivoYInactivo))
        )

        val productos = repositorio.listar()

        assertEquals(1, productos.size)
        assertEquals("Paracetamol 500mg", productos.first().nombre)
        assertTrue(productos.all { it.activo })
    }

    @Test
    fun elListadoDeClientesOcultaLosDadosDeBaja() = runTest {
        val repositorio = ClienteRepositorioRemoto(ClienteApi(clienteMock(paginaDeClientes)))

        val clientes = repositorio.listar()

        assertEquals(1, clientes.size)
        assertEquals("71234567", clientes.first().dni)
        assertTrue(clientes.all { it.activo })
    }

    @Test
    fun obtenerPorIdSigueDevolviendoElRegistroDesactivado() = runTest {
        // Un producto concreto llega como objeto simple, no envuelto en la
        // página de la lista.
        val productoDesactivado = """
            {
              "id": 21,
              "nombre": "Producto dado de baja",
              "precio": 99.9,
              "stock": 42,
              "estado": false,
              "categoriaId": 1,
              "categoriaNombre": "Analgesicos"
            }
        """.trimIndent()

        val repositorio = ProductoRepositorioRemoto(
            ProductoApi(clienteMock(productoDesactivado)),
            VentaApi(clienteMock(productoDesactivado))
        )

        val producto = repositorio.obtenerPorId(21)

        assertEquals("Producto dado de baja", producto.nombre)
        assertTrue(!producto.activo)
    }
}
