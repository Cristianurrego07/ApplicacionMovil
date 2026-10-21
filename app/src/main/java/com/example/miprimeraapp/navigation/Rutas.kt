package com.example.miprimeraapp.navigation

sealed class Ruta(val ruta: String) {
    data object Login : Ruta("login")
    data object Registro : Ruta("registro?tipo={tipo}") {
        fun crear(tipo: String = "cliente") = "registro?tipo=$tipo"
    }
    data object UsuarioHome : Ruta("usuario_home")
    data object DetalleCompromiso : Ruta("detalle_compromiso/{id}") {
        fun crear(id: Long) = "detalle_compromiso/$id"
    }
    data object AgregarServicio : Ruta("agregar_servicio")
    data object Comprobante : Ruta("comprobante/{folio}") {
        fun crear(folio: String) = "comprobante/$folio"
    }
    data object ReportarProblema : Ruta("reportar_problema")
    data object HistorialPagos : Ruta("historial_pagos")
    data object MiProgreso : Ruta("mi_progreso")
    data object PerfilConfig : Ruta("perfil_config")
    data object AgenteDashboard : Ruta("agente_dashboard")
    data object EjecutivoDashboard : Ruta("ejecutivo_dashboard")
}
