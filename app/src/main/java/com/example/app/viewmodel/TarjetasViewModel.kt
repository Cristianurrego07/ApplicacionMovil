package com.example.app.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.model.TarjetaBancaria
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class TarjetasViewModel(
    private val estado: SavedStateHandle
) : ViewModel() {

    private val datos = estado.getStateFlow(
        key = "tarjetas",
        initialValue = emptyList<String>()
    )

    val tarjetas = datos
        .map(::leer)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = leer(datos.value)
        )

    private fun leer(datos: List<String>): List<TarjetaBancaria> {
        return datos
            .chunked(4)
            .filter { grupo ->
                grupo.size == 4
            }
            .map { grupo ->
                TarjetaBancaria(
                    grupo[0],
                    grupo[1],
                    grupo[2],
                    grupo[3]
                )
            }
    }

    private fun guardar(tarjetas: List<TarjetaBancaria>) {
        val datosGuardados = tarjetas.flatMap { tarjeta ->
            listOf(
                tarjeta.ultimosCuatro,
                tarjeta.titular,
                tarjeta.vencimiento,
                tarjeta.tipo
            )
        }

        estado["tarjetas"] = ArrayList(datosGuardados)
    }

    fun agregar(
        numero: String,
        titular: String,
        vencimiento: String,
        tipo: String
    ) {
        val tarjeta = TarjetaBancaria(
            numero.takeLast(4),
            titular,
            vencimiento,
            tipo
        )

        val listaActual = leer(datos.value)
        guardar(listaActual + tarjeta)
    }

    fun eliminar(tarjeta: TarjetaBancaria) {
        val listaActual = leer(datos.value)
        guardar(listaActual - tarjeta)
    }
}