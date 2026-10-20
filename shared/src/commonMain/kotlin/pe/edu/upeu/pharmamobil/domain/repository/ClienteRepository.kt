package pe.edu.upeu.pharmamobil.domain.repository

import kotlinx.coroutines.flow.Flow
import pe.edu.upeu.pharmamobil.domain.model.Cliente

interface ClienteRepository {

    suspend fun registrar(cliente: Cliente): Cliente

    suspend fun actualizar(cliente: Cliente): Cliente

    suspend fun eliminar(clienteId: Long)

    suspend fun listar(): List<Cliente>

    suspend fun obtenerPorId(clienteId: Long): Cliente

    suspend fun sincronizar()

    fun observar(): Flow<List<Cliente>>
}
