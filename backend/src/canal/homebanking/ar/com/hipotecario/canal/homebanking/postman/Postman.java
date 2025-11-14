package ar.com.hipotecario.canal.homebanking.postman;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import ar.com.hipotecario.canal.homebanking.base.Objeto;
import ar.com.hipotecario.canal.homebanking.conector.Sql;
import ar.com.hipotecario.canal.homebanking.conector.SqlRequest;
import ar.com.hipotecario.canal.homebanking.conector.SqlResponse;
import ar.com.hipotecario.canal.homebanking.postman.PostmanJson.Body;
import ar.com.hipotecario.canal.homebanking.postman.PostmanJson.HeaderData;
import ar.com.hipotecario.canal.homebanking.postman.PostmanJson.Info;
import ar.com.hipotecario.canal.homebanking.postman.PostmanJson.Item;
import ar.com.hipotecario.canal.homebanking.postman.PostmanJson.Options;
import ar.com.hipotecario.canal.homebanking.postman.PostmanJson.ProtocolProfileBehavior;
import ar.com.hipotecario.canal.homebanking.postman.PostmanJson.Raw;
import ar.com.hipotecario.canal.homebanking.postman.PostmanJson.Requests;
import ar.com.hipotecario.canal.homebanking.postman.PostmanJson.Url;
import ar.gabrielsuarez.glib.G;

public class Postman {
	public static void main(String[] args) throws IOException {
		Long id = 0L;

		PostmanJson post = new PostmanJson();

		Info info = new Info();
		info._postman_id = G.md5(String.valueOf(new Date().getTime() + (++id))).toLowerCase();
		info.name = "homebanking-postman";
		info.schema = "https://schema.getpostman.com/json/collection/v2.1.0/collection.json";
		post.info = info;

		SqlRequest sqlRequest = Sql.request("SelectEndpoints", "homebanking");
		sqlRequest.sql = "SELECT * FROM [homebanking].[dbo].[endpoints]";
		SqlResponse sqlResponse = Sql.response(sqlRequest);

		for (Objeto registro : sqlResponse.registros) {

			String method = registro.string("metodo");
			String url = registro.string("url");
			String request = registro.string("request");

			Item item = new Item();
			item.name = url;

			ProtocolProfileBehavior protocol = new ProtocolProfileBehavior();
			protocol.disableBodyPruning = true;
			item.protocolProfileBehavior = protocol;

			Requests requests = new Requests();
			requests.method = method.toUpperCase();
			requests.header.add(new HeaderData() {
				{
					key = "Content-Type";
					value = "application/json";
					type = "default";
				}
			});

			Body body = new Body();
			body.mode = "raw";
			body.raw = request;

			Options options = new Options();
			options.raw = new Raw();
			options.raw.language = "json";
			body.options = options;

			requests.body = body;

			String protocolo = "http";
			String servidor = "localhost";
			String puerto = "82";

			Url urlData = new Url();
			urlData.raw = protocolo + "://" + servidor + ":" + puerto + url;
			urlData.protocol = protocolo;
			urlData.port = puerto;
			requests.url = urlData;

			urlData.host = Arrays.asList(servidor.split("\\."));
			urlData.path = Arrays.asList(url.split("/"));

			item.request = requests;
			post.item.add(item);
		}

		String postString = G.toJson(post);
		System.out.println(postString);
		G.writeFile(G.desktopPath(), "proyecto.json", postString);
	}
}

class PostmanJson {
	public Info info;
	public List<Item> item = new ArrayList<Item>();

	public static class Info {
		public String _postman_id;
		public String name;
		public String schema;
	}

	public static class Item {
		public String name;
		public ProtocolProfileBehavior protocolProfileBehavior;
		public Requests request;
	}

	public static class Requests {
		public String method;
		public List<HeaderData> header = new ArrayList<HeaderData>();
		public Body body;
		public Url url;
	}

	public static class HeaderData {
		public String key;
		public String value;
		public String type;
	}

	public static class Body {
		public String mode;
		public String raw;
		public Options options;
	}

	public static class Options {
		public Raw raw;
	}

	public static class Raw {
		public String language;
	}

	public static class ProtocolProfileBehavior {
		public Boolean disableBodyPruning;
	}

	public static class Url {
		public String raw;
		public String protocol;

		public String port;

		public List<String> host = new ArrayList<>();
		public List<String> path = new ArrayList<>();
	}
}