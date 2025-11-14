package ar.com.hipotecario.canal.officebanking.util;

import ar.com.hipotecario.backend.Config;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.Date;
import ar.com.hipotecario.backend.Contexto;

public class Validador3Scale {
    private static Config config = new Config();
    private static JSONObject jwksCache;
    private static long jwksFetchedAt = 0;
    private static String obSsoCD = config.string("ob_sso_cd");
    private static JSONObject discoveryCache;
    private static long discoveryFetchedAt = 0;

    // Carga y cachea el JWKS cada 30 minutos aprox.
    private static synchronized JSONObject getJwks(boolean esDesaUHomo) throws Exception {
        try {
            if (jwksCache == null || System.currentTimeMillis() - jwksFetchedAt > 30 * 60 * 1000) {
                String json = new String(new URL(obtenerUrlCert(esDesaUHomo)).openStream().readAllBytes(), StandardCharsets.UTF_8);
                jwksCache = new JSONObject(json);
                jwksFetchedAt = System.currentTimeMillis();
            }
        }catch(IOException e){
            throw e;
        }catch(Exception e){
            throw new RuntimeException(e);
        }
        return jwksCache;
    }

    public static boolean validarJwt(String jwt, String sistemaAValidar,boolean esDesaUHomo) throws Exception {
        try {
            if(sistemaAValidar==null){
                throw new RuntimeException("Sistema a validar de 3scale no definido");
            }
            String cleanToken = jwt.trim().replace("\"", "");
            String[] partes = cleanToken.split("\\.");
            if (partes.length != 3) return false;

            String headerJson = new String(Base64.getUrlDecoder().decode(partes[0]), StandardCharsets.UTF_8);
            JSONObject header = new JSONObject(headerJson);
            String kid = header.getString("kid");

            JSONObject jwks = getJwks(esDesaUHomo);
            JSONArray keys = jwks.getJSONArray("keys");
            JSONObject jwk = null;
            for (int i = 0; i < keys.length(); i++) {
                if (kid.equals(keys.getJSONObject(i).getString("kid"))) {
                    jwk = keys.getJSONObject(i);
                    break;
                }
            }
            if (jwk == null) return false;

            // Reconstruir clave pública RSA
            BigInteger n = new BigInteger(1, Base64.getUrlDecoder().decode(jwk.getString("n")));
            BigInteger e = new BigInteger(1, Base64.getUrlDecoder().decode(jwk.getString("e")));
            PublicKey publicKey = KeyFactory.getInstance("RSA")
                    .generatePublic(new RSAPublicKeySpec(n, e));

            // Verificar la firma
            String firmado = partes[0] + "." + partes[1];
            byte[] firma = Base64.getUrlDecoder().decode(partes[2]);
            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initVerify(publicKey);
            sig.update(firmado.getBytes(StandardCharsets.US_ASCII));
            if (!sig.verify(firma)) return false;

            // Parsear los claims
            String payloadJson = new String(Base64.getUrlDecoder().decode(partes[1]), StandardCharsets.UTF_8);
            JSONObject payload = new JSONObject(payloadJson);

            // Validar issuer
            String urlIss = payload.optString("iss");
            if (esDesaUHomo) {
                urlIss = urlIss.replaceFirst("^https", "http");
            }
            if (!obtenerUrlIssuer(esDesaUHomo).equals(urlIss)) return false;

            // Verificar expiración
            long expSec = payload.getLong("exp");
            long ahora = System.currentTimeMillis() / 1000L;
            if (ahora > expSec) return false;

            // Verificar que contenga algun nombre referido al sistema de donde viene
            String username = payload.optString("preferred_username", "").toLowerCase();
            if (!username.contains(sistemaAValidar.toLowerCase())) return false;

            return true;
        } catch (Exception ex) {
            throw ex;
        }
    }

    private static synchronized JSONObject getDiscoveryDocument(boolean esDesaUHomo) throws IOException {
        if (discoveryCache == null || System.currentTimeMillis() - discoveryFetchedAt > 30 * 60 * 1000) {
            String urlDiscovery = esDesaUHomo
                    ? obSsoCD.replaceFirst("^https", "http")
                    : obSsoCD;

            String jsonStr = new String(new URL(urlDiscovery).openStream().readAllBytes(), StandardCharsets.UTF_8);
            discoveryCache = new JSONObject(jsonStr);
            discoveryFetchedAt = System.currentTimeMillis();
        }
        return discoveryCache;
    }

    private static String obtenerUrlCert(boolean esDesaUHomo) throws IOException {
        return getDiscoveryDocument(esDesaUHomo).getString("jwks_uri");
    }

    private static String obtenerUrlIssuer(boolean esDesaUHomo) throws IOException {
        return getDiscoveryDocument(esDesaUHomo).getString("issuer");
    }
}