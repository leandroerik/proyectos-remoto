package ar.com.hipotecario.canal.homebanking.servicio;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import ar.com.hipotecario.mobile.conector.SqlMB;
import ar.com.hipotecario.mobile.conector.SqlRequestMB;
import ar.com.hipotecario.mobile.conector.SqlResponseMB;
import org.apache.commons.lang3.StringUtils;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.homebanking.base.ConfigHB;
import ar.com.hipotecario.canal.homebanking.base.Objeto;
import ar.com.hipotecario.canal.homebanking.conector.Sql;
import ar.com.hipotecario.canal.homebanking.conector.SqlRequest;
import ar.com.hipotecario.canal.homebanking.conector.SqlResponse;
import ar.com.hipotecario.canal.homebanking.negocio.EstadoBroker;

public class SqlHomebanking {

	/* ========== ULTIMA CONEXION ========== */
	public static Date fechaHoraUltimaConextion(String idCobis) {
		try {
			SqlRequest sqlRequest = Sql.request("SelectUltimaConexion", "homebanking");
			if (ConfigHB.bool("habilitar_sp", false)) {
				sqlRequest.sql = "EXEC [homebanking].[dbo].[sp_consUltimaConexion] @codCliente = '?1'".replace("?1",
						idCobis);
			} else {
				sqlRequest.sql = "SELECT [idCobis], [momento] FROM [Homebanking].[dbo].[ultima_conexion] WHERE [idCobis] = ?";
				sqlRequest.parametros.add(idCobis);
			}
			SqlResponse sqlResponse = Sql.response(sqlRequest);
			for (Objeto registro : sqlResponse.registros) {
				return registro.date("momento");
			}
		} catch (Exception e) {
		}
		return null;
	}

	public static Boolean registrarFechaHoraUltimaConextion(String idCobis, String modo) {
		return registrarFechaHoraUltimaConextionRowLock(idCobis);
	}

	public static Boolean registrarFechaHoraUltimaConextionRowLock(String idCobis) {
		try {
			SqlRequest sqlRequest = Sql.request("UpdateUltimaConexion", "homebanking");
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

			SqlResponse a = Sql.response(sqlRequest);
			System.out.println(a);
		} catch (Exception e) {
		}
		return true;
	}

