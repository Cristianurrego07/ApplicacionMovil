package com.example.miprimeraapp.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object CorreoNotificador {

    fun enviarCorreo(
        context: Context,
        destinatario: String,
        asunto: String,
        cuerpo: String
    ) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$destinatario")
            putExtra(Intent.EXTRA_SUBJECT, asunto)
            putExtra(Intent.EXTRA_TEXT, cuerpo)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(Intent.createChooser(intent, "Enviar correo con..."))
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Notificación por correo enviada a: $destinatario",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
