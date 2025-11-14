package ar.com.hipotecario.backend.servicio.sql.esales;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.sql.Sql;
import ar.com.hipotecario.backend.conector.sql.SqlObjeto;
import ar.com.hipotecario.backend.conector.sql.SqlObjetos;
import ar.com.hipotecario.backend.servicio.api.mobile.SoftToken;
import ar.com.hipotecario.backend.servicio.sql.SqlEsales;
import ar.com.hipotecario.backend.servicio.sql.esales.BBPersonasAlta.BBPersonaAlta;
import ar.com.hipotecario.canal.buhobank.SesionBB;

@SuppressWarnings("serial")
public class BBPersonasAlta extends SqlObjetos<BBPersonaAlta> {

	/* ========== ATRIBUTOS SoftToken ========== */
	public static class BBPersonaAlta extends SqlObjeto {
		public String id;
		public String cuil;
		public String estado;
		public String algoritmo;
		public String digitos;
		public String periodo;
		public String claveSecreta;
		public String urlClaveSecreta;
		public String username;
		public String id_dispositivo;
		public String sesion_id;
		public Fecha fecha_ultima_modificacion;
	}

	/* ========== SERVICIO ========== */
	public static Object[] obtenerParametros(Contexto contexto, String esalesId, SesionBB sesion, SoftToken sToken, Integer cantidad) {

		Object[] parametros = new Object[cantidad];

		parametros[0] = sToken.cuil;
		parametros[1] = sToken.estado;
		parametros[2] = sToken.algoritmo;
		parametros[3] = sToken.digitos;
		parametros[4] = sToken.periodo;
		parametros[5] = sToken.claveSecreta;
		parametros[6] = sToken.urlClaveSecreta;
		parametros[7] = sToken.username;
		parametros[8] = sToken.id_dispositivo;
		parametros[9] = esalesId;
		parametros[10] = Fecha.ahora();

		return parametros;
	}

	public static Boolean crearPersonasAlta(Contexto contexto, String esalesId, SesionBB sesion, SoftToken sToken) {
		String sql = "";
		sql += "INSERT INTO [esales].[dbo].[BB_PersonasAlta] ";
		sql += "([cuil], [estado], [algoritmo], [digitos], ";
		sql += "[periodo], [claveSecreta], [urlClaveSecreta], ";
		sql += "[username],[id_dispositivo], [sesion_id], [fecha_ultima_modificacion])";
		sql += "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		Object[] parametros = obtenerParametros(contexto, esalesId, sesion, sToken, 11);

		return Sql.update(contexto, SqlEsales.SQL, sql, parametros) == 1;
	}

	public static BBPersonaAlta obtenerSemilla(Contexto contexto, String cuit) {
		try{
			String sql = "";
			sql += "SELECT * ";
			sql += "FROM [esales].[dbo].[BB_PersonasAlta] WITH (NOLOCK) ";
			sql += "WHERE cuil = ? ";

			Objeto datos = Sql.select(contexto, SqlEsales.SQL, sql, cuit);
			return map(datos, BBPersonasAlta.class, BBPersonaAlta.class).first();
		}
		catch (Exception e){}
		return null;
	}

}
