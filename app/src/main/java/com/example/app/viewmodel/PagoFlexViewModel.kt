package com.example.app.viewmodel

import android.app.Application
import android.database.sqlite.SQLiteException
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.example.app.data.PagoFlexDbHelper
import com.example.app.data.SesionPreferencias
import com.example.app.data.remote.PagoFlexRepository
import com.example.app.data.remote.Resultado
import com.example.app.model.Comprobante
import com.example.app.model.Compromiso
import com.example.app.model.Reporte
import com.example.app.model.Servicio
import com.example.app.model.Usuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking

class PagoFlexViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val db = PagoFlexDbHelper(application)
    private val repo = PagoFlexRepository()
    private val sesion = SesionPreferencias(application)

    var ultimoError: String? = null
        private set

    private val _usuarioActual = MutableStateFlow<Usuario?>(null)
    val usuarioActual: StateFlow<Usuario?> = _usuarioActual.asStateFlow()

    private val _compromisos = MutableStateFlow<List<Compromiso>>(emptyList())
    val compromisos: StateFlow<List<Compromiso>> = _compromisos.asStateFlow()

    private val _servicios = MutableStateFlow<List<Servicio>>(emptyList())
    val servicios: StateFlow<List<Servicio>> = _servicios.asStateFlow()

    private val _reportes = MutableStateFlow<List<Reporte>>(emptyList())
    val reportes: StateFlow<List<Reporte>> = _reportes.asStateFlow()

    private fun <T> escritura(fallo: T, accion: () -> T): T {
        return try {
            accion()
        } catch (_: SQLiteException) {
            fallo
        }
    }

    private fun <T> red(bloque: suspend () -> Resultado<T>): T? {
        return when (val r = runBlocking(Dispatchers.IO) { bloque() }) {
            is Resultado.Ok -> { ultimoError = null; r.dato }
            is Resultado.Error -> { ultimoError = r.mensaje; Log.w("PagoFlex", r.mensaje); null }
        }
    }

    private fun <T> redOk(bloque: suspend () -> Resultado<T>): Boolean =
        runBlocking(Dispatchers.IO) { bloque() }.let {
            if (it is Resultado.Error) { ultimoError = it.mensaje; Log.w("PagoFlex", it.mensaje) } else ultimoError = null
            it is Resultado.Ok
        }

    fun guardarSesion(id: Long) {
        sesion.guardar(id)
    }

    fun recuperarSesion(): Usuario? {
        val id = sesion.leer()
        if (id == -1L) return null
        return red { repo.buscarPorId(id) }.also { usuario ->
            if (usuario == null && ultimoError?.contains("conexión", ignoreCase = true) != true) {
                sesion.borrar()
            }
        }
    }

    fun iniciarSesion(identificador: String, contrasena: String): Usuario? =
        red { repo.iniciarSesion(identificador, contrasena) }

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

    fun registrarUsuario(usuario: Usuario, clave: String): Usuario? =
        red {
            repo.registrarUsuario(
                rut = usuario.rut, nombre = usuario.nombre, correo = usuario.correo, contrasena = clave,
                rolId = usuario.rolId, telefono = usuario.telefono, direccion = usuario.direccion, ciudad = usuario.ciudad
            )
        }

    fun buscarPorId(id: Long): Usuario? = red { repo.buscarPorId(id) }

    fun actualizarUsuario(usuario: Usuario): Int =
        if (redOk { repo.actualizarUsuario(usuario) }) 1 else 0

    fun actualizarAvatar(id: Long, bytes: ByteArray): Boolean =
        redOk { repo.actualizarAvatar(id, bytes) }

    fun cambiarContrasena(id: Long, actual: String, nueva: String): Boolean =
        redOk { repo.cambiarContrasena(id, actual, nueva) }

    fun listarCompromisos(id: Long? = null): List<Compromiso> =
        red { repo.listarCompromisos(usuarioId = id) } ?: emptyList()

    fun eliminarCompromiso(folio: String, id: Long): Boolean =
        redOk { repo.eliminarCompromiso(folio) }

    fun cargarCompromisos(compromisos: List<Compromiso>) {
        _compromisos.value = compromisos
    }

    fun confirmarPago(folio: String, medio: String): Comprobante? =
        red { repo.confirmarPago(folio, canal = "App") }

    fun listarComprobantes(id: Long): List<Comprobante> =
        red { repo.listarComprobantes(id) } ?: emptyList()

    fun buscarComprobante(folio: String): Comprobante? =
        red { repo.buscarComprobante(folio) }

    fun pagar(folio: String) {
        _compromisos.update { lista ->
            lista.map { compromiso ->
                if (compromiso.folio == folio) compromiso.copy(estado = "Pagado") else compromiso
            }
        }
    }

    fun listarReportes(): List<Reporte> =
        red { repo.listarReportes() } ?: emptyList()

    fun cargarReportes(reportes: List<Reporte>) {
        _reportes.value = reportes
    }

    fun registrarReporte(folio: String, motivo: String): Boolean {
        val usuarioId = _usuarioActual.value?.id ?: return false
        return redOk { repo.registrarReporte(folio, usuarioId, motivo) }
    }

    fun actualizarEstadoReporte(folio: String, estado: String): Boolean =
        redOk { repo.tomarReporte(folio) }

    fun resolverReporte(folio: String, estado: String, respuesta: String): Boolean =
        redOk { repo.resolverReporte(folio, estado, respuesta) }

    fun listarCompromisosEjecutivo(id: Long): List<Compromiso> = db.listarCompromisosEjecutivo(id)

    fun montosPagadosEjecutivo(id: Long): Map<String, Int> = db.montosPagadosEjecutivo(id)

    fun registrarCompromisoEjecutivo(id: Long, rut: String, concepto: String, monto: Int, fecha: String, cuota: String): Boolean =
        escritura(false) { db.registrarCompromisoEjecutivo(id, rut, concepto, monto, fecha, cuota) }

    fun modificarCuotaEjecutivo(id: Long, folio: String, monto: Int, fecha: String): Boolean =
        escritura(false) { db.modificarCuotaEjecutivo(id, folio, monto, fecha) }

    fun anularCompromisoEjecutivo(id: Long, folio: String, motivo: String): Boolean =
        escritura(false) { db.anularCompromisoEjecutivo(id, folio, motivo) }

    fun listarReportesEjecutivo(id: Long): List<Reporte> = db.listarReportesEjecutivo(id)

    fun listarServicios(id: Long): List<Servicio> = db.listarServicios(id)

    fun cargarServicios(servicios: List<Servicio>) {
        _servicios.value = servicios
    }

    fun insertarServicio(servicio: Servicio): Long =
        escritura(-1L) { db.insertarServicio(servicio) }

    fun esEmpresaGestionada(nombre: String): Boolean = db.esEmpresaGestionada(nombre)

    fun crearCuotasServicio(usuarioId: Long, folio: String, cantidad: Int): Boolean =
        escritura(false) { db.crearCuotasServicio(usuarioId, folio, cantidad) }

    fun aplazarCuotaServicio(usuarioId: Long, folio: String): Boolean =
        escritura(false) { db.aplazarCuotaServicio(usuarioId, folio) }

    fun agregarServicio(servicio: Servicio) {
        _servicios.update { lista -> listOf(servicio) + lista }
    }

    override fun onCleared() {
        db.close()
        super.onCleared()
    }
}