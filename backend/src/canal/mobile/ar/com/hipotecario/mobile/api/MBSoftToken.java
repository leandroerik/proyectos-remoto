package ar.com.hipotecario.mobile.api;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import ar.com.hipotecario.backend.servicio.api.transmit.JourneyTransmitEnum;
import ar.com.hipotecario.backend.util.Transmit;
import ar.com.hipotecario.canal.libreriariesgofraudes.infrastructure.gateway.model.bm.mb.AltaDispositivoMBBMBankProcess;
import ar.com.hipotecario.canal.libreriariesgofraudes.infrastructure.gateway.model.bm.mb.AltaSoftTokenMBBMBankProcess;
import ar.com.hipotecario.mobile.servicio.*;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.mobile.ConfigMB;
import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.RespuestaMB;
import ar.com.hipotecario.mobile.api.dto.modo.TokenIsvaDto;
import ar.com.hipotecario.mobile.conector.ApiMB;
import ar.com.hipotecario.mobile.conector.ApiRequestMB;
import ar.com.hipotecario.mobile.conector.ApiResponseMB;
import ar.com.hipotecario.mobile.conector.SqlResponseMB;
import ar.com.hipotecario.mobile.lib.Encriptador;
import ar.com.hipotecario.mobile.lib.Objeto;
import ar.com.hipotecario.mobile.negocio.EstadoClaveLink;
import ar.com.hipotecario.mobile.negocio.EstadoUsuario;
import ar.com.hipotecario.mobile.negocio.TarjetaDebito;

public class MBSoftToken {
    private static final String ERROR_TECNICO = "ERROR";
    private static final String ERROR_CLAVE_RED_LINK = "ERROR_CLAVE_INCORRECTA";
    private static final String EXITO_CLAVE_RED_LINK = "VALIDADO";
    private static final String FORMATO_FECHA_ISO_8106 = "yyyy-MM-dd HH:mm:ss";

    /*
     * Para poder validar en link
     */
    private final String FII_BH = "0044";
    private static final String ERROR_VALIDAR_DATOS = "ERROR_VALIDAR_DATOS";
    private static final String MENSAJE_DESARROLLADOR = "mensajeAlDesarrollador";
    private static final String ESTADO_HABILITADA = "HABILITADA";
    private static final String NO_EXISTE_TARJETA_DEBITO = "NO_EXISTE_TARJETA_DEBITO";
    private static final String ERROR_OBTENER_TARJETAS = "ERROR_OBTENER_TARJETAS";
    private static final String SIN_CUIL = "SIN_CUIL";
    private static final String ERROR_INSERTAR_INTENTO = "ERROR_INSERTAR_INTENTO";
    private static final String MENSAJE_INSERTAR_INTENTO_EMAIL = "OTP incorrecto";
    private static final String MENSAJE_INSERTAR_INTENTO_CVV = "Los datos de la tarjeta son incorrectos";
    private static final String TARJETA_NO_HABILITADA = "TARJETA_NO_HABILITADA";
    private static final String REQUIERE_SEGUNDO_FACTOR = "REQUIERE_SEGUNDO_FACTOR";

    private static final String ERROR_HORA_DESINCRONIZADA = "ERROR_HORA_DESINCRONIZADA";
    private SqlSoftTokenService sqlSoftTokenService;

    private final String REGEX_FECHA_VENCIMIENTO = "\\b(0[1-9]|1[0-2])\\/?([0-9]{4})\\b";
    private final String REGEX_ANIO = "^[0-9]{4}$";
    private final String REGEX_MES = "^[0-9]{2}$";

    public MBSoftToken() {
        this.sqlSoftTokenService = new SqlSoftTokenService();
    }

