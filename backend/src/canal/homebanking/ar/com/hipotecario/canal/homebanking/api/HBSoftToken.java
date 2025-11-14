package ar.com.hipotecario.canal.homebanking.api;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import com.fasterxml.jackson.databind.ObjectMapper;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.Respuesta;
import ar.com.hipotecario.canal.homebanking.api.dto.TokenIsvaDto;
import ar.com.hipotecario.canal.homebanking.base.ConfigHB;
import ar.com.hipotecario.canal.homebanking.conector.ApiResponse;
import ar.com.hipotecario.canal.homebanking.conector.Sql;
import ar.com.hipotecario.canal.homebanking.conector.SqlRequest;
import ar.com.hipotecario.canal.homebanking.conector.SqlResponse;
import ar.com.hipotecario.canal.homebanking.negocio.EstadoUsuario;
import ar.com.hipotecario.canal.homebanking.servicio.RestSoftTokenService;
import ar.com.hipotecario.canal.homebanking.servicio.SqlSoftTokenService;

public class HBSoftToken {
	private static final String FORMATO_FECHA_ISO_8106 = "yyyy-MM-dd HH:mm:ss";
	private static SqlSoftTokenService sqlSoftTokenService = new SqlSoftTokenService();

	/**
	 * Comprueba que el código de 6 dígitos (soft token) sea correcto
	 *
	 * @param contexto
	 * @return Respuesta
	 */
	public static Respuesta validarSoftToken(ContextoHB contexto) {
		Respuesta respuesta;
		ApiResponse apiResponse;
		String accessTokenISVA;
		RestSoftTokenService restSoftTokenService = new RestSoftTokenService();

		Boolean ok = false;
		String error = "";

		try {
			// Parámetros
			String idCobis = contexto.idCobis();
			String softToken = contexto.parametros.string("softToken", null);

			if (idCobis == null) {
				return Respuesta.estado("SIN_PSEUDO_SESION");
			}

			if (softToken == null) {
				return Respuesta.parametrosIncorrectos();
			}

			// ---- INICIO "SOLO PRUEBAS" ----
			if (!ConfigHB.esProduccion()) {
				// CASO 1: SOFT TOKEN CORRECTO
				if (softToken.equals("999999")) {
					boolean resultadoOk = sqlSoftTokenService.limpiarIntentosFallidos(idCobis, true, false);

					if (!resultadoOk) {
						return Respuesta.error();
					}

					respuesta = new Respuesta();

					// Guarda en sesion que el soft token fue validado
					contexto.sesion.validaSegundoFactorSoftToken = (true);
					respuesta.set("softTokenValido", true);
					ok = true;

					return respuesta;
				}

				// CASO 2: NO SE PUDO VALIDAR EL CÓDIGO POR ERROR NO CONTROLADO
				if (softToken.equals("666666")) {
					return Respuesta.error();
				}
			}
			// ---- FIN "SOLO PRUEBAS" ----

			// LLAMADAS EN PARALELO
			Futuro<Respuesta> futuroConsultarSoftTokenActivoPorCliente = new Futuro<>(() -> consultarSoftTokenActivoPorCliente(contexto));
			Futuro<Respuesta> futuroValidarUsoSoftTokenBloqueado = new Futuro<>(() -> validarUsoSoftTokenBloqueado(contexto));
			Futuro<SqlResponse> futuroObtenerDatosTokenISVA = new Futuro<>(() -> sqlSoftTokenService.obtenerDatosTokenISVA(idCobis, EstadoUsuario.ACTIVO.name()));

			// Verifica soft token activo
			respuesta = futuroConsultarSoftTokenActivoPorCliente.get();

			if (respuesta.hayError()) {
				return respuesta;
			}

			if (respuesta.string("estadoSoftToken").equals(EstadoUsuario.INACTIVO.name())) {
				return Respuesta.error();
			}

			respuesta = futuroValidarUsoSoftTokenBloqueado.get();

			if (respuesta.bool("bloqueado")) {
				error = "USO_SOFT_TOKEN_BLOQUEADO";
				return Respuesta.estado("USO_SOFT_TOKEN_BLOQUEADO");
			}

			SqlResponse sqlResponse = futuroObtenerDatosTokenISVA.get();

			if (sqlResponse.hayError) {
				return Respuesta.error();
			}

			respuesta = verificarAccessTokenIsva(contexto, sqlResponse);

			if (respuesta.hayError()) {
				return respuesta;
			}

			accessTokenISVA = respuesta.string("accessToken");

			apiResponse = restSoftTokenService.iniciarValidacionSoftToken(contexto, accessTokenISVA);

			if (apiResponse.hayError() && apiResponse.codigo == 401) {
				error = "AUTORIZACION_INVALIDA";
				return Respuesta.estado("AUTORIZACION_INVALIDA");
			}

			if (apiResponse.hayError() && apiResponse.codigo != 401) {
				error = "ERROR_1";
				return Respuesta.estado("ERROR_1");
			}

			apiResponse = restSoftTokenService.validarSoftToken(contexto, apiResponse.headers.get("set-cookie"), apiResponse.string("stateId"), softToken);

			if (apiResponse.hayError() && apiResponse.codigo != 403) {
				error = "ERROR_2";
				return Respuesta.estado("ERROR_2");
			}

			if (apiResponse.codigo == 403) {
				respuesta = validarBloqueoPorUsoSoftToken(contexto, idCobis);

				if (respuesta != null) {
					return respuesta;
				}

				error = "SOFT_TOKEN_INVALIDO";
				return Respuesta.estado("SOFT_TOKEN_INVALIDO");
			}

			new Futuro<>(() -> sqlSoftTokenService.limpiarIntentosFallidos(idCobis, true, false));

			respuesta = new Respuesta();

			// Guarda en sesion que el soft token fue validado
			contexto.sesion.validaSegundoFactorSoftToken = (true);
			contexto.sesion.save();
			respuesta.set("softTokenValido", true);
			ok = true;

			return respuesta;
		} finally {
			String finalError = error;
			if (ok) {
				new Futuro<>(() -> insertLogSoftToken(contexto, "OK", finalError));
			} else {
				new Futuro<>(() -> insertLogSoftToken(contexto, "ERROR", finalError));
			}
		}
	}

