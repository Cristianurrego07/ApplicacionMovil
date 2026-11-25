package com.example.app.viewmodel

import com.example.app.data.remote.Resultado
import com.example.app.model.LoginUiState
import kotlinx.coroutines.runBlocking
import com.example.app.model.Usuario
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test

// Cada prueba sigue tres pasos: preparar, ejecutar y verificar.
class LoginViewModelTest {

    private lateinit var viewModel: LoginViewModel

    @Before
    fun preparar() {
        viewModel = LoginViewModel()
    }

    @Test
    fun iniciarSesion_conCamposVacios_muestraErroresSinAutenticar() = runBlocking {
        viewModel.iniciarSesion(
            autenticar = { _, _ -> error("El formulario vacío no debe intentar autenticar") },
            onLoginExitoso = { error("El formulario vacío no debe iniciar sesión") }
        )
        assertEquals("Ingresa tu correo o RUT", viewModel.uiState.value.errorIdentificador)
        assertEquals("Ingresa tu contraseña", viewModel.uiState.value.errorContrasena)
    }

    @Test
    fun iniciarSesion_conCredencialesValidas_devuelveUsuarioYLimpiaFormulario() = runBlocking {
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
        var usuario: Usuario? = null
        viewModel.iniciarSesion(
            autenticar = { identificador, contrasena ->
                assertEquals(usuarioEsperado.rut, identificador)
                assertEquals("Clave1!", contrasena)
                Resultado.Ok(usuarioEsperado)
            },
            onLoginExitoso = { usuario = it }
        )

        // Verificar
        assertSame(usuarioEsperado, usuario)
        assertEquals(LoginUiState(), viewModel.uiState.value)
    }

    @Test
    fun iniciarSesion_conCredencialesIncorrectas_muestraErrorGeneral() = runBlocking {
        viewModel.cambiarIdentificador("ana@example.com")
        viewModel.cambiarContrasena("OtraClave1!")

        viewModel.iniciarSesion(
            autenticar = { _, _ -> Resultado.Error("Correo, RUT o contraseña incorrectos.", 401) },
            onLoginExitoso = { error("Las credenciales incorrectas no deben iniciar sesión") }
        )
        assertEquals("Correo, RUT o contraseña incorrectos.", viewModel.uiState.value.error)
    }

    @Test
    fun cambiarIdentificador_despuesDeUnError_borraSoloElErrorDelIdentificador() = runBlocking {
        viewModel.iniciarSesion(
            autenticar = { _, _ -> error("El formulario vacío no debe intentar autenticar") },
            onLoginExitoso = { error("El formulario vacío no debe iniciar sesión") }
        )

        viewModel.cambiarIdentificador("ana@example.com")

        assertNull(viewModel.uiState.value.errorIdentificador)
        assertEquals("Ingresa tu contraseña", viewModel.uiState.value.errorContrasena)
    }
}
