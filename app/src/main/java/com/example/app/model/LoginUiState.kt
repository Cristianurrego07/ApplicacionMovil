package com.example.app.model

enum class PortalLogin(val rol: String) {
    CLIENTE("CLIENTE"), AGENTE("AGENTE")
}

data class LoginUiState(
    val portal: PortalLogin = PortalLogin.CLIENTE,
    val identificador: String = "",
    val contrasena: String = "",
    val contrasenaVisible: Boolean = false,
    val error: String? = null
)
