package ar.com.hipotecario.canal.buhobank;

import ar.com.hipotecario.backend.Config;
import ar.com.hipotecario.backend.Servidor;

public class ApiBuhoBank extends CanalBuhoBank {

	public static Config config = new Config();

	public static void main(String[] args) throws Exception {
		Servidor.main(args);
	}
	
	public static String armarHorario(String hora) {
		String minuto = "00";
		String diaMes = "*";
		String mes = "*";
		String diaSemana = "*";

		return String.format("%s %s %s %s %s", minuto, hora, diaMes, mes, diaSemana);
	}

	public static String armarHorarioRemarketing() {
		String hora = GeneralBB.CRON_ALERTA_PUSH_HORA_INICIO + "-" + GeneralBB.CRON_ALERTA_PUSH_HORA_FIN;
		String minuto = "*/" + String.valueOf(GeneralBB.MINUTO_PROCESO_ALERTA_PUSH);
		String diaMes = "*";
		String mes = "*";
		String diaSemana = "*";

		return String.format("%s %s %s %s %s", minuto, hora, diaMes, mes, diaSemana);
	}

	public static String armarHorarioBorrarSesionesNoFinalizadas() {
		String hora = "1";
		String minuto = "00";
		String diaMes = "*";
		String mes = "*";
		String diaSemana = "*";

		return String.format("%s %s %s %s %s", minuto, hora, diaMes, mes, diaSemana);
	}

	public static String armarHorarioAnonimizarClientesVu() {
		String hora = "2";
		String minuto = "00";
		String diaMes = "*";
		String mes = "*";
		String diaSemana = "*";

		return String.format("%s %s %s %s %s", minuto, hora, diaMes, mes, diaSemana);
	}

	public static String armarHorarioConfiguracionPrismaNotificacionesNFC() {
		String hora = "21";
		String minuto = "00";
		String diaMes = "13";
		String mes = "8";
		String diaSemana = "*";

		return String.format("%s %s %s %s %s", minuto, hora, diaMes, mes, diaSemana);
	}

