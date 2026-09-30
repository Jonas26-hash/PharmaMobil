package pe.edu.upeu.pharmamobil.presentation.cliente

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Contrato del filtro de la cartera: busca sobre nombre completo y DNI. */
class ClienteListadoTest {

    private fun clienteUi(
        id: Long,
        nombreCompleto: String,
        dni: String
    ) = ClienteUi(
        id = id,
        nombreCompleto = nombreCompleto,
        dni = dni,
        telefono = "",
        email = "",
        direccion = "",
        activo = true
    )

    private val cartera = ClienteUiState(
        fase = ClienteUiState.Fase.ConClientes(
            listOf(
                clienteUi(id = 1L, nombreCompleto = "María García", dni = "12345678"),
                clienteUi(id = 2L, nombreCompleto = "Juan Pérez", dni = "87654321")
            )
        )
    )

    @Test
    fun sinTextoDeBusquedaDevuelveTodo() {
        assertEquals(listOf(1L, 2L), cartera.filtrarPorBusqueda().map { it.id })
    }

    @Test
    fun filtraPorNombreCompletoSinImportarMayusculas() {
        assertEquals(
            listOf(2L),
            cartera.copy(busqueda = "juan").filtrarPorBusqueda().map { it.id }
        )
    }

    @Test
    fun filtraPorDni() {
        assertEquals(
            listOf(1L),
            cartera.copy(busqueda = "12345678").filtrarPorBusqueda().map { it.id }
        )
    }

    @Test
    fun siNoHayCoincidenciasDevuelveVacio() {
        assertTrue(cartera.copy(busqueda = "99887766").filtrarPorBusqueda().isEmpty())
    }
}