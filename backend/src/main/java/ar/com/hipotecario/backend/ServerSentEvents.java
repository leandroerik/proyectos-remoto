package ar.com.hipotecario.backend;

import java.io.IOException;
import java.util.function.BiConsumer;

import ar.com.hipotecario.backend.base.Objeto;

public class ServerSentEvents<T extends Contexto> {

	/* ========== ATRIBUTOS ========== */
	private T contexto;
	private BiConsumer<T, ServerSentEvents<T>> funcion;

	/* ========== CONSTRUCTOR ========== */
	public static <T extends Contexto> ServerSentEvents<T> crear(T contexto, BiConsumer<T, ServerSentEvents<T>> funcion) {
		ServerSentEvents<T> sse = new ServerSentEvents<T>();
		sse.contexto = contexto;
		sse.funcion = funcion;
		return sse;
	}

	/* ========== METODOS ========== */
	public void ejecutar() {
		funcion.accept(contexto, this);
	}

	public void evento(String evento, Objeto datos) {
		try {
			String x = "";
			x += "event: " + evento + "\n";
			x += "data: " + datos.toSimpleJson() + "\n\n";
			contexto.response.raw().getOutputStream().print(x);
			contexto.response.raw().getOutputStream().flush();
		} catch (IOException e) {
			throw new RuntimeException();
		}
	}
}
