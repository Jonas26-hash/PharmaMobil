package pe.edu.upeu.pharmamobil.domain.usecase

import pe.edu.upeu.pharmamobil.domain.model.Venta
import pe.edu.upeu.pharmamobil.domain.repository.ProductoRepository

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
