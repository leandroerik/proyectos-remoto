package ar.com.hipotecario.canal.buhobank.firebase;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.Notification.Builder;

import ar.com.hipotecario.backend.base.HttpRequest;
import ar.com.hipotecario.backend.base.HttpResponse;
import ar.com.hipotecario.backend.base.Resource;
import ar.com.hipotecario.backend.servicio.sql.esales.AlertaPushUsuariosEsales;
import ar.com.hipotecario.backend.servicio.sql.esales.AlertaPushUsuariosEsales.AlertaPushUsuarioEsales;
import ar.com.hipotecario.canal.buhobank.ContextoBB;

public class Firebase {

	private static Map<String, FirebaseApp> mapa = new HashMap<>();

	private static FirebaseApp initializer(ContextoBB contexto) {
		FirebaseApp app = mapa.get(contexto.ambiente);
		if (app == null) {
			String resource = contexto.config.string("buhobank_firebase_ambiente", "firebaseproduccion.json");
			String url = contexto.config.string("buhobank_firebase_url", "https://hipotecariomobile-37d77.firebaseio.com");
			InputStream serviceAccount = Resource.stream(resource);
			try {
				FirebaseOptions options = FirebaseOptions.builder().setCredentials(GoogleCredentials.fromStream(serviceAccount)).setDatabaseUrl(url).build();
				if (FirebaseApp.getApps().isEmpty()) {
					try {
						FirebaseApp.initializeApp(options);
					} catch (IllegalStateException e) {
						if (FirebaseApp.getApps().isEmpty()) {
							throw e;
						}
					}
				}
				mapa.put(resource, app);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return app;
	}

	public BatchResponse enviarNotificacionesPush(ContextoBB contexto, AlertaPushUsuariosEsales alertasPush) throws FirebaseMessagingException {

		initializer(contexto);

		BatchResponse responseFirebase = null;

		List<Message> mensajes = new ArrayList<Message>();

		for (AlertaPushUsuarioEsales alertaPush : alertasPush) {

			mensajes.add(Message.builder().setNotification(Notification.builder().setTitle(alertaPush.titulo).setBody(alertaPush.texto).setImage(null).build()).putData("url", alertaPush.url == null ? "" : alertaPush.url).putData("PushExternalLink", alertaPush.url == null ? "" : alertaPush.url).putData("OnboardingFlow", "true").setToken(alertaPush.tokenFirebase).build());
		}

		try {
			responseFirebase = FirebaseMessaging.getInstance().sendEach(mensajes);
		} catch (FirebaseMessagingException e) {
			e.printStackTrace();
		}

		return responseFirebase;
	}

	public BatchResponse enviarNotificacionesPushIndividual(ContextoBB contexto, List<String> tokensFirebase, String titulo, String texto, String imagenUrl, String url) throws FirebaseMessagingException {
		initializer(contexto);

		Builder builder = Notification.builder();
		builder.setTitle(titulo);
		builder.setBody(texto);
		builder.setImage(imagenUrl); // "https://trinityviajes.com/wp-content/uploads/2018/07/hipotecario.jpg"
		Notification notificacion = builder.build();

		MulticastMessage message = MulticastMessage.builder().putData("url", url).putData("PushExternalLink", url).putData("OnboardingFlow", "true").setNotification(notificacion).addAllTokens(tokensFirebase).build();
		BatchResponse response = null;
		try {
			response = FirebaseMessaging.getInstance().sendEachForMulticast(message);
		} catch (FirebaseMessagingException e) {
			e.printStackTrace();
		}

		return response;
	}

	public static Boolean sendEvent(ContextoBB contexto, String idDispositivo, String evento) {

		try {

			HttpRequest request = new HttpRequest("GET", "http://www.google-analytics.com/collect?v=1&tid=72763556&cid={ID_DISPOSITIVO}&t={EVENTO}");
			request.path("ID_DISPOSITIVO", idDispositivo);
			request.path("EVENTO", evento);

			HttpResponse response = request.run();
			if (response.code == 200) {
				return true;
			}

		} catch (Exception e) {
		}

		return false;
	}

	public static Boolean sendEventAdjust(ContextoBB contexto, String idDispositivo, String evento) {

		try {

			HttpRequest request = new HttpRequest("POST", "https://s2s.adjust.com/event");
			request.query("adid", idDispositivo);
			request.query("event_token", evento);
			request.query("app_token", "mh9bmq4qqcxs");
			request.query("s2s", "1");
			request.query("environment", contexto.esProduccion() ? "production" : "sandbox");

			HttpResponse response = request.run();
			if (response.code == 200) {
				return true;
			}

		} catch (Exception e) {

		}

		return false;
	}
}
