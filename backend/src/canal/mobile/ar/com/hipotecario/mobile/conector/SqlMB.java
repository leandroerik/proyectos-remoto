package ar.com.hipotecario.mobile.conector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ar.com.hipotecario.mobile.CanalMobile;
import ar.com.hipotecario.mobile.ContextoMB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;


import ar.com.hipotecario.mobile.ConfigMB;
import ar.com.hipotecario.mobile.lib.Objeto;

public class SqlMB {

	/* ========== ATRIBUTOS ========== */
	private static Logger log = LoggerFactory.getLogger(SqlMB.class);
	public static Boolean habilitarLog = ConfigMB.bool("habilitar_logs");
	private static Map<String, HikariDataSource> mapa = new HashMap<>();

	/* ========== CONEXION ========== */
	public static Connection conexion(String url, String usuario, String clave) throws SQLException {
		HikariDataSource dataSource = mapa.get(url);
		if (dataSource == null) {
			HikariConfig config = new HikariConfig();
			config.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			config.setJdbcUrl(url);
			config.setUsername(usuario);
			config.setPassword(clave);
			config.setMinimumIdle(ConfigMB.integer("sql_min_conection", ConfigMB.esARO() ? 10 : 1));
			config.setMaximumPoolSize(ConfigMB.integer("sql_max_conection", ConfigMB.esARO() ? 100 : 20));
			dataSource = new HikariDataSource(config);
			mapa.put(url, dataSource);
		}
		return dataSource.getConnection();
	}

	/* ========== REQUEST ========== */
	public static SqlRequestMB request(String servicio, String baseDatos) {
		return request(servicio, baseDatos, "");
	}

	public static SqlRequestMB request(String servicio, String baseDatos, String sql) {
		SqlRequestMB request = new SqlRequestMB();
		request.servicio = servicio;
		request.servidor = ConfigMB.string("sql_" + baseDatos + "_url");
		request.usuario = ConfigMB.string("sql_" + baseDatos + "_usuario");
		request.clave = ConfigMB.string("sql_" + baseDatos + "_clave");
		request.sql = sql;
		return request;
	}

	/* ========== RESPONSE ========== */
	@SuppressWarnings("rawtypes")
	public static SqlResponseMB response(SqlRequestMB request) {
		Long inicio = new Date().getTime();
		SqlResponseMB response = new SqlResponseMB();
		try (Connection conexion = conexion(request.servidor, request.usuario, request.clave)) {
			try (PreparedStatement ps = conexion.prepareStatement(request.sql)) {
				for (int i = 1; i <= request.parametros.size(); ++i) {
					Object parametro = request.parametros.get(i - 1);
					if (parametro instanceof List) {
						ps.setArray(i, conexion.createArrayOf("VARCHAR", ((List) parametro).toArray()));
					} else {
						ps.setObject(i, parametro);
					}
				}
				ps.execute();
				do {
					ResultSet resultSet = ps.getResultSet();
					if (resultSet != null) {
						try (ResultSet rs = ps.getResultSet()) {
							ResultSetMetaData rsmd = rs.getMetaData();
							while (rs.next()) {
								Objeto objeto = new Objeto();
								for (int i = 1; i <= rsmd.getColumnCount(); ++i) {
									String clave = rsmd.getColumnName(i);
									Object valor = rs.getObject(i);
									objeto.set(clave, valor);
								}
								response.registros.add(objeto);
							}
						} catch (Exception e) {
							if (habilitarLog) {
								log.error("ResultSet: ", e);
							}
							response.hayError = true;
						}
					}
				} while (ps.getMoreResults());
			} catch (Exception e) {
				if (habilitarLog) {
					log.error("PreparedStatement: ", e);
				}
				response.hayError = true;
			}
		} catch (Exception e) {
			if (habilitarLog) {
				log.error("Connection: ", e);
			}
			response.hayError = true;
		}
		Long fin = new Date().getTime();
		ContextoMB contexto = CanalMobile.threadLocal.get();
		if (contexto != null) {
			String head = "x-";
			if (Thread.currentThread().getName().equals(contexto.hiloPrincipal)) {
				head += "0";
			} else {
				Integer tid = Math.abs(Thread.currentThread().hashCode());
				head += String.format("%010d", tid);
			}
			head += "-sql-" + request.servicio;
			head = head.replaceAll("[^a-zA-Z0-9_-]", "");
			contexto.mapaInvocaciones.put(head, (fin - inicio) + "ms");
		}
		return response;
	}
}
