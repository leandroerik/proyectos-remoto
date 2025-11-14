package ar.com.hipotecario.backend.servicio.api.cuentas;

import java.util.ArrayList;
import java.util.List;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;

// API-Cuentas_ConsultaCuentaIdcliente
public class CuentaCoelsa extends ApiObjeto {

	/* ========== ATRIBUTOS ========== */
	public String cbu;
	public String aliasValorOriginal;
	public String nombreTitular;
	public String cuit;
	public String nroBco;
	public Boolean ctaActiva;
	public String nuevoAlias;
	public String transaccion;
	public Boolean esTransaccional;
	public String tipoCuenta;
	public String tipoPersona;
	public List<Cotitular> cotitulares = new ArrayList<>();

	public class Cotitular {
		public String tipo_persona;
		public String nombre;
		public String cuit;
	}

	/* ========== SERVICIOS ========== */
	public static CuentaCoelsa get(Contexto contexto, String cbu, String alias) {
		ApiRequest request = new ApiRequest("CuentaCoelsa", "cuentas", "GET", "/v1/cuentas", contexto);
		request.query("cbu", cbu);
		request.query("alias", alias);
		request.query("consultaalias", true);
		request.query("acuerdo", false);
		request.header("x-usuarioIP", contexto.ip());
		request.cache = false;

		ApiResponse response = request.ejecutar();

		ApiException.throwIf("CBU_INEXISTENTE", response.contains("CBU NO EXISTE"), request, response);
		ApiException.throwIf("CBU_INCORRECTO", response.contains("CBU INCORRECTO"), request, response);
		ApiException.throwIf("ALIAS_INEXISTENTE", response.contains("ALIAS NO EXISTE"), request, response);
		ApiException.throwIf("ALIAS_NOASIGNADO", response.contains("NO TIENE ALIAS ASIGNADO"), request, response);

		ApiException.throwIf("ALIAS_INCORRECTO", response.contains("ALIAS MAL FORMULADO"), request, response);
		ApiException.throwIf("ALIAS_ELIMINADO", response.contains("EL ALIAS SE ENCUENTRA ELIMINADO"), request, response);
		ApiException.throwIf("CUENTA_INACTIVA", response.contains("CBU ENCONTRADO CON ALIAS ASIGNADO, PERO LA CUENTA NO ESTÁ ACTIVA"), request, response);

		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(CuentaCoelsa.class);
	}
	
	public static CuentaCoelsa get(Contexto contexto, String cbu) {
		ApiRequest request = new ApiRequest("CuentaCoelsa", "cuentas", "GET", "/v1/cuentas", contexto);
		request.query("cbu", cbu);
		request.query("consultaalias", true);
		request.query("acuerdo", false);
		request.header("x-usuarioIP", contexto.ip());
		request.cache = false;

		ApiResponse response = request.ejecutar();

		ApiException.throwIf("CBU_INEXISTENTE", response.contains("CBU NO EXISTE"), request, response);
		ApiException.throwIf("CBU_INCORRECTO", response.contains("CBU INCORRECTO"), request, response);
		ApiException.throwIf("ALIAS_INEXISTENTE", response.contains("ALIAS NO EXISTE"), request, response);
		ApiException.throwIf("ALIAS_NOASIGNADO", response.contains("NO TIENE ALIAS ASIGNADO"), request, response);

		ApiException.throwIf("ALIAS_INCORRECTO", response.contains("ALIAS MAL FORMULADO"), request, response);
		ApiException.throwIf("ALIAS_ELIMINADO", response.contains("EL ALIAS SE ENCUENTRA ELIMINADO"), request, response);
		ApiException.throwIf("CUENTA_INACTIVA", response.contains("CBU ENCONTRADO CON ALIAS ASIGNADO, PERO LA CUENTA NO ESTÁ ACTIVA"), request, response);

		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(CuentaCoelsa.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		CuentaCoelsa datos = get(contexto, "0440054740003082706299", "");
		imprimirResultado(contexto, datos);
	}
}
