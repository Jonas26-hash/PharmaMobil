package pe.edu.upeu.pharmamobil.domain.usecase

import pe.edu.upeu.pharmamobil.domain.repository.ClienteRepository

class EliminarClienteUseCase(
    private val clienteRepository: ClienteRepository
) {

    suspend operator fun invoke(clienteId: Long): Result<Unit> = resultadoDe {
        clienteRepository.eliminar(clienteId)
    }
}
