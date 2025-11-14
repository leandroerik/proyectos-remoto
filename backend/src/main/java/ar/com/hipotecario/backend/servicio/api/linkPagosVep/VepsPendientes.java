package ar.com.hipotecario.backend.servicio.api.linkPagosVep;

import java.math.BigDecimal;
import java.util.List;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.productos.Cuentas.Cuenta;

public class VepsPendientes extends ApiObjeto {

	/* ========== ATRIBUTOS ========== */
	public Integer paginaSiguiente;
	public Integer cantidad;
	public List<Vep> veps;

	public static class Vep extends ApiObjeto {
		public String nroVep;
		public String token;
		public InformacionVep informacionVep;
	}

	public static class InformacionesVep {
		public InformacionVep informacionVep;
	}

	public static class Usuario {
		public String idTributario;
	}

	public static class Autorizante {
		public String idTributario;
	}

	public static class Contribuyente {
		public String idTributario;
	}

	public static class Obligacion {
		public String impuesto;
		public String impuestoDesc;
		public BigDecimal importe;
	}

	public static class Detalle {
		public String campo;
		public String campoTipo;
		public String campoDesc;
		public Fecha contenido;
		public String contenidoDesc;
	}

	public static class InformacionVep {
		public String nroVEP;
		public Fecha fechaHoraCreacion;
		public Fecha fechaExpiracion;
		public String nroFormulario;
		public String orgRecaudDesc;
		public String codTipoPago;
		public String pagoDesc;
		public String pagoDescExtracto;
		public Usuario usuario;
		public Autorizante autorizante;
		public Contribuyente contribuyente;
		public String establecimiento;
		public String concepto;
		public String conceptoDesc;
		public String subConcepto;
		public String subConceptoDesc;
		public String periodoFiscal;
		public String anticipoCuota;
		public BigDecimal importe;
		public List<Obligacion> obligaciones;
		public List<Detalle> detalles;
	}

	/* ========== SERVICIOS ========== */
	public static VepsPendientes get(Contexto contexto, String idTributarioCliente, String idTributarioContribuyente, String idTributarioOriginante, String maxCantidad, String numeroTarjeta, String tipoConsultaLink) {
		return get(contexto, null, null, idTributarioCliente, idTributarioContribuyente, null, idTributarioOriginante, null, numeroTarjeta, null, null, tipoConsultaLink);
	}

