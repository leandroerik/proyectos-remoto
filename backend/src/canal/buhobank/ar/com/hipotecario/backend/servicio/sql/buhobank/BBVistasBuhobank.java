package ar.com.hipotecario.backend.servicio.sql.buhobank;

import com.github.jknack.handlebars.Handlebars.Utils;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.base.Util;
import ar.com.hipotecario.backend.conector.sql.Sql;
import ar.com.hipotecario.backend.conector.sql.SqlObjeto;
import ar.com.hipotecario.backend.conector.sql.SqlObjetos;
import ar.com.hipotecario.backend.servicio.sql.SqlBuhoBank;
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBVistasBuhobank.BBVistaBuhobank;
import ar.com.hipotecario.canal.buhobank.BBFormaEntrega;
import ar.com.hipotecario.canal.buhobank.BBPersona;
import ar.com.hipotecario.canal.buhobank.BBSeguridad;
import ar.com.hipotecario.canal.buhobank.BBValidacion;
import ar.com.hipotecario.canal.buhobank.ContextoBB;
import ar.com.hipotecario.canal.buhobank.EstadosBB;
import ar.com.hipotecario.canal.buhobank.GeneralBB;
import ar.com.hipotecario.canal.buhobank.SesionBB;

@SuppressWarnings("serial")
public class BBVistasBuhobank extends SqlObjetos<BBVistaBuhobank> {

	// vistas
	public static String V_COMPLETAR_DOM_LEGAL_00 = "V_COMPLETAR_DOM_LEGAL_00";
	public static String V_GUARDAR_CONYUGE_00 = "V_GUARDAR_CONYUGE_00";
	public static String V_ELEGIR_DOM_ENVIO_00 = "V_ELEGIR_DOM_ENVIO_00";
	public static String V_FLUJO_VU_00 = "V_FLUJO_VU_00";
	public static String V_TYC_00 = "V_TYC_00";
	public static String V_SCANNER_DNI_00 = "V_SCANNER_DNI_00";
	public static String V_CREAR_CLAVE_00 = "V_CREAR_CLAVE_00";
	public static String V_VALIDACION_SMS_00 = "V_VALIDACION_SMS_00";
	public static String V_VALIDACION_SMS_OK_00 = "V_VALIDACION_SMS_OK_00";
	public static String V_FELICITACIONES_00 = "V_FELICITACIONES_00";
	public static String V_GUARDAR_NACIONALIDAD_00 = "V_GUARDAR_NACIONALIDAD_00";
	public static String V_COMPLETAR_DOM_POSTAL_00 = "V_COMPLETAR_DOM_POSTAL_00";
	public static String V_TIPO_TARJETA_00 = "V_TIPO_TARJETA_00";
	public static String V_ELEGIR_CUENTA_SUELDO_00 = "V_ELEGIR_CUENTA_SUELDO_00";
	public static String V_VALIDACION_EMAIL_00 = "V_VALIDACION_EMAIL_00";

	public static class BBVistaBuhobank extends SqlObjeto {
		public Integer id;
		public Integer id_plantilla_flujo;
		public Integer orden_vista;
		public String codigo_vista;
		public Boolean retoma_sesion;
		public String evento;
		public String contenido;
		public Boolean habilitado;
	}

	public static BBVistaBuhobank buscarId(BBVistasBuhobank vistas, Integer id) {

		if (vistas == null || Utils.isEmpty(id)) {
			return null;
		}

		for (BBVistaBuhobank vista : vistas) {

			if (id.equals(vista.id)) {
				return vista;
			}
		}

		return null;
	}

	public static BBVistaBuhobank buscarCodigo(BBVistasBuhobank vistas, String codigoVista) {

		if (vistas == null || Utils.isEmpty(codigoVista)) {
			return null;
		}

		for (BBVistaBuhobank vista : vistas) {

			if (codigoVista.equals(vista.codigo_vista)) {
				return vista;
			}
		}

		return null;
	}

	public static BBVistasBuhobank buscarIdPlantilla(BBVistasBuhobank vistas, Integer idPlantilla) {

		if (vistas == null || Utils.isEmpty(idPlantilla)) {
			return null;
		}

		BBVistasBuhobank vistaById = new BBVistasBuhobank();

		for (BBVistaBuhobank vista : vistas) {

			if (idPlantilla.equals(vista.id_plantilla_flujo)) {
				vistaById.add(vista);
			}
		}

		return vistaById;
	}

