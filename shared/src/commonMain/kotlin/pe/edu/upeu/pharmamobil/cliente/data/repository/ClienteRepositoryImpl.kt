package pe.edu.upeu.pharmamobil.cliente.data.repository

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import pe.edu.upeu.pharmamobil.cliente.domain.model.Cliente
import pe.edu.upeu.pharmamobil.cliente.domain.repository.ClienteRepository

class ClienteRepositoryImpl : ClienteRepository {

    private val _clientes = MutableStateFlow(generarClientesIniciales())

    override suspend fun obtenerClientes(): List<Cliente> {
        delay(1000)
        return _clientes.value
    }

    override suspend fun registrarCliente(
        nombre: String,
        apellido: String,
        dni: String,
        telefono: String?,
        email: String?,
        direccion: String?
    ): Cliente {
        delay(1000)

        if (nombre.isBlank()) {
            throw IllegalArgumentException("El nombre no puede estar vacío")
        }
        if (apellido.isBlank()) {
            throw IllegalArgumentException("El apellido no puede estar vacío")
        }
        if (dni.isBlank()) {
            throw IllegalArgumentException("El DNI no puede estar vacío")
        }
        if (dni.length != 8) {
            throw IllegalArgumentException("El DNI debe tener 8 dígitos")
        }

        val dniDuplicado = _clientes.value.any { it.dni == dni }
        if (dniDuplicado) {
            throw IllegalArgumentException("Ya existe un cliente con el DNI: $dni")
        }

        val nuevoCliente = Cliente(
            id = "CLI-${kotlin.random.Random.nextLong(100, 999)}",
            nombre = nombre.trim(),
            apellido = apellido.trim(),
            dni = dni.trim(),
            telefono = telefono?.trim(),
            email = email?.trim(),
            direccion = direccion?.trim()
        )

        _clientes.update { lista ->
            lista + nuevoCliente
        }

        return nuevoCliente
    }

    override fun observarClientes(): Flow<List<Cliente>> {
        return _clientes.asStateFlow()
    }

    private fun generarClientesIniciales(): List<Cliente> {
        return listOf(
            Cliente(
                id = "CLI-001",
                nombre = "María",
                apellido = "García López",
                dni = "12345678",
                telefono = "987654321",
                email = "maria.garcia@email.com",
                direccion = "Av. Principal 123"
            ),
            Cliente(
                id = "CLI-002",
                nombre = "Juan",
                apellido = "Pérez Martínez",
                dni = "87654321",
                telefono = "912345678",
                email = "juan.perez@email.com",
                direccion = "Jr. Las Flores 456"
            ),
            Cliente(
                id = "CLI-003",
                nombre = "Ana",
                apellido = "Rodríguez Sánchez",
                dni = "11223344",
                telefono = null,
                email = "ana.rodriguez@email.com",
                direccion = null
            )
        )
    }
}
