package pe.edu.upeu.pharmamobil.producto.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pe.edu.upeu.pharmamobil.presentation.model.UiState
import pe.edu.upeu.pharmamobil.producto.data.repository.ProductoRepositoryImpl
import pe.edu.upeu.pharmamobil.producto.domain.model.Medicamento
import pe.edu.upeu.pharmamobil.producto.domain.model.Venta
import pe.edu.upeu.pharmamobil.producto.domain.repository.ProductoRepository

class ProductoViewModel(
    private val repository: ProductoRepository = ProductoRepositoryImpl()
) : ViewModel() {

    private val _medicamentosState = MutableStateFlow<UiState<List<Medicamento>>>(UiState.Loading)
    val medicamentosState: StateFlow<UiState<List<Medicamento>>> = _medicamentosState.asStateFlow()

    private val _ventaState = MutableStateFlow<UiState<Venta>?>(null)
    val ventaState: StateFlow<UiState<Venta>?> = _ventaState.asStateFlow()

    private val _registroState = MutableStateFlow<UiState<Medicamento>?>(null)
    val registroState: StateFlow<UiState<Medicamento>?> = _registroState.asStateFlow()

    val medicamentos: StateFlow<List<Medicamento>> = repository.observarMedicamentos()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        cargarMedicamentos()

        repository.observarMedicamentos()
            .onEach { lista ->
                if (lista.isNotEmpty()) {
                    _medicamentosState.value = UiState.Success(lista)
                }
            }
            .launchIn(viewModelScope)
    }

    fun cargarMedicamentos() {
        viewModelScope.launch {
            _medicamentosState.value = UiState.Loading
            try {
                val lista = repository.obtenerMedicamentos()
                if (lista.isEmpty()) {
                    _medicamentosState.value = UiState.Error("No hay medicamentos disponibles")
                } else {
                    _medicamentosState.value = UiState.Success(lista)
                }
            } catch (e: Exception) {
                _medicamentosState.value = UiState.Error(
                    e.message ?: "Error desconocido al cargar medicamentos"
                )
            }
        }
    }

    fun venderMedicamento(medicamentoId: String, cantidad: Int = 1) {
        viewModelScope.launch {
            _ventaState.value = UiState.Loading
            try {
                val venta = repository.registrarVenta(medicamentoId, cantidad)
                _ventaState.value = UiState.Success(venta)
            } catch (e: IllegalArgumentException) {
                _ventaState.value = UiState.Error(e.message ?: "Error en la venta")
            } catch (e: NoSuchElementException) {
                _ventaState.value = UiState.Error(e.message ?: "Medicamento no encontrado")
            } catch (e: Exception) {
                _ventaState.value = UiState.Error(
                    e.message ?: "Error desconocido al procesar la venta"
                )
            }
        }
    }

    fun limpiarEstadoVenta() {
        _ventaState.value = null
    }

    fun registrarMedicamento(
        nombre: String,
        descripcion: String?,
        precio: Double,
        stock: Int,
        requiereReceta: Boolean,
        categoria: String
    ) {
        viewModelScope.launch {
            _registroState.value = UiState.Loading
            try {
                val medicamento = repository.registrarMedicamento(
                    nombre = nombre,
                    descripcion = descripcion,
                    precio = precio,
                    stock = stock,
                    requiereReceta = requiereReceta,
                    categoria = categoria
                )
                _registroState.value = UiState.Success(medicamento)
            } catch (e: IllegalArgumentException) {
                _registroState.value = UiState.Error(e.message ?: "Error al registrar")
            } catch (e: Exception) {
                _registroState.value = UiState.Error(
                    e.message ?: "Error desconocido al registrar medicamento"
                )
            }
        }
    }

    fun limpiarEstadoRegistro() {
        _registroState.value = null
    }
}
