package com.example.app.viewmodel

import androidx.lifecycle.ViewModel
import com.example.app.data.remote.Resultado
import com.example.app.model.LoginUiState
import com.example.app.model.Usuario
import com.example.app.validacion.Validaciones
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class LoginViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    fun cambiarIdentificador(valor: String) {
        _uiState.update {
            it.copy(
                identificador = valor,
                errorIdentificador = null,
                error = null
            )
        }
    }

    fun cambiarContrasena(valor: String) {
        _uiState.update {
            it.copy(
                contrasena = valor,
                errorContrasena = null,
                error = null
            )
        }
    }

    fun alternarVisibilidad() {
        _uiState.update {
            it.copy(
                contrasenaVisible = !it.contrasenaVisible
            )
        }
    }

    suspend fun iniciarSesion(
        autenticar: suspend (String, String) -> Resultado<Usuario>,
        onLoginExitoso: suspend (Usuario) -> Unit
    ) {
        val estado = _uiState.value
        if (estado.cargando) return

        val errorIdentificador = when {
            estado.identificador.isBlank() ->
                "Ingresa tu correo o RUT"

            estado.identificador.any { it.isWhitespace() } ->
                "El correo o RUT no puede contener espacios"

            '@' in estado.identificador && !Validaciones.correoValido(estado.identificador) ->
                "Ingresa un correo válido"

            '@' !in estado.identificador && !Validaciones.rutValido(estado.identificador) ->
                "Ingresa un RUT válido con puntos y guion"

            else -> null
        }

        val errorContrasena = when {
            estado.contrasena.isBlank() ->
                "Ingresa tu contraseña"

            estado.contrasena.any { it.isWhitespace() } ->
                "La contraseña no puede contener espacios"

            else -> null
        }

        _uiState.update {
            it.copy(
                errorIdentificador = errorIdentificador,
                errorContrasena = errorContrasena,
                error = null
            )
        }

        if (errorIdentificador != null || errorContrasena != null) return

        _uiState.update { it.copy(cargando = true) }
        try {
            when (val resultado = autenticar(estado.identificador.trim(), estado.contrasena)) {
                is Resultado.Ok -> {
                    onLoginExitoso(resultado.dato)
                    _uiState.value = LoginUiState()
                }
                is Resultado.Error -> _uiState.update { it.copy(error = resultado.mensaje) }
            }
        } finally {
            _uiState.update { it.copy(cargando = false) }
        }
    }
}