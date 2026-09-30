package pe.edu.upeu.pharmamobil.navigation

sealed class Screen {
    data object Inicio : Screen()

    data object Productos : Screen()

    data object Clientes : Screen()

    data object Pedidos : Screen()

    /**
     * Formulario de producto: con id nulo registra; con id presente edita ese
     * producto del inventario.
     */
    data class RegistrarProducto(val productoId: Long? = null) : Screen()

    /**
     * Formulario de cliente: con id nulo registra; con id presente edita ese
     * cliente de la cartera.
     */
    data class RegistrarCliente(val clienteId: Long? = null) : Screen()
}