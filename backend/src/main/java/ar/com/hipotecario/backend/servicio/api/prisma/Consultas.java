package ar.com.hipotecario.backend.servicio.api.prisma;

import java.util.List;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.prisma.Consultas.Consulta;

public class Consultas extends ApiObjetos<Consulta> {

	/* ========== ATRIBUTOS ========== */
	public static class Consulta extends ApiObjeto {
		public String codigoTema;
		public String descripcionTema;
		public String idTemaPadre;
		public String ultimoNivel;
		public String idGrupo;
		public String tipoItem;
		public String diasEstimResol;
		public String diasAvisoVto;
		public String diasTopeReap;
		public String notifica;
		public String vigente;
		public String diasAnalista;
		public String diasSupervisor;
		public String diasLider;
		public String diasGerente;
		public String idProducto;
	}

	/* ========== SERVICIOS ========== */
	// API-Prisma_ConsultaTemas
	public static Consultas get(Contexto contexto) {
		return get(contexto, null, null, null, null, null, "50");
	}

	public static Consultas get(Contexto contexto, String idTema) {
		return get(contexto, idTema, null, null, null, null, "50");
	}

	public static Consultas get(Contexto contexto, String idTema, String descripcionTema, String idProducto, List<String> vigente, String codTema, String maxResults) {
		ApiRequest request = new ApiRequest("ConsultaPrisma", "prisma", "GET", "/v1/consultaTema", contexto);
		request.query("maxResults", maxResults);
		if (idTema != null)
			request.query("idTema", idTema);
		if (descripcionTema != null)
			request.query("descripcionTema", descripcionTema);
		if (idProducto != null)
			request.query("idProducto", idProducto);
		if (vigente != null)
			request.query("vidente", vigente);
		if (codTema != null)
			request.query("codTema", codTema);

		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(Consultas.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		Consultas datos = get(contexto, "48");
		imprimirResultado(contexto, datos);
	}
}
