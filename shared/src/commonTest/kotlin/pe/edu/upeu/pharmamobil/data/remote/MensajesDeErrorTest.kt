package pe.edu.upeu.pharmamobil.data.remote

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.utils.io.errors.IOException
import kotlinx.coroutines.test.runTest
import pe.edu.upeu.pharmamobil.domain.ErrorApi
import pe.edu.upeu.pharmamobil.domain.ErrorDeApi
import pe.edu.upeu.pharmamobil.domain.esFalloDeAutenticacion
import pe.edu.upeu.pharmamobil.domain.mensajeParaUsuario
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Cada error HTTP y de transporte debe llegar al dominio como un [ErrorApi]
 * tipado y con un mensaje legible, nunca con los detalles técnicos de Ktor.
 *
 * Se simulan las respuestas con MockEngine y se comprueba el envoltorio
 * [alAplicacion] que usan los repositorios remotos.
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

        val error = assertFailsWith<ErrorDeApi> {
            repositorio.obtenerPorId(999)
        }

        assertEquals(
            ErrorApi.NoEncontrado("No se encontró el recurso solicitado."),
            error.error
        )
    }

    @Test
    fun losDatosRechazadosTienenSuMensaje() = runTest {
        assertEquals(
            "El servidor rechazó los datos enviados.",
            mensajeDeValidacionAlObtener(HttpStatusCode.BadRequest)
        )
        assertEquals(
            "El servidor rechazó los datos enviados.",
            mensajeDeValidacionAlObtener(HttpStatusCode.UnprocessableEntity)
        )
    }

    @Test
    fun unErrorDelServidorTieneSuMensaje() = runTest {
        assertEquals(
            "El servidor no pudo completar la operación. Inténtalo más tarde.",
            mensajeParaUsuarioAlObtener(HttpStatusCode.InternalServerError)
        )
    }

    @Test
    fun unErrorDeTransporteSeTraduceComoFallaDeConexion() = runTest {
        val error = assertFailsWith<ErrorDeApi> {
            alAplicacion { throw IOException("Connection refused") }
        }
        assertEquals(
            ErrorApi.SinConexion("No se pudo conectar con el servidor. Verifica tu conexión."),
            error.error
        )
    }

    @Test
    fun unFalloSinClasificarDevuelveSuMensajeOUnGenerico() = runTest {
        val conMensaje = IllegalStateException("boom").aErrorApi() as ErrorApi.Servidor
        val sinMensaje = IllegalStateException().aErrorApi() as ErrorApi.Servidor

        assertEquals("boom", conMensaje.mensaje)
        assertEquals("Se produjo un error inesperado.", sinMensaje.mensaje)
    }

    /** GET real contra el repositorio y mensaje del [ErrorApi.Validacion]. */
    private suspend fun mensajeDeValidacionAlObtener(estado: HttpStatusCode): String {
        val repositorio = ProductoRepositorioRemoto(
            ProductoApi(clienteQueResponde(estado)),
            VentaApi(clienteQueResponde(estado))
        )
        val error = assertFailsWith<ErrorDeApi> { repositorio.obtenerPorId(1) }
        return (error.error as ErrorApi.Validacion).mensaje
    }

    /** GET real contra el repositorio y mensaje legible del [ErrorApi.Servidor]. */
    private suspend fun mensajeParaUsuarioAlObtener(estado: HttpStatusCode): String {
        val repositorio = ProductoRepositorioRemoto(
            ProductoApi(clienteQueResponde(estado)),
            VentaApi(clienteQueResponde(estado))
        )
        val error = assertFailsWith<ErrorDeApi> { repositorio.obtenerPorId(1) }
        return error.error.mensajeParaUsuario()
    }
}

/**
 * Cobertura específica de la traducción a [ErrorApi] usando cuerpos con la
 * forma real de `ErrorResponseDTO` de pharmaSoft.
 */
class TraduccionErrorApiTest {

    private fun clienteQueResponde(estado: HttpStatusCode, cuerpo: String) =
        crearHttpClient(
            MockEngine {
                respond(
                    content = cuerpo,
                    status = estado,
                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                )
            }
        )

    private suspend fun errorAlObtener(
        estado: HttpStatusCode,
        cuerpo: String
    ): ErrorApi {
        val repositorio = ProductoRepositorioRemoto(
            ProductoApi(clienteQueResponde(estado, cuerpo)),
            VentaApi(clienteQueResponde(estado, cuerpo))
        )
        val error = assertFailsWith<ErrorDeApi> { repositorio.obtenerPorId(1) }
        return error.error
    }

    @Test
    fun el404SeTraduceANoEncontradoConElMensajeDelServidor() = runTest {
        val error = errorAlObtener(
            HttpStatusCode.NotFound,
            """{"status":404,"error":"Not Found","message":"Producto no encontrado con id: 999","path":"/api/v1/productos/999"}"""
        )

        assertEquals(ErrorApi.NoEncontrado("Producto no encontrado con id: 999"), error)
    }

