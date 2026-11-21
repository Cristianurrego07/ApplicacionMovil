package com.example.app.notificaciones

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.app.data.PagoFlexDbHelper
import com.example.app.data.SesionPreferencias
import java.text.SimpleDateFormat
import java.util.Locale

class AvisosWorker(context: Context, parametros: WorkerParameters) : Worker(context, parametros) {
    override fun doWork(): Result {
        val usuarioId = SesionPreferencias(applicationContext).leer()
        if (usuarioId == -1L) return Result.success()

        val preferencias = NotificacionesPreferencias(applicationContext, usuarioId)
        if (!preferencias.activadas()) return Result.success()

        val formato = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        formato.isLenient = false
        val db = PagoFlexDbHelper(applicationContext)

        return try {
            val usuario = db.buscarPorId(usuarioId)
            val esEjecutivo = usuario?.rolNombre?.equals("EJECUTIVO", ignoreCase = true) == true
            val compromisos = if (esEjecutivo) db.listarCompromisosEjecutivo(usuarioId) else db.listarCompromisos(usuarioId)
            val hoyTexto = formato.format(java.util.Date())
            val formatoUtc = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
                isLenient = false
                timeZone = java.util.TimeZone.getTimeZone("UTC")
            }
            val hoy = formatoUtc.parse(hoyTexto)!!

            for (compromiso in compromisos) {
                val dias = preferencias.dias(compromiso.folio)
                if (dias == 0) continue
                val vencimiento = formatoUtc.parse(compromiso.vencimiento) ?: continue
                val diasDesdeVencimiento = (hoy.time - vencimiento.time) / 86_400_000L

                val correspondeAvisar = when (compromiso.estado) {
                    "Pendiente" -> diasDesdeVencimiento == -dias.toLong()
                    "Vencido" -> diasDesdeVencimiento > 0 && diasDesdeVencimiento % dias == 0L
                    else -> false
                }

                if (correspondeAvisar && !preferencias.fueEnviado(compromiso.folio, hoyTexto)) {
                    val fecha = SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("es-CL"))
                        .apply { timeZone = java.util.TimeZone.getTimeZone("UTC") }
                        .format(vencimiento)
                    val enviado = NotificacionesHelper.mostrar(
                        applicationContext, compromiso.folio, compromiso.concepto, fecha,
                        vencido = compromiso.estado == "Vencido"
                    )
                    if (enviado) preferencias.marcarEnviado(compromiso.folio, hoyTexto)
                }
            }
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        } finally {
            db.close()
        }
    }
}
