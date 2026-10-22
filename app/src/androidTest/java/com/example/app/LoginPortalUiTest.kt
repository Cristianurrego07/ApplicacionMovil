package com.example.app

import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.app.model.Usuario
import com.example.app.ui.screens.LoginScreen
import com.example.app.ui.theme.AppTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class LoginPortalUiTest {
    @get:Rule val compose = createComposeRule()

    @Test fun portalesValidanElRolYLimpianLosCampos() {
        var accesos = 0
        compose.setContent {
            AppTheme {
                LoginScreen(
                    onIniciarSesion = { _, _ -> Usuario(2, "1-9", "Agente", "agente@example.com", 2, "AGENTE") },
                    onLoginExitoso = { accesos++ }, onCrearCuenta = {})
            }
        }
        compose.onRoot().captureToImage().asAndroidBitmap().let { imagen ->
            File(InstrumentationRegistry.getInstrumentation().targetContext.cacheDir, "login-cliente.png")
                .outputStream().use { imagen.compress(Bitmap.CompressFormat.PNG, 100, it) }
        }
        compose.onNodeWithText("Correo electrónico o RUT").performTextInput("agente@example.com")
        compose.onNodeWithText("Contraseña").performTextInput("123456")
        compose.onNodeWithText("Iniciar Sesión Cliente").performScrollTo().performClick()
        compose.onNodeWithText("Esta cuenta es de agente. Ingresa desde Portal Agentes.").assertExists()
        compose.runOnIdle { assertEquals(0, accesos) }
        compose.onNodeWithText("Portal Agentes").performScrollTo().performClick()
        compose.onNodeWithText("Correo electrónico o RUT").assert(
            SemanticsMatcher.expectValue(androidx.compose.ui.semantics.SemanticsProperties.EditableText,
                androidx.compose.ui.text.AnnotatedString("")))
        compose.onNodeWithText("¿No tienes cuenta? Regístrate como Cliente aquí").assertDoesNotExist()
        compose.onNodeWithText("Correo electrónico o RUT").performTextInput("agente@example.com")
        compose.onNodeWithText("Contraseña").performTextInput("123456")
        compose.onNodeWithText("Iniciar Sesión Agente").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(1, accesos) }
    }
}
