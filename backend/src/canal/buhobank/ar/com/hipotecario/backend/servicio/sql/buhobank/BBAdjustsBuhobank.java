package ar.com.hipotecario.backend.servicio.sql.buhobank;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Util;
import ar.com.hipotecario.backend.conector.sql.Sql;
import ar.com.hipotecario.backend.conector.sql.SqlObjeto;
import ar.com.hipotecario.backend.conector.sql.SqlObjetos;
import ar.com.hipotecario.backend.servicio.sql.SqlBuhoBank;
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBAdjustsBuhobank.BBAdjustBuhobank;

@SuppressWarnings("serial")
public class BBAdjustsBuhobank extends SqlObjetos<BBAdjustBuhobank> {

	/* ========== ATRIBUTOS ========== */
	public static class BBAdjustBuhobank extends SqlObjeto {

		public Integer id;
		public String gps_adid;
		public String idfa;
		public String idfv;
		public String adid;
		public String tracker_token;
		public String tracker_name;
		public String app_name;
		public String activity_kind;
		public Fecha created_at;
		public String event_token;
		public String event_name;
	}

	private static Object[] obtenerParametros(Contexto contexto, BBAdjustBuhobank adjust, int cantidad) {

		Object[] parametros = new Object[cantidad];

		parametros[0] = !Util.empty(adjust.gps_adid) ? adjust.gps_adid : "";
		parametros[1] = !Util.empty(adjust.idfa) ? adjust.idfa : "";
		parametros[2] = !Util.empty(adjust.idfv) ? adjust.idfv : "";
		parametros[3] = !Util.empty(adjust.adid) ? adjust.adid : "";
		parametros[4] = !Util.empty(adjust.tracker_token) ? adjust.tracker_token : "";
		parametros[5] = !Util.empty(adjust.tracker_name) ? adjust.tracker_name : "";
		parametros[6] = !Util.empty(adjust.app_name) ? adjust.app_name : "";
		parametros[7] = !Util.empty(adjust.activity_kind) ? adjust.activity_kind : "";
		parametros[8] = !Util.empty(adjust.created_at) ? adjust.created_at : "";
		parametros[9] = !Util.empty(adjust.event_token) ? adjust.event_token : "";
		parametros[10] = !Util.empty(adjust.event_name) ? adjust.event_name : "";

		return parametros;
	}

	public static Boolean post(Contexto contexto, BBAdjustBuhobank adjust) {

		String sql = "";
		sql += "INSERT INTO [dbo].[adjust] ";
		sql += "([gps_adid], [idfa], [idfv], [adid], [tracker_token], [tracker_name], [app_name], [activity_kind], [created_at], [event_token], [event_name])";
		sql += "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		Object[] parametros = obtenerParametros(contexto, adjust, 11);

		return Sql.update(contexto, SqlBuhoBank.SQL, sql, parametros) > 0;
	}

}
