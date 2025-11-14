package ar.com.hipotecario.mobile.servicio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

import ar.com.hipotecario.mobile.ConfigMB;
import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.conector.ApiMB;
import ar.com.hipotecario.mobile.conector.ApiRequestMB;
import ar.com.hipotecario.mobile.conector.ApiResponseMB;
import ar.com.hipotecario.mobile.lib.Concurrencia;
import ar.com.hipotecario.mobile.lib.Objeto;

public class PlazoFijoService {

	public static ApiResponseMB plazosFijosPrecancelables(ContextoMB contexto) {
		ApiRequestMB request = ApiMB.request("PlazosFijosGetSolicitudPrecancelar", "plazosfijos", "GET", "/v1/solicitudPrecancelar", contexto);
		request.query("idCobis", contexto.idCobis());
		return ApiMB.response(request, contexto.idCobis());
	}

	private static ApiResponseMB tasas(ContextoMB contexto, Integer secuencial) {
		ApiRequestMB request = ApiMB.request("PlazosFijosGetTasas", "plazosfijos", "GET", "/v1/tasas", contexto);
		request.query("idcliente", contexto.idCobis());
		request.query("secuencial", secuencial.toString());
		request.cacheSesion = true;
		return ApiMB.response(request, contexto.idCobis(), secuencial.toString());
	}

	public static ApiResponseMB precancelables(ContextoMB contexto, Integer secuencial) {
		ApiRequestMB request = ApiMB.request("PlazosFijosPrecancelables", "plazosfijos", "GET", "/v1/solicitudPrecancelar", contexto);
		request.query("idCobis", contexto.idCobis());
		return ApiMB.response(request, contexto.idCobis());
	}

	public static ApiResponseMB precancelarProcrearJoven(ContextoMB contexto) {
		ApiRequestMB request = ApiMB.request("PrecancelarProcrearJoven", "plazosfijos", "PATCH", "/v1/solicitudPrecancelar/{idCobis}", contexto);
		request.path("idCobis", contexto.idCobis());
		return ApiMB.response(request, contexto.idCobis());
	}

	public static ApiResponseMB consultarInformacionCancelacionAnticipadaCER(ContextoMB contexto, String nroCertificado) {
		ApiRequestMB request = ApiMB.request("PlazosFijosGetCancelacionAnticipada", "plazosfijos", "GET", "/v1/plazosfijos/{nroOperacion}/cancelacionanticipada/solicitud/estado", contexto);
		request.header("x-usuariocanal", ConfigMB.string("configuracion_usuario"));
		request.path("nroOperacion", nroCertificado);
		return ApiMB.response(request, nroCertificado);
	}

	public static ApiResponseMB consultarEstadoCancelacionAnticipadaCER(ContextoMB contexto, String nroCertificado) {
		// emm--> aclaración: el nombre está bien, este recurso trae el estado de la
		// solicitud a pesar de que no es el que se llama estado
		ApiRequestMB request = ApiMB.request("PlazosFijosGetEstadoCancelacionAnticipada", "plazosfijos", "GET", "/v1/plazosfijos/{nroOperacion}/cancelacionanticipada/solicitud", contexto);
		request.header("x-usuariocanal", ConfigMB.string("configuracion_usuario"));
		request.path("nroOperacion", nroCertificado);
		return ApiMB.response(request, nroCertificado);
	}

	public static ApiResponseMB precancelarPlazoFijoUvaCer(ContextoMB contexto, String nroCertificado) {
		ApiRequestMB request = ApiMB.request("PlazosFijosPrecancelarUvaCer", "plazosfijos", "POST", "/v1/plazosfijos/cancelacionanticipada/solicitud", contexto);
		request.header("x-usuariocanal", ConfigMB.string("configuracion_usuario"));
		request.body("numeroOperacion", nroCertificado);
		return ApiMB.response(request, nroCertificado);
	}

