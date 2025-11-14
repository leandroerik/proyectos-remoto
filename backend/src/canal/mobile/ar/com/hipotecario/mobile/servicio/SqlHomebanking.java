package ar.com.hipotecario.mobile.servicio;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.mobile.ConfigMB;
import ar.com.hipotecario.mobile.conector.SqlMB;
import ar.com.hipotecario.mobile.conector.SqlRequestMB;
import ar.com.hipotecario.mobile.conector.SqlResponseMB;
import ar.com.hipotecario.mobile.lib.Fecha;
import ar.com.hipotecario.mobile.lib.Objeto;
import ar.com.hipotecario.mobile.negocio.EstadoBroker;

public class SqlHomebanking {

	/* ========== ULTIMA CONEXION ========== */
	public static Date fechaHoraUltimaConextion(String idCobis) {
		try {
			SqlRequestMB sqlRequest = SqlMB.request("SelectUltimaConexion", "homebanking");
			sqlRequest.sql = "SELECT [idCobis], [momento] FROM [Homebanking].[dbo].[ultima_conexion] WHERE [idCobis] = ?";
			sqlRequest.parametros.add(idCobis);
			SqlResponseMB sqlResponse = SqlMB.response(sqlRequest);
			for (Objeto registro : sqlResponse.registros) {
				return registro.date("momento");
			}
		} catch (Exception e) {
		}
		return null;
	}

	public static Boolean registrarFechaHoraUltimaConextion(String idCobis, String modo) {
		if ("tradicional".equals(modo)) {
			new Futuro<>(() -> registrarFechaHoraUltimaConextionTradicional(idCobis));
		} else if ("rowlock".equals(modo)) {
			new Futuro<>(() -> registrarFechaHoraUltimaConextionRowLock(idCobis));
		} else if ("partido".equals(modo)) {
			new Futuro<>(() -> registrarFechaHoraUltimaConextionPartido(idCobis));
		}
		return true;
	}

	public static Boolean registrarFechaHoraUltimaConextionTradicional(String idCobis) {
		try {
			SqlRequestMB sqlRequest = SqlMB.request("InsertOrUpdateUltimaConexion", "homebanking");
			sqlRequest.sql = "UPDATE [Homebanking].[dbo].[ultima_conexion] ";
			sqlRequest.sql += "SET [momento] = ? ";
			sqlRequest.sql += "WHERE [idCobis] = ? ";
			sqlRequest.add(new Date());
			sqlRequest.add(idCobis);

			sqlRequest.sql += "IF @@ROWCOUNT = 0 ";
			sqlRequest.sql += "INSERT INTO [Homebanking].[dbo].[ultima_conexion] ([idCobis], [momento]) ";
			sqlRequest.sql += "VALUES (?, ?) ";
			sqlRequest.add(idCobis);
			sqlRequest.add(new Date());

			SqlMB.response(sqlRequest);
		} catch (Exception e) {
		}
		return true;
	}

	public static Boolean registrarFechaHoraUltimaConextionRowLock(String idCobis) {
		try {
			SqlRequestMB sqlRequest = SqlMB.request("UpdateUltimaConexion", "homebanking");
			sqlRequest.sql = "UPDATE [Homebanking].[dbo].[ultima_conexion] WITH (ROWLOCK) ";
			sqlRequest.sql += "SET [momento] = ? ";
			sqlRequest.sql += "WHERE [idCobis] = ? ";
			sqlRequest.add(new Date());
			sqlRequest.add(idCobis);

			sqlRequest.sql += "IF @@ROWCOUNT = 0 ";
			sqlRequest.sql += "INSERT INTO [Homebanking].[dbo].[ultima_conexion] ([idCobis], [momento]) ";
			sqlRequest.sql += "VALUES (?, ?) ";
			sqlRequest.add(idCobis);
			sqlRequest.add(new Date());

			SqlMB.response(sqlRequest);
		} catch (Exception e) {
		}
		return true;
	}

	public static Boolean registrarFechaHoraUltimaConextionPartido(String idCobis) {
		try {
			SqlRequestMB sqlRequest = SqlMB.request("UpdateUltimaConexion", "homebanking");
			sqlRequest.sql = "UPDATE [Homebanking].[dbo].[ultima_conexion] WITH (ROWLOCK) ";
			sqlRequest.sql += "SET [momento] = ? ";
			sqlRequest.sql += "WHERE [idCobis] = ? ";
			sqlRequest.add(new Date());
			sqlRequest.add(idCobis);
			SqlMB.response(sqlRequest);
		} catch (Exception e) {
		}
		return true;
	}

