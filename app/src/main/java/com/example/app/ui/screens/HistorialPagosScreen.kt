package com.example.app.ui.screens

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.app.R
import com.example.app.model.Comprobante
import com.example.app.model.Compromiso
import com.example.app.model.Usuario
import com.example.app.ui.theme.AppThemePreview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialPagosScreen(
    comprobantes: List<Comprobante>,
    compromisos: List<Compromiso>,
    onVolver: () -> Unit,
    onVerDetalle: (String) -> Unit
) {
    var estadoSeleccionado by rememberSaveable { mutableStateOf("Todos") }
    val compromisosFiltrados = compromisos.filter { compromiso ->
        estadoSeleccionado == "Todos" || compromiso.estado == estadoSeleccionado
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Historial de pagos") },
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
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { espacio ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(espacio)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val estados = listOf("Todos", "Pagado", "Pendiente", "Vencido", "En revisión", "Anulado")
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                estados.forEach { estado ->
                    FilterChip(
                        selected = estadoSeleccionado == estado,
                        onClick = { estadoSeleccionado = estado },
                        label = { Text(estado) }
                    )
                }
            }
            if (compromisos.isEmpty()) {
                Text("Todavía no tienes compromisos pagados.")
            } else if (compromisosFiltrados.isEmpty()) {
                Text("No hay compromisos que coincidan con los filtros.")
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(compromisosFiltrados, key = { it.folio }) { compromiso ->
                        val comprobante = comprobantes.find { it.folioCompromiso == compromiso.folio }
                        Card(
                            onClick = { onVerDetalle(compromiso.folio) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(
                                Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(compromiso.empresa, style = MaterialTheme.typography.titleMedium)
                                Text(compromiso.concepto)
                                Text("Estado: ${compromiso.estado}")
                                if (comprobante != null) {
                                    Text("${formatearMonto(comprobante.montoPagado)} · ${comprobante.fechaHora}")
                                    Text("Medio de pago: ${comprobante.medioPagoVisible}")
                                    Text("Comprobante ${comprobante.numero}")
                                } else {
                                    Text("Monto: ${formatearMonto(compromiso.monto)}")
                                    Text("Vencimiento: ${compromiso.vencimiento}")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatearMonto(monto: Int): String =
    "$" + java.text.NumberFormat.getIntegerInstance(java.util.Locale("es", "CL")).format(monto)

@Preview(name = "Historial de pagos", showBackground = true, showSystemUi = true)
@Composable
private fun HistorialPagosPreview() {
    val usuario =
        Usuario(1,
            "20.435.214-5",
            "Sofía",
            "sofia@example.com",
            1,
            "CLIENTE")
    AppThemePreview {
        HistorialPagosScreen(
            compromisos = listOf(
                Compromiso("PF-001",
                    "Universidad", usuario,
                    "Cuota mensual",
                    350000,
                    "2026-10-15",
                    "Pagado"),
                Compromiso("PF-002",
                    "Enel", usuario,
                    "Electricidad",
                    28000,
                    "2026-10-20",
                    "Pendiente")
            ),
            comprobantes = listOf(
                Comprobante(
                    numero = "CPR-000001",
                    folioCompromiso = "PF-001",
                    empresa = "Universidad",
                    concepto = "Cuota mensual",
                    montoPagado = 350000,
                    fechaHora = "2026-10-15 10:30"
                ),
                Comprobante(
                    numero = "CPR-000002",
                    folioCompromiso = "PF-002",
                    empresa = "Enel",
                    concepto = "Electricidad",
                    montoPagado = 28000,
                    fechaHora = "2026-10-20 12:00"
                )
            ),
            onVolver = {},
            onVerDetalle = {}
        )
    }
}

@Preview(name = "Historial sin pagos", showBackground = true, showSystemUi = true)
@Composable
private fun HistorialPagosVacioPreview() {
    AppThemePreview {
        HistorialPagosScreen(
            comprobantes = emptyList(),
            compromisos = emptyList(),
            onVolver = {},
            onVerDetalle = {}
        )
    }
}
