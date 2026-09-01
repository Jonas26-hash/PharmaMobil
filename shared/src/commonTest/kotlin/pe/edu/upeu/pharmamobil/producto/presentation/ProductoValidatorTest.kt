package pe.edu.upeu.pharmamobil.producto.presentation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ProductoValidatorTest {

    @Test
    fun aceptaUnNombreValido() {
        assertNull(ProductoValidator.validarNombre("Paracetamol 500 mg"))
    }

    @Test
    fun rechazaNombreVacio() {
        assertEquals(
            "El nombre es obligatorio.",
            ProductoValidator.validarNombre("")
        )
    }

    @Test
    fun rechazaNombreSoloEspacios() {
        assertEquals(
            "El nombre es obligatorio.",
            ProductoValidator.validarNombre("   ")
        )
    }

    @Test
    fun aceptaUnPrecioValido() {
        assertNull(ProductoValidator.validarPrecio("8.50"))
    }

    @Test
    fun rechazaPrecioNoNumerico() {
        assertEquals(
            "Ingresa un precio numérico.",
            ProductoValidator.validarPrecio("abc")
        )
    }

    @Test
    fun rechazaPrecioCero() {
        assertEquals(
            "El precio debe ser mayor que cero.",
            ProductoValidator.validarPrecio("0")
        )
    }

    @Test
    fun rechazaPrecioNegativo() {
        assertEquals(
            "El precio debe ser mayor que cero.",
            ProductoValidator.validarPrecio("-1")
        )
    }

    @Test
    fun aceptaStockValido() {
        assertNull(ProductoValidator.validarStock("100"))
    }

    @Test
    fun aceptaStockCero() {
        assertNull(ProductoValidator.validarStock("0"))
    }

    @Test
    fun rechazaStockNoEntero() {
        assertEquals(
            "Ingresa un stock entero.",
            ProductoValidator.validarStock("abc")
        )
    }

    @Test
    fun rechazaStockNegativo() {
        assertEquals(
            "El stock no puede ser negativo.",
            ProductoValidator.validarStock("-5")
        )
    }

    @Test
    fun secuenciaDeValidacionRechazaDatosInvalidos() {
        assertNotNull(ProductoValidator.validarNombre(""))
        assertNotNull(ProductoValidator.validarPrecio("abc"))
        assertNotNull(ProductoValidator.validarStock("-1"))
    }
}