package pe.edu.upeu.pharmamobil.domain.usecase

import pe.edu.upeu.pharmamobil.domain.model.Cliente
import pe.edu.upeu.pharmamobil.domain.repository.ClienteRepository

/** Actualiza un cliente existente. Comparte la validación con el registro. */
class ActualizarClienteUseCase(
    private val clienteRepository: ClienteRepository
) {

    suspend operator fun invoke(
        id: Long,
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
            clienteRepository.actualizar(
                Cliente(
                    id = id,
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