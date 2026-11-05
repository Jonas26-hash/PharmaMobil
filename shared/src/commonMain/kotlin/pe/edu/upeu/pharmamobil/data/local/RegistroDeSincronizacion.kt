package pe.edu.upeu.pharmamobil.data.local

interface RegistroDeSincronizacion {

    fun registrarUltimaSincronizacion(instante: Long)

    fun ultimaSincronizacion(): Long?
}

object RegistroSincronizacionMemoria : RegistroDeSincronizacion {
    private var instante: Long? = null
    override fun registrarUltimaSincronizacion(instante: Long) { this.instante = instante }
    override fun ultimaSincronizacion(): Long? = instante
}
