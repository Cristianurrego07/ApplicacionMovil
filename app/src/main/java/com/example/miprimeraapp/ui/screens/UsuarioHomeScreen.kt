package cl.duocuc.miregistro.ui.screens

import androidx.compose.foundation.clickable
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
import cl.duocuc.miregistro.R
import cl.duocuc.miregistro.model.Compromiso
import cl.duocuc.miregistro.model.Usuario
import cl.duocuc.miregistro.ui.theme.PagoFlexPurple
import cl.duocuc.miregistro.viewmodel.PagoFlexViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsuarioHomeScreen(
    usuario: Usuario?,
    onNavigateDetalle: (Long) -> Unit,
    onNavigateHistorial: () -> Unit,
    onNavigateProgreso: () -> Unit,
    onNavigatePerfil: () -> Unit,
    onCerrarSesion: () -> Unit,
    viewModel: PagoFlexViewModel = viewModel()
) {
    val usuarioId = usuario?.id ?: 1L
    val compromisos by viewModel.compromisos.collectAsStateWithLifecycle()

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    LaunchedEffect(usuarioId) {
        viewModel.cargarCompromisos(usuarioId)
    }

    val totalPendiente = compromisos
        .filter { it.estado.equals("Pendiente", ignoreCase = true) }
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
                    label = { Text("Historial de Pagos") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigateHistorial()
                    },
                    icon = { Icon(painterResource(R.drawable.ic_buscar), contentDescription = null) }
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
                                contentDescription = "Menú de navegación (3 líneas)"
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
                // Total Pending Card
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

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Próximos Compromisos",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(12.dp))

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
