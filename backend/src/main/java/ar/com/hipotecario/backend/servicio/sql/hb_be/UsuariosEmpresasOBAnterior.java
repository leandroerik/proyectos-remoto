package ar.com.hipotecario.backend.servicio.sql.hb_be;

import java.util.List;
import java.util.stream.Collectors;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.sql.Sql;
import ar.com.hipotecario.backend.conector.sql.SqlObjeto;
import ar.com.hipotecario.backend.conector.sql.SqlObjetos;
import ar.com.hipotecario.backend.exception.SqlException;
import ar.com.hipotecario.backend.servicio.sql.hb_be.UsuariosEmpresasOBAnterior.UsuarioEmpresaOBAnterior;

@SuppressWarnings("serial")
public class UsuariosEmpresasOBAnterior extends SqlObjetos<UsuarioEmpresaOBAnterior> {

	/* ========== ATRIBUTOS ========== */
	public static class UsuarioEmpresaOBAnterior extends SqlObjeto {
		public String usu_codigo;
		public String emp_cuit;
		public String rol_id;
		public String esu_codigo;
		public String emp_nombre;
	}

	public static UsuariosEmpresasOBAnterior getPorCuil(Contexto contexto, String cuil) {
		String sql = "";
		sql += "SELECT a.usu_codigo, b.emp_cuit, a.rol_id, a.esu_codigo, c.emp_nombre ";
		sql += "FROM [HB_BE].[dbo].[BE_Usuario] a ";
		sql += "INNER JOIN [HB_BE].[dbo].[BE_Usuario_Empresa] b ON a.usu_codigo = b.usu_codigo ";
		sql += "INNER JOIN [HB_BE].[dbo].[BE_Empresa] c ON b.emp_cuit = c.emp_cuit  ";
		sql += "WHERE a.usu_cuil = ? ";
		Objeto datos = Sql.select(contexto, "hb_be", sql, cuil);
		SqlException.throwIf("CUIT_INVALIDO", datos.isEmpty());
		return map(datos, UsuariosEmpresasOBAnterior.class, UsuarioEmpresaOBAnterior.class);
	}

	public List<UsuarioEmpresaOBAnterior> buscarPorEstado(String estado) {
		return this.stream().filter(usu -> usu.esu_codigo.equals(estado)).collect(Collectors.toList());
	}

	public List<Objeto> buscarObjetosPorEstado(String estado) {
		List<Objeto> empresas = this.buscarPorEstado(estado).stream().map(empresa -> {
			Objeto empresaObj = new Objeto();
			empresaObj.set("cuit", empresa.emp_cuit);
			empresaObj.set("razon_social", empresa.emp_nombre);
			return empresaObj;
		}).collect(Collectors.toList());
		return empresas;
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "desarrollo");
		imprimirResultado(contexto, getPorCuil(contexto, "20126114746"));
	}
}