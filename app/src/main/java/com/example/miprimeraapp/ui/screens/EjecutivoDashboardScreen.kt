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
import com.example.miprimeraapp.model.CobranzaEmpresa
import com.example.miprimeraapp.model.Usuario
import com.example.miprimeraapp.ui.theme.PagoFlexPurple
import com.example.miprimeraapp.viewmodel.PagoFlexViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EjecutivoDashboardScreen(
    usuario: Usuario?,
    onNavigatePerfil: () -> Unit,
    onCerrarSesion: () -> Unit,
    viewModel: PagoFlexViewModel = viewModel()
) {
    val cobranzas by viewModel.cobranzas.collectAsStateWithLifecycle()
    var mostrarDialogo by remember { mutableStateOf(false) }

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.cargarCobranzas()
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface
            ) {
                Spacer(Modifier.height(24.dp))
                Text(
                    text = "Menú Ejecutivo",
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
                    title = { Text("Panel Ejecutivo: ${usuario?.nombre ?: "Daniela Ejecutivo"}") },
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
                FloatingActionButton(
                    onClick = { mostrarDialogo = true },
                    containerColor = PagoFlexPurple,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Text("+", style = MaterialTheme.typography.headlineMedium)
                }
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Seguimiento de Cobranzas y Compromisos (RF-17 a RF-20)",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (cobranzas.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No hay cobranzas registradas", color = MaterialTheme.colorScheme.onBackground)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(cobranzas) { cobranza ->
                            CobranzaItemCard(
                                cobranza = cobranza,
                                onAnular = { viewModel.anularCobranza(cobranza.id) }
                            )
                        }
                    }
                }
            }

            if (mostrarDialogo) {
                RegistrarCobranzaDialog(
                    onDismiss = { mostrarDialogo = false },
                    onGuardar = { codigo, empresa, monto, vencimiento, descripcion ->
                        viewModel.registrarCobranza(codigo, empresa, monto, vencimiento, descripcion)
                        mostrarDialogo = false
                    }
                )
            }
        }
    }
}

@Composable
fun CobranzaItemCard(
    cobranza: CobranzaEmpresa,
    onAnular: () -> Unit
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
                    text = "${cobranza.codigo} - ${cobranza.empresa}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = cobranza.estado,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (cobranza.estado.equals("Pagada", ignoreCase = true))
                        MaterialTheme.colorScheme.secondary
                    else if (cobranza.estado.equals("Anulada", ignoreCase = true))
                        MaterialTheme.colorScheme.error
                    else
                        PagoFlexPurple
                )
            }
            Text(
                text = cobranza.descripcion,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Vence: ${cobranza.vencimiento}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = "$ %,.0f".format(cobranza.monto).replace(',', '.'),
                    style = MaterialTheme.typography.titleMedium,
                    color = PagoFlexPurple
                )
            }

            if (!cobranza.estado.equals("Anulada", ignoreCase = true) && !cobranza.estado.equals("Pagada", ignoreCase = true)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onAnular) {
                        Text("Anular Cobranza", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrarCobranzaDialog(
    onDismiss: () -> Unit,
    onGuardar: (String, String, Double, String, String) -> Unit
) {
    var codigo by remember { mutableStateOf("N° C-00130") }
    var empresa by remember { mutableStateOf("Empresa Partner") }
    var montoStr by remember { mutableStateOf("50000") }
    var vencimiento by remember { mutableStateOf("30 jun. 2025") }
    var descripcion by remember { mutableStateOf("Servicio corporativo") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Registrar Nueva Cobranza") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(value = codigo, onValueChange = { codigo = it }, label = { Text("Código") })
                OutlinedTextField(value = empresa, onValueChange = { empresa = it }, label = { Text("Empresa") })
                OutlinedTextField(value = montoStr, onValueChange = { montoStr = it }, label = { Text("Monto") })
                OutlinedTextField(value = vencimiento, onValueChange = { vencimiento = it }, label = { Text("Vencimiento") })
                OutlinedTextField(value = descripcion, onValueChange = { descripcion = it }, label = { Text("Descripción") })
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val monto = montoStr.toDoubleOrNull() ?: 0.0
                    onGuardar(codigo, empresa, monto, vencimiento, descripcion)
                },
                colors = ButtonDefaults.buttonColors(containerColor = PagoFlexPurple)
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
