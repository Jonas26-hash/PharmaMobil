package pe.edu.upeu.pharmamobil.presentation.cliente

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upeu.pharmamobil.domain.mensajeParaUsuario
import pe.edu.upeu.pharmamobil.domain.usecase.ObservarClientesUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.SincronizarClientesUseCase
import pe.edu.upeu.pharmamobil.presentation.producto.aErrorApiDePresentacion

class ClienteViewModel(
    private val observarClientes: ObservarClientesUseCase,
    private val sincronizarClientes: SincronizarClientesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClienteUiState())
    val uiState: StateFlow<ClienteUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observarClientes().collect { clientes ->
                val fase = if (clientes.isEmpty()) {
                    ClienteUiState.Fase.SinClientes
                } else {
                    ClienteUiState.Fase.ConClientes(clientes.map { it.aUi() })
                }
                _uiState.update { it.copy(fase = fase) }
            }
        }
    }

    fun cargarClientes() {
        sincronizar()
    }

    fun sincronizar() {

        viewModelScope.launch {

            _uiState.update {
                it.copy(sincronizando = true, avisoRed = null)
            }

            sincronizarClientes().fold(
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
}
