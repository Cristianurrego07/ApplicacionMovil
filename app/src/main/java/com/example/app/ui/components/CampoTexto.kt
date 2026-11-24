package com.example.app.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.app.ui.styles.EstilosCampo
import com.example.app.ui.theme.AppTheme

@Composable
fun CampoTexto(
    valor: String,
    etiqueta: String,
    alCambiar: (String) -> Unit,
    error: String? = null,
    modifier: Modifier = Modifier,
    imeAction: ImeAction = ImeAction.Next,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    OutlinedTextField(
        value = valor,
        onValueChange = alCambiar,
        modifier = modifier.fillMaxWidth(),

        label = {
            Text(text = etiqueta)
        },

        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = imeAction),
        keyboardActions = keyboardActions,
        shape = EstilosCampo.forma,
        colors = EstilosCampo.colores(),

        isError = error != null,
        supportingText = {
            error?.let { mensaje ->
                Text(text = mensaje)
            }
        }
    )
}

@Preview(
    name = "Campo de texto",
    showBackground = true
)
@Composable
private fun CampoTextoPreview() {
    AppTheme {
        CampoTexto(
            valor = "Sofía Pérez",
            etiqueta = "Nombre completo",
            alCambiar = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(
    name = "Campo de texto con error",
    showBackground = true
)
@Composable
private fun CampoTextoErrorPreview() {
    AppTheme {
        CampoTexto(
            valor = "",
            etiqueta = "Nombre completo",
            alCambiar = {},
            error = "El nombre es obligatorio",
            modifier = Modifier.padding(16.dp)
        )
    }
}