package pe.edu.upeu.pharmamobil.domain.usecase

import kotlinx.coroutines.test.runTest
import pe.edu.upeu.pharmamobil.data.repository.FakeClienteRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

/**
 * Las reglas que antes vivían en ClienteValidator, dentro de la capa de
 * presentación, se verifican ahora en el caso de uso.
 */
class RegistrarClienteUseCaseTest {

    private fun nuevoCasoDeUso(
        repositorio: FakeClienteRepository = FakeClienteRepository()
    ) = RegistrarClienteUseCase(repositorio)

    private suspend fun erroresAlRegistrar(
        nombre: String = "María",
        apellido: String = "García",
        dni: String = "12345678",
        telefono: String = "987654321",
        email: String = "maria@email.com"
    ): ErroresDeCliente {

        val resultado = nuevoCasoDeUso().invoke(
            nombre,
            apellido,
            dni,
            telefono,
            email,
            "Av. Los Sauces 123"
        )
        val fallo = assertIs<ClienteInvalidoException>(resultado.exceptionOrNull())
        return fallo.errores
    }

    @Test
    fun aceptaUnClienteValido() = runTest {

        val resultado = nuevoCasoDeUso().invoke(
            "María",
            "García",
            "12345678",
            "987654321",
            "maria@email.com",
            "Av. Los Sauces 123"
        )

        assertNull(resultado.exceptionOrNull())
        assertEquals("987654321", resultado.getOrThrow().telefono)
        assertEquals("Av. Los Sauces 123", resultado.getOrThrow().direccion)
    }

    @Test
    fun elTelefonoEsOpcionalYSeGuardaComoNull() = runTest {

        val resultado = nuevoCasoDeUso().invoke(
            "María",
            "García",
            "12345678",
            "   ",
            "maria@email.com",
            ""
        )

        assertNull(resultado.exceptionOrNull())
        assertNull(resultado.getOrThrow().telefono)
    }

    @Test
    fun elCorreoEsOpcionalYSeGuardaComoNull() = runTest {

        val resultado = nuevoCasoDeUso().invoke(
            "María",
            "García",
            "12345678",
            "",
            "   ",
            ""
        )

        assertNull(resultado.exceptionOrNull())
        assertNull(resultado.getOrThrow().email)
    }

    @Test
    fun rechazaNombreVacio() = runTest {
        assertEquals(
            "El nombre es obligatorio",
            erroresAlRegistrar(nombre = "   ").nombre
        )
    }

    @Test
    fun rechazaApellidoVacio() = runTest {
        assertEquals(
            "El apellido es obligatorio",
            erroresAlRegistrar(apellido = "   ").apellido
        )
    }

    @Test
    fun rechazaDniVacio() = runTest {
        assertEquals(
            "El DNI es obligatorio",
            erroresAlRegistrar(dni = "   ").dni
        )
    }

    @Test
    fun rechazaDniConMenosDeOchoDigitos() = runTest {
        assertEquals(
            "El DNI debe tener 8 dígitos",
            erroresAlRegistrar(dni = "12345").dni
        )
    }

    @Test
    fun rechazaCorreoConFormatoInvalido() = runTest {
        assertEquals(
            "El correo no tiene un formato válido",
            erroresAlRegistrar(email = "maria.central.pe").email
        )
    }

    @Test
    fun rechazaTelefonoConMenosDeSeisDigitos() = runTest {
        assertEquals(
            "El teléfono debe tener entre 6 y 9 dígitos",
            erroresAlRegistrar(telefono = "12345").telefono
        )
    }

    @Test
    fun reportaLosErroresALaVez() = runTest {

        val errores = erroresAlRegistrar(
            nombre = "",
            apellido = "",
            dni = "",
            telefono = "abc",
            email = "abc"
        )

        assertEquals("El nombre es obligatorio", errores.nombre)
        assertEquals("El apellido es obligatorio", errores.apellido)
        assertEquals("El DNI es obligatorio", errores.dni)
        assertEquals("El teléfono debe tener entre 6 y 9 dígitos", errores.telefono)
        assertEquals("El correo no tiene un formato válido", errores.email)
    }

    @Test
    fun elIdLoAsignaElRepositorio() = runTest {

        val casoDeUso = nuevoCasoDeUso()

        val primero = casoDeUso.invoke(
            "María",
            "García",
            "12345678",
            "",
            "",
            ""
        ).getOrThrow()
        val segundo = casoDeUso.invoke(
            "Juan",
            "Pérez",
            "87654321",
            "",
            "",
            ""
        ).getOrThrow()

        assertEquals(1L, primero.id)
        assertEquals(2L, segundo.id)
    }

    @Test
    fun devuelveFailureCuandoElRepositorioFalla() = runTest {

        val repositorio = FakeClienteRepository().apply {
            fallaAlRegistrar = IllegalStateException("Sin conexión")
        }

        val resultado = nuevoCasoDeUso(repositorio).invoke(
            "María",
            "García",
            "12345678",
            "987654321",
            "",
            ""
        )

        assertEquals("Sin conexión", resultado.exceptionOrNull()?.message)
    }
}