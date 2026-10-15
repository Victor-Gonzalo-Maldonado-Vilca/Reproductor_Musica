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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.PlaylistAdd
import androidx.compose.material.icons.automirrored.rounded.PlaylistPlay
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarVisuals
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.mimusica.app.data.Collection
import com.mimusica.app.data.LibraryStore
import com.mimusica.app.data.MusicRepository
import com.mimusica.app.data.Song
import com.mimusica.app.playback.PlayerController
import com.mimusica.app.ui.components.MiniPlayer
import com.mimusica.app.ui.components.NocturneBottomBar
import com.mimusica.app.ui.components.NocturneToast
import com.mimusica.app.ui.components.Tab
import com.mimusica.app.ui.screens.AddSongsSheet
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
import com.mimusica.app.ui.screens.SpeedSheet
import com.mimusica.app.ui.theme.Nocturne
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Acciones que cualquier pantalla puede pedir a la app. */
class AppActions(
    val openCollection: (Collection) -> Unit,
    val openNowPlaying: () -> Unit,
    val songOptions: (song: Song, playlistId: Long?) -> Unit,
    val addToPlaylist: (List<Long>) -> Unit,
    /** Abre el buscador para elegir canciones nuevas para una playlist del usuario. */
    val addSongs: (playlistId: Long) -> Unit,
    val createPlaylist: () -> Unit,
    val renamePlaylist: (Long) -> Unit,
    val deletePlaylist: (Long) -> Unit,
    val goToTab: (Tab) -> Unit,
    val message: (String) -> Unit,
    /** Como [message], pero con un ícono que acompaña al texto. */
    val notify: (String, ImageVector?) -> Unit,
    val back: () -> Unit,
    val showNews: () -> Unit,
)

/** Contenido de un aviso: el texto y el ícono que se dibujan en [NocturneToast]. */
private class ToastVisuals(override val message: String, val icon: ImageVector?) : SnackbarVisuals {
    override val actionLabel: String? = null
    override val withDismissAction = false
    override val duration = SnackbarDuration.Short
}

private data class SongMenu(val song: Song, val playlistId: Long?)

@Composable
fun NocturneApp(onRescan: (onDone: () -> Unit) -> Unit) {
    var tab by remember { mutableStateOf(Tab.Home) }
    val stack = remember { mutableStateListOf<Collection>() }
    var nowPlaying by remember { mutableStateOf(false) }

    var songMenu by remember { mutableStateOf<SongMenu?>(null) }
    var addToPlaylist by remember { mutableStateOf<List<Long>?>(null) }
    var addingSongsTo by remember { mutableStateOf<Long?>(null) }
    var creating by remember { mutableStateOf(false) }
    var renaming by remember { mutableStateOf<Long?>(null) }
    var deleting by remember { mutableStateOf<Long?>(null) }
    var news by remember { mutableStateOf<List<Song>?>(null) }
    var showSpeed by remember { mutableStateOf(false) }

    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    fun notify(text: String, icon: ImageVector?) {
        scope.launch {
            snackbar.currentSnackbarData?.dismiss()
            snackbar.showSnackbar(ToastVisuals(text, icon))
        }
    }

    val actions = remember {
        AppActions(
            openCollection = { c -> nowPlaying = false; stack.add(c) },
            openNowPlaying = { nowPlaying = true },
            songOptions = { s, p -> songMenu = SongMenu(s, p) },
            addToPlaylist = { ids -> addToPlaylist = ids },
            addSongs = { id -> addingSongsTo = id },
            createPlaylist = { creating = true },
            renamePlaylist = { renaming = it },
            deletePlaylist = { deleting = it },
            goToTab = { t -> stack.clear(); nowPlaying = false; tab = t },
            message = { m -> notify(m, null) },
            notify = { m, icon -> notify(m, icon) },
            back = {
                when {
                    nowPlaying -> nowPlaying = false
                    stack.isNotEmpty() -> stack.removeAt(stack.lastIndex)
                    else -> tab = Tab.Home
                }
            },
            showNews = {
                if (MusicRepository.newSongs.isEmpty()) {
                    notify("Estás al día: no hay novedades", Icons.Rounded.NotificationsNone)
                } else {
                    news = MusicRepository.newSongs
                    val all = (MusicRepository.songs + MusicRepository.whatsappAudios).map { it.id }
                    LibraryStore.markAllSeen(all)
                }
            },
        )
    }

    BackHandler(enabled = nowPlaying || stack.isNotEmpty() || tab != Tab.Home) { actions.back() }

    // Actualiza la barra de progreso mientras suena música.
    LaunchedEffect(PlayerController.isPlaying) {
        while (PlayerController.isPlaying) {
            PlayerController.tick()
            delay(500)
        }
    }

    val current = PlayerController.currentSong
    Box(
        Modifier
            .fillMaxSize()
            .background(Nocturne.Background)
    ) {
        Column(Modifier.fillMaxSize()) {
            Box(Modifier.weight(1f)) {
                val padding = PaddingValues(bottom = 16.dp)
                val top = stack.lastOrNull()
                Box(
                    Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                ) {
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

        // Con el reproductor abierto el aviso va arriba, para no tapar sus controles.
        SnackbarHost(
            snackbar,
            if (nowPlaying) Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 72.dp)
            else Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = if (current != null) 140.dp else 72.dp),
        ) { data ->
            NocturneToast(data.visuals.message, (data.visuals as? ToastVisuals)?.icon)
        }
    }

    // ---------- Menús y diálogos ----------
    songMenu?.let { m ->
        SongOptionsSheet(
            song = m.song,
            fromPlaylistId = m.playlistId,
            onDismiss = { songMenu = null },
            onPlayNext = {
                PlayerController.playNext(m.song)
                actions.notify("Sonará a continuación", Icons.AutoMirrored.Rounded.PlaylistPlay)
            },
            onAddToQueue = {
                PlayerController.addToQueue(m.song)
                actions.notify("Agregada a la cola", Icons.AutoMirrored.Rounded.QueueMusic)
            },
            onAddToPlaylist = { songMenu = null; addToPlaylist = listOf(m.song.id) },
            onSpeed = { songMenu = null; showSpeed = true },
            onRemoveFromPlaylist = {
                m.playlistId?.let {
                    LibraryStore.removeFromPlaylist(
                        it,
                        m.song.id
                    )
                }
            },
            onOpen = { actions.openCollection(it) },
        )
    }
    addToPlaylist?.let { ids ->
        AddToPlaylistSheet(ids, onDismiss = { addToPlaylist = null }) { msg ->
            addToPlaylist = null
            actions.notify(msg, Icons.AutoMirrored.Rounded.PlaylistAdd)
        }
    }
    if (showSpeed) {
        SpeedSheet(onDismiss = { showSpeed = false })
    }

    addingSongsTo?.let { id ->
        AddSongsSheet(id, onDismiss = { addingSongsTo = null }) { msg ->
            addingSongsTo = null
            actions.notify(msg, Icons.AutoMirrored.Rounded.PlaylistAdd)
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
