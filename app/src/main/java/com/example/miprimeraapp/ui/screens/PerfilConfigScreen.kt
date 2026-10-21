package com.example.miprimeraapp.ui.screens

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.miprimeraapp.R
import com.example.miprimeraapp.data.ConvertidorImagen
import com.example.miprimeraapp.data.CorreoNotificador
import com.example.miprimeraapp.data.UsuarioDbHelper
import com.example.miprimeraapp.model.Usuario
import com.example.miprimeraapp.permisos.Permisos
import com.example.miprimeraapp.ui.components.AvatarCircular
import com.example.miprimeraapp.ui.theme.PagoFlexPurple
import com.example.miprimeraapp.ui.theme.ThemeManager
import kotlinx.coroutines.launch

data class MetodoPago(
    val tipo: String,
    val banco: String,
    val numero: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilConfigScreen(
    usuario: Usuario?,
    onVolver: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val db = remember { UsuarioDbHelper(context) }

    var usuarioActual by remember { mutableStateOf(usuario) }
    var modoEdicion by remember { mutableStateOf(false) }

    // Campos del perfil
    var editNombre by remember(usuarioActual) { mutableStateOf(usuarioActual?.nombre ?: "") }
    var editApellido by remember(usuarioActual) { mutableStateOf(usuarioActual?.apellido ?: "") }
    var editRut by remember(usuarioActual) { mutableStateOf(usuarioActual?.rut ?: "") }
    var editCorreo by remember(usuarioActual) { mutableStateOf(usuarioActual?.correo ?: "") }
    var editTelefono by remember(usuarioActual) { mutableStateOf(usuarioActual?.telefono ?: "") }
    var editDireccion by remember(usuarioActual) { mutableStateOf(usuarioActual?.direccion ?: "") }
    var editCiudad by remember(usuarioActual) { mutableStateOf(usuarioActual?.ciudad ?: "") }

    // Cambiar clave
    var mostrarDialogoClave by remember { mutableStateOf(false) }
    var claveActualInput by remember { mutableStateOf("") }
    var claveNuevaInput by remember { mutableStateOf("") }
    var claveConfirmarInput by remember { mutableStateOf("") }
    var errorClave by remember { mutableStateOf<String?>(null) }

    var mostrarDialogoFoto by remember { mutableStateOf(false) }

    val textoCamaraDenegada = stringResource(R.string.permiso_camara_denegado)
    val textoGaleriaDenegada = stringResource(R.string.permiso_galeria_denegado)
    val textoAjustes = stringResource(R.string.btn_ajustes)

    fun avisarPermisoDenegado(mensaje: String) {
        scope.launch {
            val resultado = snackbarHostState.showSnackbar(
                message = mensaje,
                actionLabel = textoAjustes,
                duration = SnackbarDuration.Long
            )
            if (resultado == SnackbarResult.ActionPerformed) Permisos.abrirAjustes(context)
        }
    }

    val lanzadorCamara = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { fotoBitmap: Bitmap? ->
        if (fotoBitmap != null && usuarioActual != null) {
            val bytes = ConvertidorImagen.aBytes(fotoBitmap)
            db.actualizarAvatar(usuarioActual!!.id, bytes)
            usuarioActual = db.buscarPorId(usuarioActual!!.id)
            scope.launch {
                snackbarHostState.showSnackbar("¡Imagen de perfil guardada exitosamente en la Base de Datos!")
            }
        }
    }

    val pedirPermisoCamara = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            try { lanzadorCamara.launch(null) } catch (e: Exception) {}
        } else {
            avisarPermisoDenegado(textoCamaraDenegada)
        }
    }

    val lanzadorGaleria = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { resultado ->
        val uri = resultado.data?.data
        if (resultado.resultCode == Activity.RESULT_OK && uri != null && usuarioActual != null) {
            val bitmap = ConvertidorImagen.desdeUri(context, uri)
            if (bitmap != null) {
                val bytes = ConvertidorImagen.aBytes(bitmap)
                db.actualizarAvatar(usuarioActual!!.id, bytes)
                usuarioActual = db.buscarPorId(usuarioActual!!.id)
                scope.launch {
                    snackbarHostState.showSnackbar("¡Imagen de perfil guardada exitosamente en la Base de Datos!")
                }
            }
        }
    }

    val pedirPermisoImagenes = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            try { lanzadorGaleria.launch(intent) } catch (e: Exception) {}
        } else {
            avisarPermisoDenegado(textoGaleriaDenegada)
        }
    }

    val foto = remember(usuarioActual?.avatar) {
        usuarioActual?.avatar?.let { bytes -> ConvertidorImagen.aBitmap(bytes)?.asImageBitmap() }
    }

    val metodosPago = remember {
        mutableStateListOf(
            MetodoPago("Débito", "BancoEstado", "•••• 4589"),
            MetodoPago("Crédito", "Santander", "•••• 1234")
        )
    }

    var mostrarDialogoPago by remember { mutableStateOf(false) }
    var tipoTarjetaSeleccionada by remember { mutableStateOf("Débito") }
    var bancoInput by remember { mutableStateOf("") }
    var numeroInput by remember { mutableStateOf("") }
    var expandidoTipo by remember { mutableStateOf(false) }

    val tiposTarjetaOpciones = listOf("Débito", "Crédito", "Prepago", "Cuenta Corriente")

    if (mostrarDialogoFoto) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoFoto = false },
            title = { Text(stringResource(R.string.titulo_perfil)) },
            text = { Text(stringResource(R.string.texto_cambiar_foto)) },
            confirmButton = {
                TextButton(onClick = {
                    mostrarDialogoFoto = false
                    if (usuarioActual != null) {
                        if (Permisos.tiene(context, Permisos.CAMARA)) {
                            try { lanzadorCamara.launch(null) } catch (e: Exception) {}
                        } else {
                            pedirPermisoCamara.launch(Permisos.CAMARA)
                        }
                    }
                }) {
                    Text(stringResource(R.string.btn_camara))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    mostrarDialogoFoto = false
                    if (usuarioActual != null) {
                        if (Permisos.tiene(context, Permisos.IMAGENES)) {
                            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                            try { lanzadorGaleria.launch(intent) } catch (e: Exception) {}
                        } else {
                            pedirPermisoImagenes.launch(Permisos.IMAGENES)
                        }
                    }
                }) {
                    Text(stringResource(R.string.btn_galeria))
                }
            }
        )
    }

    if (mostrarDialogoClave) {
        AlertDialog(
            onDismissRequest = {
                mostrarDialogoClave = false
                claveActualInput = ""
                claveNuevaInput = ""
                claveConfirmarInput = ""
                errorClave = null
            },
            title = { Text("Cambiar Contraseña") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Ingresa tu contraseña actual y la nueva contraseña para actualizarla:")

                    OutlinedTextField(
                        value = claveActualInput,
                        onValueChange = { claveActualInput = it },
                        label = { Text("Contraseña Actual") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = claveNuevaInput,
                        onValueChange = { claveNuevaInput = it },
                        label = { Text("Nueva Contraseña") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = claveConfirmarInput,
                        onValueChange = { claveConfirmarInput = it },
                        label = { Text("Confirmar Nueva Contraseña") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    errorClave?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val u = usuarioActual
                        if (u != null) {
                            if (claveActualInput != u.contrasena) {
                                errorClave = "La contraseña actual no es correcta."
                            } else if (claveNuevaInput.length < 4) {
                                errorClave = "La nueva contraseña debe tener al menos 4 caracteres."
                            } else if (claveNuevaInput != claveConfirmarInput) {
                                errorClave = "Las contraseñas no coinciden."
                            } else {
                                val exito = db.actualizarContrasena(u.id, claveNuevaInput)
                                if (exito) {
                                    usuarioActual = db.buscarPorId(u.id)
                                    mostrarDialogoClave = false
                                    claveActualInput = ""
                                    claveNuevaInput = ""
                                    claveConfirmarInput = ""
                                    errorClave = null

                                    scope.launch {
                                        snackbarHostState.showSnackbar("¡Contraseña actualizada con éxito en la Base de Datos!")
                                    }

                                    CorreoNotificador.enviarCorreo(
                                        context = context,
                                        destinatario = u.correo,
                                        asunto = "Seguridad PagoFlex - Cambio de Contraseña",
                                        cuerpo = "Hola ${u.nombre},\n\nTe informamos que tu contraseña ha sido modificada exitosamente en PagoFlex.\n\nAtentamente,\nEquipo PagoFlex"
                                    )
                                } else {
                                    errorClave = "No se pudo actualizar la contraseña."
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PagoFlexPurple)
                ) {
                    Text("Guardar Contraseña")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    mostrarDialogoClave = false
                    errorClave = null
                }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (mostrarDialogoPago) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoPago = false },
            title = { Text("Agregar Método de Pago") },
            text = {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .imePadding(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Selecciona el tipo de tarjeta y los datos:")

                    ExposedDropdownMenuBox(
                        expanded = expandidoTipo,
                        onExpandedChange = { expandidoTipo = !expandidoTipo }
                    ) {
                        OutlinedTextField(
                            value = tipoTarjetaSeleccionada,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Tipo de Tarjeta") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandidoTipo) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
                        )
                        ExposedDropdownMenu(
                            expanded = expandidoTipo,
                            onDismissRequest = { expandidoTipo = false }
                        ) {
                            tiposTarjetaOpciones.forEach { opcion ->
                                DropdownMenuItem(
                                    text = { Text(opcion) },
                                    onClick = {
                                        tipoTarjetaSeleccionada = opcion
                                        expandidoTipo = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = bancoInput,
                        onValueChange = { bancoInput = it },
                        label = { Text("Banco / Emisor (ej. BCI, Chile)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = numeroInput,
                        onValueChange = { numeroInput = it },
                        label = { Text("Últimos 4 dígitos (ej. 9876)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (bancoInput.isNotBlank() && numeroInput.isNotBlank()) {
                            metodosPago.add(
                                MetodoPago(
                                    tipo = tipoTarjetaSeleccionada,
                                    banco = bancoInput,
                                    numero = "•••• $numeroInput"
                                )
                            )
                            bancoInput = ""
                            numeroInput = ""
                            mostrarDialogoPago = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PagoFlexPurple)
                ) {
                    Text("Agregar")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoPago = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil y Configuración") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            painter = painterResource(R.drawable.ic_volver),
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { ThemeManager.isDark = !ThemeManager.isDark }) {
                        Icon(
                            imageVector = if (ThemeManager.isDark) Icons.Default.WbSunny else Icons.Default.DarkMode,
                            contentDescription = "Cambiar Tema",
                            tint = if (ThemeManager.isDark) MaterialTheme.colorScheme.tertiary else PagoFlexPurple
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .imePadding()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(PagoFlexPurple.copy(alpha = 0.2f))
                            .clickable { mostrarDialogoFoto = true },
                        contentAlignment = Alignment.Center
                    ) {
                        AvatarCircular(
                            imagen = R.drawable.avatar_usuario,
                            descripcion = stringResource(R.string.cd_avatar),
                            foto = foto
                        )
                    }
                    Text(
                        text = "📷 Cambiar foto de perfil (se guarda en BD)",
                        style = MaterialTheme.typography.bodySmall,
                        color = PagoFlexPurple,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { mostrarDialogoFoto = true }
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Información de Perfil (${usuarioActual?.nombreRol ?: "Usuario"})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { modoEdicion = !modoEdicion }) {
                            Icon(
                                imageVector = if (modoEdicion) Icons.Default.Close else Icons.Default.Edit,
                                contentDescription = "Editar perfil",
                                tint = PagoFlexPurple
                            )
                        }
                    }

                    if (modoEdicion) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = editNombre,
                                onValueChange = { editNombre = it },
                                label = { Text("Nombre") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = editApellido,
                                onValueChange = { editApellido = it },
                                label = { Text("Apellido") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = editRut,
                                onValueChange = { editRut = it },
                                label = { Text("RUT") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = editCorreo,
                                onValueChange = { editCorreo = it },
                                label = { Text("Correo Electrónico") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = editTelefono,
                                onValueChange = { editTelefono = it },
                                label = { Text("Teléfono") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = editDireccion,
                                onValueChange = { editDireccion = it },
                                label = { Text("Dirección") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = editCiudad,
                                onValueChange = { editCiudad = it },
                                label = { Text("Ciudad") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Button(
                                onClick = {
                                    val u = usuarioActual
                                    if (u != null && editNombre.isNotBlank() && editCorreo.isNotBlank()) {
                                        val actualizado = u.copy(
                                            nombre = editNombre.trim(),
                                            apellido = editApellido.trim(),
                                            rut = editRut.trim(),
                                            correo = editCorreo.trim(),
                                            telefono = editTelefono.trim(),
                                            direccion = editDireccion.trim(),
                                            ciudad = editCiudad.trim()
                                        )
                                        val filas = db.actualizarUsuario(actualizado)
                                        if (filas > 0) {
                                            usuarioActual = db.buscarPorId(u.id)
                                            modoEdicion = false
                                            scope.launch {
                                                snackbarHostState.showSnackbar("¡Perfil actualizado con éxito en la Base de Datos!")
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = PagoFlexPurple)
                            ) {
                                Text("Guardar Cambios")
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ProfileInfoRow(label = "Nombre Completo", value = "${usuarioActual?.nombre ?: ""} ${usuarioActual?.apellido ?: ""}".trim())
                            ProfileInfoRow(label = "RUT", value = usuarioActual?.rut ?: "12.345.678-9")
                            ProfileInfoRow(label = "Correo", value = usuarioActual?.correo ?: "")
                            ProfileInfoRow(label = "Teléfono", value = usuarioActual?.telefono ?: "")
                            ProfileInfoRow(label = "Dirección", value = usuarioActual?.direccion ?: "")
                            ProfileInfoRow(label = "Ciudad", value = usuarioActual?.ciudad ?: "Santiago")
                            ProfileInfoRow(label = "Rol de Sistema", value = usuarioActual?.nombreRol ?: "Usuario")
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = PagoFlexPurple)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Seguridad y Contraseña", style = MaterialTheme.typography.titleMedium)
                            Text("Cambia tu clave y recibe alertas", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Button(
                        onClick = { mostrarDialogoClave = true },
                        colors = ButtonDefaults.buttonColors(containerColor = PagoFlexPurple)
                    ) {
                        Text("Cambiar")
                    }
                }
            }

            if (usuarioActual?.idRol == 1L || usuarioActual == null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CreditCard, contentDescription = null, tint = PagoFlexPurple)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Métodos de Pago",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            IconButton(onClick = { mostrarDialogoPago = true }) {
                                Icon(Icons.Default.Add, contentDescription = "Agregar Tarjeta", tint = PagoFlexPurple)
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                        metodosPago.forEach { metodo ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "${metodo.tipo} - ${metodo.banco}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = metodo.numero,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Badge(containerColor = PagoFlexPurple.copy(alpha = 0.2f)) {
                                    Text(metodo.tipo, color = PagoFlexPurple, modifier = Modifier.padding(4.dp))
                                }
                            }
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Modo Oscuro / Claro",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (ThemeManager.isDark) "Tema oscuro activado" else "Tema claro activado",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = ThemeManager.isDark,
                        onCheckedChange = { ThemeManager.isDark = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = PagoFlexPurple)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onCerrarSesion,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Cerrar Sesión", style = MaterialTheme.typography.titleMedium)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun ProfileInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, color = MaterialTheme.colorScheme.onSurface)
    }
}
