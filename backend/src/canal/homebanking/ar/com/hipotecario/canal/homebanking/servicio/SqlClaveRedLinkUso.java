package ar.com.hipotecario.canal.homebanking.servicio;

import ar.com.hipotecario.canal.homebanking.conector.Sql;
import ar.com.hipotecario.canal.homebanking.conector.SqlRequest;
import ar.com.hipotecario.canal.homebanking.conector.SqlResponse;

public class SqlClaveRedLinkUso {

	private static final String INSERTAR = "INSERT INTO [Homebanking].[dbo].[clave_red_link_uso] ( [id_cobis], [id_tarjeta_debito], [clave], [canal], [momento] ) VALUES ( ?, ?, ?, ?, ? )";
	private static final String CONSULTAR_ULTIMO_USO_POR_COBIS = "SELECT * FROM [Homebanking].[dbo].[clave_red_link_uso] WHERE [id_cobis] = ? AND [id_tarjeta_debito] = ? AND [clave] = ?";

	/**
	 * Guarda la clave.
	 *
	 * @return correcto
	 */
	public static boolean insertarClave(String idCobis, String idTarjetaDebito, String clave, String canal, String momento) {
		SqlRequest sqlRequest = Sql.request("InsertarClaveRedLinkUso", "homebanking");

		sqlRequest.sql = INSERTAR;
		sqlRequest.add(idCobis);
		sqlRequest.add(idTarjetaDebito);
		sqlRequest.add(clave);
		sqlRequest.add(canal);
		sqlRequest.add(momento);

		return !Sql.response(sqlRequest).hayError;
	}

	/**
	 * Obtener si usó la clave.
	 *
	 * @return correcto
	 */
	public static SqlResponse consultarUltimoUso(String idCobis, String idTarjetaDebito, String clave) {
		SqlRequest sqlRequest = Sql.request("ConsultarClaveRedLinkUso", "homebanking");
		sqlRequest.sql = CONSULTAR_ULTIMO_USO_POR_COBIS;

		sqlRequest.add(idCobis);
		sqlRequest.add(idTarjetaDebito);
		sqlRequest.add(clave);

		return Sql.response(sqlRequest);
	}

}
