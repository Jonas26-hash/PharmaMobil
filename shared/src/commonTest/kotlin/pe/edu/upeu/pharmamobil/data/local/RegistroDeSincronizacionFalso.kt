package pe.edu.upeu.pharmamobil.data.local

class RegistroDeSincronizacionFalso : RegistroDeSincronizacion {

    private var instante: Long? = null

    override fun registrarUltimaSincronizacion(instante: Long) {
        this.instante = instante
    }

    override fun ultimaSincronizacion(): Long? = instante

    fun borrar() {
        instante = null
    }
}
