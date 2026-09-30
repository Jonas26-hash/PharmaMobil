package pe.edu.upeu.pharmamobil.presentation.producto

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
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.collect
import pe.edu.upeu.pharmamobil.presentation.components.ValidatedTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductoFormScreen(
    viewModel: ProductoFormViewModel,
    productoId: Long?,
    onGuardado: () -> Unit,
    modifier: Modifier = Modifier
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(productoId) {
        viewModel.configurarProducto(productoId)
    }

    // Los mensajes se observan en tiempo de ejecución y no con el valor
    // capturado al componer: el ViewModel sobrevive a la navegación, así que un
    // mensaje de éxito viejo no debe devolver al listado al reabrir el
    // formulario. Guardar o eliminar terminan con mensajeExito y recién ahí se
    // vuelve al listado, que recarga el estado vigente del backend.
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

        if (uiState.cargandoProducto) {

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
                    text = if (productoId == null) "Registrar producto" else "Editar producto",
                    style = MaterialTheme.typography.headlineSmall
                )

                ValidatedTextField(
                    value = uiState.formulario.nombre,
                    onValueChange = viewModel::onNombreChange,
                    label = "Nombre",
                    error = uiState.formulario.nombreError,
                    leadingIcon = Icons.Default.Medication,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    ValidatedTextField(
                        value = uiState.formulario.precio,
                        onValueChange = viewModel::onPrecioChange,
                        label = "Precio",
                        error = uiState.formulario.precioError,
                        ayuda = "En soles",
                        keyboardType = KeyboardType.Decimal,
                        modifier = Modifier.weight(1f)
                    )

                    ValidatedTextField(
                        value = uiState.formulario.stock,
                        onValueChange = viewModel::onStockChange,
                        label = "Stock",
                        error = uiState.formulario.stockError,
                        ayuda = "Unidades",
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.weight(1f)
                    )
                }

                SelectorCategoria(
                    categorias = uiState.categorias,
                    seleccionId = uiState.formulario.categoriaId,
                    error = uiState.formulario.categoriaError,
                    onSeleccionar = viewModel::onCategoriaChange
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text = "Producto activo",
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
                            productoId == null -> "Registrar producto"
                            else -> "Guardar cambios"
                        }
                    )
                }

                if (productoId != null) {

                    OutlinedButton(
                        onClick = viewModel::eliminar,
                        enabled = !uiState.eliminando,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (uiState.eliminando) "Eliminando…" else "Eliminar producto",
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectorCategoria(
    categorias: List<CategoriaUi>,
    seleccionId: Long?,
    error: String?,
    onSeleccionar: (Long) -> Unit
) {

    val seleccionada = categorias.firstOrNull { it.id == seleccionId }
    var expandido by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expandido,
        onExpandedChange = { expandido = it }
    ) {

        OutlinedTextField(
            value = seleccionada?.nombre ?: "",
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            label = { Text("Categoría") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido)
            },
            isError = error != null,
            supportingText = {
                Text(error ?: "Selecciona la categoría del catálogo")
            },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expandido,
            onDismissRequest = { expandido = false }
        ) {

            categorias.forEach { categoria ->
                DropdownMenuItem(
                    text = { Text(categoria.nombre) },
                    onClick = {
                        onSeleccionar(categoria.id)
                        expandido = false
                    }
                )
            }

            if (categorias.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("No hay categorías disponibles") },
                    onClick = { expandido = false }
                )
            }
        }
    }
}