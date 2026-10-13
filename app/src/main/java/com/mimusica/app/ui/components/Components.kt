package com.mimusica.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mimusica.app.data.Song
import com.mimusica.app.data.formatDuration
import com.mimusica.app.ui.theme.Nocturne

/** Logo de Nocturne: barras de ecualizador con degradado en un cuadro oscuro. */
@Composable
fun NocturneLogo(size: Dp = 40.dp) {
    Box(
        Modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.22f))
            .background(Brush.linearGradient(listOf(Color(0xFF1E1B3A), Color(0xFF0E1A24)))),
        contentAlignment = Alignment.Center,
    ) {
        WaveBars(Modifier.size(size * 0.6f), Nocturne.Accent)
    }
}

@Composable
fun WaveBars(modifier: Modifier, brush: Brush) {
    Canvas(modifier) {
        val heights = listOf(0.35f, 0.65f, 1f, 0.65f, 0.35f)
        val gap = size.width / (heights.size * 2 - 1)
        heights.forEachIndexed { i, h ->
            val barH = size.height * h
            drawRoundRect(
                brush = brush,
                topLeft = Offset(i * gap * 2, (size.height - barH) / 2),
                size = Size(gap, barH),
                cornerRadius = CornerRadius(gap / 2),
            )
        }
    }
}

/** Avatar circular con la inicial del usuario. */
@Composable
fun UserAvatar(name: String, size: Dp = 44.dp, onClick: () -> Unit) {
    Box(
        Modifier
            .size(size)
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(Color(0xFF6D28D9), Color(0xFF0891B2))))
            .border(1.5.dp, Nocturne.SurfaceHigher, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            name.firstOrNull()?.uppercase() ?: "N",
            color = Color.White, fontWeight = FontWeight.Bold, fontSize = (size.value * 0.4f).sp,
        )
    }
}

/** Barra superior: logo + "Nocturne" + campana + avatar. */
@Composable
fun NocturneTopBar(userName: String, hasNews: Boolean, onBell: () -> Unit, onAvatar: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NocturneLogo()
        Spacer(Modifier.width(14.dp))
        Text("Nocturne", color = Nocturne.TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.weight(1f))
        Box {
            Icon(
                Icons.Rounded.NotificationsNone, "Notificaciones", tint = Nocturne.TextPrimary,
                modifier = Modifier.size(28.dp).clickable(onClick = onBell),
            )
            if (hasNews) {
                Box(Modifier.align(Alignment.TopEnd).size(8.dp).clip(CircleShape).background(Color(0xFFFF5A6E)))
            }
        }
        Spacer(Modifier.width(20.dp))
        UserAvatar(userName, onClick = onAvatar)
    }
}

/** Botón circular oscuro (corazón, aleatorio, buscar...). */
@Composable
fun CircleIconButton(
    icon: ImageVector,
    contentDescription: String,
    size: Dp = 52.dp,
    tint: Color = Nocturne.TextPrimary,
    background: Color = Nocturne.Surface,
    onClick: () -> Unit,
) {
    Box(
        Modifier.size(size).clip(CircleShape).background(background).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription, tint = tint, modifier = Modifier.size(size * 0.46f))
    }
}

/** Botón circular con el degradado morado → cian y brillo. */
@Composable
fun GradientCircleButton(
    icon: ImageVector,
    contentDescription: String,
    size: Dp = 64.dp,
    iconTint: Color = Color(0xFF1A1440),
    onClick: () -> Unit,
) {
    Box(
        Modifier
            .size(size)
            .shadow(18.dp, CircleShape, ambientColor = Nocturne.Purple, spotColor = Nocturne.Cyan)
            .clip(CircleShape)
            .background(Nocturne.Accent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription, tint = iconTint, modifier = Modifier.size(size * 0.5f))
    }
}

