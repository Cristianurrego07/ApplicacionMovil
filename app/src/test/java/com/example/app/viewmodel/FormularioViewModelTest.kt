package com.example.app.viewmodel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

// Partimos de un formulario válido y cambiamos solo la condición que queremos probar.
class FormularioViewModelTest {

    private lateinit var viewModel: FormularioViewModel

    @Before
    fun preparar() {
        viewModel = FormularioViewModel()
    }

    private fun llenarFormularioValido() {
        viewModel.onNombreCambia(" Ana Pérez ")
        viewModel.onRutCambia("123456785")
        viewModel.onCorreoCambia("Ana@Example.com")
        viewModel.onContrasenaCambia("Clave1!")
        viewModel.onConfirmarContrasenaCambia("Clave1!")
        viewModel.onTerminosCambia(true)
    }

    @Test
    fun onRegistrar_conDatosValidos_devuelveUsuarioClienteConDatosNormalizados() {
        llenarFormularioValido()

        val usuario = viewModel.onRegistrar()

        assertNotNull(usuario)
        assertEquals("Ana Pérez", usuario?.nombre)
        assertEquals("12.345.678-5", usuario?.rut)
        assertEquals("ana@example.com", usuario?.correo)
        assertEquals("CLIENTE", usuario?.rolNombre)
        assertTrue(viewModel.uiState.value.registroExitoso)
    }

    @Test
    fun onRegistrar_conDigitoVerificadorIncorrecto_muestraErrorDeRut() {
        llenarFormularioValido()
        viewModel.onRutCambia("12.345.678-4")

        val usuario = viewModel.onRegistrar()

        assertNull(usuario)
        assertEquals(
            "Ingresa un RUT válido, incluyendo su dígito verificador",
            viewModel.uiState.value.errorRut
        )
    }

    @Test
    fun onRegistrar_conCorreoSinArroba_muestraErrorDeCorreo() {
        llenarFormularioValido()
        viewModel.onCorreoCambia("ana.example.com")

        val usuario = viewModel.onRegistrar()

        assertNull(usuario)
        assertEquals("Ingresa un correo válido", viewModel.uiState.value.errorCorreo)
    }

    @Test
    fun onRegistrar_conContrasenaSinCaracterEspecial_muestraErrorDeContrasena() {
        llenarFormularioValido()
        viewModel.onContrasenaCambia("Clave12")
        viewModel.onConfirmarContrasenaCambia("Clave12")

        val usuario = viewModel.onRegistrar()

        assertNull(usuario)
        assertEquals("Debe tener un carácter especial", viewModel.uiState.value.errorContrasena)
    }

    @Test
    fun onRegistrar_conConfirmacionDistinta_muestraErrorDeConfirmacion() {
        llenarFormularioValido()
        viewModel.onConfirmarContrasenaCambia("Clave2!")

        val usuario = viewModel.onRegistrar()

        assertNull(usuario)
        assertEquals(
            "Las contraseñas no coinciden",
            viewModel.uiState.value.errorConfirmarContrasena
        )
    }

    @Test
    fun onRegistrar_sinAceptarTerminos_muestraErrorDeTerminos() {
        llenarFormularioValido()
        viewModel.onTerminosCambia(false)

        val usuario = viewModel.onRegistrar()

        assertNull(usuario)
        assertEquals("Debes aceptar los términos", viewModel.uiState.value.errorTerminos)
    }
}
