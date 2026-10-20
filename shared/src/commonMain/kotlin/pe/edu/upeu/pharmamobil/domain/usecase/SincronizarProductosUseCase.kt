package pe.edu.upeu.pharmamobil.domain.usecase

import pe.edu.upeu.pharmamobil.domain.repository.ProductoRepository

class SincronizarProductosUseCase(
    private val productoRepository: ProductoRepository
) {

    suspend operator fun invoke(): Result<Unit> = resultadoDe {
        productoRepository.sincronizar()
    }
}
