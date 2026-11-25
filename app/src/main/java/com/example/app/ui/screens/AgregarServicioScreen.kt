package com.example.app.ui.screens

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.clickable
import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.app.R
import com.example.app.data.remote.Resultado
import com.example.app.model.Servicio
import com.example.app.model.ServicioUiState
import com.example.app.ui.components.BotonPrincipal
import com.example.app.ui.components.CampoTexto
import com.example.app.ui.components.TarjetaSeccion
import com.example.app.ui.theme.AppThemePreview
import com.example.app.ui.theme.Secundario
import com.example.app.viewmodel.ServicioViewModel
import kotlinx.coroutines.launch

@Composable
fun AgregarServicioScreen(
    usuarioId: Long,
    onVolver: () -> Unit,
    onServicioGuardado: () -> Unit,
    onGuardarServicio: suspend (Servicio) -> Resultado<Servicio>,
    viewModel: ServicioViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    var guardando by remember { mutableStateOf(false) }

    AgregarServicioContent(
        uiState = uiState,
        onVolver = onVolver,
        onNombreEmpresaCambia = viewModel::onNombreEmpresaCambia,
        onRutPagadorCambia = viewModel::onRutPagadorCambia,
        onAliasCambia = viewModel::onAliasCambia,
        guardando = guardando,
        onAgregar = {
            if (!guardando) {
                viewModel.onAgregar(usuarioId)?.let { servicio ->
                    guardando = true
                    scope.launch {
                        try {
                            when (val resultado = onGuardarServicio(servicio)) {
                                is Resultado.Ok -> onServicioGuardado()
                                is Resultado.Error -> viewModel.onGuardadoFallido(resultado.mensaje)
                            }
                        } finally {
                            guardando = false
                        }
                    }
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgregarServicioContent(
    uiState: ServicioUiState,
    onVolver: () -> Unit,
    onNombreEmpresaCambia: (String) -> Unit,
    onRutPagadorCambia: (String) -> Unit,
    onAliasCambia: (String) -> Unit,
    onAgregar: () -> Unit,
    guardando: Boolean = false
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Agregar servicio") },
                navigationIcon = {
                    androidx.compose.foundation.layout.Box(
                            modifier = androidx.compose.ui.Modifier
                                .size(48.dp)
                                .clickable(
                                    interactionSource = androidx.compose.runtime.remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                    indication = null,
                                    role = androidx.compose.ui.semantics.Role.Button,
                                    onClick = onVolver
                                ),
                            contentAlignment = androidx.compose.ui.Alignment.Center
                        ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = "Volver",
                            tint = Secundario
                        )
                    }
                }
            )
        }
    ) { espacio ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(espacio)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Ingresa los datos del servicio que quieres agregar.")

            TarjetaSeccion(titulo = "Datos del servicio") {
                CampoTexto(
                    valor = uiState.nombreEmpresa,
                    etiqueta = "Nombre de la empresa",
                    alCambiar = onNombreEmpresaCambia,
                    error = uiState.errorNombreEmpresa
                )
                CampoTexto(
                    valor = uiState.rutPagador,
                    etiqueta = "RUT",
                    alCambiar = onRutPagadorCambia,
                    error = uiState.errorRutPagador
                )
                CampoTexto(
                    valor = uiState.alias,
                    etiqueta = "Descripción o alias",
                    alCambiar = onAliasCambia,
                    error = uiState.errorAlias
                )
                Text(
                    "Usa un nombre fácil de reconocer para identificar este compromiso.",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            uiState.errorRegistro?.let { mensaje ->
                Text(mensaje, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(4.dp))
            BotonPrincipal(
                texto = if (guardando) "Guardando…" else "Agregar servicio",
                onClick = onAgregar,
                enabled = !guardando,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AgregarServicioPreview() {
    AppThemePreview {
        AgregarServicioContent(
            uiState = ServicioUiState(),
            onVolver = {},
            onNombreEmpresaCambia = {},
            onRutPagadorCambia = {},
            onAliasCambia = {},
            onAgregar = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AgregarServicioConErroresPreview() {
    AppThemePreview {
        AgregarServicioContent(
            uiState = ServicioUiState(
                errorNombreEmpresa = "El nombre de la empresa es obligatorio",
                errorRutPagador = "Ingresa un RUT válido, incluyendo su dígito verificador",
                errorAlias = "El alias es obligatorio"
            ),
            onVolver = {},
            onNombreEmpresaCambia = {},
            onRutPagadorCambia = {},
            onAliasCambia = {},
            onAgregar = {}
        )
    }
}
