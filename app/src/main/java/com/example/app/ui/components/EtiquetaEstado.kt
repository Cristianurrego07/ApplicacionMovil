package com.example.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.app.ui.theme.AppTheme

@Composable
fun EtiquetaEstado(
    estado: String,
    modifier: Modifier = Modifier
) {
    val colorFondo = when (estado) {
        "Pendiente" ->
            MaterialTheme.colorScheme.secondaryContainer

        "Vencido" ->
            MaterialTheme.colorScheme.errorContainer

        "Pagado" ->
            MaterialTheme.colorScheme.primaryContainer

        else ->
            MaterialTheme.colorScheme.surfaceVariant
    }

    val colorTexto = when (estado) {
        "Pendiente" ->
            MaterialTheme.colorScheme.onSecondaryContainer

        "Vencido" ->
            MaterialTheme.colorScheme.onErrorContainer

        "Pagado" ->
            MaterialTheme.colorScheme.onPrimaryContainer

        else ->
            MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        modifier = modifier,
        color = colorFondo,
        contentColor = colorTexto,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = estado,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(
                horizontal = 10.dp,
                vertical = 4.dp
            )
        )
    }
}

@Preview(
    name = "Estados",
    showBackground = true
)
@Composable
private fun EtiquetaEstadoPreview() {
    AppTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            EtiquetaEstado(estado = "Pendiente")
            EtiquetaEstado(estado = "Vencido")
            EtiquetaEstado(estado = "Pagado")
        }
    }
}