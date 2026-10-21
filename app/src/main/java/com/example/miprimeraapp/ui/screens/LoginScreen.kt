package com.example.miprimeraapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.miprimeraapp.R
import com.example.miprimeraapp.data.CorreoNotificador
import com.example.miprimeraapp.ui.components.CampoContrasena
import com.example.miprimeraapp.ui.theme.PagoFlexPurple
import com.example.miprimeraapp.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onInicioExitoso: (Long) -> Unit,
    onIrARegistro: (String) -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    var portalSeleccionado by remember { mutableIntStateOf(0) } // 0: Clientes, 1: Empresa/Agente/Admin
    var correo by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var contrasenaVisible by remember { mutableStateOf(false) }

    var mostrarDialogoRecuperar by remember { mutableStateOf(false) }
    var inputRecuperar by remember { mutableStateOf("") }
    var mensajeRecuperacion by remember { mutableStateOf<String?>(null) }

    val mensajeError by viewModel.mensajeError.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    if (mostrarDialogoRecuperar) {
        AlertDialog(
            onDismissRequest = {
                mostrarDialogoRecuperar = false
                mensajeRecuperacion = null
                inputRecuperar = ""
            },
            icon = { Icon(Icons.Default.LockReset, contentDescription = null, tint = PagoFlexPurple) },
            title = { Text("Recuperar Contraseña") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Ingresa tu correo electrónico o RUT registrado para restablecer tu contraseña.")
                    OutlinedTextField(
                        value = inputRecuperar,
                        onValueChange = { inputRecuperar = it },
                        label = { Text("Correo o RUT") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    mensajeRecuperacion?.let { msg ->
                        Text(
                            text = msg,
                            color = if (msg.startsWith("¡")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (inputRecuperar.isNotBlank()) {
                            val usuario = viewModel.recuperarContrasena(inputRecuperar)
                            if (usuario != null) {
                                val nuevaClave = "PagoFlex2026"
                                mensajeRecuperacion = "Contraseña restablecida a '$nuevaClave'. Notificación enviada por correo."
                                CorreoNotificador.enviarCorreo(
                                    context = context,
                                    destinatario = usuario.correo,
                                    asunto = "PagoFlex - Recuperación de Contraseña",
                                    cuerpo = "Hola ${usuario.nombre},\n\nTu contraseña ha sido restablecida exitosamente a: $nuevaClave.\nTe recomendamos cambiarla desde la sección 'Perfil' de tu aplicación.\n\nAtentamente,\nEquipo PagoFlex"
                                )
                            } else {
                                mensajeRecuperacion = "No se encontró ningún usuario con ese correo o RUT."
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PagoFlexPurple)
                ) {
                    Text("Recuperar")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    mostrarDialogoRecuperar = false
                    mensajeRecuperacion = null
                    inputRecuperar = ""
                }) {
                    Text("Cerrar")
                }
            }
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .imePadding()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "PagoFlex",
                style = MaterialTheme.typography.headlineLarge,
                color = PagoFlexPurple,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (portalSeleccionado == 0) "Portal de Clientes" else "Portal Administración / Empresa",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(20.dp))

            TabRow(
                selectedTabIndex = portalSeleccionado,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = PagoFlexPurple,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = portalSeleccionado == 0,
                    onClick = {
                        portalSeleccionado = 0
                        correo = ""
                        contrasena = ""
                    },
                    text = { Text("Portal Clientes") },
                    icon = { Icon(Icons.Default.Person, contentDescription = null) }
                )
                Tab(
                    selected = portalSeleccionado == 1,
                    onClick = {
                        portalSeleccionado = 1
                        correo = ""
                        contrasena = ""
                    },
                    text = { Text("Portal Agente / Admin") },
                    icon = { Icon(Icons.Default.Business, contentDescription = null) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = correo,
                onValueChange = { correo = it },
                label = { Text("Correo electrónico o RUT") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_correo),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PagoFlexPurple,
                    focusedLabelColor = PagoFlexPurple
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            CampoContrasena(
                valor = contrasena,
                onValorCambia = { contrasena = it },
                etiqueta = "Contraseña",
                visible = contrasenaVisible,
                onAlternarVisible = { contrasenaVisible = !contrasenaVisible }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { mostrarDialogoRecuperar = true }) {
                    Text(
                        text = "¿Olvidaste tu contraseña?",
                        style = MaterialTheme.typography.bodySmall,
                        color = PagoFlexPurple
                    )
                }
            }

            if (!mensajeError.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = mensajeError ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    viewModel.iniciarSesion(correo, contrasena) { exito, idRol ->
                        if (exito) {
                            onInicioExitoso(idRol)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PagoFlexPurple)
            ) {
                Text(
                    text = if (portalSeleccionado == 0) "Iniciar Sesión Cliente" else "Ingresar al Portal Empresa",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (portalSeleccionado == 0) {
                TextButton(onClick = { onIrARegistro("cliente") }) {
                    Text(
                        text = "¿No tienes cuenta? Regístrate como Cliente aquí",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            } else {
                TextButton(onClick = { onIrARegistro("empresa") }) {
                    Text(
                        text = "¿Nuevo personal? Crear cuenta de Agente / Ejecutivo aquí",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
