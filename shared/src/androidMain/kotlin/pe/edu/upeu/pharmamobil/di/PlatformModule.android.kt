package pe.edu.upeu.pharmamobil.di

import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module
import pe.edu.upeu.pharmamobil.data.security.AlmacenSeguroAndroid
import pe.edu.upeu.pharmamobil.domain.security.AlmacenSeguro

/**
 * Implementación Android del almacén seguro. Necesita un `Context`, que Koin
 * entrega con `androidContext()` (registrado en [pe.edu.upeu.pharmamobil.MainApplication]).
 */
actual val platformModule: Module = module {
    single<AlmacenSeguro> { AlmacenSeguroAndroid(androidContext()) }
}
