package ar.com.hipotecario.mobile.servicio;

import ar.com.hipotecario.mobile.conector.SqlMB;
import ar.com.hipotecario.mobile.conector.SqlRequestMB;
import ar.com.hipotecario.mobile.conector.SqlResponseMB;

public class SqlScanDni {

	private static final String CONSULTAR_EXISTE = "SELECT [id] FROM [Mobile].[dbo].[usuarios] WHERE [id_cobis] = ?";
	private static final String CONSULTAR_ULTIMO_USUARIO_RENAPER = "SELECT TOP 1 [ur].*,[ure].[descripcion],[ure].[enumerador] FROM [Mobile].[dbo].[usuario_renaper] [ur] INNER JOIN [Mobile].[dbo].[usuario_renaper_estados] [ure] ON [ur].[id_estado] = [ure].[id] WHERE [ur].[cuil] = ? ORDER BY [ur].[id] DESC";
	private static final String COINCIDE_TOKEN = "SELECT [id] FROM [Mobile].[dbo].[usuarios] WHERE [id_cobis] = ? AND [token] = ?";
	private static final String OBTENER_USUARIO = "SELECT * FROM [Mobile].[dbo].[usuarios] WHERE [id_cobis] = ?";
	private static final String INSERTAR_USUARIO_TOKEN = "INSERT INTO [Mobile].[dbo].[usuario_token] ([id_cobis], [token_viejo],[token_nuevo] ,[id_dispositivo], [numero_telefono] ,[fecha_creacion]) VALUES (?, ?, ?, ?, ?, GETDATE())";
	private static final String INSERTAR_USUARIO_RENAPER = "INSERT INTO [Mobile].[dbo].[usuario_renaper] ([id_tramite],[ejemplar],[vencimiento],[fecha_emision],[apellido],[nombre],[fecha_nacimiento],[cuil],[calle],[numero],[piso],[departamento],[codigo_postal],[barrio],[monoblock],[ciudad],[municipio],[provincia],[pais],[id_dispositivo],[id_estado]) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
	private static final String OBTENER_USUARIO_RENAPER_POR_ENUMERADOR = "SELECT [id],[descripcion],[enumerador] FROM [Mobile].[dbo].[usuario_renaper_estados] WHERE [enumerador] = ?";
	private static final String ACTUALIZAR_ESTADO_USUARIO_RENAPER = "UPDATE [Mobile].[dbo].[usuario_renaper] SET [id_estado] = (SELECT id FROM [Mobile].[dbo].[usuario_renaper_estados] WHERE [enumerador] = 'VALIDADO') WHERE [cuil] = ? AND [id_estado] = (SELECT id FROM [Mobile].[dbo].[usuario_renaper_estados] WHERE [enumerador] = 'VALIDARSUCURSAL')";
	private static final String ACTUALIZAR_ESTADO_USUARIO_RENAPER_SUCURSAL = "UPDATE [Mobile].[dbo].[usuario_renaper] SET [validado_sucursal] = 1, [id_estado] = (SELECT id FROM [Mobile].[dbo].[usuario_renaper_estados] WHERE [enumerador] = 'VALIDADO') WHERE [cuil] = ? AND [id_estado] = (SELECT id FROM [Mobile].[dbo].[usuario_renaper_estados] WHERE [enumerador] = 'VALIDARSUCURSAL')";
	private static final String ACTUALIZAR_VALIDADO_USUARIO_RENAPER_SUCURSAL = "UPDATE [Mobile].[dbo].[usuario_renaper] SET [validado_sucursal] = 0 WHERE [cuil] = ? AND [id_estado] = (SELECT id FROM [Mobile].[dbo].[usuario_renaper_estados] WHERE [enumerador] = 'VALIDADO')";

	/**
	 * Consulta si el Usuario existe en Mobile.
	 *
	 * @param idCobis
	 * @return existe
	 */
	public static boolean usuarioExiste(String idCobis) {
		SqlRequestMB sqlRequest = SqlMB.request("ConsultarExisteUsuario", "mobile");
		sqlRequest.sql = CONSULTAR_EXISTE;
		sqlRequest.add(idCobis);
		SqlResponseMB sqlResponse = SqlMB.response(sqlRequest);
		if (sqlResponse.hayError)
			return false;
		return !sqlResponse.registros.isEmpty();
	}

	/**
	 * Obtengo el Usuario en Mobile.
	 *
	 * @param idCobis
	 * @return response
	 */
	public static SqlResponseMB obtenerUsuario(String idCobis) {
		SqlRequestMB sqlRequest = SqlMB.request("ObtenerUsuario", "mobile");
		sqlRequest.sql = OBTENER_USUARIO;
		sqlRequest.add(idCobis);
		return SqlMB.response(sqlRequest);
	}

	/**
	 * Obtiene el último registro de usuario con renaper.
	 *
	 * @param cuit
	 * @return response
	 */
	public static SqlResponseMB obtenerUltimoUsuarioRenaper(String cuit) {
		SqlRequestMB sqlRequest = SqlMB.request("ConsultarUsuarioRenaper", "mobile");
		sqlRequest.sql = CONSULTAR_ULTIMO_USUARIO_RENAPER;
		sqlRequest.add(cuit);
		return SqlMB.response(sqlRequest);
	}

