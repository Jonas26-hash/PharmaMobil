package pe.edu.upeu.pharmamobil.domain.usecase

import pe.edu.upeu.pharmamobil.domain.model.Cliente
import pe.edu.upeu.pharmamobil.domain.repository.ClienteRepository

/** Obtiene un cliente puntual de la cartera (GET /{id}). */
class ObtenerClienteUseCase(
    private val clienteRepository: ClienteRepository
) {

    suspend operator fun invoke(clienteId: Long): Result<Cliente> = resultadoDe {
        clienteRepository.obtenerPorId(clienteId)
    }
}