package pe.edu.upeu.pharmamobil.data.remote

import pe.edu.upeu.pharmamobil.domain.model.Categoria
import pe.edu.upeu.pharmamobil.domain.repository.CategoriaRepository

class CategoriaRepositorioRemoto(
    private val api: CategoriaApi
) : CategoriaRepository {

    override suspend fun listar(): List<Categoria> = alAplicacion {
        api.listar().map { it.aDominio() }
    }
}
