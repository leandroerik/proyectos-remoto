package ar.com.hipotecario.backend.servicio.sql.esales;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.base.Util;
import ar.com.hipotecario.backend.conector.sql.Sql;
import ar.com.hipotecario.backend.conector.sql.SqlObjeto;
import ar.com.hipotecario.backend.conector.sql.SqlObjetos;
import ar.com.hipotecario.backend.exception.SqlException;
import ar.com.hipotecario.backend.servicio.api.catalogo.ApiCatalogo;
import ar.com.hipotecario.backend.servicio.api.catalogo.Paises;
import ar.com.hipotecario.backend.servicio.api.catalogo.Provincias;
import ar.com.hipotecario.backend.servicio.sql.SqlEsales;
import ar.com.hipotecario.backend.servicio.sql.campniaswf.CiudadesWF.CiudadWF;
import ar.com.hipotecario.backend.servicio.sql.esales.SesionesEsales.SesionEsales;
import ar.com.hipotecario.backend.servicio.sql.esales.SesionesEsalesBB2.SesionEsalesBB2;
import ar.com.hipotecario.backend.util.Validadores;
import ar.com.hipotecario.canal.buhobank.BBPersona;
import ar.com.hipotecario.canal.buhobank.ContextoBB;
import ar.com.hipotecario.canal.buhobank.EstadosBB;
import ar.com.hipotecario.canal.buhobank.GeneralBB;
import ar.com.hipotecario.canal.buhobank.SesionBB;
import ar.com.hipotecario.canal.buhobank.SesionBB.ConyugeBB;
import ar.com.hipotecario.canal.buhobank.SesionBB.DomicilioBB;
import ar.com.hipotecario.canal.buhobank.SesionBB.TarjetaOfertaBB;

@SuppressWarnings("serial")
public class SesionesEsales extends SqlObjetos<SesionEsales> {

	/* ========== ATRIBUTOS ========== */
	public static class SesionEsales extends SqlObjeto {
		public String id;
		public String id_canal;
		public String token;
		public String ip;
		public String cuil;
		public Fecha fecha_inicio;
		public String estado;
		public String cobis_id;
		public String id_solicitud_duenios;
		public String resolucion_scoring;
		public String resolucion_riesgo_net;
		public String ticket_riesgo_net;
		public String telefono_celular_ddn;
		public String telefono_celular_caract;
		public String telefono_celular_nro;
		public String telefono_fijo_ddn;
		public String telefono_fijo_caract;
		public String telefono_fijo_nro;
		public String documento_tipo_id;
		public String documento_numero;
		public String documento_version_id;
		public String nivel_estudios;
		public String localidad_id;
		public String pais_residencia_id;
		public String provincia_id;
		public String localidad_descripcion;
		public String nacionalidad_id;
		public String pais_id;
		public Fecha fecha_nacimiento;
		public String nombre;
		public String apellido;
		public String sexo;
		public String mail;
		public Integer ingreso_neto;
		public String ciudad_nacimiento;
		public String domicilio_calle;
		public String domicilio_nro;
		public String domicilio_piso;
		public String domicilio_dpto;
		public String domicilio_cp;
		public String telefono_laboral_ddn;
		public String telefono_laboral_caract;
		public String telefono_laboral_nro;
		public String estado_civil_id;
		public String conyuge_nombre;
		public String conyuge_apellido;
		public String conyuge_cuil;
		public String conyuge_sexo;
		public String conyuge_documento_tipo_id;
		public String conyuge_documento_version_id;
		public String conyuge_documento_numero;
		public String situacion_laboral_id;
		public Boolean pep;
		public String pep_nivel2_id;
		public String pep_relacion;
		public Boolean sujeto_obligado;
		public Boolean aceptacion_oferta;
		public Boolean ciudadano_eeuu;
		public String cuil_tipo;
		public String domicilio_ent_calle1;
		public String domicilio_ent_calle2;
		public String cantidad_nupcias_id;
		public String cantidad_nupcias_desc;
		public String subtipo_estado_civil_id;
		public String subtipo_estado_civil_desc;
		public String conyuge_cuil_tipo;
		public Integer sucursal;
		public String forma_entrega;
		public String codigo_error;
		public Boolean respondio_preguntas;
		public Boolean apellido_uno_dos_car;
		public Boolean apellido_cony_uno_dos_car;
		public String dom_barrio_envio;
		public String dom_calle_envio;
		public String dom_cp_envio;
		public String dom_depto_envio;
		public String dom_localidad_envio;
		public String dom_numero_envio;
		public String dom_piso_envio;
		public String pais_nacimiento_id;
		public String reside_desde;
		public String situacion_vivienda;
		public Boolean aceptar_tyc;
		public String domicilio_prov_id;
		public String modo_aprobacion;
		public String resolucion_explicacion;
		public Boolean nombre_un_car;
		public Boolean nombre_cony_un_car;
		public String id_sesion_ob;
		public String tipo_dispositivo;
		public String referrer;
		public String utm_source;
		public String utm_medium;
		public String utm_campaign;
		public String utm_content;
		public String client_id_analytics;
		public Fecha fecha_ultima_modificacion;
		public Boolean cuenta_corriente;
		public Boolean is_standalone;
		public String tipo_standalone;
		public String id_tipo_banca;

		/* ========== METODOS ========== */
		public Boolean is_standalone() {
			return is_standalone != null && is_standalone;
		}
	}

	/* ========== SERVICIO ========== */
	public static SesionEsales getPorToken(Contexto contexto, String token) {
		return getPorToken(contexto, token, true);
	}

	public static SesionesEsales getPorEstado(Contexto contexto, String estado, Fecha fechaDesde) {
		String sql = "";
		sql += "SELECT DISTINCT * ";
		sql += "FROM [esales].[dbo].[Sesion] WITH (NOLOCK) ";
		sql += "WHERE estado = ? ";
		sql += "AND fecha_inicio >= ? ";
		sql += "ORDER BY id DESC";

		Objeto datos = Sql.select(contexto, SqlEsales.SQL, sql, estado, fechaDesde);
		return map(datos, SesionesEsales.class, SesionEsales.class);
	}

	public static SesionEsales getPorToken(Contexto contexto, String token, Boolean tokenValido) {
		String sql = "";
		sql += "SELECT * ";
		sql += "FROM [esales].[dbo].[Sesion] WITH (NOLOCK) ";
		sql += "WHERE token = ?";
		Objeto datos = Sql.select(contexto, SqlEsales.SQL, sql, token);
		SqlException.throwIf("TOKEN_INVALIDO", datos.isEmpty() && tokenValido);
		if (datos.isEmpty() && !tokenValido)
			return null;
		return map(datos, SesionesEsales.class, SesionEsales.class).first();
	}

	public static SesionesEsales obtenerSesion(Contexto contexto, String cuit, Fecha fecha) {
		String sql = "";
		sql += "SELECT * ";
		sql += "FROM Sesion WITH (NOLOCK) ";
		sql += "WHERE cuil = ? ";
		sql += "AND convert(date,fecha_inicio,112) >= ? ";
		sql += "ORDER BY id DESC ";
		Objeto datos = Sql.select(contexto, SqlEsales.SQL, sql, cuit, fecha);
		return map(datos, SesionesEsales.class, SesionEsales.class);
	}

