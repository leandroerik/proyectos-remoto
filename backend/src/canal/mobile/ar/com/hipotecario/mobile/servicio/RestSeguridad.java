package ar.com.hipotecario.mobile.servicio;

import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.conector.ApiMB;
import ar.com.hipotecario.mobile.conector.ApiRequestMB;
import ar.com.hipotecario.mobile.conector.ApiResponseMB;
import ar.com.hipotecario.mobile.conector.SqlMB;
import ar.com.hipotecario.mobile.conector.SqlRequestMB;
import ar.com.hipotecario.mobile.conector.SqlResponseMB;
import ar.com.hipotecario.mobile.lib.Objeto;

public class RestSeguridad {

	/* ========== SERVICIOS ========== */
	public static ApiResponseMB usuario(ContextoMB contexto, Boolean crearUsuario) {
		ApiResponseMB response = usuario(contexto);
		if (crearUsuario && (response.codigo == 204 || "USER_NOT_EXIST".equals(response.string("codigo")) || "404".equals(response.string("codigo")))) {
			ApiResponseMB responseCrearUsuario = crearUsuario(contexto, contexto.persona().nombreCompleto());
			response = responseCrearUsuario.hayError() ? responseCrearUsuario : usuario(contexto);
		}
		return response;
	}

	public static ApiResponseMB usuario(ContextoMB contexto) {
		ApiRequestMB request = ApiMB.request("UsuarioIDG", "seguridad", "GET", "/v1/usuario", contexto);
		request.query("grupo", "ClientesBH");
		request.query("idcliente", contexto.idCobis());
		request.permitirSinLogin = true;
		ApiResponseMB response = ApiMB.response(request, contexto.idCobis());
		if (response.codigo == 204 || "USER_NOT_EXIST".equals(response.string("codigo")) || "404".equals(response.string("codigo"))) {
			response.codigo = 204;
		}
		return response;
	}

	public static ApiResponseMB usuarioCache(ContextoMB contexto) {
		ApiRequestMB request = ApiMB.request("UsuarioIDG", "seguridad", "GET", "/v1/usuario", contexto);
		request.query("grupo", "ClientesBH");
		request.query("idcliente", contexto.idCobis());
		request.cacheSesion = true;
		ApiResponseMB response = ApiMB.response(request, contexto.idCobis());
		if (response.codigo == 204 || "USER_NOT_EXIST".equals(response.string("codigo")) || "404".equals(response.string("codigo"))) {
			response.codigo = 204;
		}
		return response;
	}

	public static ApiResponseMB crearUsuario(ContextoMB contexto, String nombreCompleto) {
		ApiRequestMB request = ApiMB.request("CrearUsuarioIDG", "seguridad", "POST", "/v1/usuario", contexto);
		request.body("grupo", "ClientesBH");
		request.body("idcliente", contexto.idCobis());
		request.body("nombreCompleto", nombreCompleto);
		request.body("comentarios", "");
		request.permitirSinLogin = true;
		return ApiMB.response(request, contexto.idCobis());
	}

	public static ApiResponseMB validarUsuario(ContextoMB contexto, String usuario, String fingerprint) {
		ApiRequestMB request = ApiMB.request("ValidarUsuarioIDG", "seguridad", "GET", "/v1/clave", contexto);
		request.header("x-fingerprint", fingerprint);
		request.query("grupo", "ClientesBH");
		request.query("idcliente", contexto.idCobis());
		request.query("clave", usuario);
		request.permitirSinLogin = true;
		return ApiMB.response(request, contexto.idCobis());
	}

	public static ApiResponseMB validarClave(ContextoMB contexto, String clave, String fingerprint) {
		ApiRequestMB request = ApiMB.request("ValidarClaveIDG", "seguridad", "GET", "/v1/clave", contexto);
		request.header("x-fingerprint", fingerprint);
		request.query("grupo", "ClientesBH");
		request.query("idcliente", contexto.idCobis());
		request.query("clave", clave);
		request.query("nombreClave", "numerica");
		request.permitirSinLogin = true;
		return ApiMB.response(request, contexto.idCobis());
	}

	public static ApiResponseMB validarClavev2(ContextoMB contexto, String clave, String fingerprint) {
		ApiRequestMB request = ApiMB.request("ValidarClaveCanalIDG", "seguridad", "GET", "/v1/clave/canal", contexto);
		request.header("x-fingerprint", fingerprint);
		request.query("grupo", "ClientesBH");
		request.query("idcliente", contexto.idCobis());
		request.query("clave", clave);
		request.query("nombreClave", "numerica");
		request.permitirSinLogin = true;
		return ApiMB.response(request, contexto.idCobis());
	}

	public static ApiResponseMB cambiarUsuario(ContextoMB contexto, String usuario, boolean permitirSinLogin) {
		ApiRequestMB request = ApiMB.request("CambiarUsuario", "seguridad", "PUT", "/v1/clave", contexto);
		request.body("grupo", "ClientesBH");
		request.body("idUsuario", contexto.idCobis());
		request.body("parametros").set("clave", usuario);
		request.permitirSinLogin = permitirSinLogin;

		return ApiMB.response(request, contexto.idCobis());
	}

