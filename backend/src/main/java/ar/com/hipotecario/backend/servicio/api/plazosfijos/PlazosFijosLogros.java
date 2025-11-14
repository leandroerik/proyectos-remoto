package ar.com.hipotecario.backend.servicio.api.plazosfijos;

import java.math.BigDecimal;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.plazosfijos.PlazosFijosLogros.PlazoFijoLogros;

public class PlazosFijosLogros extends ApiObjetos<PlazoFijoLogros> {

	public static class PlazoFijoLogros extends ApiObjeto {
		String canal;
		Integer cantidadPlazos;
		String cuenta;
		String descripcionPlazoFijo;
		String descripcionTipoPlan;
		Integer diaConstitucionPF;
		String estado;
		String fechaConstPlan;
		Integer idPlanAhorro;
		Integer moneda;
		BigDecimal monto;
		String nombre;
		String plazo;
		Integer secuencialTipoPlan;
		BigDecimal spread;
		BigDecimal tasa;
		String tasaReferencial;
		String tipoCuenta;
		String tipoPlan;
		String tipoPlazoFijo;
		String vencimiento;

		public Fecha fechaConstPlan() {
			return new Fecha(fechaConstPlan, "yyyy-MM-dd'T'hh:mm:ss");
		}

		public Fecha vencimiento() {
			return new Fecha(vencimiento, "yyyy-MM-dd'T'hh:mm:ss");
		}
	}

	public static class DetallePlazoFijoLogros extends ApiObjetos<ItemDetallePlazoFijoLogros> {
	}

	public static class ItemDetallePlazoFijoLogros extends ApiObjeto {
		BigDecimal cotizacionUVA;
		String cuenta;
		String cuota;
		String estado;
		String fechaConstiFin;
		String fechaConstiTeo;
		String fechaCotizacionUVA;
		String fechaVen;
		String garantizado;
		Integer idPlanAhorro;
		Integer moneda;
		BigDecimal monto;
		BigDecimal montoImpuestos;
		BigDecimal montoInteres;
		BigDecimal montoUVA;
		String nroCertificado;
		String numBanco;
		Integer reintentos;
		Integer secuencial;
		BigDecimal spread;
		BigDecimal tasa;
		BigDecimal tasaInteres;
		String tipoCuenta;

		public Fecha fechaConstiFin() {
			return new Fecha(fechaConstiFin, "yyyy-MM-dd'T'hh:mm:ss");
		}

		public Fecha fechaConstiTeo() {
			return new Fecha(fechaConstiTeo, "yyyy-MM-dd'T'hh:mm:ss");
		}

		public Fecha fechaCotizacionUVA() {
			return new Fecha(fechaCotizacionUVA, "yyyy-MM-dd'T'hh:mm:ss");
		}

		public Fecha fechaVen() {
			return new Fecha(fechaVen, "yyyy-MM-dd'T'hh:mm:ss");
		}
	}

	public static class RequestPatch {
		String cuenta;
		String tipoCuenta;
		String idCobis;
		String moneda;
		BigDecimal monto;
		String nombre;
		Integer planContratado;
	}

	public static class RespuestaOk extends ApiObjeto {
		public Boolean ok;
	}

	public static RespuestaOk delete(Contexto contexto, String idCobis, String planContratado) {
		ApiRequest request = new ApiRequest("PlazosFijosBajaPlazoFijoLogros", "plazosfijos", "GET", "/v1/{idCobis}/bajaPlazoFijoAhorro", contexto);
		request.path("idCobis", idCobis);
		request.query("planContratado", planContratado);

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(RespuestaOk.class);
	}

	public static RespuestaOk forzar(Contexto contexto, String idCobis, String planContratado, String secuencialPlazoFijo) {
		ApiRequest request = new ApiRequest("PlazosFijosForzadoPlazoFijoLogros", "plazosfijos", "GET", "/v1/{idCobis}/forzadoPlazoFijo", contexto);
		request.path("idCobis", idCobis);
		request.query("planContratado", planContratado);
		request.query("secuencialPlazoFijo", secuencialPlazoFijo);

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(RespuestaOk.class);
	}

	public static RespuestaOk patch(Contexto contexto, RequestPatch requestPatch) {
		ApiRequest request = new ApiRequest("PlazosFijosModificacionPlazoFijoLogros", "plazosfijos", "GET", "/v1/{idCobis}/forzadoPlazoFijo", contexto);
		request.path("idCobis", requestPatch.idCobis);
		request.query("cuenta", requestPatch.cuenta);
		request.query("moneda", requestPatch.moneda);
		request.query("monto", requestPatch.monto);
		request.query("nombre", requestPatch.nombre);
		request.query("planContratado ", requestPatch.planContratado);
		request.query("tipoCuenta ", requestPatch.tipoCuenta);

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(RespuestaOk.class);
	}

	public static PlazosFijosLogros getCabecera(Contexto contexto, String idCobis, String idPlanAhorro) {
		ApiRequest request = new ApiRequest("PlazosFijosGetCabeceraLogros", "plazosfijos", "GET", "/v1/planAhorro/cabecera", contexto);
		request.query("codCliente", idCobis);
		request.query("idPlanAhorro", idPlanAhorro);
		request.query("opcion", "Q");
		request.query("operacion", "2");

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(PlazosFijosLogros.class);
	}

	public static DetallePlazoFijoLogros getDetalle(Contexto contexto, String idCobis, String planContratado, String secuencial) {
		ApiRequest request = new ApiRequest("PlazosFijosGetCabeceraLogros", "plazosfijos", "GET", "/v1/planAhorro/detalle", contexto);
		request.query("codCliente", idCobis);
		request.query("planContratado", planContratado);
		request.query("opcion", "Q");
		request.query("operacion", "3");

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200) && !response.http(204), request, response);
		return response.crear(DetallePlazoFijoLogros.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		String test = "getDetalle";
		Contexto contexto = contexto("HB", "homologacion");

		if ("delete".equals(test)) {
			delete(contexto, "111", "111");
		}
		if ("forzar".equals(test)) {
			forzar(contexto, "111", "111", "111");
		}
		if ("patch".equals(test)) {
			RequestPatch patch = new PlazosFijosLogros.RequestPatch();
			patch.cuenta = "1";
			patch.tipoCuenta = "1";
			patch.idCobis = "1";
			patch.moneda = "1";
			patch.monto = new BigDecimal(5);
			patch.nombre = "1";
			patch.planContratado = 1;
			patch(contexto, patch);
		}

		if ("getCabecera".equals(test)) {
			PlazosFijosLogros datos = getCabecera(contexto, "4594725", "0");
			System.out.println(datos.get(0).fechaConstPlan);
			imprimirResultado(contexto, datos);
		}

		if ("getDetalle".equals(test)) {
			DetallePlazoFijoLogros datos = getDetalle(contexto, "4594725", "1360", "1");
			imprimirResultado(contexto, datos);
		}
	}
}
