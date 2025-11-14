package ar.com.hipotecario.backend.servicio.api.inversiones;

import java.util.List;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.inversiones.RentaFinancieraMinoristas.RentaFinancieraMinorista;

public class RentaFinancieraMinoristas extends ApiObjetos<RentaFinancieraMinorista> {

	/* ========== ATRIBUTOS ========== */
	public static class Cotitulares {
		public String benefExt;
		public String caracter;
		public String certificado;
		public String espacio;
		public String nombreCotitular;
		public String num_doc;
		public String tipDoc;
		public String tipoRegistro;
	}

	public static class RentaFinancieraMinorista extends ApiObjeto {
		public String tipoRegistro;
		public String tipoDeposito;
		public String certificado;
		public String sucursal;
		public String tipoDocTit;
		public String numDocTitr;
		public String nombreTitular;
		public String benefExt;
		public String caracter;
		public String cantCotitulares;
		public String moneda;
		public String tipoEmpleado;
		public String ajuste;
		public String fechaAlta;
		public String fechaVen;
		public Integer montoMonedaOri;
		public Integer montoPesos;
		public Integer montoIntOri;
		public Integer montoIntPesos;
		public Integer importeMonOri;
		public Integer importePesos;
		public List<Cotitulares> cotitulares;
	}

	/* ========== SERVICIOS ========== */
	// API-Inversiones_RentaFinancieraMinorista
	public static RentaFinancieraMinoristas get(Contexto contexto, String cuil) {
		ApiRequest request = new ApiRequest("RentaFinancieraMinorista", "inversiones", "GET", "/v1/rentafinanciera/minorista", contexto);
		request.query("cuil", cuil);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(RentaFinancieraMinoristas.class, response.objetos("certificados"));
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "desarrollo");
		RentaFinancieraMinoristas datos = get(contexto, "20081190233");
		imprimirResultado(contexto, datos);
	}

}
