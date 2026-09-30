package pe.edu.upeu.pharmamobil.domain.usecase

import kotlinx.coroutines.test.runTest
import pe.edu.upeu.pharmamobil.data.repository.FakeProductoRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Las reglas de negocio (antes ProductoValidator, en presentación) se
 * verifican aquí, donde vive la validación desde esta sesión.
 *
 * Se prueba contra [FakeProductoRepository] y no contra el repositorio en
 * memoria: una prueba de dominio no debe depender de la capa de datos.
 */
class RegistrarProductoUseCaseTest {

    private fun nuevoCasoDeUso(
        repositorio: FakeProductoRepository = FakeProductoRepository()
    ) = RegistrarProductoUseCase(repositorio)

    private suspend fun erroresAlRegistrar(
        nombre: String = "Paracetamol",
        precio: String = "12.50",
        stock: String = "5",
        estado: Boolean = true,
        categoriaId: Long = 1L
    ): ErroresDeProducto {

        val resultado = nuevoCasoDeUso().invoke(
            nombre,
            precio,
            stock,
            estado,
            categoriaId
        )
        val fallo = assertIs<ProductoInvalidoException>(resultado.exceptionOrNull())
        return fallo.errores
    }

    @Test
    fun aceptaUnProductoValido() = runTest {

        val resultado = nuevoCasoDeUso().invoke(
            "Paracetamol",
            "12.50",
            "5",
            true,
            1L
        )

        assertNull(resultado.exceptionOrNull())
        assertEquals(12.50, resultado.getOrThrow().precio, 0.0001)
        assertEquals(5, resultado.getOrThrow().stock)
        assertEquals(1L, resultado.getOrThrow().categoriaId)
    }

    @Test
    fun aceptaUnProductoInactivo() = runTest {

        val producto = nuevoCasoDeUso().invoke(
            "Paracetamol",
            "12.50",
            "0",
            false,
            1L
        ).getOrThrow()

        assertFalse(producto.activo)
    }

    @Test
    fun aceptaStockCero() = runTest {

        val resultado = nuevoCasoDeUso().invoke(
            "Paracetamol",
            "12.50",
            "0",
            true,
            1L
        )

        assertNull(resultado.exceptionOrNull())
    }

    @Test
    fun rechazaComoSinCategoria() = runTest {
        assertEquals(
            "Selecciona una categoría.",
            erroresAlRegistrar(categoriaId = 0L).categoria
        )
    }

    @Test
    fun rechazaPrecioNoNumerico() = runTest {
        assertEquals(
            "Ingresa un precio numérico.",
            erroresAlRegistrar(precio = "abc").precio
        )
    }

    @Test
    fun rechazaNaNComoPrecio() = runTest {
        assertEquals(
            "Ingresa un precio numérico.",
            erroresAlRegistrar(precio = "NaN").precio
        )
    }

    @Test
    fun rechazaInfinityComoPrecio() = runTest {
        assertEquals(
            "Ingresa un precio numérico.",
            erroresAlRegistrar(precio = "Infinity").precio
        )
    }

    @Test
    fun rechazaPrecioCero() = runTest {
        assertEquals(
            "El precio debe ser mayor que cero.",
            erroresAlRegistrar(precio = "0").precio
        )
    }

    @Test
    fun rechazaStockNegativo() = runTest {
        assertEquals(
            "El stock no puede ser negativo.",
            erroresAlRegistrar(stock = "-1").stock
        )
    }

    @Test
    fun rechazaStockNoEntero() = runTest {
        assertEquals(
            "Ingresa un stock entero.",
            erroresAlRegistrar(stock = "dos").stock
        )
    }

    @Test
    fun rechazaNombreVacio() = runTest {
        assertEquals(
            "El nombre es obligatorio.",
            erroresAlRegistrar(nombre = "   ").nombre
        )
    }

    @Test
    fun reportaLosErroresALaVez() = runTest {

        val errores = erroresAlRegistrar(nombre = "", precio = "", stock = "", categoriaId = 0L)

        assertEquals("El nombre es obligatorio.", errores.nombre)
        assertEquals("Ingresa un precio numérico.", errores.precio)
        assertEquals("Ingresa un stock entero.", errores.stock)
        assertEquals("Selecciona una categoría.", errores.categoria)
    }

    @Test
    fun elIdLoAsignaElRepositorioNoLaPantalla() = runTest {

        val casoDeUso = nuevoCasoDeUso()

        val primero = casoDeUso.invoke("Paracetamol", "12.50", "5", true, 1L).getOrThrow()
        val segundo = casoDeUso.invoke("Ibuprofeno", "8.90", "3", true, 1L).getOrThrow()

        assertEquals(1L, primero.id)
        assertEquals(2L, segundo.id)
    }

    @Test
    fun recortaElNombreAntesDeGuardar() = runTest {

        val producto = nuevoCasoDeUso().invoke(
            "  Paracetamol  ",
            "12.50",
            "5",
            true,
            1L
        ).getOrThrow()

        assertEquals("Paracetamol", producto.nombre)
    }

    @Test
    fun devuelveFailureCuandoElRepositorioFalla() = runTest {

        val repositorio = FakeProductoRepository().apply {
            fallaAlRegistrar = IllegalStateException("Sin conexión")
        }

        val resultado = nuevoCasoDeUso(repositorio).invoke(
            "Paracetamol",
            "12.50",
            "5",
            true,
            1L
        )

        assertTrue(resultado.isFailure)
        assertEquals("Sin conexión", resultado.exceptionOrNull()?.message)
    }
}