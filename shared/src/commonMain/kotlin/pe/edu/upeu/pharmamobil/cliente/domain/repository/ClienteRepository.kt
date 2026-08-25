package pe.edu.upeu.pharmamobil.cliente.domain.repository

import kotlinx.coroutines.flow.Flow
import pe.edu.upeu.pharmamobil.cliente.domain.model.Cliente

interface ClienteRepository {

    suspend fun obtenerClientes(): List<Cliente>

    suspend fun registrarCliente(
        nombre: String,
        apellido: String,
        dni: String,
        telefono: String?,
        email: String?,
        direccion: String?
    ): Cliente

    fun observarClientes(): Flow<List<Cliente>>
}
