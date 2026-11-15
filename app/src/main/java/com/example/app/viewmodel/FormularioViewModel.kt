package com.example.app.viewmodel

import androidx.lifecycle.ViewModel
import com.example.app.model.FormularioUiState
import com.example.app.model.Usuario
import com.example.app.validacion.Validaciones
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FormularioViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(FormularioUiState())
    val uiState: StateFlow<FormularioUiState> = _uiState.asStateFlow()

    fun onNombreCambia(valor: String) {
        _uiState.update {
            it.copy(
                nombre = valor,
                errorNombre = null,
                errorRegistro = null,
                registroExitoso = false
            )
        }
    }

    fun onRutCambia(valor: String) {
        _uiState.update {
            it.copy(
                rut = valor,
                errorRut = null,
                errorRegistro = null,
                registroExitoso = false
            )
        }
    }

    fun onCorreoCambia(valor: String) {
        _uiState.update {
            it.copy(
                correo = valor,
                errorCorreo = null,
                errorRegistro = null,
                registroExitoso = false
            )
        }
    }

    fun onContrasenaCambia(valor: String) {
        _uiState.update {
            it.copy(
                contrasena = valor,
                errorContrasena = null,
                errorConfirmarContrasena = null,
                errorRegistro = null,
                registroExitoso = false
            )
        }
    }

    fun onConfirmarContrasenaCambia(valor: String) {
        _uiState.update {
            it.copy(
                confirmarContrasena = valor,
                errorConfirmarContrasena = null,
                errorRegistro = null,
                registroExitoso = false
            )
        }
    }

    fun onAlternarVisibilidad() {
        _uiState.update {
            it.copy(contrasenaVisible = !it.contrasenaVisible)
        }
    }

    fun onTerminosCambia(valor: Boolean) {
        _uiState.update {
            it.copy(
                aceptaTerminos = valor,
                errorTerminos = null
            )
        }
    }

    fun onLimpiar() {
        _uiState.value = FormularioUiState()
    }

    fun onRegistrar(): Usuario? {
        val estado = _uiState.value

        val errorNombre = if (estado.nombre.isBlank()) {
            "El nombre es obligatorio"
        } else {
            null
        }

        val errorRut = if (!Validaciones.rutValido(estado.rut)) {
            "Ingresa un RUT válido, incluyendo su dígito verificador"
        } else {
            null
        }

        val errorCorreo = when {
            estado.correo.isBlank() ->
                "El correo es obligatorio"

            estado.correo.any { it.isWhitespace() } ->
                "El correo no puede contener espacios"

            !Validaciones.correoValido(estado.correo) ->
                "Ingresa un correo válido"

            else -> null
        }

        val errorContrasena = when {
            estado.contrasena.isBlank() ->
                "La contraseña es obligatoria"

            estado.contrasena.any { it.isWhitespace() } ->
                "La contraseña no puede contener espacios"

            estado.contrasena.length < 6 ->
                "Mínimo 6 caracteres"

            estado.contrasena.none { it.isUpperCase() } ->
                "Debe tener una letra mayúscula"

            estado.contrasena.none { it.isLowerCase() } ->
                "Debe tener una letra minúscula"

            estado.contrasena.none { it.isDigit() } ->
                "Debe tener un número"

            estado.contrasena.none { !it.isLetterOrDigit() } ->
                "Debe tener un carácter especial"

            else -> null
        }

        val errorConfirmarContrasena = when {
            estado.confirmarContrasena.isBlank() ->
                "Repite la contraseña"

            estado.confirmarContrasena != estado.contrasena ->
                "Las contraseñas no coinciden"

            else -> null
        }

        val errorTerminos = if (!estado.aceptaTerminos) {
            "Debes aceptar los términos"
        } else {
            null
        }

        val hayErrores = listOf(
            errorNombre,
            errorRut,
            errorCorreo,
            errorContrasena,
            errorConfirmarContrasena,
            errorTerminos
        ).any { it != null }

        _uiState.update {
            it.copy(
                errorNombre = errorNombre,
                errorRut = errorRut,
                errorCorreo = errorCorreo,
                errorContrasena = errorContrasena,
                errorConfirmarContrasena = errorConfirmarContrasena,
                errorTerminos = errorTerminos,
                registroExitoso = !hayErrores
            )
        }

        if (hayErrores) return null

        return Usuario(
            rut = Validaciones.formatearRut(estado.rut),
            nombre = estado.nombre.trim(),
            correo = estado.correo.trim().lowercase(),
            rolId = 1L,
            rolNombre = "CLIENTE"
        )
    }

    fun onRegistroFallido(mensaje: String) {
        _uiState.update {
            it.copy(
                errorRegistro = mensaje,
                registroExitoso = false
            )
        }
    }
}