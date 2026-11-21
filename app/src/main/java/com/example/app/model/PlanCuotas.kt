package com.example.app.model

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class CuotaCalculada(val numero: Int, val monto: Int, val vencimiento: String)

object PlanCuotas {
    fun calcular(montoTotal: Int, cantidad: Int, primerVencimiento: String): List<CuotaCalculada> {
        require(cantidad in 1..120 && montoTotal >= cantidad)
        require(primerVencimiento.matches(Regex("""[0-9]{4}-[0-9]{2}-[0-9]{2}""")))
        val formato = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { isLenient = false }
        val fecha = requireNotNull(formato.parse(primerVencimiento))
        val primerDia = Calendar.getInstance().apply { time = fecha }
        val montoBase = montoTotal / cantidad
        val resto = montoTotal % cantidad
        return (0 until cantidad).map { indice ->
            val vencimiento = (primerDia.clone() as Calendar).apply { add(Calendar.MONTH, indice) }
            CuotaCalculada(indice + 1, montoBase + if (indice < resto) 1 else 0, formato.format(vencimiento.time))
        }
    }
}
