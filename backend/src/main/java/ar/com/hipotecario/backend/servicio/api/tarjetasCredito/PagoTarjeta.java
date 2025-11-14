package ar.com.hipotecario.backend.servicio.api.tarjetasCredito;

import java.math.BigDecimal;
import java.util.HashMap;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.canal.officebanking.dto.ErrorGenericoOB;

public class PagoTarjeta extends ApiObjeto{

	public String nroTicket;
	public ErrorGenericoOB error = new ErrorGenericoOB();
	
	public static PagoTarjeta post(Contexto contexto, HashMap<String, String> dto, BigDecimal importe, String canal) {
		ApiRequest request = new ApiRequest("pagoTarjetaPost", "servicios", "POST", "/api/tarjeta/pagoTarjeta",
                contexto);

        request.body("cuenta", dto.get("cuenta"));
        request.body("cuentaTarjeta", dto.get("cuentaTarjeta"));
        request.body("importe", importe);
        request.body("moneda", dto.get("moneda"));
        request.body("tipoTarjeta", dto.get("tipoTarjeta"));
        request.body("tipoCuenta", dto.get("tipoCuenta"));
        request.body("numeroTarjeta", dto.get("numeroTarjeta"));
        
        request.header("x-canal", canal);
        request.header("x-sistema", canal);
        
        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200), request, response);
        
        // Registro a base auditor
        // ...
        
		return response.crear(PagoTarjeta.class);
	}
	
	
}
