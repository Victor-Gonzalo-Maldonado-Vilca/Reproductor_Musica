package com.mimusica.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mimusica.app.ui.components.NocturneLogo
import com.mimusica.app.ui.theme.Nocturne

/** Pantalla de bienvenida que pide permiso para leer la música. */
@Composable
fun PermissionScreen(denied: Boolean, onRequest: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF221B44), Nocturne.Background)))
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        NocturneLogo(110.dp)
        Spacer(Modifier.height(32.dp))
        Text("Bienvenido a Nocturne", color = Nocturne.TextPrimary, fontSize = 32.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(Modifier.height(14.dp))
        Text(
            if (denied) "Sin el permiso no podemos ver tu música. Puedes activarlo en Ajustes del teléfono → Apps → Nocturne → Permisos."
            else "Para reproducir tu música necesitamos acceso a los archivos de audio de tu teléfono. Tus archivos nunca salen del dispositivo.",
            color = Nocturne.TextSecondary, fontSize = 17.sp, textAlign = TextAlign.Center, lineHeight = 24.sp,
        )
        Spacer(Modifier.height(40.dp))
        Text(
            if (denied) "Intentar de nuevo" else "Permitir acceso",
            color = Color(0xFF1A1440), fontSize = 18.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clip(CircleShape)
                .background(Nocturne.Accent)
                .clickable(onClick = onRequest)
                .padding(vertical = 18.dp),
        )
    }
}
