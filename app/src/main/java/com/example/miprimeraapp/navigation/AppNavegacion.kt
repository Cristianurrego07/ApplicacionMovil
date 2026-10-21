package com.example.miprimeraapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.miprimeraapp.data.UsuarioDbHelper
import com.example.miprimeraapp.ui.screens.AgenteDashboardScreen
import com.example.miprimeraapp.ui.screens.AgregarServicioScreen
import com.example.miprimeraapp.ui.screens.ComprobanteScreen
import com.example.miprimeraapp.ui.screens.DetalleCompromisoScreen
import com.example.miprimeraapp.ui.screens.EjecutivoDashboardScreen
import com.example.miprimeraapp.ui.screens.HistorialPagosScreen
import com.example.miprimeraapp.ui.screens.LoginScreen
import com.example.miprimeraapp.ui.screens.MiProgresoScreen
import com.example.miprimeraapp.ui.screens.PerfilConfigScreen
import com.example.miprimeraapp.ui.screens.RegistroScreen
import com.example.miprimeraapp.ui.screens.ReportarProblemaScreen
import com.example.miprimeraapp.ui.screens.UsuarioHomeScreen
import com.example.miprimeraapp.viewmodel.AuthViewModel
import com.example.miprimeraapp.viewmodel.PagoFlexViewModel

