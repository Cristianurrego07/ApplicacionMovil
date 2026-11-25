package com.example.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.app.data.remote.Resultado
import com.example.app.model.CatalogoServicios
import com.example.app.ui.screens.AgenteScreen
import com.example.app.ui.screens.EjecutivoScreen
import com.example.app.ui.screens.DetalleEjecutivoScreen
import com.example.app.ui.screens.RegistrarCompromisoScreen
import com.example.app.ui.screens.AgregarServicioScreen
import com.example.app.ui.screens.ComprobanteScreen
import com.example.app.ui.screens.DetalleScreen
import com.example.app.ui.screens.FormularioScreen
import com.example.app.ui.screens.HistorialPagosScreen
import com.example.app.ui.screens.HistorialReportesScreen
import com.example.app.ui.screens.InicioScreen
import com.example.app.ui.screens.LoginScreen
import com.example.app.ui.screens.MisServiciosScreen
import com.example.app.ui.screens.ServiciosAsociadosScreen
import com.example.app.ui.screens.PasoRecuperacion
import com.example.app.ui.screens.PerfilScreen
import com.example.app.ui.screens.RecuperarContrasenaScreen
import com.example.app.ui.screens.ReportarProblemaScreen
import com.example.app.ui.screens.UsuarioScreen
import com.example.app.viewmodel.PagoFlexViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun AppNavegacion() {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val pagoFlexViewModel: PagoFlexViewModel = viewModel()

    val compromisos by pagoFlexViewModel.compromisos.collectAsStateWithLifecycle()
    val reportes by pagoFlexViewModel.reportes.collectAsStateWithLifecycle()
    val usuarioActual by pagoFlexViewModel.usuarioActual.collectAsStateWithLifecycle()
    val servicios by pagoFlexViewModel.servicios.collectAsStateWithLifecycle()
    val montosPagadosEjecutivo by pagoFlexViewModel.montosPagados.collectAsStateWithLifecycle()

    suspend fun cargarDatosUsuario(usuarioId: Long, rolNombre: String? = null) = withContext(Dispatchers.IO) {
        val esEjecutivo = rolNombre?.equals("EJECUTIVO", ignoreCase = true) == true
        val compromisosVisibles = when {
            esEjecutivo -> pagoFlexViewModel.listarCompromisosEjecutivo(usuarioId)
            rolNombre == "AGENTE" -> pagoFlexViewModel.listarCompromisos()
            else -> pagoFlexViewModel.listarCompromisos(usuarioId)
        }
        val foliosVisibles = compromisosVisibles.map { it.folio }.toSet()
        pagoFlexViewModel.cargarReportes(
            when {
                rolNombre == "AGENTE" -> pagoFlexViewModel.listarReportes()
                else -> pagoFlexViewModel.listarReportes().filter { it.folio in foliosVisibles }
            }
        )

        pagoFlexViewModel.cargarServicios(
            pagoFlexViewModel.listarServicios(usuarioId)
        )

        pagoFlexViewModel.cargarCompromisos(compromisosVisibles)
        if (esEjecutivo) pagoFlexViewModel.cargarMontosPagadosEjecutivo(usuarioId)
    }

    LaunchedEffect(Unit) {
        val usuarioGuardado = withContext(Dispatchers.IO) { pagoFlexViewModel.recuperarSesion() }
        if (navController.currentDestination?.route != Ruta.Login.ruta) return@LaunchedEffect
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

        composable(Ruta.RecuperarContrasena.ruta) {
            val recuperacion: com.example.app.viewmodel.RecuperarContrasenaViewModel = viewModel()
            val estado by recuperacion.uiState.collectAsStateWithLifecycle()

            RecuperarContrasenaScreen(
                paso = estado.paso,
                correo = estado.correo,
                codigo = estado.codigo,
                nuevaContrasena = estado.nuevaContrasena,
                confirmarContrasena = estado.confirmarContrasena,
                contrasenaVisible = estado.contrasenaVisible,
                error = estado.errorCorreo ?: estado.errorCodigo ?: estado.errorNuevaContrasena
                    ?: estado.errorConfirmarContrasena ?: estado.errorGeneral,
                onCorreoCambia = recuperacion::onCorreoCambia,
                onCodigoCambia = recuperacion::onCodigoCambia,
                onNuevaContrasenaCambia = recuperacion::onNuevaContrasenaCambia,
                onConfirmarContrasenaCambia = recuperacion::onConfirmarContrasenaCambia,
                onAlternarVisibilidad = recuperacion::onAlternarVisibilidad,
                onSolicitarCodigo = {
                    if (recuperacion.validarCorreo()) {
                        recuperacion.mostrarError("La recuperación aún no está conectada al servicio.")
                    }
                },
                onVerificarCodigo = { recuperacion.validarCodigo() },
                onRestablecerContrasena = { recuperacion.validarNuevaContrasena() },
                onVolver = { navController.navigateUp() }
            )
        }

        composable(Ruta.CrearCuenta.ruta) {
            FormularioScreen(
                onGuardarUsuario = pagoFlexViewModel::registrarUsuario,
                onRegistroExitoso = { usuario ->
                    scope.launch {
                        pagoFlexViewModel.guardarSesion(usuario.id)
                        pagoFlexViewModel.iniciarSesion(usuario)
                        cargarDatosUsuario(usuario.id, usuario.rolNombre)

                        navController.navigate(rutaParaRol(usuario.rolNombre)) {
                            popUpTo(Ruta.Login.ruta) {
                                inclusive = true
                            }
                        }
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
                onVerServicios = { navController.navigate(Ruta.ServiciosAsociados.ruta) },
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

        composable(Ruta.ServiciosAsociados.ruta) {
            ServiciosAsociadosScreen(
                servicios = servicios.filter { it.usuarioId == usuarioActual?.id },
                compromisos = compromisos.filter { it.usuario.id == usuarioActual?.id },
                onVerCompromiso = { folio -> navController.navigate(Ruta.Detalle.crear(folio)) },
                onAgregarServicio = { navController.navigate(Ruta.AgregarServicio.ruta) },
                onVolver = { navController.navigateUp() }
            )
        }

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

        composable(Ruta.Perfil.ruta) {
            val usuario = usuarioActual

            if (usuario != null) {
                PerfilScreen(
                    usuario = usuario,
                    compromisos = if (usuario.rolNombre == "EJECUTIVO") {
                        compromisos
                    } else {
                        compromisos.filter { it.usuario.id == usuario.id }
                    },
                    cantidadPagos = if (usuario.rolNombre == "EJECUTIVO") {
                        compromisos.count { it.estado == "Pagado" }
                    } else {
                        pagoFlexViewModel.listarComprobantes(usuario.id).size
                    },
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
                            scope.launch {
                                cargarDatosUsuario(actualizado.id, actualizado.rolNombre)
                            }
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
                onVerDetalle = { folio ->
                    navController.navigate(Ruta.Detalle.crear(folio))
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
                        navController.popBackStack(Ruta.Usuario.ruta, false)
                    },
                    onGuardarServicio = { servicio ->
                        val resultado = pagoFlexViewModel.insertarServicio(servicio)
                        if (resultado is Resultado.Ok) {
                            pagoFlexViewModel.agregarServicio(resultado.dato)
                            val actualizados = withContext(Dispatchers.IO) {
                                pagoFlexViewModel.listarCompromisos(usuario.id)
                            }
                            pagoFlexViewModel.cargarCompromisos(actualizados)
                        }
                        resultado
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

        composable(Ruta.Agente.ruta) {
            AgenteScreen(
                usuario = usuarioActual,
                reportes = reportes,
                compromisos = compromisos,
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
                onCambiarEstado = { reporte, estado, respuesta ->
                    val actualizado = if (estado == "En proceso") {
                        pagoFlexViewModel.actualizarEstadoReporte(
                            reporte.folio,
                            estado
                        )
                    } else {
                        pagoFlexViewModel.resolverReporte(
                            reporte.folio,
                            estado,
                            respuesta
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
                if (usuarioActual?.rolNombre == "EJECUTIVO") {
                    DetalleEjecutivoScreen(
                        compromiso = compromiso,
                        reporte = reportes.find { it.folio == folio },
                        onVolver = { navController.navigateUp() },
                        onModificarCuota = { monto, fecha ->
                            val id = usuarioActual?.id
                            val guardado = id != null && pagoFlexViewModel.modificarCuotaEjecutivo(id, folio, monto, fecha)
                            if (guardado) withContext(Dispatchers.IO) {
                                pagoFlexViewModel.cargarCompromisos(pagoFlexViewModel.listarCompromisosEjecutivo(id))
                            }
                            guardado
                        },
                        onAnular = { motivo ->
                            val id = usuarioActual?.id
                            val anulado = id != null && pagoFlexViewModel.anularCompromisoEjecutivo(id, folio, motivo)
                            if (anulado) withContext(Dispatchers.IO) {
                                pagoFlexViewModel.cargarCompromisos(pagoFlexViewModel.listarCompromisosEjecutivo(id))
                            }
                            anulado
                        }
                    )
                } else DetalleScreen(
                    compromiso = compromiso,
                    onCrearCuotas = if (
                        usuarioActual?.rolNombre == "CLIENTE" &&
                        CatalogoServicios.buscar(compromiso.empresa) != null &&
                        !CatalogoServicios.esDuoc(compromiso.empresa) &&
                        compromiso.estado in listOf("Pendiente", "Vencido") &&
                        compromiso.numeroCuota.isBlank() && compromiso.planId.isBlank()
                    ) {
                        { cantidad ->
                            usuarioActual?.id?.let { pagoFlexViewModel.crearCuotasServicio(it, folio, cantidad) }
                                ?: Resultado.Error("Inicia sesión para crear un plan de cuotas")
                        }
                    } else null,
                    onAplazarCuota = if (
                        usuarioActual?.rolNombre == "CLIENTE" &&
                        CatalogoServicios.buscar(compromiso.empresa) != null &&
                        !CatalogoServicios.esDuoc(compromiso.empresa) &&
                        compromiso.estado == "Pendiente" && compromiso.planId.isNotBlank()
                    ) {
                        {
                            usuarioActual?.id?.let { pagoFlexViewModel.aplazarCuotaServicio(it, folio) }
                                ?: Resultado.Error("Inicia sesión para aplazar una cuota")
                        }
                    } else null,
                    montoPagado = if (compromiso.estado == "Pagado") pagoFlexViewModel.buscarComprobante(folio)?.montoPagado else null,
                    onVerComprobante = if (compromiso.estado == "Pagado" && pagoFlexViewModel.buscarComprobante(folio) != null) {
                        { navController.navigate(Ruta.Comprobante.crear(folio)) }
                    } else null,
                    saldoPlan = if (compromiso.planId.isBlank()) null else compromisos
                        .filter { it.planId == compromiso.planId && it.estado in listOf("Pendiente", "Vencido", "En revisión") }
                        .sumOf { it.monto + if (it.estado == "Vencido") it.recargo else 0 },
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

        composable(Ruta.RegistrarCompromiso.ruta) {
            RegistrarCompromisoScreen(
                onVolver = { navController.navigateUp() },
                onGuardar = { rut, concepto, monto, fecha, cuota ->
                    val id = usuarioActual?.id
                    val guardado = id != null && pagoFlexViewModel.registrarCompromisoEjecutivo(id, rut, concepto, monto, fecha, cuota)
                    if (guardado) withContext(Dispatchers.IO) {
                                pagoFlexViewModel.cargarCompromisos(pagoFlexViewModel.listarCompromisosEjecutivo(id))
                            }
                    guardado
                }
            )
        }

        composable(Ruta.Ejecutivo.ruta) {
            EjecutivoScreen(
                usuario = usuarioActual,
                compromisos = compromisos,
                montosPagados = montosPagadosEjecutivo,
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
                    navController.navigate(Ruta.RegistrarCompromiso.ruta)
                }
            )
        }
    }
}

private fun rutaParaRol(rol: String): String {
    return when (rol.uppercase()) {
        "CLIENTE", "USUARIO_FINAL" -> Ruta.Usuario.ruta
        "AGENTE" -> Ruta.Agente.ruta
        "EJECUTIVO" -> Ruta.Ejecutivo.ruta
        else -> Ruta.Login.ruta
    }
}
