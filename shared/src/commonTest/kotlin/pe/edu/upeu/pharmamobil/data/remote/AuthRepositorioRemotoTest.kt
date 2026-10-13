package pe.edu.upeu.pharmamobil.data.remote

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.utils.io.errors.IOException
import kotlinx.coroutines.test.runTest
import pe.edu.upeu.pharmamobil.data.security.AlmacenSeguroEnMemoria
import pe.edu.upeu.pharmamobil.domain.ErrorApi
import pe.edu.upeu.pharmamobil.domain.ErrorDeApi
import pe.edu.upeu.pharmamobil.domain.auth.Credenciales
import pe.edu.upeu.pharmamobil.domain.auth.Permisos
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Reglas de sesión del [AuthRepositorioRemoto]:
 *  - el login correcto guarda el token y devuelve la sesión con permisos;
 *  - el 401 del login es credenciales inválidas y NO deja ningún token;
 *  - un token guardado se consume vía /auth/me; si el servidor dice 401 se
 *    descarta y se cae al login; un fallo de red NO descarta el token.
 */
class AuthRepositorioRemotoTest {

    private val json = headersOf(HttpHeaders.ContentType, "application/json")

    private val loginOk = """
        {"token":"tok-123","usuario":"admin","nombreCompleto":"Ada Admin","permisos":["productos:listar","productos:registrar"]}
    """.trimIndent()

    private val perfilOk = """
        {"usuario":"admin","nombreCompleto":"Ada Admin","permisos":["productos:listar","productos:registrar"]}
    """.trimIndent()

    private class Entorno(
        engine: MockEngine,
        val almacen: AlmacenSeguroEnMemoria = AlmacenSeguroEnMemoria()
    ) {
        val repositorio: AuthRepositorioRemoto

        init {
            val cliente = crearHttpClient(
                motor = engine,
                leerToken = { almacen.leerToken() },
                alExpirarSesion = { almacen.borrarToken() }
            )
            repositorio = AuthRepositorioRemoto(
                api = AuthApi(cliente),
                almacen = almacen,
                cliente = cliente
            )
        }
    }

    @Test
    fun elLoginCorrectoGuardaElTokenYDevuelveLaSesion() = runTest {

        val entorno = Entorno(
            MockEngine {
                respond(loginOk, HttpStatusCode.OK, json)
            }
        )

        val sesion = entorno.repositorio.iniciarSesion(
            Credenciales(usuario = "admin", contrasena = "secreta")
        )

        assertEquals("tok-123", sesion.token)
        assertEquals("Ada Admin", sesion.nombreCompleto)
        assertTrue(sesion.puede(Permisos.PRODUCTOS_LISTAR))
        assertTrue(sesion.puede(Permisos.PRODUCTOS_REGISTRAR))
        assertEquals("tok-123", entorno.almacen.leerToken())
    }

    @Test
    fun un401DelLoginEsCredencialesInvalidasYSinDejarToken() = runTest {

        val entorno = Entorno(
            MockEngine {
                respond(
                    """{"status":401,"error":"Unauthorized","message":"Usuario o contraseña incorrectos"}""",
                    HttpStatusCode.Unauthorized,
                    json
                )
            }
        )

        val error = assertFailsWith<ErrorDeApi> {
            entorno.repositorio.iniciarSesion(
                Credenciales(usuario = "admin", contrasena = "mala")
            )
        }

        assertIs<ErrorApi.CredencialesInvalidas>(error.error)
        assertNull(entorno.almacen.leerToken())
    }

    @Test
    fun sinTokenGuardadoNoHaySesionQueRecuperar() = runTest {

        // Si no hay token no debe ni dispararse una petición HTTP.
        val entorno = Entorno(
            MockEngine { error("no debería salir a la red sin token") }
        )

        assertNull(entorno.repositorio.recuperarSesion())
    }

    @Test
    fun recuperarSesionUsaElPerfilDelTokenGuardado() = runTest {

        val entorno = Entorno(
            MockEngine { respond(perfilOk, HttpStatusCode.OK, json) }
        )
        entorno.almacen.guardarToken("tok-123")

        val sesion = entorno.repositorio.recuperarSesion()

        assertEquals("tok-123", sesion?.token)
        assertEquals("Ada Admin", sesion?.nombreCompleto)
        assertTrue(sesion?.puede(Permisos.PRODUCTOS_REGISTRAR) == true)
    }

    @Test
    fun unTokenCaducadoSeDescartaYVuelveAlLogin() = runTest {

        val entorno = Entorno(
            MockEngine {
                respond(
                    """{"status":401,"error":"Unauthorized","message":"La sesión caducó"}""",
                    HttpStatusCode.Unauthorized,
                    json
                )
            }
        )
        entorno.almacen.guardarToken("tok-caduco")

        assertNull(entorno.repositorio.recuperarSesion())
        assertNull(entorno.almacen.leerToken())
    }

    @Test
    fun unFalloDeRedNoDescartaUnTokenTodaviaValido() = runTest {

        val entorno = Entorno(
            MockEngine { throw IOException("Connection refused") }
        )
        entorno.almacen.guardarToken("tok-provisional")

        val error = assertFailsWith<ErrorDeApi> {
            entorno.repositorio.recuperarSesion()
        }

        assertIs<ErrorApi.SinConexion>(error.error)
        // Sin red no se puede saber si el token sigue valiendo: se conserva.
        assertEquals("tok-provisional", entorno.almacen.leerToken())
    }

    @Test
    fun cerrarSesionBorraElToken() = runTest {

        val entorno = Entorno(
            MockEngine { respond("{}", HttpStatusCode.OK, json) }
        )
        entorno.almacen.guardarToken("tok-123")

        entorno.repositorio.cerrarSesion()

        assertNull(entorno.almacen.leerToken())
    }
}