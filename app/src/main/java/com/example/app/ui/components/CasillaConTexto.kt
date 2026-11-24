package com.example.app.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.app.ui.theme.AppTheme

@Composable
fun CasillaConTexto(
    texto: String,
    marcada: Boolean,
    alCambiar: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = marcada,
            onCheckedChange = alCambiar,
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.primary,
                uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant,
                checkmarkColor = MaterialTheme.colorScheme.onPrimary
            )
        )

        Text(
            text = texto,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Preview(
    name = "Casilla marcada",
    showBackground = true
)
@Composable
private fun CasillaMarcadaPreview() {
    AppTheme {
        CasillaConTexto(
            texto = "Acepto los términos y condiciones",
            marcada = true,
            alCambiar = {}
        )
    }
}

@Preview(
    name = "Casilla desmarcada",
    showBackground = true
)
@Composable
private fun CasillaDesmarcadaPreview() {
    AppTheme {
        CasillaConTexto(
            texto = "Acepto los términos y condiciones",
            marcada = false,
            alCambiar = {}
        )
    }
}