package pe.edu.upeu.pharmamobil.domain.model

/**
 * Producto del inventario de la botica. Conserva los campos propios del
 * proyecto (descripcion, categoria, requiereReceta, activo) dentro del modelo
 * del curso, que valida sus invariantes con [require] y marca la reposicion
 * contra un stock minimo de gondola.
 */
data class Producto(
    val id: Long,
    val nombre: String,
    val descripcion: String? = null,
    val precio: Double,
    val stock: Int,
    val requiereReceta: Boolean = false,
    val categoria: String = "",
    val activo: Boolean = true
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

    /**
     * Un producto activo necesita reposición cuando su stock cae por debajo
     * del mínimo que la botica mantiene en góndola.
     */
    val requiereReposicion: Boolean
        get() = activo && stock < STOCK_MINIMO

    companion object {
        const val STOCK_MINIMO = 10
    }
}