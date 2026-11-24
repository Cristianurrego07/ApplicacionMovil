package com.example.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.app.model.Compromiso
import com.example.app.model.Usuario
import com.example.app.ui.styles.EstilosTarjeta
import com.example.app.ui.theme.AppTheme
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun TarjetaCompromiso(
    compromiso: Compromiso,
    alPresionar: () -> Unit = {},
    recordatorio: @Composable () -> Unit = {}
) {
    val vencido = compromiso.estado == "Vencido"

    val total = compromiso.monto +
            if (vencido) compromiso.recargo else 0

    Card(
        onClick = alPresionar,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 150.dp),
        shape = EstilosTarjeta.forma,
        colors = EstilosTarjeta.colores(),
        elevation = EstilosTarjeta.elevacion()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = compromiso.empresa,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.secondary
                )
                recordatorio()
            }

            Text(
                text = compromiso.concepto,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (compromiso.numeroCuota.isNotBlank()) {
                Text(
                    text = "Cuota ${compromiso.numeroCuota}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = formatearMonto(total),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (vencido) {
                Text(
                    text = "Incluye recargo de ${formatearMonto(compromiso.recargo)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "Vence ${fechaBreve(compromiso.vencimiento)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )

            EtiquetaEstado(
                estado = compromiso.estado
            )
        }
    }
}

private fun formatearMonto(monto: Int): String {
    val formato = NumberFormat.getIntegerInstance(
        Locale.forLanguageTag("es-CL")
    )

    return "$" + formato.format(monto)
}

private fun fechaBreve(fecha: String): String {
    return try {
        val fechaConvertida = try {
            SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(fecha)
        } catch (_: Exception) {
            SimpleDateFormat("dd-MM-yyyy", Locale.US).parse(fecha)
        }

        fechaConvertida?.let {
            SimpleDateFormat(
                "dd-MM-yyyy",
                Locale.forLanguageTag("es-CL")
            ).format(it)
        } ?: fecha
    } catch (_: Exception) {
        fecha
    }
}

@Preview(
    name = "Compromiso pendiente",
    showBackground = true
)
@Composable
private fun TarjetaCompromisoPreview() {
    AppTheme {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            TarjetaCompromiso(
                recordatorio = {
                    RecordatorioCompromiso(
                        Compromiso(
                            folio = "12345",
                            empresa = "Enel",
                            usuario = Usuario(id = 1, rut = "12.345.678-9", nombre = "Juan Pérez", correo = "juan@correo.cl", rolId = 1),
                            concepto = "Luz", monto = 35000, vencimiento = "2027-05-20"
                        )
                    )
                },
                compromiso = Compromiso(
                    folio = "12345",
                    empresa = "Enel",
                    usuario = Usuario(
                        id = 1,
                        rut = "12.345.678-9",
                        nombre = "Juan Pérez",
                        correo = "juan@correo.cl",
                        rolId = 1
                    ),
                    concepto = "Luz",
                    monto = 35000,
                    vencimiento = "2025-05-20",
                    estado = "Pendiente"
                )
            )
        }
    }
}

@Preview(
    name = "Compromiso vencido",
    showBackground = true
)
@Composable
private fun TarjetaCompromisoVencidoPreview() {
    AppTheme {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            TarjetaCompromiso(
                compromiso = Compromiso(
                    folio = "67890",
                    empresa = "Aguas Andinas",
                    usuario = Usuario(
                        id = 1,
                        rut = "12.345.678-9",
                        nombre = "Juan Pérez",
                        correo = "juan@correo.cl",
                        rolId = 1
                    ),
                    concepto = "Agua",
                    monto = 18000,
                    vencimiento = "2025-04-10",
                    estado = "Vencido",
                    recargo = 2500
                )
            )
        }
    }
}