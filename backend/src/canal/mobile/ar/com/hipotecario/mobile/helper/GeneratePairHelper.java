package ar.com.hipotecario.mobile.helper;

import ar.com.hipotecario.mobile.ConfigMB;

import javax.crypto.Cipher;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import static java.util.Base64.*;

public class GeneratePairHelper {
    private static final String ALGORITMO_CLAVES = "RSA";
    private static final int KEY_SIZE = 2048;

    public static KeyPair generateKeyPair() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ALGORITMO_CLAVES);
            keyPairGenerator.initialize(KEY_SIZE);
            return keyPairGenerator.generateKeyPair();
        } catch (Exception e) {
            return null;
        }
    }

    public static String encryptWithPrivateKey(String plainText) {
        try {
            PrivateKey privateKey = KeyFactory.getInstance(ALGORITMO_CLAVES).generatePrivate(new PKCS8EncodedKeySpec(getDecoder().decode(privateKeyEncoded())));

            Cipher cipher = Cipher.getInstance(ALGORITMO_CLAVES);
            cipher.init(Cipher.ENCRYPT_MODE, privateKey);

            return Base64.getEncoder().encodeToString(cipher.doFinal(plainText.getBytes()));
        } catch (Exception e) {
            return "";
        }
    }

    public static String publicKeyEncoded() {
        return getEncoder().encodeToString(ConfigMB.keyPair.getPublic().getEncoded());
    }

    public static String privateKeyEncoded() {
        return getEncoder().encodeToString(ConfigMB.keyPair.getPrivate().getEncoded());
    }
}
