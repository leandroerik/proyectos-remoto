package ar.com.hipotecario.backend.servicio.sql.salma;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.sql.Sql;
import ar.com.hipotecario.backend.conector.sql.SqlObjeto;
import ar.com.hipotecario.backend.conector.sql.SqlObjetos;
import ar.com.hipotecario.backend.servicio.sql.SqlSalma;
import ar.com.hipotecario.backend.servicio.sql.salma.ModificarLimiteMinimoSalma.ModificaLimiteMinimoSalma;

@SuppressWarnings("serial")
public class ModificarLimiteMinimoSalma extends SqlObjetos<ModificaLimiteMinimoSalma> {

	/* ========== ATRIBUTOS ========== */
	public static class ModificaLimiteMinimoSalma extends SqlObjeto {

		public String lin_Tc;
		public String lin_rot;
		public Fecha fec_fin;
	}

	public static Boolean modificar(Contexto contexto, String cuil, String limiteMinimo) {

		String sql = "";
		sql += "UPDATE t SET lin_Tc= ? , lin_rot= ? , fec_fin= '20260101' ";
		sql += "FROM preaprobado t ";
		sql += "WHERE id_lote > 5000 ";
		sql += "AND nrodoc =   ? ";
		sql += "AND fec_fin > GETDATE() ";

		return Sql.update(contexto, SqlSalma.SQL, sql, limiteMinimo, limiteMinimo, cuil) > 0;
	}

	public static ModificaLimiteMinimoSalma obtener(Contexto contexto, String cuil) {

		String sql = "";
		sql += "SELECT lin_Tc, lin_rot, fec_fin ";
		sql += "FROM preaprobado ";
		sql += "WHERE id_lote > 5000 ";
		sql += "AND nrodoc = ? ";
		sql += "AND fec_fin > GETDATE()";

		Objeto datos = Sql.select(contexto, SqlSalma.SQL, sql, cuil);
		return map(datos, ModificarLimiteMinimoSalma.class, ModificaLimiteMinimoSalma.class).first();
	}
}
