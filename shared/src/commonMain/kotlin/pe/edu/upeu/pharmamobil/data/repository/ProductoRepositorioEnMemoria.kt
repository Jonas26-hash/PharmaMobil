package pe.edu.upeu.pharmamobil.data.repository

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import pe.edu.upeu.pharmamobil.domain.model.DetalleVenta
import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.model.Venta
import pe.edu.upeu.pharmamobil.domain.repository.ProductoRepository

/**
 * Almacenamiento en memoria de productos. El id lo asigna el repositorio,
 * no la pantalla, para evitar identificadores duplicados.
 *
 * El delay simula la latencia que traerá el backend REST, de modo que el
 * estado de carga de la pantalla sea visible desde ahora.
 *
 * Koin lo registra como single, así que es un objeto compartido y sus métodos
 * son suspend: nada garantiza que dos llamadas no se crucen. El [Mutex]
 * protege la lista y el contador de ids de esa carrera. Los cambios se
 * publican con un StateFlow para que la pantalla reaccione a ventas y
 * registros sin recargar a mano.
 */
class ProductoRepositorioEnMemoria : ProductoRepository {

    private val candado = Mutex()
    private val _productos = MutableStateFlow(generarProductosIniciales())
    private var siguienteId = _productos.value.size.toLong() + 1L

    override suspend fun registrar(producto: Producto): Producto {
        delay(RETARDO_REGISTRO_MS)
        return candado.withLock {
            val nombreDuplicado = _productos.value.any {
                it.nombre.equals(producto.nombre, ignoreCase = true)
            }
            if (nombreDuplicado) {
                throw IllegalArgumentException("Ya existe un producto con el nombre: ${producto.nombre}")
            }

            val guardado = producto.copy(id = siguienteId++)
            _productos.update { lista -> lista + guardado }
            guardado
        }
    }

    override suspend fun actualizar(producto: Producto): Producto {
        delay(RETARDO_REGISTRO_MS)
        return candado.withLock {
            val lista = _productos.value
            if (lista.none { it.id == producto.id }) {
                throw NoSuchElementException("Producto no encontrado con id: ${producto.id}")
            }
            val nombreDuplicado = lista.any {
                it.id != producto.id && it.nombre.equals(producto.nombre, ignoreCase = true)
            }
            if (nombreDuplicado) {
                throw IllegalArgumentException("Ya existe un producto con el nombre: ${producto.nombre}")
            }

            _productos.update { listaActual ->
                listaActual.map { if (it.id == producto.id) producto else it }
            }
            producto
        }
    }

    override suspend fun eliminar(productoId: Long) {
        delay(RETARDO_LISTADO_MS)
        candado.withLock {
            _productos.update { lista -> lista.filterNot { it.id == productoId } }
        }
    }

    override suspend fun listar(): List<Producto> {
        delay(RETARDO_LISTADO_MS)
        return candado.withLock {
            _productos.value
        }
    }

    override suspend fun obtenerPorId(productoId: Long): Producto {
        delay(RETARDO_LISTADO_MS)
        return candado.withLock {
            _productos.value.find { it.id == productoId }
                ?: throw NoSuchElementException("Producto no encontrado con id: $productoId")
        }
    }

    override suspend fun registrarVenta(clienteId: Long, productoId: Long, cantidad: Int): Venta {
        delay(RETARDO_VENTA_MS)

        if (cantidad <= 0) {
            throw IllegalArgumentException("La cantidad debe ser mayor a cero")
        }

        return candado.withLock {
            val producto = _productos.value.find { it.id == productoId }
                ?: throw NoSuchElementException("Producto no encontrado con id: $productoId")

            if (producto.stock < cantidad) {
                throw IllegalArgumentException(
                    "Stock insuficiente para ${producto.nombre}. Stock disponible: ${producto.stock}"
                )
            }

            _productos.update { lista ->
                lista.map {
                    if (it.id == productoId) it.copy(stock = it.stock - cantidad) else it
                }
            }

            val detalle = DetalleVenta.crear(
                productoId = productoId,
                nombreProducto = producto.nombre,
                cantidad = cantidad,
                precioUnitario = producto.precio
            )

            Venta.crear(
                id = "VTN-${kotlin.random.Random.nextLong(10000, 99999)}",
                fecha = "2026-08-18 08:00",
                items = listOf(detalle)
            )
        }
    }

    override fun observarProductos(): Flow<List<Producto>> {
        return _productos.asStateFlow()
    }

    private fun generarProductosIniciales(): List<Producto> {
        return listOf(
            Producto(
                id = 1L,
                nombre = "Paracetamol",
                descripcion = "Analgésico y antipirético. Alivia el dolor de cabeza y reduce la fiebre.",
                precio = 15.50,
                stock = 100,
                requiereReceta = false,
                categoria = "Analgésico"
            ),
            Producto(
                id = 2L,
                nombre = "Ibuprofeno",
                descripcion = "Antiinflamatorio no esteroideo. Reduce inflamación y dolor.",
                precio = 18.90,
                stock = 50,
                requiereReceta = false,
                categoria = "Antiinflamatorio"
            ),
            Producto(
                id = 3L,
                nombre = "Amoxicilina",
                descripcion = "Antibiótico de amplio espectro para infecciones bacterianas.",
                precio = 25.00,
                stock = 5,
                requiereReceta = true,
                categoria = "Antibiótico"
            ),
            Producto(
                id = 4L,
                nombre = "Loratadina",
                descripcion = "Antihistamínico para aliviar síntomas de alergia.",
                precio = 12.50,
                stock = 0,
                activo = false,
                requiereReceta = false,
                categoria = "Antihistamínico"
            ),
            Producto(
                id = 5L,
                nombre = "Diclofenaco",
                descripcion = "Antiinflamatorio para el alivio de dolores musculares y articulares.",
                precio = 20.00,
                stock = 3,
                requiereReceta = false,
                categoria = "Antiinflamatorio"
            ),
            Producto(
                id = 6L,
                nombre = "Omeprazol",
                descripcion = "Inhibidor de la bomba de protones para problemas gástricos.",
                precio = 5.75,
                stock = 18,
                requiereReceta = false,
                categoria = "Gastrointestinal"
            ),
            Producto(
                id = 7L,
                nombre = "Vitamina C",
                descripcion = "Suplemento vitamínico para reforzar el sistema inmunológico.",
                precio = 6.00,
                stock = 30,
                requiereReceta = false,
                categoria = "Suplemento"
            )
        )
    }

    private companion object {
        const val RETARDO_REGISTRO_MS = 400L
        const val RETARDO_LISTADO_MS = 600L
        const val RETARDO_VENTA_MS = 600L
    }
}