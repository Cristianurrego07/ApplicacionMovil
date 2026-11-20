package com.example.app.viewmodel

import com.example.app.model.LoginUiState
import com.example.app.model.Usuario
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test

// Cada prueba sigue tres pasos: preparar, ejecutar y verificar.
// La función de autenticación simulada permite probar el login sin abrir SQLite.
class LoginViewModelTest {

    private lateinit var viewModel: LoginViewModel

    @Before
    fun preparar() {
        viewModel = LoginViewModel()
    }

    @Test
    fun iniciarSesion_conCamposVacios_muestraErroresSinAutenticar() {
        val usuario = viewModel.iniciarSesion { _, _ ->
            error("El formulario vacío no debe intentar autenticar")
        }

        assertNull(usuario)
        assertEquals("Ingresa tu correo o RUT", viewModel.uiState.value.errorIdentificador)
        assertEquals("Ingresa tu contraseña", viewModel.uiState.value.errorContrasena)
    }

    @Test
    fun iniciarSesion_conCredencialesValidas_devuelveUsuarioYLimpiaFormulario() {
        // Preparar
        val usuarioEsperado = Usuario(
            id = 1L,
            rut = "12.345.678-5",
            nombre = "Ana Pérez",
            correo = "ana@example.com",
            rolId = 1L,
            rolNombre = "CLIENTE"
        )
        viewModel.cambiarIdentificador(usuarioEsperado.rut)
        viewModel.cambiarContrasena("Clave1!")

        // Ejecutar
        val usuario = viewModel.iniciarSesion { identificador, contrasena ->
            assertEquals(usuarioEsperado.rut, identificador)
            assertEquals("Clave1!", contrasena)
            usuarioEsperado
        }

        // Verificar
        assertSame(usuarioEsperado, usuario)
        assertEquals(LoginUiState(), viewModel.uiState.value)
    }

    @Test
    fun iniciarSesion_conCredencialesIncorrectas_muestraErrorGeneral() {
        viewModel.cambiarIdentificador("ana@example.com")
        viewModel.cambiarContrasena("OtraClave1!")

        val usuario = viewModel.iniciarSesion { _, _ -> null }

        assertNull(usuario)
        assertEquals("Correo, RUT o contraseña incorrectos.", viewModel.uiState.value.error)
    }

    @Test
    fun cambiarIdentificador_despuesDeUnError_borraSoloElErrorDelIdentificador() {
        viewModel.iniciarSesion { _, _ -> null }

        viewModel.cambiarIdentificador("ana@example.com")

        assertNull(viewModel.uiState.value.errorIdentificador)
        assertEquals("Ingresa tu contraseña", viewModel.uiState.value.errorContrasena)
    }
}
