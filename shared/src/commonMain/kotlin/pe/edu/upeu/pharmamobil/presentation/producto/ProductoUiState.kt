package pe.edu.upeu.pharmamobil.presentation.producto

import pe.edu.upeu.pharmamobil.domain.ErrorApi

/**
 * Estado del inventario.
 *
 * El estado separa deliberadamente dos ejes que antes iban juntos:
 *
 *  - [fase] describe **qué se está mostrando** y es excluyente: solo una de
 *    las cuatro puede estar activa.
 *  - [operacion] describe **qué está haciendo el usuario** sobre los datos y
 *    es independiente de la fase. Gracias a esa separación, guardar o borrar no
 *    tira la lista: la pantalla puede seguir mostrando los productos y solo
 *    marcar la fila o el botón como ocupado.
 */
data class ProductoUiState(
    val fase: Fase = Fase.Cargando,
    val operacion: Operacion = Operacion.Inactiva,
    val busqueda: String = ""
) {

    /** Contenido visible de la pantalla. Solo una rama puede estar activa. */
    sealed interface Fase {

        data object Cargando : Fase

        data object SinProductos : Fase

        data class ConProductos(val productos: List<ProductoUi>) : Fase

        data class Error(val mensaje: String) : Fase
    }

    /**
     * Mutación en curso sobre el inventario.
     *
     * [Inactiva] es el reposo. [EnCurso] ocupa los controles mientras espera al
     * backend. [Fallida] conserva el [ErrorApi] para que la interfaz pueda
     * mostrar el mensaje y, si procede, marcar cada campo del formulario.
     */
    sealed interface Operacion {

        data object Inactiva : Operacion

        data class EnCurso(val tipo: TipoOperacion) : Operacion

        data class Fallida(
            val tipo: TipoOperacion,
            val error: ErrorApi,
            val mensaje: String
        ) : Operacion
    }

    /** Qué mutación originó el estado de operación. */
    enum class TipoOperacion { Crear, Actualizar, Eliminar }

    /** `true` mientras hay una mutación esperando respuesta. */
    val hayOperacionEnCurso: Boolean
        get() = operacion is Operacion.EnCurso

    /** `true` si hay una operación que terminó en error. */
    val hayOperacionFallida: Boolean
        get() = operacion is Operacion.Fallida

    /** Aplica el filtro de texto sobre la lista cargada. */
    fun filtrarPorBusqueda(): List<ProductoUi> {
        return (fase as? Fase.ConProductos)?.productos.orEmpty()
            .filter { it.nombre.contains(busqueda.trim(), ignoreCase = true) }
    }
}