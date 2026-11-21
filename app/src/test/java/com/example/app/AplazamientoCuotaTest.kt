package com.example.app

import com.example.app.model.AplazamientoCuota
import com.example.app.model.CatalogoServicios
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AplazamientoCuotaTest {
    @Test fun calculaRecargoDeDosPorciento() {
        assertEquals(10200L, AplazamientoCuota.montoConRecargo(10000))
        assertEquals(102L, AplazamientoCuota.montoConRecargo(100))
    }

    @Test fun mueveUnMesRespetandoDiasDisponibles() {
        assertEquals("2028-02-29", AplazamientoCuota.mesSiguiente("2028-01-31"))
        assertEquals("2027-01-19", AplazamientoCuota.mesSiguiente("2026-12-19"))
    }

    @Test fun aceptaUnaEmpresaSinCatalogoConMontoDePrueba() {
        val servicio = CatalogoServicios.buscar("Mi empresa de prueba")
        assertEquals("Mi empresa de prueba", servicio?.empresa)
        assertEquals(30000, servicio?.monto)
        assertNull(CatalogoServicios.buscar("   "))
    }

    @Test fun catalogoNoIncluyeCobrosDeDuoc() {
        assertEquals(28000, CatalogoServicios.buscar("luz")?.monto)
        assertEquals("Aguas Andinas", CatalogoServicios.buscar("AGUA")?.empresa)
        assertNull(CatalogoServicios.buscar("Duoc Uc"))
    }
}
