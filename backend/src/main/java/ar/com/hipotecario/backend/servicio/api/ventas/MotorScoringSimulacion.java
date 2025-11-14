package ar.com.hipotecario.backend.servicio.api.ventas;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.base.Util;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.canal.buhobank.scoring.General;
import ar.com.hipotecario.canal.buhobank.scoring.SolicitudMotor;

public class MotorScoringSimulacion extends ApiObjeto {
	public static String POST_OFERTA_MOTOR_SCORING = "OfertaMotorScoring";
	
	/* ========== ATRIBUTOS ========== */
	public String ResolucionCodigo;
	public String ModoAprobacion;
	public ProductosOfrecidos ProductosOfrecidos;
	
	public static class ProductosOfrecidos extends ApiObjeto {
		public List<TarjetaOfrecida> TarjetasOfrecidas;
	}
	
	public static class TarjetaOfrecida extends ApiObjeto {
		public String Producto;
		public BigDecimal LimiteCompra;
		public String CodigoDistribucion;
		public String Cartera;
		public String GrupoAfinidad;
		public String ModeloLiquidacion;
	}
	
	public String resolucion() {
		return Util.empty(ResolucionCodigo) ? General.RECHAZAR : ResolucionCodigo;
	}
	
	public String modoAprobacion() {
		return Util.empty(ModoAprobacion) ? null : ModoAprobacion;
	}
	
	public Boolean aprobado() {
		return General.APROBAR_VERDE.equals(resolucion());
	}
	
	private Boolean tieneProductos() {
		return !Util.empty(ProductosOfrecidos) && !Util.empty(ProductosOfrecidos.TarjetasOfrecidas) && ProductosOfrecidos.TarjetasOfrecidas.size() > 0;
	}
	
	public String letraTC() {
		if(!tieneProductos()) {
			return "";
		}
		
		return switch (ProductosOfrecidos.TarjetasOfrecidas.get(0).Producto) {
			case "2" -> General.PAQ_LETRA_BUHO_PACK_WHITE;
			case "3" -> General.PAQ_LETRA_GOLD;
			case "543" -> General.PAQ_LETRA_PLAT;
			case "541" -> General.PAQ_LETRA_BLACK;
			default -> "";
		};
	}

	public String codigoTc(boolean esEmprendedor){
		if(esEmprendedor){
			return switch (letraTC()) {
				case "I" -> "47"; //WHITE
				case "P" -> "47"; //GOLD
				case "L" -> "47"; //PLAT
				case "S" -> "53"; //BLACK
				default -> null;
			};
		}
		else{
			return switch (letraTC()) {
				case "I" -> "40"; //WHITE
				case "P" -> "41"; //GOLD
				case "L" -> "42"; //PLAT
				case "S" -> "43"; //BLACK
				default -> null;
			};
		}
	}

	public BigDecimal limiteCompra() {
		return tieneProductos() ? ProductosOfrecidos.TarjetasOfrecidas.get(0).LimiteCompra : new BigDecimal("0.0");
	}
	
	public TarjetaOfrecida tarjetaOfrecida() {
		return tieneProductos() ? ProductosOfrecidos.TarjetasOfrecidas.get(0) : null;
	}
	
