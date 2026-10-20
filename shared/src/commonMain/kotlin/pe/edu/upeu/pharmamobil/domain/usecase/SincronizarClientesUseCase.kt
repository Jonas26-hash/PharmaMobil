package pe.edu.upeu.pharmamobil.domain.usecase

import pe.edu.upeu.pharmamobil.domain.repository.ClienteRepository

class SincronizarClientesUseCase(
    private val clienteRepository: ClienteRepository
) {

    suspend operator fun invoke(): Result<Unit> = resultadoDe {
        clienteRepository.sincronizar()
    }
}
