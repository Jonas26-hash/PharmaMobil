package pe.edu.upeu.pharmamobil.presentation.cliente

import pe.edu.upeu.pharmamobil.domain.model.Cliente

data class ClienteUi(
    val id: Long,
    val nombreCompleto: String,
    val dni: String,
    val telefono: String,
    val email: String,
    val direccion: String,
    val activo: Boolean
)

fun Cliente.aUi(): ClienteUi = ClienteUi(
    id = id,
    nombreCompleto = nombreCompleto,
    dni = dni,
    telefono = telefono ?: TELEFONO_AUSENTE,
    email = email ?: "",
    direccion = direccion ?: "",
    activo = activo
)

const val TELEFONO_AUSENTE = "No registrado"