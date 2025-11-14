package ar.com.hipotecario.backend.base;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class Redis {

	/* ========== ATRIBUTOS ========== */
	private JedisPool redisPool;

	/* ========== CONSTRUCTORES ========== */
	public Redis(String servidor, Integer puerto) {
		redisPool = new JedisPool(new JedisPoolConfig(), servidor, puerto, null, null);
	}

	public Redis(String servidor, Integer puerto, String clave) {
		redisPool = new JedisPool(new JedisPoolConfig(), servidor, puerto, null, clave);
	}

	public Redis(String servidor, Integer puerto, String usuario, String clave) {
		redisPool = new JedisPool(new JedisPoolConfig(), servidor, puerto, usuario, clave);
	}

	/* ========== METODOS ========== */
	public String get(String clave) {
		try (Jedis jedis = redisPool.getResource()) {
			String valor = jedis.get(clave);
			return valor;
		}
	}

	public void set(String clave, String valor, Integer expiracion) {
		try (Jedis jedis = redisPool.getResource()) {
			jedis.setex(clave, expiracion, valor);
		}
	}

	public void del(String clave) {
		try (Jedis jedis = redisPool.getResource()) {
			jedis.del(clave);
		}
	}

	public byte[] getBinary(String clave) {
		try (Jedis jedis = redisPool.getResource()) {
			byte[] valor = jedis.get(clave.getBytes());
			return valor;
		}
	}

	public void setBinary(String clave, byte[] valor, Integer expiracion) {
		try (Jedis jedis = redisPool.getResource()) {
			jedis.setex(clave.getBytes(), expiracion, valor);
		}
	}

	public void delBinary(String clave) {
		try (Jedis jedis = redisPool.getResource()) {
			jedis.del(clave.getBytes());
		}
	}
}
