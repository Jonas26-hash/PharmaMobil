package pe.edu.upeu.pharmamobil.domain.repository

import pe.edu.upeu.pharmamobil.domain.model.Categoria

interface CategoriaRepository {
    suspend fun listar(): List<Categoria>
}
