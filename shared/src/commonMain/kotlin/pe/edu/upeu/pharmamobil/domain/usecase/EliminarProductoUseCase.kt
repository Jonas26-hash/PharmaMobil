package pe.edu.upeu.pharmamobil.domain.usecase

import pe.edu.upeu.pharmamobil.domain.repository.ProductoRepository

/** Elimina un producto del inventario. */
class EliminarProductoUseCase(
    private val productoRepository: ProductoRepository
) {

    suspend operator fun invoke(productoId: Long): Result<Unit> = resultadoDe {
        productoRepository.eliminar(productoId)
    }
}