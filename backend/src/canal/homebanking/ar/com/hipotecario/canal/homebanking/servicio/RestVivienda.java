package ar.com.hipotecario.canal.homebanking.servicio;

import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.conector.Api;
import ar.com.hipotecario.canal.homebanking.conector.ApiRequest;
import ar.com.hipotecario.canal.homebanking.conector.ApiResponse;

public class RestVivienda {

	public static ApiResponse validarRenaper(ContextoHB contexto, String numeroTramite, String numeroDocumento, String sexo) {
		ApiRequest request = Api.request("ValidacionRenaper", "viviendas", "GET", "/v1/validaciones", contexto);
		request.query("idtramite", numeroTramite);
		request.query("dni", numeroDocumento);
		request.query("sexo", sexo);
		return Api.response(request);
	}

	public static ApiResponse liberacionHipotecaAutomatico(ContextoHB contexto, String nroPrestamo, String garantia, String codCiudadGarantia, String ciudadGarantia, String nroPrestamoMig) {
		ApiRequest request = Api.request("API-Viviendas_LiberacionHipoteca", "viviendas", "POST", "/v1/liberaciones", contexto);
		request.body("nroPrestamo", nroPrestamo);
		request.body("cuilTitular", contexto.persona().cuit());
		request.body("nombreApellidoTitular", contexto.persona().nombreCompleto());
		request.body("idCobisTitular", contexto.idCobis());
		request.body("sucursal", String.valueOf(Integer.parseInt(nroPrestamo.substring(0,3))));
		request.body("garantia", garantia);
		request.body("codigoCiudad", codCiudadGarantia);
		request.body("ciudad", ciudadGarantia);
		request.body("nroPrestamoMigrado", nroPrestamoMig);
		request.body("enviaNotificacionesCliente", "No");
		request.body("paso", "");
		request.body("numeroCasoCrm", "");

		ApiResponse response = Api.response(request);
		return response;
	}
}
