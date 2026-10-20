package pe.edu.upeu.pharmamobil.domain.usecase

private const val MINUTO = 60_000L
private const val HORA = 60L * MINUTO
private const val DIA = 24L * HORA

fun antiguedadDeDatos(ultimoInstante: Long?, ahora: Long): String {

    val instante = ultimoInstante ?: return "Nunca se ha sincronizado"

    val transcurrido = (ahora - instante).coerceAtLeast(0L)

    return when {
        transcurrido < MINUTO -> "Actualizado hace menos de un minuto"
        transcurrido < HORA -> hace(transcurrido / MINUTO, "minuto")
        transcurrido < DIA -> hace(transcurrido / HORA, "hora")
        else -> hace(transcurrido / DIA, "día")
    }
}

private fun hace(cantidad: Long, unidad: String): String {

    val numero = cantidad.coerceAtLeast(1L)
    val forma = if (numero == 1L) unidad else unidad + "s"

    return "Actualizado hace $numero $forma"
}
