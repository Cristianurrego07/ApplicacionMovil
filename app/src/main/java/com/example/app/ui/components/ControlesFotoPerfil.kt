package com.example.app.ui.components

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.app.R
import com.example.app.data.ConvertidorImagen
import com.example.app.permisos.Permisos
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Flujo de cámara/galería del ejemplo MiRegistroAvatar, adaptado al perfil de PagoFlex. */
@Composable
fun ControlesFotoPerfil(
    snackbar: SnackbarHostState,
    onGuardarFoto: suspend (ByteArray) -> Boolean
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var guardando by remember { mutableStateOf(false) }

    fun avisar(mensaje: String, permisoDenegado: Boolean = false) {
        scope.launch {
            val resultado = snackbar.showSnackbar(mensaje,
                actionLabel = if (permisoDenegado) "Ajustes" else null,
                duration = SnackbarDuration.Long)
            if (resultado == SnackbarResult.ActionPerformed) Permisos.abrirAjustes(context)
        }
    }

    fun guardar(obtener: () -> Bitmap?) {
        scope.launch {
            guardando = true
            try {
                val bytes = withContext(Dispatchers.IO) {
                    obtener()?.let(ConvertidorImagen::aBytes)
                }
                if (bytes == null || !onGuardarFoto(bytes)) {
                    avisar("No se pudo guardar la foto. Intenta con otra imagen.")
                }
            } finally {
                guardando = false
            }
        }
    }

    val camara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { foto ->
        if (foto != null) guardar { foto }
    }
    fun abrirCamara() {
        try { camara.launch(null) }
        catch (_: android.content.ActivityNotFoundException) { avisar("No hay una aplicación de cámara disponible.") }
        catch (_: SecurityException) { avisar("Permiso denegado para la cámara", true) }
    }
    val permisoCamara = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        if (it) abrirCamara() else avisar("Permiso denegado para la cámara", true)
    }
    val galeria = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { resultado ->
        val uri = resultado.data?.data
        if (resultado.resultCode == Activity.RESULT_OK && uri != null) {
            guardar { ConvertidorImagen.desdeUri(context, uri) }
        }
    }
    fun abrirGaleria() {
        try { galeria.launch(Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)) }
        catch (_: android.content.ActivityNotFoundException) { avisar("No hay una galería disponible.") }
        catch (_: SecurityException) { avisar("Permiso denegado para la galería", true) }
    }
    val permisoGaleria = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        if (Permisos.tieneImagenes(context)) abrirGaleria()
        else avisar("Permiso denegado para la galería", true)
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(if (guardando) "Guardando foto…" else "Cambiar foto de perfil")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(enabled = !guardando, onClick = {
                if (Permisos.tiene(context, Permisos.CAMARA)) abrirCamara()
                else permisoCamara.launch(Permisos.CAMARA)
            }) {
                Icon(painterResource(R.drawable.ic_camara), contentDescription = null, modifier = Modifier.size(20.dp))
                Text(" Cámara")
            }
            OutlinedButton(enabled = !guardando, onClick = {
                if (Permisos.tieneImagenes(context)) abrirGaleria()
                else permisoGaleria.launch(Permisos.permisosImagenes())
            }) {
                Icon(painterResource(R.drawable.ic_galeria), contentDescription = null, modifier = Modifier.size(20.dp))
                Text(" Galería")
            }
        }
    }
}
