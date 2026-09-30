package pe.edu.upeu.pharmamobil

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.koin.compose.KoinContext
import org.koin.compose.viewmodel.koinViewModel
import pe.edu.upeu.pharmamobil.navigation.Screen
import pe.edu.upeu.pharmamobil.presentation.cliente.ClienteFormScreen
import pe.edu.upeu.pharmamobil.presentation.cliente.ClienteFormViewModel
import pe.edu.upeu.pharmamobil.presentation.cliente.ClienteScreen
import pe.edu.upeu.pharmamobil.presentation.cliente.ClienteViewModel
import pe.edu.upeu.pharmamobil.presentation.components.EstadoVacio
import pe.edu.upeu.pharmamobil.presentation.inicio.InicioScreen
import pe.edu.upeu.pharmamobil.presentation.producto.ProductoFormScreen
import pe.edu.upeu.pharmamobil.presentation.producto.ProductoFormViewModel
import pe.edu.upeu.pharmamobil.presentation.producto.ProductoScreen
import pe.edu.upeu.pharmamobil.presentation.producto.ProductoViewModel
import pe.edu.upeu.pharmamobil.theme.PharmaMobilTheme

private data class Destino(
    val screen: Screen,
    val titulo: String,
    val icono: ImageVector
)

private val DESTINOS = listOf(
    Destino(Screen.Inicio, "Inicio", Icons.Default.Home),
    Destino(Screen.Productos, "Productos", Icons.Default.Medication),
    Destino(Screen.Clientes, "Clientes", Icons.Default.Person),
    Destino(Screen.Pedidos, "Pedidos", Icons.Default.ShoppingCart)
)

@Composable
fun App() = KoinContext {

    var pantallaActual by remember {
        mutableStateOf<Screen>(Screen.Inicio)
    }

    var darkTheme by rememberSaveable {
        mutableStateOf(false)
    }

    val drawerState = rememberDrawerState(
        initialValue = DrawerValue.Closed
    )

    val scope = rememberCoroutineScope()

    val esFormulario = pantallaActual is Screen.RegistrarProducto ||
        pantallaActual is Screen.RegistrarCliente

    PharmaMobilTheme(
        darkTheme = darkTheme
    ) {

        ModalNavigationDrawer(

            drawerState = drawerState,

            drawerContent = {

                ModalDrawerSheet {

                    DrawerHeader()

                    HorizontalDivider()

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    DESTINOS.forEach { destino ->

                        NavigationDrawerItem(
                            label = {
                                Text(destino.titulo)
                            },
                            selected = seccionDe(pantallaActual) == destino.screen,
                            onClick = {

                                pantallaActual = destino.screen

                                scope.launch {
                                    drawerState.close()
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = destino.icono,
                                    contentDescription = null
                                )
                            },
                            modifier = Modifier.padding(
                                NavigationDrawerItemDefaults.ItemPadding
                            )
                        )
                    }

                    Spacer(
                        modifier = Modifier.weight(1f)
                    )

                    HorizontalDivider()

                    ModoOscuro(
                        activo = darkTheme,
                        onCambiar = { darkTheme = it }
                    )
                }
            }
        ) {

            Scaffold(

                topBar = {

                    TopAppBar(

                        title = {
                            Text(
                                text = tituloDe(pantallaActual)
                            )
                        },

                        navigationIcon = {

                            IconButton(
                                onClick = {

                                    if (esFormulario) {
                                        pantallaActual = seccionDe(pantallaActual)
                                    } else {
                                        scope.launch {
                                            drawerState.open()
                                        }
                                    }
                                }
                            ) {

                                Icon(
                                    imageVector = if (esFormulario) {
                                        Icons.AutoMirrored.Filled.ArrowBack
                                    } else {
                                        Icons.Default.Menu
                                    },
                                    contentDescription = if (esFormulario) {
                                        "Volver"
                                    } else {
                                        "Abrir menú"
                                    }
                                )
                            }
                        },

                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }

            ) { paddingValues ->

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {

                    when (val pantalla = pantallaActual) {

                        Screen.Inicio ->
                            InicioScreen(
                                onNavegar = { destino -> pantallaActual = destino }
                            )

                        Screen.Productos ->
                            ProductoScreen(
                                viewModel = koinViewModel<ProductoViewModel>(),
                                onRegistrarClick = {
                                    pantallaActual = Screen.RegistrarProducto()
                                },
                                onEditarClick = { productoId ->
                                    pantallaActual = Screen.RegistrarProducto(productoId)
                                }
                            )

                        Screen.Clientes ->
                            ClienteScreen(
                                viewModel = koinViewModel<ClienteViewModel>(),
                                onRegistrarClick = {
                                    pantallaActual = Screen.RegistrarCliente()
                                },
                                onEditarClick = { clienteId ->
                                    pantallaActual = Screen.RegistrarCliente(clienteId)
                                }
                            )

                        Screen.Pedidos ->
                            EstadoVacio(
                                icono = Icons.Default.ShoppingCart,
                                titulo = "Pedidos en construcción",
                                descripcion = "Este módulo llega en una próxima sesión del curso.",
                                modifier = Modifier.align(Alignment.Center)
                            )

                        is Screen.RegistrarProducto ->
                            ProductoFormScreen(
                                viewModel = koinViewModel<ProductoFormViewModel>(),
                                productoId = pantalla.productoId,
                                onGuardado = {
                                    pantallaActual = Screen.Productos
                                }
                            )

                        is Screen.RegistrarCliente ->
                            ClienteFormScreen(
                                viewModel = koinViewModel<ClienteFormViewModel>(),
                                clienteId = pantalla.clienteId,
                                onGuardado = {
                                    pantallaActual = Screen.Clientes
                                }
                            )
                    }
                }
            }
        }
    }
}

/** A qué sección del menú pertenece una pantalla (formularios incluidos). */
private fun seccionDe(screen: Screen): Screen = when (screen) {
    is Screen.RegistrarProducto -> Screen.Productos
    is Screen.RegistrarCliente -> Screen.Clientes
    else -> screen
}

@Composable
private fun DrawerHeader() {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ) {

            Icon(
                imageVector = Icons.Default.LocalPharmacy,
                contentDescription = null,
                modifier = Modifier
                    .padding(10.dp)
                    .size(28.dp)
            )
        }

        Column {

            Text(
                text = "PharmaMobil",
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = "Gestión farmacéutica",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ModoOscuro(
    activo: Boolean,
    onCambiar: (Boolean) -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 28.dp,
                vertical = 16.dp
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Icon(
            imageVector = Icons.Default.DarkMode,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = "Modo oscuro",
            modifier = Modifier.weight(1f)
        )

        Switch(
            checked = activo,
            onCheckedChange = onCambiar
        )
    }
}

private fun tituloDe(
    screen: Screen
): String {

    return when (screen) {
        is Screen.RegistrarProducto ->
            if (screen.productoId == null) "Registrar producto" else "Editar producto"

        is Screen.RegistrarCliente ->
            if (screen.clienteId == null) "Registrar cliente" else "Editar cliente"

        else -> DESTINOS.first { it.screen == screen }.titulo
    }
}