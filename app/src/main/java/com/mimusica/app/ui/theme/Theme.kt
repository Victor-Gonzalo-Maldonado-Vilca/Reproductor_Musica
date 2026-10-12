package com.mimusica.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object Nocturne {
    val Background = Color(0xFF13141B)
    val Surface = Color(0xFF1C1D26)
    val SurfaceHigh = Color(0xFF262733)
    val SurfaceHigher = Color(0xFF30313E)
    val TextPrimary = Color(0xFFF2F2F7)
    val TextSecondary = Color(0xFFA9AABA)
    val TextMuted = Color(0xFF6E7082)
    val Lavender = Color(0xFFC6A9FF)
    val Purple = Color(0xFF8B7CF6)
    val Cyan = Color(0xFF4FD8EE)
    val CyanDim = Color(0xFF1C3A44)
    val PurpleDim = Color(0xFF332B55)

    /** Degradado morado → cian de los botones principales. */
    val Accent = Brush.linearGradient(listOf(Color(0xFF9A7BFA), Color(0xFF55C8F2)))

    /** Portadas generadas cuando una canción no tiene carátula. */
    val CoverGradients = listOf(
        listOf(Color(0xFF7C3AED), Color(0xFF22D3EE)),
        listOf(Color(0xFF312E81), Color(0xFFDB2777)),
        listOf(Color(0xFF0F766E), Color(0xFF6366F1)),
        listOf(Color(0xFF581C87), Color(0xFF0EA5E9)),
        listOf(Color(0xFF1E3A8A), Color(0xFFA855F7)),
        listOf(Color(0xFF134E4A), Color(0xFF22D3EE)),
    )
}

private val colors = darkColorScheme(
    primary = Nocturne.Purple,
    onPrimary = Color.White,
    secondary = Nocturne.Cyan,
    background = Nocturne.Background,
    onBackground = Nocturne.TextPrimary,
    surface = Nocturne.Surface,
    onSurface = Nocturne.TextPrimary,
    surfaceVariant = Nocturne.SurfaceHigh,
    onSurfaceVariant = Nocturne.TextSecondary,
    surfaceContainer = Nocturne.Surface,
    surfaceContainerHigh = Nocturne.SurfaceHigh,
    surfaceContainerLow = Nocturne.Surface,
    outline = Nocturne.SurfaceHigher,
)

@Composable
fun NocturneTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = colors, content = content)
}
