package ar.com.hipotecario.backend.servicio.sql.intercambioate;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.sql.Sql;
import ar.com.hipotecario.backend.conector.sql.SqlObjeto;
import ar.com.hipotecario.backend.conector.sql.SqlObjetos;
import ar.com.hipotecario.backend.exception.SqlException;
import ar.com.hipotecario.backend.servicio.sql.intercambioate.BancosInterbanking.BancoInterbanking;

@SuppressWarnings("serial")
public class BancosInterbanking extends SqlObjetos<BancoInterbanking> {

	/* ========== ATRIBUTOS ========== */
	public static class BancoInterbanking extends SqlObjeto {
		public Integer ba_banco;
		public String ba_clave;
		public String ba_estado;
	}

	/* ========== SERVICIO ========== */
	public static BancosInterbanking get(Contexto contexto, String canal, String banco) {
		String sql = "select * from IntercambioATE..dn_bancos_claves " + "where ba_banco = ?";
		Objeto datos = Sql.select(contexto, "intercambioate", sql, String.valueOf(banco));
		SqlException.throwIf("BANCO_NO_RUC", datos.isEmpty());
		return map(datos, BancosInterbanking.class, BancoInterbanking.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		BancosInterbanking datos = get(contexto, "OB", "389");
		imprimirResultado(contexto, datos);
	}
}