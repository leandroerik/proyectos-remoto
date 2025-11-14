package ar.com.hipotecario.mobile.lib;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;

public abstract class Encriptador {
	public static String ENC_BH = "qw3daf-";

	public static String hashCode(Object... objects) {
		String hash = "";
		for (Object object : objects) {
			hash += object.toString().hashCode();
		}
		return Integer.valueOf(hash.hashCode()).toString().replace("-", "");
	}

	public static String md5(String texto) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] digest = md.digest(texto.getBytes());
			String hash = DatatypeConverter.printHexBinary(digest).toUpperCase();
			return hash;
		} catch (Exception e) {
			return null;
		}
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

	public static String encriptarTripleDES(String key, String iv, String textoPlano) {
		try {
			SecretKey secretKey = new SecretKeySpec(key.getBytes(), "DESede");
			IvParameterSpec ivParameterSpec = new IvParameterSpec(iv.getBytes());
			Cipher cipher = Cipher.getInstance("DESede/CBC/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec);
			byte[] bytesTextoEncriptado = cipher.doFinal(textoPlano.getBytes());
			String textoEncriptado = Base64.getEncoder().encodeToString(bytesTextoEncriptado);
			return textoEncriptado;
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
			throw new RuntimeException(e);
		}
	}

	public static String desencriptarTripleDES(String key, String iv, String textoEncriptado) {
		try {
			byte[] bytesTextoEncriptado = Base64.getDecoder().decode(textoEncriptado);
			SecretKey secretKey = new SecretKeySpec(key.getBytes(), "DESede");
			IvParameterSpec ivParameterSpec = new IvParameterSpec(iv.getBytes());
			Cipher cipher = Cipher.getInstance("DESede/CBC/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);
			String textoPlano = new String(cipher.doFinal(bytesTextoEncriptado));
			return textoPlano;
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
			throw new RuntimeException(e);
		}
	}

	public static String encriptarAES(String algoritmoHash, String key, String iv, String textoPlano) {
		try {
			MessageDigest messageDigest = MessageDigest.getInstance(algoritmoHash);
			SecretKeySpec secretKeySpec = new SecretKeySpec(messageDigest.digest(key.getBytes("UTF-8")), "AES");
			IvParameterSpec ivParameterSpec = new IvParameterSpec(messageDigest.digest(iv.getBytes("UTF-8")));
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
			cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
			byte[] bytes = cipher.doFinal(textoPlano.getBytes());
			return Base64.getEncoder().encodeToString(bytes);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static String desencriptarAES(String algoritmoHash, String key, String iv, String textoEncriptado) {
		try {
			MessageDigest messageDigest = MessageDigest.getInstance(algoritmoHash);
			SecretKeySpec secretKeySpec = new SecretKeySpec(messageDigest.digest(key.getBytes("UTF-8")), "AES");
			IvParameterSpec ivParameterSpec = new IvParameterSpec(messageDigest.digest(iv.getBytes("UTF-8")));
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
			cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
			byte[] bytes = cipher.doFinal(Base64.getDecoder().decode(textoEncriptado));
			return new String(bytes);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static String encriptarPBE(String textoPlano) {
		StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
		encryptor.setAlgorithm("PBEWithMD5AndDES");
		encryptor.setPassword("SIN_PASSWORD");
		String textoEncriptado = "ENC(" + encryptor.encrypt(textoPlano) + ")";
		return textoEncriptado;
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
}
