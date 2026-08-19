package pe.edu.upeu.pharmamobil.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upeu.pharmamobil.data.repository.FarmaciaRepositoryImpl
import pe.edu.upeu.pharmamobil.domain.model.Medicamento
import pe.edu.upeu.pharmamobil.domain.model.Venta
import pe.edu.upeu.pharmamobil.domain.repository.FarmaciaRepository
import pe.edu.upeu.pharmamobil.presentation.model.UiState

class FarmaciaViewModel(
    private val repository: FarmaciaRepository = FarmaciaRepositoryImpl()
) : ViewModel() {

    private val _medicamentosState = MutableStateFlow<UiState<List<Medicamento>>>(UiState.Loading)
    val medicamentosState: StateFlow<UiState<List<Medicamento>>> = _medicamentosState.asStateFlow()

    private val _ventaState = MutableStateFlow<UiState<Venta>?>(null)
    val ventaState: StateFlow<UiState<Venta>?> = _ventaState.asStateFlow()

    val medicamentos: StateFlow<List<Medicamento>> = repository.observarMedicamentos()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        cargarMedicamentos()
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
}
