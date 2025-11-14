package ar.com.hipotecario.canal.homebanking.servicio;

import ar.com.hipotecario.canal.homebanking.conector.Sql;
import ar.com.hipotecario.canal.homebanking.conector.SqlRequest;
import ar.com.hipotecario.canal.homebanking.conector.SqlResponse;

public class SqlSoftTokenService {

	/**
	 * Consultar registros por estado (bloqueado o desbloqueado) por uso de soft
	 * token de un cliente.
	 *
	 * @param idCobis
	 * @param estado
	 * @return SqlResponse
	 */
	public SqlResponse consultarBloqueoUsoSoftTokenPorUsuario(String idCobis, boolean estado) {
		SqlRequest sqlRequest = Sql.request("ConsultarBloqueoPorUsoSoftTokenPorUsuario", "mobile");
		int estadoInt = estado ? 1 : 0;

		sqlRequest.sql = "SELECT [id], [id_cobis], [estado], [fecha], [fecha_ultima_actualizacion], [id_dispositivo] " + "FROM [Mobile].[dbo].[soft_token_uso_bloqueos] WHERE " + "id_cobis = ? AND estado = ?";

		sqlRequest.add(idCobis);
		sqlRequest.add(estadoInt);

		return Sql.response(sqlRequest);
	}

	/**
	 * Consultar registros por estado y por dispositivo (bloqueado o desbloqueado)
	 * por uso de soft token.
	 *
	 * @param idDispositivo
	 * @param estado
	 * @return SqlResponse
	 */
	public SqlResponse consultarBloqueoUsoSoftTokenPorDispositivo(String idDispositivo, boolean estado) {
		SqlRequest sqlRequest = Sql.request("ConsultarBloqueoPorUsoSoftTokenPorDispositivo", "mobile");
		int estadoInt = estado ? 1 : 0;

		sqlRequest.sql = "SELECT [id], [id_cobis], [estado], [fecha], [fecha_ultima_actualizacion], [id_dispositivo] " + "FROM [Mobile].[dbo].[soft_token_uso_bloqueos] WHERE " + "id_dispositivo = ? AND estado = ?";

		sqlRequest.add(idDispositivo);
		sqlRequest.add(estadoInt);

		return Sql.response(sqlRequest);
	}

	/**
	 * Consulta la cantidad de intentos de uso de soft token para un cliente en
	 * particular.
	 *
	 * @return SqlResponse
	 */
	public SqlResponse consultaIntentosUsoSoftToken(String idCobis, boolean activo, String fecha) {
		SqlRequest sqlRequest = Sql.request("ConsultarIntentosUsoSoftTokenUsuario", "mobile");
		int activoInt = activo ? 1 : 0;

		sqlRequest.sql = "SELECT [id], [id_cobis], [estado], [canal], [activo], [fecha] FROM " + "[Mobile].[dbo].[soft_token_uso_intentos] WHERE " + "id_cobis = ? AND activo = ? AND fecha > ?";

		sqlRequest.add(idCobis);
		sqlRequest.add(activoInt);
		sqlRequest.add(fecha);

		return Sql.response(sqlRequest);
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
		SqlRequest sqlRequest = Sql.request("InsertarIntentoUsoSoftToken", "mobile");
		int activoInt = activo ? 1 : 0;

		sqlRequest.sql = "INSERT INTO [Mobile].[dbo].[soft_token_uso_intentos] ([id_cobis], [estado], [canal], [activo], [fecha]) VALUES " + "(?, ?, ?, ?, ?)";

		sqlRequest.add(idCobis);
		sqlRequest.add(estado);
		sqlRequest.add(canal);
		sqlRequest.add(activoInt);
		sqlRequest.add(fecha);

		return !Sql.response(sqlRequest).hayError;
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
		SqlRequest sqlRequest = Sql.request("InsertarBloqueoUsoSoftToken", "mobile");
		int estadoInt = estado ? 1 : 0;

		sqlRequest.sql = "INSERT INTO [Mobile].[dbo].[soft_token_uso_bloqueos] " + "([id_cobis], [estado], [fecha], " + "[fecha_ultima_actualizacion], [id_dispositivo], [motivo]) VALUES (?, ?, ?, ?, ?, ?)";

		sqlRequest.add(idCobis);
		sqlRequest.add(estadoInt);
		sqlRequest.add(fecha);
		sqlRequest.add(fecha);
		sqlRequest.add(idDispositivo);
		sqlRequest.add(motivo);

		return !Sql.response(sqlRequest).hayError;
	}

