package ar.com.hipotecario.backend.servicio.sql.esales;

import java.math.BigDecimal;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.base.Util;
import ar.com.hipotecario.backend.conector.sql.Sql;
import ar.com.hipotecario.backend.conector.sql.SqlObjeto;
import ar.com.hipotecario.backend.conector.sql.SqlObjetos;
import ar.com.hipotecario.backend.exception.SqlException;
import ar.com.hipotecario.backend.servicio.sql.SqlEsales;
import ar.com.hipotecario.backend.servicio.sql.esales.SesionesEsales.SesionEsales;
import ar.com.hipotecario.backend.servicio.sql.esales.SesionesEsalesBB2.SesionEsalesBB2;
import ar.com.hipotecario.backend.util.Validadores;
import ar.com.hipotecario.canal.buhobank.GeneralBB;
import ar.com.hipotecario.canal.buhobank.SesionBB;
import ar.com.hipotecario.canal.buhobank.SesionBB.ConyugeBB;
import ar.com.hipotecario.canal.buhobank.SesionBB.TarjetaOfertaBB;

@SuppressWarnings("serial")
public class SesionesEsalesBB2 extends SqlObjetos<SesionEsalesBB2> {

	/* ========== ATRIBUTOS ========== */
	public static class SesionEsalesBB2 extends SqlObjeto {
		public String id;
		public String sesion_id;
		public Fecha conyuge_fecha_nacimiento;
		public String conyuge_nacionalidad_id;
		public String conyuge_pais_residencia_id;
		public Integer tarj_oferta_marca;
		public String tarj_oferta_distribucion_desc;
		public Integer tarj_oferta_producto;
		public Integer tarj_oferta_grupo;
		public String tarj_oferta_afinidad_desc;
		public Integer tarj_oferta_modelo_liquidacion;
		public BigDecimal tc_limite;
		public Boolean telefono_otp_validado;
		public Boolean email_otp_validado;
		public String token_firebase;
		public String plataforma;
		public Boolean motor_indicador;
		public String bb_inversor_aceptada;
		public String estado_caja_usd;
		public String usuario_vu;
		public String tipo_sit_laboral;
		public String id_dispositivo;
		public String sucursal_onboarding;
		public BigDecimal latitud;
		public BigDecimal longitud;
		public String operation_vu;
		public Boolean es_expuesta_politica;
		public Boolean es_sujeto_obligado;
		public Boolean es_fatca_ocde;
		public Boolean lavado_dinero;
		public String version_plataforma;
		public Boolean td_virtual;
		public Boolean tc_virtual;
		public String cuil_referido;
		public String adjust_adid;
		public String adjust_gps_adid;
		public String requiereEmbozado;

		/* ========== METODOS ========== */
		public String getFlujo() {

			if (Util.empty(sucursal_onboarding) || !sucursal_onboarding.contains("|")) {
				return null;
			}

			String[] sucursalOnboardingList = sucursal_onboarding.split("\\|");
			if (sucursalOnboardingList.length <= 1) {
				return null;
			}

			return sucursalOnboardingList[0];
		}

		public Boolean esFlujoLibertad() {
			return GeneralBB.FLUJO_LIBERTAD.equals(getFlujo());
		}
	}

	/* ========== SERVICIO ========== */
	public static SesionEsalesBB2 get(Contexto contexto, String id) {
		String sql = "";
		sql += "SELECT * ";
		sql += "FROM [esales].[dbo].[Sesion_Esales_BB2] WITH (NOLOCK) ";
		sql += "WHERE id = ?";

		Objeto datos = Sql.select(contexto, SqlEsales.SQL, sql, id);
		SqlException.throwIf("SESION_BB2_NO_ENCONTRADA", datos.isEmpty());
		return map(datos, SesionesEsalesBB2.class, SesionEsalesBB2.class).first();
	}

	public static SesionesEsalesBB2 getCuil(Contexto contexto, String cuil, Fecha fechaDesde) {
		String sql = "";
		sql += "SELECT t2.* ";
		sql += "FROM [esales].[dbo].[Sesion] AS t1 WITH (NOLOCK) ";
		sql += "INNER JOIN [esales].[dbo].[Sesion_Esales_BB2] AS t2 WITH (NOLOCK) ";
		sql += "ON t1.id = t2.sesion_id ";
		sql += "WHERE t1.fecha_inicio >= ? ";
		sql += "AND t1.cuil = ? ";
		sql += "ORDER BY t1.id DESC ";

		Objeto datos = Sql.select(contexto, SqlEsales.SQL, sql, fechaDesde, cuil);
		return map(datos, SesionesEsalesBB2.class, SesionEsalesBB2.class);
	}

