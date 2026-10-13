package pe.edu.upeu.pharmamobil.domain.auth

data class Sesion(
    val token: String,
    val usuario: String,
    val nombreCompleto: String,
    val permisos: Set<String> = emptySet()
) {

    fun puede(permiso: String): Boolean = permiso in permisos

    fun puedeAlguno(permisos: Collection<String>): Boolean =
        permisos.any { puede(it) }
}
