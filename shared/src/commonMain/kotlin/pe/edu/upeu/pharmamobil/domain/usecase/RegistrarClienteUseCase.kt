package pe.edu.upeu.pharmamobil.domain.usecase

import pe.edu.upeu.pharmamobil.domain.model.Cliente
import pe.edu.upeu.pharmamobil.domain.repository.ClienteRepository

private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
private val TELEFONO_REGEX = Regex("^[0-9]{6,9}$")

data class ErroresDeCliente(
    val nombre: String? = null,
    val apellido: String? = null,
    val dni: String? = null,
    val email: String? = null,
    val telefono: String? = null
) {

    val hayErrores: Boolean
        get() = nombre != null || apellido != null || dni != null || email != null || telefono != null
}

class ClienteInvalidoException(
    val errores: ErroresDeCliente
) : IllegalArgumentException("Los datos del cliente no cumplen las reglas del negocio")

/**
 * Registra un cliente en la cartera. Concentra en el dominio las reglas que
 * antes vivían en ClienteValidator (presentación): qué correo o teléfono se
 * acepta lo decide el negocio, no la pantalla.
 */
class RegistrarClienteUseCase(
    private val clienteRepository: ClienteRepository
) {

    suspend operator fun invoke(
        nombre: String,
        apellido: String,
        dni: String,
        telefono: String,
        email: String,
        direccion: String
    ): Result<Cliente> {

        val errores = ErroresDeCliente(
            nombre = validarNombre(nombre),
            apellido = validarApellido(apellido),
            dni = validarDni(dni),
            email = validarEmail(email),
            telefono = validarTelefono(telefono)
        )

        if (errores.hayErrores) {
            return Result.failure(ClienteInvalidoException(errores))
        }

        return resultadoDe {
            clienteRepository.registrar(
                Cliente(
                    id = 0L,
                    nombre = nombre.trim(),
                    apellido = apellido.trim(),
                    dni = dni.trim(),
                    telefono = telefono.trim().ifBlank { null },
                    email = email.trim().ifBlank { null },
                    direccion = direccion.trim().ifBlank { null }
                )
            )
        }
    }

    private fun validarNombre(nombre: String): String? {
        return if (nombre.isBlank()) "El nombre es obligatorio" else null
    }

    private fun validarApellido(apellido: String): String? {
        return if (apellido.isBlank()) "El apellido es obligatorio" else null
    }

    private fun validarDni(dni: String): String? {
        return when {
            dni.isBlank() -> "El DNI es obligatorio"
            dni.length != 8 -> "El DNI debe tener 8 dígitos"
            else -> null
        }
    }

    /** El correo es opcional: solo se valida el formato si viene escrito. */
    private fun validarEmail(email: String): String? {
        return if (email.isNotBlank() && !EMAIL_REGEX.matches(email.trim())) {
            "El correo no tiene un formato válido"
        } else {
            null
        }
    }

    /** El teléfono es opcional: solo se valida el formato si viene escrito. */
    private fun validarTelefono(telefono: String): String? {
        return if (telefono.isNotBlank() && !TELEFONO_REGEX.matches(telefono.trim())) {
            "El teléfono debe tener entre 6 y 9 dígitos"
        } else {
            null
        }
    }
}