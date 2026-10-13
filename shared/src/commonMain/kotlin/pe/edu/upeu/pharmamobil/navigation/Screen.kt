package pe.edu.upeu.pharmamobil.navigation

sealed class Screen {
    data object Inicio : Screen()

    data object Productos : Screen()

    data object Clientes : Screen()

    data object Pedidos : Screen()

    data class RegistrarProducto(val productoId: Long? = null) : Screen()

    data class RegistrarCliente(val clienteId: Long? = null) : Screen()
}
