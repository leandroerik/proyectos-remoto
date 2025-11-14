package ar.com.hipotecario.backend.servicio.sql.esales;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.conector.sql.Sql;
import ar.com.hipotecario.backend.conector.sql.SqlObjeto;
import ar.com.hipotecario.backend.conector.sql.SqlObjetos;
import ar.com.hipotecario.backend.servicio.sql.SqlEsales;
import ar.com.hipotecario.backend.servicio.sql.esales.SesionesOBEsales.SesionOBEsales;
import ar.com.hipotecario.canal.buhobank.BBVU;
import ar.com.hipotecario.canal.buhobank.SesionBB;

@SuppressWarnings("serial")
public class SesionesOBEsales extends SqlObjetos<SesionOBEsales> {

	/* ========== ATRIBUTOS ========== */
	public static class SesionOBEsales extends SqlObjeto {
		public String id;
		public Fecha fecha_inicio;
		public String estado;
		public String id_operacion;
		public String navegador;
		public String navegador_version;
		public String user_name;
		public String ip_address;
		public String application_version;
		public String operative_system;
		public String operative_system_version;
		public String id_estado_operacion;
		public float fiabilidad;
		public float fiabilidad_documento;
		public float fiabilidad_total;
		public String mrz;
		public String barcode;
		public String ocr;
		public String percentage_names;
		public String percentage_birthdate;
		public String percentage_document_number;
		public String percentage_last_names;
		public String percentage_gender;
		public String expiry_date;
		public String gender;
		public String document_number;
		public String full_name;
		public String birth_date;
		public Boolean identico;
		public Fecha fecha_ultima_modificacion;

	}

	/* ========== SERVICIO ========== */

	public static Boolean crearSesionOB(Contexto contexto, SesionBB sesion, BBVU infoVU) {
		String sql = "";
		sql += "INSERT INTO [esales].[dbo].[Sesion_OB] ";
		sql += "([fecha_inicio], [estado], [id_operacion], [navegador], ";
		sql += "[navegador_version], [user_name], [ip_address], ";
		sql += "[application_version], [operative_system], ";
		sql += "[id_estado_operacion], [fiabilidad], [fiabilidad_documento], ";
		sql += "[fiabilidad_total], [mrz], [barcode], [ocr], ";
		sql += "[percentage_names], [percentage_birthdate], ";
		sql += "[percentage_document_number], [percentage_last_names], ";
		sql += "[percentage_gender], [expiry_date], [gender], ";
		sql += "[document_number], [full_name], [birth_date], ";
		sql += "[identico], [fecha_ultima_modificacion]) ";
		sql += "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		Object[] parametros = obtenerParametros(contexto, sesion, infoVU, 28);

		return Sql.update(contexto, SqlEsales.SQL, sql, parametros) == 1;
	}

	private static Object[] obtenerParametros(Contexto contexto, SesionBB sesion, BBVU infoVU, Integer cantidad) {
		Object[] parametros = new Object[cantidad];

		// [fecha_inicio], [estado], [id_operacion], [navegador]
		parametros[0] = sesion.fechaLogin;
		parametros[1] = null;
		parametros[2] = infoVU.idOperacionVU;
		parametros[3] = null;

		// [navegador_version], [user_name], [ip_address],
		parametros[4] = null;
		parametros[5] = sesion.cuil;
		parametros[6] = contexto.ip();

		// [application_version], [operative_system]
		parametros[7] = null;
		parametros[8] = null;

		// [id_estado_operacion], [fiabilidad], [fiabilidad_documento]
		parametros[9] = null;
		parametros[10] = infoVU.fiabilidad;
		parametros[11] = infoVU.fiabilidadDocumento;

		// [fiabilidad_total], [mrz], [barcode], [ocr]
		parametros[12] = infoVU.fiabilidadTotal;
		parametros[13] = infoVU.mrz;
		parametros[14] = infoVU.barcode;
		parametros[15] = infoVU.ocr;

		// [percentage_names], [percentage_birthdate]
		parametros[16] = infoVU.percentageNames;
		parametros[17] = infoVU.percentageBirthdate;

		// [percentage_document_number], [percentage_last_names]
		parametros[18] = infoVU.percentageDocumentNumber;
		parametros[19] = infoVU.percentageLastNames;

		// [percentage_gender], [expiry_date], [gender]
		parametros[20] = infoVU.percentageGender;
		parametros[21] = infoVU.expiryDate.isNull() ? null : infoVU.expiryDate;
		parametros[22] = sesion.genero;

		// [document_number], [full_name], [birth_date]
		parametros[23] = sesion.dni();
		parametros[24] = sesion.apellido + " " + sesion.nombre;
		parametros[25] = sesion.fechaNacimiento;

		// [identico], [fecha_ultima_modificacion]
		parametros[26] = infoVU.identico;
		parametros[27] = sesion.fechaUltimaActividad;

		return parametros;
	}

	/* ========== MANEJO =========== */

}
