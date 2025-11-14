package ar.com.hipotecario.canal.buhobank;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.Gson;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Base;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.exception.SqlException;
import ar.com.hipotecario.backend.servicio.sql.SqlBuhoBank;

public class LogBB extends Base {

	private static Logger log = LoggerFactory.getLogger(LogBB.class);
	
	/* ========== CONSTANTES ========== */
	public static String PREFIJO = GeneralBB.CANAL_CODIGO;
	public static String ERROR_DEFAULT = "ERROR";
	public static String ERROR_API = "ERROR_API";
	public static String ERROR_SQL = "ERROR_SQL";
	public static String ERROR_GENERICO = "ERROR_CRITICO";

	/* ========== LOG ========== */
	protected static class Log {
		private static Gson gson = new Gson();

		public String canal;
		public String cuit;
		public String endpoint;
		public String evento;
		public String datos;
		public String error;
		public String idProceso;

		public String toString() {
			return gson.toJson(this);
		}
	}

	protected static void log(Contexto contexto, String cuit, String endpoint, String evento, String datos, String error, String idProceso) {
		Log logObj = new Log();
		logObj.canal = GeneralBB.CANAL_CODIGO;
		logObj.cuit = cuit;
		logObj.endpoint = endpoint;
		logObj.evento = evento;
		logObj.datos = datos;
		logObj.error = error;
		logObj.idProceso = idProceso;

		String logStr = Fecha.ahora().string("[HH:mm:ss] ") + logObj.toString();
		if (!empty(error)) {
			LogBB.log.error(logStr);
			return;
		}
		LogBB.log.info(logStr);
	}

	/* ========== METODOS ========== */
	public static String nombre(String nombre) {
		if (nombre == null || nombre.isEmpty())
			return PREFIJO + "_" + ERROR_DEFAULT;
		return PREFIJO + "_" + nombre;
	}

	public static String nombre() {
		return nombre("");
	}

	public static void evento(ContextoBB contexto, String evento) {
		evento(contexto, evento, (Objeto) null);
	}

	public static void evento(ContextoBB contexto, String evento, ApiObjeto datos) {
		evento(contexto, evento, datos != null ? datos.objeto() : null);
	}

	public static void evento(ContextoBB contexto, String evento, Objeto datos) {
		try {
			SesionBB sesion = contexto.sesion();
			String cuit = sesion.cuil;
			if (cuit != null) {

				String endpoint = contexto.path();
				String eventoStr = nombre(evento);
				String datosJson = datos != null ? datos.toSimpleJson() : null;
				String error = null;
				String idProceso = sesion.idSolicitud;

				if (contexto.prendidoLogsBase()) {
					SqlBuhoBank.logBB(contexto, cuit, endpoint, eventoStr, datosJson, error, idProceso);
				}

				log(contexto, cuit, endpoint, eventoStr, datosJson, error, idProceso);
			}

		} catch (Exception ex) {
		}
	}

	public static void eventoHomo(ContextoBB contexto, String datos) {
		if (!contexto.esProduccion()){
			evento(contexto, "EVENTO_HOMO", datos);
		}
	}

	public static void evento(ContextoBB contexto, String evento, String datos) {
		SesionBB sesion = contexto.sesion();
		registrarEvento(contexto, evento, datos, sesion.cuil, sesion.idSolicitud);
	}

	public static void evento(ContextoBB contexto, String evento, String datos, String cuit) {

		SesionBB sesion = contexto.sesion();
		String cuilStr = !empty(sesion.cuil) ? sesion.cuil : cuit;
		registrarEvento(contexto, evento, datos, cuilStr, sesion.idSolicitud);
	}

	public static void registrarEvento(ContextoBB contexto, String evento, String datos, String cuit, String idSolicitud) {

		try {

			if (cuit == null) {
				cuit = "-1";
			}

			String endpoint = contexto.path();
			String eventoStr = nombre(evento);
			String error = null;
			String idProceso = idSolicitud;

			if (contexto.prendidoLogsBase()) {
				SqlBuhoBank.logBB(contexto, cuit, endpoint, eventoStr, datos, error, idProceso);
			}
			log(contexto, cuit, endpoint, eventoStr, datos, error, idProceso);

		} catch (Exception ex) {
		}
	}

	public static void error(ContextoBB contexto, String evento, ApiObjeto datos) {
		error(contexto, evento, datos != null ? datos.objeto() : null);
	}

