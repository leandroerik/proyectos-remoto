package ar.com.hipotecario.canal.homebanking.api;

import java.text.SimpleDateFormat;
import java.util.List;

import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.Respuesta;
import ar.com.hipotecario.canal.homebanking.base.ConfigHB;
import ar.com.hipotecario.canal.homebanking.base.Objeto;
import ar.com.hipotecario.canal.homebanking.conector.Api;
import ar.com.hipotecario.canal.homebanking.conector.ApiRequest;
import ar.com.hipotecario.canal.homebanking.conector.ApiResponse;
import ar.com.hipotecario.canal.homebanking.conector.Sql;
import ar.com.hipotecario.canal.homebanking.conector.SqlRequest;
import ar.com.hipotecario.canal.homebanking.servicio.RestPersona;
import ar.com.hipotecario.canal.homebanking.servicio.RestPostventa;
import ar.com.hipotecario.canal.homebanking.servicio.SqlHomebanking;

public class HBLineaRoja {

    public static Respuesta validarUserRiesgoNet(ContextoHB contexto) {
        String dni = contexto.parametros.string("dni");
        String tipoDocumento = contexto.parametros.string("tipoDocumento", "01");
        String sexo = contexto.parametros.string("sexo");
        String nombre = contexto.parametros.string("nombre");
        String apellido = contexto.parametros.string("apellido");

        List<String> listaIdCobis = RestPersona.listaIdCobis(contexto, dni, tipoDocumento, sexo);

        if (listaIdCobis == null) {
            if (contexto.sesion.clienteInexistente) {
                return Respuesta.estado("PERSONA_NO_ENCONTRADA").set("mensaje", ConfigHB.string("mensaje_dni_no_encontrado", "Los datos ingresados no son válidos. Corroborá que ingresaste bien el DNI."));
            }
            return Respuesta.estado(contexto.sesion.cobisCaido ? "COBIS_CAIDO" : "ERROR");
        } else if (listaIdCobis.isEmpty()) {
            return Respuesta.estado("PERSONA_NO_ENCONTRADA").set("mensaje", ConfigHB.string("mensaje_dni_no_encontrado", "Los datos ingresados no son válidos. Corroborá que ingresaste bien el DNI."));
        } else if (listaIdCobis.size() > 1) {
            return Respuesta.estado("MULTIPLES_PERSONAS_ENCONTRADAS");
        }

        String idCobis = listaIdCobis.get(0);
        contexto.sesion.idCobis = (idCobis);
        contexto.sesion.idCobisReal = (idCobis);
        contexto.sesion.save();

        if (SqlHomebanking.bloqueadoPorFraude(idCobis))
            return Respuesta.estado("USUARIO_BLOQUEADO_POR_FRAUDE");

        if (Objeto.empty(contexto.persona()))
            return Respuesta.estado("DATOS_ERRONEOS");

        if (!nombre.toUpperCase().contains(contexto.persona().nombre().toUpperCase()))
            return Respuesta.estado("DATOS_ERRONEOS");

        if (!apellido.toUpperCase().contains(contexto.persona().apellido().toUpperCase()))
            return Respuesta.estado("DATOS_ERRONEOS");

        return "true".equals(ConfigHB.string("prendido_linea_roja_nuevo_flujo")) ? Respuesta.exito().set("esMigrado", contexto.esMigrado(contexto)) : HBLogin.preguntasRiesgoNet(contexto);
    }

    public static Respuesta responderPreguntasRiesgoNetLR(ContextoHB contexto) {
        Objeto respuestas = (Objeto) contexto.parametros.get("respuestas");
        Boolean lineaRoja = contexto.parametros.bool("lineaRoja", false);

        if (!lineaRoja) {
            return Respuesta.parametrosIncorrectos();
        }

        Boolean bloqueadoRiesgoNet = contexto.bloqueadoRiesgoNet();
        if (bloqueadoRiesgoNet) {
            return Respuesta.estado("BLOQUEADO_RIESGONET");
        }

        Integer cantidadRespuestasCorrectas = 0;
        for (Integer idPregunta : contexto.sesion.respuestasRiesgoNet.keySet()) {
            for (Objeto item : respuestas.objetos()) {
                if (idPregunta.equals(item.integer("idPregunta"))) {
                    Integer idRespuesta = contexto.sesion.respuestasRiesgoNet.get(idPregunta);
                    cantidadRespuestasCorrectas += idRespuesta.equals(item.integer("idRespuesta")) ? 1 : 0;
                    break;
                }
            }
        }

        Boolean validaRiesgoNet = false;
        Integer cantidadRespuestasIncorrectas = 5 - cantidadRespuestasCorrectas;

        if (cantidadRespuestasCorrectas < ConfigHB.integer("cantidad_respuestas_correctas_riesgonet", 4)) {
            SqlRequest sqlRequest = Sql.request("InsertRiesgoNet", "homebanking");
            sqlRequest.sql = "INSERT INTO [Homebanking].[dbo].[riesgo_net] (idCobis, momento, respuestasIncorrectas) VALUES (?, GETDATE(), ?)";
            sqlRequest.add(contexto.idCobis());
            sqlRequest.add(cantidadRespuestasIncorrectas);
            Sql.response(sqlRequest);

            bloqueadoRiesgoNet = contexto.bloqueadoRiesgoNet();

            contexto.insertarLogEnvioOtp(contexto, null, null, true, null, "R");

            if (bloqueadoRiesgoNet) {
                return Respuesta.estado("BLOQUEADO_RIESGONET");
            }
        } else {
            validaRiesgoNet = true;
            contexto.sesion.validaRiesgoNet = (true);
            contexto.sesion.save();
            Integer cantidadDiasBloqueo = ConfigHB.integer("cantidad_dias_bloqueo_riesgonet", 1);
            String inicio = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.sql.Date(new java.util.Date().getTime() - cantidadDiasBloqueo * 24 * 60 * 60 * 1000L));
            String fin = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.sql.Date(new java.util.Date().getTime()));

