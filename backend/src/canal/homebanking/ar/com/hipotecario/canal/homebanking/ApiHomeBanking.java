package ar.com.hipotecario.canal.homebanking;

import ar.com.hipotecario.backend.Servidor;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.homebanking.api.*;
import ar.com.hipotecario.canal.homebanking.endpoints.EHBLogin;
import ar.com.hipotecario.canal.homebanking.lib.Util;
import ar.com.hipotecario.canal.homebanking.negocio.TipoNotificacion;
import ar.com.hipotecario.mobile.api.MBLogin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ApiHomeBanking extends CanalHomeBanking {

    public static void main(String[] args) throws Exception {
        Servidor.main(args);
    }

    public static void iniciar() {

        // 00. Cache
        new Futuro<>(() -> EHBLogin.iniciarCacheClientes());
        new Futuro<>(() -> EHBLogin.iniciarCacheCobisAfectados());
        cron("* * * * *", new CacheHB.CronMockServicios().load());
        cron("* * * * *", new CacheHB.CronEstadisticas().load());

        // 00. LOGS
        get("/hb/api/log-api-datos", ctx -> HBLogs.logs(ctx));

        // 00. Aplicacion
        get("/hb/health", ctx -> HBAplicacion.estado(ctx));
        get("/hb/version", ctx -> HBAplicacion.version(ctx));
        get("/hb/headers", ctx -> HBAplicacion.headers(ctx));
        get("/hb/cliente", ctx -> HBAplicacion.cliente(ctx));
        post("/hb/api/config", ctx -> HBAplicacion.configuracion(ctx));
        post("/hb/api/aceptar-legal-defensa-consumidor",
                ctx -> HBAplicacion.aceptarLegalDefensaConsumidor(ctx));
        post("/hb/api/consultar-legal-defensa-consumidor",
                ctx -> HBAplicacion.consultarLegalDefensaConsumidor(ctx));
        post("/hb/api/aceptar-legal-cotizaciones-online",
                ctx -> HBAplicacion.aceptarLegalBymaCotizacionesOnline(ctx));
        get("/hb/api/consultar-legal-cotizaciones-online",
                ctx -> HBAplicacion.consultarLegalBymaCotizacionesOnline(ctx));

        // 00. Home Redirect
        get("/", ctx -> HBAplicacion.homeRedirect(ctx));
        get("/hb", ctx -> HBAplicacion.homeRedirect(ctx));
        get("/hb/", ctx -> HBAplicacion.homeRedirect(ctx));
        get("/hb/index.html", ctx -> HBAplicacion.homeRedirect(ctx));

        // 01. Login
        post("/hb/api/login", ctx -> HBLogin.login(ctx));
        post("/hb/api/pseudo-login", ctx -> HBLogin.pseudoLogin(ctx));
        post("/hb/api/logout", ctx -> HBLogin.logout(ctx));
        post("/hb/api/preguntas-riesgonet", ctx -> HBLogin.preguntasRiesgoNet(ctx));
        post("/hb/api/responder-preguntas-riesgonet", ctx -> HBLogin.responderPreguntasRiesgoNet(ctx));
        post("/hb/api/usuario-sugerido", ctx -> HBLogin.usuarioSugerido(ctx));
        post("/hb/api/validar-usuario", ctx -> HBLogin.validarUsuario(ctx));
        post("/hb/api/validar-clave", ctx -> HBLogin.validarClave(ctx));
        get("/hb/api/obtener-genero", HBLogin::obtenerGenero);

        // 02. Seguridad
        post("/hb/api/canales-otp", ctx -> HBSeguridad.canalesOTP(ctx));
        post("/hb/api/pedir-otp", ctx -> HBSeguridad.pedirOTP(ctx));
        post("/hb/api/validar-otp", ctx -> HBSeguridad.validarOTP(ctx));
        get("/hb/api/pedir-captcha", ctx -> HBSeguridad.pedirCaptcha(ctx));
        post("/hb/api/validar-captcha", ctx -> HBSeguridad.validarCaptcha(ctx));
        post("/hb/api/validar-clave-link", ctx -> HBSeguridad.validarClaveLink(ctx));
        get("/hb/api/validar-multiple-td-activas", ctx -> HBSeguridad.cantidadTarjetasCreditoActivas(ctx));
        post("/hb/api/historial-actividades", ctx -> HBSeguridad.historialActividades(ctx));
        post("/hb/api/usuario-gire-post", ctx -> HBSeguridad.usuarioGirePost(ctx));
        get("/hb/api/modal-clave-bf-vencimiento", ctx -> HBSeguridad.fechaVencimientoClaveBuhoFacil(ctx));

        // 03. Usuario
        post("/hb/api/nuevo-usuario", ctx -> HBUsuario.nuevoUsuario(ctx));
        post("/hb/api/cambiar-usuario", ctx -> HBUsuario.cambiarUsuario(ctx));
        post("/hb/api/cambiar-clave", ctx -> HBUsuario.cambiarClave(ctx));
        post("/hb/api/cambiar-usuario-logueado", ctx -> HBUsuario.cambiarUsuarioLogueado(ctx));
        post("/hb/api/cambiar-clave-logueado", ctx -> HBUsuario.cambiarClaveLogueado(ctx));

        post("/hb/api/persona", ctx -> HBPersona.cliente(ctx));
        post("/hb/api/persona-tercero", ctx -> HBPersona.personaTercero(ctx));
        post("/hb/api/actualizar-datos-personales", ctx -> HBPersona.actualizarDatosPersonales(ctx));
        post("/hb/api/paises", ctx -> HBPersona.paises(ctx));
        post("/hb/api/provincias", ctx -> HBPersona.provincias(ctx));
        post("/hb/api/localidades", ctx -> HBPersona.localidades(ctx));
        post("/hb/api/telefono-personal", ctx -> HBPersona.telefonoPersonal(ctx)); // el-front-no-lo-usa-en-prod
        post("/hb/api/validadores-segundo-factor", ctx -> HBPersona.validadoresSegundoFactor(ctx, null));
        post("/hb/api/permitir-segundo-factor-otp", ctx -> HBPersona.permitirSegundoFactorOtp(ctx));
        post("/hb/api/marcas-cliente", ctx -> HBPersona.marcasCliente(ctx));
        post("/hb/api/buscar-persona", ctx -> HBPersona.buscarPersona(ctx)); // el-front-no-lo-usa-en-prod
        post("/hb/api/domicilio-tarjeta-credito", ctx -> HBPersona.domicilioTarjetaCredito(ctx)); // el-front-no-lo-usa-en-prod
        get("/hb/api/datavalid-vigente", ctx -> HBPersona.dataValid(ctx));
        get("/hb/api/datavalid-vigente-otp", ctx -> HBPersona.dataValidOtp(ctx));
        get("/hb/api/es-cliente-exterior-o-tdv", ctx -> HBPersona.esClienteExteriorOrTdv(ctx));
        get("/hb/api/tiene-bloqueo-operaciones", contexto -> HBPersona.tieneBloqueoOperaciones(contexto));
        get("/hb/api/modal-tcv", contexto -> HBPersona.mostrarModalTcv(contexto));
        post("/hb/api/datos-adicionales-tcv", contexto -> HBPersona.guardarAdicionalesTcv(contexto));
        get("/hb/api/alerta-sos", ctx -> HBPersona.alertaSos(ctx));

        // 05. Archivo
        post("/hb/api/terminos-condiciones", ctx -> HBArchivo.terminosCondiciones(ctx));
        get("/hb/api/documento-digitalizado", ctx -> HBArchivo.archivoDigitalizado(ctx));
        get("/hb/api/comprobante", ctx -> HBArchivo.comprobante(ctx));
        post("/hb/api/libre-deuda", ctx -> HBArchivo.simularLibreDeuda(ctx));
        get("/hb/api/libre-deuda", ctx -> HBArchivo.libreDeuda(ctx));
        post("/hb/api/solicitar-documentacion-digital-tyc",
                ctx -> HBProducto.solicitarDocumentacionDigitalTyc(ctx));
        post("/hb/api/descarga-adjunto", ctx -> HBArchivo.descargaAdjunto(ctx));

        // 06. Producto
        post("/hb/api/productos", ctx -> HBProducto.productos(ctx));
        post("/hb/api/limpiar-cache", ctx -> HBProducto.limpiarCache(ctx));
        post("/hb/api/cuentas", ctx -> HBProducto.cuentas(ctx));
        post("/hb/api/cuentas-comitentes", ctx -> HBProducto.cuentasComitentes(ctx));
        post("/hb/api/cuentas-cuotapartistas", ctx -> HBProducto.cuentasCuotapartistas(ctx));
        post("/hb/api/cajas-seguridad", ctx -> HBProducto.cajasSeguridad(ctx));

        // 07. Cuenta
        post("/hb/api/consolidada-cuentas", ctx -> HBCuenta.consolidadaCuentas(ctx));
        post("/hb/api/modificar-alias-cuenta", ctx -> HBCuenta.modificarAliasCuenta(ctx));
        post("/hb/api/modificar-comentario-cuenta", ctx -> HBCuenta.modificarComentarioCuenta(ctx));
        post("/hb/api/movimientos-cuenta", ctx -> HBCuenta.movimientosCuenta(ctx));
        post("/hb/api/consolidado-impuestos-cuentas", ctx -> HBCuenta.consolidadoImpuestos(ctx));
        post("/hb/api/compartir-cbu", ctx -> HBCuenta.compartirCBU(ctx));
        post("/hb/api/valores-suspenso", ctx -> HBCuenta.valoresSuspenso(ctx));
        post("/hb/api/caja_ahorro_bloqueos", ctx -> HBCuenta.cajaAhorroBloqueos(ctx));
        post("/hb/api/cuenta-bloqueos", ctx -> HBCuenta.bloqueosCuenta(ctx));
        post("/hb/api/cuentas_comitentes_asociadas", ctx -> HBCuenta.cuentasComitentesAsociadas(ctx));
        post("/hb/api/baja-caja-ahorro", ctx -> HBProducto.bajaCajaAhorro(ctx));
        post("/hb/api/consolidada-resumen-cuenta", ctx -> HBCuenta.consolidadaResumenCuenta(ctx));
        post("/hb/api/periodos-resumen-cuenta", ctx -> HBCuenta.periodosResumenCuenta(ctx));
        post("/hb/api/resumen-cuenta", ctx -> HBCuenta.resumenCuenta(ctx));
        post("/hb/api/resumen-plazo-fijo", ctx -> HBCuenta.resumenPlazoFijo(ctx));
        post("/hb/api/adhesion-resumen-digital", ctx -> HBCuenta.actualizarMarcaResumen(ctx));
        post("/hb/api/consolidada-cambio-cuenta-principal", ctx -> HBCuenta.consolidadaCambioCuentaPrincipal(ctx)); // el-front-no-lo-usa-en-prod
        post("/hb/api/cambiar-cuenta-principal", ctx -> HBCuenta.cambiarCuentaPrincipal(ctx)); // el-front-no-lo-usa-en-prod
        post("/hb/api/renta-financiera", ctx -> HBCuenta.rentaFinanciera(ctx));
        post("/hb/api/consulta-cuenta", ctx -> HBCuenta.consultaCuenta(ctx)); // el-front-no-lo-usa-en-prod
        post("/hb/api/historico-movimientos", ctx -> HBCuenta.historicoMovimientosCuenta(ctx));
        post("/hb/api/estados-cuenta", ctx -> HBCuenta.estadosCuentaAsync(ctx));
        post("/hb/api/cuenta-tarjeta-debito", ctx -> HBCuenta.getCuentaTD(ctx));
        get("/hb/api/alta-cuenta-especial", ctx -> HBCuenta.altaCuentaEspecial(ctx));
        get("/hb/api/tyc-cuenta-especial", ctx -> HBCuenta.tycCuentaEspecial(ctx));
        post("/hb/api/movimientos-cuenta-paginado", ctx -> HBCuenta.movimientosCuentaV2(ctx));
        post("/hb/api/oferta-caja-ahorro-dolar", HBCuenta::ofertaCajaAhorroDolar);
        post("/hb/api/certificado-cuenta", ctx -> HBCuenta.certificadoCuenta(ctx));

        // 08. Tarjetas
        post("/hb/api/consolidada-tarjetas", ctx -> HBTarjetas.consolidadaTarjetas(ctx));
        post("/hb/api/consolidada-tarjetas-credito", ctx -> HBTarjetas.consolidadaTarjetasCredito(ctx));
        post("/hb/api/consolidada-tarjetas-debito", ctx -> HBTarjetas.consolidadaTarjetasDebito(ctx));
        post("/hb/api/tc-imprimir", ctx -> HBTarjetas.solicitarImpresion(ctx));
        post("/hb/api/resumen-cuenta-tarjeta", ctx -> HBTarjetas.resumenCuenta(ctx));
        post("/hb/api/autorizaciones-tarjeta", ctx -> HBTarjetas.autorizaciones(ctx));
        post("/hb/api/cuotas-pendientes-tarjeta", ctx -> HBTarjetas.cuotasPendientes(ctx));
        post("/hb/api/movimientos-tarjeta", ctx -> HBTarjetas.movimientosTC(ctx));
        post("/hb/api/categoria-movimiento-tarjeta", ctx -> HBTarjetas.categoriaMovimientoTarjeta(ctx)); // el-front-no-lo-usa-en-prod
        post("/hb/api/detalle-movimientos-comercio", ctx -> HBTarjetas.detalleMovimientoComercio(ctx));
        post("/hb/api/detalle-actual-tarjeta", ctx -> HBTarjetas.infoActualizada(ctx));
        post("/hb/api/datos-para-pagar", ctx -> HBTarjetas.datosParaPagar(ctx));
        post("/hb/api/pagar-tarjeta", ctx -> HBTarjetas.pagarTarjeta(ctx));
        post("/hb/api/programar-pago-tarjeta", ctx -> HBTarjetas.programarPagoTarjeta(ctx)); // el-front-no-lo-usa-en-prod
        post("/hb/api/pagos-programados-tarjeta-credito", ctx -> HBTarjetas.consultarPagosProgramadosTarjetaCredito(ctx)); // el-front-no-lo-usa-en-prod
        get("/hb/api/ultima-liquidacion", ctx -> HBTarjetas.ultimaLiquidacion(ctx));
        get("/hb/api/estado-ultima-liquidacion", ctx -> HBTarjetas.errorGenerandoResumen(ctx));

        post("/hb/api/blanquear-pil", ctx -> HBTarjetas.blanquearPil(ctx));
        post("/hb/api/blanquear-pin", ctx -> HBTarjetas.blanquearPin(ctx));
        post("/hb/api/habilitar-tarjeta-debito", ctx -> HBTarjetas.habilitarTarjetaDebito(ctx));
        post("/hb/api/limites-tarjeta-debito", ctx -> HBTarjetas.limitesTarjetaDebito(ctx));
        post("/hb/api/modificar-limite-tarjeta-debito", ctx -> HBTarjetas.modificarLimiteTarjetaDebito(ctx));
        post("/hb/api/consolidada-forma-pago-tarjeta-credito", ctx -> HBTarjetas.consolidadaFormaPagoTarjetaCredito(ctx));
        post("/hb/api/cambiar-forma-pago-tarjeta-credito", ctx -> HBTarjetas.cambiarFormaPagoTarjetaCredito(ctx));
        post("/hb/api/adicionales-propias", ctx -> HBTarjetas.consultaAdicionalesPropias(ctx));
        post("/hb/api/baja-tarjeta-credito", ctx -> HBProducto.bajaTarjetaCredito(ctx));
        post("/hb/api/tarjetas-credito-propias", ctx -> HBTarjetas.tarjetasCreditoPropias(ctx));
        post("/hb/api/oferta-solicitud-tarjeta-credito-adicional", ctx -> HBTarjetas.ofertaSolicitudTarjetaCreditoAdicional(ctx));
        post("/hb/api/crear-solicitud-tarjeta-credito-adicional", ctx -> HBTarjetas.crearSolicitudTarjetaCreditoAdicional(ctx));
        post("/hb/api/cambiar-limite-tarjeta-credito-adicional", ctx -> HBTarjetas.cambioLimiteTarjetaCreditoAdicional(ctx));
        post("/hb/api/horario-pago-tarjeta", ctx -> HBTarjetas.horarioPagoTarjeta(ctx)); // el-front-no-lo-usa-en-prod
        get("/hb/api/tarjetas-debito-habilitadas", ctx -> HBTarjetas.tarjetaDebitoHabilitadaRedLink(ctx)); // el-front-no-lo-usa-en-prod
        post("/hb/api/baja-tarjeta-hml", ctx -> HBProducto.bajaTarjetaHML(ctx));
        get("/hb/api/tooltip-tarjeta", ctx -> HBTarjetas.buscarTooltipConfiguracionTarjeta(ctx));
        post("/hb/api/tooltip-tarjeta", ctx -> HBTarjetas.agregarTooltipConfiguracionTarjeta(ctx));
        get("/hb/api/obtener-tarjeta-estado", ctx -> HBDelivery.obtenerTarjetaEstado(ctx)); // el-front-no-lo-usa-en-prod
        get("/hb/api/obtener-direccion-reenvio", ctx -> HBDelivery.obtenerDireccionReenvio(ctx)); // el-front-no-lo-usa-en-prod
        get("/hb/api/operacion-tarjeta-inhibida", ctx -> HBDelivery.operacionTarjetaInhibida(ctx)); // el-front-no-lo-usa-en-prod
        post("/hb/api/convertir-tarjeta-debito-virtual-a-fisica", ctx -> HBTarjetas.convertirTarjetaDebitoVirtualToFisica(ctx));
        post("/hb/api/crear-caso-reimpresionTC", ctx -> HBDelivery.crearCasoReimpresionTC(ctx)); // el-front-no-lo-usa-en-prod
        post("/hb/api/crear-caso-rescate-tc-sucursal", ctx -> HBDelivery.crearCasoRescateTcEnSucursal(ctx)); // el-front-no-lo-usa-en-prod
        post("/hb/api/reposicion-tarjeta-debito", ctx -> HBTarjetas.reposicionTD(ctx));
        post("/hb/api/estados-deuda", ctx -> HBTarjetas.estadoDeuda(ctx)); // el-front-no-lo-usa-en-prod
        post("/hb/api/obtener-fechas-tc", ctx -> HBTarjetas.obtenerFechasTC(ctx));
        post("/hb/api/detener-debito-automatico", ctx -> HBTarjetas.stopDebit(ctx));
        post("/hb/api/reposicion-tarjeta-debito-en-curso", ctx -> HBTarjetas.reposicionTDEnCurso(ctx));
        post("/hb/api/carteras-tc", ctx -> HBTarjetas.getCarterasTC(ctx));
        post("/hb/api/puede-pedir-cambio-cartera", ctx -> HBTarjetas.puedePedirCambioCartera(ctx));
        post("/hb/api/crear-caso-cambio-cartera", ctx -> HBTarjetas.crearCasoCambioCartera(ctx));
        post("/hb/api/mostrar-opcion-cambio-cartera", ctx -> HBTarjetas.mostarOpcionCambioCartera(ctx));
        get("/hb/api/puede-solicitar-tc-adicional", ctx -> HBTarjetas.puedeCrearSolicitudTarjetaCreditoAdicional(ctx));
        post("/hb/api/tc-adicional-en-curso", ctx -> HBTarjetas.AdicionalEnCurso(ctx));
        post("/hb/api/generar-reclamo-promo-td", ctx -> HBTarjetas.promoNoImpactadaTD(ctx));
        post("/hb/api/generar-reclamo-consumo-td", ctx -> HBTarjetas.crearCasoDesconomientoConsumo(ctx));
        post("/hb/api/pausar-tarjeta-debito", ctx -> HBTarjetas.pausarTarjetaDebito(ctx));
        post("/hb/api/pausar-tarjeta-debito-link", ctx -> HBTarjetas.pausarTarjetaDebitoLinkContingencia(ctx));
        post("/hb/api/pausar-tarjeta-debito-core", ctx -> HBTarjetas.pausarTarjetaDebitoCoreContingencia(ctx));
        post("/hb/api/estado-baja-tarjeta-credito", ctx -> HBTarjetas.estadoBajaTarjeta(ctx));
        post("/hb/api/retension-baja-tarjeta-credito", ctx -> HBTarjetas.retensionBajaTarjeta(ctx));
        post("/hb/api/baja-directa-tarjeta-credito", ctx -> HBTarjetas.bajaDirectaTarjetaCredito(ctx));
        post("/hb/api/puede-detener-debito-automatico", ctx -> HBTarjetas.puedeStopDebit(ctx));
        post("/hb/api/pausar-tarjeta-credito", ctx -> HBTarjetas.pausarTarjetaCredito(ctx));
        get("/hb/api/estado-pausada-tarjeta-credito", ctx -> HBTarjetas.estadoTarjetaCredito(ctx));
        post("/hb/api/alta-paquete-tarjeta-credito", ctx -> HBProducto.altaPaqueteTarjetaCredito(ctx));
        post("/hb/api/td-aviso-viaje", ctx -> HBTarjetas.avisarViajeExterior(ctx));

        // 09. PlazoFijo
        post("/hb/api/consolidada-plazos-fijos", ctx -> HBPlazoFijo.consolidadaPlazosFijo(ctx));
        post("/hb/api/tipos-plazos-fijos", ctx -> HBPlazoFijo.tiposPlazosFijos(ctx));
        post("/hb/api/simular-plazo-fijo", ctx -> HBPlazoFijo.simularPlazoFijo(ctx));
        post("/hb/api/alta-plazo-fijo", ctx -> HBPlazoFijo.altaPlazoFijo(ctx));
        post("/hb/api/baja-renovacion-plazo-fijo", ctx -> HBPlazoFijo.bajaRenovacionPlazoFijo(ctx));
        post("/hb/api/precancelar-procrear-joven", ctx -> HBPlazoFijo.precancelarProcrearJoven(ctx)); // el-front-no-lo-usa-en-prod
        post("/hb/api/tasa-preferencial", ctx -> HBPlazoFijo.tasaPreferencial(ctx));
        post("/hb/api/detalle-plazo-fijo", ctx -> HBPlazoFijo.detallePlazoFijo(ctx)); // el-front-no-lo-usa-en-prod
        get("/hb/api/comprobante-plazo-fijo", ctx -> HBPlazoFijo.comprobantePlazosFijo(ctx));
        post("/hb/api/precancelar-uva-cer", ctx -> HBPlazoFijo.precancelarPlazoFijoUvaCer(ctx));
        post("/hb/api/movimiento-plazo-fijo", ctx -> HBPlazoFijo.movimientoPlazoFijo(ctx));
        post("/hb/api/simular-plazos-fijo-por-tipo", ctx -> HBPlazoFijo.simularPlazosFijoPorTipo(ctx));

        // 10. PlazoFijoLogro
        post("/hb/api/consolidada-plazos-fijos-logros",
                ctx -> HBPlazoFijoLogro.consolidadaPlazosFijoLogros(ctx));
        post("/hb/api/tyc-plazos-fijos-logros",
                ctx -> HBPlazoFijoLogro.terminosCondicionesPlazosFijoLogros(ctx));
        post("/hb/api/monedas-plazo-fijo-logro", ctx -> HBPlazoFijoLogro.monedaValidasPlazosFijos(ctx));
        post("/hb/api/parametria-plazo-fijo-logro", ctx -> HBPlazoFijoLogro.parametriaPlazoFijoLogros(ctx));
        post("/hb/api/modificar-plazo-fijo-logro", ctx -> HBPlazoFijoLogro.modificarPlazoFijoLogros(ctx));
        post("/hb/api/simular-plazo-fijo-logro", ctx -> HBPlazoFijoLogro.simularPlazoFijoLogro(ctx));
        post("/hb/api/alta-plazo-fijo-logro", ctx -> HBPlazoFijoLogro.altaPlazoFijoLogro(ctx));
        post("/hb/api/baja-plazo-fijo-logro", ctx -> HBPlazoFijoLogro.bajaPlazoFijoLogro(ctx));
        post("/hb/api/forzar-plazo-fijo-logro", ctx -> HBPlazoFijoLogro.forzarPlazoFijoLogro(ctx));

        // 11. Divisa
        post("/hb/api/cotizaciones", ctx -> HBDivisa.cotizaciones(ctx));
        post("/hb/api/simular-compra-venta-dolares", ctx -> HBDivisa.simularCompraVentaDolares(ctx));
        post("/hb/api/compra-venta-dolares", ctx -> HBDivisa.compraVentaDolares(ctx));
        post("/hb/api/compra-venta-en-horario", ctx -> HBTransferencia.compraVentaDolarFueraDeHorario(ctx));
        post("/hb/api/comprobante_auditor_compra_venta_dolar",
                ctx -> HBComprobantes.comprobanteAuditoriaCompraVentaDolar(ctx)); // el-front-no-lo-usa-en-prod
        post("/hb/api/refreshCotizacion", ctx -> HBDivisa.refreshCotizacion(ctx));

        // 12. Transferencia
        get("/hb/api/beneficiarios-transferencias-nuevo", ctx -> HBTransferencia.beneficiariosV2(ctx));
        post("/hb/api/beneficiarios-transferencias-detalle", ctx -> HBTransferencia.beneficiariosDetalle(ctx));
        post("/hb/api/beneficiarios-transferencias", ctx -> HBTransferencia.beneficiarios(ctx));
        post("/hb/api/modificar-beneficiario-transferencia", ctx -> HBTransferencia.modificarBeneficiario(ctx));
        post("/hb/api/modificar-beneficiario-apodo", ctx -> HBTransferencia.modificarBeneficiarioApodo(ctx));
        post("/hb/api/eliminar-beneficiario-transferencia", ctx -> HBTransferencia.eliminarBeneficiario(ctx));
        post("/hb/api/limites-transferencia", ctx -> HBTransferencia.limites(ctx));
        post("/hb/api/aumentar-limite-transferencia", ctx -> HBTransferencia.aumentarLimite(ctx));
        post("/hb/api/conceptos-transferencia", ctx -> HBTransferencia.conceptos(ctx));
        post("/hb/api/cuenta-tercero", ctx -> HBTransferencia.cuentaTercero(ctx));
        post("/hb/api/cuenta-tercero-coelsa", ctx -> HBTransferencia.cuentaTerceroCoelsa(ctx));
        post("/hb/api/transferir", ctx -> HBTransferenciaV2.transferir(ctx));
        get("/hb/api/enabled-transfer", ctx -> HBTransferencia.isRiskForChangeInformation(ctx));
        get("/hb/api/puede-transferir-especial-mismo-dia",
                ctx -> HBTransferencia.puedeTransferirEspecialMismoDia(ctx));

        // 13. PagoServicio
        post("/hb/api/consolidada-pagos", ctx -> HBPago.consolidadaPagos(ctx));
        post("/hb/api/rubros-link", ctx -> HBPago.rubrosLink(ctx));
        post("/hb/api/entes-link", ctx -> HBPago.entesLink(ctx));
        post("/hb/api/agendar-pago-servicio", ctx -> HBPago.agendarPagoServicio(ctx));
        post("/hb/api/modificar-pago-servicio", ctx -> HBPago.modificarPagoServicio(ctx));
        post("/hb/api/desagendar-pago-servicio", ctx -> HBPago.desagendarPagoServicio(ctx));
        post("/hb/api/pagar-servicio", ctx -> HBPago.pagarServicio(ctx));
        post("/hb/api/entes-agendados-link", ctx -> HBPago.entesAgendadosLink(ctx));
        post("/hb/api/comprobantes-por-ente-pagos-link", ctx -> HBPago.comprobantesPorEntePagosLink(ctx)); // el-front-no-lo-usa-en-prod
        post("/hb/api/consulta-debitos-automaticos", ctx -> HBPago.consultaDebitosAutomaticos(ctx));
        post("/hb/api/agregar-debito-automatico", ctx -> HBPago.agregarDebitoAutomatico(ctx));
        post("/hb/api/eliminar-debito-automatico", ctx -> HBPago.eliminarDebitoAutomatico(ctx));
        post("/hb/api/rubros-debito-automatico", ctx -> HBPago.consultaRubrosDebitosAutomaticos(ctx));
        post("/hb/api/empresas-debito-automatico", ctx -> HBPago.consultaEmpresasDebitosAutomaticos(ctx));
        post("/hb/api/servicios-debito-automatico", ctx -> HBPago.consultaServiciosDebitosAutomaticos(ctx));
        post("/hb/api/tyc-adhesion-debito-automatico", ctx -> HBPago.tycAdhesionDebitosAutomaticos(ctx));

        // 14. PagoVep
        post("/hb/api/consolidada-veps", ctx -> HBPago.comprobantesPendientesVepPorCuit(ctx));
        post("/hb/api/pagar-vep", ctx -> HBPago.pagarVep(ctx));
        post("/hb/api/eliminar-vep", ctx -> HBPago.eliminarVep(ctx));

        // 15. Inversiones
        post("/hb/api/consolidada-inversiones", ctx -> HBInversion.consolidadaInversiones(ctx));
        post("/hb/api/licitaciones", ctx -> HBInversion.licitaciones(ctx));
        post("/hb/api/licitaciones-suscripcion-alta", ctx -> HBInversion.altaSuscripcionLicitacion(ctx)); // el-front-no-lo-usa-en-prod
        post("/hb/api/completar-test-perfil-inversor", ctx -> HBInversion.completarTestPerfilInversor(ctx));
        post("/hb/api/perfil-inversor", ctx -> HBInversion.perfilInversor(ctx));
        post("/hb/api/modificar-perfil-inversor-propio-riesgo", ctx -> HBInversion.perfilInversorPropioRiesgo(ctx));
        post("/hb/api/alta-cuenta-inversor", ctx -> HBInversion.altaCuentaInversor(ctx));
        post("/hb/api/horario-cuenta-inversor", ctx -> HBInversion.horarioCuentaInversor(ctx));
        post("/hb/api/cuenta-inversor", ctx -> HBInversion.cuentaInversor(ctx));
        post("/hb/api/cuotapartista", ctx -> HBInversion.cuotapartista(ctx));
        post("/hb/api/solicitudes-fci", ctx -> HBInversion.solicitudesFci(ctx));
        post("/hb/api/posicionCuotapartista", ctx -> HBInversion.posicionCuotapartista(ctx));
        post("/hb/api/fondos", ctx -> HBInversion.fondos(ctx));
        post("/hb/api/fondosAceptados", ctx -> HBInversion.fondosAceptados(ctx));
        post("/hb/api/rescate", ctx -> HBInversion.rescate(ctx));
        post("/hb/api/suscripcion", ctx -> HBInversion.suscripcion(ctx));
        post("/hb/api/fondosAgendados", ctx -> HBInversion.fondosAgendados(ctx));
        post("/hb/api/procesoCron", ctx -> HBInversion.procesoCron(ctx));
        post("/hb/api/poseeCuotapartista", ctx -> HBInversion.poseeCuotapartista(ctx));
        post("/hb/api/integrantes-perfil-inversor", ctx -> HBInversion.integrantesCuentaPerfilInversor(ctx));
        post("/hb/api/updateAgendadoFCI", ctx -> HBInversion.updateAgendadoFCI(ctx)); // TODO: eliminar esta ruta
        post("/hb/api/actualizaOrdenesFCIOnDemand", ctx -> HBInversion.actualizaOrdenesFCIOnDemand(ctx));
        post("/hb/api/onboarding-fci", ctx -> HBInversion.onboardingFci(ctx));

        // 16. Notificaciones
        post("/hb/api/notificaciones", ctx -> HBNotificaciones.notificaciones(ctx));
        post("/hb/api/responder-notificacion", ctx -> HBNotificaciones.notificacionesRespuesta(ctx));
        post("/hb/api/configuracion-alertas", ctx -> HBNotificaciones.configuracionAlertas(ctx));
        post("/hb/api/modificar-configuracion-alertas",
                ctx -> HBNotificaciones.modificarConfiguracionAlertas(ctx));
        get("/hb/api/notificaciones-descarga", ctx -> HBNotificaciones.notificationesDescargaAsync(ctx));
        post("/hb/api/actualizacion-notificaciones-descarga",
                ctx -> HBNotificaciones.actualizarNotificacion(ctx));
        post("/hb/api/consultar-check-notificacion",
                ctx -> HBNotificaciones.consultarNotificacionPorParametro(ctx));
        post("/hb/api/registrar-check-notificacion",
                ctx -> HBNotificaciones.guardarNotificacionPorParametro(ctx));

        // 17. Sitio Externo
        post("/hb/api/token-afip", ctx -> HBPago.tokenAfip(ctx));
        post("/hb/api/token-vfhome", ctx -> HBSitioExterno.vfhome(ctx));
        post("/hb/api/token-visahome", ctx -> HBSitioExterno.visahome(ctx));
        post("/hb/api/token-visahome-test", ctx -> HBSitioExterno.visahometest(ctx));
        post("/hb/api/permitir-salto-visahome", ctx -> HBSitioExterno.permitirSaltoVisaHome(ctx));

        // 18. Acumar
        get("/hb/api/terminos-condiciones-originacion", ctx -> HBAcumar.terminosCondiciones(ctx));
        get("/hb/api/terminos-condiciones-originacion-string", ctx -> HBAcumar.terminosCondicionesString(ctx)); // el-front-no-lo-usa-en-prod

        // 19. Paquetes
        post("/hb/api/consolida-paquetes", ctx -> HBPaquetes.consolidadaPaquetes(ctx));
        post("/hb/api/historico-bonificaciones", ctx -> HBPaquetes.historicoBonificaciones(ctx));
        post("/hb/api/historico-bonificaciones-detalle", ctx -> HBPaquetes.historicoBonificacionesDetalle(ctx));
        post("/hb/api/baja-paquete", ctx -> HBProducto.bajaPaquete(ctx)); // el-front-no-lo-usa-en-prod
        post("/hb/api/estado-baja-paquete", ctx -> HBProducto.estadoBajaPaquete(ctx));

        // 20. Titulos y Valores
        post("/hb/api/tenencia-titulos-valores", ctx -> HBTitulosValores.tenenciaPosicionNegociable(ctx));
        post("/hb/api/tenencia-titulos-valores-v2", ctx -> HBTitulosValores.tenenciaPosicionNegociableV2(ctx));
        post("/hb/api/tenencia-venta-titulos-valores",
                ctx -> HBTitulosValores.tenenciaVentaPosicionNegociable(ctx));
        post("/hb/api/movimientos-titulos-valores", ctx -> HBTitulosValores.movimientos(ctx));
        post("/hb/api/seguimiento-operaciones-titulos-valores",
                ctx -> HBTitulosValores.seguimientoOperaciones(ctx));
        post("/hb/api/seguimiento-licitaciones-titulos-valores",
                ctx -> HBTitulosValores.seguimientoLicitaciones(ctx));
        post("/hb/api/tipos-activo-titulos-valores", ctx -> HBTitulosValores.tiposActivo(ctx));
        post("/hb/api/especies-titulos-valores", ctx -> HBTitulosValores.especies(ctx));
        post("/hb/api/detalle-especie-titulos-valores",
                ctx -> HBTitulosValores.detalleEspecieProductosOperables(ctx));
        post("/hb/api/plazos-titulos-valores", ctx -> HBTitulosValores.plazosValidos(ctx));
        post("/hb/api/cuentas-asociadas-comitente", ctx -> HBTitulosValores.cuentasAsociadasComitente(ctx));
        post("/hb/api/simular-compra-titulo-valor", ctx -> HBTitulosValores.simularCompra(ctx));
        post("/hb/api/calcular-cantidad-nominal-compra-mep",
                ctx -> HBTitulosValores.calcularCantidadNominalCompraMep(ctx));
        post("/hb/api/comprar-titulo-valor", ctx -> HBTitulosValores.comprar(ctx));
        post("/hb/api/calcular-cantidad-nominal-compra",
                ctx -> HBTitulosValores.calcularCantidadNominalCompra(ctx));
        post("/hb/api/simular-venta-titulo-valor", ctx -> HBTitulosValores.simularVenta(ctx));
        post("/hb/api/vender-titulo-valor", ctx -> HBTitulosValores.vender(ctx));
        get("/hb/api/comprobante-cuenta-comitente", ctx -> HBTitulosValores.comprobanteCuentaComitente(ctx));
        get("/hb/api/comprobante-licitacion", ctx -> HBTitulosValores.comprobanteLicitaciones(ctx)); // el-front-no-lo-usa-en-prod
        get("/hb/api/comprobante-titulos-valores",
                ctx -> HBTitulosValores.comprobanteSeguimientoOperaciones(ctx));
        post("/hb/api/paneles-especies", ctx -> HBTitulosValores.panelesEspecies(ctx));
        post("/hb/api/inversiones-plazos", ctx -> HBTitulosValores.inversionesPlazos(ctx));
        post("/hb/api/indices-bursatiles-novedades-delay", ctx -> HBTitulosValores.indicesBursatilesDelay(ctx)); // el-front-no-lo-usa-en-prod
        post("/hb/api/indices-sectoriales-novedades-delay",
                ctx -> HBTitulosValores.indicesSectorialesDelay(ctx)); // el-front-no-lo-usa-en-prod
        post("/hb/api/paneles-cotizaciones-novedades-delay",
                ctx -> HBTitulosValores.panelesCotizacionesDelay(ctx)); // el-front-no-lo-usa-en-prod
        post("/hb/api/paneles-cauciones-novedades-delay", ctx -> HBTitulosValores.caucionesDelay(ctx)); // el-front-no-lo-usa-en-prod
        post("/hb/api/indices-bursatiles-novedades", ctx -> HBTitulosValores.indicesBursatilesRealTime(ctx)); // el-front-no-lo-usa-en-prod
        post("/hb/api/indices-sectoriales-novedades", ctx -> HBTitulosValores.indicesSectorialesRealTime(ctx)); // el-front-no-lo-usa-en-prod
        post("/hb/api/indices-novedades", ctx -> HBTitulosValores.indicesRealTime(ctx));
        post("/hb/api/paneles-cotizaciones-novedades",
                ctx -> HBTitulosValores.panelesCotizacionesRealTime(ctx));
        post("/hb/api/paneles-cauciones-novedades", ctx -> HBTitulosValores.caucionesRealTime(ctx)); // el-front-no-lo-usa-en-prod
        post("/hb/api/paneles-cotizaciones-especies-novedades",
                ctx -> HBTitulosValores.panelesCotizacionesRealTimeEspecie(ctx));
        post("/hb/api/profundidad-mercado", ctx -> HBTitulosValores.profundidadMercado(ctx));
        post("/hb/api/posiciones-negociables", ctx -> HBTitulosValores.posicionesNegociables(ctx)); // el-front-no-lo-usa-en-prod
        get("/hb/api/cotizacion-dolar-mep", ctx -> HBTitulosValores.cotizacionDolaMepV2(ctx));
        get("/hb/api/cotizacion-plazo-liquidacion",
                ctx -> HBTitulosValores.obtenerCotizacionPorPlazoLiquidacion(ctx)); // el-front-no-lo-usa-en-prod
        get("/hb/api/en-horario-dolar-mep", ctx -> HBTitulosValores.enHorarioDolarMep(ctx));

        // 21. Catalogo
        post("/hb/api/nacionalidades", ctx -> HBCatalogo.nacionalidades(ctx));
        post("/hb/api/provincias-por-codigo-postal", ctx -> HBCatalogo.provinciasPorCodigoPostal(ctx));
        post("/hb/api/estados-civiles", ctx -> HBCatalogo.estadosCiviles(ctx));
        post("/hb/api/localidades-por-codigo-postal", ctx -> HBCatalogo.localidadesPorCodigoPostal(ctx));
        post("/hb/api/situaciones-vivienda", ctx -> HBCatalogo.situacionesVivienda(ctx));
        post("/hb/api/niveles-estudio", ctx -> HBCatalogo.nivelesEstudio(ctx));
        post("/hb/api/sucursales", ctx -> HBCatalogo.sucursales(ctx));
        get("/hb/api/catalogo-rubros", ctx -> HBCatalogo.rubros(ctx));

        // 22. Prestamo
        post("/hb/api/consolidada-prestamos", ctx -> HBPrestamo.consolidada(ctx));
        post("/hb/api/detalle-prestamo", ctx -> HBPrestamo.detalle(ctx));
        post("/hb/api/pagar-prestamo", ctx -> HBPrestamo.pagar(ctx));
        post("/hb/api/precancelacion-total-prestamo", ctx -> HBPrestamo.precancelacionTotal(ctx));
        post("/hb/api/ultima-liquidacion-prestamo", ctx -> HBPrestamo.ultimaLiquidacion(ctx));
        post("/hb/api/ultima-liquidacion-prestamo-nsp", ctx -> HBPrestamo.ultimaLiquidacionNsp(ctx));
        post("/hb/api/consolidada-prestamo-tasa-cero", ctx -> HBPrestamo.consolidadaPrestamoTasaCero(ctx));
        post("/hb/api/alta-prestamo-tasa-cero", ctx -> HBPrestamo.altaPrestamoTasaCero(ctx)); // el-front-no-lo-usa-en-prod
        post("/hb/api/parametros-segundo-desembolso", ctx -> HBPrestamo.parametrosSegundoDesembolso(ctx));
        post("/hb/api/solicitar-segundo-desembolso", ctx -> HBPrestamo.solicitarSegundoDesembolso(ctx)); // el-front-no-lo-usa-en-prod
        post("/hb/api/solicitar-segundo-desembolso-prestamo-hipotecario",
                ctx -> HBPrestamo.solicitarSegundoDesembolsoPrestamoHipotecario(ctx));
        post("/hb/api/alertas-procrear", ctx -> HBPrestamo.alertasProcrear(ctx));
        post("/hb/api/alertas-prestamo-complementario", ctx -> HBPrestamo.alertasPrestamosComplementarios(ctx));
        post("/hb/api/generar-prestamo-complementario", ctx -> HBPrestamo.generarPrestamoComplementario(ctx));
        post("/hb/api/actualizar-prestamo-complementario",
                ctx -> HBPrestamo.actualizarPrestamoComplementario(ctx));
        post("/hb/api/finalizar-prestamo-complementario",
                ctx -> HBPrestamo.finalizarPrestamoComplementario(ctx));
        post("/hb/api/movimientos-prestamo", ctx -> HBPrestamo.movimientosPrestamo(ctx));
        post("/hb/api/movimientos-desembolso", ctx -> HBPrestamo.movimientosDesembolso(ctx));
        post("/hb/api/modal-pp", ctx -> HBPrestamo.modal(ctx));
        get("/hb/api/formas-pago-prestamo", ctx -> HBPrestamo.formasDePago(ctx)); // el-front-no-lo-usa-en-prod
        post("/hb/api/cambio-forma-pago-prestamo", ctx -> HBPrestamo.cambiarFormaPago(ctx));
        post("/hb/api/alerta-prestamo-cuotificacion", ctx -> HBPrestamo.alertaPrestamoCuotificacion(ctx));
        post("/hb/api/movimientos-prestamo-cuotificacion", ctx -> HBPrestamo.movimientosCuotificacion(ctx));
        post("/hb/api/solicitar-cuotificacion", ctx -> HBPrestamo.solicitarCuotificacion(ctx)); // el-front-no-lo-usa-en-prod
        post("/hb/api/auditor-terminos-condiciones", ctx -> HBPrestamo.aceptaTerminosCondiciones(ctx));
        get("/hb/api/hipotecas-prestamo", ctx -> HBPrestamo.obtenerHipotecasPrestamo(ctx));

        // 23. Originacion
        post("/hb/api/datos-personales-faltantes-originacion",
                ctx -> HBOriginacion.datosPersonalesFaltantes(ctx));
        post("/hb/api/datos-personales-modificar-faltantes",
                ctx -> HBOriginacion.datosPersonalesModificar(ctx));
        post("/hb/api/solicitar-primer-oferta-prestamo",
                ctx -> HBOriginacion.solicitarPrimerOfertaPrestamo(ctx));
        post("/hb/api/simular-oferta-prestamo", ctx -> HBOriginacion.simularOfertaPrestamo(ctx));
        post("/hb/api/finalizar-solicitud-prestamo", ctx -> HBOriginacion.finalizarSolicitudPrestamo(ctx));

        // 24. Omnicanalidad
        post("/hb/api/solicitudes", ctx -> HBOmnicanalidad.solicitudes(ctx));
        post("/hb/api/detalle-solicitudes", ctx -> HBOmnicanalidad.detalleSolicitudes(ctx));
        post("/hb/api/detalle-solicitud", ctx -> HBOmnicanalidad.detalleSolicitud(ctx));
        post("/hb/api/eliminar-productos-extraordinarios-solicitud",
                ctx -> HBOmnicanalidad.eliminarProductosExtraordinarios(ctx));
        post("/hb/api/ofertas-paquete", ctx -> HBOmnicanalidad.ofertasPaquete(ctx));
        post("/hb/api/productos-paquetizables", ctx -> HBOmnicanalidad.productosPaquetizables(ctx));
        post("/hb/api/crear-solicitud-caja-ahorro", ctx -> HBOmnicanalidad.crearSolicitudCajaAhorro(ctx));
        post("/hb/api/actualizar-solicitud-caja-ahorro",
                ctx -> HBOmnicanalidad.actualizarSolicitudCajaAhorro(ctx));
        post("/hb/api/crear-solicitud-paquete", ctx -> HBOmnicanalidad.crearSolicitudPaquete(ctx));
        post("/hb/api/actualizar-solicitud-paquete", ctx -> HBOmnicanalidad.actualizarSolicitudPaquete(ctx));
        post("/hb/api/finalizar-solicitud-paquete", ctx -> HBOmnicanalidad.finalizarSolicitudPaquete(ctx));
        post("/hb/api/finalizar-solicitud-venta", ctx -> HBOmnicanalidad.finalizarSolicitudVenta(ctx));
        post("/hb/api/crear-solicitud-aumento-limite-tc",
                ctx -> HBAumentoLimiteTC.crearSolicitudAumentoLimiteTC(ctx));
        post("/hb/api/confirmar-solicitud-aumento-limite-tc",
                ctx -> HBAumentoLimiteTC.confirmarSolicitudAumentoLimiteTC(ctx));
        post("/hb/api/desistir-solicitud-aumento-limite-tc",
                ctx -> HBAumentoLimiteTC.desistirSolicitudAumentoLimiteTC(ctx));
        post("/hb/api/consultar-solicitud-aumento-limite-tc",
                ctx -> HBAumentoLimiteTC.consultarSolicitudesPorId(ctx)); // el-front-no-lo-usa-en-prod
        post("/hb/api/consultar-solicitudes", ctx -> HBAumentoLimiteTC.consultarSolicitudades(ctx)); // el-front-no-lo-usa-en-prod
        get("/hb/api/puede-solicitar-aumento", ctx -> HBAumentoLimiteTC.isAumentoLimiteTCSolicitado(ctx));
        post("/hb/api/actualizar-solicitud-aumento-limite-tc",
                ctx -> HBAumentoLimiteTC.actualizarSolicitudAumentoLimiteTC(ctx)); // ver si es copia de
        // y quedarse con
        // uno
        // confirmar-solicitud-aumento-limite-tc

        // 25. Comprobantes
        post("/hb/api/comprobantes", ctx -> HBComprobantes.comprobantes(ctx));
        get("/hb/api/comprobante-usuario", ctx -> HBComprobantes.comprobante(ctx));

        // 26. Debin
        post("/hb/api/lista-debin", ctx -> HBDebin.listaDebin(ctx));
        post("/hb/api/lista-debin-recibidos", ctx -> HBDebin.listaDebinRecibidos(ctx));
        post("/hb/api/lista-debin-enviados", ctx -> HBDebin.listaDebinEnviados(ctx));
        post("/hb/api/detalle-debin", ctx -> HBDebin.detalleDebin(ctx));
        post("/hb/api/nuevo-debin", ctx -> HBDebin.nuevoDebin(ctx));
        post("/hb/api/aceptar-debin", ctx -> HBDebin.aceptarDebin(ctx));
        post("/hb/api/rechazar-debin", ctx -> HBDebin.rechazarDebin(ctx));
        post("/hb/api/activar-cuenta-debin", ctx -> HBDebin.activarCuentaDebin(ctx));
        post("/hb/api/desactivar-cuenta-debin", ctx -> HBDebin.desactivarCuentaDebin(ctx));
        post("/hb/api/consulta-recurrencias", HBDebin::consultaRecurrencias);
        post("/hb/api/aceptar-recurrencia", HBDebin::aceptarRecurrencia);
        post("/hb/api/rechazar-recurrencia", HBDebin::rechazarRecurrencia);
        post("/hb/api/baja-recurrencia", HBDebin::bajaRecurrencia);

        // 27. Buho Puntos
        post("/hb/api/buhopuntos", ctx -> HBBuhoPuntos.consolidada(ctx));
        post("/hb/api/token-buhopuntos", ctx -> HBBuhoPuntos.token(ctx));
        post("/hb/api/aceptacion-propuesta", ctx -> HBBuhopuntosProducto.aceptarPropuesta(ctx));
        get("/hb/api/consulta-propuestas", ctx -> HBBuhopuntosProducto.getPropuestas(ctx));
        get("/hb/api/comprobante-propuesta", ctx -> HBBuhopuntosProducto.comprobantePropuesta(ctx));
        get("/hb/api/historial-propuestas", ctx -> HBBuhopuntosProducto.consultarHistorialPropuestas(ctx));
        get("/hb/api/disponible-cashback", ctx -> HBBuhopuntosProducto.validarCashBack(ctx));
        get("/hb/api/consulta-cashback", ctx -> HBBuhopuntosProducto.consultaCashBack(ctx));
        post("/hb/api/canjear-cashback", ctx -> HBBuhopuntosProducto.canjearCashback(ctx));
        get("/hb/api/comprobante-cashback", ctx -> HBBuhopuntosProducto.comprobanteCashback(ctx));

        // 28. Plan Sueldo Pick up
        post("/hb/api/consulta-recurrencia", ctx -> HBDebinRecurrente.consulta(ctx));
        post("/hb/api/alta-recurrencia", ctx -> HBDebinRecurrente.alta(ctx));
        post("/hb/api/modificar-recurrencia", ctx -> HBDebinRecurrente.modificacion(ctx));
        post("/hb/api/eliminar-recurrencia", ctx -> HBDebinRecurrente.baja(ctx));
        post("/hb/api/tyc-recurrencia", ctx -> HBDebinRecurrente.terminosCondiciones(ctx));
        post("/hb/api/consulta-aumento-sueldo", ctx -> HBDebinRecurrente.consultaAumentoMontoMinimo(ctx));

        // 30. Orden Extraccion
        post("/hb/api/listar-odes", ctx -> HBOrdenExtraccion.consolidada(ctx));
        post("/hb/api/crear-ode", ctx -> HBOrdenExtraccion.crear(ctx));
        post("/hb/api/cancelar-ode", ctx -> HBOrdenExtraccion.eliminar(ctx));
        post("/hb/api/comprobante-extraccion-ode", ctx -> HBOrdenExtraccion.comprobante(ctx));

        // 31. Argentina Construye
        post("/hb/api/terminos-condiciones-originacion", ctx -> HBArgentinaConstruye.terminosCondiciones(ctx));
        post("/hb/api/subir-documentacion", ctx -> HBArgentinaConstruye.subirDocumentacion(ctx)); // el-front-no-lo-usa-en-prod

        // 32. Exportar
        post("/hb/api/exporta-movimientos", ctx -> HBExporta.exportarMovimientosCuenta(ctx));
        post("/hb/api/exporta-movimientos-tarjeta", ctx -> HBExporta.exportarMovimientosTarjeta(ctx));

        // 33. Procrear Materiales
        post("/hb/api/modal-procrear", ctx -> HBProcrearRefaccion.modal(ctx));
        post("/hb/api/seguros-vida-procrear-refaccion", ctx -> HBProcrearRefaccion.segurosVida(ctx));
        post("/hb/api/documentacion-requerida-bpm", ctx -> HBProcrearRefaccion.documentacion(ctx));
        post("/hb/api/subir-documentacion-bpm", ctx -> HBProcrearRefaccion.subirDocumentacion(ctx));
        post("/hb/api/post-consulta-procrear-refaccion", ctx -> HBProcrearRefaccion.postConsulta(ctx));
        post("/hb/api/post-finalizar-procrear-refaccion", ctx -> HBProcrearRefaccion.postFinalizar(ctx));
        post("/hb/api/post-finalizar-subir-documentacion",
                ctx -> HBProcrearRefaccion.finalizarResubirSubirDocumentacion(ctx));

        // 34. Consolidado
        post("/hb/api/consolidado-movimientos", ctx -> HBConsolidado.consolidadoMovimientos(ctx));
        get("/hb/api/proximos-vencimientos", ctx -> HBConsolidado.consolidadoProximosVencimientos(ctx));

        // 35. Encuesta
        get("/hb/api/encuesta-usuario", ctx -> HBUsuario.encuestaUsuario(ctx));
        post("/hb/api/guardar-encuesta", ctx -> HBUsuario.guardarEncuesta(ctx));

        // 36. Digitalización
        get("/hb/api/estado-documentacion", ctx -> HBDocumentacion.estadoDocumentacion(ctx));
        post("/hb/api/documentacion", ctx -> HBDocumentacion.guardarDocumentacion(ctx));
        get("/hb/api/documentacion-actualizada", ctx -> HBDocumentacion.documentacionActualizada(ctx)); // el-front-no-lo-usa-en-prod
        get("/hb/api/reportes-actualizados", ctx -> HBDocumentacion.reporteActualizados(ctx)); // el-front-no-lo-usa-en-prod
        post("/hb/api/proceso-alerta-sos", ctx -> HBDocumentacion.requerirEstados(ctx, TipoNotificacion.SOS)); // el-front-no-lo-usa-en-prod
        post("/hb/api/proceso-cuenta-inversion",
                ctx -> HBDocumentacion.requerirEstados(ctx, TipoNotificacion.Inversion)); // el-front-no-lo-usa-en-prod
        get("/hb/api/sucursales-andreani", ctx -> HBDocumentacion.sucursalesAndreani(ctx)); // trae todas las
        // sucursales
        // de andreani
        post("/hb/api/documentacionv2", ctx -> HBDocumentacion.guardarDocumentacionV2(ctx));
        post("/hb/api/documentacion-captura", ctx -> HBDocumentacion.Enviar_Captura(ctx));
        get("/hb/api/consulta-cliente-nuevo", ctx -> HBDocumentacion.Cliente_Nuevo(ctx));

        // 37. Configuracion
        get("/hb/api/obtener-configuracion-variable", ctx -> HBUsuario.obtenerConfiguracionVariable(ctx)); // el-front-no-lo-usa-en-prod

        // 38. Linea Roja
        post("/hb/api/validar-user-linea-roja", ctx -> HBLineaRoja.validarUserRiesgoNet(ctx));
        post("/hb/api/responder-preguntas-riesgonet-lr", ctx -> HBLineaRoja.responderPreguntasRiesgoNetLR(ctx));
        post("/hb/api/crear-caso-linea-roja", ctx -> HBLineaRoja.generarCasoLineaRoja(ctx));

        // 39. Seguro
        get("/hb/api/modal-seguro", ctx -> HBSeguro.modalSeguro(ctx));
        get("/hb/api/token-salesforce", ctx -> HBSeguro.obtenerToken(ctx));
        get("/hb/api/productos-consolidados", ctx -> HBSeguro.obtenerProductos(ctx));
        get("/hb/api/ramo-vida", ctx -> HBSeguro.obtenerRamoProducto(ctx));
        get("/hb/api/ofertas-hogar", ctx -> HBSeguro.obtenerOfertas(ctx));
        get("/hb/api/ofertas-vida", ctx -> HBSeguro.obtenerOfertasVida(ctx));
        get("/hb/api/ofertas-ap", ctx -> HBSeguro.obtenerOfertasAP(ctx));
        get("/hb/api/ofertas-ap-mayores", ctx -> HBSeguro.obtenerOfertasAPMayores(ctx));

        get("/hb/api/ofertas-bm", ctx -> HBSeguro.obtenerOfertasBM(ctx));
        get("/hb/api/ofertas-cp", ctx -> HBSeguro.obtenerOfertasCompraProtegida(ctx));
        get("/hb/api/ofertas-atm", ctx -> HBSeguro.obtenerOfertasATM(ctx));
        get("/hb/api/ofertas-salud", ctx -> HBSeguro.obtenerOfertasSalud(ctx));
        get("/hb/api/ofertas-salud-senior", ctx -> HBSeguro.obtenerOfertasSaludSenior(ctx));
        get("/hb/api/ofertas-movilidad", ctx -> HBSeguro.obtenerOfertasMovilidad(ctx));
        get("/hb/api/ofertas-mascotas", ctx -> HBSeguro.obtenerOfertasMascotas(ctx));
        post("/hb/api/leads", ctx -> HBSeguro.leads(ctx));
        post("/hb/api/actualizar-leads", ctx -> HBSeguro.actualizarLeads(ctx));
        post("/hb/api/emision", ctx -> HBSeguro.insertarEmisionOnlineV2(ctx));

        // 40. Biometria
        post("/hb/api/revoca-autenticador", ctx -> HBBiometria.revocaAutenticador(ctx));
        post("/hb/api/verificaAccesos", ctx -> HBBiometria.verificaAccesos(ctx));

        // 41. CRM
        get("/hb/api/cliente-exterior", ctx -> HBSeguridad.getClienteExterior(ctx));
        put("/hb/api/cliente-exterior", ctx -> HBSeguridad.putClienteExterior(ctx));
        post("/hb/api/x-consulta-recurrencia", ctx -> HBCRM.consulta(ctx));
        post("/hb/api/x-alta-recurrencia", ctx -> HBCRM.alta(ctx));
        post("/hb/api/x-modificar-recurrencia", ctx -> HBCRM.modificacion(ctx));
        post("/hb/api/x-eliminar-recurrencia", ctx -> HBCRM.baja(ctx));
        post("/hb/api/x-tyc-recurrencia", ctx -> HBCRM.terminosCondiciones(ctx));
        post("/hb/api/x-consulta-aumento-sueldo", ctx -> HBCRM.consultaAumentoMontoMinimo(ctx));
        post("/hb/api/x-cuenta-tercero", ctx -> HBCRM.cuentaTercero(ctx));
        post("/hb/api/x-monto-minimo", ctx -> HBCRM.montoMinimo(ctx));
        post("/hb/api/x-monto-minimo-v2", ctx -> HBCRM.montoMinimoV2(ctx));
        post("/hb/api/x-insert-gestion", HBCRM::servicioCRMinsertGestion);

        // 42. Util
        post("/hb/api/contador", ctx -> Util.contador(ctx));
        get("/hb/api/catalogo-relaciones", ctx -> Util.CatalogoRelaciones(ctx));
        get("/hb/api/fuera-horario-procesos-batch", ctx -> Util.isFueraHorarioProcesoBatch(ctx));
        post("/hb/api/test-seguros", ctx -> Util.testSeguros(ctx));

        // 43. Distribuicón de tarjetas
        get("/hb/api/tracking-tarjeta-debito", ctx -> HBTrackeo.agregarTrackeoTarjetaDebito(ctx));

        // 44. Soft Token
        post("/hb/api/soft-token-validacion", ctx -> HBSoftToken.validarSoftToken(ctx));
        get("/hb/api/soft-token-uso-bloqueado", ctx -> HBSoftToken.validarUsoSoftTokenBloqueado(ctx));
        get("/hb/api/soft-token-activo-por-cliente",
                ctx -> HBSoftToken.consultarSoftTokenActivoPorCliente(ctx));
        get("/hb/api/soft-token-forzar-alta", ctx -> HBSoftToken.forzarAltaSoftToken(ctx)); // el-front-no-lo-usa-en-prod

        // 45. Adelanto
        post("/hb/api/campanas", ctx -> HBConsolidado.ofertaPreAprobada(ctx));
        get("/hb/api/adelantoBH-descripcion", ctx -> HBAdelanto.catalogoPreguntaRespuesta(ctx));
        post("/hb/api/adelantoBH-solicitud", ctx -> HBAdelanto.solicitudAdelanto(ctx));
        post("/hb/api/adelantoBH-finalizar-solicitud", ctx -> HBAdelanto.finalizarSolicitud(ctx));

        // 46. Canal Amarillo
        post("/hb/api/situacion-laboral", ctx -> HBPersona.situacionLaboral(ctx));
        post("/hb/api/documentacion-requerida", ctx -> HBDocumentacion.documentacionXSolicitud(ctx));
        post("/hb/api/mejorar-primer-oferta-prestamo", ctx -> HBOriginacion.mejorarPrimerOfertaPrestamo(ctx));
        post("/hb/api/guardar-documentacion", ctx -> HBDocumentacion.guardarDocumentacionV2(ctx));
        post("/hb/api/envio-mail-canal-amarillo", ctx -> HBOriginacion.envioMailCanalAmarillo(ctx));
        post("/api/desistir-solicitud-prestamo", ctx -> HBOmnicanalidad.desistirSolicitudBPM(ctx));
        post("/api/elimina-solicitud-front", ctx -> HBOriginacion.eliminaSolicitudFront(ctx));

        // 47. originacion
        post("/hb/api/reclamo-documentacion", ctx -> HBOriginacion.reclamoDocumentacion(ctx));

        // 48. tus gestiones
        get("/hb/api/gestiones", ctx -> HBGestiones.gestionesAsync(ctx));
        get("/hb/api/gestiones-sucursal-virtual", ctx -> HBGestiones.gestionesSucursalVirtual(ctx));
        get("/hb/api/gestiones-inversion", ctx -> HBGestiones.gestionesInversion(ctx));
        get("/hb/api/gestiones-postventa", ctx -> HBGestiones.gestionesPostventa(ctx));
        get("/hb/api/gestiones-aumento-limite", ctx -> HBGestiones.gestionesAumentoLimite(ctx));
        get("/hb/api/solicitar-libre-deuda", ctx -> HBGestiones.solicitarLibreDeuda(ctx));
        get("/hb/api/solicitar-liberacion-hipoteca", ctx -> HBGestiones.solicitarLiberacionHipoteca(ctx));

        // 49. mora
        post("/hb/api/generar-promesa-pago", ctx -> HBMora.generarPromesaPago(ctx));
        post("/hb/api/productos-en-mora", ctx -> HBMora.productosEnMora(ctx));

        // 50. Pago y Recarga de Servicios
        get("/hb/api/rubros-recargas", contexto -> HBPagoRecargaServicios.obtenerRubrosRecarga(contexto));
        get("/hb/api/empresas-recargas",
                contexto -> HBPagoRecargaServicios.obtenerEmpresasPorRubroRecarga(contexto));
        post("/hb/api/alta-adhesion-recargas", contexto -> HBPagoRecargaServicios.altaAdhesion(contexto));
        get("/hb/api/adhesiones-recargas",
                contexto -> HBPagoRecargaServicios.obtenerAdhesionesTodosRubros(contexto));
        get("/hb/api/adhesiones-empresas-recargas",
                contexto -> HBPagoRecargaServicios.obtenerAdhesionesPorEmpresa(contexto));
        post("/hb/api/elimina-adhesion-recargas", contexto -> HBPagoRecargaServicios.eliminaAdhesion(contexto));
        post("/hb/api/historial-recargas", contexto -> HBPagoRecargaServicios.historialRecargas(contexto));
        post("/hb/api/recarga", contexto -> HBPagoRecargaServicios.recargaServicio(contexto));

        // 51. Consentimiento
        post("/hb/api/ps-token", ctx -> HBConsentimiento.obtenerToken(ctx));

        // 52. Cedip
        get("/hb/api/cedips-recibidos", ctx -> HBCedip.cedipsRecibidos(ctx));
        get("/hb/api/cedips", ctx -> HBCedip.consolidadaCedips(ctx));
        post("/hb/api/cedip", ctx -> HBCedip.altaCedip(ctx));
        post("/hb/api/acreditacion-cedip", ctx -> HBCedip.acreditacionCedip(ctx));
        post("/hb/api/transferencia-cedip", ctx -> HBCedip.transferenciaCedip(ctx));
        post("/hb/api/anular-transferencia-cedip", ctx -> HBCedip.anularTransferenciaCedip(ctx));
        post("/hb/api/depositar-cedip", ctx -> HBCedip.depositarCedip(ctx));
        get("/hb/api/comprobante-cedip", ctx -> HBCedip.comprobanteCedip(ctx));

        // 52. Saleforces
        post("/hb/api/registrar-evento-salesforce", ctx -> HBSalesforce.registrarEventoSalesforce(ctx));

        // 53. Sucursal Virtual - Aceptacion Digital
        post("/hb/api/datos-confirmacion-producto", ctx -> HBSucursalVirtual.datosConfirmacionProductos(ctx));
        post("/hb/api/ver-tyc", ctx -> HBSucursalVirtual.verTerminosYCondiciones(ctx));
        post("/hb/api/aceptar-solicitud", ctx -> HBSucursalVirtual.aceptarSolicitud(ctx));
        post("/hb/api/ver-nueva-propuesta", ctx -> HBSucursalVirtual.verNuevaPropuesta(ctx));
        post("/hb/api/aceptar-nueva-propuesta", ctx -> HBSucursalVirtual.aceptarNuevaPropuesta(ctx));

        // 54. ReCaptcha
        post("/hb/api/verificar-recaptcha", ctx -> HBGoogleCaptcha.verificarReCaptchaLogin(ctx));
        get("/hb/api/config-recaptcha", ctx -> HBGoogleCaptcha.config());

        // 55. Alta Online
        post("/hb/api/alta-caja-ahorro-tarjeta-debito-online",
                ctx -> HBOmnicanalidad.altaCajaAhorroTarjetaDebitoOnline(ctx));
        get("/hb/api/fuera-horario-dia-procesos-batch", ctx -> Util.isFueraHorarioDiaProcesoBatch(ctx));

        // 56. Plan V
        get("/hb/api/info-financiamiento", ctx -> HBPlanV.obtenerInformacionFinanciamiento(ctx));
        post("/hb/api/simulacion-financiamiento", ctx -> HBPlanV.simularFinanciamiento(ctx));
        post("/hb/api/confirmacion-financiamiento", ctx -> HBPlanV.confirmarFinanciamiento(ctx));
        get("/hb/api/consultar-aprobadas", ctx -> HBPlanV.obtenerFinanciamientosAprobados(ctx));

        // 57. Transmit
        post("/hb/api/renovar-transmit", HBLogin::renovacionTransmit);
        get("/hb/api/obtener-tipo-cliente-transmit", ctx -> HBPersona.obtenerDatosClientesTransmit(ctx));
        get("/hb/api/clientes-nuevo-transmit-datos", ctx -> HBPersona.obtenerDatosNuevoClientesTransmit(ctx));
        post("/hb/api/claves-link-transmit-validar", HBSeguridad::validarClaveLinkTransmit);
        post("/hb/api/nuevo-usuario-transmit", ctx -> HBUsuario.nuevoUsuarioTransmit(ctx));
    }
}
