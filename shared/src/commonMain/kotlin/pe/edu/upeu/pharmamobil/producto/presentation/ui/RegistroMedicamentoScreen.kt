package pe.edu.upeu.pharmamobil.producto.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import pe.edu.upeu.pharmamobil.presentation.model.UiState
import pe.edu.upeu.pharmamobil.producto.presentation.viewmodel.ProductoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroMedicamentoScreen(
    viewModel: ProductoViewModel,
    onNavigateBack: () -> Unit
) {
    val registroState by viewModel.registroState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("") }
    var requiereReceta by remember { mutableStateOf(false) }
    var categoria by remember { mutableStateOf("") }

    var nombreError by remember { mutableStateOf<String?>(null) }
    var precioError by remember { mutableStateOf<String?>(null) }
    var stockError by remember { mutableStateOf<String?>(null) }
    var categoriaError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(registroState) {
        val state = registroState
        if (state is UiState.Success) {
            snackbarHostState.showSnackbar(
                message = "Medicamento registrado: ${state.data.nombre}"
            )
            nombre = ""
            descripcion = ""
            precio = ""
            stock = ""
            requiereReceta = false
            categoria = ""
            viewModel.limpiarEstadoRegistro()
        } else if (state is UiState.Error) {
            snackbarHostState.showSnackbar(message = state.message)
            viewModel.limpiarEstadoRegistro()
        }
    }

    fun validar(): Boolean {
        var valido = true

        nombreError = if (nombre.isBlank()) {
            valido = false
            "El nombre es obligatorio"
        } else {
            null
        }

        precioError = if (precio.isBlank()) {
            valido = false
            "El precio es obligatorio"
        } else {
            val valor = precio.toDoubleOrNull()
            if (valor == null || valor < 0) {
                valido = false
                "Ingrese un precio válido"
            } else {
                null
            }
        }

        stockError = if (stock.isBlank()) {
            valido = false
            "El stock es obligatorio"
        } else {
            val valor = stock.toIntOrNull()
            if (valor == null || valor < 0) {
                valido = false
                "Ingrese un stock válido"
            } else {
                null
            }
        }

        categoriaError = if (categoria.isBlank()) {
            valido = false
            "La categoría es obligatoria"
        } else {
            null
        }

        return valido
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Registrar Medicamento",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) {
                        Text("Volver")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Complete los datos del medicamento",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(4.dp))

            OutlinedTextField(
                value = nombre,
                onValueChange = {
                    nombre = it
                    nombreError = null
                },
                label = { Text("Nombre *") },
                placeholder = { Text("Ej: Paracetamol") },
                isError = nombreError != null,
                supportingText = {
                    nombreError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción") },
                placeholder = { Text("Descripción del medicamento") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4
            )

            OutlinedTextField(
                value = precio,
                onValueChange = {
                    precio = it
                    precioError = null
                },
                label = { Text("Precio *") },
                placeholder = { Text("Ej: 5.50") },
                isError = precioError != null,
                supportingText = {
                    precioError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            OutlinedTextField(
                value = stock,
                onValueChange = {
                    stock = it
                    stockError = null
                },
                label = { Text("Stock *") },
                placeholder = { Text("Ej: 20") },
                isError = stockError != null,
                supportingText = {
                    stockError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            OutlinedTextField(
                value = categoria,
                onValueChange = {
                    categoria = it
                    categoriaError = null
                },
                label = { Text("Categoría *") },
                placeholder = { Text("Ej: Analgésico") },
                isError = categoriaError != null,
                supportingText = {
                    categoriaError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Requiere receta",
                    style = MaterialTheme.typography.bodyLarge
                )
                Switch(
                    checked = requiereReceta,
                    onCheckedChange = { requiereReceta = it }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (validar()) {
                        viewModel.registrarMedicamento(
                            nombre = nombre,
                            descripcion = descripcion.ifBlank { null },
                            precio = precio.toDouble(),
                            stock = stock.toInt(),
                            requiereReceta = requiereReceta,
                            categoria = categoria
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
                    Text("Registrar Medicamento")
                }
            }
        }
    }
}
