package pe.edu.upeu.pharmamobil.data.local

interface RegistroDeSincronizacion {

    fun registrarUltimaSincronizacion(instante: Long)

    fun ultimaSincronizacion(): Long?
}
