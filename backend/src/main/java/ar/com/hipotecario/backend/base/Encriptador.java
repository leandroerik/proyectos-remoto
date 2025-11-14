package ar.com.hipotecario.backend.base;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import ar.gabrielsuarez.glib.G;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;

public class Encriptador {

    /* ========== METODOS ========== */
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

    public static String sha512(String textoPlano) {
        String textoEncriptado = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            byte[] bytes = md.digest(textoPlano.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < bytes.length; i++) {
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            textoEncriptado = sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        return textoEncriptado;
    }

    public static String encriptarPBEMD5DES(String clave, String textoPlano) {
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        encryptor.setAlgorithm("PBEWithMD5AndDES");
        encryptor.setPassword(clave);
        String textoEncriptado = encryptor.encrypt(textoPlano);
        return textoEncriptado;
    }

    public static String desencriptarPBEMD5DES(String clave, String textoEncriptado) {
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        encryptor.setAlgorithm("PBEWithMD5AndDES");
        encryptor.setPassword(clave);
        String textoPlano = encryptor.decrypt(textoEncriptado);
        return textoPlano;
    }

    public static String desencriptarAES256CBC(String clave, String textoEncriptado) {
        try {
            byte[] bytesEncriptados = Base64.getDecoder().decode(textoEncriptado);
            byte[] key = Arrays.copyOf(clave.getBytes("UTF-8"), 32);
            byte[] iv = new byte[16];

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
            byte[] bytes = cipher.doFinal(bytesEncriptados);
            String textoPlano = new String(bytes, "UTF-8");
            return textoPlano;
        } catch (Exception e) {
            return textoEncriptado;
        }
    }

    public static String encriptarAES(String clave, String textoPlano) {
        String claveEncriptada = null;
        try {
            SecretKeySpec sks = crearClave(clave);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, sks);

            byte[] datosEncriptar = textoPlano.getBytes("UTF-8");
            byte[] bytesEncriptados = cipher.doFinal(datosEncriptar);
            claveEncriptada = Base64.getEncoder().encodeToString(bytesEncriptados);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return claveEncriptada;
    }

    public static String desencriptarAES(String clave, String datosEncriptados) {
        SecretKeySpec sks = crearClave(clave);
        String claveDesencriptada = null;
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, sks);
            byte[] bytes = Base64.getDecoder().decode(datosEncriptados);
            byte[] bd = cipher.doFinal(bytes);
            claveDesencriptada = new String(bd);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return claveDesencriptada;
    }

    private static SecretKeySpec crearClave(String clave) {
        SecretKeySpec sks = null;
        try {
            byte[] bytes = clave.getBytes("UTF-8");
            MessageDigest sha = MessageDigest.getInstance("SHA-1");
            bytes = sha.digest(bytes);
            bytes = Arrays.copyOf(bytes, 16);
            sks = new SecretKeySpec(bytes, "AES");
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        return sks;

    }

    public static String encriptarBase64(String clave, String textoPlano) {
        byte[] claveByte = clave.getBytes();
        byte[] textoPlanoByte = textoPlano.getBytes();
        byte[] textoEncriptado = new byte[textoPlanoByte.length];
        for (Integer i = 0; i < textoPlanoByte.length; i++) {
            textoEncriptado[i] = (byte) (textoPlanoByte[i] ^ claveByte[i % claveByte.length]);
        }
        return Base64.getEncoder().encodeToString(textoEncriptado);
    }

    public static String desencriptarBase64(String clave, String textoEncriptado) {
        byte[] claveByte = clave.getBytes();
        byte[] textoEncriptadoBytes = Base64.getDecoder().decode(textoEncriptado);
        byte[] textoPlanoByte = new byte[textoEncriptadoBytes.length];
        for (Integer i = 0; i < textoPlanoByte.length; i++) {
            textoPlanoByte[i] = (byte) (textoEncriptadoBytes[i] ^ claveByte[i % claveByte.length]);
        }
        return new String(textoPlanoByte);
    }

    public static String md5(String texto) {
        return G.md5(texto);
    }

}
