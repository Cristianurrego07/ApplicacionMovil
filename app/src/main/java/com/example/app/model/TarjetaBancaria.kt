package com.example.app.model

// Solo datos de presentación. No conserva el número completo ni el CVV.
data class TarjetaBancaria(
    val ultimosCuatro: String,
    val titular: String,
    val vencimiento: String,
    val tipo: String
)
