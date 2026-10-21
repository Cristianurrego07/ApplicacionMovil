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
    primaryContainer = Color(0xFFD1FAE5),
    onPrimaryContainer = Color(0xFF065F46),
    secondary = Secundario,
    onSecondary = Blanco,
    tertiary = Acento,
    onTertiary = Blanco,
    background = FondoClaroModo,
    onBackground = TextoClaroModo,
    surface = SuperficieClaraModo,
    onSurface = TextoClaroModo,
    surfaceVariant = Color(0xFFECFDF5),
    onSurfaceVariant = Color(0xFF047857)
)

private val EsquemaOscuro = darkColorScheme(
    primary = PagoFlexGreen,
    onPrimary = Blanco,
    primaryContainer = Color(0xFF064E3B),
    onPrimaryContainer = Color(0xFFD1FAE5),
    secondary = Secundario,
    onSecondary = Blanco,
    tertiary = Acento,
    onTertiary = Blanco,
    background = FondoOscuroModo,
    onBackground = TextoOscuroModo,
    surface = SuperficieOscuraModo,
    onSurface = TextoOscuroModo,
    surfaceVariant = Color(0xFF1E2E28),
    onSurfaceVariant = Color(0xFFA7F3D0)
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
