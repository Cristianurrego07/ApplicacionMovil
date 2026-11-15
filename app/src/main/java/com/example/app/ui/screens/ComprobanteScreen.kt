package com.example.app.ui.screens

import android.content.res.Configuration
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.app.R
import com.example.app.model.Comprobante
import com.example.app.ui.components.BotonPrincipal
import com.example.app.ui.components.TarjetaSeccion
import com.example.app.ui.theme.AppThemePreview
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComprobanteScreen(
    comprobante: Comprobante?,
    onVolver: () -> Unit
) {
    val formatoMonto = NumberFormat.getIntegerInstance(
        Locale.forLanguageTag("es-CL")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Comprobante de pago",
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
                if (comprobante == null) {
                    Text(
                        text = "No se encontró el comprobante.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                } else {
                    Text(
                        text = "¡Pago realizado!",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "Aquí puedes consultar los datos de tu pago.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    TarjetaSeccion(
                        titulo = "Detalle del compromiso"
                    ) {
                        FilaComprobante(
                            etiqueta = "Empresa",
                            valor = comprobante.empresa
                        )

                        FilaComprobante(
                            etiqueta = "Compromiso",
                            valor = comprobante.folioCompromiso
                        )

                        FilaComprobante(
                            etiqueta = "Concepto",
                            valor = comprobante.concepto
                        )

                        HorizontalDivider()

                        FilaComprobante(
                            etiqueta = "Total pagado",
                            valor = "$" + formatoMonto.format(
                                comprobante.montoPagado
                            ),
                            destacado = true
                        )
                    }

                    TarjetaSeccion(
                        titulo = "Datos del comprobante"
                    ) {
                        FilaComprobante(
                            etiqueta = "Número",
                            valor = comprobante.numero
                        )

                        FilaComprobante(
                            etiqueta = "Fecha",
                            valor = comprobante.fechaHora
                        )

                        FilaComprobante(
                            etiqueta = "Canal",
                            valor = comprobante.canal
                        )

                        FilaComprobante(
                            etiqueta = "Medio de pago",
                            valor = comprobante.medioPagoVisible
                        )
                    }

                    Text(
                        text = "Guarda el número de comprobante para consultar tu pago.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                BotonPrincipal(
                    texto = "Volver",
                    onClick = onVolver,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun FilaComprobante(
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

@Preview(
    name = "Comprobante claro",
    showBackground = true,
    showSystemUi = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "Comprobante oscuro",
    showBackground = true,
    showSystemUi = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun ComprobantePreview() {
    AppThemePreview {
        ComprobanteScreen(
            comprobante = Comprobante(
                numero = "CPR-000001",
                folioCompromiso = "PF-001",
                empresa = "Universidad",
                concepto = "Cuota mensual",
                montoPagado = 350000,
                fechaHora = "2026-10-15 10:30",
                canal = "App",
                medioPago = "Tarjeta de débito"
            ),
            onVolver = {}
        )
    }
}

@Preview(
    name = "Comprobante no encontrado",
    showBackground = true,
    showSystemUi = true
)
@Composable
private fun ComprobanteNoEncontradoPreview() {
    AppThemePreview {
        ComprobanteScreen(
            comprobante = null,
            onVolver = {}
        )
    }
}