package pe.edu.upeu.pharmamobil.domain.model

data class Producto(
    val id: Long,
    val nombre: String,
    val descripcion: String? = null,
    val precio: Double,
    val stock: Int,
    val requiereReceta: Boolean = false,
    val categoria: String = "",
    val categoriaId: Long? = null,
    val activo: Boolean = true,
    val pendiente: Boolean = false
) {
    init {
        require(nombre.isNotBlank()) {
            "El nombre del producto no puede estar vacío"
        }
        require(precio > 0 && precio.isFinite()) {
            "El precio debe ser un número mayor que cero"
        }
        require(stock >= 0) {
            "El stock no puede ser negativo"
        }
    }

    val requiereReposicion: Boolean
        get() = activo && stock < STOCK_MINIMO

    companion object {
        const val STOCK_MINIMO = 10
    }
}
