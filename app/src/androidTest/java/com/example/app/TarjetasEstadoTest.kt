package com.example.app

import android.os.Bundle
import android.os.Parcel
import androidx.lifecycle.SavedStateHandle
import com.example.app.viewmodel.TarjetasViewModel
import org.junit.Assert.*
import org.junit.Test

class TarjetasEstadoTest {
    @Test fun restauraSoloLosDatosDePresentacionSinNumeroCompletoNiCvv() {
        val handle = SavedStateHandle()
        val vm = TarjetasViewModel(handle)
        vm.agregar("0000111122223333", "Prueba", "12/30", "Débito")
        val datos = requireNotNull(handle.get<ArrayList<String>>("tarjetas"))
        assertEquals(arrayListOf("3333", "Prueba", "12/30", "Débito"), datos)
        val parcela = Parcel.obtain()
        val lectura = Parcel.obtain()
        try {
            val paquete = Bundle().apply { putStringArrayList("tarjetas", datos) }
            parcela.writeBundle(paquete)
            val bytes = parcela.marshall()
            lectura.unmarshall(bytes, 0, bytes.size)
            lectura.setDataPosition(0)
            val restaurados = requireNotNull(lectura.readBundle()?.getStringArrayList("tarjetas"))
            assertEquals(datos, restaurados)
            val nuevoHandle = SavedStateHandle(mapOf("tarjetas" to restaurados))
            val restaurado = TarjetasViewModel(nuevoHandle)
            assertEquals("3333", restaurado.tarjetas.value.single().ultimosCuatro)
            restaurado.eliminar(restaurado.tarjetas.value.single())
            assertTrue(nuevoHandle.get<List<String>>("tarjetas")!!.isEmpty())
        } finally {
            parcela.recycle()
            lectura.recycle()
        }
    }
}
