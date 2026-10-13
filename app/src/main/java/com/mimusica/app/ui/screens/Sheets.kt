package com.mimusica.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.PlaylistAdd
import androidx.compose.material.icons.automirrored.rounded.PlaylistPlay
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.RemoveCircleOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mimusica.app.data.Collection
import com.mimusica.app.data.LibraryStore
import com.mimusica.app.data.Song
import com.mimusica.app.ui.components.Artwork
import com.mimusica.app.ui.theme.Nocturne

@Composable
private fun SheetRow(icon: ImageVector, label: String, tint: Color = Nocturne.TextPrimary, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(26.dp))
        Spacer(Modifier.width(20.dp))
        Text(label, color = tint, fontSize = 18.sp)
    }
}

/** Menú "⋮" de una canción. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongOptionsSheet(
    song: Song,
    fromPlaylistId: Long?,
    onDismiss: () -> Unit,
    onPlayNext: () -> Unit,
    onAddToQueue: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onRemoveFromPlaylist: () -> Unit,
    onOpen: (Collection) -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Nocturne.Surface) {
        Column(Modifier.navigationBarsPadding().padding(bottom = 12.dp)) {
            Row(Modifier.padding(horizontal = 24.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Artwork(song, 56.dp)
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(song.title, color = Nocturne.TextPrimary, fontSize = 19.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("${song.artist}  •  ${song.format}", color = Nocturne.TextSecondary, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            Spacer(Modifier.height(8.dp))
            val fav = LibraryStore.isFavorite(song.id)
            SheetRow(
                if (fav) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                if (fav) "Quitar de Me gusta" else "Agregar a Me gusta",
                tint = if (fav) Nocturne.Lavender else Nocturne.TextPrimary,
            ) { LibraryStore.toggleFavorite(song.id); onDismiss() }
            SheetRow(Icons.AutoMirrored.Rounded.PlaylistPlay, "Reproducir a continuación") { onPlayNext(); onDismiss() }
            SheetRow(Icons.AutoMirrored.Rounded.QueueMusic, "Agregar a la cola") { onAddToQueue(); onDismiss() }
            SheetRow(Icons.AutoMirrored.Rounded.PlaylistAdd, "Agregar a una playlist") { onAddToPlaylist() }
            if (fromPlaylistId != null) {
                SheetRow(Icons.Rounded.RemoveCircleOutline, "Quitar de esta playlist", tint = Color(0xFFFF8A9A)) {
                    onRemoveFromPlaylist(); onDismiss()
                }
            }
            SheetRow(Icons.Rounded.Album, "Ir al álbum") { onOpen(Collection.AlbumOf(song.albumId)); onDismiss() }
            SheetRow(Icons.Rounded.Person, "Ir al artista") { onOpen(Collection.ArtistOf(song.artist)); onDismiss() }
        }
    }
}

/** Elegir a qué playlist agregar una o varias canciones. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddToPlaylistSheet(songIds: List<Long>, onDismiss: () -> Unit, onDone: (String) -> Unit) {
    var creating by remember { mutableStateOf(false) }
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Nocturne.Surface) {
        Column(Modifier.navigationBarsPadding().padding(bottom = 12.dp)) {
            Text(
                "Agregar a playlist", color = Nocturne.TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            )
            SheetRow(Icons.Rounded.Add, "Nueva playlist", tint = Nocturne.Cyan) { creating = true }
            LazyColumn(Modifier.heightIn(max = 420.dp)) {
                items(LibraryStore.playlists, key = { it.id }) { p ->
                    Row(
                        Modifier.fillMaxWidth().clickable {
                            LibraryStore.addToPlaylist(p.id, songIds)
                            onDone("Agregado a ${p.name}")
                        }.padding(horizontal = 24.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Artwork(p.songIds.firstOrNull()?.let { com.mimusica.app.data.MusicRepository.song(it) }, 48.dp, corner = 8.dp)
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(p.name, color = Nocturne.TextPrimary, fontSize = 18.sp)
                            Text("${p.songIds.size} canciones", color = Nocturne.TextSecondary, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
    if (creating) {
        NameDialog("Nueva playlist", "", onDismiss = { creating = false }) { name ->
            LibraryStore.createPlaylist(name, songIds)
            creating = false
            onDone("Playlist \"$name\" creada")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsSheet(songs: List<Song>, onDismiss: () -> Unit, onPlay: (List<Song>, Int) -> Unit, onOpenWhatsApp: () -> Unit,){
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Nocturne.Surface) {
        val (songNewsList, whatsappNewsList) = songs.partition { !it.isWhatsApp }
        Column(Modifier.navigationBarsPadding().padding(bottom = 12.dp)) {
            Text(
                "Novedades de la App",
                color = Nocturne.TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            )
        }
        if (songNewsList.isNotEmpty()) {
            Text(
                "${songNewsList.size} Canciones Nuevas!!!!"
            )
            LazyColumn(Modifier.heightIn(max = 420.dp)) {
                itemsIndexed(songNewsList) { index, song ->
                    Row(
                        Modifier.fillMaxWidth().clickable {
                            onPlay(songNewsList, index)
                        }.padding(horizontal = 24.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Artwork( song , 48.dp, corner = 8.dp)
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(song.title, color = Nocturne.TextPrimary, fontSize = 18.sp)
                            Text(song.artist, color = Nocturne.TextSecondary, fontSize = 14.sp)
                        }
                    }
                }
            }
        } else {
            Text(
                "No hay ninguna cancion nueva"
            )
        }
        if (whatsappNewsList.isNotEmpty()) {
            Text(
                "${whatsappNewsList.size} Canciones Nuevas!!!!"
            )
            LazyColumn(Modifier.heightIn(max = 420.dp)) {
                itemsIndexed(whatsappNewsList) { index, song ->
                    Row(
                        Modifier.fillMaxWidth().clickable {
                            onPlay(whatsappNewsList, index)
                        }.padding(horizontal = 24.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Artwork( song , 48.dp, corner = 8.dp)
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(song.title, color = Nocturne.TextPrimary, fontSize = 18.sp)
                            Text(song.artist, color = Nocturne.TextSecondary, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}

/** Diálogo para escribir un nombre (crear o renombrar playlist). */
@Composable
fun NameDialog(title: String, initial: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var text by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Nocturne.Surface,
        title = { Text(title, color = Nocturne.TextPrimary) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it.take(60) },
                singleLine = true,
                placeholder = { Text("Nombre", color = Nocturne.TextMuted) },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Nocturne.Purple,
                    unfocusedBorderColor = Nocturne.SurfaceHigher,
                    focusedTextColor = Nocturne.TextPrimary,
                    unfocusedTextColor = Nocturne.TextPrimary,
                    cursorColor = Nocturne.Cyan,
                ),
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(text) }, enabled = text.isNotBlank()) {
                Text("Guardar", color = if (text.isNotBlank()) Nocturne.Cyan else Nocturne.TextMuted)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar", color = Nocturne.TextSecondary) } },
    )
}

@Composable
fun ConfirmDialog(title: String, message: String, confirm: String, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Nocturne.Surface,
        title = { Text(title, color = Nocturne.TextPrimary) },
        text = { Text(message, color = Nocturne.TextSecondary) },
        confirmButton = { TextButton(onClick = onConfirm) { Text(confirm, color = Color(0xFFFF8A9A)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar", color = Nocturne.TextSecondary) } },
    )
}

/** Pequeño recuadro informativo con el estilo de las tarjetas del diseño. */
@Composable
fun InfoCard(text: String, modifier: Modifier = Modifier) {
    Text(
        text, color = Nocturne.TextSecondary, fontSize = 16.sp,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Nocturne.Surface)
            .padding(20.dp),
    )
}
