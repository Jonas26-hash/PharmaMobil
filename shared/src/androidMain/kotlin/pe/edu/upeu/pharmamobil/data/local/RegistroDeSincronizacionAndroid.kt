package pe.edu.upeu.pharmamobil.data.local

import android.content.Context

class RegistroDeSincronizacionAndroid(context: Context) : RegistroDeSincronizacion {

    private val preferencias =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    override fun registrarUltimaSincronizacion(instante: Long) {
        preferencias.edit().putLong(CLAVE, instante).apply()
    }

    override fun ultimaSincronizacion(): Long? {
        if (!preferencias.contains(CLAVE)) return null
        return preferencias.getLong(CLAVE, 0L)
    }

    private companion object {
        const val PREFS = "pharmamobil_sincronizacion"
        const val CLAVE = "ultima_sincronizacion"
    }
}
