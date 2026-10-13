package pe.edu.upeu.pharmamobil.domain.usecase

import kotlinx.coroutines.test.runTest
import pe.edu.upeu.pharmamobil.data.repository.FakeProductoRepository
import pe.edu.upeu.pharmamobil.domain.model.Producto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ListarProductosUseCaseTest {

    @Test
    fun devuelveElInventarioDelRepositorio() = runTest {

        val repositorio = FakeProductoRepository(
            mutableListOf(
                Producto(id = 1L, nombre = "Paracetamol", precio = 12.50, stock = 5)
            )
        )

        val resultado = ListarProductosUseCase(repositorio).invoke()

        assertEquals(1, resultado.getOrThrow().size)
        assertEquals("Paracetamol", resultado.getOrThrow().first().nombre)
    }

    @Test
    fun devuelveFailureCuandoElRepositorioFalla() = runTest {

        val repositorio = FakeProductoRepository().apply {
            fallaAlListar = IllegalStateException("Sin conexión")
        }

        val resultado = ListarProductosUseCase(repositorio).invoke()

        assertTrue(resultado.isFailure)
        assertEquals("Sin conexión", resultado.exceptionOrNull()?.message)
    }
}
