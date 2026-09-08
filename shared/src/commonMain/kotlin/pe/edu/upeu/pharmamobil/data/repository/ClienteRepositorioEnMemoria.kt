package pe.edu.upeu.pharmamobil.data.repository

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import pe.edu.upeu.pharmamobil.domain.model.Cliente
import pe.edu.upeu.pharmamobil.domain.repository.ClienteRepository

/**
 * Almacenamiento en memoria de clientes, gemelo de [ProductoRepositorioEnMemoria].
 */
class ClienteRepositorioEnMemoria : ClienteRepository {

    private val candado = Mutex()
    private val _clientes = MutableStateFlow(generarClientesIniciales())
    private var siguienteId = _clientes.value.size.toLong() + 1L

    override suspend fun registrar(cliente: Cliente): Cliente {
        delay(RETARDO_REGISTRO_MS)
        return candado.withLock {
            val dniDuplicado = _clientes.value.any { it.dni == cliente.dni }
            if (dniDuplicado) {
                throw IllegalArgumentException("Ya existe un cliente con el DNI: ${cliente.dni}")
            }

            val guardado = cliente.copy(id = siguienteId++)
            _clientes.update { lista -> lista + guardado }
            guardado
        }
    }

    override suspend fun listar(): List<Cliente> {
        delay(RETARDO_LISTADO_MS)
        return candado.withLock {
            _clientes.value
        }
    }

    override fun observarClientes(): Flow<List<Cliente>> {
        return _clientes.asStateFlow()
    }

    private fun generarClientesIniciales(): List<Cliente> {
        return listOf(
            Cliente(
                id = 1L,
                nombre = "María",
                apellido = "García López",
                dni = "12345678",
                telefono = "987654321",
                email = "maria.garcia@email.com",
                direccion = "Av. Principal 123"
            ),
            Cliente(
                id = 2L,
                nombre = "Juan",
                apellido = "Pérez Martínez",
                dni = "87654321",
                telefono = "912345678",
                email = "juan.perez@email.com",
                direccion = "Jr. Las Flores 456"
            ),
            Cliente(
                id = 3L,
                nombre = "Ana",
                apellido = "Rodríguez Sánchez",
                dni = "11223344",
                telefono = null,
                email = "ana.rodriguez@email.com",
                direccion = null
            )
        )
    }

    private companion object {
        const val RETARDO_REGISTRO_MS = 400L
        const val RETARDO_LISTADO_MS = 600L
    }
}