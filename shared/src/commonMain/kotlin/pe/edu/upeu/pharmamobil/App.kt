package pe.edu.upeu.pharmamobil

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import pe.edu.upeu.pharmamobil.cliente.presentation.ui.ClienteScreen
import pe.edu.upeu.pharmamobil.cliente.presentation.ui.RegistroClienteScreen
import pe.edu.upeu.pharmamobil.cliente.presentation.viewmodel.ClienteViewModel
import pe.edu.upeu.pharmamobil.navigation.Screen
import pe.edu.upeu.pharmamobil.presentation.inicio.InicioScreen
import pe.edu.upeu.pharmamobil.presentation.pedidos.PedidosScreen
import pe.edu.upeu.pharmamobil.producto.presentation.ui.FarmaciaScreen
import pe.edu.upeu.pharmamobil.producto.presentation.ui.RegistroMedicamentoScreen
import pe.edu.upeu.pharmamobil.producto.presentation.viewmodel.ProductoViewModel
import pe.edu.upeu.pharmamobil.theme.PharmaMobilTheme

@Composable
fun App() {
    val productoViewModel = viewModel { ProductoViewModel() }
    val clienteViewModel = viewModel { ClienteViewModel() }

    var pantallaActual by remember {
        mutableStateOf<Screen>(Screen.Inicio)
    }

    var darkTheme by remember {
        mutableStateOf(false)
    }

    // Sub-navegación dentro de las secciones Productos y Clientes:
    // la lista y el formulario de registro son dos niveles de una misma sección.
    var mostrandoFormularioProducto by remember {
        mutableStateOf(false)
    }

    var mostrandoFormularioCliente by remember {
        mutableStateOf(false)
    }

    val drawerState = rememberDrawerState(
        initialValue = DrawerValue.Closed
    )

    val scope = rememberCoroutineScope()

    val enFormulario = (pantallaActual is Screen.Productos && mostrandoFormularioProducto) ||
        (pantallaActual is Screen.Clientes && mostrandoFormularioCliente)

    PharmaMobilTheme(
        darkTheme = darkTheme
    ) {

        ModalNavigationDrawer(
            drawerState = drawerState,

            drawerContent = {
                ModalDrawerSheet {

                    DrawerHeader()

                    NavigationDrawerItem(
                        label = {
                            Text("Inicio")
                        },
                        selected = pantallaActual is Screen.Inicio,
                        onClick = {
                            pantallaActual = Screen.Inicio
                            scope.launch {
                                drawerState.close()
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = "Inicio"
                            )
                        }
                    )

                    NavigationDrawerItem(
                        label = {
                            Text("Productos")
                        },
                        selected = pantallaActual is Screen.Productos,
                        onClick = {
                            pantallaActual = Screen.Productos
                            mostrandoFormularioProducto = false
                            scope.launch {
                                drawerState.close()
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Medication,
                                contentDescription = "Productos"
                            )
                        }
                    )

                    NavigationDrawerItem(
                        label = {
                            Text("Clientes")
                        },
                        selected = pantallaActual is Screen.Clientes,
                        onClick = {
                            pantallaActual = Screen.Clientes
                            mostrandoFormularioCliente = false
                            scope.launch {
                                drawerState.close()
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Clientes"
                            )
                        }
                    )

                    NavigationDrawerItem(
                        label = {
                            Text("Pedidos")
                        },
                        selected = pantallaActual is Screen.Pedidos,
                        onClick = {
                            pantallaActual = Screen.Pedidos
                            scope.launch {
                                drawerState.close()
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = "Pedidos"
                            )
                        }
                    )

                    Spacer(
                        modifier = Modifier.padding(8.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = 16.dp,
                                vertical = 12.dp
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {

                        Text(
                            text = "Modo oscuro"
                        )

                        Switch(
                            checked = darkTheme,
                            onCheckedChange = {
                                darkTheme = it
                            }
                        )
                    }
                }
            }
        ) {

            Scaffold(
                topBar = {

                    TopAppBar(
                        title = {
                            Text(
                                text = tituloPantalla(
                                    pantallaActual,
                                    mostrandoFormularioProducto,
                                    mostrandoFormularioCliente
                                )
                            )
                        },
                        navigationIcon = {
                            if (enFormulario) {

                                IconButton(
                                    onClick = {
                                        if (pantallaActual is Screen.Productos) {
                                            mostrandoFormularioProducto = false
                                        } else {
                                            mostrandoFormularioCliente = false
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Volver"
                                    )
                                }
                            } else {

                                IconButton(
                                    onClick = {
                                        scope.launch {
                                            drawerState.open()
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Menu,
                                        contentDescription = "Abrir menú"
                                    )
                                }
                            }
                        }
                    )
                }
            ) { paddingValues ->

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {

                    when (pantallaActual) {

                        Screen.Inicio -> {
                            InicioScreen()
                        }

                        Screen.Productos -> {
                            if (mostrandoFormularioProducto) {
                                RegistroMedicamentoScreen(
                                    viewModel = productoViewModel
                                )
                            } else {
                                FarmaciaScreen(
                                    viewModel = productoViewModel,
                                    onNavigateToRegistro = {
                                        mostrandoFormularioProducto = true
                                    },
                                    onNavigateToClientes = {
                                        pantallaActual = Screen.Clientes
                                        mostrandoFormularioCliente = false
                                    }
                                )
                            }
                        }

                        Screen.Clientes -> {
                            if (mostrandoFormularioCliente) {
                                RegistroClienteScreen(
                                    viewModel = clienteViewModel
                                )
                            } else {
                                ClienteScreen(
                                    viewModel = clienteViewModel,
                                    onNavigateToRegistro = {
                                        mostrandoFormularioCliente = true
                                    },
                                    onNavigateToListaMedicamentos = {
                                        pantallaActual = Screen.Productos
                                        mostrandoFormularioProducto = false
                                    }
                                )
                            }
                        }

                        Screen.Pedidos -> {
                            PedidosScreen()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DrawerHeader() {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {

        Text(
            text = "PharmaMobil",
            style = MaterialTheme.typography.headlineSmall
        )

        Text(
            text = "Gestión farmacéutica",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

private fun tituloPantalla(
    screen: Screen,
    mostrandoFormularioProducto: Boolean,
    mostrandoFormularioCliente: Boolean
): String {

    return when (screen) {

        Screen.Inicio ->
            "Inicio"

        Screen.Productos ->
            if (mostrandoFormularioProducto) {
                "Registrar Medicamento"
            } else {
                "Productos"
            }

        Screen.Clientes ->
            if (mostrandoFormularioCliente) {
                "Registrar Cliente"
            } else {
                "Clientes"
            }

        Screen.Pedidos ->
            "Pedidos"
    }
}