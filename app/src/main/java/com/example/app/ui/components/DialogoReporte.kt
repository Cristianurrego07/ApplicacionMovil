package com.example.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.app.model.Compromiso
import com.example.app.viewmodel.ReporteFormularioViewModel

@Composable
fun DialogoReporte(
    compromiso: Compromiso,
    onEnviar: (String) -> Boolean,
    onCerrar: () -> Unit,
    onExito: () -> Unit
) {
    var motivoSeleccionado by remember { mutableStateOf("Ya pagué por otro medio") }
    var comentario by remember { mutableStateOf("") }
    val formulario: ReporteFormularioViewModel = viewModel(key = "reporte_${compromiso.folio}")
    val estado by formulario.uiState.collectAsStateWithLifecycle()

    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text("¿Qué problema tienes?") },
        text = {
            Column {
                Text("${compromiso.empresa} · ${compromiso.concepto}")
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
                            onClick = {
                                motivoSeleccionado = opcion
                                formulario.limpiarError()
                            }
                        )
                        Text(opcion)
                    }
                }
                if (motivoSeleccionado == "Otro") {
                    CampoTexto(
                        valor = comentario,
                        etiqueta = "Describe el problema",
                        alCambiar = {
                            comentario = it
                            formulario.limpiarError()
                        },
                        error = estado.error
                    )
                } else {
                    estado.error?.let { mensaje ->
                        Text(mensaje, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val motivo = formulario.crearMotivo(motivoSeleccionado, comentario)
                if (motivo != null) {
                    if (onEnviar(motivo)) {
                        formulario.limpiarError()
                        onExito()
                    } else {
                        formulario.mostrarError("No se pudo enviar el reporte. Inténtalo nuevamente.")
                    }
                }
            }) { Text("Enviar reporte") }
        },
        dismissButton = {
            TextButton(onClick = {
                formulario.limpiarError()
                onCerrar()
            }) { Text("Cancelar") }
        }
    )
}
