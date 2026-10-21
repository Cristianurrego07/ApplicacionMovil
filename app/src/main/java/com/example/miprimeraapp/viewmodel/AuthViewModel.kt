package com.example.miprimeraapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.miprimeraapp.data.SesionPreferencias
import com.example.miprimeraapp.data.UsuarioDbHelper
import com.example.miprimeraapp.model.Usuario
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
        apellido: String = "",
        rut: String = "12.345.678-9",
        correo: String,
        contrasena: String,
        telefono: String = "+56 9 9876 5432",
        direccion: String = "Av. Providencia 1234",
        ciudad: String = "Santiago",
        idRol: Long = 1L,
        onResultado: (Boolean, Long) -> Unit
    ) {
        if (nombre.isBlank() || correo.isBlank() || contrasena.isBlank()) {
            _mensajeError.value = "Todos los campos obligatorios (*)"
            onResultado(false, 0L)
            return
        }
        try {
            val nuevoUsuario = Usuario(
                nombre = nombre.trim(),
                apellido = apellido.trim(),
                rut = rut.ifBlank { "12.345.678-9" },
                correo = correo.trim(),
                contrasena = contrasena,
                telefono = telefono.ifBlank { "+56 9 9876 5432" },
                direccion = direccion.ifBlank { "Av. Providencia 1234" },
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

    fun actualizarPerfil(usuario: Usuario): Boolean {
        val lineas = db.actualizarUsuario(usuario)
        if (lineas > 0) {
            _usuarioActual.value = db.buscarPorId(usuario.id)
            return true
        }
        return false
    }

    fun cambiarContrasena(idUsuario: Long, actual: String, nueva: String): Boolean {
        val u = db.buscarPorId(idUsuario)
        if (u != null && u.contrasena == actual) {
            val exito = db.actualizarContrasena(idUsuario, nueva)
            if (exito) {
                _usuarioActual.value = db.buscarPorId(idUsuario)
            }
            return exito
        }
        return false
    }

    fun recuperarContrasena(correoOrRut: String, nuevaContrasena: String = "PagoFlex2026"): Usuario? {
        return db.recuperarContrasena(correoOrRut, nuevaContrasena)
    }

    fun recargarUsuario() {
        val id = _usuarioActual.value?.id ?: sesion.leer()
        if (id > 0) {
            _usuarioActual.value = db.buscarPorId(id)
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
