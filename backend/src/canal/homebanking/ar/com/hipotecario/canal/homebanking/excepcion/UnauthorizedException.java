package ar.com.hipotecario.canal.homebanking.excepcion;

import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

import static ar.com.hipotecario.backend.util.LoginLDAP.loginLDAP;

public class UnauthorizedException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public static void ifNot(String authorization) {
		String user = decodeAuthHeader(authorization).split(":",2)[0];
		String pass = decodeAuthHeader(authorization).split(":",2)[1];

		if (!loginLDAP(user,pass)) {
			throw new UnauthorizedException();
		}
	}

	private static String decodeAuthHeader(String authHeader) {
		if (authHeader == null || !authHeader.startsWith("Basic ")) {
			throw new UnauthorizedException();		}

		String base64Credentials = authHeader.substring("Basic ".length());
		byte[] decodedBytes = Base64.getDecoder().decode(base64Credentials);
		String decodedString = new String(decodedBytes);

		if (!decodedString.contains(":")) {
			throw new UnauthorizedException();
		} else {
			// Separar usuario y contraseña
			String[] values = decodedString.split(":", 2);
			String username = values[0];
			String password = values[1];
		}

		return decodedString; // Devuelve "usuario:clave"
	}
}
