package com.mimusica.app.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.PlaylistAdd
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mimusica.app.ui.AppActions
import com.mimusica.app.data.Collection
import com.mimusica.app.data.LibraryStore
import com.mimusica.app.data.Song
import com.mimusica.app.data.formatTotalDuration
import com.mimusica.app.playback.PlayerController
import com.mimusica.app.ui.components.Artwork
import com.mimusica.app.ui.components.CircleIconButton
import com.mimusica.app.ui.components.GradientCircleButton
import com.mimusica.app.ui.components.NocturneLogo
import com.mimusica.app.ui.components.SongRow
import com.mimusica.app.ui.components.TagBadge
import com.mimusica.app.ui.components.UserAvatar
import com.mimusica.app.ui.components.WaveBars
import com.mimusica.app.ui.theme.Nocturne

@Composable
fun PlaylistDetailScreen(collection: Collection, actions: AppActions, contentPadding: PaddingValues) {
    val info = resolve(collection)
    if (info == null) {
        // La colección ya no existe (por ejemplo, la playlist se eliminó).
        androidx.compose.runtime.LaunchedEffect(Unit) { actions.back() }
        return
    }
    var lyricsTab by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    val songs = info.songs
    val playingThis = PlayerController.currentSongId != null &&
        PlayerController.queue.sortedBy { it.index }.map { it.songId } == songs.map { it.id }
    val playlistId = (collection as? Collection.UserPlaylist)?.id

    LazyColumn(contentPadding = contentPadding) {
        // ---------- Barra superior ----------
        item {
            var menu by remember { mutableStateOf(false) }
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Rounded.ArrowBackIosNew, "Atrás", tint = Nocturne.TextPrimary,
                    modifier = Modifier.clip(CircleShape).clickable { actions.back() }.padding(12.dp).size(24.dp),
                )
                NocturneLogo(42.dp)
                Spacer(Modifier.width(14.dp))
                Text(
                    "Detalle De ${info.kind}", color = Nocturne.TextPrimary, fontSize = 21.sp,
                    fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Box {
                    Icon(
                        Icons.Rounded.MoreHoriz, "Más", tint = Nocturne.TextPrimary,
                        modifier = Modifier.clip(CircleShape).clickable { menu = true }.padding(10.dp).size(28.dp),
                    )
                    DropdownMenu(menu, onDismissRequest = { menu = false }, containerColor = Nocturne.SurfaceHigh) {
                        CollectionMenuItems(collection, actions) { menu = false }
                    }
                }
                Spacer(Modifier.width(8.dp))
                UserAvatar(LibraryStore.userName, size = 44.dp) { actions.goToTab(com.mimusica.app.ui.components.Tab.Settings) }
            }
        }

        // ---------- Portada con brillo ----------
        item {
            var menu by remember { mutableStateOf(false) }
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF1E2A35), Color(0xFF1A2230), Nocturne.Background),
                        ),
                    )
                    .padding(top = 20.dp, bottom = 8.dp),
            ) {
                Text(
                    info.label, color = Nocturne.Cyan, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.5.sp,
                    modifier = Modifier.padding(start = 4.dp).clip(RoundedCornerShape(8.dp))
                        .background(Nocturne.CyanDim.copy(alpha = 0.6f)).padding(horizontal = 12.dp, vertical = 6.dp),
                )
                Row(Modifier.align(Alignment.TopEnd)) {
                    CircleIconButton(Icons.Rounded.Share, "Compartir", size = 54.dp, background = Nocturne.SurfaceHigh) {
                        val text = "Estoy escuchando \"${info.title}\" en Nocturne 🎧" +
                            (songs.take(5).joinToString("") { "\n• ${it.title} — ${it.artist}" })
                        context.startActivity(
                            Intent.createChooser(
                                Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text),
                                "Compartir",
                            ),
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Box {
                        CircleIconButton(Icons.Rounded.MoreVert, "Opciones", size = 54.dp, background = Nocturne.SurfaceHigh) { menu = true }
                        DropdownMenu(menu, onDismissRequest = { menu = false }, containerColor = Nocturne.SurfaceHigh) {
                            CollectionMenuItems(collection, actions) { menu = false }
                        }
                    }
                }
                Box(
                    Modifier.align(Alignment.Center).padding(top = 80.dp),
                    contentAlignment = Alignment.BottomStart,
                ) {
                    Box(
                        Modifier.shadow(40.dp, RoundedCornerShape(26.dp), ambientColor = Nocturne.Cyan, spotColor = Nocturne.Cyan),
                    ) {
                        Artwork(
                            info.cover, 330.dp, corner = 26.dp,
                            fallbackBrush = if (collection is Collection.Favorites)
                                Brush.linearGradient(listOf(Color(0xFF8B5CF6), Color(0xFF22B8CF))) else null,
                            fallbackIcon = if (collection is Collection.Favorites) Icons.Rounded.Favorite else Icons.Rounded.GraphicEq,
                        )
                    }
                    Row(Modifier.padding(start = 28.dp, bottom = 22.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(9.dp).clip(CircleShape).background(Nocturne.Cyan))
                        Spacer(Modifier.width(10.dp))
                        Text(
                            if (songs.any { it.isLossless }) "HI-RES AUDIO" else "${songs.size} CANCIONES",
                            color = Nocturne.Cyan, fontSize = 17.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp,
                        )
                    }
                }
            }
        }

        // ---------- Título y datos ----------
        item {
            Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    info.title, color = Nocturne.TextPrimary, fontSize = 38.sp, fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center, lineHeight = 46.sp, maxLines = 3, overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        buildAnnotatedString {
                            append(if (collection is Collection.AlbumOf || collection is Collection.ArtistOf) "De  " else "Creado por  ")
                            withStyle(SpanStyle(color = Nocturne.TextPrimary, fontWeight = FontWeight.SemiBold)) { append(info.owner) }
                        },
                        color = Nocturne.TextSecondary, fontSize = 19.sp, maxLines = 1, overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.Rounded.Verified, null, tint = Nocturne.Cyan, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    buildAnnotatedString {
                        append("${songs.size} canciones  •  ${formatTotalDuration(info.totalMs)}  •  ")
                        withStyle(SpanStyle(color = Nocturne.Cyan)) {
                            append(songs.map { it.format }.distinct().take(2).joinToString(" / ").ifEmpty { "Vacía" })
                        }
                    },
                    color = Nocturne.TextSecondary, fontSize = 18.sp,
                )
            }
        }

        // ---------- Botones de acción ----------
        item {
            Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                val liked = collection is Collection.Favorites || LibraryStore.isPinned(collection.key)
                CircleIconButton(
                    if (liked) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                    "Guardar en la biblioteca", size = 70.dp,
                    tint = Nocturne.Lavender,
                ) {
                    if (collection is Collection.Favorites) actions.message("Esta lista siempre está en tu biblioteca")
                    else {
                        LibraryStore.togglePinned(collection.key)
                        actions.message(if (LibraryStore.isPinned(collection.key)) "Guardado en tu biblioteca" else "Quitado de tu biblioteca")
                    }
                }
                Spacer(Modifier.width(8.dp))
                CircleIconButton(Icons.AutoMirrored.Rounded.PlaylistAdd, "Agregar todo a una playlist", size = 70.dp) {
                    if (songs.isNotEmpty()) actions.addToPlaylist(songs.map { it.id })
                }
                Spacer(Modifier.width(8.dp))
                CircleIconButton(Icons.Rounded.Shuffle, "Aleatorio", size = 70.dp) {
                    PlayerController.play(songs, shuffled = true)
                }
                Spacer(Modifier.weight(1f))
                GradientCircleButton(
                    if (playingThis && PlayerController.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    "Reproducir", size = 94.dp,
                ) {
                    if (playingThis) PlayerController.togglePlay() else PlayerController.play(songs)
                }
            }
        }

        // ---------- Pestañas Canciones / Letras ----------
        item {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 20.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Nocturne.Surface)
                    .padding(6.dp),
            ) {
                listOf("Canciones" to false, "Letras en Vivo" to true).forEach { (label, isLyrics) ->
                    val selected = lyricsTab == isLyrics
                    Text(
                        label, textAlign = TextAlign.Center, fontSize = 19.sp,
                        color = if (selected) Nocturne.TextPrimary else Nocturne.TextSecondary,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (selected) Nocturne.SurfaceHigher else Color.Transparent)
                            .clickable { lyricsTab = isLyrics }
                            .padding(vertical = 12.dp),
                    )
                }
            }
        }

        if (lyricsTab) {
            item { LyricsPanel(PlayerController.currentSong) }
        } else {
            if (songs.isEmpty()) {
                item {
                    InfoCard(
                        if (collection is Collection.Favorites) "Toca ⋮ en cualquier canción y elige \"Agregar a Me gusta\"."
                        else "Esta lista está vacía. Agrega canciones desde el menú ⋮ de cualquier canción.",
                    )
                }
            }
            itemsIndexed(songs, key = { i, s -> if (playlistId != null) s.id else "$i-${s.id}" }) { index, song ->
                SongRow(
                    song,
                    number = index + 1,
                    isCurrent = PlayerController.currentSongId == song.id,
                    isPlaying = PlayerController.isPlaying,
                    onReorder = playlistId?.let { id -> { delta -> LibraryStore.moveInPlaylist(id, index, index + delta) } },
                    onClick = { PlayerController.play(songs, index) },
                    onMore = { actions.songOptions(song, playlistId, index) },
                )
            }
        }

        // ---------- Calidad de audio ----------
        item { QualityCard(PlayerController.currentSong ?: songs.firstOrNull()) }
    }
}

