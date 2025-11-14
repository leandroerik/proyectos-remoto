package ar.com.hipotecario.canal.officebanking;

import java.util.Map;

import ar.com.hipotecario.backend.Config;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

public class JwtOB {

	 public static String generarToken(String token) {
	    	Map<String, String> properties = Config.properties();
	        return Jwts.builder()
	                .claim("token", token)
	                .signWith(SignatureAlgorithm.HS256, Config.desencriptarOB(properties.get("secret_id_transmit")).getBytes())
	                .compact();
	    }
}
