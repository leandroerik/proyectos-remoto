package ar.com.hipotecario.canal.homebanking.lib;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;

import ar.gabrielsuarez.glib.G;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;

public abstract class Encriptador {
	public static String ENC_BH = "qw2daf-";

	public static String md5(String texto) {
		return G.md5(texto);
	}

	public static String sha256(String texto) {
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
		byte[] hash = digest.digest(texto.getBytes(StandardCharsets.UTF_8));
		StringBuffer hexString = new StringBuffer();
		for (int i = 0; i < hash.length; i++) {
			String hex = Integer.toHexString(0xff & hash[i]);
			if (hex.length() == 1) {
				hexString.append('0');
			}
			hexString.append(hex);
		}
		return hexString.toString();
	}

	public static String encriptarPBEBH(String textoPlano) {
		SimpleStringPBEConfig config = new SimpleStringPBEConfig();
		config.setAlgorithm("PBEWithMD5AndDES");
		config.setPassword("SIN_PASSWORD");
		config.setKeyObtentionIterations("1000");
		config.setPoolSize("1");
		config.setSaltGeneratorClassName("org.jasypt.salt.ZeroSaltGenerator");
		config.setIvGeneratorClassName("org.jasypt.iv.NoIvGenerator");

		StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
		encryptor.setConfig(config);
		return ENC_BH + encryptor.encrypt(textoPlano);
	}

	public static String desencriptarPBEBH(String textoEncriptado) {
		SimpleStringPBEConfig config = new SimpleStringPBEConfig();
		config.setAlgorithm("PBEWithMD5AndDES");
		config.setPassword("SIN_PASSWORD");
		config.setKeyObtentionIterations("1000");
		config.setPoolSize("1");
		config.setSaltGeneratorClassName("org.jasypt.salt.ZeroSaltGenerator");
		config.setIvGeneratorClassName("org.jasypt.iv.NoIvGenerator");

		StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
		encryptor.setConfig(config);
		return encryptor.decrypt(textoEncriptado.substring(ENC_BH.length()));
	}

	public static String desencriptarPBE(String textoEncriptado) {
		if (textoEncriptado != null && textoEncriptado.startsWith("ENC(") && textoEncriptado.endsWith(")")) {
			textoEncriptado = textoEncriptado.substring("ENC(".length(), textoEncriptado.length() - 1);
			StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
			encryptor.setAlgorithm("PBEWithMD5AndDES");
			encryptor.setPassword("SIN_PASSWORD");
			String textoPlano = encryptor.decrypt(textoEncriptado);
			return textoPlano;
		}
		return textoEncriptado;
	}

	public static String nextSha() {
		return sha256(sha256(Momento.hoy().string("ddMMyyyy|ddMMyyyy")));
	}

	public static void main(String[] args) {
		System.out.println(nextSha());
	}
}
