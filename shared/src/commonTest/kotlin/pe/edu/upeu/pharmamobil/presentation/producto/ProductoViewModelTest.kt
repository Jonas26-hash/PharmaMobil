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
import pe.edu.upeu.pharmamobil.domain.usecase.RegistrarProductoUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.RegistrarVentaUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * viewModelScope corre sobre Dispatchers.Main, que en una prueba no existe:
 * setMain lo sustituye por un dispatcher de prueba antes de cada caso.
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
        registrarProducto = RegistrarProductoUseCase(repositorio),
        listarProductos = ListarProductosUseCase(repositorio),
        registrarVenta = RegistrarVentaUseCase(repositorio)
    )

    @Test
    fun arrancaEnSinProductosCuandoElInventarioEstaVacio() = runTest {

        val viewModel = nuevoViewModel()

        assertEquals(ProductoUiState.Fase.SinProductos, viewModel.uiState.value.fase)
    }

    @Test
    fun muestraElInventarioConElPrecioYaFormateado() = runTest {

        val repositorio = FakeProductoRepository(
            mutableListOf(
                Producto(id = 1L, nombre = "Paracetamol", precio = 12.5, stock = 5)
            )
        )

        val fase = assertIs<ProductoUiState.Fase.ConProductos>(
            nuevoViewModel(repositorio).uiState.value.fase
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

        val fase = assertIs<ProductoUiState.Fase.Error>(
            nuevoViewModel(repositorio).uiState.value.fase
        )

        assertEquals("Sin conexión", fase.mensaje)
    }

    @Test
    fun losErroresDeValidacionCaenEnElFormularioNoEnLaFase() = runTest {

        val viewModel = nuevoViewModel()

        viewModel.onNombreChange("")
        viewModel.onPrecioChange("abc")
        viewModel.onStockChange("-1")
        viewModel.registrar()

        val estado = viewModel.uiState.value

        assertEquals("El nombre es obligatorio.", estado.formulario.nombreError)
        assertEquals("Ingresa un precio numérico.", estado.formulario.precioError)
        assertEquals("El stock no puede ser negativo.", estado.formulario.stockError)
        assertEquals(ProductoUiState.Fase.SinProductos, estado.fase)
        assertNull(estado.mensajeExito)
    }

    @Test
    fun registrarLimpiaElFormularioYRecargaElInventario() = runTest {

        val viewModel = nuevoViewModel()

        viewModel.onNombreChange("Paracetamol")
        viewModel.onPrecioChange("12.50")
        viewModel.onStockChange("5")
        viewModel.registrar()

        val estado = viewModel.uiState.value
        val fase = assertIs<ProductoUiState.Fase.ConProductos>(estado.fase)

        assertEquals("Paracetamol", fase.productos.single().nombre)
        assertEquals("", estado.formulario.nombre)
        assertEquals("", estado.formulario.precio)
        assertEquals("", estado.formulario.stock)
        assertEquals(
            "Producto \"Paracetamol\" registrado correctamente",
            estado.mensajeExito
        )
    }

    @Test
    fun venderMuestraElSnackbarConElTotalYDescuentaStock() = runTest {

        val repositorio = FakeProductoRepository(
            mutableListOf(
                Producto(id = 1L, nombre = "Paracetamol", precio = 15.50, stock = 5)
            )
        )

        val viewModel = nuevoViewModel(repositorio)
        viewModel.vender(1L)

        val estado = viewModel.uiState.value

        assertEquals("Venta exitosa: Paracetamol - Total: S/ 15.50", estado.mensaje)

        val fase = assertIs<ProductoUiState.Fase.ConProductos>(estado.fase)
        assertEquals(4, fase.productos.single().stockUnidades)
    }

    @Test
    fun venderSinStockMuestraElErrorDeStock() = runTest {

        val repositorio = FakeProductoRepository(
            mutableListOf(
                Producto(id = 1L, nombre = "Loratadina", precio = 12.50, stock = 0)
            )
        )

        val viewModel = nuevoViewModel(repositorio)
        viewModel.vender(1L)

        val mensaje = viewModel.uiState.value.mensaje

        assertTrue(mensaje != null && mensaje.startsWith("Stock insuficiente"))
    }

    @Test
    fun consumirMensajeLimpiaElSnackbar() = runTest {

        val repositorio = FakeProductoRepository(
            mutableListOf(
                Producto(id = 1L, nombre = "Paracetamol", precio = 15.50, stock = 5)
            )
        )

        val viewModel = nuevoViewModel(repositorio)
        viewModel.vender(1L)
        viewModel.consumirMensaje()

        assertNull(viewModel.uiState.value.mensaje)
    }
}