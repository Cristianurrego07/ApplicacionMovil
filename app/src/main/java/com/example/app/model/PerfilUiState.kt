package com.example.app.model

data class PerfilUiState(
    val nombre: String = "",
    val rut: String = "",
    val correo: String = "",
    val telefono: String = "",
    val direccion: String = "",
    val ciudad: String = "",

    val errorNombre: String? = null,
    val errorRut: String? = null,
    val errorCorreo: String? = null,
    val errorTelefono: String? = null,
    val errorDireccion: String? = null,
    val errorCiudad: String? = null,
    val errorGuardado: String? = null
)