package ar.com.hipotecario.backend.servicio.sql.homebanking;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.sql.Sql;
import ar.com.hipotecario.backend.conector.sql.SqlObjeto;
import ar.com.hipotecario.backend.conector.sql.SqlObjetos;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.sql.homebanking.SessionHB.Session;

@SuppressWarnings("serial")
public class SessionHB extends SqlObjetos<Session> {

	/* ========== ATRIBUTOS ========== */
	public static class Session extends SqlObjeto {
		public String s_cod_cliente;
		public String s_fingerprint;
		public Fecha s_logintimestamp;
		public String s_canal;
	}

	/* ========== SERVICIO ========== */
	public static Session select(Contexto contexto, String usuario, String fingerprint) {
		String sql = "SELECT * FROM [Homebanking].[dbo].[session] WHERE s_cod_cliente = ? AND s_login_timestamp > DATEADD(MINUTE, -10, getdate()) AND s_fingerprint = ?";
		Objeto datos = Sql.select(contexto, "homebanking", sql, usuario, fingerprint);

		ApiException.throwIf("EXISTE_SESION", datos.objetos().size() > 0, null, null);

		return map(datos, SessionHB.class, Session.class).first();
	}

	public static Boolean update(Contexto contexto, String usuario, String fingerprint) {
		String sql = "DELETE FROM [Homebanking].[dbo].[session] WHERE s_cod_cliente = ? AND s_login_timestamp <= DATEADD(MINUTE, -10, getdate())" + " INSERT INTO [Homebanking].[dbo].[session] ([s_cod_cliente], [s_fingerprint], [s_login_timestamp], [s_canal]) VALUES (?, ?, GETDATE(), 'HB')";
		return Sql.update(contexto, "homebanking", sql, usuario, usuario, fingerprint) == 1;
	}

	public static Boolean delete(Contexto contexto, String usuario) {
		String sql = "DELETE FROM [Homebanking].[dbo].[session] WHERE s_cod_cliente = ?";
		return Sql.update(contexto, "homebanking", sql, usuario) == 1;
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		String test = "select";
		if (test.equals("select")) {
			Contexto contexto = contexto("HB", "homologacion");
			Session datos = select(contexto, "1149435", "52a5018a-660b-4136-92c8-ef9083ae8f32");
			imprimirResultado(contexto, datos);
		}
		if (test.equals("insert")) {
			Contexto contexto = contexto("HB", "homologacion");
			Boolean exito = update(contexto, "1149435", "52a5018a-660b-4136-92c8-ef9083ae8f32");
			imprimirResultado(contexto, exito);
		}
		if (test.equals("delete")) {
			Contexto contexto = contexto("HB", "homologacion");
			Boolean exito = delete(contexto, "1149435");
			imprimirResultado(contexto, exito);
		}
	}
}
