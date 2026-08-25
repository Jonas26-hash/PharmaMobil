package pe.edu.upeu.pharmamobil.producto.domain.repository

import kotlinx.coroutines.flow.Flow
import pe.edu.upeu.pharmamobil.producto.domain.model.Medicamento
import pe.edu.upeu.pharmamobil.producto.domain.model.Venta

interface ProductoRepository {

    suspend fun obtenerMedicamentos(): List<Medicamento>

    suspend fun buscarMedicamento(id: String): Medicamento?

    suspend fun registrarVenta(medicamentoId: String, cantidad: Int): Venta

    suspend fun registrarMedicamento(
        nombre: String,
        descripcion: String?,
        precio: Double,
        stock: Int,
        requiereReceta: Boolean,
        categoria: String
    ): Medicamento

    fun observarMedicamentos(): Flow<List<Medicamento>>
}
