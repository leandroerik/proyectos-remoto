package ar.com.hipotecario.mobile.servicio;

import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.conector.SqlMB;
import ar.com.hipotecario.mobile.conector.SqlRequestMB;
import ar.com.hipotecario.mobile.conector.SqlResponseMB;

public class SqlSoftTokenService {

	public static final Long REGISTRO_ACTIVO = 1L;
	public static final Long REGISTRO_INACTIVO = 0L;
	public static final int ERROR_INSERCION = -1;
	private static final String CONSULTAR_USUARIO_BLOQUEADO = "SELECT [id], [id_cobis], [fecha_alta], [fecha_fin] " + "FROM [Mobile].[dbo].[soft_token_alta_bloqueos] WHERE id_cobis = ? AND fecha_fin > ?";
	private static final String CONSULTAR_ULTIMA_ALTA_SOFT_TOKEN_USUARIO = "SELECT TOP 1 [id], [id_cobis], [clave_link], [fecha_alta], " + "[estado], [id_tarjeta_debito] FROM [Mobile].[dbo].[soft_token_alta] WHERE " + "id_cobis = ? AND estado = ? AND clave_link = ? AND id_tarjeta_debito = ? " + "ORDER BY fecha_alta DESC";

	private static final String CONSULTAR_ULTIMA_ALTA_SOFT_TOKEN_USUARIO_CVV = "SELECT TOP 1 [id], [id_cobis], [fecha_alta], " + "[estado], [id_tarjeta_debito] FROM [Mobile].[dbo].[soft_token_alta] WHERE " + "id_cobis = ? AND estado = ? AND id_tarjeta_debito = ? " + "ORDER BY fecha_alta DESC";

	private static final String CONSULTAR_INTENTOS = "SELECT [id], [id_cobis], [clave_link], [fecha], [estado], [motivo], [activo] " + "FROM [Mobile].[dbo].[soft_token_alta_intentos] where id_cobis = ? and estado not in ( ? ) and activo = ? AND fecha > ? ";

	private static final String INSERT_BLOQUEO = "INSERT INTO [Mobile].[dbo].[soft_token_alta_bloqueos]([id_cobis],[fecha_alta],[fecha_fin]) values( ?, ?, ? )";

	private static final String UPDATE_ESTADO_INTENTOS = "UPDATE [Mobile].[dbo].soft_token_alta_intentos set activo = ? WHERE id_cobis = ?  and activo = ?";

	public SqlResponseMB consultarBloqueoUsuario(String idCobis, String fechaActual) {
		SqlRequestMB sqlRequest = SqlMB.request("ConsultarUsuarioBloqueado", "mobile");
		sqlRequest.sql = CONSULTAR_USUARIO_BLOQUEADO;
		sqlRequest.add(idCobis);
		sqlRequest.add(fechaActual);

		return SqlMB.response(sqlRequest);
	}

	/**
	 * Consultar registros por estado (bloqueado o desbloqueado) por uso de soft
	 * token de un cliente.
	 *
	 * @param idCobis
	 * @param estado
	 * @return SqlResponse
	 */
	public SqlResponseMB consultarBloqueoUsoSoftTokenPorUsuario(String idCobis, boolean estado) {
		SqlRequestMB sqlRequest = SqlMB.request("ConsultarBloqueoPorUsoSoftTokenPorUsuario", "mobile");
		int estadoInt = estado ? 1 : 0;

		sqlRequest.sql = "SELECT [id], [id_cobis], [estado], [fecha], [fecha_ultima_actualizacion], [id_dispositivo] " + "FROM [Mobile].[dbo].[soft_token_uso_bloqueos] WHERE " + "id_cobis = ? AND estado = ?";

		sqlRequest.add(idCobis);
		sqlRequest.add(estadoInt);

		return SqlMB.response(sqlRequest);
	}

