package pe.edu.upeu.pharmamobil.domain.usecase

import kotlin.test.Test
import kotlin.test.assertEquals

class AntiguedadDeDatosTest {

    private val ahora = 1_700_000_000_000L

    private fun hace(millis: Long): Long = ahora - millis

    @Test
    fun sinSincronizacionPreviaLoIndicaEnLenguajeNatural() {
        assertEquals("Nunca se ha sincronizado", antiguedadDeDatos(null, ahora))
    }

    @Test
    fun menosDeUnMinutoSeExpresaComoTal() {
        assertEquals(
            "Actualizado hace menos de un minuto",
            antiguedadDeDatos(hace(30_000L), ahora)
        )
    }

    @Test
    fun unMinutoEnSingular() {
        assertEquals(
            "Actualizado hace 1 minuto",
            antiguedadDeDatos(hace(60_000L), ahora)
        )
    }

    @Test
    fun variosMinutosEnPlural() {
        assertEquals(
            "Actualizado hace 5 minutos",
            antiguedadDeDatos(hace(5 * 60_000L), ahora)
        )
    }

    @Test
    fun unaHoraEnSingular() {
        assertEquals(
            "Actualizado hace 1 hora",
            antiguedadDeDatos(hace(60 * 60_000L), ahora)
        )
    }

    @Test
    fun variasHorasEnPlural() {
        assertEquals(
            "Actualizado hace 2 horas",
            antiguedadDeDatos(hace(2 * 60 * 60_000L), ahora)
        )
    }

    @Test
    fun unDiaEnSingular() {
        assertEquals(
            "Actualizado hace 1 día",
            antiguedadDeDatos(hace(24 * 60 * 60_000L), ahora)
        )
    }

    @Test
    fun variosDiasEnPlural() {
        assertEquals(
            "Actualizado hace 3 días",
            antiguedadDeDatos(hace(3 * 24 * 60 * 60_000L), ahora)
        )
    }

    @Test
    fun unRelojAdelantadoNoGeneraAntiguedadNegativa() {
        assertEquals(
            "Actualizado hace menos de un minuto",
            antiguedadDeDatos(ahora + 60_000L, ahora)
        )
    }
}
