package pe.edu.upeu.pharmamobil.di

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.koin.core.Koin
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import pe.edu.upeu.pharmamobil.data.connectivity.MonitorDeConectividadFalso
import pe.edu.upeu.pharmamobil.data.local.CategoriaRepositorioOffline
import pe.edu.upeu.pharmamobil.data.local.ClienteRepositorioOffline
import pe.edu.upeu.pharmamobil.data.local.CreadorDeDriver
import pe.edu.upeu.pharmamobil.data.local.ProductoRepositorioOffline
import pe.edu.upeu.pharmamobil.data.local.RegistroDeSincronizacion
import pe.edu.upeu.pharmamobil.data.local.RegistroDeSincronizacionFalso
import pe.edu.upeu.pharmamobil.data.local.crearDriverDePrueba
import pe.edu.upeu.pharmamobil.domain.connectivity.MonitorDeConectividad
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
import pe.edu.upeu.pharmamobil.domain.usecase.ObservarClientesUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.ObservarProductosUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.ObtenerClienteUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.ObtenerProductoUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.ObtenerUltimaSincronizacionUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.RegistrarClienteUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.RegistrarProductoUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.RegistrarVentaUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.SincronizarClientesUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.SincronizarProductosUseCase
import pe.edu.upeu.pharmamobil.presentation.cliente.ClienteFormViewModel
import pe.edu.upeu.pharmamobil.presentation.cliente.ClienteViewModel
import pe.edu.upeu.pharmamobil.presentation.producto.ProductoViewModel
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertSame

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
        modules(
            dataModule,
            domainModule,
            presentationModule,
            module {
                single<CreadorDeDriver> {
                    object : CreadorDeDriver {
                        override fun crear() = crearDriverDePrueba()
                    }
                }
                single<MonitorDeConectividad> { MonitorDeConectividadFalso() }
                single<RegistroDeSincronizacion> { RegistroDeSincronizacionFalso() }
            }
        )
    }.koin

    @Test
    fun resuelveLosRepositoriosPorSuInterfazDeDominio() {

        val koin = grafoCompleto()

        assertIs<ProductoRepositorioOffline>(koin.get<ProductoRepository>())
        assertIs<ClienteRepositorioOffline>(koin.get<ClienteRepository>())
        assertIs<CategoriaRepositorioOffline>(koin.get<CategoriaRepository>())
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
        koin.get<ObservarProductosUseCase>()
        koin.get<SincronizarProductosUseCase>()
        koin.get<ObtenerUltimaSincronizacionUseCase>()
        koin.get<RegistrarVentaUseCase>()

        koin.get<RegistrarClienteUseCase>()
        koin.get<ActualizarClienteUseCase>()
        koin.get<EliminarClienteUseCase>()
        koin.get<ObtenerClienteUseCase>()
        koin.get<ListarClientesUseCase>()
        koin.get<ObservarClientesUseCase>()
        koin.get<SincronizarClientesUseCase>()

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
