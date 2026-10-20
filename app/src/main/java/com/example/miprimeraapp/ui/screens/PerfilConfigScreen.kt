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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.miprimeraapp.R
import com.example.miprimeraapp.data.ConvertidorImagen
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

    if (mostrarDialogoPago) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoPago = false },
            title = { Text("Agregar Método de Pago") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                            .size(80.dp)
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
                        text = stringResource(R.string.texto_cambiar_foto),
                        style = MaterialTheme.typography.bodySmall,
                        color = PagoFlexPurple,
                        modifier = Modifier.clickable { mostrarDialogoFoto = true }
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ProfileInfoRow(label = "Nombre", value = usuarioActual?.nombre ?: "Usuario")
                        ProfileInfoRow(label = "Correo", value = usuarioActual?.correo ?: "")
                        ProfileInfoRow(label = "Ciudad", value = usuarioActual?.ciudad ?: "Santiago")
                        ProfileInfoRow(label = "Rol", value = usuarioActual?.nombreRol ?: "Usuario")
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

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onCerrarSesion,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Cerrar Sesión", style = MaterialTheme.typography.titleMedium)
            }
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
