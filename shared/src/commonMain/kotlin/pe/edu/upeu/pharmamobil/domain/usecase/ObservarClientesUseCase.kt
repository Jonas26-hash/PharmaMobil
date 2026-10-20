package pe.edu.upeu.pharmamobil.domain.usecase

import kotlinx.coroutines.flow.Flow
import pe.edu.upeu.pharmamobil.domain.model.Cliente
import pe.edu.upeu.pharmamobil.domain.repository.ClienteRepository

class ObservarClientesUseCase(
    private val clienteRepository: ClienteRepository
) {

    operator fun invoke(): Flow<List<Cliente>> = clienteRepository.observar()
}
