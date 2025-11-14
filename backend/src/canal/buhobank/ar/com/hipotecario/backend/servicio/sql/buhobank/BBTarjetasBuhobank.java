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
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBTarjetasBuhobank.BBTarjetaBuhobank;

@SuppressWarnings("serial")
public class BBTarjetasBuhobank extends SqlObjetos<BBTarjetaBuhobank> {

	/* ========== ATRIBUTOS ========== */
	public static class BBTarjetaBuhobank extends SqlObjeto {
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

		public BBTarjetasBeneficioBuhobank beneficios;
		public BBTarjetasBeneficioBuhobank beneficiosAdicionales;
	}

	public static BBTarjetaBuhobank buscarId(BBTarjetasBuhobank tarjetas, Integer id) {

		if (tarjetas == null || Utils.isEmpty(id)) {
			return null;
		}

		for (BBTarjetaBuhobank tarjeta : tarjetas) {

			if (id.equals(tarjeta.id)) {
				return tarjeta;
			}
		}

		return null;
	}

	private static BBTarjetasBuhobank buscarIdPaquete(BBTarjetasBuhobank tarjetasBuhobank, Integer idPaquete) {

		if (tarjetasBuhobank == null || Utils.isEmpty(idPaquete)) {
			return null;
		}

		BBTarjetasBuhobank tarjetaById = new BBTarjetasBuhobank();

		for (BBTarjetaBuhobank tarjeta : tarjetasBuhobank) {

			if (idPaquete.equals(tarjeta.id_paquete)) {
				tarjetaById.add(tarjeta);
			}
		}

		return tarjetaById;
	}

	private static Object[] obtenerParametros(Contexto contexto, BBTarjetaBuhobank tarjetaBuhobank, int cantidad) {

		Object[] parametros = new Object[cantidad];

		parametros[0] = !Util.empty(tarjetaBuhobank.id_paquete) ? tarjetaBuhobank.id_paquete : null;
		parametros[1] = !Util.empty(tarjetaBuhobank.tipo) ? tarjetaBuhobank.tipo : "";
		parametros[2] = !Util.empty(tarjetaBuhobank.nombre_corto) ? tarjetaBuhobank.nombre_corto : null;
		parametros[3] = !Util.empty(tarjetaBuhobank.descripcion) ? tarjetaBuhobank.descripcion : null;
		parametros[4] = !Util.empty(tarjetaBuhobank.virtual) ? tarjetaBuhobank.virtual : null;
		parametros[5] = !Util.empty(tarjetaBuhobank.tarjeta) ? tarjetaBuhobank.tarjeta : null;
		parametros[6] = !Util.empty(tarjetaBuhobank.titulo) ? tarjetaBuhobank.titulo : null;
		parametros[7] = !Util.empty(tarjetaBuhobank.titulo_landing) ? tarjetaBuhobank.titulo_landing : null;
		parametros[8] = !Util.empty(tarjetaBuhobank.box) ? tarjetaBuhobank.box : null;
		parametros[9] = !Util.empty(tarjetaBuhobank.boton) ? tarjetaBuhobank.boton : null;

		return parametros;
	}

	/* ========== SERVICIO ========== */
	public static BBTarjetaBuhobank getTarjeta(Contexto contexto, Integer idPaquete, Boolean esVirtual) {

		String sql = "";
		sql += "SELECT t1.numero_paquete, t2.* ";
		sql += "FROM [dbo].[bb_paquetes] AS t1 WITH (NOLOCK) ";
		sql += "INNER JOIN [dbo].[bb_tarjetas] AS t2 WITH (NOLOCK) ";
		sql += "ON t1.id = t2.id_paquete ";
		sql += "WHERE t2.id_paquete = ? ";
		sql += "AND t2.virtual = ? ";
		sql += "ORDER BY t1.id_plantilla_flujo, t2.id_paquete ASC ";

		Objeto datos = Sql.select(contexto, SqlBuhoBank.SQL, sql, idPaquete, esVirtual);
		SqlException.throwIf("PAQUETE_NO_ENCONTRADO", datos.isEmpty());

		BBTarjetaBuhobank tarjetaBuhobank = map(datos, BBTarjetasBuhobank.class, BBTarjetaBuhobank.class).first();
		tarjetaBuhobank.beneficios = BBTarjetasBeneficioBuhobank.getIdTarjeta(contexto, tarjetaBuhobank.id);
		tarjetaBuhobank.beneficiosAdicionales = BBTarjetasBeneficioBuhobank.getAdicionalesIdTarjeta(contexto, tarjetaBuhobank.id);
		
		return tarjetaBuhobank;
	}

