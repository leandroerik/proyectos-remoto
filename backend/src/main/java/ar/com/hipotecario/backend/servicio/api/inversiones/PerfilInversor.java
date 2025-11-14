package ar.com.hipotecario.backend.servicio.api.inversiones;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.personas.ApiPersonas;

public class PerfilInversor extends ApiObjeto {

	public enum EnumOperacionPI {
		INSERTAR("I"), ACTUALIZAR("U");

		private String codigo;

		EnumOperacionPI(String codigo) {
			this.codigo = codigo;
		}

		public String getCodigo() {
			return codigo;
		}

		public void setCodigo(String codigo) {
			this.codigo = codigo;
		}
	}

	public Long idCliente;
	public String tipoPersona;
	public Integer perfilInversor;
	public Fecha fechaAM;
	public Fecha fechaFin;
	public String estado;

	/* ========== SERVICIOS ========== */

	// API-Personas_ConsultaPerfilInversorCliente
	public static PerfilInversor get(Contexto contexto, String idCliente) {
		ApiRequest request = new ApiRequest("Inversiones", ApiPersonas.API, "GET", "/perfilInversor", contexto);
		request.query("clientes", idCliente);
		request.cache = false;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(PerfilInversor.class, response.objetos().get(0));
	}

	public static PerfilInversor post(Contexto contexto, String idCliente, EnumOperacionPI operacion, Integer perfilInversor) {
		ApiRequest request = new ApiRequest("Inversiones", ApiPersonas.API, "POST", "/administrarPerfil", contexto);
		request.body("ente", idCliente);
		request.body("estado", "A");
		request.body("fuenteOrigen", "W");
		request.body("fechaAMPerfil", Fecha.hoy().string("yyyy-MM-dd"));
		request.body("fechaFin", Fecha.hoy().sumarAños(1).string("yyyy-MM-dd")); // TODO.: VER LAS FECHAS
		request.body("operacion", operacion.codigo);
		request.body("perfilInversor", perfilInversor);
		request.cache = false;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf("YA_TIENE_PERFIL", response.contains("EL CLIENTE INGRESADO YA TIENE PERFIL INVERSOR"), request, response);
		ApiException.throwIf(!response.http(200), request, response);

		return response.crear(PerfilInversor.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		String test = "post";
		String idCliente = "4373070";

		Contexto contexto = contexto("HB", "desarrollo");
		if (test.equals("get")) {
			PerfilInversor datos = get(contexto, idCliente);
			imprimirResultado(contexto, datos);
		} else if (test.equals("post")) {
			post(contexto, idCliente, EnumOperacionPI.ACTUALIZAR, 2);
		}
	}
}