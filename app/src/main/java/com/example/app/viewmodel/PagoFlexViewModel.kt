package com.example.app.viewmodel

import android.app.Application
import android.database.sqlite.SQLiteException
import androidx.lifecycle.AndroidViewModel
import com.example.app.data.PagoFlexDbHelper
import com.example.app.data.SesionPreferencias
import com.example.app.model.Comprobante
import com.example.app.model.Compromiso
import com.example.app.model.Reporte
import com.example.app.model.Servicio
import com.example.app.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class PagoFlexViewModel(application: Application) : AndroidViewModel(application) {
    private val db = PagoFlexDbHelper(application)
    private val sesion = SesionPreferencias(application)

    // La UI solicita operaciones; el ViewModel administra la base y su cierre.
    private fun <T> escritura(fallo: T, accion: () -> T): T = try { accion() }
        catch (_: SQLiteException) { fallo }

    fun guardarSesion(id: Long) = sesion.guardar(id)
    fun recuperarSesion(): Usuario? {
        val id = sesion.leer()
        if (id == -1L) return null
        return db.buscarPorId(id).also { if (it == null) sesion.borrar() }
    }
    fun iniciarSesion(identificador: String, contrasena: String): Usuario? = db.iniciarSesion(identificador, contrasena)
    fun registrarUsuario(usuario: Usuario, clave: String): Usuario? = escritura(null as Usuario?) {
        val id = db.insertarUsuario(usuario, clave)
        if (id == -1L) null else db.buscarPorId(id)
    }
    fun listarCompromisos(id: Long? = null) = db.listarCompromisos(id)
    fun listarServicios(id: Long) = db.listarServicios(id)
    fun listarReportes() = db.listarReportes()
    fun listarComprobantes(id: Long) = db.listarComprobantes(id)
    fun buscarComprobante(folio: String) = db.buscarComprobante(folio)
    fun buscarPorId(id: Long) = db.buscarPorId(id)
    fun insertarServicio(servicio: Servicio) = escritura(-1L) { db.insertarServicio(servicio) }
    fun eliminarCompromiso(folio: String, id: Long) = escritura(false) { db.eliminarCompromiso(folio, id) }
    fun registrarReporte(folio: String, motivo: String) = escritura(false) { db.registrarReporte(folio, motivo) }
    fun confirmarPago(folio: String, medio: String): Comprobante? = escritura(null as Comprobante?) { db.confirmarPago(folio, medio) }
    fun actualizarEstadoReporte(folio: String, estado: String) = escritura(false) { db.actualizarEstadoReporte(folio, estado) }
    fun resolverReporte(folio: String, estado: String) = escritura(false) { db.resolverReporte(folio, estado) }
    fun actualizarUsuario(usuario: Usuario) = escritura(0) { db.actualizarUsuario(usuario) }
    fun actualizarAvatar(id: Long, bytes: ByteArray) = escritura(false) { db.actualizarAvatar(id, bytes) }
    fun cambiarContrasena(id: Long, actual: String, nueva: String) = escritura(false) { db.cambiarContrasena(id, actual, nueva) }

    override fun onCleared() { db.close(); super.onCleared() }

    private val _compromisos = MutableStateFlow<List<Compromiso>>(emptyList())
    val compromisos: StateFlow<List<Compromiso>> = _compromisos.asStateFlow()

    private val _reportes = MutableStateFlow<List<Reporte>>(emptyList())
    val reportes: StateFlow<List<Reporte>> = _reportes.asStateFlow()

    private val _usuarioActual = MutableStateFlow<Usuario?>(null)
    val usuarioActual: StateFlow<Usuario?> = _usuarioActual.asStateFlow()

    private val _servicios = MutableStateFlow<List<Servicio>>(emptyList())
    val servicios: StateFlow<List<Servicio>> = _servicios.asStateFlow()

    fun iniciarSesion(usuario: Usuario) {
        _usuarioActual.value = usuario
    }

    fun cerrarSesion() {
        sesion.borrar()
        _usuarioActual.value = null
        _servicios.value = emptyList()
        _compromisos.value = emptyList()
        _reportes.value = emptyList()
    }

    fun cargarServicios(servicios: List<Servicio>) {
        _servicios.value = servicios
    }

    fun cargarCompromisos(compromisos: List<Compromiso>) {
        _compromisos.value = compromisos
    }

    fun cargarReportes(reportes: List<Reporte>) {
        _reportes.value = reportes
    }

    fun agregarServicio(servicio: Servicio) {
        _servicios.update { lista -> listOf(servicio) + lista }
    }

    fun pagar(folio: String) {
        _compromisos.update { lista ->
            lista.map { if (it.folio == folio) it.copy(estado = "Pagado") else it }
        }
    }

}
