package ar.com.hipotecario.canal.homebanking.endpoints;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.base.Objeto;
import ar.com.hipotecario.canal.homebanking.conector.Sql;
import ar.com.hipotecario.canal.homebanking.conector.SqlRequest;
import ar.com.hipotecario.canal.homebanking.conector.SqlResponse;
import ar.com.hipotecario.canal.homebanking.servicio.RestPersona;

public class EHBLogin {

	private static Map<String, String> clientes = new HashMap<>();
	private static Map<String, Objeto> listaCobisAfectados = new HashMap<>();

	public static String get(ContextoHB contexto, String documento) {
		String idCobis = clientes.get(documento);
		if (idCobis == null || idCobis.isEmpty()) {
			List<Objeto> personas = RestPersona.personas(contexto, documento, null, null);
			if (personas != null && !personas.isEmpty() && personas.size() == 1) {
				Objeto persona = personas.get(0);
				String dni = documento;
				String cuit = persona.string("numeroIdentificacionTributaria");
				String idcobis = persona.string("idCliente");
				String nombre = persona.string("apellido") + " " + persona.string("nombre");
				
				try {
					SqlRequest sqlRequest = Sql.request("InsertarUsuario", "hbs");
					sqlRequest.sql = "INSERT INTO [hbs].[dbo].[cliente] ([dni], [cuit], [idcobis], [nombre]) VALUES (?, ?, ?, ?)";
					sqlRequest.add(dni);
					sqlRequest.add(cuit);
					sqlRequest.add(idcobis);
					sqlRequest.add(nombre);
					Sql.response(sqlRequest);
				} catch (Exception e) {
				}
				clientes.put(dni, idcobis);
				return idcobis;
			}
		}
		return idCobis;
	}

	public static Boolean iniciarCacheClientes() {
		SqlRequest sqlRequest = Sql.request("CacheUsuarios", "hbs");
		sqlRequest.sql = "SELECT dni, idcobis FROM [hbs].[dbo].[cliente]";
		SqlResponse sqlResponse = Sql.response(sqlRequest);
		for (Objeto item : sqlResponse.registros) {
			String dni = item.string("dni");
			String idcobis = item.string("idcobis");
			clientes.put(dni, idcobis);
		}
		return true;
	}
	
	public static Boolean iniciarCacheCobisAfectados() {
		SqlRequest sqlRequest = Sql.request("CacheCobis", "homebanking");
		sqlRequest.sql = "SELECT * FROM [Homebanking].[dbo].[forzar_cambio_usuario] ";
		SqlResponse sqlResponse = Sql.response(sqlRequest);
		for (Objeto item : sqlResponse.registros) {
			String idcobis = item.string("Cobis");
			listaCobisAfectados.put(idcobis, item);
		}
		return true;
	}

	public static Objeto datosCambioUsuario(String idcobis) {
		SqlRequest sqlRequest = Sql.request("CobisAfectado", "homebanking");
		sqlRequest.sql = "SELECT * FROM [Homebanking].[dbo].[forzar_cambio_usuario] WHERE Cobis = ?";
		sqlRequest.add(idcobis);
		SqlResponse sqlResponse = Sql.response(sqlRequest);
		return sqlResponse.registros.isEmpty() ? null : sqlResponse.registros.get(0);
	}

	public static Boolean esUsuarioMarcado(String idcobis) {
		return listaCobisAfectados.containsKey(idcobis);
	}
}
