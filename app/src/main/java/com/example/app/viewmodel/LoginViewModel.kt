package com.example.app.viewmodel

import androidx.lifecycle.ViewModel
import com.example.app.model.LoginUiState
import com.example.app.model.PortalLogin
import com.example.app.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Estado y validación del formulario, siguiendo el patrón MVVM de MiRegistro. */
class LoginViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    fun seleccionarPortal(portal: PortalLogin) {
        if (_uiState.value.portal != portal) _uiState.value = LoginUiState(portal = portal)
    }

    fun cambiarIdentificador(valor: String) {
        _uiState.update { it.copy(identificador = valor, error = null) }
    }

    fun cambiarContrasena(valor: String) {
        _uiState.update { it.copy(contrasena = valor, error = null) }
    }

    fun alternarVisibilidad() {
        _uiState.update { it.copy(contrasenaVisible = !it.contrasenaVisible) }
    }

    fun iniciarSesion(autenticar: (String, String) -> Usuario?): Usuario? {
        val estado = _uiState.value
        if (estado.identificador.isBlank() || estado.contrasena.isBlank()) {
            _uiState.update { it.copy(error = "Ingresa tu correo o RUT y tu contraseña.") }
            return null
        }
        val usuario = try {
            autenticar(estado.identificador.trim(), estado.contrasena)
        } catch (_: android.database.sqlite.SQLiteException) {
            _uiState.update { it.copy(error = "No se pudo iniciar sesión. Intenta nuevamente.") }
            return null
        }
        if (usuario == null) {
            _uiState.update { it.copy(error = "Correo, RUT o contraseña incorrectos.") }
            return null
        }
        if (usuario.rolNombre != estado.portal.rol) {
            val destino = when (usuario.rolNombre) {
                "CLIENTE" -> "Esta cuenta es de cliente. Ingresa desde Portal Clientes."
                "AGENTE" -> "Esta cuenta es de agente. Ingresa desde Portal Agentes."
                else -> "Esta cuenta no tiene acceso a los portales disponibles."
            }
            _uiState.update { it.copy(error = destino) }
            return null
        }
        _uiState.update { LoginUiState(portal = it.portal) }
        return usuario
    }
}
