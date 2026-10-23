package com.example.app

import com.example.app.validacion.Validaciones
import com.example.app.viewmodel.FormularioViewModel
import org.junit.Assert.*
import org.junit.Test

class ValidacionesTest {
    @Test fun validaDigitoYNormalizaElRut() {
        for (rut in listOf("11.111.111-1", "111111111", " 11.111.111-1 ", "20.435.214-3")) {
            assertTrue(rut, Validaciones.rutValido(rut))
        }
        for (rut in listOf(".-", "20.435.214-5", "00000000-0", "hola", "123-4", "11.111.111-2")) {
            assertFalse(rut, Validaciones.rutValido(rut))
        }
        assertEquals("11.111.111-1", Validaciones.formatearRut("111111111"))
        assertEquals(Validaciones.normalizarRut("111111111"), Validaciones.normalizarRut("11.111.111-1"))
    }

    @Test fun validaCorreoSinDependerDeAndroid() {
        for (correo in listOf("persona@example.com", "persona+app@sub.example.cl", " PERSONA@example.com ")) {
            assertTrue(correo, Validaciones.correoValido(correo))
        }
        for (correo in listOf("@.", "a@@example.com", "a b@example.com", "a@dominio", "a@-dominio.cl", ".a@example.com", "a..b@example.com")) {
            assertFalse(correo, Validaciones.correoValido(correo))
        }
    }

    @Test fun registroAplicaLasReglasComunes() {
        val vm = FormularioViewModel()
        vm.onNombreCambia("Prueba")
        vm.onContrasenaCambia("clave123")
        vm.onTerminosCambia(true)
        vm.onRutCambia(".-")
        vm.onCorreoCambia("@.")
        assertNull(vm.onRegistrar())
        assertNotNull(vm.uiState.value.errorRut)
        assertNotNull(vm.uiState.value.errorCorreo)
        vm.onRutCambia("111111111")
        vm.onCorreoCambia(" PERSONA@EXAMPLE.COM ")
        val usuario = requireNotNull(vm.onRegistrar("AGENTE"))
        assertEquals("11.111.111-1", usuario.rut)
        assertEquals("persona@example.com", usuario.correo)
        assertEquals("AGENTE", usuario.rolNombre)
    }
}
