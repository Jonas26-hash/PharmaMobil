package pe.edu.upeu.pharmamobil.domain.repository

import kotlinx.coroutines.flow.Flow
import pe.edu.upeu.pharmamobil.domain.model.Cliente

interface ClienteRepository {

    /** Incorpora el cliente a la cartera y devuelve el cliente ya identificado. */
    suspend fun registrar(cliente: Cliente): Cliente

    /** Actualiza un cliente existente y devuelve el cliente modificado. */
    suspend fun actualizar(cliente: Cliente): Cliente

    /** Da de baja física un cliente de la cartera. */
    suspend fun eliminar(clienteId: Long)

    /** Entrega la cartera completa. */
    suspend fun listar(): List<Cliente>

    /** Entrega un cliente puntual de la cartera. */
    suspend fun obtenerPorId(clienteId: Long): Cliente

    /** Reacciona a los cambios de la cartera. */
    fun observarClientes(): Flow<List<Cliente>>
}