package pe.edu.upeu.pharmamobil.domain.usecase

import pe.edu.upeu.pharmamobil.domain.model.Venta
import pe.edu.upeu.pharmamobil.domain.repository.ProductoRepository

/**
 * Corta la venta de un producto a un cliente y entrega la venta con su total.
 * Los tres datos son valores reales del backend: la venta se registra contra
 * POST /api/v1/ventas.
 */
class RegistrarVentaUseCase(
    private val productoRepository: ProductoRepository
) {

    suspend operator fun invoke(
        clienteId: Long,
        productoId: Long,
        cantidad: Int
    ): Result<Venta> = resultadoDe {
        productoRepository.registrarVenta(clienteId, productoId, cantidad)
    }
}