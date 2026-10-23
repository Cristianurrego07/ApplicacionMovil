package com.example.app.ui.screens

import com.example.app.ui.components.CambiarContrasenaDialog
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import android.content.res.Configuration
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.app.R
import com.example.app.model.Compromiso
import com.example.app.model.Reporte
import com.example.app.model.Usuario
import com.example.app.ui.components.TarjetaEstadistica
import com.example.app.ui.components.TarjetaSeccion
import com.example.app.ui.theme.Primario
import com.example.app.ui.theme.Secundario
import com.example.app.ui.theme.AppThemePreview

import com.example.app.data.ConvertidorImagen
import com.example.app.ui.components.ControlesFotoPerfil
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import com.example.app.validacion.Validaciones
import com.example.app.viewmodel.TarjetasViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardType

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
    tarjetasViewModel: TarjetasViewModel = viewModel(key = "tarjetas_${usuario.id}")
) {
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var cambiandoContrasena by rememberSaveable(usuario.id) { mutableStateOf(false) }
    if (cambiandoContrasena) {
        CambiarContrasenaDialog(
            usuarioId = usuario.id,
            onCerrar = { cambiandoContrasena = false },
            onCambiar = onCambiarContrasena,
            onExito = { scope.launch { snackbar.showSnackbar("Contraseña actualizada correctamente") } }
        )
    }
    val tarjetas by tarjetasViewModel.tarjetas.collectAsStateWithLifecycle()
    var mostrandoDialogoTarjeta by rememberSaveable { mutableStateOf(false) }
    var numTarjeta by remember(usuario.id) { mutableStateOf("") }
    var titTarjeta by rememberSaveable(usuario.nombre) { mutableStateOf(usuario.nombre) }
    var vencTarjeta by rememberSaveable { mutableStateOf("") }
    var cvvTarjeta by remember(usuario.id) { mutableStateOf("") }
    var tipoTarjeta by rememberSaveable { mutableStateOf("Débito") }
    var errorTarjeta by rememberSaveable { mutableStateOf<String?>(null) }

    fun cerrarTarjeta() {
        mostrandoDialogoTarjeta = false
        numTarjeta = ""
        cvvTarjeta = ""
        errorTarjeta = null
    }
    if (mostrandoDialogoTarjeta) {
        AlertDialog(
            onDismissRequest = { cerrarTarjeta() },
            title = { Text("Incorporar tarjeta de crédito o débito") },
            text = {
                Column(modifier = Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = numTarjeta, onValueChange = { numTarjeta = it },
                        label = { Text("Número de tarjeta") }, singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    OutlinedTextField(value = titTarjeta, onValueChange = { titTarjeta = it },
                        label = { Text("Nombre del titular") }, singleLine = true)
                    OutlinedTextField(value = vencTarjeta, onValueChange = { vencTarjeta = it },
                        label = { Text("Vencimiento (MM/AA)") }, singleLine = true)
                    OutlinedTextField(value = cvvTarjeta, onValueChange = { cvvTarjeta = it },
                        label = { Text("CVV") }, singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("Tipo:")
                        TextButton(onClick = { tipoTarjeta = "Débito" }) {
                            Text(if (tipoTarjeta == "Débito") "[Débito]" else "Débito")
                        }
                        TextButton(onClick = { tipoTarjeta = "Crédito" }) {
                            Text(if (tipoTarjeta == "Crédito") "[Crédito]" else "Crédito")
                        }
                    }
                    errorTarjeta?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (numTarjeta.isBlank() || numTarjeta.length < 12 || vencTarjeta.isBlank() || cvvTarjeta.isBlank()) {
                        errorTarjeta = "Completa los datos correctamente (número de al menos 12 dígitos)."
                    } else {
                        tarjetasViewModel.agregar(numTarjeta.trim(), titTarjeta.trim(), vencTarjeta.trim(), tipoTarjeta)
                        cerrarTarjeta()
                        scope.launch { snackbar.showSnackbar("Tarjeta $tipoTarjeta incorporada correctamente") }
                    }
                }) { Text("Incorporar") }
            },
            dismissButton = { TextButton(onClick = { cerrarTarjeta() }) { Text("Cancelar") } }
        )
    }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    var editando by rememberSaveable { mutableStateOf(false) }
    var nombre by rememberSaveable(usuario.nombre) { mutableStateOf(usuario.nombre) }
    var correo by rememberSaveable(usuario.correo) { mutableStateOf(usuario.correo) }
    var rut by rememberSaveable(usuario.rut) { mutableStateOf(usuario.rut) }
    var telefono by rememberSaveable(usuario.telefono) { mutableStateOf(usuario.telefono) }
    var direccion by rememberSaveable(usuario.direccion) { mutableStateOf(usuario.direccion) }
    var ciudad by rememberSaveable(usuario.ciudad) { mutableStateOf(usuario.ciudad) }
    fun editarPerfil() {
        nombre = usuario.nombre
        rut = usuario.rut
        correo = usuario.correo
        telefono = usuario.telefono
        direccion = usuario.direccion
        ciudad = usuario.ciudad
        error = null
        editando = true
    }
    val foto = remember(usuario.avatar) {
        usuario.avatar?.let { ConvertidorImagen.aBitmap(it)?.asImageBitmap() }
    }
    if (editando) {
        AlertDialog(
            onDismissRequest = { editando = false; error = null },
            title = { Text("Editar perfil") },
            text = {
                Column(modifier = Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = nombre, onValueChange = { nombre = it },
                        label = { Text("Nombre completo") }, singleLine = true)
                    OutlinedTextField(value = rut, onValueChange = { rut = it },
                        label = { Text("RUT") }, singleLine = true)
                    OutlinedTextField(value = correo, onValueChange = { correo = it },
                        label = { Text("Correo electrónico") }, singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
                    OutlinedTextField(value = telefono, onValueChange = { telefono = it },
                        label = { Text("Teléfono") }, singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
                    OutlinedTextField(value = direccion, onValueChange = { direccion = it },
                        label = { Text("Dirección") }, singleLine = true)
                    OutlinedTextField(value = ciudad, onValueChange = { ciudad = it },
                        label = { Text("Ciudad") }, singleLine = true)
                    error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val nombreLimpio = nombre.trim()
                    val correoLimpio = correo.trim().lowercase()
                    val conservaRut = Validaciones.normalizarRut(rut) == Validaciones.normalizarRut(usuario.rut)
                    if (nombreLimpio.isBlank()) {
                        error = "Ingresa tu nombre completo."
                    } else if ((!conservaRut && !Validaciones.rutValido(rut)) || rut.isBlank()) {
                        error = "Ingresa un RUT válido, incluyendo su dígito verificador."
                    } else if (!Validaciones.correoValido(correoLimpio)) {
                        error = "Ingresa un correo electrónico válido."
                    } else if (onGuardarPerfil(usuario.copy(nombre = nombreLimpio, rut = Validaciones.formatearRut(rut), correo = correoLimpio,
                        telefono = telefono.trim(), direccion = direccion.trim(), ciudad = ciudad.trim()))) {
                        editando = false
                        error = null
                    } else {
                        error = "No se pudo guardar. El RUT o el correo podrían estar en uso."
                    }
                }) { Text("Guardar") }
            },
            dismissButton = { TextButton(onClick = { editando = false; error = null }) { Text("Cancelar") } }
        )
    }
    val esAgente = usuario.rolNombre == "AGENTE"
    val pendientes = compromisos.count {
        it.estado == "Pendiente" || it.estado == "Vencido"
    }
    val reportesActivos = reportes.count { it.estado != "Resuelto" }
    val iniciales = usuario.nombre.trim().split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercase() }
        .joinToString("")

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Mi perfil") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = "Volver",
                            tint = Secundario
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
                        .border(4.dp, MaterialTheme.colorScheme.surface, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (foto != null) {
                        Image(bitmap = foto, contentDescription = "Foto de perfil",
                            contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    } else Text(
                        text = iniciales,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(52.dp))
            if (usuario.rolNombre == "CLIENTE" || esAgente) {
                ControlesFotoPerfil(snackbar = snackbar, onGuardarFoto = onGuardarFoto)
                if (!editando) error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
                }
                TarjetaSeccion(
                    titulo = "Información de perfil",
                    modifier = Modifier.padding(horizontal = 12.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(if (esAgente) "Agente" else "Cliente", modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.titleSmall)
                        IconButton(onClick = { editarPerfil() }) {
                            Icon(painterResource(R.drawable.ic_edit), contentDescription = "Editar perfil",
                                tint = MaterialTheme.colorScheme.primary)
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
                Text(usuario.nombre, style = MaterialTheme.typography.titleLarge)
                Text("${usuario.rolNombre} · ${usuario.correo}", color = Secundario)
                Text("RUT: ${usuario.rut}", style = MaterialTheme.typography.bodySmall)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (esAgente) {
                    TarjetaEstadistica("${reportes.size}", "Reportes", Modifier.weight(1f))
                    TarjetaEstadistica("$reportesActivos", "Pendientes", Modifier.weight(1f))
                    TarjetaEstadistica(
                        "${reportes.count { it.estado == "Resuelto" }}",
                        "Resueltos",
                        Modifier.weight(1f)
                    )
                } else {
                    TarjetaEstadistica(compromisos.size.toString(), "Servicios", Modifier.weight(1f))
                    TarjetaEstadistica(pendientes.toString(), "Pendientes", Modifier.weight(1f))
                    TarjetaEstadistica(cantidadPagos.toString(), "Pagos", Modifier.weight(1f))
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (usuario.rolNombre == "CLIENTE" || esAgente) {
                    TarjetaSeccion(titulo = "Seguridad y contraseña") {
                        Text("Actualiza la contraseña de tu cuenta.")
                        TextButton(onClick = { cambiandoContrasena = true }) {
                            Text("Cambiar contraseña")
                        }
                    }
                }
                if (usuario.rolNombre == "CLIENTE") {
                    TarjetaSeccion(titulo = "Tarjetas de crédito y débito") {
                        if (tarjetas.isEmpty()) {
                            Text("No tienes tarjetas incorporadas.", style = MaterialTheme.typography.bodyMedium)
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                tarjetas.forEach { t ->
                                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                        Text("${t.tipo}: •••• ${t.ultimosCuatro} (${t.vencimiento})",
                                            style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                                        TextButton(onClick = { tarjetasViewModel.eliminar(t) }) {
                                            Text("Eliminar", color = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        TextButton(onClick = {
                            numTarjeta = ""
                            titTarjeta = usuario.nombre
                            vencTarjeta = ""
                            cvvTarjeta = ""
                            errorTarjeta = null
                            mostrandoDialogoTarjeta = true
                        }) {
                            Text("+ Incorporar tarjeta de crédito o débito")
                        }
                    }
                }
                TarjetaSeccion(
                    titulo = "Sobre mí",
                    modifier = Modifier.heightIn(min = 112.dp)
                ) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        if (esAgente) "Soy agente de atención y ayudo a resolver los reportes de PagoFlex."
                        else "Soy cliente frecuente y me gusta pagar al día.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                TarjetaSeccion(
                    titulo = if (esAgente) "Mi actividad" else "Mis compromisos",
                    modifier = Modifier.heightIn(min = 120.dp)
                ) {
                    Spacer(Modifier.height(8.dp))
                    val nombres = compromisos.map { it.empresa }.distinct()
                    if (esAgente) {
                        Text("Reportes en proceso: ${reportes.count { it.estado == "En proceso" }}")
                    } else if (nombres.isEmpty()) {
                        Text("Todavía no tienes compromisos registrados.")
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            nombres.take(3).forEach { nombre ->
                                Text(
                                    text = nombre,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun DatoPerfil(etiqueta: String, valor: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(etiqueta, modifier = Modifier.weight(0.42f),
            color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium)
        Text(valor.ifBlank { "Sin registrar" }, modifier = Modifier.weight(0.58f),
            textAlign = TextAlign.End, style = MaterialTheme.typography.bodyMedium)
    }
}

@Preview(name = "Perfil claro", showBackground = true, showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Perfil oscuro", showBackground = true, showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PerfilPreview() {
    AppThemePreview {
        PerfilScreen(
            usuario = Usuario(
                id = 1,
                rut = "20.435.214-5",
                nombre = "Sofía Pérez",
                correo = "sofia@gmail.com",
                rolId = 1,
                rolNombre = "CLIENTE"
            ),
            compromisos = listOf(
                Compromiso(
                    folio = "PF-001",
                    empresa = "Universidad",
                    usuario = Usuario(1, "20.435.214-5", "Sofía Pérez", "sofia@gmail.com", 1, "CLIENTE"),
                    concepto = "Cuota mensual",
                    monto = 350000,
                    vencimiento = "2026-10-15"
                ),
                Compromiso(
                    folio = "PF-002",
                    empresa = "Enel",
                    usuario = Usuario(1, "20.435.214-5", "Sofía Pérez", "sofia@gmail.com", 1, "CLIENTE"),
                    concepto = "Electricidad",
                    monto = 28000,
                    vencimiento = "2026-10-20"
                )
            ),
            cantidadPagos = 1,
            onVolver = {},
            tarjetasViewModel = remember { TarjetasViewModel(SavedStateHandle()) }
        )
    }
}
