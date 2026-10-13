package pe.edu.upeu.pharmamobil.domain.model

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
