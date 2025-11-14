package ar.com.hipotecario.mobile.servicio;

import java.math.BigDecimal;

import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.conector.ApiMB;
import ar.com.hipotecario.mobile.conector.ApiRequestMB;
import ar.com.hipotecario.mobile.conector.ApiResponseMB;

public class TransferModoService {

	public ApiResponseMB invoqueModoTransfer(ContextoMB contexto, String recipientPhone) {
		ApiRequestMB request = ApiMB.request("CreateTransfer", "modo", "POST", "/v1/public/transfers", contexto);
		request.body("id_cobis", contexto.idCobis());
		request.body("source_cbu", contexto.parametros.string("source_cbu"));
		request.body("account_linked", contexto.parametros.bool("account_linked"));
		request.body("recipient_phone", recipientPhone);
		request.body("amount", new BigDecimal(contexto.parametros.string("amount")));
		request.body("reason_code", contexto.parametros.string("reason_code"));
		request.body("resp_mock", contexto.parametros.bool("resp_mock"));
		request.headers.put("DimoToken", "Bearer " + contexto.sesion().cache("access_token"));
		request.headers.put("x-subCanal", "MB-MODO");
		return ApiMB.response(request, contexto.idCobis());
	}

}
