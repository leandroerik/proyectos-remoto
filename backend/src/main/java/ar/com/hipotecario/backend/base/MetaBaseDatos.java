package ar.com.hipotecario.backend.base;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class MetaBaseDatos {

	/* ========== CLASES ========== */
	public static class BaseDatos {
		public String nombre;
	}

	public static class Tabla {
		public String nombre;
	}

	public static class Columna {
		public String nombre;
		public String tipo;
		public Integer tamaño;
	}

	public static class StoredProcedure {
		public String nombre;
	}

	/* ========== METODOS ========== */
	public static DataSource dataSource(String url, String usuario, String clave) {
		HikariConfig config = new HikariConfig();
		config.setJdbcUrl(url);
		config.setUsername(usuario);
		config.setPassword(clave);
		HikariDataSource hikariDataSource = new HikariDataSource(config);
		return hikariDataSource;
	}

	public static List<BaseDatos> basesDatos(DataSource dataSource) {
		List<BaseDatos> basesDatos = new ArrayList<>();
		try (Connection conexion = dataSource.getConnection()) {
			DatabaseMetaData metadatos = conexion.getMetaData();
			try (ResultSet rs = metadatos.getCatalogs()) {
				while (rs.next()) {
					BaseDatos baseDatos = new BaseDatos();
					baseDatos.nombre = rs.getString("TABLE_CAT");
					basesDatos.add(baseDatos);
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return basesDatos;
	}

	private static List<Tabla> tablas(DataSource dataSource, String baseDatos) {
		List<Tabla> tablas = new ArrayList<>();
		try (Connection conexion = dataSource.getConnection()) {
			DatabaseMetaData metadatos = conexion.getMetaData();
			try (ResultSet rs = metadatos.getTables(baseDatos, null, null, new String[] { "TABLE" })) {
				while (rs.next()) {
					Tabla tabla = new Tabla();
					tabla.nombre = rs.getString("TABLE_NAME");
					tablas.add(tabla);
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return tablas;
	}

	private static List<Tabla> vistas(DataSource dataSource, String baseDatos) {
		List<Tabla> vistas = new ArrayList<>();
		try (Connection conexion = dataSource.getConnection()) {
			DatabaseMetaData metadatos = conexion.getMetaData();
			try (ResultSet rs = metadatos.getTables(baseDatos, null, null, new String[] { "VIEW" })) {
				while (rs.next()) {
					if (!Util.set("sys", "INFORMATION_SCHEMA").contains(rs.getString("TABLE_SCHEM"))) {
						Tabla vista = new Tabla();
						vista.nombre = rs.getString("TABLE_NAME");
						vistas.add(vista);
					}
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return vistas;
	}

	private static List<Columna> columnas(DataSource dataSource, String baseDatos, String tabla) {
		List<Columna> columnas = new ArrayList<>();
		try (Connection conexion = dataSource.getConnection()) {
			DatabaseMetaData metadatos = conexion.getMetaData();
			try (ResultSet rs = metadatos.getColumns(baseDatos, null, tabla, null)) {
				while (rs.next()) {
					Columna columna = new Columna();
					columna.nombre = rs.getString("COLUMN_NAME");
					columna.tipo = rs.getString("TYPE_NAME");
					columna.tamaño = rs.getInt("COLUMN_SIZE");
					columnas.add(columna);
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return columnas;
	}

	public static List<StoredProcedure> storedProcedures(DataSource dataSource, String baseDatos) {
		List<StoredProcedure> storedProcedures = new ArrayList<>();
		try (Connection conexion = dataSource.getConnection()) {
			DatabaseMetaData metadatos = conexion.getMetaData();
			try (ResultSet rs = metadatos.getProcedures(baseDatos, null, null)) {
				while (rs.next()) {
					if (!Util.set("sys").contains(rs.getString("PROCEDURE_SCHEM"))) {
						StoredProcedure storedProcedure = new StoredProcedure();
						storedProcedure.nombre = rs.getString("PROCEDURE_NAME");
						if (storedProcedure.nombre.endsWith(";0") || storedProcedure.nombre.endsWith(";1")) {
							storedProcedure.nombre = storedProcedure.nombre.substring(0, storedProcedure.nombre.length() - 2);
						}
						storedProcedures.add(storedProcedure);
					}
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return storedProcedures;
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		basicTest();
//		reporte("HB_BE");
	}

	public static void basicTest() {
		DataSource dataSoruce = MetaBaseDatos.dataSource("jdbc:sqlserver://homomssql16:1985;databaseName=hb_be", "DTSCCS", "Porongui2");

		// Bases de datos
		System.out.println("/* ========== BASES ========== */");
		List<BaseDatos> basesDatos = MetaBaseDatos.basesDatos(dataSoruce);
		for (BaseDatos baseDatos : basesDatos) {
			System.out.println(baseDatos.nombre);
		}
		System.out.println();

		// Tablas
		System.out.println("/* ========== TABLAS (HB_BE) ========== */");
		List<Tabla> tablas = MetaBaseDatos.tablas(dataSoruce, "HB_BE");
		for (Tabla tabla : tablas) {
			System.out.println(tabla.nombre);
		}
		System.out.println();

		// Columnas
		System.out.println("/* ========== COLUMNAS (HB_BE, OB_Empresas) ========== */");
		List<Columna> columnas = MetaBaseDatos.columnas(dataSoruce, "HB_BE", "OB_Empresas");
		for (Columna columna : columnas) {
			System.out.println(String.format("%s[%s] %s", columna.tipo, columna.tamaño, columna.nombre));
		}
		System.out.println();

		// Stored Procedure
		System.out.println("/* ========== STORED PROCEDURE (HB_BE) ========== */");
		List<StoredProcedure> storedProcedures = MetaBaseDatos.storedProcedures(dataSoruce, "HB_BE");
		for (StoredProcedure storedProcedure : storedProcedures) {
			System.out.println(storedProcedure.nombre);
		}
		System.out.println();
	}

	public static void reporte(String baseDatos) {
		DataSource dataSoruce = MetaBaseDatos.dataSource("jdbc:sqlserver://homomssql16:1985;databaseName=hb_be", "sql2kccs", "sql2kccs");

		System.out.println(String.format("/* ========== TABLAS (%s) ========== */", baseDatos));
		List<Tabla> tablas = MetaBaseDatos.tablas(dataSoruce, baseDatos);
		for (Tabla tabla : tablas) {
			System.out.println(tabla.nombre);
			List<Columna> columnas = MetaBaseDatos.columnas(dataSoruce, baseDatos, tabla.nombre);
			for (Columna columna : columnas) {
				System.out.println(String.format("%20s\t%s", columna.tipo + "[" + columna.tamaño + "]", columna.nombre));
			}
			System.out.println();
		}
		System.out.println();

		System.out.println(String.format("/* ========== VISTAS (%s) ========== */", baseDatos));
		List<Tabla> vistas = MetaBaseDatos.vistas(dataSoruce, baseDatos);
		for (Tabla vista : vistas) {
			System.out.println(vista.nombre);
			List<Columna> columnas = MetaBaseDatos.columnas(dataSoruce, baseDatos, vista.nombre);
			for (Columna columna : columnas) {
				System.out.println(String.format("%20s\t%s", columna.tipo + "[" + columna.tamaño + "]", columna.nombre));
			}
			System.out.println();
		}
		System.out.println();

		System.out.println(String.format("/* ========== STORED PROCEDURE (%s) ========== */", baseDatos));
		List<StoredProcedure> storedProcedures = MetaBaseDatos.storedProcedures(dataSoruce, baseDatos);
		for (StoredProcedure storedProcedure : storedProcedures) {
			System.out.println(String.format("%s", storedProcedure.nombre));
		}
		System.out.println();
	}
}
