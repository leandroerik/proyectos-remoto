package ar.com.hipotecario.backend.servicio.api.link;

import java.math.BigDecimal;
import java.util.List;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.productos.Cuentas.Cuenta;

public class OrdenesExtraccion extends ApiObjeto {
	public Integer paginasTotales;
	public List<OrdenExtraccion> odesGeneradas;

	public static class OrdenExtraccion extends ApiObjeto {
		public OdeGenerada odeGenerada;

		public static class OdeGenerada extends ApiObjeto {
			public String pin;
			public BigDecimal importe;
			public String referencia;
			public String estadoODE;
			public Cuenta cuenta;
			public Documento documento;
			public String fechaCreacion;
			public String horaCreacion;
			public String fechaVencimiento;
			public String numeroSecuencia;

			public Fecha fechaCreacion() {
				return new Fecha(fechaCreacion + " " + horaCreacion, "ddMMyyyy hhMMss");
			}

			public Fecha fechaVencimiento() {
				return new Fecha(horaCreacion, "ddMMyyyy");
			}

			public static class Cuenta extends ApiObjeto {
				public String tipoCuenta;
				public String cuentaPBF;
			}

			public static class Documento extends ApiObjeto {
				public String tipoDocumento;
				public String descripcionDocumento;
				public String numeroDocumento;
			}
		}

	}

	public static class Documentos extends ApiObjeto {
		public List<OrdenExtraccion.OdeGenerada.Documento> odesGeneradas;
	}

	public static class Referencias extends ApiObjeto {
		public List<Referencia> referencias;

		public static class Referencia extends ApiObjeto {
			public String referencia;
		}
	}

	public static class TiposDocumentos extends ApiObjeto {
		public List<TipoDocumento> referencias;

		public static class TipoDocumento extends ApiObjeto {
			public String tipoDocumento;
			public String descripcionDocumento;
		}
	}

	public static class RespuestaOk extends ApiObjeto {
		public Boolean ok;
	}

