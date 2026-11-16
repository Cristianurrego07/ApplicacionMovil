package com.example.app.notificaciones

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.app.R

object NotificacionesHelper {
    private const val CANAL = "vencimientos"

    fun mostrar(context: Context, folio: String, concepto: String, fecha: String, vencido: Boolean): Boolean {
        val gestor = NotificationManagerCompat.from(context)
        if (!gestor.areNotificationsEnabled()) return false

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(CANAL, "Vencimientos", NotificationManager.IMPORTANCE_DEFAULT)
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(canal)
        }

        val aviso = NotificationCompat.Builder(context, CANAL)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(if (vencido) "PagoFlex: compromiso vencido" else "PagoFlex: próximo vencimiento")
            .setContentText(if (vencido) "Tu compromiso de $concepto venció el $fecha." else "Tu compromiso de $concepto vence el $fecha.")
            .setAutoCancel(true)
            .build()
        return try {
            gestor.notify(folio.hashCode(), aviso)
            true
        } catch (_: SecurityException) {
            false
        }
    }
}
