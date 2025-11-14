package ar.com.hipotecario.mobile.api;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import ar.com.hipotecario.backend.servicio.api.transmit.JourneyTransmitEnum;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.mobile.ConfigMB;
import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.RespuestaMB;
import ar.com.hipotecario.mobile.api.dto.modo.AccountMetadataDto;
import ar.com.hipotecario.mobile.api.dto.modo.GetAccountsResponseDTO;
import ar.com.hipotecario.mobile.api.dto.modo.PhoneNumberDto;
import ar.com.hipotecario.mobile.api.dto.modo.SuggestContactDto;
import ar.com.hipotecario.mobile.api.dto.modo.UserStatus;
import ar.com.hipotecario.mobile.conector.ApiMB;
import ar.com.hipotecario.mobile.conector.ApiRequestMB;
import ar.com.hipotecario.mobile.conector.ApiResponseMB;
import ar.com.hipotecario.mobile.conector.SqlResponseMB;
import ar.com.hipotecario.mobile.lib.Formateador;
import ar.com.hipotecario.mobile.lib.Objeto;
import ar.com.hipotecario.mobile.negocio.CuentaTercero;
import ar.com.hipotecario.mobile.negocio.TarjetaCredito;
import ar.com.hipotecario.mobile.negocio.TarjetaDebito;
import ar.com.hipotecario.mobile.servicio.DisableService;
import ar.com.hipotecario.mobile.servicio.ProductosService;
import ar.com.hipotecario.mobile.servicio.SqlConsentimientoService;
import ar.com.hipotecario.mobile.servicio.SqlMobile;
import ar.com.hipotecario.mobile.servicio.TransferModoService;

public class MBModo {

    private static LimitAmounts limitAmounts = new LimitAmounts();

    /**
     * Ventana temporal en la que el token aún es valido, pero de todas formas se
     * refrescará para contemplar la demora del resto de los servicios.
     */
    public static final int DELTA_PREVENTIVE_SECONDS = 15;
    public static final String SUB_CHANNEL_MODO = "MB-MODO";
    public static final String CALLBACK_URL = "https://mobile.hipotecario.com.ar";

    @SuppressWarnings("unchecked")
    public static boolean consultTokensModo(ContextoMB contexto) {
        Map<String, Object> map;
        ObjectMapper mapper = new ObjectMapper();
        try {

            // Si hay refreshToken, no es necesario consultar la bd
            if (contexto.sesion().cache("refresh_token") != null) {
                return false;
            }

            String idCobis = contexto.idCobis();

            ApiRequestMB request = ApiMB.request("ConsultarTokensModo", "modo", "GET", "/v1/modo/{id_cobis}", contexto);
            request.path("id_cobis", idCobis);
            request.headers.put("x-subCanal", "MB-MODO");

            ApiResponseMB response = ApiMB.response(request, idCobis);
            map = mapper.readValue(response.toJson(), Map.class);
            contexto.sesion().setCache("access_token", map.containsKey("access_token") ? String.valueOf(map.get("access_token")) : "");
            contexto.sesion().setCache("refresh_token", map.containsKey("refresh_token") ? String.valueOf(map.get("refresh_token")) : "");
            contexto.sesion().setCache("modo_token_expiration", LocalDateTime.now().toString());

        } catch (IOException | NumberFormatException ex) {
            contexto.sesion().setCache("access_token", "");
            contexto.sesion().setCache("refresh_token", "");
            contexto.sesion().setCache("modo_token_expiration", LocalDateTime.now().toString());
        }
        return true;
    }

    public static RespuestaMB onboardingTarjetas(ContextoMB contexto) {

        String language = contexto.parametros.string("language");
        String emulator = contexto.parametros.string("emulator");
        String latitude = contexto.parametros.string("latitude");
        String longitude = contexto.parametros.string("longitude");
        String device_model = contexto.parametros.string("device_model");
        String os_version = contexto.parametros.string("os_version");
        String os_id = contexto.parametros.string("os_id");
        String device_name = contexto.parametros.string("device_name");
        String os_name = contexto.parametros.string("os_name");
        String user_agent = contexto.parametros.string("user_agent");
        String hardware_id = contexto.parametros.string("hardware_id");
        String ip = contexto.parametros.string("ip");

        if (!(contexto.sesion() != null && contexto.sesion().idCobisReal() != null)) {
            return RespuestaMB.estado("ERROR_SIN_SESION");
        }

        Objeto fingerprint = new Objeto();
        fingerprint.set("language", language);
        fingerprint.set("emulator", emulator);

        Objeto geolocalization = new Objeto();
        geolocalization.set("latitude", latitude);
        geolocalization.set("longitude", longitude);
        fingerprint.set("geolocalization", geolocalization);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ");
        String time_stamp = ZonedDateTime.now().format(formatter);

        fingerprint.set("time_stamp", time_stamp);
        fingerprint.set("device_model", device_model);
        fingerprint.set("os_version", os_version);
        fingerprint.set("os_id", os_id);
        fingerprint.set("device_name", device_name);
        fingerprint.set("os_name", os_name);
        fingerprint.set("user_agent", user_agent);
        fingerprint.set("hardware_id", hardware_id);
        fingerprint.set("ip", ip);

        List<Objeto> listaFuturo = new ArrayList<>();
        try {

            contexto.sesion();
            Futuro<List<Objeto>> futuroTc = new Futuro<>(() -> onboardingTc(contexto, fingerprint));
            Futuro<List<Objeto>> futuroTd = new Futuro<>(() -> onboardingTd(contexto, fingerprint));

            List<Objeto> resultadoTc = futuroTc.get();
            List<Objeto> resultadoTd = futuroTd.get();

            if (resultadoTc != null) {
                listaFuturo.addAll(resultadoTc);
            }
            if (resultadoTd != null) {
                listaFuturo.addAll(resultadoTd);
            }

        } catch (Exception e) {
            return RespuestaMB.error();
        }

        return RespuestaMB.exito().set("ListaTarjetas", listaFuturo);
    }

    private static List<Objeto> onboardingTc(ContextoMB contexto, Objeto fingerprint) {

        List<Objeto> responseList = new ArrayList<>();
        for (TarjetaCredito tarjetaCredito : contexto.tarjetasCreditoTitularConAdicionalesTercero()) {
            Objeto credit_card = new Objeto();

            credit_card.set("number", tarjetaCredito.id());
            String fechaVencimiento = tarjetaCredito.fechaVigenciaHasta("MM/yyyy");
            credit_card.set("month", fechaVencimiento.substring(0, 2));
            credit_card.set("year", fechaVencimiento.substring(3, 7));
            credit_card.set("first_name", contexto.persona().nombres());
            credit_card.set("last_name", contexto.persona().apellidos());
            Objeto payment_method = new Objeto();

            if (!tarjetaCredito.idEstado().equals("29")) {
                payment_method.set("credit_card", credit_card);
            }

            ApiResponseMB response = MBModo.onboardingTarjetas(contexto, fingerprint, payment_method, tarjetaCredito.id());
            if (response != null) {
                responseList.add(response);
            } else {
                return null;
            }
        }

        return responseList;
    }

    private static List<Objeto> onboardingTd(ContextoMB contexto, Objeto fingerprint) {

        List<Objeto> responseList = new ArrayList<>();

        for (TarjetaDebito tarjetaDebito : contexto.tarjetasDebito()) {
            Objeto credit_card = new Objeto();

            credit_card.set("number", tarjetaDebito.numero());
            String fechaVencimiento = tarjetaDebito.fechaVencimiento("MM/yyyy");
            credit_card.set("month", fechaVencimiento.substring(0, 2));
            credit_card.set("year", fechaVencimiento.substring(3, 7));
            credit_card.set("first_name", contexto.persona().nombres());
            credit_card.set("last_name", contexto.persona().apellidos());

            Objeto payment_method = new Objeto();
            payment_method.set("credit_card", credit_card);

            ApiResponseMB response = MBModo.onboardingTarjetas(contexto, fingerprint, payment_method, tarjetaDebito.id());
            if (response != null) {
                responseList.add(response);
            } else {
                return null;
            }
        }

        return responseList;
    }


    public static ApiResponseMB onboardingTarjetas(ContextoMB contexto, Objeto fingerprint, Objeto payment_method, String idTarjeta) {
        verifyAccessToken(contexto);
        String accessToken = contexto.sesion().cache("access_token");

        ApiRequestMB request = ApiMB.request("onboarding", "modo", "POST", "/v1/public/cards/onboarding/tarjetas", contexto);
        request.body("fingerprint", fingerprint);
        request.body("payment_method", payment_method);
        request.headers.put("DimoToken", "Bearer " + accessToken);
        request.headers.put("x-subCanal", "MB-MODO");
        request.cacheSesion = true;

        ApiResponseMB response = ApiMB.response(request, contexto.idCobis(), idTarjeta);
        return response;
    }

    public static String obtenerRefreshToken(ContextoMB contexto) {

        String refreshToken = "";
        String idCobis = contexto.idCobis();

        if (contexto.sesion().cache("refresh_token") != null) {
            refreshToken = contexto.sesion().cache("refresh_token");
        } else {
            ApiRequestMB request = ApiMB.request("ConsultarTokensModo", "modo", "GET", "/v1/modo/{id_cobis}", contexto);
            request.path("id_cobis", idCobis);
            request.headers.put("x-subCanal", "MB-MODO");

            ApiResponseMB token = ApiMB.response(request, idCobis);
            if (token.hayError()) {
                return "error";
            } else if (token.json.isEmpty()) {
                return "SIN_USER_ID";
            }
            refreshToken = token.get("refresh_token").toString();

        }
        return refreshToken;

    }

    public static String actualizarAccessToken(ContextoMB contexto, String refreshToken) {

        try {

            ApiRequestMB request = ApiMB.request("ActualizarAccessToken", "modo", "POST", "/v1/public/tokens", contexto);
            request.body("refresh_token", refreshToken);
            request.body("grant_type", "refresh_token");
            request.headers.put("x-subCanal", "MB-MODO");

            ApiResponseMB response = ApiMB.response(request, contexto.idCobis());
            if (response.hayError()) {
                return "error";
            }

            String accessToken = response.string("access_token");
            String expiresIn = response.string("expires_in");
            String scope = response.string("scope");
            contexto.sesion().setCache("access_token", String.valueOf(response.string("access_token")));
            contexto.sesion().setCache("modo_token_expiration", LocalDateTime.now().plusSeconds(Integer.parseInt(expiresIn)).toString());

            updateTokensModo(contexto, accessToken, refreshToken, expiresIn, scope);

            return accessToken;
        } catch (Exception e) {
            return "error";
        }
    }

    public static RespuestaMB insertTokensModo(ContextoMB contexto) {
        return insertTokensModo(contexto, null, null, null, null, null);
    }

