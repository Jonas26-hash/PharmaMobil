package pe.edu.upeu.pharmamobil.presentation.producto

/**
 * Estado del formulario de producto (registro o edición). El id nulo indica
 * que se está registrando; presente, que se está editando el producto.
 */
data class ProductoFormUiState(
    val cargandoProducto: Boolean = false,
    val categorias: List<CategoriaUi> = emptyList(),
    val formulario: FormularioProducto = FormularioProducto(),
    val guardando: Boolean = false,
    val eliminando: Boolean = false,
    val mensajeExito: String? = null,
    val mensaje: String? = null
)

data class FormularioProducto(
    val id: Long? = null,
    val nombre: String = "",
    val precio: String = "",
    val stock: String = "",
    val categoriaId: Long? = null,
    val estadoActivo: Boolean = true,
    val nombreError: String? = null,
    val precioError: String? = null,
    val stockError: String? = null,
    val categoriaError: String? = null
)