	public static SesionesEsales getPendiente(Contexto contexto, String cuil, String estado, Fecha fechaDesde) {
		String sql = "";
		sql += "SELECT TOP(100) * ";
		sql += "FROM ( ";
		sql += "SELECT *, ROW_NUMBER() OVER(PARTITION BY cuil ORDER BY id DESC) row_num ";
		sql += "FROM Sesion WITH (NOLOCK) ";
		sql += "WHERE estado = ? ";
		sql += ") s1 ";
		sql += "WHERE s1.row_num = 1 ";
		sql += "AND s1.fecha_inicio >= ? ";
		sql += "AND s1.cuil = ? ";
		sql += "ORDER BY id DESC ";
		Objeto datos = Sql.select(contexto, SqlEsales.SQL, sql, estado, fechaDesde, cuil);
		return map(datos, SesionesEsales.class, SesionEsales.class);
	}

	public static SesionesEsales getPendientes(Contexto contexto, String estado, Fecha fechaDesde) {
		String sql = "";
		sql += "SELECT TOP(100) * ";
		sql += "FROM ( ";
		sql += "SELECT *, ROW_NUMBER() OVER(PARTITION BY cuil ORDER BY id DESC) row_num ";
		sql += "FROM Sesion WITH (NOLOCK) ";
		sql += "WHERE estado = ? ";
		sql += ") s1 ";
		sql += "WHERE s1.row_num = 1 ";
		sql += "AND s1.fecha_inicio >= ? ";
		sql += "ORDER BY id DESC ";
		Objeto datos = Sql.select(contexto, SqlEsales.SQL, sql, estado, fechaDesde);
		return map(datos, SesionesEsales.class, SesionEsales.class);
	}

	public static SesionesEsales getPendientesTMK(Contexto contexto, Fecha fechaFinalizadosDesde) {
		String sql = "";
		sql += "SELECT T_SESIONES.* ";
		sql += "FROM (";
		sql += "SELECT * ";
		sql += "FROM (";
		sql += "SELECT *, ROW_NUMBER() OVER (PARTITION BY cuil ORDER BY fecha_inicio DESC) AS rn ";
		sql += "FROM (";
		sql += "SELECT * FROM Sesion WITH (NOLOCK) ";
		sql += "WHERE telefono_celular_nro != 'NULL' ";
		sql += "AND estado != 'ENVIAR_OTP_MAIL_OK' AND estado != 'VALIDAR_OTP_MAIL_OK' ";
		sql += "AND estado != 'ENVIAR_OTP_SMS_OK' AND estado != 'VALIDAR_OTP_SMS_OK' ";
		sql += "AND estado != 'ERROR_ENVIAR_OTP_MAIL' AND estado != 'ERROR_VALIDAR_OTP_MAIL' ";
		sql += "AND estado != 'ERROR_ENVIAR_OTP_SMS' AND estado != 'ERROR_VALIDAR_OTP_SMS' ";
		sql += "AND resolucion_explicacion = 'APROBADA' ";
		sql += "AND estado != 'FINALIZAR_OK' AND ESTADO != 'BATCH_CORRIENDO' ";
		sql += "AND fecha_inicio >= ? ";
		sql += ") AS T_SESION";
		sql += ") T_SESION WHERE T_SESION.rn = 1";
		sql += ") AS T_SESIONES ";
		sql += "LEFT JOIN (";
		sql += "SELECT cuil, count(*) AS finalizado ";
		sql += "FROM Sesion WITH (NOLOCK) ";
		sql += "WHERE telefono_celular_nro != 'NULL' ";
		sql += "AND estado != 'ENVIAR_OTP_MAIL_OK' AND estado != 'VALIDAR_OTP_MAIL_OK' ";
		sql += "AND estado != 'ENVIAR_OTP_SMS_OK' AND estado != 'VALIDAR_OTP_SMS_OK' ";
		sql += "AND estado != 'ERROR_ENVIAR_OTP_MAIL' AND estado != 'ERROR_VALIDAR_OTP_MAIL' ";
		sql += "AND estado != 'ERROR_ENVIAR_OTP_SMS' AND estado != 'ERROR_VALIDAR_OTP_SMS' ";
		sql += "AND resolucion_explicacion = 'APROBADA' ";
		sql += "AND (estado = 'FINALIZAR_OK' OR ESTADO = 'BATCH_CORRIENDO') ";
		sql += "AND fecha_inicio >= ? ";
		sql += "GROUP BY cuil";
		sql += ") AS T_FINALIZADOS ";
		sql += "ON T_SESIONES.cuil = T_FINALIZADOS.cuil ";
		sql += "WHERE finalizado IS NULL";

		Objeto datos = Sql.select(contexto, SqlEsales.SQL, sql, fechaFinalizadosDesde, fechaFinalizadosDesde);
		return map(datos, SesionesEsales.class, SesionEsales.class);
	}

