package ar.com.hipotecario.canal.tas;

import ar.com.hipotecario.backend.Servidor;
import ar.com.hipotecario.canal.tas.modulos.auditoria.controllers.TASAuditoriaController;
import ar.com.hipotecario.canal.tas.modulos.cajasseguridad.controllers.TASCajasSeguridadController;
import ar.com.hipotecario.canal.tas.modulos.cuentas.cuenta.controllers.TASCuentaController;
import ar.com.hipotecario.canal.tas.modulos.cuentas.depositos.controllers.TASDepositosEfectivoController;
import ar.com.hipotecario.canal.tas.modulos.cuentas.transferencias.controllers.TASTransferenciasController;
import ar.com.hipotecario.canal.tas.modulos.inicio.posicion_consolidada.controllers.TASPosicionConsolidadaController;
import ar.com.hipotecario.canal.tas.modulos.inicio.posicion_consolidadaV4.controller.TASPosicionConsolidadaV4Controller;
import ar.com.hipotecario.canal.tas.modulos.no_clientes.controllers.TASLoginNCController;
import ar.com.hipotecario.canal.tas.modulos.plazosfijos.controllers.TASPlazosFijosController;
import ar.com.hipotecario.canal.tas.modulos.prestamos.controllers.TASPrestamosController;
import ar.com.hipotecario.canal.tas.modulos.prestamos.pagos.controllers.TASPagoPrestamoEfectivoController;
import ar.com.hipotecario.canal.tas.modulos.tarjetas.controllers.TASTarjetasController;
import ar.com.hipotecario.canal.tas.modulos.tarjetas.pagos.controllers.TASPagoTarjetaEfectivoController;
import ar.com.hipotecario.canal.tas.shared.modulos.apis.formularios.controllers.TASFormulariosController;
import ar.com.hipotecario.canal.tas.shared.modulos.apis.inversiones.controllers.TASInversionesControllers;
import ar.com.hipotecario.canal.tas.shared.modulos.apis.notificaciones.controllers.TASNotificacionesController;
import ar.com.hipotecario.canal.tas.shared.modulos.apis.persona.controllers.TASApiPersona;
import ar.com.hipotecario.canal.tas.modulos.inicio.login.controllers.TASLoginController;
import ar.com.hipotecario.canal.tas.shared.modulos.apis.seguridad.controllers.TASSeguridadApi;
import ar.com.hipotecario.canal.tas.shared.modulos.kiosco.controllers.TASKioscoController;
import ar.com.hipotecario.canal.tas.shared.modulos.kiosco.controllers.TASSesionKioscoController;

public class ApiTAS extends CanalTAS {

	public static void main(String[] args) throws Exception {
		Servidor.main(args);
	}