    @Test
    fun el409SeTraduceAConflictoConElMensajeDelServidor() = runTest {
        val error = errorAlObtener(
            HttpStatusCode.Conflict,
            """{"status":409,"error":"Conflict","message":"Ya existe un producto con el nombre Paracetamol"}"""
        )

        assertEquals(ErrorApi.Conflicto("Ya existe un producto con el nombre Paracetamol"), error)
    }

    @Test
    fun el400SeTraduceAValidacionYConservaLosErroresPorCampo() = runTest {
        val error = errorAlObtener(
            HttpStatusCode.BadRequest,
            """
            {
              "status":400,
              "error":"Bad Request",
              "message":"Existen errores de validación",
              "validationErrors":{
                "nombre":"El nombre debe tener entre 3 y 150 caracteres",
                "precio":"El precio debe ser mayor que cero",
                "stock":"El stock no puede ser negativo",
                "categoriaId":"La categoría es obligatoria"
              }
            }
            """.trimIndent()
        )

        assertIs<ErrorApi.Validacion>(error)
        assertEquals("Existen errores de validación", error.mensaje)
        assertEquals(
            "El nombre debe tener entre 3 y 150 caracteres",
            error.erroresPorCampo["nombre"]
        )
        assertEquals(
            "El precio debe ser mayor que cero",
            error.erroresPorCampo["precio"]
        )
        assertEquals(
            "El stock no puede ser negativo",
            error.erroresPorCampo["stock"]
        )
        assertEquals(
            "La categoría es obligatoria",
            error.erroresPorCampo["categoriaId"]
        )
    }

    @Test
    fun el400SinDetallePorCampoEsUnaValidacionSinErroresPorCampo() = runTest {
        val error = errorAlObtener(
            HttpStatusCode.BadRequest,
            """{"status":400,"error":"Bad Request","message":"El cuerpo de la petición no es válido o está mal formado"}"""
        )

        assertEquals(
            ErrorApi.Validacion("El cuerpo de la petición no es válido o está mal formado"),
            error
        )
    }

    @Test
    fun el500SeTraduceAServidorConservandoElCodigo() = runTest {
        val error = errorAlObtener(
            HttpStatusCode.InternalServerError,
            """{"status":500,"error":"Internal Server Error","message":"Ocurrió un error inesperado. Intente nuevamente más tarde."}"""
        )

        assertEquals(
            ErrorApi.Servidor("Ocurrió un error inesperado. Intente nuevamente más tarde.", 500),
            error
        )
    }

    @Test
    fun un503SeTraduceAServidorConSuCodigo() = runTest {
        val error = errorAlObtener(HttpStatusCode.ServiceUnavailable, "{}")

        assertEquals(
            ErrorApi.Servidor(
                "El servidor no pudo completar la operación. Inténtalo más tarde.",
                503
            ),
            error
        )
    }

    @Test
    fun un401SeTraduceANoAutorizadoConElMensajeDelServidor() = runTest {
        val error = errorAlObtener(
            HttpStatusCode.Unauthorized,
            """{"status":401,"error":"Unauthorized","message":"La sesión no es válida o ha expirado"}"""
        )

        assertEquals(
            ErrorApi.NoAutorizado("La sesión no es válida o ha expirado"),
            error
        )
    }

    @Test
    fun un403SeTraduceAProhibidoYNoConfundeConSesionExpirada() = runTest {
        val error = errorAlObtener(
            HttpStatusCode.Forbidden,
            """{"status":403,"error":"Forbidden","message":"No tienes permiso para esta operación"}"""
        )

        assertEquals(
            ErrorApi.Prohibido("No tienes permiso para esta operación"),
            error
        )
        assertFalse(error.esFalloDeAutenticacion)
    }

    @Test
    fun losErroresDeAutenticacionSeReconocenParaMostrarElMensajeAdecuado() {
        assertTrue(ErrorApi.NoAutorizado("x").esFalloDeAutenticacion)
        assertTrue(ErrorApi.CredencialesInvalidas("x").esFalloDeAutenticacion)
        assertTrue(ErrorApi.SesionExpirada("x").esFalloDeAutenticacion)

        assertEquals(
            "Usuario o contraseña incorrectos.",
            ErrorApi.CredencialesInvalidas("").mensajeParaUsuario()
        )
        assertEquals(
            "Tu sesión expiró. Inicia sesión de nuevo.",
            ErrorApi.SesionExpirada("").mensajeParaUsuario()
        )
        assertEquals(
            "No tienes permisos para realizar esta operación.",
            ErrorApi.Prohibido("").mensajeParaUsuario()
        )
    }

    @Test
    fun unCuerpoQueNoEsJsonNoRompeLaTraduccion() = runTest {
        val error = errorAlObtener(HttpStatusCode.NotFound, "<html>404</html>")

        assertEquals(ErrorApi.NoEncontrado("No se encontró el recurso solicitado."), error)
    }

    @Test
    fun unCuerpoJsonConCamposDesconocidosSeTolera() = runTest {
        val error = errorAlObtener(
            HttpStatusCode.NotFound,
            """{"status":404,"message":"Producto no encontrado","extra":{"a":1}}"""
        )

        assertEquals(ErrorApi.NoEncontrado("Producto no encontrado"), error)
    }
}