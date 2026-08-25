package pe.edu.upeu.pharmamobil.cliente.presentation.viewmodel

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
import pe.edu.upeu.pharmamobil.cliente.data.repository.ClienteRepositoryImpl
import pe.edu.upeu.pharmamobil.cliente.domain.model.Cliente
import pe.edu.upeu.pharmamobil.cliente.domain.repository.ClienteRepository
import pe.edu.upeu.pharmamobil.presentation.model.UiState

class ClienteViewModel(
    private val repository: ClienteRepository = ClienteRepositoryImpl()
) : ViewModel() {

    private val _clientesState = MutableStateFlow<UiState<List<Cliente>>>(UiState.Loading)
    val clientesState: StateFlow<UiState<List<Cliente>>> = _clientesState.asStateFlow()

    private val _registroClienteState = MutableStateFlow<UiState<Cliente>?>(null)
    val registroClienteState: StateFlow<UiState<Cliente>?> = _registroClienteState.asStateFlow()

    val clientes: StateFlow<List<Cliente>> = repository.observarClientes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        cargarClientes()

        repository.observarClientes()
            .onEach { lista ->
                if (lista.isNotEmpty()) {
                    _clientesState.value = UiState.Success(lista)
                }
            }
            .launchIn(viewModelScope)
    }

    fun cargarClientes() {
        viewModelScope.launch {
            _clientesState.value = UiState.Loading
            try {
                val lista = repository.obtenerClientes()
                if (lista.isEmpty()) {
                    _clientesState.value = UiState.Error("No hay clientes registrados")
                } else {
                    _clientesState.value = UiState.Success(lista)
                }
            } catch (e: Exception) {
                _clientesState.value = UiState.Error(
                    e.message ?: "Error desconocido al cargar clientes"
                )
            }
        }
    }

    fun registrarCliente(
        nombre: String,
        apellido: String,
        dni: String,
        telefono: String?,
        email: String?,
        direccion: String?
    ) {
        viewModelScope.launch {
            _registroClienteState.value = UiState.Loading
            try {
                val cliente = repository.registrarCliente(
                    nombre = nombre,
                    apellido = apellido,
                    dni = dni,
                    telefono = telefono,
                    email = email,
                    direccion = direccion
                )
                _registroClienteState.value = UiState.Success(cliente)
            } catch (e: IllegalArgumentException) {
                _registroClienteState.value = UiState.Error(e.message ?: "Error al registrar")
            } catch (e: Exception) {
                _registroClienteState.value = UiState.Error(
                    e.message ?: "Error desconocido al registrar cliente"
                )
            }
        }
    }

    fun limpiarEstadoRegistroCliente() {
        _registroClienteState.value = null
    }
}
