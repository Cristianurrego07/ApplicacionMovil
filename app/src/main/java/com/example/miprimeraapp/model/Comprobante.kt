package com.example.miprimeraapp.model

data class Comprobante(
    val numero: String,
    val folioCompromiso: String,
    val empresa: String,
    val concepto: String,
    val montoPagado: Int,
    val fechaHora: String,
    val canal: String = "App PagoFlex",
    val medioPago: String = "Pago simulado"
)
