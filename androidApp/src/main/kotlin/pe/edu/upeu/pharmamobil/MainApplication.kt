package pe.edu.upeu.pharmamobil

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import pe.edu.upeu.pharmamobil.di.initKoin

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        initKoin {
            androidLogger()
            androidContext(this@MainApplication)
        }
    }
}
