package com.mimusica.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.ViewList
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DownloadForOffline
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mimusica.app.ui.AppActions
import com.mimusica.app.data.Collection
import com.mimusica.app.data.LibraryStore
import com.mimusica.app.data.MusicRepository
import com.mimusica.app.data.Song
import com.mimusica.app.data.formatSize
import com.mimusica.app.playback.PlayerController
import com.mimusica.app.ui.components.Artwork
import com.mimusica.app.ui.components.CircleIconButton
import com.mimusica.app.ui.components.GradientCircleButton
import com.mimusica.app.ui.components.NocturneChip
import com.mimusica.app.ui.components.NocturneTopBar
import com.mimusica.app.ui.components.SongRow
import com.mimusica.app.ui.components.TagBadge
import com.mimusica.app.ui.components.Tab
import com.mimusica.app.ui.components.WaveBars
import com.mimusica.app.ui.theme.Nocturne

private enum class LibraryFilter(val label: String) { Playlists("Playlists"), Artists("Artistas"), Albums("Álbumes"), Downloads("Descargas"), WhatsApp("WhatsApp") }

/** Un elemento de la biblioteca (playlist, álbum o artista). */
private data class LibraryItem(
    val collection: Collection,
    val title: String,
    val subtitle: String,
    val cover: Song?,
    val sortTime: Long,
    val overline: Boolean = false,
    val circle: Boolean = false,
    val verified: Boolean = false,
    val coverBrush: Brush? = null,
    val coverIcon: ImageVector? = null,
)

@Composable
fun LibraryScreen(actions: AppActions, contentPadding: PaddingValues) {
    var filter by rememberSaveable { mutableStateOf(LibraryFilter.Playlists) }
    var sortRecent by rememberSaveable { mutableStateOf(true) }
    var grid by rememberSaveable { mutableStateOf(false) }

    val songs = MusicRepository.songs
    val items: List<LibraryItem> = when (filter) {
        LibraryFilter.Playlists -> playlistItems()
        LibraryFilter.Artists -> MusicRepository.artists.map {
            LibraryItem(
                Collection.ArtistOf(it.name), it.name,
                "Artista  •  ${it.songs.size} canciones", it.songs.first(),
                it.songs.maxOf { s -> s.dateAdded }, circle = true,
                verified = LibraryStore.isPinned(Collection.ArtistOf(it.name).key),
            )
        }
        LibraryFilter.Albums -> MusicRepository.albums.map {
            LibraryItem(
                Collection.AlbumOf(it.id), it.title,
                "Álbum  •  ${it.artist}  •  ${it.songs.size} canciones", it.songs.first(),
                it.songs.maxOf { s -> s.dateAdded },
            )
        }
        LibraryFilter.Downloads -> emptyList()
        LibraryFilter.WhatsApp -> emptyList()
    }.let { list ->
        if (filter == LibraryFilter.Playlists) list // los fijados primero, ya vienen ordenados
        else if (sortRecent) list.sortedByDescending { it.sortTime } else list.sortedBy { it.title.lowercase() }
    }
    val downloads = songs.filter { it.isDownload }
        .let { if (sortRecent) it else it.sortedBy { s -> s.title.lowercase() } }

    val whatsapp = MusicRepository.whatsappAudios
        .let { if (sortRecent) it else it.sortedBy { s -> s.title.lowercase() } }
    LazyColumn(contentPadding = contentPadding) {
        item {
            NocturneTopBar(
                LibraryStore.userName,
                hasNews = MusicRepository.newSongs.isNotEmpty(),
                onBell = { actions.showNews() },
                onAvatar = { actions.goToTab(Tab.Settings) },
            )
        }
        item {
            Row(
                Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Tu Biblioteca", color = Nocturne.TextPrimary, fontSize = 34.sp, fontWeight = FontWeight.Bold)
                Box(Modifier.padding(start = 6.dp, bottom = 14.dp).size(9.dp).clip(CircleShape).background(Nocturne.Cyan))
                Spacer(Modifier.weight(1f))
                CircleIconButton(Icons.Rounded.Search, "Buscar", size = 58.dp) { actions.goToTab(Tab.Search) }
                Spacer(Modifier.width(12.dp))
                GradientCircleButton(Icons.Rounded.Add, "Crear playlist", size = 62.dp, iconTint = Color(0xFF3B1E8F)) {
                    actions.createPlaylist()
                }
            }
        }
        item {
            Row(
                Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                LibraryFilter.entries.forEach { f ->
                    NocturneChip(
                        f.label, selected = f == filter,
                        leading = when (f) {
                            LibraryFilter.WhatsApp -> Icons.Rounded.Share
                            LibraryFilter.Downloads -> Icons.Rounded.DownloadForOffline
                            else -> null
                        }

                    ) { filter = f }
                }
            }
        }
        item {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 22.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    Modifier.clip(RoundedCornerShape(10.dp)).clickable { sortRecent = !sortRecent }.padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Rounded.SwapVert, null, tint = Nocturne.TextSecondary, modifier = Modifier.size(26.dp))
                    Spacer(Modifier.width(12.dp))
                    Text(if (sortRecent) "Más recientes" else "Alfabético (A–Z)", color = Nocturne.TextSecondary, fontSize = 18.sp)
                }
                Spacer(Modifier.weight(1f))
                Row(Modifier.clip(RoundedCornerShape(14.dp)).background(Nocturne.Surface).padding(4.dp)) {
                    ViewToggle(Icons.AutoMirrored.Rounded.ViewList, "Lista", !grid) { grid = false }
                    ViewToggle(Icons.Rounded.GridView, "Cuadrícula", grid) { grid = true }
                }
            }
        }

        when {
            !MusicRepository.loaded -> item { InfoCard("Buscando música en tu teléfono…") }
            filter == LibraryFilter.Downloads -> {
                if (downloads.isEmpty()) item { InfoCard("No hay canciones en la carpeta Descargas.") }
                items(downloads, key = { "dl" + it.id }) { song ->
                    SongRow(
                        song, number = null,
                        isCurrent = PlayerController.currentSongId == song.id,
                        isPlaying = PlayerController.isPlaying,
                        onClick = { PlayerController.play(downloads, downloads.indexOf(song)) },
                        onMore = { actions.songOptions(song, null) },
                    )
                }
            }
            filter == LibraryFilter.WhatsApp -> {
                if (whatsapp.isEmpty()) item { InfoCard("No hay audios en la carpeta de WhatsApp.") }
                items(whatsapp, key = { "wa" + it.id }) { song ->
                    SongRow(
                        song, number = null,
                        isCurrent = PlayerController.currentSongId == song.id,
                        isPlaying = PlayerController.isPlaying,
                        onClick = { PlayerController.play(whatsapp, whatsapp.indexOf(song)) },
                        onMore = { actions.songOptions(song, null) },
                    )
                }
            }
            items.isEmpty() -> item { InfoCard("Aún no hay nada aquí. Crea una playlist con el botón +.") }
            grid -> items(items.chunked(2), key = { it.first().collection.key }) { pair ->
                Row(Modifier.padding(horizontal = 20.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    pair.forEach { GridCard(it, Modifier.weight(1f)) { actions.openCollection(it.collection) } }
                    if (pair.size == 1) Spacer(Modifier.weight(1f))
                }
            }
            else -> items(items, key = { it.collection.key }) { item ->
                LibraryRow(item, actions)
            }
        }

        if (songs.isNotEmpty()) {
            item { StorageCard(songs) { actions.goToTab(Tab.Settings) } }
        }
    }
}

