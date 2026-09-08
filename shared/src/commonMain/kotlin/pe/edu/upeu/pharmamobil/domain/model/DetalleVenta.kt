package pe.edu.upeu.pharmamobil.domain.model

data class DetalleVenta(
    val productoId: Long,
    val nombreProducto: String,
    val cantidad: Int,
    val precioUnitario: Double,
    val subtotal: Double
) {
    companion object {
        fun crear(
            productoId: Long,
            nombreProducto: String,
            cantidad: Int,
            precioUnitario: Double
        ): DetalleVenta {
            return DetalleVenta(
                productoId = productoId,
                nombreProducto = nombreProducto,
                cantidad = cantidad,
                precioUnitario = precioUnitario,
                subtotal = cantidad * precioUnitario
            )
        }
    }
}