package ar.com.hipotecario.backend.servicio.sql.buhobank;

import com.github.jknack.handlebars.Handlebars.Utils;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.base.Util;
import ar.com.hipotecario.backend.conector.sql.Sql;
import ar.com.hipotecario.backend.conector.sql.SqlObjeto;
import ar.com.hipotecario.backend.conector.sql.SqlObjetos;
import ar.com.hipotecario.backend.exception.SqlException;
import ar.com.hipotecario.backend.servicio.sql.SqlBuhoBank;
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBTarjetasLandingBuhobank.BBTarjetaLandingBuhobank;

@SuppressWarnings("serial")
public class BBTarjetasLandingBuhobank extends SqlObjetos<BBTarjetaLandingBuhobank> {

	/* ========== ATRIBUTOS ========== */
	public static class BBTarjetaLandingBuhobank extends SqlObjeto {
		public Integer numero_paquete;

		public Integer id;
		public Integer id_paquete;
		public String tipo;
		public String nombre_corto;
		public String descripcion;
		public Boolean virtual;
		public String tarjeta;
		public String titulo;
		public String titulo_landing;
		public String box;
		public String boton;
		public Integer prioridad;

		public BBTarjetasBeneficioLandingBuhobank beneficios;
	}

	public static BBTarjetaLandingBuhobank buscarId(BBTarjetasLandingBuhobank tarjetas, Integer id) {

		if (tarjetas == null || Utils.isEmpty(id)) {
			return null;
		}

		for (BBTarjetaLandingBuhobank tarjeta : tarjetas) {

			if (id.equals(tarjeta.id)) {
				return tarjeta;
			}
		}

		return null;
	}

	public static BBTarjetasLandingBuhobank ordernar(BBTarjetasLandingBuhobank tarjetasBuhobank) {

		if (tarjetasBuhobank == null) {
			return new BBTarjetasLandingBuhobank();
		}

		int n = tarjetasBuhobank.size();
		for (int i = 0; i < n - 1; i++) {
			for (int j = 0; j < n - i - 1; j++) {
				if (tarjetasBuhobank.get(j).prioridad > tarjetasBuhobank.get(j + 1).prioridad) {
					BBTarjetaLandingBuhobank temp = tarjetasBuhobank.get(j);
					tarjetasBuhobank.set(j, tarjetasBuhobank.get(j + 1));
					tarjetasBuhobank.set(j + 1, temp);
				}
			}
		}

		return tarjetasBuhobank;
	}

	private static Object[] obtenerParametros(Contexto contexto, BBTarjetaLandingBuhobank tarjetaBuhobank, int cantidad) {

		Object[] parametros = new Object[cantidad];

		parametros[0] = !Util.empty(tarjetaBuhobank.id_paquete) ? tarjetaBuhobank.id_paquete : null;
		parametros[1] = !Util.empty(tarjetaBuhobank.tipo) ? tarjetaBuhobank.tipo : null;
		parametros[2] = !Util.empty(tarjetaBuhobank.nombre_corto) ? tarjetaBuhobank.nombre_corto : null;
		parametros[3] = !Util.empty(tarjetaBuhobank.descripcion) ? tarjetaBuhobank.descripcion : null;
		parametros[4] = !Util.empty(tarjetaBuhobank.virtual) ? tarjetaBuhobank.virtual : null;
		parametros[5] = !Util.empty(tarjetaBuhobank.tarjeta) ? tarjetaBuhobank.tarjeta : null;
		parametros[6] = !Util.empty(tarjetaBuhobank.titulo) ? tarjetaBuhobank.titulo : null;
		parametros[7] = !Util.empty(tarjetaBuhobank.titulo_landing) ? tarjetaBuhobank.titulo_landing : null;
		parametros[8] = !Util.empty(tarjetaBuhobank.box) ? tarjetaBuhobank.box : null;
		parametros[9] = !Util.empty(tarjetaBuhobank.boton) ? tarjetaBuhobank.boton : null;
		parametros[10] = !Util.empty(tarjetaBuhobank.prioridad) ? tarjetaBuhobank.prioridad : null;

		return parametros;
	}

