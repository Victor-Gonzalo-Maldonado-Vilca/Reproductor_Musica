package com.mimusica.app.ui.screens

import com.mimusica.app.data.Collection
import com.mimusica.app.data.LibraryStore
import com.mimusica.app.data.MusicRepository
import com.mimusica.app.data.Song

/** Datos listos para mostrar de cualquier colección (playlist, álbum, artista...). */
data class CollectionInfo(
    val collection: Collection,
    val title: String,
    val kind: String,
    val owner: String,
    val songs: List<Song>,
) {
    val cover: Song? get() = songs.firstOrNull()
    val totalMs: Long get() = songs.sumOf { it.durationMs }
    val isUserPlaylist: Boolean get() = collection is Collection.UserPlaylist
    val label: String
        get() = when (collection) {
            Collection.Favorites -> "AUTO-SYNC"
            Collection.Downloads -> "EN TU DISPOSITIVO"
            Collection.WhatsApp -> "EN TU DISPOSITIVO"
            is Collection.UserPlaylist -> "TU PLAYLIST"
            is Collection.AlbumOf -> "ÁLBUM"
            is Collection.ArtistOf -> "ARTISTA"
        }
}

fun resolve(collection: Collection): CollectionInfo? {
    val all = MusicRepository.songs
    val whatsapp = MusicRepository.whatsappAudios
    return when (collection) {
        Collection.Favorites -> CollectionInfo(
            collection, "Canciones que te gustan", "Playlist", "ti",
            LibraryStore.favorites.mapNotNull { MusicRepository.song(it) },
        )
        Collection.Downloads -> CollectionInfo(
            collection, "Descargas", "Carpeta", "tu dispositivo",
            all.filter { it.isDownload },
        )
        Collection.WhatsApp -> CollectionInfo(
            collection, "Audios de WhatsApp", "Carpeta", "WhatsApp",
            whatsapp
        )
        is Collection.UserPlaylist -> LibraryStore.playlist(collection.id)?.let { p ->
            CollectionInfo(collection, p.name, "Playlist", "ti", p.songIds.mapNotNull { MusicRepository.song(it) })
        }
        is Collection.AlbumOf -> all.filter { it.albumId == collection.albumId }.takeIf { it.isNotEmpty() }?.let {
            CollectionInfo(collection, it.first().album, "Álbum", it.first().artist, it)
        }
        is Collection.ArtistOf -> all.filter { it.artist == collection.name }.takeIf { it.isNotEmpty() }?.let {
            CollectionInfo(collection, collection.name, "Artista", collection.name, it)
        }
    }
}
