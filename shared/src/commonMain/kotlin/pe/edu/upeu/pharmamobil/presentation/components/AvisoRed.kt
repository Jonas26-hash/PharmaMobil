package pe.edu.upeu.pharmamobil.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AvisoRed(
    sincronizando: Boolean,
    avisoRed: String?,
    onReintentar: () -> Unit,
    modifier: Modifier = Modifier
) {

    if (avisoRed == null && !sincronizando) return

    val esFallo = avisoRed != null

    Surface(
        shape = MaterialTheme.shapes.small,
        color = if (esFallo) {
            MaterialTheme.colorScheme.errorContainer
        } else {
            MaterialTheme.colorScheme.secondaryContainer
        },
        contentColor = if (esFallo) {
            MaterialTheme.colorScheme.onErrorContainer
        } else {
            MaterialTheme.colorScheme.onSecondaryContainer
        },
        modifier = modifier.fillMaxWidth()
    ) {

        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            if (esFallo) {
                Icon(
                    imageVector = Icons.Default.CloudOff,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            } else {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp
                )
            }

            Text(
                text = avisoRed ?: "Sincronizando…",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.weight(1f)
            )

            if (esFallo) {
                TextButton(onClick = onReintentar) {
                    Text("Reintentar")
                }
            }
        }
    }
}
