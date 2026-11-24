package com.example.app.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.app.validacion.Validaciones
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CompromisoEjecutivoViewModel : ViewModel() {
    var rut by mutableStateOf("")
    var concepto by mutableStateOf("")
    var monto by mutableStateOf("")
    var vencimiento by mutableStateOf("")
    var totalCuotas by mutableStateOf("")
    var errorRut by mutableStateOf<String?>(null)
    var errorConcepto by mutableStateOf<String?>(null)
    var errorMonto by mutableStateOf<String?>(null)
    var errorFecha by mutableStateOf<String?>(null)
    var errorTotalCuotas by mutableStateOf<String?>(null)
    var errorGeneral by mutableStateOf<String?>(null)

    fun guardar(onGuardar: (String, String, Int, String, String) -> Boolean): Boolean {
        errorRut = if (!Validaciones.rutValido(rut)) {
            "Ingresa un RUT válido con puntos y guion"
        } else {
            null
        }
        errorConcepto = if (concepto.isBlank()) {
            "Ingresa el concepto"
        } else {
            null
        }

        val cantidad = monto
            .takeIf { it.isNotEmpty() && it.all { digito -> digito in '0'..'9' } }
            ?.toIntOrNull()
        errorMonto = if (cantidad == null || cantidad <= 0) {
            "Ingresa un monto mayor a cero"
        } else {
            null
        }

        val formato = SimpleDateFormat("dd/MM/yyyy", Locale.US).apply { isLenient = false }
        val fecha = try {
            if (vencimiento.matches(Regex("""[0-9]{2}/[0-9]{2}/[0-9]{4}"""))) {
                formato.parse(vencimiento)
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
        val hoy = formato.parse(formato.format(Date()))!!
        errorFecha = when {
            fecha == null -> "Ingresa una fecha válida en formato DD/MM/AAAA"
            fecha.before(hoy) -> "La fecha no puede ser anterior a hoy"
            else -> null
        }
        val total = totalCuotas
            .takeIf { it.isNotEmpty() && it.all { digito -> digito in '0'..'9' } }
            ?.toIntOrNull()
        errorTotalCuotas = when {
            totalCuotas.isBlank() -> null
            total == null || total !in 1..120 -> "Ingresa entre 1 y 120 cuotas"
            cantidad != null && total > cantidad -> "El monto debe alcanzar para cada cuota"
            else -> null
        }
        val hayErrores = listOf(
            errorRut,
            errorConcepto,
            errorMonto,
            errorFecha,
            errorTotalCuotas
        ).any { it != null }

        if (hayErrores) {
            return false
        }

        val fechaSql = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(fecha!!)
        val cuota = total?.toString() ?: ""
        if (!onGuardar(rut, concepto.trim(), cantidad!!, fechaSql, cuota)) {
            errorGeneral = "No se pudo guardar. Verifica que el cliente exista y que el compromiso no esté repetido."
            return false
        }
        errorGeneral = null
        return true
    }
}
