package com.example.app.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.app.R
import com.example.app.model.LoginUiState
import com.example.app.model.Usuario
import com.example.app.ui.components.CampoContrasena
import com.example.app.ui.theme.AppThemePreview
import com.example.app.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    onIniciarSesion: (String, String) -> Usuario?,
    onLoginExitoso: (Usuario) -> Unit,
    onCrearCuenta: () -> Unit,
    onRecuperarContrasena: () -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()

    LoginContent(
        estado = estado,
        onIdentificador = viewModel::cambiarIdentificador,
        onContrasena = viewModel::cambiarContrasena,
        onRecuperarContrasena = onRecuperarContrasena,
        onVisibilidad = viewModel::alternarVisibilidad,
        onIngresar = {
            viewModel.iniciarSesion(onIniciarSesion)?.let(onLoginExitoso)
        },
        onCrearCuenta = onCrearCuenta
    )
}

@Composable
fun LoginContent(
    estado: LoginUiState,
    onIdentificador: (String) -> Unit,
    onContrasena: (String) -> Unit,
    onRecuperarContrasena: () -> Unit,
    onVisibilidad: () -> Unit,
    onIngresar: () -> Unit,
    onCrearCuenta: () -> Unit
) {
    val focoContrasena = remember { FocusRequester() }
    Scaffold(
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
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.logo_pagoflex),
                    contentDescription = "Logo PagoFlex",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Iniciar sesión",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Text(
                        text = "Ingresa tu correo o RUT y tu contraseña.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                OutlinedTextField(
                    value = estado.identificador,
                    onValueChange = onIdentificador,
                    label = {
                        Text(
                            text = "Correo electrónico o RUT",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = estado.errorIdentificador != null,
                    supportingText = {
                        estado.errorIdentificador?.let { mensaje ->
                            Text(
                                text = mensaje,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_persona),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        cursorColor = MaterialTheme.colorScheme.primary
                    ),
                    keyboardActions = KeyboardActions(onNext = { focoContrasena.requestFocus() }),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    )
                )

                CampoContrasena(
                    modifier = Modifier.focusRequester(focoContrasena),
                    imeAction = ImeAction.Done,
                    keyboardActions = KeyboardActions(onDone = { onIngresar() }),
                    valor = estado.contrasena,
                    alCambiar = onContrasena,
                    visible = estado.contrasenaVisible,
                    alAlternarVisible = onVisibilidad,
                    error = estado.errorContrasena
                )

                TextButton(
                    onClick = onRecuperarContrasena,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(
                        text = "¿Olvidaste tu contraseña?",
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                estado.error?.let { mensaje ->
                    Text(
                        text = mensaje,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                }

                Button(
                    onClick = onIngresar,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 54.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        text = "Iniciar sesión",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "¿No tienes cuenta?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    TextButton(
                        onClick = onCrearCuenta,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = "Crear cuenta",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }
    }
}

@Preview(
    name = "Inicio de sesión claro",
    showBackground = true,
    showSystemUi = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "Inicio de sesión oscuro",
    showBackground = true,
    showSystemUi = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun LoginPreview() {
    AppThemePreview {
        LoginContent(
            estado = LoginUiState(),
            onIdentificador = {},
            onContrasena = {},
            onRecuperarContrasena = {},
            onVisibilidad = {},
            onIngresar = {},
            onCrearCuenta = {}
        )
    }
}

@Preview(
    name = "Inicio de sesión con errores",
    showBackground = true,
    showSystemUi = true
)
@Composable
private fun LoginConErroresPreview() {
    AppThemePreview {
        LoginContent(
            estado = LoginUiState(
                errorIdentificador = "Ingresa tu correo o RUT",
                errorContrasena = "Ingresa tu contraseña"
            ),
            onIdentificador = {},
            onContrasena = {},
            onRecuperarContrasena = {},
            onVisibilidad = {},
            onIngresar = {},
            onCrearCuenta = {}
        )
    }
}
@Preview(
    name = "Credenciales incorrectas",
    showBackground = true,
    showSystemUi = true
)
@Composable
private fun LoginCredencialesIncorrectasPreview() {
    AppThemePreview {
        LoginContent(
            estado = LoginUiState(
                identificador = "sofia@gmail.com",
                contrasena = "incorrecta",
                error = "Correo, RUT o contraseña incorrectos."
            ),
            onIdentificador = {},
            onContrasena = {},
            onRecuperarContrasena = {},
            onVisibilidad = {},
            onIngresar = {},
            onCrearCuenta = {}
        )
    }
}