package com.example.app.ui.screens

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.app.R
import com.example.app.model.Compromiso
import com.example.app.model.Usuario
import com.example.app.ui.components.RecordatorioCompromiso
import com.example.app.ui.components.TarjetaCompromiso
import com.example.app.ui.components.TarjetaEstadistica
import com.example.app.ui.theme.AppThemePreview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsuarioScreen(
    usuario: Usuario?,
    compromisos: List<Compromiso>,
    onVolver: () -> Unit,
    onVerDetalle: (String) -> Unit
) {
    val pendientes = compromisos.filter { it.estado == "Pendiente" }
    var filtroSeleccionado by rememberSaveable { mutableStateOf("Todos") }
    val compromisosFiltrados = if (filtroSeleccionado == "Todos") {
        compromisos
    } else {
        compromisos.filter { it.estado == filtroSeleccionado }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis compromisos") },
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

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(espacio)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("Hola, ${usuario?.nombre ?: "cliente"}")
            }

            item {
                TarjetaEstadistica(
                    etiqueta = "Compromisos pendientes",
                    valor = pendientes.size.toString()
                )
            }

            item {
                val filtros = listOf("Todos", "Pendiente", "Vencido", "En revisión", "Pagado", "Anulado")
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    filtros.forEach { filtro ->
                        FilterChip(
                            selected = filtroSeleccionado == filtro,
                            onClick = { filtroSeleccionado = filtro },
                            label = { Text(text = filtro) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                labelColor = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }
            }

            if (compromisos.isEmpty()) {
                item {
                    Text("Todavía no tienes compromisos registrados.")
                }
            } else if (compromisosFiltrados.isEmpty()) {
                item {
                    Text(
                        text = "No hay compromisos para este filtro.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(compromisosFiltrados, key = { it.folio }) { compromiso ->
                    TarjetaCompromiso(
                        compromiso = compromiso,
                        alPresionar = {
                            onVerDetalle(compromiso.folio)
                        },
                        recordatorio = {
                            if (compromiso.estado == "Pendiente" || compromiso.estado == "Vencido") {
                                RecordatorioCompromiso(compromiso)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Preview(name = "Mis compromisos", showBackground = true, showSystemUi = true)
@Composable
private fun UsuarioScreenPreview() {
    val usuario = Usuario(
        id = 1,
        rut = "20.435.214-5",
        nombre = "Sofía",
        correo = "sofia@gmail.com",
        rolId = 1,
        rolNombre = "CLIENTE"
    )
    AppThemePreview {
        UsuarioScreen(
            usuario = usuario,
            compromisos = listOf(
                Compromiso("PF-001", "Universidad", usuario, "Mensualidad", 350000, "2026-10-15", "Pendiente"),
                Compromiso("PF-002", "Enel", usuario, "Electricidad", 28000, "2026-10-20", "Pagado"),
                Compromiso("PF-003", "Agua", usuario, "Consumo", 35000, "2026-10-29", "Vencido")
            ),
            onVolver = {},
            onVerDetalle = {}
        )
    }
}

