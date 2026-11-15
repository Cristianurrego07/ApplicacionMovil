package com.example.app.ui.screens

import androidx.compose.foundation.clickable
import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.app.R
import com.example.app.data.ConvertidorImagen
import com.example.app.model.Compromiso
import com.example.app.model.Reporte
import com.example.app.model.Usuario
import com.example.app.ui.components.ConfiguracionNotificaciones
import com.example.app.ui.components.CambiarContrasenaDialog
import com.example.app.ui.components.ControlesFotoPerfil
import com.example.app.ui.components.TarjetaEstadistica
import com.example.app.ui.components.TarjetaSeccion
import com.example.app.ui.theme.AppThemePreview
import com.example.app.ui.theme.Primario
import com.example.app.ui.theme.Secundario
import com.example.app.viewmodel.PerfilViewModel
import com.example.app.viewmodel.TarjetasViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(
    usuario: Usuario,
    compromisos: List<Compromiso>,
    cantidadPagos: Int,
    onVolver: () -> Unit,
    reportes: List<Reporte> = emptyList(),
    onGuardarPerfil: (Usuario) -> Boolean = { false },
    onGuardarFoto: suspend (ByteArray) -> Boolean = { false },
    onCambiarContrasena: (String, String) -> Boolean = { _, _ -> false },
    tarjetasViewModel: TarjetasViewModel = viewModel(
        key = "tarjetas_${usuario.id}"
    )
) {
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val perfilViewModel: PerfilViewModel = viewModel(
        key = "perfil_${usuario.id}"
    )

    val perfilEstado by perfilViewModel.uiState.collectAsStateWithLifecycle()
    val tarjetas by tarjetasViewModel.tarjetas.collectAsStateWithLifecycle()

    var editando by rememberSaveable(usuario.id) {
        mutableStateOf(false)
    }

    var cambiandoContrasena by rememberSaveable(usuario.id) {
        mutableStateOf(false)
    }

    var mostrandoDialogoTarjeta by rememberSaveable {
        mutableStateOf(false)
    }

    var numTarjeta by remember(usuario.id) {
        mutableStateOf("")
    }

    var titTarjeta by rememberSaveable(usuario.nombre) {
        mutableStateOf(usuario.nombre)
    }

    var vencTarjeta by rememberSaveable {
        mutableStateOf("")
    }

    var cvvTarjeta by remember(usuario.id) {
        mutableStateOf("")
    }

    var tipoTarjeta by rememberSaveable {
        mutableStateOf("Débito")
    }

    var errorTarjeta by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    fun editarPerfil() {
        perfilViewModel.cargarUsuario(usuario)
        editando = true
    }

    fun cerrarTarjeta() {
        mostrandoDialogoTarjeta = false
        numTarjeta = ""
        cvvTarjeta = ""
        errorTarjeta = null
    }

    val foto = remember(usuario.avatar) {
        usuario.avatar?.let { imagen ->
            ConvertidorImagen.aBitmap(imagen)?.asImageBitmap()
        }
    }

    val esAgente = usuario.rolNombre == "AGENTE"

    val pendientes = compromisos.count {
        it.estado == "Pendiente" || it.estado == "Vencido"
    }

    val reportesActivos = reportes.count {
        it.estado != "Resuelto"
    }

    val iniciales = usuario.nombre
        .trim()
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercase() }
        .joinToString("")

    // Diálogo para cambiar contraseña
    if (cambiandoContrasena) {
        CambiarContrasenaDialog(
            usuarioId = usuario.id,
            onCerrar = {
                cambiandoContrasena = false
            },
            onCambiar = onCambiarContrasena,
            onExito = {
                scope.launch {
                    snackbar.showSnackbar(
                        "Contraseña actualizada correctamente"
                    )
                }
            }
        )
    }

    // Diálogo para incorporar una tarjeta
    if (mostrandoDialogoTarjeta) {
        AlertDialog(
            onDismissRequest = {
                cerrarTarjeta()
            },
            title = {
                Text(text = "Incorporar tarjeta de crédito o débito")
            },
            text = {
                Column(
                    modifier = Modifier
                        .heightIn(max = 420.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = numTarjeta,
                        onValueChange = { numTarjeta = it },
                        label = { Text("Número de tarjeta") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        )
                    )

                    OutlinedTextField(
                        value = titTarjeta,
                        onValueChange = { titTarjeta = it },
                        label = { Text("Nombre del titular") },
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = vencTarjeta,
                        onValueChange = { vencTarjeta = it },
                        label = { Text("Vencimiento (MM/AA)") },
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = cvvTarjeta,
                        onValueChange = { cvvTarjeta = it },
                        label = { Text("CVV") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        )
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Tipo:")

                        TextButton(
                            onClick = { tipoTarjeta = "Débito" }
                        ) {
                            Text(
                                text = if (tipoTarjeta == "Débito") {
                                    "[Débito]"
                                } else {
                                    "Débito"
                                }
                            )
                        }

                        TextButton(
                            onClick = { tipoTarjeta = "Crédito" }
                        ) {
                            Text(
                                text = if (tipoTarjeta == "Crédito") {
                                    "[Crédito]"
                                } else {
                                    "Crédito"
                                }
                            )
                        }
                    }

                    errorTarjeta?.let { mensaje ->
                        Text(
                            text = mensaje,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (
                            numTarjeta.isBlank() ||
                            numTarjeta.length < 12 ||
                            vencTarjeta.isBlank() ||
                            cvvTarjeta.isBlank()
                        ) {
                            errorTarjeta =
                                "Completa los datos correctamente (número de al menos 12 dígitos)."
                        } else {
                            tarjetasViewModel.agregar(
                                numTarjeta.trim(),
                                titTarjeta.trim(),
                                vencTarjeta.trim(),
                                tipoTarjeta
                            )

                            cerrarTarjeta()

                            scope.launch {
                                snackbar.showSnackbar(
                                    "Tarjeta $tipoTarjeta incorporada correctamente"
                                )
                            }
                        }
                    }
                ) {
                    Text(text = "Incorporar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { cerrarTarjeta() }
                ) {
                    Text(text = "Cancelar")
                }
            }
        )
    }

    // Diálogo para editar los datos del perfil
    if (editando) {
        AlertDialog(
            onDismissRequest = {
                editando = false
            },
            title = {
                Text(text = "Editar perfil")
            },
            text = {
                Column(
                    modifier = Modifier
                        .heightIn(max = 420.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = perfilEstado.nombre,
                        onValueChange = perfilViewModel::onNombreCambia,
                        label = { Text("Nombre completo") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = perfilEstado.errorNombre != null,
                        supportingText = {
                            perfilEstado.errorNombre?.let { mensaje ->
                                Text(text = mensaje)
                            }
                        }
                    )

                    OutlinedTextField(
                        value = perfilEstado.rut,
                        onValueChange = perfilViewModel::onRutCambia,
                        label = { Text("RUT") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = perfilEstado.errorRut != null,
                        supportingText = {
                            perfilEstado.errorRut?.let { mensaje ->
                                Text(text = mensaje)
                            }
                        }
                    )

                    OutlinedTextField(
                        value = perfilEstado.correo,
                        onValueChange = perfilViewModel::onCorreoCambia,
                        label = { Text("Correo electrónico") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email
                        ),
                        isError = perfilEstado.errorCorreo != null,
                        supportingText = {
                            perfilEstado.errorCorreo?.let { mensaje ->
                                Text(text = mensaje)
                            }
                        }
                    )

                    OutlinedTextField(
                        value = perfilEstado.telefono,
                        onValueChange = perfilViewModel::onTelefonoCambia,
                        label = { Text("Teléfono") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone
                        ),
                        isError = perfilEstado.errorTelefono != null,
                        supportingText = {
                            perfilEstado.errorTelefono?.let { mensaje ->
                                Text(text = mensaje)
                            }
                        }
                    )

                    OutlinedTextField(
                        value = perfilEstado.direccion,
                        onValueChange = perfilViewModel::onDireccionCambia,
                        label = { Text("Dirección") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = perfilEstado.errorDireccion != null,
                        supportingText = {
                            perfilEstado.errorDireccion?.let { mensaje ->
                                Text(text = mensaje)
                            }
                        }
                    )

                    OutlinedTextField(
                        value = perfilEstado.ciudad,
                        onValueChange = perfilViewModel::onCiudadCambia,
                        label = { Text("Ciudad") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = perfilEstado.errorCiudad != null,
                        supportingText = {
                            perfilEstado.errorCiudad?.let { mensaje ->
                                Text(text = mensaje)
                            }
                        }
                    )

                    perfilEstado.errorGuardado?.let { mensaje ->
                        Text(
                            text = mensaje,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val guardado = perfilViewModel.guardar(
                            usuario = usuario,
                            onGuardarPerfil = onGuardarPerfil
                        )

                        if (guardado) {
                            editando = false
                        }
                    }
                ) {
                    Text(text = "Guardar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        editando = false
                    }
                ) {
                    Text(text = "Cancelar")
                }
            }
        )
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbar)
        },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(text = "Mi perfil",
                        color = MaterialTheme.colorScheme.secondary)
                },
                navigationIcon = {
                    androidx.compose.foundation.layout.Box(
                            modifier = androidx.compose.ui.Modifier
                                .size(48.dp)
                                .clickable(
                                    interactionSource = androidx.compose.runtime.remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                    indication = null,
                                    role = androidx.compose.ui.semantics.Role.Button,
                                    onClick = onVolver
                                ),
                            contentAlignment = androidx.compose.ui.Alignment.Center
                        ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors()
            )
        }
    ) { espacio ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(espacio)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Portada y avatar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(148.dp)
                    .background(Primario),
                contentAlignment = Alignment.TopCenter
            ) {
                Image(
                    painter = painterResource(R.drawable.portada_perfil),
                    contentDescription = "Imagen de portada del perfil",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = 42.dp)
                        .size(92.dp)
                        .clip(CircleShape)
                        .background(Secundario)
                        .border(
                            width = 4.dp,
                            color = MaterialTheme.colorScheme.surface,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (foto != null) {
                        Image(
                            bitmap = foto,
                            contentDescription = "Foto de perfil",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text(
                            text = iniciales,
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(52.dp))

            if (usuario.rolNombre == "CLIENTE" || esAgente) {
                ControlesFotoPerfil(
                    snackbar = snackbar,
                    onGuardarFoto = onGuardarFoto
                )

                TarjetaSeccion(
                    titulo = "Información de perfil",
                    modifier = Modifier.padding(horizontal = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (esAgente) "Agente" else "Cliente",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.titleSmall
                        )

                        androidx.compose.foundation.layout.Box(
                            modifier = androidx.compose.ui.Modifier
                                .size(48.dp)
                                .clickable(
                                    interactionSource = androidx.compose.runtime.remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                    indication = null,
                                    role = androidx.compose.ui.semantics.Role.Button,
                                    onClick = { editarPerfil() }
                                ),
                            contentAlignment = androidx.compose.ui.Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_edit),
                                contentDescription = "Editar perfil",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    DatoPerfil("Nombre completo", usuario.nombre)
                    DatoPerfil("RUT", usuario.rut)
                    DatoPerfil("Correo", usuario.correo)
                    DatoPerfil("Teléfono", usuario.telefono)
                    DatoPerfil("Dirección", usuario.direccion)
                    DatoPerfil("Ciudad", usuario.ciudad)
                }
            } else {
                Text(
                    text = usuario.nombre,
                    style = MaterialTheme.typography.titleLarge
                )

                Text(
                    text = "${usuario.rolNombre} · ${usuario.correo}",
                    color = Secundario
                )

                Text(
                    text = "RUT: ${usuario.rut}",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Estadísticas
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (esAgente) {
                    TarjetaEstadistica(
                        "${reportes.size}",
                        "Reportes",
                        Modifier.weight(1f)
                    )

                    TarjetaEstadistica(
                        "$reportesActivos",
                        "Pendientes",
                        Modifier.weight(1f)
                    )

                    TarjetaEstadistica(
                        "${reportes.count { it.estado == "Resuelto" }}",
                        "Resueltos",
                        Modifier.weight(1f)
                    )
                } else {
                    TarjetaEstadistica(
                        compromisos.size.toString(),
                        "Servicios",
                        Modifier.weight(1f)
                    )

                    TarjetaEstadistica(
                        pendientes.toString(),
                        "Pendientes",
                        Modifier.weight(1f)
                    )

                    TarjetaEstadistica(
                        cantidadPagos.toString(),
                        "Pagos",
                        Modifier.weight(1f)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (usuario.rolNombre == "CLIENTE" || esAgente) {
                    TarjetaSeccion(
                        titulo = "Seguridad y contraseña"
                    ) {
                        Text(text = "Actualiza la contraseña de tu cuenta.")

                        TextButton(
                            onClick = { cambiandoContrasena = true }
                        ) {
                            Text(text = "Cambiar contraseña")
                        }
                    }
                }

                if (usuario.rolNombre == "CLIENTE" || esAgente) {
                    TarjetaSeccion(titulo = "Notificaciones") {
                        ConfiguracionNotificaciones(usuarioId = usuario.id)
                    }
                }

                if (usuario.rolNombre == "CLIENTE") {
                    TarjetaSeccion(
                        titulo = "Tarjetas de crédito y débito"
                    ) {
                        if (tarjetas.isEmpty()) {
                            Text(
                                text = "No tienes tarjetas incorporadas.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        } else {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                tarjetas.forEach { tarjeta ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${tarjeta.tipo}: •••• ${tarjeta.ultimosCuatro} (${tarjeta.vencimiento})",
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.weight(1f)
                                        )

                                        TextButton(
                                            onClick = {
                                                tarjetasViewModel.eliminar(tarjeta)
                                            }
                                        ) {
                                            Text(
                                                text = "Eliminar",
                                                color = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        TextButton(
                            onClick = {
                                numTarjeta = ""
                                titTarjeta = usuario.nombre
                                vencTarjeta = ""
                                cvvTarjeta = ""
                                errorTarjeta = null
                                mostrandoDialogoTarjeta = true
                            }
                        ) {
                            Text(text = "+ Incorporar tarjeta de crédito o débito")
                        }
                    }
                }

                TarjetaSeccion(
                    titulo = "Sobre mí",
                    modifier = Modifier.heightIn(min = 112.dp)
                ) {
                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (esAgente) {
                            "Soy agente de atención y ayudo a resolver los reportes de PagoFlex."
                        } else {
                            "Soy cliente frecuente y me gusta pagar al día."
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                TarjetaSeccion(
                    titulo = if (esAgente) "Mi actividad" else "Mis compromisos",
                    modifier = Modifier.heightIn(min = 120.dp)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    val nombres = compromisos
                        .map { it.empresa }
                        .distinct()

                    if (esAgente) {
                        Text(
                            text = "Reportes en proceso: ${
                                reportes.count { it.estado == "En proceso" }
                            }"
                        )
                    } else if (nombres.isEmpty()) {
                        Text(
                            text = "Todavía no tienes compromisos registrados."
                        )
                    } else {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            nombres.take(3).forEach { nombre ->
                                Text(
                                    text = nombre,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(
                                            MaterialTheme.colorScheme.primaryContainer
                                        )
                                        .padding(
                                            horizontal = 10.dp,
                                            vertical = 6.dp
                                        ),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun DatoPerfil(
    etiqueta: String,
    valor: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = etiqueta,
            modifier = Modifier.weight(0.42f),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.bodyMedium
        )

        Text(
            text = valor.ifBlank { "Sin registrar" },
            modifier = Modifier.weight(0.58f),
            textAlign = TextAlign.End,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Preview(
    name = "Perfil claro",
    showBackground = true,
    showSystemUi = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "Perfil oscuro",
    showBackground = true,
    showSystemUi = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun PerfilPreview() {
    AppThemePreview {
        val usuario = Usuario(
            id = 1,
            rut = "20.435.214-5",
            nombre = "Sofía Pérez",
            correo = "sofia@gmail.com",
            rolId = 1,
            rolNombre = "CLIENTE"
        )

        val compromisos = listOf(
            Compromiso(
                folio = "PF-001",
                empresa = "Universidad",
                usuario = usuario,
                concepto = "Cuota mensual",
                monto = 350000,
                vencimiento = "2026-10-15"
            ),
            Compromiso(
                folio = "PF-002",
                empresa = "Enel",
                usuario = usuario,
                concepto = "Electricidad",
                monto = 28000,
                vencimiento = "2026-10-20"
            )
        )

        PerfilScreen(
            usuario = usuario,
            compromisos = compromisos,
            cantidadPagos = 1,
            onVolver = {},
            tarjetasViewModel = remember {
                TarjetasViewModel(SavedStateHandle())
            }
        )
    }
}

@Preview(name = "Notificaciones - Cliente", showBackground = true)
@Composable
private fun NotificacionesClientePreview() {
    AppThemePreview {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Perfil de cliente", style = MaterialTheme.typography.titleMedium)
            TarjetaSeccion(titulo = "Notificaciones") {
                ConfiguracionNotificaciones(usuarioId = 1L)
            }
        }
    }
}

@Preview(name = "Notificaciones - Agente", showBackground = true)
@Composable
private fun NotificacionesAgentePreview() {
    AppThemePreview {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Perfil de agente", style = MaterialTheme.typography.titleMedium)
            TarjetaSeccion(titulo = "Notificaciones") {
                ConfiguracionNotificaciones(usuarioId = 2L)
            }
        }
    }
}
