package cl.duocuc.miregistro.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import cl.duocuc.miregistro.data.SesionPreferencias
import cl.duocuc.miregistro.data.UsuarioDbHelper
import cl.duocuc.miregistro.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val db = UsuarioDbHelper(application)
    private val sesion = SesionPreferencias(application)

    private val _usuarioActual = MutableStateFlow<Usuario?>(db.buscarPorId(sesion.leer()))
    val usuarioActual: StateFlow<Usuario?> = _usuarioActual.asStateFlow()

    private val _mensajeError = MutableStateFlow<String?>(null)
    val mensajeError: StateFlow<String?> = _mensajeError.asStateFlow()

    fun iniciarSesion(correo: String, contrasena: String, onResultado: (Boolean, Long) -> Unit) {
        if (correo.isBlank() || contrasena.isBlank()) {
            _mensajeError.value = "Por favor completa todos los campos"
            onResultado(false, 0L)
            return
        }
        val usuario = db.autenticar(correo.trim(), contrasena)
        if (usuario != null) {
            sesion.guardar(usuario.id)
            _usuarioActual.value = usuario
            _mensajeError.value = null
            onResultado(true, usuario.idRol)
        } else {
            _mensajeError.value = "Correo o contraseña incorrectos"
            onResultado(false, 0L)
        }
    }

    fun registrar(
        nombre: String,
        correo: String,
        contrasena: String,
        ciudad: String,
        idRol: Long = 1L,
        onResultado: (Boolean, Long) -> Unit
    ) {
        if (nombre.isBlank() || correo.isBlank() || contrasena.isBlank()) {
            _mensajeError.value = "Todos los campos son obligatorios"
            onResultado(false, 0L)
            return
        }
        try {
            val nuevoUsuario = Usuario(
                nombre = nombre.trim(),
                correo = correo.trim(),
                contrasena = contrasena,
                ciudad = ciudad.ifBlank { "Santiago" },
                idRol = idRol
            )
            val id = db.insertarUsuario(nuevoUsuario)
            if (id > 0) {
                sesion.guardar(id)
                val creado = db.buscarPorId(id)
                _usuarioActual.value = creado
                _mensajeError.value = null
                onResultado(true, creado?.idRol ?: idRol)
            } else {
                _mensajeError.value = "Error al registrar usuario (correo ya existe?)"
                onResultado(false, 0L)
            }
        } catch (e: Exception) {
            _mensajeError.value = "Error: ${e.localizedMessage}"
            onResultado(false, 0L)
        }
    }

    fun cerrarSesion() {
        sesion.borrar()
        _usuarioActual.value = null
    }

    override fun onCleared() {
        db.close()
        super.onCleared()
    }
}