    public static RespuestaMB insertTokensModo(ContextoMB contexto, String accessToken, String refreshToken, String expiresIn, String scope, String phoneNumber) {
        String idCobis = contexto.idCobis();
        accessToken = validarString(accessToken, contexto.parametros.string("access_token"));
        refreshToken = validarString(refreshToken, contexto.parametros.string("refresh_token"));
        expiresIn = validarString(expiresIn, contexto.parametros.string("expires_in"));
        scope = validarString(scope, contexto.parametros.string("scope"));
        phoneNumber = validarString(phoneNumber, contexto.parametros.string("phone_number"));

        if (Objeto.anyEmpty(idCobis, accessToken, refreshToken, expiresIn, scope, phoneNumber)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        ApiRequestMB request = ApiMB.request("InsertTokensModo", "modo", "POST", "/v1/modo/{id_cobis}", contexto);
        request.path("id_cobis", idCobis);
        request.body("id_cobis", idCobis);
        request.body("access_token", accessToken);
        request.body("refresh_token", refreshToken);
        request.body("expires_in", expiresIn);
        request.body("scope", scope);
        request.body("telefono", phoneNumber);
        request.headers.put("x-subCanal", "MB-MODO");

        ApiResponseMB response = ApiMB.response(request, idCobis);

        if (response.hayError()) {
            return RespuestaMB.estado(validarString(String.valueOf(response.string("mensajeAlUsuario")), "ERROR"));
        }

        contexto.sesion().setCache("access_token", accessToken);
        contexto.sesion().setCache("refresh_token", refreshToken);

        return RespuestaMB.exito();
    }

    public static RespuestaMB updateTokensModo(ContextoMB contexto) {
        return updateTokensModo(contexto, null, null, null, null);
    }

    public static RespuestaMB updateTokensModo(ContextoMB contexto, String accessToken, String refreshToken, String expiresIn, String scope) {
        String idCobis = contexto.idCobis();
        accessToken = validarString(accessToken, contexto.parametros.string("access_token"));
        refreshToken = validarString(refreshToken, contexto.parametros.string("refresh_token"));
        expiresIn = validarString(expiresIn, contexto.parametros.string("expires_in"));
        scope = validarString(scope, contexto.parametros.string("scope"));

        if (Objeto.anyEmpty(idCobis, accessToken, refreshToken, expiresIn, scope)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        ApiRequestMB request = ApiMB.request("UpdateTokensModo", "modo", "PUT", "/v1/modo/{id_cobis}", contexto);
        request.path("id_cobis", idCobis);
        request.body("id_cobis", idCobis);
        request.body("access_token", accessToken);
        request.body("refresh_token", refreshToken);
        request.body("expires_in", expiresIn);
        request.body("scope", scope);
        request.headers.put("x-subCanal", "MB-MODO");

        ApiResponseMB response = ApiMB.response(request, idCobis);

        if (response.hayError()) {
            return RespuestaMB.estado(validarString(String.valueOf(response.string("mensajeAlUsuario")), "ERROR"));
        }

        return RespuestaMB.exito();
    }

    // Se agrega aca para no tocar otra cosa mas que Servidor y ApiModo

    public RespuestaMB isOnboarding(ContextoMB contexto) {
        return RespuestaMB.exito("isOnboarding", contexto.isOnboardingModo());
    }

    public RespuestaMB getUserStatus(ContextoMB contexto) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            verifyAccessToken(contexto);

            UserStatus userStatus = new UserStatus();
            ApiResponseMB respAccounts = null;

            if (!contexto.isOnboardingModo()) {
                /* Check api's health status to prevent impossible onboardings */
                RespuestaMB apiHealth = MBModo.getCryptoPem(contexto);
                if (apiHealth.hayError()) {
                    return RespuestaMB.error();
                }
                userStatus = new UserStatus(Boolean.FALSE, Boolean.FALSE, Boolean.FALSE);
            } else {
                userStatus.setOnboarding(Boolean.TRUE);
                respAccounts = this.getAccount(contexto, true);

                if (respAccounts.hayError()) {
                    if ("NOT_FOUND".equals(respAccounts.string("codigo"))) {
                        return RespuestaMB.exito("user_status", Objeto.fromJson(mapper.writeValueAsString(userStatus)));
                    }
                    if ("UNAUTHORIZED".equals(respAccounts.string("codigo"))) {
                        if (!refreshAccessToken(contexto)) {
                            return RespuestaMB.estado("ERROR_TOKEN");
                        }
                        respAccounts = this.getAccount(contexto, true);
                    }
                }

                if (respAccounts.hayError()) {
                    if ("NOT_FOUND".equals(respAccounts.string("codigo"))) {
                        return RespuestaMB.exito("user_status", Objeto.fromJson(mapper.writeValueAsString(userStatus)));
                    }
                    return RespuestaMB.error();
                }

                if ("".equals(String.valueOf(respAccounts.json))) {
                    userStatus.setAnotherBank(Boolean.TRUE);
                }

                // recorrer las cuentas y si todas son no linkeadas FALSE
                userStatus.setAccountLinking(Boolean.FALSE);
                if (!"".equals(String.valueOf(respAccounts.json))) {
                    GetAccountsResponseDTO accountMetadataDto = mapper.readValue(respAccounts.json, GetAccountsResponseDTO.class);
                    boolean isLinked = accountMetadataDto.getAccounts().stream().anyMatch(p -> p.isAccountLinked());
                    userStatus.setAccountLinking(isLinked);
                }

            }

            if (checkBlackList(contexto.idCobis())) {
                userStatus = new UserStatus(Boolean.FALSE, Boolean.FALSE, Boolean.FALSE);
            }

            return RespuestaMB.exito("user_status", Objeto.fromJson(mapper.writeValueAsString(userStatus)));

        } catch (IOException ex) {
            return RespuestaMB.estado("ERROR_EN_STATUS_USER");
        }
    }

    public static ApiResponseMB getStatus(ContextoMB contexto, Boolean active) {
        String accessToken = contexto.sesion().cache("access_token");
        ApiRequestMB request = ApiMB.request("GetAccounts", "modo", "GET", "/v1/users/status", contexto);
        request.headers.put("DimoToken", "Bearer " + accessToken);
        request.headers.put("x-subCanal", "MB-MODO");
        ApiResponseMB response = ApiMB.response(request, contexto.idCobis());
        return response;
    }

