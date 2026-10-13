package pe.edu.upeu.pharmamobil.data.remote

import kotlinx.serialization.Serializable


@Serializable
data class LoginRequestDto(
    val username: String,
    val password: String
)

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

@Serializable
data class UsuarioResponseDto(
    val username: String,
    val empleadoNombre: String = "",
    val roles: List<String> = emptyList(),
    val permisos: List<String> = emptyList()
)