	/**
	 * Consulta si coincide el token del Usuario.
	 *
	 * @param idCobis
	 * @param token
	 * @return coincide
	 */
	public static boolean coincideToken(String idCobis, String token) {
		SqlRequestMB sqlRequest = SqlMB.request("CoincideTokenUsuario", "mobile");
		sqlRequest.sql = COINCIDE_TOKEN;
		sqlRequest.add(idCobis);
		sqlRequest.add(token);
		SqlResponseMB sqlResponse = SqlMB.response(sqlRequest);
		if (sqlResponse.hayError)
			return false;
		return !sqlResponse.registros.isEmpty();
	}

	/**
	 * Guarda el historial del Usuario.
	 *
	 * @param idCobis
	 * @return correcto
	 */
	public static boolean guardarUsuarioToken(String idCobis, String tokenViejo, String tokenNuevo, String idDispositivo, String numeroTelefono) {
		SqlRequestMB sqlRequest = SqlMB.request("InsertarUsuarioToken", "mobile");
		sqlRequest.sql = INSERTAR_USUARIO_TOKEN;
		sqlRequest.add(idCobis);
		sqlRequest.add(tokenViejo);
		sqlRequest.add(tokenNuevo);
		sqlRequest.add(idDispositivo);
		sqlRequest.add(numeroTelefono);
		return !SqlMB.response(sqlRequest).hayError;
	}

	/**
	 * Obtiene el estado por el enumerador.
	 *
	 * @param enumerador
	 * @return sqlRespose
	 */
	public static SqlResponseMB obtenerUsuarioRenaperEstadoPorEnumerador(String enumerador) {
		SqlRequestMB sqlRequest = SqlMB.request("ObtenerRenaperEstadoPorEnumerador", "mobile");
		sqlRequest.sql = OBTENER_USUARIO_RENAPER_POR_ENUMERADOR;
		sqlRequest.add(enumerador);
		return SqlMB.response(sqlRequest);
	}

	/**
	 * Guarda el Usuario Renaper.
	 *
	 * @param idCobis
	 * @return correcto
	 */
	public static boolean guardarUsuarioRenaper(String idTramite, String ejemplar, String vencimiento, String fechaEmision, String apellido, String nombre, String fechaNacimiento, String cuil, String calle, String numero, String piso, String departamento, String codigoPostal, String barrio, String monoblock, String ciudad, String municipio, String provincia, String pais, String idDispositivo, int idEstado) {
		SqlRequestMB sqlRequest = SqlMB.request("InsertarUsuarioRenaper", "mobile");
		sqlRequest.sql = INSERTAR_USUARIO_RENAPER;
		sqlRequest.add(idTramite);
		sqlRequest.add(ejemplar);
		sqlRequest.add(vencimiento);
		sqlRequest.add(fechaEmision);
		sqlRequest.add(apellido);
		sqlRequest.add(nombre);
		sqlRequest.add(fechaNacimiento);
		sqlRequest.add(cuil);
		sqlRequest.add(calle);
		sqlRequest.add(numero);
		sqlRequest.add(piso);
		sqlRequest.add(departamento);
		sqlRequest.add(codigoPostal);
		sqlRequest.add(barrio);
		sqlRequest.add(monoblock);
		sqlRequest.add(ciudad);
		sqlRequest.add(municipio);
		sqlRequest.add(provincia);
		sqlRequest.add(pais);
		sqlRequest.add(idDispositivo);
		sqlRequest.add(idEstado);
		return !SqlMB.response(sqlRequest).hayError;
	}

	/**
	 * Modificar estado de usuario renaper a validado.
	 *
	 * @param enumerador
	 * @return correcto
	 */
	public static boolean actualizarEstadoUsuarioValidado(String cuit) {
		SqlRequestMB sqlRequest = SqlMB.request("ActualizarEstadoValidado", "mobile");
		sqlRequest.sql = ACTUALIZAR_ESTADO_USUARIO_RENAPER;
		sqlRequest.add(cuit);
		return !SqlMB.response(sqlRequest).hayError;
	}

	/**
	 * Modificar estado de usuario renaper a validado desde sucursal.
	 *
	 * @param cuit
	 * @return correcto
	 */
	public static boolean actualizarEstadoUsuarioValidadoDesdeSucursal(String cuit) {
		SqlRequestMB sqlRequest = SqlMB.request("ActualizarEstadoValidadoSucursal", "mobile");
		sqlRequest.sql = ACTUALIZAR_ESTADO_USUARIO_RENAPER_SUCURSAL;
		sqlRequest.add(cuit);
		return !SqlMB.response(sqlRequest).hayError;
	}

	/**
	 * Modificar el validado desde sucursal.
	 *
	 * @param cuit
	 */
	public static boolean modificarValidadoUltimoUsuarioRenaper(String cuit) {
		SqlRequestMB sqlRequest = SqlMB.request("ActualizarValidadoSucursal", "mobile");
		sqlRequest.sql = ACTUALIZAR_VALIDADO_USUARIO_RENAPER_SUCURSAL;
		sqlRequest.add(cuit);
		return !SqlMB.response(sqlRequest).hayError;
	}

}
