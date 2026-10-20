package pe.edu.upeu.pharmamobil.domain.connectivity

import kotlinx.coroutines.flow.Flow

interface MonitorDeConectividad {

    fun estaConectado(): Boolean

    fun observarConectividad(): Flow<Boolean>
}
