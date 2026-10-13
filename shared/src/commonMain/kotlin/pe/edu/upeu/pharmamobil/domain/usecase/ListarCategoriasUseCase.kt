package pe.edu.upeu.pharmamobil.domain.usecase

import pe.edu.upeu.pharmamobil.domain.model.Categoria
import pe.edu.upeu.pharmamobil.domain.repository.CategoriaRepository

class ListarCategoriasUseCase(
    private val categoriaRepository: CategoriaRepository
) {

    suspend operator fun invoke(): Result<List<Categoria>> = resultadoDe {
        categoriaRepository.listar()
    }
}
