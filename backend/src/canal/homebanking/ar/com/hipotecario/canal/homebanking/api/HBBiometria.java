package ar.com.hipotecario.canal.homebanking.api;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.Respuesta;
import ar.com.hipotecario.canal.homebanking.base.Objeto;
import ar.com.hipotecario.canal.homebanking.conector.ApiResponse;
import ar.com.hipotecario.canal.homebanking.conector.SqlResponse;
import ar.com.hipotecario.canal.homebanking.lib.Fecha;
import ar.com.hipotecario.canal.homebanking.servicio.AuditorLogService;
import ar.com.hipotecario.canal.homebanking.servicio.BiometriaService;
import ar.com.hipotecario.canal.homebanking.servicio.SqlBiometriaService;

public class HBBiometria {

	public static final int VALIDES_TOKEN_SECONDS = 3599;
	public static final int VALIDES_TOKEN_INTERVALO = 20;
	public static final String publicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAymtSO8ucuAOmzWNajXXx6IGYF//GcPWYS3puUTxbn5n+Kp7n/Sl+dx/m5nC6LlTb9C59sNleAK02idlXTuWT2avGN4/+YWTnB1n/EPJSktd11Ir/wmhXfcIAI8Q2FXC6np7Ro2SGpdALjr4u+U0ERYs0yZRj0jkqX5GkxMnLv8SDcxHbAF8+hlhTwyvAPNhrg6tVR9p26Ljb/V6VhjO7q/Qkd5DOGqS8Krwgb/rrAbnqnx2GEEgkFAMlM6zZ8OFmnYeVGN3bN7lHZijQ4m7YJzS1Ck8RDpOPMmicGcJNM0DVjGxq8J0oPCQrQ5M7J8yAw3x8MrV8bt1iz9Jdgt1NqwIDAQAB";
	public static final long BLOQUEO_MODAL_BIOMETRIA_EN_DIAS = 7L;

	public static Respuesta revocaAutenticador(ContextoHB contexto) {
		String metodo = "todo";
		Boolean isDirect = contexto.parametros.bool("isDirect", false);
		Boolean isDeleted = false;

		if (StringUtils.isEmpty(contexto.idCobis())) {
			return Respuesta.estado("SIN_PSEUDO_SESION");
		}

		Boolean isRevokedEnable = HBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_revoca_biometria");
		if (isDirect && isRevokedEnable && !contexto.validaSegundoFactor("biometria-revocar")) {
			return Respuesta.estado("REQUIERE_SEGUNDO_FACTOR");
		}

		Respuesta accesos = verificaAccesosRefresh(contexto);

		if (!accesos.hayError()) {
			Respuesta usuarioIsva = consultarUsuarioIsva(contexto);
			if (usuarioIsva.hayError() || !usuarioIsva.existe("authenticators")) {
				return Respuesta.estado("ERROR_USUARIO_SIN_REGISTROS_ISVA");
			}
			BiometriaService.revocaBiometria(contexto, metodo, publicKey);
		}

		isDeleted = BiometriaService.deleteAccesos(contexto);

		contexto.limpiarSegundoFactor();
		return (isDeleted) ? Respuesta.exito() : Respuesta.estado("SIN_EFECTO");

	}

	public static Respuesta verificaAccesosRefresh(ContextoHB contexto) {
		Date fechaToken = null;

		Respuesta respAccesos = HBBiometria.verificaAccesosCompletos(contexto);
		if (respAccesos.hayError()) {
			return respAccesos;
		}

		contexto.sesion.cache.put("token_biometria", respAccesos.string("accessToken"));
		contexto.sesion.cache.put("refresh_token_biometria", respAccesos.string("refreshToken"));
		contexto.sesion.cache.put("fecha_token_biometria", respAccesos.string("fechaToken"));
		contexto.sesion.cache.put("id_Dispositivo", respAccesos.string("idDispositivo"));

		fechaToken = Fecha.stringToDate(contexto.sesion.cache.get("fecha_token_biometria"), "yyyy-MM-dd HH:mm:ss.ss");

		if (HBBiometria.verificaAccessToken(contexto, fechaToken)) {
			contexto.parametros.set("refresh_token", contexto.sesion.cache.get("refresh_token_biometria"));
			Respuesta respToken = HBBiometria.refreshTokens(contexto);
			if (respToken.hayError()) {
				return respToken;
			}
			contexto.sesion.cache.put("token_biometria", respToken.string("access_token"));
			contexto.sesion.cache.put("refresh_token_biometria", respToken.string("refresh_token"));
			contexto.sesion.cache.put("fecha_token_biometria", contexto.sesion.cache.get("fecha_token_biometria"));
			contexto.sesion.cache.put("expires_in_biometria", respToken.string("expires_in"));
			contexto.sesion.cache.put("id_Dispositivo", respToken.string("authenticator_id"));
		}
		return Respuesta.exito();
	}

