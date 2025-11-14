package ar.com.hipotecario.backend.servicio.api.inversiones;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.inversiones.CuentaComitenteCliente.CuentaComitenteClienteId;

public class CuentaComitenteCliente extends ApiObjetos<CuentaComitenteClienteId> {

	/* ========== ATRIBUTOS ========== */
	public static class CuentaComitenteClienteId extends ApiObjeto {
		public String numeroCuenta;
		public String idTipoCuenta;
		public String numeroInterviniente;
		public String descripcion;
		public Fecha fechaAlta;
	}

	/* ========== SERVICIOS ========== */
	// API-Inversiones_ConsultaCuentasComitentePorCliente
	public static CuentaComitenteCliente get(Contexto contexto, String id) {
		ApiRequest request = new ApiRequest("CuentaComitenteClientePorId", "inversiones", "GET", "/v1/cuentascomitentes/cliente/{id}", contexto);
		request.path("id", id);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(CuentaComitenteCliente.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		CuentaComitenteCliente datos = get(contexto, "4373070");
		imprimirResultado(contexto, datos);
	}
}
