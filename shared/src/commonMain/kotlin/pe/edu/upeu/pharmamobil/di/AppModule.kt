package pe.edu.upeu.pharmamobil.di

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import pe.edu.upeu.pharmamobil.data.auth.GestorSesion
import pe.edu.upeu.pharmamobil.data.firebase.TelemetriaFirebase
import pe.edu.upeu.pharmamobil.data.local.CategoriaLocal
import pe.edu.upeu.pharmamobil.data.local.EstadoColaLocalRepository
import pe.edu.upeu.pharmamobil.data.local.CategoriaRepositorioOffline
import pe.edu.upeu.pharmamobil.data.local.RegistroDeSincronizacion
import pe.edu.upeu.pharmamobil.data.local.RegistroSincronizacionMemoria
import pe.edu.upeu.pharmamobil.data.local.CreadorDeDriver
import pe.edu.upeu.pharmamobil.data.local.ClienteLocal
import pe.edu.upeu.pharmamobil.data.local.ClienteRepositorioOffline
import pe.edu.upeu.pharmamobil.data.local.ProductoLocal
import pe.edu.upeu.pharmamobil.data.local.ProductoRepositorioOffline
import pe.edu.upeu.pharmamobil.data.remote.AuthApi
import pe.edu.upeu.pharmamobil.data.remote.AuthRepositorioRemoto
import pe.edu.upeu.pharmamobil.data.remote.CategoriaApi
import pe.edu.upeu.pharmamobil.data.remote.ClienteApi
import pe.edu.upeu.pharmamobil.data.remote.ProductoApi
import pe.edu.upeu.pharmamobil.data.remote.VentaApi
import pe.edu.upeu.pharmamobil.data.remote.crearHttpClient
import pe.edu.upeu.pharmamobil.data.remote.crearMotorHttp
import pe.edu.upeu.pharmamobil.db.PharmaDb
import pe.edu.upeu.pharmamobil.domain.auth.AuthRepository
import pe.edu.upeu.pharmamobil.domain.permiso.GestorPermisos
import pe.edu.upeu.pharmamobil.domain.port.Telemetria
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
import pe.edu.upeu.pharmamobil.presentation.login.LoginViewModel
import pe.edu.upeu.pharmamobil.presentation.producto.ProductoFormViewModel
import pe.edu.upeu.pharmamobil.presentation.producto.ProductoViewModel

val dataModule = module {

    single { GestorSesion(get()) }
    single<Telemetria> { TelemetriaFirebase() }
    single<RegistroDeSincronizacion> { RegistroSincronizacionMemoria }

    single<HttpClientEngine> { crearMotorHttp() }
    single<HttpClient> {
        crearHttpClient(
            motor = get(),
            leerToken = { get<GestorSesion>().token() },
            alExpirarSesion = { get<GestorSesion>().limpiar() }
        )
    }

    single { AuthApi(get()) }
    single<AuthRepository> { AuthRepositorioRemoto(get(), get(), get()) }

    single { ProductoApi(get()) }
    single { ClienteApi(get()) }
    single { CategoriaApi(get()) }
    single { VentaApi(get()) }

    single { PharmaDb(get<CreadorDeDriver>().crear()) }
    single { ProductoLocal(get()) }
    single { ClienteLocal(get()) }
    single { CategoriaLocal(get()) }
    single { EstadoColaLocalRepository(get(), get()) }

    single { ProductoRepositorioOffline(get(), get(), get(), get(), get()) }
    single<ProductoRepository> { get<ProductoRepositorioOffline>() }

    single { ClienteRepositorioOffline(get(), get(), get()) }
    single<ClienteRepository> { get<ClienteRepositorioOffline>() }

    single { CategoriaRepositorioOffline(get(), get(), get()) }
    single<CategoriaRepository> { get<CategoriaRepositorioOffline>() }
}

val domainModule = module {
    factory { RegistrarProductoUseCase(get()) }
    factory { ActualizarProductoUseCase(get()) }
    factory { EliminarProductoUseCase(get()) }
    factory { ObtenerProductoUseCase(get()) }
    factory { ListarProductosUseCase(get()) }
    factory { ObservarProductosUseCase(get()) }
    factory { SincronizarProductosUseCase(get()) }
    factory { ObtenerUltimaSincronizacionUseCase(get()) }
    factory { RegistrarVentaUseCase(get()) }

    factory { RegistrarClienteUseCase(get()) }
    factory { ActualizarClienteUseCase(get()) }
    factory { EliminarClienteUseCase(get()) }
    factory { ObtenerClienteUseCase(get()) }
    factory { ListarClientesUseCase(get()) }
    factory { ObservarClientesUseCase(get()) }
    factory { SincronizarClientesUseCase(get()) }

    factory { ListarCategoriasUseCase(get()) }
}

val presentationModule = module {
    viewModel { LoginViewModel(get(), get()) }
    viewModel { ProductoViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { ProductoFormViewModel(get(), get(), get(), get(), get()) }
    viewModel { ClienteViewModel(get(), get()) }
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