	public static Object[] obtenerParametros(Contexto contexto, SesionBB sesion, Integer cantidad) {
		Object[] parametros = new Object[cantidad];

		// [id_canal], [token], [ip], [cuil], [cuenta_corriente], [estado]
		parametros[0] = contexto.subCanalVenta();
		parametros[1] = sesion.token;
		parametros[2] = contexto.ip();
		parametros[3] = Validadores.nullString(sesion.cuil);
		parametros[4] = false;
		parametros[5] = sesion.estado;

		// [cobis_id], [id_solicitud_duenios], [resolucion_scoring],
		// [resolucion_riesgo_net]
		parametros[6] = sesion.cobisPositivo() ? Validadores.nullString(sesion.idCobis, true) : null;
		parametros[7] = Validadores.nullString(sesion.idSolicitud, true);
		parametros[8] = Validadores.nullString(sesion.resolucionMotorDeScoring);
		parametros[9] = null;

		// [ticket_riesgo_net], [telefono_celular_ddn], [telefono_celular_caract],
		// [telefono_celular_nro]
		parametros[10] = null;
		parametros[11] = Validadores.nullString(sesion.codAreaCelular());
		parametros[12] = Validadores.nullString(sesion.caracteristicaCelular());
		parametros[13] = Validadores.nullString(sesion.numeroCelular());

		// [telefono_fijo_ddn], [telefono_fijo_caract], [telefono_fijo_nro]
		parametros[14] = null;
		parametros[15] = null;
		parametros[16] = null;

		// [documento_tipo_id], [documento_numero], [documento_version_id],
		// [nivel_estudios]
		parametros[17] = Validadores.nullString(sesion.tipoDocumento());
		parametros[18] = Validadores.nullString(sesion.numeroDocumento, true);
		parametros[19] = Validadores.nullString(sesion.ejemplar);
		parametros[20] = null;

		DomicilioBB domicilioLegal = sesion.domicilioLegal;

		// [localidad_id], [pais_residencia_id], [provincia_id], [localidad_descripcion]
		parametros[21] = domicilioLegal != null ? Validadores.nullString(domicilioLegal.idCiudad, true) : null;
		parametros[22] = domicilioLegal != null ? Validadores.nullString(domicilioLegal.idPais, true) : null;
		parametros[23] = domicilioLegal != null ? Validadores.nullString(domicilioLegal.idProvincia, true) : null;
		parametros[24] = domicilioLegal != null ? Validadores.nullString(domicilioLegal.localidad) : null;

		// [nacionalidad_id], [pais_id], [fecha_nacimiento]
		parametros[25] = Validadores.nullString(sesion.idNacionalidad, true);
		parametros[26] = Validadores.nullString(sesion.idPaisNacimiento, true);
		parametros[27] = !Util.empty(sesion.fechaNacimiento) ? sesion.fechaNacimiento : null;

		// [nombre], [apellido], [sexo], [mail], [ingreso_neto], [ciudad_nacimiento]
		parametros[28] = Validadores.nullString(sesion.nombre);
		parametros[29] = Validadores.nullString(sesion.apellido);
		parametros[30] = Validadores.nullString(sesion.genero);
		parametros[31] = Validadores.nullString(sesion.mail);
		parametros[32] = sesion.ingresoNeto != null ? sesion.ingresoNeto : null;
		parametros[33] = null;

		// [domicilio_calle], [domicilio_nro], [domicilio_piso], [domicilio_dpto],
		// [domicilio_cp]
		parametros[34] = domicilioLegal != null ? Validadores.nullString(domicilioLegal.calle) : null;
		parametros[35] = domicilioLegal != null ? Validadores.nullString(domicilioLegal.numeroCalle, true) : null;
		parametros[36] = domicilioLegal != null ? Validadores.nullString(domicilioLegal.piso) : null;
		parametros[37] = domicilioLegal != null ? Validadores.nullString(domicilioLegal.dpto) : null;
		parametros[38] = domicilioLegal != null ? Validadores.nullString(domicilioLegal.cp) : null;

		// [telefono_laboral_ddn], [telefono_laboral_caract], [telefono_laboral_nro]
		parametros[39] = null;
		parametros[40] = null;
		parametros[41] = null;

		// [estado_civil_id]
		parametros[42] = Validadores.nullString(sesion.idEstadoCivil);

		ConyugeBB conyuge = sesion.conyuge;

		// [conyuge_nombre], [conyuge_apellido], [conyuge_cuil], [conyuge_sexo],
		// [conyuge_documento_tipo_id]
		parametros[43] = conyuge != null ? Validadores.nullString(conyuge.nombres) : null;
		parametros[44] = conyuge != null ? Validadores.nullString(conyuge.apellido) : null;
		parametros[45] = conyuge != null ? Validadores.nullString(conyuge.cuil) : null;
		parametros[46] = conyuge != null ? Validadores.nullString(conyuge.genero) : null;
		parametros[47] = conyuge != null ? Validadores.nullString(conyuge.tipoDocumento()) : null;

		// [conyuge_documento_version_id], [conyuge_documento_numero]
		parametros[48] = null;
		parametros[49] = conyuge != null ? Validadores.nullString(conyuge.numeroDocumento) : null;

		// [situacion_laboral_id]
		parametros[50] = Validadores.nullString(sesion.idSituacionLaboral);

		// [pep], [pep_nivel2_id], [pep_relacion], [sujeto_obligado],
		// [aceptacion_oferta]
		parametros[51] = false;
		parametros[52] = null;
		parametros[53] = null;
		parametros[54] = false;
		parametros[55] = !Util.empty(sesion.ofertaElegida);

		// [ciudadano_eeuu], [cuil_tipo], [domicilio_ent_calle1], [domicilio_ent_calle2]
		parametros[56] = null;
		parametros[57] = !Util.empty(sesion.idTipoIDTributario) ? sesion.idTipoIDTributario : null;
		parametros[58] = null;
		parametros[59] = null;

		// [cantidad_nupcias_id], [cantidad_nupcias_desc], [subtipo_estado_civil_id],
		// [subtipo_estado_civil_desc], [conyuge_cuil_tipo]
		parametros[60] = Validadores.nullString(sesion.idCantidadNupcias);
		parametros[61] = Validadores.nullString(sesion.cantidadNupcias());
		parametros[62] = Validadores.nullString(sesion.idSubtipoEstadoCivil);
		parametros[63] = Validadores.nullString(sesion.subtipoEstadoCivil());
		parametros[64] = null;

		// [sucursal], [forma_entrega]
		parametros[65] = Validadores.nullString(sesion.idSucursal);
		parametros[66] = Validadores.nullString(sesion.formaEntrega);

		// [codigo_error], [respondio_preguntas], [apellido_uno_dos_car],
		// [apellido_cony_uno_dos_car]
		parametros[67] = null;
		parametros[68] = null;
		parametros[69] = null;
		parametros[70] = null;

		DomicilioBB domicilioPostal = sesion.domicilioPostal;

		// [dom_barrio_envio], [dom_calle_envio], [dom_cp_envio], [dom_depto_envio],
		// [dom_localidad_envio]
		parametros[71] = domicilioPostal != null ? Validadores.nullString(domicilioPostal.ciudad) : null;
		parametros[72] = domicilioPostal != null ? Validadores.nullString(domicilioPostal.calle) : null;
		parametros[73] = domicilioPostal != null ? Validadores.nullString(domicilioPostal.cp) : null;
		parametros[74] = domicilioPostal != null ? Validadores.nullString(domicilioPostal.dpto) : null;
		parametros[75] = domicilioPostal != null ? Validadores.nullString(domicilioPostal.idCiudad, true) : null;

		// [dom_numero_envio], [dom_piso_envio], [pais_nacimiento_id], [reside_desde],
		// [situacion_vivienda]
		parametros[76] = domicilioPostal != null ? Validadores.nullString(domicilioPostal.numeroCalle, true) : null;
		parametros[77] = domicilioPostal != null ? Validadores.nullString(domicilioPostal.piso) : null;
		parametros[78] = Validadores.nullString(sesion.idPaisNacimiento, true);
		parametros[79] = null;
		parametros[80] = null;

		// [aceptar_tyc], [domicilio_prov_id], [modo_aprobacion],
		// [resolucion_explicacion], [nombre_un_car]
		parametros[81] = !Util.empty(sesion.aceptartyc);
		parametros[82] = domicilioPostal != null ? Validadores.nullString(domicilioPostal.idProvincia, true) : null;
		parametros[83] = Validadores.nullString(sesion.modoAprobacion);
		String resolucion = !sesion.resolucionAprobada() ? "NO APROBADA" : "APROBADA";
		parametros[84] = sesion.resolucionMotorDeScoring != null ? resolucion : null;
		parametros[85] = null;

		// [nombre_cony_un_car], [id_sesion_ob], [tipo_dispositivo], [referrer],
		// [utm_source], [utm_medium]
		parametros[86] = null;
		parametros[87] = null;
		parametros[88] = null;
		parametros[89] = null;
		parametros[90] = null;
		parametros[91] = null;

		// [utm_campaign], [utm_content], [client_id_analytics], [id_tipo_banca]
		parametros[92] = null;
		parametros[93] = null;
		parametros[94] = null;
		parametros[95] = null;

		// [tipo_standalone], [is_standalone]
		parametros[96] = sesion.tipoOferta();
		parametros[97] = sesion.esStandalone();

		return parametros;
	}

