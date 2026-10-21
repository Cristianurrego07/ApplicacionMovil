package com.example.miprimeraapp.viewmodel

import androidx.lifecycle.ViewModel
import com.example.miprimeraapp.model.Servicio
import com.example.miprimeraapp.model.ServicioUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ServicioViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ServicioUiState())
    val uiState: StateFlow<ServicioUiState> = _uiState.asStateFlow()

    fun onNombreEmpresaCambia(valor: String) {
        _uiState.update {
            it.copy(
                nombreEmpresa = valor,
                errorNombreEmpresa = null,
                errorRegistro = null
            )
        }
    }

    fun onNumeroClienteCambia(valor: String) {
        _uiState.update {
            it.copy(
                numeroCliente = valor,
                errorNumeroCliente = null,
                errorRegistro = null
            )
        }
    }

    fun onRutPagadorCambia(valor: String) {
        _uiState.update {
            it.copy(
                rutPagador = valor,
                errorRutPagador = null,
                errorRegistro = null
            )
        }
    }

    fun onAliasCambia(valor: String) {
        _uiState.update {
            it.copy(
                alias = valor,
                errorAlias = null,
                errorRegistro = null
            )
        }
    }

    fun onAgregar(usuarioId: Long): Servicio? {
        val estado = _uiState.value

        val errorEmpresa = if (estado.nombreEmpresa.isBlank()) "Selecciona la empresa o servicio" else null
        val errorNumero = if (estado.numeroCliente.isBlank()) "Ingresa el N° de cliente o cuenta" else null
        val errorAlias = if (estado.alias.isBlank()) "Ingresa un alias para reconocer este servicio" else null

        val hayErrores = listOf(errorEmpresa, errorNumero, errorAlias).any { it != null }

        _uiState.update {
            it.copy(
                errorNombreEmpresa = errorEmpresa,
                errorNumeroCliente = errorNumero,
                errorAlias = errorAlias
            )
        }

        return if (hayErrores) null else Servicio(
            usuarioId = usuarioId,
            nombreEmpresa = estado.nombreEmpresa.trim(),
            rutPagador = estado.rutPagador.ifBlank { "12.345.678-9" },
            alias = estado.alias.trim(),
            numeroCliente = estado.numeroCliente.trim()
        )
    }

    fun onGuardadoFallido() {
        _uiState.update {
            it.copy(
                errorRegistro = "No se pudo guardar el servicio. Intenta nuevamente."
            )
        }
    }
}
