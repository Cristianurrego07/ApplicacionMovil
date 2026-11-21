package com.example.app

import com.example.app.model.PlanCuotas
import org.junit.Assert.assertEquals
import org.junit.Test

class PlanCuotasTest {
    @Test
    fun divideMontoSinPerderPesos() {
        val cuotas = PlanCuotas.calcular(100, 3, "2026-11-20")
        assertEquals(listOf(34, 33, 33), cuotas.map { it.monto })
        assertEquals(100, cuotas.sumOf { it.monto })
        assertEquals(listOf(1, 2, 3), cuotas.map { it.numero })
    }

    @Test
    fun conservaDiaDelMesCuandoEsPosible() {
        val cuotas = PlanCuotas.calcular(300, 3, "2028-01-31")
        assertEquals(listOf("2028-01-31", "2028-02-29", "2028-03-31"), cuotas.map { it.vencimiento })
    }

    @Test(expected = IllegalArgumentException::class)
    fun rechazaCuotasMayoresQueMonto() {
        PlanCuotas.calcular(2, 3, "2026-11-20")
    }
}
