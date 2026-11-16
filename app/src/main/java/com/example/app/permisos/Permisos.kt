package com.example.app.permisos

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat

object Permisos {

    // Permiso para usar la cámara
    val CAMARA: String = Manifest.permission.CAMERA

    // Permiso para mostrar notificaciones.
    val NOTIFICACIONES: String = Manifest.permission.POST_NOTIFICATIONS

    // El permiso para leer imágenes depende de la versión de Android.
    val IMAGENES: String =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

    // Comprueba si la app tiene un permiso concedido.
    fun tiene(
        context: Context,
        permiso: String
    ): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            permiso
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Comprueba el acceso a imágenes.
    fun tieneImagenes(context: Context): Boolean {
        val accesoCompleto = tiene(context, IMAGENES)

        val accesoSeleccionado =
            Build.VERSION.SDK_INT >= 34 &&
                    tiene(
                        context,
                        Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
                    )

        return accesoCompleto || accesoSeleccionado
    }

    // Indica qué permisos de imágenes solicitar según la versión.
    fun permisosImagenes(): Array<String> {
        return if (Build.VERSION.SDK_INT >= 34) {
            arrayOf(
                IMAGENES,
                Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
            )
        } else {
            arrayOf(IMAGENES)
        }
    }

    // Abre la pantalla de ajustes de esta aplicación.
    fun abrirAjustes(context: Context) {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", context.packageName, null)
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        context.startActivity(intent)
    }
}