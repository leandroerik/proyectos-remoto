package ar.com.hipotecario.backend.servicio.sql.homebanking;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.conector.sql.Sql;
import ar.com.hipotecario.backend.conector.sql.SqlObjeto;
import ar.com.hipotecario.backend.conector.sql.SqlObjetos;
import ar.com.hipotecario.backend.servicio.sql.homebanking.LogsEnvioOtpHB.LogLogin;

@SuppressWarnings("serial")
public class LogsEnvioOtpHB extends SqlObjetos<LogLogin> {

	/* ========== ATRIBUTOS ========== */
	public static class LogLogin extends SqlObjeto {
		public Long id;
		public String idCobis;
		public String canal;
		public Fecha momento;
		public String direccionIp;
		public String celular;
		public String email;
		public Integer riesgoNet;
		public Integer link;
		public String estado;
	}

	/* ========== SERVICIO ========== */
	public static Boolean insert(Contexto contexto, String usuario, String celular, String email, Boolean riesgoNet, Boolean link, String estado) {
		String sql = "INSERT INTO [Homebanking].[dbo].[logs_envios_otp] (idCobis,canal,momento,direccionIp,celular,email,riesgoNet,link,estado) VALUES (?, ?, GETDATE(), ?, ?, ?, ?, ?, ?)";
		Object[] parametros = new Object[8];
		parametros[0] = usuario;
		parametros[1] = "HB";
		parametros[2] = contexto.ip();
		parametros[3] = celular;
		parametros[4] = email;
		parametros[5] = riesgoNet ? 1 : 0;
		parametros[6] = link ? 1 : 0;
		parametros[7] = estado;
		return Sql.update(contexto, "homebanking", sql, parametros) == 1;
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		Boolean exito = insert(contexto, "135706", "151515154", "aaa@aaa.com", false, false, "P");
		imprimirResultado(contexto, exito);
	}
}
