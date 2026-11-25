package com.example.app.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EditarCuotaViewModel : ViewModel() {
    var monto by mutableStateOf("")
    var fecha by mutableStateOf("")
    var errorMonto by mutableStateOf<String?>(null)
    var errorFecha by mutableStateOf<String?>(null)
    var errorGeneral by mutableStateOf<String?>(null)

    fun cargar(montoActual: Int, fechaActual: String) {
        monto = montoActual.toString()
        fecha = try {
            val origen = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { isLenient = false }
            SimpleDateFormat("dd/MM/yyyy", Locale.US).format(origen.parse(fechaActual)!!)
        } catch (_: Exception) {
            ""
        }
        errorMonto = null
        errorFecha = null
        errorGeneral = null
    }

    suspend fun guardar(onGuardar: suspend (Int, String) -> Boolean): Boolean {
        val numero = monto
            .takeIf { it.isNotEmpty() && it.all { digito -> digito in '0'..'9' } }
            ?.toIntOrNull()
        errorMonto = if (numero == null || numero <= 0) {
            "Ingresa un monto mayor a cero"
        } else {
            null
        }

        val formato = SimpleDateFormat("dd/MM/yyyy", Locale.US).apply { isLenient = false }
        val fechaIngresada = try {
            if (fecha.matches(Regex("""[0-9]{2}/[0-9]{2}/[0-9]{4}"""))) {
                formato.parse(fecha)
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
        val hoy = formato.parse(formato.format(Date()))!!
        errorFecha = when {
            fechaIngresada == null -> "Ingresa una fecha válida en formato DD/MM/AAAA"
            fechaIngresada.before(hoy) -> "La fecha no puede ser anterior a hoy"
            else -> null
        }
        if (errorMonto != null || errorFecha != null) {
            return false
        }

        val fechaSql = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(fechaIngresada!!)
        if (!onGuardar(numero!!, fechaSql)) {
            errorGeneral = "No se pudo guardar. Comprueba que la cuota no esté pagada y que las fechas sigan el orden de las cuotas."
            return false
        }
        return true
    }
}
