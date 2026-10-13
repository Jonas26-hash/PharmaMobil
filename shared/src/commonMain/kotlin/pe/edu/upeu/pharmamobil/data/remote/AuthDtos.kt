package pe.edu.upeu.pharmamobil.data.remote

import kotlinx.serialization.Serializable

// ---------------------------------------------------------------------------
// DTO del contrato de autenticación.
//
// ADVERTENCIA (limitación verificada): el backend pharmaSoft real no implementa
// estos endpoints. Este contrato refleja el que la guía de la sesión 10 espera
// (`POST /api/v1/auth/login` y `GET /api/v1/auth/me`), pero NO ha podido
// confirmarse contra el servidor. Cuando el backend los publique hay que
// contrastar los nombres exactos de los campos antes de considerarlo estable.
// ---------------------------------------------------------------------------

/** Cuerpo de `POST /api/v1/auth/login`. */
@Serializable
data class LoginRequestDto(
    val usuario: String,
    val contrasena: String
)

/** Respuesta de `POST /api/v1/auth/login`. */
@Serializable
data class LoginResponseDto(
    val token: String,
    val usuario: String,
    val nombreCompleto: String,
    val permisos: List<String> = emptyList()
)

/** Respuesta de `GET /api/v1/auth/me`. */
@Serializable
data class UsuarioResponseDto(
    val usuario: String,
    val nombreCompleto: String,
    val permisos: List<String> = emptyList()
)
