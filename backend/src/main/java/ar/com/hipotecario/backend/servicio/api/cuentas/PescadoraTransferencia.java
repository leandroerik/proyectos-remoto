package ar.com.hipotecario.backend.servicio.api.cuentas;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;

public class PescadoraTransferencia extends ApiObjeto {

	/* ========== ATRIBUTOS ========== */
	public String codigoRespuesta;
	public RespuestaTransferencia respuestaTransferencia;

	public static class RespuestaTransferencia {
		public String codigoRespuesta;
		public String numeroTransaccion;
	}

	/* ========== SERVICIOS ========== */
	// API-Cuentas_ConsultaEstadoTransferenciaOnline
	static PescadoraTransferencia get(Contexto contexto, String idRequerimiento, String numeroTarjeta, String timeStampTransferencia) {
		ApiRequest request = new ApiRequest("PescadoraTransferencia", "cuentas", "GET", "/v1/transferencias", contexto);
		request.query("idrequerimiento", idRequerimiento);
		request.query("numeroTarjeta", numeroTarjeta);
		request.query("timestamptransferencia", timeStampTransferencia);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(PescadoraTransferencia.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		PescadoraTransferencia datos = get(contexto, "11308073", "4998590015392208", "1572018451307");
		imprimirResultado(contexto, datos);
	}
}
