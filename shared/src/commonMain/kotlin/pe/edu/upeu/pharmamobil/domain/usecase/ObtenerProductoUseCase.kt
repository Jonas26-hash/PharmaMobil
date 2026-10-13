package pe.edu.upeu.pharmamobil.domain.usecase

import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.repository.ProductoRepository

class ObtenerProductoUseCase(
    private val productoRepository: ProductoRepository
) {

    suspend operator fun invoke(productoId: Long): Result<Producto> = resultadoDe {
        productoRepository.obtenerPorId(productoId)
    }
}