    public RespuestaMB getAccountMetadata(ContextoMB contexto) {
        String accessToken = contexto.sesion().cache("access_token");
        String path = "/v1/public/metadata/account";
        ApiRequestMB request = ApiMB.request("getInfoAccountUser", "modo", "GET", path, contexto);
        request.headers.put("DimoToken", "Bearer " + accessToken);
        request.headers.put("x-subCanal", SUB_CHANNEL_MODO);
        ApiResponseMB response = ApiMB.response(request, contexto.idCobis());
        if (response.hayError()) {
            if (!"UNAUTHORIZED".equals(response.string("codigo"))) {
                return RespuestaMB.estado(validarString(String.valueOf(response.string("mensajeAlUsuario")), "ERROR"));
            }
            if (refreshAccessToken(contexto)) {
                return getAccountMetadata(contexto);
            }
            return RespuestaMB.estado("NO_ONBOARD");
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY);
            AccountMetadataDto accountMetadataDto = mapper.readValue(response.json, AccountMetadataDto.class);
            accountMetadataDto.setOnbordeado(contexto.isOnboardingModo());
            if (accountMetadataDto.getCurrencyPermit() == null) {
                accountMetadataDto.setCurrencyPermit(new ArrayList<>());
            }

            Objeto resp = Objeto.fromJson(mapper.writeValueAsString(accountMetadataDto));
            RespuestaMB respuesta = RespuestaMB.exito("accountMetadata", resp);
            return respuesta;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public RespuestaMB getAccounts(ContextoMB contexto) {
        Boolean onlyLinkedAccount = contexto.parametros.bool("onlyLinkedAccount");
        Boolean aux = Objects.isNull(onlyLinkedAccount) ? false : onlyLinkedAccount;
        ApiResponseMB response = this.getAccount(contexto, aux);
        if (response.hayError()) {
            if (!"UNAUTHORIZED".equals(response.string("codigo"))) {
                return RespuestaMB.estado(validarString(String.valueOf(response.string("mensajeAlUsuario")), "ERROR"));
            }
            if (refreshAccessToken(contexto)) {
                return getAccounts(contexto);
            }
            return RespuestaMB.estado("NO_ONBOARD");
        }

        if (!"".equals(response.string("accounts"))) {
            return RespuestaMB.exito("accounts", Objeto.fromJson(response.string("accounts")));
        }
        if ("{}".equals(String.valueOf(response.json)) || "".equals(String.valueOf(response.json))) {
            return RespuestaMB.estado("ANOTHER_BANK");
        }
        return RespuestaMB.estado("ERROR");
    }

    private ApiResponseMB getAccount(ContextoMB contexto, Boolean onlyLinkedAccount) {
        String accessToken = contexto.sesion().cache("access_token");
        Boolean aux = Objects.isNull(onlyLinkedAccount) ? false : onlyLinkedAccount;
        String path = "/v1/public/accounts?onlyLinkedAccounts=" + aux;
        ApiRequestMB request = ApiMB.request("GetAccounts", "modo", "GET", path, contexto);
        request.headers.put("DimoToken", "Bearer " + accessToken);
        request.headers.put("x-subCanal", SUB_CHANNEL_MODO);
        ApiResponseMB response = ApiMB.response(request, contexto.idCobis());

        return response;
    }

    public RespuestaMB getAllAccounts(ContextoMB contexto) {
        verifyAccessToken(contexto);
        ApiResponseMB response = this.getAccount(contexto, false);

        if (checkBlackList(contexto.idCobis())) {
            return RespuestaMB.estado("ERROR");
        }

        if (response.hayError()) {
            if ("UNAUTHORIZED".equals(response.string("codigo"))) {
                if (refreshAccessToken(contexto)) {
                    return getAllAccounts(contexto);
                }
                return RespuestaMB.estado("NO_ONBOARD");
            }
            return RespuestaMB.estado(validarString(String.valueOf(response.string("mensajeAlUsuario")), "ERROR"));
        }

        if (!"".equals(response.string("accounts"))) {
            return RespuestaMB.exito("accounts", Objeto.fromJson(response.string("accounts")));
        }

        if ("".equals(String.valueOf(response.json))) {
            return RespuestaMB.estado("ANOTHER_BANK");
        }

        return RespuestaMB.estado("ERROR");
    }

    @SuppressWarnings("unchecked")
    public static RespuestaMB startOnboarding(ContextoMB contexto) {
        Map<String, Object> map;
        ObjectMapper mapper = new ObjectMapper();
        String dni = contexto.parametros.string("dni");
        String firstName = contexto.parametros.string("first_name");
        String lastName = contexto.parametros.string("last_name");
        String email = contexto.parametros.string("email_address");
        String gender = contexto.parametros.string("gender");

        RespuestaMB telPersonal = MBPersona.telefonoPersonal(contexto);
        String telSinFormato = (String) telPersonal.get("celular");
        String phoneNumber = telSinFormato.replace("-", "");

        if (Objeto.anyEmpty(dni, firstName, lastName, phoneNumber, email)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        if (checkBlackList(contexto.idCobis())) {
            return RespuestaMB.estado("ERROR");
        }

        phoneNumber = validarPhoneNumber(phoneNumber);

        ApiRequestMB request = ApiMB.request("AltaDeUsuario", "modo", "POST", "/v1/users", contexto);
        request.body("dni", dni);
        request.body("first_name", firstName);
        request.body("last_name", lastName);
        request.body("phone_number", phoneNumber);
        request.body("email", email);
        request.body("gender", gender);
        request.headers.put("x-subCanal", "MB-MODO");

        ApiResponseMB response = ApiMB.response(request, contexto.idCobis());

        if (response.hayError()) {
            return RespuestaMB.estado(validarString(String.valueOf(response.string("mensajeAlUsuario")), "ERROR"));
        }

        try {
            map = mapper.readValue(response.toJson(), Map.class);
            if (map.containsKey("verification_id") && map.containsKey("verification_expires_in")) {
                JsonObject json = new JsonObject();
                json.addProperty("id", map.get("verification_id").toString());
                json.addProperty("expires_in", Integer.valueOf(map.get("verification_expires_in").toString()));
                return RespuestaMB.exito("verification", json);
            } else {
                return map.containsKey("dni") ? RespuestaMB.estado("USUARIO_EXISTE") : RespuestaMB.estado("ERROR_EN_START_ONBOARDING");
            }
        } catch (IOException ex) {
            return RespuestaMB.estado("ERROR_EN_ONBOARDING");
        }
    }

    public static RespuestaMB confirmOnboarding(ContextoMB contexto) {
        String dni = contexto.parametros.string("dni");
        String phoneNumber = contexto.parametros.string("phone_number");
        String verificationId = contexto.parametros.string("verification_id");
        String verificationCode = contexto.parametros.string("verification_code");

        if (Objeto.anyEmpty(dni, phoneNumber, verificationId, verificationCode)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        if (checkBlackList(contexto.idCobis())) {
            return RespuestaMB.estado("ERROR");
        }

        ApiRequestMB request = ApiMB.request("ConfirmarOnboarding", "modo", "POST", "/v1/users/confirm-onboarding", contexto);
        request.body("dni", dni);
        request.body("phone_number", phoneNumber);
        request.body("verification_id", verificationId);
        request.body("verification_code", verificationCode);
        request.headers.put("x-subCanal", "MB-MODO");

        ApiResponseMB response = ApiMB.response(request, contexto.idCobis());

        if (response.hayError()) {
            return RespuestaMB.estado(validarString(String.valueOf(response.string("mensajeAlUsuario")), "ERROR"));
        }

        return RespuestaMB.exito();
    }

    public RespuestaMB suggestedContacts(ContextoMB contexto) {
        verifyAccessToken(contexto);
        if (checkBlackList(contexto.idCobis())) {
            return RespuestaMB.estado("ERROR");
        }

        ApiResponseMB response = invocatesuggestedContacts(contexto);
        if (response.hayError()) {
            if ("UNAUTHORIZED".equals(response.string("codigo"))) {
                if (refreshAccessToken(contexto)) {
                    return suggestedContacts(contexto);
                }
            } else {
                return RespuestaMB.estado(validarString(String.valueOf(response.string("mensajeAlUsuario")), "ERROR"));
            }
        }
        return RespuestaMB.exito("contacts", response);
    }

    private ApiResponseMB invocatesuggestedContacts(ContextoMB contexto) {
        ApiRequestMB request = ApiMB.request("SuggestedContacts", "modo", "GET", "/v1/users/suggested_contacts", contexto);
        request.headers.put("x-subCanal", "MB-MODO");
        String accessToken = contexto.sesion().cache("access_token");
        request.headers.put("DimoToken", "Bearer " + accessToken);
        return ApiMB.response(request, contexto.idCobis());
    }

    public static RespuestaMB listOfPhoneNumbersAreInPS(ContextoMB contexto) {
        ObjectMapper mapper = new ObjectMapper();
        List<Object> lista = contexto.parametros.toList("contacts");
        List<Object> listOfPhoneNumbers = new ArrayList<>();

        if (Objeto.anyEmpty(lista)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        if (checkBlackList(contexto.idCobis())) {
            return RespuestaMB.estado("ERROR");
        }

        for (Object obj : lista) {
            try {
                String number = validarPhoneNumber(obj.toString());
                if (!number.equals("")) {
                    listOfPhoneNumbers.add((Object) mapper.convertValue(new PhoneNumberDto(number), Map.class));
                }
            } catch (Exception e) {
            }
        }

        ApiRequestMB request = ApiMB.request("ListOfPhoneNumbersAreInPS", "modo", "POST", "/v1/users/list", contexto);
        request.body(Objeto.fromList(listOfPhoneNumbers));
        request.headers.put("x-subCanal", "MB-MODO");

        ApiResponseMB response = ApiMB.response(request, contexto.idCobis());

        if (response.hayError()) {
            if ("UNAUTHORIZED".equals(response.string("codigo"))) {
                if (refreshAccessToken(contexto)) {
                    return listOfPhoneNumbersAreInPS(contexto);
                }
            } else {
                return RespuestaMB.estado(validarString(String.valueOf(response.string("mensajeAlUsuario")), "ERROR"));
            }
        }
        return RespuestaMB.exito("contacts", response);
    }

    public static RespuestaMB checkUserPhoneNumberRegistration(ContextoMB contexto) {
        String phoneNumber = contexto.parametros.string("phoneNumber");

        if (Objeto.anyEmpty(phoneNumber)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        if (checkBlackList(contexto.idCobis())) {
            return RespuestaMB.estado("ERROR");
        }

        ApiRequestMB request = ApiMB.request("CheckUserPhoneNumberRegistration", "modo", "POST", "/v1/users/{phoneNumber}", contexto);
        request.path("phoneNumber", phoneNumber);
        request.headers.put("x-subCanal", "MB-MODO");

        ApiResponseMB response = ApiMB.response(request, contexto.idCobis());

        if (response.hayError()) {
            if ("UNAUTHORIZED".equals(response.string("codigo"))) {
                if (refreshAccessToken(contexto)) {
                    return checkUserPhoneNumberRegistration(contexto);
                } else {
                    return RespuestaMB.estado("ERROR_TOKEN");
                }
            } else {
                return RespuestaMB.estado(validarString(String.valueOf(response.string("mensajeAlUsuario")), "ERROR"));
            }
        }

        return RespuestaMB.exito("respuesta", response);
    }

    public static RespuestaMB refreshVerification(ContextoMB contexto) {
        String dni = contexto.parametros.string("dni");
        String phoneNumber = contexto.parametros.string("phone_number");
        String verificationId = contexto.parametros.string("verification_id");

        if (Objeto.anyEmpty(dni, phoneNumber, verificationId)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        if (checkBlackList(contexto.idCobis())) {
            return RespuestaMB.estado("ERROR");
        }

        ApiRequestMB request = ApiMB.request("ActualizarVerificacion", "modo", "POST", "/v1/public/verifications/refresh", contexto);
        request.body("dni", dni);
        request.body("phone_number", phoneNumber);
        request.body("verification_id", verificationId);
        request.headers.put("x-subCanal", "MB-MODO");

        ApiResponseMB response = ApiMB.response(request, contexto.idCobis());

        if (response.hayError()) {
            return RespuestaMB.estado(validarString(String.valueOf(response.string("mensajeAlUsuario")), "ERROR"));
        }

        return RespuestaMB.exito("respuesta", response);
    }

    @SuppressWarnings("unchecked")
    public static RespuestaMB confirmVerification(ContextoMB contexto) {
        Map<String, Object> map;
        String dni = contexto.parametros.string("dni");
        String phoneNumber = contexto.parametros.string("phone_number");
        String verificationCode = contexto.parametros.string("verification_code");
        String verificationId = contexto.parametros.string("verification_id");

        if (Objeto.anyEmpty(dni, phoneNumber, verificationCode, verificationId)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        if (checkBlackList(contexto.idCobis())) {
            return RespuestaMB.estado("ERROR");
        }

        phoneNumber = validarPhoneNumber(phoneNumber);

        ApiRequestMB request = ApiMB.request("ActualizarVerificacion", "modo", "POST", "/v1/public/verifications/confirm", contexto);
        request.body("dni", dni);
        request.body("phone_number", phoneNumber);
        request.body("verification_code", verificationCode);
        request.body("verification_id", verificationId);
        request.headers.put("x-subCanal", "MB-MODO");

        ApiResponseMB response = ApiMB.response(request, contexto.idCobis());

        if (response.hayError()) {
            return RespuestaMB.estado(validarString(String.valueOf(response.string("mensajeAlUsuario")), "ERROR"));
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            map = mapper.readValue(response.toJson(), Map.class);
            if (map.containsKey("access_token") && map.containsKey("refresh_token")) {
                String accessToken = map.get("access_token").toString();
                String refreshToken = map.get("refresh_token").toString();
                String expiresIn = map.get("expires_in").toString();
                String scope = map.get("token_type").toString();

                insertTokensModo(contexto, accessToken, refreshToken, expiresIn, scope, phoneNumber);

                return RespuestaMB.exito();
            } else {
                return RespuestaMB.error();
            }
        } catch (IOException ex) {
            return RespuestaMB.estado("ERROR_EN_CONFIRM_VERIFICATION");
        }
    }

    public static Boolean refreshAccessToken(ContextoMB contexto) {

        try {
            String refreshToken = contexto.sesion().cache("refresh_token");

            if (refreshToken == null) {
                consultTokensModo(contexto);
                refreshToken = contexto.sesion().cache("refresh_token");
            }

            ApiRequestMB request = ApiMB.request("ActualizarAccessToken", "modo", "POST", "/v1/public/tokens", contexto);
            request.body("refresh_token", refreshToken);
            request.body("grant_type", "refresh_token");
            request.headers.put("x-subCanal", "MB-MODO");

            ApiResponseMB response = ApiMB.response(request, contexto.idCobis());
            if (response.hayError()) {
                return Boolean.FALSE;
            }

            String accessToken = response.string("access_token");
            String expiresIn = response.string("expires_in");
            String scope = response.string("scope");
            contexto.sesion().setCache("access_token", String.valueOf(response.string("access_token")));

            // Setea la fecha de expiracion del token en la sesion para uso en las
            // siguientes llamadas
            contexto.sesion().setCache("modo_token_expiration", LocalDateTime.now().plusSeconds(Integer.parseInt(expiresIn)).toString());

            updateTokensModo(contexto, accessToken, refreshToken, expiresIn, scope);

            return Boolean.TRUE;
        } catch (Exception e) {
            // If there's any exception, return false to prevent unwanted loops
            return Boolean.FALSE;
        }
    }

    /**
     * Verifica si el token está vencido, en cuyo caso lo renueva. Si no, no hace
     * nada
     *
     * @param contexto
     */
    private static void verifyAccessToken(ContextoMB contexto) {

        // Si no hay access_token es porque el usuario no está onbordeado
        if (String.valueOf(contexto.sesion().cache("access_token")).equals("")) {
            return;
        }

        // Si el token expira "en el futuro" (después que now() + delta) entonces
        // estamos en la ventana de validez y devolvemos true
        String tokenExpiryDate = contexto.sesion().cache("modo_token_expiration");
        LocalDateTime expiringDate = LocalDateTime.parse(tokenExpiryDate);
        if (expiringDate.isAfter(LocalDateTime.now().plusSeconds(DELTA_PREVENTIVE_SECONDS))) {
            return;
        }

        refreshAccessToken(contexto);
    }

    public static RespuestaMB revokeRefreshToken(ContextoMB contexto) {
        String refreshToken = contexto.parametros.string("refresh_token");

        if (Objeto.anyEmpty(refreshToken)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        ApiRequestMB request = ApiMB.request("RevocarAccessToken", "modo", "POST", "/v1/public/tokens/revoke", contexto);
        request.body("refresh_token", refreshToken);
        request.headers.put("x-subCanal", "MB-MODO");

        ApiResponseMB response = ApiMB.response(request, contexto.idCobis());

        if (response.hayError()) {
            return RespuestaMB.estado(validarString(String.valueOf(response.string("mensajeAlUsuario")), "ERROR"));
        }

        return RespuestaMB.exito("respuesta", response);
    }

    public static RespuestaMB linkBankAccount(ContextoMB contexto) {
        verifyAccessToken(contexto);

        String dni = contexto.parametros.string("dni");
        List<Object> accounts = contexto.parametros.toList("accounts");

        if (Objeto.anyEmpty(dni, accounts)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        if (checkBlackList(contexto.idCobis())) {
            return RespuestaMB.estado("ERROR");
        }

        ApiRequestMB request = ApiMB.request("LinkBankAccount", "modo", "POST", "/v1/public/accounts", contexto);
        request.body("dni", dni);
        request.body("id_cobis", contexto.idCobis());
        request.body("accounts", accounts);
        request.headers.put("DimoToken", "Bearer " + contexto.sesion().cache("access_token"));
        request.headers.put("x-subCanal", "MB-MODO");

        ApiResponseMB response = ApiMB.response(request, contexto.idCobis());

        if ("UNAUTHORIZED".equals(checkResponse(response))) {
            if (refreshAccessToken(contexto)) {
                return linkBankAccount(contexto);
            } else {
                return RespuestaMB.estado("ERROR_TOKEN");
            }
        } else if (!"OK".equals(checkResponse(response))) {
            return RespuestaMB.estado(checkResponse(response));
        }

        return RespuestaMB.exito();
    }

    public static RespuestaMB inviteNotification(ContextoMB contexto) {
        verifyAccessToken(contexto);

        String dni = contexto.parametros.string("dni");
        String contactPhone = contexto.parametros.string("contact_phone");

        if (Objeto.anyEmpty(dni, contactPhone)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        if (checkBlackList(contexto.idCobis())) {
            return RespuestaMB.estado("ERROR");
        }

        ApiRequestMB request = ApiMB.request("LinkBankAccount", "modo", "POST", "/v1/public/notifications/invite", contexto);
        request.body("recipient_phone", contactPhone);
        request.headers.put("DimoToken", "Bearer " + contexto.sesion().cache("access_token"));
        request.headers.put("x-subCanal", "MB-MODO");

        ApiResponseMB response = ApiMB.response(request, contexto.idCobis());

        if ("UNAUTHORIZED".equals(checkResponse(response))) {
            if (refreshAccessToken(contexto)) {
                return inviteNotification(contexto);
            } else {
                return RespuestaMB.estado("ERROR_TOKEN");
            }
        } else if (!"OK".equals(checkResponse(response))) {
            return RespuestaMB.estado(checkResponse(response));
        }

        return RespuestaMB.exito("message", validarString(String.valueOf(response.string("message")), ""));
    }

    @SuppressWarnings({"unchecked"})
    public RespuestaMB createTransfer(ContextoMB contexto) {
        TransferModoService transferModoService = new TransferModoService();
        Map<String, Object> transferMap = null;
        ObjectMapper mapper = new ObjectMapper();

        if (contexto.idCobis() == null) {
            return RespuestaMB.estado("SIN_PSEUDO_SESION");
        }

        verifyAccessToken(contexto);
        String sourceCbu = contexto.parametros.string("source_cbu");
        String recipientPhone = contexto.parametros.string("recipient_phone");

        RespuestaMB conditionResp = preconditionDataValidated(contexto);
        if (conditionResp != null) {
            return conditionResp;
        }
        recipientPhone = validarPhoneNumber(recipientPhone);
        ApiResponseMB response = transferModoService.invoqueModoTransfer(contexto, recipientPhone);
        if ("UNAUTHORIZED".equals(checkResponse(response))) {
            if (refreshAccessToken(contexto)) {
                return createTransfer(contexto);
            }
            return RespuestaMB.estado("ERROR_TOKEN", contexto.csmIdAuth);

        } else if (!"OK".equals(checkResponse(response))) {
            return RespuestaMB.estado(checkResponse(response), contexto.csmIdAuth);
        }

        try {
            transferMap = mapper.readValue(response.toJson(), Map.class);
            if (transferMap.containsKey("id_transaction") && transferMap.containsKey("recipent_cbu")) {
                enviarMail(contexto, contexto.persona().email(), new BigDecimal(String.valueOf(transferMap.get("amount"))), sourceCbu, String.valueOf(transferMap.get("recipent_cbu")), String.valueOf(transferMap.get("id_transaction")), ConfigMB.string("doppler_transferencia_origen"));
                transferMap.remove("recipent_cbu");
                crearComprobanteTransferencia(contexto, response, sourceCbu);
            }
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
        contexto.limpiarSegundoFactor();
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        ProductosService.eliminarCacheProductos(contexto);
        return RespuestaMB.exito("transfer_info", transferMap).set("csmIdAuth", contexto.csmIdAuth);
    }

    protected Boolean isPreferentContact(ContextoMB contexto, String recipientPhone) {
        ObjectMapper mapper = new ObjectMapper();
        ApiResponseMB suggestedContacts = this.invocatesuggestedContacts(contexto);
        List<SuggestContactDto> preferentContacList;
        try {
            preferentContacList = mapper.readValue(suggestedContacts.json, new TypeReference<List<SuggestContactDto>>() {
            });
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        boolean isPreferentContact = preferentContacList.stream().anyMatch(contact -> (contact.getPhoneNumber() != null && contact.getPhoneNumber().equals(recipientPhone)));
        return isPreferentContact;
    }

    private RespuestaMB preconditionDataValidated(ContextoMB contexto) {
        DisableService disableService = new DisableService();
        BigDecimal amount = new BigDecimal(contexto.parametros.string("amount"));
        if (Objeto.anyEmpty(contexto.parametros.string("source_cbu"), contexto.parametros.string("recipient_phone"), amount, contexto.parametros.string("reason_code"))) {
            return RespuestaMB.parametrosIncorrectos();
        }

        if (checkBlackList(contexto.idCobis())) {
            return RespuestaMB.estado("ERROR");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return RespuestaMB.estado("MONTO_ERRONEO");
        }

        String csmId = contexto.parametros.string("csmId");
        String checksum = contexto.parametros.string("checksum");

        boolean esMigrado = contexto.esMigrado(contexto);

        if (esMigrado && Objeto.anyEmpty(csmId, checksum))
            return RespuestaMB.parametrosIncorrectos();

        RespuestaMB respuestaValidaTransaccion = contexto.validarTransaccion(contexto, esMigrado, "transferencia", JourneyTransmitEnum.MB_INICIO_SESION);
        if (respuestaValidaTransaccion.hayError())
            return respuestaValidaTransaccion;

        boolean disable_48hrs = ConfigMB.bool("prendido_disable_48hrs", false);
        if (disable_48hrs) {
            // ===================INICIO=======================
            String tipo = ConfigMB.string("cambio_information_no_permitido");
            LocalDateTime nowTime = LocalDateTime.now();
            nowTime = nowTime.plusHours(-1 * disableService.calculateHourDelay(nowTime));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String inicio = nowTime.format(formatter);

            List<Objeto> registros = ContextoMB.obtenerContador(contexto.idCobis(), tipo, inicio);
            if (Objects.isNull(registros)) {
                return RespuestaMB.estado("ERROR", contexto.csmIdAuth);
            }
            Boolean permission = disableService.getEnabledToOperator(registros);
            if (!permission) {
                return RespuestaMB.estado("ERROR_TRANSFER_BLOCK", contexto.csmIdAuth);
            }
            // ===================FIN=======================
        }

        return null;
    }

    /**
     * Crea un comprobante de la transferencia en el contexto para luego ser
     * generado
     *
     * @param contexto
     * @param response
     * @param cuentaOrigen
     */
    private static void crearComprobanteTransferencia(ContextoMB contexto, ApiResponseMB response, String cuentaOrigen) {

        Map<String, String> comprobante = new HashMap<>();

        String fechaHora = response.string("date");

        CuentaTercero cuentaTercero = new CuentaTercero(contexto, response.string("recipent_cbu"));
        String idComprobante = response.string("id_transaction");
        BigDecimal importe = contexto.parametros.bigDecimal("amount");

        comprobante.put("FECHA_HORA", fechaHora);
        comprobante.put("ID_COMPROBANTE", idComprobante);
        comprobante.put("NOMBRE_BENEFICIARIO", cuentaTercero.titular());
        comprobante.put("IMPORTE", "ARS " + Formateador.importe(importe));
        comprobante.put("TIPO_TRANSFERENCIA", "A otro cliente");
        comprobante.put("CUENTA_ORIGEN", cuentaOrigen);
        comprobante.put("CUENTA_DESTINO", cuentaTercero.cbu());
        comprobante.put("CUIT_DESTINO", cuentaTercero.cuit());
        comprobante.put("CONCEPTO", "Varios");
        comprobante.put("COMISION", "ARS 0,00");
        comprobante.put("IMPUESTOS", "ARS 0,00");

        comprobante.put("NOMBRE_ORIGEN", contexto.persona().nombreCompleto().toUpperCase());
        comprobante.put("NOMBRE_BANCO", cuentaTercero.banco());
        comprobante.put("MENSAJE", "");

        contexto.sesion().setComprobante(generarIdComprobanteTransferencia(idComprobante), comprobante);
    }

    /**
     * Construye el id de comprobante de la transferencia para vincular en el
     * contexto.
     *
     * @param idComprobante El id original de la transacción
     * @return
     */
    private static String generarIdComprobanteTransferencia(String idComprobante) {
        return "transferencia_" + idComprobante;
    }

    // Se agrega aca para no tocar otra cosa mas que Servidor y ApiModo
    public static class LimitAmounts {

        @JsonProperty(value = "disponible_diario")
        private BigDecimal disponibleDiario = BigDecimal.ZERO;

        @JsonProperty(value = "por_transferencia")
        private BigDecimal porTransferencia = BigDecimal.ZERO;

        public LimitAmounts() {
        }

        public BigDecimal getDisponibleDiario() {
            return disponibleDiario;
        }

        public void setDisponibleDiario(BigDecimal disponibleDiario) {
            this.disponibleDiario = disponibleDiario;
        }

        public BigDecimal getPorTransferencia() {
            return porTransferencia;
        }

        public void setPorTransferencia(BigDecimal porTransferencia) {
            this.porTransferencia = porTransferencia;
        }
    }

    @SuppressWarnings("unchecked")
    public static RespuestaMB limitAmounts(ContextoMB contexto) {
        Map<String, Object> map;
        ObjectMapper mapper = new ObjectMapper();
        try {
            verifyAccessToken(contexto);

            ApiRequestMB request = ApiMB.request("AvailableLimit", "modo", "GET", "/v1/available_limit/{id_cobis}", contexto);
            request.path("id_cobis", contexto.idCobis());
            request.headers.put("x-subCanal", "MB-MODO");

            ApiResponseMB response = ApiMB.response(request, contexto.idCobis());

            if ("UNAUTHORIZED".equals(checkResponse(response))) {
                if (refreshAccessToken(contexto)) {
                    return limitAmounts(contexto);
                } else {
                    return RespuestaMB.estado("ERROR_TOKEN");
                }
            }

            map = mapper.readValue(response.toJson(), Map.class);
            limitAmounts.setDisponibleDiario(new BigDecimal(ConfigMB.string("configuracion_limite_transferencia_modo")));
            limitAmounts.setPorTransferencia(new BigDecimal(map.get("LIMITE").toString()));

            return RespuestaMB.exito("montos_maximos", Objeto.fromJson(mapper.writeValueAsString(limitAmounts)));
        } catch (JsonProcessingException ex) {
            return RespuestaMB.estado("ERROR_IN_AVAILABLE_LIMIT");
        }
    }

    public static RespuestaMB createCoupon(ContextoMB contexto) {

        verifyAccessToken(contexto);

        BigDecimal amount = new BigDecimal(contexto.parametros.string("amount"));
        String toCbu = contexto.parametros.string("to_cbu"); // Corresponde al CBU del cliente BH donde quiere recibir el
        // dinero.
        Boolean accountLinked = contexto.parametros.bool("account_linked");
        String note = contexto.parametros.string("note", "");

        if (Objeto.anyEmpty(amount, toCbu)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        if (checkBlackList(contexto.idCobis())) {
            return RespuestaMB.estado("ERROR");
        }

        if (amount.compareTo(BigDecimal.ZERO) == 0 || amount.compareTo(BigDecimal.ZERO) == -1) {
            return RespuestaMB.estado("MONTO_ERRONEO");
        }

        ApiRequestMB request = ApiMB.request("CreateCoupon", "modo", "POST", "/v1/coupons", contexto);
        request.body("amount", amount);
        request.body("to_cbu", toCbu);
        request.body("account_linked", accountLinked);
        request.body("note", note);
        request.headers.put("DimoToken", "Bearer " + contexto.sesion().cache("access_token"));
        request.headers.put("x-subCanal", "MB-MODO");

        ApiResponseMB response = ApiMB.response(request, contexto.idCobis());

        if ("UNAUTHORIZED".equals(checkResponse(response))) {
            if (refreshAccessToken(contexto)) {
                return createCoupon(contexto);
            } else {
                return RespuestaMB.estado("ERROR_TOKEN");
            }
        } else if (!"OK".equals(checkResponse(response))) {
            return RespuestaMB.estado(checkResponse(response));
        }

        return RespuestaMB.exito("coupon_info", response);
    }

    public static RespuestaMB getCoupon(ContextoMB contexto) {
        verifyAccessToken(contexto);
        String couponId = contexto.parametros.string("coupon_id");

        if (contexto.idCobis() == null) {
            return RespuestaMB.estado("SIN_PSEUDO_SESION");
        }

        if (Objeto.anyEmpty(couponId)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        if (checkBlackList(contexto.idCobis())) {
            return RespuestaMB.estado("ERROR");
        }

        ApiRequestMB request = ApiMB.request("GetCoupon", "modo", "GET", "/v1/coupons/{coupon_id}", contexto);
        request.path("coupon_id", couponId);
        request.headers.put("DimoToken", "Bearer " + contexto.sesion().cache("access_token"));
        request.headers.put("x-subCanal", "MB-MODO");

        ApiResponseMB response = ApiMB.response(request, contexto.idCobis());

        if ("UNAUTHORIZED".equals(checkResponse(response))) {
            if (refreshAccessToken(contexto)) {
                return getCoupon(contexto);
            } else {
                return RespuestaMB.estado("ERROR_TOKEN");
            }
        } else if (!"OK".equals(checkResponse(response))) {
            return RespuestaMB.estado(checkResponse(response));
        }

        return RespuestaMB.exito("coupon_info", response);
    }

    @SuppressWarnings("unchecked")
    public static RespuestaMB payCoupon(ContextoMB contexto) {
        DisableService disableService = new DisableService();
        verifyAccessToken(contexto);
        BigDecimal amount = new BigDecimal(contexto.parametros.string("amount"));
        String couponId = contexto.parametros.string("coupon_id");
        String fromCbu = contexto.parametros.string("from_cbu");
        Boolean accountLinked = contexto.parametros.bool("account_linked");
        String reasonCode = contexto.parametros.string("reason_code");
        String currencyCode = contexto.parametros.string("currency_code");
        String mail = contexto.parametros.string("mail", "");

        if (contexto.idCobis() == null) {
            return RespuestaMB.estado("SIN_PSEUDO_SESION");
        }

        if (Objeto.anyEmpty(couponId, fromCbu, reasonCode, currencyCode)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        if (checkBlackList(contexto.idCobis())) {
            return RespuestaMB.estado("ERROR");
        }
        // ===================INICIO=======================
        String tipo = ConfigMB.string("cambio_information_no_permitido");
        LocalDateTime nowTime = LocalDateTime.now();
        nowTime = nowTime.plusHours(-1 * disableService.calculateHourDelay(nowTime));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String inicio = nowTime.format(formatter);

        List<Objeto> registros = ContextoMB.obtenerContador(contexto.idCobis(), tipo, inicio);
        if (Objects.isNull(registros)) {
            return RespuestaMB.estado("ERROR");
        }
        Boolean permission = disableService.getEnabledToOperator(registros);
        if (!permission) {
            return RespuestaMB.estado("ERROR_TRANSFER_BLOCK");
        }
        // ===================FIN=======================
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return RespuestaMB.estado("MONTO_ERRONEO");
        }

        String csmId = contexto.parametros.string("csmId");
        String checksum = contexto.parametros.string("checksum");

        boolean esMigrado = contexto.esMigrado(contexto);

        if (esMigrado && Objeto.anyEmpty(csmId, checksum))
            return RespuestaMB.parametrosIncorrectos();

        RespuestaMB respuestaValidaTransaccion = contexto.validarTransaccion(contexto, esMigrado, "transferencia", JourneyTransmitEnum.MB_INICIO_SESION);
        if (respuestaValidaTransaccion.hayError())
            return respuestaValidaTransaccion;

        ApiRequestMB request = ApiMB.request("PayCoupon", "modo", "POST", "/v1/coupons/{coupon_id}/pay", contexto);
        request.path("coupon_id", couponId);
        request.body("from_cbu", fromCbu);
        request.body("account_linked", accountLinked);
        request.body("reason_code", reasonCode);
        request.body("currency_code", currencyCode);
        request.headers.put("DimoToken", "Bearer " + contexto.sesion().cache("access_token"));
        request.headers.put("x-subCanal", "MB-MODO");

        ApiResponseMB response = ApiMB.response(request, contexto.idCobis());

        if ("UNAUTHORIZED".equals(checkResponse(response))) {
            if (refreshAccessToken(contexto)) {
                return payCoupon(contexto);
            }
            return RespuestaMB.estado("ERROR_TOKEN", contexto.csmIdAuth);

        } else if (!"OK".equals(checkResponse(response))) {
            return RespuestaMB.estado(checkResponse(response), contexto.csmIdAuth);
        }
        Map<String, Object> map = null;
        ObjectMapper mapper = new ObjectMapper();
        try {
            map = mapper.readValue(response.toJson(), Map.class);
            if (map.containsKey("id_transaction") && map.containsKey("recipent_cbu")) {
                enviarMail(contexto, mail, new BigDecimal(String.valueOf(map.get("amount"))), fromCbu, String.valueOf(map.get("recipent_cbu")), String.valueOf(map.get("id_transaction")), ConfigMB.string("doppler_transferencia_origen"));
                map.remove("recipent_cbu");
                if (!ConfigMB.esProduccion()) {
                    crearComprobanteTransferencia(contexto, response, fromCbu);
                }
            }
        } catch (IOException ex) {
        }
        contexto.limpiarSegundoFactor();
        ProductosService.eliminarCacheProductos(contexto);
        return RespuestaMB.exito("pay_info", map).set("csmIdAuth", contexto.csmIdAuth);
    }

    public static RespuestaMB getCards(ContextoMB contexto) {
        verifyAccessToken(contexto);

        String onboarding = contexto.parametros.string("onboarding", "false");

        if (Objeto.anyEmpty(onboarding)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        if (checkBlackList(contexto.idCobis())) {
            return RespuestaMB.estado("ERROR");
        }

        ApiRequestMB request = ApiMB.request("GetCards", "modo", "GET", "/v1/public/cards/{onboarding}", contexto);
        request.path("onboarding", onboarding);
        request.headers.put("DimoToken", "Bearer " + contexto.sesion().cache("access_token"));
        request.headers.put("x-subCanal", "MB-MODO");

        ApiResponseMB response = ApiMB.response(request, contexto.idCobis());

        if ("UNAUTHORIZED".equals(checkResponse(response))) {
            if (refreshAccessToken(contexto)) {
                return getCards(contexto);
            } else {
                return RespuestaMB.estado("ERROR_TOKEN");
            }
        } else if (!"OK".equals(checkResponse(response))) {
            return RespuestaMB.estado(checkResponse(response));
        }

        if ("".equals(String.valueOf(response.json))) {
            return RespuestaMB.estado("CLIENTE_SIN_TARJETAS");
        }

        return RespuestaMB.exito("cards", response);

    }

    public static RespuestaMB createPayment(ContextoMB contexto) {
        verifyAccessToken(contexto);

        String qr = contexto.parametros.string("qr");
        Objeto paymentCard = contexto.parametros.objeto("payment_card");
        Objeto fingerprint = contexto.parametros.objeto("fingerprint");

        if (Objeto.anyEmpty(qr, paymentCard)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        if (checkBlackList(contexto.idCobis())) {
            return RespuestaMB.estado("ERROR");
        }

        ApiRequestMB request = ApiMB.request("CreatePayment", "modo", "POST", "/v1/payments", contexto);
        request.body("qr", qr);
        request.body("payment_card", paymentCard);
        request.body("fingerprint", fingerprint);
        request.headers.put("DimoToken", "Bearer " + contexto.sesion().cache("access_token"));
        request.headers.put("RefreshToken", contexto.sesion().cache("refresh_token"));
        request.headers.put("x-subCanal", "MB-MODO");
        request.headers.put("x-api-header", "1.1.0");

        ApiResponseMB response = ApiMB.response(request, contexto.idCobis());

        if ("UNAUTHORIZED".equals(checkResponse(response))) {
            if (refreshAccessToken(contexto)) {
                return createPayment(contexto);
            } else {
                return RespuestaMB.estado("ERROR_TOKEN");
            }
        } else if (!"OK".equals(checkResponse(response))) {
            return RespuestaMB.estado(checkResponse(response));
        }

        return RespuestaMB.exito("payment_info", response);

    }

    public static RespuestaMB getPayment(ContextoMB contexto) {
        verifyAccessToken(contexto);

        String bankPaymentId = contexto.parametros.string("bank_payment_id");

        if (Objeto.anyEmpty(bankPaymentId)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        if (checkBlackList(contexto.idCobis())) {
            return RespuestaMB.estado("ERROR");
        }

        ApiRequestMB request = ApiMB.request("GetPayment", "modo", "GET", "/v1/payments/{bank_payment_id}", contexto);
        request.path("bank_payment_id", bankPaymentId);
        request.headers.put("DimoToken", "Bearer " + contexto.sesion().cache("access_token"));
        request.headers.put("x-subCanal", "MB-MODO");

        ApiResponseMB response = ApiMB.response(request, contexto.idCobis());

        if ("UNAUTHORIZED".equals(checkResponse(response))) {
            if (refreshAccessToken(contexto)) {
                return getPayment(contexto);
            } else {
                return RespuestaMB.estado("ERROR_TOKEN");
            }
        } else if (!"OK".equals(checkResponse(response))) {
            return RespuestaMB.estado(checkResponse(response));
        }

        return RespuestaMB.exito("payment_info", response);

    }

    public static ApiResponseMB getPaymentInfo(ContextoMB contexto, String operationCode) {

        ApiRequestMB request = ApiMB.request("GetPaymentInfo", "modo", "GET", "/v1/payments/{id_cobis}/{operation_code}", contexto);
        request.path("id_cobis", contexto.idCobis());
        request.path("operation_code", operationCode);
        request.headers.put("DimoToken", "Bearer " + contexto.sesion().cache("access_token"));
        request.headers.put("x-subCanal", "MB-MODO");

        ApiResponseMB response = ApiMB.response(request, contexto.idCobis());

        if ("UNAUTHORIZED".equals(checkResponse(response))) {
            if (refreshAccessToken(contexto)) {
                return getPaymentInfo(contexto, operationCode);
            }
        } else if (!"OK".equals(checkResponse(response))) {
            return response;
        }
        return response;
    }

    public static RespuestaMB paymentQr(ContextoMB contexto) {
        verifyAccessToken(contexto);

        String qr = contexto.parametros.string("qr");

        if (Objeto.anyEmpty(qr)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        if (checkBlackList(contexto.idCobis())) {
            return RespuestaMB.estado("ERROR");
        }
        ApiRequestMB request = ApiMB.request("PaymentQr", "modo", "POST", "/v1/payments/qr", contexto);
        request.body("qr", qr);
        request.headers.put("DimoToken", "Bearer " + contexto.sesion().cache("access_token"));
        request.headers.put("x-subCanal", "MB-MODO");

        ApiResponseMB response = ApiMB.response(request, contexto.idCobis());

        if ("UNAUTHORIZED".equals(checkResponse(response))) {
            if (refreshAccessToken(contexto)) {
                return paymentQr(contexto);
            } else {
                return RespuestaMB.estado("ERROR_TOKEN");
            }
        } else if (!"OK".equals(checkResponse(response))) {
            return RespuestaMB.estado(checkResponse(response));
        }

        if (MBSalesforce.prendidoSalesforce(contexto.idCobis())) {
            Objeto parametros = new Objeto();
            String salesforce_pago_con_qr_modo = ConfigMB.string("salesforce_pago_con_qr_modo");
            parametros.set("IDCOBIS", contexto.idCobis());
            parametros.set("NOMBRE", contexto.persona().nombre());
            parametros.set("APELLIDO", contexto.persona().apellido());
            parametros.set("CANAL", ConfigMB.string("salesforce_nombre_canal"));
            new Futuro<>(() -> MBSalesforce.registrarEventoSalesforce(contexto, salesforce_pago_con_qr_modo, parametros));
        }


        return RespuestaMB.exito("qr_info", response);

    }

    public static RespuestaMB paymentCancellationDetails(ContextoMB contexto) {
        verifyAccessToken(contexto);

        String bankPaymentId = contexto.parametros.string("bank_payment_id");

        if (Objeto.anyEmpty(bankPaymentId)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        if (checkBlackList(contexto.idCobis())) {
            return RespuestaMB.estado("ERROR");
        }

        ApiRequestMB request = ApiMB.request("PaymentCancellationDetails", "modo", "GET", "/v1/payments/{bank_payment_id}/cancellation_details", contexto);
        request.path("bank_payment_id", bankPaymentId);
        request.headers.put("DimoToken", "Bearer " + contexto.sesion().cache("access_token"));
        request.headers.put("x-subCanal", "MB-MODO");

        ApiResponseMB response = ApiMB.response(request, contexto.idCobis());

        if ("UNAUTHORIZED".equals(checkResponse(response))) {
            if (refreshAccessToken(contexto)) {
                return paymentCancellationDetails(contexto);
            } else {
                return RespuestaMB.estado("ERROR_TOKEN");
            }

        } else if (!"OK".equals(checkResponse(response))) {
            return RespuestaMB.estado(checkResponse(response));
        }

        return RespuestaMB.exito("payment_info", response);
    }

    public static RespuestaMB paymentRefund(ContextoMB contexto) {
        verifyAccessToken(contexto);

        String bankPaymentId = contexto.parametros.string("bank_payment_id");

        if (Objeto.anyEmpty(bankPaymentId)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        if (checkBlackList(contexto.idCobis())) {
            return RespuestaMB.estado("ERROR");
        }

        ApiRequestMB request = ApiMB.request("PaymentRefund", "modo", "GET", "/v1/payments/{bank_payment_id}/refund", contexto);
        request.path("bank_payment_id", bankPaymentId);
        request.headers.put("DimoToken", "Bearer " + contexto.sesion().cache("access_token"));
        request.headers.put("x-subCanal", "MB-MODO");

        ApiResponseMB response = ApiMB.response(request, contexto.idCobis());

        if ("UNAUTHORIZED".equals(checkResponse(response))) {
            if (refreshAccessToken(contexto)) {
                return paymentRefund(contexto);
            } else {
                return RespuestaMB.estado("ERROR_TOKEN");
            }
        } else if (!"OK".equals(checkResponse(response))) {
            return RespuestaMB.estado(checkResponse(response));
        }

        return RespuestaMB.exito("payment_info", response);
    }

    public static RespuestaMB paymentAnnulment(ContextoMB contexto) {
        verifyAccessToken(contexto);

        String bankPaymentId = contexto.parametros.string("bank_payment_id");

        if (Objeto.anyEmpty(bankPaymentId)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        if (checkBlackList(contexto.idCobis())) {
            return RespuestaMB.estado("ERROR");
        }

        ApiRequestMB request = ApiMB.request("PaymentAnnulment", "modo", "GET", "/v1/payments/{bank_payment_id}/annulment", contexto);
        request.path("bank_payment_id", bankPaymentId);
        request.headers.put("DimoToken", "Bearer " + contexto.sesion().cache("access_token"));
        request.headers.put("x-subCanal", "MB-MODO");

        ApiResponseMB response = ApiMB.response(request, contexto.idCobis());

        if ("UNAUTHORIZED".equals(checkResponse(response))) {
            if (refreshAccessToken(contexto)) {
                return paymentAnnulment(contexto);
            } else {
                return RespuestaMB.estado("ERROR_TOKEN");
            }
        } else if (!"OK".equals(checkResponse(response))) {
            return RespuestaMB.estado(checkResponse(response));
        }

        return RespuestaMB.exito("payment_info", response);
    }

    @SuppressWarnings("unchecked")
    public static RespuestaMB getCryptoPem(ContextoMB contexto) {
        Map<String, Object> map;
        ObjectMapper mapper = new ObjectMapper();
        ApiRequestMB request = ApiMB.request("GetCryptoPem", "modo", "GET", "/v1/crypto/pem", contexto);
        request.headers.put("x-subCanal", "MB-MODO");

        if (checkBlackList(contexto.idCobis())) {
            return RespuestaMB.estado("ERROR");
        }

        String csmId = contexto.parametros.string("csmId");
        String checksum = contexto.parametros.string("checksum");

        boolean esMigrado = contexto.esMigrado(contexto);

        if (esMigrado && Objeto.anyEmpty(csmId, checksum))
            return RespuestaMB.parametrosIncorrectos();

        if (esMigrado) {
            RespuestaMB respuestaValidaTransaccion = contexto.validarTransaccion(contexto, true, "", JourneyTransmitEnum.MB_INICIO_SESION);
            if (respuestaValidaTransaccion.hayError())
                return respuestaValidaTransaccion;
        }

        ApiResponseMB response = ApiMB.response(request, contexto.idCobis());

        if (response.hayError()) {
            return RespuestaMB.estado(validarString(String.valueOf(response.string("mensajeAlUsuario")), "ERROR"), contexto.csmIdAuth);
        }

        String seed = "";
        try {
            map = mapper.readValue(response.toJson(), Map.class);
            if (map.containsKey("seed")) {
                seed = String.valueOf(map.get("seed"));
            }
        } catch (IOException ex) {
            seed = "";
        }

        return RespuestaMB.exito("seed", seed).set("csmIdAuth", contexto.csmIdAuth);
    }

    // ############################## UTILS
    // #######################################################

    public static class CanalesSegundoFactor {

        @JsonProperty(value = "tieneTCO")
        private boolean tieneTCO = Boolean.FALSE;

        @JsonProperty(value = "idSMS")
        private String tieneSMS = "";

        @JsonProperty(value = "idEMAIL")
        private String tieneEMAIL = "";

        public CanalesSegundoFactor() {
        }

        public boolean isTieneTCO() {
            return tieneTCO;
        }

        public void setTieneTCO(boolean tieneTCO) {
            this.tieneTCO = tieneTCO;
        }

        public String getTieneSMS() {
            return tieneSMS;
        }

        public void setTieneSMS(String tieneSMS) {
            this.tieneSMS = tieneSMS;
        }

        public String getTieneEMAIL() {
            return tieneEMAIL;
        }

        public void setTieneEMAIL(String tieneEMAIL) {
            this.tieneEMAIL = tieneEMAIL;
        }
    }

    @SuppressWarnings("unchecked")
    public static RespuestaMB canalesSegundoFactor(ContextoMB contexto) {
        Map<String, Object> map;
        ObjectMapper mapper = new ObjectMapper();
        try {
            contexto.sesion().limpiarSegundoFactor();
            CanalesSegundoFactor canales = new CanalesSegundoFactor();
            RespuestaMB respuesta = MBSeguridad.canalesOTP(contexto);
            map = mapper.readValue(respuesta.toJson(), Map.class);
            if (map.containsKey("canales")) {
                List<Object> lista = mapper.convertValue(map.get("canales"), mapper.getTypeFactory().constructCollectionType(List.class, Object.class));
                if (!lista.isEmpty()) {
                    for (Object objeto : lista) {
                        map = mapper.readValue(mapper.writeValueAsString(objeto), Map.class);
                        if (map.get("id").toString().startsWith("SMS_")) {
                            canales.setTieneSMS(map.get("id").toString());
                        }
                        if (map.get("id").toString().startsWith("EMAIL_")) {
                            canales.setTieneEMAIL(map.get("id").toString());
                        }
                    }
                }
            }

            return RespuestaMB.exito("canales", Objeto.fromJson(mapper.writeValueAsString(canales)));
        } catch (IOException ex) {
            return RespuestaMB.estado("ERROR_CANALES_SEGUNDO_FACTOR");
        }
    }

    public static String validarString(String cadena, String valorPorDefecto) {
        return cadena != null ? cadena : valorPorDefecto;
    }

    public static String validarPhoneNumber(String phoneNumber) {
        if (phoneNumber.startsWith("{")) {
            phoneNumber = phoneNumber.replace("{", "").replace("}", "");
            String[] parts = phoneNumber.split("=");
            phoneNumber = parts.length > 1 ? parts[1] : "";
        }

        phoneNumber = phoneNumber.replace("-", "").replace("(", "").replace(")", "");
        phoneNumber = phoneNumber.replace("/", "").replace("[", "").replace("]", "");
        phoneNumber = phoneNumber.replace("#", "").replace("%", "").replace("&", "");
        phoneNumber = phoneNumber.replace("@", "").replace("=", "").replace("$", "");
        phoneNumber = phoneNumber.replace(" ", "").replace("*", "").replace("?", "");
        phoneNumber = phoneNumber.replace("+549", "");
        phoneNumber = phoneNumber.replace("+54", "");
        phoneNumber = phoneNumber.replace("+", "");
        phoneNumber = phoneNumber.length() > 10 && phoneNumber.startsWith("549") ? phoneNumber.replace("549", "") : phoneNumber;
        phoneNumber = phoneNumber.length() > 10 && phoneNumber.startsWith("54") ? phoneNumber.replace("54", "") : phoneNumber;
        phoneNumber = phoneNumber.startsWith("0") ? phoneNumber.substring(1) : phoneNumber;
        phoneNumber = phoneNumber.length() > 10 ? phoneNumber.substring(phoneNumber.length() - 10, phoneNumber.length()) : phoneNumber;
        phoneNumber = phoneNumber.length() == 10 ? "+549" + phoneNumber : phoneNumber;
        phoneNumber = phoneNumber.length() < 10 ? "" : phoneNumber;
        return phoneNumber;
    }

    public static String checkResponse(ApiResponseMB response) {
        if (response.hayError() && "UNAUTHORIZED".equals(response.string("codigo"))) {
            return "UNAUTHORIZED";
        } else if (response.hayError()) {
            return validarString(String.valueOf(response.string("mensajeAlUsuario")), "ERROR");
        }
        return "OK";
    }

    public static void enviarMail(ContextoMB contexto, String forzarMail, BigDecimal monto, String cuenta, String cuentaDestino, String idProceso, String template) {
        if (forzarMail != null && !forzarMail.isEmpty()) {
            ApiRequestMB requestMail = ApiMB.request("NotificacionesPostCorreoElectronico", "notificaciones", "POST", "/v1/correoelectronico", contexto);
            requestMail.body("de", "aviso@mail-hipotecario.com.ar");
            requestMail.body("para", forzarMail);
            requestMail.body("plantilla", template);
            Objeto parametros = requestMail.body("parametros");
            parametros.set("Subject", "Transferencia BH");
            parametros.set("NOMBRE", contexto.persona().nombres());
            parametros.set("APELLIDO", contexto.persona().apellidos());
            parametros.set("CANAL", "HomeBanking");
            parametros.set("CUENTA_ORIGEN", cuenta);
            parametros.set("CUENTA_DESTINO", cuentaDestino);
            parametros.set("IMPORTE", Formateador.importe(monto));
            parametros.set("NRO_OPERACION", idProceso);
            ApiMB.response(requestMail, new Date().getTime());
        }
    }

    public static Boolean checkBlackList(String idCobis) {
        List<String> BLACK_LIST = Arrays.asList("5649952", "23262");
        return BLACK_LIST.contains(idCobis);
    }

    @SuppressWarnings("unused")
    private static RespuestaMB tryToExecuteAndRefreshTokenIfNeeded(ApiRequestMB request, ContextoMB contexto, Function<ContextoMB, RespuestaMB> miFuncion, String responseKey) {
        ApiResponseMB response = ApiMB.response(request, contexto.idCobis());
        if (response.hayError()) {
            if ("UNAUTHORIZED".equals(response.string("codigo"))) {
                if (refreshAccessToken(contexto)) {
                    return miFuncion.apply(contexto);
                }
                return RespuestaMB.estado("ERROR_TOKEN");
            }
            return RespuestaMB.estado(checkResponse(response));

        }
        return RespuestaMB.exito(responseKey, response);
    }

    /*
     * -----------------------------------------------------------------------------
     * ----------------------- V2 Payments
     * -----------------------------------------------------------------------------
     * -----------------------
     */

    public static RespuestaMB paymentV2Qr(ContextoMB contexto) {
        String qr = contexto.parametros.string("qr");

        if (Objeto.anyEmpty(qr)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        if (checkBlackList(contexto.idCobis())) {
            return RespuestaMB.estado("ERROR");
        }

        verifyAccessToken(contexto);

        ApiRequestMB request = ApiMB.request("PaymentQr", "modo", "POST", "/v2/payments/qr", contexto);
        request.body("qr", qr);
        request.headers.put("DimoToken", "Bearer " + contexto.sesion().cache("access_token"));

        request.headers.put("x-subCanal", "MB-MODO");

        ApiResponseMB response = ApiMB.response(request, contexto.idCobis());

        if (!"OK".equals(checkResponse(response))) {
            return RespuestaMB.estado(checkResponse(response));
        }

        if (MBSalesforce.prendidoSalesforce(contexto.idCobis())) {
            Objeto parametros = new Objeto();
            String salesforce_pago_con_qr_modo = ConfigMB.string("salesforce_pago_con_qr_modo");
            parametros.set("IDCOBIS", contexto.idCobis());
            parametros.set("NOMBRE", contexto.persona().nombre());
            parametros.set("APELLIDO", contexto.persona().apellido());
            parametros.set("CANAL", "Banca Móvil");
            new Futuro<>(() -> MBSalesforce.registrarEventoSalesforce(contexto, salesforce_pago_con_qr_modo, parametros));
        }

        return RespuestaMB.exito("qr_info", response);
    }

    public static RespuestaMB createV2Payment(ContextoMB contexto) {
        String qr = contexto.parametros.string("qr");
        Objeto paymentMethod = contexto.parametros.objeto("payment_method");
        BigDecimal totalAmt = contexto.parametros.bigDecimal("total_amount");
        Objeto fingerprint = contexto.parametros.objeto("fingerprint");
        Objeto installment = contexto.parametros.objeto("installment");

        if (Objeto.anyEmpty(qr, paymentMethod, fingerprint)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        if (checkBlackList(contexto.idCobis())) {
            return RespuestaMB.estado("ERROR");
        }

        if (!contexto.validaSegundoFactor("pago-qr")) {
            return RespuestaMB.estado("REQUIERE_SEGUNDO_FACTOR");
        }

        verifyAccessToken(contexto);

        ApiRequestMB request = ApiMB.request("CreatePayment", "modo", "POST", "/v2/payments", contexto);
        request.body("qr", qr);
        request.body("payment_method", paymentMethod);
        request.body("installment", installment);
        request.body("total_amount", totalAmt);
        request.body("fingerprint", fingerprint);
        request.headers.put("DimoToken", "Bearer " + contexto.sesion().cache("access_token"));
        request.headers.put("RefreshToken", contexto.sesion().cache("refresh_token"));
        request.headers.put("x-subCanal", "MB-MODO");
        request.headers.put("x-api-header", "1.1.0");

        ApiResponseMB response = ApiMB.response(request, contexto.idCobis());

        if ("UNAUTHORIZED".equals(checkResponse(response))) {
            if (refreshAccessToken(contexto)) {
                return createV2Payment(contexto);
            } else {
                return RespuestaMB.estado("ERROR_TOKEN");
            }
        } else if (!"OK".equals(checkResponse(response))) {
            return RespuestaMB.estado(checkResponse(response));
        }

        contexto.sesion().limpiarSegundoFactor();
        return RespuestaMB.exito("payment_info", response);
    }

    public static RespuestaMB getV2Payment(ContextoMB contexto) {

        String bankPaymentId = contexto.parametros.string("bank_payment_id");

        if (Objeto.anyEmpty(bankPaymentId)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        if (checkBlackList(contexto.idCobis())) {
            return RespuestaMB.estado("ERROR");
        }

        verifyAccessToken(contexto);

        ApiRequestMB request = ApiMB.request("GetPayment", "modo", "GET", "/v2/payments/{bank_payment_id}", contexto);
        request.path("bank_payment_id", bankPaymentId);
        request.headers.put("DimoToken", "Bearer " + contexto.sesion().cache("access_token"));
        request.headers.put("x-subCanal", "MB-MODO");

        ApiResponseMB response = ApiMB.response(request, contexto.idCobis());

        if ("UNAUTHORIZED".equals(checkResponse(response))) {
            if (refreshAccessToken(contexto)) {
                return getV2Payment(contexto);
            } else {
                return RespuestaMB.estado("ERROR_TOKEN");
            }
        } else if (!"OK".equals(checkResponse(response))) {
            return RespuestaMB.estado(checkResponse(response));
        }

        return RespuestaMB.exito("payment_info", response);
    }

    public static RespuestaMB getPaymentMethods(ContextoMB contexto) {
        String supportedPaymentTypes = contexto.parametros.string("supported_payment_types");
        String qrCode = contexto.parametros.string("qr");
        String fingerprint = contexto.parametros.string("fingerprint");

        if (Objeto.allEmpty(qrCode, supportedPaymentTypes)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        if (checkBlackList(contexto.idCobis())) {
            return RespuestaMB.estado("ERROR");
        }

        verifyAccessToken(contexto);

        ApiRequestMB request = ApiMB.request("PaymentMethods", "modo", "GET", "/v1/public/payment-methods", contexto);
        request.query("supported_payment_types", supportedPaymentTypes);
        request.query("qr", qrCode);
        request.query("fingerprint", fingerprint);
        request.headers.put("DimoToken", "Bearer " + contexto.sesion().cache("access_token"));
        request.headers.put("x-subCanal", "MB-MODO");

        ApiResponseMB response = ApiMB.response(request, contexto.idCobis());

        if ("UNAUTHORIZED".equals(checkResponse(response))) {
            return RespuestaMB.estado("ERROR_TOKEN");
        } else if (!"OK".equals(checkResponse(response))) {
            return RespuestaMB.estado(checkResponse(response));
        }

        if (response.toList().isEmpty()) {
            return RespuestaMB.estado("CLIENTE_SIN_METODOS_DE_PAGO");
        }

        return RespuestaMB.exito("payment_methods", response);
    }

    public static RespuestaMB createPaymentV3(ContextoMB contexto) {
        String qr = contexto.parametros.string("qr");
        Objeto paymentMethod = contexto.parametros.objeto("payment_method");
        BigDecimal totalAmt = contexto.parametros.bigDecimal("total_amount");
        Objeto fingerprint = contexto.parametros.objeto("fingerprint");
        Objeto installment = contexto.parametros.objeto("installment");

        if (contexto.idCobis() == null) {
            return RespuestaMB.estado("SIN_PSEUDO_SESION");
        }

        if (Objeto.anyEmpty(qr, paymentMethod, fingerprint)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        if (checkBlackList(contexto.idCobis())) {
            return RespuestaMB.estado("ERROR");
        }

        if (!contexto.validaSegundoFactor("pago-qr")) {
            return RespuestaMB.estado("REQUIERE_SEGUNDO_FACTOR");
        }

        String csmId = contexto.parametros.string("csmId");
        String checksum = contexto.parametros.string("checksum");

        boolean esMigrado = contexto.esMigrado(contexto);

        if (esMigrado && Objeto.anyEmpty(csmId, checksum))
            return RespuestaMB.parametrosIncorrectos();

        RespuestaMB respuestaValidaTransaccion = contexto.validarTransaccion(contexto, esMigrado, "pago-qr", JourneyTransmitEnum.MB_INICIO_SESION);
        if (respuestaValidaTransaccion.hayError())
            return respuestaValidaTransaccion;

        verifyAccessToken(contexto);

        ApiRequestMB request = ApiMB.request("API-PlaySistemico_Make_V3_Payments", "modo", "POST", "/v3/payments/", contexto);
        request.body("qr", qr);
        request.body("payment_method", paymentMethod);
        request.body("installment", installment);
        request.body("total_amount", totalAmt);
        request.body("fingerprint", fingerprint);
        request.headers.put("DimoToken", "Bearer " + contexto.sesion().cache("access_token"));
        request.headers.put("RefreshToken", contexto.sesion().cache("refresh_token"));
        request.headers.put("x-subCanal", "MB-MODO");
        request.headers.put("x-api-header", "1.1.0");

        ApiResponseMB response = ApiMB.response(request, contexto.idCobis());

        if ("UNAUTHORIZED".equals(checkResponse(response))) {
            if (refreshAccessToken(contexto)) {
                return createPaymentV3(contexto);
            } else {
                return RespuestaMB.estado("ERROR_TOKEN");
            }
        } else if (!"OK".equals(checkResponse(response))) {
            return RespuestaMB.estado(checkResponse(response));
        }

        contexto.sesion().limpiarSegundoFactor();
        return RespuestaMB.exito("create_payment_info", response);
    }

    public static RespuestaMB getPaymentV3(ContextoMB contexto) {
        String paymentId = contexto.parametros.string("payment_id");

        if (Objeto.anyEmpty(paymentId)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        if (checkBlackList(contexto.idCobis())) {
            return RespuestaMB.estado("ERROR");
        }

        verifyAccessToken(contexto);

        ApiRequestMB request = ApiMB.request("API-PlaySistemico_GetV3PaymentInformation", "modo", "GET", "/v3/payments/" + paymentId, contexto);
        request.headers.put("DimoToken", "Bearer " + contexto.sesion().cache("access_token"));
        request.headers.put("RefreshToken", contexto.sesion().cache("refresh_token"));
        request.headers.put("x-subCanal", "MB-MODO");
        request.headers.put("x-api-header", "1.1.0");

        ApiResponseMB response = ApiMB.response(request, contexto.idCobis());

        if ("UNAUTHORIZED".equals(checkResponse(response))) {
            if (refreshAccessToken(contexto)) {
                return getPaymentV3(contexto);
            } else {
                return RespuestaMB.estado("ERROR_TOKEN");
            }
        } else if (!"OK".equals(checkResponse(response))) {
            return RespuestaMB.estado(checkResponse(response));
        }
        return RespuestaMB.exito("get_payment_info", response);
    }

    public static RespuestaMB createPaymentIntention(ContextoMB contexto) {
        String qr = contexto.parametros.string("qr");
        Objeto paymentMethod = contexto.parametros.objeto("payment_method");
        if (Objeto.anyEmpty(qr, paymentMethod)) {
            return RespuestaMB.parametrosIncorrectos();
        }
        if (checkBlackList(contexto.idCobis())) {
            return RespuestaMB.estado("ERROR");
        }
        ApiRequestMB request = ApiMB.request("API-PlaySistemico_Create_Payment_Intention", "modo", "POST", "/v1/payments/intention", contexto);
        request.body("qr", qr);
        request.body("payment_method", paymentMethod);
        request.headers.put("DimoToken", "Bearer " + contexto.sesion().cache("access_token"));
        request.headers.put("RefreshToken", contexto.sesion().cache("refresh_token"));
        request.headers.put("x-subCanal", "MB-MODO");
        request.headers.put("x-api-header", "1.1.0");
        ApiResponseMB response = ApiMB.response(request, contexto.idCobis());

        if ("UNAUTHORIZED".equals(checkResponse(response))) {
            if (refreshAccessToken(contexto)) {
                return createPaymentV3(contexto);
            } else {
                return RespuestaMB.estado("ERROR_TOKEN");
            }
        } else if (!"OK".equals(checkResponse(response))) {
            return RespuestaMB.estado(checkResponse(response));
        }

        return RespuestaMB.exito("create_payment_intention_info", response);
    }

    public static RespuestaMB getPaymentIntention(ContextoMB contexto) {
        String paymentIntentionId = contexto.parametros.string("id");
        if (Objeto.anyEmpty(paymentIntentionId)) {
            return RespuestaMB.parametrosIncorrectos();
        }
        if (checkBlackList(contexto.idCobis())) {
            return RespuestaMB.estado("ERROR");
        }

        ApiRequestMB request = ApiMB.request("API-PlaySistemico_Get_Payment_Intention", "modo", "GET", "/v1/payments/intention/" + paymentIntentionId, contexto);
        request.headers.put("DimoToken", "Bearer " + contexto.sesion().cache("access_token"));
        request.headers.put("RefreshToken", contexto.sesion().cache("refresh_token"));
        request.headers.put("x-subCanal", "MB-MODO");
        request.headers.put("x-api-header", "1.1.0");

        ApiResponseMB response = ApiMB.response(request, contexto.idCobis());

        if ("UNAUTHORIZED".equals(checkResponse(response))) {
            if (refreshAccessToken(contexto)) {
                return getPaymentV3(contexto);
            } else {
                return RespuestaMB.estado("ERROR_TOKEN");
            }
        } else if (!"OK".equals(checkResponse(response))) {
            return RespuestaMB.estado(checkResponse(response));
        }
        return RespuestaMB.exito("get_payment_intention_info", response);
    }

    public RespuestaMB isRiskForChangeInformation(ContextoMB contexto) {
        String tipo = ConfigMB.string("cambio_information_no_permitido");
        DisableService disableService = new DisableService();
        LocalDateTime nowTime = LocalDateTime.now();
        nowTime = nowTime.plusHours(-1 * disableService.calculateHourDelay(nowTime));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String inicio = nowTime.format(formatter);
        List<Objeto> registros = ContextoMB.obtenerContador(contexto.idCobis(), tipo, inicio);
        if (Objects.isNull(registros)) {
            return RespuestaMB.estado("ERROR");
        }
        Boolean permission = disableService.getEnabledToOperator(registros);
        return RespuestaMB.exito("enableb_operator", permission);
    }

    public static RespuestaMB estadoUsuario(ContextoMB contexto) {
        if (!(contexto.sesion() != null && contexto.sesion().idCobisReal() != null)) {
            return RespuestaMB.estado("ERROR_SIN_SESION");
        }

        RespuestaMB respuesta = new RespuestaMB();

        String idCobis = contexto.idCobis();
        SqlConsentimientoService sqlConsentimiento = new SqlConsentimientoService();
        SqlResponseMB response = sqlConsentimiento.consultarUsuario(contexto, idCobis);
        int existeUsuario = (int) response.registros.get(0).get("cant_Usuario");

        if (response.hayError) {
            return respuesta.setEstado("ERROR");
        } else {
            if (existeUsuario > 0) {
                respuesta.set("usuario_nuevo", false);
                respuesta.set("sesion_caducada", false);
            } else {
                respuesta.set("usuario_nuevo", true);
                respuesta.set("sesion_caducada", false);
            }

            return respuesta;
        }

    }

    public static RespuestaMB getUri(ContextoMB contexto) {
        //Controla si está logueado
        if (!(contexto.sesion() != null && contexto.sesion().idCobisReal() != null)) {
            return RespuestaMB.estado("ERROR_SIN_SESION");
        }
        String cuit = contexto.persona().cuit();
        String bcra_id = "00044"; // como billetera

        try {
            String state = UUID.randomUUID().toString().replaceAll("\\s", "").trim();
            String codeVerifier = generarCodeVerifier().replaceAll("\\s", "").trim();
            String codeChallenge = generarCodeChallenge(codeVerifier).replaceAll("\\s", "").trim();

            String url = buildUrl(bcra_id, codeChallenge, cuit, state);

            SqlConsentimientoService sqlConsentimiento = new SqlConsentimientoService();
            Boolean response = sqlConsentimiento.insertDatos(contexto, state, codeVerifier, codeChallenge, cuit);
            if (!response) {
                return RespuestaMB.estado("ERROR_BD_CONSENTIMIENTO");
            }
            return RespuestaMB.exito("url", url);
        } catch (Exception e) {
            e.printStackTrace();

            RespuestaMB errorRespuesta = new RespuestaMB();
            RespuestaMB.error();
            return errorRespuesta;
        }
    }


    public static String generarCodeVerifier() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] codeVerifierBytes = new byte[64];
        secureRandom.nextBytes(codeVerifierBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(codeVerifierBytes);
    }

    public static String generarCodeChallenge(String codeVerifier) {
        byte[] codeVerifierBytes = codeVerifier.getBytes();
        byte[] codeChallengeBytes = DigestUtils.sha256(codeVerifierBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(codeChallengeBytes);
    }

    public static String buildUrl(String clientId, String codeChallenge, String userIdentifier, String state) {
        String baseUrl = ConfigMB.string("mb_api_url_consentimiento");
        String queryParams = String.format("client_id=%s&code_challenge=%s&code_challenge_method=S256&user_identifier=%s&state=%s",
                clientId, codeChallenge, userIdentifier, state);
        return baseUrl + "?" + queryParams;
    }


    public static String getPantallaPcpLanding(ContextoMB contexto) {
        SqlConsentimientoService sqlpcp = new SqlConsentimientoService();
        try {

            return sqlpcp.getPcpLanding(contexto);
        } catch (Exception error) {
            error.printStackTrace();
            return "Error al obtener el texto de la pantalla PCP";
        }
    }

    public static RespuestaMB revocarToken(ContextoMB contexto) {
        if (!(contexto.sesion() != null && contexto.sesion().idCobisReal() != null)) {
            return RespuestaMB.estado("ERROR_SIN_SESION");
        }

        String bcraId = contexto.parametros.string("idBcra");
        String idCobis = contexto.idCobis();

        if (bcraId.isEmpty()) {
            RespuestaMB.parametrosIncorrectos();
        }

        ApiRequestMB request = ApiMB.request("RevocarToken", "canales", "POST", "/revocar-pcp", contexto);
        request.body("bcraId", bcraId);
        request.body("idCobis", idCobis);

        ApiResponseMB apiResponse = ApiMB.response(request);

        if (apiResponse.hayError() || !(apiResponse.get("estado").equals("0"))) {
            return RespuestaMB.error();
        }

        return new RespuestaMB();
    }

    public static RespuestaMB getTokenPago(ContextoMB contexto) {

        String idCobis = contexto.idCobis();

        if (idCobis.isEmpty()) {
            RespuestaMB.parametrosIncorrectos();
        }

        String accessToken = "";

        if (contexto.sesion().cache("access_token") != null) {
            accessToken = contexto.sesion().cache("access_token");
        } else {
            ApiRequestMB request = ApiMB.request("ConsultarTokensModo", "modo", "GET", "/v1/modo/{id_cobis}", contexto);
            request.permitirSinLogin = true;
            request.path("id_cobis", idCobis);
            request.headers.put("x-subCanal", "MB-MODO");

            ApiResponseMB token = ApiMB.response(request, idCobis);
            if (token.hayError()) {
                return RespuestaMB.error();
            } else if (token.json.isEmpty()) {
                return RespuestaMB.estado("SIN_USER_ID");
            }

            accessToken = token.get("access_token").toString();
        }

        String userId = decodificar(accessToken);
        if (userId.isEmpty()) {
            return RespuestaMB.estado("SIN_USER_ID");
        }

        ApiRequestMB request = ApiMB.request("TokenPago", "modo", "GET", "/v1/public/tokens/pagos", contexto);
        request.permitirSinLogin = true;
        request.query("userId", userId);

        ApiResponseMB apiResponse = ApiMB.response(request);

        if (apiResponse.hayError()) {
            return RespuestaMB.error();
        }

        RespuestaMB respuesta = new RespuestaMB();

        respuesta.set("estado", "0");
        respuesta.set("userId", userId);
        respuesta.set("token", apiResponse.get("access_token"));
        respuesta.set("refresh_token", apiResponse.get("refresh_token"));
        respuesta.set("securePaymentSecret", apiResponse.get("securePaymentSecret"));

        return respuesta;
    }


    public static RespuestaMB obtenerNuevosToken(ContextoMB contexto) {
        String accessToken = "";

        if (contexto.sesion().cache("access_token") != null) {
            accessToken = contexto.sesion().cache("access_token");
        } else {
            ApiRequestMB request = ApiMB.request("ConsultarTokensModo", "modo", "GET", "/v1/modo/{id_cobis}", contexto);
            request.permitirSinLogin = true;
            request.path("id_cobis", contexto.idCobis());
            request.headers.put("x-subCanal", "MB-MODO");

            ApiResponseMB token = ApiMB.response(request, contexto.idCobis());
            if (token.hayError()) {
                return RespuestaMB.error();
            } else if (token.json.isEmpty()) {
                return RespuestaMB.estado("SIN_USER_ID");
            }

            accessToken = token.get("access_token").toString();
        }

        String userId = decodificar(accessToken);
        if (userId.isEmpty()) {
            return RespuestaMB.estado("SIN_USER_ID");
        }

        ApiRequestMB request = ApiMB.request("TokenPago", "modo", "GET", "/v1/public/tokens/pagos", contexto);
        request.permitirSinLogin = true;
        request.query("userId", userId);

        ApiResponseMB apiResponse = ApiMB.response(request);

        if (apiResponse.hayError()) {
            return RespuestaMB.error();
        }

        RespuestaMB respuesta = new RespuestaMB();

        respuesta.set("estado", "0");
        respuesta.set("userId", userId);
        respuesta.set("token", apiResponse.get("access_token"));
        respuesta.set("refresh_token", apiResponse.get("refresh_token"));
        respuesta.set("securePaymentSecret", apiResponse.get("securePaymentSecret"));

        return respuesta;
    }

    public static Boolean insertarEnBase(RespuestaMB respuesta, ContextoMB contexto) {
        SqlMobile sqlToken = new SqlMobile();
        Boolean response = sqlToken.persistirTokenSdk(respuesta, contexto);
        return response;
    }

    public static Boolean actualizarEnBase(ContextoMB contexto, RespuestaMB respuesta) {
        SqlMobile sqlToken = new SqlMobile();
        Boolean response = sqlToken.actualizarToken(contexto, respuesta);
        return response;
    }

    public static SqlResponseMB consultarToken(ContextoMB contexto) {
        SqlMobile sqlToken = new SqlMobile();
        SqlResponseMB response = sqlToken.consultarToken(contexto);
        return response;
    }

    public static String decodificar(String accessToken) {

        try {

            String[] parts = accessToken.split("\\.");

            String payload = new String(Base64.getDecoder().decode(parts[1]));

            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(payload, JsonObject.class);

            JsonElement subElement = jsonObject.get("sub");
            String userIdString = subElement.getAsString();
            return userIdString;
        } catch (Exception e) {
            System.out.println("Error al decodificar el accessToken JWT: " + e.getMessage());
            return null;
        }

    }

    public static String tokenLocal(ContextoMB contexto) {

        String refreshToken = obtenerRefreshToken(contexto);
        String accessToken = "";

        if (refreshToken != "error" || refreshToken != "SIN_USER_ID") {
            accessToken = actualizarAccessToken(contexto, refreshToken);
        }

        if (accessToken == "error") {
            return null;
        }
        return accessToken;
    }

    public static RespuestaMB obtenerCertificados(ContextoMB contexto) {
        String fechaInicio = contexto.parametros.string("fechaInicio");
        String fechaFin = contexto.parametros.string("fechaFin");
        String idCobis = contexto.idCobis();
        String offset = contexto.parametros.string("offset"); //corrimiento de la paginacion
        String limit = contexto.parametros.string("limit"); // cantidad de items por página

        if (StringUtils.isEmpty(idCobis)) {
            return RespuestaMB.estado("SIN_PSEUDO_SESION");
        }

        //Controlar que la fecha no supere 1 año
        LocalDate fecha1 = LocalDate.parse(fechaInicio);
        LocalDate fecha2 = LocalDate.parse(fechaFin);

        long diferenciaEnDias = ChronoUnit.DAYS.between(fecha2, fecha1);

        if (Math.abs(diferenciaEnDias) >= 365) {
            return RespuestaMB.estado("FECHA_SUPERA_AÑO");
        }

        String cuit = contexto.persona().cuit();

        ApiRequestMB request = ApiMB.request("CertificadosImpuestos", "modo", "GET", "/v1/public/listado", contexto);
        request.query("cuit", cuit);
        request.query("initDate", fechaInicio);
        request.query("endDate", fechaFin);
        request.query("limit", limit);
        request.query("offset", offset);
        request.headers.put("x-subCanal", "MB-MODO");

        ApiResponseMB apiResponse = ApiMB.response(request);

        if (apiResponse.hayError()) {
            return RespuestaMB.error();
        }

        RespuestaMB respuesta = new RespuestaMB();
        respuesta.set("datos", apiResponse.get("data"));
        respuesta.set("paginacion", apiResponse.get("pagination"));

        return respuesta;
    }

    public static RespuestaMB obtenerComprobante(ContextoMB contexto) {
        String idCobis = contexto.idCobis();
        String uuid = contexto.parametros.string("uuid");
        if (StringUtils.isEmpty(idCobis)) {
            return RespuestaMB.estado("SIN_PSEUDO_SESION");
        }

        ApiRequestMB request = ApiMB.request("ConsultarTokensModo", "modo", "GET", "/v1/public/certificado/{uuid}", contexto);
        request.path("uuid", uuid);
        request.headers.put("x-subCanal", "MB-MODO");

        ApiResponseMB apiResponse = ApiMB.response(request);

        if (apiResponse.hayError()) {
            return RespuestaMB.error();
        }

        RespuestaMB respuesta = new RespuestaMB();
        respuesta.set("url", apiResponse.get("url"));

        return respuesta;
    }

}

