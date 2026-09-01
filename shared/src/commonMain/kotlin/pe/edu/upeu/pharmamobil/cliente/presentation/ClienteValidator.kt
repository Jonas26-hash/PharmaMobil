package pe.edu.upeu.pharmamobil.cliente.presentation

object ClienteValidator {

    fun validarNombre(nombre: String): String? {
        if (nombre.isBlank()) {
            return "El nombre es obligatorio"
        }
        return null
    }

    fun validarApellido(apellido: String): String? {
        if (apellido.isBlank()) {
            return "El apellido es obligatorio"
        }
        return null
    }

    fun validarDni(dni: String): String? {
        return when {
            dni.isBlank() -> "El DNI es obligatorio"
            dni.length != 8 -> "El DNI debe tener 8 dígitos"
            else -> null
        }
    }
}