	/**
	 * Consultar registros por estado y por dispositivo (bloqueado o desbloqueado)
	 * por uso de soft token.
	 *
	 * @param idDispositivo
	 * @param estado
	 * @return SqlResponse
	 */
	public SqlResponseMB consultarBloqueoUsoSoftTokenPorDispositivo(String idDispositivo, boolean estado) {
		SqlRequestMB sqlRequest = SqlMB.request("ConsultarBloqueoPorUsoSoftTokenPorDispositivo", "mobile");
		int estadoInt = estado ? 1 : 0;

		sqlRequest.sql = "SELECT [id], [id_cobis], [estado], [fecha], [fecha_ultima_actualizacion], [id_dispositivo] " + "FROM [Mobile].[dbo].[soft_token_uso_bloqueos] WHERE " + "id_dispositivo = ? AND estado = ?";

		sqlRequest.add(idDispositivo);
		sqlRequest.add(estadoInt);

		return SqlMB.response(sqlRequest);
	}

	/**
	 * Agrega un registro de alta de soft token en estado POR_CONFIRMAR.
	 *
	 * @param idCobis
	 * @param claveLinkHasheada
	 * @param fechaAlta
	 * @param estadoUsuario
	 * @param idDispositivo
	 * @param fechaUltimaActualizacion
	 * @param idTarjetaDebito
	 * @return int
	 */
	public int insertarRegistroAltaSoftToken(String idCobis, String claveLinkHasheada, String fechaAlta, String estadoUsuario, String idDispositivo, String fechaUltimaActualizacion, String idTarjetaDebito, String direccionIp, boolean altaEmail, boolean altaCvv) {
		SqlResponseMB sqlResponse;
		SqlRequestMB sqlRequest = SqlMB.request("InsertarRegistroAltaSoftToken", "mobile");

		sqlRequest.sql = "INSERT INTO [Mobile].[dbo].[soft_token_alta] ([id_cobis], [clave_link], [fecha_alta], " + "[estado], [id_dispositivo], [fecha_ultima_actualizacion], [id_tarjeta_debito], [direccion_ip], [alta_email], [alta_cvv]) " + "OUTPUT inserted.id VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		sqlRequest.add(idCobis);
		sqlRequest.add(claveLinkHasheada);
		sqlRequest.add(fechaAlta);
		sqlRequest.add(estadoUsuario);
		sqlRequest.add(idDispositivo);
		sqlRequest.add(fechaUltimaActualizacion);
		sqlRequest.add(idTarjetaDebito);
		sqlRequest.add(direccionIp);
		sqlRequest.add(altaEmail);
		sqlRequest.add(altaCvv);

		sqlResponse = SqlMB.response(sqlRequest);

		if (sqlResponse.hayError) {
			return ERROR_INSERCION;
		}

		return Integer.parseInt(sqlResponse.registros.get(0).string("id"));
	}

	public boolean insertarIntentoAltaSoftToken(String idCobis, String claveLinkHasheada, String fecha, String estado, String motivo, Long activo) {
		SqlResponseMB sqlResponse;

		SqlRequestMB sqlRequest = SqlMB.request("InsertarIntentoAltaSoftToken", "mobile");
		sqlRequest.sql = "INSERT INTO [Mobile].[dbo].[soft_token_alta_intentos] ([id_cobis], [clave_link], [fecha], [estado], [motivo], [activo]) " + "VALUES (?, ?, ?, ?, ?, ? )";
		sqlRequest.add(idCobis);
		sqlRequest.add(claveLinkHasheada);
		sqlRequest.add(fecha);
		sqlRequest.add(estado);
		sqlRequest.add(motivo);
		sqlRequest.add(activo);

		sqlResponse = SqlMB.response(sqlRequest);

		return !sqlResponse.hayError;
	}

