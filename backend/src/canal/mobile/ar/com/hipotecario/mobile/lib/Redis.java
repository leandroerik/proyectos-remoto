package ar.com.hipotecario.mobile.lib;

import ar.com.hipotecario.mobile.ConfigMB;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public abstract class Redis {
	public static Boolean habilitado = ConfigMB.bool("redis", false);
	private static String servidor = ConfigMB.string("redis_servidor");
	private static Integer puerto = ConfigMB.integer("redis_puerto");
	private static String clave = ConfigMB.string("redis_clave");
	public static JedisPool pool = habilitado ? new JedisPool(new JedisPoolConfig(), servidor, puerto, 3000, clave) : null;

	public static <T> T get(String clave, Class<T> tipo) {
		if (Redis.habilitado) {
			try (Jedis jedis = Redis.pool.getResource()) {
				byte[] bytes = jedis.get(clave.getBytes());
				Object object = Serializador.object(bytes);
				return tipo.cast(object);
			} catch (Exception e) {
				return null;
			}
		}
		return null;
	}

	public static void set(String clave, Object valor) {
		if (Redis.habilitado) {
			try (Jedis jedis = Redis.pool.getResource()) {
				byte[] bytes = Serializador.bytes(valor);
//				jedis.set(clave.getBytes(), bytes);
				jedis.setex(clave.getBytes(), ConfigMB.integer("redis_ttl", 600), bytes);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	public static void del(String clave) {
		if (Redis.habilitado) {
			try (Jedis jedis = Redis.pool.getResource()) {
				jedis.del(clave.getBytes());
			}
		}
	}
}
