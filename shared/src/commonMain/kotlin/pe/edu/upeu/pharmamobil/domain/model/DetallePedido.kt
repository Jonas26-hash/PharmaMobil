package pe.edu.upeu.pharmamobil.domain.model

import pe.edu.upeu.pharmamobil.producto.domain.model.Medicamento

data class DetallePedido(
    val medicamento: Medicamento,
    val cantidad: Int
) {
    init {
        require(cantidad > 0) {
            "La cantidad debe ser mayor que cero"
        }
    }

    fun subtotal(): Double {
        return medicamento.precio * cantidad
    }
}