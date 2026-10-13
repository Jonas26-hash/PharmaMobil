package pe.edu.upeu.pharmamobil.domain.auth

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SesionTest {

    private val sesionAdmin = Sesion(
        token = "tok-a",
        usuario = "admin",
        nombreCompleto = "Ada Admin",
        permisos = setOf(
            Permisos.CATALOGO_LEER,
            Permisos.CATALOGO_ESCRIBIR,
            Permisos.CLIENTES_LEER,
            Permisos.CLIENTES_ESCRIBIR,
            Permisos.VENTAS_LEER,
            Permisos.VENTAS_REGISTRAR,
            Permisos.REPORTES_VER,
            Permisos.USUARIOS_ADMINISTRAR
        )
    )

    private val sesionAlmacen = Sesion(
        token = "tok-b",
        usuario = "almacen",
        nombreCompleto = "Alma Almacenera",
        permisos = setOf(
            Permisos.CATALOGO_LEER,
            Permisos.CATALOGO_ESCRIBIR
        )
    )

    private val sesionVendedor = Sesion(
        token = "tok-c",
        usuario = "vendedor",
        nombreCompleto = "Vera Vendedora",
        permisos = setOf(
            Permisos.CATALOGO_LEER,
            Permisos.CLIENTES_LEER,
            Permisos.CLIENTES_ESCRIBIR,
            Permisos.VENTAS_LEER,
            Permisos.VENTAS_REGISTRAR
        )
    )

    @Test
    fun adminPuedeHacerTodo() {
        assertTrue(sesionAdmin.puede(Permisos.CATALOGO_LEER))
        assertTrue(sesionAdmin.puede(Permisos.CATALOGO_ESCRIBIR))
        assertTrue(sesionAdmin.puede(Permisos.CLIENTES_LEER))
        assertTrue(sesionAdmin.puede(Permisos.VENTAS_REGISTRAR))
        assertTrue(sesionAdmin.puede(Permisos.REPORTES_VER))
    }

    @Test
    fun almacenPuedeProductosPeroNoClientesNiVentas() {
        assertTrue(sesionAlmacen.puede(Permisos.CATALOGO_LEER))
        assertTrue(sesionAlmacen.puede(Permisos.CATALOGO_ESCRIBIR))
        assertFalse(sesionAlmacen.puede(Permisos.CLIENTES_LEER))
        assertFalse(sesionAlmacen.puede(Permisos.VENTAS_REGISTRAR))
        assertFalse(sesionAlmacen.puede(Permisos.REPORTES_VER))
    }

    @Test
    fun vendedorNoRegistraProductosNiVeReportes() {
        assertFalse(sesionVendedor.puede(Permisos.CATALOGO_ESCRIBIR))
        assertFalse(sesionVendedor.puede(Permisos.REPORTES_VER))
        assertTrue(sesionVendedor.puede(Permisos.CLIENTES_LEER))
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
                    Permisos.CLIENTES_LEER,
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
        assertFalse(sesion.puede(Permisos.CATALOGO_LEER))
        assertFalse(sesion.puedeAlguno(setOf(Permisos.REPORTES_VER, Permisos.VENTAS_REGISTRAR)))
    }
}
