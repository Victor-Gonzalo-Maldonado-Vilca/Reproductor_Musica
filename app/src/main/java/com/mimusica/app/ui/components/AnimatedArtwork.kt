package com.mimusica.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
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
    when (base) {
        ArtworkBase.Cover -> Artwork(song, size, corner = 28.dp)
        ArtworkBase.Vinyl -> VinylDisc(song, size)
    }
}

/** Disco de vinilo: negro con surcos y la carátula como etiqueta en el centro. */
@Composable
private fun VinylDisc(song: Song?, size: Dp) {
    Box(
        Modifier.size(size).clip(CircleShape).background(Color(0xFF0B0B10)),
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