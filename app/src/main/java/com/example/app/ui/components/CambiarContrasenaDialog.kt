package com.example.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.app.ui.theme.AppThemePreview
import com.example.app.viewmodel.CambiarContrasenaViewModel

@Composable
fun CambiarContrasenaDialog(
    usuarioId: Long,
    onCerrar: () -> Unit,
    onCambiar: (String, String) -> Boolean,
    onExito: () -> Unit
) {
    val viewModel: CambiarContrasenaViewModel = viewModel(
        key = "contrasena_$usuarioId"
    )

    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    val (focoNueva, focoConfirmacion) = remember { FocusRequester.createRefs() }

    fun cerrar() {
        viewModel.limpiar()
        onCerrar()
    }

    AlertDialog(
        onDismissRequest = {
            cerrar()
        },
        title = {
            Text(
                text = "Cambiar contraseña",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "La contraseña debe tener al menos 6 caracteres, " +
                            "una mayúscula, una minúscula, un número y un carácter especial. " +
                            "No puede contener espacios.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )

                CampoContrasena(
                    valor = estado.actual,
                    alCambiar = viewModel::cambiarActual,
                    visible = estado.actualVisible,
                    alAlternarVisible = viewModel::alternarActual,
                    error = estado.errorActual,
                    imeAction = ImeAction.Next,
                    keyboardActions = KeyboardActions(onNext = { focoNueva.requestFocus() }),
                    etiqueta = "Contraseña actual"
                )

                CampoContrasena(
                    valor = estado.nueva,
                    alCambiar = viewModel::cambiarNueva,
                    visible = estado.nuevaVisible,
                    alAlternarVisible = viewModel::alternarNueva,
                    error = estado.errorNueva,
                    modifier = Modifier.focusRequester(focoNueva),
                    imeAction = ImeAction.Next,
                    keyboardActions = KeyboardActions(onNext = { focoConfirmacion.requestFocus() }),
                    etiqueta = "Nueva contraseña"
                )

                CampoContrasena(
                    valor = estado.confirmacion,
                    alCambiar = viewModel::cambiarConfirmacion,
                    visible = estado.confirmacionVisible,
                    alAlternarVisible = viewModel::alternarConfirmacion,
                    error = estado.errorConfirmacion,
                    modifier = Modifier.focusRequester(focoConfirmacion),
                    etiqueta = "Confirmar nueva contraseña"
                )

                estado.errorGuardado?.let { mensaje ->
                    Text(
                        text = mensaje,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (viewModel.guardar(onCambiar)) {
                        onCerrar()
                        onExito()
                    }
                }
            ) {
                Text(
                    text = "Guardar",
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    cerrar()
                }
            ) {
                Text(
                    text = "Cancelar",
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    )
}

@Preview(
    name = "Cambiar contraseña",
    showBackground = true,
    showSystemUi = true
)
@Composable
private fun CambiarContrasenaDialogPreview() {
    AppThemePreview {
        CambiarContrasenaDialog(
            usuarioId = 1L,
            onCerrar = {},
            onCambiar = { _, _ -> true },
            onExito = {}
        )
    }
}