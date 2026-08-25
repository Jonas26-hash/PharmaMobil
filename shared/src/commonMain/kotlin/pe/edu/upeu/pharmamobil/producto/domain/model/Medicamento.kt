package pe.edu.upeu.pharmamobil.producto.domain.model

data class Medicamento(
    val id: String,
    val nombre: String,
    val descripcion: String?,
    val precio: Double,
    val stock: Int,
    val requiereReceta: Boolean,
    val categoria: String
) {
    init {
        require(nombre.isNotBlank()) { "El nombre del medicamento no puede estar vacío" }
        require(precio >= 0.0) { "El precio no puede ser negativo" }
        require(stock >= 0) { "El stock no puede ser negativo" }
    }
}
