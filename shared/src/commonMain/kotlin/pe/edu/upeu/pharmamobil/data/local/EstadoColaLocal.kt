package pe.edu.upeu.pharmamobil.data.local

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class EstadoColaLocal(val pendientes: Long, val fallidas: Long)

class EstadoColaLocalRepository(private val producto: ProductoLocal, private val cliente: ClienteLocal) {
    fun observar(): Flow<EstadoColaLocal> = combine(
        producto.observarConteos(), cliente.observarConteos()
    ) { p, c -> EstadoColaLocal(p.first + c.first, p.second + c.second) }
}

private fun ProductoLocal.observarConteos(): Flow<Pair<Long, Long>> = kotlinx.coroutines.flow.combine(
    observarPendientes(), observarFallidas()
) { a, b -> a to b }

private fun ClienteLocal.observarConteos(): Flow<Pair<Long, Long>> = kotlinx.coroutines.flow.combine(
    observarPendientes(), observarFallidas()
) { a, b -> a to b }
