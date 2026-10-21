package com.example.miprimeraapp.model

data class Usuario(
    val nombre: String,
    val apellido: String = "",
    val rut: String = "12.345.678-9",
    val correo: String,
    val contrasena: String = "123456",
    val telefono: String = "+56 9 9876 5432",
    val direccion: String = "Av. Providencia 1234",
    val ciudad: String = "Santiago",
    val id: Long = 0,
    val idRol: Long = Rol.ID_USUARIO,
    val nombreRol: String = "",
    val avatar: ByteArray? = null
)
