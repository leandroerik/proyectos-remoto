package ar.com.hipotecario.mobile.conector;

import java.io.IOException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ar.com.hipotecario.mobile.ConfigMB;
import ar.com.hipotecario.mobile.lib.Objeto;
import ar.com.hipotecario.mobile.lib.Texto;
import ar.com.hipotecario.mobile.lib.Util;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ThreeScaleMB {

	private static OkHttpClient http;
	private static Logger log = LoggerFactory.getLogger(ThreeScaleMB.class);
	public static Boolean habilitarLog = ConfigMB.bool("habilitar_logs");
	private static Map<String, String> tokens = new HashMap<>();
	private static Map<String, Date> vencimientos = new HashMap<>();

	public static String token(String api, String clientId, String secretId, Boolean forzarRenovacion) {
		String tokenCache = tokens.get(api);
		Date vencimientoCache = vencimientos.get(api);
		if (!forzarRenovacion) {
			if (tokenCache != null && vencimientoCache != null) {
				if (new Date().getTime() < vencimientoCache.getTime()) {
					return tokenCache;
				}
			}
		}

		String url = ConfigMB.string("threescale_url") + "/protocol/openid-connect/token";

		Map<String, String> headers = new LinkedHashMap<>();
		headers.put("Content-Type", "application/x-www-form-urlencoded");
		headers.put("x-canal", "HB");

		FormBody.Builder formBodyBuilder = new FormBody.Builder();
		formBodyBuilder.add("grant_type", "password");
		formBodyBuilder.add("client_id", clientId);
		formBodyBuilder.add("client_secret", secretId);
		formBodyBuilder.add("username", ConfigMB.string("oag_usuario"));
		formBodyBuilder.add("password", ConfigMB.string("oag_clave"));
		formBodyBuilder.add("scope", "openid+HB");
		formBodyBuilder.add("token_endpoint", url);
		formBodyBuilder.add("sslValidate", "false");
		FormBody formBody = formBodyBuilder.build();

		Request.Builder requestBuilder = new Request.Builder();
		requestBuilder.url(url);
		for (String clave : headers.keySet()) {
			requestBuilder.addHeader(clave, headers.get(clave));
		}
		requestBuilder.post(formBody);
		Request request = requestBuilder.build();

		StringBuilder logRequest = new StringBuilder();
		logRequest.append("REQUEST [3scale, ").append(api).append("]\n");
		logRequest.append("POST").append(" ").append(url).append("\n");
		for (String clave : headers.keySet()) {
			logRequest.append(clave).append(": ").append(headers.get(clave)).append("\n");
		}
		logRequest.append("\n");
		for (Integer i = 0; i < formBody.size(); ++i) {
			logRequest.append(formBody.encodedName(i)).append("=").append(formBody.encodedValue(i));
			if (i + 1 < formBody.size()) {
				logRequest.append("&");
			}
		}
		logRequest.append("\n");

		String idProceso = Util.idProceso();
		if (habilitarLog) {
			if (!ConfigMB.bool("kibana", false)) {
				log.info("{} {}\n", Texto.hora(), logRequest.toString());
			} else {
				Objeto registro = new Objeto();
				registro.set("tipo", "request");
				registro.set("servicio", "3scale");
				registro.set("idProceso", idProceso);
				registro.set("timestamp", new Date().getTime());
				registro.set("fecha", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
				registro.set("hora", new SimpleDateFormat("HH:mm:ss").format(new Date()));
				registro.set("request", logRequest.toString());
				log.info(registro.toString().replace("\n", ""));
			}
		}

		try (Response response = http.newCall(request).execute()) {
			Integer code = response.code();
			String body = response.body() != null ? response.body().string() : "";
			String jsonBody = code == 200 ? Objeto.fromJson(body).toJson() : body;

			StringBuilder logResponse = new StringBuilder();
			logResponse.append("RESPONSE [3scale, ").append(api).append("] ").append(code).append("\n");
			logResponse.append(body).append("\n");

			if (habilitarLog) {
				if (!ConfigMB.bool("kibana", false)) {
					log.info("{} {}\n{}", Texto.hora(), "3scale", response.code(), jsonBody);
				} else {
					Objeto registro = new Objeto();
					registro.set("tipo", "response");
					registro.set("servicio", "3scale");
					registro.set("http", response.code());
					registro.set("idProceso", idProceso);
					registro.set("timestamp", new Date().getTime());
					registro.set("fecha", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
					registro.set("hora", new SimpleDateFormat("HH:mm:ss").format(new Date()));
					registro.set("response", body);
					log.info(registro.toString().replace("\n", ""));
				}
			}

			if (code != 200) {
				throw new RuntimeException();
			}

			ApiResponseMB apiResponse = new ApiResponseMB(null, code, body);
			String token = apiResponse.string("access_token");
			Date vencimiento = new Date(new Date().getTime() + (apiResponse.integer("expires_in") * 900));

			tokens.put(api, token);
			vencimientos.put(api, vencimiento);

			return token;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	static {
		OkHttpClient.Builder builder = new OkHttpClient.Builder();
		builder.connectTimeout(10, TimeUnit.SECONDS);
		builder.writeTimeout(10, TimeUnit.SECONDS);
		builder.readTimeout(30, TimeUnit.SECONDS);

		final ProxySelector proxySelector = new ProxySelector() {
			@Override
			public java.util.List<Proxy> select(final URI uri) {
				final List<Proxy> proxyList = new ArrayList<Proxy>(1);
				proxyList.add(Proxy.NO_PROXY);
				return proxyList;
			}

			@Override
			public void connectFailed(URI arg0, SocketAddress arg1, IOException arg2) {
				throw new UnsupportedOperationException("Not supported yet.");
			}
		};

		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			@Override
			public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
			}

			@Override
			public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
			}

			@Override
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return new java.security.cert.X509Certificate[] {};
			}
		} };

		SSLContext trustAllSslContext = null;
		try {
			trustAllSslContext = SSLContext.getInstance("SSL");
			trustAllSslContext.init(null, trustAllCerts, new java.security.SecureRandom());
		} catch (NoSuchAlgorithmException | KeyManagementException e) {
			throw new RuntimeException(e);
		}

		builder.sslSocketFactory(trustAllSslContext.getSocketFactory(), (X509TrustManager) trustAllCerts[0]);
		builder.hostnameVerifier(new HostnameVerifier() {
			@Override
			public boolean verify(String hostname, SSLSession session) {
				return true;
			}
		});

		http = builder.proxySelector(proxySelector).build();
	}
}
