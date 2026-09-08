package pe.edu.upeu.pharmamobil.domain.usecase

import kotlinx.coroutines.test.runTest
import pe.edu.upeu.pharmamobil.data.repository.FakeProductoRepository
import pe.edu.upeu.pharmamobil.domain.model.Producto
import kotlin.test.Test
import kotlin.test.assertEquals

class RegistrarVentaUseCaseTest {

    @Test
    fun registraLaVentaYEntregaElTotal() = runTest {

        val repositorio = FakeProductoRepository(
            mutableListOf(
                Producto(id = 1L, nombre = "Paracetamol", precio = 15.50, stock = 5)
            )
        )

        val venta = RegistrarVentaUseCase(repositorio)
            .invoke(productoId = 1L, cantidad = 1)
            .getOrThrow()

        assertEquals("Paracetamol", venta.items.single().nombreProducto)
        assertEquals(15.50, venta.total, 0.0001)
    }

    @Test
    fun devuelveFailureCuandoElRepositorioFalla() = runTest {

        val repositorio = FakeProductoRepository().apply {
            fallaAlVender = IllegalStateException("Sin conexión")
        }

        val resultado = RegistrarVentaUseCase(repositorio)
            .invoke(productoId = 1L, cantidad = 1)

        assertEquals("Sin conexión", resultado.exceptionOrNull()?.message)
    }
}