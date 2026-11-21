package com.example.app.viewmodel

import androidx.lifecycle.ViewModel
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

    fun iniciarSesion(
        autenticar: (String, String) -> Usuario?
    ): Usuario? {
        val estado = _uiState.value

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

        if (errorIdentificador != null || errorContrasena != null) {
            return null
        }

        val usuario = try {
            autenticar(
                estado.identificador.trim(),
                estado.contrasena
            )
        } catch (_: android.database.sqlite.SQLiteException) {
            _uiState.update {
                it.copy(
                    error = "No se pudo iniciar sesión. Intenta nuevamente."
                )
            }
            return null
        }

        if (usuario == null) {
            _uiState.update {
                it.copy(
                    error = "Correo, RUT o contraseña incorrectos."
                )
            }
            return null
        }

        _uiState.value = LoginUiState()

        return usuario
    }
}