package pe.edu.upeu.pharmamobil.presentation.inicio

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import pe.edu.upeu.pharmamobil.navigation.Screen

@Composable
fun InicioScreen(
    onNavegar: (Screen) -> Unit,
    onSolicitarNotificaciones: () -> Unit = {},
    onAbrirAjustesPermiso: () -> Unit = {},
    onSincronizar: () -> Unit = {},
    onReintentarFallidas: () -> Unit = {},
    erroresSync: List<String> = emptyList()
) {
    var mostrarExplicacion by remember { mutableStateOf(false) }
    var mostrarAjustes by remember { mutableStateOf(false) }
    var permisoSolicitado by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Portada()

        EstadoSincronizacionCard(onSincronizar, onReintentarFallidas, erroresSync)

        AccesoRapido(
            icono = Icons.Default.Notifications,
            titulo = "Activar notificaciones",
            descripcion = "Recibe avisos importantes de PharmaMobil.",
            onClick = {
                if (permisoSolicitado) mostrarAjustes = true else mostrarExplicacion = true
            }
        )

        if (mostrarExplicacion) AlertDialog(
            onDismissRequest = { mostrarExplicacion = false },
            title = { Text("Permitir notificaciones") },
            text = { Text("PharmaMobil puede enviarte avisos sobre cambios y sincronización. Puedes cambiar este permiso cuando quieras en Ajustes.") },
            confirmButton = { TextButton(onClick = { mostrarExplicacion = false; permisoSolicitado = true; onSolicitarNotificaciones() }) { Text("Continuar") } },
            dismissButton = { TextButton(onClick = { mostrarExplicacion = false }) { Text("Ahora no") } }
        )

        if (mostrarAjustes) AlertDialog(
            onDismissRequest = { mostrarAjustes = false },
            title = { Text("Permiso desactivado") },
            text = { Text("Activa las notificaciones desde Ajustes de la aplicación para recuperar esta función.") },
            confirmButton = { TextButton(onClick = { mostrarAjustes = false; onAbrirAjustesPermiso() }) { Text("Abrir Ajustes") } },
            dismissButton = { TextButton(onClick = { mostrarAjustes = false }) { Text("Cerrar") } }
        )

        Text(
            text = "Qué puedes hacer",
            style = MaterialTheme.typography.titleMedium
        )

        AccesoRapido(
            icono = Icons.Default.Medication,
            titulo = "Gestionar productos",
            descripcion = "Mantén el inventario: crea, edita y elimina medicamentos.",
            onClick = { onNavegar(Screen.Productos) }
        )

        AccesoRapido(
            icono = Icons.Default.Person,
            titulo = "Gestionar clientes",
            descripcion = "Mantén la cartera: crea, edita y elimina clientes.",
            onClick = { onNavegar(Screen.Clientes) }
        )

        AccesoRapido(
            icono = Icons.Default.ShoppingCart,
            titulo = "Revisar pedidos",
            descripcion = "Disponible en una próxima sesión del curso.",
            onClick = { onNavegar(Screen.Pedidos) }
        )
    }
}

@Composable
private fun Portada() {

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {

        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Icon(
                imageVector = Icons.Default.LocalPharmacy,
                contentDescription = null,
                modifier = Modifier.size(40.dp)
            )

            Text(
                text = "PharmaMobil",
                style = MaterialTheme.typography.headlineSmall
            )

            Text(
                text = "Sistema de gestión farmacéutica para tu cadena de boticas.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun AccesoRapido(
    icono: ImageVector,
    titulo: String,
    descripcion: String,
    onClick: () -> Unit
) {

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {

        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            ) {

                Icon(
                    imageVector = icono,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(10.dp)
                        .size(22.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {

                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleSmall
                )

                Text(
                    text = descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