private fun playlistItems(): List<LibraryItem> {
    val favs = LibraryStore.favorites.mapNotNull { MusicRepository.song(it) }
    val result = mutableListOf(
        LibraryItem(
            Collection.Favorites, "Canciones que te gustan", "${favs.size} canciones guardadas",
            null, Long.MAX_VALUE, overline = true,
            coverBrush = Brush.linearGradient(listOf(Color(0xFF8B5CF6), Color(0xFF22B8CF))),
            coverIcon = Icons.Rounded.Favorite,
        ),
    )
    // Álbumes y artistas que el usuario fijó con el corazón
    LibraryStore.pinned.mapNotNull { Collection.fromKey(it) }.forEach { c ->
        val info = resolve(c) ?: return@forEach
        if (c is Collection.UserPlaylist) return@forEach
        result += LibraryItem(
            c, info.title,
            if (c is Collection.ArtistOf) "Artista  •  ${info.songs.size} canciones"
            else "${info.kind}  •  ${info.owner}  •  ${info.songs.size} canciones",
            info.cover, 0L, circle = c is Collection.ArtistOf, verified = c is Collection.ArtistOf,
        )
    }
    val playlists = LibraryStore.playlists.map { p ->
        LibraryItem(
            Collection.UserPlaylist(p.id), p.name,
            "Playlist  •  Creada por ti  •  ${p.songIds.size} canciones",
            p.songIds.firstOrNull()?.let { MusicRepository.song(it) }, p.createdAt,
        )
    }
    return listOf(result.first()) + playlists + result.drop(1)
}

@Composable
private fun ViewToggle(icon: ImageVector, label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .size(width = 50.dp, height = 44.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) Nocturne.SurfaceHigher else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, label, tint = if (selected) Nocturne.Cyan else Nocturne.TextSecondary, modifier = Modifier.size(28.dp))
    }
}

