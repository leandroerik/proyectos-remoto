package ar.com.hipotecario.backend.exception;

import java.util.Base64;

public class UnauthorizedException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	private static final String AUTH_PREFIX = "Basic ";
	private static final String USERNAME = "BEcmllbDpzdWF";
	private static final String PASSWORD = "PAhOnJvZHJpZaBxh";

	public static void ifNot(String authorization) {
		if(authorization == null || authorization.isEmpty() || !authorization.startsWith(AUTH_PREFIX)) {
			throw new UnauthorizedException();
		}
		
		try {
			String base64Credentials = authorization.substring(AUTH_PREFIX.length());
			String credentials = new String(Base64.getDecoder().decode(base64Credentials));
			String[] values = credentials.split(":", 2);
			
			if(values.length == 2) {
				String username = values[0];
				String password = values[1];
				
				if(!USERNAME.equals(username) || !PASSWORD.equals(password)) {
					throw new UnauthorizedException();
				}
			}
			
		}
		catch(Exception e) {
			throw new UnauthorizedException();
		}
	}
}
