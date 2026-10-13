package pe.edu.upeu.pharmamobil.domain.usecase

import pe.edu.upeu.pharmamobil.domain.repository.ProductoRepository

class EliminarProductoUseCase(
    private val productoRepository: ProductoRepository
) {

    suspend operator fun invoke(productoId: Long): Result<Unit> = resultadoDe {
        productoRepository.eliminar(productoId)
    }
}
