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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.app.model.Compromiso
import com.example.app.model.Usuario
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
    onReportar: (String) -> Boolean
) {
    var mostrarConfirmacion by remember { mutableStateOf(false) }
    var mostrarReporte by remember { mutableStateOf(false) }

    var motivo by remember { mutableStateOf("") }
    var errorReporte by remember { mutableStateOf<String?>(null) }
    var errorPago by remember { mutableStateOf<String?>(null) }

    val vencido = compromiso.estado == "Vencido"
    val puedePagar = compromiso.estado == "Pendiente" || vencido

    val total = compromiso.monto +
            if (vencido) compromiso.recargo else 0

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

                    HorizontalDivider()

                    FilaDetalleCompromiso(
                        etiqueta = "Total",
                        valor = "$" + formatoMonto.format(total),
                        destacado = true
                    )
                }

                if (puedePagar) {
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
                            errorReporte = null
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
        AlertDialog(
            onDismissRequest = {
                mostrarReporte = false
            },
            title = {
                Text(text = "Reportar problema")
            },
            text = {
                Column {
                    CampoTexto(
                        valor = motivo,
                        etiqueta = "Motivo",
                        alCambiar = {
                            motivo = it
                            errorReporte = null
                        },
                        error = errorReporte
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (motivo.isBlank()) {
                            errorReporte = "Describe el motivo del reporte."
                        } else if (onReportar(motivo.trim())) {
                            mostrarReporte = false
                            errorReporte = null
                        } else {
                            errorReporte =
                                "No se pudo enviar el reporte. Puede existir " +
                                        "un reporte anterior para este compromiso."
                        }
                    }
                ) {
                    Text(text = "Enviar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        mostrarReporte = false
                    }
                ) {
                    Text(text = "Cancelar")
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