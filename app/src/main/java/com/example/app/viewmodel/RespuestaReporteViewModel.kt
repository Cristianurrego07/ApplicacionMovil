package com.example.app.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class RespuestaReporteUiState(
    val texto: String = "",
    val error: String? = null
)

class RespuestaReporteViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(RespuestaReporteUiState())
    val uiState = _uiState.asStateFlow()

    fun cambiarTexto(texto: String) {
        _uiState.update { it.copy(texto = texto, error = null) }
    }

    fun validar(): Boolean {
        val error = if (_uiState.value.texto.isBlank()) {
            "Escribe una respuesta para el cliente."
        } else {
            null
        }
        _uiState.update { it.copy(error = error) }
        return error == null
    }

    fun mostrarError(mensaje: String) {
        _uiState.update { it.copy(error = mensaje) }
    }

    fun limpiar() {
        _uiState.value = RespuestaReporteUiState()
    }
}
