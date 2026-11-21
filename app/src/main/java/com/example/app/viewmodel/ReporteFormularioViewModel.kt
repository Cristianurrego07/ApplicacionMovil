package com.example.app.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class ReporteFormularioUiState(val error: String? = null)

class ReporteFormularioViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ReporteFormularioUiState())
    val uiState = _uiState.asStateFlow()

    fun limpiarError() {
        _uiState.update { it.copy(error = null) }
    }

    fun mostrarError(mensaje: String) {
        _uiState.update { it.copy(error = mensaje) }
    }

    fun crearMotivo(opcion: String, comentario: String): String? {
        if (opcion == "Otro" && comentario.isBlank()) {
            mostrarError("Escribe un comentario para explicar el problema.")
            return null
        }
        limpiarError()
        return if (opcion == "Otro") "Otro: ${comentario.trim()}" else opcion
    }
}
