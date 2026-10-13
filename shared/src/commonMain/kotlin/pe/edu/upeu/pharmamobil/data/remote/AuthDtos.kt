package pe.edu.upeu.pharmamobil.data.remote

import kotlinx.serialization.Serializable

// ---------------------------------------------------------------------------
// DTO del contrato de autenticación.
//
// Verificado de extremo a extremo contra el backend real `pharmaSoft`
// (Spring Security + JWT) en un celular físico.
//
// POST /api/v1/auth/login recibe { username, password } y responde
//   { token, tipo, expiraEnSegundos, username, nombreCompleto, roles, permisos }.
// GET  /api/v1/auth/me responde
//   { id, username, empleadoNombre, cargo, roles, permisos, estado, fechaCreacion }.
// ---------------------------------------------------------------------------

/** Cuerpo de `POST /api/v1/auth/login`. */
@Serializable
data class LoginRequestDto(
    val username: String,
    val password: String
)

/** Respuesta de `POST /api/v1/auth/login`. */
@Serializable
data class LoginResponseDto(
    val token: String,
    val tipo: String = "Bearer",
    val expiraEnSegundos: Long = 0,
    val username: String,
    val nombreCompleto: String = "",
    val roles: List<String> = emptyList(),
    val permisos: List<String> = emptyList()
)

/** Respuesta de `GET /api/v1/auth/me`. */
@Serializable
data class UsuarioResponseDto(
    val username: String,
    val empleadoNombre: String = "",
    val roles: List<String> = emptyList(),
    val permisos: List<String> = emptyList()
)