	/* ========== SERVICIO ========== */
	public static BBTarjetaLandingBuhobank getTarjeta(Contexto contexto, String tipo, Integer idPaquete, Boolean esVirtual) {

		String sql = "";
		sql += "SELECT t1.numero_paquete, t2.* ";
		sql += "FROM [dbo].[bb_paquetes] AS t1 WITH (NOLOCK) ";
		sql += "INNER JOIN [dbo].[bb_tarjetas_landing] AS t2 WITH (NOLOCK) ";
		sql += "ON t1.id = t2.id_paquete ";
		sql += "WHERE t2.tipo = ? ";
		sql += "AND t2.id_paquete = ? ";
		sql += "AND t2.virtual = ? ";
		sql += "ORDER BY t1.id_plantilla_flujo, t2.id_paquete ASC ";

		Objeto datos = Sql.select(contexto, SqlBuhoBank.SQL, sql, tipo, idPaquete, esVirtual);
		SqlException.throwIf("PAQUETE_NO_ENCONTRADO", datos.isEmpty());

		BBTarjetaLandingBuhobank tarjetaBuhobank = map(datos, BBTarjetasLandingBuhobank.class, BBTarjetaLandingBuhobank.class).first();
		tarjetaBuhobank.beneficios = BBTarjetasBeneficioLandingBuhobank.getIdTarjeta(contexto, tarjetaBuhobank.id);

		return tarjetaBuhobank;
	}

	public static BBTarjetasLandingBuhobank get(Contexto contexto) {

		String sql = "";
		sql += "SELECT t1.numero_paquete, t2.* ";
		sql += "FROM [dbo].[bb_paquetes] AS t1 WITH (NOLOCK) ";
		sql += "INNER JOIN [dbo].[bb_tarjetas_landing] AS t2 WITH (NOLOCK) ";
		sql += "ON t1.id = t2.id_paquete ";
		sql += "ORDER BY t1.id_plantilla_flujo, t2.id_paquete ASC ";

		Objeto datos = Sql.select(contexto, SqlBuhoBank.SQL, sql);
		SqlException.throwIf("PAQUETE_NO_ENCONTRADO", datos.isEmpty());

		BBTarjetasLandingBuhobank tarjetasBuhobank = map(datos, BBTarjetasLandingBuhobank.class, BBTarjetaLandingBuhobank.class);

		for (BBTarjetaLandingBuhobank tarjeta : tarjetasBuhobank) {

			tarjeta.beneficios = BBTarjetasBeneficioLandingBuhobank.getIdTarjeta(contexto, tarjeta.id);
		}

		return tarjetasBuhobank;
	}

	public static Boolean post(Contexto contexto, BBTarjetaLandingBuhobank nuevaTarjeta) {

		String sql = "";
		sql += "INSERT INTO [dbo].[bb_tarjetas_landing] ";
		sql += "([id_paquete], [tipo], [nombre_corto], [descripcion], [virtual], [tarjeta], ";
		sql += "[titulo], [titulo_landing], [box], [boton], [prioridad], ";
		sql += "[fecha_ultima_modificacion]) ";
		sql += "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, GETDATE())";

		Object[] parametros = obtenerParametros(contexto, nuevaTarjeta, 11);

		return Sql.update(contexto, SqlBuhoBank.SQL, sql, parametros) > 0;
	}

	public static Boolean put(Contexto contexto, BBTarjetaLandingBuhobank tarjeta) {

		String sql = "";
		sql += "UPDATE [dbo].[bb_tarjetas_landing] SET ";
		sql += "id_paquete = ? ,";
		sql += "tipo = ? ,";
		sql += "nombre_corto = ? ,";
		sql += "descripcion = ? ,";
		sql += "virtual = ? ,";
		sql += "tarjeta = ? ,";
		sql += "titulo = ? ,";
		sql += "titulo_landing = ? ,";
		sql += "box = ? ,";
		sql += "boton = ? ,";
		sql += "prioridad = ? ,";
		sql += "fecha_ultima_modificacion = GETDATE() ";
		sql += "WHERE id = ? ";

		Object[] parametros = obtenerParametros(contexto, tarjeta, 12);
		parametros[11] = tarjeta.id;

		return Sql.update(contexto, SqlBuhoBank.SQL, sql, parametros) > 0;
	}

	public static Boolean delete(Contexto contexto, Integer id) {

		String sql = "";
		sql += "DELETE [dbo].[bb_tarjetas_landing] ";
		sql += "WHERE id = ? ";

		return Sql.update(contexto, SqlBuhoBank.SQL, sql, id) > 0;
	}
}
