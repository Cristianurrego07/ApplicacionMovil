package com.example.app

import com.example.app.viewmodel.CambiarContrasenaViewModel
import com.example.app.viewmodel.FormularioViewModel
import org.junit.Assert.*
import org.junit.Test

class ContrasenaTest {
    private fun formulario(clave: String) = FormularioViewModel().apply {
        onNombreCambia("Usuario Prueba")
        onRutCambia("20.435.214-3")
        onCorreoCambia("prueba@example.com")
        onTerminosCambia(true)
        onContrasenaCambia(clave)
    }

    @Test fun registroRechazaMenosDeSeisYAceptaSeisOMasSinRecortar() {
        for (clave in listOf("", "12345", "      ")) {
            val vm = formulario(clave)
            assertNull(vm.onRegistrar())
            assertNotNull(vm.uiState.value.errorContrasena)
        }
        for (clave in listOf("123456", "UnaClaveMasLarga123")) {
            val vm = formulario(clave)
            assertNotNull(vm.onRegistrar())
            assertEquals(clave, vm.uiState.value.contrasena)
        }
    }

    @Test fun cambioRechazaCamposVaciosClavesCortasYConfirmacionDistinta() {
        val vm = CambiarContrasenaViewModel()
        val noGuardar: (String, String) -> Boolean = { _, _ -> error("No debe consultar la base") }
        assertFalse(vm.guardar(noGuardar))
        vm.cambiarActual("anterior")
        vm.cambiarNueva("12345")
        vm.cambiarConfirmacion("12345")
        assertFalse(vm.guardar(noGuardar))
        assertEquals("Mínimo 6 caracteres", vm.uiState.value.errorNueva)
        vm.cambiarNueva("nueva123")
        assertFalse(vm.guardar(noGuardar))
        assertEquals("Las contraseñas no coinciden", vm.uiState.value.errorConfirmacion)
    }

    @Test fun cambioFallidoConservaFormularioYExitoLimpiaClaves() {
        val vm = CambiarContrasenaViewModel()
        vm.cambiarActual("anterior")
        vm.cambiarNueva("NuevaClave123")
        vm.cambiarConfirmacion("NuevaClave123")
        assertFalse(vm.guardar { _, _ -> false })
        assertNotNull(vm.uiState.value.errorGuardado)
        assertTrue(vm.guardar { actual, nueva -> actual == "anterior" && nueva == "NuevaClave123" })
        assertEquals("", vm.uiState.value.actual)
        assertEquals("", vm.uiState.value.nueva)
        assertEquals("", vm.uiState.value.confirmacion)
        assertNull(vm.uiState.value.errorGuardado)
    }
}
