package pe.edu.upeu.pharmamobil.presentation.producto

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import pe.edu.upeu.pharmamobil.data.repository.FakeProductoRepository
import pe.edu.upeu.pharmamobil.domain.ErrorApi
import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.tieneErroresPorCampo
import pe.edu.upeu.pharmamobil.domain.usecase.ActualizarProductoUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.EliminarProductoUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.ObservarProductosUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.RegistrarProductoUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.SincronizarProductosUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

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
        observarProductos = ObservarProductosUseCase(repositorio),
        sincronizarProductos = SincronizarProductosUseCase(repositorio),
        registrarProducto = RegistrarProductoUseCase(repositorio),
        actualizarProducto = ActualizarProductoUseCase(repositorio),
        eliminarProducto = EliminarProductoUseCase(repositorio)
    )

    @Test
    fun arrancaObservandoElInventarioLocal() = runTest {

        val repositorio = FakeProductoRepository(
            mutableListOf(
                Producto(id = 1L, nombre = "Paracetamol", precio = 12.50, stock = 5, categoriaId = 1L)
            )
        )

        val viewModel = nuevoViewModel(repositorio)

        val fase = assertIs<ProductoUiState.Fase.ConProductos>(viewModel.uiState.value.fase)
        assertEquals("Paracetamol", fase.productos.single().nombre)
    }

    @Test
    fun pasaAFaseSinProductosConElInventarioVacio() = runTest {

        val viewModel = nuevoViewModel()

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

        val fase = assertIs<ProductoUiState.Fase.ConProductos>(
            viewModel.uiState.value.fase
        )

        assertEquals("S/ 12.50", fase.productos.first().precio)
        assertEquals("5 u.", fase.productos.first().stock)
        assertTrue(fase.productos.first().requiereReposicion)
    }

    @Test
    fun sincronizarSinRedDejaAvisoDeRedSinCambiarElInventario() = runTest {

        val repositorio = FakeProductoRepository(
            mutableListOf(
                Producto(id = 1L, nombre = "Paracetamol", precio = 12.50, stock = 5, categoriaId = 1L)
            )
        ).apply {
            fallaAlSincronizar = IllegalStateException("Sin conexión")
        }

        val viewModel = nuevoViewModel(repositorio)
        viewModel.sincronizar()

        assertEquals("Sin conexión", viewModel.uiState.value.avisoRed)
        assertIs<ProductoUiState.Fase.ConProductos>(viewModel.uiState.value.fase)
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
        viewModel.onBusquedaChange("ibu")

        val visibles = viewModel.uiState.value.filtrarPorBusqueda()

        assertEquals(listOf("Ibuprofeno"), visibles.map { it.nombre })
    }

    @Test
    fun elInventarioSeActualizaSoloCuandoElRepositorioCambia() = runTest {

        val repositorio = FakeProductoRepository()
        val viewModel = nuevoViewModel(repositorio)

        repositorio.registrar(
            Producto(id = 0L, nombre = "Ibuprofeno", precio = 8.90, stock = 3, categoriaId = 1L)
        )

        val fase = assertIs<ProductoUiState.Fase.ConProductos>(viewModel.uiState.value.fase)
        assertEquals(1, fase.productos.size)
        assertEquals("Ibuprofeno", fase.productos.first().nombre)
    }

    @Test
    fun crearConDatosInvalidosGeneraOperacionFallidaConErroresPorCampo() = runTest {

        val repositorio = FakeProductoRepository()
        val viewModel = nuevoViewModel(repositorio)

        viewModel.crear("", "-1", "abc", true, 0L)

        val operacion = viewModel.uiState.value.operacion
        val fallida = assertIs<ProductoUiState.Operacion.Fallida>(operacion)
        assertEquals(ProductoUiState.TipoOperacion.Crear, fallida.tipo)

        val validacion = assertIs<ErrorApi.Validacion>(fallida.error)
        assertTrue(validacion.tieneErroresPorCampo)
        assertNotNull(validacion.erroresPorCampo["nombre"])
        assertNotNull(validacion.erroresPorCampo["precio"])
        assertNotNull(validacion.erroresPorCampo["stock"])
        assertNotNull(validacion.erroresPorCampo["categoriaId"])
    }

    @Test
    fun eliminarProductoPasaAInactivaYLasListaSeActualizaSola() = runTest {

        val productoExistente = Producto(
            id = 42L,
            nombre = "Producto a borrar",
            precio = 10.0,
            stock = 5,
            categoriaId = 1L
        )
        val repositorio = FakeProductoRepository(mutableListOf(productoExistente))
        val viewModel = nuevoViewModel(repositorio)

        val faseInicial = assertIs<ProductoUiState.Fase.ConProductos>(viewModel.uiState.value.fase)
        assertEquals(1, faseInicial.productos.size)

        viewModel.eliminar(42L)

        assertEquals(ProductoUiState.Operacion.Inactiva, viewModel.uiState.value.operacion)
        assertEquals(ProductoUiState.Fase.SinProductos, viewModel.uiState.value.fase)
    }

    @Test
    fun cancelacionDuranteMutacionPropagaCancellationException() = runTest {

        val repositorio = FakeProductoRepository(
            mutableListOf(Producto(id = 1L, nombre = "Test", precio = 1.0, stock = 1, categoriaId = 1L))
        )
        val viewModel = nuevoViewModel(repositorio)

        val job = launch { viewModel.eliminar(1L) }
        job.cancel()
        job.join()

        assertEquals(ProductoUiState.Operacion.Inactiva, viewModel.uiState.value.operacion)
    }

    @Test
    fun sincronizarExitosoLimpiaElAvisoDeRed() = runTest {

        val repositorio = FakeProductoRepository().apply {
            fallaAlSincronizar = IllegalStateException("Sin conexión")
        }
        val viewModel = nuevoViewModel(repositorio)

        viewModel.sincronizar()
        assertNotNull(viewModel.uiState.value.avisoRed)

        repositorio.fallaAlSincronizar = null
        viewModel.sincronizar()

        assertNull(viewModel.uiState.value.avisoRed)
    }
}