	public static void iniciar() {

		//GENERICOS
		get("/tas/api/health", contexto -> TASAplicacion.health(contexto));
		get("/tas/api/flag-bacuni", contexto -> TASAplicacion.flagBacuni(contexto));
		get("/tas/api/data-sesion", contexto -> TASAplicacion.dataEnContexto(contexto));

		//KIOSCO
		post("/tas/api/orden-ingreso-kiosco", contexto -> TASSesionKioscoController.ordenIngresoKiosco(contexto));
		get("/tas/api/get-kiosco", contexto -> TASKioscoController.getKiosco(contexto));
		post("/tas/api/estado-operativo-kiosco", contexto -> TASSesionKioscoController.estadoKiosco(contexto));
		get("/tas/api/flags", contexto -> TASKioscoController.getFlagsKiosco(contexto));

		//SESION
		post("/tas/api/login", contexto -> TASLoginController.ingresar(contexto));
		post("/tas/api/logout", contexto -> TASLoginController.salir(contexto));
		post("/tas/api/no-clientes/login", contexto -> TASLoginNCController.loginNoCliente(contexto));
		post("/tas/api/no-clientes/logout", contexto -> TASLoginNCController.logoutNoCliente(contexto));
		get("/tas/api/posicion-consolidada-v3", contexto -> TASPosicionConsolidadaController.getPosicionConsolidada(contexto));
		get("/tas/api/posicion-consolidada", contexto -> TASPosicionConsolidadaV4Controller.getPosicionConsolidadaV4(contexto));

		get("/tas/api/notificaciones", contexto -> TASNotificacionesController.getNotificaciones(contexto));
		post("/tas/api/notificaciones", contexto -> TASNotificacionesController.postNotificaciones(contexto));
		get("/tas/api/validacion-isva", contexto -> TASSeguridadApi.validacionISVA(contexto));
		put("/tas/api/modificar-clave", contexto -> TASLoginController.putCambiarClave(contexto));

		//PERSONA
		get("/tas/api/cliente-persona", contexto -> TASApiPersona.getClientePersona(contexto));
		get("/tas/api/persona", contexto -> TASApiPersona.getPersona(contexto));
		get("/tas/api/mail-prioritario", contexto -> TASApiPersona.getMailPrioritario(contexto));
		post("/tas/api/validar-persona", contexto -> TASApiPersona.validarPersona(contexto));
		get("/tas/api/documento-persona", contexto -> TASApiPersona.getPersonaDocumento(contexto));
		get("/tas/api/consultar-censo", contexto -> TASApiPersona.getConsultarCenso(contexto));
		get("/tas/api/consultar-datos-personales", contexto -> TASApiPersona.getConsultarDatosPersonales(contexto));		

		//CUENTAS
		get("/tas/api/cuentas", contexto -> TASCuentaController.getCuentas(contexto));
		get("/tas/api/validar-cuenta-deposito", contexto -> TASCuentaController.validarCuentaDeposito(contexto));
		get("/tas/api/detalle-ca", contexto -> TASCuentaController.getDetalleCA(contexto));
		get("/tas/api/detalle-cc", contexto -> TASCuentaController.getDetalleCC(contexto));
		get("/tas/api/ultimos-movimientos-ca", contexto -> TASCuentaController.getUltimosMovimientosCa(contexto));
		get("/tas/api/ultimos-movimientos-cc", contexto -> TASCuentaController.getUltimosMovimientosCc(contexto));
		get("/tas/api/titulares-ca", contexto -> TASCuentaController.getTitulares(contexto));
		get("/tas/api/bloqueos", contexto -> TASCuentaController.getBloqueosCa(contexto));
		post("/tas/api/baja-ca", contexto -> TASCuentaController.bajaCa(contexto));
		post("/tas/api/ejecutar-transferencia", contexto -> TASTransferenciasController.ejecutarTransferencia(contexto));
		post("/tas/api/historico-saldos", contexto -> TASCuentaController.getSaldosHistoricos(contexto));
		get("/tas/api/seguimiento-paquetes", contexto -> TASCuentaController.getSeguimientoPaquetes(contexto));
		get("/tas/api/buscar-textos", contexto -> TASFormulariosController.getBuscarTextos(contexto));
		post("/tas/api/envio-solicitud-ca", contexto -> TASCuentaController.postEnvioSolicitudCA(contexto));
		post("/tas/api/generar-ca" , contexto -> TASCuentaController.postGenerarCA(contexto));
		post("/tas/api/vincular-td", contexto -> TASCuentaController.postVincularTD(contexto));


		// DEPOSITOS
		// 1- inicializar proceso de deposito
		post("/tas/api/inicializar-deposito-efectivo",
				contexto -> TASDepositosEfectivoController.postInicializarDeposito(contexto));

		// 2- registrar billetes y monto total del deposito
		post("/tas/api/registrar-billetes-deposito-efectivo",
				contexto -> TASDepositosEfectivoController.registrarBilletesDeposito(contexto));

		post("/tas/api/depositos-efectivo", contexto -> TASDepositosEfectivoController.postDepositosEfectivo(contexto));
		post("/tas/api/actualizar-deposito", contexto -> TASDepositosEfectivoController.postActualizarDeposito(contexto));

		patch("/tas/api/depositos-efectivo",
				contexto -> TASDepositosEfectivoController.patchDepositosEfectivoReversa(contexto));

		// TARJETAS
		// 1- inicializar proceso de pago
		post("/tas/api/inicializar-pago-tarjeta-efectivo",
				contexto -> TASPagoTarjetaEfectivoController.postInicializarPago(contexto));
		
		post("/tas/api/pago-tarjeta-efectivo", contexto -> TASPagoTarjetaEfectivoController.postPagoTarjetaEfectivo(contexto));

		post("/tas/api/actualizar-pago-tarjeta-efectivo", contexto -> TASPagoTarjetaEfectivoController.postActualizarPago(contexto));

		patch("/tas/api/pago-tarjeta-efectivo",
				contexto -> TASPagoTarjetaEfectivoController.patchPagoTarjetaEfectivoReversa(contexto));
		
		// PRESTAMOS
		// 1- inicializar proceso de pago
		post("/tas/api/inicializar-pago-prestamo-efectivo",
						contexto -> TASPagoPrestamoEfectivoController.postInicializarPago(contexto));
				
		post("/tas/api/pago-prestamo-efectivo", contexto -> TASPagoPrestamoEfectivoController.postPagoPrestamoEfectivo(contexto));

		post("/tas/api/actualizar-pago-prestamo-efectivo", contexto -> TASPagoPrestamoEfectivoController.postActualizarPago(contexto));

		patch("/tas/api/pago-prestamo-efectivo",
						contexto -> TASPagoPrestamoEfectivoController.patchPagoPrestamoEfectivoReversa(contexto));


		
		post("/tas/api/pago-tarjeta-debito-cuenta", contexto -> TASTarjetasController.postPagoTarjetaDebitoCuenta(contexto));
		post("/tas/api/programar-pago-tarjeta", contexto -> TASTarjetasController.postProgramarPagoTarjeta(contexto));

		get("/tas/api/consultar-tc", contexto -> TASTarjetasController.getTarjetaCredito(contexto));
		get("/tas/api/resumen-tc", contexto -> TASTarjetasController.getResumenTarjetaCredito(contexto));
		post("/tas/api/envio-resumen-tc", contexto -> TASTarjetasController.envioResumenTC(contexto));
		patch("/tas/api/cambio-forma-pago-tc", contexto -> TASTarjetasController.patchCambioFormaPago(contexto));
		get("/tas/api/obtener-mensajes-forma-pago", contexto -> TASTarjetasController.getMensajesFormaPago(contexto));
		post("/tas/api/consultar-estados-td", contexto -> TASTarjetasController.getEstadosTarjetaDebito(contexto));
		post("/tas/api/blanquear-td", contexto -> TASTarjetasController.postBlanqueoTD(contexto));
		get("/tas/api/consultar-tc-terceros", contexto -> TASTarjetasController.getTarjetaCreditoTerceros(contexto));

		
		//PRESTAMOS
		get("/tas/api/consulta-prestamo", contexto -> TASPrestamosController.getPrestamo(contexto));
		get("/tas/api/resumen-prestamo", contexto -> TASPrestamosController.getResumenPrestamo(contexto));
		post("/tas/api/pago-prestamo-debito-cuenta", contexto -> TASPrestamosController.postPagoPrestamoDebitoCuenta(contexto));
		get("/tas/api/consulta-prestamo-terceros", contexto -> TASPrestamosController.getPrestamoTerceros(contexto));

		//PLAZOS FIJOS
		get("/tas/api/tipos-pf", contexto -> TASPlazosFijosController.getTiposPF(contexto));
		get("/tas/api/tasa-preferencial", contexto -> TASPlazosFijosController.getTasasPromocionalesPF(contexto));
		get("/tas/api/detalle-pf", contexto -> TASPlazosFijosController.getDetallePF(contexto));
        post("/tas/api/alta-pf", contexto -> TASPlazosFijosController.postConstituirPF(contexto));
		get("/tas/api/consultar-cancelacion-ant", contexto -> TASPlazosFijosController.getCancelacionAnticipada(contexto));
		post("/tas/api/solicitar-cancelacion-ant", contexto -> TASPlazosFijosController.postCancelacionAnticipada(contexto));
		get("/tas/api/pf-detalle-cancelacion-ant", contexto -> TASPlazosFijosController.getPlazoFijoDetalle(contexto));

		//PLAZOS FIJOS LOGROS
		get("/tas/api/tipos-pf-logros", contexto -> TASPlazosFijosController.getTiposPFLogros(contexto));
		get("/tas/api/cabecera-pf-logros", contexto -> TASPlazosFijosController.getCabeceraPFLogros(contexto));
		post("/tas/api/alta-pf-logros", contexto -> TASPlazosFijosController.postConstituirPFLogros(contexto));
		get("/tas/api/detalle-cuotas-pf-logros", contexto -> TASPlazosFijosController.getDetalleCuotasPFLogros(contexto));
		get("/tas/api/forzado-cuota-pf-logros", contexto -> TASPlazosFijosController.getForzadoCuotaPFLogros(contexto));
		get("/tas/api/baja-pf-logros", contexto -> TASPlazosFijosController.getBajaPFLogros(contexto));
		get("/tas/api/modificar-pf-logros", contexto -> TASPlazosFijosController.getModificarPFLogros(contexto));


		//INVERSIONES
		get("/tas/api/cotizaciones", contexto -> TASInversionesControllers.getCotizaciones(contexto));
		post("/tas/api/operaciones-moneda-extranjera", contexto -> TASTransferenciasController.postOperacionesMonedaExtranjera(contexto));
		get("/tas/api/cotizaciones-moneda", contexto -> TASInversionesControllers.getCotizacionesMoneda(contexto));

		//CAJAS DE SEGURIDAD
		post("/tas/api/detalle-cajas-seg", contexto -> TASCajasSeguridadController.getCajasSeguridad(contexto));

		// AUDITORIA TERMINALES
		get("/tas/api/consulta-auditoria", contexto -> TASAuditoriaController.consultaAuditoria(contexto));
		get("/tas/api/ultima-fecha-auditoria", contexto -> TASAuditoriaController.ultimaFechaAuditoria(contexto));
		post("/tas/api/cierre-inteligente", contexto -> TASAuditoriaController.cierreDBInteligente(contexto));
		post("/tas/api/reintento-cierre-inteligente", contexto -> TASAuditoriaController.reintentoCierreDBInteligente(contexto));
		post("/tas/api/reintento-operaciones-inteligente", contexto -> TASAuditoriaController.reintentoOperacionesDBInteligente(contexto));
		post("/tas/api/e-ticket-largo", contexto -> TASAuditoriaController.generacionTicketLargo(contexto));
		post("/tas/api/e-ticket", contexto -> TASAuditoriaController.generacionTicket(contexto));
		
	}
}
