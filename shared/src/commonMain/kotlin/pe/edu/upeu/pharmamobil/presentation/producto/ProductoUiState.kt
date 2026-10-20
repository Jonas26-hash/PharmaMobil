package pe.edu.upeu.pharmamobil.presentation.producto

import pe.edu.upeu.pharmamobil.domain.ErrorApi

data class ProductoUiState(
    val fase: Fase = Fase.Cargando,
    val operacion: Operacion = Operacion.Inactiva,
    val busqueda: String = "",
    val sincronizando: Boolean = false,
    val avisoRed: String? = null
) {

    sealed interface Fase {

        data object Cargando : Fase

        data object SinProductos : Fase

        data class ConProductos(val productos: List<ProductoUi>) : Fase

        data class Error(val mensaje: String) : Fase
    }

    sealed interface Operacion {

        data object Inactiva : Operacion

        data class EnCurso(val tipo: TipoOperacion) : Operacion

        data class Fallida(
            val tipo: TipoOperacion,
            val error: ErrorApi,
            val mensaje: String
        ) : Operacion
    }

    enum class TipoOperacion { Crear, Actualizar, Eliminar }

    val hayOperacionEnCurso: Boolean
        get() = operacion is Operacion.EnCurso

    val hayOperacionFallida: Boolean
        get() = operacion is Operacion.Fallida

    fun filtrarPorBusqueda(): List<ProductoUi> {
        return (fase as? Fase.ConProductos)?.productos.orEmpty()
            .filter { it.nombre.contains(busqueda.trim(), ignoreCase = true) }
    }
}