	private static boolean verificaAccessToken(ContextoHB contexto, Date fechaToken) {
		LocalDateTime fechaTokenCreado = null;
		Integer expires_in = contexto.sesion.cache.get("expires_in_biometria") == null ? VALIDES_TOKEN_SECONDS : Integer.parseInt(contexto.sesion.cache.get("expires_in_biometria"));

		try {
			fechaTokenCreado = Fecha.convertToLocalDateTime(fechaToken);
			if (LocalDateTime.now().isAfter(fechaTokenCreado.plusSeconds(Math.subtractExact(expires_in, VALIDES_TOKEN_INTERVALO)))) {
				return true;
			}
		} catch (DateTimeParseException e) {
			return true;
		}

		return false;
	}

	private static Respuesta consultarUsuarioIsva(ContextoHB contexto) {
		String token = contexto.sesion.cache.get("token_biometria");
		String dispositivo = contexto.sesion.cache.get("id_Dispositivo");

		if (Objeto.anyEmpty(token, dispositivo)) {
			return Respuesta.parametrosIncorrectos();
		}

		ApiResponse response = BiometriaService.consultaUsuario(contexto, token);
		if (response.hayError()) {
			return Respuesta.estado("ERROR_CONSULTA_USUARIO_ISVA");
		}

		Respuesta respuesta = new Respuesta();
		respuesta.set("authenticators", respuestaUsuarioIsva(contexto, response, "authenticators", dispositivo));
		respuesta.set("userPresenceMethods", respuestaUsuarioIsva(contexto, response, "userPresenceMethods", dispositivo));
		respuesta.set("fingerprintMethods", respuestaUsuarioIsva(contexto, response, "fingerprintMethods", dispositivo));

		return respuesta;
	}

	private static Objeto respuestaUsuarioIsva(ContextoHB contexto, ApiResponse response, String clave, String dispositivo) {
		Objeto objeto = response.objeto("urn:ietf:params:scim:schemas:extension:isam:1.0:MMFA:Authenticator");
		List<Objeto> lista = objeto.objeto(clave).objetos();
		if (lista.isEmpty()) {
			return null;
		}

		for (Objeto obj : lista) {
			if (clave.equalsIgnoreCase("authenticators") && obj.string("id").equals(dispositivo)) {
				return obj;
			}

			if ((clave.equalsIgnoreCase("userPresenceMethods") || clave.equalsIgnoreCase("fingerprintMethods")) && dispositivo.equals(obj.string("authenticator"))) {
				return obj;
			}
		}
		return null;
	}

	private static Respuesta refreshTokens(ContextoHB contexto) {
		String refreshToken = contexto.sesion.cache.get("refresh_token_biometria") == null ? contexto.parametros.string("refresh_token") : contexto.sesion.cache.get("refresh_token_biometria");

		if (Objeto.anyEmpty(refreshToken)) {
			return Respuesta.parametrosIncorrectos();
		}

		ApiResponse response = null;
		Respuesta respuesta = new Respuesta();
		try {
			response = BiometriaService.generaRefreshTokens(contexto, refreshToken);
			if (response.hayError()) {
				response = verificaValidezToken(contexto);
				if (response == null || response.hayError()) {
					return Respuesta.estado("ERROR_REFRESS_TOKEN");
				}
			}

			Boolean resp = BiometriaService.updateToken(contexto, response.string("access_token").trim(), response.string("refresh_token").trim(), response.string("authenticator_id").trim());
			updateBiometriaLog(contexto, response, "UpdateRealizado");
			if (!resp) {
				updateBiometriaLog(contexto, response, "ERROR_REFRESS_TOKEN_BD");
				return Respuesta.estado("ERROR_REFRESS_TOKEN_BD");
			}

			respuesta.set("access_token", response.string("access_token"));
			respuesta.set("refresh_token", response.string("refresh_token"));
			respuesta.set("scope", response.string("scope"));
			respuesta.set("authenticator_id", response.string("authenticator_id"));
			respuesta.set("token_type", response.string("token_type"));
			respuesta.set("display_name", response.string("display_name"));
			respuesta.set("expires_in", response.integer("expires_in"));
			updateBiometriaLog(contexto, response, respuesta.toJson());

		} catch (Exception e) {
			if (response != null) {
				updateBiometriaLog(contexto, response, "ExceptionGeneradaRefrehToken: " + e);
			}
		}
		return respuesta;
	}

	@SuppressWarnings("unused")
	private static Respuesta seteaAccesoBiometria(ContextoHB contexto) {
		String dispositivo = contexto.sesion.cache.get("dispositivo") == null ? contexto.parametros.string("dispositivo") : contexto.sesion.cache.get("dispositivo");
		Boolean biometriaActiva = contexto.parametros.bool("biometria", null);

		if (Objeto.anyEmpty(biometriaActiva, dispositivo)) {
			return Respuesta.parametrosIncorrectos();
		}

		Respuesta respuesta = new Respuesta();
		Boolean response = BiometriaService.updateAccesoBiometria(contexto, biometriaActiva, dispositivo);
		if (!response) {
			return respuesta.setEstado("ERROR_BD_ACCESO_BIOMETRIA");
		}

		return Respuesta.exito();
	}

