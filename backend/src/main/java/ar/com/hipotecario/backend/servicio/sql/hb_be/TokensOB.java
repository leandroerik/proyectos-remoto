package ar.com.hipotecario.backend.servicio.sql.hb_be;

import java.util.UUID;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.sql.Sql;
import ar.com.hipotecario.backend.conector.sql.SqlObjeto;
import ar.com.hipotecario.backend.conector.sql.SqlObjetos;
import ar.com.hipotecario.backend.exception.SqlException;
import ar.com.hipotecario.backend.servicio.sql.hb_be.TokensOB.TokenOB;

@SuppressWarnings("serial")
public class TokensOB extends SqlObjetos<TokenOB> {

	/* ========== ATRIBUTOS ========== */
	public static class TokenOB extends SqlObjeto {
		public String uuid;
		public String cuit;
		public String cuil;
		public String origen;
		public Fecha fechaExpiracion;

		public Boolean expirado() {
			return fechaExpiracion.esAnterior(Fecha.ahora());
		}
	}

	/* ========== SERVICIO ========== */
	public static TokenOB get(Contexto contexto, String uuid) {
		String sql = "";
		sql += "SELECT * ";
		sql += "FROM [hb_be].[dbo].[OB_Token] ";
		sql += "WHERE uuid = ? ";
		Objeto datos = Sql.select(contexto, "hb_be", sql, uuid);
		SqlException.throwIf("TOKEN_INEXISTENTE", datos.isEmpty());
		return map(datos, TokensOB.class, TokenOB.class).first();
	}

	public static Boolean crear(Contexto contexto, String uuid, String cuit, String cuil, Fecha fechaExpiracion, String usuario) {
		String sql = "";
		sql += "INSERT INTO [hb_be].[dbo].[OB_Token] ";
		sql += "([uuid],[cuit],[cuil],[origen],[fechaExpiracion],[usuario]) ";
		sql += "VALUES (?, ?, ?, 'ob-nuevo', ?, ?) ";
		return Sql.update(contexto, "hb_be", sql, uuid, cuit, cuil, fechaExpiracion, usuario) == 1;
	}

	public static Boolean crear(Contexto contexto, String uuid, String cuit, String cuil, String tipoOperacion, Fecha fechaExpiracion, String usuario) {
		String sql = "";
		sql += "INSERT INTO [hb_be].[dbo].[OB_Token] ";
		sql += "([uuid],[cuit],[cuil],[origen],[fechaExpiracion],[usuario]) ";
		sql += "VALUES (?, ?, ?, ?, ?, ?) ";
		return Sql.update(contexto, "hb_be", sql, uuid, cuit, cuil, tipoOperacion, fechaExpiracion, usuario) == 1;
	}

	public static Boolean actualizarFechaExpiracion(Contexto contexto, String uuid, Fecha fechaExpiracion) {
		String sql = "";
		sql += "UPDATE [hb_be].[dbo].[OB_Token] SET ";
		sql += "fechaExpiracion = ? ";
		sql += "WHERE uuid = ? ";
		return Sql.update(contexto, "hb_be", sql, fechaExpiracion, uuid) == 1;
	}

	public static Boolean eliminar(Contexto contexto, String cuit, String cuil) {
		String sql = "";
		sql += "DELETE [hb_be].[dbo].[OB_Token] WHERE cuit = ? AND cuil = ?";
		return Sql.update(contexto, "hb_be", sql, cuit, cuil) == 1;
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		String test = "eliminar";
		if ("get".equals(test)) {
			Contexto contexto = contexto("OB", "desarrollo");
			TokenOB token = get(contexto, "30957926-1682-4ba4-a876-24ac8f693b25");
			imprimirResultado(contexto, token);
			System.out.println("EXPIRADO: " + token.expirado());
		}
		if ("crear".equals(test)) {
			Contexto contexto = contexto("OB", "desarrollo");
			String uuid = UUID.randomUUID().toString();
			String cuit = "301234567891";
			String cuil = "208765432101";
			String usu_login = "Febrero04";
			Fecha fechaExpiracion = Fecha.ahora().sumarMinutos(5);
			Boolean exito = crear(contexto, uuid, cuit, cuil, fechaExpiracion, usu_login);
			imprimirResultado(contexto, exito);
			System.out.println(uuid);
		}
		if ("eliminar".equals(test)) {
			Contexto contexto = contexto("OB", "desarrollo");
			String cuit = "301234567891";
			String cuil = "208765432101";
			Boolean exito = eliminar(contexto, cuit, cuil);
			imprimirResultado(contexto, exito);
		}
	}
}