    /**
     * Realiza el alta de soft token para un cliente.
     *
     * @param contexto
     * @return semilla de soft token.
     */
    public RespuestaMB altaSoftToken(ContextoMB contexto) {
        ApiResponseMB apiResponse;
        RespuestaMB respuesta;
        ApiRequestMB apiRequest;

        String clave = contexto.parametros.string("pin");
        String idTarjetaDebito = contexto.parametros.string("idTarjetaDebito", null);
        String idCobis = contexto.idCobis();
        String idDispositivo = contexto.parametros.string("idDispositivo", null);
        /*
         * Parámetros para poder hacer el alta online por cvv
         */
        Boolean onlineCvv = contexto.parametros.bool("onlineCvv", false);
        String numeroTarjeta = contexto.parametros.string("numeroTarjeta", null);
        String fechaVencimiento = contexto.parametros.string("fechaVencimiento", null);
        String cvv = contexto.parametros.string("cvv", null);
        /*
         * Parámetros para poder hacer el alta de onboarding
         */
        Boolean onboarding = contexto.parametros.bool("onboarding", false);
        String idCobisOnboarding = contexto.parametros.string("idCobisOnboarding", null);
        String idDispositivoOnboarding = contexto.parametros.string("idDispositivoOnboarding", null);

        String claveLinkHasheada = "";
        TarjetaDebito tarjetaDebito = null;
        boolean altaCvv = false;
        boolean altaEmail = false;

        if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_modo_transaccional_alta_soft_token",
                "prendido_modo_transaccional_alta_soft_token_cobis")) {
            try {
                String sessionToken = contexto.parametros.string(Transmit.getSessionToken(), null);
                if (Objeto.empty(sessionToken))
                    return RespuestaMB.parametrosIncorrectos();

                AltaSoftTokenMBBMBankProcess altaSoftTokenMBBMBankProcess = new AltaSoftTokenMBBMBankProcess(contexto.idCobis(), sessionToken);

                respuesta = TransmitMB.recomendacionTransmit(contexto, altaSoftTokenMBBMBankProcess, "alta-soft-token");
                if (respuesta.hayError())
                    return respuesta;
            } catch (Exception e) {
            }
        }


        // si no es onboarding sigo con toda la lógica ya implementada
        if (!onboarding) {
            if (idCobis == null)
                return RespuestaMB.estado("SIN_PSEUDO_SESION");

            // Verificar que exista usuario en IDG
            apiResponse = RestSeguridad.usuario(contexto, true);

            if (apiResponse.hayError())
                return RespuestaMB.estado("ERROR_USUARIO_ISVA");

            /*
             * Separo la lógica dejando en el else la funcionalidad original
             */
            if (onlineCvv) {
                if (!MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_soft_token_cvv"))
                    return RespuestaMB.error();

                if (idDispositivo == null || numeroTarjeta == null || fechaVencimiento == null || cvv == null)
                    return RespuestaMB.parametrosIncorrectos();

                // Valido con la api que los datos sean correctos
                String cuil = contexto.persona().cuit();

                if (StringUtils.isBlank(cuil)) {
                    respuesta = intentoFallido(contexto, MENSAJE_INSERTAR_INTENTO_CVV);
                    if (respuesta != null)
                        return respuesta;
                    return RespuestaMB.estado(SIN_CUIL);
                }

                // armo request para pegarle a api link
                apiRequest = ApiMB.request("LinkInternalGetTarjetas", "link", "GET", "/v1/servicios/{cuil}/tarjetas",
                        contexto);
                apiRequest.path("cuil", cuil);
                apiResponse = ApiMB.response(apiRequest);
                if (apiResponse.hayError()) {
                    respuesta = intentoFallido(contexto, MENSAJE_INSERTAR_INTENTO_CVV);
                    if (respuesta != null)
                        return respuesta;
                    return RespuestaMB.estado(ERROR_OBTENER_TARJETAS);
                }

                List<Objeto> tarjetas = apiResponse.objetos().stream()
                        .filter(item -> item.string("nroTarjeta").trim().equals(numeroTarjeta)
                                && Arrays.asList(ConfigMB.string("mb_tipo_tarjeta_alta_soft_token").split("_"))
                                .contains(item.string("tipoTarjeta").trim()))
                        .collect(Collectors.toList());

                // chequeo que alguna tarjeta coincida con los filtros
                if (tarjetas.stream().collect(Collectors.toList()).size() != 0) {
                    // chequeo que la tarjeta esté habilitada
                    if (tarjetas.stream().filter(item -> item.string("estado").trim().equals(ESTADO_HABILITADA))
                            .collect(Collectors.toList()).size() == 0)
                        return RespuestaMB.estado(TARJETA_NO_HABILITADA);

                    // valido en api link los datos ingresados de la tarjeta
                    apiRequest = ApiMB.request("LinkInternalValidacion", "link", "POST",
                            "/v1/servicios/tarjetas/validar", contexto);
                    apiRequest.body("fiid", FII_BH);
                    apiRequest.body("nroTarjeta", numeroTarjeta);
                    apiRequest.body("fechaVencimiento", formatearFechaVencimiento(fechaVencimiento));
                    apiRequest.body("codigoValidacion", cvv);

                    apiResponse = ApiMB.response(apiRequest);

                    if (apiResponse.hayError()) {
                        respuesta = intentoFallido(contexto, MENSAJE_INSERTAR_INTENTO_CVV);
                        if (respuesta != null)
                            return respuesta;
                        return apiResponse.codigo == 400
                                ? errorDatosIncorrectos(apiResponse.string(MENSAJE_DESARROLLADOR))
                                : RespuestaMB.estado(ERROR_VALIDAR_DATOS);
                    }

                    // busco la tarjeta en el contexto para obtener el idTarjetaDebito
                    tarjetaDebito = contexto.tarjetaDebito(numeroTarjeta);
                    if (tarjetaDebito == null) {
                        respuesta = intentoFallido(contexto, MENSAJE_INSERTAR_INTENTO_CVV);
                        if (respuesta != null)
                            return respuesta;
                        return RespuestaMB.estado(NO_EXISTE_TARJETA_DEBITO);
                    }
                    idTarjetaDebito = tarjetaDebito.id();
                    altaCvv = true;
                } else {
                    respuesta = intentoFallido(contexto, MENSAJE_INSERTAR_INTENTO_CVV);
                    if (respuesta != null)
                        return respuesta;
                    return RespuestaMB.estado(NO_EXISTE_TARJETA_DEBITO);
                }
            } else {
                if (Objeto.anyEmpty(idDispositivo))
                    return RespuestaMB.parametrosIncorrectos();

                String csmId = contexto.parametros.string("csmId", "");
                String checksum = contexto.parametros.string("checksum", "");

                boolean esMigrado = contexto.esMigrado(contexto);

                if (esMigrado && Objeto.anyEmpty(csmId, checksum))
                    return RespuestaMB.parametrosIncorrectos();

                RespuestaMB respuestaValidaTransaccion = contexto.validarTransaccion(contexto, esMigrado, "alta-soft-token", JourneyTransmitEnum.MB_INICIO_SESION);
                if (respuestaValidaTransaccion.hayError()) {
                    respuesta = intentoFallido(contexto, MENSAJE_INSERTAR_INTENTO_EMAIL);
                    if (respuesta != null)
                        return respuesta;
                    return respuestaValidaTransaccion;
                }

                tarjetaDebito = contexto.tarjetaDebitoPorDefecto();
                if (Objeto.empty(tarjetaDebito)) {
                    respuesta = intentoFallido(contexto, MENSAJE_INSERTAR_INTENTO_EMAIL);
                    if (respuesta != null)
                        return respuesta;
                    return RespuestaMB.estado(NO_EXISTE_TARJETA_DEBITO);
                }

                if (!contexto.validaSegundoFactor("alta-soft-token")) {
                    respuesta = intentoFallido(contexto, MENSAJE_INSERTAR_INTENTO_EMAIL);
                    if (respuesta != null)
                        return respuesta;
                    return RespuestaMB.estado(REQUIERE_SEGUNDO_FACTOR);
                }
                idTarjetaDebito = tarjetaDebito.id();
                altaEmail = true;

				/*
				if (idDispositivo == null || clave == null || idTarjetaDebito == null)
					return RespuestaMB.parametrosIncorrectos();

				// claveLinkHasheada = Encriptador.sha256(clave);
				tarjetaDebito = contexto.tarjetaDebito(idTarjetaDebito);

				if (tarjetaDebito == null)
					return RespuestaMB.estado(NO_EXISTE_TARJETA_DEBITO);

				// logica clave redlink validacion
				respuesta = this.validacionClaveRedLink(contexto, tarjetaDebito, clave, claveLinkHasheada);
				if (respuesta != null) {
					return respuesta;
				}

				// logica clave en uso validacion
				respuesta = this.validacionClaveEnUso(contexto, claveLinkHasheada, idTarjetaDebito);
				if (respuesta != null) {
					return respuesta;
				}*/
            }
        } else {
            if (Objeto.empty(idCobisOnboarding, idDispositivoOnboarding))
                return RespuestaMB.parametrosIncorrectos();
            // hago esto para no modificar toda la lógica ya usada, porque el idcobis
            // sacando onboarding se obtiene del contexto
            idCobis = idCobisOnboarding;
            // uso la misma variable interna para no modificar la lógica siguiente
            idDispositivo = idDispositivoOnboarding;
            // seteo en null lo que corresponda
            idTarjetaDebito = null;
        }

        // logica de usuario bloqueado validacion
        respuesta = this.validacionUsuarioBloqueado(contexto);
        if (respuesta != null) {
            return respuesta;
        }

        String fechaActual = this.thisInstant();
        int idRegistroAltaST = sqlSoftTokenService.insertarRegistroAltaSoftToken(idCobis, claveLinkHasheada,
                fechaActual, EstadoUsuario.POR_CONFIRMAR.name(), idDispositivo, fechaActual, idTarjetaDebito,
                contexto.ip(), altaEmail, altaCvv);

        if (idRegistroAltaST == SqlSoftTokenService.ERROR_INSERCION) {
            return RespuestaMB.estado("INSERTAR_REGISTRO_ALTA");
        }

        // obtener token isva
        TokenIsvaDto tokenIsva = this.obtenerTokenIsva(contexto, false, null, idCobis);
        if (tokenIsva == null) {
            return RespuestaMB.estado("ERROR_TOKEN_ISVA");
        }

        // obtenemos la semilla
        apiResponse = new RestSoftTokenService().obtainSeedInformation(contexto, idCobis, tokenIsva.getAccessToken());
        if (apiResponse.hayError()) {
            return RespuestaMB.estado("OBTENER_SEED");
        }

        // anular softtoken previos
        respuesta = this.anularSoftTokenPrevios(contexto);
        if (respuesta != null) {
            return respuesta;
        }

        // confirmamos el alta softtoken por confirmar ha habilitado
        respuesta = this.actualizarAltaSoftToken(contexto, tokenIsva, idRegistroAltaST, idDispositivo);
        if (respuesta != null) {
            return respuesta;
        }

        if (StringUtils.isNotBlank(claveLinkHasheada)) {
            if (ConfigMB.esProduccion())
                SqlClaveRedLinkUso.insertarClave(idCobis, tarjetaDebito.numero(), claveLinkHasheada, "MB",
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern(FORMATO_FECHA_ISO_8106)));
            else {
                if (StringUtils.isNotBlank(ConfigMB.string("mb_prendido_link_uso_cobis", ""))
                        && Arrays.asList(ConfigMB.string("mb_prendido_link_uso_cobis", "").split("_")).stream()
                        .filter(c -> c.equals(contexto.idCobis())).count() == 1)
                    SqlClaveRedLinkUso.insertarClave(idCobis, tarjetaDebito.numero(), claveLinkHasheada, "MB",
                            LocalDateTime.now().format(DateTimeFormatter.ofPattern(FORMATO_FECHA_ISO_8106)));
            }
        }

        // Valida si un usuario tiene bloqueado el uso de soft token para desbloquearlo
        respuesta = this.validarBloqueoPorUsoSoftToken(idCobis, true);

        if (respuesta != null) {
            return respuesta;
        }

        this.enviarEmailAltaSoftToken(contexto);

        // construccion de la respuesta
        respuesta = new RespuestaMB();
        respuesta.set("csmIdAuth", contexto.csmIdAuth);
        respuesta.set("algoritmo", apiResponse.string("algorithm"));
        respuesta.set("digitos", apiResponse.string("digits"));
        respuesta.set("periodo", apiResponse.string("period"));
        respuesta.set("claveSecreta", apiResponse.string("secretKey"));
        respuesta.set("urlClaveSecreta", apiResponse.string("secretKeyUrl"));
        respuesta.set("username", apiResponse.string("username"));

        return respuesta;
    }

    /**
     * Consulta si un cliente tiene soft token activo por dispositivo asociado. La
     * consulta puede hacerse por fuera sin tener una sesión activa.
     *
     * @param contexto
     * @return estado del soft token por dispositivo.
     */
    public RespuestaMB consultarSoftTokenActivoPorDispositivo(ContextoMB contexto) {
        RespuestaMB respuesta = new RespuestaMB();

        // Parámetros
        String idCobis = contexto.idCobis();
        String idDispositivo = contexto.parametros.string("idDispositivo", null);
        boolean esDirecto = contexto.parametros.bool("esDirecto", false); // permitir sin login

        if (!esDirecto && idCobis == null) {
            return RespuestaMB.estado("SIN_PSEUDO_SESION");
        }

        if (idDispositivo == null) {
            return RespuestaMB.parametrosIncorrectos();
        }

        SqlResponseMB sqlResponse = this.sqlSoftTokenService.consultarAltaSoftToken(idCobis, idDispositivo,
                EstadoUsuario.ACTIVO.name());

        if (sqlResponse.hayError) {
            return RespuestaMB.estado("CONSULTAR_ALTA_SOFT_TOKEN");
        }

        if (!sqlResponse.registros.isEmpty()) {
            respuesta.set("estadoSoftToken", EstadoUsuario.ACTIVO_CON_DISPOSITIVO_INDICADO.name());
            respuesta.set("mensaje", "El cliente tiene soft token activo con el dispositivo especificado");

            return respuesta;
        }

        sqlResponse = this.sqlSoftTokenService.consultarAltaSoftToken(idCobis, EstadoUsuario.ACTIVO.name());

        if (sqlResponse.hayError) {
            return RespuestaMB.estado("CONSULTAR_ALTA_SOFT_TOKEN");
        }

        if (!sqlResponse.registros.isEmpty()) {
            respuesta.set("estadoSoftToken", EstadoUsuario.ACTIVO_CON_OTRO_DISPOSITIVO.name());
            respuesta.set("mensaje", "El cliente tiene soft token activo con otro dispositivo móvil");

            return respuesta;
        }

        if (!esDirecto) {
            sqlResponse = this.sqlSoftTokenService.consultarForzadoAltaSoftToken(idCobis);

            if (sqlResponse.hayError) {
                return RespuestaMB.estado("CONSULTAR_FORZADO_ALTA");
            }

            boolean forzarAlta = this.tieneForzarAltaSoftToken(sqlResponse);

            respuesta.set("forzarAlta", forzarAlta);
        }

        respuesta.set("estadoSoftToken", EstadoUsuario.INACTIVO.name());
        respuesta.set("mensaje", "El cliente no tiene soft token activo");

        return respuesta;
    }

    /**
     * Consulta si un cliente tiene soft token activo.
     *
     * @param contexto
     * @return estado del soft token por cliente.
     */
    public RespuestaMB consultarSoftTokenActivoPorCliente(ContextoMB contexto) {
        RespuestaMB respuesta = new RespuestaMB();
        String idCobis = contexto.idCobis();

        if (idCobis == null) {
            return RespuestaMB.estado("SIN_PSEUDO_SESION");
        }

        SqlResponseMB sqlResponse = this.sqlSoftTokenService.consultarAltaSoftToken(idCobis,
                EstadoUsuario.ACTIVO.name());

        if (sqlResponse.hayError) {
            return RespuestaMB.estado("CONSULTAR_FORZADO_ALTA");
        }

        if (!sqlResponse.registros.isEmpty()) {
            respuesta.set("estadoSoftToken", EstadoUsuario.ACTIVO.name());
            respuesta.set("mensaje", "El cliente tiene soft token activo");

            return respuesta;
        }

        sqlResponse = this.sqlSoftTokenService.consultarForzadoAltaSoftToken(idCobis);

        if (sqlResponse.hayError) {
            return RespuestaMB.estado("CONSULTAR_FORZADO_ALTA");
        }

        boolean forzarAlta = this.tieneForzarAltaSoftToken(sqlResponse);

        respuesta.set("estadoSoftToken", EstadoUsuario.INACTIVO.name());
        respuesta.set("forzarAlta", forzarAlta);
        respuesta.set("mensaje", "El cliente no tiene soft token activo");

        return respuesta;
    }

    public RespuestaMB generarIDCliente(ContextoMB contexto) {
        String idCobis = contexto.idCobis();

        if (idCobis == null) {
            return RespuestaMB.estado("SIN_PSEUDO_SESION");
        }

        RespuestaMB respuesta = new RespuestaMB();
        respuesta.set("ID", Encriptador.sha256(idCobis));

        return respuesta;
    }

    /**
     * Comprueba que el código de 6 dígitos (soft token) sea correcto
     *
     * @param contexto
     * @return Respuesta
     */
    public RespuestaMB validarSoftToken(ContextoMB contexto) {
        if (ConfigMB.string("mb_apicanales_softtoken", "true").equals("true")) {
            ApiRequestMB request = ApiMB.request("CanalesSoftToken", "canales", "POST", "/mb/api/soft-token-validacion", contexto);
            contexto.parametros.set("idCobis", contexto.idCobis());
            request.body = contexto.parametros;
            if (ConfigMB.string("mb_apicanales_softtoken_cache", "false").equals("true")) {
                request.body.set("cache", contexto.sesion().cache);
            }

            ApiResponseMB response = ApiMB.response(request);
            if (!response.hayError() && response.bool("softTokenValido", false)) {
                contexto.sesion().setValidaSegundoFactorSoftToken(true);
            }

            RespuestaMB respuesta = new RespuestaMB();
            respuesta.set("estado", response.string("estado"));
            respuesta.set("softTokenValido", response.bool("softTokenValido", false));
            return respuesta;
        }


        RespuestaMB respuesta;
        ApiResponseMB apiResponse;
        RestSoftTokenService restSoftTokenService = new RestSoftTokenService();

        // Parámetros
        String idCobis = contexto.idCobis();
        String softToken = contexto.parametros.string("softToken", null);
        String idDispositivo = contexto.parametros.string("idDispositivo", null);

        if ("999999".equals(softToken) && !contexto.esProduccion()) {
            respuesta = new RespuestaMB();
            SqlSoftTokenService.insertLogSoftToken(contexto, idDispositivo, true);
            contexto.sesion().setValidaSegundoFactorSoftToken(true);
            respuesta.set("softTokenValido", true);
            return respuesta;
        }

        if (idCobis == null) {
            return RespuestaMB.estado("SIN_PSEUDO_SESION");
        }

        if (softToken == null || idDispositivo == null) {
            return RespuestaMB.parametrosIncorrectos();
        }

        // Forzar usuario invalido isva y apagar softoken
        if (ConfigMB.bool("deshabilitar_isva", false)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        // Verifica soft token activo
        respuesta = this.consultarSoftTokenActivoPorDispositivo(contexto);
        if (respuesta.hayError()) {
            return respuesta;
        }

        if (respuesta.string("estadoSoftToken").equals(EstadoUsuario.ACTIVO_CON_OTRO_DISPOSITIVO.name())
                || respuesta.string("estadoSoftToken").equals(EstadoUsuario.INACTIVO.name())) {
            return RespuestaMB.estado("ESTADO_SOFT_TOKEN");
        }

        respuesta = this.validarUsoSoftTokenBloqueado(contexto);

        if (respuesta.bool("bloqueado")) {
            SqlSoftTokenService.insertLogSoftToken(contexto, idDispositivo, false);
            return RespuestaMB.estado("USO_SOFT_TOKEN_BLOQUEADO");
        }

        respuesta = conexionISVASoftToken(contexto);
        if (respuesta.hayError()) {
            return respuesta;
        }

        apiResponse = restSoftTokenService.validarSoftToken(contexto, respuesta.string("cookie"),
                respuesta.string("statedId"), softToken);
        if (apiResponse.hayError() && apiResponse.codigo != 403) {
//			return Respuesta.estado("VALIDAR_SOFT_TOKEN");
            // modificamos provisoriamente para que despliegue la contingencia en prod
            return RespuestaMB.parametrosIncorrectos();
        }

        if (apiResponse.codigo == 403) {
            respuesta = this.validarHoraDesincronizada(apiResponse.string("mensajeAlUsuario"));
            if (respuesta != null)
                return respuesta;

            respuesta = this.validarBloqueoPorUsoSoftToken(contexto, idCobis);

            if (respuesta != null) {
                return respuesta;
            }
            SqlSoftTokenService.insertLogSoftToken(contexto, idDispositivo, false);
            return RespuestaMB.estado("SOFT_TOKEN_INVALIDO");
        }

        boolean resultado = sqlSoftTokenService.limpiarIntentosFallidos(idCobis, true, false);
        if (!resultado) {
            return RespuestaMB.estado("ERROR_LIMPIEZA_FALLIDOS");
        }

        respuesta = new RespuestaMB();

        // Guarda en sesion que el soft token fue validado
        SqlSoftTokenService.insertLogSoftToken(contexto, idDispositivo, true);
        contexto.sesion().setValidaSegundoFactorSoftToken(true);
        respuesta.set("softTokenValido", true);

        return respuesta;
    }

    public RespuestaMB conexionISVASoftToken(ContextoMB contexto) {
        RespuestaMB respuesta = new RespuestaMB();
        String accessTokenISVA;
        RestSoftTokenService restSoftTokenService = new RestSoftTokenService();

        SqlResponseMB sqlResponse = this.sqlSoftTokenService.obtenerDatosTokenISVA(contexto.idCobis(),
                EstadoUsuario.ACTIVO.name());
        if (sqlResponse.hayError) {
            return RespuestaMB.estado("ERROR_DATOS_SOFT_TOKEN_ISVA");
        }

        respuesta = this.verificarAccessTokenIsva(contexto, sqlResponse);
        if (respuesta.hayError()) {
            return respuesta;
        }

        accessTokenISVA = respuesta.string("accessToken");
        ApiResponseMB apiResponse = restSoftTokenService.iniciarValidacionSoftToken(contexto, accessTokenISVA);
        if (apiResponse.hayError() && apiResponse.codigo == 401) {
            return RespuestaMB.estado("AUTORIZACION_INVALIDA");
        }

        if (apiResponse.hayError() && apiResponse.codigo != 401) {
            return RespuestaMB.estado("INICIO_VALIDACION_SOFT_TOKEN");
        }

        String cookie = apiResponse.headers.get("set-cookie");
        String statedId = apiResponse.string("stateId");
        return respuesta.set("cookie", cookie).set("statedId", statedId);
    }

    private RespuestaMB verificarAccessTokenIsva(ContextoMB contexto, SqlResponseMB sqlResponse) {
        RespuestaMB respuesta = new RespuestaMB();
        LocalDateTime fechaHoraSistema = LocalDateTime.now().plusSeconds(-1L);
        Timestamp expiracionTokenISVATimestamp = (Timestamp) sqlResponse.registros.get(0).get("expires_in");
        LocalDateTime expiracionTokenISVA = expiracionTokenISVATimestamp.toLocalDateTime();

        if (fechaHoraSistema.isAfter(expiracionTokenISVA)) {
            TokenIsvaDto tokenIsvaDto = this.obtenerTokenIsva(contexto, true,
                    sqlResponse.registros.get(0).string("refresh_token"), contexto.idCobis());

            if (tokenIsvaDto == null) {
                return RespuestaMB.estado("OBTENER_TOKEN_ISVA");
            }

            respuesta = this.actualizarAltaSoftToken(contexto, tokenIsvaDto,
                    Integer.parseInt(sqlResponse.registros.get(0).string("id")),
                    sqlResponse.registros.get(0).string("id_dispositivo"));

            if (respuesta != null) {
                return respuesta;
            }

            respuesta = new RespuestaMB();
            respuesta.set("accessToken", tokenIsvaDto.getAccessToken());

            return respuesta;
        }

        respuesta.set("accessToken", sqlResponse.registros.get(0).string("access_token"));

        return respuesta;
    }

    private RespuestaMB actualizarAltaSoftToken(ContextoMB contexto, TokenIsvaDto tokenIsva, int idBaseDatos,
                                                String idDispositivo) {
        String expiresIn = this.newFutureDate(tokenIsva.getExpiresIn());

        boolean resActualizarRergistroAltaST = sqlSoftTokenService.actualizarRegistrosAltaSoftToken(idBaseDatos,
                EstadoUsuario.ACTIVO.name(), tokenIsva.getAccessToken(), tokenIsva.getRefreshToken(), expiresIn,
                tokenIsva.getScope(), tokenIsva.getTokenType(), this.thisInstant(), idDispositivo);

        if (!resActualizarRergistroAltaST) {
            return RespuestaMB.estado("ACTUALIZAR_REGISTROS_ALTA");
        }

        boolean resultadoOk = sqlSoftTokenService.limpiaIntentosFallidos(contexto.idCobis(),
                SqlSoftTokenService.REGISTRO_INACTIVO, SqlSoftTokenService.REGISTRO_ACTIVO);

        if (!resultadoOk) {
            return RespuestaMB.estado("LIMPIA_INTENTOS_FALLIDOS");
        }

        return null;
    }

    /**
     * Actualiza el estado alta de soft token ACTIVO a INACTIVO.
     *
     * @param contexto
     * @return Respuesta
     */
    private RespuestaMB anularSoftTokenPrevios(ContextoMB contexto) {
        boolean resActualizarRergistroAltaST = sqlSoftTokenService.actualizarRegistrosAltaSoftToken(contexto.idCobis(),
                EstadoUsuario.ACTIVO.name(), EstadoUsuario.INACTIVO.name(), this.thisInstant());

        if (!resActualizarRergistroAltaST) {
            return RespuestaMB.estado("ANULAR_ALTA_SOFT_TOKEN");
        }

        return null;
    }

    private TokenIsvaDto obtenerTokenIsva(ContextoMB contexto, boolean refrescarTokenISVA, String refreshToken,
                                          String idCobis) {
        ObjectMapper mapper = new ObjectMapper();
        RestSoftTokenService restSoftTokenService = new RestSoftTokenService();
        ApiResponseMB apiResponse;

        if (refrescarTokenISVA) {
            apiResponse = restSoftTokenService.refrescarTokenISVA(contexto, refreshToken, idCobis);
        } else {
            apiResponse = restSoftTokenService.obtainTokenIsva(contexto, idCobis);
        }

        if (apiResponse.hayError()) {
            return null;
        }
        TokenIsvaDto tokenIsvaDto = null;
        try {
            tokenIsvaDto = mapper.readValue(apiResponse.json, TokenIsvaDto.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return tokenIsvaDto;
    }

    /**
     * Comprueba que la clave y la tarjeta de débito seleccionada no tengan alta de
     * soft token activo.
     *
     * @param contexto
     * @param claveLinkHasheada
     * @param idTarjetaDebito
     * @return Respuesta
     */
    private RespuestaMB validacionClaveEnUso(ContextoMB contexto, String claveLinkHasheada, String idTarjetaDebito) {
        SqlResponseMB sqlResponse = sqlSoftTokenService.consultarUltimaAltaSoftTokenUsuario(contexto.idCobis(),
                EstadoUsuario.ACTIVO.name(), claveLinkHasheada, idTarjetaDebito);

        if (sqlResponse.hayError) {
            return RespuestaMB.estado("CONSULTAR_ULTIMA_ALTA_SOFT_TOKEN");
        }

        if (!sqlResponse.registros.isEmpty()) {
            return RespuestaMB.estado("ERROR_CLAVE_EN_USO");
        }

        return null;
    }

    /**
     * Comprueba que la tarjeta de débito seleccionada no tengan alta por cvv de
     * soft token activo.
     *
     * @param contexto}
     * @param idTarjetaDebito
     * @return Respuesta
     */
    protected RespuestaMB validacionCvvEnUso(ContextoMB contexto, String idTarjetaDebito) {
        SqlResponseMB sqlResponse = sqlSoftTokenService.consultarUltimaAltaSoftTokenUsuarioCvv(contexto.idCobis(),
                EstadoUsuario.ACTIVO.name(), idTarjetaDebito);
        if (sqlResponse.hayError)
            return RespuestaMB.estado("ULTIMA_ALTA_SOFT_TOKEN");

        if (!sqlResponse.registros.isEmpty())
            return RespuestaMB.estado("ERROR_TARJETA_EN_USO");

        return null;
    }

    /**
     * Inserta intento fallido por uso de soft token y determina si debe registrar
     * bloqueo.
     *
     * @param contexto
     * @param idCobis
     * @return Respuesta
     */
    private RespuestaMB validarBloqueoPorUsoSoftToken(ContextoMB contexto, String idCobis) {
        boolean resultado = this.sqlSoftTokenService.insertarIntentoUsoSoftToken(idCobis, "INCORRECTO", "MOBILE", true,
                this.thisInstant());

        if (!resultado) {
            return RespuestaMB.estado("INSERTAR_INTENTO_USO");
        }

        return this.procesarBloqueoUsoSoftToken(contexto);
    }

    /**
     * Valida si existe un bloqueo por uso de soft token para un cliente y, en tal
     * caso, lo desbloquea.
     *
     * @param idCobis
     * @param estado
     * @return Respuesta
     */
    private RespuestaMB validarBloqueoPorUsoSoftToken(String idCobis, boolean estado) {
        SqlResponseMB sqlResponse = this.sqlSoftTokenService.consultarBloqueoUsoSoftTokenPorUsuario(idCobis, estado);

        if (sqlResponse.hayError) {
            return RespuestaMB.estado("CONSULTAR_BLOQUEO_USO");
        }

        if (!sqlResponse.registros.isEmpty()) {
            boolean res = this.sqlSoftTokenService.desbloquearUsuarioUsoSoftToken(
                    Integer.parseInt(sqlResponse.registros.get(0).string("id")), this.thisInstant());

            if (!res) {
                return RespuestaMB.estado("DESBLOQUEAR_USUARIO_SOFT_TOKEN");
            }
        }

        return null;
    }

    private RespuestaMB procesoBloqueoAltaSoftToken(ContextoMB contexto) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(FORMATO_FECHA_ISO_8106);
        Integer maximoIntentos = ConfigMB.integer("maximo_errores_alta_softtoken", 3);
//    Long minutosAnterior = Config.longer("softtoken_tiempo_bloqueo_minutos", 40L);
        LocalDateTime fechaHoraActual = LocalDateTime.now();
//    LocalDateTime limitTime = fechaHoraActual.plusMinutes(-1 * minutosAnterior);
        LocalDateTime inicioConteo = LocalDateTime.of(LocalDate.now(ZoneId.of("America/Argentina/Buenos_Aires")),
                LocalTime.MIDNIGHT);

        String fechaInicioConteo = inicioConteo.format(formatter);
        SqlResponseMB intentosFallidos = sqlSoftTokenService.consultaIntentosAlta(contexto.idCobis(),
                SqlSoftTokenService.REGISTRO_ACTIVO, fechaInicioConteo);

        if (intentosFallidos.hayError) {
            return RespuestaMB.estado("CONSULTAR_INTENTO_FALLOS");
        }

        if (intentosFallidos.registros.size() < maximoIntentos) {
            return null;// no bloqueo
        }

        String fechaAltaBloqueo = fechaHoraActual.format(formatter);
        fechaHoraActual = fechaHoraActual.plusHours(24L);
        String fechaFinBloqueo = fechaHoraActual.format(formatter);

        boolean todoOk = sqlSoftTokenService.insertarBloqueo(contexto.idCobis(), fechaAltaBloqueo, fechaFinBloqueo);
        if (!todoOk) {
            return RespuestaMB.estado("INSERTAR_BLOQUEO");
        }

        boolean seLimpio = sqlSoftTokenService.limpiaIntentosFallidos(contexto.idCobis(),
                SqlSoftTokenService.REGISTRO_INACTIVO, SqlSoftTokenService.REGISTRO_ACTIVO);

        if (!seLimpio) {
            return RespuestaMB.estado("LIMPIA_INTENTOS_FALLIDOS");
        }

        return RespuestaMB.estado("USUARIO_BLOQUEADO");
    }

    /**
     * Determina si debe registrar el bloqueo de uso de soft token por reiteradas
     * fallas.
     *
     * @param contexto
     * @return Respuesta
     */
    private RespuestaMB procesarBloqueoUsoSoftToken(ContextoMB contexto) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(FORMATO_FECHA_ISO_8106);
        Integer maximoIntentos = ConfigMB.integer("soft_token_max_intentos_error_uso", 3);
        Long minutos = ConfigMB.longer("soft_token_intervalo_tiempo_bloqueo_minutos", 40L);
        LocalDateTime fechaHoraActual = LocalDateTime.now();
        LocalDateTime tiempoLimite = fechaHoraActual.plusMinutes(-1 * minutos);

        String fechaInicioConteo = tiempoLimite.format(formatter);
        SqlResponseMB intentosFallidos = sqlSoftTokenService.consultaIntentosUsoSoftToken(contexto.idCobis(), true,
                fechaInicioConteo);

        if (intentosFallidos.hayError) {
            return RespuestaMB.estado("ERROR_INTENTOS");
        }

        if (intentosFallidos.registros.size() < maximoIntentos) {
            return null;
        }

        String fechaHoraActualBloqueo = fechaHoraActual.format(formatter);
        SqlResponseMB softTokenActivo = sqlSoftTokenService.consultarAltaSoftToken(contexto.idCobis(),
                EstadoUsuario.ACTIVO.name());
        String idDispositivo = null;

        if (softTokenActivo.hayError) {
            return RespuestaMB.estado("ERROR_SOFT_ACT");
        }

        if (!softTokenActivo.registros.isEmpty()) {
            idDispositivo = softTokenActivo.registros.get(0).string("id_dispositivo");
        }

        boolean resultado = sqlSoftTokenService.insertarBloqueo(contexto.idCobis(), true, fechaHoraActualBloqueo,
                idDispositivo, "USO_DESINCRONIZADO");

        if (!resultado) {
            return RespuestaMB.estado("ERROR_INS_BLOCK");
        }

        resultado = sqlSoftTokenService.limpiarIntentosFallidos(contexto.idCobis(), true, false);

        if (!resultado) {
            return RespuestaMB.estado("ERROR_LIMPIEZA");
        }

        return RespuestaMB.estado("USO_SOFT_TOKEN_DESINCRONIZADO");
    }

    public RespuestaMB consultaUsuarioBloqueado(ContextoMB contexto) {

        if (contexto.idCobis() == null) {
            return RespuestaMB.estado("SIN_PSEUDO_SESION");
        }
        SqlResponseMB sqlResponse = sqlSoftTokenService.consultarBloqueoUsuario(contexto.idCobis(), this.thisInstant());

        if (sqlResponse.hayError) {
            return RespuestaMB.estado("CONSULTAR_BLOQUEO_USUARIO");
        }

        boolean usuarioBloqueado = this.esUsuarioBloqueado(sqlResponse);
        RespuestaMB respuesta = new RespuestaMB();
        if (usuarioBloqueado) {
            respuesta.set("usuarioBloqueado", true);
            respuesta.set("fechaFin", sqlResponse.registros.get(0).string("fecha_fin"));
            return respuesta;
        }
        respuesta.set("usuarioBloqueado", false);
        return respuesta;
    }

    /**
     * Comprueba si el usuario tiene el alta de soft token bloqueado.
     *
     * @param contexto
     * @return Respuesta
     */
    public RespuestaMB validacionUsuarioBloqueado(ContextoMB contexto) {
        SqlResponseMB sqlResponse = sqlSoftTokenService.consultarBloqueoUsuario(contexto.idCobis(), this.thisInstant());

        if (sqlResponse.hayError) {
            return RespuestaMB.estado("CONSULTAR_BLOQUEO_USUARIO");
        }

        RespuestaMB respuesta = new RespuestaMB();
        boolean usuarioBloqueado = this.esUsuarioBloqueado(sqlResponse);

        if (usuarioBloqueado) {
            respuesta.setEstado("USUARIO_BLOQUEADO");

            respuesta.set("fechaFin", sqlResponse.registros.get(0).string("fecha_fin"));
            return respuesta;
        }
        return null;
    }

    /**
     * Determina si un cliente/usuario tiene bloqueado el soft token para su uso.
     *
     * @param contexto
     * @return Respuesta
     */
    public RespuestaMB validarUsoSoftTokenBloqueado(ContextoMB contexto) {
        // Parámetros
        String idCobis = contexto.idCobis();
        String idDispositivo = contexto.parametros.string("idDispositivo", null);
        boolean esDirecto = contexto.parametros.bool("esDirecto", false); // permitir sin login

        if (!esDirecto && idCobis == null) {
            return RespuestaMB.estado("SIN_PSEUDO_SESION");
        }

        if (esDirecto && idDispositivo == null) {
            return RespuestaMB.parametrosIncorrectos();
        }

        SqlResponseMB sqlResponse = null;

        if (!esDirecto) {
            sqlResponse = this.sqlSoftTokenService.consultarBloqueoUsoSoftTokenPorUsuario(idCobis, true);
        } else {
            sqlResponse = this.sqlSoftTokenService.consultarBloqueoUsoSoftTokenPorDispositivo(idDispositivo, true);
        }

        if (sqlResponse.hayError) {
            return RespuestaMB.estado("CONSULTAR_BLOQUEO_USO");
        }

        RespuestaMB respuesta = new RespuestaMB();
        boolean estaBloqueado = !sqlResponse.registros.isEmpty();

        if (estaBloqueado) {
            respuesta.set("bloqueado", true);

            return respuesta;
        }

        respuesta.set("bloqueado", false);

        return respuesta;
    }

    /**
     * Comprueba que la clave red link es la correcta para la tarjeta de débito
     * seleccionada.
     *
     * @param contexto
     * @param tarjetaDebito
     * @param clave
     * @param claveLinkHasheada
     * @return Respuesta
     */
    private RespuestaMB validacionClaveRedLink(ContextoMB contexto, TarjetaDebito tarjetaDebito, String clave,
                                               String claveLinkHasheada) {

        String result = this.verificaClaveRedLink(contexto, tarjetaDebito.numero(), clave);
        if (result.equals(ERROR_TECNICO)) {
            return RespuestaMB.estado("VERIFICAR_CLAVE_RED_LINK");
        }
        if (result.equals(ERROR_CLAVE_RED_LINK)) {
            boolean resInsertarIntento = sqlSoftTokenService.insertarIntentoAltaSoftToken(contexto.idCobis(),
                    claveLinkHasheada, this.thisInstant(), EstadoClaveLink.INCORRECTO.name(),
                    "La clave ingresada es incorrecta", SqlSoftTokenService.REGISTRO_ACTIVO);
            if (!resInsertarIntento) {
                return RespuestaMB.estado("INSERTAR_INTENTO_ALTA");
            }
            RespuestaMB respuesta = this.procesoBloqueoAltaSoftToken(contexto);
            if (respuesta != null) {
                return respuesta;
            }
            return RespuestaMB.estado(ERROR_CLAVE_RED_LINK);
        }

        boolean resInsertarIntento = sqlSoftTokenService.insertarIntentoAltaSoftToken(contexto.idCobis(),
                claveLinkHasheada, this.thisInstant(), EstadoClaveLink.CORRECTO.name(),
                "La clave ingresada es correcta", SqlSoftTokenService.REGISTRO_ACTIVO);
        if (!resInsertarIntento) {
            return RespuestaMB.estado("INSERTAR_INTENTO_ALTA");
        }
        return null;
    }

    private String verificaClaveRedLink(ContextoMB contexto, String cardId, String clave) {
        RestSeguridad restSeguridad = new RestSeguridad();

        SqlResponseMB sqlResponse = SqlClaveRedLinkUso.consultarUltimoUso(contexto.idCobis(), cardId,
                Encriptador.sha256(clave));
        if (sqlResponse.hayError || (!sqlResponse.hayError && !sqlResponse.registros.isEmpty()))
            return ERROR_CLAVE_RED_LINK;

        if (!ConfigMB.esProduccion() && clave.equals("999999"))
            return EXITO_CLAVE_RED_LINK;

        if (!ConfigMB.esProduccion() && clave.equals("111111"))
            return ERROR_CLAVE_RED_LINK;

        ApiResponseMB apiResponse = restSeguridad.verificarClaveRedLink(cardId, clave, contexto);
        if (apiResponse.hayError())
            return "VERIFICAR_CLAVE_RED_LINK";

        if (!"true".equals(apiResponse.string("adherido")))
            return ERROR_CLAVE_RED_LINK;

        return EXITO_CLAVE_RED_LINK;
    }

    private boolean esUsuarioBloqueado(SqlResponseMB sqlResponse) {
        return !sqlResponse.registros.isEmpty();
    }

    private String thisInstant() {
        LocalDateTime nowTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(FORMATO_FECHA_ISO_8106);
        return nowTime.format(formatter);
    }

    private String newFutureDate(String aditionalSeconds) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(FORMATO_FECHA_ISO_8106);
        return LocalDateTime.now().plusSeconds(Long.parseLong(aditionalSeconds)).format(formatter);
    }

    private void enviarEmailAltaSoftToken(ContextoMB contexto) {
        try {
            Objeto parametros = new Objeto();
            parametros.set("Subject", "Alta de soft token");
            parametros.set("NOMBRE_APELLIDO", contexto.persona().nombre() + " " + contexto.persona().apellido());
            parametros.set("NOMBRE", contexto.persona().nombre());
            parametros.set("APELLIDO", contexto.persona().apellido());
            Date hoy = new Date();
            parametros.set("FECHA", new SimpleDateFormat("dd/MM/yyyy").format(hoy));
            parametros.set("HORA", new SimpleDateFormat("hh:mm a").format(hoy));

			if (MBSalesforce.prendidoSalesforceAmbienteBajoConFF(contexto)) {
				String salesforce_alta_soft_token =  ConfigMB.string("salesforce_alta_soft_token");
                parametros.set("CANAL", ConfigMB.string("salesforce_nombre_canal"));
				parametros.set("ISMOBILE", true);
				new Futuro<>(() -> MBSalesforce.registrarEventoSalesforce(contexto, salesforce_alta_soft_token, parametros));
			}
			else {
	            RestNotificaciones.envioMail(contexto, ConfigMB.string("doppler_soft_token_alta"), parametros);
			}

        } catch (Exception e) {
            // TODO: manejar excepción para envío fallido
        }
    }


    /**
     * Determina si un cliente tiene forzado de alta de Soft Token.
     *
     * @param contexto
     * @return Respuesta
     */
    public RespuestaMB forzarAltaSoftToken(ContextoMB contexto) {
        String idCobis = contexto.idCobis();

        if (idCobis == null) {
            return RespuestaMB.estado("SIN_PSEUDO_SESION");
        }

        SqlResponseMB sqlResponse = this.sqlSoftTokenService.consultarForzadoAltaSoftToken(idCobis);

        if (sqlResponse.hayError) {
            return RespuestaMB.estado("CONSULTAR_FORZADO");
        }

        RespuestaMB respuesta = new RespuestaMB();

        if (!sqlResponse.registros.isEmpty() && sqlResponse.registros.get(0).string("forzar_alta").equals("true")) {
            respuesta.set("forzarAlta", true);

            return respuesta;
        }

        respuesta.set("forzarAlta", false);

        return respuesta;
    }

    /**
     * Determina si tiene forzado de alta de soft token.
     *
     * @param sqlResponse Consulta SQL de forzado.
     * @return true/false
     */
    private boolean tieneForzarAltaSoftToken(SqlResponseMB sqlResponse) {
        return !sqlResponse.registros.isEmpty() && sqlResponse.registros.get(0).string("forzar_alta").equals("true");
    }

    /**
     * Determino el error que devolvió la api
     */
    private RespuestaMB errorDatosIncorrectos(String mensaje) {
        String estado = ERROR_VALIDAR_DATOS;
        try {
            JsonObject errorJson = new Gson().fromJson(mensaje, JsonObject.class);
            if (errorJson != null && errorJson.has("codigo"))
                estado = errorJson.get("codigo").getAsString();
        } catch (JsonParseException e) {
        }
        return RespuestaMB.estado(estado);

    }

    /**
     * Inserto intento Fallido y/o bloqueo
     */
    public RespuestaMB intentoFallido(ContextoMB contexto, String mensaje) {
        if (!sqlSoftTokenService.insertarIntentoAltaSoftToken(contexto.idCobis(), "", this.thisInstant(),
                EstadoClaveLink.INCORRECTO.name(), mensaje, SqlSoftTokenService.REGISTRO_ACTIVO))
            return RespuestaMB.estado(ERROR_INSERTAR_INTENTO);
        return this.procesoBloqueoAltaSoftToken(contexto);
    }

    /**
     * Formatea la fecha de vencimiento si viene en formato MM/AA(sólamente ese
     * formato) a MM/AAAA
     */
    private String formatearFechaVencimiento(String fechaVencimiento) {
        if (!Pattern.matches(REGEX_FECHA_VENCIMIENTO, fechaVencimiento)) {
            if (fechaVencimiento.contains("/")) {
                String[] valores = fechaVencimiento.split("/");
                if (Pattern.matches(REGEX_MES, valores[0]) && !Pattern.matches(REGEX_ANIO, valores[1])
                        && Pattern.matches(REGEX_MES, valores[1])) {
                    return fechaVencimiento.replace("/", "/20");
                }
            }
        }
        return fechaVencimiento;
    }

    /**
     * Determina si tiene alta de soft token en las últimas 24 horas.
     *
     * @param contexto
     * @return true/false
     */
    public boolean activoSoftTokenEnElUltimoDia(ContextoMB contexto) {
        return !sqlSoftTokenService.consultarAltaSoftTokenUltimoDia(contexto.idCobis()).registros.isEmpty();
    }

    /**
     * Determina si tiene alta de soft token en las últimas 48 horas hábiles.
     *
     * @param contexto
     * @return true/false
     */
    public boolean activoSoftTokenEnLasUltimas48HorasHabiles(ContextoMB contexto) {
        String dias = "2";
        if (LocalDateTime.now().getDayOfWeek() == DayOfWeek.MONDAY
                || LocalDateTime.now().getDayOfWeek() == DayOfWeek.TUESDAY)
            dias = "4";
        else if (LocalDateTime.now().getDayOfWeek() == DayOfWeek.SUNDAY)
            dias = "3";
        return !sqlSoftTokenService.consultarAltaSoftTokenUltimas48HorasHabiles(contexto.idCobis(), dias).registros
                .isEmpty();
    }

    /**
     * Valida si el error de validación de Soft Token es por hora Desincronizada
     *
     * @param mensaje
     * @return Respuesta
     */
    private RespuestaMB validarHoraDesincronizada(String mensaje) {
        if (StringUtils.isNotBlank(mensaje) && (mensaje.contains("La clave ingresada es inválida, intente de nuevo") || mensaje.contains("La clave ingresada es invalida, intente de nuevo")))
            return RespuestaMB.estado(ERROR_HORA_DESINCRONIZADA);
        return null;
    }

}
