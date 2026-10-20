package pe.edu.upeu.pharmamobil.data.local

import pe.edu.upeu.pharmamobil.data.remote.CategoriaApi
import pe.edu.upeu.pharmamobil.data.remote.aDominio
import pe.edu.upeu.pharmamobil.data.remote.alAplicacion
import pe.edu.upeu.pharmamobil.domain.connectivity.MonitorDeConectividad
import pe.edu.upeu.pharmamobil.domain.model.Categoria
import pe.edu.upeu.pharmamobil.domain.repository.CategoriaRepository

class CategoriaRepositorioOffline(
    private val local: CategoriaLocal,
    private val remoto: CategoriaApi,
    private val conectividad: MonitorDeConectividad
) : CategoriaRepository {

    override suspend fun listar(): List<Categoria> = alAplicacion {
        if (conectividad.estaConectado()) {
            try {
                val remotos = remoto.listar().map { it.aDominio() }
                local.reemplazarTodo(remotos)
                return@alAplicacion local.todas()
            } catch (fallo: Throwable) {
                val guardadas = local.todas()
                if (guardadas.isNotEmpty()) return@alAplicacion guardadas
                throw fallo
            }
        }
        local.todas()
    }
}
