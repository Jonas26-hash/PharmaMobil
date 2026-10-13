package pe.edu.upeu.pharmamobil.domain.usecase

import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.repository.ProductoRepository

class ListarProductosUseCase(
    private val productoRepository: ProductoRepository
) {

    suspend operator fun invoke(): Result<List<Producto>> = resultadoDe {
        productoRepository.listar()
    }
}