	public static List<ApiResponseMB> tasas(ContextoMB contexto) {
		List<ApiResponseMB> lista = new ArrayList<>();
		Integer cantidadRegistros = tasas(contexto, 0).integer("totalRegistros");
		Integer cantidadInvocaciones = ((cantidadRegistros - 1) / 30) + 1;
		ExecutorService executorService = Concurrencia.executorService(cantidadInvocaciones);
		for (Integer i = 0; i < cantidadInvocaciones; ++i) {
			final Integer x = i;
			executorService.submit(() -> {
				ApiResponseMB response = tasas(contexto, x * 30);
				lista.add(response);
			});
		}
		Concurrencia.esperar(executorService, null);
		return lista;
	}

	public static Objeto tasaPreferencial(ContextoMB contexto) {
		Integer moneda = Optional.ofNullable(contexto.parametros.integer("moneda")).orElse(80);
		Integer tipoDeposito = Optional.ofNullable(contexto.parametros.integer("tipoDeposito")).orElse(10000);

		ApiRequestMB request = ApiMB.request(
				"API-PlazoFijo_ConsultaTasaPreferencialV2",
				"plazosfijos",
				"GET",
				"/v2/tasaspreferenciales",
				contexto
		);
		request.header("x-usuariocanal", ConfigMB.string("configuracion_usuario"));
		request.query("idcliente", contexto.idCobis());
		request.query("moneda", String.valueOf(moneda));           // String requerido
		request.query("tipoDeposito", String.valueOf(tipoDeposito));

		ApiResponseMB response = ApiMB.response(request, contexto.idCobis());
		if (response.hayError()) {
			if ("123008".equals(response.string("codigo"))) {
				Objeto sin = new Objeto();
				sin.set("OfertaCliente", "N");
				sin.set("OfertaPlataNueva", "N");
				sin.set("Pricing", "N");
				sin.set("OfertaPricing", Collections.emptyList());
				return sin;
			}
			return null;
		}

		Objeto out = new Objeto();

		List<Objeto> ofertas = response.objetos("oferta");
		if (ofertas != null && !ofertas.isEmpty()) {
			Objeto o = ofertas.get(0);
			out.set("OfertaCliente", o.string("ofertaCliente"));
			out.set("OfertaPlataNueva", o.string("ofertaPlataNueva"));
			out.set("MontoVencimientoPFTradicional", o.string("montoVenPFTradicional"));
			out.set("MontoListadoDisponible", o.string("montoListadoDisponible"));
			out.set("MontoDesde", o.string("montoDesde"));
			out.set("MontoHasta", o.string("montoHasta"));
			out.set("PlazoMaximo", o.string("plazoMaximo"));
			out.set("Tasa", o.string("tasa"));
			out.set("Moneda", o.string("moneda"));
			out.set("TEA", o.string("TEA"));
			out.set("TEM", o.string("TEM"));
			String fv = o.string("fechaVigencia");
			if (fv != null && !fv.isBlank()) out.set("FechaVigencia", fv + "T00:00:00");
			out.set("Pricing", o.string("pricing")); // "S"/"N"
		} else {
			out.set("OfertaCliente", "N");
			out.set("OfertaPlataNueva", "N");
			out.set("Pricing", "N");
		}

		List<Objeto> pricingList = new ArrayList<>();
		List<Objeto> raws = response.objetos("ofertaPricing");
		if (raws != null) {
			for (Objeto p : raws) {
				Objeto row = new Objeto();
				row.set("codigoProducto", p.string("codigoProducto"));
				row.set("moneda", p.integer("moneda"));
				row.set("tasa", p.bigDecimal("tasa"));
				row.set("fechaInicio", p.string("fechaInicio"));
				row.set("fechaFin", p.string("fechaFin"));
				row.set("plazoMinimo", p.integer("plazoMinimo"));
				row.set("plazoMaximo", p.integer("plazoMaximo"));
				row.set("montoMinimo", p.bigDecimal("montoMinimo"));
				row.set("montoMaximo", p.bigDecimal("montoMaximo"));
				row.set("campania", p.string("campania"));
				row.set("mensaje", p.string("mensaje"));
				pricingList.add(row);
			}
		}
		out.set("OfertaPricing", pricingList);

		return out;
	}

}
