package com.example.app.ui.screens

import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.app.R
import com.example.app.model.Compromiso
import com.example.app.model.Usuario
import com.example.app.ui.components.RecordatorioCompromiso
import com.example.app.ui.components.TarjetaCompromiso
import com.example.app.ui.theme.AppThemePreview
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EjecutivoScreen(
    usuario: Usuario?,
    compromisos: List<Compromiso>,
    montosPagados: Map<String, Int> = emptyMap(),
    onCerrarSesion: () -> Unit,
    onVerDetalle: (String) -> Unit,
    onVerPerfil: () -> Unit = {},
    onRegistrarCompromiso: () -> Unit = {}
) {
    val mes = java.text.SimpleDateFormat("yyyy-MM", Locale.US).format(java.util.Date())
    var estado by remember { mutableStateOf("Todos") }
    val compromisosMes = compromisos.filter { it.vencimiento.startsWith(mes) }
    val pendientes = compromisosMes.filter { it.estado == "Pendiente" }
    val vencidos = compromisosMes.filter { it.estado == "Vencido" }
    val pagados = compromisosMes.filter { it.estado == "Pagado" }
    val totalPendiente = (pendientes + vencidos).sumOf {
        it.monto + if (it.estado == "Vencido") it.recargo else 0
    }
    val baseCobranza = compromisosMes.count { it.estado != "Anulado" }
    val tasa = if (baseCobranza == 0) 0.0 else pagados.size * 100.0 / baseCobranza
    val recaudado = pagados.sumOf { montosPagados[it.folio] ?: 0 }
    val visibles = if (estado == "Todos") compromisos else compromisos.filter { it.estado == estado }
    val formato = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-CL"))
    val menu = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = menu,
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
                        text = "Ejecutivo · ${usuario?.nombre ?: "Usuario"}",
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
                        label = { Text("Inicio") },
                        selected = false,
                        modifier = Modifier.padding(horizontal = 12.dp),
                        onClick = {
                            scope.launch { menu.close() }
                        }
                    )
                    NavigationDrawerItem(
                        label = { Text("Registrar compromiso") },
                        selected = false,
                        modifier = Modifier.padding(horizontal = 12.dp),
                        onClick = {
                            scope.launch { menu.close() }
                            onRegistrarCompromiso()
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
                            scope.launch { menu.close() }
                            onVerPerfil()
                        }
                    )
                    NavigationDrawerItem(
                        label = { Text("Cerrar sesión") },
                        selected = false,
                        modifier = Modifier.padding(horizontal = 12.dp),
                        onClick = {
                            scope.launch { menu.close() }
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
                    navigationIcon = {
                        Box(
                            modifier = Modifier.size(48.dp).clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                role = Role.Button,
                                onClickLabel = "Abrir menú",
                                onClick = { scope.launch { menu.open() } }
                            ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_menu),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    title = {
                        Text(
                            text = buildAnnotatedString {
                                withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) { append("Pago") }
                                withStyle(SpanStyle(color = MaterialTheme.colorScheme.secondary)) { append("Flex") }
                            },
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        ) { espacio ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(espacio).padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Hola, ${usuario?.nombre ?: "ejecutivo"}",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Aquí tienes un resumen de la cobranza",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("Total por cobrar del mes", style = MaterialTheme.typography.bodyLarge)
                            Text(formato.format(totalPendiente), style = MaterialTheme.typography.headlineLarge)
                            Text("Pendientes: ${pendientes.size} · Vencidos: ${vencidos.size} · Pagados: ${pagados.size}")
                            Text("Recaudado: ${formato.format(recaudado)}")
                            Text("Tasa de cobranza: ${String.format(Locale.forLanguageTag("es-CL"), "%.1f", tasa)}%")
                        }
                    }
                }
                item {
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Todos", "Pendiente", "Vencido", "Pagado", "En revisión", "Anulado").forEach { opcion ->
                            FilterChip(selected = estado == opcion, onClick = { estado = opcion }, label = { Text(opcion) })
                        }
                    }
                }
                item {
                    Text(
                        text = "Compromisos de la empresa:",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                if (visibles.isEmpty()) {
                    item { Text("No hay compromisos para este estado.") }
                } else {
                    items(visibles, key = { it.folio }) { compromiso ->
                        TarjetaCompromiso(
                            compromiso = compromiso,
                            alPresionar = { onVerDetalle(compromiso.folio) },
                            recordatorio = {
                                if (usuario != null && (compromiso.estado == "Pendiente" || compromiso.estado == "Vencido")) {
                                    RecordatorioCompromiso(compromiso, usuarioId = usuario.id)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Preview(name = "Inicio ejecutivo", showBackground = true, showSystemUi = true)
@Composable
private fun EjecutivoScreenPreview() {
    val ejecutivo = Usuario(
        id = 3, rut = "12.345.678-9", nombre = "Carla Ejecutivo",
        correo = "carla@duocuc.cl", rolId = 3, rolNombre = "EJECUTIVO"
    )
    val cliente = Usuario(
        id = 1, rut = "20.435.214-5", nombre = "Sofía Pérez",
        correo = "sofia@gmail.com", rolId = 1, rolNombre = "CLIENTE"
    )
    AppThemePreview {
        EjecutivoScreen(
            usuario = ejecutivo,
            compromisos = listOf(
                Compromiso("PF-001", "Duoc Uc", cliente, "Mensualidad julio", 356000, "2026-07-19", "Pagado"),
                Compromiso("PF-002", "Duoc Uc", cliente, "Mensualidad noviembre", 356000, "2026-11-19", "Vencido"),
                Compromiso("PF-003", "Duoc Uc", cliente, "Mensualidad diciembre", 356000, "2026-12-19", "Pendiente"),
                Compromiso("PF-004", "Duoc Uc", cliente, "Mensualidad enero", 356000, "2027-01-19", "Pendiente"),
                Compromiso("PF-005", "Duoc Uc", cliente, "Mensualidad febrero", 356000, "2027-02-19", "Pendiente")
            ),
            onCerrarSesion = {},
            onVerDetalle = {}
        )
    }
}
