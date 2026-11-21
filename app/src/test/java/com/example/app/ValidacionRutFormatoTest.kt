package com.example.app

import com.example.app.validacion.Validaciones
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidacionRutFormatoTest {
    @Test fun aceptaRutConFormatoYVerificadorCorrecto() {
        assertTrue(Validaciones.rutValido("12.345.678-5"))
    }

    @Test fun rechazaFaltaDePuntosOGuion() {
        assertFalse(Validaciones.rutValido("123456785"))
        assertFalse(Validaciones.rutValido("12345678-5"))
        assertFalse(Validaciones.rutValido("12.345.6785"))
    }

    @Test fun rechazaVerificadorIncorrecto() {
        assertFalse(Validaciones.rutValido("12.345.678-4"))
    }
}
