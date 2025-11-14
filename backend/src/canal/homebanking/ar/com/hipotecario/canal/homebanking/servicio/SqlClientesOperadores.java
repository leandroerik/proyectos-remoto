package ar.com.hipotecario.canal.homebanking.servicio;

import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.base.Objeto;
import ar.com.hipotecario.canal.homebanking.conector.Sql;
import ar.com.hipotecario.canal.homebanking.conector.SqlRequest;
import ar.com.hipotecario.canal.homebanking.conector.SqlResponse;

public class SqlClientesOperadores {
	private static final String DB_ESALES = "esales";
	private static final String TRAER_CLIENTE_OPERADOR_USUARIO = "TraerClienteOperadorUsuario";
	private static final String SP_EXEC_TRAER_CLIENTE_OPERADOR_USUARIO = "[esales].[dbo].[TraerClienteOperadorUsuario]";

	private static final String TRAER_CLIENTE_ESALES = "TraerClienteEsales";
	private static final String SP_EXEC_TRAER_CLIENTE_ESALES = "[esales].[dbo].[TraerClienteEsales]";

	public static String idCobis(String usuario) {
		SqlRequest request = Sql.request("SelectUsuario", "hbs");
		request.sql = "SELECT * FROM [hbs].[dbo].[op_user] WHERE [id] = ?";
		request.parametros.add(usuario);
		SqlResponse response = Sql.response(request);
		for (Objeto registro : response.registros) {
			return registro.string("clientId");
		}
		return null;
	}

	public static boolean esUsuarioNuevo(ContextoHB contexto, String fechaInicio) {
		SqlResponse sqlResponseCanales = esUsuarioNuevoCanales(contexto, fechaInicio);
		if (sqlResponseCanales.hayError)
			return false;

		SqlResponse sqlResponseOb = esUsuarioNuevoOB(contexto, fechaInicio);
		if (sqlResponseOb.hayError)
			return false;

		return !sqlResponseCanales.registros.isEmpty() || !sqlResponseOb.registros.isEmpty();
	}

	private static SqlResponse esUsuarioNuevoCanales(ContextoHB contexto, String fechaInicio) {
		SqlRequest sqlRequest = Sql.request(TRAER_CLIENTE_OPERADOR_USUARIO, DB_ESALES);
		sqlRequest.configurarStoredProcedure(SP_EXEC_TRAER_CLIENTE_OPERADOR_USUARIO, contexto.idCobis(), fechaInicio);
		return Sql.response(sqlRequest);
	}


	private static SqlResponse esUsuarioNuevoOB(ContextoHB contexto, String fechaInicio) {
		SqlRequest sqlRequest = Sql.request(TRAER_CLIENTE_ESALES, DB_ESALES);
		sqlRequest.configurarStoredProcedure(SP_EXEC_TRAER_CLIENTE_ESALES, contexto.persona().cuit(), fechaInicio);
		return Sql.response(sqlRequest);
	}

//	public static String usuarioViejoHB(String idCobis) {
//		SqlRequestMB request = SqlMB.request("SelectUsuario", "clientes_operadores");
//		request.sql = "SELECT * FROM [clientes-operadores].[dbo].[op_user] WHERE [clientId] = ?";
//		request.parametros.add(idCobis);
//		SqlResponseMB response = SqlMB.response(request);
//		for (Objeto registro : response.registros) {
//			return registro.string("id");
//		}
//		return null;
//	}

//	public static Boolean existeUsuarioViejoHB(String idCobis) {
//		SqlRequest sqlRequest = Sql.request("SelectUsuario", "clientes_operadores");
//		sqlRequest.sql = "SELECT * FROM [clientes-operadores].[dbo].[op_user] WHERE [clientId] = ?";
//		sqlRequest.parametros.add(idCobis);
//		SqlResponse sqlResponse = Sql.response(sqlRequest);
//		return !sqlResponse.registros.isEmpty();
//	}
//
//	public static String usuarioViejoHB(Contexto contexto, String documento, String usuarioIngresado) {
//		List<String> listaIdCobis = RestPersona.listaIdCobis(contexto, documento, null, null);
//		for (String idCobis : listaIdCobis) {
//			SqlRequest sqlRequest = Sql.request("SelectUsuario", "clientes_operadores");
//			sqlRequest.sql = "SELECT * FROM [clientes-operadores].[dbo].[op_user] WHERE [clientId] = ?";
//			sqlRequest.parametros.add(idCobis);
//			SqlResponse sqlResponse = Sql.response(sqlRequest);
//			if (!sqlResponse.hayError) {
//				for (Objeto registro : sqlResponse.registros) {
//					String usuarioBaseDatos = registro.string("id");
//					if (usuarioBaseDatos.equals(usuarioIngresado)) {
//						return true;
//					}
//				}
//			}
//		}
//		return false;
//	}
}
