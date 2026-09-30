package pe.edu.upeu.pharmamobil.presentation.cliente

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upeu.pharmamobil.domain.usecase.ListarClientesUseCase

/**
 * Cartera de clientes: carga el listado del backend y filtra por el texto de
 * búsqueda. El registro y la edición viven en [ClienteFormViewModel].
 */
class ClienteViewModel(
    private val listarClientes: ListarClientesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClienteUiState())
    val uiState: StateFlow<ClienteUiState> = _uiState.asStateFlow()

    fun cargarClientes() {

        viewModelScope.launch {

            _uiState.update {
                it.copy(fase = ClienteUiState.Fase.Cargando)
            }

            val fase = listarClientes().fold(
                onSuccess = { clientes ->
                    if (clientes.isEmpty()) {
                        ClienteUiState.Fase.SinClientes
                    } else {
                        ClienteUiState.Fase.ConClientes(clientes.map { it.aUi() })
                    }
                },
                onFailure = { fallo ->
                    ClienteUiState.Fase.Error(
                        fallo.message ?: "No se pudo cargar la cartera de clientes"
                    )
                }
            )

            _uiState.update {
                it.copy(fase = fase)
            }
        }
    }

    fun onBusquedaChange(busqueda: String) {
        _uiState.update {
            it.copy(busqueda = busqueda)
        }
    }
}