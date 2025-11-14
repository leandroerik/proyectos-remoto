package ar.com.hipotecario.backend.servicio.sql.homebanking;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.sql.Sql;
import ar.com.hipotecario.backend.conector.sql.SqlObjeto;
import ar.com.hipotecario.backend.conector.sql.SqlObjetos;
import ar.com.hipotecario.backend.servicio.sql.homebanking.UltimasConexionesHB.UltimaConexionHB;

@SuppressWarnings("serial")
public class UltimasConexionesHB extends SqlObjetos<UltimaConexionHB> {

	/* ========== ATRIBUTOS ========== */
	public static class UltimaConexionHB extends SqlObjeto {
		public String idCobis;
		public Fecha momento;
	}

	/* ========== SERVICIO ========== */
	public static UltimaConexionHB select(Contexto contexto, String usuario) {
		String sql = "SELECT TOP 1 [idCobis], [momento] FROM [Homebanking].[dbo].[ultima_conexion] WHERE [idCobis] = ?";
		Objeto datos = Sql.select(contexto, "homebanking", sql, usuario);
		return map(datos, UltimasConexionesHB.class, UltimaConexionHB.class).first();
	}

	public static Boolean update(Contexto contexto, String usuario) {
		String sql = "UPDATE [Homebanking].[dbo].[ultima_conexion] SET [momento] = getdate() WHERE [idCobis] = ? ";
		sql += "IF @@ROWCOUNT = 0 ";
		sql += "INSERT INTO [Homebanking].[dbo].[ultima_conexion] ([idCobis], [momento]) VALUES (?, getdate()) ";

		return Sql.update(contexto, "homebanking", sql, usuario, usuario) == 1;
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		String test = "select";
		if (test.equals("select")) {
			Contexto contexto = contexto("HB", "homologacion");
			UltimaConexionHB datos = select(contexto, "135706");
			imprimirResultado(contexto, datos);
		}

		if (test.equals("update")) {
			Contexto contexto = contexto("HB", "homologacion");
			Boolean exito = update(contexto, "135706");
			imprimirResultado(contexto, exito);
		}
	}
}
