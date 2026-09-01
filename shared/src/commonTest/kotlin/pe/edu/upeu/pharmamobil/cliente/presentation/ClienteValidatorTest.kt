package pe.edu.upeu.pharmamobil.cliente.presentation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ClienteValidatorTest {

    @Test
    fun aceptaNombreValido() {
        assertNull(ClienteValidator.validarNombre("María"))
    }

    @Test
    fun rechazaNombreVacio() {
        assertEquals(
            "El nombre es obligatorio",
            ClienteValidator.validarNombre("")
        )
    }

    @Test
    fun aceptaApellidoValido() {
        assertNull(ClienteValidator.validarApellido("García López"))
    }

    @Test
    fun rechazaApellidoVacio() {
        assertEquals(
            "El apellido es obligatorio",
            ClienteValidator.validarApellido("   ")
        )
    }

    @Test
    fun aceptaDniDeOchoDigitos() {
        assertNull(ClienteValidator.validarDni("12345678"))
    }

    @Test
    fun rechazaDniVacio() {
        assertEquals(
            "El DNI es obligatorio",
            ClienteValidator.validarDni("")
        )
    }

    @Test
    fun rechazaDniConLongitudIncorrecta() {
        assertEquals(
            "El DNI debe tener 8 dígitos",
            ClienteValidator.validarDni("1234")
        )
    }
}