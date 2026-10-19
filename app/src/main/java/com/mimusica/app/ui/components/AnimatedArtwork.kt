package com.mimusica.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
    val rotation = remember { Animatable(0f) }
    val pulse = ArtworkEffect.Pulse in effects
    val scale by animateFloatAsState(
        targetValue = if (pulse && !playing) 0.85f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        label = "rebote",
    )
    val halo = ArtworkEffect.Halo in effects
    val glow by animateFloatAsState(
        targetValue = if (playing) 1f else 0f,
        animationSpec = tween(durationMillis = 600),
        label = "halo",
    )
    // Si hay halo, la carátula se hace un poco más pequeña para dejarle sitio alrededor
    val inner = if (halo) size * 0.82f else size

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
    val layer = Modifier.graphicsLayer {
        scaleX = scale
        scaleY = scale
        rotationZ = if (spin) rotation.value else 0f
    }
    Box(Modifier.size(size), contentAlignment = Alignment.Center) {
        if (halo) {
            Canvas(Modifier.fillMaxSize()) {
                val radio = this.size.minDimension / 2f
                drawCircle(
                    brush = Brush.radialGradient(
                        0.85f to Nocturne.Cyan.copy(alpha = glow),     // dónde empieza el brillo
                        1f to Color.Transparent,                     // en el borde ya no brilla
                        center = center,
                        radius = radio,
                    ),
                    radius = radio,
                )
            }
        }
        when (base) {
            ArtworkBase.Cover -> Artwork(song, inner, layer, corner = 28.dp)
            ArtworkBase.Vinyl -> VinylDisc(song, inner, layer)
        }
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
        val label = size * 0.6f
        Artwork(song, label, corner =  label / 2)
        // Agujero central
        Box(Modifier.size(size * 0.04f).clip(CircleShape).background(Nocturne.Background))
    }
}