package com.example.app.data.remote

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface UsuariosApi {
    @POST("api/usuarios/login") suspend fun login(@Body req: LoginRequest): UsuarioDto
    @POST("api/usuarios") suspend fun registrar(@Body req: RegistroUsuarioRequest): UsuarioDto
    @GET("api/usuarios") suspend fun listar(): List<UsuarioDto>
    @GET("api/usuarios/buscar") suspend fun buscar(@Query("texto") texto: String): List<UsuarioDto>
    @GET("api/usuarios/rol/{rolId}") suspend fun listarPorRol(@Path("rolId") rolId: Long): List<UsuarioDto>
    @GET("api/usuarios/{id}") suspend fun obtener(@Path("id") id: Long): UsuarioDto
    @PUT("api/usuarios/{id}") suspend fun actualizar(@Path("id") id: Long, @Body req: ActualizarUsuarioRequest): UsuarioDto
    @PUT("api/usuarios/{id}/avatar") suspend fun actualizarAvatar(@Path("id") id: Long, @Body req: AvatarRequest): UsuarioDto
    @PUT("api/usuarios/{id}/contrasena") suspend fun cambiarContrasena(@Path("id") id: Long, @Body req: CambiarContrasenaRequest): MensajeDto
    @DELETE("api/usuarios/{id}") suspend fun eliminar(@Path("id") id: Long)

    @GET("api/roles") suspend fun roles(): List<RolDto>

    @POST("api/usuarios/recuperar/solicitar") suspend fun solicitarCodigo(@Body req: SolicitarCodigoRequest): SolicitudCodigoDto
    @POST("api/usuarios/recuperar/verificar") suspend fun verificarCodigo(@Body req: VerificarCodigoRequest): MensajeDto
    @POST("api/usuarios/recuperar/restablecer") suspend fun restablecer(@Body req: RestablecerContrasenaRequest): MensajeDto
}

interface CobranzaApi {
    @GET("api/cobranza/compromisos")
    suspend fun compromisos(@Query("usuarioId") usuarioId: Long? = null, @Query("estado") estado: String? = null): List<CompromisoDto>

    @GET("api/cobranza/compromisos/{folio}") suspend fun compromiso(@Path("folio") folio: String): CompromisoDto
    @POST("api/cobranza/compromisos") suspend fun crear(@Body req: CompromisoRequest): CompromisoDto
    @DELETE("api/cobranza/compromisos/{folio}") suspend fun eliminar(@Path("folio") folio: String)
    @POST("api/cobranza/compromisos/{folio}/pago") suspend fun pagar(@Path("folio") folio: String, @Body req: PagoRequest): ComprobanteDto
    @PATCH("api/cobranza/compromisos/{folio}/revision") suspend fun marcarEnRevision(@Path("folio") folio: String): CompromisoDto

    @GET("api/cobranza/comprobantes") suspend fun comprobantes(@Query("usuarioId") usuarioId: Long): List<ComprobanteDto>
    @GET("api/cobranza/comprobantes/folio/{folio}") suspend fun comprobantePorFolio(@Path("folio") folio: String): ComprobanteDto
}

interface HabitoApi {
    @GET("api/habito/avisos") suspend fun avisos(@Query("usuarioId") usuarioId: Long): List<AvisoDto>
    @GET("api/habito/recomendaciones") suspend fun recomendaciones(@Query("usuarioId") usuarioId: Long): List<RecomendacionDto>
    @GET("api/habito/reconocimientos") suspend fun reconocimientos(@Query("usuarioId") usuarioId: Long): List<ReconocimientoDto>
    @GET("api/habito/reconocimientos/catalogo") suspend fun catalogo(@Query("usuarioId") usuarioId: Long): List<CatalogoReconocimientoDto>
    @GET("api/habito/preferencias/{usuarioId}") suspend fun preferencias(@Path("usuarioId") usuarioId: Long): PreferenciaDto
    @PUT("api/habito/preferencias/{usuarioId}") suspend fun guardarPreferencias(@Path("usuarioId") usuarioId: Long, @Body req: PreferenciaRequest): PreferenciaDto
    @POST("api/habito/pagos") suspend fun registrarPago(@Body req: PagoHabitoRequest): RegistroPagoDto
    @GET("api/habito/pagos") suspend fun pagos(@Query("usuarioId") usuarioId: Long): List<PagoHabitoDto>
    @GET("api/habito/estadisticas/{usuarioId}") suspend fun estadisticas(@Path("usuarioId") usuarioId: Long): EstadisticaDto
}

interface ReportesApi {
    @POST("api/reportes") suspend fun registrar(@Body req: ReporteRequest): ReporteDto
    @GET("api/reportes")
    suspend fun listar(@Query("usuarioId") usuarioId: Long? = null, @Query("estado") estado: String? = null): List<ReporteDto>
    @GET("api/reportes/resumen") suspend fun resumen(): ResumenReportesDto
    @GET("api/reportes/motivos") suspend fun motivos(): List<String>
    @GET("api/reportes/{folio}") suspend fun obtener(@Path("folio") folio: String): ReporteDto
    @PATCH("api/reportes/{folio}/tomar") suspend fun tomar(@Path("folio") folio: String): ReporteDto
    @PATCH("api/reportes/{folio}/resolver") suspend fun resolver(@Path("folio") folio: String, @Body req: ResolucionReporteRequest): ReporteDto
}
