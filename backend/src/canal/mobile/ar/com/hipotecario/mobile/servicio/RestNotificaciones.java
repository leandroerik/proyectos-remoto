package ar.com.hipotecario.mobile.servicio;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.mobile.ConfigMB;
import org.apache.commons.lang3.StringUtils;

import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.api.MBAplicacion;
import ar.com.hipotecario.mobile.api.MBSalesforce;
import ar.com.hipotecario.mobile.conector.ApiMB;
import ar.com.hipotecario.mobile.conector.ApiRequestMB;
import ar.com.hipotecario.mobile.conector.ApiResponseMB;
import ar.com.hipotecario.mobile.conector.SqlMB;
import ar.com.hipotecario.mobile.conector.SqlRequestMB;
import ar.com.hipotecario.mobile.conector.SqlResponseMB;
import ar.com.hipotecario.mobile.lib.Objeto;
import ar.com.hipotecario.mobile.negocio.Persona;

public class RestNotificaciones {

    public static SqlResponseMB consultaConfiguracionAlertas(ContextoMB contexto) {
        SqlRequestMB sqlRequest = SqlMB.request("SelectConfiguracionAlertas", "homebanking");
        sqlRequest.sql = "SELECT * FROM [homebanking].[dbo].[alertas_por_mail] WHERE [a_id] = ?";
        sqlRequest.parametros.add(contexto.idCobis());
        SqlResponseMB sqlResponse = SqlMB.response(sqlRequest);
        return sqlResponse;
    }

    public static boolean consultaConfiguracionAlertasEspecifica(ContextoMB contexto, String alerta) {
        SqlResponseMB sqlResponse = consultaConfiguracionAlertas(contexto);
        if (sqlResponse.hayError) {
            return false;
        }
        String alertas = "";
        for (Objeto registro : sqlResponse.registros) {
            alertas = registro.string("a_alertas");
        }

        return alertas.contains(alerta);

    }