	public static SesionEsalesBB2 getId(Contexto contexto, String id) {
		return getId(contexto, id, true);
	}

	public static SesionEsalesBB2 getId(Contexto contexto, String id, Boolean validarExistencia) {
		String sql = "";
		sql += "SELECT * ";
		sql += "FROM [esales].[dbo].[Sesion_Esales_BB2] WITH (NOLOCK) ";
		sql += "WHERE sesion_id = ?";

		Objeto datos = Sql.select(contexto, SqlEsales.SQL, sql, id);
		SqlException.throwIf("SESION_BB2_NO_ENCONTRADA", datos.isEmpty() && validarExistencia);
		if (datos.isEmpty() && !validarExistencia)
			return null;
		return map(datos, SesionesEsalesBB2.class, SesionEsalesBB2.class).first();
	}

	public static Object[] obtenerParametros(Contexto contexto, String esalesId, SesionBB sesion, Integer cantidad) {
		Object[] parametros = new Object[cantidad];

		ConyugeBB conyuge = sesion.conyuge;
		// [conyuge_fecha_nacimiento], [conyuge_nacionalidad_id],
		// [conyuge_pais_residencia_id]
		parametros[0] = !Util.empty(conyuge) && !Util.empty(conyuge.fechaNacimiento) ? conyuge.fechaNacimiento : null;
		parametros[1] = !Util.empty(conyuge) ? Validadores.nullString(conyuge.idNacionalidad) : null;
		parametros[2] = !Util.empty(conyuge) ? Validadores.nullString(conyuge.idPaisResidencia) : null;

		TarjetaOfertaBB tarjetaOferta = sesion.tarjetaOferta;
		// [tarj_oferta_marca], [tarj_oferta_distribucion_desc], [tarj_oferta_producto]
		parametros[3] = !Util.empty(tarjetaOferta) && tarjetaOferta.marca != null ? tarjetaOferta.marca : 0;
		parametros[4] = !Util.empty(tarjetaOferta) ? Validadores.nullString(tarjetaOferta.distribucionDesc) : null;
		parametros[5] = !Util.empty(tarjetaOferta) && tarjetaOferta.producto != null ? tarjetaOferta.producto : 0;

		// [tarj_oferta_grupo], [tarj_oferta_afinidad_desc],
		// [tarj_oferta_modelo_liquidacion]
		parametros[6] = !Util.empty(tarjetaOferta) && tarjetaOferta.grupo != null ? tarjetaOferta.grupo : 0;
		parametros[7] = !Util.empty(tarjetaOferta) ? Validadores.nullString(tarjetaOferta.afinidadDesc) : null;
		parametros[8] = !Util.empty(tarjetaOferta) && tarjetaOferta.modLiq != null ? tarjetaOferta.modLiq : 0;

		// [tc_limite], [telefono_otp_validado]
		parametros[9] = sesion.limite != null ? String.valueOf(sesion.limite) : null;
		parametros[10] = sesion.telefonoOtpValidado != null ? sesion.telefonoOtpValidado : 0;

		// [emailOtpValidado] [tokenFirebase] [plataforma] [motorIndicador]
		parametros[11] = sesion.emailOtpValidado != null ? sesion.emailOtpValidado : 0;
		parametros[12] = sesion.tokenFirebase != null ? sesion.tokenFirebase : null;
		parametros[13] = sesion.plataforma != null ? sesion.plataforma.toUpperCase() : null;
		parametros[14] = sesion.motorIndicador != null ? sesion.motorIndicador : null;

		// [bbInversorAceptada] [bbInversorPresentaDoc] [estadoCajaUsd] [usuarioVU]
		parametros[15] = sesion.bbInversorAceptada != null ? sesion.bbInversorAceptada : null;
		parametros[16] = null;
		parametros[17] = sesion.estadoCajaUsd;
		parametros[18] = sesion.usuarioVU != null ? sesion.usuarioVU : null;

		// [usuarioLibertad] [usuarioLibertad] [sesion_id]
		parametros[19] = sesion.tipoSitLaboral;
		parametros[20] = sesion.idDispositivo != null ? sesion.idDispositivo : null;
		parametros[21] = sesion.sucursalOnboarding != null ? sesion.sucursalOnboarding : null;
		parametros[22] = sesion.latitud != null ? sesion.latitud : null;
		parametros[23] = sesion.longitud != null ? sesion.longitud : null;
		parametros[24] = sesion.operationVU != null ? sesion.operationVU : null;
		parametros[25] = sesion.esExpuestaPolitica != null ? sesion.esExpuestaPolitica : null;
		parametros[26] = sesion.esSujetoObligado != null ? sesion.esSujetoObligado : null;
		parametros[27] = sesion.esFatcaOcde != null ? sesion.esFatcaOcde : null;
		parametros[28] = sesion.lavadoDinero != null ? sesion.lavadoDinero : null;
		parametros[29] = sesion.versionPlataforma != null ? sesion.versionPlataforma : null;
		parametros[30] = sesion.tdVirtual != null ? sesion.tdVirtual : null;
		parametros[31] = sesion.tcVirtual != null ? sesion.tcVirtual : null;
		parametros[32] = sesion.cuilReferido != null ? sesion.cuilReferido : null;
		parametros[33] = sesion.adjustAdid != null ? sesion.adjustAdid : null;
		parametros[34] = sesion.adjustGpsAdid != null ? sesion.adjustGpsAdid : null;
		parametros[35] = sesion.tdFisica != null ? sesion.tdFisica : GeneralBB.VISUALIZA_N;
		parametros[36] = sesion.codeVU;
		parametros[37] = esalesId;

		return parametros;
	}

