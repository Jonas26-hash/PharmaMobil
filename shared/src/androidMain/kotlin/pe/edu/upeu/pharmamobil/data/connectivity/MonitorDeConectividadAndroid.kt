package pe.edu.upeu.pharmamobil.data.connectivity

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import pe.edu.upeu.pharmamobil.domain.connectivity.MonitorDeConectividad

class MonitorDeConectividadAndroid(
    context: Context
) : MonitorDeConectividad {

    private val gestor =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    override fun estaConectado(): Boolean {
        val red = gestor.activeNetwork ?: return false
        val capacidades = gestor.getNetworkCapabilities(red) ?: return false
        return capacidades.tieneInternet()
    }

    override fun observarConectividad(): Flow<Boolean> = callbackFlow {
        trySend(estaConectado())
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(true)
            }

            override fun onLost(network: Network) {
                trySend(estaConectado())
            }

            override fun onCapabilitiesChanged(
                network: Network,
                capacidades: NetworkCapabilities
            ) {
                trySend(capacidades.tieneInternet())
            }
        }
        gestor.registerDefaultNetworkCallback(callback)
        awaitClose { gestor.unregisterNetworkCallback(callback) }
    }.distinctUntilChanged()

    private fun NetworkCapabilities.tieneInternet(): Boolean =
        hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
}
