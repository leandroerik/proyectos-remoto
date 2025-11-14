package ar.com.hipotecario.backend.servicio.api.viviendas;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.conector.api.Api;

public class ApiViviendas extends Api {
	/* ========== CONSTANTES ========== */
	public static String API = "viviendas";
	public static String DATOS = "Datos";
	public static String X_HANDLE = "X-Handle";

	/* ========== SERVICIOS ========== */

	// validaciones
	public static Futuro<ConsultaPersona> validaciones(Contexto contexto, String numeroTramite, String numeroDocumento, String sexo) {
		return futuro(() -> Validaciones.get(contexto, numeroTramite, numeroDocumento, sexo));
	}
}
