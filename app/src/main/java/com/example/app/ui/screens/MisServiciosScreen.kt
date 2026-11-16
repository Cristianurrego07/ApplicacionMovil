package com.example.app.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import com.example.app.model.Usuario
import com.example.app.ui.components.EtiquetaEstado
import com.example.app.ui.components.TarjetaSeccion
import com.example.app.ui.theme.AppThemePreview
import com.example.app.ui.theme.Secundario
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisServiciosScreen(
    compromisos: List<Compromiso>,
    onVolver: () -> Unit,
    onEliminar: (Compromiso) -> Boolean
) {
    var compromisoSeleccionado by remember {
        mutableStateOf<Compromiso?>(null)
    }
    var errorEliminar by remember {
        mutableStateOf<String?>(null)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Eliminar servicios",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
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
                }
            )
        }
    ) { espacio ->
        if (compromisos.isEmpty()) {
            Text(
                text = "No tienes compromisos registrados.",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(espacio)
                    .padding(16.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(espacio)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(compromisos, key = { it.folio }) { compromiso ->
                    TarjetaSeccion(titulo = compromiso.empresa) {
                        Text(text = compromiso.concepto)
                        Text(text = "Folio: ${compromiso.folio}")
                        Text(text = "Monto: ${formatearMonto(compromiso.monto)}")
                        Text(text = "Vencimiento: ${fechaVisible(compromiso.vencimiento)}")
                        EtiquetaEstado(estado = compromiso.estado)
                        if (compromiso.estado == "Pendiente" || compromiso.estado == "Vencido") {
                            TextButton(
                                onClick = {
                                    compromisoSeleccionado = compromiso
                                    errorEliminar = null
                                }
                            ) {
                                Text(
                                    text = "Eliminar",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        } else {
                            Text(
                                text = "No se puede eliminar porque tiene un pago o reporte asociado.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }

    compromisoSeleccionado?.let { compromiso ->
        AlertDialog(
            onDismissRequest = {
                compromisoSeleccionado = null
                errorEliminar = null
            },
            title = { Text(text = "Eliminar compromiso") },
            text = {
                Column {
                    Text(text = "¿Quieres eliminar ${compromiso.concepto} de ${compromiso.empresa}?")
                    errorEliminar?.let { mensaje ->
                        Text(text = mensaje, color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (onEliminar(compromiso)) {
                            compromisoSeleccionado = null
                            errorEliminar = null
                        } else {
                            errorEliminar =
                                "No se puede eliminar un compromiso con pago o reporte asociado."
                        }
                    }
                ) {
                    Text(text = "Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        compromisoSeleccionado = null
                        errorEliminar = null
                    }
                ) {
                    Text(text = "Cancelar")
                }
            }
        )
    }
}

private fun formatearMonto(monto: Int): String =
    "$" + java.text.NumberFormat.getIntegerInstance(Locale("es", "CL")).format(monto)

private fun fechaVisible(fecha: String): String = try {
    val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(fecha)
    date?.let { SimpleDateFormat("dd/MM/yyyy", Locale("es", "CL")).format(it) } ?: fecha
} catch (_: Exception) {
    fecha
}

@Preview(name = "Eliminar servicios claro", showBackground = true, showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Eliminar servicios oscuro", showBackground = true, showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun MisServiciosPreview() {
    AppThemePreview {
        val usuario = Usuario(1,
            "20.435.214-5",
            "Sofía Pérez",
            "sofia@duocuc.cl",
            1,
            "CLIENTE")
        MisServiciosScreen(
            compromisos = listOf(
                Compromiso("PF-001",
                    "Universidad",
                    usuario,
                    "Mensualidad",
                    350000,
                    "2026-10-15"),
                Compromiso("PF-002",
                    "Enel",
                    usuario,
                    "Luz",
                    28000,
                    "2026-10-20"),
                Compromiso("PF-003",
                    "Condominio",
                    usuario,
                    "Gasto común",
                    45000,
                    "2026-10-28"),
                Compromiso("PF-004",
                    "Agua",
                    usuario,
                    "Cuenta de agua",
                    35000,
                    "2026-10-29")
            ),
            onVolver = {},
            onEliminar = { true }
        )
    }
}
