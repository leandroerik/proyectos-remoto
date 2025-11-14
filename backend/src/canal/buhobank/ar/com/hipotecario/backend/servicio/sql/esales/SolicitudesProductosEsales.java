package ar.com.hipotecario.backend.servicio.sql.esales;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.base.Util;
import ar.com.hipotecario.backend.conector.sql.Sql;
import ar.com.hipotecario.backend.conector.sql.SqlObjeto;
import ar.com.hipotecario.backend.conector.sql.SqlObjetos;
import ar.com.hipotecario.backend.servicio.api.personas.Emails.Email;
import ar.com.hipotecario.backend.servicio.api.personas.Telefonos.Telefono;
import ar.com.hipotecario.backend.servicio.sql.SqlEsales;
import ar.com.hipotecario.backend.servicio.sql.esales.SesionesEsales.SesionEsales;
import ar.com.hipotecario.backend.servicio.sql.esales.SolicitudesProductosEsales.SolicitudProductosHB;
import ar.com.hipotecario.canal.buhobank.BBInversor.DescripcionDomicilio;
import ar.com.hipotecario.canal.buhobank.BBPersona;

public class SolicitudesProductosEsales extends SqlObjetos<SolicitudProductosHB> {

	private static final long serialVersionUID = 1L;

	/* ========== ATRIBUTOS ========== */
	public static class SolicitudProductosHB extends SqlObjeto {
		public Long sp_id;
		public String sp_idTributario;
		public String sp_ciudadNacimiento;
		public String sp_provinciaCod;
		public String sp_provinciaDesc;
		public String sp_localidadCod;
		public String sp_localidadDesc;
		public String sp_nacionalidadCod;
		public String sp_nacionalidadDesc;
		public String sp_paisNacimientoCod;
		public String sp_paisNacimientoDesc;

	}

	public static SolicitudesProductosEsales obtenerSolicitudes(Contexto contexto) {
		String sql = "";
		sql += "SELECT sp_idTributario, sp_ciudadNacimiento, sp_provinciaCod, sp_provinciaDesc, sp_localidadCod, sp_localidadDesc, sp_nacionalidadCod, sp_paisNacimientoCod ";
		sql += "FROM [esales].[dbo].[solicitudProducto] WITH (NOLOCK) ";
		sql += "GROUP BY sp_idTributario, sp_ciudadNacimiento, sp_provinciaCod, sp_provinciaDesc, sp_localidadCod, sp_localidadDesc, sp_nacionalidadCod, sp_paisNacimientoCod ";

		Objeto datos = Sql.select(contexto, SqlEsales.SQL, sql);
		return map(datos, SolicitudesProductosEsales.class, SolicitudProductosHB.class);
	}