	public static Boolean crearSesion(Contexto contexto, SesionBB sesion) {
		String sql = "";
		sql += "INSERT INTO [esales].[dbo].[Sesion] ";
		sql += "([id_canal], [token], [ip], [cuil], [cuenta_corriente], [estado], ";
		sql += "[cobis_id], [id_solicitud_duenios], [resolucion_scoring], [resolucion_riesgo_net], ";
		sql += "[ticket_riesgo_net], [telefono_celular_ddn], [telefono_celular_caract], ";
		sql += "[telefono_celular_nro], [telefono_fijo_ddn], [telefono_fijo_caract], [telefono_fijo_nro], ";
		sql += "[documento_tipo_id], [documento_numero], [documento_version_id], [nivel_estudios], ";
		sql += "[localidad_id], [pais_residencia_id], [provincia_id], [localidad_descripcion], ";
		sql += "[nacionalidad_id], [pais_id], [fecha_nacimiento], [nombre], [apellido], [sexo], ";
		sql += "[mail], [ingreso_neto], [ciudad_nacimiento], [domicilio_calle], [domicilio_nro], ";
		sql += "[domicilio_piso], [domicilio_dpto], [domicilio_cp], [telefono_laboral_ddn], ";
		sql += "[telefono_laboral_caract], [telefono_laboral_nro], [estado_civil_id], [conyuge_nombre], ";
		sql += "[conyuge_apellido], [conyuge_cuil], [conyuge_sexo], [conyuge_documento_tipo_id], ";
		sql += "[conyuge_documento_version_id], [conyuge_documento_numero], [situacion_laboral_id], ";
		sql += "[pep], [pep_nivel2_id], [pep_relacion], [sujeto_obligado], [aceptacion_oferta], ";
		sql += "[ciudadano_eeuu], [cuil_tipo], [domicilio_ent_calle1], [domicilio_ent_calle2], ";
		sql += "[cantidad_nupcias_id], [cantidad_nupcias_desc], [subtipo_estado_civil_id], ";
		sql += "[subtipo_estado_civil_desc], [conyuge_cuil_tipo], [sucursal], [forma_entrega], ";
		sql += "[codigo_error], [respondio_preguntas], [apellido_uno_dos_car], [apellido_cony_uno_dos_car], ";
		sql += "[dom_barrio_envio], [dom_calle_envio], [dom_cp_envio], [dom_depto_envio], [dom_localidad_envio], ";
		sql += "[dom_numero_envio], [dom_piso_envio], [pais_nacimiento_id], [reside_desde], [situacion_vivienda], ";
		sql += "[aceptar_tyc], [domicilio_prov_id], [modo_aprobacion], [resolucion_explicacion], [nombre_un_car], ";
		sql += "[nombre_cony_un_car], [id_sesion_ob], [tipo_dispositivo], [referrer], [utm_source], [utm_medium], ";
		sql += "[utm_campaign], [utm_content], [client_id_analytics], [id_tipo_banca], ";
		sql += "[tipo_standalone], [is_standalone], [fecha_inicio], [fecha_ultima_modificacion]) ";
		sql += "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ";
		sql += "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ";
		sql += "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ";
		sql += "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, GETDATE(), GETDATE())";
		Object[] parametros = obtenerParametros(contexto, sesion, 98);

		return Sql.update(contexto, SqlEsales.SQL, sql, parametros) == 1;
	}

	public static Boolean actualizarSesion(Contexto contexto, SesionBB sesion) {
		String sql = "";
		sql += "UPDATE [esales].[dbo].[Sesion] SET ";
		sql += "[id_canal] = ? , ";
		sql += "[token] = ? , ";
		sql += "[ip] = ? , ";
		sql += "[cuil] = ? , ";
		sql += "[cuenta_corriente] = ? , ";
		sql += "[estado] = ? , ";
		sql += "[cobis_id] = ? , ";
		sql += "[id_solicitud_duenios] = ? , ";
		sql += "[resolucion_scoring] = ? , ";
		sql += "[resolucion_riesgo_net] = ? , ";
		sql += "[ticket_riesgo_net] = ? , ";
		sql += "[telefono_celular_ddn] = ? , ";
		sql += "[telefono_celular_caract] = ? , ";
		sql += "[telefono_celular_nro] = ? , ";
		sql += "[telefono_fijo_ddn] = ? , ";
		sql += "[telefono_fijo_caract] = ? , ";
		sql += "[telefono_fijo_nro] = ? , ";
		sql += "[documento_tipo_id] = ? , ";
		sql += "[documento_numero] = ? , ";
		sql += "[documento_version_id] = ? , ";
		sql += "[nivel_estudios] = ? , ";
		sql += "[localidad_id] = ? , ";
		sql += "[pais_residencia_id] = ? , ";
		sql += "[provincia_id] = ? , ";
		sql += "[localidad_descripcion] = ? , ";
		sql += "[nacionalidad_id] = ? , ";
		sql += "[pais_id] = ? , ";
		sql += "[fecha_nacimiento] = ? , ";
		sql += "[nombre] = ? , ";
		sql += "[apellido] = ? , ";
		sql += "[sexo] = ? , ";
		sql += "[mail] = ? , ";
		sql += "[ingreso_neto] = ? , ";
		sql += "[ciudad_nacimiento] = ? , ";
		sql += "[domicilio_calle] = ? , ";
		sql += "[domicilio_nro] = ? , ";
		sql += "[domicilio_piso] = ? , ";
		sql += "[domicilio_dpto] = ? , ";
		sql += "[domicilio_cp] = ? , ";
		sql += "[telefono_laboral_ddn] = ? , ";
		sql += "[telefono_laboral_caract] = ? , ";
		sql += "[telefono_laboral_nro] = ? , ";
		sql += "[estado_civil_id] = ? , ";
		sql += "[conyuge_nombre] = ? , ";
		sql += "[conyuge_apellido] = ? , ";
		sql += "[conyuge_cuil] = ? , ";
		sql += "[conyuge_sexo] = ? , ";
		sql += "[conyuge_documento_tipo_id] = ? , ";
		sql += "[conyuge_documento_version_id] = ? , ";
		sql += "[conyuge_documento_numero] = ? , ";
		sql += "[situacion_laboral_id] = ? , ";
		sql += "[pep] = ? , ";
		sql += "[pep_nivel2_id] = ? , ";
		sql += "[pep_relacion] = ? , ";
		sql += "[sujeto_obligado] = ? , ";
		sql += "[aceptacion_oferta] = ? , ";
		sql += "[ciudadano_eeuu] = ? , ";
		sql += "[cuil_tipo] = ? , ";
		sql += "[domicilio_ent_calle1] = ? , ";
		sql += "[domicilio_ent_calle2] = ? , ";
		sql += "[cantidad_nupcias_id] = ? , ";
		sql += "[cantidad_nupcias_desc] = ? , ";
		sql += "[subtipo_estado_civil_id] = ? , ";
		sql += "[subtipo_estado_civil_desc] = ? , ";
		sql += "[conyuge_cuil_tipo] = ? , ";
		sql += "[sucursal] = ? , ";
		sql += "[forma_entrega] = ? , ";
		sql += "[codigo_error] = ? , ";
		sql += "[respondio_preguntas] = ? , ";
		sql += "[apellido_uno_dos_car] = ? , ";
		sql += "[apellido_cony_uno_dos_car] = ? , ";
		sql += "[dom_barrio_envio] = ? , ";
		sql += "[dom_calle_envio] = ? , ";
		sql += "[dom_cp_envio] = ? , ";
		sql += "[dom_depto_envio] = ? , ";
		sql += "[dom_localidad_envio] = ? , ";
		sql += "[dom_numero_envio] = ? , ";
		sql += "[dom_piso_envio] = ? , ";
		sql += "[pais_nacimiento_id] = ? , ";
		sql += "[reside_desde] = ? , ";
		sql += "[situacion_vivienda] = ? , ";
		sql += "[aceptar_tyc] = ? , ";
		sql += "[domicilio_prov_id] = ? , ";
		sql += "[modo_aprobacion] = ? , ";
		sql += "[resolucion_explicacion] = ? , ";
		sql += "[nombre_un_car] = ? , ";
		sql += "[nombre_cony_un_car] = ? , ";
		sql += "[id_sesion_ob] = ? , ";
		sql += "[tipo_dispositivo] = ? , ";
		sql += "[referrer] = ? , ";
		sql += "[utm_source] = ? , ";
		sql += "[utm_medium] = ? , ";
		sql += "[utm_campaign] = ? , ";
		sql += "[utm_content] = ? , ";
		sql += "[client_id_analytics] = ? , ";
		sql += "[id_tipo_banca] = ? , ";
		sql += "[tipo_standalone] = ? , ";
		sql += "[is_standalone] = ? , ";
		sql += "[fecha_ultima_modificacion] = GETDATE() ";
		sql += "WHERE token = ? ";
		Object[] parametros = obtenerParametros(contexto, sesion, 99);

		// Token
		parametros[98] = sesion.token;

		return Sql.update(contexto, SqlEsales.SQL, sql, parametros) == 1;
	}

