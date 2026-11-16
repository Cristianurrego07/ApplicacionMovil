package com.example.app.navigation

sealed class Ruta(val ruta: String) {

    // Acceso y cuenta
    data object Login : Ruta("login")

    data object RecuperarContrasena : Ruta("recuperar-contrasena")

    data object CrearCuenta : Ruta("crear-cuenta")

    // Pantallas del cliente
    data object Usuario : Ruta("usuario")

    data object Perfil : Ruta("perfil")

    data object AgregarServicio : Ruta("agregar-servicio")

    data object MisServicios : Ruta("mis-servicios")

    data object MisCompromisos : Ruta("mis-compromisos")

    // Pantalla del agente
    data object Agente : Ruta("agente")

    // Detalle de un compromiso
    data object Detalle : Ruta("detalle/{folio}") {
        fun crear(folio: String): String {
            return "detalle/$folio"
        }
    }

    // Comprobante de pago
    data object Comprobante : Ruta("comprobante/{folio}") {
        fun crear(folio: String): String {
            return "comprobante/$folio"
        }
    }

    // Historiales y reportes
    data object HistorialPagos : Ruta("historial-pagos")

    data object ReportarProblema : Ruta("reportar-problema")

    data object HistorialReportes : Ruta("historial-reportes")
}