	public static Object[] obtenerParametros(Contexto contexto, Integer cantidad, String idCobis, SesionEsales sesion, String tipoProducto, String numeroCuentaUsd, String cbuUsd, String numeroCuentaAsociada, String idSucursal, String cbuCuentaAsociada, String presentaDoc, DescripcionDomicilio domicilio) {

		Object[] parametros = new Object[cantidad];

		// [sp_tipoOperacion], [sp_idCobis], [sp_secuencial]
		parametros[0] = "ALTA";
		parametros[1] = idCobis;
		parametros[2] = null;

		// [sp_tipoProducto], [sp_numeroProducto], [sp_moneda]
		parametros[3] = tipoProducto;
		parametros[4] = null;
		parametros[5] = null;

		// [sp_tipoCuentaAsociada], [sp_numeroCuentaAsociada], [sp_sucursal]
		parametros[6] = "AHO";
		parametros[7] = numeroCuentaAsociada;
		parametros[8] = idSucursal;

		// [sp_apellido], [sp_nombre], [sp_sexo]
		parametros[9] = sesion.apellido;
		parametros[10] = sesion.nombre;
		parametros[11] = sesion.sexo;

		// [sp_tipoDocumento], [sp_numeroDocumento]
		parametros[12] = BBPersona.quitarPrimerosCeros(sesion.documento_tipo_id);
		parametros[13] = sesion.documento_numero;

		// [sp_fechaNacimiento], [sp_paisNacimientoCod], [sp_paisNacimientoDesc],
		// [sp_nacionalidadCod], [sp_nacionalidadDesc], [sp_ciudadNacimiento]

		parametros[14] = getFechaStr(sesion.fecha_nacimiento, "dd/MM/yyyy");
		parametros[15] = domicilio.paisNacimientoId;
		parametros[16] = domicilio.paisNacimientoDesc;
		parametros[17] = domicilio.nacionalidadId;
		parametros[18] = domicilio.nacionalidadDesc;
		parametros[19] = domicilio.ciudadNacimientoDesc;

		// [sp_tipoIdTributario], [sp_idTributario], [sp_estadoCivil],
		// [sp_situacionLaboral]
		parametros[20] = sesion.cuil_tipo;
		parametros[21] = sesion.cuil;
		parametros[22] = sesion.estado_civil_id;
		parametros[23] = sesion.situacion_laboral_id;

		// [sp_profesionCod], [sp_profesionDesc], [sp_fechaIngreso], [sp_sueldo]
		parametros[24] = null;
		parametros[25] = null;
		parametros[26] = getFechaStr(sesion.fecha_nacimiento, "dd/MM/yyyy");
		parametros[27] = null;

		// [sp_calle], [sp_altura], [sp_piso], [sp_departamento], [sp_codigoPostal]
		parametros[28] = sesion.dom_calle_envio;
		parametros[29] = sesion.dom_numero_envio;
		parametros[30] = sesion.dom_piso_envio;
		parametros[31] = sesion.dom_depto_envio;
		parametros[32] = sesion.dom_cp_envio;

		// [sp_localidadCod], [sp_localidadDesc], [sp_provinciaCod], [sp_provinciaDesc]
		parametros[33] = domicilio.localidadId;
		parametros[34] = domicilio.localidadDesc;
		parametros[35] = domicilio.provinciaId;
		parametros[36] = domicilio.provinciaDesc;

		// [sp_tipoTelefonoCod], [sp_tipoTelefonoDesc], [sp_ddiTelefono],
		// [sp_ddnTelefono], [sp_caracteristicaTelefono], [sp_numeroTelefono]
		parametros[37] = Telefono.CELULAR;
		parametros[38] = "Celular";
		parametros[39] = "54";
		parametros[40] = sesion.telefono_celular_ddn;
		parametros[41] = sesion.telefono_celular_caract;
		parametros[42] = sesion.telefono_celular_nro;

		// [sp_tipoMail], [sp_mail]
		parametros[43] = Email.PERSONAL;
		parametros[44] = sesion.mail;

		// [sp_pep], [sp_so], [sp_ocde], [sp_fatca]
		parametros[45] = "N";
		parametros[46] = "N";

		String cuilOcde = sesion.cuil;

		if (!Util.empty(cuilOcde) && cuilOcde.length() == 11) {
			cuilOcde = sesion.cuil.substring(0, 2) + '-' + sesion.cuil.substring(2, 10) + '-' + sesion.cuil.substring(10, 11);
		}

		parametros[47] = "[" + cuilOcde + "|80|" + getFechaStr(sesion.fecha_nacimiento, "yyyy/MM/dd").replace("/", "") + "]";
		parametros[48] = "A";

		// [sp_licitudFondos], [sp_ttcc], [sp_resolucion], [sp_cbuCuentaAsociada]
		parametros[49] = "on";
		parametros[50] = "on";
		parametros[51] = null;
		parametros[52] = cbuCuentaAsociada;
		parametros[53] = presentaDoc;
		parametros[54] = null;
		parametros[55] = numeroCuentaUsd;
		parametros[56] = cbuUsd;

		return parametros;
	}

	public static String getFechaStr(Fecha fecha, String format) {

		if (Util.empty(fecha)) {
			return "";
		}

		String anio = fecha.toString().substring(8, 10);
		String mes = fecha.toString().substring(5, 7);
		String dia = fecha.toString().substring(0, 4);

		if (!format.equals("dd/MM/yyyy")) {
			return dia + "/" + mes + "/" + anio;
		}

		return anio + "/" + mes + "/" + dia;
	}

