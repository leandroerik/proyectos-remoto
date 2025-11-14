package ar.com.hipotecario.backend.servicio.api.cuentas;

import java.math.BigDecimal;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.cuentas.TarjetasDebitoAsociadasCajaAhorro.TarjetaDebitoAsociadasCajaAhorro;

public class TarjetasDebitoAsociadasCajaAhorro extends ApiObjetos<TarjetaDebitoAsociadasCajaAhorro> {

	/* ========== ATRIBUTOS ========== */
	public static class TarjetaDebitoAsociadasCajaAhorro extends ApiObjeto {
		public String numeroTarjeta;
		public String tipoTarjeta;
		public String tipoTarjetaNombre;
		public String estado;
		public String estadoTarjetaDesc;
		public String oficina;
		public BigDecimal limiteExtraccion;
		public Integer cantAdicionales;
	}

	/* ========== SERVICIOS ========== */
	// API-Cuentas_ConsultaCuentaCajasAhorrosTarjetasDebitoVinculadas
	static TarjetasDebitoAsociadasCajaAhorro get(Contexto contexto, String idCuenta) {
		ApiRequest request = new ApiRequest("CajasAhorroTarjetasDebito", "cuentas", "GET", "/v1/cajasahorros/{idcuenta}/tarjetasdebito", contexto);
		request.path("idcuenta", idCuenta);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(TarjetasDebitoAsociadasCajaAhorro.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		TarjetasDebitoAsociadasCajaAhorro datos = get(contexto, "404500000745801");
		imprimirResultado(contexto, datos);
	}
}
