package pe.edu.upeu.pharmamobil.data.security

import pe.edu.upeu.pharmamobil.domain.security.AlmacenSeguro

/**
 * Doble de [AlmacenSeguro] para las pruebas: guarda el token en memoria.
 *
 * Es el sustituto determinista del almacén cifrado real (Keystore/Keychain), que
 * no existe en un entorno de pruebas JVM/nativo. Registra además cuántas veces
 * se guardó, leyó o borró el token, para poder afirmar que el flujo de sesión
 * borra el secreto cuando corresponde.
 */
class AlmacenSeguroEnMemoria(
    private var token: String? = null
) : AlmacenSeguro {

    var guardados = 0
        private set

    var borrados = 0
        private set

    override fun guardarToken(token: String) {
        this.token = token
        guardados++
    }

    override fun leerToken(): String? = token

    override fun borrarToken() {
        token = null
        borrados++
    }
}
