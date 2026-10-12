package com.mimusica.app.ui.components

import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.os.Build
import android.util.LruCache
import android.util.Size
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mimusica.app.data.Song
import com.mimusica.app.ui.theme.Nocturne
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.absoluteValue

private object ArtCache {
    val images = LruCache<Long, ImageBitmap>(80)
    val missing = mutableSetOf<Long>()
}

private fun loadArt(context: Context, song: Song): ImageBitmap? = runCatching {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        context.contentResolver.loadThumbnail(song.uri, Size(512, 512), null).asImageBitmap()
    } else {
        val r = MediaMetadataRetriever()
        try {
            r.setDataSource(context, song.uri)
            r.embeddedPicture?.let { BitmapFactory.decodeByteArray(it, 0, it.size)?.asImageBitmap() }
        } finally {
            r.release()
        }
    }
}.getOrNull()

/** Carátula del álbum de la canción (o null si el archivo no trae imagen). */
@Composable
fun rememberAlbumArt(song: Song?): ImageBitmap? {
    val context = LocalContext.current
    val key = song?.albumId
    val art by produceState(key?.let { ArtCache.images.get(it) }, key) {
        if (song == null || key == null) {
            value = null
            return@produceState
        }
        ArtCache.images.get(key)?.let { value = it; return@produceState }
        if (key in ArtCache.missing) return@produceState
        val loaded = withContext(Dispatchers.IO) { loadArt(context, song) }
        if (loaded != null) ArtCache.images.put(key, loaded) else ArtCache.missing += key
        value = loaded
    }
    return art
}

fun gradientFor(seed: Any?): Brush {
    val colors = Nocturne.CoverGradients[(seed.hashCode().absoluteValue) % Nocturne.CoverGradients.size]
    return Brush.linearGradient(colors)
}

/**
 * Portada cuadrada redondeada. Muestra la carátula del archivo si existe;
 * si no, un degradado con un ícono, al estilo del diseño.
 */
@Composable
fun Artwork(
    song: Song?,
    size: Dp,
    modifier: Modifier = Modifier,
    corner: Dp = 10.dp,
    fallbackIcon: ImageVector = Icons.Rounded.GraphicEq,
    fallbackBrush: Brush? = null,
) {
    val art = rememberAlbumArt(song)
    Box(
        modifier
            .size(size)
            .clip(RoundedCornerShape(corner))
            .background(fallbackBrush ?: gradientFor(song?.albumId)),
        contentAlignment = Alignment.Center,
    ) {
        if (art != null) {
            Image(art, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        } else {
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.15f)))
            Icon(fallbackIcon, null, tint = Color.White.copy(alpha = 0.9f), modifier = Modifier.size(size * 0.42f))
        }
    }
}
