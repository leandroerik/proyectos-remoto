package ar.com.hipotecario.backend.servicio.sql.telemarketing;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.sql.Sql;
import ar.com.hipotecario.backend.conector.sql.SqlObjeto;
import ar.com.hipotecario.backend.conector.sql.SqlObjetos;
import ar.com.hipotecario.backend.servicio.sql.SqlCarritoAbandonado;
import ar.com.hipotecario.backend.servicio.sql.esales.ProspectsEsales.ProspectEsales;
import ar.com.hipotecario.backend.servicio.sql.telemarketing.CarritoAbandonadosTelemarketing.CarritoAbandonadoTelemarketing;

@SuppressWarnings("serial")
public class CarritoAbandonadosTelemarketing extends SqlObjetos<CarritoAbandonadoTelemarketing> {
	/* ========== ATRIBUTOS ========== */
	public static class CarritoAbandonadoTelemarketing extends SqlObjeto {
		public Integer record_id;
		public String contact_info;
		public Integer contact_info_type;
		public Integer record_type;
		public Integer record_status;
		public Integer call_result;
		public Integer attempt;
		public Integer dial_sched_time;
		public Integer call_time;
		public Integer daily_from;
		public Integer daily_till;
		public Integer tz_dbid;
		public Integer campaign_id;
		public String agent_id;
		public Integer chain_id;
		public Integer chain_n;
		public Integer group_id;
		public Integer app_id;
		public String treatments;
		public Integer media_ref;
		public String email_subject;
		public Integer email_template_id;
		public Integer switch_id;
		public Integer caso;
		public String tipo_doc;
		public String nro_doc;
		public String cuil;
		public String nombre_apellido;
		public String campania;
		public String lista_campania;
		public String lista_discador;
		public Integer resultado_gestion;
	}

	public static CarritoAbandonadoTelemarketing getByCuil(Contexto contexto, String cuil, String listaDiscador) {
		String sql = "";
		sql += "SELECT * ";
		sql += "FROM [dbo].[CarritoAbandonado] WITH (NOLOCK) ";
		sql += "WHERE lista_discador = ? ";
		sql += "AND cuil = ? ";

		Objeto datos = Sql.select(contexto, SqlCarritoAbandonado.SQL, sql, listaDiscador, cuil);
		if (datos.isEmpty())
			return new CarritoAbandonadoTelemarketing();
		return map(datos, CarritoAbandonadosTelemarketing.class, CarritoAbandonadoTelemarketing.class).first();
	}

	public static CarritoAbandonadosTelemarketing getLlamados(Contexto contexto) {
		String sql = "";
		sql += "SELECT * ";
		sql += "FROM [dbo].[CarritoAbandonado] WITH (NOLOCK) ";
		sql += "WHERE call_result = 33 ";

		Objeto datos = Sql.select(contexto, SqlCarritoAbandonado.SQL, sql);
		return map(datos, CarritoAbandonadosTelemarketing.class, CarritoAbandonadoTelemarketing.class);
	}

	public static Boolean cargarCarritoAbandonado(Contexto contexto, ProspectEsales prospect) {
		String sql = "EXEC [gesys_OCSTBHSA].[dbo].[BuhoBank_CargarLlamadaCarritoAbandonado] ";
		sql += "?, ?, NULL, ?, ?, ?";

		return Sql.update(contexto, SqlCarritoAbandonado.SQL, sql, prospect.CUIL, prospect.TelefonoCelular, prospect.DNI, prospect.getFullName(), prospect.ListaDiscador) > 0;
	}

	public static Boolean refresh(Contexto contexto) {
		String sql = "DELETE FROM [dbo].[CarritoAbandonado] ";

		return Sql.update(contexto, SqlCarritoAbandonado.SQL, sql) > 0;
	}

}