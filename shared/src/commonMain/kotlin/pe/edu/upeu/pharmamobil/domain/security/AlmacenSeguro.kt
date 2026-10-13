package pe.edu.upeu.pharmamobil.domain.security

/**
 * Almacenamiento cifrado del token de sesión, común a todas las plataformas.
 *
 * El contrato es mínimo a propósito: solo el token se considera un secreto y
 * por eso es lo único que se guarda aquí. Android lo respalda con el
 * Android Keystore e iOS con el Keychain (ver los `actual` en cada plataforma).
 *
 * La interfaz se inyecta con Koin: Android necesita un `Context` para su
 * implementación e iOS un identificador de Keychain, así que cada plataforma
 * construye la suya y la registra en su propio módulo.
 */
interface AlmacenSeguro {

    /** Guarda (o reemplaza) el token. */
    fun guardarToken(token: String)

    /** Devuelve el token guardado o `null` si no hay ninguno. */
    fun leerToken(): String?

    /** Elimina el token; es seguro llamarlo aunque no exista. */
    fun borrarToken()
}
