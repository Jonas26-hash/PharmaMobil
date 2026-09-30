package pe.edu.upeu.pharmamobil.presentation.producto

import kotlin.test.Test
import kotlin.test.assertEquals

/** Formato del precio para pantalla y edición (Kotlin común no trae String.format). */
class FormatoPrecioTest {

    @Test
    fun enSolesFormateaConDosDecimales() {
        assertEquals("S/ 12.50", 12.5.enSoles())
        assertEquals("S/ 0.00", 0.0.enSoles())
        assertEquals("S/ 5.76", 5.755.enSoles())
    }

    @Test
    fun enSolesManejaPreciosQueContentanDecimales() {
        assertEquals("S/ 5.75", 5.75.enSoles())
    }

    @Test
    fun aTextoEditorialQuitaElCeroColganteDeLosEnteros() {
        assertEquals("12", 12.0.aTextoEditorial())
        assertEquals("5", 5.0.aTextoEditorial())
    }

    @Test
    fun aTextoEditorialConservaLosDecimalesSignificativos() {
        assertEquals("5.5", 5.5.aTextoEditorial())
        assertEquals("3.25", 3.25.aTextoEditorial())
    }
}