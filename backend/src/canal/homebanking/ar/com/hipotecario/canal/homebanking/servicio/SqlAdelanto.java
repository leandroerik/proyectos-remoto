package ar.com.hipotecario.canal.homebanking.servicio;

import java.util.List;

import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.base.Objeto;
import ar.com.hipotecario.canal.homebanking.conector.Sql;
import ar.com.hipotecario.canal.homebanking.conector.SqlRequest;

public class SqlAdelanto {

	public static List<Objeto> preguntasFrecuentes(ContextoHB contexto) {

		SqlRequest sqlRequest = Sql.request("AdelantoPreguntasFrecuentes", "homebanking");

		sqlRequest.sql = "SELECT [id],[pregunta],[descripcion] FROM [Hbs].[dbo].[adelanto_preguntas_frecuentes] where canal = ?  ORDER BY id asc";
		sqlRequest.add("HB");
		List<Objeto> lista = Sql.response(sqlRequest).registros;
		return lista;
	}

}
