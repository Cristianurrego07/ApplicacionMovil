package com.example.miprimeraapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.example.miprimeraapp.R
import com.example.miprimeraapp.model.Compromiso
import com.example.miprimeraapp.ui.components.BotonPrincipal
import com.example.miprimeraapp.ui.components.CampoTexto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportarProblemaScreen(
    compromisos: List<Compromiso>,
    onVolver: () -> Unit,
    onReportar: (String, String) -> Boolean
) {
    var compromisoSeleccionado by remember { mutableStateOf(compromisos.firstOrNull()) }
    var motivo by remember { mutableStateOf("") }
    var expandido by remember { mutableStateOf(false) }
    var mensajeResultado by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reportar Problema") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(painterResource(R.drawable.ic_volver), contentDescription = "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Selecciona el compromiso y describe el motivo de tu reporte.")

            ExposedDropdownMenuBox(
                expanded = expandido,
                onExpandedChange = { expandido = it }
            ) {
                OutlinedTextField(
                    value = compromisoSeleccionado?.let { "${it.titulo} (${it.empresa})" } ?: "Seleccionar compromiso",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Compromiso") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                )
                ExposedDropdownMenu(
                    expanded = expandido,
                    onDismissRequest = { expandido = false }
                ) {
                    compromisos.forEach { comp ->
                        DropdownMenuItem(
                            text = { Text("${comp.titulo} - $ %,.0f".format(comp.monto)) },
                            onClick = {
                                compromisoSeleccionado = comp
                                expandido = false
                            }
                        )
                    }
                }
            }

            CampoTexto(
                valor = motivo,
                etiqueta = "Motivo o Descripción del Problema",
                onValorCambia = { motivo = it },
                icono = R.drawable.ic_buscar
            )

            mensajeResultado?.let {
                Text(it)
            }

            BotonPrincipal(
                texto = "Enviar Reporte",
                onClick = {
                    val comp = compromisoSeleccionado
                    if (comp != null && motivo.isNotBlank()) {
                        val exito = onReportar(comp.folio, motivo)
                        if (exito) {
                            onVolver()
                        } else {
                            mensajeResultado = "No se pudo registrar el reporte"
                        }
                    } else {
                        mensajeResultado = "Por favor selecciona un compromiso y escribe el motivo"
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
