package ar.com.hipotecario.backend.servicio.api.inversiones;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.inversiones.CuentaComitentePosturas.CuentaComitentePostura;;

public class CuentaComitentePosturas extends ApiObjetos<CuentaComitentePostura> {

	/* ========== ATRIBUTOS ========== */
	public static class CuentaComitentePostura extends ApiObjeto {
		public Integer Numero;
		public String HoraConcertacion;
		public String EspecieDescripcion;
		public Fecha FechaConcertacion;
		public String Licitacion;
		public Integer PrecioAdjudicado;
		public String Estado;
		public String Tramo;
		public Integer CantidadAdjudicada;
		public String LicitacionDescripcion;
		public String Comitente;
		public String Especie;
		public Integer CantidadLicitada;
		public String Cuenta;
	};

	/* ========== SERVICIOS ========== */
	// API-Inversiones_Posturas
	public static CuentaComitentePosturas get(Contexto contexto, String cuentaComitente, Fecha fechaDesde, Fecha fechaHasta) {
		ApiRequest request = new ApiRequest("CuentasComitentePosturas", "inversiones", "GET", "/v1/cuentascomitentes/{cuentacomitente}/posturas", contexto);
		request.path("cuentacomitente", cuentaComitente);
		request.query("fechadesde", fechaDesde.string("yyyy-MM-dd"));
		request.query("fechahasta", fechaHasta.string("yyyy-MM-dd"));
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(CuentaComitentePosturas.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Fecha fechaDesde = new Fecha("2019-01-01", "yyyy-MM-dd");
		Fecha fechaHasta = new Fecha("2020-01-01", "yyyy-MM-dd");

		Contexto contexto = contexto("HB", "homologacion");
		CuentaComitentePosturas datos = get(contexto, "2-000108703", fechaDesde, fechaHasta);
		imprimirResultado(contexto, datos);
	}
}
