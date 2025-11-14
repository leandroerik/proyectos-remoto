package ar.com.hipotecario.mobile.servicio;


import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.conector.SqlMB;
import ar.com.hipotecario.mobile.conector.SqlRequestMB;
import ar.com.hipotecario.mobile.conector.SqlResponseMB;
import ar.com.hipotecario.mobile.lib.Fecha;
import ar.com.hipotecario.mobile.lib.Objeto;

public class SqlConsentimientoService {

	public Boolean insertDatos(ContextoMB contexto, String state,String codeVerifier, String codeChallenge, String cuit) {
		try {
			SqlRequestMB sqlRequest = SqlMB.request("InsertDatosConsentimiento", "hbs");
			sqlRequest.sql = "INSERT INTO [Hbs].[dbo].[MODO_consentimiento] (state, code_verifier, code_challenge,id_cobis, user_identifier, fecha_alta) VALUES ((RTRIM(?)), ?, ?, ?, ?, GETDATE() )";
			sqlRequest.add(state);
			sqlRequest.add(codeVerifier);
			sqlRequest.add(codeChallenge);
			sqlRequest.add(contexto.idCobis().replaceAll("\\s", "").trim());
			sqlRequest.add(cuit);
			SqlMB.response(sqlRequest);
		} catch (Exception error) {
			return false;
		}
		return true;
	}
	
	public SqlResponseMB consultarUsuario(ContextoMB contexto, String idCobis) {
		try {
			SqlRequestMB sqlRequest = SqlMB.request("ConsultarConsentimiento", "hbs");
			sqlRequest.sql = "SELECT COUNT(*) as cant_Usuario FROM [Hbs].[dbo].[MODO_consentimiento] WHERE id_cobis = ? ";
			sqlRequest.add(idCobis);
			SqlResponseMB response = SqlMB.response(sqlRequest);
			return response;
		} catch (Exception error) {
			return null;
		}
	}
	
	public String getPcpLanding(ContextoMB contexto) {
	    try {
	        SqlRequestMB sqlRequest = SqlMB.request("ObtenerTextoConsentimiento", "hbs");
	        sqlRequest.sql = "SELECT texto FROM [Hbs].[dbo].[MODO_consentimiento_parametria] WHERE id = (SELECT MAX(id) FROM [Hbs].[dbo].[MODO_consentimiento_parametria])";
	        SqlResponseMB response = SqlMB.response(sqlRequest);

	        // Verificar si hay al menos un registro en la respuesta
	        if (!response.registros.isEmpty()) {
	            Objeto registro = response.registros.get(0);
	            String texto = registro.string("texto");
	            if (texto != null && !texto.isEmpty()) {
	                return texto;
	            } else {
	                return "El campo texto está vacío para el registro con el último ID generado";
	            }
	        } else {
	            return "No se encontró ningún registro en la tabla MODO_consentimiento_parametria";
	        }
	    } catch (Exception error) {
	        error.printStackTrace();
	        return "Error al recuperar el último texto de la tabla MODO_consentimiento_parametria";
	    }

	}

}