@Composable
fun AppNavegacion() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val dbHelper = remember(context) { UsuarioDbHelper(context.applicationContext) }

    val authViewModel: AuthViewModel = viewModel()
    val pagoFlexViewModel: PagoFlexViewModel = viewModel()
    val usuarioActual by authViewModel.usuarioActual.collectAsStateWithLifecycle()

    val startDestination = if (usuarioActual != null) {
        when (usuarioActual?.idRol) {
            2L -> Ruta.AgenteDashboard.ruta
            3L -> Ruta.EjecutivoDashboard.ruta
            else -> Ruta.UsuarioHome.ruta
        }
    } else {
        Ruta.Login.ruta
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Ruta.Login.ruta) {
            LoginScreen(
                onInicioExitoso = { idRol ->
                    when (idRol) {
                        2L -> navController.navigate(Ruta.AgenteDashboard.ruta) {
                            popUpTo(Ruta.Login.ruta) { inclusive = true }
                        }

                        3L -> navController.navigate(Ruta.EjecutivoDashboard.ruta) {
                            popUpTo(Ruta.Login.ruta) { inclusive = true }
                        }

                        else -> navController.navigate(Ruta.UsuarioHome.ruta) {
                            popUpTo(Ruta.Login.ruta) { inclusive = true }
                        }
                    }
                },
                onIrARegistro = { tipo ->
                    navController.navigate(Ruta.Registro.crear(tipo))
                },
                viewModel = authViewModel
            )
        }

        composable(
            route = Ruta.Registro.ruta,
            arguments = listOf(navArgument("tipo") {
                type = NavType.StringType
                defaultValue = "cliente"
            })
        ) { backStackEntry ->
            val tipo = backStackEntry.arguments?.getString("tipo") ?: "cliente"
            RegistroScreen(
                tipoRegistro = tipo,
                onRegistroExitoso = { idRol ->
                    when (idRol) {
                        2L -> navController.navigate(Ruta.AgenteDashboard.ruta) {
                            popUpTo(Ruta.Login.ruta) { inclusive = true }
                        }

                        3L -> navController.navigate(Ruta.EjecutivoDashboard.ruta) {
                            popUpTo(Ruta.Login.ruta) { inclusive = true }
                        }

                        else -> navController.navigate(Ruta.UsuarioHome.ruta) {
                            popUpTo(Ruta.Login.ruta) { inclusive = true }
                        }
                    }
                },
                onVolver = {
                    navController.popBackStack()
                },
                viewModel = authViewModel
            )
        }

        composable(Ruta.UsuarioHome.ruta) {
            UsuarioHomeScreen(
                usuario = usuarioActual,
                onNavigateDetalle = { id ->
                    navController.navigate(Ruta.DetalleCompromiso.crear(id))
                },
                onNavigateAgregarServicio = {
                    navController.navigate(Ruta.AgregarServicio.ruta)
                },
                onNavigateReportarProblema = {
                    navController.navigate(Ruta.ReportarProblema.ruta)
                },
                onNavigateHistorial = {
                    navController.navigate(Ruta.HistorialPagos.ruta)
                },
                onNavigateProgreso = {
                    navController.navigate(Ruta.MiProgreso.ruta)
                },
                onNavigatePerfil = {
                    navController.navigate(Ruta.PerfilConfig.ruta)
                },
                onCerrarSesion = {
                    authViewModel.cerrarSesion()
                    navController.navigate(Ruta.Login.ruta) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Ruta.AgregarServicio.ruta) {
            val u = usuarioActual
            if (u != null) {
                AgregarServicioScreen(
                    usuarioId = u.id,
                    onVolver = { navController.popBackStack() },
                    onGuardarServicio = { servicio ->
                        val id = dbHelper.insertarServicio(servicio)
                        id != -1L
                    }
                )
            }
        }

        composable(Ruta.ReportarProblema.ruta) {
            val u = usuarioActual
            val compromisos by pagoFlexViewModel.compromisos.collectAsStateWithLifecycle()
            if (u != null) {
                ReportarProblemaScreen(
                    compromisos = compromisos,
                    onVolver = { navController.popBackStack() },
                    onReportar = { folio, motivo ->
                        val target = compromisos.find { it.folio == folio }
                        val exitoRevision = dbHelper.marcarCompromisoEnRevision(folio)
                        val fechaActual = java.text.SimpleDateFormat("dd MMM, yyyy - HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
                        val nuevoReporte = com.example.miprimeraapp.model.ReporteProblema(
                            codigo = "#RP-" + System.currentTimeMillis().toString().takeLast(4),
                            titulo = "Problema en " + (target?.titulo ?: "Compromiso"),
                            cliente = "${u.nombre} (${u.rut})",
                            estado = "Abierto",
                            fecha = fechaActual,
                            descripcion = motivo,
                            tipo = "Reporte"
                        )
                        val idReporte = dbHelper.insertarReporte(nuevoReporte)
                        pagoFlexViewModel.cargarReportes()
                        exitoRevision && idReporte > 0
                    }
                )
            }
        }

        composable(
            route = Ruta.DetalleCompromiso.ruta,
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("id") ?: 0L
            DetalleCompromisoScreen(
                compromisoId = id,
                usuarioId = usuarioActual?.id ?: 1L,
                onVolver = { navController.popBackStack() }
            )
        }

        composable(
            route = Ruta.Comprobante.ruta,
            arguments = listOf(navArgument("folio") { type = NavType.StringType })
        ) { backStackEntry ->
            val folio = backStackEntry.arguments?.getString("folio") ?: ""
            ComprobanteScreen(
                comprobante = dbHelper.buscarComprobante(folio),
                onVolver = { navController.popBackStack() }
            )
        }

        composable(Ruta.HistorialPagos.ruta) {
            HistorialPagosScreen(
                usuario = usuarioActual,
                onVolver = { navController.popBackStack() }
            )
        }

        composable(Ruta.MiProgreso.ruta) {
            MiProgresoScreen(
                onVolver = { navController.popBackStack() }
            )
        }

        composable(Ruta.PerfilConfig.ruta) {
            PerfilConfigScreen(
                usuario = usuarioActual,
                onVolver = { navController.popBackStack() },
                onCerrarSesion = {
                    authViewModel.cerrarSesion()
                    navController.navigate(Ruta.Login.ruta) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Ruta.AgenteDashboard.ruta) {
            AgenteDashboardScreen(
                usuario = usuarioActual,
                onNavigatePerfil = {
                    navController.navigate(Ruta.PerfilConfig.ruta)
                },
                onCerrarSesion = {
                    authViewModel.cerrarSesion()
                    navController.navigate(Ruta.Login.ruta) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Ruta.EjecutivoDashboard.ruta) {
            EjecutivoDashboardScreen(
                usuario = usuarioActual,
                onNavigatePerfil = {
                    navController.navigate(Ruta.PerfilConfig.ruta)
                },
                onCerrarSesion = {
                    authViewModel.cerrarSesion()
                    navController.navigate(Ruta.Login.ruta) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
