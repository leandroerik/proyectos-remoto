package ar.com.hipotecario.backend.servicio.sql.homebanking;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.conector.sql.Sql;
import ar.com.hipotecario.backend.conector.sql.SqlObjeto;
import ar.com.hipotecario.backend.conector.sql.SqlObjetos;
import ar.com.hipotecario.backend.servicio.api.catalogo.ApiCatalogo;
import ar.com.hipotecario.backend.servicio.api.catalogo.Paises;
import ar.com.hipotecario.backend.servicio.api.personas.Emails.Email;
import ar.com.hipotecario.backend.servicio.api.personas.Telefonos.Telefono;
import ar.com.hipotecario.backend.servicio.sql.esales.SesionesEsales.SesionEsales;
import ar.com.hipotecario.backend.servicio.sql.homebanking.SolicitudesProductosHB.SolicitudProductosHB;

public class SolicitudesProductosHB extends SqlObjetos<SolicitudProductosHB> {

	private static final long serialVersionUID = 1L;

	/* ========== ATRIBUTOS ========== */
	public static class SolicitudProductosHB extends SqlObjeto {
		public Long sp_id;
	}

	public static Object[] obtenerParametros(Contexto contexto, Integer cantidad, String idCobis, SesionEsales sesion, String tipoProducto, String numeroCuentaAsociada, String idSucursal, String cbuCuentaAsociada) {

		Object[] parametros = new Object[cantidad];

		// [sp_tipoOperacion], [sp_idCobis], [sp_secuencial]
		parametros[0] = "ALTA";
		parametros[1] = idCobis;
		parametros[2] = contexto.canal();

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
		parametros[12] = sesion.documento_tipo_id;
		parametros[13] = sesion.documento_numero;

		// [sp_fechaNacimiento], [sp_paisNacimientoCod], [sp_paisNacimientoDesc],
		// [sp_nacionalidadCod], [sp_nacionalidadDesc], [sp_ciudadNacimiento]

		Paises paises = ApiCatalogo.paises(contexto).tryGet();
		parametros[14] = getFechaStr(sesion.fecha_nacimiento, "dd/MM/yyyy");
		parametros[15] = sesion.pais_nacimiento_id;
		parametros[16] = paises.buscarPaisById(sesion.pais_nacimiento_id).descripcion;
		parametros[17] = sesion.nacionalidad_id;
		parametros[18] = paises.buscarPaisById(sesion.nacionalidad_id).nacionalidad;
		parametros[19] = null;

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
		parametros[33] = sesion.dom_localidad_envio;
		parametros[34] = null;
		parametros[35] = sesion.domicilio_prov_id;
		parametros[36] = null;

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
		parametros[47] = "[" + sesion.cuil + "|80|" + getFechaStr(sesion.fecha_nacimiento, "yyyy/MM/dd").replace("/", "") + "]";
		parametros[48] = "A";

		// [sp_licitudFondos], [sp_ttcc], [sp_resolucion], [sp_cbuCuentaAsociada]
		parametros[49] = "on";
		parametros[50] = "on";
		parametros[51] = null;
		parametros[52] = cbuCuentaAsociada;

		return parametros;
	}

	public static String getFechaStr(Fecha fecha, String format) {

		String anio = fecha.toString().substring(8, 10);
		String mes = fecha.toString().substring(5, 7);
		String dia = fecha.toString().substring(0, 4);

		if (!format.equals("dd/MM/yyyy")) {
			return dia + "/" + mes + "/" + anio;
		}

		return anio + "/" + mes + "/" + dia;
	}

	public static Boolean crearSolicitudProducto(Contexto contexto, String idCobis, SesionEsales sesion, String tipoProducto, String numeroCuentaAsociada, String idSucursal, String cbuCuentaAsociada) {
		String sql = "";
		sql += "INSERT INTO [homebanking].[dbo].[solicitudProducto] ";
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
		sql += "[sp_licitudFondos], [sp_ttcc], [sp_resolucion], [sp_cbuCuentaAsociada] ";
		sql += ") VALUES (";
		sql += "?, ?, GETDATE(), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ";
		sql += "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		Object[] parametros = obtenerParametros(contexto, 53, idCobis, sesion, tipoProducto, numeroCuentaAsociada, idSucursal, cbuCuentaAsociada);

		return Sql.update(contexto, "homebanking", sql, parametros) == 1;
	}

}
