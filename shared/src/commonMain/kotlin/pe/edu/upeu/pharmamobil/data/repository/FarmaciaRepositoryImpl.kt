package pe.edu.upeu.pharmamobil.data.repository

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import pe.edu.upeu.pharmamobil.domain.model.DetalleVenta
import pe.edu.upeu.pharmamobil.domain.model.Medicamento
import pe.edu.upeu.pharmamobil.domain.model.Venta
import pe.edu.upeu.pharmamobil.domain.repository.FarmaciaRepository

class FarmaciaRepositoryImpl : FarmaciaRepository {

    private val _medicamentos = MutableStateFlow(generarMedicamentosIniciales())

    override suspend fun obtenerMedicamentos(): List<Medicamento> {
        delay(1500)
        return _medicamentos.value
    }

    override suspend fun buscarMedicamento(id: String): Medicamento? {
        delay(500)
        return _medicamentos.value.find { it.id == id }
    }

    override suspend fun registrarVenta(medicamentoId: String, cantidad: Int): Venta {
        delay(1000)

        if (cantidad <= 0) {
            throw IllegalArgumentException("La cantidad debe ser mayor a cero")
        }

        val medicamento = _medicamentos.value.find { it.id == medicamentoId }
            ?: throw NoSuchElementException("Medicamento no encontrado con id: $medicamentoId")

        if (medicamento.stock < cantidad) {
            throw IllegalArgumentException(
                "Stock insuficiente para ${medicamento.nombre}. Stock disponible: ${medicamento.stock}"
            )
        }

        val medicamentoActualizado = medicamento.copy(
            stock = medicamento.stock - cantidad
        )

        _medicamentos.update { lista ->
            lista.map { if (it.id == medicamentoId) medicamentoActualizado else it }
        }

        val detalle = DetalleVenta.crear(
            medicamentoId = medicamentoId,
            nombreMedicamento = medicamento.nombre,
            cantidad = cantidad,
            precioUnitario = medicamento.precio
        )

        return Venta.crear(
            id = "VTN-${kotlin.random.Random.nextLong(10000, 99999)}",
            fecha = obtenerFechaActual(),
            items = listOf(detalle)
        )
    }

    override fun observarMedicamentos(): Flow<List<Medicamento>> {
        return _medicamentos.asStateFlow()
    }

    private fun obtenerFechaActual(): String {
        return "2026-08-18 08:00"
    }

    private fun generarMedicamentosIniciales(): List<Medicamento> {
        return listOf(
            Medicamento(
                id = "MED-001",
                nombre = "Paracetamol",
                descripcion = "Analgésico y antipirético. Alivia el dolor de cabeza y reduce la fiebre.",
                precio = 2.50,
                stock = 20,
                requiereReceta = false,
                categoria = "Analgésico"
            ),
            Medicamento(
                id = "MED-002",
                nombre = "Ibuprofeno",
                descripcion = "Antiinflamatorio no esteroideo. Reduce inflamación y dolor.",
                precio = 3.25,
                stock = 15,
                requiereReceta = false,
                categoria = "Antiinflamatorio"
            ),
            Medicamento(
                id = "MED-003",
                nombre = "Amoxicilina",
                descripcion = "Antibiótico de amplio espectro para infecciones bacterianas.",
                precio = 8.50,
                stock = 10,
                requiereReceta = true,
                categoria = "Antibiótico"
            ),
            Medicamento(
                id = "MED-004",
                nombre = "Loratadina",
                descripcion = "Antihistamínico para aliviar síntomas de alergia.",
                precio = 4.00,
                stock = 25,
                requiereReceta = false,
                categoria = "Antihistamínico"
            ),
            Medicamento(
                id = "MED-005",
                nombre = "Omeprazol",
                descripcion = "Inhibidor de la bomba de protones para problemas gástricos.",
                precio = 5.75,
                stock = 18,
                requiereReceta = false,
                categoria = "Gastrointestinal"
            ),
            Medicamento(
                id = "MED-006",
                nombre = "Vitamina C",
                descripcion = "Suplemento vitamínico para reforzar el sistema inmunológico.",
                precio = 6.00,
                stock = 30,
                requiereReceta = false,
                categoria = "Suplemento"
            )
        )
    }
}
