package pe.edu.upeu.pharmamobil.domain.model

/**
 * Categoría de medicamento, reflejo del catálogo que publica el backend
 * (GET /api/v1/categorias). El dominio la conserva como modelo propio para
 * que la lista de categorías del formulario no dependa de un DTO de red.
 */
data class Categoria(
    val id: Long,
    val nombre: String,
    val descripcion: String? = null,
    val activo: Boolean = true
) {
    init {
        require(nombre.isNotBlank()) {
            "El nombre de la categoría no puede estar vacío"
        }
    }
}