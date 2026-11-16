package com.example.app.data

import android.content.Context
import androidx.core.content.edit

class SesionPreferencias(context: Context) {

    private val prefs = context.getSharedPreferences(
        "sesion",
        Context.MODE_PRIVATE
    )

    companion object {
        private const val CLAVE_ID_USUARIO = "id_usuario"
        private const val SIN_SESION = -1L
    }

    // Guarda el identificador del usuario que inició sesión.
    fun guardar(idUsuario: Long) {
        prefs.edit {
            putLong(CLAVE_ID_USUARIO, idUsuario)
        }
    }

    // Devuelve el identificador guardado, o -1 si no hay sesión.
    fun leer(): Long {
        return prefs.getLong(CLAVE_ID_USUARIO, SIN_SESION)
    }

    // Elimina el identificador al cerrar sesión.
    fun borrar() {
        prefs.edit {
            remove(CLAVE_ID_USUARIO)
        }
    }
}