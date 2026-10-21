package com.example.miprimeraapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.miprimeraapp.R
import com.example.miprimeraapp.model.Comprobante
import com.example.miprimeraapp.ui.components.BotonPrincipal
import com.example.miprimeraapp.ui.theme.PagoFlexPurple

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComprobanteScreen(
    comprobante: Comprobante?,
    onVolver: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Comprobante de Pago") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            painter = painterResource(R.drawable.ic_volver),
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (comprobante == null) {
                Text("No se encontró el comprobante digital.")
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "¡Pago realizado exitosamente!",
                            style = MaterialTheme.typography.titleLarge,
                            color = PagoFlexPurple
                        )
                        Text("Número Comprobante: ${comprobante.numero}", style = MaterialTheme.typography.bodyMedium)
                        Text("Folio Compromiso: ${comprobante.folioCompromiso}", style = MaterialTheme.typography.bodyMedium)
                        Text("Empresa: ${comprobante.empresa}", style = MaterialTheme.typography.bodyMedium)
                        Text("Concepto: ${comprobante.concepto}", style = MaterialTheme.typography.bodyMedium)
                        Text("Total Pagado: $ %,d".format(comprobante.montoPagado), style = MaterialTheme.typography.titleMedium)
                        Text("Fecha/Hora: ${comprobante.fechaHora}", style = MaterialTheme.typography.bodySmall)
                        Text("Canal: ${comprobante.canal}", style = MaterialTheme.typography.bodySmall)
                        Text("Medio de Pago: ${comprobante.medioPago}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            BotonPrincipal(
                texto = "Volver al Inicio",
                onClick = onVolver,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
