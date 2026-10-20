package com.example.miprimeraapp.model

data class Perfil(
    val nombre: String,
    val profesion: String,
    val ciudad: String,
    val descripcion: String,
    val intereses: List<String>
)

data class PerfilUiState(
    val perfil: Perfil
)
