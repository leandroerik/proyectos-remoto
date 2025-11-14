package ar.com.hipotecario.backend.servicio.sql.esales;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.sql.Sql;
import ar.com.hipotecario.backend.conector.sql.SqlObjeto;
import ar.com.hipotecario.backend.conector.sql.SqlObjetos;
import ar.com.hipotecario.backend.servicio.sql.SqlEsales;
import ar.com.hipotecario.backend.servicio.sql.esales.AlertasPushEsales.AlertaPushEsales;

@SuppressWarnings("serial")
public class AlertasPushEsales extends SqlObjetos<AlertaPushEsales> {

	public static class AlertaPushEsales extends SqlObjeto {
		public String codigoAlerta;
		public String titulo;
		public String texto;
		public String url;
		public Integer minutosDesdeAbandono;
		public Boolean pushHabilitado;
		public String asuntoMail;
		public String plantillaMail;
		public Boolean mailHabilitado;
	}

	public static AlertasPushEsales obtenerAlertaPush(Contexto contexto) {
		String sql = "";
		sql += "SELECT * ";
		sql += "FROM [dbo].[Alerta_Push] WITH (NOLOCK) ";
		sql += "WHERE (LEN(titulo) > 3 AND pushHabilitado = 1) ";
		sql += "OR (LEN(plantillaMail) > 3 AND mailHabilitado = 1) ";

		Objeto datos = Sql.select(contexto, SqlEsales.SQL, sql);
		return map(datos, AlertasPushEsales.class, AlertaPushEsales.class);
	}

	public AlertaPushEsales buscarCodigo(String codigo) {

		for (AlertaPushEsales alerta : this) {
			if (alerta.codigoAlerta.equals(codigo)) {
				return alerta;
			}
		}
		return null;
	}

	public static Boolean guardarNuevaAlertaPush(Contexto contexto, String codigoAlerta, String tokenFirebase, String mail, String cuil, Fecha fechaUltimoAbandono, String estado, String estadoMail, String plataforma) {
		String sql = "";
		sql += "INSERT INTO [dbo].[Alerta_Push_Usuarios] ";
		sql += "(codigoAlerta, tokenFirebase, mail, cuil, fechaUltimoAbandono, estado, estadoMail, plataforma)";
		sql += "VALUES (?, ?, ?, ?, ?, ?, ?, ?) ";

		return Sql.update(contexto, SqlEsales.SQL, sql, codigoAlerta, tokenFirebase, mail, cuil, fechaUltimoAbandono, estado, estadoMail, plataforma) == 1;
	}

}
