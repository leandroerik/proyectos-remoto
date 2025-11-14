package ar.com.hipotecario.backend.servicio.api.ventas;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;

public class Indicador extends ApiObjeto {

	public static String GET_INDICADOR = "Indicador";

	/* ========== ATRIBUTOS ========== */
	public String nroIntegrante;
	public String nroIdTributaria;
	public Boolean indOK;
	public String resolucion;
	public String resolucionCodigo;
	public String resolucionDescripcion;
	public String explicacion;
	public String codigoExplicacion;
	public Object tipoOperacion;
	public Object Advertencias;
	public Object Id;

	/* ========== SERVICIO ========== */
	public static Indicador get(Contexto contexto, String numeroDocumento, String idTributario, String sexo, String tipoDocumento, String tipoTributario) {
		return get(contexto, null, numeroDocumento, idTributario, sexo, tipoDocumento, tipoTributario);
	}

	// indicadoresGET
	public static Indicador get(Contexto contexto, String idCobis, String numeroDocumento, String idTributario, String sexo, String tipoDocumento, String tipoTributario) {
		ApiRequest request = new ApiRequest(GET_INDICADOR, ApiVentas.API, "GET", "/indicadores", contexto);
		request.query("idCliente", idCobis);
		request.query("nroDocumento", numeroDocumento);
		request.query("nroIdTributaria", idTributario);
		request.query("sexo", sexo);
		request.query("tipoDocumento", tipoDocumento);
		request.query("tipoIdTributaria", tipoTributario);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(ApiVentas.errorVentas(response), request, response);
		return response.crear(Indicador.class, response.objetos(ApiVentas.DATOS).get(0));
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		Indicador datos = get(contexto, "27389", "17078668", "27170786683", "F", "01", "08");
		imprimirResultadoApiVentas(contexto, datos);
	}
}
