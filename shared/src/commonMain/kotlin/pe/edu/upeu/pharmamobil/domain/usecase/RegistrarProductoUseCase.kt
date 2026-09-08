package pe.edu.upeu.pharmamobil.domain.usecase

import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.repository.ProductoRepository

data class ErroresDeProducto(
    val nombre: String? = null,
    val precio: String? = null,
    val stock: String? = null
) {

    val hayErrores: Boolean
        get() = nombre != null || precio != null || stock != null
}

class ProductoInvalidoException(
    val errores: ErroresDeProducto
) : IllegalArgumentException("Los datos del producto no cumplen las reglas del negocio")

/**
 * Registra un producto en el inventario. Concentra la validación (antes
 * repartida entre la pantalla y el repositorio) en el dominio; la pantalla
 * solo muestra los errores que esta devuelve.
 */
class RegistrarProductoUseCase(
    private val productoRepository: ProductoRepository
) {

    suspend operator fun invoke(
        nombre: String,
        descripcion: String?,
        precio: String,
        stock: String,
        requiereReceta: Boolean,
        categoria: String
    ): Result<Producto> {

        val errores = ErroresDeProducto(
            nombre = validarNombre(nombre),
            precio = validarPrecio(precio),
            stock = validarStock(stock)
        )

        if (errores.hayErrores) {
            return Result.failure(ProductoInvalidoException(errores))
        }

        return resultadoDe {
            productoRepository.registrar(
                Producto(
                    id = 0L,
                    nombre = nombre.trim(),
                    descripcion = descripcion?.trim(),
                    precio = precio.toDouble(),
                    stock = stock.toInt(),
                    requiereReceta = requiereReceta,
                    categoria = categoria.trim()
                )
            )
        }
    }

    private fun validarNombre(nombre: String): String? {
        return if (nombre.isBlank()) "El nombre es obligatorio." else null
    }

    private fun validarPrecio(precio: String): String? {
        val precioValor = precio.toDoubleOrNull()
        return when {
            precioValor == null || !precioValor.isFinite() -> "Ingresa un precio numérico."
            precioValor <= 0.0 -> "El precio debe ser mayor que cero."
            else -> null
        }
    }

    private fun validarStock(stock: String): String? {
        val stockValor = stock.toIntOrNull()
        return when {
            stockValor == null -> "Ingresa un stock entero."
            stockValor < 0 -> "El stock no puede ser negativo."
            else -> null
        }
    }
}