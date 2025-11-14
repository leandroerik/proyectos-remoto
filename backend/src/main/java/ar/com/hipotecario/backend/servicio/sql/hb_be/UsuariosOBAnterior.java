package ar.com.hipotecario.backend.servicio.sql.hb_be;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.sql.Sql;
import ar.com.hipotecario.backend.conector.sql.SqlObjeto;
import ar.com.hipotecario.backend.conector.sql.SqlObjetos;
import ar.com.hipotecario.backend.exception.SqlException;
import ar.com.hipotecario.backend.servicio.sql.hb_be.UsuariosOBAnterior.UsuarioOBAnterior;

@SuppressWarnings("serial")
public class UsuariosOBAnterior extends SqlObjetos<UsuarioOBAnterior> {

	/* ========== ATRIBUTOS ========== */
	public static class UsuarioOBAnterior extends SqlObjeto {
		public String usu_codigo;
		public String usu_cuil;
		public String usu_idCobis;
		public String usu_compania_movil;
		public String valido_otp;
		public String usu_email_validado;
		public String usu_adherido_gire;
		public String usu_nombre;
		public String usu_apellido;
		public String usu_telefono_laboral;
		public String usu_telefono_movil;
		public Fecha usu_fecha_creacion;
		public String usu_login;
		public String usu_email;
		public String esu_codigo;
		public Fecha usu_ultimo_acceso;
		public Integer usu_intentos;
	}

	/* ========== SERVICIO ========== */
	public static UsuarioOBAnterior getPorUsuarioYCuit(Contexto contexto, String usuario, Long cuit) {
		String sql = "";
		sql += "SELECT b.usu_codigo, b.usu_cuil, b.usu_idCobis ";
		sql += "FROM [HB_BE].[dbo].[BE_Usuario_Empresa] a ";
		sql += "JOIN [HB_BE].[dbo].[BE_Usuario] b ON a.usu_codigo = b.usu_codigo ";
		sql += "WHERE b.usu_login = ? AND a.emp_cuit = ? ";
		sql += "AND b.esu_codigo = 1;";
		Objeto datos = Sql.select(contexto, "hb_be", sql, usuario, cuit);
		SqlException.throwIf("ERROR", datos.isEmpty());
		return map(datos, UsuariosOBAnterior.class, UsuarioOBAnterior.class).first();
	}

	public static UsuarioOBAnterior getPorCuil(Contexto contexto, String cuil) {
		String sql = "";
		sql += "SELECT u.* ";
		sql += "FROM [HB_BE].[dbo].[BE_Usuario] u ";
		sql += "WHERE usu_cuil = ?";
		Objeto datos = Sql.select(contexto, "hb_be", sql, cuil);
		SqlException.throwIf("NO_EXISTE_USUARIO", datos.isEmpty());
		return map(datos, UsuariosOBAnterior.class, UsuarioOBAnterior.class).first();
	}

	public static UsuarioOBAnterior getPorCuilYCuit(Contexto contexto, String cuil, Long cuit) {
		String sql = "";
		sql += "SELECT b.* ";
		sql += "FROM [HB_BE].[dbo].[BE_Usuario_Empresa] a ";
		sql += "JOIN [HB_BE].[dbo].[BE_Usuario] b ON a.usu_codigo = b.usu_codigo ";
		sql += "WHERE b.usu_cuil = ? AND a.emp_cuit = ? ";
		sql += "AND b.esu_codigo = 1;";
		Objeto datos = Sql.select(contexto, "hb_be", sql, cuil, cuit);
		SqlException.throwIf("ERROR", datos.isEmpty());
		return map(datos, UsuariosOBAnterior.class, UsuarioOBAnterior.class).first();
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "desarrollo");
		imprimirResultado(contexto, getPorCuil(contexto, "20125859063"));
	}
}