	public static BBVistasBuhobank obtenerHabilitados(BBVistasBuhobank vistas) {

		BBVistasBuhobank vistasHabilitadas = new BBVistasBuhobank();

		for (BBVistaBuhobank vista : vistas) {
			if (vista.habilitado) {
				vistasHabilitadas.add(vista);
			}
		}

		return vistasHabilitadas;
	}

	public static Integer calcularOrden(BBVistasBuhobank vistas, String codigoVista) {

		if (vistas == null || Utils.isEmpty(codigoVista)) {
			return null;
		}

		Integer orden = 1;

		for (BBVistaBuhobank vista : vistas) {

			if (codigoVista.equals(vista.codigo_vista)) {
				return orden;
			}

			orden++;
		}

		return null;
	}

	public static BBVistaBuhobank obtenerSiguienteVista(ContextoBB contexto, BBVistasBuhobank vistas, String vistaActual) {

		if (vistas == null || vistas.size() == 0) {
			return null;
		}

		if (Utils.isEmpty(vistaActual)) {
			for (BBVistaBuhobank vista : vistas) {
				if (vista.habilitado) {
					return vista;
				}
			}
		}

		Boolean posVista = false;

		for (int i = 0; i < vistas.size(); i++) {

			BBVistaBuhobank vista = vistas.get(i);
			if (posVista) {

				if (vista.habilitado && validarSiguienteVista(contexto, vistas, vista)) {
					return vista;
				}
			} else {

				if (vistaActual.equals(vista.codigo_vista)) {

					posVista = true;
					if (i == vistas.size() - 1) {
						return null;
					}
				}
			}
		}

		return null;
	}

	public static Boolean validarSiguienteVista(ContextoBB contexto, BBVistasBuhobank vistas, BBVistaBuhobank vista) {

		SesionBB sesion = contexto.sesion();

		if (EstadosBB.RETOMA_SESION.equals(sesion.estado) || !BBPersona.datosPersonalesVacios(contexto)) {

			if (vista.retoma_sesion) {
				return false;
			}
		}

		if (V_COMPLETAR_DOM_LEGAL_00.equals(vista.codigo_vista) && !BBFormaEntrega.datosDomicilioVacios(sesion.domicilioLegal)) {
			return false;
		}

		if (V_GUARDAR_CONYUGE_00.equals(vista.codigo_vista) && !sesion.casada()) {
			return false;
		}

		if (V_ELEGIR_DOM_ENVIO_00.equals(vista.codigo_vista) && sesion.tieneDomVirtual()) {
			return false;
		}

		if (V_FLUJO_VU_00.equals(vista.codigo_vista) && BBSeguridad.validacionRenaperPorNroTramite(contexto)) {
			return false;
		}

		if (V_TYC_00.equals(vista.codigo_vista) && sesion.esAndroid() && BBSeguridad.validacionRenaperPorNroTramite(contexto)) {
			return false;
		}

		if (V_SCANNER_DNI_00.equals(vista.codigo_vista) && sesion.estadoVUOK()) {
			return false;
		}

		if (V_VALIDACION_SMS_00.equals(vista.codigo_vista) && BBValidacion.tieneCelularOtpValidado(contexto)){

			BBVistaBuhobank vistaValidacionSmsOK = buscarCodigo(vistas, V_VALIDACION_SMS_OK_00);
			if (vistaValidacionSmsOK == null || !vistaValidacionSmsOK.habilitado) {
				sesion.telefonoOtpValidado = true;
				sesion.save();
			}

			return false;
		}

		if (V_VALIDACION_SMS_OK_00.equals(vista.codigo_vista) && BBValidacion.tieneCelularOtpValidado(contexto) && (sesion.telefonoOtpValidado == null || !sesion.telefonoOtpValidado)) {

			sesion.telefonoOtpValidado = true;
			sesion.save();
			return false;
		}

		if (V_GUARDAR_NACIONALIDAD_00.equals(vista.codigo_vista) && BBSeguridad.tieneNacionalidad(contexto)) {
			return false;
		}

		if (V_COMPLETAR_DOM_POSTAL_00.equals(vista.codigo_vista) && (!GeneralBB.ENTREGA_DOM_ALTERNATIVO.equals(sesion.formaEntrega) || sesion.tieneDomVirtual())) {
			return false;
		}

		if (V_CREAR_CLAVE_00.equals(vista.codigo_vista) && (EstadosBB.BATCH_CORRIENDO.equals(sesion.estado) || BBSeguridad.tieneClaveActivaBool(contexto, sesion.cuil))) {
			return false;
		}

		if (V_TIPO_TARJETA_00.equals(vista.codigo_vista) && !sesion.esCGU() && !sesion.getCheckTdFisica()) {
			return false;
		}
		
		if (V_TIPO_TARJETA_00.equals(vista.codigo_vista) && sesion.esCGU() && !sesion.getCheckTdFisicaCgu()) {
			return false;
		}

		if (V_ELEGIR_CUENTA_SUELDO_00.equals(vista.codigo_vista) && !sesion.getCheckCuentaSueldo()) {
			return false;
		}
		
		if(V_VALIDACION_EMAIL_00.equals(vista.codigo_vista) && BBValidacion.tieneMailOtpValidado(contexto) && (sesion.emailOtpValidado == null || !sesion.emailOtpValidado)) {
			sesion.emailOtpValidado = true;
			sesion.save();
			return false;
		}

		return true;
	}

