package com.example.app.notificaciones

import android.content.Context
import androidx.core.content.edit

class NotificacionesPreferencias(context: Context, usuarioId: Long) {
    private val prefs = context.getSharedPreferences("avisos_$usuarioId", Context.MODE_PRIVATE)

    fun activadas(): Boolean = prefs.getBoolean("activadas", false)
    fun dias(folio: String): Int = prefs.getInt("dias_$folio", 0)
    fun guardarActivadas(valor: Boolean) { prefs.edit { putBoolean("activadas", valor) } }
    fun guardarDias(folio: String, valor: Int) { prefs.edit { putInt("dias_$folio", valor) } }
    fun fueEnviado(folio: String, fecha: String): Boolean = prefs.getString("aviso_$folio", null) == fecha
    fun marcarEnviado(folio: String, fecha: String) { prefs.edit { putString("aviso_$folio", fecha) } }
}
