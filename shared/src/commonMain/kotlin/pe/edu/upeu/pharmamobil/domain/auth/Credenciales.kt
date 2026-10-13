package pe.edu.upeu.pharmamobil.domain.auth

/**
 * Credenciales de acceso que el usuario escribe en la pantalla de login.
 *
 * Viven en el dominio porque son una entrada de negocio, no un DTO: la capa de
 * datos las traduce al contrato real del backend y la presentación nunca
 * conoce el JSON.
 */
data class Credenciales(
    val usuario: String,
    val contrasena: String
)
