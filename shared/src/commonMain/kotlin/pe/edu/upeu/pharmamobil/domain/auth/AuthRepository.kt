package pe.edu.upeu.pharmamobil.domain.auth

/**
 * Contrato de autenticación en el dominio.
 *
 * La presentación habla con esta interfaz y nunca con Ktor ni con el almacén
 * seguro: no construye cabeceras ni toca el token. Las implementaciones viven
 * en la capa de datos.
 */
interface AuthRepository {

    /**
     * Autentica con [credenciales] y devuelve la [Sesion] resultante.
     *
     * Lanza `ErrorDeApi` con:
     *  - `CredencialesInvalidas` si el usuario o la contraseña son incorrectos,
     *  - `SinConexion` / `TiempoAgotado` si no se pudo alcanzar el servidor.
     */
    suspend fun iniciarSesion(credenciales: Credenciales): Sesion

    /**
     * Recupera la sesión guardada al arrancar la aplicación.
     *
     * Devuelve `null` si no hay token almacenado o si el token ya no es válido;
     * en ese último caso borra el token caducado.
     */
    suspend fun recuperarSesion(): Sesion?

    /** Cierra la sesión local: borra el token y limpia la caché del cliente. */
    suspend fun cerrarSesion()
}
