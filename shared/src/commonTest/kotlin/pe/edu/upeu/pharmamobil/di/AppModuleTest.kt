package pe.edu.upeu.pharmamobil.di

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.koin.core.Koin
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import pe.edu.upeu.pharmamobil.data.remote.CategoriaRepositorioRemoto
import pe.edu.upeu.pharmamobil.data.remote.ClienteRepositorioRemoto
import pe.edu.upeu.pharmamobil.data.remote.ProductoRepositorioRemoto
import pe.edu.upeu.pharmamobil.domain.repository.CategoriaRepository
import pe.edu.upeu.pharmamobil.domain.repository.ClienteRepository
import pe.edu.upeu.pharmamobil.domain.repository.ProductoRepository
import pe.edu.upeu.pharmamobil.domain.usecase.ActualizarClienteUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.ActualizarProductoUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.EliminarClienteUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.EliminarProductoUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.ListarCategoriasUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.ListarClientesUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.ListarProductosUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.ObtenerClienteUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.ObtenerProductoUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.RegistrarClienteUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.RegistrarProductoUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.RegistrarVentaUseCase
import pe.edu.upeu.pharmamobil.presentation.cliente.ClienteFormViewModel
import pe.edu.upeu.pharmamobil.presentation.cliente.ClienteViewModel
import pe.edu.upeu.pharmamobil.presentation.producto.ProductoViewModel
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertSame

/**
 * Comprueba que el grafo se ensambla sin arrancar la aplicación: si una
 * definición falta o está declarada con el tipo equivocado, falla aquí.
 *
 * Los repositorios resueltos son las variantes remotas (REST). Construir el
 * [HttpClient] de Koin en una prueba es seguro: no sale a la red hasta que
 * alguien haga una petición.
 *
 * [ProductoFormViewModel] no se resuelve aquí a propósito: su init pide el
 * catálogo de categorías (una llamada real de red). Su construcción queda
 * cubierta con dobles en ProductoFormViewModelTest.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AppModuleTest {

    @BeforeTest
    fun instalarMain() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun detenerKoin() {
        stopKoin()
        Dispatchers.resetMain()
    }

    private fun grafoCompleto(): Koin = startKoin {
        modules(dataModule, domainModule, presentationModule, platformModule)
    }.koin

    @Test
    fun resuelveLosRepositoriosPorSuInterfazDeDominio() {

        val koin = grafoCompleto()

        assertIs<ProductoRepositorioRemoto>(koin.get<ProductoRepository>())
        assertIs<ClienteRepositorioRemoto>(koin.get<ClienteRepository>())
        assertIs<CategoriaRepositorioRemoto>(koin.get<CategoriaRepository>())
    }

    @Test
    fun losRepositoriosSonUnicosEnTodaLaAplicacion() {

        val koin = grafoCompleto()

        assertSame(
            koin.get<ProductoRepository>(),
            koin.get<ProductoRepository>()
        )
        assertSame(
            koin.get<ClienteRepository>(),
            koin.get<ClienteRepository>()
        )
    }

    @Test
    fun resuelveLosCasosDeUsoConSusRepositorios() {

        val koin = grafoCompleto()

        koin.get<RegistrarProductoUseCase>()
        koin.get<ActualizarProductoUseCase>()
        koin.get<EliminarProductoUseCase>()
        koin.get<ObtenerProductoUseCase>()
        koin.get<ListarProductosUseCase>()
        koin.get<RegistrarVentaUseCase>()

        koin.get<RegistrarClienteUseCase>()
        koin.get<ActualizarClienteUseCase>()
        koin.get<EliminarClienteUseCase>()
        koin.get<ObtenerClienteUseCase>()
        koin.get<ListarClientesUseCase>()

        koin.get<ListarCategoriasUseCase>()
    }

    @Test
    fun resuelveLosViewModelDeListado() {

        val koin = grafoCompleto()

        koin.get<ProductoViewModel>()
        koin.get<ClienteViewModel>()
        koin.get<ClienteFormViewModel>()
    }
}