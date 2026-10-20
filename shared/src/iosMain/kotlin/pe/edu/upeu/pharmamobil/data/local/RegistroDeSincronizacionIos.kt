package pe.edu.upeu.pharmamobil.data.local

import platform.Foundation.NSUserDefaults

class RegistroDeSincronizacionIos : RegistroDeSincronizacion {

    private val preferencias = NSUserDefaults.standardUserDefaults

    override fun registrarUltimaSincronizacion(instante: Long) {
        preferencias.setInteger(instante, CLAVE)
    }

    override fun ultimaSincronizacion(): Long? {
        if (preferencias.objectForKey(CLAVE) == null) return null
        return preferencias.integerForKey(CLAVE)
    }

    private companion object {
        const val CLAVE = "ultima_sincronizacion"
    }
}
