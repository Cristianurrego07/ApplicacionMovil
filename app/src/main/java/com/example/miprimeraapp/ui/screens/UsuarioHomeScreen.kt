package com.example.miprimeraapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.miprimeraapp.R
import com.example.miprimeraapp.model.Compromiso
import com.example.miprimeraapp.model.Servicio
import com.example.miprimeraapp.model.Usuario
import com.example.miprimeraapp.ui.theme.PagoFlexPurple
import com.example.miprimeraapp.viewmodel.PagoFlexViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsuarioHomeScreen(
    usuario: Usuario?,
    onNavigateDetalle: (Long) -> Unit,
    onNavigateAgregarServicio: () -> Unit = {},
    onNavigateReportarProblema: () -> Unit = {},
    onNavigateHistorial: () -> Unit,
    onNavigateProgreso: () -> Unit,
    onNavigatePerfil: () -> Unit,
    onCerrarSesion: () -> Unit,
    viewModel: PagoFlexViewModel = viewModel()
) {
    val usuarioId = usuario?.id ?: 1L
    val compromisos by viewModel.compromisos.collectAsStateWithLifecycle()
    val servicios by viewModel.servicios.collectAsStateWithLifecycle()

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    LaunchedEffect(usuarioId) {
        viewModel.cargarCompromisos(usuarioId)
        viewModel.cargarServicios(usuarioId)
    }

    val totalPendiente = compromisos
        .filter { it.estado.equals("Pendiente", ignoreCase = true) || it.estado.equals("Vencido", ignoreCase = true) }
        .sumOf { it.monto }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface
            ) {
                Spacer(Modifier.height(24.dp))
                Text(
                    text = "PagoFlex Menú",
                    style = MaterialTheme.typography.titleLarge,
                    color = PagoFlexPurple,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                NavigationDrawerItem(
                    label = { Text("Inicio") },
                    selected = true,
                    onClick = { scope.launch { drawerState.close() } },
                    icon = { Icon(painterResource(R.drawable.ic_check), contentDescription = null) },
                    colors = NavigationDrawerItemDefaults.colors(selectedContainerColor = PagoFlexPurple.copy(alpha = 0.2f))
                )
                NavigationDrawerItem(
                    label = { Text("Inscribir Servicio") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigateAgregarServicio()
                    },
                    icon = { Icon(painterResource(R.drawable.ic_buscar), contentDescription = null) }
                )
                NavigationDrawerItem(
                    label = { Text("Historial de Pagos") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigateHistorial()
                    },
                    icon = { Icon(painterResource(R.drawable.ic_buscar), contentDescription = null) }
                )
                NavigationDrawerItem(
                    label = { Text("Reportar Problema") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigateReportarProblema()
                    },
                    icon = { Icon(painterResource(R.drawable.ic_candado), contentDescription = null) }
                )
                NavigationDrawerItem(
                    label = { Text("Mi Progreso") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigateProgreso()
                    },
                    icon = { Icon(painterResource(R.drawable.ic_seguir), contentDescription = null) }
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
                    title = { Text("Hola, ${usuario?.nombre ?: "Usuario"}") },
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
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = onNavigateAgregarServicio,
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Inscribir Servicio") },
                    containerColor = PagoFlexPurple,
                    contentColor = MaterialTheme.colorScheme.onPrimary
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
                // Tarjeta Resumen Total
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Total Pendiente",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "$ %,.0f".format(totalPendiente).replace(',', '.'),
                            style = MaterialTheme.typography.headlineLarge,
                            color = PagoFlexPurple
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Sección: Mis Servicios Inscritos
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Mis Servicios Inscritos",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    TextButton(onClick = onNavigateAgregarServicio) {
                        Text("+ Inscribir", color = PagoFlexPurple)
                    }
                }

                if (servicios.isEmpty()) {
                    Text(
                        text = "Aún no has inscrito servicios. Presiona '+ Inscribir' para agregar tus cuentas.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        items(servicios) { servicio ->
                            FilterChip(
                                selected = true,
                                onClick = { },
                                label = { Text("${servicio.alias.ifBlank { servicio.nombreEmpresa }} (${servicio.nombreEmpresa})") },
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_check),
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PagoFlexPurple.copy(alpha = 0.15f),
                                    selectedLabelColor = PagoFlexPurple
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Sección: Próximos Compromisos
                Text(
                    text = "Próximos Compromisos por Pagar",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(compromisos) { compromiso ->
                        CompromisoItemCard(
                            compromiso = compromiso,
                            onClick = { onNavigateDetalle(compromiso.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CompromisoItemCard(
    compromiso: Compromiso,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = compromiso.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = compromiso.subtitulo,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Vence: ${compromiso.fechaVencimiento}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$ %,.0f".format(compromiso.monto).replace(',', '.'),
                    style = MaterialTheme.typography.titleMedium,
                    color = PagoFlexPurple
                )
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = if (compromiso.estado.equals("Pagada", ignoreCase = true))
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                    else
                        MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = compromiso.estado,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (compromiso.estado.equals("Pagada", ignoreCase = true))
                            MaterialTheme.colorScheme.secondary
                        else
                            MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
