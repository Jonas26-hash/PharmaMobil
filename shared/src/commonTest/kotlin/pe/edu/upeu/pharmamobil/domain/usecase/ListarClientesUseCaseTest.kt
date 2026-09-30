package pe.edu.upeu.pharmamobil.domain.usecase

import kotlinx.coroutines.test.runTest
import pe.edu.upeu.pharmamobil.data.repository.FakeClienteRepository
import pe.edu.upeu.pharmamobil.domain.model.Cliente
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ListarClientesUseCaseTest {

    @Test
    fun devuelveLaCarteraDelRepositorio() = runTest {

        val repositorio = FakeClienteRepository(
            mutableListOf(
                Cliente(
                    id = 1L,
                    nombre = "María",
                    apellido = "García",
                    dni = "12345678"
                )
            )
        )

        val resultado = ListarClientesUseCase(repositorio).invoke()

        assertEquals(1, resultado.getOrThrow().size)
        assertEquals("María García", resultado.getOrThrow().first().nombreCompleto)
    }

    @Test
    fun devuelveFailureCuandoElRepositorioFalla() = runTest {

        val repositorio = FakeClienteRepository().apply {
            fallaAlListar = IllegalStateException("Sin conexión")
        }

        val resultado = ListarClientesUseCase(repositorio).invoke()

        assertTrue(resultado.isFailure)
        assertEquals("Sin conexión", resultado.exceptionOrNull()?.message)
    }
}