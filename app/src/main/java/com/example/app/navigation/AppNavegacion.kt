
package com.example.app.navigation

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.app.ui.screens.AgenteScreen
import com.example.app.ui.screens.AgregarServicioScreen
import com.example.app.ui.screens.ComprobanteScreen
import com.example.app.ui.screens.DetalleScreen
import com.example.app.ui.screens.FormularioScreen
import com.example.app.ui.screens.HistorialPagosScreen
import com.example.app.ui.screens.HistorialReportesScreen
import com.example.app.ui.screens.InicioScreen
import com.example.app.ui.screens.LoginScreen
import com.example.app.ui.screens.MisServiciosScreen
import com.example.app.ui.screens.PerfilScreen
import com.example.app.ui.screens.ReportarProblemaScreen
import com.example.app.viewmodel.PagoFlexViewModel

@Composable
fun AppNavegacion() {
    val navController = rememberNavController()

    val pagoFlexViewModel: PagoFlexViewModel = viewModel()

    val compromisos by pagoFlexViewModel.compromisos.collectAsStateWithLifecycle()
    val reportes by pagoFlexViewModel.reportes.collectAsStateWithLifecycle()
    val usuarioActual by pagoFlexViewModel.usuarioActual.collectAsStateWithLifecycle()
    val servicios by pagoFlexViewModel.servicios.collectAsStateWithLifecycle()

    val usuarioGuardado = remember { pagoFlexViewModel.recuperarSesion() }

    // Cargar siempre los datos del usuario correspondiente.
    fun cargarDatosUsuario(usuarioId: Long) {
        pagoFlexViewModel.cargarReportes(pagoFlexViewModel.listarReportes())
        pagoFlexViewModel.cargarServicios(
            pagoFlexViewModel.listarServicios(usuarioId)
        )
        pagoFlexViewModel.cargarCompromisos(
            pagoFlexViewModel.listarCompromisos(usuarioId)
        )
    }

    LaunchedEffect(usuarioGuardado) {
        usuarioGuardado?.let { usuario ->
            pagoFlexViewModel.iniciarSesion(usuario)
            cargarDatosUsuario(usuario.id)

            navController.navigate(rutaParaRol(usuario.rolNombre)) {
                popUpTo(Ruta.Login.ruta) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Ruta.Login.ruta
    ) {
        composable(Ruta.Login.ruta) {
            LoginScreen(
                onIniciarSesion = pagoFlexViewModel::iniciarSesion,
                onLoginExitoso = { usuario ->
                    pagoFlexViewModel.guardarSesion(usuario.id)
                    pagoFlexViewModel.iniciarSesion(usuario)
                    cargarDatosUsuario(usuario.id)

                    navController.navigate(rutaParaRol(usuario.rolNombre)) {
                        popUpTo(Ruta.Login.ruta) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onCrearCuenta = {
                    navController.navigate(Ruta.CrearCuenta.crear("CLIENTE"))
                },
                onCrearCuentaAgente = {
                    navController.navigate(Ruta.CrearCuenta.crear("AGENTE"))
                }
            )
        }

        composable(
            route = Ruta.CrearCuenta.ruta,
            arguments = listOf(
                navArgument("rol") {
                    type = NavType.StringType
                    defaultValue = "CLIENTE"
                }
            )
        ) { entry ->
            val rol = entry.arguments?.getString("rol") ?: "CLIENTE"
            FormularioScreen(
                rolInicial = rol,
                onGuardarUsuario = pagoFlexViewModel::registrarUsuario,
                onRegistroExitoso = { usuario ->
                    pagoFlexViewModel.guardarSesion(usuario.id)
                    pagoFlexViewModel.iniciarSesion(usuario)
                    cargarDatosUsuario(usuario.id)

                    navController.navigate(rutaParaRol(usuario.rolNombre)) {
                        popUpTo(Ruta.Login.ruta) { inclusive = true }
                    }
                },
                onVolverInicio = {
                    navController.navigateUp()
                }
            )
        }

        composable(Ruta.Usuario.ruta) {
            InicioScreen(
                usuario = usuarioActual,
                compromisos = compromisos.filter {
                    it.usuario.id == usuarioActual?.id
                },
                servicios = servicios.filter {
                    it.usuarioId == usuarioActual?.id
                },
                onVolver = {
                    pagoFlexViewModel.cerrarSesion()
                    navController.navigate(Ruta.Login.ruta) {
                        popUpTo(Ruta.Usuario.ruta) { inclusive = true }
                    }
                },
                onVerDetalle = { folio ->
                    navController.navigate(Ruta.Detalle.crear(folio))
                },
                onVerPerfil = {
                    navController.navigate(Ruta.Perfil.ruta)
                },
                onAgregarServicio = {
                    navController.navigate(Ruta.AgregarServicio.ruta)
                },
                onEliminarServicios = {
                    navController.navigate(Ruta.MisServicios.ruta)
                },
                onVerHistorial = {
                    navController.navigate(Ruta.HistorialPagos.ruta)
                },
                onReportarProblema = {
                    navController.navigate(Ruta.ReportarProblema.ruta)
                },
                onVerHistorialReportes = {
                    navController.navigate(Ruta.HistorialReportes.ruta)
                }
            )
        }

        composable(Ruta.Perfil.ruta) {
            val usuario = usuarioActual

            if (usuario != null) {
                PerfilScreen(
                    usuario = usuario,
                    compromisos = compromisos.filter {
                        it.usuario.id == usuario.id
                    },
                    cantidadPagos = pagoFlexViewModel.listarComprobantes(usuario.id).size,
                    onVolver = {
                        navController.navigateUp()
                    },
                    reportes = reportes,
                    onCambiarContrasena = { actual, nueva ->
                        runCatching { pagoFlexViewModel.cambiarContrasena(usuario.id, actual, nueva) }
                            .getOrDefault(false)
                    },
                    onGuardarFoto = { bytes ->
                        val actualizado = withContext(Dispatchers.IO) {
                            runCatching {
                                if (pagoFlexViewModel.actualizarAvatar(usuario.id, bytes)) pagoFlexViewModel.buscarPorId(usuario.id)
                                else null
                            }.getOrNull()
                        }
                        if (actualizado != null) pagoFlexViewModel.iniciarSesion(actualizado)
                        actualizado != null
                    },
                    onGuardarPerfil = { actualizado ->
                        val guardado = runCatching { pagoFlexViewModel.actualizarUsuario(actualizado) == 1 }
                            .getOrDefault(false)
                        if (guardado) {
                            pagoFlexViewModel.iniciarSesion(actualizado)
                            cargarDatosUsuario(actualizado.id)
                        }
                        guardado
                    }
                )
            }
        }

        composable(Ruta.ReportarProblema.ruta) {
            ReportarProblemaScreen(
                compromisos = compromisos.filter {
                    it.usuario.id == usuarioActual?.id
                },
                onVolver = {
                    navController.navigateUp()
                },
                onReportar = { folio, motivo ->
                    if (pagoFlexViewModel.registrarReporte(folio, motivo)) {
                        pagoFlexViewModel.cargarReportes(pagoFlexViewModel.listarReportes())
                        usuarioActual?.let {
                            pagoFlexViewModel.cargarCompromisos(
                                pagoFlexViewModel.listarCompromisos(it.id)
                            )
                        }
                        true
                    } else {
                        false
                    }
                }
            )
        }

        composable(Ruta.HistorialPagos.ruta) {
            HistorialPagosScreen(
                comprobantes = usuarioActual
                    ?.let { pagoFlexViewModel.listarComprobantes(it.id) }
                    .orEmpty(),
                onVolver = {
                    navController.navigateUp()
                },
                onVerComprobante = { folio ->
                    navController.navigate(Ruta.Comprobante.crear(folio))
                }
            )
        }

        composable(Ruta.AgregarServicio.ruta) {
            val usuario = usuarioActual

            if (usuario != null) {
                AgregarServicioScreen(
                    usuarioId = usuario.id,
                    onVolver = {
                        navController.navigateUp()
                    },
                    onServicioGuardado = {
                        navController.navigate(Ruta.MisServicios.ruta) {
                            popUpTo(Ruta.Usuario.ruta)
                            launchSingleTop = true
                        }
                    },
                    onGuardarServicio = { servicio ->
                        val idServicio = pagoFlexViewModel.insertarServicio(servicio)

                        if (idServicio == -1L) {
                            false
                        } else {
                            pagoFlexViewModel.cargarServicios(
                                pagoFlexViewModel.listarServicios(usuario.id)
                            )
                            true
                        }
                    }
                )
            }
        }

        composable(Ruta.MisServicios.ruta) {
            MisServiciosScreen(
                compromisos = compromisos.filter {
                    it.usuario.id == usuarioActual?.id
                },
                onVolver = {
                    navController.navigateUp()
                },
                onEliminar = { compromiso ->
                    val usuario = usuarioActual
                    if (usuario == null) {
                        false
                    } else {
                        val eliminado = pagoFlexViewModel.eliminarCompromiso(compromiso.folio, usuario.id)
                        if (eliminado) {
                            pagoFlexViewModel.cargarCompromisos(
                                pagoFlexViewModel.listarCompromisos(usuario.id)
                            )
                        }
                        eliminado
                    }
                }
            )
        }

        composable(Ruta.Agente.ruta) {
            AgenteScreen(
                usuario = usuarioActual,
                reportes = reportes,
                onVerPerfil = {
                    navController.navigate(Ruta.Perfil.ruta)
                },
                onVerHistorial = {
                    navController.navigate(Ruta.HistorialReportes.ruta)
                },
                onCerrarSesion = {
                    pagoFlexViewModel.cerrarSesion()
                    navController.navigate(Ruta.Login.ruta) {
                        popUpTo(Ruta.Agente.ruta) { inclusive = true }
                    }
                },
                onCambiarEstado = { reporte, estado ->
                    val actualizado = if (estado == "En proceso") {
                        pagoFlexViewModel.actualizarEstadoReporte(reporte.folio, estado)
                    } else {
                        pagoFlexViewModel.resolverReporte(reporte.folio, estado)
                    }
                    if (actualizado) {
                        pagoFlexViewModel.cargarReportes(pagoFlexViewModel.listarReportes())
                        pagoFlexViewModel.cargarCompromisos(pagoFlexViewModel.listarCompromisos())
                    }
                    actualizado
                }
            )
        }

        composable(Ruta.HistorialReportes.ruta) {
            val esAgente = usuarioActual?.rolNombre == "AGENTE"
            val foliosUsuario = compromisos
                .filter { it.usuario.id == usuarioActual?.id }
                .map { it.folio }
                .toSet()
            HistorialReportesScreen(
                reportes = if (esAgente) reportes else reportes.filter { it.folio in foliosUsuario },
                compromisos = compromisos.filter { it.usuario.id == usuarioActual?.id },
                esAgente = esAgente,
                onVolver = { navController.navigateUp() }
            )
        }

        composable(
            route = Ruta.Detalle.ruta,
            arguments = listOf(
                navArgument("folio") {
                    type = NavType.StringType
                }
            )
        ) { entrada ->
            val folio = entrada.arguments?.getString("folio") ?: ""
            val compromiso = compromisos.find { it.folio == folio }

            if (compromiso != null) {
                DetalleScreen(
                    compromiso = compromiso,
                    onVolver = {
                        navController.navigateUp()
                    },
                    onPagar = { medioPago ->
                        val comprobante = pagoFlexViewModel.confirmarPago(
                            folio,
                            medioPago
                        )

                        if (comprobante != null) {
                            usuarioActual?.let {
                                pagoFlexViewModel.cargarCompromisos(
                                    pagoFlexViewModel.listarCompromisos(it.id)
                                )
                            }

                            navController.navigate(
                                Ruta.Comprobante.crear(folio)
                            ) {
                                popUpTo(Ruta.Usuario.ruta) {
                                    inclusive = false
                                }
                            }
                            true
                        } else {
                            false
                        }
                    },
                    onReportar = { motivo ->
                        if (pagoFlexViewModel.registrarReporte(folio, motivo)) {
                            pagoFlexViewModel.cargarReportes(pagoFlexViewModel.listarReportes())
                            usuarioActual?.let {
                                pagoFlexViewModel.cargarCompromisos(
                                    pagoFlexViewModel.listarCompromisos(it.id)
                                )
                            }
                            true
                        } else {
                            false
                        }
                    }
                )
            }
        }

        composable(
            route = Ruta.Comprobante.ruta,
            arguments = listOf(
                navArgument("folio") {
                    type = NavType.StringType
                }
            )
        ) { entrada ->
            val folio = entrada.arguments?.getString("folio") ?: ""

            ComprobanteScreen(
                comprobante = pagoFlexViewModel.buscarComprobante(folio),
                onVolver = {
                    navController.navigateUp()
                }
            )
        }
    }
}

private fun rutaParaRol(rol: String): String {
    return when (rol) {
        "CLIENTE" -> Ruta.Usuario.ruta
        "AGENTE" -> Ruta.Agente.ruta
        else -> Ruta.Login.ruta
    }
}
