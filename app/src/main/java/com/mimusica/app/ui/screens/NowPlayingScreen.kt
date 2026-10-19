package com.mimusica.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Bedtime
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
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
import androidx.compose.ui.input.pointer.pointerInput
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
import com.mimusica.app.ui.components.AnimatedArtwork
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
    var showSleep by remember { mutableStateOf(false) }
    // Mientras el usuario arrastra la barra, mostramos su posición en vez de la real.
    var scrubbing by remember { mutableStateOf(false) }
    var scrubValue by remember { mutableFloatStateOf(0f) }
    val duration = PlayerController.durationMs.takeIf { it > 0 } ?: song.durationMs
    val position = if (scrubbing) (scrubValue * duration).toLong() else PlayerController.positionMs

    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF231C45), Color(0xFF141A26), Nocturne.Background)))
            // El reproductor se dibuja encima de las demás pantallas: sin esto, un toque en una zona
            // sin botón (la carátula, el título…) atravesaba y activaba lo que hay debajo.
            .pointerInput(Unit) { detectTapGestures { } },
    ) {
        Column(
            Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // ---------- Barra superior ----------
            Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Rounded.KeyboardArrowDown, "Cerrar", tint = Nocturne.TextPrimary,
                    modifier = Modifier.clip(CircleShape).clickable { actions.back() }.padding(8.dp).size(28.dp),
                )
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("REPRODUCIENDO DESDE", color = Nocturne.Cyan, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.5.sp)
                    Text(song.album, color = Nocturne.TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Icon(
                    Icons.Rounded.MoreVert, "Opciones", tint = Nocturne.TextPrimary,
                    modifier = Modifier.clip(CircleShape).clickable { actions.songOptions(song, null) }.padding(10.dp).size(24.dp),
                )
            }

            // ---------- Carátula ----------
            BoxWithConstraints(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                val side = minOf(maxWidth - 16.dp, maxHeight - 16.dp, 340.dp)
                AnimatedArtwork(
                    song = song,
                    size = side,
                    playing = PlayerController.isPlaying,
                    progress = if (PlayerController.durationMs > 0) PlayerController.positionMs.toFloat() / PlayerController.durationMs else 0f,
                    base = LibraryStore.artworkBase,
                    effects = LibraryStore.artworkEffects,
                )
            }

            // ---------- Título ----------
            Row(Modifier.fillMaxWidth().padding(top = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            song.title, color = Nocturne.TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold,
                            maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false),
                        )
                        if (song.isLossless) {
                            Spacer(Modifier.width(10.dp))
                            TagBadge("HI-RES")
                        }
                    }
                    Text(
                        song.artist, color = Nocturne.TextSecondary, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.clickable {
                            actions.openCollection(
                                if (song.isWhatsApp) com.mimusica.app.data.Collection.WhatsApp
                                else com.mimusica.app.data.Collection.ArtistOf(song.artist),
                            )
                        },
                    )
                }
                val fav = LibraryStore.isFavorite(song.id)
                Icon(
                    if (fav) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder, "Me gusta",
                    tint = Nocturne.Lavender,
                    modifier = Modifier.clip(CircleShape).clickable { LibraryStore.toggleFavorite(song.id) }.padding(10.dp).size(26.dp),
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
                thumb = {
                    Box(Modifier.size(14.dp).shadow(4.dp, CircleShape).clip(CircleShape).background(Nocturne.Lavender))
                },
                track = { state ->
                    SliderDefaults.Track(
                        sliderState = state,
                        modifier = Modifier.height(4.dp),
                        colors = SliderDefaults.colors(
                            activeTrackColor = Nocturne.Purple,
                            inactiveTrackColor = Nocturne.SurfaceHigher,
                        ),
                        thumbTrackGapSize = 0.dp,
                        drawStopIndicator = null,
                    )
                },
                modifier = Modifier.padding(top = 8.dp),
            )
            Row(Modifier.fillMaxWidth()) {
                Text(formatDuration(position), color = Nocturne.TextSecondary, fontSize = 12.sp)
                Spacer(Modifier.weight(1f))
                Text(formatDuration(duration), color = Nocturne.TextSecondary, fontSize = 12.sp)
            }

            // ---------- Controles ----------
            Row(
                Modifier.fillMaxWidth().padding(vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ToggleIcon(Icons.Rounded.Shuffle, "Aleatorio", PlayerController.shuffle) {
                    val on = PlayerController.toggleShuffle()
                    actions.notify(if (on) "Aleatorio activado" else "Aleatorio desactivado", Icons.Rounded.Shuffle)
                }
                ControlIcon(Icons.Rounded.SkipPrevious, "Anterior") { PlayerController.previous() }
                GradientCircleButton(
                    if (PlayerController.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    if (PlayerController.isPlaying) "Pausar" else "Reproducir",
                    size = 72.dp,
                ) { PlayerController.togglePlay() }
                ControlIcon(Icons.Rounded.SkipNext, "Siguiente") { PlayerController.next() }
                ToggleIcon(
                    if (PlayerController.repeatMode == Player.REPEAT_MODE_ONE) Icons.Rounded.RepeatOne else Icons.Rounded.Repeat,
                    "Repetir", PlayerController.repeatMode != Player.REPEAT_MODE_OFF,
                ) {
                    when (PlayerController.cycleRepeat()) {
                        Player.REPEAT_MODE_ALL -> actions.notify("Repetir toda la lista", Icons.Rounded.Repeat)
                        Player.REPEAT_MODE_ONE -> actions.notify("Repetir esta canción", Icons.Rounded.RepeatOne)
                        else -> actions.notify("Repetición desactivada", Icons.Rounded.Repeat)
                    }
                }
            }

            // ---------- Pie: calidad y cola ----------
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Nocturne.Surface.copy(alpha = 0.8f))
                    .padding(start = 16.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                WaveBars(Modifier.size(20.dp), Brush.linearGradient(listOf(Nocturne.Cyan, Nocturne.Cyan)))
                Spacer(Modifier.width(10.dp))
                Text(song.format, color = Nocturne.TextSecondary, fontSize = 14.sp, maxLines = 1, modifier = Modifier.weight(1f))
                FooterButton(
                    Icons.Rounded.Bedtime, sleepLabel(),
                    active = PlayerController.sleepTimerActive,
                ) { showSleep = true }
                Spacer(Modifier.width(6.dp))
                FooterButton(Icons.AutoMirrored.Rounded.QueueMusic, "Cola", active = false) { showQueue = true }
            }
        }
    }

    if (showSleep) {
        SleepTimerSheet(onDismiss = { showSleep = false }) { msg ->
            showSleep = false
            actions.notify(msg, Icons.Rounded.Bedtime)
        }
    }

    if (showQueue) {
        // La cola se abre en la canción que está sonando, no al principio.
        val start = PlayerController.queue.indexOfFirst { it.songId == PlayerController.currentSongId }
        val listState = rememberLazyListState(initialFirstVisibleItemIndex = if (start >= 0) start else 0)
        ModalBottomSheet(onDismissRequest = { showQueue = false }, containerColor = Nocturne.Surface) {
            Text(
                "A continuación", color = Nocturne.TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            )
            LazyColumn(Modifier.heightIn(max = 520.dp).navigationBarsPadding(), state = listState) {
                itemsIndexed(PlayerController.queue) { index, item ->
                    val s = MusicRepository.song(item.songId) ?: return@itemsIndexed
                    val current = item.songId == PlayerController.currentSongId
                    Row(
                        Modifier.fillMaxWidth().clickable { PlayerController.skipTo(item.index) }.padding(horizontal = 24.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Artwork(s, 44.dp, corner = 8.dp)
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(s.title, color = if (current) Nocturne.Lavender else Nocturne.TextPrimary, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(s.artist, color = Nocturne.TextSecondary, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
                item { Spacer(Modifier.height(12.dp)) }
            }
        }
    }
}

/** Texto del botón del temporizador: "Dormir", la cuenta atrás o "Fin canción". */
@Composable
private fun sleepLabel(): String {
    val endsAt = PlayerController.sleepEndsAt
    if (endsAt == null) return if (PlayerController.sleepAtEndOfSong) "Fin canción" else "Dormir"
    var now by remember { mutableLongStateOf(android.os.SystemClock.elapsedRealtime()) }
    LaunchedEffect(endsAt) {
        while (true) {
            now = android.os.SystemClock.elapsedRealtime()
            kotlinx.coroutines.delay(1000)
        }
    }
    return formatDuration((endsAt - now).coerceAtLeast(0))
}

@Composable
private fun FooterButton(icon: ImageVector, label: String, active: Boolean, onClick: () -> Unit) {
    val color = if (active) Nocturne.Cyan else Nocturne.TextSecondary
    Row(
        Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (active) Nocturne.CyanDim.copy(alpha = 0.5f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(6.dp))
        Text(label, color = color, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
    }
}

@Composable
private fun ControlIcon(icon: ImageVector, label: String, onClick: () -> Unit) {
    Icon(
        icon, label, tint = Nocturne.TextPrimary,
        modifier = Modifier.clip(CircleShape).clickable(onClick = onClick).padding(8.dp).size(36.dp),
    )
}

@Composable
private fun ToggleIcon(icon: ImageVector, label: String, active: Boolean, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            icon, label, tint = if (active) Nocturne.Cyan else Nocturne.TextSecondary,
            modifier = Modifier.clip(CircleShape).clickable(onClick = onClick).padding(10.dp).size(24.dp),
        )
        Box(Modifier.size(4.dp).clip(CircleShape).background(if (active) Nocturne.Cyan else Color.Transparent))
    }
}
