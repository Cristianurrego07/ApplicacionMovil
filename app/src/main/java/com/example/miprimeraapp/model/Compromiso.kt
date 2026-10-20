package com.example.miprimeraapp.model

data class Compromiso(
    val id: Long = 0,
    val usuarioId: Long,
    val titulo: String,
    val subtitulo: String,
    val monto: Double,
    val fechaVencimiento: String,
    val estado: String, // "Pendiente", "Pagado", "Vencido"
    val empresa: String = ""
)
