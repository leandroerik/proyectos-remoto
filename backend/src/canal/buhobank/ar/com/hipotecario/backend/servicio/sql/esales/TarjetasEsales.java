package ar.com.hipotecario.backend.servicio.sql.esales;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.sql.Sql;
import ar.com.hipotecario.backend.conector.sql.SqlObjeto;
import ar.com.hipotecario.backend.conector.sql.SqlObjetos;
import ar.com.hipotecario.backend.exception.SqlException;
import ar.com.hipotecario.backend.servicio.sql.SqlEsales;
import ar.com.hipotecario.backend.servicio.sql.esales.TarjetasEsales.TarjetaEsales;

@SuppressWarnings("serial")
public class TarjetasEsales extends SqlObjetos<TarjetaEsales> {

	/* ========== ATRIBUTOS ========== */
	public static class TarjetaEsales extends SqlObjeto {
		public String id;
		public String nombre;
		public String descripcion;
		public String detalle;
		public String url_imagen;
		public String estado;
		public String legales;
		public Fecha fecha;

		public Boolean estaActiva() {
			return estado.substring(0, 1).toLowerCase().equals("t");
		}
	}

	/* ========== SERVICIO ========== */
	public static TarjetaEsales get(Contexto contexto, Integer id) {
		String sql = "";
		sql += "SELECT * ";
		sql += "FROM [esales].[dbo].[tarjeta] WITH (NOLOCK) ";
		sql += "WHERE id = ?";

		Objeto datos = Sql.select(contexto, SqlEsales.SQL, sql, id);
		SqlException.throwIf("TARJETA_NO_ENCONTRADA", datos.isEmpty());

		return map(datos, TarjetasEsales.class, TarjetaEsales.class).first();
	}

	public static TarjetaEsales getPorInicial(Contexto contexto, String inicial) {
		String sql = "";
		sql += "SELECT * ";
		sql += "FROM [esales].[dbo].[tarjeta] WITH (NOLOCK) ";
		sql += "WHERE nombre LIKE ?";

		Objeto datos = Sql.select(contexto, SqlEsales.SQL, sql, inicial + "%");
		SqlException.throwIf("TARJETA_NO_ENCONTRADA", datos.isEmpty());

		return map(datos, TarjetasEsales.class, TarjetaEsales.class).first();
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("BB", "homologacion");
		Boolean porInicial = true;

		if (porInicial) {
			TarjetaEsales datos = getPorInicial(contexto, "S");
			imprimirResultado(contexto, datos);
			return;
		}

		TarjetaEsales datos = get(contexto, 4);
		imprimirResultado(contexto, datos);
	}
}
