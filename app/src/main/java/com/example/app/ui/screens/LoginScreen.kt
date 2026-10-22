package com.example.app.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.app.model.PortalLogin
import com.example.app.model.Usuario
import com.example.app.ui.components.CampoContrasena
import com.example.app.ui.theme.AppThemePreview
import com.example.app.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    onIniciarSesion: (String, String) -> Usuario?,
    onLoginExitoso: (Usuario) -> Unit,
    onCrearCuenta: () -> Unit,
    onCrearCuentaAgente: () -> Unit = onCrearCuenta,
    viewModel: LoginViewModel = viewModel()
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    LoginContent(
        estado = estado,
        onPortal = viewModel::seleccionarPortal,
        onIdentificador = viewModel::cambiarIdentificador,
        onContrasena = viewModel::cambiarContrasena,
        onVisibilidad = viewModel::alternarVisibilidad,
        onIngresar = { viewModel.iniciarSesion(onIniciarSesion)?.let(onLoginExitoso) },
        onCrearCuenta = onCrearCuenta,
        onCrearCuentaAgente = onCrearCuentaAgente
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginContent(
    estado: LoginUiState,
    onPortal: (PortalLogin) -> Unit,
    onIdentificador: (String) -> Unit,
    onContrasena: (String) -> Unit,
    onVisibilidad: () -> Unit,
    onIngresar: () -> Unit,
    onCrearCuenta: () -> Unit,
    onCrearCuentaAgente: () -> Unit = onCrearCuenta
) {
    val esCliente = estado.portal == PortalLogin.CLIENTE
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .imePadding().padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(painterResource(R.drawable.logo_pagoflex), contentDescription = "Logo PagoFlex",
                modifier = Modifier.size(80.dp))
            Text("PagoFlex", style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Text(if (esCliente) "Portal de Clientes" else "Portal de Agentes",
                style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(24.dp))
            TabRow(selectedTabIndex = estado.portal.ordinal,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary) {
                Tab(selected = esCliente, onClick = { onPortal(PortalLogin.CLIENTE) },
                    text = { Text("Portal Clientes") },
                    icon = { Icon(painterResource(R.drawable.ic_persona), contentDescription = null) })
                Tab(selected = !esCliente, onClick = { onPortal(PortalLogin.AGENTE) },
                    text = { Text("Portal Agentes") },
                    icon = { Icon(painterResource(R.drawable.ic_empresa), contentDescription = null) })
            }
            Spacer(Modifier.height(28.dp))
            OutlinedTextField(value = estado.identificador, onValueChange = onIdentificador,
                label = { Text("Correo electrónico o RUT") }, singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(painterResource(R.drawable.ic_correo), contentDescription = null,
                    modifier = Modifier.size(20.dp)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next))
            Spacer(Modifier.height(20.dp))
            CampoContrasena(valor = estado.contrasena, alCambiar = onContrasena,
                visible = estado.contrasenaVisible, alAlternarVisible = onVisibilidad)
            estado.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp))
            }
            Spacer(Modifier.height(20.dp))
            Button(onClick = onIngresar, modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp)) {
                Text(if (esCliente) "Iniciar Sesión Cliente" else "Iniciar Sesión Agente",
                    style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.height(16.dp))
            if (esCliente) {
                TextButton(onClick = onCrearCuenta) {
                    Text("¿No tienes cuenta? Regístrate como Cliente aquí")
                }
            } else {
                TextButton(onClick = onCrearCuentaAgente) {
                    Text("¿No tienes cuenta? Regístrate como Agente aquí")
                }
            }
        }
    }
}

@Preview(name = "Cliente claro", showBackground = true, showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Cliente oscuro", showBackground = true, showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun LoginPreview() {
    AppThemePreview {
        LoginContent(LoginUiState(), {}, {}, {}, {}, {}, {})
    }
}

@Preview(name = "Agente", showBackground = true, showSystemUi = true)
@Composable
private fun LoginAgentePreview() {
    AppThemePreview {
        LoginContent(LoginUiState(portal = PortalLogin.AGENTE), {}, {}, {}, {}, {}, {})
    }
}
