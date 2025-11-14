package ar.com.hipotecario.backend.servicio.sql.homebanking;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.conector.sql.Sql;
import ar.com.hipotecario.backend.conector.sql.SqlObjeto;
import ar.com.hipotecario.backend.conector.sql.SqlObjetos;
import ar.com.hipotecario.backend.servicio.sql.homebanking.LogsLoginHB.LogLogin;

@SuppressWarnings("serial")
public class LogsLoginHB extends SqlObjetos<LogLogin> {

	/* ========== ATRIBUTOS ========== */
	public static class LogLogin extends SqlObjeto {
		public Long id;
		public String idCobis;
		public String canal;
		public Fecha momento;
		public String direccionIp;
	}

	/* ========== SERVICIO ========== */
	public static Boolean insert(Contexto contexto, String usuario) {
		String sql = "INSERT INTO [Homebanking].[dbo].[logs_login] (idCobis, canal, momento, direccionIp) VALUES (?, ?, GETDATE(), ?)";
		Object[] parametros = new Object[3];
		parametros[0] = usuario;
		parametros[1] = "HB";
		parametros[2] = contexto.ip();
		return Sql.update(contexto, "homebanking", sql, parametros) == 1;
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		Boolean exito = insert(contexto, "135706");
		imprimirResultado(contexto, exito);
	}
}
