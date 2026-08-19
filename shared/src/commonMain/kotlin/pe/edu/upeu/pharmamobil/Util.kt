package pe.edu.upeu.pharmamobil

fun formatearPrecio(valor: Double): String {
    val entero = valor.toInt()
    val decimal = ((valor - entero) * 100).toInt()
    return "$entero.${if (decimal < 10) "0$decimal" else "$decimal"}"
}
