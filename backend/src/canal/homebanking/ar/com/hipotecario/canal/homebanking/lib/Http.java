package ar.com.hipotecario.canal.homebanking.lib;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import ar.com.hipotecario.canal.homebanking.base.ConfigHB;
import okhttp3.Authenticator;
import okhttp3.ConnectionPool;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

public abstract class Http {

	public static OkHttpClient okHttpClient() {
		OkHttpClient.Builder builder = new OkHttpClient.Builder();
		builder.connectTimeout(10, TimeUnit.SECONDS);
		builder.writeTimeout(10, TimeUnit.SECONDS);

		Integer ReadTimeOut = ConfigHB.integer("read_time_out", 30);
		builder.readTimeout(ReadTimeOut, TimeUnit.SECONDS);

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

		builder.setConnectionPool$okhttp(new ConnectionPool(50, 5, TimeUnit.MINUTES));
		OkHttpClient http = builder.proxySelector(proxySelector).build();
		return http;
	}

	public static OkHttpClient okHttpClientProxy() {
		if (ConfigHB.string("proxy", null) == null) {
			return null;
		}

		OkHttpClient.Builder builder = new OkHttpClient.Builder();
		builder.connectTimeout(10, TimeUnit.SECONDS);
		builder.writeTimeout(10, TimeUnit.SECONDS);

		Integer ReadTimeOut = ConfigHB.integer("read_time_out", 30);
		builder.readTimeout(ReadTimeOut, TimeUnit.SECONDS);

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

		String urlProxy = ConfigHB.string("proxy").split(" ")[0];
		String puertoProxy = ConfigHB.string("proxy").split(" ")[1];
		String usuarioProxy = ConfigHB.string("proxy").split(" ")[2];
		String claveProxy = ConfigHB.string("proxy").split(" ")[3];

		Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(urlProxy, Integer.valueOf(puertoProxy)));
		Authenticator proxyAuthenticator = new Authenticator() {
			@Override
			public Request authenticate(Route route, Response response) throws IOException {
				String credential = Credentials.basic(usuarioProxy, claveProxy);
				return response.request().newBuilder().header("Proxy-Authorization", credential).build();
			}
		};
		builder.proxy(proxy);
		builder.proxyAuthenticator(proxyAuthenticator);

		builder.sslSocketFactory(trustAllSslContext.getSocketFactory(), (X509TrustManager) trustAllCerts[0]);
		builder.hostnameVerifier(new HostnameVerifier() {
			@Override
			public boolean verify(String hostname, SSLSession session) {
				return true;
			}
		});

		OkHttpClient http = builder.proxySelector(proxySelector).build();
		return http;
	}

	public static String authBasic(String usuario, String clave) {
		String token = Base64.getEncoder().encodeToString((usuario + ":" + clave).getBytes());
		return token;
	}
}
