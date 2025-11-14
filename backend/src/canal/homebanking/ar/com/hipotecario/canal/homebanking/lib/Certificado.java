package ar.com.hipotecario.canal.homebanking.lib;

import java.io.FileInputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import ar.com.hipotecario.canal.homebanking.base.ConfigHB;

public abstract class Certificado {

	public static KeyStore keystore;

	static {
		try {
			String ruta = ConfigHB.string("jks_ruta");
			String clave = ConfigHB.string("jks_clave");
			FileInputStream is = new FileInputStream(ruta);
			keystore = KeyStore.getInstance(KeyStore.getDefaultType());
			keystore.load(is, clave.toCharArray());
			Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		} catch (Exception e) {
		}
	}

	/* ========== UTIL ========== */
	public static PrivateKey privateKey(String alias) {
		try {
			String valorAlias = ConfigHB.string("jks_" + alias + "_alias");
			String valorClave = ConfigHB.string("jks_" + alias + "_clave");

			Key key = keystore.getKey(valorAlias, valorClave.toCharArray());
			if (key instanceof PrivateKey) {
				return (PrivateKey) key;
			}
			return null;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static X509Certificate x509(String alias) {
		try {
			String valorAlias = ConfigHB.string("jks_" + alias + "_alias");
			String valorClave = ConfigHB.string("jks_" + alias + "_clave");

			Key key = keystore.getKey(valorAlias, valorClave.toCharArray());
			if (key instanceof PrivateKey) {
				Certificate cert = keystore.getCertificate(valorAlias);
				if (cert instanceof X509Certificate) {
					return (X509Certificate) cert;
				}
			}
			return null;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
