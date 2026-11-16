package com.example.app.ui.components

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.app.R
import com.example.app.data.ConvertidorImagen
import com.example.app.permisos.Permisos
import com.example.app.ui.theme.AppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ControlesFotoPerfil(
    snackbar: SnackbarHostState,
    onGuardarFoto: suspend (ByteArray) -> Boolean
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var guardando by remember {
        mutableStateOf(false)
    }

    // Muestra un mensaje y, si corresponde, permite abrir ajustes.
    fun avisar(
        mensaje: String,
        permisoDenegado: Boolean = false
    ) {
        scope.launch {
            val resultado = snackbar.showSnackbar(
                message = mensaje,
                actionLabel = if (permisoDenegado) "Ajustes" else null,
                duration = SnackbarDuration.Long
            )

            if (resultado == SnackbarResult.ActionPerformed) {
                Permisos.abrirAjustes(context)
            }
        }
    }

    // Convierte la imagen y solicita guardarla.
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

    // Recibe la foto tomada con la cámara.
    val camara = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { foto ->
        if (foto != null) {
            guardar { foto }
        }
    }

    fun abrirCamara() {
        try {
            camara.launch(null)
        } catch (_: ActivityNotFoundException) {
            avisar("No hay una aplicación de cámara disponible.")
        } catch (_: SecurityException) {
            avisar(
                mensaje = "Permiso denegado para la cámara",
                permisoDenegado = true
            )
        }
    }

    val permisoCamara = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            abrirCamara()
        } else {
            avisar(
                mensaje = "Permiso denegado para la cámara",
                permisoDenegado = true
            )
        }
    }

    // Recibe la imagen seleccionada desde la galería.
    val galeria = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { resultado ->
        val uri = resultado.data?.data

        if (resultado.resultCode == Activity.RESULT_OK && uri != null) {
            guardar {
                ConvertidorImagen.desdeUri(context, uri)
            }
        }
    }

    fun abrirGaleria() {
        try {
            val intent = Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            )

            galeria.launch(intent)
        } catch (_: ActivityNotFoundException) {
            avisar("No hay una galería disponible.")
        } catch (_: SecurityException) {
            avisar(
                mensaje = "Permiso denegado para la galería",
                permisoDenegado = true
            )
        }
    }

    val permisoGaleria = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        if (Permisos.tieneImagenes(context)) {
            abrirGaleria()
        } else {
            avisar(
                mensaje = "Permiso denegado para la galería",
                permisoDenegado = true
            )
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (guardando) {
                "Guardando foto…"
            } else {
                "Cambiar foto de perfil"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Botón de cámara
            OutlinedButton(
                enabled = !guardando,
                onClick = {
                    if (Permisos.tiene(context, Permisos.CAMARA)) {
                        abrirCamara()
                    } else {
                        permisoCamara.launch(Permisos.CAMARA)
                    }
                },
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_camara),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )

                Text(text = " Cámara")
            }

            // Botón de galería
            OutlinedButton(
                enabled = !guardando,
                onClick = {
                    if (Permisos.tieneImagenes(context)) {
                        abrirGaleria()
                    } else {
                        permisoGaleria.launch(Permisos.permisosImagenes())
                    }
                },
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_galeria),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )

                Text(text = " Galería")
            }
        }
    }
}

@Preview(
    name = "Cambiar foto de perfil",
    showBackground = true
)
@Composable
private fun ControlesFotoPerfilPreview() {
    AppTheme {
        ControlesFotoPerfil(
            snackbar = remember { SnackbarHostState() },
            onGuardarFoto = { true }
        )
    }
}