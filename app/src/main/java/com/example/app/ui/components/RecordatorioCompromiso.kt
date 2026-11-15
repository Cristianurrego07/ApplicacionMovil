package com.example.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.app.R
import com.example.app.model.Compromiso
import com.example.app.notificaciones.AvisosScheduler
import com.example.app.notificaciones.NotificacionesPreferencias

@Composable
fun RecordatorioCompromiso(compromiso: Compromiso) {
    val context = LocalContext.current
    val esPreview = LocalInspectionMode.current
    val preferencias = remember(compromiso.usuario.id, esPreview) {
        if (esPreview) null else NotificacionesPreferencias(context, compromiso.usuario.id)
    }
    var mostrarDialogo by remember { mutableStateOf(false) }
    var dias by remember(compromiso.folio) { mutableIntStateOf(preferencias?.dias(compromiso.folio) ?: 0) }

    Box(
        modifier = Modifier.size(48.dp).clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            role = Role.Button,
            onClick = { mostrarDialogo = true }
        ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_notificacion),
            contentDescription = "Configurar recordatorio de ${compromiso.concepto}",
            tint = MaterialTheme.colorScheme.primary
        )
    }

    if (mostrarDialogo) {
        AlertDialog(
            onDismissRequest = { mostrarDialogo = false },
            title = { Text("Recordatorio: ${compromiso.concepto}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (preferencias?.activadas() != true) {
                        Text("Activa las notificaciones en Mi perfil para recibir recordatorios.")
                    }
                    Text(
                        if (compromiso.estado == "Vencido")
                            "¿Cada cuántos días quieres recibir un recordatorio?"
                        else
                            "¿Cuántos días antes del vencimiento quieres que te avisemos?"
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(1, 3, 7).forEach { opcion ->
                            FilterChip(
                                selected = dias == opcion,
                                onClick = { dias = opcion },
                                label = { Text(if (opcion == 1) "1 día" else "$opcion días") }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(enabled = dias != 0, onClick = {
                    preferencias?.guardarDias(compromiso.folio, dias)
                    if (preferencias?.activadas() == true) AvisosScheduler.programar(context)
                    mostrarDialogo = false
                }) { Text("Guardar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogo = false }) { Text("Cancelar") }
            }
        )
    }
}
