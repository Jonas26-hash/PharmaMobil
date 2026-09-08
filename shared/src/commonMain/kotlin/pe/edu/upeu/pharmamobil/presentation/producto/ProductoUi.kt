package pe.edu.upeu.pharmamobil.presentation.producto

import kotlin.math.roundToLong
import pe.edu.upeu.pharmamobil.domain.model.Producto

data class ProductoUi(
    val id: Long,
    val nombre: String,
    val descripcion: String?,
    val categoria: String,
    val precio: String,
    val stock: String,
    val stockUnidades: Int,
    val requiereReceta: Boolean,
    val activo: Boolean,
    val requiereReposicion: Boolean
)

fun Producto.aUi(): ProductoUi = ProductoUi(
    id = id,
    nombre = nombre,
    descripcion = descripcion,
    categoria = categoria,
    precio = precio.enSoles(),
    stock = "$stock u.",
    stockUnidades = stock,
    requiereReceta = requiereReceta,
    activo = activo,
    requiereReposicion = requiereReposicion
)

/** Kotlin común no trae String.format, así que armamos los dos decimales a mano. */
internal fun Double.enSoles(): String {

    val centavos = (this * 100).roundToLong()

    val enteros = centavos / 100
    val decimales = (centavos % 100).toString().padStart(2, '0')

    return "S/ $enteros.$decimales"
}