	public static OrdenesExtraccion get(Contexto contexto, String numeroTarjetaDebito, Fecha fechaDesde, Fecha fechaHasta) {
		ApiRequest request = new ApiRequest("ListarOdes", "link", "GET", "/v1/puntoefectivo/{numeroTarjeta}/odes", contexto);
		request.path("numeroTarjeta", numeroTarjetaDebito);
		request.query("paginaActual", "1");
		request.query("cantidadPorPagina", "100");
		request.query("fechaDesde", fechaDesde.string("ddMMyyyy"));
		request.query("fechaHasta", fechaHasta.string("ddMMyyyy"));

		Object[] parametros = new Object[5];
		parametros[0] = numeroTarjetaDebito;
		parametros[1] = "1";
		parametros[2] = "100";
		parametros[3] = fechaDesde.string("ddMMyyyy");
		parametros[4] = fechaHasta.string("ddMMyyyy");

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204, 404), request, response);

		OrdenesExtraccion ordenesExtracion = response.crear(OrdenesExtraccion.class);
		return ordenesExtracion;

	}

	public static Documentos getDocumentos(Contexto contexto, String numeroTarjetaDebito, String tipoDocumento) {
		ApiRequest request = new ApiRequest("DocumentosOdes", "link", "GET", "/v1/puntoefectivo/{numeroTarjeta}/documentos", contexto);
		request.path("numeroTarjeta", numeroTarjetaDebito);
		request.query("tipodoc", tipoDocumento);

		Object[] parametros = new Object[2];
		parametros[0] = numeroTarjetaDebito;
		parametros[1] = tipoDocumento;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204, 404), request, response);

		Documentos documentos = response.crear(Documentos.class);
		return documentos;
	}

	public static Referencias getReferencias(Contexto contexto, String numeroTarjetaDebito) {
		ApiRequest request = new ApiRequest("ReferenciasOdes", "link", "GET", "/v1/puntoefectivo/{numeroTarjeta}/referencias", contexto);
		request.path("numeroTarjeta", numeroTarjetaDebito);

		Object[] parametros = new Object[1];
		parametros[0] = numeroTarjetaDebito;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204, 404), request, response);

		Referencias referencias = response.crear(Referencias.class);
		return referencias;
	}

	public static TiposDocumentos getTiposDocumentos(Contexto contexto, String numeroTarjetaDebito) {
		ApiRequest request = new ApiRequest("tiposDocumentosOdes", "link", "GET", "/v1/puntoefectivo/{numeroTarjeta}/tiposdocumentos", contexto);
		request.path("numeroTarjeta", numeroTarjetaDebito);

		Object[] parametros = new Object[1];
		parametros[0] = numeroTarjetaDebito;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204, 404), request, response);

		TiposDocumentos tiposDocumentos = response.crear(TiposDocumentos.class);
		return tiposDocumentos;
	}

	public static RespuestaOk post(Contexto contexto, String numeroTarjeta, BigDecimal importe, String numeroDocumento, String nombreCompleto, Cuenta cuenta) {

		Integer montoMinimoOrdenExtraccion = contexto.config.integer("monto_minimo_orden_extraccion", 500);
		Integer montoMaximoOrdenExtraccion = contexto.config.integer("monto_maximo_orden_extraccion", 10000);
		Integer stepOrdenExtraccion = contexto.config.integer("step_orden_extraccion", 100);

		ApiException.throwIf("ERROR_MONTO_MINIMO_ODE", importe.compareTo(new BigDecimal(montoMinimoOrdenExtraccion)) < 0, null, null);
		ApiException.throwIf("ERROR_MONTO_MINIMO_ODE", importe.compareTo(new BigDecimal(montoMaximoOrdenExtraccion)) > 0, null, null);
		ApiException.throwIf("ERROR_STEP_ODE", importe.intValue() % stepOrdenExtraccion != 0, null, null);

		ApiRequest request = new ApiRequest("CrearOde", "link", "POST", "/v1/puntoefectivo/{numeroTarjeta}/ode", contexto);
		request.path("numeroTarjeta", numeroTarjeta);
		request.body("importe", importe.setScale(2));
		request.body("tipoDocumento", "1");
		request.body("numeroDocumento", numeroDocumento);
		request.body("referencia", nombreCompleto);
		request.body("pin", "");
		if (cuenta.esCajaAhorro() && cuenta.esPesos()) {
			request.body("tipoCuenta", "11");
		}
		if (cuenta.esCuentaCorriente() && cuenta.esPesos()) {
			request.body("tipoCuenta", "01");
		}
		if (cuenta.esCajaAhorro() && cuenta.esDolares()) {
			request.body("tipoCuenta", "07");
		}
		request.body("cuentaPBF", cuenta.numeroProducto);

		ApiResponse response = request.ejecutar();
		ApiException.throwIf("NUMERO_DOCUMENTO_INVALIDO", response.codigo("602"), request, response);
		ApiException.throwIf("IMPORTE_INVALIDO", response.codigo("114"), request, response);
		ApiException.throwIf("LIMITE_DIARIO", response.codigo("B1"), request, response);
		ApiException.throwIf("LIMITE_CANTIDAD_DIARIA", response.codigo("B2"), request, response);
		ApiException.throwIf(!response.http(200), request, response);

		return response.crear(RespuestaOk.class);
	}

	public static RespuestaOk delete(Contexto contexto, String numeroTarjetaDebito, String idODE) {
		ApiRequest request = new ApiRequest("EliminarOde", "link", "DELETE", "/v1/puntoefectivo/{numeroTarjeta}/ode", contexto);
		request.path("numeroTarjeta", numeroTarjetaDebito);
		request.query("idOde", idODE);

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);

		return response.crear(RespuestaOk.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		String prueba = "getTiposDocumentos";

		if ("get".equals(prueba)) {
			Contexto contexto = contexto("HB", "homologacion");
			OrdenesExtraccion datos = get(contexto, "4998590237644808", new Fecha("25022021", "ddMMyyyy"), new Fecha("25082021", "ddMMyyyy"));
			imprimirResultado(contexto, datos);
		}

		if ("getDocumentos".equals(prueba)) {
			Contexto contexto = contexto("HB", "homologacion");
			Documentos datos = getDocumentos(contexto, "4998590237644808", "1");
			imprimirResultado(contexto, datos);

		}

		if ("getReferencias".equals(prueba)) {
			Contexto contexto = contexto("HB", "homologacion");
			Referencias datos = getReferencias(contexto, "4998590237644808");
			imprimirResultado(contexto, datos);

		}

		if ("getTiposDocumentos".equals(prueba)) {
			Contexto contexto = contexto("HB", "homologacion");
			TiposDocumentos datos = getTiposDocumentos(contexto, "4998590237644808");
			imprimirResultado(contexto, datos);

		}

		if ("post".equals(prueba)) {
			// esto es más para ver que el request se arme bien
			// no pude probar una ode, no funcionaba en el momento en que hice el código
			Contexto contexto = contexto("HB", "homologacion");
			Cuenta cuenta = new Cuenta();
			cuenta.moneda = "80";
			cuenta.numeroProducto = "0123456789012345";
			post(contexto, "4998590237644808", new BigDecimal(1000), "30495657", "ejemplo", cuenta);
		}

		if ("delete".equals(prueba)) {
			// esto es más para ver que el request se arme bien
			// no pude probar una ode, no funcionaba en el momento en que hice el código
			Contexto contexto = contexto("HB", "homologacion");
			delete(contexto, "4998590237644808", "1");
		}
	}
}
