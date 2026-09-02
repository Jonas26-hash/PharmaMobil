package pe.edu.upeu.pharmamobil.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF00606B),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFB8EBF4),
    onPrimaryContainer = Color(0xFF001F24),
    secondary = Color(0xFF4B6268),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFCDE7EE),
    onSecondaryContainer = Color(0xFF061F24),
    tertiary = Color(0xFF3E6936),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFBDF1B0),
    onTertiaryContainer = Color(0xFF042200),
    background = Color(0xFFFBFCFD),
    onBackground = Color(0xFF191C1D),
    surface = Color(0xFFFBFCFD),
    onSurface = Color(0xFF191C1D),
    surfaceVariant = Color(0xFFDBE4E7),
    onSurfaceVariant = Color(0xFF3F484B),
    outline = Color(0xFF6F797C),
    outlineVariant = Color(0xFFBFC8CB)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF8ECFD8),
    onPrimary = Color(0xFF00363C),
    primaryContainer = Color(0xFF004E56),
    onPrimaryContainer = Color(0xFFB8EBF4),
    secondary = Color(0xFFB1CBD2),
    onSecondary = Color(0xFF1C343A),
    secondaryContainer = Color(0xFF334B51),
    onSecondaryContainer = Color(0xFFCDE7EE),
    tertiary = Color(0xFFA1D495),
    onTertiary = Color(0xFF0E3909),
    tertiaryContainer = Color(0xFF275020),
    onTertiaryContainer = Color(0xFFBDF1B0),
    background = Color(0xFF191C1D),
    onBackground = Color(0xFFE1E3E4),
    surface = Color(0xFF191C1D),
    onSurface = Color(0xFFE1E3E4),
    surfaceVariant = Color(0xFF3F484B),
    onSurfaceVariant = Color(0xFFBEC8CB),
    outline = Color(0xFF899294),
    outlineVariant = Color(0xFF3F484B)
)

private val PharmaTypography = Typography()

private val PharmaShapes = Shapes()

@Composable
fun PharmaMobilTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {

    val colors = if (darkTheme) {
        DarkColors
    } else {
        LightColors
    }

    MaterialTheme(
        colorScheme = colors,
        typography = PharmaTypography,
        shapes = PharmaShapes,
        content = content
    )
}