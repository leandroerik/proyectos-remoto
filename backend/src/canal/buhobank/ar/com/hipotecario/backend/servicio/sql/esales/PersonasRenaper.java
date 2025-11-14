package ar.com.hipotecario.backend.servicio.sql.esales;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.base.Util;
import ar.com.hipotecario.backend.conector.sql.Sql;
import ar.com.hipotecario.backend.conector.sql.SqlObjeto;
import ar.com.hipotecario.backend.conector.sql.SqlObjetos;
import ar.com.hipotecario.backend.exception.SqlException;
import ar.com.hipotecario.backend.servicio.sql.SqlEsales;
import ar.com.hipotecario.backend.servicio.sql.esales.PersonasRenaper.PersonaRenaper;
import ar.com.hipotecario.canal.buhobank.BBInfoPersona;

@SuppressWarnings("serial")
public class PersonasRenaper extends SqlObjetos<PersonaRenaper> {

	/* ========== ATRIBUTOS ========== */
	public static class PersonaRenaper extends SqlObjeto {
		public String id;
		public String id_tramite;
		public String ejemplar;
		public Fecha vencimiento;
		public Fecha fecha_emision;
		public String apellido;
		public String nombre;
		public Fecha fecha_nacimiento;
		public String cuil;
		public String calle;
		public String numero;
		public String piso;
		public String departamento;
		public String codigo_postal;
		public String barrio;
		public String monoblock;
		public String ciudad;
		public String municipio;
		public String provincia;
		public String pais;
		public String id_dispositivo;
		public String estado;
		public Boolean renaperVU;
	}

	/* ========== SERVICIO ========== */
	public static PersonaRenaper get(Contexto contexto, String cuil) {
		String sql = "";
		sql += "SELECT * ";
		sql += "FROM [esales].[dbo].[Persona_Renaper] WITH (NOLOCK) ";
		sql += "WHERE cuil = ?";

		Objeto datos = Sql.select(contexto, SqlEsales.SQL, sql, cuil);
		SqlException.throwIf("PERSONA_NO_ENCONTRADA", datos.isEmpty());
		return map(datos, PersonasRenaper.class, PersonaRenaper.class).first();
	}

	public static Boolean update(Contexto contexto, PersonaRenaper persona) {
		if (persona != null) {
			PersonaRenaper personaOriginal = get(contexto, persona.cuil);
			Integer resultado = Sql.updateGenerico(contexto, SqlEsales.SQL, "[esales].[dbo].[Persona_Renaper]", "WHERE cuil = ?", personaOriginal, persona, persona.cuil);
			return resultado != 0;
		}
		return false;
	}

	public static Boolean crear(Contexto contexto, BBInfoPersona infoPersona) {
		String sql = "";
		sql += "INSERT INTO [esales].[dbo].[Persona_Renaper]";
		sql += "([id_tramite], [ejemplar], [vencimiento], [fecha_emision], ";
		sql += "[apellido], [nombre], [fecha_nacimiento], ";
		sql += "[cuil], [calle], ";
		sql += "[numero], [piso], [departamento], ";
		sql += "[codigo_postal], [barrio], [monoblock], [ciudad], ";
		sql += "[municipio], [provincia], ";
		sql += "[pais], [id_dispositivo],[estado], [renaper_VU] )";
		sql += "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		Object[] parametros = obtenerParametros(contexto, infoPersona, 22);

		return Sql.update(contexto, SqlEsales.SQL, sql, parametros) == 1;
	}

	private static Object[] obtenerParametros(Contexto contexto, BBInfoPersona infoPersona, Integer cantidad) {
		Object[] parametros = new Object[cantidad];

		// [id_tramite], [ejemplar], [vencimiento], [fecha_emision]
		parametros[0] = infoPersona.idTramite;
		parametros[1] = infoPersona.ejemplar;
		parametros[2] = !Util.empty(infoPersona.vencimiento) ? infoPersona.vencimiento : null;
		parametros[3] = !Util.empty(infoPersona.emision) ? infoPersona.emision : null;

		// [apellido], [nombre], [fecha_nacimiento], [cuil]
		parametros[4] = infoPersona.apellido;
		parametros[5] = infoPersona.nombre;
		parametros[6] = !Util.empty(infoPersona.fechaNacimiento) ? infoPersona.fechaNacimiento : null;
		parametros[7] = infoPersona.cuil;

		// [calle], [numero], [piso], [departamento]
		parametros[8] = infoPersona.calle;
		parametros[9] = infoPersona.numero;
		parametros[10] = infoPersona.piso;
		parametros[11] = infoPersona.departamento;

		// [codigo_postal], [barrio], [monoblock], [ciudad]
		parametros[12] = infoPersona.codigoPostal;
		parametros[13] = infoPersona.barrio;
		parametros[14] = infoPersona.monoblock;
		parametros[15] = infoPersona.ciudad;

		// [municipio], [provincia], [pais], [id_dispositivo]
		parametros[16] = infoPersona.municipio;
		parametros[17] = infoPersona.provincia;
		parametros[18] = infoPersona.pais;
		parametros[19] = infoPersona.idDispositivo;

		// [estado] [renaper_VU]
		parametros[20] = infoPersona.estado;
		parametros[21] = Util.empty(infoPersona.renaperVU) ? true : infoPersona.renaperVU;

		return parametros;
	}

}
