package com.mimusica.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mimusica.app.ui.AppActions
import com.mimusica.app.data.Collection
import com.mimusica.app.data.MusicRepository
import com.mimusica.app.data.Song
import com.mimusica.app.playback.PlayerController
import com.mimusica.app.ui.components.Artwork
import com.mimusica.app.ui.components.SectionTitle
import com.mimusica.app.ui.components.SongRow
import com.mimusica.app.ui.theme.Nocturne
import java.text.Normalizer

private fun String.norm() = Normalizer.normalize(lowercase(), Normalizer.Form.NFD).replace(Regex("\\p{Mn}+"), "")

@Composable
fun SearchScreen(actions: AppActions, contentPadding: PaddingValues) {
    var query by rememberSaveable { mutableStateOf("") }
    val q = query.trim().norm()
    val all = MusicRepository.songs
    val songs: List<Song> = if (q.isEmpty()) all.sortedBy { it.title.lowercase() }
    else all.filter { it.title.norm().contains(q) || it.artist.norm().contains(q) || it.album.norm().contains(q) }
    val artists = if (q.isEmpty()) emptyList() else MusicRepository.artists.filter { it.name.norm().contains(q) }.take(8)
    val albums = if (q.isEmpty()) emptyList() else MusicRepository.albums.filter { it.title.norm().contains(q) }.take(8)

    LazyColumn(contentPadding = contentPadding) {
        item {
            Text(
                "Buscar", color = Nocturne.TextPrimary, fontSize = 38.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 20.dp, top = 24.dp, bottom = 16.dp),
            )
        }
        item {
            TextField(
                value = query,
                onValueChange = { query = it },
                singleLine = true,
                placeholder = { Text("Canciones, artistas o álbumes", color = Nocturne.TextMuted, fontSize = 17.sp) },
                leadingIcon = { Icon(Icons.Rounded.Search, null, tint = Nocturne.TextSecondary) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        Icon(Icons.Rounded.Close, "Borrar", tint = Nocturne.TextSecondary, modifier = Modifier.clickable { query = "" })
                    }
                },
                shape = RoundedCornerShape(18.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Nocturne.Surface,
                    unfocusedContainerColor = Nocturne.Surface,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = Nocturne.TextPrimary,
                    unfocusedTextColor = Nocturne.TextPrimary,
                    cursorColor = Nocturne.Cyan,
                ),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            )
        }

        if (artists.isNotEmpty()) {
            item { SectionTitle("Artistas", Modifier.padding(top = 24.dp, bottom = 8.dp)) }
            items(artists, key = { "ar" + it.name }) { a ->
                ResultRow(a.songs.first(), a.name, "Artista  •  ${a.songs.size} canciones", circle = true) {
                    actions.openCollection(Collection.ArtistOf(a.name))
                }
            }
        }
        if (albums.isNotEmpty()) {
            item { SectionTitle("Álbumes", Modifier.padding(top = 24.dp, bottom = 8.dp)) }
            items(albums, key = { "al" + it.id }) { a ->
                ResultRow(a.songs.first(), a.title, "Álbum  •  ${a.artist}", circle = false) {
                    actions.openCollection(Collection.AlbumOf(a.id))
                }
            }
        }
        item {
            SectionTitle(
                if (q.isEmpty()) "Todas las canciones" else "Canciones",
                Modifier.padding(top = 24.dp, bottom = 8.dp),
            )
        }
        if (songs.isEmpty()) {
            item { InfoCard(if (q.isEmpty()) "No hay canciones en el teléfono." else "Sin resultados para \"$query\".") }
        }
        items(songs, key = { "s" + it.id }) { s ->
            SongRow(
                s, number = null,
                isCurrent = PlayerController.currentSongId == s.id,
                isPlaying = PlayerController.isPlaying,
                onClick = { PlayerController.play(songs, songs.indexOf(s)) },
                onMore = { actions.songOptions(s, null, -1) },
            )
        }
    }
}

@Composable
private fun ResultRow(cover: Song, title: String, subtitle: String, circle: Boolean, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 30.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Artwork(cover, 56.dp, corner = if (circle) 28.dp else 8.dp, fallbackIcon = if (circle) Icons.Rounded.Person else Icons.Rounded.Search)
        Spacer(Modifier.width(14.dp))
        Column {
            Text(title, color = Nocturne.TextPrimary, fontSize = 18.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(subtitle, color = Nocturne.TextSecondary, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

