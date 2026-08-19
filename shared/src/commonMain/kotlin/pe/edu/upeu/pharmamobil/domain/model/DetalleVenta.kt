package pe.edu.upeu.pharmamobil.domain.model

data class DetalleVenta(
    val medicamentoId: String,
    val nombreMedicamento: String,
    val cantidad: Int,
    val precioUnitario: Double,
    val subtotal: Double
) {
    companion object {
        fun crear(
            medicamentoId: String,
            nombreMedicamento: String,
            cantidad: Int,
            precioUnitario: Double
        ): DetalleVenta {
            return DetalleVenta(
                medicamentoId = medicamentoId,
                nombreMedicamento = nombreMedicamento,
                cantidad = cantidad,
                precioUnitario = precioUnitario,
                subtotal = cantidad * precioUnitario
            )
        }
    }
}
