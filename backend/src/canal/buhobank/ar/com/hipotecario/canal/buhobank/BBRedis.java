package ar.com.hipotecario.canal.buhobank;

import ar.com.hipotecario.backend.Modulo;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.base.Redis;

public class BBRedis extends Modulo {

	public static Object leerRedis(ContextoBB contexto) {
		String sesion = contexto.parametros.string("sesion");
		Redis redis = contexto.redis();
		String datos = redis.get("chatbot_" + sesion);
		return datos != null ? datos : "{}";
	}

	public static Object guardarRedis(ContextoBB contexto) {
		String sesion = contexto.parametros.string("sesion");
		Objeto datos = contexto.parametros.objeto("datos");
		Redis redis = contexto.redis();
		redis.set("chatbot_" + sesion, datos.toJson(), 30 * 60); // 30 minutos
		return leerRedis(contexto);
	}
	
	public static Object borrarRedis(ContextoBB contexto) {
		String dato = contexto.parametros.string("dato");
		Redis redis = contexto.redis();
		redis.del(dato);
		return respuesta();
	}
}
