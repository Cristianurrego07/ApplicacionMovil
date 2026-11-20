package com.example.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.app.ui.screens.AgenteScreen
import com.example.app.ui.screens.EjecutivoScreen
import com.example.app.ui.screens.AgregarServicioScreen
import com.example.app.ui.screens.ComprobanteScreen
import com.example.app.ui.screens.DetalleScreen
import com.example.app.ui.screens.FormularioScreen
import com.example.app.ui.screens.HistorialPagosScreen
import com.example.app.ui.screens.HistorialReportesScreen
import com.example.app.ui.screens.InicioScreen
import com.example.app.ui.screens.LoginScreen
import com.example.app.ui.screens.MisServiciosScreen
import com.example.app.ui.screens.PasoRecuperacion
import com.example.app.ui.screens.PerfilScreen
import com.example.app.ui.screens.RecuperarContrasenaScreen
import com.example.app.ui.screens.ReportarProblemaScreen
import com.example.app.ui.screens.UsuarioScreen
import com.example.app.viewmodel.PagoFlexViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun AppNavegacion() {
    val navController = rememberNavController()
    val pagoFlexViewModel: PagoFlexViewModel = viewModel()

    val compromisos by pagoFlexViewModel.compromisos.collectAsStateWithLifecycle()
    val reportes by pagoFlexViewModel.reportes.collectAsStateWithLifecycle()
    val usuarioActual by pagoFlexViewModel.usuarioActual.collectAsStateWithLifecycle()
    val servicios by pagoFlexViewModel.servicios.collectAsStateWithLifecycle()

    val usuarioGuardado = remember {
        pagoFlexViewModel.recuperarSesion()
    }

    fun cargarDatosUsuario(usuarioId: Long, rolNombre: String? = null) {
        pagoFlexViewModel.cargarReportes(
            pagoFlexViewModel.listarReportes()
        )

        pagoFlexViewModel.cargarServicios(
            pagoFlexViewModel.listarServicios(usuarioId)
        )

        // Ejecutivo ve todos los compromisos de la empresa; los demás solo los propios
        val esEjecutivo = rolNombre?.equals("EJECUTIVO", ignoreCase = true) == true
        pagoFlexViewModel.cargarCompromisos(
            pagoFlexViewModel.listarCompromisos(if (esEjecutivo) null else usuarioId)
        )
    }

    // Recupera la sesión si el usuario ya había iniciado sesión.
    LaunchedEffect(usuarioGuardado) {
        usuarioGuardado?.let { usuario ->
            pagoFlexViewModel.iniciarSesion(usuario)
            cargarDatosUsuario(usuario.id, usuario.rolNombre)

            navController.navigate(rutaParaRol(usuario.rolNombre)) {
                popUpTo(Ruta.Login.ruta) {
                    inclusive = true
                }
                launchSingleTop = true
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Ruta.Login.ruta
    ) {
        // Inicio de sesión
        composable(Ruta.Login.ruta) {
            LoginScreen(
                onIniciarSesion = pagoFlexViewModel::iniciarSesion,
                onLoginExitoso = { usuario ->
                    pagoFlexViewModel.guardarSesion(usuario.id)
                    pagoFlexViewModel.iniciarSesion(usuario)
                    cargarDatosUsuario(usuario.id, usuario.rolNombre)

                    navController.navigate(rutaParaRol(usuario.rolNombre)) {
                        popUpTo(Ruta.Login.ruta) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
                onCrearCuenta = {
                    navController.navigate(Ruta.CrearCuenta.ruta)
                },
                onRecuperarContrasena = {
                    navController.navigate(Ruta.RecuperarContrasena.ruta)
                }
            )
        }

        // Recuperación de contraseña
        composable(Ruta.RecuperarContrasena.ruta) {
            var correo by rememberSaveable {
                mutableStateOf("")
            }

            var mensaje by rememberSaveable {
                mutableStateOf<String?>(null)
            }

            RecuperarContrasenaScreen(
                paso = PasoRecuperacion.CORREO,
                correo = correo,
                codigo = "",
                nuevaContrasena = "",
                confirmarContrasena = "",
                contrasenaVisible = false,
                error = mensaje,
                onCorreoCambia = { valor ->
                    correo = valor
                    mensaje = null
                },
                onCodigoCambia = {},
                onNuevaContrasenaCambia = {},
                onConfirmarContrasenaCambia = {},
                onAlternarVisibilidad = {},
                onSolicitarCodigo = {
                    mensaje = if (correo.isBlank()) {
                        "Ingresa tu correo electrónico."
                    } else {
                        "La recuperación aún no está conectada al servicio."
                    }
                },
                onVerificarCodigo = {},
                onRestablecerContrasena = {},
                onVolver = {
                    navController.navigateUp()
                }
            )
        }

        // Crear cuenta de cliente
        composable(Ruta.CrearCuenta.ruta) {
            FormularioScreen(
                onGuardarUsuario = pagoFlexViewModel::registrarUsuario,
                onRegistroExitoso = { usuario ->
                    pagoFlexViewModel.guardarSesion(usuario.id)
                    pagoFlexViewModel.iniciarSesion(usuario)
                    cargarDatosUsuario(usuario.id, usuario.rolNombre)

                    navController.navigate(rutaParaRol(usuario.rolNombre)) {
                        popUpTo(Ruta.Login.ruta) {
                            inclusive = true
                        }
                    }
                },
                onVolverInicio = {
                    navController.navigateUp()
                }
            )
        }

        // Inicio del cliente
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
                        popUpTo(Ruta.Usuario.ruta) {
                            inclusive = true
                        }
                    }
                },
                onVerDetalle = { folio ->
                    navController.navigate(Ruta.Detalle.crear(folio))
                },
                onVerPerfil = {
                    navController.navigate(Ruta.Perfil.ruta)
                },
                onVerCompromisos = {
                    navController.navigate(Ruta.MisCompromisos.ruta)
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

        // Lista de compromisos
        composable(Ruta.MisCompromisos.ruta) {
            UsuarioScreen(
                usuario = usuarioActual,
                compromisos = compromisos.filter {
                    it.usuario.id == usuarioActual?.id
                },
                onVolver = {
                    navController.navigateUp()
                },
                onVerDetalle = { folio ->
                    navController.navigate(Ruta.Detalle.crear(folio))
                }
            )
        }

        // Perfil
        composable(Ruta.Perfil.ruta) {
            val usuario = usuarioActual

            if (usuario != null) {
                PerfilScreen(
                    usuario = usuario,
                    compromisos = compromisos.filter {
                        it.usuario.id == usuario.id
                    },
                    cantidadPagos = pagoFlexViewModel
                        .listarComprobantes(usuario.id)
                        .size,
                    reportes = reportes,
                    onVolver = {
                        navController.navigateUp()
                    },
                    onCambiarContrasena = { actual, nueva ->
                        runCatching {
                            pagoFlexViewModel.cambiarContrasena(
                                usuario.id,
                                actual,
                                nueva
                            )
                        }.getOrDefault(false)
                    },
                    onGuardarFoto = { bytes ->
                        val actualizado = withContext(Dispatchers.IO) {
                            runCatching {
                                val guardado = pagoFlexViewModel.actualizarAvatar(
                                    usuario.id,
                                    bytes
                                )

                                if (guardado) {
                                    pagoFlexViewModel.buscarPorId(usuario.id)
                                } else {
                                    null
                                }
                            }.getOrNull()
                        }

                        if (actualizado != null) {
                            pagoFlexViewModel.iniciarSesion(actualizado)
                        }

                        actualizado != null
                    },
                    onGuardarPerfil = { actualizado ->
                        val guardado = runCatching {
                            pagoFlexViewModel.actualizarUsuario(actualizado) == 1
                        }.getOrDefault(false)

                        if (guardado) {
                            pagoFlexViewModel.iniciarSesion(actualizado)
                            cargarDatosUsuario(actualizado.id)
                        }

                        guardado
                    }
                )
            }
        }

        // Reportar problema
        composable(Ruta.ReportarProblema.ruta) {
            ReportarProblemaScreen(
                compromisos = compromisos.filter {
                    it.usuario.id == usuarioActual?.id
                },
                onVolver = {
                    navController.navigateUp()
                },
                onReportar = { folio, motivo ->
                    val enviado = pagoFlexViewModel.registrarReporte(
                        folio,
                        motivo
                    )

                    if (enviado) {
                        pagoFlexViewModel.cargarReportes(
                            pagoFlexViewModel.listarReportes()
                        )

                        usuarioActual?.let { usuario ->
                            pagoFlexViewModel.cargarCompromisos(
                                pagoFlexViewModel.listarCompromisos(usuario.id)
                            )
                        }
                    }

                    enviado
                }
            )
        }

        // Historial de pagos
        composable(Ruta.HistorialPagos.ruta) {
            HistorialPagosScreen(
                comprobantes = usuarioActual
                    ?.let { pagoFlexViewModel.listarComprobantes(it.id) }
                    .orEmpty(),
                compromisos = compromisos.filter {
                    it.usuario.id == usuarioActual?.id
                },
                onVolver = {
                    navController.navigateUp()
                },
                onVerComprobante = { folio ->
                    navController.navigate(Ruta.Comprobante.crear(folio))
                },
                onVerDetalle = { folio ->
                    navController.navigate(Ruta.Detalle.crear(folio))
                }
            )
        }

        // Agregar servicio
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
                        val idServicio = pagoFlexViewModel.insertarServicio(
                            servicio
                        )

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

        // Eliminar servicios
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
                        val eliminado = pagoFlexViewModel.eliminarCompromiso(
                            compromiso.folio,
                            usuario.id
                        )

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

        // Panel del agente
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
                        popUpTo(Ruta.Agente.ruta) {
                            inclusive = true
                        }
                    }
                },
                onCambiarEstado = { reporte, estado ->
                    val actualizado = if (estado == "En proceso") {
                        pagoFlexViewModel.actualizarEstadoReporte(
                            reporte.folio,
                            estado
                        )
                    } else {
                        pagoFlexViewModel.resolverReporte(
                            reporte.folio,
                            estado
                        )
                    }

                    if (actualizado) {
                        pagoFlexViewModel.cargarReportes(
                            pagoFlexViewModel.listarReportes()
                        )
                        pagoFlexViewModel.cargarCompromisos(
                            pagoFlexViewModel.listarCompromisos()
                        )
                    }

                    actualizado
                }
            )
        }

        // Historial de reportes
        composable(Ruta.HistorialReportes.ruta) {
            val esAgente = usuarioActual?.rolNombre == "AGENTE"

            val foliosUsuario = compromisos
                .filter { it.usuario.id == usuarioActual?.id }
                .map { it.folio }
                .toSet()

            HistorialReportesScreen(
                reportes = if (esAgente) {
                    reportes
                } else {
                    reportes.filter { it.folio in foliosUsuario }
                },
                compromisos = compromisos.filter {
                    it.usuario.id == usuarioActual?.id
                },
                esAgente = esAgente,
                onVolver = {
                    navController.navigateUp()
                }
            )
        }

        // Detalle de un compromiso
        composable(
            route = Ruta.Detalle.ruta,
            arguments = listOf(
                navArgument("folio") {
                    type = NavType.StringType
                }
            )
        ) { entrada ->
            val folio = entrada.arguments?.getString("folio") ?: ""
            val compromiso = compromisos.find {
                it.folio == folio
            }

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
                            usuarioActual?.let { usuario ->
                                pagoFlexViewModel.cargarCompromisos(
                                    pagoFlexViewModel.listarCompromisos(usuario.id)
                                )
                            }

                            navController.navigate(Ruta.Comprobante.crear(folio)) {
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
                        val enviado = pagoFlexViewModel.registrarReporte(
                            folio,
                            motivo
                        )

                        if (enviado) {
                            pagoFlexViewModel.cargarReportes(
                                pagoFlexViewModel.listarReportes()
                            )

                            usuarioActual?.let { usuario ->
                                pagoFlexViewModel.cargarCompromisos(
                                    pagoFlexViewModel.listarCompromisos(usuario.id)
                                )
                            }
                        }

                        enviado
                    }
                )
            }
        }

        // Comprobante de pago
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

        // Pantalla del Ejecutivo de empresa
        composable(Ruta.Ejecutivo.ruta) {
            EjecutivoScreen(
                usuario = usuarioActual,
                compromisos = compromisos,
                onCerrarSesion = {
                    pagoFlexViewModel.cerrarSesion()
                    navController.navigate(Ruta.Login.ruta) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onVerDetalle = { folio ->
                    navController.navigate(Ruta.Detalle.crear(folio))
                },
                onVerPerfil = {
                    navController.navigate(Ruta.Perfil.ruta)
                },
                onRegistrarCompromiso = {
                    // Por ahora redirige a agregar servicio / formulario; se puede ampliar con pantalla específica de Main
                    navController.navigate(Ruta.AgregarServicio.ruta)
                }
            )
        }
    }
}

// Decide la pantalla inicial según el rol de la cuenta.
private fun rutaParaRol(rol: String): String {
    return when (rol.uppercase()) {
        "CLIENTE", "USUARIO_FINAL" -> Ruta.Usuario.ruta
        "AGENTE" -> Ruta.Agente.ruta
        "EJECUTIVO" -> Ruta.Ejecutivo.ruta
        else -> Ruta.Login.ruta
    }
}