@Composable
private fun LibraryRow(item: LibraryItem, actions: AppActions) {
    var menu by androidx.compose.runtime.remember { mutableStateOf(false) }
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (item.overline) Nocturne.Surface else Color.Transparent)
            .clickable { actions.openCollection(item.collection) }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Artwork(
            item.cover, 84.dp,
            corner = if (item.circle) 42.dp else 12.dp,
            fallbackBrush = item.coverBrush,
            fallbackIcon = item.coverIcon ?: if (item.circle) Icons.Rounded.Person else Icons.Rounded.GraphicEq,
        )
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            if (item.overline) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("FIJADO", color = Nocturne.Lavender, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp)
                    Box(Modifier.padding(horizontal = 8.dp).size(4.dp).clip(CircleShape).background(Nocturne.TextMuted))
                    Text("Auto-sync", color = Nocturne.Cyan, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    item.title, color = Nocturne.TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.SemiBold,
                    maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false),
                )
                if (item.verified) {
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.Rounded.Verified, "Fijado", tint = Nocturne.Cyan, modifier = Modifier.size(22.dp))
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(item.subtitle, color = Nocturne.TextSecondary, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        when {
            item.overline -> Icon(Icons.Rounded.PushPin, "Fijado", tint = Nocturne.Lavender, modifier = Modifier.padding(8.dp).size(24.dp))
            item.circle -> Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, null, tint = Nocturne.TextSecondary, modifier = Modifier.padding(12.dp).size(30.dp))
            else -> Box {
                Icon(
                    Icons.Rounded.MoreVert, "Opciones", tint = Nocturne.TextSecondary,
                    modifier = Modifier.clip(CircleShape).clickable { menu = true }.padding(12.dp).size(26.dp),
                )
                DropdownMenu(menu, onDismissRequest = { menu = false }, containerColor = Nocturne.SurfaceHigh) {
                    CollectionMenuItems(item.collection, actions) { menu = false }
                }
            }
        }
    }
}

/** Opciones comunes del menú de una colección (fijar, renombrar, eliminar). */
@Composable
fun CollectionMenuItems(collection: Collection, actions: AppActions, close: () -> Unit) {
    val info = resolve(collection)
    DropdownMenuItem(
        text = { Text("Reproducir", color = Nocturne.TextPrimary) },
        onClick = { info?.let { PlayerController.play(it.songs) }; close() },
    )
    DropdownMenuItem(
        text = { Text("Agregar a una playlist", color = Nocturne.TextPrimary) },
        onClick = { info?.let { actions.addToPlaylist(it.songs.map { s -> s.id }) }; close() },
    )
    if (collection !is Collection.Favorites) {
        DropdownMenuItem(
            text = { Text(if (LibraryStore.isPinned(collection.key)) "Quitar de la biblioteca" else "Fijar en la biblioteca", color = Nocturne.TextPrimary) },
            onClick = { LibraryStore.togglePinned(collection.key); close() },
        )
    }
    if (collection is Collection.UserPlaylist) {
        DropdownMenuItem(
            text = { Text("Renombrar", color = Nocturne.TextPrimary) },
            onClick = { actions.renamePlaylist(collection.id); close() },
        )
        DropdownMenuItem(
            text = { Text("Eliminar playlist", color = Color(0xFFFF8A9A)) },
            onClick = { actions.deletePlaylist(collection.id); close() },
        )
    }
}

@Composable
private fun GridCard(item: LibraryItem, modifier: Modifier, onClick: () -> Unit) {
    Column(modifier.clip(RoundedCornerShape(14.dp)).clickable(onClick = onClick).padding(4.dp)) {
        Box(Modifier.fillMaxWidth().aspectRatio(1f)) {
            androidx.compose.foundation.layout.BoxWithConstraints {
                Artwork(
                    item.cover, maxWidth,
                    corner = if (item.circle) maxWidth / 2 else 14.dp,
                    fallbackBrush = item.coverBrush,
                    fallbackIcon = item.coverIcon ?: if (item.circle) Icons.Rounded.Person else Icons.Rounded.GraphicEq,
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        Text(item.title, color = Nocturne.TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(item.subtitle, color = Nocturne.TextSecondary, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

/** Tarjeta inferior con el resumen de la música guardada en el teléfono. */
@Composable
private fun StorageCard(songs: List<Song>, onManage: () -> Unit) {
    val lossless = songs.count { it.isLossless }
    Row(
        Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 28.dp, bottom = 12.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Nocturne.Surface)
            .padding(horizontal = 18.dp, vertical = 22.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(52.dp).clip(RoundedCornerShape(14.dp)).background(Nocturne.SurfaceHigh),
            contentAlignment = Alignment.Center,
        ) {
            WaveBars(Modifier.size(30.dp), Brush.linearGradient(listOf(Nocturne.Cyan, Nocturne.Cyan)))
        }
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Música local", color = Nocturne.TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                Spacer(Modifier.width(8.dp))
                TagBadge(if (lossless > 0) "HI-RES" else songs.first().format, color = Nocturne.Cyan, background = Nocturne.CyanDim)
            }
            Text(
                "${formatSize(songs.sumOf { it.sizeBytes })}  •  ${songs.size} canciones",
                color = Nocturne.TextSecondary, fontSize = 14.sp, maxLines = 2, overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(
            "Gestionar", color = Nocturne.TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(Nocturne.SurfaceHigher).clickable(onClick = onManage)
                .padding(horizontal = 14.dp, vertical = 8.dp),
        )
    }
}
