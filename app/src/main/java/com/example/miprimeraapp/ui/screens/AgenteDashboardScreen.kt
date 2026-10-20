package com.example.miprimeraapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.miprimeraapp.R
import com.example.miprimeraapp.model.ReporteProblema
import com.example.miprimeraapp.model.Usuario
import com.example.miprimeraapp.ui.theme.PagoFlexPurple
import com.example.miprimeraapp.viewmodel.PagoFlexViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgenteDashboardScreen(
    usuario: Usuario?,
    onNavigatePerfil: () -> Unit,
    onCerrarSesion: () -> Unit,
    viewModel: PagoFlexViewModel = viewModel()
) {
    val reportes by viewModel.reportes.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf(0) }

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.cargarReportes()
    }

    val tabs = listOf("Pendientes", "En proceso", "Resueltas")

    val reportesFiltrados = reportes.filter { reporte ->
        when (selectedTab) {
            0 -> reporte.estado.equals("Pendiente", ignoreCase = true) || reporte.estado.equals("Abierto", ignoreCase = true)
            1 -> reporte.estado.equals("En proceso", ignoreCase = true)
            2 -> reporte.estado.equals("Resuelto", ignoreCase = true) || reporte.estado.equals("Resuelta", ignoreCase = true)
            else -> true
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface
            ) {
                Spacer(Modifier.height(24.dp))
                Text(
                    text = "Menú Agente",
                    style = MaterialTheme.typography.titleLarge,
                    color = PagoFlexPurple,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                NavigationDrawerItem(
                    label = { Text("Inicio (Panel)") },
                    selected = true,
                    onClick = { scope.launch { drawerState.close() } },
                    icon = { Icon(painterResource(R.drawable.ic_check), contentDescription = null) },
                    colors = NavigationDrawerItemDefaults.colors(selectedContainerColor = PagoFlexPurple.copy(alpha = 0.2f))
                )
                NavigationDrawerItem(
                    label = { Text("Perfil y Configuración") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigatePerfil()
                    },
                    icon = { Icon(painterResource(R.drawable.ic_persona), contentDescription = null) }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                NavigationDrawerItem(
                    label = { Text("Cerrar Sesión") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onCerrarSesion()
                    },
                    icon = { Icon(painterResource(R.drawable.ic_eliminar), contentDescription = null, tint = MaterialTheme.colorScheme.error) }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Panel Agente: ${usuario?.nombre ?: "Carla Agente"}") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                painter = painterResource(R.drawable.ic_menu),
                                contentDescription = "Menú de navegación"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = PagoFlexPurple,
                    edgePadding = 0.dp
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (reportesFiltrados.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No hay reportes o recomendaciones en esta categoría", color = MaterialTheme.colorScheme.onBackground)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(reportesFiltrados) { reporte ->
                            ReporteItemCard(
                                reporte = reporte,
                                onCambiarEstado = { nuevoEstado ->
                                    viewModel.actualizarEstadoReporte(reporte.id, nuevoEstado)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReporteItemCard(
    reporte: ReporteProblema,
    onCambiarEstado: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${reporte.codigo} - ${reporte.tipo}",
                    style = MaterialTheme.typography.labelMedium,
                    color = PagoFlexPurple
                )
                Text(
                    text = reporte.estado,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            Text(
                text = reporte.titulo,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Cliente: ${reporte.cliente}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = reporte.descripcion,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!reporte.estado.equals("En proceso", ignoreCase = true)) {
                    TextButton(onClick = { onCambiarEstado("En proceso") }) {
                        Text("En proceso", color = PagoFlexPurple)
                    }
                }
                if (!reporte.estado.equals("Resuelto", ignoreCase = true) && !reporte.estado.equals("Resuelta", ignoreCase = true)) {
                    TextButton(onClick = { onCambiarEstado("Resuelto") }) {
                        Text("Resolver", color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }
        }
    }
}