	private static Respuesta verificaAccesosCompletos(ContextoHB contexto) {

		Respuesta respuesta = new Respuesta();
		SqlResponse response = BiometriaService.selectAccesos(contexto);

		if (response.hayError) {
			return respuesta.setEstado("ERROR_ACCESO_BIOMETRIA");
		}

		if (response.registros.isEmpty()) {
			return respuesta.setEstado("NO_TIENE_ACCESOS_BIOMETRIA");
		}

		Objeto registro = response.registros.get(0);
		if ((registro.integer("disp_registrado") == 0 && registro.integer("biometria_activa") == 0) || (Objeto.anyEmpty(registro.string("access_token").trim(), registro.string("refresh_token").trim(), registro.date("fecha_token")))) {
			return respuesta.setEstado("NO_TIENE_ACCESS_ACTIVOS_BIOMETRIA");
		}

		respuesta.set("fechaRegistro", registro.string("fecha_disp_registrado"));
		respuesta.set("dispositivoRegistrado", registro.string("disp_registrado"));
		respuesta.set("biometriaActiva", registro.string("biometria_activa"));
		respuesta.set("fechaBiometriaActiva", registro.string("fecha_biometria_activa"));
		respuesta.set("buhoFacilActivo", registro.string("buhoFacil_activo"));
		respuesta.set("fechaBuhoFacilActivo", registro.string("fecha_buhoFacil_activo"));
		respuesta.set("idDispositivo", registro.string("id_dispositivo").trim());
		respuesta.set("accessToken", registro.string("access_token").trim());
		respuesta.set("refreshToken", registro.string("refresh_token").trim());
		respuesta.set("fechaToken", registro.date("fecha_token"));

		return respuesta;
	}

	public static Respuesta verificaAccesos(ContextoHB contexto) {
		SqlBiometriaService sqlBiometriaService = new SqlBiometriaService();
		String dispositivo = contexto.parametros.string("dispositivo", null);

		if (contexto.idCobis() == null) {
			return Respuesta.estado("SIN_PSEUDO_SESION");
		}

		Respuesta respuesta = new Respuesta();
		SqlResponse response = sqlBiometriaService.selectAccesos(contexto, dispositivo);

		if (response.hayError) {
			return respuesta.setEstado("ERROR_ACCESO_BIOMETRIA");
		}

		if (response.registros.isEmpty()) {
			if (sqlBiometriaService.selectCountAccesos(contexto).registros.get(0).integer("dispositivos") >= 1) {
				return respuesta.setEstado("ERROR_TIENE_DISP_REGISTRADOS");
			}
			return respuesta.setEstado("NO_TIENE_ACCESOS_BIOMETRIA");
		}

		Objeto registro = response.registros.get(0);
		respuesta.set("fechaRegistro", registro.string("fecha_disp_registrado"));
		respuesta.set("dispositivoRegistrado", registro.string("disp_registrado"));
		respuesta.set("biometriaActiva", registro.string("biometria_activa"));
		respuesta.set("fechaBiometriaActiva", registro.string("fecha_biometria_activa"));
		respuesta.set("biometriaActivaISVA", registro.string("fecha_biometria_activa") != null && "1".equalsIgnoreCase(registro.string("biometria_activa")));
		respuesta.set("buhoFacilActivo", registro.string("buhoFacil_activo"));
		respuesta.set("fechaBuhoFacilActivo", registro.string("fecha_buhoFacil_activo"));
		respuesta.set("activaModalBio", mostrarModalBiometria(registro.string("fecha_biometria_activa")));

		return respuesta;
	}

	private static ApiResponse verificaValidezToken(ContextoHB contexto) {
		SqlResponse response = BiometriaService.selectLogsBiometria(contexto);

		if (!response.registros.isEmpty()) {
			Objeto objeto = response.registros.get(0);
			String refreshTokenLogs = objeto.string("refresh_token").trim();
			return BiometriaService.generaRefreshTokens(contexto, refreshTokenLogs);
		}
		return null;
	}

	private static void updateBiometriaLog(ContextoHB contexto, ApiResponse response, String updateRealizado) {
		Objeto detalle = new Objeto();
		detalle.set("peticion", "/v1/refress");
		if (response.hayError()) {
			detalle.set("error", response.json);
		} else {
			detalle.set("access_token", response.string("access_token"));
			detalle.set("refresh_token", response.string("refresh_token"));
			detalle.set("authenticator_id", response.string("authenticator_id"));
		}
		detalle.set("updateRealizado", updateRealizado);

		AuditorLogService.biometriaLogVisualizador(contexto, "Api-Biometria_UpdateBiometriaLog", detalle);
	}

	private static Boolean mostrarModalBiometria(String fechaRegistracionBiometria) {
		if (fechaRegistracionBiometria == "" || fechaRegistracionBiometria == null) {
			return false;
		}
		Date fechaRegistro = Fecha.stringToDate(fechaRegistracionBiometria, "yyyy-MM-dd HH:mm:ss.ss");
		Date fechaEstimada = Fecha.sumarDias(fechaRegistro, BLOQUEO_MODAL_BIOMETRIA_EN_DIAS);

		if (new Date().after(fechaEstimada)) {
			return true;
		}
		return false;
	}
}