	public static Boolean guardarSesion(Contexto contexto, SesionBB sesion) {
		SesionEsales sesionEsales = getPorToken(contexto, sesion.token, false);
		Boolean fueGuardado = false;
		if (sesionEsales == null) {
			fueGuardado = crearSesion(contexto, sesion);
		} else {
			fueGuardado = actualizarSesion(contexto, sesion);
		}
		return fueGuardado;
	}

	public static Boolean update(Contexto contexto, SesionEsales sesionEsales) {
		if (sesionEsales != null && sesionEsales.token != null) {
			SesionEsales sesionEsalesOriginal = getPorToken(contexto, sesionEsales.token);
			Integer resultado = Sql.updateGenerico(contexto, SqlEsales.SQL, "[esales].[dbo].[Sesion]", "WHERE id = ?", sesionEsalesOriginal, sesionEsales, sesionEsales.id);
			return resultado != 0;
		}
		return false;
	}

	/* ========== METODOS ========== */
	public static SesionBB retomarSesionAbandono(ContextoBB contexto, SesionEsales sesionEsales, SesionEsalesBB2 sesionEsalesBB2) {
		SesionBB sesion = contexto.sesion();

		sesion.fechaLogin = Fecha.ahora();
		sesion.token = sesion.uuid();
		sesion.cuil = sesionEsales.cuil;
		sesion.idCobis = sesionEsales.cobis_id;
		sesion.reintentos = 1;

		sesion.crearSesion();

		sesion.nombre = sesionEsales.nombre;
		sesion.apellido = sesionEsales.apellido;
		sesion.genero = sesionEsales.sexo;
		sesion.ejemplar = sesionEsales.documento_version_id;
		sesion.fechaNacimiento = sesionEsales.fecha_nacimiento;
		sesion.idPaisNacimiento = sesionEsales.pais_nacimiento_id;
		sesion.idNacionalidad = sesionEsales.nacionalidad_id;

		Provincias provincias = ApiCatalogo.provincias(contexto).tryGet();
		String provincia = null;
		if (!sesionEsales.provincia_id.isEmpty()) {
			provincia = provincias.buscarProvinciaById(sesionEsales.provincia_id).descripcion;
		}
		Paises paises = ApiCatalogo.paises(contexto).tryGet();
		String pais = null;
		if (!sesion.idPaisNacimiento.isEmpty()) {
			pais = paises.buscarPaisById(sesion.idPaisNacimiento).descripcion;
			sesion.nacionalidad = paises.buscarPaisById(sesion.idPaisNacimiento).descripcion;
		}
		sesion.paisNacimiento = sesion.nacionalidad;

		DomicilioBB domicilioLegal = new DomicilioBB();
		String idCiudadLegal = sesionEsales.localidad_id;

		domicilioLegal.ciudad = !Util.empty(sesionEsales.dom_barrio_envio) ? sesionEsales.dom_barrio_envio : "";
		domicilioLegal.localidad = sesionEsales.localidad_descripcion;
		domicilioLegal.calle = sesionEsales.domicilio_calle;
		domicilioLegal.idCiudad = idCiudadLegal;
		domicilioLegal.provincia = provincia;
		domicilioLegal.pais = pais;
		domicilioLegal.idProvincia = sesionEsales.provincia_id;
		domicilioLegal.cp = sesionEsales.domicilio_cp;
		domicilioLegal.numeroCalle = sesionEsales.domicilio_nro;
		domicilioLegal.piso = sesionEsales.domicilio_piso;
		domicilioLegal.dpto = sesionEsales.domicilio_dpto;
		domicilioLegal.idPais = sesionEsales.pais_residencia_id;

		sesion.domicilioLegal = domicilioLegal;

		DomicilioBB domicilioPostal = domicilioLegal.clonar();
		sesion.domicilioPostal = domicilioPostal;

		sesion.mail = sesionEsales.mail;

		sesion.codArea = BBPersona.quitarPrimerosCeros(sesionEsales.telefono_celular_ddn);
		sesion.celular = sesionEsales.telefono_celular_caract + sesionEsales.telefono_celular_nro;

		sesion.idTipoIDTributario = sesionEsales.cuil_tipo;
		sesion.numeroDocumento = sesion.dni();
		sesion.aceptartyc = "1";

		sesion.tokenFirebase = sesionEsalesBB2.token_firebase;
		sesion.telefonoOtpValidado = sesionEsalesBB2.telefono_otp_validado;
		sesion.emailOtpValidado = sesionEsalesBB2.email_otp_validado;
		sesion.plataforma = sesionEsalesBB2.plataforma;
		sesion.versionPlataforma = sesionEsalesBB2.version_plataforma;
		sesion.motorIndicador = sesionEsalesBB2.motor_indicador;
		sesion.usuarioVU = sesionEsalesBB2.usuario_vu;
		sesion.idDispositivo = sesionEsalesBB2.id_dispositivo;
		sesion.sucursalOnboarding = sesionEsalesBB2.sucursal_onboarding;
		sesion.cuilReferido = sesionEsalesBB2.cuil_referido;
		sesion.latitud = sesionEsalesBB2.latitud;
		sesion.longitud = sesionEsalesBB2.longitud;
		sesion.operationVU = sesionEsalesBB2.operation_vu;
		sesion.tdVirtual = sesionEsalesBB2.td_virtual;
		sesion.tcVirtual = sesionEsalesBB2.tc_virtual;
		sesion.finalizarEnEjecucion = false;
		sesion.estado = EstadosBB.RETOMA_SESION;
		sesion.save();

		return sesion;
	}

