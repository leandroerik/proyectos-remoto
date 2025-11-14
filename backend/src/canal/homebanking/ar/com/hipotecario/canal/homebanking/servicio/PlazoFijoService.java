package ar.com.hipotecario.canal.homebanking.servicio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.base.ConfigHB;
import ar.com.hipotecario.canal.homebanking.base.Objeto;
import ar.com.hipotecario.canal.homebanking.conector.Api;
import ar.com.hipotecario.canal.homebanking.conector.ApiRequest;
import ar.com.hipotecario.canal.homebanking.conector.ApiResponse;
import ar.com.hipotecario.canal.homebanking.lib.Concurrencia;

public class PlazoFijoService {

	private static ApiResponse tasas(ContextoHB contexto, Integer secuencial) {
		ApiRequest request = Api.request("PlazosFijosGetTasas", "plazosfijos", "GET", "/v1/tasas", contexto);
		request.query("idcliente", contexto.idCobis());
		request.query("secuencial", secuencial.toString());
		request.cacheSesion = true;
		return Api.response(request, contexto.idCobis(), secuencial.toString());
	}

	public static ApiResponse precancelarProcrearJoven(ContextoHB contexto) {
		ApiRequest request = Api.request("PrecancelarProcrearJoven", "plazosfijos", "PATCH", "/v1/solicitudPrecancelar/{idCobis}", contexto);
		request.path("idCobis", contexto.idCobis());
		return Api.response(request, contexto.idCobis());
	}

	public static ApiResponse consultarInformacionCancelacionAnticipadaCER(ContextoHB contexto, String nroCertificado) {
		ApiRequest request = Api.request("PlazosFijosGetCancelacionAnticipada", "plazosfijos", "GET", "/v1/plazosfijos/{nroOperacion}/cancelacionanticipada/solicitud/estado", contexto);
		request.header("x-usuariocanal", ConfigHB.string("configuracion_usuario"));
		request.path("nroOperacion", nroCertificado);

		// TODO optimizar consolidada PF
		request.cacheSesion = true;

		return Api.response(request, nroCertificado);
	}

	public static ApiResponse consultarEstadoCancelacionAnticipadaCER(ContextoHB contexto, String nroCertificado) {
		// emm--> aclaración: el nombre está bien, este recurso trae el estado de la
		// solicitud a pesar de que no es el que se llama estado
		ApiRequest request = Api.request("PlazosFijosGetEstadoCancelacionAnticipada", "plazosfijos", "GET", "/v1/plazosfijos/{nroOperacion}/cancelacionanticipada/solicitud", contexto);
		request.header("x-usuariocanal", ConfigHB.string("configuracion_usuario"));
		request.path("nroOperacion", nroCertificado);

		// TODO optimizar consolidada PF
		request.cacheSesion = true;

		return Api.response(request, nroCertificado);
	}

	public static ApiResponse precancelarPlazoFijoUvaCer(ContextoHB contexto, String nroCertificado) {
		ApiRequest request = Api.request("PlazosFijosPrecancelarUvaCer", "plazosfijos", "POST", "/v1/plazosfijos/cancelacionanticipada/solicitud", contexto);
		request.header("x-usuariocanal", ConfigHB.string("configuracion_usuario"));
		request.body("numeroOperacion", nroCertificado);
		return Api.response(request, nroCertificado);
	}

	public static List<ApiResponse> tasas(ContextoHB contexto) {
		List<ApiResponse> lista = new ArrayList<>();
		Integer cantidadRegistros = tasas(contexto, 0).integer("totalRegistros");
		Integer cantidadInvocaciones = ((cantidadRegistros - 1) / 30) + 1;
		ExecutorService executorService = Concurrencia.executorService(cantidadInvocaciones);
		for (Integer i = 0; i < cantidadInvocaciones; ++i) {
			final Integer x = i;
			executorService.submit(() -> {
				ApiResponse response = tasas(contexto, x * 30);
				lista.add(response);
			});
		}
		Concurrencia.esperar(executorService, null);
		return lista;
	}

	public static ApiResponse movimientoPlazoFijo(ContextoHB contexto, String secMovimiento) {
		ApiRequest request = Api.request("PlazosFijosMovimientos", "plazosfijos", "GET", "/v1/movimientos/{secMovimiento}", contexto);
		request.header("x-usuariocanal", ConfigHB.string("configuracion_usuario"));
		request.path("secMovimiento", secMovimiento);
		return Api.response(request, secMovimiento);
	}

