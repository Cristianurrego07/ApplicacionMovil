package com.example.app.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
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

    private val _montosPagados = MutableStateFlow<Map<String, Int>>(emptyMap())
    val montosPagados: StateFlow<Map<String, Int>> = _montosPagados.asStateFlow()

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

    suspend fun iniciarSesion(identificador: String, contrasena: String): Resultado<Usuario> =
        repo.iniciarSesion(identificador, contrasena)

    fun iniciarSesion(usuario: Usuario) {
        _usuarioActual.value = usuario
    }

    fun cerrarSesion() {
        sesion.borrar()
        _usuarioActual.value = null
        _servicios.value = emptyList()
        _compromisos.value = emptyList()
        _reportes.value = emptyList()
        _montosPagados.value = emptyMap()
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
        red { repo.confirmarPago(folio, medioPago = medio) }

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

    fun listarCompromisosEjecutivo(id: Long): List<Compromiso> =
        red { repo.listarCompromisosEjecutivo(id) } ?: emptyList()

    suspend fun cargarMontosPagadosEjecutivo(id: Long) {
        when (val resultado = repo.montosPagadosEjecutivo(id)) {
            is Resultado.Ok -> _montosPagados.value = resultado.dato
            is Resultado.Error -> Log.w("PagoFlex", resultado.mensaje)
        }
    }

    suspend fun registrarCompromisoEjecutivo(id: Long, rut: String, concepto: String, monto: Int, fecha: String, cuota: String): Boolean =
        repo.registrarCompromisoEjecutivo(id, rut, concepto, monto, fecha, cuota.toIntOrNull()) is Resultado.Ok

    suspend fun modificarCuotaEjecutivo(id: Long, folio: String, monto: Int, fecha: String): Boolean =
        repo.modificarCuotaEjecutivo(id, folio, monto, fecha) is Resultado.Ok

    suspend fun anularCompromisoEjecutivo(id: Long, folio: String, motivo: String): Boolean =
        repo.anularCompromisoEjecutivo(id, folio, motivo) is Resultado.Ok

    suspend fun listarServicios(id: Long): List<Servicio> =
        when (val respuesta = repo.listarServicios(id)) {
            is Resultado.Ok -> respuesta.dato
            is Resultado.Error -> _servicios.value.filter { it.usuarioId == id }
        }

    fun cargarServicios(servicios: List<Servicio>) {
        _servicios.value = servicios
    }

    suspend fun insertarServicio(servicio: Servicio): Resultado<Servicio> =
        repo.asociarServicio(servicio)

    suspend fun crearCuotasServicio(usuarioId: Long, folio: String, cantidad: Int) =
        incorporarCuotas(repo.crearCuotas(usuarioId, folio, cantidad))

    suspend fun aplazarCuotaServicio(usuarioId: Long, folio: String) =
        incorporarCuotas(repo.aplazarCuota(usuarioId, folio))

    private fun incorporarCuotas(resultado: Resultado<List<Compromiso>>): Resultado<List<Compromiso>> {
        if (resultado is Resultado.Ok) {
            val modificados = resultado.dato.map { it.folio }.toSet()
            _compromisos.update { actuales ->
                (actuales.filterNot { it.folio in modificados } + resultado.dato)
                    .sortedBy { it.vencimiento }
            }
        }
        return resultado
    }

    fun agregarServicio(servicio: Servicio) {
        _servicios.update { lista -> listOf(servicio) + lista }
    }
}