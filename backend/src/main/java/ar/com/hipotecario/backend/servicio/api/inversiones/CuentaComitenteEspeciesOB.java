package ar.com.hipotecario.backend.servicio.api.inversiones;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.inversiones.CuentaComitenteEspeciesOB.CuentaComitenteEspecie;
import java.math.BigDecimal;
import java.util.List;


public class CuentaComitenteEspeciesOB extends ApiObjetos<CuentaComitenteEspecie>{

	public List<CuentaComitenteEspecie> especies;
	
	/* ========== ATRIBUTOS ========== */
	public static class CuentaComitenteEspecie extends ApiObjeto {
		public BigDecimal saldoNegociableResidual;
		public Fecha fechaCotizacion;
		public String entidadCustodia;
		public String codigo;
		public String monedaEspecie;
		public Double saldoBloqueadoResidual;
		public BigDecimal saldoGarantiaNominal;
		public Double cotizacionSistemaNoticias;
		public BigDecimal saldoGarantiaResidual;
		public BigDecimal valorizacion;
		public BigDecimal saldoBloqueadoNominal;
		public String tipoCotizacion;
		public String codigoEspecie;
		public BigDecimal saldoDisponibleResidual;
		public BigDecimal saldoDisponibleNominal;
		public Integer saldoNegociableNominal;
		public String monedaCotizacion;
		public Integer cantidadNominal;
		public String tipoProducto;
	};

	/* ========== SERVICIOS ========== */
	// API-Inversiones_ConsultaCuentasComitenteEspecies
	public static CuentaComitenteEspeciesOB get(Contexto contexto, String idCliente, String cuentaComitente) {
		ApiRequest request = new ApiRequest("CuentasComitenteEspecie", "inversiones", "GET", "/v1/cuentascomitentes/{cuentacomitente}/especies", contexto);
		request.path("cuentacomitente", cuentaComitente);
		request.query("idcliente", idCliente);
		request.cache = false;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(CuentaComitenteEspeciesOB.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		CuentaComitenteEspeciesOB datos = get(contexto, "4373070", "2-000108703");
		imprimirResultado(contexto, datos);
	}

}