	public static Objeto tasaPreferencial(ContextoHB contexto) {
		// NUEVO: tomar moneda y tipoDeposito del contexto si vienen, con defaults
		// moneda: 80 (ARS), tipoDeposito: 10000 (PF Tradicional) – ajustá si tu canal usa otros valores
		Integer moneda = Optional.ofNullable(contexto.parametros.integer("moneda")).orElse(80);
		Integer tipoDeposito = Optional.ofNullable(contexto.parametros.integer("tipoDeposito")).orElse(10000);

		ApiRequest request = Api.request("API-PlazoFijo_ConsultaTasaPreferencialV2", "plazosfijos", "GET", "/v2/tasaspreferenciales", contexto);
		request.header("x-usuariocanal", ConfigHB.string("configuracion_usuario"));
		request.query("idcliente", contexto.idCobis());
		request.query("moneda", String.valueOf(Optional.ofNullable(contexto.parametros.integer("moneda")).orElse(80)));
		request.query("tipoDeposito", String.valueOf(Optional.ofNullable(contexto.parametros.integer("tipoDeposito")).orElse(10000)));


		ApiResponse response = Api.response(request, contexto.idCobis());
		if (response.hayError()) {
			// Caso sin oferta para el cliente (mantener compatibilidad)
			if ("123008".equals(response.string("codigo"))) {
				Objeto objSinOferta = new Objeto();
				objSinOferta.set("OfertaCliente", "N");
				// También dejamos explícito que no hay pricing
				objSinOferta.set("Pricing", "N");
				objSinOferta.set("OfertaPricing", Collections.emptyList());
				return objSinOferta;
			}
			return null;
		}

		// =========== MAPEO OFERTA (retrocompatible) ===========
		// v2: "oferta": [ { ofertaCliente, ofertaPlataNueva, ... , pricing } ]
		// Tomamos la primera, si existe
		Objeto objeto = new Objeto();
		List<Objeto> ofertas = response.objetos("oferta");
		if (ofertas != null && !ofertas.isEmpty()) {
			Objeto o = ofertas.get(0);
			// Mantener nombres legacy para no romper el resto del código:
			objeto.set("OfertaCliente", o.string("ofertaCliente"));                    // "S" / "N"
			objeto.set("OfertaPlataNueva", o.string("ofertaPlataNueva"));              // "S" / "N"
			objeto.set("MontoVencimientoPFTradicional", o.string("montoVenPFTradicional"));
			objeto.set("MontoListadoDisponible", o.string("montoListadoDisponible"));
			objeto.set("MontoDesde", o.string("montoDesde"));
			objeto.set("MontoHasta", o.string("montoHasta"));
			objeto.set("PlazoMaximo", o.string("plazoMaximo"));
			objeto.set("Tasa", o.string("tasa"));
			objeto.set("Moneda", o.string("moneda"));
			objeto.set("TEA", o.string("TEA"));
			objeto.set("TEM", o.string("TEM"));
			// v2 viene "YYYY-MM-DD"; el legacy esperaba "yyyy-MM-dd'T'HH:mm:ss"
			String fv = o.string("fechaVigencia");
			if (fv != null && !fv.isBlank()) {
				objeto.set("FechaVigencia", fv + "T00:00:00");
			}
			// NUEVO: flag de pricing que trae v2
			objeto.set("Pricing", o.string("pricing")); // "S" / "N"
		} else {
			// Si no vino "oferta", dejamos valores por defecto "N" y pricing "N"
			objeto.set("OfertaCliente", "N");
			objeto.set("OfertaPlataNueva", "N");
			objeto.set("Pricing", "N");
		}

		// =========== MAPEO OFERTA PRICING ===============
		// v2: "ofertaPricing": [ { codigoProducto, moneda, tasa, fechaInicio, fechaFin, plazoMinimo, plazoMaximo, montoMinimo, montoMaximo, campania, mensaje } ]
		List<Objeto> listaPricing = new ArrayList<>();
		List<Objeto> raws = response.objetos("ofertaPricing");
		if (raws != null) {
			for (Objeto p : raws) {
				Objeto row = new Objeto();
				row.set("codigoProducto", p.string("codigoProducto"));
				row.set("moneda", p.integer("moneda"));
				row.set("tasa", p.bigDecimal("tasa"));
				// v2: fechas en "dd/MM/yyyy"
				row.set("fechaInicio", p.string("fechaInicio"));
				row.set("fechaFin", p.string("fechaFin"));
				row.set("plazoMinimo", p.integer("plazoMinimo"));
				row.set("plazoMaximo", p.integer("plazoMaximo"));
				row.set("montoMinimo", p.bigDecimal("montoMinimo"));
				row.set("montoMaximo", p.bigDecimal("montoMaximo"));
				row.set("campania", p.string("campania"));
				row.set("mensaje", p.string("mensaje"));
				listaPricing.add(row);
			}
		}
		objeto.set("OfertaPricing", listaPricing);

		return objeto;
	}


	public static ApiResponse cedips(ContextoHB contexto, String cuil, String filtros, Integer pagina) {
		ApiRequest request = Api.request("cedipsGet", "plazosfijos", "GET", "/v1/cedips/{cuit}", contexto);
		request.path("cuit", cuil);
		request.query("pag", pagina.toString());
		request.query("filtros", filtros);
		return Api.response(request, contexto.idCobis());
	}
}
