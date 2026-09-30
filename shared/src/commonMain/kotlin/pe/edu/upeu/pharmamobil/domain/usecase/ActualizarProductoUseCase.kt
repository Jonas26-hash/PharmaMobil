package pe.edu.upeu.pharmamobil.domain.usecase

import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.repository.ProductoRepository

/** Actualiza un producto existente. Comparte la validación con el registro. */
class ActualizarProductoUseCase(
    private val productoRepository: ProductoRepository
) {

    suspend operator fun invoke(
        id: Long,
        nombre: String,
        precio: String,
        stock: String,
        estado: Boolean,
        categoriaId: Long
    ): Result<Producto> {

        val errores = ErroresDeProducto(
            nombre = ValidacionesProducto.nombre(nombre),
            precio = ValidacionesProducto.precio(precio),
            stock = ValidacionesProducto.stock(stock),
            categoria = if (categoriaId <= 0L) "Selecciona una categoría." else null
        )

        if (errores.hayErrores) {
            return Result.failure(ProductoInvalidoException(errores))
        }

        return resultadoDe {
            productoRepository.actualizar(
                Producto(
                    id = id,
                    nombre = nombre.trim(),
                    precio = precio.toDouble(),
                    stock = stock.toInt(),
                    categoriaId = categoriaId,
                    activo = estado
                )
            )
        }
    }
}