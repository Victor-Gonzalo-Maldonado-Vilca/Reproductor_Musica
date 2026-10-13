package com.mimusica.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mimusica.app.data.Collection
import com.mimusica.app.data.LibraryStore
import com.mimusica.app.data.MusicRepository
import com.mimusica.app.data.Song
import com.mimusica.app.playback.PlayerController
import com.mimusica.app.ui.components.MiniPlayer
import com.mimusica.app.ui.components.NocturneBottomBar
import com.mimusica.app.ui.components.Tab
import com.mimusica.app.ui.screens.AddToPlaylistSheet
import com.mimusica.app.ui.screens.ConfirmDialog
import com.mimusica.app.ui.screens.HomeScreen
import com.mimusica.app.ui.screens.LibraryScreen
import com.mimusica.app.ui.screens.NameDialog
import com.mimusica.app.ui.screens.NewsSheet
import com.mimusica.app.ui.screens.NowPlayingScreen
import com.mimusica.app.ui.screens.PlaylistDetailScreen
import com.mimusica.app.ui.screens.SearchScreen
import com.mimusica.app.ui.screens.SettingsScreen
import com.mimusica.app.ui.screens.SongOptionsSheet
import com.mimusica.app.ui.theme.Nocturne
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Acciones que cualquier pantalla puede pedir a la app. */
class AppActions(
    val openCollection: (Collection) -> Unit,
    val openNowPlaying: () -> Unit,
    val songOptions: (song: Song, playlistId: Long?, index: Int) -> Unit,
    val addToPlaylist: (List<Long>) -> Unit,
    val createPlaylist: () -> Unit,
    val renamePlaylist: (Long) -> Unit,
    val deletePlaylist: (Long) -> Unit,
    val goToTab: (Tab) -> Unit,
    val message: (String) -> Unit,
    val back: () -> Unit,
    val showNews: () -> Unit
)

private data class SongMenu(val song: Song, val playlistId: Long?, val index: Int)

