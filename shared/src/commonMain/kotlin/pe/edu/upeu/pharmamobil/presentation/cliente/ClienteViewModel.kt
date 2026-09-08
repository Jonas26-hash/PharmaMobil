package pe.edu.upeu.pharmamobil.presentation.cliente

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upeu.pharmamobil.domain.usecase.ClienteInvalidoException
import pe.edu.upeu.pharmamobil.domain.usecase.ListarClientesUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.RegistrarClienteUseCase

class ClienteViewModel(
    private val registrarCliente: RegistrarClienteUseCase,
    private val listarClientes: ListarClientesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClienteUiState())
    val uiState: StateFlow<ClienteUiState> = _uiState.asStateFlow()

    init {
        cargarClientes()
    }

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

    fun onNombreChange(nombre: String) {
        _uiState.update {
            it.copy(
                formulario = it.formulario.copy(nombre = nombre, nombreError = null),
                mensajeExito = null
            )
        }
    }

    fun onApellidoChange(apellido: String) {
        _uiState.update {
            it.copy(
                formulario = it.formulario.copy(apellido = apellido, apellidoError = null),
                mensajeExito = null
            )
        }
    }

    fun onDniChange(dni: String) {
        _uiState.update {
            it.copy(
                formulario = it.formulario.copy(dni = dni, dniError = null),
                mensajeExito = null
            )
        }
    }

    fun onTelefonoChange(telefono: String) {
        _uiState.update {
            it.copy(
                formulario = it.formulario.copy(telefono = telefono, telefonoError = null),
                mensajeExito = null
            )
        }
    }

    fun onEmailChange(email: String) {
        _uiState.update {
            it.copy(
                formulario = it.formulario.copy(email = email, emailError = null),
                mensajeExito = null
            )
        }
    }

    fun onDireccionChange(direccion: String) {
        _uiState.update {
            it.copy(
                formulario = it.formulario.copy(direccion = direccion),
                mensajeExito = null
            )
        }
    }

    fun registrar() {

        if (_uiState.value.registrando) return

        viewModelScope.launch {

            _uiState.update {
                it.copy(registrando = true, mensajeExito = null)
            }

            val formulario = _uiState.value.formulario

            registrarCliente(
                nombre = formulario.nombre,
                apellido = formulario.apellido,
                dni = formulario.dni,
                telefono = formulario.telefono,
                email = formulario.email,
                direccion = formulario.direccion
            ).fold(
                onSuccess = { cliente ->
                    _uiState.update {
                        it.copy(
                            registrando = false,
                            formulario = FormularioCliente(),
                            mensajeExito = "Cliente \"${cliente.nombre}\" registrado correctamente"
                        )
                    }
                    cargarClientes()
                },
                onFailure = { fallo ->
                    when (fallo) {

                        is ClienteInvalidoException -> _uiState.update {
                            it.copy(
                                registrando = false,
                                formulario = it.formulario.copy(
                                    nombreError = fallo.errores.nombre,
                                    apellidoError = fallo.errores.apellido,
                                    dniError = fallo.errores.dni,
                                    emailError = fallo.errores.email,
                                    telefonoError = fallo.errores.telefono
                                )
                            )
                        }

                        else -> _uiState.update {
                            it.copy(
                                registrando = false,
                                fase = ClienteUiState.Fase.Error(
                                    fallo.message ?: "No se pudo registrar el cliente"
                                )
                            )
                        }
                    }
                }
            )
        }
    }
}