	public static VepsPendientes get(Contexto contexto, Fecha fechaDesde, Fecha fechaHasta, String idTributarioCliente, String idTributarioContribuyente, String idTributarioEmpresa, String idTributarioOriginante, String maxCantidad, String numeroTarjeta, String numeroVep, String pagina, String tipoConsultaLink) {
		ApiRequest request = new ApiRequest("LinkGetVepsPendientes", "veps", "GET", "/v1/veps/{idTributarioCliente}/pendientes", contexto);
		request.path("idTributarioCliente", idTributarioCliente);
		request.query("fechaDesde", fechaDesde == null ? null : fechaDesde.string("yyyy-MM-dd"));
		request.query("fechaHasta", fechaHasta == null ? null : fechaHasta.string("yyyy-MM-dd"));
		request.query("idTributarioContribuyente", idTributarioContribuyente);
		request.query("idTributarioEmpresa", idTributarioEmpresa);
		request.query("idTributarioOriginante", idTributarioOriginante);
		request.query("maxCantidad", maxCantidad);
		request.query("numeroTarjeta", numeroTarjeta);
		request.query("numeroVep", numeroVep);
		request.query("pagina", pagina);
		request.query("tipoConsultaLink", tipoConsultaLink);
		request.cache = false;

		Object[] parametros = new Object[12];
		parametros[0] = idTributarioCliente;
		parametros[1] = fechaDesde == null ? null : fechaDesde.string("yyyy-MM-dd");
		parametros[2] = fechaHasta == null ? null : fechaHasta.string("yyyy-MM-dd");
		parametros[3] = idTributarioContribuyente;
		parametros[4] = idTributarioEmpresa;
		parametros[5] = idTributarioOriginante;
		parametros[6] = idTributarioEmpresa;
		parametros[7] = maxCantidad;
		parametros[8] = numeroTarjeta;
		parametros[9] = numeroVep;
		parametros[10] = pagina;
		parametros[11] = tipoConsultaLink;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204, 404), request, response);
		VepsPendientes vepsPendientes = response.crear(VepsPendientes.class);
		return vepsPendientes;
	}

	public static VepsPendientes getNuevo(Contexto contexto, Fecha fechaDesde, Fecha fechaHasta, String idTributarioCliente, String idTributarioContribuyente, String idTributarioEmpresa, String idTributarioOriginante, String maxCantidad, String numeroTarjeta, String numeroVep, String pagina, String tipoConsultaLink) {
		ApiRequest request = new ApiRequest("LinkGetVepsPendientes", "veps", "GET", "/v1/veps/{idTributarioCliente}/pendientes", contexto);
		request.path("idTributarioCliente", idTributarioCliente);
		request.query("fechaDesde", fechaDesde == null ? null : fechaDesde.string("yyyy-MM-dd"));
		request.query("fechaHasta", fechaHasta == null ? null : fechaHasta.string("yyyy-MM-dd"));
		request.query("idTributarioContribuyente", idTributarioContribuyente);
		request.query("idTributarioEmpresa", idTributarioEmpresa);
		request.query("idTributarioOriginante", idTributarioOriginante);
		request.query("maxCantidad", maxCantidad);
		request.query("numeroTarjeta", numeroTarjeta);
		request.query("numeroVep", numeroVep);
		request.query("pagina", pagina);
		request.query("tipoConsultaLink", tipoConsultaLink);
		request.cache = false;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);

		ApiException.throwIf("NO_HAY_VEPS_PENDIENTES", response.contains("su respuesta no tiene ningún contenido"), request, response);
		ApiException.throwIf("NO_HAY_VEPS_PENDIENTES", response.codigoHttp.equals(204), request, response);
		ApiException.throwIf("NO_SE_ENCONTRO", response.codigoHttp.equals(404), request, response);
		return response.crear(VepsPendientes.class);
	}

	public static ApiObjeto delete(Contexto contexto, String idTributarioCliente, String numeroTarjeta, String numeroVep) {
		ApiRequest request = new ApiRequest("LinkDeleteVeps", "veps", "DELETE", "/v1/veps", contexto);
		request.query("idTributarioCliente", idTributarioCliente);
		request.query("numeroTarjeta", numeroTarjeta);
		request.query("numeroVep", numeroVep);

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(ApiObjeto.class, response);
	}

	public static VepsPagados.Vep post(Contexto contexto, String idTributarioCliente, String idTributarioEmpresa, String numeroTarjeta, String numeroVep, String idTributarioContribuyente, BigDecimal importe, String token, Cuenta cuenta) {

		Objeto cuentaLink = new Objeto().set("numero", cuenta.numeroProducto).set("tipo", cuenta.tipoProducto).set("moneda", new Objeto().set("id", cuenta.moneda));

		ApiRequest request = new ApiRequest("LinkPostPagoVeps", "veps", "POST", "/v1/pagoVeps", contexto);
		request.body("cliente", new Objeto().set("idTributario", idTributarioCliente));
		request.body("empresa", new Objeto().set("idTributario", idTributarioEmpresa));
		request.body("tarjetaDebito", new Objeto().set("numero", numeroTarjeta));
		request.body("numeroVep", numeroVep);
		request.body("contribuyente", new Objeto().set("idTributario", idTributarioContribuyente));
		request.body("importe", importe);
		request.body("token", token);
		request.body("cuenta", cuentaLink);

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(VepsPagados.Vep.class, response);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		String prueba = "get";
		if ("get".equals(prueba)) {
			Contexto contexto = contexto("HB", "homologacion");
			VepsPendientes datos = get(contexto, null, null, "27254406614", null, "30539222259", "20105176512", null, "5046200441112559", null, null, "1");
			imprimirResultado(contexto, datos);
		}
		if ("post".equals(prueba)) {
			Cuenta cuenta = new Cuenta();
			cuenta.numeroProducto = "400400011740843";
			cuenta.tipoProducto = "AHO";
			cuenta.moneda = "80";
			Contexto contexto = contexto("HB", "homologacion");
			VepsPagados.Vep vepPagado = post(contexto, "20105176512", "20105176512", "4998590266707708", "54693708", "20105176512", new BigDecimal(30), "a150e5cf-0255-4435-9d38-485db4a2a249", cuenta);
			imprimirResultado(contexto, vepPagado);
		}
		if ("delete".equals(prueba)) {
			Contexto contexto = contexto("HB", "homologacion");
			delete(contexto, "20105176512", "4998590266707708", "11111");
		}
	}
}