	public static SesionBB retomarSesion(ContextoBB contexto, SesionEsales sesionEsales) {
		String idEsales = sesionEsales.id;

		SesionBB sesion = contexto.sesion();
		String[] splitArr = !Util.empty(sesionEsales.tipo_standalone) ? sesionEsales.tipo_standalone.split(":") : null;

		sesion.idSolicitud = sesionEsales.id_solicitud_duenios;
		sesion.cuil = sesionEsales.cuil;
		sesion.resolucionMotorDeScoring = sesionEsales.resolucion_scoring;
		sesion.modoAprobacion = sesionEsales.modo_aprobacion;
		sesion.idCobis = sesionEsales.cobis_id;
		sesion.ofertaElegida = splitArr != null && splitArr.length > 0 ? splitArr[0] : null;
		sesion.codigoPaqueteMotor = splitArr != null && splitArr.length > 0 ? splitArr[0] : null;
		sesion.letraTC = splitArr != null && splitArr.length > 1 ? splitArr[1] : null;
		sesion.subProducto = splitArr != null && splitArr.length > 3 ? splitArr[3] : null;
		sesion.tdFisica = splitArr != null && splitArr.length > 4 ? splitArr[4] : null;
		sesion.ingresoNeto = sesionEsales.ingreso_neto;

		sesion.nombre = sesionEsales.nombre;
		sesion.apellido = sesionEsales.apellido;
		sesion.formaEntrega = sesionEsales.forma_entrega;
		sesion.idSucursal = sesionEsales.sucursal;
		sesion.idSituacionLaboral = sesionEsales.situacion_laboral_id;
		sesion.mail = sesionEsales.mail;

		sesion.genero = sesionEsales.sexo;
		sesion.ejemplar = sesionEsales.documento_version_id;
		sesion.fechaNacimiento = sesionEsales.fecha_nacimiento;
		sesion.idPaisNacimiento = sesionEsales.pais_nacimiento_id;
		sesion.idNacionalidad = sesionEsales.nacionalidad_id;

		DomicilioBB domicilioLegal = new DomicilioBB();
		String idCiudadLegal = sesionEsales.localidad_id;
		CiudadWF ciudadLegal = DomicilioBB.ciudadPorId(contexto, idCiudadLegal);
		domicilioLegal.ciudad = !Util.empty(ciudadLegal) ? ciudadLegal.CIU_Descripcion : "";
		domicilioLegal.calle = sesionEsales.domicilio_calle;
		domicilioLegal.idCiudad = idCiudadLegal;
		domicilioLegal.idProvincia = sesionEsales.provincia_id;
		domicilioLegal.cp = sesionEsales.domicilio_cp;
		domicilioLegal.numeroCalle = sesionEsales.domicilio_nro;
		domicilioLegal.piso = sesionEsales.domicilio_piso;
		domicilioLegal.dpto = sesionEsales.domicilio_dpto;
		domicilioLegal.idPais = sesionEsales.pais_residencia_id;
		domicilioLegal.provincia = "";

		DomicilioBB domicilioPostal = new DomicilioBB();
		String idCiudadPostal = sesionEsales.dom_localidad_envio;
		CiudadWF ciudadPostal = DomicilioBB.ciudadPorId(contexto, idCiudadPostal);
		domicilioPostal.ciudad = !Util.empty(ciudadPostal) ? ciudadPostal.CIU_Descripcion : "";
		domicilioPostal.calle = sesionEsales.dom_calle_envio;
		domicilioPostal.idCiudad = idCiudadPostal;
		domicilioPostal.idProvincia = sesionEsales.domicilio_prov_id;
		domicilioPostal.cp = sesionEsales.dom_cp_envio;
		domicilioPostal.numeroCalle = sesionEsales.dom_numero_envio;
		domicilioPostal.piso = sesionEsales.dom_piso_envio;
		domicilioPostal.dpto = sesionEsales.dom_depto_envio;
		domicilioPostal.idPais = sesionEsales.pais_residencia_id;
		domicilioPostal.provincia = "";

		sesion.domicilioLegal = domicilioLegal;
		sesion.domicilioPostal = domicilioPostal;

		sesion.idTipoIDTributario = sesionEsales.cuil_tipo;
		sesion.idEstadoCivil = sesionEsales.estado_civil_id;
		sesion.idCantidadNupcias = sesionEsales.cantidad_nupcias_id;
		sesion.idSubtipoEstadoCivil = sesionEsales.subtipo_estado_civil_id;
		sesion.SubtipoEstadoCivilDescr = sesionEsales.subtipo_estado_civil_desc;

		ConyugeBB conyuge = new ConyugeBB();
		if (sesion.casada()) {
			conyuge.nombres = sesionEsales.conyuge_nombre;
			conyuge.apellido = sesionEsales.conyuge_apellido;
			conyuge.genero = sesionEsales.conyuge_sexo;
			conyuge.numeroDocumento = sesionEsales.conyuge_documento_numero;
			conyuge.cuil = sesionEsales.conyuge_cuil;
		}

		sesion.codArea = BBPersona.quitarPrimerosCeros(sesionEsales.telefono_celular_ddn);
		sesion.celular = sesionEsales.telefono_celular_caract + sesionEsales.telefono_celular_nro;

		TarjetaOfertaBB tarjetaOferta = new TarjetaOfertaBB();

		SesionEsalesBB2 sesionEsalesBB2 = SqlEsales.sesionEsalesBB2(contexto, idEsales).tryGet();
		if (sesionEsalesBB2 != null) {

			sesion.tokenFirebase = sesionEsalesBB2.token_firebase;
			sesion.telefonoOtpValidado = sesionEsalesBB2.telefono_otp_validado;
			sesion.emailOtpValidado = sesionEsalesBB2.email_otp_validado;
			sesion.plataforma = sesionEsalesBB2.plataforma;
			sesion.versionPlataforma = sesionEsalesBB2.version_plataforma;
			sesion.motorIndicador = sesionEsalesBB2.motor_indicador;
			sesion.bbInversorAceptada = sesionEsalesBB2.bb_inversor_aceptada;
			sesion.estadoCajaUsd = sesionEsalesBB2.estado_caja_usd;
			sesion.usuarioVU = sesionEsalesBB2.usuario_vu;
			sesion.idDispositivo = sesionEsalesBB2.id_dispositivo;
			sesion.tipoSitLaboral = sesionEsalesBB2.tipo_sit_laboral;
			sesion.sucursalOnboarding = sesionEsalesBB2.sucursal_onboarding;
			sesion.latitud = sesionEsalesBB2.latitud;
			sesion.longitud = sesionEsalesBB2.longitud;
			sesion.operationVU = sesionEsalesBB2.operation_vu;

			sesion.esExpuestaPolitica = sesionEsalesBB2.es_expuesta_politica;
			sesion.esSujetoObligado = sesionEsalesBB2.es_sujeto_obligado;
			sesion.esFatcaOcde = sesionEsalesBB2.es_fatca_ocde;
			sesion.lavadoDinero = sesionEsalesBB2.lavado_dinero;
			sesion.tdVirtual = sesionEsalesBB2.td_virtual;
			sesion.tcVirtual = sesionEsalesBB2.tc_virtual;
			sesion.cuilReferido = sesionEsalesBB2.cuil_referido;
			sesion.adjustAdid = sesionEsalesBB2.adjust_adid;
			sesion.adjustGpsAdid = sesionEsalesBB2.adjust_gps_adid;

			if (sesion.casada()) {
				conyuge.fechaNacimiento = sesionEsalesBB2.conyuge_fecha_nacimiento;
				conyuge.idNacionalidad = sesionEsalesBB2.conyuge_nacionalidad_id;
				conyuge.idPaisResidencia = sesionEsalesBB2.conyuge_pais_residencia_id;
			}

			if (!sesion.esStandalone()) {
				sesion.limite = sesionEsalesBB2.tc_limite;
				tarjetaOferta.marca = sesionEsalesBB2.tarj_oferta_marca;
				tarjetaOferta.distribucionDesc = sesionEsalesBB2.tarj_oferta_distribucion_desc;
				tarjetaOferta.producto = sesionEsalesBB2.tarj_oferta_producto;
				tarjetaOferta.grupo = sesionEsalesBB2.tarj_oferta_grupo;
				tarjetaOferta.afinidadDesc = sesionEsalesBB2.tarj_oferta_afinidad_desc;
				tarjetaOferta.modLiq = sesionEsalesBB2.tarj_oferta_modelo_liquidacion;
			}
		}

		sesion.tarjetaOferta = tarjetaOferta;
		sesion.conyuge = conyuge;

		sesion.estado = !Util.empty(sesionEsales.estado) ? sesionEsales.estado : null;
		sesion.numeroDocumento = sesion.dni();
		sesion.token = sesionEsales.token;
		sesion.reintentos = 0;
		sesion.finalizarEnEjecucion = false;
		sesion.save();

		return sesion;
	}

