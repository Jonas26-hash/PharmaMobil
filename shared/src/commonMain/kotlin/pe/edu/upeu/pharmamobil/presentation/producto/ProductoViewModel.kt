package pe.edu.upeu.pharmamobil.presentation.producto

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upeu.pharmamobil.domain.ErrorApi
import pe.edu.upeu.pharmamobil.domain.ErrorDeApi
import pe.edu.upeu.pharmamobil.domain.mensajeParaUsuario
import pe.edu.upeu.pharmamobil.domain.usecase.ActualizarProductoUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.EliminarProductoUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.ObservarProductosUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.ProductoInvalidoException
import pe.edu.upeu.pharmamobil.domain.usecase.RegistrarProductoUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.SincronizarProductosUseCase

class ProductoViewModel(
    private val observarProductos: ObservarProductosUseCase,
    private val sincronizarProductos: SincronizarProductosUseCase,
    private val registrarProducto: RegistrarProductoUseCase? = null,
    private val actualizarProducto: ActualizarProductoUseCase? = null,
    private val eliminarProducto: EliminarProductoUseCase? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductoUiState())
    val uiState: StateFlow<ProductoUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observarProductos().collect { productos ->
                val fase = if (productos.isEmpty()) {
                    ProductoUiState.Fase.SinProductos
                } else {
                    ProductoUiState.Fase.ConProductos(productos.map { it.aUi() })
                }
                _uiState.update { it.copy(fase = fase) }
            }
        }
    }

    fun cargarProductos() {
        sincronizar()
    }

    fun sincronizar() {

        viewModelScope.launch {

            _uiState.update {
                it.copy(sincronizando = true, avisoRed = null)
            }

            sincronizarProductos().fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(sincronizando = false, avisoRed = null)
                    }
                },
                onFailure = { fallo ->
                    _uiState.update {
                        it.copy(
                            sincronizando = false,
                            avisoRed = fallo.aErrorApiDePresentacion().mensajeParaUsuario()
                        )
                    }
                }
            )
        }
    }

    fun onBusquedaChange(busqueda: String) {
        _uiState.update {
            it.copy(busqueda = busqueda)
        }
    }

    fun crear(
        nombre: String,
        precio: String,
        stock: String,
        estado: Boolean,
        categoriaId: Long
    ) = mutacion(ProductoUiState.TipoOperacion.Crear) {
        registrarProducto?.invoke(nombre, precio, stock, estado, categoriaId)
    }

    fun actualizar(
        id: Long,
        nombre: String,
        precio: String,
        stock: String,
        estado: Boolean,
        categoriaId: Long
    ) = mutacion(ProductoUiState.TipoOperacion.Actualizar) {
        actualizarProducto?.invoke(id, nombre, precio, stock, estado, categoriaId)
    }

    fun eliminar(productoId: Long) =
        mutacion(ProductoUiState.TipoOperacion.Eliminar) {
            eliminarProducto?.invoke(productoId)
        }

    private fun mutacion(
        tipo: ProductoUiState.TipoOperacion,
        operacion: suspend () -> Result<*>?
    ) {

        viewModelScope.launch {

            _uiState.update {
                it.copy(
                    operacion = ProductoUiState.Operacion.EnCurso(tipo)
                )
            }

            val resultado = operacion()

            val fallo = when {
                resultado == null -> ErrorApi.SinConexion(
                    "La operación no está disponible en esta configuración."
                )

                resultado.isFailure ->
                    resultado.exceptionOrNull()?.aErrorApiDePresentacion() ?: ErrorApi.Servidor(
                        "Se produjo un error inesperado."
                    )

                else -> null
            }

            if (fallo != null) {
                _uiState.update {
                    it.copy(
                        operacion = ProductoUiState.Operacion.Fallida(
                            tipo = tipo,
                            error = fallo,
                            mensaje = fallo.mensajeParaUsuario()
                        )
                    )
                }
                return@launch
            }

            _uiState.update {
                it.copy(operacion = ProductoUiState.Operacion.Inactiva)
            }
        }
    }

    fun limpiarOperacion() {
        _uiState.update {
            it.copy(operacion = ProductoUiState.Operacion.Inactiva)
        }
    }
}

internal fun Throwable.aErrorApiDePresentacion(): ErrorApi = when (this) {

    is ErrorDeApi -> error

    is ProductoInvalidoException ->
        ErrorApi.Validacion(
            mensaje = "Los datos del producto no cumplen las reglas del negocio",
            erroresPorCampo = errores.camposEnMapa()
        )

    is kotlinx.coroutines.CancellationException -> throw this

    else -> ErrorApi.Servidor(message ?: "Se produjo un error inesperado.")
}

internal fun pe.edu.upeu.pharmamobil.domain.usecase.ErroresDeProducto.camposEnMapa():
    Map<String, String> = buildMap {
    nombre?.let { put("nombre", it) }
    precio?.let { put("precio", it) }
    stock?.let { put("stock", it) }
    categoria?.let { put("categoriaId", it) }
}
