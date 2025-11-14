package ar.com.hipotecario.mobile;

import ar.com.hipotecario.backend.Servidor;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.mobile.api.*;
import ar.com.hipotecario.mobile.endpoints.EMBLogin;
import ar.com.hipotecario.mobile.lib.Util;

public class ApiMobile extends CanalMobile {

    public static void main(String[] args) throws Exception {
        Servidor.main(args);
    }

    public static void iniciar() {

        // 00. Cache
        new Futuro<>(() -> EMBLogin.iniciarCacheClientes());
        //new Futuro<>(() -> EMBLogin.iniciarCacheCobisAfectados());
        cron("* * * * *", new CacheMB.CronMockServicios().load());
        cron("* * * * *", new CacheMB.CronEstadisticas().load());
        cron("*/5 * * * *", new CacheMB.CronSuperCache().load());

        // 00. Aplicacion
        get("/mb/health", contexto -> MBAplicacion.estado(contexto));
        get("/mb/headers", contexto -> MBAplicacion.headers(contexto));
        post("/mb/api/config", contexto -> MBAplicacion.configuracion(contexto));
        get("/mb/api/version", contexto -> MBAplicacion.version(contexto));

        // 01. Login
        post("/mb/api/login-new", MBLogin::loginNew);
        post("/mb/api/login", MBLogin::login);
        post("/mb/api/pseudo-login", contexto -> MBLogin.pseudoLogin(contexto));
        post("/mb/api/logout", contexto -> MBLogin.logout(contexto));
        post("/mb/api/tipos-documento", contexto -> MBLogin.tiposDocumento());
        post("/mb/api/tipos-sexo", contexto -> MBLogin.tiposSexos());
        post("/mb/api/login-token", contexto -> MBLogin.loginToken(contexto));
        post("/mb/api/token-sesion", contexto -> MBLogin.tokenSesion(contexto));
        post("/mb/api/refrescar-sesion", contexto -> MBLogin.refrescarSesion());
        post("/mb/api/preguntas-riesgonet", contexto -> MBLogin.preguntasRiesgoNet(contexto));
        post("/mb/api/responder-preguntas-riesgonet", contexto -> MBLogin.responderPreguntasRiesgoNet(contexto));

        // 02. Seguridad
        post("/mb/api/canales-otp", contexto -> MBSeguridad.canalesOTP(contexto));
        post("/mb/api/canales-otp-usuario", contexto -> MBSeguridad.canalesOTPorUsuario(contexto));
        post("/mb/api/pedir-otp", contexto -> MBSeguridad.pedirOTP(contexto));
        post("/mb/api/validar-otp", contexto -> MBSeguridad.validarOTP(contexto));
        post("/mb/api/pedir-tarjeta-coordenadas", contexto -> MBSeguridad.pedirTarjetaCoordenadas(contexto));
        post("/mb/api/validar-tarjeta-coordenadas", contexto -> MBSeguridad.validarTarjetaCoordenadas(contexto));
        post("/mb/api/validar-tarjeta-coordenadas-preproducidas", contexto -> MBSeguridad.validarTarjetaCoordenadasPreproducidas(contexto));
        get("/mb/api/pedir-captcha", contexto -> MBSeguridad.pedirCaptcha(contexto));
        post("/mb/api/validar-captcha", contexto -> MBSeguridad.validarCaptcha(contexto));
        post("/mb/api/validar-clave-link", contexto -> MBSeguridad.validarClaveLink(contexto));
        post("/mb/api/solicitar-tco", contexto -> MBSeguridad.solicitudTCO(contexto));
        post("/mb/api/consulta-preguntas-por-defecto", contexto -> MBSeguridad.consultaPreguntasPorDefecto(contexto));
        post("/mb/api/enrolar-preguntas-seguridad", contexto -> MBSeguridad.enrolarPreguntasDesafio(contexto));
        post("/mb/api/consulta-preguntas-desafio", contexto -> MBSeguridad.consultaPreguntasDesafio(contexto));
        post("/mb/api/tiene-tarjeta-coordenadas", contexto -> MBSeguridad.tieneTCO(contexto));
        post("/mb/api/desbloquear-tco", contexto -> MBSeguridad.desbloquearTCO(contexto));
        post("/mb/api/baja-tco", contexto -> MBSeguridad.bajaTCO(contexto));
        post("/mb/api/pedir-activacion-tco", contexto -> MBSeguridad.pedirActivacionTCO(contexto));
        post("/mb/api/estado-tco", contexto -> MBSeguridad.consultaEstadoTCO(contexto));
        post("/mb/api/validar-preguntas-seguridad", contexto -> MBSeguridad.validarPreguntasSeguridad(contexto));
        post("/mb/api/historial-actividades", contexto -> MBSeguridad.historialActividades(contexto));
        post("/mb/api/access-token-gire", contexto -> MBSeguridad.accessTokenGire(contexto));
        post("/mb/api/es-gire-usuario", contexto -> MBSeguridad.esGireUsuario(contexto));
        get("/mb/api/cliente-exterior", contexto -> MBSeguridad.getClienteExterior(contexto));
        post("/mb/api/validar-otp-soft-token", contexto -> MBSeguridad.validarOTPSoftToken(contexto));
        get("/mb/api/obtener-tipo-cliente-transmit", MBPersona::obtenerDatosClientesTransmit);
        post("/mb/api/claves/link/transmit/validar", MBSeguridad::validarClaveLinkTransmit);
        get("/mb/api/validadores-otp", MBSeguridad::limpiarValidadoresDrs);

        // 03. Usuario
        post("/mb/api/nuevo-usuario", contexto -> MBUsuario.nuevoUsuario(contexto));
        post("/mb/api/nuevo-usuario-SS", contexto -> MBUsuario.nuevoUsuarioSS(contexto));
        post("/mb/api/cambiar-usuario", contexto -> MBUsuario.cambiarUsuario(contexto));
        post("/mb/api/cambiar-clave", contexto -> MBUsuario.cambiarClave(contexto));
        post("/mb/api/cambiar-usuario-logueado", contexto -> MBUsuario.cambiarUsuarioLogueado(contexto));
        post("/mb/api/cambiar-clave-logueado", contexto -> MBUsuario.cambiarClaveLogueado(contexto));
        post("/mb/api/cambiar-usuario-transmit", MBUsuario::cambiarUsuarioTrasmit);
        post("/mb/api/cambiar-clave-transmit", MBUsuario::cambiarClaveTrasmit);

        // 04. Persona
        post("/mb/api/persona", contexto -> MBPersona.cliente(contexto));
        post("/mb/api/actualizar-datos-personales", contexto -> MBPersona.actualizarDatosPersonales(contexto));
        post("/mb/api/paises", contexto -> MBPersona.paises(contexto));
        post("/mb/api/provincias", contexto -> MBPersona.provincias(contexto));
        post("/mb/api/localidades", contexto -> MBPersona.localidades(contexto));
        post("/mb/api/telefono-personal", contexto -> MBPersona.telefonoPersonal(contexto));
        post("/mb/api/funcionalidades-segundo-factor", contexto -> MBPersona.funcionalidadesSegundoFactor(contexto));
        post("/mb/api/validadores-segundo-factor", contexto -> MBPersona.validadoresSegundoFactor(contexto));
        post("/mb/api/permitir-segundo-factor-otp", contexto -> MBPersona.permitirSegundoFactorOtp(contexto));
        post("/mb/api/marcas-cliente", contexto -> MBPersona.marcasCliente(contexto));
        post("/mb/api/buscar-persona", contexto -> MBPersona.buscarPersona(contexto));
        post("/mb/api/domicilio-tarjeta-credito", contexto -> MBPersona.domicilioTarjetaCredito(contexto));
        post("/mb/api/consulta-validacion-censo-nacional", contexto -> MBPersona.consultaValidacionCensoNacional(contexto));
        get("/mb/api/account-officer-persona", contexto -> MBPersona.accountOfficer(contexto));
        get("/mb/api/account-officer", contexto -> MBCRM.accountOfficer(contexto));
        post("/mb/api/usage-data", contexto -> MBPersona.usageData(contexto));
        get("/mb/api/datavalid-vigente", contexto -> MBPersona.dataValid(contexto));
        get("/mb/api/datavalid-vigente-otp", contexto -> MBPersona.dataValidOtp(contexto));
        post("/mb/api/validar-id-dispositivo", contexto -> MBPersona.validarIdDispositivo(contexto));
        post("/mb/api/validar-datos-renaper", contexto -> MBPersona.validarDatosRenaper(contexto));
        post("/mb/api/validar-datos-crm", contexto -> MBPersona.validarDatosCRM(contexto));
        post("/mb/api/scan-dni-dispositivo", contexto -> MBPersona.scanDniDispositivo(contexto));
        post("/mb/api/scan-dni-renaper", contexto -> MBPersona.scanDniRenaper(contexto));
        get("/mb/api/validar-estado-usuario", contexto -> MBPersona.validarEstadoUsuario(contexto));
        post("/mb/api/actualizar-estado-usuario-crm", contexto -> MBPersona.actualizarEstadoUsuario(contexto));
        get("/mb/api/validar-onboarding-tdv", contexto -> MBPersona.validarEsOnboardingTdv(contexto));
        get("/mb/api/tiene-bloqueo-operaciones", contexto -> MBPersona.tieneBloqueoOperaciones(contexto));
        get("/mb/api/modal-tcv", contexto -> MBPersona.mostrarModalTcv(contexto));
        post("/mb/api/datos-adicionales-tcv", contexto -> MBPersona.guardarAdicionalesTcv(contexto));

        // 05. Archivo
        post("/mb/api/terminos-condiciones", contexto -> MBArchivo.terminosCondiciones(contexto));
        get("/mb/api/documento-digitalizado", contexto -> MBArchivo.archivoDigitalizado(contexto));
        get("/mb/api/comprobante", contexto -> MBArchivo.comprobante(contexto));
        post("/mb/api/libre-deuda", contexto -> MBArchivo.simularLibreDeuda(contexto));
        get("/mb/api/libre-deuda", contexto -> MBArchivo.libreDeuda(contexto));
        post("/mb/api/descarga-adjunto", contexto -> MBArchivo.descargaAdjunto(contexto));

        // 06. Producto
        post("/mb/api/productos", contexto -> MBProducto.productos(contexto));
        post("/mb/api/limpiar-cache", contexto -> MBProducto.limpiarCache(contexto));
        post("/mb/api/cuentas", contexto -> MBProducto.cuentas(contexto));
        post("/mb/api/cuentas-comitentes", contexto -> MBProducto.cuentasComitentes(contexto));
        post("/mb/api/cuentas-cuotapartistas", contexto -> MBProducto.cuentasCuotapartistas(contexto));
        post("/mb/api/cajas-seguridad", contexto -> MBProducto.cajasSeguridad(contexto));
        get("/mb/api/boton-arrepentimiento", contexto -> MBProducto.botonArrepentimiento(contexto));
        post("/mb/api/aceptacion-propuesta", contexto -> MBProducto.aceptarPropuesta(contexto));
        get("/mb/api/consulta-propuestas", contexto -> MBProducto.getPropuestas(contexto));
        get("/mb/api/disponible-propuestas", contexto -> MBProducto.propuestasDisponibilidad(contexto));
        get("/mb/api/comprobante-propuesta", contexto -> MBProducto.comprobantePropuesta(contexto));
        get("/mb/api/historial-propuestas", contexto -> MBProducto.consultarHistorialPropuestas(contexto));
        get("/mb/api/mostrar-onboarding", contexto -> MBProducto.mostrarOnboarding(contexto));
        get("/mb/api/disponible-cashback", contexto -> MBProducto.validarCashBack(contexto));
        get("/mb/api/consulta-cashback", ctx -> MBProducto.consultaCashBack(ctx));
        post("/mb/api/canjear-cashback", ctx -> MBProducto.canjearCashback(ctx));
        get("/mb/api/comprobante-cashback", ctx -> MBProducto.comprobanteCashback(ctx));


        // 07. Cuenta
        post("/mb/api/consolidada-cuentas", contexto -> MBCuenta.consolidadaCuentas(contexto));
        post("/mb/api/modificar-alias-cuenta", contexto -> MBCuenta.modificarAliasCuenta(contexto));
        post("/mb/api/modificar-comentario-cuenta", contexto -> MBCuenta.modificarComentarioCuenta(contexto));
        post("/mb/api/movimientos-cuenta", contexto -> MBCuenta.movimientosCuenta(contexto));
        post("/mb/api/consolidado-impuestos-cuentas", contexto -> MBCuenta.consolidadoImpuestos(contexto));
        post("/mb/api/compartir-cbu", contexto -> MBCuenta.compartirCBU(contexto));
        post("/mb/api/valores-suspenso", contexto -> MBCuenta.valoresSuspenso(contexto));
        post("/mb/api/caja_ahorro_bloqueos", contexto -> MBCuenta.cajaAhorroBloqueos(contexto));
        post("/mb/api/cuentas_comitentes_asociadas", contexto -> MBCuenta.cuentasComitentesAsociadas(contexto));
        post("/mb/api/baja-caja-ahorro", contexto -> MBProducto.bajaCajaAhorro(contexto));
        post("/mb/api/consolidada-resumen-cuenta", contexto -> MBCuenta.consolidadaResumenCuenta(contexto));
        post("/mb/api/periodos-resumen-cuenta", contexto -> MBCuenta.periodosResumenCuenta(contexto));
        post("/mb/api/resumen-cuenta", contexto -> MBCuenta.resumenCuenta(contexto));
        post("/mb/api/resumen-plazo-fijo", contexto -> MBCuenta.resumenPlazoFijo(contexto));
        post("/mb/api/consolidada-cambio-cuenta-principal", contexto -> MBCuenta.consolidadaCambioCuentaPrincipal(contexto));
        post("/mb/api/cambiar-cuenta-principal", contexto -> MBCuenta.cambiarCuentaPrincipal(contexto));
        post("/mb/api/renta-financiera", contexto -> MBCuenta.rentaFinanciera(contexto));
        post("/mb/api/consulta-cuenta", contexto -> MBCuenta.consultaCuenta(contexto));
        post("/mb/api/historico-movimientos", contexto -> MBCuenta.historicoMovimientosCuenta(contexto));
        post("/mb/api/consultar-cuenta-td-virtual", contexto -> MBCuenta.validarCuentaAsociadaVirtual(contexto));
        post("/mb/api/estados-cuenta", contexto -> MBCuenta.estadosCuentaAsync(contexto));
        post("/mb/api/cuenta-tarjeta-debito", contexto -> MBCuenta.getCuentaTD(contexto));
        post("/mb/api/alta-cuenta-especial", ctx -> MBCuenta.altaCuentaEspecial(ctx));
        post("/mb/api/oferta-caja-ahorro-dolar", MBCuenta::ofertaCajaAhorroDolar);

        // 08. Tarjetas
//		ApiTarjetas apiTarjetas = new ApiTarjetas();
        post("/mb/api/consolidada-tarjetas", contexto -> MBTarjetas.consolidadaTarjetas(contexto));
        get("/mb/api/consolidada-tarjetas-credito", contexto -> MBTarjetas.consolidadaTarjetasCredito(contexto));
        post("/mb/api/tc/imprimir", contexto -> MBTarjetas.solicitarImpresion(contexto));
        post("/mb/api/resumen-cuenta-tarjeta", contexto -> MBTarjetas.resumenCuenta(contexto));
        post("/mb/api/autorizaciones-tarjeta", contexto -> MBTarjetas.autorizaciones(contexto));
        post("/mb/api/cuotas-pendientes-tarjeta", contexto -> MBTarjetas.cuotasPendientes(contexto));
        post("/mb/api/movimientos-tarjeta", contexto -> MBTarjetas.movimientos(contexto));
        post("/mb/api/categoria-movimiento-tarjeta", contexto -> MBTarjetas.categoriaMovimientoTarjeta(contexto));
        post("/mb/api/detalle-movimientos-comercio", contexto -> MBTarjetas.detalleMovimientoComercio(contexto));
        post("/mb/api/detalle-actual-tarjeta", contexto -> MBTarjetas.infoActualizada(contexto));
        post("/mb/api/datos-para-pagar", contexto -> MBTarjetas.datosParaPagar(contexto));
        post("/mb/api/pagar-tarjeta", contexto -> MBTarjetas.pagarTarjeta(contexto));
        post("/mb/api/programar-pago-tarjeta", contexto -> MBTarjetas.programarPagoTarjeta(contexto));
        post("/mb/api/pagos-programados-tarjeta-credito", contexto -> MBTarjetas.consultarPagosProgramadosTarjetaCredito(contexto));
        get("/mb/api/ultima-liquidacion", contexto -> MBTarjetas.ultimaLiquidacion(contexto));
        post("/mb/api/blanquear-pil", contexto -> MBTarjetas.blanquearPil(contexto));
        post("/mb/api/blanquear-pin", contexto -> MBTarjetas.blanquearPin(contexto));
        post("/mb/api/estado-tarjeta-debito", contexto -> MBTarjetas.tarjetaDebitoGetEstado(contexto));
        post("/mb/api/habilitar-tarjeta-debito", contexto -> MBTarjetas.habilitarTarjetaDebito(contexto));
        post("/mb/api/limites-tarjeta-debito", contexto -> MBTarjetas.limitesTarjetaDebito(contexto));
        post("/mb/api/modificar-limite-tarjeta-debito", contexto -> MBTarjetas.modificarLimiteTarjetaDebito(contexto));
        post("/mb/api/consolidada-forma-pago-tarjeta-credito", contexto -> MBTarjetas.consolidadaFormaPagoTarjetaCredito(contexto));
        post("/mb/api/cambiar-forma-pago-tarjeta-credito", contexto -> MBTarjetas.cambiarFormaPagoTarjetaCredito(contexto));
        post("/mb/api/adicionales-propias", contexto -> MBTarjetas.consultaAdicionalesPropias(contexto));
        post("/mb/api/tarjetas-credito-propias", contexto -> MBTarjetas.tarjetasCreditoPropias(contexto));
        post("/mb/api/oferta-solicitud-tarjeta-credito-adicional", contexto -> MBTarjetas.ofertaSolicitudTarjetaCreditoAdicional(contexto));
        post("/mb/api/crear-solicitud-tarjeta-credito-adicional", contexto -> MBTarjetas.crearSolicitudTarjetaCreditoAdicional(contexto));
        post("/mb/api/cambiar-limite-tarjeta-credito-adicional", contexto -> MBTarjetas.cambioLimiteTarjetaCreditoAdicional(contexto));
        post("/mb/api/horario-pago-tarjeta", contexto -> MBTarjetas.horarioPagoTarjeta(contexto));
        post("/mb/api/consolidada-tarjetasFull", contexto -> MBTarjetas.consolidadaTarjetasFull(contexto));
        get("/mb/api/tarjetas-debito-habilitadas", contexto -> MBTarjetas.tarjetaDebitoHabilitadaRedLink(contexto));
        get("/mb/api/tooltip-tarjeta", contexto -> MBTarjetas.buscarTooltipConfiguracionTarjeta(contexto));
        post("/mb/api/tooltip-tarjeta", contexto -> MBTarjetas.agregarTooltipConfiguracionTarjeta(contexto));
        get("/mb/api/limites-tarjeta-credito", contexto -> MBTarjetas.limitesTarjetaCredito(contexto));
        post("/mb/api/adherir-resumen-digital", contexto -> MBTarjetas.adherirResumenDigital(contexto));
        post("/mb/api/consulta-resumen-digital", contexto -> MBTarjetas.consultaEResumenDigital(contexto));
        post("/mb/api/baja-tarjeta-credito-adicional", contexto -> MBProducto.bajaTarjetaCreditoAdicional(contexto));
        post("/mb/api/baja-tarjeta-credito-stop-debit", contexto -> MBTarjetas.stopDebit(contexto));
        post("/mb/api/reposicion-tarjeta-debito", contexto -> MBTarjetas.reposicionTD(contexto));
        get("/mb/api/validar-estados-baja-tarjeta-credito", contexto -> MBTarjetas.validarEstadosBajaTarjetaCredito(contexto));
        get("/mb/api/validar-cuotas-pendientes", contexto -> MBTarjetas.validarCuotasPendientes(contexto));
        post("/mb/api/crear-caso-baja-tarjeta-credito", contexto -> MBTarjetas.crearCasoBajaTarjetaCredito(contexto));
        post("/mb/api/convertir-tarjeta-debito-virutal-a-fisica", contexto -> MBTarjetas.convertirTarjetaDebitoVirtualToFisica(contexto));
        post("/mb/api/estados-deuda", contexto -> MBTarjetas.estadoDeuda(contexto));
        post("/mb/api/obtener-fechas-tc", contexto -> MBTarjetas.obtenerFechasTC(contexto));
        post("/mb/api/horario-bancario-tarjeta", contexto -> MBTarjetas.horarioBancarioPagoTarjeta(contexto));
        post("/mb/api/carteras-tc", contexto -> MBTarjetas.getCarterasTC(contexto));
        post("/mb/api/puede-pedir-cambio-cartera", contexto -> MBTarjetas.puedePedirCambioCartera(contexto));
        post("/mb/api/crear-caso-cambio-cartera", contexto -> MBTarjetas.crearCasoCambioCartera(contexto));
        post("/mb/api/mostrar-opcion-cambio-cartera", contexto -> MBTarjetas.mostarOpcionCambioCartera(contexto));
        get("/mb/api/obtener-cvv-tarjeta-credito", contexto -> MBTarjetas.obtenerCvvTcPrisma(contexto));
        post("/mb/api/solicitud-plastico-tc-virtual", contexto -> MBTarjetas.solicitudPlasticoTCvirtual(contexto));
        get("/mb/api/puede-solicitar-tc-adicional", contexto -> MBTarjetas.puedeCrearSolicitudTarjetaCreditoAdicional(contexto));
        post("/mb/api/tc-adicional-en-curso", contexto -> MBTarjetas.AdicionalEnCurso(contexto));
        post("/mb/api/consulta-tiene-tdvirtual", contexto -> MBTarjetas.consultaTDVirtual(contexto));
        post("/mb/api/generar-reclamo-promo-td", contexto -> MBTarjetas.promoNoImpactadaTD(contexto));
        post("/mb/api/generar-reclamo-consumo-td", contexto -> MBTarjetas.crearCasoDesconomientoConsumo(contexto));
        post("/mb/api/pausar-tarjeta-debito", ctx -> MBTarjetas.pausarTarjetaDebito(ctx));
        post("/mb/api/pausar-tarjeta-debito-link", ctx -> MBTarjetas.pausarTarjetaDebitoLinkContingencia(ctx));
        post("/mb/api/pausar-tarjeta-debito-core", ctx -> MBTarjetas.pausarTarjetaDebitoCoreContingencia(ctx));
        post("/mb/api/pausar-tarjeta-credito", ctx -> MBTarjetas.pausarTarjetaCredito(ctx));
        post("/mb/api/estado-pausada-tarjeta-credito", ctx -> MBTarjetas.estadoTarjetaCredito(ctx));
        get("/mb/api/tarjeta-credito-datos", ctx -> MBTarjetas.obtenerDatosTarjetaCredito(ctx));
        post("/mb/api/td-aviso-viaje", ctx -> MBTarjetas.avisarViajeExterior(ctx));


        // 09. PlazoFijo
        post("/mb/api/consolidada-plazos-fijos", contexto -> MBPlazoFijo.consolidadaPlazosFijo(contexto));
        post("/mb/api/consolidada-plazos-fijos-uva", contexto -> MBPlazoFijo.consolidadaPlazosFijoUva(contexto));
        post("/mb/api/tipos-plazos-fijos", contexto -> MBPlazoFijo.tiposPlazosFijos(contexto));
        post("/mb/api/simular-plazo-fijo", contexto -> MBPlazoFijo.simularPlazoFijo(contexto));
        post("/mb/api/alta-plazo-fijo", contexto -> MBPlazoFijo.altaPlazoFijo(contexto));
        post("/mb/api/baja-renovacion-plazo-fijo", contexto -> MBPlazoFijo.bajaRenovacionPlazoFijo(contexto));
        post("/mb/api/precancelar-procrear-joven", contexto -> MBPlazoFijo.precancelarProcrearJoven(contexto));
        post("/mb/api/tasa-preferencial", contexto -> MBPlazoFijo.tasaPreferencial(contexto));
        post("/mb/api/detalle-plazo-fijo", contexto -> MBPlazoFijo.detallePlazoFijo(contexto));
        get("/mb/api/comprobante-plazo-fijo", contexto -> MBPlazoFijo.comprobantePlazosFijo(contexto));
        post("/mb/api/precancelar-uva-cer", contexto -> MBPlazoFijo.precancelarPlazoFijoUvaCer(contexto));
        post("/mb/api/obtener-cvv-td-link", contexto -> MBTarjetas.obtenerCvvTdLink(contexto));
        post("/mb/api/simular-plazos-fijo-por-tipo", contexto -> MBPlazoFijo.simularPlazosFijoPorTipo(contexto));
        get("/mb/api/consulta-titularidad-td", contexto -> MBTarjetas.obtenerTitularidadTd(contexto));

        // 10. PlazoFijoLogro
        post("/mb/api/consolidada-plazos-fijos-logros", contexto -> MBPlazoFijoLogro.consolidadaPlazosFijoLogros(contexto));
        post("/mb/api/tyc-plazos-fijos-logros", contexto -> MBPlazoFijoLogro.terminosCondicionesPlazosFijoLogros(contexto));
        post("/mb/api/monedas-plazo-fijo-logro", contexto -> MBPlazoFijoLogro.monedaValidasPlazosFijos(contexto));
        post("/mb/api/parametria-plazo-fijo-logro", contexto -> MBPlazoFijoLogro.parametriaPlazoFijoLogros(contexto));
        post("/mb/api/modificar-plazo-fijo-logro", contexto -> MBPlazoFijoLogro.modificarPlazoFijoLogros(contexto));
        post("/mb/api/simular-plazo-fijo-logro", contexto -> MBPlazoFijoLogro.simularPlazoFijoLogro(contexto));
        post("/mb/api/alta-plazo-fijo-logro", contexto -> MBPlazoFijoLogro.altaPlazoFijoLogro(contexto));
        post("/mb/api/baja-plazo-fijo-logro", contexto -> MBPlazoFijoLogro.bajaPlazoFijoLogro(contexto));
        post("/mb/api/forzar-plazo-fijo-logro", contexto -> MBPlazoFijoLogro.forzarPlazoFijoLogro(contexto));

        // 11. Divisa
        post("/mb/api/cotizaciones", contexto -> MBDivisa.cotizaciones(contexto));
        post("/mb/api/simular-compra-venta-dolares", contexto -> MBDivisa.simularCompraVentaDolares(contexto));
        post("/mb/api/compra-venta-dolares", contexto -> MBDivisa.compraVentaDolares(contexto));
        post("/mb/api/compra-venta-en-horario", contexto -> MBTransferencia.compraVentaDolarFueraDeHorario(contexto));

        // 12. Transferencia
        get("/mb/api/beneficiarios-transferencias", ctx -> MBTransferencia.beneficiariosV1(ctx));
        get("/mb/api/beneficiarios-transferencias-detalle", ctx -> MBTransferencia.beneficiariosDetalleV1(ctx));

        get("/mb/api/beneficiarios-transferencias-nuevo", ctx -> MBTransferencia.beneficiariosV2(ctx));
        post("/mb/api/beneficiarios-transferencias-detalle", ctx -> MBTransferencia.beneficiariosDetalleV2(ctx));
        post("/mb/api/beneficiarios-transferencias", contexto -> MBTransferencia.beneficiarios(contexto));
        post("/mb/api/agendar-beneficiario-transferencia", contexto -> MBTransferencia.agendarBeneficiario(contexto));
        post("/mb/api/modificar-beneficiario-transferencia", contexto -> MBTransferencia.modificarBeneficiario(contexto));
        post("/mb/api/modificar-beneficiario-apodo", ctx -> MBTransferencia.modificarBeneficiarioApodo(ctx));
        post("/mb/api/eliminar-beneficiario-transferencia", contexto -> MBTransferencia.eliminarBeneficiario(contexto));
        post("/mb/api/limites-transferencia", contexto -> MBTransferencia.limites(contexto));
        post("/mb/api/aumentar-limite-transferencia", contexto -> MBTransferencia.aumentarLimite(contexto));
        post("/mb/api/conceptos-transferencia", contexto -> MBTransferencia.conceptos(contexto));
        post("/mb/api/cuenta-tercero", contexto -> MBTransferencia.cuentaTercero(contexto));
        post("/mb/api/cuenta-tercero-coelsa", contexto -> MBTransferencia.cuentaTerceroCoelsa(contexto));
        post("/mb/api/transferir", contexto -> MBTransferencia.transferir(contexto));
        get("/mb/api/enabled-transfer", contexto -> MBTransferencia.isRiskForChangeInformation(contexto));
        get("/mb/api/puede-transferir-especial-mismo-dia", contexto -> MBTransferencia.puedeTransferirEspecialMismoDia(contexto));

        // 13. PagoServicio
        post("/mb/api/consolidada-pagos", contexto -> MBPago.consolidadaPagos(contexto));
        post("/mb/api/rubros-link", contexto -> MBPago.rubrosLink(contexto));
        post("/mb/api/entes-link", contexto -> MBPago.entesLink(contexto));
        post("/mb/api/agendar-pago-servicio", contexto -> MBPago.agendarPagoServicio(contexto));
        post("/mb/api/modificar-pago-servicio", contexto -> MBPago.modificarPagoServicio(contexto));
        post("/mb/api/desagendar-pago-servicio", contexto -> MBPago.desagendarPagoServicio(contexto));
        post("/mb/api/pagar-servicio", contexto -> MBPago.pagarServicio(contexto));
        post("/mb/api/entes-agendados-link", contexto -> MBPago.entesAgendadosLink(contexto));
        post("/mb/api/comprobantes-por-ente-pagos-link", contexto -> MBPago.comprobantesPorEntePagosLink(contexto));
        post("/mb/api/comprobantes-pago-servicio", contexto -> MBPago.comprobantesPagoServicio(contexto));
        get("/mb/api/comprobante-pago", contexto -> MBPago.comprobantePago(contexto));
        post("/mb/api/consulta-debitos-automaticos", contexto -> MBPago.consultaDebitosAutomaticos(contexto));// Listo
        post("/mb/api/eliminar-debito-automatico", contexto -> MBPago.eliminarDebitoAutomatico(contexto));
        post("/mb/api/rubros-debito-automatico", contexto -> MBPago.consultaRubrosDebitosAutomaticos(contexto));// Listo
        post("/mb/api/empresas-debito-automatico", contexto -> MBPago.consultaEmpresasDebitosAutomaticos(contexto));// Listo
        post("/mb/api/servicios-debito-automatico", contexto -> MBPago.consultaServiciosDebitosAutomaticos(contexto));// Listo
        post("/mb/api/tyc-adhesion-debito-automatico", contexto -> MBPago.tycAdhesionDebitosAutomaticos(contexto));// Listo
        post("/mb/api/agregar-debito-automatico", contexto -> MBPago.agregarDebitoAutomatico(contexto));// Listo

        // 14. PagoVep
        post("/mb/api/consolidada-veps", contexto -> MBPago.comprobantesPendientesVepPorCuit(contexto));
        post("/mb/api/pagar-vep", contexto -> MBPago.pagarVep(contexto));
        post("/mb/api/eliminar-vep", contexto -> MBPago.eliminarVep(contexto));
        post("/mb/api/comprobantes-vep", contexto -> MBPago.comprobantesVep(contexto));
        post("/mb/api/comprobantes-vep-pendientes", contexto -> MBPago.comprobantesPendientesVepPorCuit(contexto));

        // 15. Inversiones
        post("/mb/api/consolidada-inversiones", contexto -> MBInversion.consolidadaInversiones(contexto));
        post("/mb/api/licitaciones", contexto -> MBInversion.licitaciones(contexto));
        post("/mb/api/licitaciones-suscripcion-alta", contexto -> MBInversion.altaSuscripcionLicitacion(contexto));
        post("/mb/api/activos-vigentes", contexto -> MBInversion.activosVigentes(contexto));
        post("/mb/api/test-perfil-inversor", contexto -> MBInversion.testPerfilInversor(contexto));
        post("/mb/api/completar-test-perfil-inversor", contexto -> MBInversion.completarTestPerfilInversor(contexto));
        post("/mb/api/perfil-inversor", contexto -> MBInversion.perfilInversor(contexto));
        post("/mb/api/modificar-perfil-inversor-propio-riesgo", contexto -> MBInversion.perfilInversorPropioRiesgo(contexto));
        post("/mb/api/alta-cuenta-inversor", contexto -> MBInversion.altaCuentaInversor(contexto));
        post("/mb/api/cuenta-inversor", contexto -> MBInversion.cuentaInversor(contexto));
        post("/mb/api/cuotapartista", contexto -> MBInversion.cuotapartista(contexto));
        post("/mb/api/formulario", contexto -> MBInversion.formulario(contexto));
        post("/mb/api/liquidaciones", contexto -> MBInversion.liquidaciones(contexto));
        post("/mb/api/solicitudes-fci", contexto -> MBInversion.solicitudesFci(contexto));
        post("/mb/api/posicionCuotapartista", contexto -> MBInversion.posicionCuotapartista(contexto));
        post("/mb/api/fondos", contexto -> MBInversion.fondos(contexto));
        post("/mb/api/fondosAceptados", contexto -> MBInversion.fondosAceptados(contexto));
        post("/mb/api/rescate", contexto -> MBInversion.rescate(contexto));
        post("/mb/api/suscripcion", contexto -> MBInversion.suscripcion(contexto));
        post("/mb/api/poseeCuotapartista", contexto -> MBInversion.poseeCuotapartista(contexto));
        post("/mb/api/integrantes-perfil-inversor", contexto -> MBInversion.integrantesCuentaPerfilInversor(contexto));
        post("/mb/api/horario-cuenta-inversor", ctx -> MBInversion.horarioCuentaInversor(ctx));

        // 16. Notificaciones
        post("/mb/api/notificaciones", contexto -> MBNotificaciones.notificaciones(contexto));
        post("/mb/api/responder-notificacion", contexto -> MBNotificaciones.notificacionesRespuesta(contexto));
        post("/mb/api/configuracion-alertas", contexto -> MBNotificaciones.configuracionAlertas(contexto));
        post("/mb/api/modificar-configuracion-alertas", contexto -> MBNotificaciones.modificarConfiguracionAlertas(contexto));
        get("/mb/api/notificaciones-descarga", contexto -> MBNotificaciones.notificationesDescarga(contexto));
        post("/mb/api/actualizacion-notificaciones-descarga", contexto -> MBNotificaciones.actualizarNotificacion(contexto));

        // 17. Sitio Externo
        post("/mb/api/token-afip", contexto -> MBPago.tokenAfip(contexto));
        post("/mb/api/token-vfhome", contexto -> MBSitioExterno.vfhome(contexto));
        post("/mb/api/token-todopago", contexto -> MBSitioExterno.todopago(contexto));
        post("/mb/api/permitir-salto-visahome", contexto -> MBSitioExterno.permitirSaltoVisaHome(contexto));
        post("/mb/api/permitir-salto-todopago", contexto -> MBSitioExterno.permitirSaltoTodoPago(contexto));

        // 18. Acumar
        post("/mb/api/oferta-acumar", contexto -> MBAcumar.oferta(contexto));
        post("/mb/api/detalle-acumar", contexto -> MBAcumar.detalle(contexto));
        post("/mb/api/subir-documentacion-acumar", contexto -> MBAcumar.subirDocumentacion(contexto));
        post("/mb/api/preparar-alta-acumar", contexto -> MBAcumar.prepararAltaAcumar(contexto));
        get("/mb/api/terminos-condiciones-acumar", contexto -> MBAcumar.terminosCondiciones(contexto));
        get("/mb/api/terminos-condiciones-originacion", contexto -> MBAcumar.terminosCondiciones(contexto));
        post("/mb/api/terminos-condiciones-originacion-string", contexto -> MBAcumar.terminosCondicionesString(contexto));
        post("/mb/api/finalizar-alta-acumar", contexto -> MBAcumar.finalizarAltaAcumar(contexto));
        post("/mb/api/alta-acumar", contexto -> MBAcumar.altaAcumar(contexto));

        // 19. Paquetes
        post("/mb/api/consolida-paquetes", contexto -> MBPaquetes.consolidadaPaquetes(contexto));
        post("/mb/api/historico-bonificaciones", contexto -> MBPaquetes.historicoBonificaciones(contexto));
        post("/mb/api/historico-bonificaciones-detalle", contexto -> MBPaquetes.historicoBonificacionesDetalle(contexto));
        post("/mb/api/baja-paquete", contexto -> MBProducto.bajaPaquete(contexto));

        // 20. Titulos y Valores
        post("/mb/api/tenencia-titulos-valores", contexto -> MBTitulosValores.tenencia(contexto));
        post("/mb/api/tenencia-venta-titulos-valores", contexto -> MBTitulosValores.tenenciaVenta(contexto));
        post("/mb/api/movimientos-titulos-valores", contexto -> MBTitulosValores.movimientos(contexto));
        post("/mb/api/seguimiento-operaciones-titulos-valores", contexto -> MBTitulosValores.seguimientoOperaciones(contexto));
        post("/mb/api/seguimiento-licitaciones-titulos-valores", contexto -> MBTitulosValores.seguimientoLicitaciones(contexto));
        post("/mb/api/tipos-activo-titulos-valores", contexto -> MBTitulosValores.tiposActivo(contexto));
        post("/mb/api/especies-titulos-valores", contexto -> MBTitulosValores.especies(contexto, true));
        post("/mb/api/calcular-cantidad-nominal-compra", contexto -> MBTitulosValores.calcularCantidadNominalCompra(contexto));
        post("/mb/api/detalle-especie-titulos-valores", contexto -> MBTitulosValores.detalleEspecie(contexto));
        post("/mb/api/plazos-titulos-valores", contexto -> MBTitulosValores.plazosValidos(contexto));
        post("/mb/api/cuentas-asociadas-comitente", contexto -> MBTitulosValores.cuentasAsociadasComitente(contexto));
        post("/mb/api/simular-compra-titulo-valor", contexto -> MBTitulosValores.simularCompra(contexto));
        post("/mb/api/comprar-titulo-valor", contexto -> MBTitulosValores.comprar(contexto));
        post("/mb/api/simular-venta-titulo-valor", contexto -> MBTitulosValores.simularVenta(contexto));
        post("/mb/api/vender-titulo-valor", contexto -> MBTitulosValores.vender(contexto));
        get("/mb/api/comprobante-cuenta-comitente", contexto -> MBTitulosValores.comprobanteCuentaComitente(contexto));
        get("/mb/api/comprobante-licitacion", contexto -> MBTitulosValores.comprobanteLicitaciones(contexto));
        get("/mb/api/comprobante-titulos-valores", contexto -> MBTitulosValores.comprobanteSeguimientoOperaciones(contexto));
        post("/mb/api/paneles-especies", contexto -> MBTitulosValores.panelesEspecies(contexto));
        post("/mb/api/inversiones-plazos", contexto -> MBTitulosValores.inversionesPlazos(contexto));
        post("/mb/api/indices-bursatiles-novedades-delay", contexto -> MBTitulosValores.indicesBursatilesDelay(contexto));
        post("/mb/api/indices-sectoriales-novedades-delay", contexto -> MBTitulosValores.indicesSectorialesDelay(contexto));
        post("/mb/api/paneles-cotizaciones-novedades-delay", contexto -> MBTitulosValores.panelesCotizacionesDelay(contexto));
        post("/mb/api/paneles-cauciones-novedades-delay", contexto -> MBTitulosValores.caucionesDelay(contexto));
        post("/mb/api/indices-bursatiles-novedades", contexto -> MBTitulosValores.indicesBursatilesRealTime(contexto));
        post("/mb/api/indices-sectoriales-novedades", contexto -> MBTitulosValores.indicesSectorialesRealTime(contexto));
        post("/mb/api/indices-novedades", contexto -> MBTitulosValores.indicesRealTime(contexto));
        post("/mb/api/paneles-cotizaciones-novedades", contexto -> MBTitulosValores.panelesCotizacionesRealTime(contexto));
        post("/mb/api/paneles-cauciones-novedades", contexto -> MBTitulosValores.caucionesRealTime(contexto));
        post("/mb/api/paneles-cotizaciones-especies-novedades", contexto -> MBTitulosValores.panelesCotizacionesRealTimeEspecie(contexto));
        post("/mb/api/profundidad-mercado", contexto -> MBTitulosValores.profundidadMercado(contexto));
        post("/mb/api/posiciones-negociables", contexto -> MBTitulosValores.posicionesNegociables(contexto));
        get("/mb/api/cotizacion-dolar-mep", contexto -> MBTitulosValores.cotizacionDolaMep(contexto));
        post("/mb/api/tenencia-titulos-valores-v2", contexto -> MBTitulosValores.tenenciaPosicionNegociable(contexto));
        post("/mb/api/tenencia-venta-titulos-valores-v2", contexto -> MBTitulosValores.tenenciaVentaPosicionNegociable(contexto));
        post("/mb/api/detalle-especie-titulos-valores-v2", contexto -> MBTitulosValores.detalleEspecieProductosOperables(contexto));
        post("/mb/api/especies-titulos-valores-v2", contexto -> MBTitulosValores.especies(contexto, false));
        get("/mb/api/cotizacion-plazo-liquidacion", contexto -> MBTitulosValores.obtenerCotizacionPorPlazoLiquidacion(contexto));
        get("/mb/api/en-horario-dolar-mep", contexto -> MBTitulosValores.enHorarioDolarMep(contexto));

        // 21. Catalogo
        post("/mb/api/nacionalidades", contexto -> MBCatalogo.nacionalidades(contexto));
        post("/mb/api/estados-civiles", contexto -> MBCatalogo.estadosCiviles(contexto));
        post("/mb/api/ciudades", contexto -> MBCatalogo.ciudades(contexto));
        post("/mb/api/situaciones-laborales", contexto -> MBCatalogo.situacionesLaborales(contexto));
        post("/mb/api/situaciones-vivienda", contexto -> MBCatalogo.situacionesVivienda(contexto));
        post("/mb/api/profesiones", contexto -> MBCatalogo.profesiones(contexto));
        post("/mb/api/niveles-estudio", contexto -> MBCatalogo.nivelesEstudio(contexto));
        post("/mb/api/sucursales", contexto -> MBCatalogo.sucursales(contexto));
        post("/mb/api/tipos-cuenta", contexto -> MBCatalogo.tiposCuenta(contexto));
        post("/mb/api/sucursalesPorProvincia", contexto -> MBCatalogo.sucursalesPorProvincia(contexto));
        post("/mb/api/sucursales-tipo-turno", contexto -> MBCatalogo.sucursalesPorTipoTurno(contexto));
        get("/mb/api/motivos", contexto -> MBCatalogo.motivos(contexto));
        post("/mb/api/localidades-por-codigo-postal", contexto -> MBCatalogo.localidadesPorCodigoPostal(contexto));
        post("/mb/api/provincias-por-codigo-postal", contexto -> MBCatalogo.provinciasPorCodigoPostal(contexto));
        get("/mb/api/catalogo-rubros", contexto -> MBCatalogo.rubros(contexto));

        // 22. Prestamo
        post("/mb/api/consolidada-prestamos", contexto -> MBPrestamo.consolidada(contexto));
        post("/mb/api/detalle-prestamo", contexto -> MBPrestamo.detalle(contexto));
        post("/mb/api/pagar-prestamo", contexto -> MBPrestamo.pagar(contexto));
        post("/mb/api/precancelacion-total-prestamo", contexto -> MBPrestamo.precancelacionTotal(contexto));
        post("/mb/api/ultima-liquidacion-prestamo", contexto -> MBPrestamo.ultimaLiquidacion(contexto));
        post("/mb/api/ultima-liquidacion-prestamo-nsp", contexto -> MBPrestamo.ultimaLiquidacionNsp(contexto));
        post("/mb/api/movimientos-prestamo", contexto -> MBPrestamo.movimientosPrestamo(contexto));
        post("/mb/api/movimientos-desembolso", contexto -> MBPrestamo.movimientosDesembolso(contexto));
        get("/mb/api/formas-pago-prestamo", contexto -> MBPrestamo.formasDePago(contexto));
        post("/mb/api/cambio-forma-pago-prestamo", contexto -> MBPrestamo.cambiarFormaPago(contexto));
        post("/mb/api/oferta-pp", contexto -> MBPrestamo.ofertaPreAprobadaPP(contexto));
        post("/mb/api/campanas", contexto -> MBPrestamo.ofertaPreAprobada(contexto));
        post("/mb/api/contador-oferta-pp", contexto -> Util.contador(contexto));
        post("/mb/api/auditor-terminos-condiciones", contexto -> MBPrestamo.aceptaTerminosCondiciones(contexto));
        post("/mb/api/alerta-prestamo-cuotificacion", ctx -> MBPrestamo.alertaPrestamoCuotificacion(ctx));
        post("/mb/api/movimientos-prestamo-cuotificacion", ctx -> MBPrestamo.movimientosCuotificacion(ctx));
        post("/mb/api/solicitar-cuotificacion", ctx -> MBPrestamo.solicitarCuotificacion(ctx));

        // 23. Originacion
        post("/mb/api/datos-personales-faltantes-originacion", contexto -> MBOriginacion.datosPersonalesFaltantes(contexto));
        post("/mb/api/datos-personales-originacion", contexto -> MBOriginacion.datosPersonales(contexto));
        post("/mb/api/datos-personales-modificar-faltantes", contexto -> MBOriginacion.datosPersonalesModificar(contexto));
        post("/mb/api/crear-paquete", contexto -> MBOriginacion.crearPaquete(contexto));
        post("/mb/api/consultar-oferta-paquetes", contexto -> MBOriginacion.consultarOfertaPaquetes(contexto));
        post("/mb/api/solicitar-primer-oferta-prestamo", contexto -> MBOriginacion.solicitarPrimerOfertaPrestamo(contexto));
        post("/mb/api/simular-oferta-prestamo", contexto -> MBOriginacion.simularOfertaPrestamo(contexto));
        post("/mb/api/consultar-solicitud-prestamo", contexto -> MBOriginacion.consultarPrestamo(contexto));
        post("/mb/api/finalizar-solicitud-prestamo", contexto -> MBOriginacion.finalizarSolicitudPrestamo(contexto));
        post("/mb/api/paquetes-beneficios", contexto -> MBCatalogo.paquetesBeneficios(contexto));
        post("/mb/api/evaluar-caso-paquete", contexto -> MBOriginacion.crearPaquete(contexto));
        get("/mb/api/consumos-sugeridos", contexto -> MBOriginacion.consultarConsumosSugeridos(contexto));
        post("/mb/api/auditor-consumo-sugerido", contexto -> MBUsuario.guardarAuditorConsumoSugerido(contexto));

        // 24. Omnicanalidad
        post("/mb/api/solicitudes", contexto -> MBOmnicanalidad.solicitudes(contexto));
        // respuesta = post && url.equals(base + "/api/detalle-solicitudes", contexto ->
        // ApiOmnicanalidad.detalleSolicitudes(contexto));
        post("/mb/api/detalle-solicitud", contexto -> MBOmnicanalidad.detalleSolicitud(contexto));
        post("/mb/api/desistir-solicitud", contexto -> MBOmnicanalidad.desistirSolicitud(contexto));
        post("/mb/api/eliminar-productos-extraordinarios-solicitud", contexto -> MBOmnicanalidad.eliminarProductosExtraordinarios(contexto));
        post("/mb/api/ofertas-paquete", contexto -> MBOmnicanalidad.ofertasPaquete(contexto));
        post("/mb/api/productos-paquetizables", contexto -> MBOmnicanalidad.productosPaquetizables(contexto));
        post("/mb/api/crear-solicitud-caja-ahorro", contexto -> MBOmnicanalidad.crearSolicitudCajaAhorro(contexto));
        get("/mb/api/seguro-atm-productos", contexto -> MBOmnicanalidad.seguroAtmProductos(contexto));
        post("/mb/api/post-solicitud-caja-ahorro", contexto -> MBOmnicanalidad.postSolicitudCajaAhorro(contexto));
        put("/mb/api/alta-seguro-atm", contexto -> MBOmnicanalidad.altaSeguroAtm(contexto));
        post("/mb/api/crear-solicitud-seguro-atm", contexto -> MBOmnicanalidad.crearSolicitudSeguroATM(contexto));
        put("/mb/api/put-resoluciones-atm", contexto -> MBOmnicanalidad.putResolucionesATM(contexto));
        post("/mb/api/actualizar-solicitud-caja-ahorro", contexto -> MBOmnicanalidad.actualizarSolicitudCajaAhorro(contexto));
        post("/mb/api/crear-solicitud-paquete", contexto -> MBOmnicanalidad.crearSolicitudPaquete(contexto));
        post("/mb/api/actualizar-solicitud-paquete", contexto -> MBOmnicanalidad.actualizarSolicitudPaquete(contexto));
        post("/mb/api/finalizar-solicitud-paquete", contexto -> MBOmnicanalidad.finalizarSolicitudPaquete(contexto));
        post("/mb/api/finalizar-solicitud-venta", contexto -> MBOmnicanalidad.finalizarSolicitudVenta(contexto));

        // TODO: AUT-274 MVP solo ruta Verde
        get("/mb/api/puede-solicitar-aumento", contexto -> MBAumentoLimiteTC.isAumentoLimiteTCSolicitado(contexto));
        post("/mb/api/crear-solicitud-aumento-limite-tc", contexto -> MBAumentoLimiteTC.crearSolicitudAumentoLimiteTC(contexto));
        post("/mb/api/confirmar-solicitud-aumento-limite-tc", contexto -> MBAumentoLimiteTC.confirmarSolicitudAumentoLimiteTC(contexto));
        post("/mb/api/desistir-solicitud-aumento-limite-tc", contexto -> MBAumentoLimiteTC.desistirSolicitudAumentoLimiteTC(contexto));

        // 25. Comprobantes
        post("/mb/api/comprobantes", contexto -> MBComprobantes.comprobantes(contexto));
        get("/mb/api/comprobante-usuario", contexto -> MBComprobantes.comprobante(contexto));

        // 26. Debin
        post("/mb/api/lista-debin", contexto -> MBDebin.listaDebin(contexto));
        post("/mb/api/lista-debin-recibidos", contexto -> MBDebin.listaDebinRecibidos(contexto));
        post("/mb/api/lista-debin-enviados", contexto -> MBDebin.listaDebinEnviados(contexto));
        post("/mb/api/detalle-debin", contexto -> MBDebin.detalleDebin(contexto));
        post("/mb/api/nuevo-debin", contexto -> MBDebin.nuevoDebin(contexto));
        post("/mb/api/aceptar-debin", contexto -> MBDebin.aceptarDebin(contexto));
        post("/mb/api/rechazar-debin", contexto -> MBDebin.rechazarDebin(contexto));
        post("/mb/api/activar-cuenta-debin", contexto -> MBDebin.activarCuentaDebin(contexto));
        post("/mb/api/desactivar-cuenta-debin", contexto -> MBDebin.desactivarCuentaDebin(contexto));
        post("/mb/api/consulta-recurrencias", MBDebin::consultaRecurrencias);
        post("/mb/api/aceptar-recurrencia", MBDebin::aceptarRecurrencia);
        post("/mb/api/rechazar-recurrencia", MBDebin::rechazarRecurrencia);
        post("/mb/api/baja-recurrencia", MBDebin::bajaRecurrencia);
        post("/mb/api/consulta-debines-programados", MBDebin::consultaDebinesProgramados);
        post("/mb/api/aceptar-debin-programado", MBDebin::aceptarDebinProgramado);
        post("/mb/api/rechazar-debin-programado", MBDebin::rechazarDebinProgramado);
        post("/mb/api/baja-debin-programado", MBDebin::bajaDebinProgramado);
        post("/mb/api/cancelar-debin-programado", MBDebin::cancelarDebinProgramado);

        // 27. Buho Puntos
        post("/mb/api/buhopuntos", contexto -> MBBuhoPuntos.consolidada(contexto));
        post("/mb/api/token-buhopuntos", contexto -> MBBuhoPuntos.token(contexto));
        post("/mb/api/historico-buhopuntos", contexto -> MBBuhoPuntos.canjesBuhoPuntos(contexto));

        // 28. Plan Sueldo Pick up
        post("/mb/api/consulta-recurrencia", contexto -> MBDebinRecurrente.consulta(contexto));
        post("/mb/api/alta-recurrencia", contexto -> MBDebinRecurrente.alta(contexto));
        post("/mb/api/modificar-recurrencia", contexto -> MBDebinRecurrente.modificacion(contexto));
        post("/mb/api/eliminar-recurrencia", contexto -> MBDebinRecurrente.baja(contexto));
        get("/mb/api/tyc-recurrencia", contexto -> MBDebinRecurrente.terminosCondiciones(contexto));
        post("/mb/api/consulta-aumento-sueldo", contexto -> MBDebinRecurrente.consultaAumentoMontoMinimo(contexto));

        // 29. Cheques
        post("/mb/api/cheques-pendientes", contexto -> MBCheques.chequesPendientes(contexto));
        post("/mb/api/cheques-rechazados", contexto -> MBCheques.chequesRechazados(contexto));

        // 30. Exportar
        post("/mb/api/exporta-movimientos", contexto -> MBExporta.exportarMovimientosCuenta(contexto));

        // 31. Consolidado
        post("/mb/api/consolidado-movimientos", contexto -> MBConsolidado.consolidadoMovimientos(contexto));
        get("/mb/api/proximos-vencimientos", contexto -> MBConsolidado.consolidadoProximosVencimientos(contexto));

        // 32. Encuesta
        get("/mb/api/encuesta-usuario", contexto -> MBUsuario.encuestaUsuario(contexto));
        post("/mb/api/guardar-encuesta", contexto -> MBUsuario.guardarEncuesta(contexto));

        // 33. Modo
        MBModo apiModo = new MBModo();
        post("/mb/api/ps-user-status", contexto -> apiModo.getUserStatus(contexto));
        get("/mb/api/ps-account-metadata", contexto -> apiModo.getAccountMetadata(contexto));
        get("/mb/api/ps-onboarding", contexto -> apiModo.isOnboarding(contexto));
        post("/mb/api/ps-get-accounts", contexto -> apiModo.getAllAccounts(contexto));
        get("/mb/api/ps-enabled-transfer", contexto -> apiModo.isRiskForChangeInformation(contexto));
        get("/mb/api/ps-accounts", contexto -> apiModo.getAccounts(contexto));
        post("/mb/api/onboarding-tarjetas", contexto -> MBModo.onboardingTarjetas(contexto));
        post("/mb/api/ps-onboarding", contexto -> MBModo.startOnboarding(contexto));
        post("/mb/api/dimo-users-confirm", contexto -> MBModo.confirmOnboarding(contexto));
        post("/mb/api/ps-check-contacts", contexto -> MBModo.listOfPhoneNumbersAreInPS(contexto));
        get("/mb/api/ps-suggested-contacts", contexto -> apiModo.suggestedContacts(contexto));
        post("/mb/api/dimo-users-phone", contexto -> MBModo.checkUserPhoneNumberRegistration(contexto));
        post("/mb/api/dimo-verifications-refresh", contexto -> MBModo.refreshVerification(contexto));
        post("/mb/api/ps-confirm-onboarding", contexto -> MBModo.confirmVerification(contexto));
        post("/mb/api/dimo-tokens-refresh", contexto -> MBModo.refreshAccessToken(contexto));
        post("/mb/api/dimo-tokens-revoke", contexto -> MBModo.revokeRefreshToken(contexto));
        post("/mb/api/dimo-insert", contexto -> MBModo.insertTokensModo(contexto));
        post("/mb/api/dimo-update", contexto -> MBModo.updateTokensModo(contexto));
        post("/mb/api/ps-accounts-link", contexto -> MBModo.linkBankAccount(contexto));
        post("/mb/api/ps-invite-message", contexto -> MBModo.inviteNotification(contexto));
        post("/mb/api/ps-transfer", contexto -> apiModo.createTransfer(contexto));
        get("/mb/api/ps-limit-amounts", contexto -> MBModo.limitAmounts(contexto));
        post("/mb/api/ps-canales-segundo-factor", contexto -> MBModo.canalesSegundoFactor(contexto));
        post("/mb/api/ps-create-coupon", contexto -> MBModo.createCoupon(contexto));
        post("/mb/api/ps-get-coupon", contexto -> MBModo.getCoupon(contexto));
        post("/mb/api/ps-pay-coupon", contexto -> MBModo.payCoupon(contexto));
        post("/mb/api/ps-payments", contexto -> MBModo.createPayment(contexto));
        post("/mb/api/ps-payments-qr", contexto -> MBModo.paymentQr(contexto));
        post("/mb/api/ps-get-cards", contexto -> MBModo.getCards(contexto));
        get("/mb/api/ps-crypto-pem", contexto -> MBModo.getCryptoPem(contexto));
        post("/mb/api/certificados/listado", contexto -> MBModo.obtenerCertificados(contexto));
        post("/mb/api/certificado", contexto -> MBModo.obtenerComprobante(contexto));

        //33.a MODO - Pagos V2
        post("/mb/api/ps-payments-qr/v2", contexto -> MBModo.paymentV2Qr(contexto));
        post("/mb/api/ps-payments/v2", contexto -> MBModo.createV2Payment(contexto));
        post("/mb/api/ps-payment-info/v2", contexto -> MBModo.getV2Payment(contexto));
        get("/mb/api/ps-payment-methods", contexto -> MBModo.getPaymentMethods(contexto));
        post("/mb/api/ps-payments/v3", contexto -> MBModo.createPaymentV3(contexto));
        get("/mb/api/ps-payments/v3", contexto -> MBModo.getPaymentV3(contexto));
        post("/mb/api/ps-payments/intention", contexto -> MBModo.createPaymentIntention(contexto));
        get("/mb/api/ps-payments/intention", contexto -> MBModo.getPaymentIntention(contexto));

        //33.b MODO - Consentimiento
        get("/mb/api/estado-usuario", contexto -> MBModo.estadoUsuario(contexto));
        get("/mb/api/ps-consentimiento", contexto -> MBModo.getUri(contexto));
        get("/mb/api/ps-consentimiento/pantalla-pcp", contexto -> MBModo.getPantallaPcpLanding(contexto));
        post("/mb/api/ps-consentimiento/revocar-pcp", contexto -> MBModo.revocarToken(contexto));

        //33.c MODO - Transferencias Pull
        get("/mb/api/obtenerCtasAsociadas", contexto -> MBTransferenciaPull.getListadoCuentas(contexto));
        post("/mb/api/ingresar-dinero", contexto -> MBTransferenciaPull.ingresarDinero(contexto));


        //33.d Modo - SDK
        get("/mb/api/token-pago", contexto -> MBModo.getTokenPago(contexto));


        // 34. PFM
        post("/mb/api/movimientos-pfm", contexto -> MBPfm.consultarMovimientos(contexto));
        post("/mb/api/movimientos-mensuales-pfm", contexto -> MBPfm.consultarMovimientosMensuales(contexto));

        // 35. ISVA Biometria
        MBBiometria apiBiometria = new MBBiometria();
        post("/mb/api/tokenBiometria", contexto -> apiBiometria.accessTokensAutenticador(contexto));
        post("/mb/api/refressTokenBiometria", contexto -> MBBiometria.refreshTokens(contexto));
        post("/mb/api/enrolaAutenticador", contexto -> apiBiometria.enrolaAutenticador(contexto));
        post("/mb/api/consultaUsuarioBiometria", contexto -> MBBiometria.consultarUsuarioIsva(contexto));
        post("/mb/api/verificaAccesos", contexto -> MBBiometria.verificaAccesos(contexto));
        post("/mb/api/accesoBiometria", contexto -> apiBiometria.seteaAccesoBiometria(contexto));
        post("/mb/api/accesoBuhoFacil", contexto -> apiBiometria.seteaAccesoBuhoFacil(contexto));
        post("/mb/api/revocaAutenticador", contexto -> apiBiometria.revocaAutenticador(contexto));
        post("/mb/api/loginBiometria", MBLogin::biometria);
        post("/mb/api/otpBiometria", contexto -> apiBiometria.otpBiometria(contexto));
        post("/mb/api/otpBuhoFacil", contexto -> MBBiometria.otpBuhoFacil(contexto));
        post("/mb/api/login-biometria", MBLogin::biometriaTransmit);

        // 36. Agenda
        get("/mb/api/agenda-tipoturnos", contexto -> MBAgenda.tipoTurnosHabilitados(contexto));
        post("/mb/api/agenda-altaturnos", contexto -> MBAgenda.postAgenda(contexto));
        post("/mb/api/agenda-consultaturnos", contexto -> MBAgenda.getAgenda(contexto));
        get("/mb/api/agenda-consultasucursales", contexto -> MBAgenda.getSucursales(contexto));

        // 37. Configuracion de variables
        get("/mb/api/obtener-configuracion-variable", contexto -> MBUsuario.obtenerConfiguracionVariable(contexto));

        // 38. Echeq
        post("/mb/api/echeq-enable-account", contexto -> MBEcheq.enableAccount(contexto));
        post("/mb/api/echeq-disable-account", contexto -> MBEcheq.disableAccount(contexto));
        post("/mb/api/echeq-get-echeqs", contexto -> MBEcheq.getECheqs(contexto));
        post("/mb/api/echeq-accept", contexto -> MBEcheq.accept(contexto));
        post("/mb/api/echeq-reject", contexto -> MBEcheq.reject(contexto));
        get("/mb/api/echeq-tyc", contexto -> MBEcheq.tyc(contexto));
        post("/mb/api/echeq-deposit", contexto -> MBEcheq.deposit(contexto));
        get("/mb/api/echeq-get-checkbook-types", contexto -> MBEcheq.getCheckbookTypes(contexto));
        post("/mb/api/echeq-post-checkbook", contexto -> MBEcheq.postCheckbook(contexto));
        get("/mb/api/echeq-get-person", contexto -> MBEcheq.getPerson(contexto));
        post("/mb/api/echeq-generate", contexto -> MBEcheq.generate(contexto));
        post("/mb/api/echeq-cancel", contexto -> MBEcheq.cancel(contexto));
        post("/mb/api/echeq-return-request", contexto -> MBEcheq.returnRequest(contexto));
        post("/mb/api/echeq-cancel-return", contexto -> MBEcheq.cancelReturn(contexto));
        post("/mb/api/echeq-reject-return", contexto -> MBEcheq.rejectReturn(contexto));
        post("/mb/api/echeq-accept-return", contexto -> MBEcheq.acceptReturn(contexto));
        post("/mb/api/echeq-endorse", contexto -> MBEcheq.endorse(contexto));

        // 39. Combos
        get("/mb/api/combos/echeqs", contexto -> MBEcheq.combosEcheqs(contexto));

        // 40. Distribuicón de tarjetas
        get("/mb/api/tracking-tarjeta-debito", contexto -> MBTrackeo.agregarTrackeoTarjetaDebito(contexto));

        // 41. Soft Token
        MBSoftToken apiSoftToken = new MBSoftToken();
        post("/mb/api/soft-token-alta", contexto -> apiSoftToken.altaSoftToken(contexto));
        get("/mb/api/soft-token-activo-por-dispositivo", contexto -> apiSoftToken.consultarSoftTokenActivoPorDispositivo(contexto));
        get("/mb/api/soft-token-activo-por-cliente", contexto -> apiSoftToken.consultarSoftTokenActivoPorCliente(contexto));
        get("/mb/api/soft-token-generar-idcliente", contexto -> apiSoftToken.generarIDCliente(contexto));
        get("/mb/api/soft-token-usuario-bloqueado", contexto -> apiSoftToken.consultaUsuarioBloqueado(contexto));
        post("/mb/api/soft-token-validacion", contexto -> apiSoftToken.validarSoftToken(contexto));
        get("/mb/api/soft-token-uso-bloqueado", contexto -> apiSoftToken.validarUsoSoftTokenBloqueado(contexto));
        get("/mb/api/soft-token-forzar-alta", contexto -> apiSoftToken.forzarAltaSoftToken(contexto));
        post("/mb/api/x-soft-token-validacion", contexto -> MBOrquestado.xValidarSoftToken(contexto));

        // 42. Adelanto
        post("/mb/api/adelantoBH-descripcion", contexto -> MBAdelanto.catalogoPreguntaRespuesta(contexto));
        post("/mb/api/adelantoBH-solicitud", contexto -> MBAdelanto.solicitudAdelanto(contexto));
        post("/mb/api/adelantoBH-finalizar-solicitud", contexto -> MBAdelanto.finalizarSolicitud(contexto));

        // 43. Orden Extraccion
        post("/mb/api/listar-odes", contexto -> MBOrdenExtraccion.consolidada(contexto));
        post("/mb/api/crear-ode", contexto -> MBOrdenExtraccion.crear(contexto));
        post("/mb/api/cancelar-ode", contexto -> MBOrdenExtraccion.eliminar(contexto));
        post("/mb/api/comprobante-extraccion-ode", contexto -> MBOrdenExtraccion.comprobante(contexto));

        // 44. PCT Modo
        get("/mb/api/obtener-comercio", contexto -> MBPCTModo.getComercio(contexto));
        get("/mb/api/obtener-qr-cuenta", contexto -> MBPCTModo.getQrCuenta(contexto));
        post("/mb/api/crear-comercio-cuenta", contexto -> MBPCTModo.crearComercioCuenta(contexto));
        post("/mb/api/crear-qr-cuenta", contexto -> MBPCTModo.crearQrCuenta(contexto));
        // PCT SQL
        get("/mb/api/validar-tyc", contexto -> MBPCTModo.getRegistroTyC(contexto));
        patch("/mb/api/acepta-tyc", contexto -> MBPCTModo.modificarRegistroTyC(contexto));
        // PCT Alta Unificada
        post("/mb/api/comercio-qr-cuenta", contexto -> MBPCTModo.crearComercioAndQR(contexto));

        // 45. Util
        post("/mb/api/contador", contexto -> Util.contador(contexto));
        get("/mb/api/catalogo-relaciones", contexto -> Util.CatalogoRelaciones(contexto));
        get("/mb/api/catalogo-relaciones-v2", contexto -> Util.CatalogoRelacionesV2(contexto));

        // 46. Canal Amarillo
        post("/mb/api/situacion-laboral", contexto -> MBPersona.situacionLaboral(contexto));
        post("/mb/api/documentacion", contexto -> MBDocumentacion.documentacionXSolicitud(contexto));
        post("/mb/api/mejorar-primer-oferta-prestamo", contexto -> MBOriginacion.mejorarPrimerOfertaPrestamo(contexto));
        post("/mb/api/guardar-documentacion", contexto -> MBDocumentacion.guardarDocumentacionV2(contexto));
        post("/mb/api/desistir-solicitud-prestamo", contexto -> MBOmnicanalidad.desistirSolicitudBPM(contexto));
        post("/mb/api/elimina-solicitud-front", contexto -> MBOriginacion.eliminaSolicitudFront(contexto));

        get("/mb/api/gestiones", contexto -> MBGestiones.gestionesV2(contexto));
        get("/mb/api/gestiones-sucursal-virtual", ctx -> MBGestiones.gestionesSucursalVirtual(ctx));
        get("/mb/api/gestiones-inversion", ctx -> MBGestiones.gestionesInversion(ctx));
        get("/mb/api/gestiones-postventa", ctx -> MBGestiones.gestionesPostventa(ctx));
        get("/mb/api/gestiones-aumento-limite", ctx -> MBGestiones.gestionesAumentoLimite(ctx));

        get("/mb/api/solicitar-libre-deuda", ctx -> MBGestiones.solicitarLibreDeuda(ctx));

        // 48. Corresponsalia
        post("/mb/api/tiene-plan", contexto -> MBCorresponsalia.esJubilado(contexto));
        post("/mb/api/mismo-dni", contexto -> MBCorresponsalia.mismoDNI(contexto));
        post("/mb/api/obtener-etapa-corresponsalia", contexto -> MBCorresponsalia.getEtapa(contexto));
        post("/mb/api/avanzar-etapa-corresponsalia", contexto -> MBCorresponsalia.avanzarEtapa(contexto));
        post("/mb/api/guardar-datos-corresponsalia", contexto -> MBCorresponsalia.guardarDatos(contexto));
        post("/mb/api/obtener-id-cliente", contexto -> MBCorresponsalia.obtenerId(contexto));
        post("/mb/api/actualizar-celular", contexto -> MBCorresponsalia.actualizarTelefono(contexto));
        // 49. Promesa de pago
        post("/mb/api/generar-promesa-pago", contexto -> MBMora.generarPromesaPago(contexto));
        post("/mb/api/productos-en-mora", contexto -> MBMora.productosEnMora(contexto));
        post("/mb/api/generar-multi-promesa-pago", contexto -> MBMora.generarMultiPromesaPago(contexto));

        // 50. Pago y Recarga de Servicios
        get("/mb/api/rubros-recargas", contexto -> MBPagoRecargaServicios.obtenerRubrosRecarga(contexto));
        get("/mb/api/empresas-recargas", contexto -> MBPagoRecargaServicios.obtenerEmpresasPorRubroRecarga(contexto));
        post("/mb/api/alta-adhesion-recargas", contexto -> MBPagoRecargaServicios.altaAdhesion(contexto));
        get("/mb/api/adhesiones-recargas", contexto -> MBPagoRecargaServicios.obtenerAdhesionesTodosRubros(contexto));
        get("/mb/api/adhesiones-empresas-recargas", contexto -> MBPagoRecargaServicios.obtenerAdhesionesPorEmpresa(contexto));
        post("/mb/api/elimina-adhesion-recargas", contexto -> MBPagoRecargaServicios.eliminaAdhesion(contexto));
        post("/mb/api/historial-recargas", contexto -> MBPagoRecargaServicios.historialRecargas(contexto));
        post("/mb/api/recarga", contexto -> MBPagoRecargaServicios.recargaServicio(contexto));

        // 51. Registración Dispositivo
        post("/mb/api/registrar-dispositivo", contexto -> MBRegistroDispositivo.registrar(contexto));
        get("/mb/api/tiene-dispositivos-registrados", contexto -> MBRegistroDispositivo.tieneDispositivosRegistrados(contexto));
        get("/mb/api/es-ultimo-registrado", contexto -> MBRegistroDispositivo.esUltimoRegistrado(contexto));
        get("/mb/api/ultimo-registrado", contexto -> MBRegistroDispositivo.ultimoRegistrado(contexto));
        // 52. NFC
        post("/mb/api/obtener-tarjetas-NFC", contexto -> MBNfcThales.registrar(contexto));
        get("/mb/api/token", contexto -> MBNfcThales.obtenerTokenThales(contexto));

        // 53. Saleforces
        post("/mb/api/registrar-evento-salesforce", ctx -> MBSalesforce.registrarEventoSalesforce(ctx));

        // 54. Seguro
        get("/mb/api/productos-consolidados", ctx -> MBSeguro.obtenerProductos(ctx));
        get("/mb/api/ofertas-mascotas", ctx -> MBSeguro.obtenerOfertasMascotas(ctx));
        get("/mb/api/token-salesforce", ctx -> MBSeguro.obtenerToken(ctx));
        post("/mb/api/emision", ctx -> MBSeguro.insertarEmisionOnlineV2(ctx));
        post("/mb/api/emision-movilidad", ctx -> MBSeguro.insertarEmisionOnlineV2Movilidad(ctx));
        post("/mb/api/emision-v2", ctx -> MBSeguro.insertarEmisionOnlineV2BienesMoviles(ctx));
        get("/mb/api/ofertas-movilidad", ctx -> MBSeguro.obtenerOfertasMovilidad(ctx));
        get("/mb/api/ofertas-bienes-moviles", ctx -> MBSeguro.obtenerOfertasBienesMoviles(ctx));

        get("/mb/api/ofertas-hogar", ctx -> MBSeguro.obtenerOfertasHogar(ctx));
        post("/mb/api/emision-hogar", ctx -> MBSeguro.insertarEmisionOnlineV2Hogar(ctx));

        get("/mb/api/ofertas-ap", ctx -> MBSeguro.obtenerOfertasAP(ctx));
        post("/mb/api/emision-ap", ctx -> MBSeguro.insertarEmisionOnlineV2AP(ctx));

        get("/mb/api/ofertas-salud-senior", ctx -> MBSeguro.obtenerOfertasSaludSenior(ctx));
        get("/mb/api/ofertas-ap-senior", ctx -> MBSeguro.obtenerOfertasAPSenior(ctx));

        // Sucursal Virtual - Aceptacion Digital
        post("/mb/api/datos-confirmacion-producto", MBSucursalVirtual::datosConfirmacionProductos);
        post("/mb/api/ver-tyc", ctx -> MBSucursalVirtual.verTerminosYCondiciones(ctx));
        post("/mb/api/aceptar-solicitud", ctx -> MBSucursalVirtual.aceptarSolicitud(ctx));
        post("/mb/api/ver-nueva-propuesta", ctx -> MBSucursalVirtual.verNuevaPropuesta(ctx));
        post("/mb/api/aceptar-nueva-propuesta", ctx -> MBSucursalVirtual.aceptarNuevaPropuesta(ctx));

        // 55. Alta Online
        post("/mb/api/alta-caja-ahorro-tarjeta-debito-online",
                ctx -> MBOmnicanalidad.altaCajaAhorroTarjetaDebitoOnline(ctx));
        get("/mb/api/fuera-horario-dia-procesos-batch", ctx -> Util.isFueraHorarioDiaProcesoBatch(ctx));
        
        // 56. Plan V
        get("/mb/api/info-financiamiento", ctx -> MBPlanV.obtenerInformacionFinanciamiento(ctx));
        post("/mb/api/simulacion-financiamiento", ctx -> MBPlanV.simularFinanciamiento(ctx));
        post("/mb/api/confirmacion-financiamiento", ctx -> MBPlanV.confirmarFinanciamiento(ctx));
        post("/mb/api/consultar-aprobadas", ctx -> MBPlanV.obtenerFinanciamientosAprobados(ctx));

        // 57. Transmit
        get("/mb/api/obtener-genero", MBLogin::obtenerGenero);
        post("/mb/api/migrar-transmit", MBLogin::migrarTransmit);
        post("/mb/api/renovar-transmit", MBLogin::renovacionTransmit);

    }
}