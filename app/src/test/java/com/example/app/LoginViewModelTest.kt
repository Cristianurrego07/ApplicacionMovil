package com.example.app

import com.example.app.model.PortalLogin
import com.example.app.model.Usuario
import com.example.app.viewmodel.LoginViewModel
import org.junit.Assert.*
import org.junit.Test

class LoginViewModelTest {
    private val cliente = Usuario(1, "20.435.214-5", "Sofía", "sofia@example.com", 1, "CLIENTE")
    private val agente = cliente.copy(id = 2, rolId = 2, rolNombre = "AGENTE")

    private fun preparar(vm: LoginViewModel) {
        vm.cambiarIdentificador("  sofia@example.com  ")
        vm.cambiarContrasena("123456")
    }

    @Test fun noConsultaLaBaseSinCredenciales() {
        val vm = LoginViewModel()
        assertNull(vm.iniciarSesion { _, _ -> error("No debe autenticar campos vacíos") })
        assertNotNull(vm.uiState.value.error)
    }

    @Test fun clienteAutenticadoEnPortalCliente() {
        val vm = LoginViewModel()
        preparar(vm)
        val resultado = vm.iniciarSesion { identificador, clave ->
            assertEquals("sofia@example.com", identificador)
            assertEquals("123456", clave)
            cliente
        }
        assertEquals(cliente, resultado)
        assertEquals("", vm.uiState.value.contrasena)
    }

    @Test fun rechazaAgenteEnPortalClienteYClienteEnPortalAgente() {
        val vm = LoginViewModel()
        preparar(vm)
        assertNull(vm.iniciarSesion { _, _ -> agente })
        assertTrue(vm.uiState.value.error!!.contains("Portal Agentes"))
        vm.seleccionarPortal(PortalLogin.AGENTE)
        preparar(vm)
        assertNull(vm.iniciarSesion { _, _ -> cliente })
        assertTrue(vm.uiState.value.error!!.contains("Portal Clientes"))
        assertEquals(agente, vm.iniciarSesion { _, _ -> agente })
    }

    @Test fun cambiarPortalLimpiaCredencialesVisibilidadYError() {
        val vm = LoginViewModel()
        preparar(vm)
        vm.alternarVisibilidad()
        assertNull(vm.iniciarSesion { _, _ -> null })
        vm.seleccionarPortal(PortalLogin.AGENTE)
        val estado = vm.uiState.value
        assertEquals("", estado.identificador)
        assertEquals("", estado.contrasena)
        assertFalse(estado.contrasenaVisible)
        assertNull(estado.error)
    }
}
