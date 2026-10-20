package pe.edu.upeu.pharmamobil.data.local

import pe.edu.upeu.pharmamobil.db.PharmaDb
import pe.edu.upeu.pharmamobil.domain.model.Categoria

class CategoriaLocal(base: PharmaDb) {

    private val consultas = base.categoriaQueries

    fun todas(): List<Categoria> =
        consultas.listarTodas().executeAsList().map { it.aDominio() }

    fun reemplazarTodo(categorias: List<Categoria>) {
        consultas.transaction {
            consultas.borrarTodo()
            categorias.forEach { categoria ->
                consultas.insertar(
                    id = categoria.id,
                    nombre = categoria.nombre,
                    descripcion = categoria.descripcion,
                    activo = if (categoria.activo) 1L else 0L
                )
            }
        }
    }

    fun contar(): Long = consultas.contar().executeAsOne()
}

internal fun pe.edu.upeu.pharmamobil.db.Categoria.aDominio(): Categoria = Categoria(
    id = id,
    nombre = nombre,
    descripcion = descripcion,
    activo = activo != 0L
)
