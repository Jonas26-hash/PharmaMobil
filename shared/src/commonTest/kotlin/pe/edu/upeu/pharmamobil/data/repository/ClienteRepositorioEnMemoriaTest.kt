package pe.edu.upeu.pharmamobil.data.repository

import kotlinx.coroutines.test.runTest
import pe.edu.upeu.pharmamobil.domain.model.Cliente
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ClienteRepositorioEnMemoriaTest {

    private fun nuevoCliente(dni: String) = Cliente(
        id = 0L,
        nombre = "Lucía",
        apellido = "Torres",
        dni = dni
    )

    @Test
    fun asignaElSiguienteIdTrasLaSemilla() = runTest {

        val repositorio = ClienteRepositorioEnMemoria()

        val guardado = repositorio.registrar(nuevoCliente("55667788"))

        assertEquals(4L, guardado.id)
    }

    @Test
    fun listarDevuelveLaSemillaYLoRegistrado() = runTest {

        val repositorio = ClienteRepositorioEnMemoria()
        repositorio.registrar(nuevoCliente("55667788"))

        val resultado = repositorio.listar()

        assertEquals(4, resultado.size)
        assertEquals("55667788", resultado.last().dni)
    }

    @Test
    fun rechazaUnDniDuplicado() = runTest {

        val repositorio = ClienteRepositorioEnMemoria()

        val fallo = assertFailsWith<IllegalArgumentException> {
            repositorio.registrar(nuevoCliente("12345678"))
        }

        assertEquals("Ya existe un cliente con el DNI: 12345678", fallo.message)
    }
}