@Composable
private fun LyricsPanel(song: Song?) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.verticalGradient(listOf(Nocturne.PurpleDim, Nocturne.Surface)))
            .padding(24.dp),
    ) {
        Text(
            song?.title ?: "Nada sonando", color = Nocturne.Lavender, fontSize = 22.sp, fontWeight = FontWeight.Bold,
        )
        Text(song?.artist ?: "Reproduce una canción", color = Nocturne.TextSecondary, fontSize = 16.sp)
        Spacer(Modifier.height(20.dp))
        Text(
            "Las letras sincronizadas todavía no están disponibles para los archivos de tu teléfono.",
            color = Nocturne.TextPrimary, fontSize = 18.sp, lineHeight = 26.sp,
        )
    }
}

/** Tarjeta "Master Quality Audio" con los datos reales del archivo que suena. */
@Composable
private fun QualityCard(song: Song?) {
    if (song == null) return
    val kbps = if (song.durationMs > 0) song.sizeBytes * 8 / song.durationMs else 0
    Row(
        Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 28.dp, bottom = 12.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Nocturne.Surface)
            .padding(horizontal = 22.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        WaveBars(Modifier.size(30.dp), Brush.linearGradient(listOf(Nocturne.Cyan, Nocturne.Cyan)))
        Spacer(Modifier.width(18.dp))
        Column(Modifier.weight(1f)) {
            Text(
                if (song.isLossless) "Master Quality Audio" else "Calidad de audio",
                color = Nocturne.TextPrimary, fontSize = 19.sp, fontWeight = FontWeight.SemiBold,
            )
            Text(
                "${song.format}  /  ~$kbps kbps  •  Archivo local",
                color = Nocturne.TextSecondary, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis,
            )
        }
        TagBadge("LOCAL", color = Nocturne.Cyan, background = Nocturne.CyanDim)
    }
}
