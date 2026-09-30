package pe.edu.upeu.pharmamobil.domain.usecase

import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.repository.ProductoRepository

/** Obtiene un producto puntual del inventario (rewrite del GET /{id}). */
class ObtenerProductoUseCase(
    private val productoRepository: ProductoRepository
) {

    suspend operator fun invoke(productoId: Long): Result<Producto> = resultadoDe {
        productoRepository.obtenerPorId(productoId)
    }
}