package com.mimusica.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.mimusica.app.data.LibraryStore
import com.mimusica.app.data.MusicRepository
import com.mimusica.app.playback.PlayerController
import com.mimusica.app.ui.NocturneApp
import com.mimusica.app.ui.screens.PermissionScreen
import com.mimusica.app.ui.theme.NocturneTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val audioPermission =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.READ_MEDIA_AUDIO
        else Manifest.permission.READ_EXTERNAL_STORAGE

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
        )
        super.onCreate(savedInstanceState)
        LibraryStore.init(this)

        setContent {
            NocturneTheme {
                val scope = rememberCoroutineScope()
                var granted by remember { mutableStateOf(hasAudioPermission()) }
                var denied by remember { mutableStateOf(false) }
                val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
                    granted = result[audioPermission] == true
                    denied = !granted
                }
                val rescan: () -> Unit = {
                    scope.launch { MusicRepository.load(this@MainActivity, LibraryStore.skipShortTracks) }
                }

                if (granted) {
                    LaunchedEffect(Unit) {
                        PlayerController.connect(this@MainActivity)
                        rescan()
                    }
                    NocturneApp(onRescan = rescan)
                } else {
                    PermissionScreen(denied) {
                        val perms = buildList {
                            add(audioPermission)
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) add(Manifest.permission.POST_NOTIFICATIONS)
                        }
                        launcher.launch(perms.toTypedArray())
                    }
                }
            }
        }
    }

    private fun hasAudioPermission() =
        ContextCompat.checkSelfPermission(this, audioPermission) == PackageManager.PERMISSION_GRANTED

    override fun onDestroy() {
        if (isFinishing) PlayerController.release()
        super.onDestroy()
    }
}
