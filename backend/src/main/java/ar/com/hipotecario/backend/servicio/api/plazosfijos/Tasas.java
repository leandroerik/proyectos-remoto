package ar.com.hipotecario.backend.servicio.api.plazosfijos;

import java.math.BigDecimal;
import java.util.List;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;

public class Tasas extends ApiObjeto {

	/* ========== ATRIBUTOS ========== */
	public class ItemTasas {
		BigDecimal orden;
		String idMoneda;
		String idTipoDeposito;
		String descTipoDeposito;
		Integer plazoMinimo;
		Integer plazoMaximo;
		BigDecimal montoMinimo;
		BigDecimal montoMaximo;
		BigDecimal valorTasa;
	}

	List<ItemTasas> tasas;
	Integer totalRegistros;

	public class ItemTasaPreferencial {
		String ofertaCliente;
		String ofertaPlataNueva;
		BigDecimal montoVenPFTradicional;
		BigDecimal montoListadoDisponible;
		BigDecimal montoDesde;
		BigDecimal montoHasta;
		Integer plazoMaximo;
		BigDecimal tasa;
		String moneda;
		BigDecimal TEA;
		BigDecimal TEM;
		Fecha fechaVigencia;
	}

	public static class TasasPreferenciales extends ApiObjeto {
		List<ItemTasaPreferencial> ofertaTasaPreferencial;
	}

	/* ========== SERVICIOS ========== */
	// API-PlazoFijo_ConsultaTasaPlazoFijo
	public static Tasas getTasas(Contexto contexto, String idCliente, String secuencial, String canal) {
		ApiRequest request = new ApiRequest("PlazosFijosTasasGet", "plazosfijos", "GET", "/v1/tasas", contexto);
		request.query("idcliente", idCliente);
		request.query("secuencial", secuencial);
		request.header("x-canal", canal);

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(Tasas.class);
	}

	// API-PlazoFijo_ConsultaTasaPreferencial
	public static TasasPreferenciales getTasasPreferenciales(Contexto contexto, String idCliente) {
		ApiRequest request = new ApiRequest("PlazosFijosTasasPreferencialesGet", "plazosfijos", "GET", "/v1/tasasPreferenciales", contexto);
		request.query("idcliente", idCliente);

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(TasasPreferenciales.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		String test = "tasas";
		Contexto contexto = contexto("HB", "homologacion");

		if ("tasas".equals(test)) {
			Tasas datos = getTasas(contexto, "332658", "180", "HB_BE");
			imprimirResultado(contexto, datos);
		}

		if ("TasasPreferenciales".equals(test)) {
			TasasPreferenciales datos = getTasasPreferenciales(contexto, "135706");
			imprimirResultado(contexto, datos);
		}
	}

}
