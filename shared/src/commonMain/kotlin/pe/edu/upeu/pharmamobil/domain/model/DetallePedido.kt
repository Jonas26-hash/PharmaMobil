package pe.edu.upeu.pharmamobil.domain.model

data class DetallePedido(
    val producto: Producto,
    val cantidad: Int
) {
    init {
        require(cantidad > 0) {
            "La cantidad debe ser mayor que cero"
        }
    }

    fun subtotal(): Double {
        return producto.precio * cantidad
    }
}
