package com.example.app.model

data class TarjetaBancaria(
    val ultimosCuatro: String,
    val titular: String,
    val vencimiento: String,
    val tipo: String
)
