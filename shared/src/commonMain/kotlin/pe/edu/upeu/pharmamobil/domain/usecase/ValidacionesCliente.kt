package pe.edu.upeu.pharmamobil.domain.usecase

private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
private val TELEFONO_REGEX = Regex("^[0-9]{6,9}$")

/**
 * Reglas de negocio del cliente, compartidas por Registro y Actualización
 * para que la validación no viva en dos sitios con mensajes distintos.
 */
internal object ValidacionesCliente {

    fun nombre(nombre: String): String? {
        return if (nombre.isBlank()) "El nombre es obligatorio" else null
    }

    fun apellido(apellido: String): String? {
        return if (apellido.isBlank()) "El apellido es obligatorio" else null
    }

    fun dni(dni: String): String? {
        return when {
            dni.isBlank() -> "El DNI es obligatorio"
            dni.length != 8 -> "El DNI debe tener 8 dígitos"
            else -> null
        }
    }

    /** El correo es opcional: solo se valida el formato si viene escrito. */
    fun email(email: String): String? {
        return if (email.isNotBlank() && !EMAIL_REGEX.matches(email.trim())) {
            "El correo no tiene un formato válido"
        } else {
            null
        }
    }

    /** El teléfono es opcional: solo se valida el formato si viene escrito. */
    fun telefono(telefono: String): String? {
        return if (telefono.isNotBlank() && !TELEFONO_REGEX.matches(telefono.trim())) {
            "El teléfono debe tener entre 6 y 9 dígitos"
        } else {
            null
        }
    }
}