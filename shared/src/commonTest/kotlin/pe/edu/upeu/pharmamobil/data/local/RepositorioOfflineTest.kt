package pe.edu.upeu.pharmamobil.data.local

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import pe.edu.upeu.pharmamobil.data.connectivity.MonitorDeConectividadFalso
import pe.edu.upeu.pharmamobil.data.remote.ClienteApi
import pe.edu.upeu.pharmamobil.data.remote.ProductoApi
import pe.edu.upeu.pharmamobil.data.remote.VentaApi
import pe.edu.upeu.pharmamobil.data.remote.crearHttpClient
import pe.edu.upeu.pharmamobil.db.PharmaDb
import pe.edu.upeu.pharmamobil.domain.model.Cliente
import pe.edu.upeu.pharmamobil.domain.model.Producto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RepositorioOfflineTest {

    private val cabecerasJson = headersOf(HttpHeaders.ContentType, "application/json")

    private val paginaDeProductos = """
        {
          "contenido": [
            {
              "id": 1,
              "nombre": "Paracetamol 500mg",
              "precio": 8.5,
              "stock": 120,
              "estado": true,
              "categoriaId": 1,
              "categoriaNombre": "Analgesicos"
            },
            {
              "id": 21,
              "nombre": "Producto dado de baja",
              "precio": 99.9,
              "stock": 42,
              "estado": false,
              "categoriaId": 1,
              "categoriaNombre": "Analgesicos"
            }
          ]
        }
    """.trimIndent()

    private fun clienteHttp(
        cuerpo: String,
        estado: HttpStatusCode = HttpStatusCode.OK
    ): HttpClient = crearHttpClient(
        MockEngine { respond(cuerpo, estado, cabecerasJson) }
    )

    private fun repositorioDeProductos(
        db: PharmaDb,
        monitor: MonitorDeConectividadFalso,
        cliente: HttpClient
    ): ProductoRepositorioOffline = ProductoRepositorioOffline(
        local = ProductoLocal(db),
        remoto = ProductoApi(cliente),
        ventaApi = VentaApi(cliente),
        conectividad = monitor
    )

    @Test
    fun cuandoHayRedGuardaLaCopiaLocalYListaSinRed() = runTest {
        val db = crearBaseDeDatosDePrueba()
        val monitor = MonitorDeConectividadFalso(conectadoInicial = true)
        val repositorio = repositorioDeProductos(db, monitor, clienteHttp(paginaDeProductos))

        val enLinea = repositorio.listar()

        assertEquals(1, enLinea.size)
        assertEquals("Paracetamol 500mg", enLinea.first().nombre)

        monitor.cambiar(false)
        val sinRed = repositorio.listar()

        assertEquals(1, sinRed.size)
        assertEquals("Paracetamol 500mg", sinRed.first().nombre)
        assertEquals(1L, sinRed.first().id)
    }

    @Test
    fun siElServidorFallaDevuelveLaCopiaLocal() = runTest {
        val db = crearBaseDeDatosDePrueba()
        val monitor = MonitorDeConectividadFalso(conectadoInicial = true)
        repositorioDeProductos(db, monitor, clienteHttp(paginaDeProductos)).listar()

        val servidorCaido = repositorioDeProductos(
            db,
            monitor,
            clienteHttp("{}", HttpStatusCode.InternalServerError)
        )

        val productos = servidorCaido.listar()

        assertEquals(1, productos.size)
        assertEquals("Paracetamol 500mg", productos.first().nombre)
    }

    @Test
    fun sinRedRegistraLocalmenteConIdNegativoYQuedaPendiente() = runTest {
        val db = crearBaseDeDatosDePrueba()
        val monitor = MonitorDeConectividadFalso(conectadoInicial = false)
        val repositorio = repositorioDeProductos(db, monitor, clienteHttp(paginaDeProductos))
        val local = ProductoLocal(db)

        val creado = repositorio.registrar(
            Producto(
                id = 0,
                nombre = "Ibuprofeno 400mg",
                precio = 5.0,
                stock = 10,
                categoriaId = 1
            )
        )

        assertEquals(-1L, creado.id)
        assertEquals(listOf(-1L), local.pendientes().map { it.id })
        assertEquals(listOf(-1L), repositorio.listar().map { it.id })
    }

    @Test
    fun observarEmiteLosCambiosLocales() = runTest {
        val db = crearBaseDeDatosDePrueba()
        val monitor = MonitorDeConectividadFalso(conectadoInicial = false)
        val repositorio = repositorioDeProductos(db, monitor, clienteHttp(paginaDeProductos))

        repositorio.registrar(
            Producto(
                id = 0,
                nombre = "Jarabe para la tos",
                precio = 7.5,
                stock = 20,
                categoriaId = 1
            )
        )

        val productos = repositorio.observar().first()

        assertEquals(1, productos.size)
        assertEquals("Jarabe para la tos", productos.first().nombre)
    }

    @Test
    fun sincronizarSubeLosPendientesYLosReemplaza() = runTest {
        val db = crearBaseDeDatosDePrueba()
        val monitor = MonitorDeConectividadFalso(conectadoInicial = false)
        val repositorio = repositorioDeProductos(db, monitor, clienteHttp(paginaDeProductos))
        val local = ProductoLocal(db)

        val localCreado = repositorio.registrar(
            Producto(
                id = 0,
                nombre = "Ibuprofeno 400mg",
                precio = 5.0,
                stock = 10,
                categoriaId = 1
            )
        )
        assertEquals(-1L, localCreado.id)
        assertEquals(1, local.pendientes().size)

        val productoSubido = """
            {
              "id": 50,
              "nombre": "Ibuprofeno 400mg",
              "precio": 5.0,
              "stock": 10,
              "estado": true,
              "categoriaId": 1,
              "categoriaNombre": "Analgesicos"
            }
        """.trimIndent()
        val paginaConSubido = """
            {
              "contenido": [
                {
                  "id": 50,
                  "nombre": "Ibuprofeno 400mg",
                  "precio": 5.0,
                  "stock": 10,
                  "estado": true,
                  "categoriaId": 1,
                  "categoriaNombre": "Analgesicos"
                }
              ]
            }
        """.trimIndent()
        val clienteMixto = crearHttpClient(
            MockEngine { solicitud ->
                val cuerpo = if (solicitud.method == HttpMethod.Post) {
                    productoSubido
                } else {
                    paginaConSubido
                }
                respond(cuerpo, HttpStatusCode.OK, cabecerasJson)
            }
        )
        val repositorioConRed = repositorioDeProductos(db, monitor, clienteMixto)
        monitor.cambiar(true)

        repositorioConRed.sincronizar()

        assertTrue(local.pendientes().isEmpty())
        assertEquals(50L, local.buscarPorId(50L)?.id)
        assertEquals(null, local.buscarPorId(-1L))
    }

    @Test
    fun sinRedRegistraClientesConIdNegativo() = runTest {
        val db = crearBaseDeDatosDePrueba()
        val monitor = MonitorDeConectividadFalso(conectadoInicial = false)
        val cliente = clienteHttp("""{"contenido":[]}""")
        val repositorio = ClienteRepositorioOffline(
            local = ClienteLocal(db),
            remoto = ClienteApi(cliente),
            conectividad = monitor
        )

        val creado = repositorio.registrar(
            Cliente(
                id = 0,
                nombre = "Ana",
                apellido = "Perez",
                dni = "12345678"
            )
        )

        assertEquals(-1L, creado.id)
        assertEquals(listOf(-1L), repositorio.listar().map { it.id })
        assertEquals(1, ClienteLocal(db).pendientes().size)
    }
}