	public static Boolean insertLogSoftToken(ContextoHB contexto, String estado, String error) {
		try {
			SqlRequest request = Sql.request("logsSoftTokenAlta", "hbs");
			request.sql = """
					INSERT INTO [hbs].[dbo].[auditor_soft_token] ([momento],[cobis],[canal],[estado],[error])
					VALUES (?,?,?,?,?)
					""";
			request.add(new Date());
			request.add(contexto.idCobis());
			request.add("HB");
			request.add(estado);
			request.add(error);
			Sql.response(request);
		} catch (Throwable t) {
		}
		return true;
	}

	/**
	 * Determina si el usuario tiene alta de soft token activo.
	 *
	 * @param contexto
	 * @return Respuesta
	 */
	public static Respuesta consultarSoftTokenActivoPorCliente(ContextoHB contexto) {
		Respuesta respuesta = new Respuesta();
		String idCobis = contexto.idCobis();

		if (idCobis == null) {
			return Respuesta.estado("SIN_PSEUDO_SESION");
		}

		SqlResponse sqlResponse = sqlSoftTokenService.consultarAltaSoftToken(idCobis, EstadoUsuario.ACTIVO.name());

		if (sqlResponse.hayError) {
			return Respuesta.error();
		}

		if (!sqlResponse.registros.isEmpty()) {
			respuesta.set("estadoSoftToken", EstadoUsuario.ACTIVO.name());
			respuesta.set("mensaje", "El cliente tiene soft token activo");

			return respuesta;
		}

		sqlResponse = sqlSoftTokenService.consultarForzadoAltaSoftToken(idCobis);

		if (sqlResponse.hayError) {
			return Respuesta.error();
		}

		boolean forzarAlta = tieneForzarAltaSoftToken(sqlResponse);

		respuesta.set("estadoSoftToken", EstadoUsuario.INACTIVO.name());
		respuesta.set("forzarAlta", forzarAlta);
		respuesta.set("mensaje", "El cliente no tiene soft token activo");

		return respuesta;
	}

