package pe.edu.upeu.pharmamobil.domain.usecase

import pe.edu.upeu.pharmamobil.domain.repository.ProductoRepository

class ObtenerUltimaSincronizacionUseCase(
    private val productoRepository: ProductoRepository
) {

    operator fun invoke(): Long? = productoRepository.ultimaSincronizacion()
}
