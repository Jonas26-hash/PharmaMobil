package pe.edu.upeu.pharmamobil.domain.auth

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * La sesión concentra la consulta de permisos: la interfaz oculta acciones con
 * [Sesion.puede] / [Sesion.puedeAlguno] y nunca compara roles a mano.
 *
 * Los nombres de permisos son el contrato esperado de la guía (ver
 * [Permisos]); estas pruebas verifican la semántica de la consulta, no el
 * catálogo.
 */
class SesionTest {

    private val sesionAdmin = Sesion(
        token = "tok-a",
        usuario = "admin",
        nombreCompleto = "Ada Admin",
        permisos = setOf(
            Permisos.PRODUCTOS_LISTAR,
            Permisos.PRODUCTOS_REGISTRAR,
            Permisos.CLIENTES_LISTAR,
            Permisos.VENTAS_REGISTRAR,
            Permisos.REPORTES_VER
        )
    )

    private val sesionAlmacen = Sesion(
        token = "tok-b",
        usuario = "almacen",
        nombreCompleto = "Alma Almacenera",
        permisos = setOf(
            Permisos.PRODUCTOS_LISTAR,
            Permisos.PRODUCTOS_REGISTRAR
        )
    )

    private val sesionVendedor = Sesion(
        token = "tok-c",
        usuario = "vendedor",
        nombreCompleto = "Vera Vendedora",
        permisos = setOf(
            Permisos.CLIENTES_LISTAR,
            Permisos.VENTAS_REGISTRAR
        )
    )

    @Test
    fun adminPuedeHacerTodo() {
        assertTrue(sesionAdmin.puede(Permisos.PRODUCTOS_LISTAR))
        assertTrue(sesionAdmin.puede(Permisos.PRODUCTOS_REGISTRAR))
        assertTrue(sesionAdmin.puede(Permisos.CLIENTES_LISTAR))
        assertTrue(sesionAdmin.puede(Permisos.VENTAS_REGISTRAR))
        assertTrue(sesionAdmin.puede(Permisos.REPORTES_VER))
    }

    @Test
    fun almacenPuedeProductosPeroNoClientesNiVentas() {
        assertTrue(sesionAlmacen.puede(Permisos.PRODUCTOS_LISTAR))
        assertTrue(sesionAlmacen.puede(Permisos.PRODUCTOS_REGISTRAR))
        assertFalse(sesionAlmacen.puede(Permisos.CLIENTES_LISTAR))
        assertFalse(sesionAlmacen.puede(Permisos.VENTAS_REGISTRAR))
        assertFalse(sesionAlmacen.puede(Permisos.REPORTES_VER))
    }

    @Test
    fun vendedorNoRegistraProductosNiVeReportes() {
        assertFalse(sesionVendedor.puede(Permisos.PRODUCTOS_REGISTRAR))
        assertFalse(sesionVendedor.puede(Permisos.REPORTES_VER))
        assertTrue(sesionVendedor.puede(Permisos.CLIENTES_LISTAR))
    }

    @Test
    fun puedeAlgunoEsVerdadConAlMenosUnPermiso() {
        assertTrue(
            sesionVendedor.puedeAlguno(
                listOf(
                    Permisos.REPORTES_VER,
                    Permisos.VENTAS_REGISTRAR
                )
            )
        )
    }

    @Test
    fun puedeAlgunoEsFalsoSinNingunPermiso() {
        assertFalse(
            sesionAlmacen.puedeAlguno(
                listOf(
                    Permisos.CLIENTES_LISTAR,
                    Permisos.VENTAS_REGISTRAR
                )
            )
        )
    }

    @Test
    fun unPermisoDesconocidoNuncaEsTrue() {
        assertFalse(sesionAdmin.puede("reportes:eliminar"))
    }

    @Test
    fun sinPermisosLaSesionNoPuedeNada() {
        val sesion = Sesion(
            token = "tok-d",
            usuario = "anonimo",
            nombreCompleto = ""
        )
        assertFalse(sesion.puede(Permisos.PRODUCTOS_LISTAR))
        assertFalse(sesion.puedeAlguno(setOf(Permisos.REPORTES_VER, Permisos.VENTAS_REGISTRAR)))
    }
}