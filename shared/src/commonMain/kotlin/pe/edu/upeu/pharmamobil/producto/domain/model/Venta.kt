package pe.edu.upeu.pharmamobil.producto.domain.model

data class Venta(
    val id: String,
    val fecha: String,
    val items: List<DetalleVenta>,
    val total: Double
) {
    companion object {
        fun crear(id: String, fecha: String, items: List<DetalleVenta>): Venta {
            val totalCalculado = items.sumOf { it.subtotal }
            return Venta(
                id = id,
                fecha = fecha,
                items = items,
                total = totalCalculado
            )
        }
    }
}
