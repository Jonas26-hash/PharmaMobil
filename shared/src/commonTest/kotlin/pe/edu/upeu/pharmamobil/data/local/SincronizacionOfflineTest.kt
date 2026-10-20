package pe.edu.upeu.pharmamobil.data.local

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.utils.io.errors.IOException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import pe.edu.upeu.pharmamobil.data.connectivity.MonitorDeConectividadFalso
import pe.edu.upeu.pharmamobil.data.remote.ProductoApi
import pe.edu.upeu.pharmamobil.data.remote.VentaApi
import pe.edu.upeu.pharmamobil.data.remote.crearHttpClient
import pe.edu.upeu.pharmamobil.db.PharmaDb
import pe.edu.upeu.pharmamobil.domain.ErrorApi
import pe.edu.upeu.pharmamobil.domain.ErrorDeApi
import pe.edu.upeu.pharmamobil.domain.model.Producto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SincronizacionOfflineTest {

    private val cabecerasJson = headersOf(HttpHeaders.ContentType, "application/json")

    private fun itemRemoto(
        id: Long,
        nombre: String,
        stock: Int = 10,
        activo: Boolean = true
    ): String = """
        {
          "id": $id,
          "nombre": "$nombre",
          "precio": 8.5,
          "stock": $stock,
          "estado": $activo,
          "categoriaId": 1,
          "categoriaNombre": "Analgesicos"
        }
    """.trimIndent()

    private fun pagina(vararg items: String): String =
        """{"contenido":[${items.joinToString(",")}]}"""

    private val servidorConUnProducto =
        pagina(itemRemoto(id = 7L, nombre = "Paracetamol 500mg", stock = 120))

    private fun clienteQueResponde(cuerpo: String): HttpClient = crearHttpClient(
        MockEngine { respond(cuerpo, HttpStatusCode.OK, cabecerasJson) }
    )

    private fun clienteSinRed(): HttpClient = crearHttpClient(
        MockEngine { throw IOException("Sin conexión") }
    )

    private fun repositorio(
        db: PharmaDb,
        monitor: MonitorDeConectividadFalso,
        cliente: HttpClient,
        registro: RegistroDeSincronizacionFalso = RegistroDeSincronizacionFalso()
    ): ProductoRepositorioOffline = ProductoRepositorioOffline(
        local = ProductoLocal(db),
        remoto = ProductoApi(cliente),
        ventaApi = VentaApi(cliente),
        conectividad = monitor,
        registroSincronizacion = registro
    )

    private fun productoNuevo(nombre: String = "Ibuprofeno 400mg"): Producto = Producto(
        id = 0L,
        nombre = nombre,
        precio = 5.0,
        stock = 10,
        categoriaId = 1L
    )

    @Test
    fun uno_conRedLaBaseQuedaConLosProductosDelServidorYElFlujoLosEmite() = runTest {

        val db = crearBaseDeDatosDePrueba()
        val monitor = MonitorDeConectividadFalso(conectadoInicial = true)
        val repositorio = repositorio(db, monitor, clienteQueResponde(servidorConUnProducto))

        repositorio.sincronizar()

        val locales = ProductoLocal(db).todas()
        assertEquals(listOf(7L), locales.map { it.id })
        assertEquals("Paracetamol 500mg", locales.first().nombre)
        assertEquals(120, locales.first().stock)

        val emitidos = repositorio.observar().first()
        assertEquals(listOf(7L), emitidos.map { it.id })
    }

    @Test
    fun dos_registrarSinRedGuardaLaMarcaPendienteYElFlujoLaEmite() = runTest {

        val db = crearBaseDeDatosDePrueba()
        val monitor = MonitorDeConectividadFalso(conectadoInicial = false)
        val repositorio = repositorio(db, monitor, clienteSinRed())
        val local = ProductoLocal(db)

        val creado = repositorio.registrar(productoNuevo())

        assertEquals(-1L, creado.id)
        assertTrue(creado.pendiente)

        val pendiente = local.pendientes().single()
        assertEquals(-1L, pendiente.id)
        assertTrue(pendiente.pendiente)

        val emitidos = repositorio.observar().first()
        assertEquals(listOf(-1L), emitidos.map { it.id })
        assertTrue(emitidos.first().pendiente)
    }

    @Test
    fun tres_alReconectarSeSubeElPendienteYLaFilaDejaDeEstarMarcada() = runTest {

        val db = crearBaseDeDatosDePrueba()
        val monitor = MonitorDeConectividadFalso(conectadoInicial = false)
        val repositorioSinRed = repositorio(db, monitor, clienteSinRed())

        repositorioSinRed.registrar(productoNuevo())
        assertEquals(1, ProductoLocal(db).pendientes().size)

        val idSubido = 50L
        val clienteMixto = crearHttpClient(
            MockEngine { solicitud ->
                val cuerpo = if (solicitud.method == HttpMethod.Post) {
                    itemRemoto(id = idSubido, nombre = "Ibuprofeno 400mg", stock = 10)
                } else {
                    pagina(itemRemoto(id = idSubido, nombre = "Ibuprofeno 400mg", stock = 10))
                }
                respond(cuerpo, HttpStatusCode.OK, cabecerasJson)
            }
        )
        val conRed = repositorio(db, monitor, clienteMixto)
        monitor.cambiar(true)

        conRed.sincronizar()

        val local = ProductoLocal(db)
        assertTrue(local.pendientes().isEmpty())
        assertEquals(idSubido, local.buscarPorId(idSubido)?.id)
        assertNull(local.buscarPorId(-1L))
        assertTrue(local.todas().none { it.pendiente })
    }

    @Test
    fun cuatro_laBajadaNoBorraLoQueSiguePendiente() = runTest {

        val db = crearBaseDeDatosDePrueba()
        val monitor = MonitorDeConectividadFalso(conectadoInicial = false)
        val repositorioSinRed = repositorio(db, monitor, clienteSinRed())

        repositorioSinRed.registrar(productoNuevo("Ibuprofeno 400mg"))
        assertEquals(listOf(-1L), ProductoLocal(db).pendientes().map { it.id })

        val clienteConCatalogo = clienteQueResponde(
            pagina(itemRemoto(id = 1L, nombre = "Paracetamol 500mg"), itemRemoto(id = 2L, nombre = "Amoxicilina"))
        )
        val conRed = repositorio(db, monitor, clienteConCatalogo)
        monitor.cambiar(true)

        assertFailsWith<ErrorDeApi> { conRed.sincronizar() }

        val local = ProductoLocal(db)
        assertEquals(listOf(-1L), local.pendientes().map { it.id })
        assertNotNull(local.buscarPorId(1L))
        assertNotNull(local.buscarPorId(2L))
    }

    @Test
    fun cinco_losIdentificadoresLocalesYDelServidorNoColisionan() = runTest {

        val db = crearBaseDeDatosDePrueba()
        val monitor = MonitorDeConectividadFalso(conectadoInicial = false)
        val repositorioSinRed = repositorio(db, monitor, clienteSinRed())

        repositorioSinRed.registrar(productoNuevo("Nuevo local"))
        repositorioSinRed.registrar(productoNuevo("Otro nuevo local"))
        assertEquals(listOf(-2L, -1L), ProductoLocal(db).pendientes().map { it.id }.sorted())

        val clienteConCatalogo = clienteQueResponde(
            pagina(itemRemoto(id = 1L, nombre = "Paracetamol 500mg"), itemRemoto(id = 2L, nombre = "Amoxicilina"))
        )
        val conRed = repositorio(db, monitor, clienteConCatalogo)
        monitor.cambiar(true)

        runCatching { conRed.sincronizar() }

        val ids = ProductoLocal(db).todas().map { it.id }
        assertTrue(ids.contains(-1L))
        assertTrue(ids.contains(-2L))
        assertTrue(ids.contains(1L))
        assertTrue(ids.contains(2L))
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun seis_sincronizarDosVecesNoDuplicaLosProductos() = runTest {

        val db = crearBaseDeDatosDePrueba()
        val monitor = MonitorDeConectividadFalso(conectadoInicial = true)
        val repositorio = repositorio(db, monitor, clienteQueResponde(servidorConUnProducto))
        val local = ProductoLocal(db)

        repositorio.sincronizar()
        val trasLaPrimera = local.todas().size

        repositorio.sincronizar()

        assertEquals(1, trasLaPrimera)
        assertEquals(1, local.todas().size)
        assertEquals(listOf(7L), local.todas().map { it.id })
    }

    @Test
    fun laUltimaSincronizacionSeSellaSoloTrasUnExitoConRed() = runTest {

        val db = crearBaseDeDatosDePrueba()
        val monitor = MonitorDeConectividadFalso(conectadoInicial = false)
        val registro = RegistroDeSincronizacionFalso()
        val sinRed = repositorio(db, monitor, clienteSinRed(), registro)

        assertFailsWith<ErrorDeApi> { sinRed.sincronizar() }
        assertNull(registro.ultimaSincronizacion())

        val conRed = repositorio(db, monitor, clienteQueResponde(servidorConUnProducto), registro)
        monitor.cambiar(true)

        conRed.sincronizar()

        assertNotNull(registro.ultimaSincronizacion())
        assertTrue(registro.ultimaSincronizacion()!! > 0L)
    }

    @Test
    fun confirmaQueElFalloDeSubidaSeReportaComoErrorDeApi() = runTest {

        val db = crearBaseDeDatosDePrueba()
        val monitor = MonitorDeConectividadFalso(conectadoInicial = false)
        repositorio(db, monitor, clienteSinRed()).registrar(productoNuevo())

        val conRed = repositorio(db, monitor, clienteSinRed())
        monitor.cambiar(true)

        val fallo = assertFailsWith<ErrorDeApi> { conRed.sincronizar() }

        assertTrue(fallo.error is ErrorApi.SinConexion)
    }
}