	public static void error(ContextoBB contexto, String estado) {
		try {

			SesionBB sesion = contexto.sesion();

			String errorImpuesto = "NO EXISTE CATEGORIA DEL IMPUESTO";
			if (estado.contains(errorImpuesto)) {
				estado = "error: " + errorImpuesto + " | idCobis: " + sesion.idCobis + " | estadoCivil: " + sesion.idEstadoCivil + " | ofertaElegida: " + sesion.ofertaElegida;
			}

			String cuit = sesion.cuil;
			if (cuit != null) {
				String endpoint = contexto.path();
				String evento = nombre();
				String datos = null;
				String error = estado;

				String idProceso = sesion.idSolicitud;

				if (contexto.prendidoLogsBase()) {
					SqlBuhoBank.logBB(contexto, cuit, endpoint, evento, datos, error, idProceso);
				}
				log(contexto, cuit, endpoint, evento, datos, error, idProceso);
			}

		} catch (Exception ex) {
		}
	}

	public static void error(ContextoBB contexto, String estado, Objeto datos) {
		try {
			SesionBB sesion = contexto.sesion();
			String cuit = sesion.cuil;
			if (cuit != null) {
				String endpoint = contexto.path();
				String evento = nombre();
				String datosJson = datos != null ? datos.toSimpleJson() : null;
				String error = estado;

				String idProceso = sesion.idSolicitud;

				if (contexto.prendidoLogsBase()) {
					SqlBuhoBank.logBB(contexto, cuit, endpoint, evento, datosJson, error, idProceso);
				}
				log(contexto, cuit, endpoint, evento, datosJson, error, idProceso);
			}
		} catch (Exception ex) {
		}
	}

	public static void error(ContextoBB contexto, String estado, String datos) {
		try {
			SesionBB sesion = contexto.sesion();
			String cuit = sesion.cuil;
			if (cuit != null) {
				String endpoint = contexto.path();
				String evento = nombre();
				String error = estado;

				String idProceso = sesion.idSolicitud;

				if (contexto.prendidoLogsBase()) {
					SqlBuhoBank.logBB(contexto, cuit, endpoint, evento, datos, error, idProceso);
				}
				log(contexto, cuit, endpoint, evento, datos, error, idProceso);
			}
		} catch (Exception ex) {
		}
	}

	public static void error(ContextoBB contexto, ApiException e) {
		try {
			SesionBB sesion = contexto.sesion();
			String cuit = sesion.cuil;
			if (cuit != null) {
				String endpoint = contexto.path();
				String evento = nombre(ERROR_API);

				String datos = String.format("%s:%s:%s", e.request.api(), e.request.metodo(), e.request.url());

				String codigoError = e.codigoError;
				String codigoApi = e.response.string("codigo");
				String mensaje = e.response.string("mensajeAlUsuario", null);
				String error = mensaje != null ? String.format("%s | %s | %s", codigoError, codigoApi, mensaje) : e.response.body;

				String idProceso = e.request.handle();

				if (contexto.prendidoLogsBase()) {
					SqlBuhoBank.logBB(contexto, cuit, endpoint, evento, datos, error, idProceso);
				}
				log(contexto, cuit, endpoint, evento, datos, error, idProceso);
			}
		} catch (Exception ex) {
		}
	}

	public static void error(ContextoBB contexto, SqlException e) {
		try {
			SesionBB sesion = contexto.sesion();
			String cuit = sesion.cuil;
			if (cuit != null) {
				String endpoint = contexto.path();
				String evento = nombre(ERROR_SQL);

				if (e == null) {

					if (contexto.prendidoLogsBase()) {
						SqlBuhoBank.logBB(contexto, cuit, endpoint, evento, null, "400", null);
					}
					log(contexto, cuit, endpoint, evento, null, "400", null);
					return;
				}

				String datos = e.query != null && e.parametros != null ? String.format("%s | %s", e.query, e.parametros) : e.query;

				String codigoError = e.codigoError;
				String mensaje = e.getMessage();
				String error = mensaje != null ? String.format("%s | %s", codigoError, mensaje) : codigoError;

				String idProceso = null;

				if (contexto.prendidoLogsBase()) {
					SqlBuhoBank.logBB(contexto, cuit, endpoint, evento, datos, error, idProceso);
				}
				log(contexto, cuit, endpoint, evento, datos, error, idProceso);
			}
		} catch (Exception ex) {
		}
	}

