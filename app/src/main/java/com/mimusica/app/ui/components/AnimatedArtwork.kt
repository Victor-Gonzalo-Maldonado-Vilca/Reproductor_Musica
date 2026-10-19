package com.mimusica.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
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
    // Un efecto se dibuja si el usuario lo activó Y la forma actual lo permite
    fun on(effect: ArtworkEffect) =
        effect in effects && (!effect.needsVinyl || base == ArtworkBase.Vinyl)

    val spin = on(ArtworkEffect.Spin)
    val rotation = remember { Animatable(0f) }
    val pulse = on(ArtworkEffect.Pulse)
    val scale by animateFloatAsState(
        targetValue = if (pulse && !playing) 0.85f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        label = "rebote",
    )
    val halo = on(ArtworkEffect.Halo)
    val glow by animateFloatAsState(
        targetValue = if (playing) 0.5f else 0f,
        animationSpec = tween(durationMillis = 600),
        label = "halo",
    )

    val ring = on(ArtworkEffect.Ring)
    val sweep by animateFloatAsState(
        targetValue = if (playing) 1f else 0f,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "anillo",
    )
    val showProgress = on(ArtworkEffect.Progress)
    // La posición puede pasarse un instante de la duración al terminar la canción.
    val songProgress = progress.coerceIn(0f, 1f)
    // Con halo o anillos, la carátula se encoge para dejarles sitio alrededor.
    val inner = if (halo || ring || showProgress) size * 0.82f else size

    LaunchedEffect(playing, spin) {
        // Sin música o sin el efecto, el disco se queda quieto en su ángulo actual.
        if (!playing || !spin) return@LaunchedEffect
        while (true) {
            rotation.animateTo(
                targetValue = rotation.value + 360f,
                animationSpec = tween(durationMillis = 8000, easing = LinearEasing),
            )
            rotation.snapTo(rotation.value % 360f)
        }
    }
    // Giro y rebote en una sola capa. La sombra va dentro para que siga a la carátula
    // (forma y tamaño) y no se quede grande detrás cuando se encoge.
    val shape = if (base == ArtworkBase.Vinyl) CircleShape else RoundedCornerShape(28.dp)
    val layer = Modifier
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
            rotationZ = if (spin) rotation.value else 0f
        }
        .shadow(48.dp, shape, ambientColor = Nocturne.Purple, spotColor = Nocturne.Cyan)
    Box(Modifier.size(size), contentAlignment = Alignment.Center) {
        if (halo) {
            Canvas(Modifier.fillMaxSize()) {
                val radio = this.size.minDimension / 2f
                drawCircle(
                    brush = Brush.radialGradient(
                        0.7f to Nocturne.Cyan.copy(alpha = glow),
                        1f to Color.Transparent,
                        center = center,
                        radius = radio,
                    ),
                    radius = radio,
                )
            }
        }
        if (ring) {
            Canvas(Modifier.fillMaxSize()) {
                val r = this.size.minDimension / 2f * 0.9f
                val grosor = 6.dp.toPx()
                // 1. El aro completo, tenue
                drawCircle(
                    color = Color.White.copy(alpha = 0.12f),
                    radius = r,
                    style = Stroke(width = grosor),
                )
                // 2. Arco de color que se llena. Vacío no se dibuja: el extremo
                // redondeado dejaría un punto suelto arriba.
                if (sweep > 0f) {
                    drawArc(
                        brush = Brush.sweepGradient(listOf(Nocturne.Purple, Nocturne.Cyan, Nocturne.Purple)),
                        startAngle = -90f,
                        sweepAngle = 360f * sweep,
                        useCenter = false,
                        topLeft = Offset(center.x - r, center.y - r),
                        size = Size(r * 2, r * 2),
                        style = Stroke(width = grosor, cap = StrokeCap.Round),
                    )
                }
            }
        }
        if (showProgress) {
            Canvas(Modifier.fillMaxSize()) {
                val r = this.size.minDimension / 2f * 0.97f
                val grosor = 4.dp.toPx()
                drawCircle(
                    color = Color.White.copy(alpha = 0.15f),
                    radius = r,
                    style = Stroke(width = grosor),
                )
                if (songProgress > 0f) {
                    drawArc(
                        color = Nocturne.Purple,
                        startAngle = -90f,
                        sweepAngle = 360f * songProgress,
                        useCenter = false,
                        topLeft = Offset(center.x - r, center.y - r),
                        size = Size(r * 2, r * 2),
                        style = Stroke(width = grosor, cap = StrokeCap.Round),
                    )
                }
            }
        }
        when (base) {
            ArtworkBase.Cover -> Artwork(song, inner, layer, corner = 28.dp)
            ArtworkBase.Vinyl -> VinylDisc(song, inner, layer)
        }
    }
}

/** Parte del disco que ocupa la etiqueta (la carátula del centro). */
private const val LABEL_RATIO = 0.6f

/** Disco de vinilo: negro con surcos y la carátula como etiqueta en el centro. */
@Composable
private fun VinylDisc(song: Song?, size: Dp, modifier: Modifier = Modifier) {
    Box(
        modifier.size(size).clip(CircleShape).background(Color(0xFF0B0B10)),
        contentAlignment = Alignment.Center,
    ) {
        // Surcos: repartidos entre el borde de la etiqueta y el del disco,
        // así siguen viéndose aunque cambie el tamaño de la etiqueta.
        Canvas(Modifier.fillMaxSize()) {
            val radio = this.size.minDimension / 2f
            for (k in 1..3) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.06f),
                    radius = radio * (LABEL_RATIO + (1f - LABEL_RATIO) * k / 4f),
                    style = Stroke(width = 1.dp.toPx()),
                )
            }
        }
        // Etiqueta: la carátula, pequeña y redonda
        val label = size * LABEL_RATIO
        Artwork(song, label, corner = label / 2)
        // Agujero central
        Box(Modifier.size(size * 0.04f).clip(CircleShape).background(Nocturne.Background))
    }
}