	public static Boolean crearSesion(Contexto contexto, String esalesId, SesionBB sesion) {
		String sql = "";
		sql += "INSERT INTO [esales].[dbo].[Sesion_Esales_BB2] ";
		sql += "([conyuge_fecha_nacimiento], [conyuge_nacionalidad_id], [conyuge_pais_residencia_id], ";
		sql += "[tarj_oferta_marca], [tarj_oferta_distribucion_desc], [tarj_oferta_producto], ";
		sql += "[tarj_oferta_grupo], [tarj_oferta_afinidad_desc], [tarj_oferta_modelo_liquidacion], ";
		sql += "[tc_limite], [telefono_otp_validado], [email_otp_validado], [token_firebase], [plataforma],";
		sql += "[motor_indicador], [bb_inversor_aceptada], [bb_inversor_presenta_doc], [estado_caja_usd],";
		sql += "[usuario_vu],[tipo_sit_laboral], [id_dispositivo], [sucursal_onboarding], [latitud], [longitud], [operation_vu],";
		sql += "[es_expuesta_politica], [es_sujeto_obligado], [es_fatca_ocde], [lavado_dinero], [version_plataforma], [td_virtual], [tc_virtual], [cuil_referido], [adjust_adid], [adjust_gps_adid], [requiereEmbozado], [code_vu], ";
		sql += "[sesion_id])";
		sql += "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		Object[] parametros = obtenerParametros(contexto, esalesId, sesion, 38);

		return Sql.update(contexto, SqlEsales.SQL, sql, parametros) == 1;
	}

	public static Boolean actualizarSesion(Contexto contexto, String esalesId, SesionBB sesion) {
		String sql = "";
		sql += "UPDATE [esales].[dbo].[Sesion_Esales_BB2] SET ";
		sql += "[conyuge_fecha_nacimiento] = ? , ";
		sql += "[conyuge_nacionalidad_id] = ? , ";
		sql += "[conyuge_pais_residencia_id] = ? , ";
		sql += "[tarj_oferta_marca] = ? , ";
		sql += "[tarj_oferta_distribucion_desc] = ? , ";
		sql += "[tarj_oferta_producto] = ? , ";
		sql += "[tarj_oferta_grupo] = ? , ";
		sql += "[tarj_oferta_afinidad_desc] = ? , ";
		sql += "[tarj_oferta_modelo_liquidacion] = ? , ";
		sql += "[tc_limite] = ? , ";
		sql += "[telefono_otp_validado] = ? , ";
		sql += "[email_otp_validado] = ? , ";
		sql += "[token_firebase] = ? , ";
		sql += "[plataforma] = ? , ";
		sql += "[motor_indicador] = ? , ";
		sql += "[bb_inversor_aceptada] = ? , ";
		sql += "[bb_inversor_presenta_doc] = ? , ";
		sql += "[estado_caja_usd] = ? , ";
		sql += "[usuario_vu] = ? ,";
		sql += "[tipo_sit_laboral] = ? ,";
		sql += "[id_dispositivo] = ? ,";
		sql += "[sucursal_onboarding] = ? ,";
		sql += "[latitud] = ? ,";
		sql += "[longitud] = ? ,";
		sql += "[operation_vu] = ? ,";
		sql += "[es_expuesta_politica] = ? ,";
		sql += "[es_sujeto_obligado] = ? ,";
		sql += "[es_fatca_ocde] = ? ,";
		sql += "[lavado_dinero] = ? ,";
		sql += "[version_plataforma] = ? ,";
		sql += "[td_virtual] = ? ,";
		sql += "[tc_virtual] = ? ,";
		sql += "[cuil_referido] = ? ,";
		sql += "[adjust_adid] = ? ,";
		sql += "[adjust_gps_adid] = ? ,";
		sql += "[requiereEmbozado] = ? , ";
		sql += "[code_vu] = ? ";
		sql += "WHERE sesion_id = ? ";
		Object[] parametros = obtenerParametros(contexto, esalesId, sesion, 38);

		return Sql.update(contexto, SqlEsales.SQL, sql, parametros) == 1;
	}

