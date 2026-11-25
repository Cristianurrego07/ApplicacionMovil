package com.example.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.app.R
import com.example.app.model.Compromiso
import com.example.app.model.Servicio
import com.example.app.ui.theme.AppThemePreview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiciosAsociadosScreen(
    servicios: List<Servicio>,
    onVolver: () -> Unit,
    compromisos: List<Compromiso> = emptyList(),
    onVerCompromiso: (String) -> Unit = {},
    onAgregarServicio: () -> Unit = {}
) {
    var seleccionado by remember { mutableStateOf<Servicio?>(null) }
    seleccionado?.let { servicio ->
        AlertDialog(
            onDismissRequest = { seleccionado = null },
            title = { Text(servicio.nombreEmpresa) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Alias: ${servicio.alias}")
                    Text("RUT: ${servicio.rutPagador}")
                }
            },
            confirmButton = { TextButton(onClick = { seleccionado = null }) { Text("Cerrar") } }
        )
    }
    Scaffold(topBar = {
        TopAppBar(title = { Text("Mis servicios") }, navigationIcon = {
            Box(
                modifier = Modifier.size(48.dp).clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onVolver
                ),
                contentAlignment = Alignment.Center
            ) {
                Icon(painterResource(R.drawable.ic_arrow_back), contentDescription = "Volver",
                    tint = MaterialTheme.colorScheme.primary)
            }
        })
    }) { espacio ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(espacio),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (servicios.isEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Todavía no tienes servicios asociados.")
                        TextButton(onClick = onAgregarServicio) { Text("Agregar servicio") }
                    }
                }
                if (compromisos.isNotEmpty()) {
                    item {
                        Text("Tus compromisos de pago", style = MaterialTheme.typography.titleMedium)
                    }
                    items(compromisos, key = { it.folio }) { compromiso ->
                        Card(
                            onClick = { onVerCompromiso(compromiso.folio) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            ListItem(
                                headlineContent = { Text(compromiso.empresa) },
                                supportingContent = { Text(compromiso.concepto) },
                                trailingContent = { Text(compromiso.estado) }
                            )
                        }
                    }
                }
            } else {
                items(servicios, key = { it.id }) { servicio ->
                    Card(onClick = { seleccionado = servicio }, modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(servicio.nombreEmpresa, style = MaterialTheme.typography.titleMedium)
                            Text(servicio.alias)
                            Text("Ver datos", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ServiciosAsociadosPreview() {
    AppThemePreview {
        ServiciosAsociadosScreen(
            listOf(Servicio(1, 1, "Enel", "12.345.678-5", "Electricidad")),
            onVolver = {}
        )
    }
}
