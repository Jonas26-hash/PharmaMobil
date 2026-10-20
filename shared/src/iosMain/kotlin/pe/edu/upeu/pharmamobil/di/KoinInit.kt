package pe.edu.upeu.pharmamobil.di

import org.koin.core.module.Module
import org.koin.dsl.module
import pe.edu.upeu.pharmamobil.data.connectivity.MonitorDeConectividadIos
import pe.edu.upeu.pharmamobil.data.local.CreadorDeDriver
import pe.edu.upeu.pharmamobil.data.local.CreadorDeDriverIos
import pe.edu.upeu.pharmamobil.data.security.AlmacenSeguroKeychain
import pe.edu.upeu.pharmamobil.domain.connectivity.MonitorDeConectividad
import pe.edu.upeu.pharmamobil.domain.security.AlmacenSeguro

actual val platformModule: Module = module {
    single<AlmacenSeguro> { AlmacenSeguroKeychain() }
    single<CreadorDeDriver> { CreadorDeDriverIos() }
    single<MonitorDeConectividad> { MonitorDeConectividadIos() }
}

fun initKoinIos() = initKoin()
