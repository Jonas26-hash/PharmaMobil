package pe.edu.upeu.pharmamobil.presentation.producto

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upeu.pharmamobil.domain.usecase.ActualizarProductoUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.EliminarProductoUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.ListarCategoriasUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.ObtenerProductoUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.ProductoInvalidoException
import pe.edu.upeu.pharmamobil.domain.usecase.RegistrarProductoUseCase

/**
 * Formulario de producto: registro cuando el producto aún no tiene id y
 * edición cuando la pantalla le pasa uno. También carga el catálogo de
 * categorías para el selector.
 */
class ProductoFormViewModel(
    private val registrarProducto: RegistrarProductoUseCase,
    private val actualizarProducto: ActualizarProductoUseCase,
    private val eliminarProducto: EliminarProductoUseCase,
    private val obtenerProducto: ObtenerProductoUseCase,
    private val listarCategorias: ListarCategoriasUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductoFormUiState())
    val uiState: StateFlow<ProductoFormUiState> = _uiState.asStateFlow()

    init {
        cargarCategorias()
    }

    fun cargarCategorias() {

        viewModelScope.launch {

            listarCategorias().fold(
                onSuccess = { categorias ->
                    _uiState.update {
                        it.copy(categorias = categorias.map { categoria -> categoria.aUi() })
                    }
                },
                onFailure = { fallo ->
                    _uiState.update {
                        it.copy(
                            mensaje = fallo.message ?: "No se pudieron cargar las categorías"
                        )
                    }
                }
            )
        }
    }

    /**
     * Prepara el formulario para la pantalla. Cada visita arranca limpia: el
     * ViewModel sobrevive a la navegación, así que sin este reset el formulario
     * de registro mostraría los datos del guardado anterior y el mensaje de
     * éxito anterior devolvería al listado nada más abrirlo.
     */
    fun configurarProducto(productoId: Long?) {

        _uiState.update {
            it.copy(
                cargandoProducto = productoId != null,
                formulario = FormularioProducto(),
                mensaje = null,
                mensajeExito = null
            )
        }

        if (productoId == null) return

        viewModelScope.launch {

            obtenerProducto(productoId).fold(
                onSuccess = { producto ->
                    _uiState.update {
                        it.copy(
                            cargandoProducto = false,
                            formulario = FormularioProducto(
                                id = producto.id,
                                nombre = producto.nombre,
                                precio = producto.precio.aTextoEditorial(),
                                stock = producto.stock.toString(),
                                categoriaId = producto.categoriaId,
                                estadoActivo = producto.activo
                            )
                        )
                    }
                },
                onFailure = { fallo ->
                    _uiState.update {
                        it.copy(
                            cargandoProducto = false,
                            mensaje = fallo.message ?: "No se pudo cargar el producto"
                        )
                    }
                }
            )
        }
    }

    fun onNombreChange(nombre: String) {
        _uiState.update {
            it.copy(
                formulario = it.formulario.copy(nombre = nombre, nombreError = null),
                mensaje = null
            )
        }
    }

    fun onPrecioChange(precio: String) {
        _uiState.update {
            it.copy(
                formulario = it.formulario.copy(precio = precio, precioError = null),
                mensaje = null
            )
        }
    }

    fun onStockChange(stock: String) {
        _uiState.update {
            it.copy(
                formulario = it.formulario.copy(stock = stock, stockError = null),
                mensaje = null
            )
        }
    }

    fun onCategoriaChange(categoriaId: Long) {
        _uiState.update {
            it.copy(
                formulario = it.formulario.copy(categoriaId = categoriaId, categoriaError = null),
                mensaje = null
            )
        }
    }

    fun onEstadoChange(activo: Boolean) {
        _uiState.update {
            it.copy(
                formulario = it.formulario.copy(estadoActivo = activo),
                mensaje = null
            )
        }
    }

    fun guardar() {

        if (_uiState.value.guardando) return

        viewModelScope.launch {

            _uiState.update {
                it.copy(guardando = true, mensaje = null, mensajeExito = null)
            }

            val formulario = _uiState.value.formulario
            val categoriaId = formulario.categoriaId ?: 0L

            val resultado = if (formulario.id == null) {
                registrarProducto(
                    nombre = formulario.nombre,
                    precio = formulario.precio,
                    stock = formulario.stock,
                    estado = formulario.estadoActivo,
                    categoriaId = categoriaId
                )
            } else {
                actualizarProducto(
                    id = formulario.id,
                    nombre = formulario.nombre,
                    precio = formulario.precio,
                    stock = formulario.stock,
                    estado = formulario.estadoActivo,
                    categoriaId = categoriaId
                )
            }

            resultado.fold(
                onSuccess = { producto ->
                    val accion = if (formulario.id == null) "registrado" else "actualizado"
                    _uiState.update {
                        it.copy(
                            guardando = false,
                            mensajeExito = "Producto \"${producto.nombre}\" $accion correctamente"
                        )
                    }
                },
                onFailure = { fallo ->
                    when (fallo) {

                        is ProductoInvalidoException -> _uiState.update {
                            it.copy(
                                guardando = false,
                                formulario = it.formulario.copy(
                                    nombreError = fallo.errores.nombre,
                                    precioError = fallo.errores.precio,
                                    stockError = fallo.errores.stock,
                                    categoriaError = fallo.errores.categoria
                                )
                            )
                        }

                        else -> _uiState.update {
                            it.copy(
                                guardando = false,
                                mensaje = fallo.message ?: "No se pudo guardar el producto"
                            )
                        }
                    }
                }
            )
        }
    }

    fun eliminar() {

        val productoId = _uiState.value.formulario.id ?: return
        if (_uiState.value.eliminando) return

        viewModelScope.launch {

            _uiState.update {
                it.copy(eliminando = true, mensaje = null)
            }

            eliminarProducto(productoId).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            eliminando = false,
                            mensajeExito = "Producto eliminado correctamente"
                        )
                    }
                },
                onFailure = { fallo ->
                    _uiState.update {
                        it.copy(
                            eliminando = false,
                            mensaje = fallo.message ?: "No se pudo eliminar el producto"
                        )
                    }
                }
            )
        }
    }

    fun consumirMensaje() {
        _uiState.update { it.copy(mensaje = null) }
    }

    /** La pantalla lo llama antes de volver al listado: el mensaje no se repite. */
    fun consumirMensajeExito() {
        _uiState.update { it.copy(mensajeExito = null) }
    }
}