	/* ========== SESION ========== */
	public static Boolean existeSesion(String idCobis, String fingerprint) {
		try {
			if (idCobis != null) {
				SqlRequest sqlRequest = Sql.request("InsertSesion", "homebanking");
				sqlRequest.sql = "SELECT * FROM [Homebanking].[dbo].[session] WHERE s_cod_cliente = ?";
				sqlRequest.parametros.add(idCobis);
				SqlResponse sqlResponse = Sql.response(sqlRequest);
				if (!sqlResponse.hayError) {
					for (Objeto registro : sqlResponse.registros) {
						Long momentoLogin = registro.date("s_login_timestamp").getTime();
						Long momentoFinSesion = momentoLogin
								+ ConfigHB.integer("servidor_tiempo_sesion", 10 * 60) * 1000;
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

	public static Boolean registrarSesion(String idCobis, String fingerprint, String idSesion) {
		try {
			if (idCobis != null) {
				SqlRequest sqlRequest = Sql.request("InsertSesion", "homebanking");
				sqlRequest.sql += "INSERT INTO [Homebanking].[dbo].[session] ";
				sqlRequest.sql += "([s_cod_cliente], [s_fingerprint], [s_login_timestamp], [s_canal], [redis_id]) ";
				sqlRequest.sql += "VALUES (?, ?, GETDATE(), 'HB', ? )";
				sqlRequest.parametros.add(idCobis);
				sqlRequest.parametros.add(fingerprint);
				sqlRequest.parametros.add(idSesion);
				new Futuro<SqlResponse>(() -> Sql.response(sqlRequest));
				return true;
			}
		} catch (Exception e) {
		}
		return false;
	}

	public static void eliminarSesion(String idCobis) {
		try {
			if (idCobis != null) {
				SqlRequest sqlRequest = Sql.request("DeleteSesion", "homebanking");
				sqlRequest.sql += "DELETE FROM [Homebanking].[dbo].[session] WHERE s_cod_cliente = ?";
				sqlRequest.parametros.add(idCobis);
				new Futuro<SqlResponse>(() -> Sql.response(sqlRequest));
			}
		} catch (Exception e) {
		}
	}

	/* ========== FRAUDE ========== */
	public static Boolean bloqueadoPorFraude(String idCobis) {
		try {
			if (idCobis != null) {
				SqlRequest sqlRequest = Sql.request("SelectFraude", "homebanking");
				sqlRequest.sql = "SELECT * FROM [homebanking].[dbo].[muestreo] WHERE m_subid = ?";
				sqlRequest.parametros.add(idCobis);
				SqlResponse sqlResponse = Sql.response(sqlRequest);
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

	// chequeo 90 dias

	//
	public static Objeto getTipoMuestreo(String idCobis, String tipoMuestra) {
		try {
			if (idCobis != null) {
				SqlRequest sqlRequest = Sql.request("SelectMuestreo", "homebanking");
				sqlRequest.sql = "SELECT * FROM [homebanking].[dbo].[muestreo] WHERE m_subid = ?";
				sqlRequest.parametros.add(idCobis);
				SqlResponse sqlResponse = Sql.response(sqlRequest);
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

	public static Boolean bloqueadoPorTransaccionFraudeLink(String idCobis) {
		String sql = "select * from [homebanking].[dbo].[muestreo] where m_subid = ? and m_tipoMuestra = ? and m_valor = ?";

		SqlRequest sqlRequest = Sql.request("SelectFraude", "homebanking");
		sqlRequest.sql = sql;
		sqlRequest.parametros.add(idCobis);
		sqlRequest.parametros.add("bloquear.acceso.fraude.link");
		sqlRequest.parametros.add("true");
		SqlResponse sqlResponse = Sql.response(sqlRequest);
		if (sqlResponse.hayError) {
			return false;
		}
		if (sqlResponse.registros.size() > 0) {
			return true;
		}
		return false;
	}

	public static List<Objeto> mostrarEncuestaUsuario(String idCobis, List<Objeto> preguntas) {

		if (idCobis != null && !preguntas.isEmpty()) {
			SqlRequest sqlRequest = Sql.request("SelectEncuestaUsuario", "homebanking");
			sqlRequest.sql = "SELECT * FROM [homebanking].[dbo].[encuesta_hb] WHERE cobisCliente = ?";
			sqlRequest.parametros.add(idCobis);
			SqlResponse sqlResponse = Sql.response(sqlRequest);
			if (!sqlResponse.hayError || !sqlResponse.registros.isEmpty()) {
				Iterator<Objeto> itera = preguntas.iterator();
				while (itera.hasNext()) {
					Objeto pregunta = itera.next();
					for (Objeto registro : sqlResponse.registros) {
						if (pregunta.string("id_pregunta").equals(registro.string("id_pregunta"))
								&& registro.integer("volverAMostrar") == 0) {
							itera.remove();
						}
					}
				}
			}
		}
		return preguntas;
	}

	public static List<Objeto> encuestaPreguntas(String canal, String funcionalidad) {
		List<Objeto> preguntas = new ArrayList<Objeto>();
		try {
			if (canal != null) {
				SqlRequest sqlRequest = Sql.request("SelectEncuestaPreguntas", "homebanking");
				sqlRequest.sql = "SELECT [id_pregunta], [pregunta] FROM [homebanking].[dbo].[encuesta_preguntas_hb] WHERE canal = ? and funcionalidad = ?";
				sqlRequest.parametros.add(canal);
				sqlRequest.parametros.add(funcionalidad);
				SqlResponse sqlResponse = Sql.response(sqlRequest);
				if (!sqlResponse.hayError) {
					preguntas.addAll(sqlResponse.registros);
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
			if (!StringUtils.isEmpty(opinion) || puntuacion > 0) {
				volverAMostrar = 0;
			}
			if (idCobis != null) {
				SqlRequest sqlRequest = Sql.request("InsertEncuesta", "homebanking");
				sqlRequest.sql += "INSERT INTO [Homebanking].[dbo].[encuesta_hb] ";
				sqlRequest.sql += "([cobisCliente],[id_pregunta],[fecha],[opinion],[puntuacion],[volverAMostrar]) ";
				sqlRequest.sql += "VALUES (?, ?, GETDATE(), ?, ?, ?)";
				sqlRequest.parametros.add(idCobis);
				sqlRequest.parametros.add(idPregunta);
				sqlRequest.parametros.add(opinion);
				sqlRequest.parametros.add(puntuacion);
				sqlRequest.parametros.add(volverAMostrar);
				SqlResponse sqlResponse = Sql.response(sqlRequest);
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
			SqlRequest sqlRequest = Sql.request("SelectFondos", "homebanking");
			sqlRequest.sql = "SELECT * FROM [homebanking].[dbo].[parametria_fci]";
			sqlRequest.sql += "WHERE tipo_solicitud LIKE CONCAT('%', ?, '%') AND criterio_clase = ? AND habilitado_canales = 1";
			sqlRequest.parametros.add(tipoSolicitud);
			sqlRequest.parametros.add(clase);
			SqlResponse sqlResponse = Sql.response(sqlRequest);

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
			SqlRequest sqlRequest = Sql.request("SelectFondos", "homebanking");
			sqlRequest.sql = "SELECT MIN(hora_inicio) hora_inicio , MAX(hora_fin) hora_fin  FROM [homebanking].[dbo].[parametria_fci]";
			sqlRequest.sql += "WHERE tipo_solicitud LIKE CONCAT('%', ?, '%') AND habilitado_canales = 1";
			sqlRequest.parametros.add(tipoSolicitud);
			SqlResponse sqlResponse = Sql.response(sqlRequest);

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
				SqlRequest sqlRequest = Sql.request("SelectConfiguracionVariable", "homebanking");
				sqlRequest.sql = "SELECT * FROM [homebanking].[dbo].[configuracion_variable] WHERE habilitado ='S' AND llave = ?";
				sqlRequest.parametros.add(llave);

				SqlResponse sqlResponse = Sql.response(sqlRequest);
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
			SqlRequest sqlRequest = Sql.request("SelectFondosAceptados", "homebanking");
			sqlRequest.sql = "SELECT * FROM [homebanking].[dbo].[fondos_aceptados]";
			sqlRequest.sql += "WHERE id_persona = ? AND fondo = ?";
			sqlRequest.parametros.add(idPersona);
			sqlRequest.parametros.add(fondo);
			SqlResponse sqlResponse = Sql.response(sqlRequest);

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
			SqlRequest sqlRequest = Sql.request("InsertFondo", "homebanking");
			sqlRequest.sql += "INSERT INTO [Homebanking].[dbo].[fondos_aceptados] ";
			sqlRequest.sql += "([id_persona], [cuit], [fecha], [version], [fondo]) ";
			sqlRequest.sql += "VALUES (?, ?, ?, ?, ?)";
			sqlRequest.parametros.add(idPersona);
			sqlRequest.parametros.add(cuit);
			sqlRequest.parametros.add(fecha);
			sqlRequest.parametros.add(version);
			sqlRequest.parametros.add(fondo);
			Sql.response(sqlRequest);
		} catch (Exception e) {
		}
	}

	/* ========== BLOQUEAR POR FRAUDE ========== */
	public static Boolean bloquearPorFraude(String idCobis) {
		String deshabilitarAcceso = "deshabilitar.acceso.fraude";
		try {
			if (idCobis != null) {
				SqlRequest sqlRequest = Sql.request("SelectFraude", "homebanking");
				sqlRequest.sql = "SELECT * FROM [homebanking].[dbo].[muestreo] WHERE m_subid = ? and [m_tipoMuestra] = ? ";
				sqlRequest.parametros.add(idCobis);
				sqlRequest.parametros.add(deshabilitarAcceso);
				SqlResponse sqlResponse = Sql.response(sqlRequest);
				if (sqlResponse.hayError) {
					return false;
				}
				if (sqlResponse.registros.isEmpty()) {
					// insertar
					SqlRequest sqlRequestInsert = Sql.request("InsertFraude", "homebanking");
					sqlRequestInsert.sql += "INSERT INTO [homebanking].[dbo].[muestreo] ";
					sqlRequestInsert.sql += "([m_tipoMuestra],[m_valor],[m_subid] ) ";
					sqlRequestInsert.sql += "VALUES (?, ?, ?)";
					sqlRequestInsert.parametros.add(deshabilitarAcceso);
					sqlRequestInsert.parametros.add("true");
					sqlRequestInsert.parametros.add(idCobis);
					SqlResponse sqlResponseInsert = Sql.response(sqlRequestInsert);
					if (sqlResponseInsert.hayError) {
						return false;
					}
				} else {
					// updatear
					SqlRequest sqlRequestUpdate = Sql.request("UpdateFraude", "homebanking");
					sqlRequestUpdate.sql = "update [homebanking].[dbo].[muestreo] set m_valor = ? where [m_subid] = ? and [m_tipoMuestra] = ? ";
					sqlRequestUpdate.parametros.add("true");
					sqlRequestUpdate.parametros.add(idCobis);
					sqlRequestUpdate.parametros.add(deshabilitarAcceso);
					SqlResponse sqlResponseUpdate = Sql.response(sqlRequestUpdate);
					if (sqlResponseUpdate.hayError) {
						return false;
					}
				}
				return true;
			}
			return false;
		} catch (Exception e) {
			return false;
		}
	}

	public static Objeto findConfiguracionTarjeta(String idCobis, String proceso) {
		Objeto configuracionTarjeta = null;
		try {
			SqlRequest sqlRequest = Sql.request("SelectConfiguracionTarjeta", "homebanking");
			sqlRequest.sql = "SELECT * FROM [homebanking].[dbo].[configuracion_tarjeta]";
			sqlRequest.sql += "WHERE id_cobis = ? AND proceso = ?";
			sqlRequest.parametros.add(idCobis);
			sqlRequest.parametros.add(proceso);
			SqlResponse sqlResponse = Sql.response(sqlRequest);

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
			SqlRequest sqlRequest = Sql.request("InsertFondo", "homebanking");
			sqlRequest.sql += "INSERT INTO [Homebanking].[dbo].[configuracion_tarjeta] ";
			sqlRequest.sql += "([id_cobis], [proceso], [fecha_creacion]) ";
			sqlRequest.sql += "VALUES (?, ?, SYSDATETIME())";
			sqlRequest.parametros.add(idCobis);
			sqlRequest.parametros.add(proceso);
			Sql.response(sqlRequest);
		} catch (Exception e) {
		}
	}

	public static Objeto findEstadoTarjetaCRM(Integer idEstado) {
		Objeto estado = null;
		try {
			SqlRequest sqlRequest = Sql.request("SelectEstadoTarjetaCRM", "homebanking");
			sqlRequest.sql = "SELECT * FROM [homebanking].[dbo].[estado_tarjeta_crm]";
			sqlRequest.sql += "WHERE id_estado = ?";
			sqlRequest.parametros.add(idEstado);
			SqlResponse sqlResponse = Sql.response(sqlRequest);

			if (!sqlResponse.registros.isEmpty()) {
				estado = sqlResponse.registros.get(0);
			}
			return estado;
		} catch (Exception e) {
			return estado;
		}
	}

	/* ========== Solicitudes AgendadasFCI ========== */
	public static Boolean actualizaEstadoAgendaFci(String estado, String id_solicitud, String estado_previo) {
		try {
			SqlRequest sqlRequest = Sql.request("UpdateUltimaConexion", "homebanking");
			sqlRequest.sql = "UPDATE [Homebanking].[dbo].[ordenes_agendadas_fci] ";
			sqlRequest.sql += "SET [estado] = ? ";
			sqlRequest.sql += "WHERE [id_solicitud] = ?  AND [estado] = ? ";
			sqlRequest.parametros.add(estado);
			sqlRequest.parametros.add(id_solicitud);
			sqlRequest.parametros.add(estado_previo);

			SqlResponse a = Sql.response(sqlRequest);
			System.out.println(a);
		} catch (Exception e) {
		}
		return true;
	} // todo eliminar

	public static List<Objeto> getOrdenesAgendadasFCIPorEstado(String estado) { // Obtengo la agenda por estado
		List<Objeto> fondos = new ArrayList<>();
		try {
			SqlRequest sqlRequest = Sql.request("GetOrdenAgenda", "homebanking");
			sqlRequest.sql = "SELECT * FROM [Homebanking].[dbo].[ordenes_agendadas_fci]";
			sqlRequest.sql += "WHERE estado = ?";
			sqlRequest.parametros.add(estado);
			SqlResponse sqlResponse = Sql.response(sqlRequest);

			if (!sqlResponse.hayError) {
				fondos.addAll(sqlResponse.registros);
			}
			return fondos;
		} catch (Exception e) {
			return fondos;
		}
	}

	public static List<Objeto> getOrdenesAgendadosFCIPorNumCuotaPartista(String cuotapartista) {
		List<Objeto> fondos = new ArrayList<>();
		String estado = "Agendada";
		try {
			SqlRequest sqlRequest = Sql.request("GetOrdenAgenda", "homebanking");
			sqlRequest.sql = "SELECT * FROM [Homebanking].[dbo].[ordenes_agendadas_fci]";
			sqlRequest.sql += "WHERE cuotapartista = ? AND estado = ?";
			sqlRequest.parametros.add(cuotapartista);
			sqlRequest.parametros.add(estado);
			SqlResponse sqlResponse = Sql.response(sqlRequest);

			if (!sqlResponse.hayError) {
				fondos.addAll(sqlResponse.registros);
			}
			return fondos;
		} catch (Exception e) {
			return fondos;
		}
	}

	public static List<Objeto> getAgendaSuscripcionFCI(String cobis_id, String cuotapartista, String estado) {
		List<Objeto> fondos = new ArrayList<>();
		String error = "Error";
		try {
			SqlRequest sqlRequest = Sql.request("GetOrdenAgenda", "homebanking");
			sqlRequest.sql = "SELECT DISTINCT o.*, p.fondo_nombre, p.moneda_descripcion FROM [Homebanking].[dbo].[ordenes_agendadas_fci] AS o INNER JOIN [Homebanking].[dbo].[parametria_fci] AS p ON o.fondo_id = p.id_fondo ";
			sqlRequest.sql += "WHERE ( o.cobis_id = ? AND o.cuotapartista = ? ) AND ( o.estado = ? OR o.estado = ?) ";
			sqlRequest.parametros.add(cobis_id);
			sqlRequest.parametros.add(cuotapartista);
			sqlRequest.parametros.add(estado);
			sqlRequest.parametros.add(error);
			SqlResponse sqlResponse = Sql.response(sqlRequest);

			if (!sqlResponse.hayError) {
				fondos.addAll(sqlResponse.registros);
			}
			return fondos;
		} catch (Exception e) {
			return fondos;
		}
	}

	public static List<Objeto> getOrdenesAgendadasFCIPorEstadoAndCobis(String estado, String cobis) {
		List<Objeto> fondos = new ArrayList<>();
		try {
			SqlRequest sqlRequest = Sql.request("GetOrdenAgenda", "homebanking");
			sqlRequest.sql = "SELECT * FROM [Homebanking].[dbo].[ordenes_agendadas_fci] WHERE estado = ? AND cobis_id = ?";
			sqlRequest.parametros.add(estado);
			sqlRequest.parametros.add(cobis);

			// Agregar condición para el parámetro cobis solo si se proporciona
			/*
			 * if (cobis != null && !cobis.isEmpty()) { sqlRequest.sql +=
			 * " AND cobis_id = ?"; sqlRequest.parametros.add(cobis); }
			 */
			SqlResponse sqlResponse = Sql.response(sqlRequest);

			if (!sqlResponse.hayError) {
				fondos.addAll(sqlResponse.registros);
			}
			return fondos;
		} catch (Exception e) {
			return fondos;
		}
	}

	public static String updateEstadoOrdenesAgendadasFCI(String idSolicitud, String estado) {
		try {
			SqlRequest sqlRequestUpdate = Sql.request("UpdateAgendadoFCI", "homebanking");
			sqlRequestUpdate.sql = "UPDATE [homebanking].[dbo].[ordenes_agendadas_fci] SET estado = ? WHERE [id_solicitud] = ? ";
			sqlRequestUpdate.parametros.add(estado);
			sqlRequestUpdate.parametros.add(idSolicitud);
			SqlResponse sqlResponseUpdate = Sql.response(sqlRequestUpdate);
			if (sqlResponseUpdate.hayError) {
				return "error";
			}
			return "ok";
		} catch (Exception e) {
			return "error";
		}
	}

	public static void agendarOperacionFCI(String cobis_id, String id_solicitud, String cuotapartista,
			String fecha_solicitud, String importe, String fondo_id, String estado, String origen_solicitud,
			String tipo_solicitud, String tipo_cuenta) {
		try {
			SqlRequest sqlRequest = Sql.request("InsertarOrdenAgenda", "homebanking");
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
			Sql.response(sqlRequest);
		} catch (Exception e) {
		}
	}

	public static void saveErrorAltaCuentaInversor(Objeto sqlError) {
		String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
		sqlError.set("detalle", "");
		try {
			if (sqlError.string("id_cobis") != null) {
				SqlRequest sqlRequest = Sql.request("InsertErrorAltaCuentaInversor", "hbs");
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
				sqlRequest.parametros.add("HB");

				Sql.response(sqlRequest);
			}
		} catch (Exception e) {
		}
	}

	public static Objeto getBroker(String cuit, EstadoBroker estado) {
		try {
			if (cuit != null && cuit.length() == 11) {
				SqlRequest sqlRequest = Sql.request("SelectBroker", "homebanking");
				sqlRequest.sql = "SELECT * " + "FROM [homebanking].[dbo].[brokers] " + "WHERE cuit = ? and "
						+ "estado = ?";
				sqlRequest.parametros.add(cuit);
				sqlRequest.parametros.add(estado.ordinal());
				SqlResponse sqlResponse = Sql.response(sqlRequest);
				if (!sqlResponse.hayError && sqlResponse.registros != null && sqlResponse.registros.size() > 0) {
					return sqlResponse.registros.get(0);
				}
			}
			return null;
		} catch (Exception e) {
			return null;
		}
	}

	public static boolean esClienteTasaPreferencial(String idCobs, boolean activo) {
		try {
			int activoParam = activo ? 1 : 0;

			SqlRequest sqlRequest = Sql.request("SelectClienteTasaPreferencial", "homebanking");
			sqlRequest.sql = "SELECT Activo " +
					"FROM [homebanking].[dbo].[clientesTasaPreferencialPF] " +
					"WHERE idCobis = ? AND Activo = ?";

			sqlRequest.parametros.add(idCobs);
			sqlRequest.parametros.add(activoParam);

			SqlResponse sqlResponse = Sql.response(sqlRequest);
			if (!sqlResponse.hayError && !sqlResponse.registros.isEmpty()) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public static Boolean getCobisOfertaCADolar (String idCobis) {
		try {

			String DB_HBS = "hbs";
			/* ========== GET ========== */
			String SP_EXEC_GET = "EXEC [hbs].[dbo].[sp_VerificarExistenciaIdCobis] @idCobis = ?";
			String GET = "GetCobisOfertaCADolar";

			SqlRequest sqlRequest = Sql.request(GET, DB_HBS);

			sqlRequest.sql = SP_EXEC_GET;
			sqlRequest.parametros.add(idCobis);

			SqlResponse sqlResponse = Sql.response(sqlRequest);

			if (!sqlResponse.hayError && !sqlResponse.registros.isEmpty()) {
				return true;
			}

		} catch (Exception e) {
			return false;
		}
		return false;
	}

	// ========== Idempotencia Compra/Venta USD (anti-replay 60s, sin SP) ==========
	private static final int CVUSD_WINDOW_SECS = 10;

	/**
	 * Intenta tomar el lock lógico (cobis, operacion) por 60s.
	 * Devuelve true si LO TOMÓ esta llamada; false si hay operación en curso o cooldown.
	 */
	public static boolean tryLockCVUSD(int cobis, String operacion) {
		try {
			// 1) INSERT optimista. Si colisiona UNIQUE (2601/2627), devuelve ok=0 sin lanzar error.
			SqlRequest ins = Sql.request("CVUSD_TRY_INSERT", "homebanking");
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

			SqlResponse r1 = Sql.response(ins);
			if (!r1.hayError && !r1.registros.isEmpty() && r1.registros.get(0).integer("ok", 0) == 1) {
				return true; // lock tomado por INSERT
			}

			// 2) UPDATE condicional para "robar" el lock si expiró o si venció el cooldown.
			SqlRequest upd = Sql.request("CVUSD_TRY_UPDATE", "homebanking");
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

			SqlResponse r2 = Sql.response(upd);
			return (!r2.hayError && !r2.registros.isEmpty() && r2.registros.get(0).integer("ok", 0) == 1);

		} catch (Exception e) {
			// Por seguridad, si algo raro pasa, no dejamos pasar otra operación en paralelo
			return false;
		}
	}

	/**
	 * Libera el lock. Si success=true, activa cooldown (last_ok_at = now). Si no, solo libera concurrencia.
	 */
	public static void releaseCVUSD(int cobis, String operacion, boolean success) {
		try {
			SqlRequest rel = Sql.request("CVUSD_RELEASE", "homebanking");
			rel.sql  = "UPDATE homebanking.dbo.cvusd_idempotencia "
					+ "   SET tomado = 0, "
					+ "       tomado_desde = NULL, "
					+ "       last_ok_at = CASE WHEN ? = 1 THEN SYSDATETIME() ELSE last_ok_at END, "
					+ "       actualizado_at = SYSDATETIME() "
					+ " WHERE cobis = ? AND operacion = ?;";
			rel.add(success ? 1 : 0);
			rel.add(cobis);
			rel.add(operacion);
			Sql.response(rel);
		} catch (Exception ignored) {}
	}

	/** (Opcional) Segundos restantes para poder operar de nuevo (cuenta forward para UI). */
	public static int remainingSecsCVUSD(int cobis, String operacion) {
		try {
			SqlRequest q = Sql.request("CVUSD_REMAINING", "homebanking");
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

			SqlResponse r = Sql.response(q);
			for (Objeto reg : r.registros) return reg.integer("wait_sec", 0);
			return 0;
		} catch (Exception e) {
			return 0;
		}
	}


}
