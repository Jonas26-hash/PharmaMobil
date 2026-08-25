package pe.edu.upeu.pharmamobil

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import pe.edu.upeu.pharmamobil.cliente.presentation.ui.ClienteScreen
import pe.edu.upeu.pharmamobil.cliente.presentation.ui.RegistroClienteScreen
import pe.edu.upeu.pharmamobil.cliente.presentation.viewmodel.ClienteViewModel
import pe.edu.upeu.pharmamobil.producto.presentation.ui.FarmaciaScreen
import pe.edu.upeu.pharmamobil.producto.presentation.ui.RegistroMedicamentoScreen
import pe.edu.upeu.pharmamobil.producto.presentation.viewmodel.ProductoViewModel

enum class Pantalla {
    LISTA,
    REGISTRO,
    CLIENTES,
    REGISTRO_CLIENTE
}

@Composable
fun App() {
    val productoViewModel = viewModel { ProductoViewModel() }
    val clienteViewModel = viewModel { ClienteViewModel() }
    val pantallaActual = remember { mutableStateOf(Pantalla.LISTA) }

    MaterialTheme {
        when (pantallaActual.value) {
            Pantalla.LISTA -> FarmaciaScreen(
                viewModel = productoViewModel,
                onNavigateToRegistro = { pantallaActual.value = Pantalla.REGISTRO },
                onNavigateToClientes = { pantallaActual.value = Pantalla.CLIENTES }
            )
            Pantalla.REGISTRO -> RegistroMedicamentoScreen(
                viewModel = productoViewModel,
                onNavigateBack = { pantallaActual.value = Pantalla.LISTA }
            )
            Pantalla.CLIENTES -> ClienteScreen(
                viewModel = clienteViewModel,
                onNavigateToRegistro = { pantallaActual.value = Pantalla.REGISTRO_CLIENTE },
                onNavigateToListaMedicamentos = { pantallaActual.value = Pantalla.LISTA }
            )
            Pantalla.REGISTRO_CLIENTE -> RegistroClienteScreen(
                viewModel = clienteViewModel,
                onNavigateBack = { pantallaActual.value = Pantalla.CLIENTES }
            )
        }
    }
}
