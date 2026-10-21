package com.example.miprimeraapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.miprimeraapp.R
import com.example.miprimeraapp.model.Servicio
import com.example.miprimeraapp.model.ServicioUiState
import com.example.miprimeraapp.ui.components.BotonPrincipal
import com.example.miprimeraapp.ui.components.CampoTexto
import com.example.miprimeraapp.ui.components.TarjetaSeccion
import com.example.miprimeraapp.ui.styles.EstilosCampo
import com.example.miprimeraapp.ui.theme.PagoFlexPurple
import com.example.miprimeraapp.viewmodel.ServicioViewModel

@Composable
fun AgregarServicioScreen(
    usuarioId: Long,
    onVolver: () -> Unit,
    onGuardarServicio: (Servicio) -> Boolean,
    viewModel: ServicioViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AgregarServicioContent(
        uiState = uiState,
        onVolver = onVolver,
        onNombreEmpresaCambia = viewModel::onNombreEmpresaCambia,
        onNumeroClienteCambia = viewModel::onNumeroClienteCambia,
        onRutPagadorCambia = viewModel::onRutPagadorCambia,
        onAliasCambia = viewModel::onAliasCambia,
        onAgregar = {
            val servicio = viewModel.onAgregar(usuarioId)
            if (servicio != null) {
                if (onGuardarServicio(servicio)) {
                    onVolver()
                } else {
                    viewModel.onGuardadoFallido()
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
    onNumeroClienteCambia: (String) -> Unit,
    onRutPagadorCambia: (String) -> Unit,
    onAliasCambia: (String) -> Unit,
    onAgregar: () -> Unit
) {
    var expandidoEmpresas by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inscribir Servicio") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            painter = painterResource(R.drawable.ic_volver),
                            contentDescription = "Volver",
                            tint = PagoFlexPurple
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
                .imePadding()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Inscribe tus cuentas y servicios para recibir tus cobros y realizar tus pagos fácilmente en PagoFlex.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            TarjetaSeccion(titulo = "Información de la Cuenta / Servicio") {
                // Dropdown Biller Selection
                ExposedDropdownMenuBox(
                    expanded = expandidoEmpresas,
                    onExpandedChange = { expandidoEmpresas = !expandidoEmpresas }
                ) {
                    OutlinedTextField(
                        value = uiState.nombreEmpresa,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Empresa o Biller") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandidoEmpresas) },
                        shape = EstilosCampo.forma,
                        colors = EstilosCampo.colores(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
                    )
                    ExposedDropdownMenu(
                        expanded = expandidoEmpresas,
                        onDismissRequest = { expandidoEmpresas = false }
                    ) {
                        uiState.empresasDisponibles.forEach { opcion ->
                            DropdownMenuItem(
                                text = { Text(opcion) },
                                onClick = {
                                    onNombreEmpresaCambia(opcion)
                                    expandidoEmpresas = false
                                }
                            )
                        }
                    }
                }

                CampoTexto(
                    valor = uiState.numeroCliente,
                    etiqueta = "Número de Cliente / Cuenta / Contrato",
                    onValorCambia = onNumeroClienteCambia,
                    icono = R.drawable.ic_buscar,
                    error = uiState.errorNumeroCliente
                )

                CampoTexto(
                    valor = uiState.alias,
                    etiqueta = "Alias o Nombre Personalizado (ej. Casa, Depto 102)",
                    onValorCambia = onAliasCambia,
                    icono = R.drawable.ic_check,
                    error = uiState.errorAlias
                )

                CampoTexto(
                    valor = uiState.rutPagador,
                    etiqueta = "RUT del Pagador (Opcional)",
                    onValorCambia = onRutPagadorCambia,
                    icono = R.drawable.ic_persona,
                    error = uiState.errorRutPagador
                )

                Text(
                    "Al inscribir este servicio, tus próximos cobros y boletas aparecerán automáticamente en tu panel principal.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            uiState.errorRegistro?.let { mensaje ->
                Text(mensaje, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(8.dp))
            BotonPrincipal(
                texto = "Inscribir Cuenta / Servicio",
                onClick = onAgregar,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
