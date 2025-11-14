package ar.com.hipotecario.backend.servicio.api.seguro;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.conector.api.Api;
import ar.com.hipotecario.backend.servicio.api.seguro.Seguros.Seguro;
import ar.com.hipotecario.backend.servicio.api.productos.SegurosV4;
import ar.com.hipotecario.canal.officebanking.ContextoOB;

import java.util.List;

public class ApiSeguro extends Api {

	/* ========== CONSTANTES ========== */
	public static String API = "seguro";

	/* ========== SERVICIOS ========== */
	// GET /v1/token-salesforce
	public static Futuro<String> getToken(Contexto contexto) {
		return futuro(() -> Seguros.get(contexto));
	}

	// GET /v1/{cuit}/obtenerProductos

	// GET /v1/{cuit}/ramo-productos

	// GET /v1/ofertas/{sessionId}/
	public static Futuro<Seguro> getOferta(Contexto contexto, String sessionId) {
		return futuro(() -> Seguros.getOferta(contexto, sessionId));
	}
}
