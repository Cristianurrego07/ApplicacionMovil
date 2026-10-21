package com.example.miprimeraapp.model

data class Servicio(
    val id: Long = 0,
    val usuarioId: Long,
    val nombreEmpresa: String,
    val rutPagador: String,
    val alias: String,
    val numeroCliente: String = ""
)

data class ServicioUiState(
    val nombreEmpresa: String = "Enel - Electricidad",
    val numeroCliente: String = "",
    val rutPagador: String = "",
    val alias: String = "",
    val empresasDisponibles: List<String> = listOf(
        "Enel - Electricidad",
        "Aguas Andinas - Agua Potable",
        "Metrogas - Gas Natural",
        "Movistar - Hogar y Móvil",
        "VTR - Internet y Cable",
        "Instituto / Universidad - Educación",
        "Condominio - Gastos Comunes"
    ),
    val errorNombreEmpresa: String? = null,
    val errorNumeroCliente: String? = null,
    val errorRutPagador: String? = null,
    val errorAlias: String? = null,
    val errorRegistro: String? = null
)
