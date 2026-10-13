package pe.edu.upeu.pharmamobil.domain.auth

/**
 * Nombres de permisos que la aplicación consulta para mostrar u ocultar
 * acciones.
 *
 * IMPORTANTE (limitación verificada): el backend `pharmaSoft` real **no**
 * implementa autenticación ni publica una lista de permisos, así que estos
 * identificadores NO han podido confirmarse contra el servidor. Se declaran
 * aquí como el contrato esperado por la guía de la sesión 10 y deben
 * contrastarse con el backend cuando éste exponga `GET /auth/me`.
 *
 * La matriz de referencia de la guía es:
 *
 * | Operación          | admin | vendedor | almacen |
 * |--------------------|-------|----------|---------|
 * | Listar productos   | sí    | sí       | sí      |
 * | Registrar producto | sí    | no       | sí      |
 * | Listar clientes    | sí    | sí       | no      |
 * | Registrar venta    | sí    | sí       | no      |
 * | Ver reportes       | sí    | no       | no      |
 * | Consultar /auth/me | sí    | sí       | sí      |
 */
object Permisos {

    const val PRODUCTOS_LISTAR = "productos:listar"
    const val PRODUCTOS_REGISTRAR = "productos:registrar"

    const val CLIENTES_LISTAR = "clientes:listar"

    const val VENTAS_REGISTRAR = "ventas:registrar"

    const val REPORTES_VER = "reportes:ver"

    /** Permiso implícito: cualquier sesión autenticada puede consultar su perfil. */
    const val PERFIL_CONSULTAR = "auth:me"
}
