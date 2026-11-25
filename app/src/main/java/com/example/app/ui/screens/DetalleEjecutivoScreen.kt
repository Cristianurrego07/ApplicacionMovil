package com.example.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.app.viewmodel.EditarCuotaViewModel
import com.example.app.model.Compromiso
import com.example.app.model.Reporte
import com.example.app.ui.components.BotonPrincipal
import com.example.app.ui.components.BotonSecundario
import com.example.app.ui.components.EtiquetaEstado
import java.text.NumberFormat
import java.util.Locale

@Composable
fun DetalleEjecutivoScreen(
    compromiso: Compromiso,
    reporte: Reporte?,
    onVolver: () -> Unit,
    onAnular: suspend (String) -> Boolean,
    onModificarCuota: suspend (Int, String) -> Boolean
) {
    val editarCuota: EditarCuotaViewModel = viewModel(key = "cuota_${compromiso.folio}")
    val scope = rememberCoroutineScope()
    var guardando by remember { mutableStateOf(false) }
    var mostrarEditar by remember { mutableStateOf(false) }
    var mostrar by remember { mutableStateOf(false) }
    var motivo by remember { mutableStateOf("Repactación") }
    var comentario by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    Scaffold { espacio ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(espacio)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BotonSecundario("Volver", onVolver)
            Text("Detalle del compromiso", style = MaterialTheme.typography.headlineSmall)
            Text("Empresa: ${compromiso.empresa}")
            Text("Cliente: ${compromiso.usuario.nombre}")
            Text("RUT: ${compromiso.usuario.rut}")
            Text("Correo: ${compromiso.usuario.correo}")
            if (compromiso.usuario.telefono.isNotBlank()) Text("Teléfono: ${compromiso.usuario.telefono}")
            Text("Concepto: ${compromiso.concepto}")
            Text("Monto: ${NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-CL")).format(compromiso.monto)}")
            Text("Vencimiento: ${compromiso.vencimiento}")
            if (compromiso.numeroCuota.isNotBlank()) Text("Cuota: ${compromiso.numeroCuota}")
            EtiquetaEstado(compromiso.estado)
            if (compromiso.estado == "En revisión") Text("Reporte: ${reporte?.motivo ?: "No disponible"}")
            if (compromiso.numeroCuota.isNotBlank() && compromiso.estado in listOf("Pendiente", "Vencido")) {
                BotonSecundario("Modificar cuota", {
                    editarCuota.cargar(compromiso.monto, compromiso.vencimiento)
                    mostrarEditar = true
                })
            }
            if (compromiso.estado in listOf("Pendiente", "Vencido", "En revisión")) {
                BotonPrincipal("Anular compromiso", { mostrar = true })
            }
        }
    }
    if (mostrarEditar) {
        AlertDialog(
            onDismissRequest = { mostrarEditar = false },
            title = { Text("Modificar cuota ${compromiso.numeroCuota}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Modificar el monto cambiará el total pendiente del plan.")
                    OutlinedTextField(
                        value = editarCuota.monto,
                        onValueChange = {
                            editarCuota.monto = it
                            editarCuota.errorMonto = null
                            editarCuota.errorGeneral = null
                        },
                        label = { Text("Monto de la cuota") },
                        isError = editarCuota.errorMonto != null,
                        supportingText = { editarCuota.errorMonto?.let { Text(it) } },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = editarCuota.fecha,
                        onValueChange = {
                            editarCuota.fecha = it
                            editarCuota.errorFecha = null
                            editarCuota.errorGeneral = null
                        },
                        label = { Text("Vencimiento (DD/MM/AAAA)") },
                        isError = editarCuota.errorFecha != null,
                        supportingText = { editarCuota.errorFecha?.let { Text(it) } },
                        singleLine = true
                    )
                    editarCuota.errorGeneral?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            },
            confirmButton = {
                TextButton(enabled = !guardando, onClick = {
                    guardando = true
                    scope.launch {
                        try {
                            if (editarCuota.guardar(onModificarCuota)) {
                                mostrarEditar = false
                                onVolver()
                            }
                        } finally {
                            guardando = false
                        }
                    }
                }) { Text(if (guardando) "Guardando…" else "Guardar") }
            },
            dismissButton = { TextButton(onClick = { mostrarEditar = false }) { Text("Cancelar") } }
        )
    }
    if (mostrar) {
        AlertDialog(
            onDismissRequest = { mostrar = false },
            title = { Text("Anular compromiso") },
            text = {
                Column {
                    Text("Selecciona el motivo:")
                    listOf("Repactación", "Error de registro", "Pagado directamente a la empresa", "Otro").forEach { opcion ->
                        TextButton(onClick = { motivo = opcion; error = null }) {
                            Text(if (motivo == opcion) "✓ $opcion" else opcion)
                        }
                    }
                    if (motivo == "Otro") {
                        OutlinedTextField(
                            value = comentario,
                            onValueChange = {
                                comentario = it
                                error = null
                            },
                            label = { Text("Comentario") }
                        )
                    }
                    error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            },
            confirmButton = {
                TextButton(enabled = !guardando, onClick = {
                    val motivoFinal = if (motivo == "Otro") {
                        "Otro: ${comentario.trim()}"
                    } else {
                        motivo
                    }
                    if (motivo == "Otro" && comentario.isBlank()) {
                        error = "Describe el motivo"
                    } else {
                        guardando = true
                        scope.launch {
                            try {
                                if (onAnular(motivoFinal)) {
                                    mostrar = false
                                    onVolver()
                                } else {
                                    error = "No se pudo anular el compromiso"
                                }
                            } finally {
                                guardando = false
                            }
                        }
                    }
                }) { Text("Confirmar anulación") }
            },
            dismissButton = { TextButton(onClick = { mostrar = false }) { Text("Cancelar") } }
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, showSystemUi = true)
@Composable
private fun DetalleEjecutivoPreview() {
    com.example.app.ui.theme.AppThemePreview {
        val usuario = com.example.app.model.Usuario(
            id = 1,
            rut = "12.345.678-5",
            nombre = "Sofía Pérez",
            correo = "sofia@correo.cl",
            rolId = 1
        )
        DetalleEjecutivoScreen(
            compromiso = Compromiso(
                folio = "PF-001",
                empresa = "Duoc Uc",
                usuario = usuario,
                concepto = "Mensualidad",
                monto = 182000,
                vencimiento = "2026-12-20",
                estado = "Pendiente"
            ),
            reporte = null,
            onVolver = {},
            onAnular = { false },
            onModificarCuota = { _, _ -> false }
        )
    }
}
