package ar.com.hipotecario.backend.servicio.sql.homebanking;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.sql.Sql;
import ar.com.hipotecario.backend.conector.sql.SqlObjeto;
import ar.com.hipotecario.backend.conector.sql.SqlObjetos;
import ar.com.hipotecario.backend.exception.SqlException;
import ar.com.hipotecario.backend.servicio.sql.homebanking.MuestreoHB.Muestreo;

@SuppressWarnings("serial")
public class MuestreoHB extends SqlObjetos<Muestreo> {

	/* ========== ATRIBUTOS ========== */
	public static class Muestreo extends SqlObjeto {
		public String m_id;
		public String m_tipoMuestra;
		public String m_valor;
		public String m_subid;
	}

	/* ========== SERVICIO ========== */
	public static Muestreo select(Contexto contexto, String usuario, String tipoMuestra, String valor) {
		String sql = "SELECT * FROM [homebanking].[dbo].[muestreo] WHERE m_subid = ? ";
		sql += "and m_tipoMuestra = ? and m_valor = ?";
		Objeto datos = Sql.select(contexto, "homebanking", sql, usuario, tipoMuestra, valor);

		SqlException.throwIf("USUARIO_BLOQUEADO_POR_FRAUDE", datos.objetos().size() > 0);

		return map(datos, MuestreoHB.class, Muestreo.class).first();
	}

	public static Boolean insert(Contexto contexto, String usuario, String tipoMuestra, String valor) {
		String sql = "INSERT INTO [homebanking].[dbo].[muestreo] (m_tipoMuestra, m_valor, m_subid) VALUES(?, ?, ?) ";
		Integer exito = Sql.update(contexto, "homebanking", sql, "deshabilitar.acceso.fraude", "true", usuario);
		return exito == 1;
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		String test = "select";
		if (test.equals("select")) {
			Contexto contexto = contexto("HB", "homologacion");
			Muestreo datos = select(contexto, "174240", "deshabilitar.acceso.fraude", "true");
			imprimirResultado(contexto, datos);
		}

	}
}