	/**
	 * Insertar intento de uso de soft token por cliente.
	 *
	 * @param idCobis
	 * @param estado
	 * @param activo
	 * @param fecha
	 * @return boolean
	 */
	public boolean insertarIntentoUsoSoftToken(String idCobis, String estado, String canal, boolean activo, String fecha) {
		SqlRequestMB sqlRequest = SqlMB.request("InsertarIntentoUsoSoftToken", "mobile");
		int activoInt = activo ? 1 : 0;

		sqlRequest.sql = "INSERT INTO [Mobile].[dbo].[soft_token_uso_intentos] ([id_cobis], [estado], [canal], [activo], [fecha]) VALUES " + "(?, ?, ?, ?, ?)";

		sqlRequest.add(idCobis);
		sqlRequest.add(estado);
		sqlRequest.add(canal);
		sqlRequest.add(activoInt);
		sqlRequest.add(fecha);

		return !SqlMB.response(sqlRequest).hayError;
	}

	public boolean actualizarRegistrosAltaSoftToken(String idCobis, String estadoUsuarioActual, String nuevoEstadoUsuario, String fechaUltimaActualizacion) {
		SqlResponseMB sqlResponse;

		SqlRequestMB sqlRequest = SqlMB.request("ActualizarRegistroAltaSoftToken", "mobile");
		sqlRequest.sql = "UPDATE [Mobile].[dbo].[soft_token_alta] SET " + "[estado] = ?, [fecha_ultima_actualizacion] = ? " + "WHERE id_cobis = ? AND estado = ?";
		sqlRequest.add(nuevoEstadoUsuario);
		sqlRequest.add(fechaUltimaActualizacion);
		sqlRequest.add(idCobis);
		sqlRequest.add(estadoUsuarioActual);

		sqlResponse = SqlMB.response(sqlRequest);

		return !sqlResponse.hayError;
	}

	public boolean actualizarRegistrosAltaSoftToken(int id, String estadoUsuario, String accessToken, String refreshToken, String expiresIn, String scope, String tokenType, String fechaUltimaActualizacion, String idDispositivo) {
		SqlResponseMB sqlResponse;

		SqlRequestMB sqlRequest = SqlMB.request("ActualizarRegistroAltaSoftToken", "mobile");
		sqlRequest.sql = "UPDATE [Mobile].[dbo].[soft_token_alta] SET [estado] = ?, [access_token] = ?, " + "[refresh_token] = ?, [expires_in] = ?, [scope] = ?, [token_type] = ?, [id_dispositivo] = ?, [fecha_ultima_actualizacion] = ? WHERE id = ?";
		sqlRequest.add(estadoUsuario);
		sqlRequest.add(accessToken);
		sqlRequest.add(refreshToken);
		sqlRequest.add(expiresIn);
		sqlRequest.add(scope);
		sqlRequest.add(tokenType);
		sqlRequest.add(idDispositivo);
		sqlRequest.add(fechaUltimaActualizacion);
		sqlRequest.add(id);

		sqlResponse = SqlMB.response(sqlRequest);

		return !sqlResponse.hayError;
	}

	/**
	 * Desbloquea a un usuario para que pueda volver a operar con soft token.
	 *
	 * @param id
	 * @param fecha
	 * @return
	 */
	public boolean desbloquearUsuarioUsoSoftToken(int id, String fecha) {
		SqlRequestMB sqlRequest = SqlMB.request("DesbloquearUsuarioParaUsoSoftToken", "mobile");

		sqlRequest.sql = "UPDATE [Mobile].[dbo].[soft_token_uso_bloqueos] SET [estado] = 0, [fecha_ultima_actualizacion] = ? WHERE id = ?";

		sqlRequest.add(fecha);
		sqlRequest.add(id);

		return !SqlMB.response(sqlRequest).hayError;
	}

	public SqlResponseMB consultarUltimaAltaSoftTokenUsuario(String idCobis, String estado, String claveHasheada, String idTarjetaDebito) {
		SqlRequestMB sqlRequest = SqlMB.request("ConsultarAltaSoftTokenUsuario", "mobile");

		sqlRequest.sql = CONSULTAR_ULTIMA_ALTA_SOFT_TOKEN_USUARIO;
		sqlRequest.add(idCobis);
		sqlRequest.add(estado);
		sqlRequest.add(claveHasheada);
		sqlRequest.add(idTarjetaDebito);

		return SqlMB.response(sqlRequest);
	}

