package com.example.app.viewmodel

import androidx.lifecycle.ViewModel
import com.example.app.model.PerfilUiState
import com.example.app.model.Usuario
import com.example.app.validacion.Validaciones
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class PerfilViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(PerfilUiState())
    val uiState = _uiState.asStateFlow()

    fun cargarUsuario(usuario: Usuario) {
        _uiState.value = PerfilUiState(
            nombre = usuario.nombre,
            rut = usuario.rut,
            correo = usuario.correo,
            telefono = usuario.telefono,
            direccion = usuario.direccion,
            ciudad = usuario.ciudad
        )
    }

    fun onNombreCambia(valor: String) {
        _uiState.update {
            it.copy(
                nombre = valor,
                errorNombre = null,
                errorGuardado = null
            )
        }
    }

    fun onRutCambia(valor: String) {
        _uiState.update {
            it.copy(
                rut = valor,
                errorRut = null,
                errorGuardado = null
            )
        }
    }

    fun onCorreoCambia(valor: String) {
        _uiState.update {
            it.copy(
                correo = valor,
                errorCorreo = null,
                errorGuardado = null
            )
        }
    }

    fun onTelefonoCambia(valor: String) {
        _uiState.update {
            it.copy(
                telefono = valor,
                errorTelefono = null,
                errorGuardado = null
            )
        }
    }

    fun onDireccionCambia(valor: String) {
        _uiState.update {
            it.copy(
                direccion = valor,
                errorDireccion = null,
                errorGuardado = null
            )
        }
    }

    fun onCiudadCambia(valor: String) {
        _uiState.update {
            it.copy(
                ciudad = valor,
                errorCiudad = null,
                errorGuardado = null
            )
        }
    }

    fun validar(): Boolean {
        val estado = _uiState.value

        val errorNombre = when {
            estado.nombre.isBlank() ->
                "Ingresa tu nombre completo"

            estado.nombre.any { it.isDigit() } ->
                "El nombre no puede contener números"

            !Validaciones.nombreValido(estado.nombre) ->
                "El nombre solo debe contener letras"

            else ->
                null
        }

        val errorRut = if (!Validaciones.rutValido(estado.rut)) {
            "Ingresa un RUT válido, incluyendo su dígito verificador"
        } else {
            null
        }

        val errorCorreo = when {
            estado.correo.isBlank() ->
                "Ingresa tu correo electrónico"

            estado.correo.any { it.isWhitespace() } ->
                "El correo no puede contener espacios"

            !Validaciones.correoValido(estado.correo) ->
                "Ingresa un correo válido"

            else -> null
        }

        val errorTelefono = when {
            estado.telefono.isBlank() ->
                null

            !estado.telefono.all { it.isDigit() } ->
                "El teléfono solo puede contener números"

            estado.telefono.length !in 8..15 ->
                "El teléfono debe tener entre 8 y 15 dígitos"

            else -> null
        }

        // Dirección y ciudad son opcionales.
        val errorDireccion: String? = null
        val errorCiudad: String? = null

        _uiState.update {
            it.copy(
                errorNombre = errorNombre,
                errorRut = errorRut,
                errorCorreo = errorCorreo,
                errorTelefono = errorTelefono,
                errorDireccion = errorDireccion,
                errorCiudad = errorCiudad,
                errorGuardado = null
            )
        }

        return listOf(
            errorNombre,
            errorRut,
            errorCorreo,
            errorTelefono,
            errorDireccion,
            errorCiudad
        ).all { it == null }
    }

    fun guardar(
        usuario: Usuario,
        onGuardarPerfil: (Usuario) -> Boolean
    ): Boolean {
        if (!validar()) {
            return false
        }

        val estado = _uiState.value

        val usuarioActualizado = usuario.copy(
            nombre = estado.nombre.trim(),
            rut = Validaciones.formatearRut(estado.rut),
            correo = estado.correo.lowercase(),
            telefono = estado.telefono,
            direccion = estado.direccion.trim(),
            ciudad = estado.ciudad.trim()
        )

        if (!onGuardarPerfil(usuarioActualizado)) {
            _uiState.update {
                it.copy(
                    errorGuardado =
                        "No se pudo guardar. El RUT o correo podrían estar en uso."
                )
            }
            return false
        }

        return true
    }
}