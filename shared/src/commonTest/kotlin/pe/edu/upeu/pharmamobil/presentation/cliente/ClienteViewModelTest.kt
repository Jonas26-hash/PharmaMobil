package pe.edu.upeu.pharmamobil.presentation.cliente

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import pe.edu.upeu.pharmamobil.data.repository.FakeClienteRepository
import pe.edu.upeu.pharmamobil.domain.usecase.ListarClientesUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.RegistrarClienteUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class ClienteViewModelTest {

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
    ) = ClienteViewModel(
        registrarCliente = RegistrarClienteUseCase(repositorio),
        listarClientes = ListarClientesUseCase(repositorio)
    )

    private fun llenarFormulario(viewModel: ClienteViewModel) {
        viewModel.onNombreChange("María")
        viewModel.onApellidoChange("García")
        viewModel.onDniChange("12345678")
        viewModel.onTelefonoChange("987654321")
        viewModel.onEmailChange("maria@email.com")
        viewModel.onDireccionChange("Av. Los Sauces 123")
    }

    @Test
    fun arrancaEnSinClientesCuandoLaCarteraEstaVacia() = runTest {

        val viewModel = nuevoViewModel()

        assertEquals(ClienteUiState.Fase.SinClientes, viewModel.uiState.value.fase)
    }

    /**
     * Antes el botón solo pintaba un mensaje de éxito y el cliente no se
     * guardaba en ninguna parte: aquí se comprueba que vuelve en el listado.
     */
    @Test
    fun elClienteRegistradoApareceEnLaCartera() = runTest {

        val viewModel = nuevoViewModel()
        llenarFormulario(viewModel)
        viewModel.registrar()

        val estado = viewModel.uiState.value
        val fase = assertIs<ClienteUiState.Fase.ConClientes>(estado.fase)

        assertEquals("María García", fase.clientes.single().nombreCompleto)
        assertEquals("12345678", fase.clientes.single().dni)
        assertEquals(
            "Cliente \"María\" registrado correctamente",
            estado.mensajeExito
        )
        assertEquals("", estado.formulario.nombre)
    }

    @Test
    fun elTelefonoAusenteSeMuestraComoNoRegistrado() = runTest {

        val viewModel = nuevoViewModel()
        viewModel.onNombreChange("María")
        viewModel.onApellidoChange("García")
        viewModel.onDniChange("12345678")
        viewModel.registrar()

        val fase = assertIs<ClienteUiState.Fase.ConClientes>(viewModel.uiState.value.fase)

        assertEquals(TELEFONO_AUSENTE, fase.clientes.single().telefono)
    }

    @Test
    fun elCorreoInvalidoCaeEnElFormulario() = runTest {

        val viewModel = nuevoViewModel()
        viewModel.onNombreChange("María")
        viewModel.onApellidoChange("García")
        viewModel.onDniChange("12345678")
        viewModel.onEmailChange("maria.central.pe")
        viewModel.registrar()

        val estado = viewModel.uiState.value

        assertEquals("El correo no tiene un formato válido", estado.formulario.emailError)
        assertEquals(ClienteUiState.Fase.SinClientes, estado.fase)
        assertNull(estado.mensajeExito)
    }

    @Test
    fun elDniInvalidoCaeEnElFormulario() = runTest {

        val viewModel = nuevoViewModel()
        llenarFormulario(viewModel)
        viewModel.onDniChange("123")
        viewModel.registrar()

        val estado = viewModel.uiState.value

        assertEquals("El DNI debe tener 8 dígitos", estado.formulario.dniError)
        assertEquals(ClienteUiState.Fase.SinClientes, estado.fase)
    }

    @Test
    fun pasaAFaseErrorCuandoElRepositorioFalla() = runTest {

        val repositorio = FakeClienteRepository().apply {
            fallaAlListar = IllegalStateException("Sin conexión")
        }

        val fase = assertIs<ClienteUiState.Fase.Error>(
            nuevoViewModel(repositorio).uiState.value.fase
        )

        assertEquals("Sin conexión", fase.mensaje)
    }
}