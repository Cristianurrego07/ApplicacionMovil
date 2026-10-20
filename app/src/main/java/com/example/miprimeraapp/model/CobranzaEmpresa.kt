package com.example.miprimeraapp.model

data class CobranzaEmpresa(
    val id: Long = 0,
    val codigo: String, // e.g. "N° C-00124"
    val empresa: String, // e.g. "Luz del Sur"
    val monto: Double,
    val vencimiento: String,
    val descripcion: String,
    val estado: String // "En curso", "Pagada", "Anulada" (RF-17 a RF-20)
)
