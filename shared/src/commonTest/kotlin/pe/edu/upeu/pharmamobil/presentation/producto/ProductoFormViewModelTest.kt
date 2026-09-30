package pe.edu.upeu.pharmamobil.presentation.producto

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import pe.edu.upeu.pharmamobil.data.repository.FakeCategoriaRepository
import pe.edu.upeu.pharmamobil.data.repository.FakeProductoRepository
import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.usecase.ActualizarProductoUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.EliminarProductoUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.ListarCategoriasUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.ObtenerProductoUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.RegistrarProductoUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Una prueba del ViewModel de formulario no debe tocar la red: se construye
 * con los dobles y casos de uso reales (así se compara también la firma de
 * cada caso con lo que el ViewModel le pide).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ProductoFormViewModelTest {

    @BeforeTest
    fun instalarMain() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun restaurarMain() {
        Dispatchers.resetMain()
    }

    private fun nuevoViewModel(
        repositorio: FakeProductoRepository = FakeProductoRepository(),
        categorias: FakeCategoriaRepository = FakeCategoriaRepository()
    ) = ProductoFormViewModel(
        registrarProducto = RegistrarProductoUseCase(repositorio),
        actualizarProducto = ActualizarProductoUseCase(repositorio),
        eliminarProducto = EliminarProductoUseCase(repositorio),
        obtenerProducto = ObtenerProductoUseCase(repositorio),
        listarCategorias = ListarCategoriasUseCase(categorias)
    )

    private fun llenarFormulario(viewModel: ProductoFormViewModel) {
        viewModel.onNombreChange("Paracetamol")
        viewModel.onPrecioChange("12.50")
        viewModel.onStockChange("5")
        viewModel.onCategoriaChange(1L)
    }

    @Test
    fun cargaElCatalogoDeCategoriasAlIniciar() {

        val viewModel = nuevoViewModel()

        assertTrue(viewModel.uiState.value.categorias.isNotEmpty())
        assertEquals("Analgésico", viewModel.uiState.value.categorias.first().nombre)
    }

    @Test
    fun arrancaEnModoRegistro() {

        val viewModel = nuevoViewModel()

        val estado = viewModel.uiState.value

        assertNull(estado.formulario.id)
        assertTrue(estado.formulario.estadoActivo)
    }

    @Test
    fun guardarUnProductoNuevoSeteaMensajeExito() = runTest {

        val repositorio = FakeProductoRepository()
        val viewModel = nuevoViewModel(repositorio)
        llenarFormulario(viewModel)
        viewModel.guardar()

        val estado = viewModel.uiState.value

        assertEquals(
            "Producto \"Paracetamol\" registrado correctamente",
            estado.mensajeExito
        )
        assertFalse(estado.guardando)
        assertEquals(1, repositorio.listar().size)
    }

    @Test
    fun guardarSinCategoriaCaeEnElFormulario() = runTest {

        val viewModel = nuevoViewModel()
        viewModel.onNombreChange("Paracetamol")
        viewModel.onPrecioChange("12.50")
        viewModel.onStockChange("5")
        viewModel.guardar()

        val estado = viewModel.uiState.value

        assertEquals("Selecciona una categoría.", estado.formulario.categoriaError)
        assertNull(estado.mensajeExito)
    }

    @Test
    fun losErroresDeValidacionCaenEnElFormulario() = runTest {

        val viewModel = nuevoViewModel()
        viewModel.onNombreChange("")
        viewModel.onPrecioChange("abc")
        viewModel.onStockChange("-1")
        viewModel.guardar()

        val estado = viewModel.uiState.value

        assertEquals("El nombre es obligatorio.", estado.formulario.nombreError)
        assertEquals("Ingresa un precio numérico.", estado.formulario.precioError)
        assertEquals("El stock no puede ser negativo.", estado.formulario.stockError)
        assertNull(estado.mensajeExito)
    }

    @Test
    fun configurarProductoPreparaLaEdicion() = runTest {

        val repositorio = FakeProductoRepository(
            mutableListOf(
                Producto(
                    id = 7L,
                    nombre = "Losartán",
                    precio = 5.5,
                    stock = 3,
                    categoriaId = 2L,
                    activo = false
                )
            )
        )

        val viewModel = nuevoViewModel(repositorio, FakeCategoriaRepository())
        viewModel.configurarProducto(7L)

        val formulario = viewModel.uiState.value.formulario

        assertEquals(7L, formulario.id)
        assertEquals("Losartán", formulario.nombre)
        assertEquals("5.5", formulario.precio)
        assertEquals("3", formulario.stock)
        assertEquals(2L, formulario.categoriaId)
        assertFalse(formulario.estadoActivo)
    }

    @Test
    fun editarUnProductoActualizaEnLugarDeCrearOtro() = runTest {

        val repositorio = FakeProductoRepository(
            mutableListOf(
                Producto(id = 7L, nombre = "Losartán", precio = 5.5, stock = 3, categoriaId = 2L)
            )
        )

        val viewModel = nuevoViewModel(repositorio, FakeCategoriaRepository())
        viewModel.configurarProducto(7L)
        viewModel.onPrecioChange("6.00")
        viewModel.guardar()

        val estado = viewModel.uiState.value

        assertEquals(
            "Producto \"Losartán\" actualizado correctamente",
            estado.mensajeExito
        )
        assertEquals(1, repositorio.listar().size)
        assertEquals(6.00, repositorio.listar().single().precio, 0.0001)
    }

    @Test
    fun eliminarBorraElProductoDelInventario() = runTest {

        val repositorio = FakeProductoRepository(
            mutableListOf(
                Producto(id = 7L, nombre = "Losartán", precio = 5.5, stock = 3, categoriaId = 2L)
            )
        )

        val viewModel = nuevoViewModel(repositorio, FakeCategoriaRepository())
        viewModel.configurarProducto(7L)
        viewModel.eliminar()

        assertEquals("Producto eliminado correctamente", viewModel.uiState.value.mensajeExito)
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

        viewModel.configurarProducto(null)

        val estado = viewModel.uiState.value

        assertNull(estado.mensajeExito)
        assertEquals("", estado.formulario.nombre)
        assertNull(estado.formulario.id)
    }
}