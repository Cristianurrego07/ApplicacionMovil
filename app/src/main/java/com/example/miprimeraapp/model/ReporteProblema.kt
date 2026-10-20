package cl.duocuc.miregistro.model

data class ReporteProblema(
    val id: Long = 0,
    val codigo: String, // e.g. "#RP-001"
    val titulo: String, // e.g. "Error en pago con tarjeta"
    val cliente: String, // e.g. "Empresa Los Ríos"
    val estado: String, // "Abierto", "En proceso", "Resuelto"
    val fecha: String,
    val descripcion: String,
    val tipo: String // "Reporte" o "Recomendación" (RF-14 a RF-16)
)
