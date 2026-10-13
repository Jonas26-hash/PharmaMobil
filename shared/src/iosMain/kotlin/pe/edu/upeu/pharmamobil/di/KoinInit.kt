package pe.edu.upeu.pharmamobil.di

import org.koin.core.module.Module
import org.koin.dsl.module
import pe.edu.upeu.pharmamobil.data.security.AlmacenSeguroKeychain
import pe.edu.upeu.pharmamobil.domain.security.AlmacenSeguro

/**
 * Implementación iOS del almacén seguro (Keychain). No necesita `Context`:
 * usa identificadores fijos de servicio y cuenta.
 */
actual val platformModule: Module = module {
    single<AlmacenSeguro> { AlmacenSeguroKeychain() }
}

fun initKoinIos() = initKoin()
