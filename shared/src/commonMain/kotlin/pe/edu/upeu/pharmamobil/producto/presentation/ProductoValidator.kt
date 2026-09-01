package pe.edu.upeu.pharmamobil.producto.presentation

object ProductoValidator {

    fun validarNombre(nombre: String): String? {
        if (nombre.isBlank()) {
            return "El nombre es obligatorio."
        }
        return null
    }

    fun validarPrecio(precio: String): String? {
        val precioConvertido = precio.toDoubleOrNull()
        return when {
            precioConvertido == null -> "Ingresa un precio numérico."
            precioConvertido <= 0.0 -> "El precio debe ser mayor que cero."
            else -> null
        }
    }

    fun validarStock(stock: String): String? {
        val stockConvertido = stock.toIntOrNull()
        return when {
            stockConvertido == null -> "Ingresa un stock entero."
            stockConvertido < 0 -> "El stock no puede ser negativo."
            else -> null
        }
    }
}