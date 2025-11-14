package ar.com.hipotecario.backend.servicio.api.debin;

import java.math.BigDecimal;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;

public class DebinPlazoFijoWeb extends ApiObjeto {

	/* ========== ATRIBUTOS ========== */
	public String id;
	public Estado estado;
	public String fechaAlta;
	public String fechaExpiracion;

	/* ========== SERVICIOS ========== */
	// API-DEBIN_AltaDebinPlazoFijo
	public static DebinPlazoFijoWeb post(Contexto contexto, String idCliente, String cuit, String cbu, BigDecimal monto, String numeroPlazoFijoWeb) {
		ApiRequest request = new ApiRequest("CrearDebinPlazoFijoWeb", "debin", "POST", "/v1/debin/plazofijo", contexto);
		request.body("cliente.idTributario", cuit);
		request.body("cliente.cuenta.cbu", cbu);
		request.body("idCliente", idCliente);
		request.body("appId", numeroPlazoFijoWeb);
		request.body("moneda.id", "80");
		request.body("importe", monto);

		ApiResponse response = request.ejecutar();
		ApiException.throwIf("FUERA_HORARIO", response.contains("FUERA DE HORARIO"), request, response);
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(DebinPlazoFijoWeb.class, response.objeto("debin"));
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		DebinPlazoFijoWeb datos = post(contexto, "8051446", "27606060608", "0340265008124514801000", new BigDecimal("6000"), "13626049");
		imprimirResultado(contexto, datos);
	}
}
