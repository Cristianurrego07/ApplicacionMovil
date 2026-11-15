package com.example.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.app.R
import com.example.app.model.Compromiso
import com.example.app.model.Usuario
import com.example.app.ui.components.BotonPrincipal
import com.example.app.ui.components.CampoTexto
import com.example.app.ui.components.TarjetaSeccion
import com.example.app.ui.theme.AppThemePreview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportarProblemaScreen(
    compromisos: List<Compromiso>,
    onVolver: () -> Unit,
    onReportar: (String, String) -> Boolean
) {
    val reportables = compromisos.filter {
        it.estado == "Pendiente" || it.estado == "Vencido"
    }
    var compromisoSeleccionado by remember { mutableStateOf<Compromiso?>(null) }
    var motivoSeleccionado by remember { mutableStateOf("Ya pagué por otro medio") }
    var comentario by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var mostrarConfirmacion by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(text = "Reportar problema",
                color = Color.Black) },
                navigationIcon = {
                    Box(
                        modifier = Modifier.size(48.dp).clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            role = Role.Button,
                            onClick = onVolver
                        ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { espacio ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(espacio),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Elige el compromiso que quieres reportar.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            if (reportables.isEmpty()) {
                item {
                    Text(
                        text = "No tienes compromisos disponibles para reportar.",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            } else {
                items(reportables, key = { it.folio }) { compromiso ->
                    TarjetaSeccion(titulo = compromiso.empresa) {
                        Text(text = compromiso.concepto)
                        Text(text = "Folio: ${compromiso.folio} · $${compromiso.monto}")
                        BotonPrincipal(
                            texto = "Elegir compromiso",
                            onClick = {
                                compromisoSeleccionado = compromiso
                                motivoSeleccionado = "Ya pagué por otro medio"
                                comentario = ""
                                error = null
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }

    compromisoSeleccionado?.let { compromiso ->
        AlertDialog(
            onDismissRequest = { compromisoSeleccionado = null },
            title = { Text(text = "¿Qué problema tienes?") },
            text = {
                Column {
                    Text(text = "${compromiso.empresa} · ${compromiso.concepto}")
                    val motivos = listOf(
                        "Ya pagué por otro medio",
                        "No reconozco este cobro",
                        "El monto no corresponde",
                        "Otro"
                    )
                    motivos.forEach { opcion ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = motivoSeleccionado == opcion,
                                onClick = { motivoSeleccionado = opcion; error = null }
                            )
                            Text(text = opcion)
                        }
                    }
                    if (motivoSeleccionado == "Otro") {
                        CampoTexto(
                            valor = comentario,
                            etiqueta = "Describe el problema",
                            alCambiar = { comentario = it; error = null },
                            error = error
                        )
                    } else {
                        error?.let { mensaje ->
                            Text(text = mensaje, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (motivoSeleccionado == "Otro" && comentario.isBlank()) {
                        error = "Escribe un comentario para explicar el problema."
                    } else {
                        val motivo = if (motivoSeleccionado == "Otro") {
                            "Otro: ${comentario.trim()}"
                        } else {
                            motivoSeleccionado
                        }
                        if (onReportar(compromiso.folio, motivo)) {
                            compromisoSeleccionado = null
                            mostrarConfirmacion = true
                        } else {
                            error = "No se pudo enviar el reporte. Inténtalo nuevamente."
                        }
                    }
                }) { Text(text = "Enviar reporte") }
            },
            dismissButton = {
                TextButton(onClick = { compromisoSeleccionado = null }) {
                    Text(text = "Cancelar")
                }
            }
        )
    }

    if (mostrarConfirmacion) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmacion = false; onVolver() },
            title = { Text(text = "Reporte enviado") },
            text = {
                Text(text = "El compromiso quedó en revisión y no se puede pagar mientras revisamos el caso.")
            },
            confirmButton = {
                TextButton(onClick = { mostrarConfirmacion = false; onVolver() }) {
                    Text(text = "Listo")
                }
            }
        )
    }
}

@Preview(name = "Reportar problema", showBackground = true, showSystemUi = true)
@Composable
private fun ReportarProblemaPreview() {
    val usuario = Usuario(1, "20.435.214-5", "Sofía", "sofia@gmail.com", 1, "CLIENTE")
    AppThemePreview {
        ReportarProblemaScreen(
            compromisos = listOf(
                Compromiso("PF-001", "Universidad", usuario, "Mensualidad", 350000, "2026-10-15"),
                Compromiso("PF-002", "Enel", usuario, "Electricidad", 28000, "2026-10-20", "Vencido")
            ),
            onVolver = {},
            onReportar = { _, _ -> true }
        )
    }
}

@Preview(name = "Sin compromisos", showBackground = true, showSystemUi = true)
@Composable
private fun ReportarProblemaVacioPreview() {
    AppThemePreview {
        ReportarProblemaScreen(
            compromisos = emptyList(),
            onVolver = {},
            onReportar = { _, _ -> true }
        )
    }
}
