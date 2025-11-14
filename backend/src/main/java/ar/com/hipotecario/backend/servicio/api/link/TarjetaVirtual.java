package ar.com.hipotecario.backend.servicio.api.link;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;

public class TarjetaVirtual extends ApiObjeto {

	/* ========== ATRIBUTOS ========== */
	public String nroTarjeta;
	private static final String CODIGO_BCRA = "044";
	private static final String PREFIJO_TARJETA_VIRTUAL = "504621";
		
	public String getNroTarjeta() {
		return nroTarjeta;
	}

	public void setNroTarjeta(String nroTarjeta) {
		this.nroTarjeta = nroTarjeta;
	}

	/* =============== SERVICIOS ================ */
	public static TarjetaVirtual post(Contexto contexto, String tvIndex) {
		ApiRequest request = new ApiRequest("LinkPostTarjetaVirtual", "link", "POST", "/v1/servicios/tarjetaVirtual", contexto);
		request.body("idEmpresa", tvIndex);

		ApiResponse response = request.ejecutar();

		ApiException.throwIf("PARAMETRO_REQUERIDO", response.contains("PARAMETRO_REQUERIDO"), request, response);
		ApiException.throwIf("PARAMETRO_INVALIDO", response.contains("PARAMETRO_INVALIDO"), request, response);

		if (response.contains("ID_EMPRESA_YA_UTILIZADO")) {
			TarjetaVirtual tarjeta = new TarjetaVirtual();
			tarjeta.setNroTarjeta(PREFIJO_TARJETA_VIRTUAL + CODIGO_BCRA + tvIndex);
			return tarjeta;
		}

		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(TarjetaVirtual.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		TarjetaVirtual tv = post(contexto, "30617280406");
		imprimirResultado(contexto, tv);
	}
}