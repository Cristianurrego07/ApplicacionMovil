package com.example.app.ui.components

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.NotificationManagerCompat
import com.example.app.notificaciones.AvisosScheduler
import com.example.app.notificaciones.NotificacionesPreferencias
import com.example.app.permisos.Permisos
import com.example.app.ui.theme.AppTheme

@Composable
fun ConfiguracionNotificaciones(usuarioId: Long) {
    val context = LocalContext.current
    val esPreview = LocalInspectionMode.current
    val preferencias = remember(usuarioId, esPreview) {
        if (esPreview) null else NotificacionesPreferencias(context, usuarioId)
    }
    var activadas by remember(usuarioId) { mutableStateOf(preferencias?.activadas() ?: false) }
    var mostrarAjustes by remember { mutableStateOf(false) }

    val solicitarPermiso = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        preferencias?.guardarActivadas(concedido)
        activadas = concedido
        if (concedido) {
            AvisosScheduler.programar(context)
        } else {
            mostrarAjustes = true
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Recibir notificaciones",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )

        Switch(
            checked = activadas,
            onCheckedChange = { activar ->
                if (!activar) {
                    preferencias?.guardarActivadas(false)
                    activadas = false
                } else if (
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    !Permisos.tiene(context, Manifest.permission.POST_NOTIFICATIONS)
                ) {
                    solicitarPermiso.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) {
                    mostrarAjustes = true
                } else {
                    preferencias?.guardarActivadas(true)
                    activadas = true
                    AvisosScheduler.programar(context)
                }
            }
        )
    }

    if (mostrarAjustes) {
        AlertDialog(
            onDismissRequest = { mostrarAjustes = false },
            title = { Text("Permiso de notificaciones") },
            text = {
                Text("Para recibir avisos, activa las notificaciones de PagoFlex en ajustes.")
            },
            confirmButton = {
                TextButton(onClick = {
                    mostrarAjustes = false
                    Permisos.abrirAjustes(context)
                }) { Text("Abrir ajustes") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarAjustes = false }) { Text("Cerrar") }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ConfiguracionNotificacionesPreview() {
    AppTheme { ConfiguracionNotificaciones(usuarioId = 1L) }
}
