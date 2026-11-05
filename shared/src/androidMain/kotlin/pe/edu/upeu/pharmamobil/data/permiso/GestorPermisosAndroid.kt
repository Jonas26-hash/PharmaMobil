package pe.edu.upeu.pharmamobil.data.permiso

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import pe.edu.upeu.pharmamobil.domain.permiso.EstadoPermiso
import pe.edu.upeu.pharmamobil.domain.permiso.GestorPermisos
import pe.edu.upeu.pharmamobil.domain.permiso.Permiso

class GestorPermisosAndroid(private val contexto: Context) : GestorPermisos {
    @Volatile var puedeExplicar: (String) -> Boolean = { false }
    @Volatile var solicitarSistema: suspend (String) -> Boolean = { false }

    override suspend fun estado(permiso: Permiso): EstadoPermiso {
        val nombre = permiso.sistema() ?: return EstadoPermiso.Concedido
        if (ContextCompat.checkSelfPermission(contexto, nombre) == PackageManager.PERMISSION_GRANTED) {
            return EstadoPermiso.Concedido
        }
        val yaPedido = contexto.getSharedPreferences(PREFERENCIAS, Context.MODE_PRIVATE)
            .getBoolean("pedido_$nombre", false)
        return if (!yaPedido || puedeExplicar(nombre)) EstadoPermiso.Denegado
        else EstadoPermiso.DenegadoParaSiempre
    }

    override suspend fun solicitar(permiso: Permiso): EstadoPermiso {
        val nombre = permiso.sistema() ?: return EstadoPermiso.Concedido
        contexto.getSharedPreferences(PREFERENCIAS, Context.MODE_PRIVATE).edit()
            .putBoolean("pedido_$nombre", true).apply()
        val concedido = solicitarSistema(nombre)
        if (concedido) return EstadoPermiso.Concedido
        return estado(permiso)
    }

    override fun abrirAjustes() {
        contexto.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.parse("package:${contexto.packageName}")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    private fun Permiso.sistema(): String? = when (this) {
        Permiso.NOTIFICACIONES -> if (android.os.Build.VERSION.SDK_INT >= 33) Manifest.permission.POST_NOTIFICATIONS else null
        Permiso.CAMARA -> Manifest.permission.CAMERA
        Permiso.UBICACION -> Manifest.permission.ACCESS_FINE_LOCATION
    }

    private companion object { const val PREFERENCIAS = "pharmamobil_permisos" }
}
