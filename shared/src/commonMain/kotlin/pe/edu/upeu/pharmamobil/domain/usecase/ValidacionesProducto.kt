package pe.edu.upeu.pharmamobil.domain.usecase

/**
 * Reglas de negocio del producto, compartidas por el Registro y la
 * Actualización para que la validación no viva en dos sitios con mensajes
 * distintos.
 */
internal object ValidacionesProducto {

    fun nombre(nombre: String): String? {
        return if (nombre.isBlank()) "El nombre es obligatorio." else null
    }

    fun precio(precio: String): String? {
        val precioValor = precio.toDoubleOrNull()
        return when {
            precioValor == null || !precioValor.isFinite() -> "Ingresa un precio numérico."
            precioValor <= 0.0 -> "El precio debe ser mayor que cero."
            else -> null
        }
    }

    fun stock(stock: String): String? {
        val stockValor = stock.toIntOrNull()
        return when {
            stockValor == null -> "Ingresa un stock entero."
            stockValor < 0 -> "El stock no puede ser negativo."
            else -> null
        }
    }
}