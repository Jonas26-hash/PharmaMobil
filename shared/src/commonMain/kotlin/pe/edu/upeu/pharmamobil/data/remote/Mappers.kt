package pe.edu.upeu.pharmamobil.data.remote

import pe.edu.upeu.pharmamobil.domain.model.Categoria
import pe.edu.upeu.pharmamobil.domain.model.Cliente
import pe.edu.upeu.pharmamobil.domain.model.DetalleVenta
import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.model.Venta

// ---------------------------------------------------------------------------
// Traducción entre el contrato REST (DTO) y los modelos del dominio.
// ---------------------------------------------------------------------------

fun ProductoResponseDto.aDominio(): Producto = Producto(
    id = id,
    nombre = nombre,
    precio = precio,
    stock = stock,
    categoriaId = categoriaId,
    categoria = categoriaNombre.orEmpty(),
    activo = estado
)

fun Producto.aRemoto(): ProductoRequestDto = ProductoRequestDto(
    nombre = nombre,
    precio = precio,
    stock = stock,
    estado = activo,
    categoriaId = categoriaId ?: 0L
)

fun ClienteResponseDto.aDominio(): Cliente = Cliente(
    id = id,
    nombre = nombres,
    apellido = apellidos,
    dni = dni,
    telefono = telefono,
    email = email,
    direccion = direccion,
    activo = estado
)

fun Cliente.aRemoto(): ClienteRequestDto = ClienteRequestDto(
    dni = dni,
    nombres = nombre,
    apellidos = apellido,
    email = email,
    telefono = telefono,
    direccion = direccion,
    estado = activo
)

fun CategoriaResponseDto.aDominio(): Categoria = Categoria(
    id = id,
    nombre = nombre,
    descripcion = descripcion,
    activo = activo
)

fun VentaResponseDto.aDominio(): Venta = Venta(
    id = id?.toString() ?: "",
    fecha = fecha ?: "",
    items = detalles.map { it.aDominio() },
    total = total
)

fun DetalleVentaResponseDto.aDominio(): DetalleVenta = DetalleVenta(
    productoId = productoId ?: 0L,
    nombreProducto = productoNombre ?: "",
    cantidad = cantidad,
    precioUnitario = precio,
    subtotal = subtotal
)