package ar.com.hipotecario.backend.conector.sql;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PropertyKey;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Base;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.exception.SqlException;
import ar.com.hipotecario.canal.tas.shared.modulos.depositos.models.ResponseSpAltaDepositos;

public class Sql extends Base {

	public static Objeto select(Contexto contexto, String base, String sql, Object... parametros) {
		Objeto datos = new Objeto().setList();
		try (Connection conexion = contexto.dataSource(base).getConnection()) {
			try (PreparedStatement ps = conexion.prepareStatement(sql)) {
				cargarParametros(ps, parametros);
				try (ResultSet rs = ps.executeQuery()) {
					List<String> columnas = columnas(rs);
					while (rs.next()) {
						Objeto registro = registro(rs, columnas);
						datos.add(registro);
					}
				}
			}
		} catch (Exception e) {
			throw new SqlException(e, sql, parametros);
		}
		return datos;
	}

	public static Integer update(Contexto contexto, String base, String sql, Object... parametros) {
		try (Connection conexion = contexto.dataSource(base).getConnection()) {
			try (PreparedStatement ps = conexion.prepareStatement(sql)) {
				cargarParametros(ps, parametros);
				return ps.executeUpdate();
			}
		} catch (Exception e) {
			throw new SqlException(e, sql, parametros);
		}
	}

	public static Map<String, Object> executeSpAltaDepositoTasi(Contexto contexto, String base, String sql,
			Object... parametros) {
		try (Connection conexion = contexto.dataSource(base).getConnection()) {
			try (CallableStatement cs = conexion.prepareCall(sql)) {
				cargarParametrosSpAltaDepositoTasi(cs, parametros);
				cs.execute();

				// Junto los valores de salida
				Map<String, Object> mapaSalida = new HashMap<>();

				for (int i = 0; i < parametros.length; i++) {
					if (parametros[i] instanceof ResponseSpAltaDepositos) {
						ResponseSpAltaDepositos parametrosSalida = (ResponseSpAltaDepositos) parametros[i];
						parametrosSalida.setValue(cs.getObject(i + 1));
						if (i == 1) {
							mapaSalida.put("NumeroTicket", parametrosSalida.getValue());
						} else {
							mapaSalida.put("DepositoId", parametrosSalida.getValue());
						}
					}
				}

				return mapaSalida;
			}
		} catch (Exception e) {
			throw new SqlException(e, sql, parametros);
		}
	}

	public static Integer insertGenerico(Contexto contexto, String base, String insert, Object parametro) {
		Field[] atributos = parametro.getClass().getDeclaredFields();
		List<Object> parametros = new ArrayList<>();
		StringBuilder sql = new StringBuilder();

		try {
			sql.append(insert).append(" (");
			for (Integer i = 0; i < atributos.length; ++i) {
				Field atributo = atributos[i];
				if (atributo.getAnnotation(PropertyKey.class) == null) {
					sql.append(atributo.getName());
					if (i + 1 < atributos.length) {
						sql.append(", "); // TODO: si la key est� al final no funciona
					}
				}
			}
			sql.append(") VALUES (");
			for (Integer i = 0; i < atributos.length; ++i) {
				Field atributo = atributos[i];
				if (atributo.getAnnotation(PropertyKey.class) == null) {
					sql.append("?");
					if (i + 1 < atributos.length) {
						sql.append(",");
					}
					Object valor = atributo.get(parametro);
					parametros.add(valor);
				}
			}
			sql.append(")");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return update(contexto, base, sql.toString(), parametros.toArray());
	}

	public static Integer updateGenerico(Contexto contexto, String base, String tabla, String condicion,
			Object original,
			Object actual, Object... parametrosCondicion) {
		Field[] atributos = original.getClass().getDeclaredFields();
		List<Object> parametros = new ArrayList<>();
		StringBuilder sql = new StringBuilder();

		try {
			sql.append("UPDATE ").append(tabla).append(" SET ");
			for (Integer i = 0; i < atributos.length; ++i) {
				Field atributo = atributos[i];
				Object valorOriginal = atributo.get(original);
				Object valorActual = atributo.get(actual);
				if (valorOriginal != null && !valorOriginal.equals(valorActual)) {
					sql.append(atributo.getName()).append(" = ?, ");
					parametros.add(valorActual);
				}
			}
			if (sql.charAt(sql.length() - 2) != ',') {
				return 0;
			}
			sql.delete(sql.length() - 2, sql.length() - 1);
			sql.append(condicion);
			for (Object parametro : parametrosCondicion) {
				parametros.add(parametro);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return update(contexto, base, sql.toString(), parametros.toArray());
	}

	/* ========== METODOS PRIVADOS ========== */
	private static Objeto registro(ResultSet rs, List<String> columnas) throws SQLException {
		Objeto registro = new Objeto();
		for (String columna : columnas) {
			Object valor = rs.getObject(columna);
			if (valor instanceof Date) {
				valor = ((Date) valor).getTime();
			}
			if (valor instanceof Timestamp) {
				valor = ((Timestamp) valor).getTime();
			}
			registro.set(columna, valor);
		}
		return registro;
	}

	private static List<String> columnas(ResultSet rs) throws SQLException {
		ResultSetMetaData metadata = rs.getMetaData();
		Integer cantidadColumnas = metadata.getColumnCount();
		List<String> columnas = new ArrayList<>();
		for (Integer i = 1; i <= cantidadColumnas; ++i) {
			columnas.add(metadata.getColumnName(i));
		}
		return columnas;
	}

	private static void cargarParametros(PreparedStatement ps, Object... parametros) throws SQLException {
		Integer i = 0;
		for (Object parametro : parametros) {
			if (parametro instanceof String) {
				ps.setString(++i, (String) parametro);
			} else if (parametro instanceof Integer) {
				ps.setInt(++i, (Integer) parametro);
			} else if (parametro instanceof Long) {
				ps.setLong(++i, (Long) parametro);
			} else if (parametro instanceof BigDecimal) {
				ps.setBigDecimal(++i, (BigDecimal) parametro);
			} else if (parametro instanceof Fecha) {
				ps.setTimestamp(++i, ((Fecha) parametro).timestamp());
			} else {
				ps.setObject(++i, parametro);
			}
		}
	}

	private static void cargarParametrosSpAltaDepositoTasi(CallableStatement cs, Object... parametros)
			throws SQLException {
		int i = 1; // Note the change to 1-based indexing for CallableStatement
		for (Object parametro : parametros) {
			if (parametro instanceof String) {
				cs.setString(i, (String) parametro);
			} else if (parametro instanceof Integer) {
				cs.setInt(i, (Integer) parametro);
			} else if (parametro instanceof Long) {
				cs.setLong(i, (Long) parametro);
			} else if (parametro instanceof BigDecimal) {
				cs.setBigDecimal(i, (BigDecimal) parametro);
			} else if (parametro instanceof Timestamp) {
				cs.setTimestamp(i, (Timestamp) parametro);
			} else if (parametro instanceof ResponseSpAltaDepositos) {
				ResponseSpAltaDepositos outputParam = (ResponseSpAltaDepositos) parametro;
				cs.registerOutParameter(i, outputParam.getSqlType());
			} else {
				cs.setObject(i, parametro);
			}
			i++;
		}
	}

}
