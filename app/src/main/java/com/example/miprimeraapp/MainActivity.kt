package com.example.miprimeraapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.miprimeraapp.navigation.AppNavegacion
import com.example.miprimeraapp.ui.theme.MiRegistroTheme
import com.example.miprimeraapp.ui.theme.ThemeManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MiRegistroTheme(darkTheme = ThemeManager.isDark) {
                AppNavegacion()
            }
        }
    }
}
