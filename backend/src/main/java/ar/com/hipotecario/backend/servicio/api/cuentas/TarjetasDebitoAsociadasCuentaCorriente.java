package ar.com.hipotecario.backend.servicio.api.cuentas;

import java.math.BigDecimal;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.cuentas.TarjetasDebitoAsociadasCuentaCorriente.TarjetaDebitoAsociadasCuentaCorriente;

public class TarjetasDebitoAsociadasCuentaCorriente extends ApiObjetos<TarjetaDebitoAsociadasCuentaCorriente> {

	/* ========== ATRIBUTOS ========== */
	public static class TarjetaDebitoAsociadasCuentaCorriente extends ApiObjeto {
		public String numeroTarjeta;
		public String tipoTarjeta;
		public String tipoTarjetaNombre;
		public String estado;
		public String estadoTarjetaDesc;
		public String oficina;
		public Integer cantAdicionales;
		public BigDecimal limiteExtraccion;
	}

	/* ========== SERVICIOS ========== */
	// API-Cuentas_ConsultaCuentaCorrienteTarjetasDebitoVinculadas
	static TarjetasDebitoAsociadasCuentaCorriente get(Contexto contexto, String idCuenta) {
		ApiRequest request = new ApiRequest("CuentasCuentaCorrienteTarjetasDebitos", "cuentas", "GET", "/v1/cuentascorrientes/{idcuenta}/tarjetasdebito", contexto);
		request.path("idcuenta", idCuenta);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(TarjetasDebitoAsociadasCuentaCorriente.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		TarjetasDebitoAsociadasCuentaCorriente datos = get(contexto, "304500000022494");
		imprimirResultado(contexto, datos);
	}
}
