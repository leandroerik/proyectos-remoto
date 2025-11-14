package ar.com.hipotecario.backend;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ar.com.hipotecario.backend.base.HttpRequest;
import ar.com.hipotecario.backend.base.Objeto;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ThreeScale {

//	private static Logger log = LoggerFactory.getLogger(ApiRequest.class);
	private static OkHttpClient http = HttpRequest.okHttpClient();
	private static Map<String, Token> tokens = new ConcurrentHashMap<>();
	private static Config config = new Config();

	public static class Token {
		public String accessToken;
		public Date vencimiento;

		public Boolean vencido() {
			return new Date().getTime() > vencimiento.getTime();
		}
	}

	public static Token token(String canal, String api) {
//		StringBuilder log1 = new StringBuilder();

		String key = canal + ":" + api;
		Token token = tokens.get(key);

		if (token == null || token.vencido()) {
			String url = config.string("backend_sso");

			if (config.string("backend_3scale_" + api, "").isEmpty()) {
				return null;
			}

			String clientId = config.string("backend_3scale_" + api, "").trim().split(" ")[1];

			String secretId = config.string("backend_3scale_" + api, "").trim().split(" ")[2];

			FormBody.Builder formBodyBuilder = new FormBody.Builder();
			formBodyBuilder.add("grant_type", "password");
			formBodyBuilder.add("client_id", clientId);
			formBodyBuilder.add("client_secret", secretId);
			formBodyBuilder.add("username", config.string(canal.toLowerCase() + "_oag_usuario"));

			formBodyBuilder.add("password", config.string(canal.toLowerCase() + "_oag_clave"));

			formBodyBuilder.add("scope", "openid+" + canal);
			formBodyBuilder.add("token_endpoint", url);

			formBodyBuilder.add("sslValidate", "false");
			FormBody formBody = formBodyBuilder.build();

			Request.Builder requestBuilder = new Request.Builder();
			requestBuilder.url(url);
			requestBuilder.post(formBody);
			Request request = requestBuilder.build();
			try (Response response = http.newCall(request).execute()) {
				Integer code = response.code();
				String body = response.body() != null ? response.body().string() : "";
				if (code == 200) {
					Objeto data = Objeto.fromJson(body);
					String accessToken = data.string("access_token");
					Long expiresIn = data.longer("expires_in") * 900;

					token = new Token();
					token.accessToken = accessToken;
					token.vencimiento = new Date(new Date().getTime() + expiresIn);
					tokens.put(key, token);
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return token;
	}

	public static void main(String[] args) {
		Token token = token("HB", "personas");
		System.out.println(token.accessToken);
	}
}
