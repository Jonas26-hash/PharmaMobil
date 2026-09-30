package pe.edu.upeu.pharmamobil.domain.usecase

import pe.edu.upeu.pharmamobil.domain.model.Cliente
import pe.edu.upeu.pharmamobil.domain.repository.ClienteRepository

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
 * Registra un cliente en la cartera contra el backend REST. Concentra en el
 * dominio las reglas que antes vivían en ClienteValidator (presentación):
 * qué correo o teléfono se acepta lo decide el negocio, no la pantalla.
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
        direccion: String,
        estado: Boolean = true
    ): Result<Cliente> {

        val errores = ErroresDeCliente(
            nombre = ValidacionesCliente.nombre(nombre),
            apellido = ValidacionesCliente.apellido(apellido),
            dni = ValidacionesCliente.dni(dni),
            email = ValidacionesCliente.email(email),
            telefono = ValidacionesCliente.telefono(telefono)
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
                    direccion = direccion.trim().ifBlank { null },
                    activo = estado
                )
            )
        }
    }
}