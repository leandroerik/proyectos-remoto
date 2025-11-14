package ar.com.hipotecario.canal.homebanking.lib;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ar.com.hipotecario.canal.homebanking.SesionHB;
import ar.com.hipotecario.canal.homebanking.base.ConfigHB;
import ar.com.hipotecario.canal.homebanking.base.Objeto;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public abstract class Redis {

	private static Logger log = LoggerFactory.getLogger(Redis.class);
	private static Integer MINIMO_PESO = ConfigHB.integer("log_redis_minimo_peso", 2 * 1024 * 1024); // 2 MB

	public static Boolean habilitado = ConfigHB.bool("redis", false);
	private static String servidor = ConfigHB.string("redis_servidor");
	private static Integer puerto = ConfigHB.integer("redis_puerto");
	private static String clave = ConfigHB.string("redis_clave");
	public static JedisPool pool = habilitado ? new JedisPool(new JedisPoolConfig(), servidor, puerto, 3000, clave) : null;

	public static <T> T get(String clave, Class<T> tipo) {
		if (Redis.habilitado) {
			try (Jedis jedis = Redis.pool.getResource()) {
				byte[] bytes = jedis.get(clave.getBytes());
				Object object = Serializador.object(bytes);
				try {
					if (bytes.length > MINIMO_PESO) {
						Objeto registro = new Objeto();
						registro.set("tipo", "REDIS_GET");
						registro.set("clave", clave);
						registro.set("peso", bytes.length);
						if (object instanceof SesionHB) {
							SesionHB sesion = (SesionHB) object;
							for (String key : sesion.cache.keySet()) {
								if (sesion.cache.get(key) != null) {
									Integer length = sesion.cache.get(key).getBytes().length;
									if (length > MINIMO_PESO) {
										registro.set("length_" + key, length);
									}
								}
							}
						}
						String json = registro.toString().replace("\n", "");
						log.info(json);
					}
				} catch (Exception e) {
				}
				return tipo.cast(object);
			} catch (Exception e) {
				return null;
			}
		}
		return null;
	}

	public static void set(String clave, Object valor) {
		set(clave, valor, ConfigHB.integer("redis_ttl", 600));
	}

	public static void set(String clave, Object valor, Integer timeout) {
		if (Redis.habilitado) {
			try (Jedis jedis = Redis.pool.getResource()) {
				byte[] bytes = Serializador.bytes(valor);
				jedis.setex(clave.getBytes(), timeout, bytes);
				try {
					if (bytes.length > MINIMO_PESO) {
						Objeto registro = new Objeto();
						registro.set("tipo", "REDIS_SET");
						registro.set("clave", clave);
						registro.set("peso", bytes.length);
						String json = registro.toString().replace("\n", "");
						log.info(json);
					}
				} catch (Exception e) {
				}
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
