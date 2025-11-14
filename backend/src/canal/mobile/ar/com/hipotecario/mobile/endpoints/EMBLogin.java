package ar.com.hipotecario.mobile.endpoints;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.conector.SqlMB;
import ar.com.hipotecario.mobile.conector.SqlRequestMB;
import ar.com.hipotecario.mobile.conector.SqlResponseMB;
import ar.com.hipotecario.mobile.lib.Objeto;
import ar.com.hipotecario.mobile.servicio.RestPersona;

public class EMBLogin {
	
	public static Map<String, String> clientes = new HashMap<>();
	private static Map<String, Objeto> listaCobisAfectados = new HashMap<>();

	
	public static String get(ContextoMB contexto, String documento) {
		String idCobis = clientes.get(documento);
		if (idCobis == null || idCobis.isEmpty()) {
			List<Objeto> personas = RestPersona.personas(contexto, documento, null, null);
			if (personas != null && !personas.isEmpty() && personas.size() == 1) {
				Objeto persona = personas.get(0);
				String dni = documento;
				String cuit = persona.string("numeroIdentificacionTributaria");
				String idcobis = persona.string("idCliente");
				String nombre = persona.string("apellido") + " " + persona.string("nombre");
				
				// insert
				SqlRequestMB sqlRequest = SqlMB.request("InsertarUsuario", "hbs");
				sqlRequest.sql = "INSERT INTO [hbs].[dbo].[cliente] ([dni], [cuit], [idcobis], [nombre]) VALUES (?, ?, ?, ?)";
				sqlRequest.add(dni);
				sqlRequest.add(cuit);
				sqlRequest.add(idcobis);
				sqlRequest.add(nombre);
				SqlMB.response(sqlRequest);
				clientes.put(dni, idcobis);
				return idcobis;
			}
		}
		return idCobis;
	}

	public static Boolean iniciarCacheClientes() {
		SqlRequestMB sqlRequest = SqlMB.request("CacheUsuarios", "hbs");
		sqlRequest.sql = "SELECT dni, idcobis FROM [hbs].[dbo].[cliente]";
		SqlResponseMB sqlResponse = SqlMB.response(sqlRequest);
		for (Objeto item : sqlResponse.registros) {
			String dni = item.string("dni");
			String idcobis = item.string("idcobis");
			clientes.put(dni, idcobis);
		}
		return true;
	}
	
	public static Boolean iniciarCacheCobisAfectados() {
		SqlRequestMB sqlRequest = SqlMB.request("CacheCobis", "homebanking");
		sqlRequest.sql = "SELECT * FROM [Homebanking].[dbo].[forzar_cambio_usuario] ";
		SqlResponseMB sqlResponse = SqlMB.response(sqlRequest);
		for (Objeto item : sqlResponse.registros) {
			String idcobis = item.string("Cobis");
			listaCobisAfectados.put(idcobis, item);
		}
		return true;
	}
	
	public static Boolean esCobisAfectado(String idcobis) {
		return listaCobisAfectados.containsKey(idcobis);
	}
	
	public static Objeto datosCambioUsuario(String idcobis) {
		SqlRequestMB sqlRequest = SqlMB.request("CobisAfectado", "homebanking");
		sqlRequest.sql = "SELECT * FROM [Homebanking].[dbo].[forzar_cambio_usuario] WHERE Cobis = ?";
		sqlRequest.add(idcobis);
		SqlResponseMB sqlResponse = SqlMB.response(sqlRequest);
		return sqlResponse.registros.get(0);
	}
	
	public static Boolean esUsuarioMarcado(String idcobis) {
		return listaCobisAfectados.containsKey(idcobis);
	}
}
