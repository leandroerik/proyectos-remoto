package ar.com.hipotecario.mobile.servicio;

import java.text.SimpleDateFormat;

import ar.com.hipotecario.mobile.ConfigMB;
import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.conector.ApiMB;
import ar.com.hipotecario.mobile.conector.ApiRequestMB;
import ar.com.hipotecario.mobile.conector.ApiResponseMB;
import ar.com.hipotecario.mobile.conector.SqlMB;
import ar.com.hipotecario.mobile.conector.SqlRequestMB;
import ar.com.hipotecario.mobile.lib.Objeto;

public class SqlLogin {

	public static ApiResponseMB preguntasRiesgoNet(ContextoMB contexto, Objeto domicilioPostal) {

		ApiRequestMB request = ApiMB.request("PreguntasRiesgoNet", "personas", "GET", "/rnvconsulta", contexto);
		if (ConfigMB.esHomologacion()) {
			request.query("dni", "85777996");
			request.query("genero", "M");
			request.query("cuit", "20857779968");
			request.query("apellido", "DATOS DE ");
			request.query("nombre", "PRUEBA");
		} else {
			request.query("dni", contexto.persona().numeroDocumento());
			request.query("genero", contexto.persona().idSexo());
			request.query("cuit", contexto.persona().cuit());
			request.query("apellido", contexto.persona().apellido());
			request.query("nombre", contexto.persona().nombres());
		}
		request.query("fechaNacimiento", new SimpleDateFormat("dd/MM/yyyy").format(contexto.persona().fechaNacimiento()));
		request.query("provinciaP", domicilioPostal.string("provincia", "-"));
		request.query("localidadP", domicilioPostal.string("ciudad", "-"));
		request.query("calleP", domicilioPostal.string("calle", "-"));
		request.query("alturaP", domicilioPostal.string("numero", "0"));
		request.permitirSinLogin = true;

		ApiResponseMB response = ApiMB.response(request, contexto.persona().numeroDocumento());
		if (!response.hayError()) {
			contexto.insertLogPreguntasRiesgoNet(contexto, "PreguntasRiesgoNet", request.idProceso());
		}
		return response;
	}

	public static void insertRiesgoNet(ContextoMB contexto, int cantidadRespuestasIncorrectas) {
		SqlRequestMB sqlRequest = SqlMB.request("InsertRiesgoNet", "homebanking");
		sqlRequest.sql = "INSERT INTO [Homebanking].[dbo].[riesgo_net] (idCobis, momento, respuestasIncorrectas) VALUES (?, GETDATE(), ?)";
		sqlRequest.add(contexto.idCobis());
		sqlRequest.add(cantidadRespuestasIncorrectas);
		SqlMB.response(sqlRequest);
	}

	public static void deleteRiesgoNet(ContextoMB contexto) {
		Integer cantidadDiasBloqueo = ConfigMB.integer("cantidad_dias_bloqueo_riesgonet", 1);
		String inicio = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.sql.Date(new java.util.Date().getTime() - cantidadDiasBloqueo * 24 * 60 * 60 * 1000L));
		String fin = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.sql.Date(new java.util.Date().getTime()));

		SqlRequestMB sqlRequest = SqlMB.request("DeleteRiesgoNet", "homebanking");
		sqlRequest.sql = "DELETE [Homebanking].[dbo].[riesgo_net] WHERE idCobis = ? AND momento > ? AND momento < ?";
		sqlRequest.add(contexto.idCobis());
		sqlRequest.add(inicio);
		sqlRequest.add(fin);
		SqlMB.response(sqlRequest);
	}

	public static Integer selectRiesgoNet(ContextoMB contexto) {

		Integer cantidadDiasBloqueo = ConfigMB.integer("cantidad_dias_bloqueo_riesgonet", 1);
		String inicio = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.sql.Date(new java.util.Date().getTime() - cantidadDiasBloqueo * 24 * 60 * 60 * 1000L));
		String fin = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.sql.Date(new java.util.Date().getTime() + 60000L));

		SqlRequestMB sqlRequest = SqlMB.request("ConsultaRiesgoNet", "homebanking");
		sqlRequest.sql = "SELECT * FROM [Homebanking].[dbo].[riesgo_net] WHERE idCobis = ? AND momento > ? AND momento < ?";
		sqlRequest.add(contexto.idCobis());
		sqlRequest.add(inicio);
		sqlRequest.add(fin);
		Integer cantidadIntentosFallidos = SqlMB.response(sqlRequest).registros.size();
		return cantidadIntentosFallidos;
	}
}