/** Chip tipo píldora de los filtros (Playlists, Artistas...). */
@Composable
fun NocturneChip(
    label: String,
    selected: Boolean,
    leading: ImageVector? = null,
    onClick: () -> Unit,
) {
    Row(
        Modifier
            .clip(CircleShape)
            .background(if (selected) Nocturne.PurpleDim else Nocturne.Surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 22.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leading != null) {
            Icon(leading, null, tint = Nocturne.Cyan, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(
            label,
            color = if (selected) Nocturne.Lavender else Nocturne.TextSecondary,
            fontSize = 17.sp,
        )
        if (selected) {
            Spacer(Modifier.width(10.dp))
            Box(Modifier.size(7.dp).clip(CircleShape).background(Nocturne.Lavender))
        }
    }
}

/** Etiqueta pequeña como "HI-RES" o "DAC ON". */
@Composable
fun TagBadge(text: String, color: Color = Nocturne.Lavender, background: Color = Nocturne.PurpleDim) {
    Text(
        text,
        color = color,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.8.sp,
        maxLines = 1,
        softWrap = false,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(background)
            .padding(horizontal = 8.dp, vertical = 4.dp),
    )
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier, action: String? = null, onAction: () -> Unit = {}) {
    Row(modifier.fillMaxWidth().padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(text, color = Nocturne.TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        if (action != null) {
            Text(action, color = Nocturne.Cyan, fontSize = 15.sp, modifier = Modifier.clickable(onClick = onAction))
        }
    }
}

/**
 * Fila de canción como en "Detalle de Playlist": número, título, artista • duración,
 * menú de opciones y asa para reordenar. La canción que suena se resalta.
 */
@Composable
fun SongRow(
    song: Song,
    number: Int?,
    isCurrent: Boolean,
    isPlaying: Boolean,
    onReorder: ((Int) -> Unit)? = null,
    onClick: () -> Unit,
    onMore: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (isCurrent) Nocturne.Surface else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = if (isCurrent) 6.dp else 14.dp, vertical = if (isCurrent) 6.dp else 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (isCurrent) {
            Box(contentAlignment = Alignment.Center) {
                Artwork(song, 60.dp, corner = 10.dp)
                if (isPlaying) {
                    Box(Modifier.size(60.dp).clip(RoundedCornerShape(10.dp)).background(Color.Black.copy(alpha = 0.45f)))
                    WaveBars(Modifier.size(30.dp), Brush.linearGradient(listOf(Nocturne.Cyan, Nocturne.Cyan)))
                }
            }
            Spacer(Modifier.width(14.dp))
        } else if (number != null) {
            Text(
                "%02d".format(number),
                color = Nocturne.TextMuted, fontSize = 17.sp,
                modifier = Modifier.width(36.dp),
            )
            Spacer(Modifier.width(10.dp))
        } else {
            Artwork(song, 52.dp, corner = 8.dp)
            Spacer(Modifier.width(14.dp))
        }
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    song.title,
                    color = if (isCurrent) Nocturne.Lavender else Nocturne.TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Normal,
                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (isCurrent && song.isLossless) {
                    Spacer(Modifier.width(8.dp))
                    TagBadge("HI-RES")
                }
            }
            Spacer(Modifier.height(2.dp))
            Row {
                Text(
                    song.artist, color = Nocturne.TextSecondary, fontSize = 15.sp,
                    maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false),
                )
                if (!isCurrent) {
                    Text(
                        "  •  ${formatDuration(song.durationMs)}",
                        color = Nocturne.TextSecondary, fontSize = 15.sp, maxLines = 1, softWrap = false,
                    )
                }
            }
        }
        Icon(
            Icons.Rounded.MoreVert, "Opciones", tint = Nocturne.TextSecondary,
            modifier = Modifier.size(40.dp).clip(CircleShape).clickable(onClick = onMore).padding(8.dp),
        )
        if (onReorder != null) {
            // Arrastra el asa hacia arriba o abajo para mover la canción dentro de la playlist.
            val stepPx = with(LocalDensity.current) { 72.dp.toPx() }
            var drag by remember { mutableFloatStateOf(0f) }
            val reorder by rememberUpdatedState(onReorder)
            Spacer(Modifier.width(12.dp))
            Icon(
                Icons.Rounded.DragHandle, "Reordenar", tint = Nocturne.TextSecondary,
                modifier = Modifier
                    .size(28.dp)
                    .pointerInput(Unit) {
                        detectVerticalDragGestures(onDragEnd = { drag = 0f }) { change, dy ->
                            change.consume()
                            drag += dy
                            if (drag > stepPx) { reorder(1); drag -= stepPx }
                            if (drag < -stepPx) { reorder(-1); drag += stepPx }
                        }
                    },
            )
            Spacer(Modifier.width(8.dp))
        }
    }
}

