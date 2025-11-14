package ar.com.hipotecario.mobile.api;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.util.Transmit;
import ar.com.hipotecario.canal.libreriariesgofraudes.infrastructure.gateway.model.bm.mb.AltaDispositivoMBBMBankProcess;
import ar.com.hipotecario.mobile.ConfigMB;
import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.RespuestaMB;
import ar.com.hipotecario.mobile.conector.ApiMB;
import ar.com.hipotecario.mobile.conector.ApiRequestMB;
import ar.com.hipotecario.mobile.conector.ApiResponseMB;
import ar.com.hipotecario.mobile.conector.SqlResponseMB;
import ar.com.hipotecario.mobile.lib.Objeto;
import ar.com.hipotecario.mobile.lib.Util;
import ar.com.hipotecario.mobile.servicio.RestNotificaciones;
import ar.com.hipotecario.mobile.servicio.SqlRegistroDispositivo;
import ar.com.hipotecario.mobile.servicio.TransmitMB;

public class MBRegistroDispositivo {

    private static final String SIN_PSEUDO_SESION = "SIN_PSEUDO_SESION";
    private static final String ERROR_REGISTRAR = "ERROR_REGISTRAR";
    private static final String ERROR_SQL = "ERROR_SQL";
    private static final String REQUIERE_SEGUNDO_FACTOR = "REQUIERE_SEGUNDO_FACTOR";
    private static final String LABEL_PUSH = "DISPOSITIVO_SEGURO";
    private static final String TITLE_PUSH = "Registraste tu dispositivo";
    private static final String MENSAJE_PUSH = "✋🏻⚠️ Detectamos que registraste tu dispositivo como seguro ⚠️✋🏻 Si no fuiste vos, entrá a Home Banking y hace click en “Mi cuenta está en riesgo”";
    private static final String TYPE_PUSH = "Login";
    private static final String NOTIFICATION_TYPE_PUSH = "Login";

