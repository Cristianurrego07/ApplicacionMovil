package com.example.miprimeraapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.miprimeraapp.ui.screens.AgenteDashboardScreen
import com.example.miprimeraapp.ui.screens.DetalleCompromisoScreen
import com.example.miprimeraapp.ui.screens.EjecutivoDashboardScreen
import com.example.miprimeraapp.ui.screens.HistorialPagosScreen
import com.example.miprimeraapp.ui.screens.LoginScreen
import com.example.miprimeraapp.ui.screens.MiProgresoScreen
import com.example.miprimeraapp.ui.screens.PerfilConfigScreen
import com.example.miprimeraapp.ui.screens.RegistroScreen
import com.example.miprimeraapp.ui.screens.UsuarioHomeScreen
import com.example.miprimeraapp.viewmodel.AuthViewModel

@Composable
fun AppNavegacion() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
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
                onIrARegistro = {
                    navController.navigate(Ruta.Registro.ruta)
                },
                viewModel = authViewModel
            )
        }

        composable(Ruta.Registro.ruta) {
            RegistroScreen(
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