	public static Boolean crearSolicitudProducto(Contexto contexto, String idCobis, SesionEsales sesion, String tipoProducto, String numeroCuentaUsd, String cbuUsd, String numeroCuentaAsociada, String idSucursal, String cbuCuentaAsociada, String presentaDoc, DescripcionDomicilio domicilio) {
		String sql = "";
		sql += "INSERT INTO [esales].[dbo].[solicitudProducto] ";
		sql += "([sp_tipoOperacion], [sp_idCobis], [sp_fechaSolicitud], [sp_secuencial], ";
		sql += "[sp_tipoProducto], [sp_numeroProducto], [sp_moneda], ";
		sql += "[sp_tipoCuentaAsociada], [sp_numeroCuentaAsociada], [sp_sucursal], ";
		sql += "[sp_apellido], [sp_nombre], [sp_sexo], ";
		sql += "[sp_tipoDocumento], [sp_numeroDocumento], ";
		sql += "[sp_fechaNacimiento], [sp_paisNacimientoCod], [sp_paisNacimientoDesc], [sp_nacionalidadCod], [sp_nacionalidadDesc], [sp_ciudadNacimiento], ";
		sql += "[sp_tipoIdTributario], [sp_idTributario], [sp_estadoCivil], [sp_situacionLaboral], ";
		sql += "[sp_profesionCod], [sp_profesionDesc], [sp_fechaIngreso], [sp_sueldo], ";
		sql += "[sp_calle], [sp_altura], [sp_piso], [sp_departamento], [sp_codigoPostal],  ";
		sql += "[sp_localidadCod], [sp_localidadDesc], [sp_provinciaCod], [sp_provinciaDesc],  ";
		sql += "[sp_tipoTelefonoCod], [sp_tipoTelefonoDesc], [sp_ddiTelefono], [sp_ddnTelefono], [sp_caracteristicaTelefono], [sp_numeroTelefono], ";
		sql += "[sp_tipoMail], [sp_mail], ";
		sql += "[sp_pep], [sp_so], [sp_ocde], [sp_fatca], ";
		sql += "[sp_licitudFondos], [sp_ttcc], [sp_resolucion], [sp_cbuCuentaAsociada], [sp_presenta_doc], [sp_estado], [sp_numeroCuentaUsd], [sp_cbuCuentaUsd] ";
		sql += ") VALUES (";
		sql += "?, ?, GETDATE(), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ";
		sql += "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		Object[] parametros = obtenerParametros(contexto, 57, idCobis, sesion, tipoProducto, numeroCuentaUsd, cbuUsd, numeroCuentaAsociada, idSucursal, cbuCuentaAsociada, presentaDoc, domicilio);

		return Sql.update(contexto, SqlEsales.SQL, sql, parametros) == 1;
	}

	public static Boolean modSolicitudByCuil(Contexto contexto, SolicitudProductosHB solicitud) {
		String sql = "";
		sql += "UPDATE solicitudProducto SET ";
		sql += "sp_ciudadNacimiento = ? , ";
		sql += "sp_provinciaCod = ? , ";
		sql += "sp_provinciaDesc = ? , ";
		sql += "sp_localidadCod = ? , ";
		sql += "sp_localidadDesc = ? , ";
		sql += "sp_nacionalidadCod = ? , ";
		sql += "sp_nacionalidadDesc = ? , ";
		sql += "sp_paisNacimientoCod = ? , ";
		sql += "sp_paisNacimientoDesc = ? ";
		sql += "WHERE sp_idTributario = ? ";

		return Sql.update(contexto, SqlEsales.SQL, sql, solicitud.sp_ciudadNacimiento, solicitud.sp_provinciaCod, solicitud.sp_provinciaDesc, solicitud.sp_localidadCod, solicitud.sp_localidadDesc, solicitud.sp_nacionalidadCod, solicitud.sp_nacionalidadDesc, solicitud.sp_paisNacimientoCod, solicitud.sp_paisNacimientoDesc, solicitud.sp_idTributario) > 0;
	}

}
