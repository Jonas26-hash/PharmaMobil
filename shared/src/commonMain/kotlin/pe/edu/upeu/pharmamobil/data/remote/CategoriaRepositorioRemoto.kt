package pe.edu.upeu.pharmamobil.data.remote

import pe.edu.upeu.pharmamobil.domain.model.Categoria
import pe.edu.upeu.pharmamobil.domain.repository.CategoriaRepository

/** Catálogo de categorías servido por el backend (GET /api/v1/categorias). */
class CategoriaRepositorioRemoto(
    private val api: CategoriaApi
) : CategoriaRepository {

    override suspend fun listar(): List<Categoria> = alAplicacion {
        api.listar().map { it.aDominio() }
    }
}