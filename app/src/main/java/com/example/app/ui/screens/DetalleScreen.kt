package com.example.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.app.R
import com.example.app.model.AplazamientoCuota
import com.example.app.model.Compromiso
import com.example.app.model.Usuario
import com.example.app.ui.components.DialogoReporte
import com.example.app.ui.components.BotonPrincipal
import com.example.app.ui.components.BotonSecundario
import com.example.app.ui.components.CampoTexto
import com.example.app.ui.components.TarjetaSeccion
import com.example.app.ui.theme.AppThemePreview
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleScreen(
    compromiso: Compromiso,
    onVolver: () -> Unit,
    onPagar: (String) -> Boolean,
    onReportar: (String) -> Boolean,
    saldoPlan: Int? = null,
    montoPagado: Int? = null,
    onVerComprobante: (() -> Unit)? = null,
    onCrearCuotas: ((Int) -> Boolean)? = null,
    onAplazarCuota: (() -> Boolean)? = null
) {
    var mostrarConfirmacion by remember { mutableStateOf(false) }
    var mostrarReporte by remember { mutableStateOf(false) }
    var mostrarCuotas by remember { mutableStateOf(false) }
    var mostrarAplazamiento by remember { mutableStateOf(false) }
    var cantidadCuotas by remember { mutableIntStateOf(2) }
    var errorCuotas by remember { mutableStateOf<String?>(null) }

    var reporteEnviado by remember { mutableStateOf(false) }
    var errorPago by remember { mutableStateOf<String?>(null) }

    val vencido = compromiso.estado == "Vencido"
    val puedePagar = compromiso.estado == "Pendiente" || vencido

    val total = if (compromiso.estado == "Pagado" && montoPagado != null) {
        montoPagado
    } else {
        compromiso.monto + if (vencido) compromiso.recargo else 0
    }

    val formatoMonto = NumberFormat.getIntegerInstance(
        Locale.forLanguageTag("es-CL")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Detalle del compromiso",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clickable(
                                interactionSource = remember {
                                    MutableInteractionSource()
                                },
                                indication = null,
                                role = Role.Button,
                                onClick = onVolver
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { espacio ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(espacio)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 500.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = compromiso.empresa,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )

                Text(
                    text = compromiso.concepto,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                TarjetaSeccion(
                    titulo = "Datos del compromiso"
                ) {
                    FilaDetalleCompromiso(
                        etiqueta = "Folio",
                        valor = compromiso.folio
                    )

                    FilaDetalleCompromiso(
                        etiqueta = "Concepto",
                        valor = compromiso.concepto
                    )

                    if (compromiso.numeroCuota.isNotBlank()) {
                        FilaDetalleCompromiso(
                            etiqueta = "Cuota",
                            valor = compromiso.numeroCuota
                        )
                    }

                    FilaDetalleCompromiso(
                        etiqueta = "Vencimiento",
                        valor = fechaVisible(compromiso.vencimiento)
                    )

                    FilaDetalleCompromiso(
                        etiqueta = "Estado",
                        valor = compromiso.estado
                    )
                }

                TarjetaSeccion(
                    titulo = "Detalle del monto"
                ) {
                    FilaDetalleCompromiso(
                        etiqueta = "Monto",
                        valor = "$" + formatoMonto.format(compromiso.monto)
                    )

                    if (vencido) {
                        FilaDetalleCompromiso(
                            etiqueta = "Recargo por atraso",
                            valor = "$" + formatoMonto.format(compromiso.recargo)
                        )
                    }

                    if (compromiso.estado == "Pagado" && montoPagado != null && montoPagado > compromiso.monto) {
                        FilaDetalleCompromiso(
                            etiqueta = "Recargo pagado",
                            valor = "$" + formatoMonto.format(montoPagado - compromiso.monto)
                        )
                    }

                    HorizontalDivider()

                    FilaDetalleCompromiso(
                        etiqueta = if (compromiso.estado == "Pagado") "Total pagado" else "Total",
                        valor = "$" + formatoMonto.format(total),
                        destacado = true
                    )
                }

                if (compromiso.estado == "Pagado" && onVerComprobante != null) {
                    BotonPrincipal(
                        texto = "Ver comprobante",
                        onClick = onVerComprobante,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (saldoPlan != null) {
                    Text(
                        text = "Saldo pendiente del plan: $" + formatoMonto.format(saldoPlan),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                if (puedePagar) {
                    if (onCrearCuotas != null) {
                        BotonSecundario(
                            texto = "Elegir cuotas",
                            onClick = { errorCuotas = null; mostrarCuotas = true },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    if (onAplazarCuota != null) {
                        BotonSecundario(
                            texto = "Aplazar cuota",
                            onClick = { errorCuotas = null; mostrarAplazamiento = true },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    BotonPrincipal(
                        texto = "Pagar $" + formatoMonto.format(total),
                        onClick = {
                            errorPago = null
                            mostrarConfirmacion = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    BotonSecundario(
                        texto = "Reportar problema",
                        onClick = {
                            mostrarReporte = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else if (
                    compromiso.estado == "En revisión" ||
                    compromiso.estado == "Anulado"
                ) {
                    Text(
                        text = "Este compromiso no se puede pagar mientras esté " +
                                compromiso.estado.lowercase() + ".",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                errorPago?.let { mensaje ->
                    Text(
                        text = mensaje,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }

    if (mostrarCuotas && onCrearCuotas != null) {
        AlertDialog(
            onDismissRequest = { mostrarCuotas = false },
            title = { Text("Elegir cuotas") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Total a dividir: $" + formatoMonto.format(total))
                    Text("Selecciona la cantidad de cuotas mensuales:")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(2, 3, 6, 12).forEach { cantidad ->
                            FilterChip(
                                selected = cantidadCuotas == cantidad,
                                onClick = { cantidadCuotas = cantidad; errorCuotas = null },
                                label = { Text(cantidad.toString()) }
                            )
                        }
                    }
                    val montoBase = total / cantidadCuotas
                    val resto = total % cantidadCuotas
                    Text("Cada cuota: desde $" + formatoMonto.format(montoBase))
                    if (resto > 0) Text("$resto cuota(s) tendrán $1 adicional por el redondeo.")
                    if (vencido) Text("El recargo está incluido y la primera cuota vencerá hoy.")
                    errorCuotas?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (onCrearCuotas(cantidadCuotas)) {
                        mostrarCuotas = false
                    } else {
                        errorCuotas = "No se pudo crear el plan. Revisa que el compromiso no tenga pagos ni reportes."
                    }
                }) { Text("Confirmar") }
            },
            dismissButton = { TextButton(onClick = { mostrarCuotas = false }) { Text("Cancelar") } }
        )
    }

    if (mostrarAplazamiento && onAplazarCuota != null) {
        val nuevoMonto = AplazamientoCuota.montoConRecargo(compromiso.monto)
        val recargoAplazamiento = nuevoMonto - compromiso.monto
        val fechaSiguiente = try {
            val formato = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { isLenient = false }
            val siguiente = AplazamientoCuota.mesSiguiente(compromiso.vencimiento)
            val fecha = formato.parse(siguiente)!!
            SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("es-CL")).format(fecha)
        } catch (_: Exception) { "No disponible" }

        AlertDialog(
            onDismissRequest = { mostrarAplazamiento = false },
            title = { Text("Aplazar cuota") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Monto actual: $" + formatoMonto.format(compromiso.monto))
                    Text("Recargo de prueba (2 %): $" + formatoMonto.format(recargoAplazamiento))
                    Text("Nuevo monto: $" + formatoMonto.format(nuevoMonto))
                    Text("Nuevo vencimiento: $fechaSiguiente")
                    Text("Las cuotas posteriores también se moverán un mes. Puedes solicitar esta opción una vez por cuota pendiente.")
                    errorCuotas?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (onAplazarCuota()) {
                        mostrarAplazamiento = false
                    } else {
                        errorCuotas = "No se pudo aplazar. Revisa que esta cuota no se haya aplazado antes y que las posteriores estén pendientes y sin reportes."
                    }
                }) { Text("Confirmar") }
            },
            dismissButton = { TextButton(onClick = { mostrarAplazamiento = false }) { Text("Cancelar") } }
        )
    }

    if (mostrarConfirmacion) {
        AlertDialog(
            onDismissRequest = {
                mostrarConfirmacion = false
            },
            title = {
                Text(text = "Confirmar pago")
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Pagarás el total de $" +
                                formatoMonto.format(total) +
                                " a ${compromiso.empresa}."
                    )

                    Text(
                        text = "Medio de pago: Tarjeta de débito"
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val realizado = onPagar("Tarjeta de débito")

                        mostrarConfirmacion = false

                        errorPago = if (realizado) {
                            null
                        } else {
                            "No se pudo completar el pago. Verifica si existe " +
                                    "un compromiso anterior pendiente de esta empresa."
                        }
                    }
                ) {
                    Text(text = "Confirmar pago")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        mostrarConfirmacion = false
                    }
                ) {
                    Text(text = "Cancelar")
                }
            }
        )
    }

    if (mostrarReporte) {
        DialogoReporte(
            compromiso = compromiso,
            onEnviar = onReportar,
            onCerrar = { mostrarReporte = false },
            onExito = {
                mostrarReporte = false
                reporteEnviado = true
            }
        )
    }

    if (reporteEnviado) {
        AlertDialog(
            onDismissRequest = { reporteEnviado = false; onVolver() },
            title = { Text("Reporte enviado") },
            text = { Text("El compromiso quedó en revisión y no se puede pagar mientras revisamos el caso.") },
            confirmButton = {
                TextButton(onClick = { reporteEnviado = false; onVolver() }) {
                    Text("Listo")
                }
            }
        )
    }
}

@Composable
private fun FilaDetalleCompromiso(
    etiqueta: String,
    valor: String,
    destacado: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = etiqueta,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = valor,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (destacado) {
                FontWeight.Bold
            } else {
                FontWeight.Normal
            },
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End
        )
    }
}

private fun fechaVisible(fecha: String): String {
    return try {
        val fechaConvertida = try {
            SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(fecha)
        } catch (_: Exception) {
            SimpleDateFormat("dd-MM-yyyy", Locale.US).parse(fecha)
        }

        fechaConvertida?.let {
            SimpleDateFormat("dd-MM-yyyy", Locale.forLanguageTag("es-CL"))
                .format(it)
        } ?: fecha
    } catch (_: Exception) {
        fecha
    }
}

@Preview(
    name = "Detalle pendiente",
    showBackground = true,
    showSystemUi = true
)
@Composable
private fun DetallePendientePreview() {
    AppThemePreview {
        val usuario = Usuario(
            id = 1,
            rut = "20.435.214-5",
            nombre = "Sofía Pérez",
            correo = "sofia@gmail.com",
            rolId = 1,
            rolNombre = "CLIENTE"
        )

        val compromiso = Compromiso(
            folio = "PF-001",
            empresa = "Universidad",
            usuario = usuario,
            concepto = "Cuota mensual",
            monto = 350000,
            vencimiento = "2026-10-15",
            estado = "Pendiente"
        )

        DetalleScreen(
            compromiso = compromiso,
            onVolver = {},
            onPagar = { true },
            onReportar = { true }
        )
    }
}