@Composable
fun NocturneApp(onRescan: () -> Unit) {
    var tab by remember { mutableStateOf(Tab.Home) }
    val stack = remember { mutableStateListOf<Collection>() }
    var nowPlaying by remember { mutableStateOf(false) }

    var songMenu by remember { mutableStateOf<SongMenu?>(null) }
    var addToPlaylist by remember { mutableStateOf<List<Long>?>(null) }
    var creating by remember { mutableStateOf(false) }
    var renaming by remember { mutableStateOf<Long?>(null) }
    var deleting by remember { mutableStateOf<Long?>(null) }
    var news by remember { mutableStateOf<List<Song>?>(null) }

    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val actions = remember {
        AppActions(
            openCollection = { c -> nowPlaying = false; stack.add(c) },
            openNowPlaying = { nowPlaying = true },
            songOptions = { s, p, i -> songMenu = SongMenu(s, p, i) },
            addToPlaylist = { ids -> addToPlaylist = ids },
            createPlaylist = { creating = true },
            renamePlaylist = { renaming = it },
            deletePlaylist = { deleting = it },
            goToTab = { t -> stack.clear(); nowPlaying = false; tab = t },
            message = { m ->
                scope.launch {
                    snackbar.currentSnackbarData?.dismiss(); snackbar.showSnackbar(
                    m
                )
                }
            },
            back = {
                when {
                    nowPlaying -> nowPlaying = false
                    stack.isNotEmpty() -> stack.removeAt(stack.lastIndex)
                    else -> tab = Tab.Library
                }
            },
            showNews = {
                if (MusicRepository.newSongs.isEmpty()) {
                    scope.launch { snackbar.currentSnackbarData?.dismiss(); snackbar.showSnackbar("No tiene ninguna notificación disponible") }
                } else {
                    news = MusicRepository.newSongs
                    val all = (MusicRepository.songs + MusicRepository.whatsappAudios).map { it.id }
                    LibraryStore.markAllSeen(all)
                }
            },
        )
    }

    BackHandler(enabled = nowPlaying || stack.isNotEmpty()) { actions.back() }

    // Actualiza la barra de progreso mientras suena música.
    LaunchedEffect(PlayerController.isPlaying) {
        while (PlayerController.isPlaying) {
            PlayerController.tick()
            delay(500)
        }
    }

    val current = PlayerController.currentSong
    Box(Modifier
        .fillMaxSize()
        .background(Nocturne.Background)) {
        Column(Modifier.fillMaxSize()) {
            Box(Modifier.weight(1f)) {
                val padding = PaddingValues(bottom = 16.dp)
                val top = stack.lastOrNull()
                Box(Modifier
                    .fillMaxSize()
                    .statusBarsPadding()) {
                    if (top != null) {
                        PlaylistDetailScreen(top, actions, padding)
                    } else when (tab) {
                        Tab.Home -> HomeScreen(actions, padding)
                        Tab.Search -> SearchScreen(actions, padding)
                        Tab.Library -> LibraryScreen(actions, padding)
                        Tab.Settings -> SettingsScreen(actions, padding, onRescan)
                    }
                }
            }
            if (current != null) {
                MiniPlayer(
                    current, PlayerController.isPlaying,
                    progress = if (PlayerController.durationMs > 0) PlayerController.positionMs.toFloat() / PlayerController.durationMs else 0f,
                    onOpen = { nowPlaying = true },
                    onTogglePlay = { PlayerController.togglePlay() },
                    onNext = { PlayerController.next() },
                )
            }
            NocturneBottomBar(tab) { actions.goToTab(it) }
        }

        AnimatedVisibility(
            visible = nowPlaying && current != null,
            enter = slideInVertically { it },
            exit = slideOutVertically { it },
        ) {
            NowPlayingScreen(actions)
        }

        SnackbarHost(snackbar, Modifier
            .align(Alignment.BottomCenter)
            .padding(bottom = 180.dp)) {
            Snackbar(
                containerColor = Nocturne.SurfaceHigher,
                contentColor = Nocturne.TextPrimary
            ) { Text(it.visuals.message) }
        }
    }

    // ---------- Menús y diálogos ----------
    songMenu?.let { m ->
        SongOptionsSheet(
            song = m.song,
            fromPlaylistId = m.playlistId,
            onDismiss = { songMenu = null },
            onPlayNext = { PlayerController.playNext(m.song); actions.message("Sonará a continuación") },
            onAddToQueue = { PlayerController.addToQueue(m.song); actions.message("Agregada a la cola") },
            onAddToPlaylist = { songMenu = null; addToPlaylist = listOf(m.song.id) },
            onRemoveFromPlaylist = {
                m.playlistId?.let {
                    LibraryStore.removeFromPlaylist(
                        it,
                        m.index
                    )
                }
            },
            onOpen = { actions.openCollection(it) },
        )
    }
    addToPlaylist?.let { ids ->
        AddToPlaylistSheet(ids, onDismiss = { addToPlaylist = null }) { msg ->
            addToPlaylist = null
            actions.message(msg)
        }
    }

    news?.let { list ->
        NewsSheet(
            songs = list,
            onDismiss = { news = null },
            onPlay = { songs, index -> PlayerController.play(songs, index) },
        )
    }

    if (creating) {
        NameDialog("Nueva playlist", "", onDismiss = { creating = false }) { name ->
            val p = LibraryStore.createPlaylist(name)
            creating = false
            actions.openCollection(Collection.UserPlaylist(p.id))
        }
    }
    renaming?.let { id ->
        NameDialog(
            "Renombrar playlist",
            LibraryStore.playlist(id)?.name ?: "",
            onDismiss = { renaming = null }) { name ->
            LibraryStore.renamePlaylist(id, name)
            renaming = null
        }
    }
    deleting?.let { id ->
        ConfirmDialog(
            "¿Eliminar playlist?",
            "Se eliminará \"${LibraryStore.playlist(id)?.name}\". Las canciones seguirán en tu teléfono.",
            "Eliminar",
            onDismiss = { deleting = null },
        ) {
            LibraryStore.deletePlaylist(id)
            stack.removeAll { it == Collection.UserPlaylist(id) }
            deleting = null
        }
    }
}