    public static ApiResponseMB envioMail(ContextoMB contexto, String plantilla, Objeto parametros) {
        if (!MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_notificaciones_mail", "prendido_notificaciones_mail_cobis"))
            return null;

        String emailDestino = RestPersona.direccionEmail(contexto, contexto.persona().cuit());

        if (StringUtils.isNotBlank(emailDestino)) {
            ApiRequestMB requestMail = ApiMB.request("LoginCorreoElectronico", "notificaciones", "POST", "/v1/correoelectronico", contexto);
            requestMail.body("de", "aviso@mail-hipotecario.com.ar");
            requestMail.body("para", emailDestino);
            requestMail.body("plantilla", plantilla);
            requestMail.body("parametros", parametros);
            requestMail.permitirSinLogin = true;
            return ApiMB.response(requestMail, new Date().getTime());
        }

        return null;
    }

    public static ApiResponseMB cacheNotificacionesPriorizadas(ContextoMB contexto) {
        ApiRequestMB request = ApiMB.request("NotificacionesGetPriorizada", "notificaciones", "GET", "/v1/notificaciones/", contexto);
        request.query("idcliente", contexto.idCobis());
        request.query("priorizada", "true");
        request.cacheSesion = true;
        return ApiMB.response(request, contexto.idCobis());
    }

    public static void eliminarCacheNotificacionesPriorizadas(ContextoMB contexto) {
        ApiMB.eliminarCache(contexto, "NotificacionesGetPriorizada");
    }

    public static ApiResponseMB cacheNotificaciones(ContextoMB contexto) {
        // String idCliente = "4373070";
        ApiRequestMB request = ApiMB.request("NotificacionesGet", "notificaciones", "GET", "/v1/notificaciones/", contexto);
        request.query("idcliente", contexto.idCobis());
        request.query("priorizada", "false");
        request.cacheSesion = true;
        return ApiMB.response(request);
    }

    public static void eliminarCacheNotificaciones(ContextoMB contexto) {
        ApiMB.eliminarCache(contexto, "NotificacionesGet");
    }

    public static ApiResponseMB sendSms(ContextoMB contexto, String telefono, String mensaje, String codigo) {
        ApiRequestMB request = ApiMB.request("SendSMS", "notificaciones", "POST", "/v1/notificaciones/sms", contexto);

        request.query("tipo", "MB");
        request.body("telefono", telefono);
        request.body("mensaje", mensaje); // utiliza solo el mensaje
        request.body("codigo", codigo); // no lo utiliza para componer el mensaje
        request.permitirSinLogin = true;
        ApiResponseMB response = ApiMB.response(request);
        return response;
    }

    public static ApiResponseMB envioMailOtroDestino(ContextoMB contexto, String plantilla, Objeto parametros, String emailDestino) {
        if (emailDestino == null || "".equals(emailDestino)) {
            return null;
        }
        if (!MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_notificaciones_mail", "prendido_notificaciones_mail_cobis")) {
            return null;
        }
        if (emailDestino != null && !emailDestino.isEmpty()) {
            ApiRequestMB requestMail = ApiMB.request("LoginCorreoElectronico", "notificaciones", "POST", "/v1/correoelectronico", contexto);
            requestMail.body("de", "aviso@mail-hipotecario.com.ar");
            requestMail.body("para", emailDestino);
            requestMail.body("plantilla", plantilla);
            requestMail.body("parametros", parametros);
            requestMail.permitirSinLogin = true;
            return ApiMB.response(requestMail, new Date().getTime());
        }
        return null;
    }

    public static ApiResponseMB enviarCorreo(ContextoMB contexto, String de, String para, String plantilla, Objeto parametros, String Subject) {
        try {
            if (para == null || para.isEmpty()) {
                return null;
            }

            ApiRequestMB requestMail = ApiMB.request("NotificacionesPostCorreoElectronico", "notificaciones", "POST", "/v1/correoelectronico", contexto);
            requestMail.body("de", de);
            requestMail.body("para", para);
            requestMail.body("plantilla", plantilla);
            requestMail.body("parametros", parametros);
            return ApiMB.response(requestMail, new Date().getTime());
        } catch (Exception e) {
            // TODO: handle exception
        }
        return null;
    }

    /**
     * Envío de Notificacion Push
     *
     * @param contexto
     * @param mensaje
     * @param url
     * @return apiResponse.
     */
    public static ApiResponseMB enviarNotificacionPush(ContextoMB contexto, String mensaje, String url, String notificationType, String label, String title, String type) {
        ApiRequestMB request = ApiMB.request("SegmentacionPush", "segmentacion_mobile", "POST", "/v1/send", contexto);
        request.body("cobisList", Collections.singletonList(contexto.idCobis()));
        request.body("label", label);
        request.body("message", mensaje);
        request.body("title", title);
        request.body("type", type);
        request.body("notificationType", notificationType);
        request.body("validLabel", "true");
        request.body("webUrl", url);

        return ApiMB.response(request);
    }

    /**
     * Envío de Notificacion Push Online
     *
     * @param contexto
     * @param mensaje
     * @param url
     * @return apiResponse.
     */
    public static ApiResponseMB enviarNotificacionPushOnline(ContextoMB contexto, String mensaje, String url, String notificationType, String label, String title, String type) {
        ApiRequestMB request = ApiMB.request("SegmentacionPush", "segmentacion_mobile", "POST", "/v1/notify", contexto);
        request.body("cobisList", Collections.singletonList(contexto.idCobis()));
        request.body("label", label);
        request.body("message", mensaje);
        request.body("title", title);
        request.body("type", type);
        request.body("notificationType", notificationType);
        request.body("validLabel", "true");
        request.body("webUrl", url);

        return ApiMB.response(request);
    }

    /**
     * Envío de Email de Ingreso a Aplicacion
     *
     * @param contexto
     * @param cuit
     * @param nombre
     * @param apellido
     */
    public static boolean enviarMailIngreso(ContextoMB contexto, String cuit, String nombre, String apellido) {
        try {
            if (consultaConfiguracionAlertasEspecifica(contexto, "A_ACC")) {
                String emailDestino = RestPersona.direccionEmail(contexto, cuit);
                if (StringUtils.isBlank(emailDestino))
                    return false;

                if (cuit == null || "".equals(cuit)) {
                    Persona persona = contexto.persona();
                    cuit = persona.cuit();
                    apellido = persona.apellidos();
                    nombre = persona.nombres();
                }

                ApiRequestMB requestMail = ApiMB.request("LoginCorreoElectronico", "notificaciones", "POST", "/v1/correoelectronico", contexto);
                requestMail.body("de", "aviso@mail-hipotecario.com.ar");
                requestMail.body("para", emailDestino);
                requestMail.body("plantilla", ConfigMB.string("doppler_acceso_usuario"));
                requestMail.permitirSinLogin = true;
                Objeto parametros = requestMail.body("parametros");
                parametros.set("Subject", "Ingreso a BH");
                parametros.set("NOMBRE", nombre);
                parametros.set("APELLIDO", apellido);
                Date hoy = new Date();
                parametros.set("FECHA", new SimpleDateFormat("dd/MM/yyyy").format(hoy));
                parametros.set("HORA", new SimpleDateFormat("hh:mm a").format(hoy));
                parametros.set("CANAL", ConfigMB.string("salesforce_nombre_canal"));
                parametros.set("TITULAR_CANAL", apellido);

                ApiMB.response(requestMail, new Date().getTime());

                return true;
            }
            return false;
        } catch (Exception ex) {
            return false;
        }
    }
}