            SqlRequest sqlRequest = Sql.request("DeleteRiesgoNet", "homebanking");
            sqlRequest.sql = "DELETE [Homebanking].[dbo].[riesgo_net] WHERE idCobis = ? AND momento > ? AND momento < ?";
            sqlRequest.add(contexto.idCobis());
            sqlRequest.add(inicio);
            sqlRequest.add(fin);
            Sql.response(sqlRequest);

            contexto.insertarLogEnvioOtp(contexto, null, null, true, null, "A");
        }

        return validaRiesgoNet ? Respuesta.exito() : Respuesta.estado("RESPUESTAS_INCORRECTAS");
    }

    public static Respuesta generarCasoLineaRoja(ContextoHB contexto) {
        Respuesta respuesta = new Respuesta();
        try {

            if (!contexto.validaSegundoFactor("linea-roja"))
                return Respuesta.estado("REQUIERE_SEGUNDO_FACTOR");

            if (!SqlHomebanking.bloquearPorFraude(contexto.idCobis())) {
                return Respuesta.error();
            }
            respuesta = HBBiometria.revocaAutenticador(contexto);
            if (respuesta.hayError()) {
                return respuesta;
            }

            respuesta = HBSoftToken.revocarSoftToken(contexto, "CUENTA_EN_RIESGO");

            if (respuesta.hayError()) {
                return respuesta;
            }

            ApiRequest requestMail = Api.request("NotificacionesPostCorreoElectronico", "notificaciones", "POST", "/v1/correoelectronico", contexto);
            requestMail.body("de", "aviso@mail-hipotecario.com.ar");
            requestMail.body("para", contexto.persona().email());
            requestMail.body("plantilla", ConfigHB.string("doppler_linea_roja"));
            Objeto parametros = requestMail.body("parametros");
            parametros.set("Subject", "Bloqueo de tu cuenta");
            parametros.set("nombreapellido", contexto.persona().nombreCompleto());
            requestMail.permitirSinLogin = true;

            ApiResponse responseMail = Api.response(requestMail);
            if (responseMail.hayError()) {
                return Respuesta.error();
            }
        } catch (Exception e) {
            Respuesta.error();
        }
        return respuesta;
    }

    @SuppressWarnings("unused")
    private static Respuesta crearLineaRoja(ContextoHB contexto) {
        String numeroCaso = "";
        String pTelefono = contexto.parametros.string("telefono");
        String pDomicilio = contexto.parametros.string("domicilio");
        String pMail = contexto.parametros.string("mail");
        String pDetalleUltMov = contexto.parametros.string("detalleUltMov");
        String pDetalleMovDesc = contexto.parametros.string("detalleMovDesc");
        String pDetalle = contexto.parametros.string("detalle");

        if (Objeto.anyEmpty(pTelefono, pMail, pDetalleUltMov, pDetalleMovDesc, pDetalle, pDomicilio)) {
            return Respuesta.parametrosIncorrectos();
        }

        ApiResponse caso = RestPostventa.crearCasoLineaRoja(contexto);

        if (caso == null || caso.hayError()) {
            return Respuesta.estado("ERROR_GENERANDO_RECLAMO");
        }

        Objeto reclamo = (Objeto) caso.get("Datos");
        numeroCaso = reclamo.objetos().get(0).string("NumeracionCRM");

        if (numeroCaso.isEmpty()) {
            return Respuesta.estado("ERROR_GENERANDO_RECLAMO");
        }
        return Respuesta.exito();
    }

}
