package com.example.app.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
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
import com.example.app.ui.components.RecordatorioCompromiso
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
    onVerCompromisos: () -> Unit,
    onAgregarServicio: () -> Unit,
    onEliminarServicios: () -> Unit,
    onVerHistorial: () -> Unit,
    onReportarProblema: () -> Unit,
    onVerHistorialReportes: () -> Unit,
    onVerServicios: () -> Unit = {}
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
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    Row(modifier = Modifier.padding(start = 20.dp, top = 20.dp, end = 20.dp)) {
                        Text("Pago", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleLarge)
                        Text("Flex", color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.titleLarge)
                    }
                    Text(
                        text = "Cliente · ${usuario?.nombre ?: "Usuario"}",
                        modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 16.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    HorizontalDivider()
                    Text(
                        text = "Inicio",
                        modifier = Modifier.padding(start = 20.dp, top = 14.dp, bottom = 4.dp),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge
                    )
                    NavigationDrawerItem(
                        label = { Text("Mis compromisos") },
                        selected = false,
                        modifier = Modifier.padding(horizontal = 12.dp),
                        onClick = {
                            scope.launch { drawerState.close() }
                            onVerCompromisos()
                        }
                    )
                    HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
                    Text(
                        text = "Servicios",
                        modifier = Modifier.padding(start = 20.dp, top = 14.dp, bottom = 4.dp),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge
                    )
                    NavigationDrawerItem(
                        label = { Text("Mis servicios") },
                        selected = false,
                        modifier = Modifier.padding(horizontal = 12.dp),
                        onClick = {
                            scope.launch { drawerState.close() }
                            onVerServicios()
                        }
                    )
                    NavigationDrawerItem(
                        label = { Text("Agregar servicio") },
                        selected = false,
                        modifier = Modifier.padding(horizontal = 12.dp),
                        onClick = {
                            scope.launch { drawerState.close() }
                            onAgregarServicio()
                        }
                    )
                    NavigationDrawerItem(
                        label = { Text("Eliminar servicios") },
                        selected = false,
                        modifier = Modifier.padding(horizontal = 12.dp),
                        onClick = {
                            scope.launch { drawerState.close() }
                            onEliminarServicios()
                        }
                    )
                    HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
                    Text(
                        text = "Actividad",
                        modifier = Modifier.padding(start = 20.dp, top = 14.dp, bottom = 4.dp),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge
                    )
                    NavigationDrawerItem(
                        label = { Text("Historial de pagos") },
                        selected = false,
                        modifier = Modifier.padding(horizontal = 12.dp),
                        onClick = {
                            scope.launch { drawerState.close() }
                            onVerHistorial()
                        }
                    )
                    NavigationDrawerItem(
                        label = { Text("Reportar problema") },
                        selected = false,
                        modifier = Modifier.padding(horizontal = 12.dp),
                        onClick = {
                            scope.launch { drawerState.close() }
                            onReportarProblema()
                        }
                    )
                    NavigationDrawerItem(
                        label = { Text("Mis reportes") },
                        selected = false,
                        modifier = Modifier.padding(horizontal = 12.dp),
                        onClick = {
                            scope.launch { drawerState.close() }
                            onVerHistorialReportes()
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
                            onVolver()
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
                                tint = Primario,
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
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }

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
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White
                            )

                            Text(
                                text = formatearMonto(totalPendiente),
                                style = MaterialTheme.typography.headlineLarge,
                                color = Color.White
                            )

                            Surface(
                                color = Color.White,
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text(
                                    text = "Próximo vencimiento",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary,
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

        val hoy = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(java.util.Date())

        fun estadoSegunFecha(fecha: String): String {
            return if (fecha < hoy) "Vencido" else "Pendiente"
        }

        val compromisos = listOf(
            Compromiso(
                folio = "PF-UNI-001",
                empresa = "Duoc Uc",
                usuario = usuario,
                concepto = "Mensualidad",
                monto = 356000,
                vencimiento = "2026-11-19",
                estado = estadoSegunFecha("2026-11-19"),
                recargo = 7000
            ),
            Compromiso(
                folio = "PF-LUZ-001",
                empresa = "Enel",
                usuario = usuario,
                concepto = "Luz",
                monto = 28000,
                vencimiento = "2026-11-25",
                estado = estadoSegunFecha("2026-11-25"),
                recargo = 1100
            ),
            Compromiso(
                folio = "PF-COND-001",
                empresa = "Condominio",
                usuario = usuario,
                concepto = "Gasto común",
                monto = 45000,
                vencimiento = "2026-12-10",
                estado = estadoSegunFecha("2026-12-10"),
                recargo = 3000
            ),
            Compromiso(
                folio = "PF-AGUA-001",
                empresa = "Aguas Andinas",
                usuario = usuario,
                concepto = "Agua",
                monto = 35000,
                vencimiento = "2026-12-20",
                estado = estadoSegunFecha("2026-12-20"),
                recargo = 2000
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
            onVerCompromisos = {},
            onAgregarServicio = {},
            onEliminarServicios = {},
            onVerHistorial = {},
            onReportarProblema = {},
            onVerHistorialReportes = {}
        )
    }
}