	/**
	 * Determina si un cliente/usuario tiene bloqueado el soft token para su uso.
	 *
	 * @param contexto
	 * @return Respuesta
	 */
	public static Respuesta validarUsoSoftTokenBloqueado(ContextoHB contexto) {
		// Parámetros
		String idCobis = contexto.idCobis();
		String idDispositivo = contexto.parametros.string("idDispositivo", null);
		boolean esDirecto = contexto.parametros.bool("esDirecto", false); // permitir sin login

		if (!esDirecto && idCobis == null) {
			return Respuesta.estado("SIN_PSEUDO_SESION");
		}

		if (esDirecto && idDispositivo == null) {
			return Respuesta.parametrosIncorrectos();
		}

		SqlResponse sqlResponse = null;

		if (!esDirecto) {
			sqlResponse = sqlSoftTokenService.consultarBloqueoUsoSoftTokenPorUsuario(idCobis, true);
		} else {
			sqlResponse = sqlSoftTokenService.consultarBloqueoUsoSoftTokenPorDispositivo(idDispositivo, true);
		}

		if (sqlResponse.hayError) {
			return Respuesta.error();
		}

		Respuesta respuesta = new Respuesta();
		boolean estaBloqueado = !sqlResponse.registros.isEmpty();

		if (estaBloqueado) {
			respuesta.set("bloqueado", true);

			return respuesta;
		}

		respuesta.set("bloqueado", false);

		return respuesta;
	}

	/**
	 * Bloquea de forma directa el uso de soft token.
	 *
	 * @param contexto
	 * @param motivo
	 * @return Respuesta
	 */
	public static Respuesta revocarSoftToken(ContextoHB contexto, String motivo) {
		String idCobis = contexto.idCobis();

		if (idCobis == null) {
			return Respuesta.estado("SIN_PSEUDO_SESION");
		}

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(FORMATO_FECHA_ISO_8106);
		String fechaHoraActualBloqueo = LocalDateTime.now().format(formatter);
		String idDispositivo = null;
		SqlResponse softTokenActivo = sqlSoftTokenService.consultarAltaSoftToken(idCobis, EstadoUsuario.ACTIVO.name());

		if (softTokenActivo.hayError) {
			return Respuesta.error();
		}

		if (softTokenActivo.registros.isEmpty()) {
			return Respuesta.exito();
		}

		idDispositivo = softTokenActivo.registros.get(0).string("id_dispositivo");

		boolean resultado = sqlSoftTokenService.insertarBloqueo(idCobis, true, fechaHoraActualBloqueo, idDispositivo, motivo);

		if (!resultado) {
			return Respuesta.error();
		}

		resultado = sqlSoftTokenService.limpiarIntentosFallidos(idCobis, true, false);

		if (!resultado) {
			return Respuesta.error();
		}

		/*
		 * resultado = sqlSoftTokenService.actualizarRegistrosAltaSoftToken(
		 * contexto.idCobis(), EstadoUsuario.ACTIVO.name(),
		 * EstadoUsuario.INACTIVO.name(), thisInstant());
		 * 
		 * if (!resultado) { return Respuesta.error(); }
		 */

		return Respuesta.exito();
	}

