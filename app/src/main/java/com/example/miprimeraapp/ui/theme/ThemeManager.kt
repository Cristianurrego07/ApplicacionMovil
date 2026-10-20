package com.example.miprimeraapp.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object ThemeManager {
    // Por defecto inicia en Modo Claro (fondo blanco/claro)
    var isDark by mutableStateOf(false)
}
