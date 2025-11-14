package ar.com.hipotecario.mobile.servicio;

import java.util.List;

import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.conector.SqlMB;
import ar.com.hipotecario.mobile.conector.SqlRequestMB;
import ar.com.hipotecario.mobile.lib.Objeto;

public class SqlAdelanto {

	public static List<Objeto> preguntasFrecuentes(ContextoMB contexto) {

		SqlRequestMB sqlRequest = SqlMB.request("AdelantoPreguntasFrecuentes", "homebanking");

		sqlRequest.sql = "SELECT [id],[pregunta],[descripcion] FROM [Hbs].[dbo].[adelanto_preguntas_frecuentes] where canal = ?  ORDER BY id asc";
		sqlRequest.add("MB");
		List<Objeto> lista = SqlMB.response(sqlRequest).registros;
		return lista;
	}

}
