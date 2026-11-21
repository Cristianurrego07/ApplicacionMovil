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

class PagoFlexViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val db = PagoFlexDbHelper(application)
    private val sesion = SesionPreferencias(application)

    private val _usuarioActual = MutableStateFlow<Usuario?>(null)
    val usuarioActual: StateFlow<Usuario?> = _usuarioActual.asStateFlow()

    private val _compromisos = MutableStateFlow<List<Compromiso>>(emptyList())
    val compromisos: StateFlow<List<Compromiso>> = _compromisos.asStateFlow()

    private val _servicios = MutableStateFlow<List<Servicio>>(emptyList())
    val servicios: StateFlow<List<Servicio>> = _servicios.asStateFlow()

    private val _reportes = MutableStateFlow<List<Reporte>>(emptyList())
    val reportes: StateFlow<List<Reporte>> = _reportes.asStateFlow()

    private fun <T> escritura(
        fallo: T,
        accion: () -> T
    ): T {
        return try {
            accion()
        } catch (_: SQLiteException) {
            fallo
        }
    }

    fun guardarSesion(id: Long) {
        sesion.guardar(id)
    }

    fun recuperarSesion(): Usuario? {
        val id = sesion.leer()

        if (id == -1L) {
            return null
        }

        return db.buscarPorId(id).also { usuario ->
            if (usuario == null) {
                sesion.borrar()
            }
        }
    }

    fun iniciarSesion(
        identificador: String,
        contrasena: String
    ): Usuario? {
        return db.iniciarSesion(identificador, contrasena)
    }

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

    fun registrarUsuario(
        usuario: Usuario,
        clave: String
    ): Usuario? {
        return escritura(null as Usuario?) {
            val id = db.insertarUsuario(usuario, clave)

            if (id == -1L) {
                null
            } else {
                db.buscarPorId(id)
            }
        }
    }

    fun buscarPorId(id: Long): Usuario? {
        return db.buscarPorId(id)
    }

    fun actualizarUsuario(usuario: Usuario): Int {
        return escritura(0) {
            db.actualizarUsuario(usuario)
        }
    }

    fun actualizarAvatar(
        id: Long,
        bytes: ByteArray
    ): Boolean {
        return escritura(false) {
            db.actualizarAvatar(id, bytes)
        }
    }

    fun cambiarContrasena(
        id: Long,
        actual: String,
        nueva: String
    ): Boolean {
        return escritura(false) {
            db.cambiarContrasena(id, actual, nueva)
        }
    }

    fun listarCompromisos(id: Long? = null): List<Compromiso> {
        return db.listarCompromisos(id)
    }

    fun listarCompromisosEjecutivo(id: Long): List<Compromiso> = db.listarCompromisosEjecutivo(id)

    fun montosPagadosEjecutivo(id: Long): Map<String, Int> = db.montosPagadosEjecutivo(id)

    fun registrarCompromisoEjecutivo(id: Long, rut: String, concepto: String, monto: Int, fecha: String, cuota: String): Boolean =
        escritura(false) { db.registrarCompromisoEjecutivo(id, rut, concepto, monto, fecha, cuota) }

    fun modificarCuotaEjecutivo(id: Long, folio: String, monto: Int, fecha: String): Boolean =
        escritura(false) { db.modificarCuotaEjecutivo(id, folio, monto, fecha) }

    fun anularCompromisoEjecutivo(id: Long, folio: String, motivo: String): Boolean =
        escritura(false) { db.anularCompromisoEjecutivo(id, folio, motivo) }

    fun cargarCompromisos(compromisos: List<Compromiso>) {
        _compromisos.value = compromisos
    }

    fun eliminarCompromiso(
        folio: String,
        id: Long
    ): Boolean {
        return escritura(false) {
            db.eliminarCompromiso(folio, id)
        }
    }

    fun listarServicios(id: Long): List<Servicio> {
        return db.listarServicios(id)
    }

    fun cargarServicios(servicios: List<Servicio>) {
        _servicios.value = servicios
    }

    fun insertarServicio(servicio: Servicio): Long {
        return escritura(-1L) {
            db.insertarServicio(servicio)
        }
    }

    fun esEmpresaGestionada(nombre: String): Boolean = db.esEmpresaGestionada(nombre)

    fun crearCuotasServicio(usuarioId: Long, folio: String, cantidad: Int): Boolean =
        escritura(false) { db.crearCuotasServicio(usuarioId, folio, cantidad) }

    fun aplazarCuotaServicio(usuarioId: Long, folio: String): Boolean =
        escritura(false) { db.aplazarCuotaServicio(usuarioId, folio) }

    fun agregarServicio(servicio: Servicio) {
        _servicios.update { lista ->
            listOf(servicio) + lista
        }
    }

    fun listarReportes(): List<Reporte> {
        return db.listarReportes()
    }

    fun listarReportesEjecutivo(id: Long): List<Reporte> = db.listarReportesEjecutivo(id)

    fun cargarReportes(reportes: List<Reporte>) {
        _reportes.value = reportes
    }

    fun registrarReporte(
        folio: String,
        motivo: String
    ): Boolean {
        return escritura(false) {
            db.registrarReporte(folio, motivo)
        }
    }

    fun actualizarEstadoReporte(
        folio: String,
        estado: String
    ): Boolean {
        return escritura(false) {
            db.actualizarEstadoReporte(folio, estado)
        }
    }

    fun resolverReporte(
        folio: String,
        estado: String,
        respuesta: String
    ): Boolean {
        return escritura(false) {
            db.resolverReporte(folio, estado, respuesta)
        }
    }

    fun confirmarPago(
        folio: String,
        medio: String
    ): Comprobante? {
        return escritura(null as Comprobante?) {
            db.confirmarPago(folio, medio)
        }
    }

    fun listarComprobantes(id: Long): List<Comprobante> {
        return db.listarComprobantes(id)
    }

    fun buscarComprobante(folio: String): Comprobante? {
        return db.buscarComprobante(folio)
    }

    fun pagar(folio: String) {
        _compromisos.update { lista ->
            lista.map { compromiso ->
                if (compromiso.folio == folio) {
                    compromiso.copy(estado = "Pagado")
                } else {
                    compromiso
                }
            }
        }
    }

    override fun onCleared() {
        db.close()
        super.onCleared()
    }
}