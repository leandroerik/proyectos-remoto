package ar.com.hipotecario.backend.servicio.api.cuentas;

import java.math.BigDecimal;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;

public class LimitesCuentaDisponible extends ApiObjeto {

	/* ========== ATRIBUTOS ========== */
	public BigDecimal importe;

	/* ========== SERVICIOS ========== */
	// API-Cuentas_ConsultaLimiteTransferenciaEspecialDisponible
	static LimitesCuentaDisponible get(Contexto contexto, String idcuenta, String idCliente, Fecha fecha, String idMoneda, String importe) {
		ApiRequest request = new ApiRequest("CuentasLimitesDisponibles", "cuentas", "GET", "/v1/cuentas/{idcuenta}/limites/disponible", contexto);

		request.path("idcuenta", idcuenta);
		request.query("idcliente", idCliente);
		request.query("fecha", fecha.string("yyyy-MM-dd"));
		request.query("idmoneda", idMoneda);
		request.query("importe", importe);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(LimitesCuentaDisponible.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		Fecha fechaDesde = new Fecha("2019-09-18", "yyyy-MM-dd");
		LimitesCuentaDisponible datos = get(contexto, "404500000745801", "133366", fechaDesde, "80", "0");
		imprimirResultado(contexto, datos);
	}
}
