package com.example.app.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.app.viewmodel.RespuestaReporteViewModel
import com.example.app.model.Reporte
import com.example.app.model.Usuario
import com.example.app.ui.components.BotonPrincipal
import com.example.app.ui.components.BotonSecundario
import com.example.app.ui.components.EtiquetaEstado
import com.example.app.ui.components.TarjetaSeccion
import com.example.app.ui.theme.AppThemePreview
import com.example.app.ui.theme.Primario
import com.example.app.ui.theme.Secundario
import kotlinx.coroutines.launch

private val estadosReporte = listOf(
    "Pendientes",
    "En proceso",
    "Resueltos"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgenteScreen(
    usuario: Usuario?,
    reportes: List<Reporte>,
    onVerPerfil: () -> Unit,
    onVerHistorial: () -> Unit,
    onCerrarSesion: () -> Unit,
    onCambiarEstado: (Reporte, String, String) -> Boolean
) {
    var pestanaSeleccionada by remember {
        mutableIntStateOf(0)
    }

    var errorAccion by remember {
        mutableStateOf<String?>(null)
    }

    var reporteAResolver by remember { mutableStateOf<Reporte?>(null) }
    var rechazar by remember { mutableStateOf(false) }
    val respuestaViewModel: RespuestaReporteViewModel = viewModel()
    val respuesta by respuestaViewModel.uiState.collectAsStateWithLifecycle()

    reporteAResolver?.let { reporte ->
        AlertDialog(
            onDismissRequest = { reporteAResolver = null; respuestaViewModel.limpiar() },
            title = { Text(if (rechazar) "Rechazar reporte" else "Responder reporte") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Al cerrar el reporte, el compromiso volverá a estar pendiente.")
                    OutlinedTextField(
                        value = respuesta.texto,
                        onValueChange = respuestaViewModel::cambiarTexto,
                        label = { Text(if (rechazar) "Motivo del rechazo" else "Solución") },
                        isError = respuesta.error != null,
                        supportingText = { respuesta.error?.let { Text(it) } }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (respuestaViewModel.validar()) {
                        val tipo = if (rechazar) "Rechazado" else "Solución"
                        if (onCambiarEstado(reporte, "Pendiente", "$tipo: ${respuesta.texto.trim()}")) {
                            reporteAResolver = null
                            respuestaViewModel.limpiar()
                            errorAccion = null
                        } else {
                            respuestaViewModel.mostrarError("No se pudo guardar la respuesta. Inténtalo nuevamente.")
                        }
                    }
                }) { Text("Enviar") }
            },
            dismissButton = {
                TextButton(onClick = { reporteAResolver = null; respuestaViewModel.limpiar() }) {
                    Text("Cancelar")
                }
            }
        )
    }

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val reportesPestana = when (pestanaSeleccionada) {
        0 -> reportes.filter {
            it.estado == "Pendiente" || it.estado == "Abierto"
        }

        1 -> reportes.filter {
            it.estado == "En proceso"
        }

        else -> reportes.filter {
            it.estado == "Resuelto"
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    Row(modifier = Modifier.padding(start = 20.dp, top = 20.dp, end = 20.dp)) {
                        Text("Pago", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleLarge)
                        Text("Flex", color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.titleLarge)
                    }
                    Text(
                        text = "Agente · ${usuario?.nombre ?: "Usuario"}",
                        modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 16.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    HorizontalDivider()
                    Text(
                        text = "Reportes",
                        modifier = Modifier.padding(start = 20.dp, top = 14.dp, bottom = 4.dp),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge
                    )
                    NavigationDrawerItem(
                        label = { Text("Ver reportes") },
                        selected = false,
                        modifier = Modifier.padding(horizontal = 12.dp),
                        onClick = {
                            scope.launch { drawerState.close() }
                        }
                    )
                    NavigationDrawerItem(
                        label = { Text("Historial de reportes") },
                        selected = false,
                        modifier = Modifier.padding(horizontal = 12.dp),
                        onClick = {
                            scope.launch { drawerState.close() }
                            onVerHistorial()
                        }
                    )
                    HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
                    Text(
                        text = "Cuenta",
                        modifier = Modifier.padding(start = 20.dp, top = 14.dp, bottom = 4.dp),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge
                    )
                    NavigationDrawerItem(
                        label = { Text("Mi perfil") },
                        selected = false,
                        modifier = Modifier.padding(horizontal = 12.dp),
                        onClick = {
                            scope.launch { drawerState.close() }
                            onVerPerfil()
                        }
                    )
                    NavigationDrawerItem(
                        label = { Text("Cerrar sesión") },
                        selected = false,
                        modifier = Modifier.padding(horizontal = 12.dp),
                        onClick = {
                            scope.launch { drawerState.close() }
                            onCerrarSesion()
                        }
                    )
                }
            }
        }
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = buildAnnotatedString {
                                    withStyle(SpanStyle(color = Primario)) {
                                        append("Pago")
                                    }
                                    withStyle(SpanStyle(color = Secundario)) {
                                        append("Flex")
                                    }
                                },
                                style = MaterialTheme.typography.titleLarge
                            )

                            Text(
                                text = "Panel Agente: ${usuario?.nombre ?: "Agente"}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    navigationIcon = {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .semantics {
                                    contentDescription = "Abrir menú"
                                }
                                .clickable(
                                    interactionSource = remember {
                                        MutableInteractionSource()
                                    },
                                    indication = null,
                                    role = Role.Button,
                                    onClick = {
                                        scope.launch {
                                            drawerState.open()
                                        }
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "☰",
                                style = MaterialTheme.typography.titleLarge,
                                color = Secundario
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        navigationIconContentColor = Secundario
                    )
                )
            }
        ) { espacio ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(espacio)
            ) {
                TabRow(
                    selectedTabIndex = pestanaSeleccionada
                ) {
                    estadosReporte.forEachIndexed { indice, estado ->
                        Tab(
                            selected = pestanaSeleccionada == indice,
                            onClick = {
                                pestanaSeleccionada = indice
                            },
                            text = {
                                Text(text = estado)
                            },
                            selectedContentColor = Secundario,
                            unselectedContentColor =
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (reportesPestana.isEmpty()) {
                        item {
                            Text(
                                text = when (pestanaSeleccionada) {
                                    0 -> "No hay reportes pendientes."
                                    1 -> "No hay reportes en proceso."
                                    else -> "Todavía no hay reportes resueltos."
                                },
                                modifier = Modifier.padding(vertical = 8.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        items(
                            items = reportesPestana,
                            key = { it.folio }
                        ) { reporte ->

                            TarjetaSeccion(
                                titulo = "Compromiso: ${reporte.folio}"
                            ) {
                                Text(
                                    text = "Motivo: ${reporte.motivo}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                EtiquetaEstado(
                                    if (reporte.estado == "Abierto") {
                                        "Pendiente"
                                    } else {
                                        reporte.estado
                                    }
                                )

                                when (reporte.estado) {
                                    "Pendiente",
                                    "Abierto" -> {
                                        BotonPrincipal(
                                            texto = "Tomar reporte",
                                            onClick = {
                                                errorAccion = if (
                                                    onCambiarEstado(
                                                        reporte,
                                                        "En proceso",
                                                        ""
                                                    )
                                                ) {
                                                    null
                                                } else {
                                                    "No se pudo actualizar el reporte. Inténtalo nuevamente."
                                                }
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }

                                    "En proceso" -> {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            BotonPrincipal(
                                                texto = "Responder",
                                                onClick = {
                                                    respuestaViewModel.limpiar()
                                                    rechazar = false
                                                    reporteAResolver = reporte
                                                },
                                                modifier = Modifier.weight(1f)
                                            )
                                            BotonSecundario(
                                                texto = "Rechazar",
                                                onClick = {
                                                    respuestaViewModel.limpiar()
                                                    rechazar = true
                                                    reporteAResolver = reporte
                                                },
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }

                                errorAccion?.let { mensaje ->
                                    Text(
                                        text = mensaje,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(
    name = "Agente claro",
    showBackground = true,
    showSystemUi = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "Agente oscuro",
    showBackground = true,
    showSystemUi = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun AgentePreview() {
    AppThemePreview {
        AgenteScreen(
            usuario = Usuario(
                id = 2,
                rut = "10.625.244-K",
                nombre = "Carla Agente",
                correo = "carla@pagoflex.cl",
                rolId = 2,
                rolNombre = "AGENTE"
            ),
            reportes = listOf(
                Reporte(
                    "PF-001",
                    "El monto no corresponde",
                    "Pendiente"
                ),
                Reporte(
                    "PF-002",
                    "Ya pagué por otro medio",
                    "En proceso"
                ),
                Reporte(
                    "PF-003",
                    "No reconozco este cobro",
                    "Resuelto"
                )
            ),
            onVerPerfil = {},
            onVerHistorial = {},
            onCerrarSesion = {},
            onCambiarEstado = { _, _, _ -> true }
        )
    }
}