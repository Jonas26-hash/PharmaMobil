package pe.edu.upeu.pharmamobil.presentation.cliente

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import pe.edu.upeu.pharmamobil.data.repository.FakeClienteRepository
import pe.edu.upeu.pharmamobil.domain.model.Cliente
import pe.edu.upeu.pharmamobil.domain.usecase.ObservarClientesUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.SincronizarClientesUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull

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
        observarClientes = ObservarClientesUseCase(repositorio),
        sincronizarClientes = SincronizarClientesUseCase(repositorio)
    )

    @Test
    fun arrancaObservandoLaCarteraLocal() = runTest {

        val repositorio = FakeClienteRepository(
            mutableListOf(
                Cliente(id = 1L, nombre = "María", apellido = "García", dni = "12345678")
            )
        )

        val viewModel = nuevoViewModel(repositorio)

        val fase = assertIs<ClienteUiState.Fase.ConClientes>(viewModel.uiState.value.fase)
        assertEquals("María García", fase.clientes.single().nombreCompleto)
    }

    @Test
    fun pasaAFaseSinClientesConLaCarteraVacia() = runTest {

        val viewModel = nuevoViewModel()

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

        val fase = assertIs<ClienteUiState.Fase.ConClientes>(
            viewModel.uiState.value.fase
        )

        assertEquals("María García", fase.clientes.single().nombreCompleto)
        assertEquals("12345678", fase.clientes.single().dni)
        assertEquals(TELEFONO_AUSENTE, fase.clientes.single().telefono)
    }

    @Test
    fun sincronizarSinRedDejaAvisoDeRed() = runTest {

        val repositorio = FakeClienteRepository().apply {
            fallaAlSincronizar = IllegalStateException("Sin conexión")
        }

        val viewModel = nuevoViewModel(repositorio)
        viewModel.sincronizar()

        assertEquals("Sin conexión", viewModel.uiState.value.avisoRed)
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
        viewModel.onBusquedaChange("87654321")

        val visibles = viewModel.uiState.value.filtrarPorBusqueda()

        assertEquals(listOf("Juan Pérez"), visibles.map { it.nombreCompleto })
    }
}
