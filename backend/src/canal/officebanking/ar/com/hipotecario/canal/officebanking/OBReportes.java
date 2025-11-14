package ar.com.hipotecario.canal.officebanking;

import java.io.IOException;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.Modulo;
import ar.com.hipotecario.backend.base.Archivo;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.base.Resource;
import ar.com.hipotecario.backend.servicio.sql.SqlHB_BE;
import ar.com.hipotecario.backend.servicio.sql.hb_be.LogsOfficeBanking;
import ar.com.hipotecario.backend.servicio.sql.hb_be.LogsOfficeBanking.LogOfficeBanking;

public class OBReportes extends Modulo {

	private static Handlebars handlebars = new Handlebars();

	public static Object Logs(Contexto contexto) {
		String parametro = contexto.parametros.string(":params", Fecha.hoy().string("yyyy-MM-dd"));

		LogsOfficeBanking logs = new LogsOfficeBanking();
		Objeto registros = new Objeto();
		Boolean esPorFecha = true;

		if (parametro.contains("-")) {
			logs = SqlHB_BE.selectPorFecha(contexto, new Fecha(parametro, "yyyy-MM-dd")).get();
		} else {
			String parametroEmpresa = contexto.parametros.string(":empresa", "0");
			parametroEmpresa = parametroEmpresa.equals("empresa") ? "0" : parametroEmpresa;
			parametro = parametro.equals("usuario") ? "0" : parametro;

			logs = SqlHB_BE.selectPorUsuarioOrEmpresa(contexto, parametro, parametroEmpresa).get();
		}

		if (logs.isEmpty())
			return esPorFecha ? respuesta("Sin datos a mostrar para la fecha ".concat(parametro)) : respuesta("Sin datos a mostrar correspondientes a: ".concat(parametro));

		for (LogOfficeBanking log : logs) {
			Objeto registro = new Objeto();
			registro.set("momento", log.momento);
			registro.set("empresa", log.empresa);
			registro.set("usuario", log.usuario);
			registro.set("endpoint", log.endpoint);
			registro.set("evento", log.evento);

			String evento = log.evento;

			if (evento.equals("ERROR_API") || evento.equals("ERROR")) {
				String datos = log.datos == null ? "" : log.datos.concat(" - ");
				datos += log.error;
				if (log.idProceso != null)
					datos += "- ID PROCESO: ".concat(log.idProceso.toString());
				registro.set("datos", datos);
			}
			registros.add(registro);
		}

		try {
			Archivo archivo = Resource.archivo("web/OfficeBanking/logs.hbs");
			String contenido = archivo.string();
			Template template = handlebars.compileInline(contenido);

			String html = template.apply(registros.toList());

			return new Archivo("index.html", html.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}
}
