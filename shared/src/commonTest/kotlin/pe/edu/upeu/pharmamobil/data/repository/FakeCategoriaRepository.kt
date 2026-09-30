package pe.edu.upeu.pharmamobil.data.repository

import pe.edu.upeu.pharmamobil.domain.model.Categoria
import pe.edu.upeu.pharmamobil.domain.repository.CategoriaRepository

/** Doble del catálogo de categorías del backend, con fallo configurable. */
class FakeCategoriaRepository(
    private val categorias: List<Categoria> = DEFAULT_CATEGORIAS
) : CategoriaRepository {

    var fallaAlListar: Throwable? = null

    override suspend fun listar(): List<Categoria> {
        fallaAlListar?.let { throw it }
        return categorias
    }

    companion object {
        val DEFAULT_CATEGORIAS = listOf(
            Categoria(id = 1L, nombre = "Analgésico"),
            Categoria(id = 2L, nombre = "Antibiótico")
        )
    }
}