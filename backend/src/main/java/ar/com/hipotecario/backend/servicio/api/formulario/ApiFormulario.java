package ar.com.hipotecario.backend.servicio.api.formulario;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.conector.api.Api;

public class ApiFormulario extends Api {

	/* ========== CONSTANTES ========== */
	public static String API = "formularios";

	/* ========== SERVICIOS ========== */
	public static Futuro<String> get(Contexto contexto, String idSolicitud, String grupoCodigo) {
		return futuro(() -> Formularios.get(contexto, idSolicitud, grupoCodigo));
	}

}
