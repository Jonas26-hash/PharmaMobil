package pe.edu.upeu.pharmamobil.domain.usecase

import kotlinx.coroutines.test.runTest
import pe.edu.upeu.pharmamobil.data.repository.FakeProductoRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Las reglas que antes cubría ProductoValidatorTest ahora se verifican aquí,
 * que es donde vive la validación desde esta sesión.
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
        descripcion: String = "Analgésico",
        precio: String = "12.50",
        stock: String = "5",
        requiereReceta: Boolean = false,
        categoria: String = "Analgésico"
    ): ErroresDeProducto {

        val resultado = nuevoCasoDeUso().invoke(
            nombre,
            descripcion,
            precio,
            stock,
            requiereReceta,
            categoria
        )
        val fallo = assertIs<ProductoInvalidoException>(resultado.exceptionOrNull())
        return fallo.errores
    }

    @Test
    fun aceptaUnProductoValido() = runTest {

        val resultado = nuevoCasoDeUso().invoke(
            "Paracetamol",
            "Analgésico",
            "12.50",
            "5",
            false,
            "Analgésico"
        )

        assertNull(resultado.exceptionOrNull())
        assertEquals(12.50, resultado.getOrThrow().precio, 0.0001)
        assertEquals(5, resultado.getOrThrow().stock)
    }

    @Test
    fun aceptaLosCamposPropiosDelProyecto() = runTest {

        val resultado = nuevoCasoDeUso().invoke(
            nombre = "Amoxicilina",
            descripcion = "Antibiótico",
            precio = "25.00",
            stock = "0",
            requiereReceta = true,
            categoria = "Antibiótico"
        )

        val producto = resultado.getOrThrow()

        assertEquals("Antibiótico", producto.descripcion)
        assertTrue(producto.requiereReceta)
        assertEquals("Antibiótico", producto.categoria)
    }

    @Test
    fun aceptaStockCero() = runTest {

        val resultado = nuevoCasoDeUso().invoke(
            "Paracetamol",
            null,
            "12.50",
            "0",
            false,
            ""
        )

        assertNull(resultado.exceptionOrNull())
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
    fun reportaLosTresErroresALaVez() = runTest {

        val errores = erroresAlRegistrar(nombre = "", precio = "", stock = "")

        assertEquals("El nombre es obligatorio.", errores.nombre)
        assertEquals("Ingresa un precio numérico.", errores.precio)
        assertEquals("Ingresa un stock entero.", errores.stock)
    }

    @Test
    fun elIdLoAsignaElRepositorioNoLaPantalla() = runTest {

        val casoDeUso = nuevoCasoDeUso()

        val primero = casoDeUso.invoke(
            "Paracetamol",
            null,
            "12.50",
            "5",
            false,
            ""
        ).getOrThrow()
        val segundo = casoDeUso.invoke(
            "Ibuprofeno",
            null,
            "8.90",
            "3",
            false,
            ""
        ).getOrThrow()

        assertEquals(1L, primero.id)
        assertEquals(2L, segundo.id)
    }

    @Test
    fun recortaElNombreAntesDeGuardar() = runTest {

        val producto = nuevoCasoDeUso().invoke(
            "  Paracetamol  ",
            null,
            "12.50",
            "5",
            false,
            ""
        ).getOrThrow()

        assertEquals("Paracetamol", producto.nombre)
    }

    /** El camino que antes quedaba sin cubrir: el repositorio revienta. */
    @Test
    fun devuelveFailureCuandoElRepositorioFalla() = runTest {

        val repositorio = FakeProductoRepository().apply {
            fallaAlRegistrar = IllegalStateException("Sin conexión")
        }

        val resultado = nuevoCasoDeUso(repositorio).invoke(
            "Paracetamol",
            null,
            "12.50",
            "5",
            false,
            ""
        )

        assertEquals("Sin conexión", resultado.exceptionOrNull()?.message)
    }
}