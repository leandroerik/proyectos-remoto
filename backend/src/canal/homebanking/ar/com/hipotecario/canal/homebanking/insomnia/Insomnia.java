package ar.com.hipotecario.canal.homebanking.insomnia;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import ar.com.hipotecario.canal.homebanking.base.Objeto;
import ar.com.hipotecario.canal.homebanking.conector.Sql;
import ar.com.hipotecario.canal.homebanking.conector.SqlRequest;
import ar.com.hipotecario.canal.homebanking.conector.SqlResponse;
import ar.com.hipotecario.canal.homebanking.insomnia.InsomniaYaml.Resource;
import ar.gabrielsuarez.glib.G;

public class Insomnia {
	public static void main(String[] args) throws IOException {
//		Long id = 0L;

		InsomniaYaml yaml = new InsomniaYaml();
		yaml.__export_format = 4;

		Resource workspace = new Resource();
		workspace._id = "wrk_" + G.md5(UUID.randomUUID().toString()).toLowerCase();
		workspace.parentId = null;
		workspace._type = "workspace";
		workspace.name = "homebanking";
		yaml.resources.add(workspace);

		Resource enviroment = new Resource();
		enviroment._id = "env_" + G.md5(UUID.randomUUID().toString()).toLowerCase();
		enviroment.parentId = workspace._id;
		enviroment._type = "environment";
		yaml.resources.add(enviroment);

		Resource cookieJar = new Resource();
		cookieJar._id = "jar_" + G.md5(UUID.randomUUID().toString()).toLowerCase();
		cookieJar.parentId = workspace._id;
		cookieJar._type = "cookie_jar";
		yaml.resources.add(cookieJar);

		Resource spec = new Resource();
		spec._id = "spc_" + G.md5(UUID.randomUUID().toString()).toLowerCase();
		spec.parentId = workspace._id;
		spec._type = "api_spec";
		yaml.resources.add(spec);

		Map<String, String> carpetas = InsomniaHB.carpetas();

		for (String carpeta : carpetas.keySet()) {
			Resource folder = new Resource();
			folder._id = carpetas.get(carpeta);
			folder.parentId = workspace._id;
			folder.name = carpeta;
			folder._type = "request_group";
			yaml.resources.add(folder);
		}

		SqlRequest sqlRequest = Sql.request("SelectEndpoints", "homebanking");
		sqlRequest.sql = "SELECT * FROM [homebanking].[dbo].[endpoints]";
		SqlResponse sqlResponse = Sql.response(sqlRequest);

		for (Objeto registro : sqlResponse.registros) {
			String metodo = registro.string("metodo");
			String url = registro.string("url");
			String request = registro.string("request");

			String parentId = carpetas.get(InsomniaHB.carpeta(url));
			if (parentId == null) {
				continue;
			}

			Resource item = new Resource();
			item._id = "req_" + G.md5(UUID.randomUUID().toString()).toLowerCase();
			item.parentId = parentId;
			item._type = "request";

			item.url = "{{ servidor  }}" + url;
			item.name = url;
			item.method = metodo.toUpperCase();
			item.body.mimeType = "application/json";
			item.body.text = request;

			yaml.resources.add(item);
		}

		String yamlString = G.toYaml(yaml);
		System.out.println(yamlString);
		G.writeFile(G.desktopPath(), "proyecto.yaml", yamlString);
	}
}

class InsomniaYaml {
	public Integer __export_format;
	public List<Resource> resources = new ArrayList<Resource>();

	public static class Resource {
		public String _id;
		public String parentId;
		public String _type;
		public String url;
		public String name;
		public String method;
		public Body body = new Body();
	}

	public static class Body {
		public String mimeType;
		public String text;
	}
}