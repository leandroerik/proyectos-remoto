package ar.com.hipotecario.mobile.servicio;

import ar.com.hipotecario.mobile.conector.SqlMB;
import ar.com.hipotecario.mobile.conector.SqlRequestMB;
import ar.com.hipotecario.mobile.conector.SqlResponseMB;

public class SqlClaveRedLinkUso {

	private static final String INSERTAR = "INSERT INTO [Homebanking].[dbo].[clave_red_link_uso] ( [id_cobis], [id_tarjeta_debito], [clave], [canal], [momento] ) VALUES ( ?, ?, ?, ?, ? )";
	private static final String CONSULTAR_ULTIMO_USO_POR_COBIS = "SELECT * FROM [Homebanking].[dbo].[clave_red_link_uso] WHERE [id_cobis] = ? AND [id_tarjeta_debito] = ? AND [clave] = ?";

	/**
	 * Guarda la clave.
	 *
	 * @return correcto
	 */
	public static boolean insertarClave(String idCobis, String idTarjetaDebito, String clave, String canal, String momento) {
		SqlRequestMB sqlRequest = SqlMB.request("InsertarClaveRedLinkUso", "homebanking");

		sqlRequest.sql = INSERTAR;
		sqlRequest.add(idCobis);
		sqlRequest.add(idTarjetaDebito);
		sqlRequest.add(clave);
		sqlRequest.add(canal);
		sqlRequest.add(momento);

		return !SqlMB.response(sqlRequest).hayError;
	}

	/**
	 * Obtener si usó la clave.
	 *
	 * @return correcto
	 */
	public static SqlResponseMB consultarUltimoUso(String idCobis, String idTarjetaDebito, String clave) {
		SqlRequestMB sqlRequest = SqlMB.request("ConsultarClaveRedLinkUso", "homebanking");
		sqlRequest.sql = CONSULTAR_ULTIMO_USO_POR_COBIS;

		sqlRequest.add(idCobis);
		sqlRequest.add(idTarjetaDebito);
		sqlRequest.add(clave);

		return SqlMB.response(sqlRequest);
	}

}
