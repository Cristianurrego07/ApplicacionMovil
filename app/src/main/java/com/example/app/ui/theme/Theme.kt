package com.example.app.ui.theme

import android.os.Build
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = PrimarioClaroModo,
    onPrimary = Blanco,
    primaryContainer = Color(0xFFD1FAE5),
    onPrimaryContainer = Color(0xFF065F46),
    secondary = Secundario,
    onSecondary = Blanco,
    secondaryContainer = SecundarioClaro,
    onSecondaryContainer = Color(0xFF0C4A6E),
    tertiary = Acento,
    onTertiary = Blanco,
    background = FondoClaroModo,
    onBackground = TextoClaroModo,
    surface = SuperficieClaraModo,
    onSurface = TextoClaroModo,
    surfaceVariant = Color(0xFFECFDF5),
    onSurfaceVariant = Color(0xFF047857)
)

private val DarkColorScheme = darkColorScheme(
    primary = PagoFlexGreen,
    onPrimary = Blanco,
    primaryContainer = Color(0xFF064E3B),
    onPrimaryContainer = Color(0xFFD1FAE5),
    secondary = Secundario,
    onSecondary = Blanco,
    secondaryContainer = Color(0xFF0C4A6E),
    onSecondaryContainer = SecundarioClaro,
    tertiary = Acento,
    onTertiary = Blanco,
    background = FondoOscuroModo,
    onBackground = TextoOscuroModo,
    surface = SuperficieOscuraModo,
    onSurface = TextoOscuroModo,
    surfaceVariant = Color(0xFF1E2E28),
    onSurfaceVariant = Color(0xFFA7F3D0)
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun AppThemePreview(content: @Composable () -> Unit) {
    AppTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground
        ) {
            content()
        }
    }
}
