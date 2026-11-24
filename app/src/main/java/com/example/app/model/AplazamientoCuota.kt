package com.example.app.model

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object AplazamientoCuota {
    fun montoConRecargo(monto: Int): Long {
        require(monto > 0)
        return (monto.toLong() * 102 + 50) / 100
    }

    fun mesSiguiente(fecha: String): String {
        require(fecha.matches(Regex("""[0-9]{4}-[0-9]{2}-[0-9]{2}""")))
        val formato = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { isLenient = false }
        val fechaInicial = requireNotNull(formato.parse(fecha))
        val calendario = Calendar.getInstance().apply {
            time = fechaInicial
            add(Calendar.MONTH, 1)
        }
        return formato.format(calendario.time)
    }
}
