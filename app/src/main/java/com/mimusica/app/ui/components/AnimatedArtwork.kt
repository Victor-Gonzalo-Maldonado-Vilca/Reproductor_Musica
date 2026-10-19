package com.mimusica.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mimusica.app.data.ArtworkBase
import com.mimusica.app.data.ArtworkEffect
import com.mimusica.app.data.Song
import com.mimusica.app.ui.theme.Nocturne

/** Carátula de "Ahora suena": portada o disco, con los efectos que el usuario elija. */
@Composable
fun AnimatedArtwork(
    song: Song?,
    size: Dp,
    playing: Boolean,
    progress: Float,
    base: ArtworkBase,
    effects: Set<ArtworkEffect>,
) {
    val spin = ArtworkEffect.Spin in effects && base == ArtworkBase.Vinyl
    val rotation = remember { Animatable(0f) }        // ángulo inicial

    LaunchedEffect(playing, spin) {
        if (!playing || !spin) return@LaunchedEffect    // si no toca girar, no hagas nada
        while (true) {
            rotation.animateTo(
                targetValue = rotation.value + 360f,       // una vuelta completa
                animationSpec = tween(durationMillis = 8000, easing = LinearEasing),
            )
            rotation.snapTo(rotation.value % 360f)
        }
    }
    when (base) {
        ArtworkBase.Cover -> Artwork(song, size, corner = 28.dp)
        ArtworkBase.Vinyl -> VinylDisc(song, size, Modifier.graphicsLayer { rotationZ = rotation.valuegit add app/src/main/java/com/mimusica/app/ui/components/AnimatedArtwork.kt })
    }
}

/** Disco de vinilo: negro con surcos y la carátula como etiqueta en el centro. */
@Composable
private fun VinylDisc(song: Song?, size: Dp, modifier: Modifier = Modifier) {
    Box(
        modifier.size(size).clip(CircleShape).background(Color(0xFF0B0B10)),
        contentAlignment = Alignment.Center,
    ) {
        // Surcos: círculos finos
        Canvas(Modifier.fillMaxSize()) {
            val radio = this.size.minDimension / 2f
            for (k in 1..3) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.06f),
                    radius = radio * (0.5f + 0.1f * k),
                    style = Stroke(width = 1.dp.toPx()),
                )
            }
        }
        // Etiqueta: la carátula, pequeña y redonda
        Artwork(song, size * 0.4f, corner = size * 0.2f)
        // Agujero central
        Box(Modifier.size(size * 0.04f).clip(CircleShape).background(Nocturne.Background))
    }
}