	public static void iniciar() {

		// CRON
		if (Config.esOpenShift()) {
			cron(armarHorario(GeneralBB.CRON_ALTA_BATCH_HORA_INICIO), new CronAltaAutomaticaBB());
			cron(armarHorario(GeneralBB.CRON_INVERSOR_HORA_INICIO), new CronBBInversor());
			cron(armarHorarioRemarketing(), new CronAlertasPush());
			cron(armarHorarioBorrarSesionesNoFinalizadas(), new CronSesionesNoFinalizadas());
			if(!config.esDesarrollo()){
				cron(armarHorarioAnonimizarClientesVu(), new CronAnonimizarClientesVu());
				cron(armarHorarioConfiguracionPrismaNotificacionesNFC(), new CronPrismaNotificacionesNFC());
			}
		}

		// 00. Tools deploy
		iget("/bb/api/health", contexto -> BBAplicacion.health(contexto));

		// 00. Aplicacion
		iget("/bb/api/config", contexto -> BBAplicacion.config(contexto));
		iget("/bb/api/config/vista", contexto -> BBAplicacion.obtenerSiguienteVista(contexto));

		// 01. Login
		ipost("/bb/api/sesion", contexto -> BBSeguridad.crearSesion(contexto));
		get("/bb/api/sesion", contexto -> BBSeguridad.obtenerSesion(contexto));
		ipost("/bb/api/retomarSesion", contexto -> BBSeguridad.retomarSesion(contexto));
		post("/bb/api/sesion/salir", contexto -> BBSeguridad.borrarSesion(contexto));
		get("/bb/api/claveactiva", contexto -> BBSeguridad.claveActiva(contexto, true));
		post("/bb/api/crearclave", contexto -> BBSeguridad.generarCredenciales(contexto));
		ipost("/bb/api/login", contexto -> BBSeguridad.login(contexto));

		// 02. Validacion
		post("/bb/api/guardarcontacto", contexto -> BBValidacion.guardarContacto(contexto));
		post("/bb/api/guardartelefono", contexto -> BBValidacion.guardartelefono(contexto));
		get("/bb/api/enviarotpsms", contexto -> BBValidacion.enviarOtpSms(contexto));
		post("/bb/api/validarotpsms", contexto -> BBValidacion.validarOtpSms(contexto));
		post("/bb/api/guardarmail", contexto -> BBValidacion.guardarMail(contexto));
		get("/bb/api/enviarotpmail", contexto -> BBValidacion.enviarOtpMail(contexto));
		post("/bb/api/validarotpmail", contexto -> BBValidacion.validarOtpMail(contexto));
		get("/bb/api/aceptartyc", contexto -> BBValidacion.aceptartyc(contexto));
		
		// 03. Persona
		put("/bb/api/sesion", contexto -> BBPersona.actualizarSesion(contexto));
		ipost("/bb/api/escliente", contexto -> BBPersona.esClienteBH(contexto));
		ipost("/bb/api/obtenercuil", contexto -> BBPersona.obtenerCuil(contexto));
		post("/bb/api/guardarvucompleto", contexto -> BBPersona.guardarRespuestaCompletaVU(contexto));
		post("/bb/api/validarpersona", contexto -> BBPersona.validarPersona(contexto));
		post("/bb/api/guardarnacionalidad", contexto -> BBPersona.guardarNacionalidad(contexto));
		post("/bb/api/guardaradicionales", contexto -> BBPersona.guardarAdicionales(contexto));
		post("/bb/api/guardarconyuge", contexto -> BBPersona.guardarAdicionalConyuge(contexto));
		get("/bb/api/noconfirmardatos", contexto -> BBPersona.noConfirmarDatos(contexto));
		get("/bb/api/validardatospersonales", contexto -> BBPersona.validarDatosPersonales(contexto));
		get("/bb/api/obtenerdatoscobis", contexto -> BBPersona.obtenerDatosCobis(contexto));
		post("/bb/api/localizacion", contexto -> BBPersona.guardarLocalizacion(contexto));

		// 04. Paquetes
		get("/bb/api/ofertasNuevo", contexto -> BBPaquetes.ofertas(contexto));
		get("/bb/api/standaloneVirtual", contexto -> BBPaquetes.ofertaStandaloneVirtual(contexto));
		post("/bb/api/elegiroferta", contexto -> BBPaquetes.elegirOferta(contexto));
		get("/bb/api/elegirfisica", contexto -> BBPaquetes.elegirFisica(contexto));
		get("/bb/api/elegircuentasueldo", contexto -> BBPaquetes.elegirCuentaSueldo(contexto));
		get("/bb/api/desistir-solicitud", contexto -> BBPaquetes.desistirSolicitud(contexto));
		
		// 05. Forma de Entrega
		post("/bb/api/formaentrega", contexto -> BBFormaEntrega.guardarFormaDeEntrega(contexto));
		post("/bb/api/guardarpostalalt", contexto -> BBFormaEntrega.guardarDomicilioAlternativo(contexto));
		post("/bb/api/guardardomlegal", contexto -> BBFormaEntrega.guardarDomicilioLegal(contexto));
		get("/bb/api/validarformaentrega", contexto -> BBFormaEntrega.validarDatosFormaEntrega(contexto));

		iget("/bb/api/codigosareas/provincia", contexto -> BBFormaEntrega.obtenerCodigosAreasPorProvincia(contexto));
		iget("/bb/api/codigosareas", contexto -> BBFormaEntrega.obtenerCodigosAreas(contexto));
		iget("/bb/api/codigosareas/codigo", contexto -> BBFormaEntrega.obtenerCodigosAreasPorCodigo(contexto));
		ipost("/bb/api/codigosareas/codigo", contexto -> BBFormaEntrega.obtenerCodigosAreasPorCodigo(contexto));
		iget("/bb/api/catalogo/paises", contexto -> BBFormaEntrega.paises(contexto));
		iget("/bb/api/catalogo/provincias", contexto -> BBFormaEntrega.provincias(contexto));
		iget("/bb/api/catalogo/localidad", contexto -> BBFormaEntrega.localidad(contexto));
		iget("/bb/api/catalogo/sucursal", contexto -> BBFormaEntrega.sucursales(contexto));
		iget("/bb/api/catalogo/sucursal-andriani", contexto -> BBSucursalAndreani.obtenerSucursales(contexto));

		// 06. Landing
		iget("/bb/api/landing/ofertas", contexto -> BBLanding.todasOfertas(contexto));
		iget("/bb/api/landing/ofertas/:numeropaquete", contexto -> BBLanding.ofertas(contexto));
		iget("/bb/api/promos", contexto -> BBLanding.obtenerPromosLanding(contexto));

		// 07. Alta
		get("/bb/api/finalizar", contexto -> BBAlta.finalizar(contexto));
		get("/bb/api/generar-idcliente", contexto -> BBAltaProducFinal.generarIDCliente(contexto));
		post("/bb/api/otrosProductos", contexto -> BBAltaProducFinal.altaOtrosProductos(contexto, null));

		// 08. Reportes
		ipost("/bb/api/registrarevento", contexto -> BBReporte.registrarEvento(contexto));
		ipost("/bb/api/registrareventoexterno", contexto -> BBReporte.registrarEventoExterno(contexto));

		// 09. PlazoFijoWeb
		oget("/api/plazoFijo/configuracionBackendUnificado", contexto -> BBPlazoFijoWeb.configuracion(contexto));
		oget("/api/plazoFijo/diasFeriados", contexto -> BBPlazoFijoWeb.diasFeriados(contexto));
		oget("/api/plazoFijo/parametria", contexto -> BBPlazoFijoWeb.parametria(contexto));
		oget("/api/plazoFijo/inicializarDatosBasicos", contexto -> BBPlazoFijoWeb.inicializarDatosBasicos(contexto));
		oget("/api/plazoFijo/obtenerCuil", contexto -> BBPlazoFijoWeb.obtenerCuil(contexto));
		opost("/api/plazoFijo/iniciar", contexto -> BBPlazoFijoWeb.iniciar(contexto));
		oget("/api/plazoFijo/catalogo/pais", contexto -> BBPlazoFijoWeb.catalogoPais(contexto));
		oget("/api/plazoFijo/catalogo/provincias", contexto -> BBPlazoFijoWeb.catalogoProvincias(contexto));
		oget("/api/plazoFijo/situacionLaboral", contexto -> BBPlazoFijoWeb.situacionlaboral(contexto));
		oget("/api/plazoFijo/catalogo/estadosCiviles", contexto -> BBPlazoFijoWeb.catalogoEstadosCiviles(contexto));
		oget("/api/plazoFijo/localidad", contexto -> BBPlazoFijoWeb.localidad(contexto));
		oget("/api/plazoFijo/verificarCBU", contexto -> BBPlazoFijoWeb.verificarCBU(contexto));
		opost("/api/plazoFijo/simular", contexto -> BBPlazoFijoWeb.simular(contexto));
		opost("/api/plazoFijo/constituir", contexto -> BBPlazoFijoWeb.constituir(contexto));

		// 10. Archivos
		post("/bb/api/guardarDocumentacion", contexto -> BBDocumentacion.guardarArchivos(contexto));

		// 12. Contenido
		ipost("/bb/api/contenido", contexto -> BBLanding.obtenerContenido(contexto));
		iget("/bb/api/contenido/dinamico", contexto -> BBAplicacion.obtenerContenidoDinamico(contexto));
		ipost("/bb/api/contenido/dinamico", contexto -> BBAplicacion.obtenerContenidoDinamicoTipos(contexto));
		iget("/bb/api/contenido/dinamico/paquete", contexto -> BBAplicacion.obtenerContenidoDinamicoPaquete(contexto));
		get("/bb/api/contenidofinalizar", contexto -> BBAplicacion.obtenerContenidoFinalizar(contexto));
		iget("/bb/api/contenido/promos", contexto -> BBLanding.obtenerContenidoPromos(contexto));

		// 13. Test y QA
		post("/bb/api/simularVU", contexto -> BBPruebas.simularVU(contexto));
		ipost("/bb/api/obtenerguardarvucompleto", contexto -> BBPersona.obtenerGuardarRespuestaCompletaVU(contexto));
		ipost("/bb/api/testobtenerOferta", contexto -> BBPaquetes.testObtenerOferta(contexto));
		ipost("/bb/api/test-tcv", contexto -> BBPruebas.testFinalizarTcv(contexto));

		// 14. Renaper
		ipost("/bb/api/serviciorenaper", contexto -> BBPersona.obtenerRenaperQr(contexto));
		ipost("/bb/api/renaper", contexto -> BBPersona.obtenerRenaperFinalizados(contexto));

		// 15. redis
		iget("/bb/api/redis", contexto -> BBRedis.leerRedis(contexto));
		ipost("/bb/api/redis", contexto -> BBRedis.guardarRedis(contexto));
		idelete("/bb/api/redis", contexto -> BBRedis.borrarRedis(contexto));

		// 16. Remarketing manual - SofToken Manual
		aget("/bb/api/batch", contexto -> BBAlta.finalizarPendientesBatch(contexto));
		aget("/bb/api/sesiones", contexto -> BBAlta.obtenerSesiones(contexto));
		aget("/bb/api/sesiones/estado", contexto -> BBAlta.obtenerSesionesEstado(contexto));
		aget("/bb/api/sesiones/standby", contexto -> BBAlta.obtenerSesionesStandByControlar(contexto));
		aget("/bb/api/sesiones/standby/reporte", contexto -> BBAlta.obtenerReporteSesionesStandBy(contexto));
		aget("/bb/api/sesiones/standby/estados", contexto -> BBAlta.obtenerEstadosStandBy(contexto));
		apost("/bb/api/marcarbatch", contexto -> BBAlta.marcarBatch(contexto));
		apost("/bb/api/standby", contexto -> BBAlta.cambiarEstadoSesionStandBy(contexto));
		aget("/bb/api/datosvu", contexto -> BBPersona.obtenerDatosVu(contexto));
		aget("/bb/api/registros", BBReporte::obtenerRegistros);
		aget("/bb/api/registros/reporte", contexto -> BBReporte.obtenerReporteRegistros(contexto));
		aget("/bb/api/registros/reporte/periodico", contexto -> BBReporte.obtenerReportePeriodicoRegistros(contexto));
		apost("/bb/api/rm/retomarsesion", contexto -> BBAlta.retomarSesionRm(contexto));
		apost("/bb/api/rm/estados", contexto -> BBPruebas.obtenerEstadosSesiones(contexto));

		// 17. config manual
		ipost("/bb/api/config/login", contexto -> BBSeguridad.configLogin(contexto));
		aget("/bb/api/config-manual-sucursales", contexto -> BBAplicacion.obtenerSucursalesOnboarding(contexto));
		apost("/bb/api/config-manual-sucursales", contexto -> BBAplicacion.actualizarAgregarSucursalOnboarding(contexto));
		aget("/bb/api/config-manual", contexto -> BBConfigManual.getConfigManual(contexto));
		apost("/bb/api/config-manual", contexto -> BBConfigManual.postConfigManual(contexto));
		aput("/bb/api/config-manual", contexto -> BBConfigManual.putConfigManual(contexto));
		adelete("/bb/api/config-manual", contexto -> BBConfigManual.deleteConfigManual(contexto));
				
		// 18. externo
		iget("/bb/api/remarketing/chatbot", contexto -> BBRemarketingChatbot.obtenerRemarketingWhatsapp(contexto));
		iget("/bb/api/adjust", contexto -> BBReporte.registrarEventoAdjust(contexto));
		
		// 19. Corresponsalia
		ipost("/bb/api/tiene-plan", contexto -> BBCorresponsalia.esJubilado(contexto));
		ipost("/bb/api/afip", contexto -> BBPersona.obtenerDatosAfip(contexto));

		// 20. flujo tcv
		ipost("/bb/api/persona", contexto -> BBPersona.personaRenaper(contexto));
		ipost("/bb/api/sesion-v2", contexto -> BBSeguridad.crearSesionV2(contexto));
		get("/bb/api/oferta", contexto -> BBPaquetes.ofertasV2(contexto));
		get("/bb/api/oferta-standalone", contexto -> BBPaquetes.ofertasStandalone(contexto));
		post("/bb/api/aceptar-oferta", contexto -> BBPaquetes.aceptarOfertaMotor(contexto));
		get("/bb/api/oferta-sesion", contexto -> BBPaquetes.ofertaSesion(contexto));
		get("/bb/api/domicilio", contexto -> BBFormaEntrega.validarDomicilioV2(contexto));
		post("/bb/api/fatca", contexto -> BBPersona.guardarFatca(contexto));
		get("/bb/api/aceptartyc-oferta", contexto -> BBValidacion.aceptartycOferta(contexto));
	}
}
