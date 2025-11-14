package ar.com.hipotecario.backend.servicio.api.plazosfijos;

import java.math.BigDecimal;

import org.json.JSONObject;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.Parametros;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.plazosfijos.Cedips.Cedip.CedipNuevo;
import ar.com.hipotecario.backend.servicio.api.plazosfijos.PlazosFijos.PlazoFijo;
import ar.com.hipotecario.backend.servicio.api.plazosfijos.PlazosFijos.PlazoFijo.RequestPost;
import ar.com.hipotecario.backend.servicio.api.plazosfijos.PlazosFijos.PlazoFijo.ResponsePost;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.inversiones.PlazoFijoOB;

public class PlazosFijos extends ApiObjetos<PlazoFijo> {

	/* ========== ATRIBUTOS ========== */
	public static class PlazoFijo extends ApiObjeto {
		public String cancelacionAnticipada;
		public String certificado;
		public BigDecimal cotizacion;
		public String cuota;
		public String descEstado;
		public String descripcionMoneda;
		public String estado;
		public String fechaCarga;
		public String fechaDesde;
		public String fechaDesdeCancelacionAnt;
		public String fechaFin;
		public String fechaHastaCancelacionAnt;
		public String fechaIndiceUVA;
		public String garantizado;
		public String indiceAjusteUVA;
		public Integer moneda;
		public BigDecimal monto;
		public BigDecimal montoEstimado;
		public BigDecimal montoPesos;
		public BigDecimal montoXImpuestos;
		public String nroCertificado;
		public String nroCuenta;
		public Integer nroRenovacion;
		public Integer oficina;
		public Integer operacion;
		public String planAhorro;
		public Integer plazo;
		public String renovacion;
		public Integer renovacionesPend;
		public String renueva;
		public String renuevaInteres;
		public String rol;
		public BigDecimal tasaEfectiva;
		public BigDecimal tasaNominal;
		public BigDecimal teaCancelacionAnt;
		public String tipoCertificado;
		public BigDecimal tnaCancelacionAnt;
		public BigDecimal valorIndiceUVA;
		public BigDecimal valorUVA;
		public Integer idOperacion;
		public String nroPlazoFijo;

		public Fecha fechaCarga() {
			return new Fecha(fechaCarga, "yyyy-MM-dd'T'hh:mm:ss");
		}

		public Fecha fechaDesde() {
			return new Fecha(fechaDesde, "yyyy-MM-dd'T'hh:mm:ss");
		}

		public Fecha fechaDesdeCancelacionAnt() {
			return new Fecha(fechaDesdeCancelacionAnt, "dd/MM/yyyy");
		}

		public Fecha fechaFin() {
			return new Fecha(fechaFin, "yyyy-MM-dd'T'hh:mm:ss");
		}

		public Fecha fechaHastaCancelacionAnt() {
			return new Fecha(fechaHastaCancelacionAnt, "dd/MM/yyyy");
		}

		public Fecha fechaIndiceUVA() {
			return new Fecha(fechaIndiceUVA, "yyyy-MM-dd'T'hh:mm:ss");
		}

		public static class RequestPost extends ApiObjeto {
			String canal;
			String capInteres;
			String cuenta;
			Integer idPlanAhorro;
			Integer idcliente;
			Integer moneda;
			BigDecimal monto;
			Integer nroOperacion;
			Integer periodo;
			Integer plazo;
			String renova;
			String reverso;
			String tipoCuenta;
			String tipoOperacion;
			String usuarioAlta;
		}

		public static class ResponsePost extends ApiObjeto {
			String cancelacionAnticipada;
			BigDecimal capital;
			String cubiertoPorGarantia;
			String cuenta;
			String cuotas;
			String diaDePago;
			String fechaActual;
			String fechaDesdeCancelacionAnt;
			String fechaHastaCancelacionAnt;
			String fechaPagoIntereses;
			String fechaVencimiento;
			Integer idOperacion;
			BigDecimal impuestos;
			BigDecimal interesEstimado;
			Integer moneda;
			BigDecimal monto;
			String nroPlazoFijo;
			Integer plazo;
			String producto;
			BigDecimal tasa;
			BigDecimal tasaCancelacionAnt;
			BigDecimal teaCancelacionAnt;
			String tipoOperacion;
			BigDecimal tnaCancelacionAnt;
			BigDecimal totalInteresEstimado;
		}

		public static class RespuestaOk extends ApiObjeto {
			public Boolean ok;
		}

	}

	public static class PlazoFijoPF extends ApiObjetos<ItemPF> {
	}

	public static class ItemPF extends ApiObjeto {
		Fecha fechaIngreso;
		BigDecimal impuesto;
		Fecha fechaValor;
		Boolean cubiertoPorGarantia;
		String oficina;
		BigDecimal importePesos;
		String numeroBanco;
		BigDecimal tasaEfectiva;
		String cuenta;
		BigDecimal cotizacion;
		String idEstado;
		BigDecimal tasa;
		String rol;
		Integer renovaciones;
		Boolean renuevaIntereses;
		Integer cantidadDias;
		Boolean renueva;
		BigDecimal montoUva;
		String descMoneda;
		BigDecimal valorIndice;
		String descEstado;
		String tipoOperacion;
		String idMoneda;
		Fecha fechaVencimiento;
		String importe;
		String nroOperacion;
		BigDecimal interesEstimado;
		String descOperacion;
		Boolean renovacionAutomatica;
		Fecha fechaCancelacion;
		Fecha fechaActivacion;
		String cancelacionAnticipada;
		BigDecimal tnaCancelacionAnt;
		BigDecimal teaCancelacionAnt;
		Fecha fechaDesdeCancelacionAnt;
		Fecha fechaHastaCancelacionAnt;
	}

