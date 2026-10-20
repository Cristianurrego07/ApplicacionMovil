package com.example.miprimeraapp.model

data class Rol(
    val id: Long,
    val nombre: String
) {
    companion object {
        const val ID_USUARIO = 1L
        const val ID_AGENTE = 2L
        const val ID_EJECUTIVO = 3L
    }
}