    /**
     * Realiza la registración del dispositivo.
     *
     * @param contexto
     * @return respuesta.
     */
    public static RespuestaMB registrar(ContextoMB contexto) {
        String idDispositivo = contexto.parametros.string("idDispositivo", null);
        String alias = contexto.parametros.string("alias", null);

        if (Objeto.anyEmpty(contexto.idCobis()))
            return RespuestaMB.estado(SIN_PSEUDO_SESION);

        if (Objeto.anyEmpty(idDispositivo, alias))
            return RespuestaMB.parametrosIncorrectos();

        if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_modo_transaccional_alta_nuevo_dispositivo",
                "prendido_modo_transaccional_alta_nuevo_dispositivo_cobis")) {
            try {
                String sessionToken = contexto.parametros.string(Transmit.getSessionToken(), null);
                if (Objeto.empty(sessionToken))
                    return RespuestaMB.parametrosIncorrectos();

                AltaDispositivoMBBMBankProcess altaDispositivoMBBMBankProcess = new AltaDispositivoMBBMBankProcess(contexto.idCobis(), sessionToken);

                RespuestaMB respuesta = TransmitMB.recomendacionTransmit(contexto, altaDispositivoMBBMBankProcess, "registrar-dispositivo");
                if (respuesta.hayError())
                    return respuesta;
            } catch (Exception e) {
            }
        }

        if (!contexto.validaSegundoFactor("registrar-dispositivo"))
            return RespuestaMB.estado(REQUIERE_SEGUNDO_FACTOR);

        if (SqlRegistroDispositivo.insertarDispositivo(contexto.idCobis(), idDispositivo, alias, contexto.ip())) {
            contexto.limpiarSegundoFactor();
            enviarNotificaciones(contexto, alias);
            return RespuestaMB.exito();
        }

        return RespuestaMB.estado(ERROR_REGISTRAR);
    }

    /**
     * Verifica si el cobis tiene dispositivos registrados.
     *
     * @param contexto
     * @return respuesta.
     */
    public static RespuestaMB tieneDispositivosRegistrados(ContextoMB contexto) {

        if (Objeto.anyEmpty(contexto.idCobis()))
            return RespuestaMB.estado(SIN_PSEUDO_SESION);

        SqlResponseMB sqlResponse = SqlRegistroDispositivo.obtenerDispositivosRegistrados(contexto.idCobis());

        if (sqlResponse == null || (sqlResponse != null && sqlResponse.hayError))
            return RespuestaMB.estado(ERROR_SQL);

        return RespuestaMB.exito("tieneDispositivosRegistrados", !sqlResponse.registros.isEmpty());
    }

    /**
     * Consulta si el dispositivo es el último registrado del usuario
     *
     * @param contexto
     * @return respuesta.
     */
    public static RespuestaMB esUltimoRegistrado(ContextoMB contexto) {
        String idDispositivo = contexto.parametros.string("idDispositivo", null);

        if (Objeto.anyEmpty(contexto.idCobis()))
            return RespuestaMB.estado(SIN_PSEUDO_SESION);

        if (Objeto.anyEmpty(idDispositivo))
            return RespuestaMB.parametrosIncorrectos();

        SqlResponseMB sqlResponse = SqlRegistroDispositivo.ultimoRegistrado(contexto.idCobis());

        if (sqlResponse == null || (sqlResponse != null && sqlResponse.hayError) || (sqlResponse != null && !sqlResponse.hayError && sqlResponse.registros.isEmpty()))
            return RespuestaMB.estado(ERROR_SQL);

        return RespuestaMB.exito("esUltimoRegistrado", sqlResponse.registros.get(0).get("id_dispositivo").equals(idDispositivo));
    }

    /**
     * Devuelve el último dispositivo registrado del usuario
     *
     * @param contexto
     * @return respuesta.
     */
    public static RespuestaMB ultimoRegistrado(ContextoMB contexto) {
        if (Objeto.anyEmpty(contexto.idCobis()))
            return RespuestaMB.estado(SIN_PSEUDO_SESION);

        SqlResponseMB sqlResponse = SqlRegistroDispositivo.ultimoRegistrado(contexto.idCobis());

        if (sqlResponse.hayError)
            return RespuestaMB.estado(ERROR_SQL);

        return RespuestaMB.exito("ultimoRegistrado", sqlResponse.registros.isEmpty() ? "" : sqlResponse.registros.get(0).get("id_dispositivo"));
    }

    /**
     * Envío de Notificaciones
     *
     * @param contexto
     */
    private static void enviarNotificaciones(ContextoMB contexto, String alias) {
        try {
            Objeto parametros = new Objeto();
            parametros.set("NOMBRE_APELLIDO", contexto.persona().nombre() + " " + contexto.persona().apellido());
            parametros.set("NOMBRE", contexto.persona().nombre());
            parametros.set("APELLIDO", contexto.persona().apellido());
            parametros.set("FECHA", new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
            parametros.set("HORA", new SimpleDateFormat("HH:mm").format(new Date()));
            parametros.set("NOMBRE_DISPOSITIVO", alias);

            if (MBSalesforce.prendidoSalesforceAmbienteBajoConFF(contexto)) {
                String salesforce_registro_dispositivo_seguro = ConfigMB.string("salesforce_registro_dispositivo_seguro");
                parametros.set("IDCOBIS", contexto.idCobis());
                parametros.set("CANAL", ConfigMB.string("salesforce_nombre_canal"));
                new Futuro<>(() -> MBSalesforce.registrarEventoSalesforce(contexto, salesforce_registro_dispositivo_seguro, parametros));
            } else {
                new Futuro<>(() -> RestNotificaciones.envioMail(contexto, ConfigMB.string("doppler_registro_dispositivo"), parametros));
            }
            new Futuro<>(() -> RestNotificaciones.enviarNotificacionPushOnline(contexto, MENSAJE_PUSH, "", NOTIFICATION_TYPE_PUSH, LABEL_PUSH, TITLE_PUSH, TYPE_PUSH));
        } catch (Exception e) {
        }
    }

}
