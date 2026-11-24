package com.example.app.model

import com.example.app.ui.screens.PasoRecuperacion

data class RecuperarContrasenaUiState(
    val paso: PasoRecuperacion = PasoRecuperacion.CORREO,
    val correo: String = "",
    val codigo: String = "",
    val nuevaContrasena: String = "",
    val confirmarContrasena: String = "",
    val contrasenaVisible: Boolean = false,

    val errorCorreo: String? = null,
    val errorCodigo: String? = null,
    val errorNuevaContrasena: String? = null,
    val errorConfirmarContrasena: String? = null,
    val errorGeneral: String? = null
)