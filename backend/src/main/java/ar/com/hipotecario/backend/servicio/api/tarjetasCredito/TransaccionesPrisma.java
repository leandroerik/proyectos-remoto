package ar.com.hipotecario.backend.servicio.api.tarjetasCredito;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;

import java.util.List;
import ar.com.hipotecario.canal.officebanking.dto.prisma.TransaccionesRequest;
import ar.com.hipotecario.backend.base.Objeto;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransaccionesPrisma extends ApiObjeto{
	
	@JsonProperty("card_details")
    public CardDetails card_details;

    public static class CardDetails {
        public Limits limits;
        public Availables availables;
        public List<Movements> movements;
        @JsonProperty("card_number")
        public String card_number;
        @JsonProperty("card_denomination")
        public String card_denomination;
        @JsonProperty("one_payment_exception")
        public OnePaymentException one_payment_exception;
        @JsonProperty("instalments_exception")
        public InstalmentsException instalments_exception;
        
        public static class Limits {
            public double purchases; 
            public double instalments; 
        }
        
        public static class Availables {
            public double purchases;   
            public double instalments;  
        }
        
        public static class Movements {
            @JsonProperty("date")
            public String date;
            @JsonProperty("merchant_denomination")
            public String merchant_denomination;
            @JsonProperty("merchants_category_denomination")
            public String merchants_category_denomination;
            @JsonProperty("transaction_receipt_amount")
            public double transaction_receipt_amount;
            @JsonProperty("transaction_receipt_dollar_amount")
            public double transaction_receipt_dollar_amount;
            @JsonProperty("instalment_number")
            public int instalment_number;
            @JsonProperty("transaction_type")
            public String transaction_type;
            @JsonProperty("number_of_instalments")
            public int number_of_instalments;
            @JsonProperty("transaction_receipt_number")
            public String transaction_receipt_number;
        }
        
        public static class OnePaymentException {
            @JsonProperty("limit")
            public double limit;
            @JsonProperty("from_date")
            public String from_date;
            @JsonProperty("to_date")
            public String to_date;
        }
        
        public static class InstalmentsException {
            @JsonProperty("limit")
            public double limit;
            @JsonProperty("from_date")
            public String from_date;
            @JsonProperty("to_date")
            public String to_date;
        }
    }
	
	public static TransaccionesPrisma post(Contexto contexto, TransaccionesRequest body) {
		ApiRequest request = new ApiRequest("ListadoTarjetasEmpresa", "tarjetascredito", "POST", 
		"/v1/tarjetascredito/transacciones", contexto);
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
		String jsonBody;
		try {
			jsonBody = objectMapper.writeValueAsString(body);
            System.out.println("jsonBody generado para el POST: " + jsonBody);
			Objeto obj = Objeto.fromJson(jsonBody);
			request.body(obj);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(TransaccionesPrisma.class);
	}
	
}
