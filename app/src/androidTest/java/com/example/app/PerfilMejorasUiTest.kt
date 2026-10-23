package com.example.app

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.lifecycle.SavedStateHandle
import com.example.app.model.Usuario
import com.example.app.ui.screens.PerfilScreen
import com.example.app.ui.theme.AppTheme
import com.example.app.viewmodel.TarjetasViewModel
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class PerfilMejorasUiTest {
    @get:Rule val compose = createComposeRule()
    private val usuario = Usuario(1, "20.435.214-5", "Cliente", "cliente@example.com", 1, "CLIENTE")

    @Test fun tarjetaSeRestauraSinGuardarNumeroCompletoNiCvv() {
        val restauracion = StateRestorationTester(compose)
        val handle = SavedStateHandle()
        val vm = TarjetasViewModel(handle)
        restauracion.setContent {
            AppTheme { PerfilScreen(usuario, emptyList(), 0, {}, tarjetasViewModel = vm) }
        }
        compose.onNodeWithText("+ Incorporar tarjeta de crédito o débito").performScrollTo().performClick()
        compose.onNodeWithText("Número de tarjeta").performTextInput("0000111122223333")
        compose.onNodeWithText("Vencimiento (MM/AA)").performTextInput("12/30")
        compose.onNodeWithText("CVV").performScrollTo().performTextInput("123")
        compose.onNodeWithText("Incorporar").performClick()
        compose.onNodeWithText("Débito: •••• 3333 (12/30)").assertExists()
        restauracion.emulateSavedInstanceStateRestore()
        compose.onNodeWithText("Débito: •••• 3333 (12/30)").assertExists()
        val datos = requireNotNull(handle.get<List<String>>("tarjetas"))
        assertFalse(datos.contains("0000111122223333"))
        assertFalse(datos.contains("123"))
    }

    @Test fun edicionValidaCamposYConservaElRutDeLaCuentaDeEjemplo() {
        var guardado: Usuario? = null
        compose.setContent {
            AppTheme { PerfilScreen(usuario, emptyList(), 0, {}, onGuardarPerfil = { guardado = it; true }) }
        }
        compose.onNodeWithContentDescription("Editar perfil").performScrollTo().performClick()
        compose.onNodeWithText("Guardar").performClick()
        compose.runOnIdle { assertEquals(usuario.rut, guardado?.rut) }
        compose.onNodeWithContentDescription("Editar perfil").performScrollTo().performClick()
        val campoRut = hasSetTextAction() and hasText("RUT")
        compose.onNode(campoRut).performTextReplacement(".-")
        compose.onNodeWithText("Guardar").performClick()
        compose.onNodeWithText("Ingresa un RUT válido, incluyendo su dígito verificador.").assertExists()
        compose.onNode(campoRut).performTextReplacement("111111111")
        val campoCorreo = hasSetTextAction() and hasText("Correo electrónico")
        compose.onNode(campoCorreo).performTextReplacement("@.")
        compose.onNodeWithText("Guardar").performClick()
        compose.onNodeWithText("Ingresa un correo electrónico válido.").assertExists()
        compose.onNode(campoCorreo).performTextReplacement("nuevo@example.com")
        compose.onNodeWithText("Guardar").performClick()
        compose.runOnIdle {
            assertEquals("11.111.111-1", guardado?.rut)
            assertEquals("nuevo@example.com", guardado?.correo)
        }
    }
}
