package ar.com.hipotecario.backend.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class PemUtils {

    public static RSAPublicKey loadPublicKey(String pem) {
        try {
            if (pem == null || pem.trim().isEmpty())
                throw new IllegalArgumentException("La clave pública está vacía");

            String contenidoBase64 = pem
                    .replace("\\n", "\n")
                    .replace("\\\\n", "\n")
                    .replaceAll("\\\\", "")
                    .replace("\\r", "")
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s+", "");

            if (contenidoBase64.isEmpty())
                throw new IllegalArgumentException("No se encontró contenido Base64 válido");

            return (RSAPublicKey) KeyFactory.getInstance("RSA")
                    .generatePublic(
                            new X509EncodedKeySpec(
                                    Base64.getDecoder().decode(contenidoBase64)
                            ));
        } catch (Exception e) {
            throw new RuntimeException("Error al procesar la clave pública: " + e.getMessage(), e);
        }
    }

}
