package com.example.app.data.remote

import android.util.Base64
import com.example.app.model.Comprobante
import com.example.app.model.Compromiso
import com.example.app.model.Reporte
import com.example.app.model.Rol
import com.example.app.model.Servicio
import com.example.app.model.Usuario
import com.google.gson.Gson
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

sealed class Resultado<out T> {
    data class Ok<T>(val dato: T) : Resultado<T>()
    data class Error(val mensaje: String, val codigo: Int? = null) : Resultado<Nothing>()
}

class PagoFlexRepository(
    private val usuarios: UsuariosApi = ApiClient.usuarios,
    private val cobranza: CobranzaApi = ApiClient.cobranza,
    private val habito: HabitoApi = ApiClient.habito,
    private val reportes: ReportesApi = ApiClient.reportes
) {
    private val gson = Gson()

    private suspend fun <T> llamar(bloque: suspend () -> T): Resultado<T> = withContext(Dispatchers.IO) {
        try {
            Resultado.Ok(bloque())
        } catch (e: CancellationException) {
            throw e
        } catch (e: HttpException) {
            val cuerpo = try { gson.fromJson(e.response()?.errorBody()?.string(), ErrorApi::class.java) } catch (_: Exception) { null }
            val detalle = cuerpo?.detalles?.values?.firstOrNull()
            Resultado.Error(detalle ?: cuerpo?.mensaje ?: cuerpo?.detail ?: "Error ${e.code()} del servidor", e.code())
        } catch (_: IOException) {
            Resultado.Error("No hay conexión con el servidor. Revisa que el gateway esté encendido.")
        } catch (e: Exception) {
            Resultado.Error(e.message ?: "Ocurrió un error inesperado")
        }
    }

    suspend fun iniciarSesion(identificador: String, contrasena: String) =
        llamar { usuarios.login(LoginRequest(identificador, contrasena)).aModelo() }

    suspend fun registrarUsuario(
        rut: String, nombre: String, correo: String, contrasena: String, rolId: Long?,
        telefono: String = "", direccion: String = "", ciudad: String = ""
    ) = llamar {
        usuarios.registrar(RegistroUsuarioRequest(rut, nombre, correo, contrasena, rolId, telefono, direccion, ciudad)).aModelo()
    }

    suspend fun buscarPorId(id: Long) = llamar { usuarios.obtener(id).aModelo() }
    suspend fun listarUsuarios() = llamar { usuarios.listar().map { it.aModelo() } }
    suspend fun buscarUsuarios(texto: String) = llamar { usuarios.buscar(texto).map { it.aModelo() } }
    suspend fun listarRoles() = llamar { usuarios.roles().map { Rol(it.id, it.nombre) } }

    suspend fun actualizarUsuario(u: Usuario) = llamar {
        usuarios.actualizar(
            u.id, ActualizarUsuarioRequest(u.rut, u.nombre, u.correo, u.rolId, u.telefono, u.direccion, u.ciudad)
        ).aModelo()
    }

    suspend fun actualizarAvatar(id: Long, avatar: ByteArray) = llamar {
        usuarios.actualizarAvatar(id, AvatarRequest(Base64.encodeToString(avatar, Base64.NO_WRAP))).aModelo()
    }

    suspend fun cambiarContrasena(id: Long, actual: String, nueva: String) =
        llamar { usuarios.cambiarContrasena(id, CambiarContrasenaRequest(actual, nueva)).mensaje }

    suspend fun eliminarUsuario(id: Long) = llamar { usuarios.eliminar(id) }

    suspend fun solicitarCodigo(correo: String) = llamar { usuarios.solicitarCodigo(SolicitarCodigoRequest(correo)) }
    suspend fun verificarCodigo(correo: String, codigo: String) = llamar { usuarios.verificarCodigo(VerificarCodigoRequest(correo, codigo)).mensaje }
    suspend fun restablecerContrasena(correo: String, codigo: String, nueva: String) =
        llamar { usuarios.restablecer(RestablecerContrasenaRequest(correo, codigo, nueva)).mensaje }

    suspend fun listarCompromisosEjecutivo(id: Long) =
        llamar { cobranza.compromisosEjecutivo(id).map { it.aModelo() } }

    suspend fun montosPagadosEjecutivo(id: Long) = llamar { cobranza.montosPagadosEjecutivo(id) }

    suspend fun registrarCompromisoEjecutivo(id: Long, rut: String, concepto: String, monto: Int,
                                            fecha: String, cantidad: Int?) =
        llamar { cobranza.registrarCompromisoEjecutivo(id,
            RegistroCompromisoEjecutivoRequest(rut, concepto, monto, fecha, cantidad)) }

    suspend fun modificarCuotaEjecutivo(id: Long, folio: String, monto: Int, fecha: String) =
        llamar { cobranza.editarCuotaEjecutivo(id, folio, EditarCuotaEjecutivoRequest(monto, fecha)) }

    suspend fun anularCompromisoEjecutivo(id: Long, folio: String, motivo: String) =
        llamar { cobranza.anularCompromisoEjecutivo(id, folio, AnularCompromisoEjecutivoRequest(motivo)) }

    suspend fun listarServicios(usuarioId: Long) = llamar { cobranza.servicios(usuarioId) }
    suspend fun asociarServicio(servicio: Servicio) = llamar {
        cobranza.asociarServicio(
            AsociarServicioRequest(servicio.usuarioId, servicio.nombreEmpresa, servicio.rutPagador, servicio.alias)
        )
    }

    suspend fun listarCompromisos(usuarioId: Long? = null, estado: String? = null) =
        llamar { cobranza.compromisos(usuarioId, estado).map { it.aModelo() } }

    suspend fun crearCuotas(usuarioId: Long, folio: String, cantidad: Int) =
        llamar { cobranza.crearCuotas(folio, PlanCuotasRequest(usuarioId, cantidad)).map { it.aModelo() } }

    suspend fun aplazarCuota(usuarioId: Long, folio: String) =
        llamar { cobranza.aplazarCuota(folio, AplazamientoRequest(usuarioId)).map { it.aModelo() } }

    suspend fun crearCompromiso(
        folio: String, usuarioId: Long, rut: String?, nombre: String?, empresa: String,
        concepto: String, monto: Int, vencimiento: String, recargo: Int? = null, cuota: String? = null
    ) = llamar {
        cobranza.crear(CompromisoRequest(folio, usuarioId, rut, nombre, empresa, concepto, monto, vencimiento, recargo, cuota)).aModelo()
    }

    suspend fun eliminarCompromiso(folio: String) = llamar { cobranza.eliminar(folio) }
    suspend fun marcarCompromisoEnRevision(folio: String) = llamar { cobranza.marcarEnRevision(folio).aModelo() }

    suspend fun confirmarPago(folio: String, medioPago: String, canal: String = "App") =
        llamar { cobranza.pagar(folio, PagoRequest(medioPago, canal)).aModelo() }

    suspend fun buscarComprobante(folio: String) = llamar { cobranza.comprobantePorFolio(folio).aModelo() }
    suspend fun listarComprobantes(usuarioId: Long) = llamar { cobranza.comprobantes(usuarioId).map { it.aModelo() } }

    suspend fun registrarReporte(folio: String, usuarioId: Long, motivo: String, comentario: String? = null) =
        llamar { reportes.registrar(ReporteRequest(folio, usuarioId, motivo, comentario)).aModelo() }

    suspend fun listarReportes(usuarioId: Long? = null) = llamar { reportes.listar(usuarioId).map { it.aModelo() } }
    suspend fun tomarReporte(folio: String) = llamar { reportes.tomar(folio).aModelo() }
    suspend fun resolverReporte(folio: String, estadoCompromiso: String, respuesta: String) =
        llamar { reportes.resolver(folio, ResolucionReporteRequest(estadoCompromiso, respuesta)).aModelo() }
    suspend fun motivosReporte() = llamar { reportes.motivos() }

    suspend fun avisos(usuarioId: Long) = llamar { habito.avisos(usuarioId) }
    suspend fun recomendaciones(usuarioId: Long) = llamar { habito.recomendaciones(usuarioId) }
    suspend fun reconocimientos(usuarioId: Long) = llamar { habito.reconocimientos(usuarioId) }
    suspend fun catalogoReconocimientos(usuarioId: Long) = llamar { habito.catalogo(usuarioId) }
    suspend fun preferencias(usuarioId: Long) = llamar { habito.preferencias(usuarioId) }
    suspend fun guardarPreferencias(usuarioId: Long, diasAnticipacion: Int) =
        llamar { habito.guardarPreferencias(usuarioId, PreferenciaRequest(diasAnticipacion)) }
    suspend fun estadisticas(usuarioId: Long) = llamar { habito.estadisticas(usuarioId) }
    suspend fun pagosHabito(usuarioId: Long) = llamar { habito.pagos(usuarioId) }
    suspend fun registrarPagoHabito(usuarioId: Long, folio: String, vencimiento: String, fechaPago: String) =
        llamar { habito.registrarPago(PagoHabitoRequest(usuarioId, folio, vencimiento, fechaPago)) }
}

