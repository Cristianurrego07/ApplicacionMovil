package com.example.app.data.remote

// Fechas: los ms las envían como texto ISO ("2026-11-25" / "2026-11-25T10:30:00").
// Enums con @JsonValue (estado, motivo...): llegan como la etiqueta ("En revisión", "Pendiente").

// ---------- Error común de los ms ----------
data class ErrorApi(
    val estado: Int? = null,
    val error: String? = null,
    val mensaje: String? = null,
    val detalles: Map<String, String>? = null
)

// ---------- msUsuarios ----------
data class UsuarioDto(
    val id: Long,
    val rut: String,
    val nombre: String,
    val correo: String,
    val rolId: Long,
    val rolNombre: String? = null,
    val telefono: String? = null,
    val direccion: String? = null,
    val ciudad: String? = null,
    val avatar: String? = null // Base64
)

data class RolDto(val id: Long, val nombre: String)

data class LoginRequest(val identificador: String, val contrasena: String)

data class RegistroUsuarioRequest(
    val rut: String,
    val nombre: String,
    val correo: String,
    val contrasena: String,
    val rolId: Long? = null,
    val telefono: String? = null,
    val direccion: String? = null,
    val ciudad: String? = null
)

data class ActualizarUsuarioRequest(
    val rut: String,
    val nombre: String,
    val correo: String,
    val rolId: Long? = null,
    val telefono: String? = null,
    val direccion: String? = null,
    val ciudad: String? = null
)

data class AvatarRequest(val avatar: String) // Base64
data class CambiarContrasenaRequest(val actual: String, val nueva: String)
data class MensajeDto(val mensaje: String)

data class SolicitarCodigoRequest(val correo: String)
data class SolicitudCodigoDto(
    val mensaje: String,
    val minutosExpiracion: Int,
    val codigoDesarrollo: String? = null
)
data class VerificarCodigoRequest(val correo: String, val codigo: String)
data class RestablecerContrasenaRequest(val correo: String, val codigo: String, val nuevaContrasena: String)

// ---------- msCobranza ----------
data class CompromisoDto(
    val folio: String,
    val usuarioId: Long,
    val rutUsuario: String? = null,
    val nombreUsuario: String? = null,
    val empresa: String,
    val concepto: String,
    val monto: Int,
    val vencimiento: String,
    val estado: String,
    val recargo: Int? = 0,
    val numeroCuota: String? = "",
    val montoAPagar: Int = 0
)

data class CompromisoRequest(
    val folio: String,
    val usuarioId: Long,
    val rutUsuario: String? = null,
    val nombreUsuario: String? = null,
    val empresa: String,
    val concepto: String,
    val monto: Int,
    val vencimiento: String,
    val recargo: Int? = null,
    val numeroCuota: String? = null
)

data class PagoRequest(val canal: String? = "App")

data class ComprobanteDto(
    val numero: String,
    val folioCompromiso: String,
    val empresa: String,
    val concepto: String,
    val montoPagado: Int,
    val fechaHora: String,
    val canal: String? = null,
    val medioPago: String? = null
)

// ---------- msReportes ----------
data class ReporteDto(
    val folio: String,
    val usuarioId: Long,
    val empresa: String? = null,
    val concepto: String? = null,
    val motivo: String,
    val comentario: String? = null,
    val estado: String,
    val respuesta: String? = null,
    val estadoCompromiso: String? = null,
    val fechaReporte: String? = null,
    val fechaActualizacion: String? = null
)

data class ReporteRequest(
    val folio: String,
    val usuarioId: Long,
    val motivo: String,      // etiqueta ("No reconozco este cobro") o nombre del enum
    val comentario: String? = null
)

data class ResolucionReporteRequest(
    val estadoCompromiso: String, // "Pendiente" o "Anulado"
    val respuesta: String? = null
)

data class ResumenReportesDto(val total: Long, val pendientes: Long, val enProceso: Long, val resueltos: Long)

// ---------- msHabito ----------
data class AvisoDto(
    val tipo: String,            // VENCIDO / VENCE_HOY / ...
    val folio: String,
    val empresa: String,
    val concepto: String,
    val vencimiento: String,
    val montoAPagar: Int,
    val mensaje: String
)

data class RecomendacionDto(val tipo: String, val titulo: String, val mensaje: String)

data class ReconocimientoDto(
    val codigo: String,
    val titulo: String,
    val descripcion: String,
    val fechaOtorgado: String? = null
)

data class CatalogoReconocimientoDto(
    val codigo: String,
    val titulo: String,
    val descripcion: String,
    val obtenido: Boolean? = null
)

data class PreferenciaDto(val usuarioId: Long, val avisosActivos: Boolean, val diasAnticipacion: Int)
data class PreferenciaRequest(val diasAnticipacion: Int)

data class PagoHabitoRequest(val usuarioId: Long, val folio: String, val vencimiento: String, val fechaPago: String)

data class PagoHabitoDto(
    val usuarioId: Long,
    val folio: String,
    val empresa: String,
    val concepto: String,
    val monto: Int,
    val vencimiento: String,
    val fechaPago: String,
    val puntual: Boolean,
    val diasAnticipacion: Long
)

data class RegistroPagoDto(val pago: PagoHabitoDto, val reconocimientosNuevos: List<ReconocimientoDto>)

data class EstadisticaDto(
    val totalPagos: Int,
    val pagosPuntuales: Int,
    val mejorRacha: Int,
    val promedioDiasAnticipacion: Double
)
