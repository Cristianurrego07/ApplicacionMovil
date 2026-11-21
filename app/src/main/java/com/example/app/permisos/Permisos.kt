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

    val CAMARA: String = Manifest.permission.CAMERA

    val NOTIFICACIONES: String = Manifest.permission.POST_NOTIFICATIONS

    val IMAGENES: String =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

    fun tiene(
        context: Context,
        permiso: String
    ): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            permiso
        ) == PackageManager.PERMISSION_GRANTED
    }

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

    fun abrirAjustes(context: Context) {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", context.packageName, null)
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        context.startActivity(intent)
    }
}