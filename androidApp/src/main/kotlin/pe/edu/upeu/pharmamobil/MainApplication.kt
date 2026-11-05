package pe.edu.upeu.pharmamobil

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import pe.edu.upeu.pharmamobil.di.initKoin

class MainApplication : Application() {

    override fun onCreate() {
        try {
            super.onCreate()
            initKoin {
                androidLogger()
                androidContext(this@MainApplication)
            }
        } catch (e: Throwable) {
            android.util.Log.e("CRITICAL_CRASH", "Fallo al iniciar MainApplication", e)
            println("CRITICAL_CRASH: ${e.stackTraceToString()}")
        }
    }
}
