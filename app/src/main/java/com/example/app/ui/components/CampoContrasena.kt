package com.example.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.app.R
import com.example.app.ui.styles.EstilosCampo
import com.example.app.ui.theme.AppTheme

@Composable
fun CampoContrasena(
    valor: String,
    alCambiar: (String) -> Unit,
    visible: Boolean,
    alAlternarVisible: () -> Unit,
    modifier: Modifier = Modifier,
    error: String? = null,
    etiqueta: String = "Contraseña",
    imeAction: ImeAction = ImeAction.Done,
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

        leadingIcon = {
            Icon(
                painter = painterResource(R.drawable.ic_candado),
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
        },

        visualTransformation = if (visible) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        trailingIcon = {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clickable(
                        interactionSource = remember {
                            MutableInteractionSource()
                        },
                        indication = null,
                        role = Role.Button,
                        onClick = alAlternarVisible
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(
                        if (visible) {
                            R.drawable.ic_ojo_tachado
                        } else {
                            R.drawable.ic_ojo
                        }
                    ),
                    contentDescription = if (visible) {
                        "Ocultar contraseña"
                    } else {
                        "Mostrar contraseña"
                    },
                    modifier = Modifier.size(20.dp)
                )
            }
        },

        shape = EstilosCampo.forma,
        colors = EstilosCampo.colores(),
        isError = error != null,
        supportingText = {
            error?.let { mensaje ->
                Text(text = mensaje)
            }
        },
        keyboardActions = keyboardActions,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = imeAction
        )
    )
}

@Preview(
    name = "Campo contraseña",
    showBackground = true
)
@Composable
private fun CampoContrasenaPreview() {
    AppTheme {
        CampoContrasena(
            valor = "clave123",
            alCambiar = {},
            visible = false,
            alAlternarVisible = {}
        )
    }
}

@Preview(
    name = "Campo contraseña con error",
    showBackground = true
)
@Composable
private fun CampoContrasenaErrorPreview() {
    AppTheme {
        CampoContrasena(
            valor = "",
            alCambiar = {},
            visible = false,
            alAlternarVisible = {},
            error = "La contraseña es obligatoria"
        )
    }
}