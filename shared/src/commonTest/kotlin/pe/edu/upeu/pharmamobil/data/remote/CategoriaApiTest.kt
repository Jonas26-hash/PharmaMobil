package pe.edu.upeu.pharmamobil.data.remote

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * El catálogo de categorías es un endpoint plano (sin paginación): se
 * comprueba que [CategoriaApi] lo pide tal cual y que el repositorio remoto
 * lo traduce al dominio.
 */
class CategoriaApiTest {

    @Test
    fun listarDesenvuelveElArregloPlanoDelBackend() = runTest {

        val client = crearHttpClient(
            MockEngine { request ->
                assertTrue(request.url.encodedPath.endsWith("/categorias"))
                respond(
                    content = """
                        [
                          {"id": 1, "nombre": "Analgésico", "descripcion": "Dolor", "activo": true},
                          {"id": 2, "nombre": "Antibiótico", "activo": false}
                        ]
                    """.trimIndent(),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                )
            }
        )

        val repositorio = CategoriaRepositorioRemoto(CategoriaApi(client))

        val categorias = repositorio.listar()

        assertEquals(2, categorias.size)
        assertEquals("Analgésico", categorias.first().nombre)
        assertTrue(categorias.first().activo)
        assertFalse(categorias.last().activo)
    }
}