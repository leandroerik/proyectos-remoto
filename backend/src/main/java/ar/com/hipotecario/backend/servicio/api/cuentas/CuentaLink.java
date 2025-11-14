package ar.com.hipotecario.backend.servicio.api.cuentas;

import java.util.ArrayList;
import java.util.List;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;

// API-Cuentas_ConsultaCuentaIdcliente
public class CuentaLink extends ApiObjeto {

	public static String PESOS = "80";
	public static String DOLARES = "2";

	/* ========== ATRIBUTOS ========== */
	public String cuenta;
	public String cbu;
	public String moneda;
	public String tipoProducto;
	public String estadoCuenta;
	public Boolean esTransaccional;
	public String codigoBancoDestino;
	public String redDestino;
	public String tipoPersona;
	public String cuentaPBF;
	public String nombreBancoDestino;
	public List<Titular> titulares = new ArrayList<>();

	public static class Titular extends ApiObjeto {
		public String denominacion;
		public String idTributario;
	}

	/* ========== SERVICIOS ========== */
	static CuentaLink get(Contexto contexto, String cbu) {
		return get(contexto, cbu, PESOS, "0");
	}

	static CuentaLink get(Contexto contexto, String cbu, String idMoneda) {
		return get(contexto, cbu, idMoneda, "0");
	}

	static CuentaLink get(Contexto contexto, String cbu, String idMoneda, String numeroTarjetaDebito) {
		ApiRequest request = new ApiRequest("CuentaLink", "cuentas", "GET", "/v1/cuentas", contexto);
		request.query("cbu", cbu);
		request.query("idcliente", contexto.sesion().idCobis);
		request.query("numerotarjeta", numeroTarjetaDebito);
		request.query("idmoneda", idMoneda);
		request.query("consultaalias", false);
		if(!contexto.esProduccion()){
			request.header("x-usuarioIP", "10.99.87.114");
		}else{
			request.header("x-usuarioIP", contexto.ip());
		}
		request.cache = false;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf("CBU_INVALIDA", response.contains("CBU ingresada inválida"), request, response);
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(CuentaLink.class);
	}

	/* ========== METODOS ========== */
	public String cuitTitular() {
		if (!titulares.isEmpty()) {
			return titulares.get(0).idTributario;
		}
		return "";
	}

	public String nombreTitular() {
		if (!titulares.isEmpty()) {
			return titulares.get(0).denominacion;
		}
		return "";
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		CuentaLink datos = get(contexto, "044004553000000022494");
		imprimirResultado(contexto, datos);
	}
}
