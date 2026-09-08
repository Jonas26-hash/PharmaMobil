package pe.edu.upeu.pharmamobil.domain.repository

import kotlinx.coroutines.flow.Flow
import pe.edu.upeu.pharmamobil.domain.model.Cliente

interface ClienteRepository {

    /** Incorpora el cliente a la cartera y devuelve el cliente ya identificado. */
    suspend fun registrar(cliente: Cliente): Cliente

    /** Entrega la cartera completa en el orden en que fue registrada. */
    suspend fun listar(): List<Cliente>

    /** Reacciona a los cambios de la cartera. */
    fun observarClientes(): Flow<List<Cliente>>
}