package pe.edu.upeu.pharmamobil.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.plugins.auth.clearAuthTokens
import pe.edu.upeu.pharmamobil.domain.ErrorApi
import pe.edu.upeu.pharmamobil.domain.ErrorDeApi
import pe.edu.upeu.pharmamobil.domain.auth.AuthRepository
import pe.edu.upeu.pharmamobil.domain.auth.Credenciales
import pe.edu.upeu.pharmamobil.domain.auth.Sesion
import pe.edu.upeu.pharmamobil.domain.security.AlmacenSeguro

class AuthRepositorioRemoto(
    private val api: AuthApi,
    private val almacen: AlmacenSeguro,
    private val cliente: HttpClient
) : AuthRepository {

    override suspend fun iniciarSesion(credenciales: Credenciales): Sesion {

        val sesion = try {
            alAplicacion { api.iniciarSesion(credenciales.aRemoto()).aDominio() }
        } catch (fallo: ErrorDeApi) {
            val error = fallo.error
            throw if (error is ErrorApi.NoAutorizado) {
                ErrorDeApi(ErrorApi.CredencialesInvalidas(error.mensaje))
            } else {
                fallo
            }
        }

        almacen.guardarToken(sesion.token)
        cliente.clearAuthTokens()
        return sesion
    }

    override suspend fun recuperarSesion(): Sesion? {

        val token = almacen.leerToken() ?: return null

        return try {
            alAplicacion { api.perfil().aDominio(token) }.also {
                cliente.clearAuthTokens()
            }
        } catch (fallo: ErrorDeApi) {
            if (fallo.error is ErrorApi.NoAutorizado || fallo.error is ErrorApi.SesionExpirada) {
                almacen.borrarToken()
                cliente.clearAuthTokens()
                null
            } else {
                throw fallo
            }
        }
    }

    override suspend fun cerrarSesion() {
        almacen.borrarToken()
        cliente.clearAuthTokens()
    }
}
