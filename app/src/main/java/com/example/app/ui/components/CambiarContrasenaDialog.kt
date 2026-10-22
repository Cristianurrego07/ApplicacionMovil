package com.example.app.ui.components

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.app.viewmodel.CambiarContrasenaViewModel

@Composable
fun CambiarContrasenaDialog(
    usuarioId: Long,
    onCerrar: () -> Unit,
    onCambiar: (String, String) -> Boolean,
    onExito: () -> Unit
) {
    val viewModel: CambiarContrasenaViewModel = viewModel(key = "contrasena_$usuarioId")
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    fun cerrar() { viewModel.limpiar(); onCerrar() }
    AlertDialog(
        onDismissRequest = { cerrar() },
        title = { Text("Cambiar contraseña") },
        text = {
            Column(modifier = Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("La nueva contraseña debe tener al menos 6 caracteres.")
                CampoContrasena(valor = estado.actual, alCambiar = viewModel::cambiarActual,
                    visible = estado.actualVisible, alAlternarVisible = viewModel::alternarActual,
                    error = estado.errorActual, etiqueta = "Contraseña actual")
                CampoContrasena(valor = estado.nueva, alCambiar = viewModel::cambiarNueva,
                    visible = estado.nuevaVisible, alAlternarVisible = viewModel::alternarNueva,
                    error = estado.errorNueva, etiqueta = "Nueva contraseña")
                CampoContrasena(valor = estado.confirmacion, alCambiar = viewModel::cambiarConfirmacion,
                    visible = estado.confirmacionVisible, alAlternarVisible = viewModel::alternarConfirmacion,
                    error = estado.errorConfirmacion, etiqueta = "Confirmar nueva contraseña")
                estado.errorGuardado?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (viewModel.guardar(onCambiar)) { onCerrar(); onExito() }
            }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = { cerrar() }) { Text("Cancelar") } }
    )
}
