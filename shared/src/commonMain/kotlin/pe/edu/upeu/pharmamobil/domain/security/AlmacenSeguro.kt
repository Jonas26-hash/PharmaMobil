package pe.edu.upeu.pharmamobil.domain.security

interface AlmacenSeguro {

    fun guardarToken(token: String)

    fun leerToken(): String?

    fun borrarToken()
}