	/* ========== SERVICIOS ========== */
	public static PlazosFijos get(Contexto contexto, String idCobis, String nroOperacion) {
		ApiRequest request = new ApiRequest("PlazosFijosGet", "plazosfijos", "GET", "/v1/{idCobis}", contexto);
		request.path("idCobis", idCobis);
		request.query("fechaInicio", Fecha.hoy().string("dd/MM/yyyy"));
		request.query("fechaFin", Fecha.hoy().string("dd/MM/yyyy"));
		request.query("certificado", nroOperacion);

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(PlazosFijos.class);
	}

	public static PlazosFijos getBajaPlanAhorro(Contexto contexto, String idCobis, String planContratado) {
		ApiRequest request = new ApiRequest("PlazosFijosGetBajaPlanAhorro", "plazosfijos", "GET", "/v1/{idCobis}/bajarPlazoFijoAhorro", contexto);
		request.path("idCobis", idCobis);
		request.query("planContratado", planContratado);

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(PlazosFijos.class);
	}

	public static PlazosFijos getHistoricos(Contexto contexto, String idCobis, String nroOperacion) {
		ApiRequest request = new ApiRequest("PlazosFijosGet", "plazosfijos", "GET", "/v1/{idCobis}/historicos", contexto);
		request.path("idCobis", idCobis);
		request.query("fechaInicio", Fecha.hoy().string("dd/MM/yyyy"));
		request.query("fechaFin", Fecha.hoy().string("dd/MM/yyyy"));
		request.query("certificado", nroOperacion);

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(PlazosFijos.class);
	}

	public static ResponsePost post(Contexto contexto, RequestPost requestPost) {
		ApiRequest request = new ApiRequest("PlazosFijosPost", "plazosfijos", "POST", "/v1/plazoFijos", contexto);
		request.body(requestPost.objeto());

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(ResponsePost.class);
	}

	// API-PlazoFijo_ConsultaPlazosFijos
	public static PlazoFijoPF getPf(Contexto contexto, String nroPf) {
		ApiRequest request = new ApiRequest("PlazosFijosGetDetalle", "plazosfijos", "GET", "/v1/plazosfijos/{nropf}", contexto);
		request.path("nropf", nroPf);

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(PlazoFijoPF.class);
	}
	
	public static PlazoFijo post(Contexto contexto, PlazoFijoOB plazoFijo) {
		ApiRequest request = new ApiRequest("CedipPost", "plazosfijos", "POST", "/v1/plazoFijos", contexto);
		
		Objeto objeto = new Objeto();
        objeto.set("canal", plazoFijo.canal);
        objeto.set("capInteres", plazoFijo.capInteres);
        objeto.set("cuenta", plazoFijo.cuenta);
        objeto.set("idPlanAhorro", plazoFijo.idPlanAhorro);
        objeto.set("idcliente", plazoFijo.idcliente);
        objeto.set("moneda", plazoFijo.moneda.id);
        objeto.set("monto", plazoFijo.monto);
        objeto.set("nroOperacion", plazoFijo.nroOperacion);
        objeto.set("periodo", plazoFijo.periodo);
        objeto.set("plazo", plazoFijo.plazo);
        objeto.set("renova", plazoFijo.renova);
        objeto.set("reverso", plazoFijo.reverso);
        objeto.set("tipoCuenta", plazoFijo.tipoCuenta);
        objeto.set("tipoOperacion", plazoFijo.tipoOperacion);
        objeto.set("usuarioAlta", plazoFijo.usuarioAlta);
		
        request.body(objeto);

		ApiResponse response = request.ejecutar();
		
		// Validar el error 404
		String mensajeError = "0";
		if(response.http(404) && response.body.contains("codigo")) {
			JSONObject jsonObject = new JSONObject(response.body);
			mensajeError = (String) jsonObject.get("mensajeAlUsuario");
		}
		
		ApiException.throwIf(mensajeError, !response.http(200), request, response);
		return response.crear(PlazoFijo.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		String test = "getPF";
		Contexto contexto = contexto("HB", "homologacion");

		if ("get".equals(test)) {
			PlazosFijos datos = get(contexto, "135706", "00408000110233518");
			System.out.println(datos.get(0).fechaCarga());
			imprimirResultado(contexto, datos);
		}

		if ("post".equals(test)) {
			RequestPost requestPost = new RequestPost();
			requestPost.canal = "3";
			requestPost.capInteres = "";
			requestPost.cuenta = "";
			requestPost.idPlanAhorro = 1;
			requestPost.idcliente = 1;
			requestPost.moneda = 80;
			requestPost.monto = new BigDecimal(5);
			requestPost.nroOperacion = null;
			requestPost.periodo = null;
			requestPost.plazo = null;
			requestPost.renova = "N";
			requestPost.reverso = null;
			requestPost.tipoCuenta = "AHO";
			requestPost.tipoOperacion = "1";
			requestPost.usuarioAlta = "1";
			post(contexto, requestPost);
		}

		if ("historicos".equals(test)) {
			PlazosFijos datos = getHistoricos(contexto, "135706", "00408000110233518");
			imprimirResultado(contexto, datos);
		}

		if ("getPF".equals(test)) {
			PlazoFijoPF datos = getPf(contexto, "04308000420000012");
			imprimirResultado(contexto, datos);
		}
	}
}
