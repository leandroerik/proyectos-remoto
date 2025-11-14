package ar.com.hipotecario.backend.servicio.api.paquetes;

import java.math.BigDecimal;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.paquetes.Costos.Costo;

public class Costos extends ApiObjetos<Costo> {

	/* ========== CLASES ========== */
	public static class Costo extends ApiObjeto {
		public String codigo;
		public String tipo;
		public String categoriaDefault;
		public Object pagoTarjeta;
		public Object programaRecompensa;
		public BigDecimal sinIVA;
	}

	/* ========== SERVICIOS ========== */
	// API-Paquetes_ParametriaCostoSucursal
	public static Costos get(Contexto contexto, String numeroSucursal) {
		ApiRequest request = new ApiRequest("Costos", "paquetes", "GET", "/v1/infoParametrias/{numeroSucursal}/costos", contexto);
		request.path("numeroSucursal", numeroSucursal);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(Costos.class);
	}

	/* ========== METODOS ========== */
	public Costo buscar(String codigoPaquete) {
		Costo dato = null;
		for (Costo costo : this) {
			if (codigoPaquete.equals(costo.codigo)) {
				dato = costo;
			}
		}
		return dato;
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("BB", "homologacion");
		Costos datos = get(contexto, "0");
		imprimirResultado(contexto, datos);
	}

}
