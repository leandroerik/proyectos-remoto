package ar.com.hipotecario.mobile.servicio;

import ar.com.hipotecario.mobile.ConfigMB;
import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.conector.ApiMB;
import ar.com.hipotecario.mobile.conector.ApiRequestMB;
import ar.com.hipotecario.mobile.conector.ApiResponseMB;

public class UtilPersona {

	public static ApiResponseMB actualizarPersona(ContextoMB contexto, String idSituacionVivienda, String idVersionDocumento) {
		ApiRequestMB request = ApiMB.request("ActualizarPersona", "personas", "PATCH", "/personas/{id}", contexto);
		request.header("x-usuario", ConfigMB.string("configuracion_usuario"));
		request.path("id", contexto.persona().cuit());
		request.body("idSituacionVivienda", idSituacionVivienda);
		request.body("idVersionDocumento", idVersionDocumento);
		return ApiMB.response(request, contexto.idCobis());
	}

	/* ========== EMAIL ========== */
	public static ApiResponseMB crearEmail(ContextoMB contexto, String email, String tipo) {
		ApiRequestMB request = ApiMB.request("CrearEmail", "personas", "POST", "/personas/{id}/mails", contexto);
		request.header("x-usuario", ConfigMB.string("configuracion_usuario"));
		request.path("id", contexto.persona().cuit());
		request.body("canalModificacion", "HB");
		request.body("usuarioModificacion", ConfigMB.string("configuracion_usuario"));
		request.body("idTipoMail", tipo);
		request.body("direccion", email);
		// TODO: guardar cambios de datos del usuario
		// return Api.response(request, contexto.idCobis());
		ApiResponseMB response = ApiMB.response(request, contexto.idCobis());
		if (!response.hayError()) {
			contexto.insertarContador("CAMBIO_MAIL");
		}
		return response;
	}

	public static ApiResponseMB crearEmailParticular(ContextoMB contexto, String email) {
		return crearEmail(contexto, email, "EMP");
	}

	/* ========== TELEFONO ========== */
	public static ApiResponseMB crearTelefono(ContextoMB contexto, String codigoPais, String codigoArea, String caracteristica, String numero, String tipo) {
		ApiRequestMB request = ApiMB.request("CrearTelefono", "personas", "POST", "/personas/{id}/telefonos", contexto);
		request.header("x-usuario", ConfigMB.string("configuracion_usuario"));
		request.path("id", contexto.persona().cuit());
		request.body("canalModificacion", "HB");
		request.body("usuarioModificacion", ConfigMB.string("configuracion_usuario"));
		request.body("idTipoTelefono", tipo);
		request.body("codigoPais", codigoPais);
		request.body("codigoArea", codigoArea);
		request.body("caracteristica", caracteristica);
		request.body("numero", numero);
		// TODO: guardar cambios de datos del usuario
		// return Api.response(request, contexto.idCobis());
		ApiResponseMB response = ApiMB.response(request, contexto.idCobis());
		if (!response.hayError()) {
			contexto.insertarContador("CAMBIO_TELEFONO");
		}
		return response;
	}

	public static ApiResponseMB crearTelefonoLaboral(ContextoMB contexto, String codigoPais, String codigoArea, String caracteristica, String numero) {
		return crearTelefono(contexto, codigoPais, codigoArea, caracteristica, numero, "L");
	}

	/* ========== DOMICILIO ========== */
	public static ApiResponseMB crearDomicilio(ContextoMB contexto, String calle, String numero, String piso, String departamento, String entreCalle1, String entreCalle2, Integer idPais, Integer idProvincia, Integer idCiudad, String codigoPostal, String tipo) {
		ApiRequestMB request = ApiMB.request("CrearDomicilio", "personas", "POST", "/personas/{id}/domicilios", contexto);
		request.header("x-usuario", ConfigMB.string("configuracion_usuario"));
		request.path("id", contexto.persona().cuit());
		request.body("canalModificacion", "HB");
		request.body("usuarioModificacion", ConfigMB.string("configuracion_usuario"));
		request.body("idTipoDomicilio", tipo);
		request.body("calle", calle);
		request.body("numero", numero);
		request.body("piso", piso);
		request.body("departamento", departamento);
		request.body("calleEntre1", entreCalle1);
		request.body("calleEntre2", entreCalle2);
		request.body("idPais", idPais);
		request.body("idProvincia", idProvincia);
		request.body("idCiudad", idCiudad);
		request.body("idCodigoPostal", codigoPostal);
		return ApiMB.response(request, contexto.idCobis());
	}

	public static ApiResponseMB crearDomicilioLaboral(ContextoMB contexto, String calle, String numero, String piso, String departamento, String entreCalle1, String entreCalle2, Integer idPais, Integer idProvincia, Integer idCiudad, String codigoPostal) {
		return crearDomicilio(contexto, calle, numero, piso, departamento, entreCalle1, entreCalle2, idPais, idProvincia, idCiudad, codigoPostal, "LA");
	}
}
