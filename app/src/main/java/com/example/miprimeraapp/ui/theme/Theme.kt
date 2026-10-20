package com.example.miprimeraapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val EsquemaClaro = lightColorScheme(
    primary = PrimarioClaroModo,
    onPrimary = Blanco,
    primaryContainer = Color(0xFFEDE9FE),
    onPrimaryContainer = Color(0xFF5B21B6),
    secondary = Secundario,
    onSecondary = Blanco,
    tertiary = Acento,
    onTertiary = Blanco,
    background = FondoClaroModo,
    onBackground = TextoClaroModo,
    surface = SuperficieClaraModo,
    onSurface = TextoClaroModo,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF64748B)
)

private val EsquemaOscuro = darkColorScheme(
    primary = PagoFlexPurple,
    onPrimary = Blanco,
    primaryContainer = PrimarioOscuroModo,
    onPrimaryContainer = Color(0xFFEDE9FE),
    secondary = Secundario,
    onSecondary = Blanco,
    tertiary = Acento,
    onTertiary = Blanco,
    background = FondoOscuroModo,
    onBackground = TextoOscuroModo,
    surface = SuperficieOscuraModo,
    onSurface = TextoOscuroModo,
    surfaceVariant = Color(0xFF242038),
    onSurfaceVariant = Color(0xFF9CA3AF)
)

private val Formas = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(20.dp)
)

@Composable
fun MiRegistroTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) EsquemaOscuro else EsquemaClaro,
        typography = Tipografia,
        shapes = Formas,
        content = content
    )
}
