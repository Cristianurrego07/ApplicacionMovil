package com.example.miprimeraapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.miprimeraapp.data.SesionPreferencias
import com.example.miprimeraapp.data.UsuarioDbHelper
import com.example.miprimeraapp.model.Rol
import com.example.miprimeraapp.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UsuariosViewModel(application: Application) : AndroidViewModel(application) {

    private val db = UsuarioDbHelper(application)
    private val sesion = SesionPreferencias(application)

    private val _filtro = MutableStateFlow("")
    val filtro: StateFlow<String> = _filtro.asStateFlow()

    private val _usuarios = MutableStateFlow(db.listarUsuarios())
    val usuarios: StateFlow<List<Usuario>> = _usuarios.asStateFlow()

    val roles: List<Rol> = db.listarRoles()

    private val _usuarioActual = MutableStateFlow(db.buscarPorId(sesion.leer()))
    val usuarioActual: StateFlow<Usuario?> = _usuarioActual.asStateFlow()

    private fun recargar() {
        val texto = _filtro.value.trim()
        val todos = db.listarUsuarios()
        _usuarios.value = if (texto.isEmpty()) todos else todos.filter {
            it.nombre.contains(texto, ignoreCase = true) || it.correo.contains(texto, ignoreCase = true)
        }
    }

    fun buscar(texto: String) {
        _filtro.value = texto
        recargar()
    }

    fun obtener(id: Long): Usuario? = db.buscarPorId(id)

    fun registrar(usuario: Usuario) {
        val id = db.insertarUsuario(usuario)
        _usuarioActual.value = db.buscarPorId(id)
        sesion.guardar(id)
        recargar()
    }

    fun actualizar(editado: Usuario) {
        db.actualizarUsuario(editado)
        if (_usuarioActual.value?.id == editado.id) _usuarioActual.value = db.buscarPorId(editado.id)
        recargar()
    }

    fun eliminar(usuario: Usuario) {
        db.eliminarUsuario(usuario.id)
        if (_usuarioActual.value?.id == usuario.id) cerrarSesion()
        recargar()
    }

    fun cerrarSesion() {
        _usuarioActual.value = null
        sesion.borrar()
    }

    override fun onCleared() {
        db.close()
        super.onCleared()
    }
}
