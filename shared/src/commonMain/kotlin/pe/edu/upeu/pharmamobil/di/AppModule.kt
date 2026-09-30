package pe.edu.upeu.pharmamobil.di

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import pe.edu.upeu.pharmamobil.data.remote.CategoriaApi
import pe.edu.upeu.pharmamobil.data.remote.CategoriaRepositorioRemoto
import pe.edu.upeu.pharmamobil.data.remote.ClienteApi
import pe.edu.upeu.pharmamobil.data.remote.ClienteRepositorioRemoto
import pe.edu.upeu.pharmamobil.data.remote.ProductoApi
import pe.edu.upeu.pharmamobil.data.remote.ProductoRepositorioRemoto
import pe.edu.upeu.pharmamobil.data.remote.VentaApi
import pe.edu.upeu.pharmamobil.data.remote.crearHttpClient
import pe.edu.upeu.pharmamobil.data.remote.crearMotorHttp
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
import pe.edu.upeu.pharmamobil.presentation.producto.ProductoFormViewModel
import pe.edu.upeu.pharmamobil.presentation.producto.ProductoViewModel

val dataModule = module {

    // Un único motor HTTP por plataforma y un único cliente con la misma
    // configuración para toda la app.
    single<HttpClientEngine> { crearMotorHttp() }
    single<HttpClient> { crearHttpClient(get()) }

    single { ProductoApi(get()) }
    single { ClienteApi(get()) }
    single { CategoriaApi(get()) }
    single { VentaApi(get()) }

    // Los datos vienen del backend REST; los repositorios en memoria quedan
    // como referencia de pruebas, no como la fuente de la aplicación.
    single<ProductoRepository> { ProductoRepositorioRemoto(get(), get()) }
    single<ClienteRepository> { ClienteRepositorioRemoto(get()) }
    single<CategoriaRepository> { CategoriaRepositorioRemoto(get()) }
}

val domainModule = module {
    factory { RegistrarProductoUseCase(get()) }
    factory { ActualizarProductoUseCase(get()) }
    factory { EliminarProductoUseCase(get()) }
    factory { ObtenerProductoUseCase(get()) }
    factory { ListarProductosUseCase(get()) }
    factory { RegistrarVentaUseCase(get()) }

    factory { RegistrarClienteUseCase(get()) }
    factory { ActualizarClienteUseCase(get()) }
    factory { EliminarClienteUseCase(get()) }
    factory { ObtenerClienteUseCase(get()) }
    factory { ListarClientesUseCase(get()) }

    factory { ListarCategoriasUseCase(get()) }
}

val presentationModule = module {
    viewModel { ProductoViewModel(get()) }
    viewModel { ProductoFormViewModel(get(), get(), get(), get(), get()) }
    viewModel { ClienteViewModel(get()) }
    viewModel { ClienteFormViewModel(get(), get(), get(), get()) }
}

expect val platformModule: Module

fun initKoin(configuracionAdicional: KoinApplication.() -> Unit = {}) {
    startKoin {
        configuracionAdicional()
        modules(
            dataModule,
            domainModule,
            presentationModule,
            platformModule
        )
    }
}