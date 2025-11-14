package ar.com.hipotecario.backend.servicio.api.empresas;

import java.math.BigDecimal;
import java.util.List;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;

public class GrupoEconomicoOB extends ApiObjeto {

	/* ========== ATRIBUTOS ========== */
	public String id;
	public String descripcion;
	public String banca;
	public String descripcionBanca;
	public String nombreOficial;
	public String sucursal;
	public String empresaControlante;
	public Integer totalRegistros;
	public Integer ultimoRegistro;
	public List<Cliente> clientes;

	public static class Cliente extends ApiObjeto {
		public String codigo;
		public String nombre;
		public String nroDocumento;
		public String tipo;
		public BigDecimal porcentajeParticipacion;
		public String actividad;
		public List<Producto> productos;
	}

	public static class Producto extends ApiObjeto {
		public String tipo;
	}

	/* ========== SERVICIOS ========== */
	// API-Empresas_ConsultaDeGruposEconómicosDeUnaEmpresa
	public static GrupoEconomicoOB get(Contexto contexto, String idEmpresa) {
		ApiRequest request = new ApiRequest("GrupoEconomicoOB", "empresas", "GET", "/v1/empresas/{id}/grupos", contexto);
		request.query("tipoconsulta", "P");
		request.path("id", idEmpresa);

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);

		GrupoEconomicoOB grupoEconomicoOB = response.crear(GrupoEconomicoOB.class);

		return grupoEconomicoOB;
	}

	/* ========== TEST ========== */ // 5175946
	public static void main(String[] args) throws InterruptedException {

		Contexto contexto = contexto("OB", "homologacion");
		GrupoEconomicoOB datos = get(contexto, "135706");
		imprimirResultado(contexto, datos);
	}
}
