package pe.edu.upeu.pharmamobil.domain.usecase

import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.repository.ProductoRepository

data class ErroresDeProducto(
    val nombre: String? = null,
    val precio: String? = null,
    val stock: String? = null,
    val categoria: String? = null
) {

    val hayErrores: Boolean
        get() = nombre != null || precio != null || stock != null || categoria != null
}

class ProductoInvalidoException(
    val errores: ErroresDeProducto
) : IllegalArgumentException("Los datos del producto no cumplen las reglas del negocio")

/**
 * Registra un producto en el inventario contra el backend REST. Concentra la
 * validación (antes repartida entre la pantalla y el repositorio) en el
 * dominio; la pantalla solo muestra los errores que esta devuelve.
 *
 * Los campos que el backend no acepta (descripcion, requiereReceta) salen
 * del formulario: la identidad del producto la define nombre, precio, stock,
 * estado y la categoría, que es un id real del catálogo del servidor.
 */
class RegistrarProductoUseCase(
    private val productoRepository: ProductoRepository
) {

    suspend operator fun invoke(
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
            categoria = validarCategoria(categoriaId)
        )

        if (errores.hayErrores) {
            return Result.failure(ProductoInvalidoException(errores))
        }

        return resultadoDe {
            productoRepository.registrar(
                Producto(
                    id = 0L,
                    nombre = nombre.trim(),
                    precio = precio.toDouble(),
                    stock = stock.toInt(),
                    categoriaId = categoriaId,
                    activo = estado
                )
            )
        }
    }

    private fun validarCategoria(categoriaId: Long): String? {
        return if (categoriaId <= 0L) "Selecciona una categoría." else null
    }
}