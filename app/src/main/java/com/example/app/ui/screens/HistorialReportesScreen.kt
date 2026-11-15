package com.example.app.ui.screens

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.app.R
import com.example.app.model.Compromiso
import com.example.app.model.Reporte
import com.example.app.ui.components.EtiquetaEstado
import com.example.app.ui.theme.AppThemePreview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialReportesScreen(
    reportes: List<Reporte>,
    compromisos: List<Compromiso> = emptyList(),
    esAgente: Boolean = false,
    onVolver: () -> Unit
) {
    var reporteSeleccionado by remember { mutableStateOf<Reporte?>(null) }

    reporteSeleccionado?.let { reporte ->
        val compromiso = compromisos.find { it.folio == reporte.folio }
        AlertDialog(
            onDismissRequest = { reporteSeleccionado = null },
            title = { Text(text = compromiso?.empresa ?: "Detalle del reporte") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = "Compromiso: ${reporte.folio}")
                    compromiso?.let { Text(text = "Concepto: ${it.concepto}") }
                    Text(text = "Motivo: ${reporte.motivo}")
                    Text(text = "Estado: ${if (reporte.estado == "Abierto") "Pendiente" else reporte.estado}")
                    Text(
                        text = "Respuesta",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = reporte.respuesta?.takeIf { it.isNotBlank() }
                            ?: "Aún no hay una respuesta disponible.",
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { reporteSeleccionado = null }) {
                    Text(text = "Cerrar")
                }
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (esAgente) "Historial de reportes" else "Mis reportes") },
                navigationIcon = {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clickable(
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
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { espacio ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(espacio),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (reportes.isEmpty()) {
                item {
                    Text(
                        if (esAgente) "Todavía no hay reportes en el historial."
                        else "Todavía no has enviado reportes.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(reportes.asReversed(), key = { it.folio }) { reporte ->
                    val compromiso = compromisos.find { it.folio == reporte.folio }
                    val tituloCompromiso = compromiso?.let {
                        "${it.empresa} · ${reporte.folio}"
                    } ?: "Compromiso: ${reporte.folio}"
                    Card(
                        onClick = { reporteSeleccionado = reporte },
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = tituloCompromiso,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(text = "Motivo: ${reporte.motivo}")
                            EtiquetaEstado(
                                if (reporte.estado == "Abierto") "Pendiente" else reporte.estado
                            )
                            Text(
                                text = "Ver detalle y respuesta",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(name = "Historial reportes claro", showBackground = true, showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Historial reportes oscuro", showBackground = true, showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HistorialReportesPreview() {
    AppThemePreview {
        HistorialReportesScreen(
            reportes = listOf(
                Reporte("PF-001", "El monto no corresponde", "Resuelto"),
                Reporte("PF-002", "Ya pagué por otro medio", "En proceso")
            ),
            onVolver = {}
        )
    }
}
