package pe.edu.upeu.pharmamobil.data.security

import pe.edu.upeu.pharmamobil.domain.security.AlmacenSeguro

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
