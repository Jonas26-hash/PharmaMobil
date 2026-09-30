package pe.edu.upeu.pharmamobil.presentation.producto

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import pe.edu.upeu.pharmamobil.data.repository.FakeProductoRepository
import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.usecase.ListarProductosUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * El listado ya no carga en el init ni registra ventas: [ProductoViewModel]
 * solo lista y filtra. La carga la pide la pantalla con LaunchedEffect para
 * que cada visita al listado refresque el estado del backend.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ProductoViewModelTest {

    @BeforeTest
    fun instalarMain() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun restaurarMain() {
        Dispatchers.resetMain()
    }

    private fun nuevoViewModel(
        repositorio: FakeProductoRepository = FakeProductoRepository()
    ) = ProductoViewModel(
        listarProductos = ListarProductosUseCase(repositorio)
    )

    @Test
    fun arrancaEnCargando() = runTest {

        val viewModel = nuevoViewModel()

        assertEquals(ProductoUiState.Fase.Cargando, viewModel.uiState.value.fase)
    }

    @Test
    fun pasaAPaseSinProductosConElInventarioVacio() = runTest {

        val viewModel = nuevoViewModel()
        viewModel.cargarProductos()

        assertEquals(ProductoUiState.Fase.SinProductos, viewModel.uiState.value.fase)
    }

    @Test
    fun muestraElInventarioConElPrecioYaFormateado() = runTest {

        val repositorio = FakeProductoRepository(
            mutableListOf(
                Producto(
                    id = 1L,
                    nombre = "Paracetamol",
                    precio = 12.5,
                    stock = 5,
                    categoriaId = 1L
                )
            )
        )

        val viewModel = nuevoViewModel(repositorio)
        viewModel.cargarProductos()

        val fase = assertIs<ProductoUiState.Fase.ConProductos>(
            viewModel.uiState.value.fase
        )

        assertEquals("S/ 12.50", fase.productos.first().precio)
        assertEquals("5 u.", fase.productos.first().stock)
        assertTrue(fase.productos.first().requiereReposicion)
    }

    @Test
    fun pasaAFaseErrorCuandoElRepositorioFalla() = runTest {

        val repositorio = FakeProductoRepository().apply {
            fallaAlListar = IllegalStateException("Sin conexión")
        }

        val viewModel = nuevoViewModel(repositorio)
        viewModel.cargarProductos()

        val fase = assertIs<ProductoUiState.Fase.Error>(
            viewModel.uiState.value.fase
        )

        assertEquals("Sin conexión", fase.mensaje)
    }

    @Test
    fun laBusquedaFiltraEnLaMismaPantalla() = runTest {

        val repositorio = FakeProductoRepository(
            mutableListOf(
                Producto(id = 1L, nombre = "Paracetamol", precio = 12.50, stock = 5),
                Producto(id = 2L, nombre = "Ibuprofeno", precio = 8.90, stock = 3)
            )
        )

        val viewModel = nuevoViewModel(repositorio)
        viewModel.cargarProductos()
        viewModel.onBusquedaChange("ibu")

        val visibles = viewModel.uiState.value.filtrarPorBusqueda()

        assertEquals(listOf("Ibuprofeno"), visibles.map { it.nombre })
    }
}