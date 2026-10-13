package pe.edu.upeu.pharmamobil.presentation.producto

import kotlin.math.roundToLong
import pe.edu.upeu.pharmamobil.domain.model.Categoria
import pe.edu.upeu.pharmamobil.domain.model.Producto

data class ProductoUi(
    val id: Long,
    val nombre: String,
    val descripcion: String?,
    val categoria: String,
    val categoriaId: Long? = null,
    val precio: String,
    val precioValor: Double,
    val stock: String,
    val stockUnidades: Int,
    val requiereReceta: Boolean,
    val activo: Boolean,
    val requiereReposicion: Boolean
)

data class CategoriaUi(
    val id: Long,
    val nombre: String
)

fun Producto.aUi(): ProductoUi = ProductoUi(
    id = id,
    nombre = nombre,
    descripcion = descripcion,
    categoria = categoria,
    categoriaId = categoriaId,
    precio = precio.enSoles(),
    precioValor = precio,
    stock = "$stock u.",
    stockUnidades = stock,
    requiereReceta = requiereReceta,
    activo = activo,
    requiereReposicion = requiereReposicion
)

fun Categoria.aUi(): CategoriaUi = CategoriaUi(
    id = id,
    nombre = nombre
)

internal fun Double.enSoles(): String {

    val centavos = (this * 100).roundToLong()

    val enteros = centavos / 100
    val decimales = (centavos % 100).toString().padStart(2, '0')

    return "S/ $enteros.$decimales"
}

internal fun Double.aTextoEditorial(): String {
    val texto = toString()
    return when {
        texto.endsWith(".0") -> texto.dropLast(2)
        else -> texto
    }
}
