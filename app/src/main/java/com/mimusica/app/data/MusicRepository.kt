package com.mimusica.app.data

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Lee la música guardada en el teléfono a través de MediaStore. */
object MusicRepository {

    var songs by mutableStateOf<List<Song>>(emptyList())
        private set

    var whatsappAudios by mutableStateOf<List<Song>>(emptyList())
        private set

    var loaded by mutableStateOf(false)
        private set

    val albums: List<Album>
        get() = songs.groupBy { it.albumId }.map { (id, list) ->
            Album(id, list.first().album, list.first().artist, list)
        }.sortedBy { it.title.lowercase() }

    val artists: List<Artist>
        get() = songs.groupBy { it.artist }.map { (name, list) -> Artist(name, list) }
            .sortedBy { it.name.lowercase() }

    val newSongs: List<Song>
        get() = (songs + whatsappAudios).filter { it.id !in LibraryStore.knownSongs}

    fun song(id: Long): Song? = songs.firstOrNull { it.id == id } ?: whatsappAudios.firstOrNull { it.id == id}

    suspend fun load(context: Context, skipShort: Boolean) {
        val alls = withContext(Dispatchers.IO) { query(context, skipShort) }
        val (songsList, whatsapAudiosList) = alls.partition {!it.isWhatsApp}
        songs = songsList
        whatsappAudios = whatsapAudiosList
        if (!LibraryStore.hasKnownSongs) LibraryStore.markAllSeen(newSongs.map{ it.id })
        loaded = true
    }

    @Suppress("DEPRECATION")
    private fun query(context: Context, skipShort: Boolean): List<Song> {
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.DATA,
        )
        val minDuration = if (skipShort) 30_000 else 1
        //Se extraera toda musica que no es corta, ademas de todos los audios de WhatsApp
        val selection =
            "(${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} >= ?) " +
                    "OR (${MediaStore.Audio.Media.DATA} LIKE ?)"
        val list = mutableListOf<Song>()
        context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            arrayOf(minDuration.toString(), "%/WhatsApp%"),
            "${MediaStore.Audio.Media.DATE_ADDED} DESC",
        )?.use { c ->
            val idCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumIdCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val durCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val dateCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
            val sizeCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            val mimeCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)
            val dataCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            while (c.moveToNext()) {
                val id = c.getLong(idCol)
                val artist = c.getString(artistCol)
                list += Song(
                    id = id,
                    title = c.getString(titleCol) ?: "Sin título",
                    artist = if (artist.isNullOrBlank() || artist == "<unknown>") "Artista desconocido" else artist,
                    album = c.getString(albumCol) ?: "Álbum desconocido",
                    albumId = c.getLong(albumIdCol),
                    durationMs = c.getLong(durCol),
                    dateAdded = c.getLong(dateCol),
                    sizeBytes = c.getLong(sizeCol),
                    mimeType = c.getString(mimeCol) ?: "",
                    path = c.getString(dataCol) ?: "",
                    uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id),
                )
            }
        }
        return list
    }
}
