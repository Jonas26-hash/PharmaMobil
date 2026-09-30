package pe.edu.upeu.pharmamobil.presentation.cliente

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upeu.pharmamobil.domain.usecase.ActualizarClienteUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.ClienteInvalidoException
import pe.edu.upeu.pharmamobil.domain.usecase.EliminarClienteUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.ObtenerClienteUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.RegistrarClienteUseCase

/**
 * Formulario de cliente: registro cuando el cliente aún no tiene id y edición
 * cuando la pantalla le pasa uno.
 */
class ClienteFormViewModel(
    private val registrarCliente: RegistrarClienteUseCase,
    private val actualizarCliente: ActualizarClienteUseCase,
    private val eliminarCliente: EliminarClienteUseCase,
    private val obtenerCliente: ObtenerClienteUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClienteFormUiState())
    val uiState: StateFlow<ClienteFormUiState> = _uiState.asStateFlow()

    /**
     * Prepara el formulario para la pantalla. Cada visita arranca limpia: el
     * ViewModel sobrevive a la navegación, así que sin este reset el formulario
     * de registro mostraría los datos del guardado anterior y el mensaje de
     * éxito anterior devolvería al listado nada más abrirlo.
     */
    fun configurarCliente(clienteId: Long?) {

        _uiState.update {
            it.copy(
                cargandoCliente = clienteId != null,
                formulario = FormularioCliente(),
                mensaje = null,
                mensajeExito = null
            )
        }

        if (clienteId == null) return

        viewModelScope.launch {

            obtenerCliente(clienteId).fold(
                onSuccess = { cliente ->
                    _uiState.update {
                        it.copy(
                            cargandoCliente = false,
                            formulario = FormularioCliente(
                                id = cliente.id,
                                nombre = cliente.nombre,
                                apellido = cliente.apellido,
                                dni = cliente.dni,
                                telefono = cliente.telefono.orEmpty(),
                                email = cliente.email.orEmpty(),
                                direccion = cliente.direccion.orEmpty(),
                                estadoActivo = cliente.activo
                            )
                        )
                    }
                },
                onFailure = { fallo ->
                    _uiState.update {
                        it.copy(
                            cargandoCliente = false,
                            mensaje = fallo.message ?: "No se pudo cargar el cliente"
                        )
                    }
                }
            )
        }
    }

    fun onNombreChange(nombre: String) {
        _uiState.update {
            it.copy(formulario = it.formulario.copy(nombre = nombre, nombreError = null), mensaje = null)
        }
    }

    fun onApellidoChange(apellido: String) {
        _uiState.update {
            it.copy(formulario = it.formulario.copy(apellido = apellido, apellidoError = null), mensaje = null)
        }
    }

    fun onDniChange(dni: String) {
        _uiState.update {
            it.copy(formulario = it.formulario.copy(dni = dni, dniError = null), mensaje = null)
        }
    }

    fun onTelefonoChange(telefono: String) {
        _uiState.update {
            it.copy(formulario = it.formulario.copy(telefono = telefono, telefonoError = null), mensaje = null)
        }
    }

    fun onEmailChange(email: String) {
        _uiState.update {
            it.copy(formulario = it.formulario.copy(email = email, emailError = null), mensaje = null)
        }
    }

    fun onDireccionChange(direccion: String) {
        _uiState.update {
            it.copy(formulario = it.formulario.copy(direccion = direccion), mensaje = null)
        }
    }

    fun onEstadoChange(activo: Boolean) {
        _uiState.update {
            it.copy(formulario = it.formulario.copy(estadoActivo = activo), mensaje = null)
        }
    }

    fun guardar() {

        if (_uiState.value.guardando) return

        viewModelScope.launch {

            _uiState.update {
                it.copy(guardando = true, mensaje = null, mensajeExito = null)
            }

            val formulario = _uiState.value.formulario

            val resultado = if (formulario.id == null) {
                registrarCliente(
                    nombre = formulario.nombre,
                    apellido = formulario.apellido,
                    dni = formulario.dni,
                    telefono = formulario.telefono,
                    email = formulario.email,
                    direccion = formulario.direccion,
                    estado = formulario.estadoActivo
                )
            } else {
                actualizarCliente(
                    id = formulario.id,
                    nombre = formulario.nombre,
                    apellido = formulario.apellido,
                    dni = formulario.dni,
                    telefono = formulario.telefono,
                    email = formulario.email,
                    direccion = formulario.direccion,
                    estado = formulario.estadoActivo
                )
            }

            resultado.fold(
                onSuccess = { cliente ->
                    val accion = if (formulario.id == null) "registrado" else "actualizado"
                    _uiState.update {
                        it.copy(
                            guardando = false,
                            mensajeExito = "Cliente \"${cliente.nombre}\" $accion correctamente"
                        )
                    }
                },
                onFailure = { fallo ->
                    when (fallo) {

                        is ClienteInvalidoException -> _uiState.update {
                            it.copy(
                                guardando = false,
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
                                guardando = false,
                                mensaje = fallo.message ?: "No se pudo guardar el cliente"
                            )
                        }
                    }
                }
            )
        }
    }

    fun eliminar() {

        val clienteId = _uiState.value.formulario.id ?: return
        if (_uiState.value.eliminando) return

        viewModelScope.launch {

            _uiState.update {
                it.copy(eliminando = true, mensaje = null)
            }

            eliminarCliente(clienteId).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            eliminando = false,
                            mensajeExito = "Cliente eliminado correctamente"
                        )
                    }
                },
                onFailure = { fallo ->
                    _uiState.update {
                        it.copy(
                            eliminando = false,
                            mensaje = fallo.message ?: "No se pudo eliminar el cliente"
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