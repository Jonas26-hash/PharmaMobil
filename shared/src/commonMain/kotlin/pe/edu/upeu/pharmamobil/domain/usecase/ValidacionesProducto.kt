package pe.edu.upeu.pharmamobil.domain.usecase

/**
 * Reglas de negocio del producto, compartidas por el Registro y la
 * Actualización para que la validación no viva en dos sitios con mensajes
 * distintos.
 *
 * Los umbrales reproducen las anotaciones de `ProductoRequestDTO` en
 * pharmaSoft (`@Size(min=3,max=150)`, `@DecimalMin("0.01")`, `@Min(0)`) para
 * avisar en el formulario antes de gastar una ida al servidor.
 */
internal object ValidacionesProducto {

    fun nombre(nombre: String): String? {
        val limpio = nombre.trim()
        return when {
            limpio.isEmpty() -> "El nombre del producto es obligatorio"
            limpio.length < 3 -> "El nombre debe tener entre 3 y 150 caracteres"
            limpio.length > 150 -> "El nombre debe tener entre 3 y 150 caracteres"
            else -> null
        }
    }

    fun precio(precio: String): String? {
        val precioValor = precio.toDoubleOrNull()
        return when {
            precioValor == null || !precioValor.isFinite() -> "Ingresa un precio numérico."
            precioValor < 0.01 -> "El precio debe ser mayor que cero"
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