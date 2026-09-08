package pe.edu.upeu.pharmamobil.presentation.producto

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import pe.edu.upeu.pharmamobil.presentation.components.EstadoVacio
import pe.edu.upeu.pharmamobil.presentation.components.MensajeExito
import pe.edu.upeu.pharmamobil.presentation.components.ValidatedTextField

@Composable
fun ProductoScreen(
    viewModel: ProductoViewModel,
    modifier: Modifier = Modifier
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var tabSeleccionada by remember { mutableStateOf(TabInventario.ACTIVOS) }

    LaunchedEffect(uiState.mensaje) {
        val mensaje = uiState.mensaje
        if (mensaje != null) {
            snackbarHostState.showSnackbar(message = mensaje)
            viewModel.consumirMensaje()
        }
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            FormularioProductoCard(
                formulario = uiState.formulario,
                registrando = uiState.registrando,
                onNombreChange = viewModel::onNombreChange,
                onDescripcionChange = viewModel::onDescripcionChange,
                onPrecioChange = viewModel::onPrecioChange,
                onStockChange = viewModel::onStockChange,
                onCategoriaChange = viewModel::onCategoriaChange,
                onRequiereRecetaChange = viewModel::onRequiereRecetaChange,
                onRegistrar = viewModel::registrar
            )

            uiState.mensajeExito?.let {
                MensajeExito(it)
            }

            EncabezadoInventario(uiState.fase)

            if (uiState.fase is ProductoUiState.Fase.ConProductos) {
                SelectorTabInventario(
                    seleccion = tabSeleccionada,
                    onSeleccionar = { tabSeleccionada = it }
                )
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
                            descripcion = "Registra el primero con el formulario de arriba.",
                            modifier = Modifier.align(Alignment.Center)
                        )

                    is ProductoUiState.Fase.ConProductos -> {
                        val filtrados = tabSeleccionada.filtrar(fase.productos)

                        if (filtrados.isEmpty()) {
                            EstadoVacio(
                                icono = Icons.Default.Inventory2,
                                titulo = "Nada en \"${tabSeleccionada.titulo}\"",
                                descripcion = "Cambia de filtro o registra un producto nuevo.",
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
                                        onVenderClick = { viewModel.vender(producto.id) }
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

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun FormularioProductoCard(
    formulario: FormularioProducto,
    registrando: Boolean,
    onNombreChange: (String) -> Unit,
    onDescripcionChange: (String) -> Unit,
    onPrecioChange: (String) -> Unit,
    onStockChange: (String) -> Unit,
    onCategoriaChange: (String) -> Unit,
    onRequiereRecetaChange: (Boolean) -> Unit,
    onRegistrar: () -> Unit
) {

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Text(
                text = "Registrar producto",
                style = MaterialTheme.typography.titleMedium
            )

            ValidatedTextField(
                value = formulario.nombre,
                onValueChange = onNombreChange,
                label = "Nombre",
                error = formulario.nombreError,
                leadingIcon = Icons.Default.Medication,
                modifier = Modifier.fillMaxWidth()
            )

            ValidatedTextField(
                value = formulario.descripcion,
                onValueChange = onDescripcionChange,
                label = "Descripción",
                error = null,
                leadingIcon = Icons.Default.Description,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                ValidatedTextField(
                    value = formulario.precio,
                    onValueChange = onPrecioChange,
                    label = "Precio",
                    error = formulario.precioError,
                    ayuda = "En soles",
                    keyboardType = KeyboardType.Decimal,
                    modifier = Modifier.weight(1f)
                )

                ValidatedTextField(
                    value = formulario.stock,
                    onValueChange = onStockChange,
                    label = "Stock",
                    error = formulario.stockError,
                    ayuda = "Unidades",
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f)
                )
            }

            ValidatedTextField(
                value = formulario.categoria,
                onValueChange = onCategoriaChange,
                label = "Categoría",
                error = null,
                ayuda = "Ej. Analgésico",
                modifier = Modifier.fillMaxWidth()
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
                    checked = formulario.requiereReceta,
                    onCheckedChange = onRequiereRecetaChange
                )
            }

            Button(
                onClick = onRegistrar,
                enabled = !registrando,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (registrando) "Registrando…" else "Registrar")
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
private fun SelectorTabInventario(
    seleccion: TabInventario,
    onSeleccionar: (TabInventario) -> Unit
) {

    PrimaryScrollableTabRow(
        selectedTabIndex = TabInventario.entries.indexOf(seleccion),
        modifier = Modifier.fillMaxWidth()
    ) {

        TabInventario.entries.forEach { tab ->
            Tab(
                selected = seleccion == tab,
                onClick = { onSeleccionar(tab) },
                text = { Text(tab.titulo) }
            )
        }
    }
}

@Composable
private fun ProductoItem(
    producto: ProductoUi,
    onVenderClick: () -> Unit
) {

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
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
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        text = producto.nombre,
                        style = MaterialTheme.typography.titleSmall
                    )

                    Text(
                        text = "${producto.precio}  ·  ${producto.stock}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

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
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                if (producto.requiereReceta) {
                    EtiquetaProducto(
                        texto = "Requiere receta",
                        colorTexto = MaterialTheme.colorScheme.error
                    )
                }

                if (!producto.activo) {
                    EtiquetaProducto(
                        texto = "Inactivo",
                        colorTexto = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (producto.categoria.isNotBlank()) {
                    EtiquetaProducto(
                        texto = producto.categoria,
                        colorTexto = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (!producto.descripcion.isNullOrBlank()) {
                Text(
                    text = producto.descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = onVenderClick,
                enabled = producto.stockUnidades > 0,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (producto.stockUnidades > 0) "Vender" else "Sin stock"
                )
            }
        }
    }
}

@Composable
private fun EtiquetaProducto(
    texto: String,
    colorTexto: androidx.compose.ui.graphics.Color
) {

    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = colorTexto
    ) {

        Text(
            text = texto,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(
                horizontal = 8.dp,
                vertical = 4.dp
            )
        )
    }
}