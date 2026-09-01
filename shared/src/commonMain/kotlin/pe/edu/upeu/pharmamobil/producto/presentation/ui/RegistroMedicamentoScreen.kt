package pe.edu.upeu.pharmamobil.producto.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
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
import pe.edu.upeu.pharmamobil.presentation.components.ValidatedTextField
import pe.edu.upeu.pharmamobil.presentation.model.UiState
import pe.edu.upeu.pharmamobil.producto.presentation.ProductoValidator
import pe.edu.upeu.pharmamobil.producto.presentation.viewmodel.ProductoViewModel

@Composable
fun RegistroMedicamentoScreen(
    viewModel: ProductoViewModel
) {
    val registroState by viewModel.registroState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("") }
    var requiereReceta by remember { mutableStateOf(false) }
    var categoria by remember { mutableStateOf("") }

    var intentoRegistro by remember { mutableStateOf(false) }

    var nombreError by remember { mutableStateOf<String?>(null) }
    var precioError by remember { mutableStateOf<String?>(null) }
    var stockError by remember { mutableStateOf<String?>(null) }

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
            intentoRegistro = false
            nombreError = null
            precioError = null
            stockError = null
            viewModel.limpiarEstadoRegistro()
        } else if (state is UiState.Error) {
            snackbarHostState.showSnackbar(message = state.message)
            viewModel.limpiarEstadoRegistro()
        }
    }

    fun validar(): Pair<Double, Int>? {
        // 1. Validar nombre (vacío o solo espacios)
        nombreError = ProductoValidator.validarNombre(nombre)
        // 2/3. Convertir precio de forma segura y validar que sea > 0
        precioError = ProductoValidator.validarPrecio(precio)
        // 4/5. Convertir stock de forma segura y validar que sea >= 0 (0 es válido)
        stockError = ProductoValidator.validarStock(stock)

        if (nombreError != null || precioError != null || stockError != null) return null

        // 6. Solo tras superar todas las validaciones se devuelven los valores convertidos
        return (precio.toDoubleOrNull() ?: 0.0) to (stock.toIntOrNull() ?: 0)
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
                text = "Complete los datos del medicamento",
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
                placeholder = "Ej: Paracetamol",
                error = if (intentoRegistro) nombreError else null
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

            ValidatedTextField(
                value = precio,
                onValueChange = {
                    precio = it
                    precioError = null
                },
                label = "Precio *",
                placeholder = "Ej: 5.50",
                error = if (intentoRegistro) precioError else null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            ValidatedTextField(
                value = stock,
                onValueChange = {
                    stock = it
                    stockError = null
                },
                label = "Stock *",
                placeholder = "Ej: 20",
                error = if (intentoRegistro) stockError else null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            ValidatedTextField(
                value = categoria,
                onValueChange = { categoria = it },
                label = "Categoría",
                placeholder = "Ej: Analgésico",
                error = null
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
                    intentoRegistro = true
                    val valores = validar()
                    if (valores != null) {
                        viewModel.registrarMedicamento(
                            nombre = nombre.trim(),
                            descripcion = descripcion.ifBlank { null },
                            precio = valores.first,
                            stock = valores.second,
                            requiereReceta = requiereReceta,
                            categoria = categoria.trim()
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

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}