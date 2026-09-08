package pe.edu.upeu.pharmamobil.domain.usecase

import pe.edu.upeu.pharmamobil.domain.model.Cliente
import pe.edu.upeu.pharmamobil.domain.repository.ClienteRepository

class ListarClientesUseCase(
    private val clienteRepository: ClienteRepository
) {

    suspend operator fun invoke(): Result<List<Cliente>> = resultadoDe {
        clienteRepository.listar()
    }
}