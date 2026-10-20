package cl.duocuc.miregistro.model

data class Usuario(
    val nombre: String,
    val correo: String,
    val contrasena: String = "123456",
    val ciudad: String = "Santiago",
    val id: Long = 0,
    val idRol: Long = Rol.ID_USUARIO,
    val nombreRol: String = "",
    val avatar: ByteArray? = null
)
