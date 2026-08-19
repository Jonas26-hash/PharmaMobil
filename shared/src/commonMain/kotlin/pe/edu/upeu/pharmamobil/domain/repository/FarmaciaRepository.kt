package pe.edu.upeu.pharmamobil.domain.repository

import kotlinx.coroutines.flow.Flow
import pe.edu.upeu.pharmamobil.domain.model.Medicamento
import pe.edu.upeu.pharmamobil.domain.model.Venta

interface FarmaciaRepository {

    suspend fun obtenerMedicamentos(): List<Medicamento>

    suspend fun buscarMedicamento(id: String): Medicamento?

    suspend fun registrarVenta(medicamentoId: String, cantidad: Int): Venta

    fun observarMedicamentos(): Flow<List<Medicamento>>
}
