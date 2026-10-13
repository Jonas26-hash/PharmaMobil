package pe.edu.upeu.pharmamobil.domain.auth

/**
 * Sesión autenticada vigente.
 *
 * [permisos] son las capacidades que el backend reconoce para el usuario. El
 * cliente las usa solo para **ocultar** acciones que el usuario no puede
 * ejecutar; la autorización real la sigue imponiendo el servidor. Ocultar un
 * botón nunca sustituye una comprobación del backend.
 *
 * [puede] es la consulta central de permisos: la presentación no reparte
 * comparaciones de roles por las pantallas.
 */
data class Sesion(
    val token: String,
    val usuario: String,
    val nombreCompleto: String,
    val permisos: Set<String> = emptySet()
) {

    /** `true` si la sesión incluye exactamente [permiso]. */
    fun puede(permiso: String): Boolean = permiso in permisos

    /** `true` si la sesión incluye al menos uno de [permisos]. */
    fun puedeAlguno(permisos: Collection<String>): Boolean =
        permisos.any { puede(it) }
}