	public static Boolean actualizarMotorIndicador(Contexto contexto, String sesionId, int valor) {
		String sql = "";
		sql += "UPDATE [dbo].[Sesion_Esales_BB2] ";
		sql += "SET motor_indicador = ? ";
		sql += "WHERE sesion_id = ? ";

		return Sql.update(contexto, SqlEsales.SQL, sql, valor, sesionId) == 1;
	}

	public static Boolean actualizarEstadoBBInversor(Contexto contexto, String id, String estado) {

		String sql = "";
		sql += "UPDATE [dbo].[Sesion_Esales_BB2] ";
		sql += "SET bb_inversor_aceptada = ? ";
		sql += "WHERE sesion_id = ? ";

		return Sql.update(contexto, SqlEsales.SQL, sql, estado, id) == 1;
	}

	public static Boolean actualizarOtps(Contexto contexto, String id, Boolean otpTelefono, Boolean otpMail) {

		String sql = "";
		sql += "UPDATE Sesion_Esales_BB2 ";
		sql += "SET telefono_otp_validado = ?, email_otp_validado = ? ";
		sql += "WHERE id = ? ";

		return Sql.update(contexto, SqlEsales.SQL, sql, otpTelefono, otpMail, id) == 1;
	}

	/* ========== METODOS ========== */
	public static Boolean guardarSesionBB2(Contexto contexto, SesionBB sesion) {
		SesionEsales sesionEsales = SesionesEsales.getPorToken(contexto, sesion.token);
		if (Util.empty(sesionEsales))
			return false;
		String esalesId = sesionEsales.id;

		SesionEsalesBB2 sesionEsalesBB2 = getId(contexto, esalesId, false);
		Boolean fueGuardado = false;
		if (sesionEsalesBB2 == null) {
			fueGuardado = crearSesion(contexto, esalesId, sesion);
		} else {
			fueGuardado = actualizarSesion(contexto, esalesId, sesion);
		}
		return fueGuardado;
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("BB", "desarrollo");
		String test = "get";

		if ("guardar".equals(test)) {
			String token = "gagAqWrR3utiP5O/HmJ4hUwoojGaLUIg0gcdL3H+h3gfqHnx8AAtfEMeY7fvE0JC8iXaHH/+NJWAXkb1u8gLTx5WR0gkROVQ";

			SesionBB sesion = new SesionBB();
			sesion.token = token;

			ConyugeBB conyuge = new ConyugeBB();
			conyuge.fechaNacimiento = Fecha.ahora().restarAños(40);
			conyuge.idPaisResidencia = "80";
			conyuge.idNacionalidad = "80";

			TarjetaOfertaBB tarjetaOferta = new TarjetaOfertaBB();
			tarjetaOferta.marca = GeneralBB.MARCA_TC_DUENIOS;
			tarjetaOferta.afinidadDesc = "1234";
			tarjetaOferta.distribucionDesc = "875";
			tarjetaOferta.grupo = 1029;
			tarjetaOferta.modLiq = 74;
			tarjetaOferta.producto = 541;

			BigDecimal limiteTC = new BigDecimal("23401.58");

			sesion.conyuge = conyuge;
			sesion.tarjetaOferta = tarjetaOferta;
			sesion.limite = limiteTC;

			Boolean fueGuardado = guardarSesionBB2(contexto, sesion);
			imprimirResultado(contexto, fueGuardado);
		}

		if ("getId".equals(test)) {
			String id = "368310";
			SesionEsalesBB2 dato = getId(contexto, id);
			imprimirResultado(contexto, dato);
		}

		if ("get".equals(test)) {
			String id = "3";
			SesionEsalesBB2 dato = get(contexto, id);
			imprimirResultado(contexto, dato);
		}
	}

}
