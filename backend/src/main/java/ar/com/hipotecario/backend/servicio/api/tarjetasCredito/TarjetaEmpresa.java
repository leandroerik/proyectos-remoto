package ar.com.hipotecario.backend.servicio.api.tarjetasCredito;

import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.dto.prisma.TransaccionesRequest;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TarjetaEmpresa extends ApiObjeto{
	
	public Account account;

    public static class Account {
        public String account_number;
        public String product;
        public String product_denomination;
        public String account_denomination;
        public List<Card> cards;
        public Availables availables;
        public Totals totals;
        public Limits limits;
    }

    public static class Card {
        public String product;
        public String estado;
        public String card_denomination;
        public String product_denomination;
        public String card_number;
        public String scheme;
        public Client client;
        public Totals totals;
        public TransaccionesPrisma transactions;
    }

    public static class Client {
        public String document_number;
        public String gender;
        public String document_type;
    }

    public static class Availables {
        public double purchases;
        public double advances;
        public double instalments;
    }

    public static class Totals {
        public double dollar_account_purchases;
        public double other_concepts;
        public double dollar_other_concepts;
        public double credits;
        public double dollar_advances;
        public double advances;
        public double dollar_instalments;
        public double account_purchases;
        public double dollar_credits;
        public double instalments;
    }

    public static class Limits {
        public double financiation;
        public double purchases;
        public double advances;
        public double instalments;
    }
    
    public static TarjetaEmpresa get(ContextoOB contexto, String cuenta, String  idPrisma) {
		ApiRequest request = new ApiRequest("ListadoTarjetasEmpresa", "tarjetascredito", "GET", "/v1/tarjetascredito/tarjetaEmpresa", contexto);
		
		request.query("cuenta", cuenta);
		request.query("idPrisma", idPrisma);
		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		TarjetaEmpresa tarjetas = response.crear(TarjetaEmpresa.class);
		
		return tarjetas;
	}
    
}

