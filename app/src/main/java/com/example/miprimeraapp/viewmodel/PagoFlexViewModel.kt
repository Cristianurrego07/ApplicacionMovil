package com.example.miprimeraapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.miprimeraapp.data.UsuarioDbHelper
import com.example.miprimeraapp.model.CobranzaEmpresa
import com.example.miprimeraapp.model.Comprobante
import com.example.miprimeraapp.model.Compromiso
import com.example.miprimeraapp.model.ReporteProblema
import com.example.miprimeraapp.model.Servicio
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PagoFlexViewModel(application: Application) : AndroidViewModel(application) {
    private val db = UsuarioDbHelper(application)

    private val _compromisos = MutableStateFlow<List<Compromiso>>(emptyList())
    val compromisos: StateFlow<List<Compromiso>> = _compromisos.asStateFlow()

    private val _servicios = MutableStateFlow<List<Servicio>>(emptyList())
    val servicios: StateFlow<List<Servicio>> = _servicios.asStateFlow()

    private val _comprobantes = MutableStateFlow<List<Comprobante>>(emptyList())
    val comprobantes: StateFlow<List<Comprobante>> = _comprobantes.asStateFlow()

    private val _reportes = MutableStateFlow<List<ReporteProblema>>(emptyList())
    val reportes: StateFlow<List<ReporteProblema>> = _reportes.asStateFlow()

    private val _cobranzas = MutableStateFlow<List<CobranzaEmpresa>>(emptyList())
    val cobranzas: StateFlow<List<CobranzaEmpresa>> = _cobranzas.asStateFlow()

    fun cargarCompromisos(usuarioId: Long) {
        _compromisos.value = db.listarCompromisos(usuarioId)
    }

    fun cargarServicios(usuarioId: Long) {
        _servicios.value = db.listarServicios(usuarioId)
    }

    fun cargarComprobantes(usuarioId: Long) {
        _comprobantes.value = db.listarComprobantes(usuarioId)
    }

    fun pagarCompromiso(id: Long, usuarioId: Long) {
        db.pagarCompromiso(id)
        cargarCompromisos(usuarioId)
        cargarComprobantes(usuarioId)
    }

    fun confirmarPago(folio: String, usuarioId: Long, medioPago: String = "Pago simulado"): Comprobante? {
        val comprobante = db.confirmarPago(folio, medioPago)
        if (comprobante != null) {
            cargarCompromisos(usuarioId)
            cargarComprobantes(usuarioId)
        }
        return comprobante
    }

    fun cargarReportes() {
        _reportes.value = db.listarReportes()
    }

    fun actualizarEstadoReporte(id: Long, nuevoEstado: String) {
        db.actualizarEstadoReporte(id, nuevoEstado)
        cargarReportes()
    }

    fun cargarCobranzas() {
        _cobranzas.value = db.listarCobranzas()
    }

    fun registrarCobranza(
        codigo: String,
        empresa: String,
        monto: Double,
        vencimiento: String,
        descripcion: String,
        estado: String = "En curso"
    ) {
        val nueva = CobranzaEmpresa(
            codigo = codigo,
            empresa = empresa,
            monto = monto,
            vencimiento = vencimiento,
            descripcion = descripcion,
            estado = estado
        )
        db.insertarCobranza(nueva)
        cargarCobranzas()
    }

    fun anularCobranza(id: Long) {
        db.anularCobranza(id)
        cargarCobranzas()
    }

    override fun onCleared() {
        db.close()
        super.onCleared()
    }
}