	public SqlResponseMB consultarUltimaAltaSoftTokenUsuarioCvv(String idCobis, String estado, String idTarjetaDebito) {
		SqlRequestMB sqlRequest = SqlMB.request("ConsultarAltaSoftTokenUsuarioCvv", "mobile");

		sqlRequest.sql = CONSULTAR_ULTIMA_ALTA_SOFT_TOKEN_USUARIO_CVV;
		sqlRequest.add(idCobis);
		sqlRequest.add(estado);
		sqlRequest.add(idTarjetaDebito);

		return SqlMB.response(sqlRequest);
	}

	public SqlResponseMB consultaIntentosAlta(String idCobis, Long estadoRegistro, String fechaLimite) {
		SqlRequestMB sqlRequest = SqlMB.request("ConsultarIntentosAltaSoftTokenUsuario", "mobile");
		sqlRequest.sql = CONSULTAR_INTENTOS;
		sqlRequest.add(idCobis);
		sqlRequest.add("CORRECTO");
		sqlRequest.add(estadoRegistro);
		sqlRequest.add(fechaLimite);
		return SqlMB.response(sqlRequest);
	}

	/**
	 * Consulta la cantidad de intentos de uso de soft token para un cliente en
	 * particular.
	 *
	 * @return SqlResponse
	 */
	public SqlResponseMB consultaIntentosUsoSoftToken(String idCobis, boolean activo, String fecha) {
		SqlRequestMB sqlRequest = SqlMB.request("ConsultarIntentosUsoSoftTokenUsuario", "mobile");
		int activoInt = activo ? 1 : 0;

		sqlRequest.sql = "SELECT [id], [id_cobis], [estado], [canal], [activo], [fecha] FROM " + "[Mobile].[dbo].[soft_token_uso_intentos] WHERE " + "id_cobis = ? AND activo = ? AND fecha > ?";

		sqlRequest.add(idCobis);
		sqlRequest.add(activoInt);
		sqlRequest.add(fecha);

		return SqlMB.response(sqlRequest);
	}

	public boolean insertarBloqueo(String idCobis, String fechaAlta, String fechaFin) {
		SqlRequestMB sqlRequest = SqlMB.request("InsertBloqueoAltaSofttoken", "mobile");
		sqlRequest.sql = INSERT_BLOQUEO;
		sqlRequest.add(idCobis);
		sqlRequest.add(fechaAlta);
		sqlRequest.add(fechaFin);

		return !SqlMB.response(sqlRequest).hayError;// TodoOK
	}

	/**
	 * Insertar bloqueo de uso de soft token. El cliente no podrá volver a operar
	 * con soft token hasta que no haya sido desbloqueado (darse de alta
	 * nuevamente).
	 *
	 * @param idCobis
	 * @param estado
	 * @param fecha
	 * @param idDispositivo
	 * @param motivo
	 * @return boolean
	 */
	public boolean insertarBloqueo(String idCobis, boolean estado, String fecha, String idDispositivo, String motivo) {
		SqlRequestMB sqlRequest = SqlMB.request("InsertarBloqueoUsoSoftToken", "mobile");
		int estadoInt = estado ? 1 : 0;

		sqlRequest.sql = "INSERT INTO [Mobile].[dbo].[soft_token_uso_bloqueos] " + "([id_cobis], [estado], [fecha], " + "[fecha_ultima_actualizacion], [id_dispositivo], [motivo]) VALUES (?, ?, ?, ?, ?, ?)";

		sqlRequest.add(idCobis);
		sqlRequest.add(estadoInt);
		sqlRequest.add(fecha);
		sqlRequest.add(fecha);
		sqlRequest.add(idDispositivo);
		sqlRequest.add(motivo);

		return !SqlMB.response(sqlRequest).hayError;
	}

