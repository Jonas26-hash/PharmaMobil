package pe.edu.upeu.pharmamobil.presentation.producto

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.edu.upeu.pharmamobil.presentation.components.EstadoVacio

@Composable
fun ProductoScreen(
    viewModel: ProductoViewModel,
    onRegistrarClick: () -> Unit,
    onEditarClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Al volver desde el formulario la composición se re-crea y el servidor
    // devuelve el dato recién guardado.
    LaunchedEffect(Unit) {
        viewModel.cargarProductos()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        EncabezadoInventario(uiState.fase)

        OutlinedTextField(
            value = uiState.busqueda,
            onValueChange = viewModel::onBusquedaChange,
            label = { Text("Buscar producto") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null
                )
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = onRegistrarClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null
            )
            Text("Registrar producto")
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {

            when (val fase = uiState.fase) {

                ProductoUiState.Fase.Cargando ->
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        CircularProgressIndicator()

                        Text(
                            text = "Cargando inventario…",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                ProductoUiState.Fase.SinProductos ->
                    EstadoVacio(
                        icono = Icons.Default.Inventory2,
                        titulo = "Todavía no hay productos",
                        descripcion = "Pulsa \"Registrar producto\" para dar de alta el primero.",
                        modifier = Modifier.align(Alignment.Center)
                    )

                is ProductoUiState.Fase.ConProductos -> {
                    val filtrados = uiState.filtrarPorBusqueda()

                    if (filtrados.isEmpty()) {
                        EstadoVacio(
                            icono = Icons.Default.Search,
                            titulo = "Sin resultados",
                            descripcion = "Ningún producto coincide con la búsqueda.",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(
                                items = filtrados,
                                key = { it.id }
                            ) { producto ->
                                ProductoItem(
                                    producto = producto,
                                    onClick = { onEditarClick(producto.id) }
                                )
                            }
                        }
                    }
                }

                is ProductoUiState.Fase.Error ->
                    EstadoVacio(
                        icono = Icons.Default.CloudOff,
                        titulo = "No pudimos cargar el inventario",
                        descripcion = fase.mensaje,
                        colorIcono = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center),
                        accion = {
                            FilledTonalButton(onClick = viewModel::cargarProductos) {
                                Text("Reintentar")
                            }
                        }
                    )
            }
        }
    }
}

@Composable
private fun EncabezadoInventario(
    fase: ProductoUiState.Fase
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = "Inventario",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f)
        )

        if (fase is ProductoUiState.Fase.ConProductos) {

            val cantidad = fase.productos.size

            Text(
                text = if (cantidad == 1) "1 producto" else "$cantidad productos",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ProductoItem(
    producto: ProductoUi,
    onClick: () -> Unit
) {

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {

        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            ) {

                Icon(
                    imageVector = Icons.Default.Medication,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(8.dp)
                        .size(20.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {

                Text(
                    text = producto.nombre,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "${producto.precio}  ·  ${producto.stock}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    if (producto.requiereReposicion) {

                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        ) {

                            Text(
                                text = "Reponer",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(
                                    horizontal = 8.dp,
                                    vertical = 4.dp
                                )
                            )
                        }
                    }

                    if (!producto.activo) {

                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ) {

                            Text(
                                text = "Inactivo",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(
                                    horizontal = 8.dp,
                                    vertical = 4.dp
                                )
                            )
                        }
                    }

                    if (producto.categoria.isNotBlank()) {

                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ) {

                            Text(
                                text = producto.categoria,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(
                                    horizontal = 8.dp,
                                    vertical = 4.dp
                                )
                            )
                        }
                    }
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Editar ${producto.nombre}",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}