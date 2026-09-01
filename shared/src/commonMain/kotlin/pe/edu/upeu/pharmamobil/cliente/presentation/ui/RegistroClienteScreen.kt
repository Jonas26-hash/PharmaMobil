package pe.edu.upeu.pharmamobil.cliente.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import pe.edu.upeu.pharmamobil.cliente.presentation.ClienteValidator
import pe.edu.upeu.pharmamobil.cliente.presentation.viewmodel.ClienteViewModel
import pe.edu.upeu.pharmamobil.presentation.components.ValidatedTextField
import pe.edu.upeu.pharmamobil.presentation.model.UiState

@Composable
fun RegistroClienteScreen(
    viewModel: ClienteViewModel
) {
    val registroState by viewModel.registroClienteState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var nombre by remember { mutableStateOf("") }
    var apellido by remember { mutableStateOf("") }
    var dni by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }

    var nombreError by remember { mutableStateOf<String?>(null) }
    var apellidoError by remember { mutableStateOf<String?>(null) }
    var dniError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(registroState) {
        val state = registroState
        if (state is UiState.Success) {
            snackbarHostState.showSnackbar(
                message = "Cliente registrado: ${state.data.nombreCompleto}"
            )
            nombre = ""
            apellido = ""
            dni = ""
            telefono = ""
            email = ""
            direccion = ""
            viewModel.limpiarEstadoRegistroCliente()
        } else if (state is UiState.Error) {
            snackbarHostState.showSnackbar(message = state.message)
            viewModel.limpiarEstadoRegistroCliente()
        }
    }

    fun validar(): Boolean {
        nombreError = ClienteValidator.validarNombre(nombre)
        apellidoError = ClienteValidator.validarApellido(apellido)
        dniError = ClienteValidator.validarDni(dni)

        return nombreError == null && apellidoError == null && dniError == null
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Complete los datos del cliente",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(4.dp))

            ValidatedTextField(
                value = nombre,
                onValueChange = {
                    nombre = it
                    nombreError = null
                },
                label = "Nombre *",
                placeholder = "Ej: María",
                error = nombreError
            )

            ValidatedTextField(
                value = apellido,
                onValueChange = {
                    apellido = it
                    apellidoError = null
                },
                label = "Apellido *",
                placeholder = "Ej: García López",
                error = apellidoError
            )

            ValidatedTextField(
                value = dni,
                onValueChange = {
                    dni = it
                    dniError = null
                },
                label = "DNI *",
                placeholder = "8 dígitos",
                error = dniError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            ValidatedTextField(
                value = telefono,
                onValueChange = { telefono = it },
                label = "Teléfono",
                placeholder = "Ej: 987654321",
                error = null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                placeholder = { Text("Ej: cliente@email.com") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            OutlinedTextField(
                value = direccion,
                onValueChange = { direccion = it },
                label = { Text("Dirección") },
                placeholder = { Text("Ej: Av. Principal 123") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 1,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (validar()) {
                        viewModel.registrarCliente(
                            nombre = nombre,
                            apellido = apellido,
                            dni = dni,
                            telefono = telefono.ifBlank { null },
                            email = email.ifBlank { null },
                            direccion = direccion.ifBlank { null }
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = registroState !is UiState.Loading
            ) {
                if (registroState is UiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Registrar Cliente")
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}