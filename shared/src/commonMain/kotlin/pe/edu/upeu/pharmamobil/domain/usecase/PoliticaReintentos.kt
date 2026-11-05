package pe.edu.upeu.pharmamobil.domain.usecase

import pe.edu.upeu.pharmamobil.domain.ErrorApi

enum class TipoFalloSync { TRANSITORIO, PERMANENTE }

fun ErrorApi.tipoFalloSync(): TipoFalloSync = when (this) {
    is ErrorApi.SinConexion, is ErrorApi.TiempoAgotado -> TipoFalloSync.TRANSITORIO
    is ErrorApi.Servidor -> if ((codigo ?: 500) >= 500) TipoFalloSync.TRANSITORIO else TipoFalloSync.PERMANENTE
    is ErrorApi.NoAutorizado, is ErrorApi.SesionExpirada,
    is ErrorApi.Validacion, is ErrorApi.NoEncontrado, is ErrorApi.Conflicto,
    is ErrorApi.Prohibido, is ErrorApi.CredencialesInvalidas -> TipoFalloSync.PERMANENTE
}

fun esperaReintentoMillis(intentosPrevios: Int): Long =
    1_000L shl intentosPrevios.coerceIn(0, 4)
