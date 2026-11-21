package com.example.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Alignment
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.app.R
import com.example.app.ui.components.BotonPrincipal
import com.example.app.ui.components.CampoTexto
import com.example.app.viewmodel.CompromisoEjecutivoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrarCompromisoScreen(
    onVolver: () -> Unit,
    onGuardar: (String, String, Int, String, String) -> Boolean,
    vm: CompromisoEjecutivoViewModel = viewModel()
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registrar compromiso") },
                navigationIcon = {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clickable(onClick = onVolver),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = "Volver"
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CampoTexto(
                valor = vm.rut,
                etiqueta = "RUT del cliente",
                alCambiar = {
                    vm.rut = it
                    vm.errorRut = null
                    vm.errorGeneral = null
                },
                error = vm.errorRut
            )
            CampoTexto(
                valor = vm.concepto,
                etiqueta = "Concepto",
                alCambiar = {
                    vm.concepto = it
                    vm.errorConcepto = null
                    vm.errorGeneral = null
                },
                error = vm.errorConcepto
            )
            CampoTexto(
                valor = vm.monto,
                etiqueta = "Monto total",
                alCambiar = {
                    vm.monto = it
                    vm.errorMonto = null
                },
                error = vm.errorMonto
            )
            CampoTexto(
                valor = vm.vencimiento,
                etiqueta = "Primer vencimiento (DD/MM/AAAA)",
                alCambiar = {
                    vm.vencimiento = it
                    vm.errorFecha = null
                },
                error = vm.errorFecha
            )
            CampoTexto(
                valor = vm.totalCuotas,
                etiqueta = "Cantidad de cuotas (opcional)",
                alCambiar = {
                    vm.totalCuotas = it
                    vm.errorTotalCuotas = null
                },
                error = vm.errorTotalCuotas
            )
            vm.errorGeneral?.let { mensaje ->
                Text(mensaje)
            }
            BotonPrincipal(
                texto = "Registrar compromiso",
                onClick = {
                    if (vm.guardar(onGuardar)) {
                        onVolver()
                    }
                }
            )
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun RegistrarCompromisoPreview() {
    com.example.app.ui.theme.AppThemePreview {
        RegistrarCompromisoScreen(
            onVolver = {},
            onGuardar = { _, _, _, _, _ -> false },
            vm = CompromisoEjecutivoViewModel()
        )
    }
}
