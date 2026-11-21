package com.example.app.viewmodel

import androidx.lifecycle.ViewModel
import com.example.app.model.Servicio
import com.example.app.model.CatalogoServicios
import com.example.app.model.ServicioUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import com.example.app.validacion.Validaciones

class ServicioViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ServicioUiState())
    val uiState: StateFlow<ServicioUiState> = _uiState.asStateFlow()

    fun onNombreEmpresaCambia(valor: String) {
        _uiState.update {
            it.copy(nombreEmpresa = valor, errorNombreEmpresa = null, errorRegistro = null)
        }
    }

    fun onRutPagadorCambia(valor: String) {
        _uiState.update {
            it.copy(rutPagador = valor, errorRutPagador = null, errorRegistro = null)
        }
    }

    fun onAliasCambia(valor: String) {
        _uiState.update {
            it.copy(alias = valor, errorAlias = null, errorRegistro = null)
        }
    }

    fun onAgregar(usuarioId: Long): Servicio? {
        val estado = _uiState.value
        val empresa = CatalogoServicios.buscar(estado.nombreEmpresa)
        val errorNombre = when {
            estado.nombreEmpresa.isBlank() -> "El nombre de la empresa es obligatorio"
            else -> null
        }
        val errorRut = if (!Validaciones.rutValido(estado.rutPagador)) {
            "Ingresa un RUT válido con puntos y guion"
        } else null
        val errorAlias = if (estado.alias.isBlank()) {
            "Ingresa una descripción o alias"
        } else null

        val hayErrores = listOf(errorNombre, errorRut, errorAlias).any { it != null }
        _uiState.update {
            it.copy(
                errorNombreEmpresa = errorNombre,
                errorRutPagador = errorRut,
                errorAlias = errorAlias
            )
        }

        if (hayErrores) return null

        return Servicio(
            usuarioId = usuarioId,
            nombreEmpresa = if (CatalogoServicios.esDuoc(estado.nombreEmpresa)) "Duoc Uc" else empresa!!.empresa,
            rutPagador = Validaciones.formatearRut(estado.rutPagador),
            alias = estado.alias.trim()
        )
    }

    fun onGuardadoFallido() {
        _uiState.update { it.copy(errorRegistro = "No se pudo guardar. Revisa si ya agregaste este servicio.") }
    }
}
