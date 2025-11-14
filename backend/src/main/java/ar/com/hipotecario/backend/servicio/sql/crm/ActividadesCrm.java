package ar.com.hipotecario.backend.servicio.sql.crm;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.sql.Sql;
import ar.com.hipotecario.backend.conector.sql.SqlObjeto;
import ar.com.hipotecario.backend.conector.sql.SqlObjetos;
import ar.com.hipotecario.backend.servicio.sql.SqlCrm;
import ar.com.hipotecario.backend.servicio.sql.crm.ActividadesCrm.ActividadCrm;

@SuppressWarnings("serial")
public class ActividadesCrm extends SqlObjetos<ActividadCrm> {

	/* ========== ATRIBUTOS ========== */
	public static class ActividadCrm extends SqlObjeto {
		public Fecha fecha;
		public String mcs_subcanalname;
		public String mcs_gestionname;
		public String usuario_Nombre;
		public String usuario;
		public String mcs_contactoname;
		public String mcs_resultadoname;
		public String actividad;
		public String mcs_clientename;
		public String mcs_CodigoTributario;
		public String producto;
	}

	public static Boolean fueLlamadoCrmPaquetes(Contexto contexto, String cuil, Fecha fechaDesde) {
		String sql = "";
		sql += "SELECT ";
		sql += "a.createdon AS fecha, ";
		sql += "a.mcs_subcanalname, ";
		sql += "a.mcs_gestionname, ";
		sql += "a.createdbyname AS usuario_Nombre, ";
		sql += "SUBSTRING ( CAST ( G.DOMAINNAME AS CHAR(6) ) ,1 , 6 ) AS usuario, ";
		sql += "a.mcs_contactoname, ";
		sql += "a.mcs_resultadoname, ";
		sql += "a.mcs_numeracion AS actividad, ";
		sql += "a.mcs_clientename, ";
		sql += "b.mcs_CodigoTributario, ";
		sql += "a.mcs_productoname AS producto ";
		sql += "FROM Filteredmcs_contactcenter a WITH (NOLOCK) ";
		sql += "INNER JOIN FilteredAccount b WITH (NOLOCK) ";
		sql += "ON a.mcs_cliente = b.accountid ";
		sql += "INNER JOIN FILTEREDSYSTEMUSER G WITH (NOLOCK) ";
		sql += "ON a.CREATEDBYNAME= G.FULLNAME ";
		sql += "WHERE a.activitytypecodename= 'Contact Center' ";
		sql += "AND G.isdisabled = '0' ";

		sql += "AND ((a.mcs_productoname LIKE 'PAQUETE%' AND ( ";
		sql += "a.mcs_resultadoname NOT LIKE '%ARGUMENTADO/LO PIENSA%' ";
		sql += "AND a.mcs_resultadoname NOT LIKE '%NO TIENE LOS DATOS EN ESTE MOMENTO%' ";
		sql += "AND a.mcs_resultadoname NOT LIKE '%SIN SISTEMA/NO FUNCIONA WF%')) ";
		sql += "OR (a.mcs_productoname LIKE 'UPGRADE PAQUETE%' AND ( ";
		sql += "a.mcs_resultadoname NOT LIKE '%ARGUMENTADO/LO PIENSA%' ";
		sql += "AND a.mcs_resultadoname NOT LIKE '%NO TIENE LOS DATOS EN ESTE MOMENTO%' ";
		sql += "AND a.mcs_resultadoname NOT LIKE '%SIN SISTEMA/NO FUNCIONA WF%' ";
		sql += "AND a.mcs_resultadoname NOT LIKE '%NO ES EL TITULAR DE LA TC%'))) ";

		sql += "AND b.mcs_CodigoTributario = ? ";
		sql += "AND a.createdon >= ? ";

		Objeto datos = Sql.select(contexto, SqlCrm.SQL, sql, cuil, fechaDesde);

		return map(datos, ActividadesCrm.class, ActividadCrm.class).size() > 0;
	}

	public static Boolean fueLlamadoCrmPP(Contexto contexto, String cuil, Fecha fechaDesde) {
		String sql = "";
		sql += "SELECT ";
		sql += "a.createdon AS fecha, ";
		sql += "a.mcs_subcanalname, ";
		sql += "a.mcs_gestionname, ";
		sql += "a.createdbyname AS usuario_Nombre, ";
		sql += "SUBSTRING ( CAST ( G.DOMAINNAME AS CHAR(6) ) ,1 , 6 ) AS usuario, ";
		sql += "a.mcs_contactoname, ";
		sql += "a.mcs_resultadoname, ";
		sql += "a.mcs_numeracion AS actividad, ";
		sql += "a.mcs_clientename, ";
		sql += "b.mcs_CodigoTributario ";
		sql += "a.mcs_productoname AS producto ";
		sql += "FROM Filteredmcs_contactcenter a WITH (NOLOCK) ";
		sql += "INNER JOIN FilteredAccount b WITH (NOLOCK) ";
		sql += "ON a.mcs_cliente = b.accountid ";
		sql += "INNER JOIN FILTEREDSYSTEMUSER G WITH (NOLOCK) ";
		sql += "ON a.CREATEDBYNAME= G.FULLNAME ";
		sql += "WHERE a.activitytypecodename= 'Contact Center' ";
		sql += "AND G.isdisabled = '0' ";

		sql += "AND (a.mcs_productoname LIKE 'PRESTAMO PERSONAL%' AND ( ";
		sql += "a.mcs_resultadoname NOT LIKE '%ARGUMENTADO/LO PIENSA%' ";
		sql += "AND a.mcs_resultadoname NOT LIKE '%NO TIENE LOS DATOS EN ESTE MOMENTO%' ";
		sql += "AND a.mcs_resultadoname NOT LIKE '%SOLO ASESORAMIENTO%' ";
		sql += "AND a.mcs_resultadoname NOT LIKE '%NO OFRECIDO%')) ";

		sql += "AND b.mcs_CodigoTributario = ? ";
		sql += "AND a.createdon >= ? ";

		Objeto datos = Sql.select(contexto, SqlCrm.SQL, sql, cuil, fechaDesde);

		return map(datos, ActividadesCrm.class, ActividadCrm.class).size() > 0;
	}

}