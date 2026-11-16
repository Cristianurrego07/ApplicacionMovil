package com.example.app.ui.screens

import android.content.res.Configuration
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
    onCambiarEstado: (Reporte, String) -> Boolean
) {
    var pestanaSeleccionada by remember {
        mutableIntStateOf(0)
    }

    var errorAccion by remember {
        mutableStateOf<String?>(null)
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
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = Primario)) {
                            append("Pago")
                        }
                        withStyle(SpanStyle(color = Secundario)) {
                            append("Flex")
                        }
                    },
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(20.dp)
                )

                HorizontalDivider()

                NavigationDrawerItem(
                    label = {
                        Text(text = "Ver reportes")
                    },
                    selected = false,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                        }
                    }
                )

                NavigationDrawerItem(
                    label = {
                        Text(text = "Mi perfil")
                    },
                    selected = false,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                        }
                        onVerPerfil()
                    }
                )

                NavigationDrawerItem(
                    label = {
                        Text(text = "Historial reportes")
                    },
                    selected = false,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                        }
                        onVerHistorial()
                    }
                )

                HorizontalDivider()

                NavigationDrawerItem(
                    label = {
                        Text(text = "Cerrar sesión")
                    },
                    selected = false,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                        }
                        onCerrarSesion()
                    }
                )
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
                // Pestañas de estados de reportes
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

                // Lista de reportes
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
                                                        "En proceso"
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
                                            horizontalArrangement =
                                                Arrangement.spacedBy(8.dp),
                                            verticalAlignment =
                                                Alignment.CenterVertically
                                        ) {
                                            BotonPrincipal(
                                                texto = "Reactivar",
                                                onClick = {
                                                    errorAccion = if (
                                                        onCambiarEstado(
                                                            reporte,
                                                            "Pendiente"
                                                        )
                                                    ) {
                                                        null
                                                    } else {
                                                        "No se pudo reactivar el compromiso."
                                                    }
                                                },
                                                modifier = Modifier.weight(1f)
                                            )

                                            BotonSecundario(
                                                texto = "Anular",
                                                onClick = {
                                                    errorAccion = if (
                                                        onCambiarEstado(
                                                            reporte,
                                                            "Anulado"
                                                        )
                                                    ) {
                                                        null
                                                    } else {
                                                        "No se pudo anular el compromiso."
                                                    }
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
            onCambiarEstado = { _, _ -> true }
        )
    }
}