package com.mimusica.app.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.heightIn
import com.mimusica.app.ui.AppActions
import com.mimusica.app.data.LibraryStore
import com.mimusica.app.data.MusicRepository
import com.mimusica.app.data.formatSize
import com.mimusica.app.ui.components.UserAvatar
import com.mimusica.app.ui.theme.Nocturne

@Composable
fun SettingsScreen(actions: AppActions, contentPadding: PaddingValues, onRescan: () -> Unit) {
    var editingName by remember { mutableStateOf(false) }
    var showPrivacy by remember { mutableStateOf(false) }
    val songs = MusicRepository.songs

    LazyColumn(contentPadding = contentPadding) {
        item {
            Text(
                "Ajustes", color = Nocturne.TextPrimary, fontSize = 38.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 20.dp, top = 24.dp, bottom = 16.dp),
            )
        }
        // ---------- Perfil ----------
        item {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Nocturne.Surface)
                    .clickable { editingName = true }
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                UserAvatar(LibraryStore.userName, size = 60.dp) { editingName = true }
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        LibraryStore.userName.ifBlank { "Tu nombre" },
                        color = Nocturne.TextPrimary, fontSize = 21.sp, fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        "${songs.size} canciones  •  ${LibraryStore.playlists.size} playlists  •  ${LibraryStore.favorites.size} me gusta",
                        color = Nocturne.TextSecondary, fontSize = 14.sp,
                    )
                }
                Icon(Icons.Rounded.Edit, "Editar nombre", tint = Nocturne.Cyan)
            }
        }

        item { Group("REPRODUCCIÓN") }
        item {
            SettingRow(
                Icons.Rounded.Timer, "Ocultar audios cortos",
                "No muestra archivos de menos de 30 segundos (notas de voz, tonos)",
            ) {
                Switch(
                    checked = LibraryStore.skipShortTracks,
                    onCheckedChange = { LibraryStore.setSkipShort(it); onRescan() },
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = Nocturne.Purple,
                        checkedThumbColor = androidx.compose.ui.graphics.Color.White,
                        uncheckedTrackColor = Nocturne.SurfaceHigher,
                    ),
                )
            }
        }

        item { Group("BIBLIOTECA") }
        item {
            SettingRow(
                Icons.Rounded.Refresh, "Volver a escanear",
                "${songs.size} canciones  •  ${formatSize(songs.sumOf { it.sizeBytes })}",
                onClick = { onRescan(); actions.message("Biblioteca actualizada") },
            )
        }

        item { Group("ACERCA DE") }
        item { SettingRow(Icons.Rounded.Shield, "Política de privacidad", "Nocturne no recopila tus datos", onClick = { showPrivacy = true }) }
        item { SettingRow(Icons.Rounded.Info, "Versión", "Nocturne 1.0") }
    }

    if (editingName) {
        NameDialog("Tu nombre", LibraryStore.userName, onDismiss = { editingName = false }) {
            LibraryStore.saveUserName(it)
            editingName = false
        }
    }
    if (showPrivacy) {
        AlertDialog(
            onDismissRequest = { showPrivacy = false },
            containerColor = Nocturne.Surface,
            title = { Text("Política de privacidad", color = Nocturne.TextPrimary) },
            text = {
                Text(
                    PRIVACY_TEXT, color = Nocturne.TextSecondary,
                    modifier = Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState()),
                )
            },
            confirmButton = { TextButton(onClick = { showPrivacy = false }) { Text("Entendido", color = Nocturne.Cyan) } },
        )
    }
}

const val PRIVACY_TEXT = """Nocturne reproduce los archivos de música guardados en tu teléfono.

• Acceso a audio: la app lee tus archivos de música solo para mostrarlos y reproducirlos. Nunca salen de tu dispositivo.
• Notificaciones: se usan únicamente para mostrar los controles de reproducción.
• Datos guardados: tus playlists, favoritos, historial y nombre se guardan solo en tu teléfono.
• No usamos internet, publicidad, analíticas ni compartimos datos con terceros.

Si desinstalas la app, toda esta información se borra."""

@Composable
private fun Group(title: String) {
    Text(
        title, color = Nocturne.Cyan, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp,
        modifier = Modifier.padding(start = 24.dp, top = 28.dp, bottom = 8.dp),
    )
}

@Composable
private fun SettingRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Nocturne.Surface)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = Nocturne.Lavender, modifier = Modifier.size(26.dp))
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = Nocturne.TextPrimary, fontSize = 18.sp)
            Text(subtitle, color = Nocturne.TextSecondary, fontSize = 14.sp)
        }
        when {
            trailing != null -> trailing()
            onClick != null -> Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, null, tint = Nocturne.TextSecondary)
        }
    }
}