/** Mini reproductor flotante encima de la barra de navegación. */
@Composable
fun MiniPlayer(
    song: Song,
    isPlaying: Boolean,
    progress: Float,
    onOpen: () -> Unit,
    onTogglePlay: () -> Unit,
    onNext: () -> Unit,
) {
    Box(
        Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Nocturne.SurfaceHigh)
            .clickable(onClick = onOpen),
    ) {
        Row(
            Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier.size(52.dp).clip(RoundedCornerShape(12.dp)).background(Nocturne.Surface),
                contentAlignment = Alignment.Center,
            ) {
                val art = rememberAlbumArt(song)
                if (art != null) Artwork(song, 52.dp, corner = 12.dp)
                else WaveBars(Modifier.size(26.dp), Brush.linearGradient(listOf(Nocturne.Cyan, Nocturne.Cyan)))
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(song.title, color = Nocturne.TextPrimary, fontSize = 18.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(song.artist, color = Nocturne.TextSecondary, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Icon(
                Icons.Rounded.SkipNext, "Siguiente", tint = Nocturne.TextPrimary,
                modifier = Modifier.size(44.dp).clip(CircleShape).clickable(onClick = onNext).padding(8.dp),
            )
            Spacer(Modifier.width(10.dp))
            GradientCircleButton(
                if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                if (isPlaying) "Pausar" else "Reproducir",
                size = 56.dp, onClick = onTogglePlay,
            )
        }
        // Línea de progreso en el borde inferior
        Box(
            Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .height(2.dp)
                .background(Nocturne.Accent),
        )
    }
}

enum class Tab(val label: String, val icon: ImageVector) {
    Home("Inicio", Icons.Rounded.Home),
    Search("Buscar", Icons.Rounded.Search),
    Library("Biblioteca", Icons.Rounded.LibraryMusic),
    Settings("Ajustes", Icons.Rounded.Tune),
}

@Composable
fun NocturneBottomBar(selected: Tab, onSelect: (Tab) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(Nocturne.Background)
            .navigationBarsPadding()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        Tab.entries.forEach { tab ->
            val active = tab == selected
            Column(
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onSelect(tab) }
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    tab.icon, tab.label,
                    tint = if (active) Nocturne.Cyan else Nocturne.TextSecondary,
                    modifier = Modifier.size(28.dp),
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    tab.label,
                    color = if (active) Nocturne.Cyan else Nocturne.TextSecondary,
                    fontSize = 15.sp,
                    fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium,
                )
            }
        }
    }
}

/**
 * Aviso flotante de Nocturne: tarjeta oscura con borde degradado y un ícono en un círculo
 * con el degradado de la app. Si no hay ícono, muestra las barras del logo.
 */
@Composable
fun NocturneToast(message: String, icon: ImageVector?) {
    val shape = RoundedCornerShape(20.dp)
    Row(
        Modifier
            .padding(horizontal = 20.dp)
            .shadow(24.dp, shape, ambientColor = Nocturne.Purple, spotColor = Nocturne.Purple)
            .clip(shape)
            .background(Nocturne.SurfaceHigh)
            .border(
                1.dp,
                Brush.linearGradient(listOf(Nocturne.Purple.copy(alpha = 0.7f), Nocturne.Cyan.copy(alpha = 0.7f))),
                shape,
            )
            .padding(start = 10.dp, end = 18.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(36.dp).clip(CircleShape).background(Nocturne.Accent),
            contentAlignment = Alignment.Center,
        ) {
            val dark = Color(0xFF1A1440)
            if (icon != null) Icon(icon, null, tint = dark, modifier = Modifier.size(20.dp))
            else WaveBars(Modifier.size(18.dp), Brush.linearGradient(listOf(dark, dark)))
        }
        Spacer(Modifier.width(12.dp))
        Text(
            message,
            color = Nocturne.TextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
