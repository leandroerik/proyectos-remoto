package ar.com.hipotecario.canal.buhobank;

import java.security.Key;
import java.util.Date;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import ar.com.hipotecario.backend.base.Objeto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

public class JwtBB {

	private static String KEY = GeneralBB.CODIGO_ADMIN;
	private static long EXPIRE = 18000000;

	public static String create(String subject, String name, String role) {

		// The JWT signature algorithm used to sign the token
		SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

		long nowMillis = System.currentTimeMillis();
		Date now = new Date(nowMillis);

		// sign JWT with our ApiKey secret
		byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(KEY);
		Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());

		// set the JWT Claims
		JwtBuilder builder = Jwts.builder().setIssuedAt(now).setSubject(subject).claim("name", name).claim("role", role).signWith(signatureAlgorithm, signingKey);

		if (EXPIRE >= 0) {
			long expMillis = nowMillis + EXPIRE;
			Date exp = new Date(expMillis);
			builder.setExpiration(exp);
		}

		// Builds the JWT and serializes it to a compact, URL-safe string
		return builder.compact();
	}

	/**
	 * Method to validate and read the JWT
	 *
	 * @param jwt
	 * @return
	 */
	public static Objeto getValue(String jwt) {
		// This line will throw an exception if it is not a signed JWS (as
		// expected)
		try {
			Claims claims = Jwts.parser().setSigningKey(DatatypeConverter.parseBase64Binary(KEY)).parseClaimsJws(jwt).getBody();

			return new Objeto().set("value", claims.getSubject());

		} catch (Exception e) {

			if (e.getMessage().contains("expire")) {
				return new Objeto().set("mensaje", "ERROR_SESION_EXPIRADA");
			}

			return new Objeto().set("mensaje", "ERROR_TOKEN_INVALIDO");
		}
	}

	/**
	 * Method to validate and read the JWT
	 *
	 * @param jwt
	 * @return
	 */
	public static String getKey(String jwt) {
		// This line will throw an exception if it is not a signed JWS (as
		// expected)
		Claims claims = Jwts.parser().setSigningKey(DatatypeConverter.parseBase64Binary(KEY)).parseClaimsJws(jwt).getBody();

		return claims.getId();
	}

}