	public boolean limpiaIntentosFallidos(String idCobis, Long estadoNuevo, Long estadoViejo) {
		SqlRequestMB sqlRequest = SqlMB.request("LimpiaIntentosAltaFallidos", "mobile");
		sqlRequest.sql = UPDATE_ESTADO_INTENTOS;
		sqlRequest.add(estadoNuevo);
		sqlRequest.add(idCobis);
		sqlRequest.add(estadoViejo);

		return !SqlMB.response(sqlRequest).hayError;
	}

	/**
	 * Actualiza a inactivos los intentos fallidos registrados en la tabla de
	 * soft_token_uso_intentos.
	 *
	 * @param idCobis
	 * @param estadoActivoAnterior
	 * @param estadoActivoNuevo
	 * @return boolean
	 */
	public boolean limpiarIntentosFallidos(String idCobis, boolean estadoActivoAnterior, boolean estadoActivoNuevo) {
		SqlRequestMB sqlRequest = SqlMB.request("LimpiarIntentosFallidosDeUsoSoftToken", "mobile");
		int activoAnteriorInt = estadoActivoAnterior ? 1 : 0;
		int activoNuevoInt = estadoActivoNuevo ? 1 : 0;

		sqlRequest.sql = "UPDATE [Mobile].[dbo].[soft_token_uso_intentos] SET activo = ? WHERE id_cobis = ? AND activo = ?";

		sqlRequest.add(activoNuevoInt);
		sqlRequest.add(idCobis);
		sqlRequest.add(activoAnteriorInt);

		return !SqlMB.response(sqlRequest).hayError;
	}

	/**
	 * Consulta si un cliente tiene alta de Soft Token activo con un dispositivo en
	 * particular. También se puede reutilizar para obtener los registros pasando
	 * como argumento cualquier estado que desee.
	 *
	 * @param idCobis
	 * @param idDispositivo
	 * @param estadoUsuario
	 * @return SqlResponse
	 */
	public SqlResponseMB consultarAltaSoftToken(String idCobis, String idDispositivo, String estadoUsuario) {
		SqlRequestMB sqlRequest = SqlMB.request("ConsultarSoftTokenActivo", "mobile");
		sqlRequest.sql = "SELECT [id], [id_cobis], [clave_link], [fecha_alta], [estado], " + "[id_dispositivo], [fecha_ultima_actualizacion] " + "FROM [Mobile].[dbo].[soft_token_alta] " + "WHERE id_dispositivo = ? AND estado = ?";
		sqlRequest.add(idDispositivo);
		sqlRequest.add(estadoUsuario);

		if (idCobis != null) {
			sqlRequest.sql += " AND id_cobis = ?";
			sqlRequest.add(idCobis);
		}

		return SqlMB.response(sqlRequest);
	}

	/**
	 * Consulta si un cliente tiene alta de Soft Token activo. También se puede
	 * reutilizar para obtener los registros pasando como argumento cualquier estado
	 * que desee.
	 *
	 * @param idCobis
	 * @param estadoUsuario
	 * @return SqlResponse
	 */
	public SqlResponseMB consultarAltaSoftToken(String idCobis, String estadoUsuario) {
		SqlRequestMB sqlRequest = SqlMB.request("ConsultarSoftTokenActivo", "mobile");
		sqlRequest.sql = "SELECT [id], [id_cobis], [clave_link], [fecha_alta], [estado], " + "[id_dispositivo], [fecha_ultima_actualizacion] " + "FROM [Mobile].[dbo].[soft_token_alta] " + "WHERE id_cobis = ? AND estado = ?";
		sqlRequest.add(idCobis);
		sqlRequest.add(estadoUsuario);

		return SqlMB.response(sqlRequest);
	}

