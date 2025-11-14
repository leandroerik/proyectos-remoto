package ar.com.hipotecario.backend.servicio.sql.campniaswf;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.sql.Sql;
import ar.com.hipotecario.backend.conector.sql.SqlObjeto;
import ar.com.hipotecario.backend.conector.sql.SqlObjetos;
import ar.com.hipotecario.backend.servicio.sql.SqlCampaniasWF;
import ar.com.hipotecario.backend.servicio.sql.campniaswf.SolicitudesWorkflow.SolicitudWorkflow;

@SuppressWarnings("serial")
public class SolicitudesWorkflow extends SqlObjetos<SolicitudWorkflow> {

	/* ========== ATRIBUTOS ========== */
	public static class SolicitudWorkflow extends SqlObjeto {
		public String cuil;
		public String apellido;
		public String nombre;
		public Fecha fechaSolicitud;
		public Fecha fechaAprobacion;
		public Boolean solicitudFinalizada;
		public String productos;

	}

	public static Boolean tieneSolicitudAprobada(Contexto contexto, String cuil, Fecha fechaDesde) {
		String sql = "";
		sql += "SELECT * FROM (";
		sql += "SELECT b.INT_NumeroTrib as cuil, ";
		sql += "b.INT_Apellido as apellido, ";
		sql += "b.INT_Nombres as nombre, ";
		sql += "a.Sol_id as solicitud, ";
		sql += "cast(a.sol_fecha as date) as fechaSolicitud, ";
		sql += "cast(a.sol_FechaAprobacion as date) as fechaAprobacion, ";
		sql += "a.Sol_finalizada as solicitudFinalizada, ";
		sql += "d.pfe_descripcion as productos ";
		sql += "FROM Solicitud as a ";
		sql += "INNER JOIN Integrante as b ";
		sql += "ON a.sol_id = b.int_sol_id ";
		sql += "AND b.int_secuencia = 0 ";
		sql += "INNER JOIN Solicitudproducto as c ";
		sql += "ON a.Sol_id = c.spr_sol_id ";
		sql += "LEFT JOIN ProductoFrontEnd as d ";
		sql += "ON c.spr_pfe_id = d.pfe_id) AS w ";
		sql += "WHERE (";
		sql += "w.productos = 'TARJETA DE CRÉDITO VISA' ";
		sql += "OR w.productos = 'CAJA DE AHORROS')";
		sql += "AND w.solicitudFinalizada = 1 ";
		sql += "AND w.fechaSolicitud >= ? ";
		sql += "AND w.cuil = ? ";

		Objeto datos = Sql.select(contexto, SqlCampaniasWF.SQL, sql, fechaDesde, cuil);

		return map(datos, SolicitudesWorkflow.class, SolicitudWorkflow.class).size() > 0;
	}

	public static Boolean cancelarSolicitudes(Contexto contexto, String cuil) {

		String sql = "";
		sql += "UPDATE s SET ";
		sql += "SOL_EST_Id = 'C' ";
		sql += "FROM Solicitud S (nolock), Integrante I (nolock) ";
		sql += "WHERE  I.INT_SOL_Id = S.SOL_Id ";
		sql += "AND INT_NumeroTrib = ? ";

		return Sql.update(contexto, SqlCampaniasWF.SQL, sql, cuil) > 0;
	}

}