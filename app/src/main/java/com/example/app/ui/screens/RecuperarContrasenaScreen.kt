package com.example.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.app.R
import com.example.app.ui.components.CampoContrasena
import com.example.app.ui.theme.AppThemePreview

// Pasos que se mostrarán en la pantalla.
enum class PasoRecuperacion {
    CORREO,
    CODIGO,
    NUEVA_CONTRASENA,
    EXITO
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecuperarContrasenaScreen(
    paso: PasoRecuperacion,
    correo: String,
    codigo: String,
    nuevaContrasena: String,
    confirmarContrasena: String,
    contrasenaVisible: Boolean,
    error: String?,
    onCorreoCambia: (String) -> Unit,
    onCodigoCambia: (String) -> Unit,
    onNuevaContrasenaCambia: (String) -> Unit,
    onConfirmarContrasenaCambia: (String) -> Unit,
    onAlternarVisibilidad: () -> Unit,
    onSolicitarCodigo: () -> Unit,
    onVerificarCodigo: () -> Unit,
    onRestablecerContrasena: () -> Unit,
    onVolver: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Recuperar contraseña",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                role = Role.Button,
                                onClick = onVolver
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { espacio ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(espacio)
                .imePadding()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 500.dp)
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (paso) {
                    PasoRecuperacion.CORREO -> {
                        Text(
                            text = "Ingresa tu correo electrónico para recibir un código de recuperación.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Black
                        )

                        OutlinedTextField(
                            value = correo,
                            onValueChange = onCorreoCambia,
                            label = { Text("Correo electrónico") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email
                            )
                        )

                        Button(
                            onClick = onSolicitarCodigo,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Text(text = "Enviar código")
                        }
                    }

                    PasoRecuperacion.CODIGO -> {
                        Text(
                            text = "Ingresa el código que recibiste en tu correo.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Black
                        )

                        OutlinedTextField(
                            value = codigo,
                            onValueChange = onCodigoCambia,
                            label = { Text("Código de verificación") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number
                            )
                        )

                        Button(
                            onClick = onVerificarCodigo,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Text(text = "Verificar código")
                        }
                    }

                    PasoRecuperacion.NUEVA_CONTRASENA -> {
                        Text(
                            text = "Crea una nueva contraseña y escríbela nuevamente para confirmarla.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Black
                        )

                        CampoContrasena(
                            valor = nuevaContrasena,
                            alCambiar = onNuevaContrasenaCambia,
                            etiqueta = "Nueva contraseña",
                            visible = contrasenaVisible,
                            alAlternarVisible = onAlternarVisibilidad
                        )

                        CampoContrasena(
                            valor = confirmarContrasena,
                            alCambiar = onConfirmarContrasenaCambia,
                            etiqueta = "Confirmar contraseña",
                            visible = contrasenaVisible,
                            alAlternarVisible = onAlternarVisibilidad
                        )

                        Button(
                            onClick = onRestablecerContrasena,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Text(text = "Restablecer contraseña")
                        }
                    }

                    PasoRecuperacion.EXITO -> {
                        Text(
                            text = "Contraseña actualizada",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text(
                            text = "Ya puedes iniciar sesión con tu nueva contraseña.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Black,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = onVolver,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Text(text = "Volver al inicio de sesión")
                        }
                    }
                }

                error?.let { mensaje ->
                    Text(
                        text = mensaje,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun RecuperarContrasenaPreview() {
    AppThemePreview {
        RecuperarContrasenaScreen(
            paso = PasoRecuperacion.CORREO,
            correo = "",
            codigo = "",
            nuevaContrasena = "",
            confirmarContrasena = "",
            contrasenaVisible = false,
            error = null,
            onCorreoCambia = {},
            onCodigoCambia = {},
            onNuevaContrasenaCambia = {},
            onConfirmarContrasenaCambia = {},
            onAlternarVisibilidad = {},
            onSolicitarCodigo = {},
            onVerificarCodigo = {},
            onRestablecerContrasena = {},
            onVolver = {}
        )
    }
}

@Preview(name = "Verificar código", showBackground = true, showSystemUi = true)
@Composable
private fun RecuperarCodigoPreview() {
    RecuperacionPasoPreview(PasoRecuperacion.CODIGO)
}

@Preview(name = "Nueva contraseña", showBackground = true, showSystemUi = true)
@Composable
private fun RecuperarNuevaContrasenaPreview() {
    RecuperacionPasoPreview(PasoRecuperacion.NUEVA_CONTRASENA)
}

@Preview(name = "Contraseña actualizada", showBackground = true, showSystemUi = true)
@Composable
private fun RecuperarExitoPreview() {
    RecuperacionPasoPreview(PasoRecuperacion.EXITO)
}

@Composable
private fun RecuperacionPasoPreview(paso: PasoRecuperacion) {
    AppThemePreview {
        RecuperarContrasenaScreen(
            paso = paso,
            correo = "ejemplo@correo.com",
            codigo = "",
            nuevaContrasena = "",
            confirmarContrasena = "",
            contrasenaVisible = false,
            error = null,
            onCorreoCambia = {},
            onCodigoCambia = {},
            onNuevaContrasenaCambia = {},
            onConfirmarContrasenaCambia = {},
            onAlternarVisibilidad = {},
            onSolicitarCodigo = {},
            onVerificarCodigo = {},
            onRestablecerContrasena = {},
            onVolver = {}
        )
    }
}
