package ar.com.hipotecario.backend.servicio.api.tarjetasCredito;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Vencimientos extends ApiObjeto{
	
	public ExpirationDates expiration_dates;
    public ClosingDates closing_dates;

    public static class ExpirationDates {
        public String next_expiration_date;
        public String expiration_date;
        public String previous_expiration_date;
    }

    public static class ClosingDates {
        public String closing_date;
        public String previous_closing_date;
        public String next_closing_date;
    }
	
	public static Vencimientos get(Contexto contexto, String cuenta,String idPrisma) {
		ApiRequest request = new ApiRequest("CuentasPrisma", "tarjetascredito", "GET", "/v1/tarjetascredito/vencimientos", contexto);
		request.query("idPrisma", idPrisma);
		request.query("cuenta", cuenta);

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(Vencimientos.class);
	}
}

