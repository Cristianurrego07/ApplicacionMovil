package com.example.miprimeraapp.ui.screens

import androidx.compose.foundation.layout.*
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
import com.example.miprimeraapp.ui.theme.PagoFlexPurple
import com.example.miprimeraapp.viewmodel.PagoFlexViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleCompromisoScreen(
    compromisoId: Long,
    usuarioId: Long,
    onVolver: () -> Unit,
    viewModel: PagoFlexViewModel = viewModel()
) {
    val compromisos by viewModel.compromisos.collectAsStateWithLifecycle()

    LaunchedEffect(usuarioId) {
        if (compromisos.isEmpty()) {
            viewModel.cargarCompromisos(usuarioId)
        }
    }

    val compromiso = compromisos.find { it.id == compromisoId }
    var pagadoExitoso by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de Compromiso") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            painter = painterResource(R.drawable.ic_volver),
                            contentDescription = "Volver"
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
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            if (compromiso == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Compromiso no encontrado", color = MaterialTheme.colorScheme.onBackground)
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = compromiso.titulo,
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = compromiso.subtitulo,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Empresa:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(compromiso.empresa.ifBlank { "PagoFlex" }, color = MaterialTheme.colorScheme.onSurface)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Fecha Vencimiento:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(compromiso.fechaVencimiento, color = MaterialTheme.colorScheme.onSurface)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Estado:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = if (pagadoExitoso) "Pagada" else compromiso.estado,
                                    color = if ((pagadoExitoso || compromiso.estado.equals("Pagada", ignoreCase = true)))
                                        MaterialTheme.colorScheme.secondary
                                    else
                                        MaterialTheme.colorScheme.error
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Monto a Pagar:", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                                Text(
                                    text = "$ %,.0f".format(compromiso.monto).replace(',', '.'),
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = PagoFlexPurple
                                )
                            }
                        }
                    }

                    if (pagadoExitoso || compromiso.estado.equals("Pagada", ignoreCase = true)) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f))
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_check),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                                Text(
                                    text = "Este compromiso ya fue pagado exitosamente",
                                    color = MaterialTheme.colorScheme.secondary,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }

                if (!pagadoExitoso && !compromiso.estado.equals("Pagada", ignoreCase = true)) {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    Button(
                        onClick = {
                            viewModel.pagarCompromiso(compromiso.id, usuarioId)
                            pagadoExitoso = true

                            // Enviar notificación por correo de la venta / pago
                            com.example.miprimeraapp.data.CorreoNotificador.enviarCorreo(
                                context = context,
                                destinatario = "cliente@pagoflex.cl",
                                asunto = "Comprobante de Venta / Pago - PagoFlex",
                                cuerpo = "Estimado/a cliente,\n\nTu pago para '${compromiso.titulo}' (${compromiso.empresa}) por el monto de $ %,.0f fue procesado exitosamente.\n\nFolio: ${compromiso.folio}\nGracias por preferir PagoFlex.\n\nAtentamente,\nEquipo PagoFlex".format(compromiso.monto)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PagoFlexPurple)
                    ) {
                        Text("Pagar ahora", style = MaterialTheme.typography.titleMedium)
                    }
                } else {
                    Button(
                        onClick = onVolver,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Text("Volver al Inicio", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
