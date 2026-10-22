package com.example.app

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.app.model.Usuario
import com.example.app.ui.screens.PerfilScreen
import com.example.app.ui.theme.AppTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class CambiarContrasenaUiTest {
    @get:Rule val compose = createComposeRule()

    @Test fun clientePuedeCambiarLaClaveDesdeSuPerfil() = probarCambio("CLIENTE")
    @Test fun agentePuedeCambiarLaClaveDesdeSuPerfil() = probarCambio("AGENTE")

    private fun probarCambio(rol: String) {
        val usuario = Usuario(1, "1-9", "Usuario", "usuario@example.com", 1, rol)
        var cambios = 0
        compose.setContent {
            AppTheme {
                PerfilScreen(usuario = usuario, compromisos = emptyList(), cantidadPagos = 0,
                    onVolver = {}, onCambiarContrasena = { actual, nueva ->
                        if (actual == "claveActual" && nueva == "claveNueva123") { cambios++; true } else false
                    })
            }
        }
        compose.onNodeWithText("Cambiar contraseña").performScrollTo().performClick()
        compose.onNodeWithText("Contraseña actual").performTextInput("claveActual")
        compose.onNodeWithText("Nueva contraseña").performTextInput("claveNueva123")
        compose.onNodeWithText("Confirmar nueva contraseña").performScrollTo().performTextInput("claveNueva123")
        compose.onNodeWithText("Guardar").performClick()
        compose.onNodeWithText("Contraseña actual").assertDoesNotExist()
        compose.runOnIdle { assertEquals(1, cambios) }
    }

    @Test fun cancelarDescartaLasClavesEscritas() {
        compose.setContent {
            AppTheme {
                PerfilScreen(Usuario(1, "1-9", "Cliente", "cliente@example.com", 1, "CLIENTE"),
                    emptyList(), 0, {})
            }
        }
        compose.onNodeWithText("Cambiar contraseña").performScrollTo().performClick()
        compose.onNodeWithText("Contraseña actual").performTextInput("claveActual")
        compose.onNodeWithText("Cancelar").performClick()
        compose.onNodeWithText("Cambiar contraseña").performScrollTo().performClick()
        compose.onNodeWithText("Contraseña actual").assert(
            SemanticsMatcher.expectValue(androidx.compose.ui.semantics.SemanticsProperties.EditableText,
                androidx.compose.ui.text.AnnotatedString("")))
    }
}
