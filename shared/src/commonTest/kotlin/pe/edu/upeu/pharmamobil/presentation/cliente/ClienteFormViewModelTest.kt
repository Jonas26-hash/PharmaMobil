package pe.edu.upeu.pharmamobil.presentation.cliente

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import pe.edu.upeu.pharmamobil.data.repository.FakeClienteRepository
import pe.edu.upeu.pharmamobil.domain.model.Cliente
import pe.edu.upeu.pharmamobil.domain.usecase.ActualizarClienteUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.EliminarClienteUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.ObtenerClienteUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.RegistrarClienteUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ClienteFormViewModelTest {

    @BeforeTest
    fun instalarMain() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun restaurarMain() {
        Dispatchers.resetMain()
    }

    private fun nuevoViewModel(
        repositorio: FakeClienteRepository = FakeClienteRepository()
    ) = ClienteFormViewModel(
        registrarCliente = RegistrarClienteUseCase(repositorio),
        actualizarCliente = ActualizarClienteUseCase(repositorio),
        eliminarCliente = EliminarClienteUseCase(repositorio),
        obtenerCliente = ObtenerClienteUseCase(repositorio)
    )

    private fun llenarFormulario(viewModel: ClienteFormViewModel) {
        viewModel.onNombreChange("María")
        viewModel.onApellidoChange("García")
        viewModel.onDniChange("12345678")
        viewModel.onTelefonoChange("987654321")
        viewModel.onEmailChange("maria@email.com")
        viewModel.onDireccionChange("Av. Los Sauces 123")
    }

    @Test
    fun arrancaEnModoRegistro() {

        val viewModel = nuevoViewModel()

        assertNull(viewModel.uiState.value.formulario.id)
        assertTrue(viewModel.uiState.value.formulario.estadoActivo)
    }

    @Test
    fun guardarUnClienteNuevoSeteaMensajeExito() = runTest {

        val repositorio = FakeClienteRepository()
        val viewModel = nuevoViewModel(repositorio)
        llenarFormulario(viewModel)
        viewModel.guardar()

        val estado = viewModel.uiState.value

        assertEquals(
            "Cliente \"María\" registrado correctamente",
            estado.mensajeExito
        )
        assertFalse(estado.guardando)
        assertEquals(1, repositorio.listar().size)
    }

    @Test
    fun losErroresDeValidacionCaenEnElFormulario() = runTest {

        val viewModel = nuevoViewModel()
        llenarFormulario(viewModel)
        viewModel.onDniChange("123")
        viewModel.guardar()

        val estado = viewModel.uiState.value

        assertEquals("El DNI debe tener 8 dígitos", estado.formulario.dniError)
        assertNull(estado.mensajeExito)
    }

    @Test
    fun configurarClientePreparaLaEdicion() = runTest {

        val repositorio = FakeClienteRepository(
            mutableListOf(
                Cliente(
                    id = 3L,
                    nombre = "Ana",
                    apellido = "Rodríguez",
                    dni = "11223344",
                    activo = false
                )
            )
        )

        val viewModel = nuevoViewModel(repositorio)
        viewModel.configurarCliente(3L)

        val formulario = viewModel.uiState.value.formulario

        assertEquals(3L, formulario.id)
        assertEquals("Ana", formulario.nombre)
        assertEquals("11223344", formulario.dni)
        assertFalse(formulario.estadoActivo)
    }

    @Test
    fun editarUnClienteActualizaEnLugarDeCrearOtro() = runTest {

        val repositorio = FakeClienteRepository(
            mutableListOf(
                Cliente(id = 3L, nombre = "Ana", apellido = "Rodríguez", dni = "11223344")
            )
        )

        val viewModel = nuevoViewModel(repositorio)
        viewModel.configurarCliente(3L)
        viewModel.onTelefonoChange("963852741")
        viewModel.guardar()

        val estado = viewModel.uiState.value

        assertEquals(
            "Cliente \"Ana\" actualizado correctamente",
            estado.mensajeExito
        )
        assertEquals(1, repositorio.listar().size)
        assertEquals("963852741", repositorio.listar().single().telefono)
    }

    @Test
    fun eliminarBorraElClienteDeLaCartera() = runTest {

        val repositorio = FakeClienteRepository(
            mutableListOf(
                Cliente(id = 3L, nombre = "Ana", apellido = "Rodríguez", dni = "11223344")
            )
        )

        val viewModel = nuevoViewModel(repositorio)
        viewModel.configurarCliente(3L)
        viewModel.eliminar()

        assertEquals("Cliente eliminado correctamente", viewModel.uiState.value.mensajeExito)
        assertTrue(repositorio.listar().isEmpty())
    }

    /**
     * El ViewModel sobrevive a la navegación: al reabrir el formulario de
     * registro no puede quedar el mensaje de éxito del guardado anterior (esa
     * pantalla lo consumió al volver al listado) ni el formulario llenado.
     */
    @Test
    fun reabrirElFormularioLimpiaElEstadoDelGuardadoAnterior() = runTest {

        val viewModel = nuevoViewModel()
        llenarFormulario(viewModel)
        viewModel.guardar()
        viewModel.consumirMensajeExito()

        viewModel.configurarCliente(null)

        val estado = viewModel.uiState.value

        assertNull(estado.mensajeExito)
        assertEquals("", estado.formulario.nombre)
        assertNull(estado.formulario.id)
    }
}