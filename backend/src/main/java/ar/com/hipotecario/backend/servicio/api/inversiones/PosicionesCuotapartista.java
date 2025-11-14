package ar.com.hipotecario.backend.servicio.api.inversiones;

import java.math.BigDecimal;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.inversiones.PosicionesCuotapartista.PosicionCuotapartista;

public class PosicionesCuotapartista extends ApiObjetos<PosicionCuotapartista> {

	/* ========== ATRIBUTOS ========== */
	public static class PosicionCuotapartista extends ApiObjeto {
		public String FondoID;
		public Integer FondoNumero;
		public String FondoNombre;
		public String TipoVCPID;
		public String TipoVCPAbreviatura;
		public String TipoVCPDescripcion;
		public String CuotapartistaID;
		public String CuotapartistaNombre;
		public Integer CuotapartistaNumero;
		public BigDecimal CuotapartesTotales;
		public BigDecimal CuotapartesBloqueadas;
		public BigDecimal CuotapartesValuadas;
		public BigDecimal UltimoVCPValor;
		public String UltimoVCPFecha;
		public String IDCondicionIngEgr;
		public Integer IDMoneda;
		public String MonedaDescripcion;
		public String MonedaSimbolo;
	}

	/* ========== SERVICIOS ========== */
	// API-Inversiones_PosicionCuotapartistaESCO
	public static PosicionesCuotapartista post(Contexto contexto, String fecha, String idCuotapartista, Integer numeroCuotapartista, String nombre) {
		ApiRequest request = new ApiRequest("PosicionesCuotapartista", "inversiones", "POST", "/v1/posicionCuotapartista", contexto);
		request.body("pPosicionCuotapartista.fecha", fecha);
		request.body("pPosicionCuotapartista.idCuotapartista", idCuotapartista);
		request.body("pPosicionCuotapartista.numeroCuotapartista", numeroCuotapartista);
		request.body("pPosicionCuotapartista.nombre", nombre);
		request.cache = false;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(PosicionesCuotapartista.class, response.objeto("PosicionCuotapartista").objetos());
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		PosicionesCuotapartista datos = post(contexto, Fecha.hoy().string("yyyy-MM-dd"), "8106", 8106, "");
		imprimirResultado(contexto, datos);
	}

}