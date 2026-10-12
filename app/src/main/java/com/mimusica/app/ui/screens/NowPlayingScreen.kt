package com.mimusica.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import com.mimusica.app.ui.AppActions
import com.mimusica.app.data.LibraryStore
import com.mimusica.app.data.MusicRepository
import com.mimusica.app.data.formatDuration
import com.mimusica.app.playback.PlayerController
import com.mimusica.app.ui.components.Artwork
import com.mimusica.app.ui.components.GradientCircleButton
import com.mimusica.app.ui.components.TagBadge
import com.mimusica.app.ui.components.WaveBars
import com.mimusica.app.ui.theme.Nocturne

/** Reproductor a pantalla completa (se abre al tocar el mini reproductor). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingScreen(actions: AppActions) {
    val song = PlayerController.currentSong
    if (song == null) {
        androidx.compose.runtime.LaunchedEffect(Unit) { actions.back() }
        return
    }
    var showQueue by remember { mutableStateOf(false) }
    // Mientras el usuario arrastra la barra, mostramos su posición en vez de la real.
    var scrubbing by remember { mutableStateOf(false) }
    var scrubValue by remember { mutableFloatStateOf(0f) }
    val duration = PlayerController.durationMs.takeIf { it > 0 } ?: song.durationMs
    val position = if (scrubbing) (scrubValue * duration).toLong() else PlayerController.positionMs

    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF231C45), Color(0xFF141A26), Nocturne.Background))),
    ) {
        Column(
            Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // ---------- Barra superior ----------
            Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Rounded.KeyboardArrowDown, "Cerrar", tint = Nocturne.TextPrimary,
                    modifier = Modifier.clip(CircleShape).clickable { actions.back() }.padding(8.dp).size(32.dp),
                )
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("REPRODUCIENDO DESDE", color = Nocturne.Cyan, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.5.sp)
                    Text(song.album, color = Nocturne.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Icon(
                    Icons.Rounded.MoreVert, "Opciones", tint = Nocturne.TextPrimary,
                    modifier = Modifier.clip(CircleShape).clickable { actions.songOptions(song, null, -1) }.padding(8.dp).size(28.dp),
                )
            }

            // ---------- Carátula ----------
            BoxWithConstraints(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                val side = minOf(maxWidth, maxHeight) - 16.dp
                Box(Modifier.shadow(48.dp, RoundedCornerShape(28.dp), ambientColor = Nocturne.Purple, spotColor = Nocturne.Cyan)) {
                    Artwork(song, side, corner = 28.dp)
                }
            }

            // ---------- Título ----------
            Row(Modifier.fillMaxWidth().padding(top = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            song.title, color = Nocturne.TextPrimary, fontSize = 28.sp, fontWeight = FontWeight.Bold,
                            maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false),
                        )
                        if (song.isLossless) {
                            Spacer(Modifier.width(10.dp))
                            TagBadge("HI-RES")
                        }
                    }
                    Text(
                        song.artist, color = Nocturne.TextSecondary, fontSize = 19.sp, maxLines = 1, overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.clickable { actions.openCollection(com.mimusica.app.data.Collection.ArtistOf(song.artist)) },
                    )
                }
                val fav = LibraryStore.isFavorite(song.id)
                Icon(
                    if (fav) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder, "Me gusta",
                    tint = Nocturne.Lavender,
                    modifier = Modifier.clip(CircleShape).clickable { LibraryStore.toggleFavorite(song.id) }.padding(10.dp).size(30.dp),
                )
            }

            // ---------- Barra de progreso ----------
            Slider(
                value = if (duration > 0) (position.toFloat() / duration).coerceIn(0f, 1f) else 0f,
                onValueChange = { scrubbing = true; scrubValue = it },
                onValueChangeFinished = {
                    PlayerController.seekTo((scrubValue * duration).toLong())
                    scrubbing = false
                },
                colors = SliderDefaults.colors(
                    thumbColor = Nocturne.Lavender,
                    activeTrackColor = Nocturne.Purple,
                    inactiveTrackColor = Nocturne.SurfaceHigher,
                ),
                modifier = Modifier.padding(top = 12.dp),
            )
            Row(Modifier.fillMaxWidth()) {
                Text(formatDuration(position), color = Nocturne.TextSecondary, fontSize = 14.sp)
                Spacer(Modifier.weight(1f))
                Text(formatDuration(duration), color = Nocturne.TextSecondary, fontSize = 14.sp)
            }

            // ---------- Controles ----------
            Row(
                Modifier.fillMaxWidth().padding(vertical = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ToggleIcon(Icons.Rounded.Shuffle, "Aleatorio", PlayerController.shuffle) { PlayerController.toggleShuffle() }
                ControlIcon(Icons.Rounded.SkipPrevious, "Anterior") { PlayerController.previous() }
                GradientCircleButton(
                    if (PlayerController.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    if (PlayerController.isPlaying) "Pausar" else "Reproducir",
                    size = 86.dp,
                ) { PlayerController.togglePlay() }
                ControlIcon(Icons.Rounded.SkipNext, "Siguiente") { PlayerController.next() }
                ToggleIcon(
                    if (PlayerController.repeatMode == Player.REPEAT_MODE_ONE) Icons.Rounded.RepeatOne else Icons.Rounded.Repeat,
                    "Repetir", PlayerController.repeatMode != Player.REPEAT_MODE_OFF,
                ) { PlayerController.cycleRepeat() }
            }

            // ---------- Pie: calidad y cola ----------
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Nocturne.Surface.copy(alpha = 0.8f))
                    .padding(horizontal = 18.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                WaveBars(Modifier.size(22.dp), Brush.linearGradient(listOf(Nocturne.Cyan, Nocturne.Cyan)))
                Spacer(Modifier.width(12.dp))
                Text("${song.format}  •  Archivo local", color = Nocturne.TextSecondary, fontSize = 15.sp, modifier = Modifier.weight(1f))
                Row(
                    Modifier.clip(RoundedCornerShape(10.dp)).clickable { showQueue = true }.padding(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.AutoMirrored.Rounded.QueueMusic, null, tint = Nocturne.Cyan, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Cola", color = Nocturne.Cyan, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }

    if (showQueue) {
        ModalBottomSheet(onDismissRequest = { showQueue = false }, containerColor = Nocturne.Surface) {
            Text(
                "A continuación", color = Nocturne.TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            )
            LazyColumn(Modifier.heightIn(max = 520.dp).navigationBarsPadding()) {
                itemsIndexed(PlayerController.queue) { index, id ->
                    val s = MusicRepository.song(id) ?: return@itemsIndexed
                    val current = id == PlayerController.currentSongId
                    Row(
                        Modifier.fillMaxWidth().clickable { PlayerController.skipTo(index) }.padding(horizontal = 24.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Artwork(s, 48.dp, corner = 8.dp)
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(s.title, color = if (current) Nocturne.Lavender else Nocturne.TextPrimary, fontSize = 17.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(s.artist, color = Nocturne.TextSecondary, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
                item { Spacer(Modifier.height(12.dp)) }
            }
        }
    }
}

@Composable
private fun ControlIcon(icon: ImageVector, label: String, onClick: () -> Unit) {
    Icon(
        icon, label, tint = Nocturne.TextPrimary,
        modifier = Modifier.clip(CircleShape).clickable(onClick = onClick).padding(8.dp).size(44.dp),
    )
}

@Composable
private fun ToggleIcon(icon: ImageVector, label: String, active: Boolean, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            icon, label, tint = if (active) Nocturne.Cyan else Nocturne.TextSecondary,
            modifier = Modifier.clip(CircleShape).clickable(onClick = onClick).padding(8.dp).size(28.dp),
        )
        Box(Modifier.size(5.dp).clip(CircleShape).background(if (active) Nocturne.Cyan else Color.Transparent))
    }
}