private fun UsuarioDto.aModelo() = Usuario(
    id = id, rut = rut, nombre = nombre, correo = correo, rolId = rolId,
    rolNombre = rolNombre.orEmpty(), telefono = telefono.orEmpty(),
    direccion = direccion.orEmpty(), ciudad = ciudad.orEmpty(),
    avatar = avatar?.takeIf { it.isNotBlank() }?.let { Base64.decode(it, Base64.DEFAULT) }
)

private fun CompromisoDto.aModelo() = Compromiso(
    folio = folio, empresa = empresa,
    usuario = Usuario(id = usuarioId, rut = rutUsuario.orEmpty(), nombre = nombreUsuario.orEmpty(), correo = "", rolId = 1L),
    concepto = concepto, monto = monto, vencimiento = vencimiento, estado = estado,
    recargo = recargo ?: 0, numeroCuota = numeroCuota.orEmpty(), planId = planId.orEmpty()
)

private fun ComprobanteDto.aModelo() = Comprobante(
    numero = numero, folioCompromiso = folioCompromiso, empresa = empresa, concepto = concepto,
    montoPagado = montoPagado, fechaHora = fechaHora,
    canal = canal ?: "App", medioPago = medioPago ?: "Tarjeta de débito"
)

private fun ReporteDto.aModelo() = Reporte(folio = folio, motivo = motivo, estado = estado, respuesta = respuesta)
