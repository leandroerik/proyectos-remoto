package ar.com.hipotecario.backend.servicio.sql.esales;

import java.math.BigDecimal;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.sql.Sql;
import ar.com.hipotecario.backend.conector.sql.SqlObjeto;
import ar.com.hipotecario.backend.conector.sql.SqlObjetos;
import ar.com.hipotecario.backend.servicio.sql.SqlEsales;
import ar.com.hipotecario.backend.servicio.sql.esales.ProspectsEsales.ProspectEsales;
import ar.com.hipotecario.backend.servicio.sql.esales.SesionesEsales.SesionEsales;

@SuppressWarnings("serial")
public class ProspectsEsales extends SqlObjetos<ProspectEsales> {

	public static String listaDiscadorPaquete = "CarritoPaquete";
	public static String listaDiscadorPP = "CarritoPP";
	public static String listaDiscadorIndicador = "CarritoIndicador";

	/* ========== ATRIBUTOS ========== */
	public static class ProspectEsales extends SqlObjeto {
		public Integer CodProspect;
		public Integer CodLanding;
		public Fecha FechaRegistro;
		public String Nombre;
		public String Apellido;
		public String DNI;
		public Integer EjemplarDNI;
		public String CUIL;
		public String cuil_tipo;
		public String Sexo;
		public String EMail;
		public String CodPostal;
		public String TelefonoCelular;
		public String TelefonoAlternativo;
		public Boolean GuardadoEnGenesys;
		public Boolean EsDemorado;
		public Boolean EsCliente;
		public String ListaDiscador;
		public String UserAgentNombre;
		public String UserAgentVersion;
		public String OSNombre;
		public String OSVersion;
		public String DeviceNombre;
		public String ClientIP;
		public String HostIP;
		public BigDecimal Latitud;
		public BigDecimal Longitud;

		public String getFullName() {
			return Nombre + " " + Apellido;
		}

		public Boolean esListaDiscadorPaquete() {
			return listaDiscadorPaquete.equals(ListaDiscador);
		}

		public Boolean esListaDiscadorIndicador() {
			return listaDiscadorIndicador.equals(ListaDiscador);
		}
	}

	public static ProspectsEsales getProspectByCuil(Contexto contexto, String cuil, String listaDiscador) {
		String sql = "";
		sql += "SELECT * ";
		sql += "FROM Leads_Prospect WITH (NOLOCK) ";
		sql += "WHERE ListaDiscador = ? ";
		sql += "AND CUIL = ? ";
		Objeto datos = Sql.select(contexto, SqlEsales.SQL, sql, listaDiscador, cuil);
		return map(datos, ProspectsEsales.class, ProspectEsales.class);
	}

	public static Object[] obtenerParametrosProspect(Contexto contexto, SesionEsales sesion, String listaDiscador, Integer cantidad) {
		Object[] parametros = new Object[cantidad];

		// [Nombre], [Apellido], [DNI], [CUIL], [Sexo]
		parametros[0] = sesion.nombre;
		parametros[1] = sesion.apellido;
		parametros[2] = sesion.documento_numero;
		parametros[3] = sesion.cuil;
		parametros[4] = sesion.sexo;

		// [EMail], [CodPostal], [TelefonoCelular], [ListaDiscador]
		parametros[5] = sesion.mail;
		parametros[6] = sesion.domicilio_cp;

		String celular_ddn = sesion.telefono_celular_ddn.equals("011") ? "" : "0";
		celular_ddn += sesion.telefono_celular_ddn.equals("011") ? "015" : sesion.telefono_celular_ddn + "15";
		parametros[7] = celular_ddn + sesion.telefono_celular_caract + sesion.telefono_celular_nro;

		parametros[8] = listaDiscador;

		return parametros;
	}

