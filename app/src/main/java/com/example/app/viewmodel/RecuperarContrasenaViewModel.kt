package com.example.app.viewmodel

import android.util.Patterns
import androidx.lifecycle.ViewModel
import com.example.app.model.RecuperarContrasenaUiState
import com.example.app.ui.screens.PasoRecuperacion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class RecuperarContrasenaViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(RecuperarContrasenaUiState())
    val uiState = _uiState.asStateFlow()

    fun onCorreoCambia(valor: String) {
        _uiState.update {
            it.copy(
                correo = valor,
                errorCorreo = null,
                errorGeneral = null
            )
        }
    }

    fun onCodigoCambia(valor: String) {
        _uiState.update {
            it.copy(
                codigo = valor,
                errorCodigo = null,
                errorGeneral = null
            )
        }
    }

    fun onNuevaContrasenaCambia(valor: String) {
        _uiState.update {
            it.copy(
                nuevaContrasena = valor,
                errorNuevaContrasena = null,
                errorConfirmarContrasena = null,
                errorGeneral = null
            )
        }
    }

    fun onConfirmarContrasenaCambia(valor: String) {
        _uiState.update {
            it.copy(
                confirmarContrasena = valor,
                errorConfirmarContrasena = null,
                errorGeneral = null
            )
        }
    }

    fun onAlternarVisibilidad() {
        _uiState.update {
            it.copy(
                contrasenaVisible = !it.contrasenaVisible
            )
        }
    }

    fun validarCorreo(): Boolean {
        val correo = _uiState.value.correo

        val errorCorreo = when {
            correo.isBlank() ->
                "Ingresa tu correo electrónico"

            correo.any { it.isWhitespace() } ->
                "El correo no puede contener espacios"

            !Patterns.EMAIL_ADDRESS.matcher(correo).matches() ->
                "Ingresa un correo válido"

            else -> null
        }

        _uiState.update {
            it.copy(
                errorCorreo = errorCorreo,
                errorGeneral = null
            )
        }

        return errorCorreo == null
    }

    fun codigoSolicitado() {
        _uiState.update {
            it.copy(
                paso = PasoRecuperacion.CODIGO,
                errorCorreo = null,
                errorGeneral = null
            )
        }
    }

    fun validarCodigo(): Boolean {
        val codigo = _uiState.value.codigo

        val errorCodigo = when {
            codigo.isBlank() ->
                "Ingresa el código de verificación"

            codigo.any { it.isWhitespace() } ->
                "El código no puede contener espacios"

            else -> null
        }

        _uiState.update {
            it.copy(
                errorCodigo = errorCodigo,
                errorGeneral = null
            )
        }

        return errorCodigo == null
    }

    fun codigoVerificado() {
        _uiState.update {
            it.copy(
                paso = PasoRecuperacion.NUEVA_CONTRASENA,
                errorCodigo = null,
                errorGeneral = null
            )
        }
    }

    fun validarNuevaContrasena(): Boolean {
        val estado = _uiState.value
        val clave = estado.nuevaContrasena

        val errorNuevaContrasena = when {
            clave.isBlank() ->
                "Ingresa una nueva contraseña"

            clave.any { it.isWhitespace() } ->
                "La contraseña no puede contener espacios"

            clave.length < 6 ->
                "La contraseña debe tener al menos 6 caracteres"

            clave.none { it.isUpperCase() } ->
                "Debe tener una letra mayúscula"

            clave.none { it.isLowerCase() } ->
                "Debe tener una letra minúscula"

            clave.none { it.isDigit() } ->
                "Debe tener un número"

            clave.none { !it.isLetterOrDigit() } ->
                "Debe tener un carácter especial"

            else -> null
        }

        val errorConfirmarContrasena = when {
            estado.confirmarContrasena.isBlank() ->
                "Repite la contraseña"

            estado.confirmarContrasena != clave ->
                "Las contraseñas no coinciden"

            else -> null
        }

        _uiState.update {
            it.copy(
                errorNuevaContrasena = errorNuevaContrasena,
                errorConfirmarContrasena = errorConfirmarContrasena,
                errorGeneral = null
            )
        }

        return errorNuevaContrasena == null &&
                errorConfirmarContrasena == null
    }

    fun contrasenaRestablecida() {
        _uiState.update {
            it.copy(
                paso = PasoRecuperacion.EXITO,
                codigo = "",
                nuevaContrasena = "",
                confirmarContrasena = "",
                errorCodigo = null,
                errorNuevaContrasena = null,
                errorConfirmarContrasena = null,
                errorGeneral = null
            )
        }
    }

    fun mostrarError(mensaje: String) {
        _uiState.update {
            it.copy(errorGeneral = mensaje)
        }
    }

    fun volverAlPasoAnterior() {
        _uiState.update { estado ->
            val pasoAnterior = when (estado.paso) {
                PasoRecuperacion.CORREO ->
                    PasoRecuperacion.CORREO

                PasoRecuperacion.CODIGO ->
                    PasoRecuperacion.CORREO

                PasoRecuperacion.NUEVA_CONTRASENA ->
                    PasoRecuperacion.CODIGO

                PasoRecuperacion.EXITO ->
                    PasoRecuperacion.EXITO
            }

            estado.copy(
                paso = pasoAnterior,
                errorCorreo = null,
                errorCodigo = null,
                errorNuevaContrasena = null,
                errorConfirmarContrasena = null,
                errorGeneral = null
            )
        }
    }
}