	public static SesionesEsales getSesionesSinOferta(Contexto contexto, Fecha fechaDesde, Fecha fechaHasta) {

		String sql = "";
		sql += "SELECT * FROM ( ";
		sql += "SELECT t6.* FROM ( ";
		sql += "SELECT t2.sesion_id FROM ( ";
		sql += "SELECT t1.sesion_id ";
		sql += "FROM [esales].[dbo].[Sesion_Esales_BB2] AS t1 WITH (NOLOCK) ";
		sql += "WHERE t1.telefono_otp_validado = 1 ";
		sql += "GROUP BY t1.sesion_id) AS t2 ";
		sql += "LEFT JOIN (SELECT t3.sesion_id ";
		sql += "FROM [esales].[dbo].[Sesion_Esales_BB2] AS t3 WITH (NOLOCK) ";
		sql += "WHERE t3.motor_indicador IS NOT NULL ";
		sql += "GROUP BY t3.sesion_id) AS t4 ";
		sql += "ON t2.sesion_id = t4.sesion_id ";
		sql += "WHERE t4.sesion_id IS NULL) AS t5 ";
		sql += "INNER JOIN  [esales].[dbo].[Sesion] AS t6 WITH (NOLOCK) ";
		sql += "ON t5.sesion_id = t6.id) AS t7 ";
		sql += "LEFT JOIN (SELECT cuit ";
		sql += "FROM [buhobank].[dbo].[log] WITH (NOLOCK) ";
		sql += "WHERE evento = 'BB_OFERTAS_OBTENIDAS' ";
		sql += "GROUP BY cuit) AS t8 ";
		sql += "ON t7.cuil = t8.cuit ";
		sql += "WHERE t8.cuit IS NULL ";
		sql += "AND fecha_ultima_modificacion > ? ";
		sql += "AND fecha_ultima_modificacion < ? ";

		Objeto datos = Sql.select(contexto, SqlEsales.SQL, sql, fechaDesde, fechaHasta);
		return map(datos, SesionesEsales.class, SesionEsales.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("BB", "desarrollo");
		String nroDocumento = "12345678";
		String cuil = "20305255891";
		String test = "obtenerSesion";

		if ("obtenerSesion".equals(test)) {
			Fecha fecha = Fecha.hoy().restarDias(5).removerHora();
			SesionesEsales sesionesEsales = obtenerSesion(contexto, cuil, fecha);
			imprimirResultado(contexto, sesionesEsales);
		}

		if ("get".equals(test)) {
			String token = "/envSstXUoMod9at6wGd/xYg5QCv7kdSUuyW/jcEaX/hVaobBoN+9JGoPrzd8EVt7MDykS0klLbUjwiQSw+8gaTtLKUW3SUY";
			SesionEsales sesionEsales = getPorToken(contexto, token);
			imprimirResultado(contexto, sesionEsales);
		}

		if ("getp".equals(test)) {
			String estado = "FINALIZAR_OK";
			Fecha fechaDesde = Fecha.ahora().restarDias(4);
			SesionesEsales sesionesEsales = getPendientes(contexto, estado, fechaDesde);
			imprimirResultado(contexto, sesionesEsales);
		}

		if ("crear".equals(test)) {
			SesionBB sesion = new SesionBB();
			sesion.idCobis = "-1";
			sesion.cuil = cuil;
			sesion.numeroDocumento = nroDocumento;
			sesion.fechaLogin = Fecha.ahora();
			sesion.token = sesion.uuid();

			Boolean fueCreado = guardarSesion(contexto, sesion);
			imprimirResultado(contexto, fueCreado);
		}

		if ("nuevo".equals(test)) {
			SesionBB sesion = new SesionBB();
			sesion.idCobis = "-1";
			sesion.cuil = cuil;
			sesion.numeroDocumento = nroDocumento;
			sesion.fechaLogin = Fecha.ahora();
			sesion.token = "DuOAuO0h9qq/udYf3QyfalLeKHo78+Pb4zPbBfmXJzRrwKIwLJKnEAiHI5nQn33kGYgodistG/r+QHf//8n4HLmR6NwQuj5q";

			Boolean fueCreado = guardarSesion(contexto, sesion);
			imprimirResultado(contexto, fueCreado);
		}

		if ("retomar".equals(test)) {
			String token = "3KwM+BnSGlknI5/5f+Y+vJ0APzUsaFfWRTmJnoMnTkYwbMj8RexFFwxOgVXS5rJ0X/ZHYAYp2mw=";
			ContextoBB contextoTest = new ContextoBB("BB", "homologacion", "1");
			SesionEsales sesionEsales = getPorToken(contexto, token);
			SesionBB sesion = retomarSesion(contextoTest, sesionEsales);
			System.out.println("Termina retomar SesionBB con token: " + sesion.token);
		}

		if ("retomarLoop".equals(test)) {
			ContextoBB contextoTest = new ContextoBB("BB", "homologacion", "1");
			String[] tokens = { "DuOAuO0h9qq/udYf3QyfalLeKHo78+Pb4zPbBfmXJzRrwKIwLJKnEAiHI5nQn33kGYgodistG/r+QHf//8n4HLmR6NwQuj5q", "3KwM+BnSGlknI5/5f+Y+vJ0APzUsaFfWRTmJnoMnTkYwbMj8RexFFwxOgVXS5rJ0X/ZHYAYp2mw=" };
			for (String token : tokens) {
				SesionEsales sesionEsales = getPorToken(contexto, token);
				if (sesionEsales == null)
					continue;

				SesionBB sesion = retomarSesion(contextoTest, sesionEsales);
				System.out.println("Termina retomar SesionBB con token: " + sesion.token);
			}

			SesionBB sesFinal = contextoTest.sesion();
			System.out.println("Termina retomar loop con ultimo registro: " + sesFinal.token);
		}
	}

	public static SesionesEsales sesionesBBInversorPaquetes(Contexto contexto, Fecha fechaDesde) {

		String sql = "";
		sql += "SELECT t1.* ";
		sql += "FROM [esales].[dbo].[Sesion] AS t1 WITH (NOLOCK) ";
		sql += "INNER JOIN [esales].[dbo].[Sesion_Esales_BB2] AS t2 WITH (NOLOCK) ";
		sql += "ON t1.id = t2.sesion_id ";
		sql += "WHERE t1.fecha_inicio > ? ";
		sql += "AND t2.bb_inversor_aceptada = ? ";
		sql += "AND t1.estado = ? ";
		sql += "ORDER BY t1.id DESC ";

		Objeto datos = Sql.select(contexto, SqlEsales.SQL, sql, fechaDesde, GeneralBB.CUENTA_INVERSOR_ACEPTADA, EstadosBB.FINALIZAR_OK);
		return map(datos, SesionesEsales.class, SesionEsales.class);
	}

	public static Boolean actualizarEstadoByToken(Contexto contexto, String tokenSesion, String estado) {
		String sql = "";
		sql += "UPDATE [esales].[dbo].[Sesion] SET ";
		sql += "[estado] = ? ";
		sql += "WHERE token = ? ";

		return Sql.update(contexto, SqlEsales.SQL, sql, estado, tokenSesion) > 0;
	}

	public static SesionesEsales obtenerSesionByEstado(Contexto contexto, String cuit, String estado, Fecha fecha) {
		String sql = "";
		sql += "SELECT * ";
		sql += "FROM Sesion WITH (NOLOCK) ";
		sql += "WHERE cuil = ? ";
		sql += "AND estado = ? ";
		sql += "AND convert(date,fecha_inicio,112) >= ? ";
		sql += "ORDER BY id DESC ";
		Objeto datos = Sql.select(contexto, SqlEsales.SQL, sql, cuit, estado, fecha);
		return map(datos, SesionesEsales.class, SesionEsales.class);
	}

	public static SesionesEsales getPorMail(Contexto contexto, String mail) {
		String sql = "";
		sql += "SELECT * ";
		sql += "FROM Sesion WITH (NOLOCK) ";
		sql += "WHERE mail = ? ";

		Objeto datos = Sql.select(contexto, SqlEsales.SQL, sql, mail);
		return map(datos, SesionesEsales.class, SesionEsales.class);
	}
	
	public static Boolean mailValidado(Contexto contexto, String cuil, String mail) {
		String sql = "";
		sql += "SELECT t1.* ";
		sql += "FROM [esales].[dbo].[Sesion] AS t1 WITH (NOLOCK) ";
		sql += "INNER JOIN [esales].[dbo].[Sesion_Esales_BB2] AS t2 WITH (NOLOCK) ";
		sql += "ON t1.id = t2.sesion_id ";
		sql += "WHERE t1.fecha_ultima_modificacion > ? ";
		sql += "AND t1.cuil = ? ";
		sql += "AND t1.mail = ? ";
		sql += "AND t2.email_otp_validado = 1 ";

		Objeto datos = Sql.select(contexto, SqlEsales.SQL, sql, Fecha.ahora().restarDias(GeneralBB.DIAS_RETOMAR_CONTACTO), cuil, mail);
		return map(datos, SesionesEsales.class, SesionEsales.class).size() > 0;
	}
	
	public static Boolean existeMailFinalizado(Contexto contexto, String cuil, String mail) {
		String sql = "";
		sql += "SELECT * ";
		sql += "FROM Sesion WITH (NOLOCK) ";
		sql += "WHERE estado = 'FINALIZAR_OK' ";
		sql += "AND mail = ? ";
		sql += "AND cuil != ? ";

		Objeto datos = Sql.select(contexto, SqlEsales.SQL, sql, mail, cuil);
		return map(datos, SesionesEsales.class, SesionEsales.class).size() > 0;
	}

	public static Boolean existeTelefonoFinalizado(Contexto contexto, String cuil, String codigoArea, String caracteristica, String numero) {
		String sql = "";
		sql += "SELECT * ";
		sql += "FROM Sesion WITH (NOLOCK) ";
		sql += "WHERE estado = 'FINALIZAR_OK' ";
		sql += "AND telefono_celular_ddn = ? ";
		sql += "AND telefono_celular_caract = ? ";
		sql += "AND telefono_celular_nro = ? ";
		sql += "AND cuil != ? ";

		Objeto datos = Sql.select(contexto, SqlEsales.SQL, sql, codigoArea, caracteristica, numero, cuil);
		return map(datos, SesionesEsales.class, SesionEsales.class).size() > 0;
	}

	public static Boolean telefonoValidado(Contexto contexto, String cuil, String codigoArea, String caracteristica, String numero) {
		String sql = "";
		sql += "SELECT t1.* ";
		sql += "FROM [esales].[dbo].[Sesion] AS t1 WITH (NOLOCK) ";
		sql += "INNER JOIN [esales].[dbo].[Sesion_Esales_BB2] AS t2 WITH (NOLOCK) ";
		sql += "ON t1.id = t2.sesion_id ";
		sql += "WHERE t1.fecha_ultima_modificacion >= ? ";
		sql += "AND t1.cuil = ? ";
		sql += "AND t2.telefono_otp_validado = 1 ";
		sql += "AND t1.telefono_celular_ddn = ? ";
		sql += "AND t1.telefono_celular_caract = ? ";
		sql += "AND t1.telefono_celular_nro = ? ";

		Fecha fechaDesde = Fecha.ahora().restarDias(GeneralBB.DIAS_RETOMAR_CONTACTO);
		Objeto datos = Sql.select(contexto, SqlEsales.SQL, sql, fechaDesde, cuil, codigoArea, caracteristica, numero);
		return map(datos, SesionesEsales.class, SesionEsales.class).size() > 0;
	}
}
