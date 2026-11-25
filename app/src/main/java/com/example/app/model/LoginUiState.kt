package com.example.app.model

data class LoginUiState(
    val identificador: String = "",
    val contrasena: String = "",
    val contrasenaVisible: Boolean = false,

    val errorIdentificador: String? = null,
    val errorContrasena: String? = null,
    val error: String? = null,
    val cargando: Boolean = false
)