package com.example.app.viewmodel

import org.junit.Assert.*
import org.junit.Test

class ReporteFormularioViewModelTest {
    @Test
    fun otroRequiereUnaExplicacion() {
        val viewModel = ReporteFormularioViewModel()
        assertNull(viewModel.crearMotivo("Otro", "   "))
        assertNotNull(viewModel.uiState.value.error)
        assertEquals("Otro: Cobro incorrecto", viewModel.crearMotivo("Otro", "  Cobro incorrecto  "))
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun respuestaDebeTenerContenido() {
        val viewModel = RespuestaReporteViewModel()
        assertFalse(viewModel.validar())
        viewModel.cambiarTexto("Se revisó el cobro")
        assertTrue(viewModel.validar())
        assertNull(viewModel.uiState.value.error)
    }
}
