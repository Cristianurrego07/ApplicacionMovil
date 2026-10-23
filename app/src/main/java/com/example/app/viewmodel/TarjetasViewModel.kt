package com.example.app.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.model.TarjetaBancaria
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class TarjetasViewModel(private val estado: SavedStateHandle) : ViewModel() {
    private val datos = estado.getStateFlow("tarjetas", emptyList<String>())
    val tarjetas = datos.map(::leer).stateIn(viewModelScope,
        SharingStarted.WhileSubscribed(5000), leer(datos.value))

    private fun leer(datos: List<String>): List<TarjetaBancaria> = datos.chunked(4)
        .filter { it.size == 4 }.map { TarjetaBancaria(it[0], it[1], it[2], it[3]) }

    private fun guardar(tarjetas: List<TarjetaBancaria>) {
        // Strings compatibles con el Bundle que restaura Android.
        estado["tarjetas"] = ArrayList(tarjetas.flatMap {
            listOf(it.ultimosCuatro, it.titular, it.vencimiento, it.tipo)
        })
    }

    fun agregar(numero: String, titular: String, vencimiento: String, tipo: String) {
        guardar(leer(datos.value) + TarjetaBancaria(numero.takeLast(4), titular, vencimiento, tipo))
    }

    fun eliminar(tarjeta: TarjetaBancaria) { guardar(leer(datos.value) - tarjeta) }
}
