package ar.com.hipotecario.backend.servicio.api.inversiones;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.inversiones.CuentaComitenteEspecies.CuentaComitenteEspecie;

public class CuentaComitenteEspecies extends ApiObjetos<CuentaComitenteEspecie> {

	/* ========== ATRIBUTOS ========== */
	public static class CuentaComitenteEspecie extends ApiObjeto {
		public Integer saldoNegociableResidual;
		public Fecha fechaCotizacion;
		public String entidadCustodia;
		public String codigo;
		public String monedaEspecie;
		public Integer saldoBloqueadoResidual;
		public Integer saldoGarantiaNominal;
		public Integer cotizacionSistemaNoticias;
		public Integer saldoGarantiaResidual;
		public Integer valorizacion;
		public Integer saldoBloqueadoNominal;
		public String tipoCotizacion;
		public String codigoEspecie;
		public Integer saldoDisponibleResidual;
		public Integer saldoDisponibleNominal;
		public Integer saldoNegociableNominal;
		public String monedaCotizacion;
		public Integer cantidadNominal;
		public String tipoProducto;
	};

	/* ========== SERVICIOS ========== */
	// API-Inversiones_ConsultaCuentasComitenteEspecies
	public static CuentaComitenteEspecies get(Contexto contexto, String idCliente, String cuentaComitente) {
		ApiRequest request = new ApiRequest("CuentasComitenteEspecie", "inversiones", "GET", "/v1/cuentascomitentes/{cuentacomitente}/especies", contexto);
		request.path("cuentacomitente", cuentaComitente);
		request.query("idcliente", idCliente);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(CuentaComitenteEspecies.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		CuentaComitenteEspecies datos = get(contexto, "4373070", "2-000108703");
		imprimirResultado(contexto, datos);
	}
}
