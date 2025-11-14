package ar.com.hipotecario.backend.servicio.sql.clientesoperadores;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.sql.Sql;
import ar.com.hipotecario.backend.conector.sql.SqlObjeto;
import ar.com.hipotecario.backend.conector.sql.SqlObjetos;
import ar.com.hipotecario.backend.servicio.sql.clientesoperadores.OpUsers.OpUser;

@SuppressWarnings("serial")
public class OpUsers extends SqlObjetos<OpUser> {

	/* ========== ATRIBUTOS ========== */
	public static class OpUser extends SqlObjeto {
		public String id;
		public String cardId;
		public String clientId;
		public Fecha creation;
		public Fecha modification;
		public Fecha expiration;
		public String attempts;
		public String accessType;
	}

	public static OpUser select(Contexto contexto, String usuario) {
		String sql = "SELECT * FROM [hbs].[dbo].[op_user] WHERE [id] = ?";
		Objeto datos = Sql.select(contexto, "hbs", sql, usuario);
		return map(datos, OpUsers.class, OpUser.class).first();
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		String test = "select";
		if (test.equals("select")) {
			Contexto contexto = contexto("HB", "homologacion");
			OpUser datos = select(contexto, "cadolar2");
			imprimirResultado(contexto, datos);
		}

	}
}
