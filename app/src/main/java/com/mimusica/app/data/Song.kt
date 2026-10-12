package com.mimusica.app.data

import android.net.Uri

data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val albumId: Long,
    val durationMs: Long,
    val dateAdded: Long,
    val sizeBytes: Long,
    val mimeType: String,
    val path: String,
    val uri: Uri,
) {
    /** Etiqueta corta del formato, por ejemplo "FLAC" o "MP3". */
    val format: String
        get() = when {
            mimeType.contains("flac") -> "FLAC"
            mimeType.contains("mpeg") -> "MP3"
            mimeType.contains("mp4") || mimeType.contains("aac") -> "AAC"
            mimeType.contains("ogg") || mimeType.contains("opus") -> "OGG"
            mimeType.contains("wav") -> "WAV"
            else -> mimeType.substringAfter("/").uppercase().ifBlank { "AUDIO" }
        }

    val isLossless: Boolean get() = format == "FLAC" || format == "WAV"

    val isDownload: Boolean get() = path.contains("/Download", ignoreCase = true)

    val isWhatsApp: Boolean get() = path.contains("/WhatsApp", ignoreCase = true)
}

data class Album(val id: Long, val title: String, val artist: String, val songs: List<Song>)

data class Artist(val name: String, val songs: List<Song>) {
    val albumCount: Int get() = songs.map { it.albumId }.distinct().size
}

data class Playlist(val id: Long, val name: String, val createdAt: Long, val songIds: List<Long>)

/** Cualquier lista de canciones que se puede abrir en la pantalla de detalle. */
sealed interface Collection {
    val key: String

    data object Favorites : Collection {
        override val key = "favorites"
    }

    data object Downloads : Collection {
        override val key = "downloads"
    }

    data class UserPlaylist(val id: Long) : Collection {
        override val key get() = "playlist:$id"
    }

    data class AlbumOf(val albumId: Long) : Collection {
        override val key get() = "album:$albumId"
    }

    data class ArtistOf(val name: String) : Collection {
        override val key get() = "artist:$name"
    }

    companion object {
        fun fromKey(key: String): Collection? {
            val value = key.substringAfter(":")
            return when {
                key == Favorites.key -> Favorites
                key == Downloads.key -> Downloads
                key.startsWith("playlist:") -> value.toLongOrNull()?.let { UserPlaylist(it) }
                key.startsWith("album:") -> value.toLongOrNull()?.let { AlbumOf(it) }
                key.startsWith("artist:") -> ArtistOf(value)
                else -> null
            }
        }
    }
}

fun formatDuration(ms: Long): String {
    val totalSec = ms / 1000
    return "%d:%02d".format(totalSec / 60, totalSec % 60)
}

fun formatTotalDuration(ms: Long): String {
    val totalMin = ms / 60_000
    val h = totalMin / 60
    val m = totalMin % 60
    return if (h > 0) "$h h $m min" else "$m min"
}

fun formatSize(bytes: Long): String {
    val gb = bytes / 1_073_741_824.0
    return if (gb >= 1) "%.1f GB".format(gb) else "%.0f MB".format(bytes / 1_048_576.0)
}
