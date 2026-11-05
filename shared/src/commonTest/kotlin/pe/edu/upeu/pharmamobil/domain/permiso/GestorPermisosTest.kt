package pe.edu.upeu.pharmamobil.domain.permiso

import kotlin.test.Test
import kotlin.test.assertEquals

private class GestorFalso : GestorPermisos {
    var actual: EstadoPermiso = EstadoPermiso.Denegado
    override suspend fun estado(permiso: Permiso) = actual
    override suspend fun solicitar(permiso: Permiso) = actual
    override fun abrirAjustes() = Unit
}

class GestorPermisosTest {
    @Test
    fun falsoPermiteModelarLosTresEstadosDelSistema() {
        val gestor = GestorFalso()
        gestor.actual = EstadoPermiso.Denegado
        assertEquals(EstadoPermiso.Denegado, gestor.actual)
        gestor.actual = EstadoPermiso.DenegadoParaSiempre
        assertEquals(EstadoPermiso.DenegadoParaSiempre, gestor.actual)
        gestor.actual = EstadoPermiso.Concedido
        assertEquals(EstadoPermiso.Concedido, gestor.actual)
    }
}
