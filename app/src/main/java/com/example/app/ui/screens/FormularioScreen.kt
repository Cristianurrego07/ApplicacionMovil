package com.example.app.ui.screens

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.app.R
import com.example.app.model.FormularioUiState
import com.example.app.model.Usuario
import com.example.app.ui.components.BotonPrincipal
import com.example.app.ui.components.BotonSecundario
import com.example.app.ui.components.CampoContrasena
import com.example.app.ui.components.CampoTexto
import com.example.app.ui.components.CasillaConTexto
import com.example.app.ui.components.TarjetaSeccion
import com.example.app.ui.theme.AppThemePreview
import com.example.app.viewmodel.FormularioViewModel
import kotlinx.coroutines.launch

@Composable
fun FormularioScreen(
    rolInicial: String = "CLIENTE",
    onRegistroExitoso: (Usuario) -> Unit,
    onVolverInicio: () -> Unit,
    onGuardarUsuario: (Usuario, String) -> Usuario? = { _, _ -> null },
    viewModel: FormularioViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text("PagoFlex", modifier = Modifier.padding(20.dp))
                HorizontalDivider()
                NavigationDrawerItem(
                    label = { Text("Inicio") },
                    selected = false,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            onVolverInicio()
                        }
                    }
                )
            }
        }
    ) {
        FormularioContent(
            uiState = uiState,
            rol = rolInicial,
            onNombreCambia = viewModel::onNombreCambia,
            onRutCambia = viewModel::onRutCambia,
            onCorreoCambia = viewModel::onCorreoCambia,
            onContrasenaCambia = viewModel::onContrasenaCambia,
            onConfirmarContrasenaCambia = viewModel::onConfirmarContrasenaCambia,
            onAlternarVisibilidad = viewModel::onAlternarVisibilidad,
            onTerminosCambia = viewModel::onTerminosCambia,
            onRegistrar = {
                val usuario = viewModel.onRegistrar()
                if (usuario != null) {
                    val usuarioGuardado = onGuardarUsuario(usuario, uiState.contrasena)
                    if (usuarioGuardado != null) {
                        onRegistroExitoso(usuarioGuardado)
                    } else {
                        viewModel.onRegistroFallido(
                            "No se pudo crear la cuenta. Revisa si el RUT o correo ya están registrados."
                        )
                    }
                }
            },
            onLimpiar = viewModel::onLimpiar,
            onAbrirMenu = {
                scope.launch {
                    drawerState.open()
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioContent(
    uiState: FormularioUiState,
    rol: String = "CLIENTE",
    onNombreCambia: (String) -> Unit,
    onRutCambia: (String) -> Unit,
    onCorreoCambia: (String) -> Unit,
    onContrasenaCambia: (String) -> Unit,
    onConfirmarContrasenaCambia: (String) -> Unit,
    onAlternarVisibilidad: () -> Unit,
    onTerminosCambia: (Boolean) -> Unit,
    onRegistrar: () -> Unit,
    onLimpiar: () -> Unit,
    onAbrirMenu: () -> Unit = {}
) {
    val esAgente = rol == "AGENTE"
    val (focoRut, focoCorreo, focoClave, focoConfirmacion) = remember { FocusRequester.createRefs() }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (esAgente) "Crear cuenta de agente" else "Crear cuenta") },
                navigationIcon = {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                role = Role.Button,
                                onClick = onAbrirMenu
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_menu),
                            contentDescription = "Abrir menú",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
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
                .animateContentSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(if (esAgente) "Ingresa tus datos para registrarte como agente." else "Ingresa tus datos para registrarte como cliente.")

            TarjetaSeccion(titulo = "Datos personales") {
                CampoTexto(
                    valor = uiState.nombre,
                    etiqueta = "Nombre completo",
                    alCambiar = onNombreCambia,
                    error = uiState.errorNombre,
                    keyboardActions = KeyboardActions(onNext = { focoRut.requestFocus() })
                )
                CampoTexto(
                    valor = uiState.rut,
                    etiqueta = "RUT",
                    alCambiar = onRutCambia,
                    error = uiState.errorRut,
                    modifier = Modifier.focusRequester(focoRut),
                    keyboardActions = KeyboardActions(onNext = { focoCorreo.requestFocus() })
                )
                CampoTexto(
                    valor = uiState.correo,
                    etiqueta = "Correo electrónico",
                    alCambiar = onCorreoCambia,
                    error = uiState.errorCorreo,
                    modifier = Modifier.focusRequester(focoCorreo),
                    keyboardActions = KeyboardActions(onNext = { focoClave.requestFocus() })
                )
                CampoContrasena(
                    valor = uiState.contrasena,
                    alCambiar = onContrasenaCambia,
                    visible = uiState.contrasenaVisible,
                    alAlternarVisible = onAlternarVisibilidad,
                    error = uiState.errorContrasena,
                    modifier = Modifier.focusRequester(focoClave),
                    imeAction = ImeAction.Next,
                    keyboardActions = KeyboardActions(onNext = { focoConfirmacion.requestFocus() })
                )
                CampoContrasena(
                    valor = uiState.confirmarContrasena,
                    alCambiar = onConfirmarContrasenaCambia,
                    visible = uiState.contrasenaVisible,
                    alAlternarVisible = onAlternarVisibilidad,
                    error = uiState.errorConfirmarContrasena,
                    etiqueta = "Repetir contraseña",
                    modifier = Modifier.focusRequester(focoConfirmacion)
                )
            }

            TarjetaSeccion(titulo = "Términos") {
                CasillaConTexto(
                    texto = "Acepto los términos y condiciones",
                    marcada = uiState.aceptaTerminos,
                    alCambiar = onTerminosCambia
                )
                uiState.errorTerminos?.let { mensaje ->
                    Text(mensaje, color = MaterialTheme.colorScheme.error)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                BotonSecundario(
                    texto = "Limpiar",
                    onClick = onLimpiar,
                    modifier = Modifier.weight(1f)
                )
                BotonPrincipal(
                    texto = "Crear cuenta",
                    onClick = onRegistrar,
                    modifier = Modifier.weight(1f)
                )
            }

            if (uiState.registroExitoso) {
                Text(
                    text = "Cuenta creada correctamente",
                    color = MaterialTheme.colorScheme.primary
                )
            }

            uiState.errorRegistro?.let { mensaje ->
                Text(mensaje, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(12.dp))
        }
    }
}

@Preview(name = "Formulario claro", showBackground = true, showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Formulario oscuro", showBackground = true, showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun FormularioVacioPreview() {
    AppThemePreview {
        FormularioContent(
            uiState = FormularioUiState(),
            onNombreCambia = {}, onRutCambia = {}, onCorreoCambia = {},
            onContrasenaCambia = {}, onConfirmarContrasenaCambia = {},
            onAlternarVisibilidad = {}, onTerminosCambia = {},
            onRegistrar = {}, onLimpiar = {}, onAbrirMenu = {}
        )
    }
}

@Preview(name = "Errores claro", showBackground = true, heightDp = 1400, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Errores oscuro", showBackground = true, heightDp = 1400, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun FormularioConErroresPreview(){
    AppThemePreview {
        FormularioContent(
            uiState = FormularioUiState(
                errorNombre = "El nombre es obligatorio",
                errorRut = "Ingresa un rut válido",
                errorCorreo = "Ingresa un correo válido",
                errorContrasena = "Minímo 6 caracteres",
                errorConfirmarContrasena = "Repite la contraseña",
                errorTerminos = "Debes aceptar los términos y condiciones"
            ),
            onNombreCambia = {}, onRutCambia = {}, onCorreoCambia = {},
            onContrasenaCambia = {}, onConfirmarContrasenaCambia = {},
            onAlternarVisibilidad = {}, onTerminosCambia = {},
            onRegistrar = {}, onLimpiar = {}, onAbrirMenu = {}
        )
    }
}
