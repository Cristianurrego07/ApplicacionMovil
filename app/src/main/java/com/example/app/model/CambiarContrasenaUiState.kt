package com.example.app.model

data class CambiarContrasenaUiState(
    val actual: String = "",
    val nueva: String = "",
    val confirmacion: String = "",
    val actualVisible: Boolean = false,
    val nuevaVisible: Boolean = false,
    val confirmacionVisible: Boolean = false,
    val errorActual: String? = null,
    val errorNueva: String? = null,
    val errorConfirmacion: String? = null,
    val errorGuardado: String? = null
)
