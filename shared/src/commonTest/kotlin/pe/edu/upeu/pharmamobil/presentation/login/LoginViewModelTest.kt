package pe.edu.upeu.pharmamobil.presentation.login

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import pe.edu.upeu.pharmamobil.data.auth.GestorSesion
import pe.edu.upeu.pharmamobil.data.security.AlmacenSeguroEnMemoria
import pe.edu.upeu.pharmamobil.domain.ErrorApi
import pe.edu.upeu.pharmamobil.domain.ErrorDeApi
import pe.edu.upeu.pharmamobil.domain.auth.AuthRepository
import pe.edu.upeu.pharmamobil.domain.auth.Credenciales
import pe.edu.upeu.pharmamobil.domain.auth.Permisos
import pe.edu.upeu.pharmamobil.domain.auth.Sesion
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * La pantalla de login solo conversa con [AuthRepository] y [GestorSesion]:
 * la credencial se elimina del estado tras autenticar, un doble toque no lanza
 * dos logins y un 401 se muestra como mensaje claro sin avanzar a sesión.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private lateinit var vista: LoginViewModel
    private lateinit var gestor: GestorSesion
    private lateinit var almacen: AlmacenSeguroEnMemoria
    private lateinit var auth: FakeAuthRepository

    @BeforeTest
    fun instalarMain() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun restaurarMain() {
        Dispatchers.resetMain()
    }

    private fun preparar(
        retraso: CompletableDeferred<Unit>? = null
    ) {
        almacen = AlmacenSeguroEnMemoria()
        gestor = GestorSesion(almacen)
        auth = FakeAuthRepository(retraso = retraso)
        vista = LoginViewModel(auth, gestor)
    }

    @Test
    fun losCamposVaciosNoCantanComoError() = runTest {
        preparar()

        vista.ingresar()

        assertEquals("Ingresa tu usuario y tu contraseña.", vista.uiState.value.error)
        assertFalse(vista.uiState.value.cargando)
        assertEquals(0, auth.llamadas)
    }

    @Test
    fun elLoginCorrectoPublicaSesionYaNoConservalaContrasena() = runTest {
        preparar()
        vista.onUsuarioChange("admin")
        vista.onContrasenaChange("secreta")

        vista.ingresar()

        assertNull(vista.uiState.value.error)
        assertFalse(vista.uiState.value.cargando)
        assertEquals("", vista.uiState.value.usuario)
        assertEquals("", vista.uiState.value.contrasena)
        assertSame(auth.sesionAdministrador, gestor.sesion.value)
        assertEquals(auth.sesionAdministrador.token, almacen.leerToken())
    }

    @Test
    fun unasCredencialesInvalidasNoAvanzanASesion() = runTest {
        preparar()
        vista.onUsuarioChange("admin")
        vista.onContrasenaChange("incorrecta")

        vista.ingresar()

        assertEquals(
            "Usuario o contraseña incorrectos.",
            vista.uiState.value.error
        )
        assertFalse(vista.uiState.value.cargando)
        assertNull(gestor.sesion.value)
        assertNull(almacen.leerToken())
    }

    @Test
    fun unDobleteToqueSoloLanzaUnLogin() = runTest {
        val retraso = CompletableDeferred<Unit>()
        preparar(retraso = retraso)
        vista.onUsuarioChange("admin")
        vista.onContrasenaChange("secreta")

        vista.ingresar()
        vista.ingresar()

        // La segunda llamada se ignora porque la primera sigue en curso.
        assertEquals(1, auth.llamadas)

        retraso.complete(Unit)
        advanceUntilIdle()

        assertSame(auth.sesionAdministrador, gestor.sesion.value)
        assertTrue(vista.uiState.value.cargando.not())
    }

    @Test
    fun laEdicionDeUnCampoBorraElErrorAnterior() = runTest {
        preparar()
        vista.ingresar()
        assertTrue(vista.uiState.value.error != null)

        vista.onUsuarioChange("admin")

        assertNull(vista.uiState.value.error)
    }

    private class FakeAuthRepository(
        private val retraso: CompletableDeferred<Unit>? = null
    ) : AuthRepository {

        val sesionAdministrador = Sesion(
            token = "tok-admin",
            usuario = "admin",
            nombreCompleto = "Ada Admin",
            permisos = setOf(
                Permisos.CATALOGO_LEER,
                Permisos.CATALOGO_ESCRIBIR,
                Permisos.CLIENTES_LEER,
                Permisos.VENTAS_REGISTRAR,
                Permisos.REPORTES_VER
            )
        )

        var llamadas = 0
            private set

        override suspend fun iniciarSesion(credenciales: Credenciales): Sesion {
            llamadas++
            retraso?.await()
            if (credenciales.usuario == "admin" && credenciales.contrasena == "secreta") {
                return sesionAdministrador
            }
            throw ErrorDeApi(
                ErrorApi.CredencialesInvalidas("Usuario o contraseña incorrectos.")
            )
        }

        override suspend fun recuperarSesion(): Sesion? = null

        override suspend fun cerrarSesion() = Unit
    }
}