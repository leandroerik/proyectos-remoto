package ar.com.hipotecario.mobile.servicio;

import ar.com.hipotecario.mobile.conector.SqlMB;
import ar.com.hipotecario.mobile.conector.SqlRequestMB;
import ar.com.hipotecario.mobile.conector.SqlResponseMB;

public class SqlRegistroDispositivo {

	private static final String INSERTAR_DISPOSITIVO = "INSERT INTO [Mobile].[dbo].[registro_dispositivo] ( [id_cobis], [id_dispositivo], [alias], [direccion_ip] ) VALUES ( ?, ?, ?, ? )";
	private static final String CONSULTAR_ULTIMO_DISPOSITIVO_REGISTRADO_POR_COBIS = "SELECT TOP 1 * FROM [Mobile].[dbo].[registro_dispositivo] WHERE [id_cobis] = ? ORDER BY [fecha_alta] DESC";
	private static final String CONSULTAR_DISPOSITIVOS_REGISTRADOS_POR_COBIS = "SELECT * FROM [Mobile].[dbo].[registro_dispositivo] WHERE [id_cobis] = ?";
	private static final String CONSULTAR_FORZAR_ALTA_POR_COBIS = "SELECT * FROM [Mobile].[dbo].[registro_dispositivo_forzado] WHERE [id_cobis] = ? AND [forzar_alta] = 1";
	private static final String CONSULTAR_REGISTRO_DISPOSITIVO_ULTIMAS24HS_POR_COBIS = "SELECT * FROM [Mobile].[dbo].[registro_dispositivo] WHERE [id_cobis] = ? AND [fecha_alta] > GETDATE() - 1";

	/**
	 * Guarda el dispositivo.
	 *
	 * @param idCobis
	 * @param idDispositivo
	 * @param alias
	 * @return correcto
	 */
	public static boolean insertarDispositivo(String idCobis, String idDispositivo, String alias, String direccionIp) {
		SqlRequestMB sqlRequest = SqlMB.request("InsertarRegistroDispositivo", "mobile");
		sqlRequest.sql = INSERTAR_DISPOSITIVO;
		sqlRequest.add(idCobis);
		sqlRequest.add(idDispositivo);
		sqlRequest.add(alias);
		sqlRequest.add(direccionIp);
		return !SqlMB.response(sqlRequest).hayError;
	}

	/**
	 * Busca el último Dispositivo Registrado.
	 *
	 * @param idCobis
	 * @return response
	 */
	public static SqlResponseMB ultimoRegistrado(String idCobis) {
		SqlRequestMB sqlRequest = SqlMB.request("ConsultarUltimoDispositivoRegistrado", "mobile");
		sqlRequest.sql = CONSULTAR_ULTIMO_DISPOSITIVO_REGISTRADO_POR_COBIS;
		sqlRequest.add(idCobis);
		return SqlMB.response(sqlRequest);
	}

	/**
	 * Busca todos los Dispositivos Registrados.
	 *
	 * @param idCobis
	 * @return response
	 */
	public static SqlResponseMB obtenerDispositivosRegistrados(String idCobis) {
		SqlRequestMB sqlRequest = SqlMB.request("ConsultarDispositivosRegistrados", "mobile");
		sqlRequest.sql = CONSULTAR_DISPOSITIVOS_REGISTRADOS_POR_COBIS;
		sqlRequest.add(idCobis);
		return SqlMB.response(sqlRequest);
	}

	/**
	 * Busca si el usuario está en la tabla de forzado de registro de dispositivo.
	 *
	 * @param idCobis
	 * @return response
	 */
	public static SqlResponseMB obtenerForzadoRegistroDispositivo(String idCobis) {
		SqlRequestMB sqlRequest = SqlMB.request("ConsultarForzadoDispositivosRegistrados", "mobile");
		sqlRequest.sql = CONSULTAR_FORZAR_ALTA_POR_COBIS;
		sqlRequest.add(idCobis);
		return SqlMB.response(sqlRequest);
	}

	/**
	 * Busca si el usuario registró dispositivo en las últimas 24 horas.
	 *
	 * @param idCobis
	 * @return response
	 */
	public static SqlResponseMB obtenerRegistroDispositivoUltimas24hsPorCobis(String idCobis) {
		SqlRequestMB sqlRequest = SqlMB.request("ConsultarRegistroDispositivoUltimas24hs", "mobile");
		sqlRequest.sql = CONSULTAR_REGISTRO_DISPOSITIVO_ULTIMAS24HS_POR_COBIS;
		sqlRequest.add(idCobis);

		return SqlMB.response(sqlRequest);
	}
}
