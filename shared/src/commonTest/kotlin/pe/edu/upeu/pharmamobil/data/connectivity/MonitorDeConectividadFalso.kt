package pe.edu.upeu.pharmamobil.data.connectivity

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import pe.edu.upeu.pharmamobil.domain.connectivity.MonitorDeConectividad

class MonitorDeConectividadFalso(conectadoInicial: Boolean = true) : MonitorDeConectividad {

    private val estado = MutableStateFlow(conectadoInicial)

    override fun estaConectado(): Boolean = estado.value

    override fun observarConectividad(): Flow<Boolean> = estado.asStateFlow()

    fun cambiar(valor: Boolean) {
        estado.value = valor
    }
}
