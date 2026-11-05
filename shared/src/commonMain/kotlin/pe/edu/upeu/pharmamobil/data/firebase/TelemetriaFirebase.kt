package pe.edu.upeu.pharmamobil.data.firebase

import pe.edu.upeu.pharmamobil.domain.port.Telemetria

class TelemetriaFirebase : Telemetria {
    override fun registrarError(e: Throwable, contexto: Map<String, String>) = Unit
    override fun registrarEvento(nombre: String, datos: Map<String, String>) = Unit
}