	/**
	 * Devuelve los datos referidos al token de ISVA de la tabla soft_token_alta
	 * para un usuario específico y con soft token en un estado determinado.
	 *
	 * @param idCobis
	 * @param estadoUsuario
	 * @return SqlResponse
	 */
	public SqlResponseMB obtenerDatosTokenISVA(String idCobis, String estadoUsuario) {
		SqlRequestMB sqlRequest = SqlMB.request("ObtenerDatosTokenISVA", "mobile");
		sqlRequest.sql = "SELECT TOP 1 [id], [id_dispositivo], [access_token], [refresh_token], [scope], [token_type], [expires_in] " + "FROM [Mobile].[dbo].[soft_token_alta] " + "WHERE id_cobis = ? AND estado = ?";
		sqlRequest.add(idCobis);
		sqlRequest.add(estadoUsuario);

		return SqlMB.response(sqlRequest);
	}

	/**
	 * Consulta la tabla <tt>soft_token_forzado</tt> para determinar si un cliente
	 * tiene que forzar el alta de soft token.
	 *
	 * @param idCobis Id cobis del cliente.
	 * @return SqlResponse
	 */
	public SqlResponseMB consultarForzadoAltaSoftToken(String idCobis) {
		SqlRequestMB sqlRequest = SqlMB.request("TieneForzadoAltaSoftToken", "mobile");

		sqlRequest.sql = "SELECT [id], [id_cobis], [forzar_alta] FROM [Mobile].[dbo].[soft_token_forzado] WHERE id_cobis = ?";

		sqlRequest.add(idCobis);

		return SqlMB.response(sqlRequest);
	}

	public static boolean insertLogSoftToken(ContextoMB contexto, String dispositivo, Boolean estado) {

		try {
			SqlRequestMB sqlRequest = SqlMB.request("insertLogSoftToken", "homebanking");
			sqlRequest.sql = "INSERT INTO [Homebanking].[dbo].[logs_soft_token] (idCobis, canal, momento, dispositivo, estado) VALUES (?, ?, GETDATE(), ?, ?)";
			sqlRequest.add(contexto.idCobis());
			sqlRequest.add("MB");
			sqlRequest.add(dispositivo);
			sqlRequest.add(estado);
			SqlMB.response(sqlRequest);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	/**
	 * Consulta la tabla <tt>soft_token_alta</tt> para obtener las altas de un
	 * cliente en las últimas 24 horas
	 * 
	 * @param idCobis Id cobis del cliente.
	 * @return SqlResponse
	 */
	public SqlResponseMB consultarAltaSoftTokenUltimoDia(String idCobis) {
		SqlRequestMB sqlRequest = SqlMB.request("ConsultarAltaSoftTokenUltimoDia", "mobile");

		sqlRequest.sql = "SELECT [id], [id_cobis] FROM [Mobile].[dbo].[soft_token_alta] WHERE [id_cobis] = ? AND [estado] = 'ACTIVO' AND [fecha_alta] > GETDATE() - 1";

		sqlRequest.add(idCobis);

		return SqlMB.response(sqlRequest);
	}

	/**
	 * Consulta la tabla <tt>soft_token_alta</tt> para obtener las altas de un
	 * cliente en las últimas 48 horas hábiles
	 * 
	 * @param idCobis Id cobis del cliente.
	 * @param dias    cantidad de dias de anterioridad hábiles.
	 * @return SqlResponse
	 */
	public SqlResponseMB consultarAltaSoftTokenUltimas48HorasHabiles(String idCobis, String dias) {
		SqlRequestMB sqlRequest = SqlMB.request("ConsultarAltaSoftTokenUltimas48Habiles", "mobile");

		sqlRequest.sql = "SELECT [id], [id_cobis] FROM [Mobile].[dbo].[soft_token_alta] WHERE [id_cobis] = ? AND [estado] = 'ACTIVO' AND [fecha_alta] > GETDATE() - ".concat(dias);

		sqlRequest.add(idCobis);

		return SqlMB.response(sqlRequest);
	}

}
