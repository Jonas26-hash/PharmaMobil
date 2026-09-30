package pe.edu.upeu.pharmamobil.presentation.cliente

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import pe.edu.upeu.pharmamobil.data.repository.FakeClienteRepository
import pe.edu.upeu.pharmamobil.domain.model.Cliente
import pe.edu.upeu.pharmamobil.domain.usecase.ListarClientesUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/**
 * El listado no carga en el init ni registra: [ClienteViewModel] solo lista y
 * filtra; el registro y la edición viven en [ClienteFormViewModel].
 */
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
        listarClientes = ListarClientesUseCase(repositorio)
    )

    @Test
    fun arrancaEnCargando() = runTest {

        val viewModel = nuevoViewModel()

        assertEquals(ClienteUiState.Fase.Cargando, viewModel.uiState.value.fase)
    }

    @Test
    fun pasaAFaseSinClientesConLaCarteraVacia() = runTest {

        val viewModel = nuevoViewModel()
        viewModel.cargarClientes()

        assertEquals(ClienteUiState.Fase.SinClientes, viewModel.uiState.value.fase)
    }

    @Test
    fun elClienteSinTelefonoSeMuestraComoNoRegistrado() = runTest {

        val repositorio = FakeClienteRepository(
            mutableListOf(
                Cliente(
                    id = 1L,
                    nombre = "María",
                    apellido = "García",
                    dni = "12345678"
                )
            )
        )

        val viewModel = nuevoViewModel(repositorio)
        viewModel.cargarClientes()

        val fase = assertIs<ClienteUiState.Fase.ConClientes>(
            viewModel.uiState.value.fase
        )

        assertEquals("María García", fase.clientes.single().nombreCompleto)
        assertEquals("12345678", fase.clientes.single().dni)
        assertEquals(TELEFONO_AUSENTE, fase.clientes.single().telefono)
    }

    @Test
    fun pasaAFaseErrorCuandoElRepositorioFalla() = runTest {

        val repositorio = FakeClienteRepository().apply {
            fallaAlListar = IllegalStateException("Sin conexión")
        }

        val viewModel = nuevoViewModel(repositorio)
        viewModel.cargarClientes()

        val fase = assertIs<ClienteUiState.Fase.Error>(
            viewModel.uiState.value.fase
        )

        assertEquals("Sin conexión", fase.mensaje)
    }

    @Test
    fun laBusquedaFiltraPorNombreYPorDni() = runTest {

        val repositorio = FakeClienteRepository(
            mutableListOf(
                Cliente(
                    id = 1L,
                    nombre = "María",
                    apellido = "García",
                    dni = "12345678"
                ),
                Cliente(
                    id = 2L,
                    nombre = "Juan",
                    apellido = "Pérez",
                    dni = "87654321"
                )
            )
        )

        val viewModel = nuevoViewModel(repositorio)
        viewModel.cargarClientes()
        viewModel.onBusquedaChange("87654321")

        val visibles = viewModel.uiState.value.filtrarPorBusqueda()

        assertEquals(listOf("Juan Pérez"), visibles.map { it.nombreCompleto })
    }
}