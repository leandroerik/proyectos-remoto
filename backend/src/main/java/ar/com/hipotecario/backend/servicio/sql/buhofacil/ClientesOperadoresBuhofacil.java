package ar.com.hipotecario.backend.servicio.sql.buhofacil;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.sql.Sql;
import ar.com.hipotecario.backend.conector.sql.SqlObjeto;
import ar.com.hipotecario.backend.conector.sql.SqlObjetos;
import ar.com.hipotecario.backend.servicio.sql.buhofacil.ClientesOperadoresBuhofacil.ClienteOperadoresBuhofacil;

@SuppressWarnings("serial")
public class ClientesOperadoresBuhofacil extends SqlObjetos<ClienteOperadoresBuhofacil> {

	public static class ClienteOperadoresBuhofacil extends SqlObjeto {

	}

	public static Boolean deleteUsuarioBuhofacil(Contexto contexto, String idCobis) {

		String sql = "";
		sql += "DELETE FROM [dbo].[ApiSeg_BuhoFacil_Cliente] ";
		sql += "WHERE cli_id = ? ";

		return Sql.update(contexto, "buhofacil", sql, idCobis) > 0;
	}

	public static Boolean deleteClaveBuhofacil(Contexto contexto, String idCobis) {

		String sql = "";
		sql += "DELETE FROM [dbo].[ApiSeg_Clave_Cliente] ";
		sql += "WHERE cli_id = ? ";

		return Sql.update(contexto, "buhofacil", sql, idCobis) > 0;
	}

	public static Boolean delete(Contexto contexto, String idCobis) {

		try {

			if (deleteUsuarioBuhofacil(contexto, idCobis) && deleteClaveBuhofacil(contexto, idCobis)) {
				return true;
			}
		} catch (Exception e) {

		}

		return false;
	}
}