	/* ========== SESION ========== */
	public static Boolean existeSesion(String idCobis, String fingerprint) {
		try {
			if (StringUtils.isNotBlank(idCobis)) {
				SqlRequestMB sqlRequest = SqlMB.request("InsertSesion", "homebanking");
				sqlRequest.sql = "SELECT * FROM [Homebanking].[dbo].[session] WHERE s_cod_cliente = ?";
				sqlRequest.parametros.add(idCobis);
				SqlResponseMB sqlResponse = SqlMB.response(sqlRequest);
				if (!sqlResponse.hayError) {
					for (Objeto registro : sqlResponse.registros) {
						Long momentoLogin = registro.date("s_login_timestamp").getTime();
						Long momentoFinSesion = momentoLogin + ConfigMB.integer("servidor_tiempo_sesion", 10 * 60) * 1000;
						Long momentoActual = new Date().getTime();
						if (momentoFinSesion < momentoActual) {
							eliminarSesion(idCobis);
						} else {
							if (fingerprint.equals(registro.string("s_fingerprint"))) {
								eliminarSesion(idCobis);
								return false;
							}
							return true;
						}
					}
				}
			}
			return false;
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean registrarSesion(String idCobis, String fingerprint, String idSesion) {
		try {
			if (!Objeto.empty(idCobis)) {
				SqlRequestMB sqlRequest = SqlMB.request("InsertSesion", "homebanking");
				sqlRequest.sql += "INSERT INTO [Homebanking].[dbo].[session] ";
				sqlRequest.sql += "([s_cod_cliente], [s_fingerprint], [s_login_timestamp], [s_canal], [redis_id]) ";
				sqlRequest.sql += "VALUES (?, ?, GETDATE(), 'MB', ? )";
				sqlRequest.parametros.add(idCobis);
				sqlRequest.parametros.add(fingerprint);
				sqlRequest.parametros.add(idSesion);
				SqlMB.response(sqlRequest);
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static void eliminarSesion(String idCobis) {
		try {
			if (StringUtils.isNotBlank(idCobis)) {
				SqlRequestMB sqlRequest = SqlMB.request("DeleteSesion", "homebanking");
				sqlRequest.sql += "DELETE FROM [Homebanking].[dbo].[session] WHERE s_cod_cliente = ?";
				sqlRequest.parametros.add(idCobis);
				SqlMB.response(sqlRequest);
			}
		} catch (Exception e) {
		}
	}

	/* ========== FRAUDE ========== */
	public static Boolean bloqueadoPorFraude(String idCobis) {
		try {
			if (StringUtils.isNotBlank(idCobis)) {
				SqlRequestMB sqlRequest = SqlMB.request("SelectFraude", "homebanking");
				sqlRequest.sql = "SELECT * FROM [homebanking].[dbo].[muestreo] WHERE m_subid = ?";
				sqlRequest.parametros.add(idCobis);
				SqlResponseMB sqlResponse = SqlMB.response(sqlRequest);
				if (!sqlResponse.hayError) {
					for (Objeto registro : sqlResponse.registros) {
						if ("deshabilitar.acceso.fraude".equals(registro.string("m_tipoMuestra"))) {
							if ("true".equals(registro.string("m_valor"))) {
								return true;
							}
						}
					}
				}
			}
			return false;
		} catch (Exception e) {
			return false;
		}
	}

	/* BLOQUE POR FRAUDE 90 DIAS */
	public static Objeto getTipoMuestreo(String idCobis, String tipoMuestra) {
		try {
			if (idCobis != null) {
				SqlRequestMB sqlRequest = SqlMB.request("SelectMuestreo", "homebanking");
				sqlRequest.sql = "SELECT * FROM [homebanking].[dbo].[muestreo] WHERE m_subid = ?";
				sqlRequest.parametros.add(idCobis);
				SqlResponseMB sqlResponse = SqlMB.response(sqlRequest);
				if (!sqlResponse.hayError) {
					for (Objeto registro : sqlResponse.registros) {
						if (tipoMuestra.equals(registro.string("m_tipoMuestra"))) {
							return registro;
						}
					}
				}
			}
			return null;
		} catch (Exception e) {
			return null;
		}
	}

	/* BLOQUEADO POR FRAUDE DESDE LINK */
	public static Boolean bloqueadoPorTransaccionFraudeLink(String idCobis) {
		String sql = "select * from [homebanking].[dbo].[muestreo] where m_subid = ? and m_tipoMuestra = ? and m_valor = ?";

		SqlRequestMB sqlRequest = SqlMB.request("SelectFraude", "homebanking");
		sqlRequest.sql = sql;
		sqlRequest.parametros.add(idCobis);
		sqlRequest.parametros.add("bloquear.acceso.fraude.link");
		sqlRequest.parametros.add("true");
		SqlResponseMB sqlResponse = SqlMB.response(sqlRequest);
		if (sqlResponse.hayError) {
			return false;
		}
		if (sqlResponse.registros.size() > 0) {
			return true;
		}
		return false;
	}

	public static List<Objeto> mostrarEncuestaUsuario(String idCobis, List<Objeto> preguntas, String funcionalidad) {

		if (idCobis != null && !preguntas.isEmpty()) {
			SqlRequestMB sqlRequest = SqlMB.request("SelectEncuestaUsuario", "homebanking");

			if (funcionalidad.equals("PantallaInicio")) {
				sqlRequest.sql = "SELECT * FROM [homebanking].[dbo].[encuesta_hb] WHERE cobisCliente = ? AND DATEADD(day, ?, fecha) >= GETDATE() order by fecha desc";
				sqlRequest.parametros.add(idCobis);
				sqlRequest.parametros.add(ConfigMB.integer("dias_solicitud_encuesta", 30));
			} else {
				sqlRequest.sql = "SELECT * FROM [homebanking].[dbo].[encuesta_hb] WHERE cobisCliente = ? order by fecha desc";
				sqlRequest.parametros.add(idCobis);
			}

			SqlResponseMB sqlResponse = SqlMB.response(sqlRequest);
			if (!sqlResponse.hayError || !sqlResponse.registros.isEmpty()) {
				Iterator<Objeto> itera = preguntas.iterator();
				while (itera.hasNext()) {
					Objeto pregunta = itera.next();
					List<Objeto> respuestas = removeRespuestaDupli(sqlResponse.registros);
					for (Objeto respuesta : respuestas) {
						if (validaMostrarPregunta(pregunta, respuesta)) {
							itera.remove();
						}
					}
				}
			}
		}
		return preguntas;
	}

	private static List<Objeto> removeRespuestaDupli(List<Objeto> respuestas) {
		Set<Objeto> respSet = new TreeSet<Objeto>((o1, o2) -> o1.string("id_pregunta").compareTo(o2.string("id_pregunta")));
		respSet.addAll(respuestas);
		return new ArrayList<>(respSet);
	}

	private static Boolean validaMostrarPregunta(Objeto pregunta, Objeto respuesta) {

		if (pregunta.string("id_pregunta").equals(respuesta.string("id_pregunta")) && respuesta.integer("volverAMostrar") == 0) {
			if (pregunta.string("inicio_encuesta").equalsIgnoreCase("null") || "".equals(pregunta.string("inicio_encuesta"))) {
				return true;
			}

			if (!"".equals(pregunta.string("inicio_encuesta")) && respuesta.date("fecha").after(Fecha.stringToDate(pregunta.string("inicio_encuesta"), "yyyy-MM-dd HH:mm:ss.ss")) || respuesta.date("fecha").equals(pregunta.date("inicio_encuesta"))) {
				return true;
			}

			if (!"".equals(pregunta.string("inicio_encuesta")) && Fecha.esPasado(Fecha.stringToDate(pregunta.string("inicio_encuesta"), "yyyy-MM-dd HH:mm:ss.ss")) && respuesta.date("fecha").before(Fecha.stringToDate(pregunta.string("inicio_encuesta"), "yyyy-MM-dd HH:mm:ss.ss"))) {
				return false;
			}

			return false;
		}

		return false;
	}

	public static List<Objeto> encuestaPreguntas(String canal, String funcionalidad) {
		List<Objeto> preguntas = new ArrayList<Objeto>();
		try {
			if (canal != null) {
				SqlRequestMB sqlRequest = SqlMB.request("SelectEncuestaPreguntas", "homebanking");
				sqlRequest.sql = "SELECT [id_pregunta], [pregunta], [inicio_encuesta] FROM [homebanking].[dbo].[encuesta_preguntas_hb] WHERE canal = ? and funcionalidad = ?";

				sqlRequest.parametros.add(canal);
				sqlRequest.parametros.add(funcionalidad);
				SqlResponseMB sqlResponse = SqlMB.response(sqlRequest);
				if (!sqlResponse.hayError) {
					preguntas.add(new Objeto().set("id_pregunta", sqlResponse.registros.get(0).get("id_pregunta")).set("pregunta", sqlResponse.registros.get(0).string("pregunta")).set("inicio_encuesta", sqlResponse.registros.get(0).string("inicio_encuesta")));
				}
			}
			return preguntas;
		} catch (Exception e) {
			return preguntas;
		}
	}

	public static Boolean guardaEncuesta(String idCobis, int idPregunta, String opinion, int puntuacion) {
		int volverAMostrar = 1;
		try {
			if (!StringUtils.isEmpty(opinion) || puntuacion >= 0) {
				volverAMostrar = 0;
			}
			if (idCobis != null) {
				SqlRequestMB sqlRequest = SqlMB.request("InsertEncuesta", "homebanking");
				sqlRequest.sql += "INSERT INTO [Homebanking].[dbo].[encuesta_hb] ";
				sqlRequest.sql += "([cobisCliente],[id_pregunta],[fecha],[opinion],[puntuacion],[volverAMostrar]) ";
				sqlRequest.sql += "VALUES (?, ?, GETDATE(), ?, ?, ?)";
				sqlRequest.parametros.add(idCobis);
				sqlRequest.parametros.add(idPregunta);
				sqlRequest.parametros.add(opinion);
				sqlRequest.parametros.add(puntuacion);
				sqlRequest.parametros.add(volverAMostrar);
				SqlResponseMB sqlResponse = SqlMB.response(sqlRequest);
				if (sqlResponse.hayError) {
					return false;
				}
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public static Boolean guardarAuditorConsumoSugerido(String idCobis, int recomendedMerchant, String nombreComercioVisualizacion, String canal, int vioDetalle) {
		try {
			if (idCobis != null) {
				SqlRequestMB sqlRequest = SqlMB.request("InsertAuditorConsumoSugerido", "homebanking");
				sqlRequest.sql += "INSERT INTO [Homebanking].[dbo].[auditor_consumo_sugerido] ";
				sqlRequest.sql += "([cobisId],[fechaHora],[recomendedMerchant],[nombreComercioVisualizacion],[canal],[vioDetalle]) ";
				sqlRequest.sql += "VALUES (?, GETDATE(),?, ?, ?, ?)";
				sqlRequest.parametros.add(idCobis);
				sqlRequest.parametros.add(recomendedMerchant);
				sqlRequest.parametros.add(nombreComercioVisualizacion);
				sqlRequest.parametros.add(canal);
				sqlRequest.parametros.add(vioDetalle);
				SqlResponseMB sqlResponse = SqlMB.response(sqlRequest);
				if (sqlResponse.hayError) {
					return false;
				}
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public static List<Objeto> getFondosParametria(String tipoSolicitud, String clase) {
		List<Objeto> fondos = new ArrayList<Objeto>();
		try {
			SqlRequestMB sqlRequest = SqlMB.request("SelectFondos", "homebanking");
			sqlRequest.sql = "SELECT * FROM [homebanking].[dbo].[parametria_fci]";
			sqlRequest.sql += "WHERE tipo_solicitud LIKE CONCAT('%', ?, '%') AND criterio_clase = ? AND habilitado_canales = 1";
			sqlRequest.parametros.add(tipoSolicitud);
			sqlRequest.parametros.add(clase);
			SqlResponseMB sqlResponse = SqlMB.response(sqlRequest);

			if (!sqlResponse.hayError) {
				fondos.addAll(sqlResponse.registros);
			}
			return fondos;
		} catch (Exception e) {
			return fondos;
		}
	}

	public static Objeto getHorarioMaximoMinimo(String tipoSolicitud) {
		Objeto variable = null;
		try {
			SqlRequestMB sqlRequest = SqlMB.request("SelectFondos", "homebanking");
			sqlRequest.sql = "SELECT MIN(hora_inicio) hora_inicio , MAX(hora_fin) hora_fin  FROM [homebanking].[dbo].[parametria_fci]";
			sqlRequest.sql += "WHERE tipo_solicitud LIKE CONCAT('%', ?, '%') AND habilitado_canales = 1";
			sqlRequest.parametros.add(tipoSolicitud);
			SqlResponseMB sqlResponse = SqlMB.response(sqlRequest);

			if (!sqlResponse.registros.isEmpty()) {
				variable = sqlResponse.registros.get(0);
			}
			return variable;
		} catch (Exception e) {
			return variable;
		}
	}

	public static Objeto obtenerConfiguracionVariable(String llave) {
		Objeto variable = null;
		try {
			if (llave != null) {
				SqlRequestMB sqlRequest = SqlMB.request("SelectConfiguracionVariable", "homebanking");
				sqlRequest.sql = "SELECT * FROM [homebanking].[dbo].[configuracion_variable] WHERE habilitado ='S' AND llave = ?";
				sqlRequest.parametros.add(llave);

				SqlResponseMB sqlResponse = SqlMB.response(sqlRequest);
				if (!sqlResponse.registros.isEmpty()) {
					variable = sqlResponse.registros.get(0);
				}
			}
			return variable;
		} catch (Exception e) {
			return variable;
		}
	}

	public static List<Objeto> getFondosAceptados(String idPersona, Integer fondo) {
		List<Objeto> fondos = new ArrayList<Objeto>();
		try {
			SqlRequestMB sqlRequest = SqlMB.request("SelectFondosAceptados", "homebanking");
			sqlRequest.sql = "SELECT * FROM [homebanking].[dbo].[fondos_aceptados]";
			sqlRequest.sql += "WHERE id_persona = ? AND fondo = ?";
			sqlRequest.parametros.add(idPersona);
			sqlRequest.parametros.add(fondo);
			SqlResponseMB sqlResponse = SqlMB.response(sqlRequest);

			if (!sqlResponse.hayError) {
				fondos.addAll(sqlResponse.registros);
			}
			return fondos;
		} catch (Exception e) {
			return fondos;
		}
	}

	public static void registrarFondo(String idPersona, String cuit, Date fecha, Integer version, Integer fondo) {
		try {
			SqlRequestMB sqlRequest = SqlMB.request("InsertFondo", "homebanking");
			sqlRequest.sql += "INSERT INTO [Homebanking].[dbo].[fondos_aceptados] ";
			sqlRequest.sql += "([id_persona], [cuit], [fecha], [version], [fondo]) ";
			sqlRequest.sql += "VALUES (?, ?, ?, ?, ?)";
			sqlRequest.parametros.add(idPersona);
			sqlRequest.parametros.add(cuit);
			sqlRequest.parametros.add(fecha);
			sqlRequest.parametros.add(version);
			sqlRequest.parametros.add(fondo);
			SqlMB.response(sqlRequest);
		} catch (Exception e) {
		}
	}

	public static Objeto findConfiguracionTarjeta(String idCobis, String proceso) {
		Objeto configuracionTarjeta = null;
		try {
			SqlRequestMB sqlRequest = SqlMB.request("SelectConfiguracionTarjeta", "homebanking");
			sqlRequest.sql = "SELECT * FROM [homebanking].[dbo].[configuracion_tarjeta]";
			sqlRequest.sql += "WHERE id_cobis = ? AND proceso = ?";
			sqlRequest.parametros.add(idCobis);
			sqlRequest.parametros.add(proceso);
			SqlResponseMB sqlResponse = SqlMB.response(sqlRequest);

			if (!sqlResponse.registros.isEmpty()) {
				configuracionTarjeta = sqlResponse.registros.get(0);
			}
			return configuracionTarjeta;
		} catch (Exception e) {
			return configuracionTarjeta;
		}
	}

	public static void saveConfiguracionTarjeta(String idCobis, String proceso) {
		try {
			SqlRequestMB sqlRequest = SqlMB.request("InsertFondo", "homebanking");
			sqlRequest.sql += "INSERT INTO [Homebanking].[dbo].[configuracion_tarjeta] ";
			sqlRequest.sql += "([id_cobis], [proceso], [fecha_creacion]) ";
			sqlRequest.sql += "VALUES (?, ?, SYSDATETIME())";
			sqlRequest.parametros.add(idCobis);
			sqlRequest.parametros.add(proceso);
			SqlMB.response(sqlRequest);
		} catch (Exception e) {
		}
	}

	public static void agendarOperacionFCI(String cobis_id, String id_solicitud, String cuotapartista, String fecha_solicitud, String importe, String fondo_id, String estado, String origen_solicitud, String tipo_solicitud, String tipo_cuenta) {
		try {
//			String fecha = fecha_solicitud;
			SqlRequestMB sqlRequest = SqlMB.request("InsertarOrdenAgenda", "homebanking");
			sqlRequest.sql += "INSERT INTO [Homebanking].[dbo].[ordenes_agendadas_fci] ";
			sqlRequest.sql += "([cobis_id], [id_solicitud], [cuotapartista], [fecha_solicitud], [importe], [fondo_id], [estado], [origen_solicitud], [tipo_solicitud], [tipo_cuenta]) ";
			sqlRequest.sql += "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			sqlRequest.parametros.add(cobis_id);
			sqlRequest.parametros.add(id_solicitud);
			sqlRequest.parametros.add(cuotapartista);
			sqlRequest.parametros.add(fecha_solicitud);
			sqlRequest.parametros.add(importe);
			sqlRequest.parametros.add(fondo_id);
			sqlRequest.parametros.add(estado);
			sqlRequest.parametros.add(origen_solicitud);
			sqlRequest.parametros.add(tipo_solicitud);
			sqlRequest.parametros.add(tipo_cuenta);
			SqlMB.response(sqlRequest);
		} catch (Exception e) {
		}
	}

	public static List<Objeto> getOrdenesAgendadosFCIPorNumCuotaPartista(String cuotapartista) {
		List<Objeto> fondos = new ArrayList<>();
		String estado = "Agendada";
		try {
			SqlRequestMB sqlRequest = SqlMB.request("GetOrdenAgenda", "homebanking");
			sqlRequest.sql = "SELECT * FROM [Homebanking].[dbo].[ordenes_agendadas_fci]";
			sqlRequest.sql += "WHERE cuotapartista = ? AND estado = ?";
			sqlRequest.parametros.add(cuotapartista);
			sqlRequest.parametros.add(estado);
			SqlResponseMB sqlResponse = SqlMB.response(sqlRequest);

			if (!sqlResponse.hayError) {
				fondos.addAll(sqlResponse.registros);
			}
			return fondos;
		} catch (Exception e) {
			return fondos;
		}
	}

	public static Objeto buscarDocumentoParaTDVirtual(Integer idEstado, String tipoDocumento, String nroDocumento) {
		Objeto estado = null;
		try {
			SqlRequestMB sqlRequest = SqlMB.request("SelectCobisTDVirtual", "homebanking");
			sqlRequest.sql = "SELECT id_cobis FROM [homebanking].[dbo].[cobis_td_virtual]";
			sqlRequest.sql += "WHERE estado = ? and nro_docu = ? and tipo_docu = ?";
			sqlRequest.parametros.add(idEstado);
			sqlRequest.parametros.add(nroDocumento);
			sqlRequest.parametros.add(tipoDocumento);
			SqlResponseMB sqlResponse = SqlMB.response(sqlRequest);

			if (!sqlResponse.registros.isEmpty()) {
				estado = sqlResponse.registros.get(0);
			}
			return estado;
		} catch (Exception e) {
			return estado;
		}
	}

	public static List<Objeto> getOrdenesAgendadasFCIPorEstadoAndCobis(String estado, String cobis) {
		List<Objeto> fondos = new ArrayList<>();
		try {
			SqlRequestMB sqlRequest = SqlMB.request("GetOrdenAgenda", "homebanking");
			sqlRequest.sql = "SELECT * FROM [Homebanking].[dbo].[ordenes_agendadas_fci] WHERE estado = ? AND cobis_id = ?";
			sqlRequest.parametros.add(estado);
			sqlRequest.parametros.add(cobis);

			// Agregar condici�n para el par�metro cobis solo si se proporciona
			/*
			 * if (cobis != null && !cobis.isEmpty()) { sqlRequest.sql +=
			 * " AND cobis_id = ?"; sqlRequest.parametros.add(cobis); }
			 */
			SqlResponseMB sqlResponse = SqlMB.response(sqlRequest);

			if (!sqlResponse.hayError) {
				fondos.addAll(sqlResponse.registros);
			}
			return fondos;
		} catch (Exception e) {
			return fondos;
		}
	}
	
	public static Objeto getBroker(String cuit, EstadoBroker activo) {
		try {
			if (cuit != null && cuit.length() == 11) {
				SqlRequestMB sqlRequest = SqlMB.request("SelectBroker", "homebanking");
				sqlRequest.sql = "SELECT * "
						+ "FROM [homebanking].[dbo].[brokers] "
						+ "WHERE cuit = ? and "
						+ "estado = ?";
				sqlRequest.parametros.add(cuit);
				sqlRequest.parametros.add(activo.ordinal());
				SqlResponseMB sqlResponse = SqlMB.response(sqlRequest);
				if (!sqlResponse.hayError 
						&& sqlResponse.registros != null 
						&& sqlResponse.registros.size() > 0) {
					return sqlResponse.registros.get(0);
				}
			}
			return null;
		} catch (Exception e) {
			return null;
		}
	}

	public static void saveErrorAltaCuentaInversor(Objeto sqlError) {
		String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
		sqlError.set("detalle", "");
		try {
			if (sqlError.string("id_cobis") != null) {
				SqlRequestMB sqlRequest = SqlMB.request("InsertErrorAltaCuentaInversor", "hbs");
				sqlRequest.sql += "INSERT INTO [Hbs].[dbo].[error_cuentaInversor] ";
				sqlRequest.sql += "([id_cobis], [unitrade], [cv], [fondos_cuenta], [fondos_persona], [rcp_cta_comitente], [rcp_cta_cuotapartista], [detalle], [fecha], [canal_venta]) ";
				sqlRequest.sql += "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
				sqlRequest.parametros.add(sqlError.string("id_cobis"));
				sqlRequest.parametros.add(sqlError.string("unitrade"));
				sqlRequest.parametros.add(sqlError.string("cv"));
				sqlRequest.parametros.add(sqlError.string("fondos_cuenta"));
				sqlRequest.parametros.add(sqlError.string("fondos_persona"));
				sqlRequest.parametros.add(sqlError.string("rcp_cta_comitente"));

				sqlRequest.parametros.add(sqlError.string("rcp_cta_cuotapartista"));
				sqlRequest.parametros.add(sqlError.string("detalle"));
				sqlRequest.parametros.add(fecha);
				sqlRequest.parametros.add("MB");

				SqlMB.response(sqlRequest);
			}
		} catch (Exception e) {
		}
	}

	// === Idempotencia Compra/Venta USD (anti-replay, sin SP) ===
	private static final int CVUSD_WINDOW_SECS = 10;

	/**
	 * Intenta tomar el lock lógico (cobis, operacion) por la ventana configurada.
	 * Devuelve true si LO TOMÓ esta llamada; false si hay operación en curso o cooldown.
	 */
	public static boolean tryLockCVUSD(int cobis, String operacion) {
		try {
			// 1) INSERT optimista. Si choca UNIQUE (2601/2627), devolvemos ok=0 sin lanzar error.
			SqlRequestMB ins = SqlMB.request("CVUSD_TRY_INSERT_MB", "homebanking");
			ins.sql  = "BEGIN TRY "
					+ "  INSERT INTO homebanking.dbo.cvusd_idempotencia "
					+ "    (cobis, operacion, last_ok_at, tomado, tomado_desde, actualizado_at) "
					+ "  VALUES (?, ?, DATEADD(SECOND, -(? + 1), SYSDATETIME()), 1, SYSDATETIME(), SYSDATETIME()); "
					+ "  SELECT CAST(1 AS INT) AS ok; "
					+ "END TRY "
					+ "BEGIN CATCH "
					+ "  IF ERROR_NUMBER() IN (2601,2627) "
					+ "    SELECT CAST(0 AS INT) AS ok; "
					+ "  ELSE "
					+ "    THROW; "
					+ "END CATCH";
			ins.add(cobis);
			ins.add(operacion);
			ins.add(CVUSD_WINDOW_SECS);

			SqlResponseMB r1 = SqlMB.response(ins);
			if (!r1.hayError && !r1.registros.isEmpty() && r1.registros.get(0).integer("ok", 0) == 1) {
				return true; // lock tomado por INSERT
			}

			// 2) UPDATE condicional (robar lock si expiró o si venció el cooldown).
			SqlRequestMB upd = SqlMB.request("CVUSD_TRY_UPDATE_MB", "homebanking");
			upd.sql  = "UPDATE t WITH (UPDLOCK, ROWLOCK) "
					+ "   SET tomado = 1, tomado_desde = SYSDATETIME(), actualizado_at = SYSDATETIME() "
					+ "FROM homebanking.dbo.cvusd_idempotencia t "
					+ "WHERE t.cobis = ? AND t.operacion = ? AND ("
					+ "    (t.tomado = 0 AND (t.last_ok_at IS NULL OR DATEDIFF(SECOND, t.last_ok_at, SYSDATETIME()) >= ?)) "
					+ " OR (t.tomado = 1 AND DATEDIFF(SECOND, t.tomado_desde, SYSDATETIME()) >= ?) "
					+ " OR (t.last_ok_at IS NOT NULL AND DATEDIFF(SECOND, t.last_ok_at, SYSDATETIME()) >= ?)"
					+ "); "
					+ "SELECT CASE WHEN @@ROWCOUNT = 1 THEN CAST(1 AS INT) ELSE CAST(0 AS INT) END AS ok;";
			upd.add(cobis);
			upd.add(operacion);
			upd.add(CVUSD_WINDOW_SECS);
			upd.add(CVUSD_WINDOW_SECS);
			upd.add(CVUSD_WINDOW_SECS);

			SqlResponseMB r2 = SqlMB.response(upd);
			return (!r2.hayError && !r2.registros.isEmpty() && r2.registros.get(0).integer("ok", 0) == 1);

		} catch (Exception e) {
			// Por seguridad, si algo raro pasa, no dejamos pasar otra operación en paralelo
			return false;
		}
	}

	/** Libera el lock. Si success=true, activa cooldown (last_ok_at = now). */
	public static void releaseCVUSD(int cobis, String operacion, boolean success) {
		try {
			SqlRequestMB rel = SqlMB.request("CVUSD_RELEASE_MB", "homebanking");
			rel.sql  = "UPDATE homebanking.dbo.cvusd_idempotencia "
					+ "   SET tomado = 0, "
					+ "       tomado_desde = NULL, "
					+ "       last_ok_at = CASE WHEN ? = 1 THEN SYSDATETIME() ELSE last_ok_at END, "
					+ "       actualizado_at = SYSDATETIME() "
					+ " WHERE cobis = ? AND operacion = ?;";
			rel.add(success ? 1 : 0);
			rel.add(cobis);
			rel.add(operacion);
			SqlMB.response(rel);
		} catch (Exception ignored) {}
	}

	/** (Opcional) Segundos restantes para poder operar de nuevo (para countdown en UI). */
	public static int remainingSecsCVUSD(int cobis, String operacion) {
		try {
			SqlRequestMB q = SqlMB.request("CVUSD_REMAINING_MB", "homebanking");
			q.sql  = "SELECT CASE "
					+ "  WHEN tomado = 1 THEN IIF(? - DATEDIFF(SECOND, tomado_desde, SYSDATETIME()) > 0, "
					+ "                               ? - DATEDIFF(SECOND, tomado_desde, SYSDATETIME()), 0) "
					+ "  WHEN last_ok_at IS NOT NULL THEN IIF(? - DATEDIFF(SECOND, last_ok_at, SYSDATETIME()) > 0, "
					+ "                               ? - DATEDIFF(SECOND, last_ok_at, SYSDATETIME()), 0) "
					+ "  ELSE 0 "
					+ "END AS wait_sec "
					+ "FROM homebanking.dbo.cvusd_idempotencia WITH (READCOMMITTEDLOCK) "
					+ "WHERE cobis = ? AND operacion = ?;";
			q.add(CVUSD_WINDOW_SECS);
			q.add(CVUSD_WINDOW_SECS);
			q.add(CVUSD_WINDOW_SECS);
			q.add(CVUSD_WINDOW_SECS);
			q.add(cobis);
			q.add(operacion);

			SqlResponseMB r = SqlMB.response(q);
			for (Objeto reg : r.registros) return reg.integer("wait_sec", 0);
			return 0;
		} catch (Exception e) {
			return 0;
		}
	}

	// Overloads convenientes
	public static boolean tryLockCVUSD(String idCobis, String operacion) {
		try { if (idCobis == null) return false; return tryLockCVUSD(Integer.parseInt(idCobis), operacion); }
		catch (Exception e) { return false; }
	}
	public static void releaseCVUSD(String idCobis, String operacion, boolean success) {
		try { if (idCobis == null) return; releaseCVUSD(Integer.parseInt(idCobis), operacion, success); }
		catch (Exception ignored) {}
	}
	public static int remainingSecsCVUSD(String idCobis, String operacion) {
		try { if (idCobis == null) return 0; return remainingSecsCVUSD(Integer.parseInt(idCobis), operacion); }
		catch (Exception e) { return 0; }
	}

}
