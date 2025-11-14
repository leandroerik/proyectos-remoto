package ar.com.hipotecario.backend.servicio.sql.hb_be;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.sql.Sql;
import ar.com.hipotecario.backend.conector.sql.SqlObjeto;
import ar.com.hipotecario.backend.conector.sql.SqlObjetos;
import ar.com.hipotecario.backend.exception.SqlException;
import ar.com.hipotecario.backend.servicio.sql.hb_be.CuentasOBAnterior.CuentaOBAnterior;

@SuppressWarnings("serial")
public class CuentasOBAnterior extends SqlObjetos<CuentaOBAnterior> {

	/* ========== ATRIBUTOS ========== */
	public static class CuentaOBAnterior extends SqlObjeto {
		public String cue_cbu;
		public String emp_cuit;
		public String cue_numero;
		public String cue_nombre;
		public String tic_id;
		public String cue_debito;
		public String cue_consulta;
		public String cue_credito;
		public String cue_cuit;
		public String esc_estado;
		public String cue_moneda;
		public String cue_tipo_prod;
		public String cue_razon_social;
		public String cue_cat_prod;
		public String cue_id_prod;
		public String cue_vinculada_idCobisCli;

	}

	/* ========== SERVICIO ========== */

	public static CuentasOBAnterior getPorCuit(Contexto contexto, String cuit) {
		String sql = "";
		sql += "SELECT u.* ";
		sql += "FROM [HB_BE].[dbo].[BE_Cuenta] u ";
		sql += "WHERE emp_cuit = ?";
		Objeto datos = Sql.select(contexto, "hb_be", sql, cuit);
		SqlException.throwIf("NO_EXISTE_CUIT", datos.isEmpty());
		return map(datos, CuentasOBAnterior.class, CuentaOBAnterior.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "desarrollo");
		imprimirResultado(contexto, getPorCuit(contexto, "30612929455"));
	}
}