	public static ApiResponseMB cambiarClave(ContextoMB contexto, String clave, boolean permitirSinLogin) {
		ApiRequestMB request = ApiMB.request("CambiarClave", "seguridad", "PUT", "/v1/clave", contexto);
		request.body("grupo", "ClientesBH");
		request.body("idUsuario", contexto.idCobis());
		request.body("nombreClave", "numerica");
		request.body("parametros").set("clave", clave);
		request.permitirSinLogin = permitirSinLogin;

		return ApiMB.response(request, contexto.idCobis());
	}

	public static ApiResponseMB consultaPreguntasPorDefecto(ContextoMB contexto, Integer cantidad) {
		ApiRequestMB request = ApiMB.request("ConsultaPreguntasPorDefecto", "seguridad", "GET", "/v1/preguntas", contexto);
		request.query("cantidad", cantidad.toString());
		return ApiMB.response(request, contexto.idCobis());
	}

	public static ApiResponseMB consultaPreguntasPorCliente(ContextoMB contexto, Integer cantidad) {
		ApiRequestMB request = ApiMB.request("ConsultaPreguntasPorCliente", "seguridad", "GET", "/v1/preguntas/desafio", contexto);
		request.query("cantidad", cantidad.toString());
		request.query("grupo", "ClientesBH");
		request.query("idcliente", contexto.idCobis());
		return ApiMB.response(request, contexto.idCobis());
	}

	public static ApiResponseMB blanquearPreguntasDesafio(ContextoMB contexto) {
		ApiRequestMB request = ApiMB.request("BlanquearPreguntasDesafio", "seguridad", "DELETE", "/v1/preguntas/{idcliente}", contexto);
		request.query("grupo", "ClientesBH");
		request.path("idcliente", contexto.idCobis());
		return ApiMB.response(request, contexto.idCobis());
	}

	public static ApiResponseMB respuestaDesafio(ContextoMB contexto, Integer cantidad, Objeto respuestas) {
		ApiRequestMB request = ApiMB.request("RespuestasDesafio", "seguridad", "POST", "/v1/preguntas/respuesta?idcliente=" + contexto.idCobis(), contexto);

		request.body("cantidad", cantidad);
		request.body("grupo", "ClientesBH");
		request.body("respuestas", respuestas);
		return ApiMB.response(request, contexto.idCobis());
	}

	public static ApiResponseMB desbloquearTCO(ContextoMB contexto) {
		ApiRequestMB request = ApiMB.request("SeguridadDesbloquearTCO", "seguridad", "PATCH", "/v1/tarjetascoordenadas", contexto);
		request.query("grupo", "ClientesBH");
		request.query("idcliente", contexto.idCobis());
		return ApiMB.response(request, contexto.idCobis());
	}

	public static ApiResponseMB bajaTCO(ContextoMB contexto, String numeroDeSerie) {
		ApiRequestMB request = ApiMB.request("SeguridadBajaTCO", "seguridad", "PATCH", "/v1/tarjetascoordenadas/{numerodeserie}/usuario", contexto);
		request.path("numerodeserie", numeroDeSerie);
		request.query("grupo", "ClientesBH");
		request.query("idcliente", contexto.idCobis());
		return ApiMB.response(request, contexto.idCobis());
	}

	public static ApiResponseMB historialActividades(ContextoMB contexto, String actividad) {
		ApiRequestMB request = ApiMB.request("SeguridadHistorialActividades", "seguridad", "GET", "/v1/clientes/{idCliente}/auditorias", contexto);
		request.path("idCliente", contexto.idCobis());
		request.query("actividad", actividad);
		return ApiMB.response(request, contexto.idCobis());
	}

	public static ApiResponseMB accessTokenGire(ContextoMB contexto, String usuario) {
		ApiRequestMB request = ApiMB.request("GireLoginAccessToken", "seguridad", "GET", "/v1/accesstokengire?user=" + usuario, contexto);
		return ApiMB.response(request, contexto.idCobis());
	}

	public static SqlResponseMB usuarioGireSql(ContextoMB contexto) {
		SqlRequestMB sqlRequest = SqlMB.request("SelectGireUsuario", "homebanking");
		sqlRequest.sql = "select * from [homebanking].[dbo].[gire_usuario] where id_cobis =  ?";
		sqlRequest.parametros.add(contexto.idCobis());
		return SqlMB.response(sqlRequest);
	}

	public ApiResponseMB verificarClaveRedLink(String cardId, String claveLink, ContextoMB contexto) {
		ApiRequestMB request = ApiMB.request("LinkPostVerificacion", "link", "POST", "/v1/verificacion", contexto);
		request.body("cardId", cardId);
		request.body("pin", claveLink);
		request.permitirSinLogin = true;

		return ApiMB.response(request, contexto.idCobis());
	}

	public static ApiResponseMB datosBuhoFacil(ContextoMB contexto) {
		ApiRequestMB request = ApiMB.request("DatosBuhoFacil", "seguridad", "GET", "/v1/datosbuhofacil", contexto);
		request.query("idCobis", contexto.idCobis());
		request.permitirSinLogin = true;

		return ApiMB.response(request, contexto.idCobis());
	}
}
