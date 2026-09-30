package pe.edu.upeu.pharmamobil.data.remote

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.utils.io.errors.IOException
import kotlinx.coroutines.test.runTest
import pe.edu.upeu.pharmamobil.domain.ErrorDeAplicacion
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Cada error HTTP y de transporte debe llegar al usuario como un mensaje
 * legible, nunca con los detalles técnicos de Ktor. Se simulan las respuestas
 * con MockEngine y se comprueba tanto [mensajeLegible] como el envoltorio
 * [alAplicacion] de los repositorios remotos.
 */
class MensajesDeErrorTest {

    private fun clienteQueResponde(
        estado: HttpStatusCode,
        cuerpo: String = "{}"
    ) = crearHttpClient(
        MockEngine {
            respond(
                content = cuerpo,
                status = estado,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
    )

    @Test
    fun recursoNoEncontradoTieneSuMensaje() = runTest {

        val repositorio = ProductoRepositorioRemoto(
            ProductoApi(clienteQueResponde(HttpStatusCode.NotFound)),
            VentaApi(clienteQueResponde(HttpStatusCode.NotFound))
        )

        val error = assertFailsWith<ErrorDeAplicacion> {
            repositorio.obtenerPorId(999)
        }

        assertEquals("No se encontró el recurso solicitado.", error.message)
    }

    @Test
    fun losDatosRechazadosTienenSuMensaje() = runTest {
        assertEquals(
            "El servidor rechazó los datos enviados. Revisa el formulario.",
            mensajeAlObtener(HttpStatusCode.BadRequest)
        )
        assertEquals(
            "El servidor rechazó los datos enviados. Revisa el formulario.",
            mensajeAlObtener(HttpStatusCode.UnprocessableEntity)
        )
    }

    @Test
    fun unErrorDelServidorTieneSuMensaje() = runTest {
        assertEquals(
            "El servidor no pudo completar la operación. Inténtalo más tarde.",
            mensajeAlObtener(HttpStatusCode.InternalServerError)
        )
    }

    @Test
    fun unErrorDeTransporteSeTraduceComoFallaDeConexion() {
        assertEquals(
            "No se pudo conectar con el servidor. Verifica tu conexión a internet.",
            IOException("Connection refused").mensajeLegible()
        )
    }

    @Test
    fun unFalloSinClasificarDevuelveSuMensajeOUnGenerico() {
        assertEquals("boom", IllegalStateException("boom").mensajeLegible())
        assertEquals(
            "Se produjo un error inesperado.",
            IllegalStateException().mensajeLegible()
        )
    }

    private suspend fun mensajeAlObtener(estado: HttpStatusCode): String {

        val repositorio = ProductoRepositorioRemoto(
            ProductoApi(clienteQueResponde(estado)),
            VentaApi(clienteQueResponde(estado))
        )

        return assertFailsWith<ErrorDeAplicacion> {
            repositorio.obtenerPorId(1)
        }.message.orEmpty()
    }
}