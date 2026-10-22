package com.example.app.viewmodel

import androidx.lifecycle.ViewModel
import com.example.app.model.CambiarContrasenaUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// Mismo patrón de FormularioViewModel en MiRegistro: estado, validación y errores.
class CambiarContrasenaViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(CambiarContrasenaUiState())
    val uiState = _uiState.asStateFlow()

    fun cambiarActual(valor: String) {
        _uiState.update { it.copy(actual = valor, errorActual = null, errorGuardado = null) }
    }
    fun cambiarNueva(valor: String) {
        _uiState.update { it.copy(nueva = valor, errorNueva = null, errorConfirmacion = null, errorGuardado = null) }
    }
    fun cambiarConfirmacion(valor: String) {
        _uiState.update { it.copy(confirmacion = valor, errorConfirmacion = null, errorGuardado = null) }
    }
    fun alternarActual() { _uiState.update { it.copy(actualVisible = !it.actualVisible) } }
    fun alternarNueva() { _uiState.update { it.copy(nuevaVisible = !it.nuevaVisible) } }
    fun alternarConfirmacion() { _uiState.update { it.copy(confirmacionVisible = !it.confirmacionVisible) } }

    fun limpiar() { _uiState.value = CambiarContrasenaUiState() }

    fun guardar(onCambiar: (String, String) -> Boolean): Boolean {
        val estado = _uiState.value
        val errorActual = if (estado.actual.isBlank()) "Ingresa tu contraseña actual" else null
        val errorNueva = when {
            estado.nueva.isBlank() -> "Ingresa una nueva contraseña"
            estado.nueva.length < 6 -> "Mínimo 6 caracteres"
            else -> null
        }
        val errorConfirmacion = if (estado.nueva != estado.confirmacion) "Las contraseñas no coinciden" else null
        _uiState.update { it.copy(errorActual = errorActual, errorNueva = errorNueva,
            errorConfirmacion = errorConfirmacion, errorGuardado = null) }
        if (listOf(errorActual, errorNueva, errorConfirmacion).any { it != null }) return false
        if (!onCambiar(estado.actual, estado.nueva)) {
            _uiState.update { it.copy(errorGuardado = "No se pudo cambiar. Revisa tu contraseña actual e intenta nuevamente.") }
            return false
        }
        limpiar()
        return true
    }
}
