package pe.edu.upeu.pharmamobil

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.PermanentNavigationDrawer
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
import androidx.compose.ui.graphics.vector.ImageVector
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

private enum class TipoNavegacion {
    COMPACTA,
    MEDIANA,
    AMPLIA
}

private data class Destino(
    val pantalla: Screen,
    val titulo: String,
    val icono: ImageVector
)

private val destinos = listOf(
    Destino(Screen.Inicio, "Inicio", Icons.Default.Home),
    Destino(Screen.Productos, "Productos", Icons.Default.Medication),
    Destino(Screen.Clientes, "Clientes", Icons.Default.Person),
    Destino(Screen.Pedidos, "Pedidos", Icons.Default.ShoppingCart)
)

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

    PharmaMobilTheme(
        darkTheme = darkTheme
    ) {

        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {

            val tipo = when {
                maxWidth < 600.dp -> TipoNavegacion.COMPACTA
                maxWidth < 840.dp -> TipoNavegacion.MEDIANA
                else -> TipoNavegacion.AMPLIA
            }

            val onSeleccionar: (Screen) -> Unit = { pantalla ->
                pantallaActual = pantalla
                mostrandoFormularioProducto = false
                mostrandoFormularioCliente = false
                scope.launch {
                    drawerState.close()
                }
            }

            when (tipo) {

                TipoNavegacion.COMPACTA -> {
                    ModalNavigationDrawer(
                        drawerState = drawerState,
                        drawerContent = {
                            ModalDrawerSheet {
                                MenuNavegacion(
                                    pantallaActual = pantallaActual,
                                    darkTheme = darkTheme,
                                    onSeleccionar = onSeleccionar,
                                    onCambiarTema = { darkTheme = it }
                                )
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        EstructuraPrincipal(
                            pantallaActual = pantallaActual,
                            mostrandoFormularioProducto = mostrandoFormularioProducto,
                            mostrandoFormularioCliente = mostrandoFormularioCliente,
                            mostrarBotonMenu = true,
                            onAbrirMenu = {
                                scope.launch {
                                    drawerState.open()
                                }
                            },
                            onVolverFormulario = {
                                if (pantallaActual is Screen.Productos) {
                                    mostrandoFormularioProducto = false
                                } else {
                                    mostrandoFormularioCliente = false
                                }
                            },
                            onRegistrarProducto = { mostrandoFormularioProducto = true },
                            onRegistrarCliente = { mostrandoFormularioCliente = true },
                            onIrAProductos = {
                                pantallaActual = Screen.Productos
                                mostrandoFormularioProducto = false
                            },
                            onIrAClientes = {
                                pantallaActual = Screen.Clientes
                                mostrandoFormularioCliente = false
                            },
                            productoViewModel = productoViewModel,
                            clienteViewModel = clienteViewModel
                        )
                    }
                }

                TipoNavegacion.MEDIANA -> {
                    Row(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        RailNavegacion(
                            pantallaActual = pantallaActual,
                            darkTheme = darkTheme,
                            onSeleccionar = onSeleccionar,
                            onCambiarTema = { darkTheme = it }
                        )

                        EstructuraPrincipal(
                            pantallaActual = pantallaActual,
                            mostrandoFormularioProducto = mostrandoFormularioProducto,
                            mostrandoFormularioCliente = mostrandoFormularioCliente,
                            mostrarBotonMenu = false,
                            onAbrirMenu = {},
                            onVolverFormulario = {
                                if (pantallaActual is Screen.Productos) {
                                    mostrandoFormularioProducto = false
                                } else {
                                    mostrandoFormularioCliente = false
                                }
                            },
                            onRegistrarProducto = { mostrandoFormularioProducto = true },
                            onRegistrarCliente = { mostrandoFormularioCliente = true },
                            onIrAProductos = {
                                pantallaActual = Screen.Productos
                                mostrandoFormularioProducto = false
                            },
                            onIrAClientes = {
                                pantallaActual = Screen.Clientes
                                mostrandoFormularioCliente = false
                            },
                            productoViewModel = productoViewModel,
                            clienteViewModel = clienteViewModel,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                TipoNavegacion.AMPLIA -> {
                    PermanentNavigationDrawer(
                        drawerContent = {
                            PermanentDrawerSheet {
                                MenuNavegacion(
                                    pantallaActual = pantallaActual,
                                    darkTheme = darkTheme,
                                    onSeleccionar = onSeleccionar,
                                    onCambiarTema = { darkTheme = it }
                                )
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        EstructuraPrincipal(
                            pantallaActual = pantallaActual,
                            mostrandoFormularioProducto = mostrandoFormularioProducto,
                            mostrandoFormularioCliente = mostrandoFormularioCliente,
                            mostrarBotonMenu = false,
                            onAbrirMenu = {},
                            onVolverFormulario = {
                                if (pantallaActual is Screen.Productos) {
                                    mostrandoFormularioProducto = false
                                } else {
                                    mostrandoFormularioCliente = false
                                }
                            },
                            onRegistrarProducto = { mostrandoFormularioProducto = true },
                            onRegistrarCliente = { mostrandoFormularioCliente = true },
                            onIrAProductos = {
                                pantallaActual = Screen.Productos
                                mostrandoFormularioProducto = false
                            },
                            onIrAClientes = {
                                pantallaActual = Screen.Clientes
                                mostrandoFormularioCliente = false
                            },
                            productoViewModel = productoViewModel,
                            clienteViewModel = clienteViewModel
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MenuNavegacion(
    pantallaActual: Screen,
    darkTheme: Boolean,
    onSeleccionar: (Screen) -> Unit,
    onCambiarTema: (Boolean) -> Unit
) {

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {

        DrawerHeader()

        destinos.forEach { destino ->
            NavigationDrawerItem(
                label = {
                    Text(destino.titulo)
                },
                selected = pantallaActual == destino.pantalla,
                onClick = {
                    onSeleccionar(destino.pantalla)
                },
                icon = {
                    Icon(
                        imageVector = destino.icono,
                        contentDescription = destino.titulo
                    )
                }
            )
        }

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
                onCheckedChange = onCambiarTema
            )
        }
    }
}

@Composable
private fun RailNavegacion(
    pantallaActual: Screen,
    darkTheme: Boolean,
    onSeleccionar: (Screen) -> Unit,
    onCambiarTema: (Boolean) -> Unit
) {

    NavigationRail {

        destinos.forEach { destino ->
            NavigationRailItem(
                selected = pantallaActual == destino.pantalla,
                onClick = {
                    onSeleccionar(destino.pantalla)
                },
                icon = {
                    Icon(
                        imageVector = destino.icono,
                        contentDescription = destino.titulo
                    )
                },
                label = {
                    Text(destino.titulo)
                }
            )
        }

        Spacer(
            modifier = Modifier
                .padding(8.dp)
                .weight(1f)
        )

        Switch(
            checked = darkTheme,
            onCheckedChange = onCambiarTema,
            modifier = Modifier.padding(vertical = 12.dp)
        )
    }
}

@Composable
private fun EstructuraPrincipal(
    pantallaActual: Screen,
    mostrandoFormularioProducto: Boolean,
    mostrandoFormularioCliente: Boolean,
    mostrarBotonMenu: Boolean,
    onAbrirMenu: () -> Unit,
    onVolverFormulario: () -> Unit,
    onRegistrarProducto: () -> Unit,
    onRegistrarCliente: () -> Unit,
    onIrAProductos: () -> Unit,
    onIrAClientes: () -> Unit,
    productoViewModel: ProductoViewModel,
    clienteViewModel: ClienteViewModel,
    modifier: Modifier = Modifier
) {

    val enFormulario = (pantallaActual is Screen.Productos && mostrandoFormularioProducto) ||
        (pantallaActual is Screen.Clientes && mostrandoFormularioCliente)

    Scaffold(
        modifier = modifier,
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
                            onClick = onVolverFormulario
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver"
                            )
                        }
                    } else if (mostrarBotonMenu) {

                        IconButton(
                            onClick = onAbrirMenu
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
                            onNavigateToRegistro = onRegistrarProducto,
                            onNavigateToClientes = onIrAClientes
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
                            onNavigateToRegistro = onRegistrarCliente,
                            onNavigateToListaMedicamentos = onIrAProductos
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