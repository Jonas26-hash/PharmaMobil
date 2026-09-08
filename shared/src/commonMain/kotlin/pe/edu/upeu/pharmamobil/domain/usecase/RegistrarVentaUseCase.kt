package pe.edu.upeu.pharmamobil.domain.usecase

import pe.edu.upeu.pharmamobil.domain.model.Venta
import pe.edu.upeu.pharmamobil.domain.repository.ProductoRepository

/** Corta la venta de un producto y entrega la venta con su total. */
class RegistrarVentaUseCase(
    private val productoRepository: ProductoRepository
) {

    suspend operator fun invoke(productoId: Long, cantidad: Int): Result<Venta> = resultadoDe {
        productoRepository.registrarVenta(productoId, cantidad)
    }
}