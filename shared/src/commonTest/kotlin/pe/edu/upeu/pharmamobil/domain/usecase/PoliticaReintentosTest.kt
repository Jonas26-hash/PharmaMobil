package pe.edu.upeu.pharmamobil.domain.usecase

import kotlin.test.Test
import kotlin.test.assertEquals
import pe.edu.upeu.pharmamobil.domain.ErrorApi

class PoliticaReintentosTest {
    @Test fun tiemposCrecenConTope() {
        assertEquals(listOf(1_000L, 2_000L, 4_000L, 8_000L, 16_000L, 16_000L),
            (0..5).map(::esperaReintentoMillis))
    }

    @Test fun clasificaErroresDeRedYValidacion() {
        assertEquals(TipoFalloSync.TRANSITORIO, ErrorApi.SinConexion("offline").tipoFalloSync())
        assertEquals(TipoFalloSync.TRANSITORIO, ErrorApi.Servidor("ocupado", 503).tipoFalloSync())
        assertEquals(TipoFalloSync.PERMANENTE, ErrorApi.Validacion("inválido").tipoFalloSync())
        assertEquals(TipoFalloSync.PERMANENTE, ErrorApi.Conflicto("duplicado").tipoFalloSync())
    }
}
