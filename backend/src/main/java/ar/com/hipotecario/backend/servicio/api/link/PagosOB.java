package ar.com.hipotecario.backend.servicio.api.link;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.link.PagosOB.PagoOB;

import java.math.BigDecimal;
import java.util.List;

public class PagosOB extends ApiObjetos<PagoOB> {

	/* ========== ATRIBUTOS ========== */
	public static class PagoOB extends ApiObjeto {
		public String codigoAdhesion;
		public String ususarioLP;
		public Integer numeroPagina;
		public Integer cantidadPagina;
		public Integer cantidadTotal;
		public Ente ente;
		public List<PagoRealizados> pagoRealizados;
		public List<Conceptos> conceptos;
		public List<Vencimiento> vencimiento;
	}

	/* ========== ATRIBUTOS ========== */
	public static class PagoOriginal {
		public String usuarioLP;
		public String codigoEnte;
		public BigDecimal importe;
		public String idDeuda;
		public String referencia;
		public String codigoConcepto;
		public String identificadorPago;
	}

	public static class ResponsePost extends ApiObjeto {
		public PagoOriginal pagoOriginal;
		public String usuarioLP;
		public String fechaPago;
		public String horaPago;
		public String codigoSeguridad;
		public BigDecimal importe;
		public String numeroSecuencial;
		public String numeroTerminal;
		public String fechaRendicion;
		public Ente ente;
		public Conceptos concepto;
		public List<String> lineasTicket;
	}

	public static class Conceptos {
		public String codigo;
		public String descripcion;
		public String tipoPago;
		public Boolean isIngresoReferencia;
		public Boolean isLongitudReferencia;
		public BigDecimal longitudMinimaTextoReferencia;
		public BigDecimal longitudMaximaTextoReferencia;
		public BigDecimal importeFijo;
		public BigDecimal importeMinimo;
		public BigDecimal importeMaximo;
		public Boolean rango;
		public Boolean ingresoImportes;
	}

	public static class PagoRealizados {
		public String idDeuda;
		public String fecha;
		public String referencia;
		public String codigoSeguridad;
		public BigDecimal importe;
		public String numeroSecuencia;
		public String numeroTerminal;
		public Conceptos conceptos;
	}

	public static class Rubro {
		public String codigo;
		public String descripcion;
		public String descripcionAbreviada;
	}

	public static class Ente {
		public String codigo;
		public String descripcion;
		public Boolean isBaseDeuda;
		public Boolean isMultipleConcepto;
		public Boolean isIngresoReferencia;
		public Boolean isIngresoImporte;
		public Boolean isHabilitado;
		public Rubro rubro;
	}

	public static class Vencimiento {
		public String id;
		public String fecha;
		public BigDecimal importe;
		public String textoIngreso;
		public Conceptos concepto;
		public String idDeuda;
		public String idDeudaPago;
	}

	public static class CpeValido extends ApiObjeto {
		public String cpevalido;
	}

	/* =============== SERVICIOS ================ */
	// API-Link_ConsultaPagosEfectuados-PagosServicios
	public static PagoOB get(Contexto contexto, String numeroTarjeta, String usuarioLP, String codigoEnte, String fechaDesde, String fechaHasta) {
		ApiRequest request = new ApiRequest("LinkGetPagos", "link", "GET", "/v1/servicios/{numeroTarjeta}/pagos", contexto);
		request.path("numeroTarjeta", numeroTarjeta);
		request.query("usuarioLP", usuarioLP);
		request.query("codigoEnte", codigoEnte);
		request.query("paginaActual", "1");
		request.query("cantidadPagina", "100");
		request.query("fechaDesde", fechaDesde);
		request.query("fechaHasta", fechaHasta);

		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 201), request, response);
		return response.crear(PagoOB.class);
	}

	// API-Link_ConsultaPagoPendientes-PagosServicios
	public static PagosOB getPendientes(Contexto contexto, String numeroTarjeta) {
		ApiRequest request = new ApiRequest("LinkGetPagosPendientes", "link", "GET", "/v2/servicios/{numeroTarjeta}/pagos/pendientes", contexto);
		request.path("numeroTarjeta", numeroTarjeta);

		request.cache = false;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(PagosOB.class);
	}

	// API-Link_ConsultaPagoStatus-PagosServicios
	// FALTA DATOS PARA MAPEAR
	public static PagoOB getStatus(Contexto contexto, String identificadorPago, String timeStampTransaccion) {
		ApiRequest request = new ApiRequest("LinkGetPagosPendientes", "link", "GET", "/v/servicios/pagos/status", contexto);
		request.query("identificadorPago", identificadorPago);
		request.query("timeStampTransaccion", timeStampTransaccion);

		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(PagoOB.class);
	}

	// API-Link_CrearAdhesion-PagosServicios
	public static ResponsePost post(Contexto contexto, String numeroTarjeta, String idMoneda, String numeroCuenta, String tipoCuenta, String codigoConcepto, String codigoEnte, String identificadorPago, String referencia, String usuarioLP, String importe, String idBase) {
		ApiRequest request = new ApiRequest("LinkPostPago", "link", "POST", "/v1/servicios/{numeroTarjeta}/pagos", contexto);
		request.path("numeroTarjeta", numeroTarjeta);
		request.body("idMoneda", idMoneda);

		Objeto bodyCuenta = new Objeto();
		bodyCuenta.set("numero", numeroCuenta);
		bodyCuenta.set("tipo", tipoCuenta);
		request.body("cuenta", bodyCuenta);

		Objeto bodyPago = new Objeto();
		bodyPago.set("codigoConcepto", codigoConcepto);
		bodyPago.set("codigoEnte", codigoEnte);
		bodyPago.set("identificadorPago", identificadorPago);
		bodyPago.set("referencia", referencia);
		bodyPago.set("usuarioLP", usuarioLP);
		bodyPago.set("importe", importe);
		bodyPago.set("idDeuda", idBase);

		request.body("pago", bodyPago);

		ApiResponse response = request.ejecutar();
		ApiException.throwIf("OPERACION_RECHAZADA", response.codigoHttp.equals(404), request, response);
		ApiException.throwIf(!response.http(200), request, response);

		return response.crear(ResponsePost.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		String method = "getPendientes";

		if (method.equals("get")) {
			PagoOB datos = get(contexto, "4998590211782905", "0002081", "919", "01082019", "26022020");
			imprimirResultado(contexto, datos);
		}

		if (method.equals("getPendientes")) {
			PagosOB datos = getPendientes(contexto, "5046200441045200");
			imprimirResultado(contexto, datos);
		}

		if (method.equals("getStatus")) {
			PagoOB datos = getStatus(contexto, "", "");
			imprimirResultado(contexto, datos);
		}

		if (method.equals("post")) {
			ResponsePost datos = post(contexto, "4998590015392208", "80", "400400011740843", "AHO", "001", "919", "e8fb11b3-ba99-4d85-8676-40f2ef915766", "0002081", "0002081", "1", "");
			imprimirResultado(contexto, datos);
		}

	}

}
