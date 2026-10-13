package com.mimusica.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DownloadForOffline
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mimusica.app.ui.AppActions
import com.mimusica.app.data.Collection
import com.mimusica.app.data.LibraryStore
import com.mimusica.app.data.MusicRepository
import com.mimusica.app.data.Song
import com.mimusica.app.playback.PlayerController
import com.mimusica.app.ui.components.Artwork
import com.mimusica.app.ui.components.GradientCircleButton
import com.mimusica.app.ui.components.NocturneTopBar
import com.mimusica.app.ui.components.SectionTitle
import com.mimusica.app.ui.components.Tab
import com.mimusica.app.ui.theme.Nocturne
import java.util.Calendar
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(actions: AppActions, contentPadding: PaddingValues) {
    val songs = MusicRepository.songs
    val recents = LibraryStore.recents.mapNotNull { MusicRepository.song(it) }.take(12)
    val newAlbums = MusicRepository.albums.sortedByDescending { a -> a.songs.maxOf { it.dateAdded } }.take(12)
    val artists = MusicRepository.artists.sortedByDescending { it.songs.size }.take(12)
    val hour = remember{mutableStateOf(Calendar.getInstance().get(Calendar.HOUR_OF_DAY))}
    val greeting = when (hour.value) {
        in 5..11 -> "Buenos días"
        in 12..18 -> "Buenas tardes"
        else -> "Buenas noches"
    }
    val name = LibraryStore.userName.trim().substringBefore(" ")

    LaunchedEffect(Unit) {
        while(true) {
            hour.value = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            delay(60_000)
        }
    }
    LazyColumn(contentPadding = contentPadding) {
        item {
            NocturneTopBar(name, MusicRepository.newSongs.isNotEmpty(), onBell = { actions.showNews() }, onAvatar = { actions.goToTab(Tab.Settings) })
        }
        item {
            Column(Modifier.padding(start = 20.dp, top = 8.dp, bottom = 20.dp), horizontalAlignment = Alignment.Start) {
                if (name.isNotBlank()) {
                    Text("$greeting,", color = Nocturne.TextSecondary, fontSize = 18.sp)
                    Spacer(Modifier.height(2.dp))
                }
                // El punto cian va pegado al final de la última línea, como en "Tu Biblioteca".
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = name.ifBlank { greeting },
                        color = Nocturne.TextPrimary, fontSize = 30.sp, fontWeight = FontWeight.Bold,
                    )
                    Box(Modifier.padding(start = 5.dp, bottom = 9.dp).size(8.dp).clip(CircleShape).background(Nocturne.Cyan))
                }
            }
        }

        if (MusicRepository.loaded && songs.isEmpty()) {
            item {
                InfoCard(
                    "No encontramos canciones en tu teléfono. Copia archivos de música (MP3, FLAC…) a la carpeta Música o Descargas y vuelve a escanear desde Ajustes.",
                )
            }
            return@LazyColumn
        }

        // ---------- Accesos rápidos ----------
        item {
            Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    QuickTile(
                        "Me gusta", Icons.Rounded.Favorite,
                        Brush.linearGradient(listOf(Color(0xFF8B5CF6), Color(0xFF22B8CF))), Modifier.weight(1f),
                    ) { actions.openCollection(Collection.Favorites) }
                    QuickTile(
                        "Descargas", Icons.Rounded.DownloadForOffline,
                        Brush.linearGradient(listOf(Color(0xFF0F766E), Color(0xFF6366F1))), Modifier.weight(1f),
                    ) { actions.openCollection(Collection.Downloads) }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    QuickTile(
                        "WhatsApp", Icons.Rounded.Share,
                        Brush.linearGradient(listOf(Color(0xFF25D366), Color(0xFF128C7E))), Modifier.weight(1f),
                    ) { actions.openCollection(Collection.WhatsApp)}
                    QuickTile(
                        "Nueva playlist", Icons.Rounded.Add, Brush.linearGradient(listOf(Nocturne.SurfaceHigh, Nocturne.SurfaceHigher)),
                        Modifier.weight(1f),
                    ) { actions.createPlaylist() }
                }
            }
        }

        // ---------- Mezcla aleatoria ----------
        if (songs.isNotEmpty()) {
            item {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFF2A2350), Color(0xFF16303C))))
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("MEZCLA NOCTURNA", color = Nocturne.Cyan, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
                        Spacer(Modifier.height(4.dp))
                        Text("Toda tu música en aleatorio", color = Nocturne.TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                        Text("${songs.size} canciones", color = Nocturne.TextSecondary, fontSize = 14.sp)
                    }
                    Spacer(Modifier.width(12.dp))
                    GradientCircleButton(Icons.Rounded.Shuffle, "Aleatorio", size = 52.dp) {
                        PlayerController.play(songs, shuffled = true)
                        actions.notify("Mezcla nocturna en aleatorio", Icons.Rounded.Shuffle)
                    }
                }
            }
        }

        if (recents.isNotEmpty()) {
            item { SectionTitle("Escuchado recientemente") }
            item {
                LazyRow(contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(recents, key = { it.id }) { s -> SongCard(s) { PlayerController.play(recents, recents.indexOf(s)) } }
                }
            }
        }
        if (newAlbums.isNotEmpty()) {
            item { SectionTitle("Agregados recientemente", action = "Ver todo") { actions.goToTab(Tab.Library) } }
            item {
                LazyRow(contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(newAlbums, key = { it.id }) { a ->
                        Column(Modifier.width(150.dp).clickable { actions.openCollection(Collection.AlbumOf(a.id)) }) {
                            Artwork(a.songs.first(), 150.dp, corner = 14.dp)
                            Spacer(Modifier.height(8.dp))
                            Text(a.title, color = Nocturne.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(a.artist, color = Nocturne.TextSecondary, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }
        if (artists.isNotEmpty()) {
            item { SectionTitle("Tus artistas") }
            item {
                LazyRow(contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(artists, key = { it.name }) { a ->
                        Column(
                            Modifier.width(110.dp).clickable { actions.openCollection(Collection.ArtistOf(a.name)) },
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Artwork(a.songs.first(), 110.dp, corner = 55.dp, fallbackIcon = Icons.Rounded.Person)
                            Spacer(Modifier.height(8.dp))
                            Text(a.name, color = Nocturne.TextPrimary, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickTile(
    title: String,
    icon: ImageVector?,
    brush: Brush?,
    modifier: Modifier,
    cover: Song? = null,
    onClick: () -> Unit,
) {
    Row(
        modifier.clip(RoundedCornerShape(14.dp)).background(Nocturne.Surface).clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Artwork(cover, 48.dp, corner = 0.dp, fallbackBrush = brush, fallbackIcon = icon ?: Icons.Rounded.PlayArrow)
        Spacer(Modifier.width(12.dp))
        Text(title, color = Nocturne.TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(end = 8.dp))
    }
}

@Composable
private fun SongCard(song: Song, onClick: () -> Unit) {
    Column(Modifier.width(150.dp).clickable(onClick = onClick)) {
        Artwork(song, 150.dp, corner = 14.dp)
        Spacer(Modifier.height(8.dp))
        Text(song.title, color = Nocturne.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(song.artist, color = Nocturne.TextSecondary, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}
