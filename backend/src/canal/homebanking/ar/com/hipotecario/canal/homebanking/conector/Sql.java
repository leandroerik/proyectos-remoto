package ar.com.hipotecario.canal.homebanking.conector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import ar.com.hipotecario.canal.homebanking.CanalHomeBanking;
import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.base.ConfigHB;
import ar.com.hipotecario.canal.homebanking.base.Objeto;

public class Sql {

	/* ========== ATRIBUTOS ========== */
//	private static Logger log = LoggerFactory.getLogger(Sql.class);
	private static Map<String, HikariDataSource> mapa = new HashMap<>();

	/* ========== CONEXION ========== */
	public static Connection conexion(String url, String usuario, String clave) throws SQLException {
		HikariDataSource dataSource = mapa.get(url);
		if (dataSource == null) {
			if (usuario.contains("\\")) {
				usuario = usuario.substring(usuario.indexOf("\\") + 1);
			}
			HikariConfig config = new HikariConfig();
			config.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			config.setJdbcUrl(url);
			config.setUsername(usuario);
			config.setPassword(clave);
			config.setMinimumIdle(ConfigHB.integer("sql_min_conection", 1));
			config.setMaximumPoolSize(ConfigHB.integer("sql_max_conection", 20));
			dataSource = new HikariDataSource(config);
			mapa.put(url, dataSource);
		}
		return dataSource.getConnection();
	}

	/* ========== REQUEST ========== */
	public static SqlRequest request(String servicio, String baseDatos) {
		return request(servicio, baseDatos, "");
	}

	public static SqlRequest request(String servicio, String baseDatos, String sql) {
		SqlRequest request = new SqlRequest();
		request.servicio = servicio;
		request.servidor = ConfigHB.string("sql_" + baseDatos + "_url");
		request.usuario = ConfigHB.string("sql_" + baseDatos + "_usuario");
		request.clave = ConfigHB.string("sql_" + baseDatos + "_clave");
		request.sql = sql;
		return request;
	}

	/* ========== RESPONSE ========== */
	@SuppressWarnings("rawtypes")
	public static SqlResponse response(SqlRequest request) {
//		System.out.println(request.sql);
		SqlResponse response = new SqlResponse();
		Long inicio = new Date().getTime();
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
							response.exception = e;
							response.hayError = true;
						}
					}
				} while (ps.getMoreResults());
			} catch (Exception e) {
				response.exception = e;
				response.hayError = true;
			}
		} catch (Exception e) {
			response.exception = e;
			response.hayError = true;
		}
		Long fin = new Date().getTime();
		ContextoHB contexto = CanalHomeBanking.threadLocal.get();
		if (contexto != null) {
//			String momento = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date());
//			String uriFront = "GET:" + contexto.request.headers("uri");
//			String uriBack = contexto.request.requestMethod() + ":" + contexto.request.uri();
//			String sql = request.sql;

//			String evento = momento + ": " + uriFront + " | " + uriBack + " | " + sql + " | " + (fin - inicio) + "ms";
//			Eventos.registrar(contexto.idCobis(), evento);

			try {
				Set<String> serviciosNoLog = new HashSet<>();
				serviciosNoLog.add("InsertLog");
				serviciosNoLog.add("InsertarTiempos");
				serviciosNoLog.add("RegistrarServicios");
				serviciosNoLog.add("InsertLogError");
				
				if (!serviciosNoLog.contains(request.servicio)) {
					String head = "x-";
					if (Thread.currentThread().getName().equals(contexto.hiloPrincipal)) {
						head += "0";
					} else {
						Integer tid = Math.abs(Thread.currentThread().hashCode());
						head += String.format("%010d", tid);
					}
					head += "-sql-" + request.servicio.replace(" ", "-");
					head = head.replaceAll("[^a-zA-Z0-9_-]", "");
					contexto.mapaInvocaciones.put(head, (fin - inicio) + "ms");
				}
			} catch (Throwable t) {
			}
		}
		return response;
	}
}
