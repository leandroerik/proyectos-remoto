package ar.com.hipotecario.backend.servicio.api.inversiones;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.inversiones.RentaFinancieraMayoristas.RentaFinancieraMayorista;

public class RentaFinancieraMayoristas extends ApiObjetos<RentaFinancieraMayorista> {

	/* ========== ATRIBUTOS ========== */
	public static class RentaFinancieraMayorista extends ApiObjeto {
		public String NRO_COMITENTE;
		public Integer PERIODO;
		public String TIPO_REGISTRO;
		public String CLAUSULA_AJUSTE;
		public String IMP_ACTUALIZACION_MON_ORIGEN;
		public String ESPECIE_DESCRIPCION;
		public String ESPECIE_HB;
		public Fecha FECHA_ALTA;
		public Fecha FECHA_VENCIMIENTO;
		public String MONEDA_ORIGEN;
		public String INTERES_MON_ORIGEN;
		public String INTERES_MON_LOCAL;
		public String IMP_ACTUALIZACION_MON_LOCAL;
		public String TIPO_OPERACION;
		public String CAPITAL_MON_LOCAL;
		public String CAPITAL_MON_ORIGEN;
		public Integer NRO_CERTIFICADO;
	}

	/* ========== SERVICIOS ========== */
	// API-Inversiones_RentaFinancieraMayorista
	public static RentaFinancieraMayoristas get(Contexto contexto, String idCobis, String periodo) {
		ApiRequest request = new ApiRequest("RentaFinancieraMayorista", "inversiones", "GET", "/v1/rentafinanciera/mayorista", contexto);
		request.query("idCobis", idCobis);
		request.query("periodo", periodo);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(RentaFinancieraMayoristas.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		RentaFinancieraMayoristas datos = get(contexto, "4373070", "2019");
		imprimirResultado(contexto, datos);
	}

}
