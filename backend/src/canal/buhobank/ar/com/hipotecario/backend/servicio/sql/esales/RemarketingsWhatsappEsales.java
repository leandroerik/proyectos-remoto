package ar.com.hipotecario.backend.servicio.sql.esales;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.sql.Sql;
import ar.com.hipotecario.backend.conector.sql.SqlObjeto;
import ar.com.hipotecario.backend.conector.sql.SqlObjetos;
import ar.com.hipotecario.backend.servicio.sql.SqlEsales;
import ar.com.hipotecario.backend.servicio.sql.esales.RemarketingsWhatsappEsales.RemarketingWhatsappWorkEsales;

@SuppressWarnings("serial")
public class RemarketingsWhatsappEsales extends SqlObjetos<RemarketingWhatsappWorkEsales> {

	public static class RemarketingWhatsappWorkEsales extends SqlObjeto {

		public String cuil;
		public String telefono;
		public String fecha_inicio;
	}

	public static RemarketingsWhatsappEsales get(Contexto contexto) {

		String sql = "SELECT telefono, fecha fecha_inicio, cuil ";
		sql += "FROM [esales].[dbo].[remarketing_whatsapp_2] WITH (NOLOCK) ";

		Objeto datos = Sql.select(contexto, SqlEsales.SQL, sql);
		return map(datos, RemarketingsWhatsappEsales.class, RemarketingWhatsappWorkEsales.class);
	}

	public static Boolean ejecutarHistorico(Contexto contexto, Fecha fechaDesde, String estados, String eventos) {

		String sql = "EXEC [esales].[dbo].[SP_actualizar_historico_remarketing_whatsapp] ";
		sql += "?, ?, ? ";

		return Sql.update(contexto, SqlEsales.SQL, sql, fechaDesde, estados, eventos) > 0;
	}
}
