package pe.edu.upeu.pharmamobil.cliente.domain.model

data class Cliente(
    val id: String,
    val nombre: String,
    val apellido: String,
    val dni: String,
    val telefono: String?,
    val email: String?,
    val direccion: String?
) {
    init {
        require(nombre.isNotBlank()) { "El nombre no puede estar vacío" }
        require(apellido.isNotBlank()) { "El apellido no puede estar vacío" }
        require(dni.isNotBlank()) { "El DNI no puede estar vacío" }
        require(dni.length == 8) { "El DNI debe tener 8 dígitos" }
    }

    val nombreCompleto: String
        get() = "$nombre $apellido"
}