	public static Boolean guardarProspects(Contexto contexto, String listaDiscador, Fecha fechaDesde) {

		String sql = "";
		sql += "INSERT INTO Leads_Prospect ";
		sql += "(Nombre, Apellido, DNI, CUIL, ";
		sql += "Sexo, EMail, CodPostal, TelefonoCelular, ";
		sql += "ListaDiscador, FechaRegistro, GuardadoEnGenesys, EsDemorado) ";
		sql += "SELECT IIF(t1.nombre IS NULL, 'SIN_NOMBR', t1.nombre) AS Nombre, " + "IIF(t1.apellido IS NULL, 'SIN_APELL', t1.apellido) AS Apellido, " + "t1.documento_numero, t1.cuil, ";
		sql += "IIF(t1.sexo IS NULL, 'SIN_SEXO', t1.sexo) AS Sexo, " + "t1.mail, t1.domicilio_cp, ";
		sql += "CONCAT(IIF(t1.telefono_celular_ddn = '011', '', '0'), IIF(t1.telefono_celular_ddn = '011', '015', CONCAT(t1.telefono_celular_ddn, '15')), t1.telefono_celular_caract, t1.telefono_celular_nro) AS TelefonoCelular, ";
		sql += "? AS ListaDiscador,  GETDATE() AS FechaRegistro, 0 AS GuardadoEnGenesys, 0 AS EsDemorado FROM (";
		sql += "SELECT T_SESIONES.* FROM (SELECT * FROM ( ";
		sql += "SELECT *, ROW_NUMBER() OVER (PARTITION BY cuil ORDER BY fecha_inicio DESC) AS rn ";
		sql += "FROM (SELECT * FROM Sesion WITH (NOLOCK) ";
		sql += "WHERE telefono_celular_nro != 'NULL' ";
		sql += "AND estado != 'ENVIAR_OTP_MAIL_OK' AND estado != 'VALIDAR_OTP_MAIL_OK' ";
		sql += "AND estado != 'ENVIAR_OTP_SMS_OK' AND estado != 'VALIDAR_OTP_SMS_OK' ";
		sql += "AND estado != 'ERROR_ENVIAR_OTP_MAIL' AND estado != 'ERROR_VALIDAR_OTP_MAIL' ";
		sql += "AND estado != 'ERROR_ENVIAR_OTP_SMS' AND estado != 'ERROR_VALIDAR_OTP_SMS' ";
		sql += "AND resolucion_scoring = 'AV' ";
		sql += "AND estado != 'FINALIZAR_OK' AND estado != 'BATCH_CORRIENDO' ";
		sql += "AND fecha_inicio >= ? ";
		sql += ") AS T_SESION ";
		sql += ") T_SESION WHERE T_SESION.rn = 1 ";
		sql += ") AS T_SESIONES ";
		sql += "LEFT JOIN (";
		sql += "SELECT cuil, count(*) AS finalizado ";
		sql += "FROM Sesion WITH (NOLOCK) ";
		sql += "WHERE telefono_celular_nro != 'NULL' ";
		sql += "AND estado != 'ENVIAR_OTP_MAIL_OK' AND estado != 'VALIDAR_OTP_MAIL_OK' ";
		sql += "AND estado != 'ENVIAR_OTP_SMS_OK' AND estado != 'VALIDAR_OTP_SMS_OK' ";
		sql += "AND estado != 'ERROR_ENVIAR_OTP_MAIL' AND estado != 'ERROR_VALIDAR_OTP_MAIL' ";
		sql += "AND estado != 'ERROR_ENVIAR_OTP_SMS' AND estado != 'ERROR_VALIDAR_OTP_SMS' ";
		sql += "AND resolucion_scoring = 'AV' ";
		sql += "AND (estado = 'FINALIZAR_OK' OR ESTADO = 'BATCH_CORRIENDO') ";
		sql += "AND fecha_inicio >= ? ";
		sql += "GROUP BY cuil ";
		sql += ") AS T_FINALIZADOS ";
		sql += "ON T_SESIONES.cuil = T_FINALIZADOS.cuil ";
		sql += "WHERE finalizado IS NULL) AS t1 ";
		sql += "LEFT JOIN (SELECT * FROM Leads_Prospect WITH (NOLOCK) WHERE ListaDiscador = ?) AS t2 ";
		sql += "ON t1.cuil = t2.CUIL ";
		sql += "WHERE t2.CUIL IS NULL ";

		return Sql.update(contexto, SqlEsales.SQL, sql, listaDiscador, fechaDesde, fechaDesde, listaDiscador) == 1;
	}

	public static Boolean guardarProspect(Contexto contexto, SesionEsales sesion, String listaDiscador) {

		String sql = "";
		sql += "INSERT INTO Leads_Prospect ";
		sql += "(Nombre, Apellido, DNI, CUIL, Sexo, ";
		sql += "EMail, CodPostal, TelefonoCelular, ListaDiscador,";
		sql += "FechaRegistro, GuardadoEnGenesys, EsDemorado) ";
		sql += "VALUES ( ?, ?, ?, ?, ?, ";
		sql += "?, ?, ?, ?, ";
		sql += "GETDATE(), 0, 0 )";

		Object[] parametros = obtenerParametrosProspect(contexto, sesion, listaDiscador, 9);

		return Sql.update(contexto, SqlEsales.SQL, sql, parametros) == 1;
	}

	public static ProspectsEsales obtenerProspects(Contexto contexto, Fecha fechaDesde) {
		String sql = "";
		sql += "SELECT * ";
		sql += "FROM Leads_Prospect WITH (NOLOCK) ";
		sql += "WHERE GuardadoEnGenesys = '0' ";
		sql += "AND FechaRegistro >= ? ";
		sql += "AND (ListaDiscador = ? OR ListaDiscador = ?) ";

		Objeto datos = Sql.select(contexto, SqlEsales.SQL, sql, fechaDesde, ProspectsEsales.listaDiscadorPaquete, ProspectsEsales.listaDiscadorIndicador);
		return map(datos, ProspectsEsales.class, ProspectEsales.class);
	}

	public static Boolean actualizarEstadoProspect(Contexto contexto, String cuil, String listaDiscador) {
		String sql = "";
		sql += "UPDATE [esales].[dbo].[Leads_Prospect] SET ";
		sql += "[GuardadoEnGenesys] = 1 ";
		sql += "WHERE ListaDiscador = ? ";
		sql += "AND CUIL = ? ";

		return Sql.update(contexto, SqlEsales.SQL, sql, listaDiscador, cuil) == 1;
	}

	public static Boolean actualizarFechaUltimoEnvio(Contexto contexto, ProspectEsales prospect) {
		String sql = "";
		sql += "UPDATE [esales].[dbo].[Leads_Prospect] SET ";
		sql += "[FechaUltimoEnvio] = GETDATE() ";
		sql += "WHERE ListaDiscador = ? ";
		sql += "AND CUIL = ? ";
		sql += "AND CodProspect = ? ";

		return Sql.update(contexto, SqlEsales.SQL, sql, prospect.ListaDiscador, prospect.CUIL, prospect.CodProspect) == 1;
	}

}