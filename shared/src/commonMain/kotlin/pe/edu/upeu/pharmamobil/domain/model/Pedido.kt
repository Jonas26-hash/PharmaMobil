package pe.edu.upeu.pharmamobil.domain.model

import pe.edu.upeu.pharmamobil.cliente.domain.model.Cliente

data class Pedido(
    val id: Long,
    val cliente: Cliente,
    val detalles: List<DetallePedido>,
    val estado: EstadoPedido
)