	public static BBTarjetasBuhobank get(Contexto contexto) {

		String sql = "";
		sql += "SELECT t1.numero_paquete, t2.* ";
		sql += "FROM [dbo].[bb_paquetes] AS t1 WITH (NOLOCK) ";
		sql += "INNER JOIN [dbo].[bb_tarjetas] AS t2 WITH (NOLOCK) ";
		sql += "ON t1.id = t2.id_paquete ";
		sql += "ORDER BY t1.id_plantilla_flujo, t2.id_paquete ASC ";

		Objeto datos = Sql.select(contexto, SqlBuhoBank.SQL, sql);
		SqlException.throwIf("PAQUETE_NO_ENCONTRADO", datos.isEmpty());

		BBTarjetasBuhobank tarjetasBuhobank = map(datos, BBTarjetasBuhobank.class, BBTarjetaBuhobank.class);

		for (BBTarjetaBuhobank tarjeta : tarjetasBuhobank) {
			tarjeta.beneficios = BBTarjetasBeneficioBuhobank.getIdTarjeta(contexto, tarjeta.id);
			tarjeta.beneficiosAdicionales = BBTarjetasBeneficioBuhobank.getAdicionalesIdTarjeta(contexto, tarjeta.id);
		}

		return tarjetasBuhobank;
	}

	public static Boolean post(Contexto contexto, BBTarjetaBuhobank nuevaTarjeta) {

		String sql = "";
		sql += "INSERT INTO [dbo].[bb_tarjetas] ";
		sql += "([id_paquete], [tipo], [nombre_corto], [descripcion], [virtual], [tarjeta], ";
		sql += "[titulo], [titulo_landing], [box], [boton], ";
		sql += "[fecha_ultima_modificacion]) ";
		sql += "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, GETDATE())";

		Object[] parametros = obtenerParametros(contexto, nuevaTarjeta, 10);

		return Sql.update(contexto, SqlBuhoBank.SQL, sql, parametros) > 0;
	}

	public static Boolean put(Contexto contexto, BBTarjetaBuhobank tarjeta) {

		String sql = "";
		sql += "UPDATE [dbo].[bb_tarjetas] SET ";
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
		sql += "fecha_ultima_modificacion = GETDATE() ";
		sql += "WHERE id = ? ";

		Object[] parametros = obtenerParametros(contexto, tarjeta, 11);
		parametros[10] = tarjeta.id;

		return Sql.update(contexto, SqlBuhoBank.SQL, sql, parametros) > 0;
	}

	public static Boolean delete(Contexto contexto, Integer id) {

		String sql = "";
		sql += "DELETE [dbo].[bb_tarjetas] ";
		sql += "WHERE id = ? ";

		return Sql.update(contexto, SqlBuhoBank.SQL, sql, id) > 0;
	}

	public static void clonarTotal(Contexto contexto, Integer idPaqueteBase, Integer idPaquete) {

		BBTarjetasBuhobank tarjetasBuhobank = get(contexto);
		BBTarjetasBuhobank tarjetasBase = buscarIdPaquete(tarjetasBuhobank, idPaqueteBase);
		if (tarjetasBase == null || tarjetasBase.size() == 0) {
			return;
		}

		for (BBTarjetaBuhobank tarjetaBase : tarjetasBase) {

			BBTarjetaBuhobank nuevoTarjeta = clonarTarjeta(contexto, tarjetaBase.id, idPaquete);
			if (!Utils.isEmpty(nuevoTarjeta)) {
				BBTarjetasBeneficioBuhobank.clonar(contexto, tarjetaBase.id, nuevoTarjeta.id);
				BBTarjetasBeneficioBuhobank.clonarAdicionales(contexto, tarjetaBase.id, nuevoTarjeta.id);
				BBTarjetasFinalizarBuhobank.clonar(contexto, tarjetaBase.id, nuevoTarjeta.id);
				BBTarjetasFinalizarBatchBuhobank.clonar(contexto, tarjetaBase.id, nuevoTarjeta.id);
			}
		}
	}

	private static BBTarjetaBuhobank clonarTarjeta(Contexto contexto, Integer idTarjetaBase, Integer idPaquete) {

		String sql = "";
		sql += "INSERT [dbo].[bb_tarjetas] (id_paquete, tipo, nombre_corto, descripcion, virtual, tarjeta, titulo, titulo_landing, box, boton, fecha_ultima_modificacion) ";
		sql += "SELECT ? AS id_paquete, tipo, nombre_corto, descripcion, virtual, tarjeta, titulo, titulo_landing, box, boton, GETDATE() AS fecha_ultima_modificacion ";
		sql += "FROM [dbo].[bb_tarjetas] WITH (NOLOCK) ";
		sql += "WHERE id = ? ;";
		sql += "SELECT SCOPE_IDENTITY() AS id";

		Objeto datos = Sql.select(contexto, SqlBuhoBank.SQL, sql, idPaquete, idTarjetaBase);
		return map(datos, BBTarjetasBuhobank.class, BBTarjetaBuhobank.class).first();
	}
}
