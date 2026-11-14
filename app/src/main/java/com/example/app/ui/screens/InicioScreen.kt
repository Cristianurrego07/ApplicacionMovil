package com.example.app.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.app.R
import com.example.app.model.Compromiso
import com.example.app.model.Servicio
import com.example.app.model.Usuario
import com.example.app.ui.components.TarjetaCompromiso
import com.example.app.ui.theme.AppThemePreview
import com.example.app.ui.theme.PagoFlexGreen
import com.example.app.ui.theme.Primario
import com.example.app.ui.theme.Secundario
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InicioScreen(
    usuario: Usuario?,
    compromisos: List<Compromiso>,
    servicios: List<Servicio>,
    onVolver: () -> Unit,
    onVerDetalle: (String) -> Unit,
    onVerPerfil: () -> Unit,
    onAgregarServicio: () -> Unit,
    onEliminarServicios: () -> Unit,
    onVerHistorial: () -> Unit,
    onReportarProblema: () -> Unit,
    onVerHistorialReportes: () -> Unit
) {
    val pendientes = compromisos.filter {
        it.estado == "Pendiente" || it.estado == "Vencido"
    }

    val totalPendiente = pendientes.sumOf {
        it.monto + if (it.estado == "Vencido") it.recargo else 0
    }

    val proximoCompromiso = pendientes.firstOrNull()

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

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
                        Text(
                            text = "Mi perfil",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onVerPerfil()
                    }
                )

                NavigationDrawerItem(
                    label = {
                        Text(
                            text = "Agregar servicio",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onAgregarServicio()
                    }
                )

                NavigationDrawerItem(
                    label = {
                        Text(
                            text = "Eliminar servicios",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onEliminarServicios()
                    }
                )

                NavigationDrawerItem(
                    label = {
                        Text(
                            text = "Historial de pagos",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onVerHistorial()
                    }
                )

                NavigationDrawerItem(
                    label = {
                        Text(
                            text = "Reportar problema",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onReportarProblema()
                    }
                )

                NavigationDrawerItem(
                    label = {
                        Text(
                            text = "Historial de reportes",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onVerHistorialReportes()
                    }
                )

                HorizontalDivider()

                NavigationDrawerItem(
                    label = {
                        Text(
                            text = "Cerrar sesión",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onVolver()
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
                        Image(
                            painter = painterResource(R.drawable.logo_pagoflex),
                            contentDescription = "Logo PagoFlex",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .width(100.dp)
                                .height(50.dp)
                        )
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
                            Icon(
                                painter = painterResource(R.drawable.ic_menu),
                                contentDescription = null,
                                tint = Secundario,
                                modifier = Modifier.size(24.dp)
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

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(espacio)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Saludo y descripción
                item {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Hola, ${usuario?.nombre ?: "cliente"}",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        Text(
                            text = "Aquí tienes un resumen de tus pagos",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Resumen de pagos
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = PagoFlexGreen
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Total pendiente",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White
                            )

                            Text(
                                text = formatearMonto(totalPendiente),
                                style = MaterialTheme.typography.headlineSmall,
                                color = Color.White
                            )

                            Surface(
                                color = Color.White,
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text(
                                    text = "Próximo vencimiento",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Black,
                                    modifier = Modifier.padding(
                                        horizontal = 8.dp,
                                        vertical = 4.dp
                                    )
                                )
                            }

                            Text(
                                text = proximoCompromiso?.let {
                                    fechaVisibleInicio(it.vencimiento)
                                } ?: "Sin pagos pendientes",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White
                            )
                        }
                    }
                }

                // Lista de compromisos
                item {
                    Text(
                        text = "Tus próximos compromisos:",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                if (pendientes.isEmpty()) {
                    item {
                        Text(
                            text = "No tienes compromisos pendientes.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    items(pendientes) { compromiso ->
                        TarjetaCompromiso(
                            compromiso = compromiso,
                            alPresionar = {
                                onVerDetalle(compromiso.folio)
                            }
                        )
                    }
                }
            }
        }
    }
}

private fun formatearMonto(monto: Int): String {
    val formato = NumberFormat.getIntegerInstance(Locale("es", "CL"))
    return "$" + formato.format(monto)
}

private fun fechaVisibleInicio(fecha: String): String {
    return try {
        val fechaConvertida = try {
            SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(fecha)
        } catch (_: Exception) {
            SimpleDateFormat("dd-MM-yyyy", Locale.US).parse(fecha)
        }

        fechaConvertida?.let {
            SimpleDateFormat(
                "dd 'de' MMMM 'de' yyyy",
                Locale("es", "CL")
            ).format(it)
        } ?: fecha
    } catch (_: Exception) {
        fecha
    }
}

@Preview(
    name = "Inicio claro",
    showBackground = true,
    showSystemUi = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "Inicio oscuro",
    showBackground = true,
    showSystemUi = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun InicioPreview() {
    AppThemePreview {
        val usuario = Usuario(
            rut = "20.435.214-5",
            nombre = "Sofía",
            correo = "sofia@duocuc.cl",
            rolId = 1L,
            rolNombre = "CLIENTE"
        )

        val compromisos = listOf(
            Compromiso(
                folio = "PF-001",
                empresa = "Duoc Uc",
                usuario = usuario,
                concepto = "Mensualidad",
                monto = 356000,
                vencimiento = "2026-10-25"
            ),
            Compromiso(
                folio = "PF-002",
                empresa = "Enel",
                usuario = usuario,
                concepto = "Cuenta de luz",
                monto = 28000,
                vencimiento = "2026-10-20"
            )
        )

        val servicios = listOf(
            Servicio(
                usuarioId = usuario.id,
                nombreEmpresa = "Instituto Profesional",
                rutPagador = "20.435.214-5",
                alias = "Mensualidad"
            )
        )

        InicioScreen(
            usuario = usuario,
            compromisos = compromisos,
            servicios = servicios,
            onVolver = {},
            onVerDetalle = {},
            onVerPerfil = {},
            onAgregarServicio = {},
            onEliminarServicios = {},
            onVerHistorial = {},
            onReportarProblema = {},
            onVerHistorialReportes = {}
        )
    }
}