	private static Object[] obtenerParametros(Contexto contexto, BBVistaBuhobank vista, int cantidad) {

		Object[] parametros = new Object[cantidad];

		parametros[0] = !Util.empty(vista.id_plantilla_flujo) ? vista.id_plantilla_flujo : null;
		parametros[1] = !Util.empty(vista.orden_vista) ? vista.orden_vista : null;
		parametros[2] = !Util.empty(vista.codigo_vista) ? vista.codigo_vista : null;
		parametros[3] = !Util.empty(vista.retoma_sesion) ? vista.retoma_sesion : null;
		parametros[4] = !Util.empty(vista.contenido) ? vista.contenido : null;
		parametros[5] = !Util.empty(vista.habilitado) ? vista.habilitado : null;

		return parametros;
	}

	/* ========== SERVICIO ========== */
	public static BBVistasBuhobank get(Contexto contexto) {

		String sql = "";
		sql += "SELECT * ";
		sql += "FROM [dbo].[bb_vistas] WITH (NOLOCK) ";
		sql += "ORDER BY id_plantilla_flujo, orden_vista ASC ";

		Objeto datos = Sql.select(contexto, SqlBuhoBank.SQL, sql);
		return map(datos, BBVistasBuhobank.class, BBVistaBuhobank.class);
	}

	public static Boolean post(Contexto contexto, BBVistaBuhobank nuevaVista) {

		String sql = "";
		sql += "INSERT INTO [dbo].[bb_vistas] ";
		sql += "([id_plantilla_flujo], [orden_vista], [codigo_vista], [retoma_sesion], [contenido], [habilitado], ";
		sql += "[fecha_ultima_modificacion]) ";
		sql += "VALUES (?, ?, ?, ?, ?, ?, GETDATE())";

		Object[] parametros = obtenerParametros(contexto, nuevaVista, 6);

		return Sql.update(contexto, SqlBuhoBank.SQL, sql, parametros) > 0;
	}

	public static Boolean put(Contexto contexto, BBVistaBuhobank vista) {

		String sql = "";
		sql += "UPDATE [dbo].[bb_vistas] SET ";
		sql += "id_plantilla_flujo = ? ,";
		sql += "orden_vista = ? ,";
		sql += "codigo_vista = ? ,";
		sql += "retoma_sesion = ? ,";
		sql += "contenido = ? ,";
		sql += "habilitado = ? ,";
		sql += "fecha_ultima_modificacion = GETDATE() ";
		sql += "WHERE id = ? ";

		Object[] parametros = obtenerParametros(contexto, vista, 7);
		parametros[6] = vista.id;

		return Sql.update(contexto, SqlBuhoBank.SQL, sql, parametros) > 0;
	}

	public static Boolean delete(Contexto contexto, Integer id) {

		String sql = "";
		sql += "DELETE [dbo].[bb_vistas] ";
		sql += "WHERE id = ? ";

		return Sql.update(contexto, SqlBuhoBank.SQL, sql, id) > 0;
	}

	public static Boolean clonar(Contexto contexto, Integer idPlantillaBase, Integer idPlantilla) {

		String sql = "";
		sql += "INSERT [dbo].[bb_vistas] (id_plantilla_flujo, orden_vista, codigo_vista, retoma_sesion, contenido, habilitado, fecha_ultima_modificacion) ";
		sql += "SELECT ? AS id_plantilla_flujo, orden_vista, codigo_vista, retoma_sesion, contenido, habilitado, GETDATE() AS fecha_ultima_modificacion ";
		sql += "FROM [dbo].[bb_vistas] WITH (NOLOCK) ";
		sql += "WHERE id_plantilla_flujo = ? ";

		return Sql.update(contexto, SqlBuhoBank.SQL, sql, idPlantilla, idPlantillaBase) > 0;
	}

}
