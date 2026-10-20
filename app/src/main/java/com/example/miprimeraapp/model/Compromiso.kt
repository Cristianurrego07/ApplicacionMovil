package com.example.miprimeraapp.model

data class Compromiso(
    val id: Long = 0,
    val usuarioId: Long,
    val titulo: String,
    val subtitulo: String,
    val monto: Double,
    val fechaVencimiento: String,
    val estado: String, // "Pendiente", "Pagada", "Vencido", "En revisión"
    val empresa: String = "",
    val folio: String = "",
    val concepto: String = "",
    val recargo: Int = 0
)
