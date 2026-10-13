package pe.edu.upeu.pharmamobil.domain.auth

interface AuthRepository {

    suspend fun iniciarSesion(credenciales: Credenciales): Sesion

    suspend fun recuperarSesion(): Sesion?

    suspend fun cerrarSesion()
}
