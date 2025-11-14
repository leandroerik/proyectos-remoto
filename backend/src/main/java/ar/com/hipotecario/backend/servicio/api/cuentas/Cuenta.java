package ar.com.hipotecario.backend.servicio.api.cuentas;

import java.util.List;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;

public class Cuenta extends ApiObjeto {

	public class Titulares {
		public String denominacion;
		public String idTributario;
	}

	/* ========== ATRIBUTOS ========== */
	public String cuenta;
	public String cbu;
	public String aliasValorOriginal;
	public String nombreTitular;
	public String cuit;
	public String nroBco;
	public String ctaActiva;
	public String tipoCuenta;
	public String moneda;
	public String tipoProducto;
	public String estadoCuenta;
	public String codigoBancoDestino;
	public String redDestino;
	public String tipoPersona;
	public String cuentaPBF;
	public String nombreBancoDestino;
	public Boolean esTransaccional;
	public List<Titulares> titulares;

	/* ========== SERVICIOS ========== */
	// API-Cuentas_ConsultaCuentaIdcliente
	static Cuenta get(Contexto contexto, String cbu, String idMoneda, String nroTarjeta) {
		return get(contexto, cbu, idMoneda, nroTarjeta, true, false);
	}

	static Cuenta get(Contexto contexto, String cbu, String idMoneda, String nroTarjeta, Boolean consultaAlias, Boolean acuerdo) {
		ApiRequest request = new ApiRequest("CuentaCoelsa", "cuentas", "GET", "/v1/cuentas", contexto);
		request.query("cbu", cbu);
		request.query("idmoneda", "2");
		request.query("consultaalias", consultaAlias);
		request.query("acuerdo", acuerdo);
		request.query("numerotarjeta", nroTarjeta);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(Cuenta.class);
	}

	public static Cuenta get(Contexto contexto, String cbu, String alias, Boolean consultaAlias, Boolean acuerdo) {
		ApiRequest request = new ApiRequest("CuentaCoelsa", "cuentas", "GET", "/v1/cuentas", contexto);
		request.query("cbu", cbu);
		request.query("alias", alias);
		request.query("consultaalias", consultaAlias);
		request.query("acuerdo", acuerdo);
		request.cache = false;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf("CBU_INEXISTENTE", response.contains("CBU NO EXISTE"), request, response);
		ApiException.throwIf("CBU_INCORRECTO", response.contains("CBU INCORRECTO"), request, response);
		ApiException.throwIf("ALIAS_INEXISTENTE", response.contains("ALIAS NO EXISTE"), request, response);
		ApiException.throwIf("ALIAS_INCORRECTO", response.contains("ALIAS MAL FORMULADO"), request, response);
		ApiException.throwIf("ALIAS_ELIMINADO", response.contains("EL ALIAS SE ENCUENTRA ELIMINADO"), request, response);
		ApiException.throwIf("CUENTA_INACTIVA", response.contains("CBU ENCONTRADO CON ALIAS ASIGNADO, PERO LA CUENTA NO ESTÁ ACTIVA"), request, response);
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(Cuenta.class);
	}

	public String descripcion() {
		String descripcion = "";
		descripcion = "AHO".equals(tipoProducto) ? "Caja de Ahorro" : descripcion;
		descripcion = "CTE".equals(tipoProducto) ? "Cuenta Corriente" : descripcion;
		return descripcion;
	}

	public String descripcionCorta() {
		String descripcion = "";
		descripcion = "AHO".equals(tipoProducto) ? "CA" : descripcion;
		descripcion = "CTE".equals(tipoProducto) ? "CC" : descripcion;
		return descripcion;
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "desarrollo");
		// Cuenta datos = get(contexto, "2810001221001098480014", "80",
		// "4998590015392208");
		Cuenta data = get(contexto, "0440000420000196164094", null, false, false);
		imprimirResultado(contexto, data);
		// imprimirResultado(contexto, datos);
	}
}
