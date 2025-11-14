package ar.com.hipotecario.backend.util;

import java.util.Date;

import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;

import ar.com.hipotecario.backend.base.Util;

public class EncryptableService {

	public static StringEncryptor stringEncryptor() {
		PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
		SimpleStringPBEConfig config = new SimpleStringPBEConfig();
		config.setPassword("password");
		config.setAlgorithm("PBEWithMD5AndDES");
		config.setKeyObtentionIterations("1000");
		config.setPoolSize("1");
		config.setProviderName("SunJCE");
		config.setSaltGeneratorClassName("org.jasypt.salt.RandomSaltGenerator");
		config.setStringOutputType("base64");
		encryptor.setConfig(config);
		return encryptor;
	}

	public static String factorRandom() {
		return String.valueOf(Util.random(1, 10) / (1.0d * Util.random(1, 10)));
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		StringEncryptor strEncryptor = stringEncryptor();
		String stringForToken = "dsahui_0.0.0.0" + "_" + new Date().getTime() + "_" + factorRandom() + "_uhfuihew";
		System.out.println(strEncryptor.encrypt(stringForToken));
	}
}
