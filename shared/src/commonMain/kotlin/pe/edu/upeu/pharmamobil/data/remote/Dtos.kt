package pe.edu.upeu.pharmamobil.data.remote

import kotlinx.serialization.Serializable

// ---------------------------------------------------------------------------
// DTO del contrato REST de pharmaSoft (Spring Boot 4, endpoints en /api/v1).
// El dominio nunca ve estos tipos: los repositorios remotos los traducen.
// ---------------------------------------------------------------------------

@Serializable
data class ProductoRequestDto(
    val nombre: String,
    val precio: Double,
    val stock: Int,
    val estado: Boolean,
    val categoriaId: Long
)

@Serializable
data class ProductoResponseDto(
    val id: Long,
    val nombre: String,
    val precio: Double,
    val stock: Int,
    val estado: Boolean = true,
    val categoriaId: Long? = null,
    val categoriaNombre: String? = null,
    val fechaCreacion: String? = null,
    val fechaModificacion: String? = null
)

@Serializable
data class ClienteRequestDto(
    val dni: String,
    val nombres: String,
    val apellidos: String,
    val email: String? = null,
    val telefono: String? = null,
    val direccion: String? = null,
    val estado: Boolean = true
)

@Serializable
data class ClienteResponseDto(
    val id: Long,
    val dni: String,
    val nombres: String,
    val apellidos: String,
    val email: String? = null,
    val telefono: String? = null,
    val direccion: String? = null,
    val estado: Boolean = true,
    val fechaCreacion: String? = null,
    val fechaModificacion: String? = null
)

@Serializable
data class CategoriaResponseDto(
    val id: Long,
    val nombre: String,
    val descripcion: String? = null,
    val activo: Boolean = true
)

/** Envoltura de paginación de Spring Data: tipo de contenido dinámico. */
@Serializable
data class PaginaResponseDto<T>(
    val contenido: List<T> = emptyList(),
    val pagina: Int = 0,
    val tamanio: Int = 0,
    val totalElementos: Long = 0,
    val totalPaginas: Int = 0,
    val ultima: Boolean = true
)

@Serializable
data class VentaRequestDto(
    val clienteId: Long,
    val detalles: List<DetalleVentaRequestDto>
)

@Serializable
data class DetalleVentaRequestDto(
    val productoId: Long,
    val cantidad: Int
)

@Serializable
data class VentaResponseDto(
    val id: Long? = null,
    val fecha: String? = null,
    val clienteId: Long = 0,
    val clienteNombre: String? = null,
    val estado: String? = null,
    val total: Double = 0.0,
    val detalles: List<DetalleVentaResponseDto> = emptyList()
)

@Serializable
data class DetalleVentaResponseDto(
    val productoId: Long? = null,
    val productoNombre: String? = null,
    val cantidad: Int = 0,
    val precio: Double = 0.0,
    val subtotal: Double = 0.0
)