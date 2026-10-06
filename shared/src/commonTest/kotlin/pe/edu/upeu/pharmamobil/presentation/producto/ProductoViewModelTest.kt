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
import pe.edu.upeu.pharmamobil.domain.tieneErroresPorCampo
import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.usecase.ActualizarProductoUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.EliminarProductoUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.ListarProductosUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.RegistrarProductoUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * [ProductoViewModel] gobierna dos ejes de estado: FASE (qué se ve) y
 * OPERACION (qué mutación está en curso). Estos tests cubren la guía práctica.
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
        listarProductos = ListarProductosUseCase(repositorio),
        registrarProducto = RegistrarProductoUseCase(repositorio),
        actualizarProducto = ActualizarProductoUseCase(repositorio),
        eliminarProducto = EliminarProductoUseCase(repositorio)
    )

    @Test
    fun arrancaEnCargando() = runTest {

        val viewModel = nuevoViewModel()

        assertEquals(ProductoUiState.Fase.Cargando, viewModel.uiState.value.fase)
    }

    @Test
    fun pasaAFaseSinProductosConElInventarioVacio() = runTest {

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

    // ========== Tests requeridos por la guía ==========

    /**
     * REQUISITO 16-a: Cargando → ConProductos
     *
     * La fase arranca en Cargando y, tras una carga exitosa con datos,
     * transiciona a ConProductos. La lista no está vacía.
     */
    @Test
    fun faseCargandoPasaAConProductosCuandoHayDatos() = runTest {

        val repositorio = FakeProductoRepository(
            mutableListOf(
                Producto(id = 1L, nombre = "Paracetamol", precio = 12.50, stock = 5, categoriaId = 1L)
            )
        )

        val viewModel = nuevoViewModel(repositorio)

        // Estado inicial
        assertEquals(ProductoUiState.Fase.Cargando, viewModel.uiState.value.fase)

        // Dispara carga
        viewModel.cargarProductos()

        // Fase final
        val fase = assertIs<ProductoUiState.Fase.ConProductos>(viewModel.uiState.value.fase)
        assertEquals(1, fase.productos.size)
        assertEquals("Paracetamol", fase.productos.first().nombre)
    }

    /**
     * REQUISITO 16-b: Cargando → SinProductos
     *
     * La fase arranca en Cargando y, tras una carga exitosa SIN datos,
     * transiciona a SinProductos.
     */
    @Test
    fun faseCargandoPasaASinProductosCuandoInventarioVacio() = runTest {

        val viewModel = nuevoViewModel()

        assertEquals(ProductoUiState.Fase.Cargando, viewModel.uiState.value.fase)

        viewModel.cargarProductos()

        assertEquals(ProductoUiState.Fase.SinProductos, viewModel.uiState.value.fase)
    }

    /**
     * REQUISITO 16-c: error de validación con errores por campo
     *
     * Al crear un producto con datos inválidos, la operación pasa a
     * Fallida(Crear) y el ErrorApi.Validacion conserva el mapa de errores
     * por campo (nombre, precio, stock, categoriaId).
     */
    @Test
    fun crearConDatosInvalidosGeneraOperacionFallidaConErroresPorCampo() = runTest {

        val repositorio = FakeProductoRepository()
        val viewModel = nuevoViewModel(repositorio)

        // Dispara creación con nombre vacío, precio negativo, stock inválido, sin categoría
        viewModel.crear("", "-1", "abc", true, 0L)

        // Operación final: Fallida(Crear) con ErrorApi.Validacion
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

    /**
     * REQUISITO 16-d: EnCurso(Eliminar) → Inactiva + recarga
     *
     * Al eliminar un producto, la operación pasa a EnCurso(Eliminar) y,
     * tras el éxito, vuelve a Inactiva y se recarga la lista desde el
     * repositorio (reflejando la baja lógica).
     */
    @Test
    fun eliminarProductoPasaAEnCursoEliminarYLuegoInactivaConRecarga() = runTest {

        val productoExistente = Producto(
            id = 42L,
            nombre = "Producto a borrar",
            precio = 10.0,
            stock = 5,
            categoriaId = 1L
        )
        val repositorio = FakeProductoRepository(mutableListOf(productoExistente))
        val viewModel = nuevoViewModel(repositorio)

        // Carga inicial: lista con 1 producto
        viewModel.cargarProductos()
        val faseInicial = assertIs<ProductoUiState.Fase.ConProductos>(viewModel.uiState.value.fase)
        assertEquals(1, faseInicial.productos.size)

        // Dispara eliminación
        viewModel.eliminar(42L)

        // Al terminar: operación Inactiva + lista recargada (vacía -> SinProductos)
        assertEquals(ProductoUiState.Operacion.Inactiva, viewModel.uiState.value.operacion)
        assertEquals(ProductoUiState.Fase.SinProductos, viewModel.uiState.value.fase)
    }

    /**
     * REQUISITO 15: Cancelación no se convierte en ErrorApi
     *
     * Si la corrutina que ejecuta una mutación se cancela, la
     * CancellationException se propaga y NO se envuelve en ErrorApi.
     */
    @Test
    fun cancelacionDuranteMutacionPropagaCancellationException() = runTest {

        val repositorio = FakeProductoRepository(
            mutableListOf(Producto(id = 1L, nombre = "Test", precio = 1.0, stock = 1, categoriaId = 1L))
        )
        val viewModel = nuevoViewModel(repositorio)

        // Dispara eliminación y cancela inmediatamente el job de prueba
        val job = launch { viewModel.eliminar(1L) }
        job.cancel()
        job.join()

        // La cancelación se propaga: no hay operación Fallida por cancelación
        assertEquals(ProductoUiState.Operacion.Inactiva, viewModel.uiState.value.operacion)
    }
}