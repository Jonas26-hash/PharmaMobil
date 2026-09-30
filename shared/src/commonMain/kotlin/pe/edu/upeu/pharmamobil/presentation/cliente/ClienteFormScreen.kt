package pe.edu.upeu.pharmamobil.presentation.cliente

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.collect
import pe.edu.upeu.pharmamobil.presentation.components.ValidatedTextField

@Composable
fun ClienteFormScreen(
    viewModel: ClienteFormViewModel,
    clienteId: Long?,
    onGuardado: () -> Unit,
    modifier: Modifier = Modifier
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(clienteId) {
        viewModel.configurarCliente(clienteId)
    }

    // Se observan en tiempo de ejecución (y no con el valor capturado al
    // componer) porque el ViewModel sobrevive a la navegación: un mensaje de
    // éxito viejo no debe devolver al listado al reabrir el formulario.
    LaunchedEffect(Unit) {
        viewModel.uiState.collect { estado ->

            val exito = estado.mensajeExito
            val mensaje = estado.mensaje

            when {

                exito != null -> {
                    viewModel.consumirMensajeExito()
                    onGuardado()
                }

                mensaje != null -> {
                    viewModel.consumirMensaje()
                    snackbarHostState.showSnackbar(message = mensaje)
                }
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {

        if (uiState.cargandoCliente) {

            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )

        } else {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                Text(
                    text = if (clienteId == null) "Registrar cliente" else "Editar cliente",
                    style = MaterialTheme.typography.headlineSmall
                )

                ValidatedTextField(
                    value = uiState.formulario.nombre,
                    onValueChange = viewModel::onNombreChange,
                    label = "Nombre",
                    error = uiState.formulario.nombreError,
                    leadingIcon = Icons.Default.Person,
                    modifier = Modifier.fillMaxWidth()
                )

                ValidatedTextField(
                    value = uiState.formulario.apellido,
                    onValueChange = viewModel::onApellidoChange,
                    label = "Apellido",
                    error = uiState.formulario.apellidoError,
                    leadingIcon = Icons.Default.Badge,
                    modifier = Modifier.fillMaxWidth()
                )

                ValidatedTextField(
                    value = uiState.formulario.dni,
                    onValueChange = viewModel::onDniChange,
                    label = "DNI",
                    error = uiState.formulario.dniError,
                    ayuda = "8 dígitos",
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.fillMaxWidth()
                )

                ValidatedTextField(
                    value = uiState.formulario.email,
                    onValueChange = viewModel::onEmailChange,
                    label = "Correo",
                    error = uiState.formulario.emailError,
                    leadingIcon = Icons.Default.Email,
                    keyboardType = KeyboardType.Email,
                    modifier = Modifier.fillMaxWidth()
                )

                ValidatedTextField(
                    value = uiState.formulario.telefono,
                    onValueChange = viewModel::onTelefonoChange,
                    label = "Teléfono",
                    error = uiState.formulario.telefonoError,
                    leadingIcon = Icons.Default.Phone,
                    ayuda = "Opcional, entre 6 y 9 dígitos",
                    keyboardType = KeyboardType.Phone,
                    modifier = Modifier.fillMaxWidth()
                )

                ValidatedTextField(
                    value = uiState.formulario.direccion,
                    onValueChange = viewModel::onDireccionChange,
                    label = "Dirección",
                    error = null,
                    leadingIcon = Icons.Default.LocationOn,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text = "Cliente activo",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Switch(
                        checked = uiState.formulario.estadoActivo,
                        onCheckedChange = viewModel::onEstadoChange
                    )
                }

                Button(
                    onClick = viewModel::guardar,
                    enabled = !uiState.guardando,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        when {
                            uiState.guardando -> "Guardando…"
                            clienteId == null -> "Registrar cliente"
                            else -> "Guardar cambios"
                        }
                    )
                }

                if (clienteId != null) {

                    OutlinedButton(
                        onClick = viewModel::eliminar,
                        enabled = !uiState.eliminando,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (uiState.eliminando) "Eliminando…" else "Eliminar cliente",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}