package com.mimusica.app.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.json.JSONArray
import org.json.JSONObject

/** Guarda en el teléfono los favoritos, las playlists, el historial y los ajustes. */
object LibraryStore {

    private lateinit var prefs: SharedPreferences

    var favorites by mutableStateOf<List<Long>>(emptyList())
        private set
    var playlists by mutableStateOf<List<Playlist>>(emptyList())
        private set
    var recents by mutableStateOf<List<Long>>(emptyList())
        private set
    var pinned by mutableStateOf<Set<String>>(emptySet())
        private set
    var skipShortTracks by mutableStateOf(true)
        private set
    var userName by mutableStateOf("")
        private set

    fun init(context: Context) {
        if (::prefs.isInitialized) return
        prefs = context.getSharedPreferences("nocturne", Context.MODE_PRIVATE)
        favorites = readIds("favorites")
        recents = readIds("recents")
        pinned = prefs.getStringSet("pinned", emptySet())!!.toSet()
        skipShortTracks = prefs.getBoolean("skip_short", true)
        userName = prefs.getString("user_name", "") ?: ""
        playlists = runCatching {
            val arr = JSONArray(prefs.getString("playlists", "[]"))
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                val ids = o.getJSONArray("songs")
                Playlist(
                    id = o.getLong("id"),
                    name = o.getString("name"),
                    createdAt = o.getLong("created"),
                    songIds = (0 until ids.length()).map { ids.getLong(it) },
                )
            }
        }.getOrDefault(emptyList())
    }

    // ---------- Favoritos ----------
    fun isFavorite(id: Long) = id in favorites

    fun toggleFavorite(id: Long) {
        favorites = if (id in favorites) favorites - id else listOf(id) + favorites
        writeIds("favorites", favorites)
    }

    // ---------- Historial ----------
    fun addRecent(id: Long) {
        recents = (listOf(id) + (recents - id)).take(50)
        writeIds("recents", recents)
    }

    // ---------- Colecciones fijadas ----------
    fun isPinned(key: String) = key in pinned

    fun togglePinned(key: String) {
        pinned = if (key in pinned) pinned - key else pinned + key
        prefs.edit().putStringSet("pinned", pinned).apply()
    }

    // ---------- Playlists ----------
    fun playlist(id: Long) = playlists.firstOrNull { it.id == id }

    fun createPlaylist(name: String, songIds: List<Long> = emptyList()): Playlist {
        val now = System.currentTimeMillis()
        val p = Playlist(now, name.trim(), now, songIds)
        playlists = listOf(p) + playlists
        savePlaylists()
        return p
    }

    fun renamePlaylist(id: Long, name: String) = update(id) { it.copy(name = name.trim()) }

    fun deletePlaylist(id: Long) {
        playlists = playlists.filterNot { it.id == id }
        pinned = pinned - Collection.UserPlaylist(id).key
        prefs.edit().putStringSet("pinned", pinned).apply()
        savePlaylists()
    }

    fun addToPlaylist(id: Long, songIds: List<Long>) =
        update(id) { p -> p.copy(songIds = p.songIds + songIds.filterNot { it in p.songIds }) }

    fun removeFromPlaylist(id: Long, index: Int) =
        update(id) { p -> p.copy(songIds = p.songIds.filterIndexed { i, _ -> i != index }) }

    fun moveInPlaylist(id: Long, from: Int, to: Int) = update(id) { p ->
        if (from !in p.songIds.indices || to !in p.songIds.indices) return@update p
        val list = p.songIds.toMutableList()
        list.add(to, list.removeAt(from))
        p.copy(songIds = list)
    }

    // ---------- Ajustes ----------
    fun setSkipShort(value: Boolean) {
        skipShortTracks = value
        prefs.edit().putBoolean("skip_short", value).apply()
    }

    fun saveUserName(value: String) {
        userName = value.trim()
        prefs.edit().putString("user_name", userName).apply()
    }

    private fun update(id: Long, block: (Playlist) -> Playlist) {
        playlists = playlists.map { if (it.id == id) block(it) else it }
        savePlaylists()
    }

    private fun savePlaylists() {
        val arr = JSONArray()
        playlists.forEach { p ->
            arr.put(
                JSONObject()
                    .put("id", p.id)
                    .put("name", p.name)
                    .put("created", p.createdAt)
                    .put("songs", JSONArray(p.songIds)),
            )
        }
        prefs.edit().putString("playlists", arr.toString()).apply()
    }

    private fun readIds(key: String): List<Long> = runCatching {
        val arr = JSONArray(prefs.getString(key, "[]"))
        (0 until arr.length()).map { arr.getLong(it) }
    }.getOrDefault(emptyList())

    private fun writeIds(key: String, ids: List<Long>) {
        prefs.edit().putString(key, JSONArray(ids).toString()).apply()
    }
}
