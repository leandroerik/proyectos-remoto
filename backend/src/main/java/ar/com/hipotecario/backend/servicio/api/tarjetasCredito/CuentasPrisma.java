package ar.com.hipotecario.backend.servicio.api.tarjetasCredito;

import ar.com.hipotecario.backend.Contexto;

import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CuentasPrisma extends ApiObjeto {
	
	public Company company;
	
	public static class Company {
		public String id;
	    public String denomination;
	    public List<Account> accounts;

	    public static class Account {

	        public long account_number;
	        public String product;
	        public String product_denomination;
	        public String account_denomination;
	        public Totals totals;

	        public static class Totals {

	            public double dollar_account_purchases;
	            public double other_concepts;
	            public double dollar_other_concepts;
	            public double credits;
	            public double dollarAdvances;
	            public double advances;
	            public double dollar_instalments;
	            public double account_purchases;
	            public double dollar_credits;
	            public double instalments;

	        }
	    }
	}
	
	public static CuentasPrisma get(Contexto contexto, String idPrisma) {
		ApiRequest request = new ApiRequest("CuentasPrisma", "tarjetascredito", "GET", "/v1/cuentas/visa", contexto);
		request.query("idPrisma", idPrisma);

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(CuentasPrisma.class);
	}

}