	private static Respuesta verificarAccessTokenIsva(ContextoHB contexto, SqlResponse sqlResponse) {
		Respuesta respuesta = new Respuesta();
		LocalDateTime fechaHoraSistema = LocalDateTime.now().plusSeconds(-1L);
		Timestamp expiracionTokenISVATimestamp = (Timestamp) sqlResponse.registros.get(0).get("expires_in");
		LocalDateTime expiracionTokenISVA = expiracionTokenISVATimestamp.toLocalDateTime();

		if (fechaHoraSistema.isAfter(expiracionTokenISVA)) {
			TokenIsvaDto tokenIsvaDto = obtenerTokenIsva(contexto, true, sqlResponse.registros.get(0).string("refresh_token"));

			if (tokenIsvaDto == null) {
				return Respuesta.error();
			}

			respuesta = actualizarAltaSoftToken(contexto, tokenIsvaDto, Integer.parseInt(sqlResponse.registros.get(0).string("id")), sqlResponse.registros.get(0).string("id_dispositivo"));

			if (respuesta != null) {
				return respuesta;
			}

			respuesta = new Respuesta();
			respuesta.set("accessToken", tokenIsvaDto.getAccessToken());

			return respuesta;
		}

		respuesta.set("accessToken", sqlResponse.registros.get(0).string("access_token"));

		return respuesta;
	}

	/**
	 * Inserta intento fallido por uso de soft token y determina si debe registrar
	 * bloqueo.
	 *
	 * @param contexto
	 * @param idCobis
	 * @return Respuesta
	 */
	private static Respuesta validarBloqueoPorUsoSoftToken(ContextoHB contexto, String idCobis) {
		boolean resultado = sqlSoftTokenService.insertarIntentoUsoSoftToken(idCobis, "INCORRECTO", "HOMEBANKING", true, thisInstant());

		if (!resultado) {
			return Respuesta.estado("ERROR_INSERT");
		}

		return procesarBloqueoUsoSoftToken(contexto);
	}

	/**
	 * Determina si debe registrar el bloqueo de uso de soft token por reiteradas
	 * fallas.
	 *
	 * @param contexto
	 * @return Respuesta
	 */
	private static Respuesta procesarBloqueoUsoSoftToken(ContextoHB contexto) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(FORMATO_FECHA_ISO_8106);
		Integer maximoIntentos = ConfigHB.integer("soft_token_max_intentos_error_uso", 3);
//        Long minutos = Config.longer("soft_token_intervalo_tiempo_bloqueo_minutos", 40L);
		LocalDateTime fechaHoraActual = LocalDateTime.now();
//        LocalDateTime tiempoLimite = fechaHoraActual.plusMinutes(-1 * minutos);
		LocalDateTime inicioConteo = LocalDateTime.of(LocalDate.now(ZoneId.of("America/Argentina/Buenos_Aires")), LocalTime.MIDNIGHT);

		String fechaInicioConteo = inicioConteo.format(formatter);
		SqlResponse intentosFallidos = sqlSoftTokenService.consultaIntentosUsoSoftToken(contexto.idCobis(), true, fechaInicioConteo);

		if (intentosFallidos.hayError) {
			return Respuesta.estado("ERROR_INTENTOS");
		}

		if (intentosFallidos.registros.size() < maximoIntentos) {
			return null;
		}

		String fechaHoraActualBloqueo = fechaHoraActual.format(formatter);
		SqlResponse softTokenActivo = sqlSoftTokenService.consultarAltaSoftToken(contexto.idCobis(), EstadoUsuario.ACTIVO.name());
		String idDispositivo = null;

		if (softTokenActivo.hayError) {
			return Respuesta.estado("ERROR_SOFT_ACT");
		}

		if (!softTokenActivo.registros.isEmpty()) {
			idDispositivo = softTokenActivo.registros.get(0).string("id_dispositivo");
		}

		boolean resultado = sqlSoftTokenService.insertarBloqueo(contexto.idCobis(), true, fechaHoraActualBloqueo, idDispositivo, "USO");

		if (!resultado) {
			return Respuesta.estado("ERROR_INS_BLOCK");
		}

