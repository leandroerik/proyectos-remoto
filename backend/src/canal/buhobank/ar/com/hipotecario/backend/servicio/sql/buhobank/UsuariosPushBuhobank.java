package ar.com.hipotecario.backend.servicio.sql.buhobank;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.sql.Sql;
import ar.com.hipotecario.backend.conector.sql.SqlObjeto;
import ar.com.hipotecario.backend.conector.sql.SqlObjetos;
import ar.com.hipotecario.backend.servicio.sql.SqlBuhoBank;
import ar.com.hipotecario.backend.servicio.sql.buhobank.UsuariosPushBuhobank.UsuarioPushBuhobank;
import ar.com.hipotecario.backend.servicio.sql.esales.AlertasPushEsales.AlertaPushEsales;
import ar.com.hipotecario.canal.buhobank.GeneralBB;

@SuppressWarnings("serial")
public class UsuariosPushBuhobank extends SqlObjetos<UsuarioPushBuhobank> {

	/* ========== ATRIBUTOS ========== */
	public static class UsuarioPushBuhobank extends SqlObjeto {
		public Fecha momento;
		public String cuil;
		public String tokenFirebase;
		public String mail;
		public String plataforma;
	}

	public static UsuariosPushBuhobank obtenerAbandonosSesion(Contexto contexto, AlertaPushEsales notificacion) {
		Fecha fechaActual = Fecha.ahora();
		Fecha fechaPushHasta = fechaActual.restarMinutos(notificacion.minutosDesdeAbandono);
		Fecha fechaPushDesde = fechaPushHasta.restarMinutos(GeneralBB.MINUTO_PROCESO_ALERTA_PUSH);

		String sql = "";
		sql += "SELECT cuil, ";
		sql += "token_firebase AS tokenFirebase, ";
		sql += "fecha_ultima_modificacion AS momento, ";
		sql += "mail, plataforma FROM (";
		sql += "SELECT * FROM ( ";
		sql += "SELECT ROW_NUMBER() OVER(PARTITION BY cuil ORDER BY fecha_ultima_modificacion DESC) AS r, * ";
		sql += "FROM [dbo].[bb_historico_push_sesion] WITH (NOLOCK) ";
		sql += "WHERE fecha_ultima_modificacion > ? ";
		sql += ") AS r ";
		sql += "WHERE r.r = 1 ";

		if (notificacion.codigoAlerta.equals(GeneralBB.COD_ALERTA_POST_OFERTA_INICIA) || notificacion.codigoAlerta.equals(GeneralBB.COD_ALERTA_POST_OFERTA_INICIA_MAIL)) {
			sql += "AND resolucion_scoring != 'AV' ";
			sql += "AND resolucion_scoring IS NOT NULL ";
		}

		if (notificacion.codigoAlerta.equals(GeneralBB.COD_ALERTA_POST_OFERTA_CREDITICIA) || notificacion.codigoAlerta.equals(GeneralBB.COD_ALERTA_POST_OFERTA_CREDITICIA_MAIL)) {
			sql += "AND resolucion_scoring = 'AV' ";
		}
		
		if(notificacion.codigoAlerta.equals(GeneralBB.COD_ALERTA_PRIMER_FLUJO_VU)) {
			sql += "AND estado = 'TYC_ACEPTADO' ";
		}

		sql += ") AS t1 ";
		sql += "WHERE fecha_ultima_modificacion < ? ";

		Objeto datos = Sql.select(contexto, SqlBuhoBank.SQL, sql, fechaPushDesde, fechaPushHasta);
		return map(datos, UsuariosPushBuhobank.class, UsuarioPushBuhobank.class);
	}

	public static UsuariosPushBuhobank obtenerAbandonosOnboarding(Contexto contexto, AlertaPushEsales notificacion) {
		Fecha fechaActual = Fecha.ahora();
		Fecha fechaPushHasta = fechaActual.restarMinutos(notificacion.minutosDesdeAbandono);
		Fecha fechaPushDesde = fechaPushHasta.restarMinutos(GeneralBB.MINUTO_PROCESO_ALERTA_PUSH);

		String sql = "";
		sql += "SELECT t1.datos AS tokenFirebase, t1.momento FROM ( ";
		sql += "SELECT r.datos, r.momento FROM ( ";
		sql += "SELECT ROW_NUMBER() OVER(PARTITION BY datos ORDER BY momento DESC) AS r, ";
		sql += "* FROM [dbo].[bb_historico_push_log] WITH (NOLOCK) ";
		sql += "WHERE momento > ? ";
		sql += "AND evento LIKE '%launch_bank_app' ";
		sql += ") AS r WHERE r.r = 1) AS t1 ";
		sql += "LEFT JOIN ( ";
		sql += "SELECT datos FROM [dbo].[bb_historico_push_log] WITH (NOLOCK) ";
		sql += "WHERE momento > ? ";
		sql += "AND ( ";
		sql += "evento LIKE '%is_client' ";
		sql += "OR evento LIKE '%start_%' ";
		sql += ") ";
		sql += "GROUP BY datos) AS t2 ";
		sql += "ON t1.datos = t2.datos ";
		sql += "WHERE t2.datos IS NULL ";

		Objeto datos = Sql.select(contexto, SqlBuhoBank.SQL, sql, fechaPushDesde, fechaPushDesde);
		return map(datos, UsuariosPushBuhobank.class, UsuarioPushBuhobank.class);
	}
}
