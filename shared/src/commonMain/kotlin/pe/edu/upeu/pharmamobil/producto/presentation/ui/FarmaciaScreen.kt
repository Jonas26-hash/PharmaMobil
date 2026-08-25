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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pe.edu.upeu.pharmamobil.formatearPrecio
import pe.edu.upeu.pharmamobil.presentation.model.UiState
import pe.edu.upeu.pharmamobil.producto.domain.model.Medicamento
import pe.edu.upeu.pharmamobil.producto.presentation.viewmodel.ProductoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmaciaScreen(
    viewModel: ProductoViewModel,
    onNavigateToRegistro: () -> Unit = {},
    onNavigateToClientes: () -> Unit = {}
) {
    val medicamentosState by viewModel.medicamentosState.collectAsState()
    val ventaState by viewModel.ventaState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(ventaState) {
        val currentVenta = ventaState
        if (currentVenta is UiState.Success) {
            val venta = currentVenta.data
            val nombreItem = venta.items.firstOrNull()?.nombreMedicamento ?: "Medicamento"
            snackbarHostState.showSnackbar(
                message = "Venta exitosa: $nombreItem - Total: $${formatearPrecio(venta.total)}"
            )
            viewModel.limpiarEstadoVenta()
        } else if (currentVenta is UiState.Error) {
            snackbarHostState.showSnackbar(
                message = currentVenta.message
            )
            viewModel.limpiarEstadoVenta()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "FARMACIA KMP",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    TextButton(onClick = onNavigateToClientes) {
                        Text("Clientes")
                    }
                    TextButton(onClick = onNavigateToRegistro) {
                        Text("+ Registrar")
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
        ) {
            Text(
                text = "Medicamentos disponibles",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            when (val state = medicamentosState) {
                is UiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Cargando medicamentos...")
                        }
                    }
                }

                is UiState.Success -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.data) { medicamento ->
                            MedicamentoCard(
                                medicamento = medicamento,
                                onVenderClick = {
                                    viewModel.venderMedicamento(medicamento.id)
                                }
                            )
                        }
                    }
                }

                is UiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Error",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = state.message,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { viewModel.cargarMedicamentos() }) {
                                Text("Reintentar")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MedicamentoCard(
    medicamento: Medicamento,
    onVenderClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = medicamento.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (medicamento.requiereReceta) {
                    Text(
                        text = "Requiere receta",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Precio: $${formatearPrecio(medicamento.precio)}",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Stock: ${medicamento.stock}",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Categoría: ${medicamento.categoria}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (medicamento.descripcion?.isNotBlank() == true) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = medicamento.descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onVenderClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = medicamento.stock > 0
            ) {
                Text(
                    text = if (medicamento.stock > 0) "Vender" else "Sin stock"
                )
            }
        }
    }
}