	/**
	 * Actualiza a inactivos los intentos fallidos registrados en la tabla de
	 * soft_token_uso_intentos.
	 *
	 * @param idCobis
	 * @param estadoActivoAnterior
	 * @param estadoActivoNuevo
	 * @return
	 */
	public boolean limpiarIntentosFallidos(String idCobis, boolean estadoActivoAnterior, boolean estadoActivoNuevo) {
		SqlRequest sqlRequest = Sql.request("LimpiarIntentosFallidosDeUsoSoftToken", "mobile");
		int activoAnteriorInt = estadoActivoAnterior ? 1 : 0;
		int activoNuevoInt = estadoActivoNuevo ? 1 : 0;

		sqlRequest.sql = "UPDATE [Mobile].[dbo].[soft_token_uso_intentos] SET activo = ? WHERE id_cobis = ? AND activo = ?";

		sqlRequest.add(activoNuevoInt);
		sqlRequest.add(idCobis);
		sqlRequest.add(activoAnteriorInt);

		return !Sql.response(sqlRequest).hayError;
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
	public SqlResponse consultarAltaSoftToken(String idCobis, String estadoUsuario) {
		SqlRequest sqlRequest = Sql.request("ConsultarSoftTokenActivo", "mobile");
		sqlRequest.sql = "SELECT [id], [id_cobis], [clave_link], [fecha_alta], [estado], " + "[id_dispositivo], [fecha_ultima_actualizacion] " + "FROM [Mobile].[dbo].[soft_token_alta] " + "WHERE id_cobis = ? AND estado = ?";
		sqlRequest.add(idCobis);
		sqlRequest.add(estadoUsuario);

		return Sql.response(sqlRequest);
	}

	/**
	 * Actualiza registro de la tabla soft_token_alta.
	 *
	 * @param id
	 * @param estadoUsuario
	 * @param accessToken
	 * @param refreshToken
	 * @param expiresIn
	 * @param scope
	 * @param tokenType
	 * @param fechaUltimaActualizacion
	 * @param idDispositivo
	 * @return
	 */
	public boolean actualizarRegistrosAltaSoftToken(int id, String estadoUsuario, String accessToken, String refreshToken, String expiresIn, String scope, String tokenType, String fechaUltimaActualizacion, String idDispositivo) {
		SqlResponse sqlResponse;

		SqlRequest sqlRequest = Sql.request("ActualizarRegistroAltaSoftToken", "mobile");
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

		sqlResponse = Sql.response(sqlRequest);

		return !sqlResponse.hayError;
	}

	/**
	 * Devuelve los datos referidos al token de ISVA de la tabla soft_token_alta
	 * para un usuario específico y con soft token en un estado determinado.
	 *
	 * @param idCobis
	 * @param estadoUsuario
	 * @return SqlResponse
	 */
	public SqlResponse obtenerDatosTokenISVA(String idCobis, String estadoUsuario) {
		SqlRequest sqlRequest = Sql.request("Con", "mobile");
		sqlRequest.sql = "SELECT TOP 1 [id], [id_dispositivo], [access_token], [refresh_token], [scope], [token_type], [expires_in] " + "FROM [Mobile].[dbo].[soft_token_alta] " + "WHERE id_cobis = ? AND estado = ?";
		sqlRequest.add(idCobis);
		sqlRequest.add(estadoUsuario);

		return Sql.response(sqlRequest);
	}

	/**
	 * Consulta la tabla <tt>soft_token_forzado</tt> para determinar si un cliente
	 * tiene que forzar el alta de soft token.
	 *
	 * @param idCobis Id cobis del cliente.
	 * @return SqlResponse
	 */
	public SqlResponse consultarForzadoAltaSoftToken(String idCobis) {
		SqlRequest sqlRequest = Sql.request("TieneForzadoAltaSoftToken", "mobile");

		sqlRequest.sql = "SELECT [id], [id_cobis], [forzar_alta] FROM [Mobile].[dbo].[soft_token_forzado] WHERE id_cobis = ?";

		sqlRequest.add(idCobis);

		return Sql.response(sqlRequest);
	}

	/**
	 * Consulta la tabla <tt>soft_token_alta</tt> para obtener las altas de un
	 * cliente en las últimas 24 horas
	 * 
	 * @param idCobis Id cobis del cliente.
	 * @return SqlResponse
	 */
	public SqlResponse consultarAltaSoftTokenUltimoDia(String idCobis) {
		SqlRequest sqlRequest = Sql.request("ConsultarAltaSoftTokenUltimoDia", "mobile");

		sqlRequest.sql = "SELECT [id], [id_cobis] FROM [Mobile].[dbo].[soft_token_alta] WHERE [id_cobis] = ? AND [estado] = 'ACTIVO' AND [fecha_alta] > GETDATE() - 1";

		sqlRequest.add(idCobis);

		return Sql.response(sqlRequest);
	}
}
