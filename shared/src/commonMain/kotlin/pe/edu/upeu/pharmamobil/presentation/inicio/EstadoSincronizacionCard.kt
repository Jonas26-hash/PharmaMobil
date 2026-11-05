package pe.edu.upeu.pharmamobil.presentation.inicio

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.TextButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import pe.edu.upeu.pharmamobil.data.local.EstadoColaLocalRepository
import pe.edu.upeu.pharmamobil.data.local.RegistroDeSincronizacion
import pe.edu.upeu.pharmamobil.domain.usecase.antiguedadDeDatos
import pe.edu.upeu.pharmamobil.ahoraEnMilisegundos

@Composable
fun EstadoSincronizacionCard(
    onSincronizar: () -> Unit = {},
    onReintentarFallidas: () -> Unit = {},
    errores: List<String> = emptyList()
) {
    val cola = koinInject<EstadoColaLocalRepository>()
    val estado by cola.observar().collectAsState(initial = null)
    val registro = koinInject<RegistroDeSincronizacion>()
    val antiguedad = antiguedadDeDatos(registro.ultimaSincronizacion(), ahoraEnMilisegundos())
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row {
                Icon(Icons.Default.CloudSync, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text("  Estado de sincronización", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                IconButton(onClick = onSincronizar) { Icon(Icons.Default.Refresh, contentDescription = "Sincronizar ahora") }
            }
            Text(
                "Pendientes: ${estado?.pendientes ?: 0}   ·   Fallidas: ${estado?.fallidas ?: 0}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(antiguedad, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if ((estado?.fallidas ?: 0) > 0) Text("Revisa los errores desde Productos o Clientes.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            errores.take(3).forEach { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error) }
            if ((estado?.fallidas ?: 0) > 0) TextButton(onClick = onReintentarFallidas) { Text("Reintentar ahora") }
        }
    }
}
