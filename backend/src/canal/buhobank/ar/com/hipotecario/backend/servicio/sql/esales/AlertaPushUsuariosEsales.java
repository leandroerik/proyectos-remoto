package ar.com.hipotecario.backend.servicio.sql.esales;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.sql.Sql;
import ar.com.hipotecario.backend.conector.sql.SqlObjeto;
import ar.com.hipotecario.backend.conector.sql.SqlObjetos;
import ar.com.hipotecario.backend.servicio.sql.SqlBuhoBank;
import ar.com.hipotecario.backend.servicio.sql.SqlEsales;
import ar.com.hipotecario.backend.servicio.sql.esales.AlertaPushUsuariosEsales.AlertaPushUsuarioEsales;
import ar.com.hipotecario.canal.buhobank.GeneralBB;

@SuppressWarnings("serial")
public class AlertaPushUsuariosEsales extends SqlObjetos<AlertaPushUsuarioEsales> {

	public static class AlertaPushUsuarioEsales extends SqlObjeto {

		public String id;
		public String codigoAlerta;
		public String minutosDesdeAbandono;

		public String cuil;
		public String plataforma;
		
		public String tokenFirebase;
		public String titulo;
		public String texto;
		public String url;

		public String mail;
		public String asuntoMail;
		public String plantillaMail;
	}

	public static AlertaPushUsuariosEsales pendientesPush(Contexto contexto) {
		String sql = "";
		sql += "SELECT * ";
		sql += "FROM [dbo].[Alerta_Push_Usuarios] AS t1 WITH (NOLOCK) ";
		sql += "INNER JOIN [dbo].[Alerta_Push] AS t2 WITH (NOLOCK) ";
		sql += "ON t1.codigoAlerta = t2.codigoAlerta ";
		sql += "WHERE t1.estado IS NULL ";
		sql += "AND t1.fechaUltimoAbandono > ? ";

		Objeto datos = Sql.select(contexto, SqlEsales.SQL, sql, Fecha.ahora().restarHoras(2));
		return map(datos, AlertaPushUsuariosEsales.class, AlertaPushUsuarioEsales.class);
	}
	
	public static AlertaPushUsuariosEsales pendientesMail(Contexto contexto) {
		String sql = "";
		sql += "SELECT * ";
		sql += "FROM [dbo].[Alerta_Push_Usuarios] AS t1 WITH (NOLOCK) ";
		sql += "INNER JOIN [dbo].[Alerta_Push] AS t2 WITH (NOLOCK) ";
		sql += "ON t1.codigoAlerta = t2.codigoAlerta ";
		sql += "WHERE t1.estadoMail IS NULL ";
		sql += "AND t1.fechaUltimoAbandono > ? ";

		Objeto datos = Sql.select(contexto, SqlEsales.SQL, sql, Fecha.ahora().restarHoras(2));
		return map(datos, AlertaPushUsuariosEsales.class, AlertaPushUsuarioEsales.class);
	}

	public static Boolean actualizarEstadoAlerta(Contexto contexto, String id, String estado, String tipoEstado) {
		String sql = "";
		sql += "UPDATE [dbo].[Alerta_Push_Usuarios] ";

		if (GeneralBB.TIPO_ALERTA_PUSH.equals(tipoEstado)) {
			sql += "SET estado = ?, fechaUltimoEnvio = GETDATE() ";
		}

		if (GeneralBB.TIPO_ALERTA_MAIL.equals(tipoEstado)) {
			sql += "SET estadoMail = ?, fechaUltimoEnvio = GETDATE() ";
		}

		sql += "WHERE id = ? ";

		return Sql.update(contexto, SqlEsales.SQL, sql, estado, id) == 1;
	}

	public static AlertaPushUsuarioEsales existeAlertaPushUsuarioByToken(Contexto contexto, String codigoAlerta, String tokenFirebase) {
		String sql = "";
		sql += "SELECT * FROM [dbo].[Alerta_Push_Usuarios] WITH (NOLOCK) ";
		sql += "WHERE codigoAlerta = ? ";
		sql += "AND tokenFirebase = ? ";

		Objeto datos = Sql.select(contexto, SqlEsales.SQL, sql, codigoAlerta, tokenFirebase);
		return map(datos, AlertaPushUsuariosEsales.class, AlertaPushUsuarioEsales.class).first();
	}

	public static AlertaPushUsuarioEsales existeAlertaPushUsuarioByMail(Contexto contexto, String codigoAlerta, String mail) {
		String sql = "";
		sql += "SELECT * FROM [dbo].[Alerta_Push_Usuarios] WITH (NOLOCK) ";
		sql += "WHERE codigoAlerta = ? ";
		sql += "AND mail = ? ";

		Objeto datos = Sql.select(contexto, SqlEsales.SQL, sql, codigoAlerta, mail);
		return map(datos, AlertaPushUsuariosEsales.class, AlertaPushUsuarioEsales.class).first();
	}

	public static AlertaPushUsuarioEsales obtenerUltimoToken(Contexto contexto, String cuil) {
		String sql = "";
		sql += "SELECT TOP 1 token_firebase AS tokenFirebase, mail, plataforma ";
		sql += "FROM [dbo].[bb_historico_push_sesion] WITH (NOLOCK) ";
		sql += "WHERE token_firebase IS NOT NULL ";
		sql += "AND LEN(token_firebase) > 100 ";
		sql += "AND cuil = ? ";
		sql += "ORDER BY id DESC ";

		Objeto datos = Sql.select(contexto, SqlBuhoBank.SQL, sql, cuil);
		return map(datos, AlertaPushUsuariosEsales.class, AlertaPushUsuarioEsales.class).first();
	}

	public static AlertaPushUsuarioEsales obtenerUltimoMail(Contexto contexto, String cuil) {
		String sql = "";
		sql += "SELECT TOP 1 token_firebase AS tokenFirebase, mail, plataforma ";
		sql += "FROM [dbo].[bb_historico_push_sesion] WITH (NOLOCK) ";
		sql += "WHERE mail IS NOT NULL ";
		sql += "AND cuil = ? ";
		sql += "ORDER BY id DESC ";

		Objeto datos = Sql.select(contexto, SqlBuhoBank.SQL, sql, cuil);
		return map(datos, AlertaPushUsuariosEsales.class, AlertaPushUsuarioEsales.class).first();
	}

	public static AlertaPushUsuarioEsales obtenerUltimaPlataforma(Contexto contexto, String cuil) {
		String sql = "";
		sql += "SELECT TOP 1 token_firebase AS tokenFirebase, mail, plataforma ";
		sql += "FROM [dbo].[bb_historico_push_sesion] WITH (NOLOCK) ";
		sql += "WHERE plataforma IS NOT NULL ";
		sql += "AND cuil = ? ";
		sql += "ORDER BY id DESC ";

		Objeto datos = Sql.select(contexto, SqlBuhoBank.SQL, sql, cuil);
		return map(datos, AlertaPushUsuariosEsales.class, AlertaPushUsuarioEsales.class).first();
	}

}
