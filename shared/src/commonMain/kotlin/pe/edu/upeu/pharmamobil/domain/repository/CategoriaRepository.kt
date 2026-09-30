package pe.edu.upeu.pharmamobil.domain.repository

import pe.edu.upeu.pharmamobil.domain.model.Categoria

/** Catálogo de categorías del backend (GET /api/v1/categorias). */
interface CategoriaRepository {
    suspend fun listar(): List<Categoria>
}