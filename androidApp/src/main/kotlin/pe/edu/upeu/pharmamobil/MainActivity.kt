package pe.edu.upeu.pharmamobil

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.koin.android.ext.android.inject
import pe.edu.upeu.pharmamobil.data.permiso.GestorPermisosAndroid
import pe.edu.upeu.pharmamobil.domain.permiso.Permiso

class MainActivity : ComponentActivity() {
    private val gestorPermisos: GestorPermisosAndroid by inject()
    private var permisoPendiente: kotlinx.coroutines.CompletableDeferred<Boolean>? = null
    private val permisoLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        permisoPendiente?.complete(concedido)
        permisoPendiente = null
    }

    suspend fun solicitarPermisoSistema(nombre: String): Boolean {
        val deferred = kotlinx.coroutines.CompletableDeferred<Boolean>()
        permisoPendiente = deferred
        permisoLauncher.launch(nombre)
        return deferred.await()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            enableEdgeToEdge()
            super.onCreate(savedInstanceState)
            gestorPermisos.puedeExplicar = { nombre ->
                androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale(this, nombre)
            }
            gestorPermisos.solicitarSistema = { nombre ->
                val deferred = kotlinx.coroutines.CompletableDeferred<Boolean>()
                permisoPendiente = deferred
                permisoLauncher.launch(nombre)
                deferred.await()
            }

            setContent {
                App(onSolicitarNotificaciones = {
                    lifecycleScope.launch { gestorPermisos.solicitar(Permiso.NOTIFICACIONES) }
                }, onAbrirAjustesPermiso = gestorPermisos::abrirAjustes)
            }
        } catch (e: Throwable) {
            android.util.Log.e("CRITICAL_CRASH", "Fallo al iniciar MainActivity", e)
            println("CRITICAL_CRASH: ${e.stackTraceToString()}")
        }
    }

}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
