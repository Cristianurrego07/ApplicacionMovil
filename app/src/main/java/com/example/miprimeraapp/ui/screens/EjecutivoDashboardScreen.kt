package com.example.miprimeraapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
    var filtroSeleccionado by remember { mutableStateOf("Todas") }

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.cargarCobranzas()
    }

    val totalMonto = cobranzas.sumOf { it.monto }
    val enCursoCount = cobranzas.count { it.estado.equals("En curso", ignoreCase = true) }
    val pagadasCount = cobranzas.count { it.estado.equals("Pagada", ignoreCase = true) }

    val cobranzasFiltradas = cobranzas.filter { cobranza ->
        when (filtroSeleccionado) {
            "En curso" -> cobranza.estado.equals("En curso", ignoreCase = true)
            "Pagada" -> cobranza.estado.equals("Pagada", ignoreCase = true)
            "Anulada" -> cobranza.estado.equals("Anulada", ignoreCase = true)
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
                    text = "Menú Ejecutivo",
                    style = MaterialTheme.typography.titleLarge,
                    color = PagoFlexPurple,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                NavigationDrawerItem(
                    label = { Text("Panel Principal") },
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
                    title = {
                        Column {
                            Text("Panel Ejecutivo", style = MaterialTheme.typography.titleLarge)
                            Text(
                                text = usuario?.nombre ?: "Ejecutivo PagoFlex",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
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
                    onClick = { mostrarDialogo = true },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Nueva Cobranza") },
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
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Tarjetas KPI de resumen
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    KpiCard(
                        titulo = "Total Cobranzas",
                        valor = "${cobranzas.size}",
                        icono = Icons.Default.BusinessCenter,
                        modifier = Modifier.weight(1f)
                    )
                    KpiCard(
                        titulo = "En Curso",
                        valor = "$enCursoCount",
                        icono = Icons.Default.PendingActions,
                        modifier = Modifier.weight(1f)
                    )
                    KpiCard(
                        titulo = "Pagadas",
                        valor = "$pagadasCount",
                        icono = Icons.Default.CheckCircle,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Total Recaudado Banner
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Monto Total en Seguimiento",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$ %,.0f".format(totalMonto).replace(',', '.'),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = PagoFlexPurple
                            )
                        }
                        Button(
                            onClick = { mostrarDialogo = true },
                            colors = ButtonDefaults.buttonColors(containerColor = PagoFlexPurple.copy(alpha = 0.15f))
                        ) {
                            Text("+ Registrar", color = PagoFlexPurple)
                        }
                    }
                }

                // Chips de filtro
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val opciones = listOf("Todas", "En curso", "Pagada", "Anulada")
                    items(opciones) { opcion ->
                        FilterChip(
                            selected = (filtroSeleccionado == opcion),
                            onClick = { filtroSeleccionado = opcion },
                            label = { Text(opcion) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PagoFlexPurple,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }

                Text(
                    text = "Gestión de Cobranzas Empresariales",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )

                if (cobranzasFiltradas.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No hay cobranzas registradas en esta categoría",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(cobranzasFiltradas) { cobranza ->
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
fun KpiCard(
    titulo: String,
    valor: String,
    icono: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = PagoFlexPurple,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = valor,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = titulo,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun CobranzaItemCard(
    cobranza: CobranzaEmpresa,
    onAnular: () -> Unit
) {
    val esPagada = cobranza.estado.equals("Pagada", ignoreCase = true)
    val esAnulada = cobranza.estado.equals("Anulada", ignoreCase = true)

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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = cobranza.empresa,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Código: ${cobranza.codigo}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = when {
                        esPagada -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                        esAnulada -> MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                        else -> PagoFlexPurple.copy(alpha = 0.2f)
                    }
                ) {
                    Text(
                        text = cobranza.estado,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = when {
                            esPagada -> MaterialTheme.colorScheme.secondary
                            esAnulada -> MaterialTheme.colorScheme.error
                            else -> PagoFlexPurple
                        }
                    )
                }
            }

            if (cobranza.descripcion.isNotBlank()) {
                Text(
                    text = cobranza.descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Vencimiento",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = cobranza.vencimiento,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                Text(
                    text = "$ %,.0f".format(cobranza.monto).replace(',', '.'),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = PagoFlexPurple
                )
            }

            if (!esAnulada && !esPagada) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onAnular) {
                        Icon(
                            imageVector = Icons.Default.Cancel,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(4.dp))
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
    var codigo by remember { mutableStateOf("") }
    var empresa by remember { mutableStateOf("") }
    var montoStr by remember { mutableStateOf("") }
    var vencimiento by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }

    var errorMensaje by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Registrar Nueva Cobranza") },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .imePadding(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Ingresa los datos correspondientes para generar un nuevo registro de cobranza.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = codigo,
                    onValueChange = { codigo = it },
                    label = { Text("Código de Cobranza") },
                    placeholder = { Text("Ej: C-00130") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = empresa,
                    onValueChange = { empresa = it },
                    label = { Text("Empresa o Cliente") },
                    placeholder = { Text("Ej: Empresa Partner S.A.") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = montoStr,
                    onValueChange = { montoStr = it },
                    label = { Text("Monto ($)") },
                    placeholder = { Text("Ej: 55000") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = vencimiento,
                    onValueChange = { vencimiento = it },
                    label = { Text("Fecha Vencimiento") },
                    placeholder = { Text("Ej: 30 jul. 2025") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción") },
                    placeholder = { Text("Ej: Servicio corporativo mensual") },
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMensaje != null) {
                    Text(
                        text = errorMensaje!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val monto = montoStr.toDoubleOrNull()
                    if (codigo.isBlank() || empresa.isBlank() || monto == null || vencimiento.isBlank()) {
                        errorMensaje = "Por favor completa el código, empresa, fecha y un monto válido."
                    } else {
                        onGuardar(codigo, empresa, monto, vencimiento, descripcion)
                    }
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