		resultado = sqlSoftTokenService.limpiarIntentosFallidos(contexto.idCobis(), true, false);

		if (!resultado) {
			return Respuesta.estado("ERROR_LIMPIEZA");
		}

		return Respuesta.estado("USO_SOFT_TOKEN_BLOQUEADO");
	}

	private static TokenIsvaDto obtenerTokenIsva(ContextoHB contexto, boolean refrescarTokenISVA, String refreshToken) {
		ObjectMapper mapper = new ObjectMapper();
		RestSoftTokenService restSoftTokenService = new RestSoftTokenService();
		ApiResponse apiResponse;

		if (refrescarTokenISVA) {
			apiResponse = restSoftTokenService.refrescarTokenISVA(contexto, refreshToken);
		} else {
			apiResponse = restSoftTokenService.obtenerTokenIsva(contexto);
		}

		if (apiResponse.hayError()) {
			return null;
		}
		TokenIsvaDto tokenIsvaDto = null;
		try {
			tokenIsvaDto = mapper.readValue(apiResponse.json, TokenIsvaDto.class);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		return tokenIsvaDto;
	}

	private static Respuesta actualizarAltaSoftToken(ContextoHB contexto, TokenIsvaDto tokenIsva, int idBaseDatos, String idDispositivo) {
		String expiresIn = newFutureDate(tokenIsva.getExpiresIn());
		boolean resActualizarRergistroAltaST = sqlSoftTokenService.actualizarRegistrosAltaSoftToken(idBaseDatos, EstadoUsuario.ACTIVO.name(), tokenIsva.getAccessToken(), tokenIsva.getRefreshToken(), expiresIn, tokenIsva.getScope(), tokenIsva.getTokenType(), thisInstant(), idDispositivo);
		if (!resActualizarRergistroAltaST) {
			return Respuesta.error();
		}

		return null;
	}

	private static String thisInstant() {
		LocalDateTime nowTime = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(FORMATO_FECHA_ISO_8106);
		return nowTime.format(formatter);
	}

	private static String newFutureDate(String aditionalSeconds) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(FORMATO_FECHA_ISO_8106);
		return LocalDateTime.now().plusSeconds(Long.parseLong(aditionalSeconds)).format(formatter);
	}

	/**
	 * Determina si un cliente tiene forzado de alta de Soft Token.
	 *
	 * @param contexto
	 * @return Respuesta
	 */
	public static Respuesta forzarAltaSoftToken(ContextoHB contexto) {
		String idCobis = contexto.idCobis();

		if (idCobis == null) {
			return Respuesta.estado("SIN_PSEUDO_SESION");
		}

		SqlResponse sqlResponse = sqlSoftTokenService.consultarForzadoAltaSoftToken(idCobis);

		if (sqlResponse.hayError) {
			return Respuesta.error();
		}

		Respuesta respuesta = new Respuesta();

		if (!sqlResponse.registros.isEmpty() && sqlResponse.registros.get(0).string("forzar_alta").equals("true")) {
			respuesta.set("forzarAlta", true);

			return respuesta;
		}

		respuesta.set("forzarAlta", false);

		return respuesta;
	}

	/**
	 * Determina si tiene forzado de alta de soft token.
	 *
	 * @param sqlResponse Consulta SQL de forzado.
	 * @return true/false
	 */
	private static boolean tieneForzarAltaSoftToken(SqlResponse sqlResponse) {
		return !sqlResponse.registros.isEmpty() && sqlResponse.registros.get(0).string("forzar_alta").equals("true");
	}

	/**
	 * Determina si tiene alta de soft token en las últimas 24 horas.
	 *
	 * @param contexto
	 * @return true/false
	 */
	public static boolean activoSoftTokenEnElUltimoDia(ContextoHB contexto) {
		return !sqlSoftTokenService.consultarAltaSoftTokenUltimoDia(contexto.idCobis()).registros.isEmpty();
	}
}