	public static MotorScoringSimulacion get(Contexto contexto, String numeroSolicitud, SolicitudMotor datos, boolean esTcv) {
		ApiRequest request = new ApiRequest(POST_OFERTA_MOTOR_SCORING, ApiVentas.API, "POST", "/solicitudes/{numeroSolicitud}/ofertaMotorScoring", contexto);
		request.header(ApiVentas.X_HANDLE, numeroSolicitud);
		request.path("numeroSolicitud", numeroSolicitud);
		request.body("idSolicitud", datos.body.solicitud.idSolicitud);
		request.body("nroInstancia", datos.body.solicitud.nroInstancia);
		request.body("canalVenta", datos.body.solicitud.canalVenta);
		request.body("subCanalVenta", datos.body.solicitud.subCanalVenta);
		request.body("puntoVenta", datos.body.solicitud.puntoVenta);
		request.body("oficialVenta", datos.body.solicitud.oficialVenta);
		request.body("canalTramite", datos.body.solicitud.canalTramite);
		request.body("subCanalTramite", datos.body.solicitud.subCanalTramite);
		request.body("oficialTramite", datos.body.solicitud.oficialTramite);
		request.body("tipoInvocacion", datos.body.solicitud.tipoInvocacion);
		
		List<Objeto> solicitantes = new ArrayList<Objeto>();
		
		Objeto solicitante = new Objeto();
		solicitante.set("nroSolicitante", datos.body.solicitud.solicitantes.solicitante.get(0).nroSolicitante);
		
		Objeto identidad = new Objeto();
		identidad.set("tipoDocumento", datos.body.solicitud.solicitantes.solicitante.get(0).identidad.tipoDocumento);
		identidad.set("nroDocumento", datos.body.solicitud.solicitantes.solicitante.get(0).identidad.nroDocumento);
		identidad.set("versionDNI", datos.body.solicitud.solicitantes.solicitante.get(0).identidad.versionDNI);
		identidad.set("sexo", datos.body.solicitud.solicitantes.solicitante.get(0).identidad.sexo);
		identidad.set("tipoIdTributaria", datos.body.solicitud.solicitantes.solicitante.get(0).identidad.tipoIdTributaria);
		identidad.set("nroIdTributaria", datos.body.solicitud.solicitantes.solicitante.get(0).identidad.nroIdTributaria);
		identidad.set("idCliente", datos.body.solicitud.solicitantes.solicitante.get(0).identidad.idCliente);
		identidad.set("apellido", datos.body.solicitud.solicitantes.solicitante.get(0).identidad.apellido);
		identidad.set("nombres", datos.body.solicitud.solicitantes.solicitante.get(0).identidad.nombres);
		solicitante.set("identidad", identidad);
		
		Objeto datosPersonales = new Objeto();
		datosPersonales.set("fechaNacimiento", datos.body.solicitud.solicitantes.solicitante.get(0).datosPersonales.fechaNacimiento);
		datosPersonales.set("tipoPersona", datos.body.solicitud.solicitantes.solicitante.get(0).datosPersonales.tipoPersona);
		datosPersonales.set("montoAlquiler", datos.body.solicitud.solicitantes.solicitante.get(0).datosPersonales.montoAlquiler);
		datosPersonales.set("unidoHecho", datos.body.solicitud.solicitantes.solicitante.get(0).datosPersonales.unidoHecho);
		solicitante.set("datosPersonales", datosPersonales);
		
		Objeto telemailParticular = new Objeto();
		telemailParticular.set("telCelularPrefijo", datos.body.solicitud.solicitantes.solicitante.get(0).telemailParticular.telCelularPrefijo);
		telemailParticular.set("telCelularCaract", datos.body.solicitud.solicitantes.solicitante.get(0).telemailParticular.telCelularCaract);
		telemailParticular.set("telCelularNro", datos.body.solicitud.solicitantes.solicitante.get(0).telemailParticular.telCelularNro);
		telemailParticular.set("email", datos.body.solicitud.solicitantes.solicitante.get(0).telemailParticular.email);
		telemailParticular.set("tieneMail", datos.body.solicitud.solicitantes.solicitante.get(0).telemailParticular.tieneMail);
		solicitante.set("telemailParticular", telemailParticular);
		
		Objeto buhoBank = new Objeto();
		buhoBank.set("ingresoNeto", datos.body.solicitud.solicitantes.solicitante.get(0).buhoBank.ingresoNeto);
		solicitante.set("buhoBank", buhoBank);
		
		solicitante.set("esSujetoObligado", datos.body.solicitud.solicitantes.solicitante.get(0).esSujetoObligado);
		solicitante.set("solicitaValidarIdentidad", datos.body.solicitud.solicitantes.solicitante.get(0).solicitaValidarIdentidad);
		solicitante.set("esPlanSueldo", datos.body.solicitud.solicitantes.solicitante.get(0).esPlanSueldo);
		solicitante.set("solicitaEvaluarMercadoAbierto", datos.body.solicitud.solicitantes.solicitante.get(0).solicitaEvaluarMercadoAbierto);
		
		solicitantes.add(solicitante);
		request.body("solicitantes", solicitantes);
		
		Objeto productosSolicitados = new Objeto();
		List<Objeto> cuentasSolicitadas = new ArrayList<Objeto>();
		Objeto cuentaSolicitada = new Objeto();
		
		List<Objeto> rolesSolicitantes = new ArrayList<Objeto>();
		Objeto rolSolicitante = new Objeto();
		rolSolicitante.set("nroSolicitante", datos.body.solicitud.productosSolicitados.cuentasSolicitadas.cuentaSolicitada.get(0).rolesSolicitantes.rolSolicitante.get(0).nroSolicitante);
		rolSolicitante.set("rolSolicitante", datos.body.solicitud.productosSolicitados.cuentasSolicitadas.cuentaSolicitada.get(0).rolesSolicitantes.rolSolicitante.get(0).rolSolicitante);
		rolesSolicitantes.add(rolSolicitante);
		
		cuentaSolicitada.set("rolesSolicitantes", rolesSolicitantes);
		cuentaSolicitada.set("tipoCuenta", datos.body.solicitud.productosSolicitados.cuentasSolicitadas.cuentaSolicitada.get(0).tipoCuenta);
		cuentaSolicitada.set("nro", datos.body.solicitud.productosSolicitados.cuentasSolicitadas.cuentaSolicitada.get(0).nro);
		cuentaSolicitada.set("montoAlta", datos.body.solicitud.productosSolicitados.cuentasSolicitadas.cuentaSolicitada.get(0).montoAlta);
		cuentaSolicitada.set("flagPaquete", datos.body.solicitud.productosSolicitados.cuentasSolicitadas.cuentaSolicitada.get(0).flagPaquete);
		cuentasSolicitadas.add(cuentaSolicitada);
		productosSolicitados.set("cuentasSolicitadas", cuentasSolicitadas);
		
		
		List<Objeto> tarjetasSolicitadas = new ArrayList<Objeto>();
		Objeto tarjetaSolicitada = new Objeto();
		
		List<Objeto> rolesSolicitantesTarjeta = new ArrayList<Objeto>();
		Objeto rolSolicitanteTarjeta = new Objeto();
		rolSolicitanteTarjeta.set("nroSolicitante", datos.body.solicitud.productosSolicitados.tarjetasSolicitadas.tarjetaSolicitada.get(0).rolesSolicitantes.rolSolicitante.get(0).nroSolicitante);
		rolSolicitanteTarjeta.set("rolSolicitante", datos.body.solicitud.productosSolicitados.tarjetasSolicitadas.tarjetaSolicitada.get(0).rolesSolicitantes.rolSolicitante.get(0).rolSolicitante);
		rolesSolicitantesTarjeta.add(rolSolicitanteTarjeta);
		
		tarjetaSolicitada.set("rolesSolicitantes", rolesSolicitantesTarjeta);
		tarjetaSolicitada.set("tipoTarjeta", datos.body.solicitud.productosSolicitados.tarjetasSolicitadas.tarjetaSolicitada.get(0).tipoTarjeta);
		tarjetaSolicitada.set("nro", datos.body.solicitud.productosSolicitados.tarjetasSolicitadas.tarjetaSolicitada.get(0).nro);
		tarjetaSolicitada.set("solicitaPrimeraCompra", datos.body.solicitud.productosSolicitados.tarjetasSolicitadas.tarjetaSolicitada.get(0).solicitaPrimeraCompra);
		tarjetaSolicitada.set("montoSolicitado", datos.body.solicitud.productosSolicitados.tarjetasSolicitadas.tarjetaSolicitada.get(0).montoSolicitado);
		tarjetaSolicitada.set("montoAlta", datos.body.solicitud.productosSolicitados.tarjetasSolicitadas.tarjetaSolicitada.get(0).montoAlta);
		tarjetaSolicitada.set("flagPaquete", datos.body.solicitud.productosSolicitados.tarjetasSolicitadas.tarjetaSolicitada.get(0).flagPaquete);
		if(esTcv) tarjetaSolicitada.set("esVirtual", datos.body.solicitud.productosSolicitados.tarjetasSolicitadas.tarjetaSolicitada.get(0).esVirtual);
		tarjetasSolicitadas.add(tarjetaSolicitada);
		productosSolicitados.set("tarjetasSolicitadas", tarjetasSolicitadas);
		
		request.body("productosSolicitados", productosSolicitados);
		request.body("flagSimulacion", datos.body.solicitud.flagSimulacion);
		request.body("flagRutaConPactado", datos.body.solicitud.flagRutaConPactado);
		request.body("flagSolicitaAprobacionEstandar", datos.body.solicitud.flagSolicitaAprobacionEstandar);
		request.body("flagSolicitaComprobarIngresos", datos.body.solicitud.flagSolicitaComprobarIngresos);
		request.body("flagSolicitaAprobacionCentralizada", datos.body.solicitud.flagSolicitaAprobacionCentralizada);
		request.body("flagSolicitaExcepcionChequeoFinal", datos.body.solicitud.flagSolicitaExcepcionChequeoFinal);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(ApiVentas.errorVentas(response), request, response);
		return response.crear(MotorScoringSimulacion.class, response.objetos(ApiVentas.DATOS).get(0));
	}

}
