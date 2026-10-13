package pe.edu.upeu.pharmamobil.domain.auth

/**
 * Nombres de permisos que la aplicación consulta para mostrar u ocultar
 * acciones.
 *
 * El backend `pharmaSoft` real (Spring Security + JWT) publica la lista de
 * `permisos` en `POST /auth/login` y `GET /auth/me`; estos identificadores son
 * los que emite `PermisosPorRol` y están confirmados contra el servidor.
 *
 * Matriz de permisos de pharmaSoft:
 *
 * | Operación                                | ADMIN | VENDEDOR | ALMACENERO |
 * |------------------------------------------|-------|----------|------------|
 * | CATALOGO_LEER (productos y categorías)   | sí    | sí       | sí         |
 * | CATALOGO_ESCRIBIR (productos y categorías)| sí   | no       | sí         |
 * | CLIENTES_LEER                            | sí    | sí       | no         |
 * | CLIENTES_ESCRIBIR                        | sí    | sí       | no         |
 * | VENTAS_LEER                              | sí    | sí       | no         |
 * | VENTAS_REGISTRAR                         | sí    | sí       | no         |
 * | REPORTES_VER                             | sí    | no       | no         |
 * | USUARIOS_ADMINISTRAR                     | sí    | no       | no         |
 */
object Permisos {

    const val CATALOGO_LEER = "CATALOGO_LEER"
    const val CATALOGO_ESCRIBIR = "CATALOGO_ESCRIBIR"

    const val CLIENTES_LEER = "CLIENTES_LEER"
    const val CLIENTES_ESCRIBIR = "CLIENTES_ESCRIBIR"

    const val VENTAS_LEER = "VENTAS_LEER"
    const val VENTAS_REGISTRAR = "VENTAS_REGISTRAR"

    const val REPORTES_VER = "REPORTES_VER"

    const val USUARIOS_ADMINISTRAR = "USUARIOS_ADMINISTRAR"
}