	public static void error(ContextoBB contexto, Exception e) {
		try {
			SesionBB sesion = contexto.sesion();
			String cuit = sesion.cuil;
			if (cuit != null) {
				String endpoint = contexto.path();
				String evento = nombre(ERROR_GENERICO);

				Throwable t = getCause(e);
				StackTraceElement st = stackTraceElement(t);
				String datos = st != null ? String.format("%s:%s:%s()", st.getFileName(), st.getLineNumber(), st.getMethodName()) : null;

				String exception = t.getClass().getSimpleName();
				String message = t.getMessage();
				String error = message != null ? String.format("%s: %s", exception, message) : exception;

				String idProceso = null;

				if (contexto.prendidoLogsBase()) {
					SqlBuhoBank.logBB(contexto, cuit, endpoint, evento, datos, error, idProceso);
				}
				log(contexto, cuit, endpoint, evento, datos, error, idProceso);
			}
		} catch (Exception ex) {
		}
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		ContextoBB contexto = new ContextoBB("BB", "homologacion", "1");

		String cuit = "27323898265";
		String endpoint = "/bb/api/guardaradicionales";
		String evento = "BB_GUARDAR_ADICIONALES_OK";
		String datos = "{\"estado\":\"0\",\"idEstadoCivil\":\"S\",\"idSituacionLaboral\":\"1\"}";
		String error = null;
		String idProceso = null;

		log(contexto, cuit, endpoint, evento, datos, error, idProceso);

		evento = "BB_ERROR_GUARDAR_ADICIONALES";
		error = "NullPointerException";
		idProceso = "12345678";

		log(contexto, cuit, endpoint, evento, datos, error, idProceso);

		String query = "UPDATE [esales].[dbo].[Sesion] SET [id_canal] = ? , [token] = ? , [ip] = ? , [cuil] = ? , [cuenta_corriente] = ? , [estado] = ? , [cobis_id] = ? , [id_solicitud_duenios] = ? , [resolucion_scoring] = ? , [resolucion_riesgo_net] = ? , [ticket_riesgo_net] = ? , [telefono_celular_ddn] = ? , [telefono_celular_caract] = ? , [telefono_celular_nro] = ? , [telefono_fijo_ddn] = ? , [telefono_fijo_caract] = ? , [telefono_fijo_nro] = ? , [documento_tipo_id] = ? , [documento_numero] = ? , [documento_version_id] = ? , [nivel_estudios] = ? , [localidad_id] = ? , [pais_residencia_id] = ? , [provincia_id] = ? , [localidad_descripcion] = ? , [nacionalidad_id] = ? , [pais_id] = ? , [fecha_nacimiento] = ? , [nombre] = ? , [apellido] = ? , [sexo] = ? , [mail] = ? , [ingreso_neto] = ? , [ciudad_nacimiento] = ? , [domicilio_calle] = ? , [domicilio_nro] = ? , [domicilio_piso] = ? , [domicilio_dpto] = ? , [domicilio_cp] = ? , [telefono_laboral_ddn] = ? , [telefono_laboral_caract] = ? , [telefono_laboral_nro] = ? , [estado_civil_id] = ? , [conyuge_nombre] = ? , [conyuge_apellido] = ? , [conyuge_cuil] = ? , [conyuge_sexo] = ? , [conyuge_documento_tipo_id] = ? , [conyuge_documento_version_id] = ? , [conyuge_documento_numero] = ? , [situacion_laboral_id] = ? , [pep] = ? , [pep_nivel2_id] = ? , [pep_relacion] = ? , [sujeto_obligado] = ? , [aceptacion_oferta] = ? , [ciudadano_eeuu] = ? , [cuil_tipo] = ? , [domicilio_ent_calle1] = ? , [domicilio_ent_calle2] = ? , [cantidad_nupcias_id] = ? , [cantidad_nupcias_desc] = ? , [subtipo_estado_civil_id] = ? , [subtipo_estado_civil_desc] = ? , [conyuge_cuil_tipo] = ? , [sucursal] = ? , [forma_entrega] = ? , [codigo_error] = ? , [respondio_preguntas] = ? , [apellido_uno_dos_car] = ? , [apellido_cony_uno_dos_car] = ? , [dom_barrio_envio] = ? , [dom_calle_envio] = ? , [dom_cp_envio] = ? , [dom_depto_envio] = ? , [dom_localidad_envio] = ? , [dom_numero_envio] = ? , [dom_piso_envio] = ? , [pais_nacimiento_id] = ? , [reside_desde] = ? , [situacion_vivienda] = ? , [aceptar_tyc] = ? , [domicilio_prov_id] = ? , [modo_aprobacion] = ? , [resolucion_explicacion] = ? , [nombre_un_car] = ? , [nombre_cony_un_car] = ? , [id_sesion_ob] = ? , [tipo_dispositivo] = ? , [referrer] = ? , [utm_source] = ? , [utm_medium] = ? , [utm_campaign] = ? , [utm_content] = ? , [client_id_analytics] = ? , [id_tipo_banca] = ? , [tipo_standalone] = ? , [is_standalone] = ? , [fecha_ultima_modificacion] = GETDATE() WHERE token = ? ";
		Object[] parametros = { 20, "hola", 202, "", 0L };
		System.out.println(String.format("%s | %s", query, parametros));
	}
}
