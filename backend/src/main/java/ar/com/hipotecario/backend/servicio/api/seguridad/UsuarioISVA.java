package ar.com.hipotecario.backend.servicio.api.seguridad;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;

public class UsuarioISVA extends ApiObjeto {

	/* ========== ATRIBUTOS ========== */
	public boolean tieneSoftToken;
	public String idISVA;
	public String idcliente;
	public String grupo;
	public Boolean tieneClaveDefault;
	public Boolean tieneClaveNumerica;
	public String fechaVencimientoClaveBuho;

	/* ========== SERVICIOS ========== */
	// API-Seguridad_ConsultaUsuarioIDG
	public static UsuarioISVA get(Contexto contexto, String idCliente) {
		return get(contexto, idCliente, contexto.canal);
	}

	public static UsuarioISVA get(Contexto contexto, String idCliente, String grupo) {
		ApiRequest request = new ApiRequest("UsuarioISVA", "seguridad", "GET", "/v1/usuario", contexto);
		request.query("idcliente", idCliente);
		request.query("grupo", grupo);

		ApiResponse response = request.ejecutar();
		ApiException.throwIf("USUARIO_NO_ENCONTRADO", response.http(204) || response.http(404) || "USER_NOT_EXIST".equals(response.string("codigo")), request, response);
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(UsuarioISVA.class, response);
	}

	public static UsuarioISVA post(Contexto contexto, String idCliente, String nombreCompleto) {
		ApiRequest request = new ApiRequest("CrearUsuarioIDG", "seguridad", "POST", "/v1/usuario", contexto);
		request.body("idcliente", idCliente);

		ApiResponse response = request.ejecutar();
		ApiException.throwIf("USUARIO_EXISTE", response.http(400) || response.http(404) || "USER_EXIST".equals(response.string("codigo")), request, response);
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(UsuarioISVA.class, response);
	}

	public static ApiObjeto crearClave(Contexto contexto, String idCobis, String clave, String tipo) {
		ApiRequest request = new ApiRequest("CrearClave", "seguridad", "POST", "/v1/clave", contexto);
		request.body("grupo", "ClientesBH");
		request.body("idUsuario", idCobis);
		request.body("parametros.clave", clave);
		if (tipo != null) {
			request.body("nombreClave", tipo);
		}

		ApiResponse response = request.ejecutar();
		ApiException.throwIf("USUARIO_FORMATO_INVALIDO", response.contains("La clave ingresada no cumple") && tipo == null, request, response);
		ApiException.throwIf("CLAVE_FORMATO_INVALIDO", response.contains("La clave ingresada no cumple") && tipo != null, request, response);
		ApiException.throwIf(!response.http(200, 202), request, response);
		return response.crear(ApiObjeto.class);
	}

	public static ApiObjeto cambiarClave(Contexto contexto, String idCobis, String clave, String tipo) {
		ApiRequest request = new ApiRequest("CambiarClave", "seguridad", "PUT", "/v1/clave", contexto);
		request.body("grupo", "ClientesBH");
		request.body("idUsuario", idCobis);
		request.body("parametros.clave", clave);
		if (tipo != null) {
			request.body("nombreClave", tipo);
		}

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 202), request, response);
		return response.crear(ApiObjeto.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) throws InterruptedException {
		Contexto contexto = contexto("OB", "desarrollo");
		UsuarioISVA datos = get(contexto, "6592030", "");
		imprimirResultado(contexto, datos);
	}
}