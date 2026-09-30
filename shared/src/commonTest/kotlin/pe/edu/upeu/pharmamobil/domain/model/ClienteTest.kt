package pe.edu.upeu.pharmamobil.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

/**
 * El "No registrado" del teléfono ausente ya no se prueba aquí: era texto de
 * pantalla dentro de un modelo de dominio y ahora vive en [ClienteUi]. Lo que
 * sí le corresponde al modelo son sus invariantes.
 */
class ClienteTest {

    private fun cliente(
        nombre: String = "María",
        apellido: String = "García",
        dni: String = "12345678"
    ) = Cliente(
        id = 1L,
        nombre = nombre,
        apellido = apellido,
        dni = dni
    )

    @Test
    fun aceptaUnClienteConTelefonoYCorreo() {

        val cliente = Cliente(
            id = 1L,
            nombre = "María",
            apellido = "García",
            dni = "12345678",
            telefono = "987654321",
            email = "maria@email.com",
            direccion = "Av. Los Sauces 123"
        )

        assertEquals("987654321", cliente.telefono)
        assertEquals("maria@email.com", cliente.email)
    }

    @Test
    fun elTelefonoPuedeFaltar() {
        assertNull(cliente().telefono)
    }

    @Test
    fun elCorreoPuedeFaltar() {
        assertNull(cliente().email)
    }

    @Test
    fun laDireccionPuedeFaltar() {
        assertNull(cliente().direccion)
    }

    @Test
    fun nombreCompletoUneNombreYApellido() {
        assertEquals("María García", cliente().nombreCompleto)
    }

    @Test
    fun noSeConstruyeSinNombre() {
        assertFailsWith<IllegalArgumentException> { cliente(nombre = "   ") }
    }

    @Test
    fun noSeConstruyeSinApellido() {
        assertFailsWith<IllegalArgumentException> { cliente(apellido = " ") }
    }

    @Test
    fun noSeConstruyeSinDni() {
        assertFailsWith<IllegalArgumentException> { cliente(dni = "  ") }
    }

    @Test
    fun noSeConstruyeConDniDeOtraLongitud() {
        assertFailsWith<IllegalArgumentException> { cliente(dni = "12345") }
    }

    @Test
    fun noSeConstruyeConTelefonoVacio() {

        assertFailsWith<IllegalArgumentException> {
            Cliente(
                id = 1L,
                nombre = "María",
                apellido = "García",
                dni = "12345678",
                telefono = ""
            )
        }
    }
}