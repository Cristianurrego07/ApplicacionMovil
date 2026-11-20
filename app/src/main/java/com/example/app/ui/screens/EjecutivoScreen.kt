package com.example.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.app.model.Compromiso
import com.example.app.model.Usuario
import com.example.app.ui.components.BotonPrincipal
import com.example.app.ui.components.BotonSecundario
import com.example.app.ui.components.TarjetaCompromiso
import com.example.app.ui.components.TarjetaEstadistica
import com.example.app.ui.components.TarjetaSeccion
import com.example.app.ui.theme.AppThemePreview
import com.example.app.ui.theme.Dimens
import java.text.NumberFormat
import java.util.Locale

/**
 * Pantalla de inicio del rol EJECUTIVO.
 * Usa el mismo diseño y componentes de Cristian (colores PagoFlexGreen / PagoFlexBlue).
 * Funcionalidad básica portada desde Main: resumen de cobranza + listado de compromisos.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EjecutivoScreen(
    usuario: Usuario?,
    compromisos: List<Compromiso>,
    onCerrarSesion: () -> Unit,
    onVerDetalle: (String) -> Unit,
    onVerPerfil: () -> Unit = {},
    onRegistrarCompromiso: () -> Unit = {}
) {
    val formato = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-CL"))
    val pendientes = compromisos.filter { it.estado.equals("Pendiente", true) || it.estado.equals("Vencido", true) }
    val pagados = compromisos.filter { it.estado.equals("Pagado", true) }
    val totalPendiente = pendientes.sumOf { it.monto + it.recargo }
    val totalPagado = pagados.sumOf { it.monto }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Hola, ${usuario?.nombre?.substringBefore(" ") ?: "Ejecutivo"}",
                        fontWeight = FontWeight.SemiBold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(Dimens.espacioMedio),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
        ) {
            item {
                Text(
                    text = "Panel Ejecutivo · Cobranza de la empresa",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
                ) {
                    TarjetaEstadistica(
                        valor = pendientes.size.toString(),
                        etiqueta = "Pendientes",
                        modifier = Modifier.weight(1f)
                    )
                    TarjetaEstadistica(
                        valor = pagados.size.toString(),
                        etiqueta = "Pagados",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                TarjetaSeccion(titulo = "Resumen de cobranza") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Total por cobrar: ${formato.format(totalPendiente)}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Total recuperado: ${formato.format(totalPagado)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
                ) {
                    BotonPrincipal(
                        texto = "Registrar compromiso",
                        onClick = onRegistrarCompromiso,
                        modifier = Modifier.weight(1f)
                    )
                    BotonSecundario(
                        texto = "Mi perfil",
                        onClick = onVerPerfil,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                BotonSecundario(
                    texto = "Cerrar sesión",
                    onClick = onCerrarSesion,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Compromisos de la empresa",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (compromisos.isEmpty()) {
                item {
                    Text(
                        text = "No hay compromisos registrados aún.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(compromisos, key = { it.folio }) { compromiso ->
                    TarjetaCompromiso(
                        compromiso = compromiso,
                        alPresionar = { onVerDetalle(compromiso.folio) }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EjecutivoScreenPreview() {
    AppThemePreview {
        EjecutivoScreen(
            usuario = Usuario(
                id = 3,
                rut = "12.345.678-9",
                nombre = "Carla Ejecutivo",
                correo = "carla@empresa.com",
                rolId = 3,
                rolNombre = "EJECUTIVO"
            ),
            compromisos = emptyList(),
            onCerrarSesion = {},
            onVerDetalle = {}
        )
    }
}
