package pe.edu.upeu.pharmamobil.domain.model

/**
 * Cliente de la cartera. Modelo propio del proyecto, más rico que el del
 * curso (apellido, DNI y dirección), con sus invariantes validadas en el
 * constructor. El id lo asigna el repositorio, no la pantalla.
 */
data class Cliente(
    val id: Long,
    val nombre: String,
    val apellido: String,
    val dni: String,
    val telefono: String? = null,
    val email: String? = null,
    val direccion: String? = null,
    val activo: Boolean = true
) {
    init {
        require(nombre.isNotBlank()) {
            "El nombre del cliente no puede estar vacío"
        }
        require(apellido.isNotBlank()) {
            "El apellido del cliente no puede estar vacío"
        }
        require(dni.isNotBlank()) {
            "El DNI del cliente no puede estar vacío"
        }
        require(dni.length == 8) {
            "El DNI debe tener 8 dígitos"
        }
        require(telefono == null || telefono.isNotBlank()) {
            "El teléfono es opcional, pero no puede ser una cadena vacía"
        }
        require(email == null || email.isNotBlank()) {
            "El correo es opcional, pero no puede ser una cadena vacía"
        }
    }

    val nombreCompleto: String
        get() = "$nombre $apellido".trim()
}