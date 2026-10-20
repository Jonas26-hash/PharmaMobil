package pe.edu.upeu.pharmamobil.di

import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module
import pe.edu.upeu.pharmamobil.data.connectivity.MonitorDeConectividadAndroid
import pe.edu.upeu.pharmamobil.data.local.CreadorDeDriver
import pe.edu.upeu.pharmamobil.data.local.CreadorDeDriverAndroid
import pe.edu.upeu.pharmamobil.data.security.AlmacenSeguroAndroid
import pe.edu.upeu.pharmamobil.domain.connectivity.MonitorDeConectividad
import pe.edu.upeu.pharmamobil.domain.security.AlmacenSeguro

actual val platformModule: Module = module {
    single<AlmacenSeguro> { AlmacenSeguroAndroid(androidContext()) }
    single<CreadorDeDriver> { CreadorDeDriverAndroid(androidContext()) }
    single<MonitorDeConectividad> { MonitorDeConectividadAndroid(androidContext()) }
}
