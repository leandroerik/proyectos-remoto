package ar.com.hipotecario.canal.buhobank;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Date;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.concurrent.CompletableFuture;

import ar.com.hipotecario.backend.Modulo;
import ar.com.hipotecario.backend.ServerSentEvents;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.cuentas.ApiCuentasBB;
import ar.com.hipotecario.backend.servicio.api.cuentas.CuentasBB;
import ar.com.hipotecario.backend.servicio.api.cuentas.CuentasBB.CuentaBB;
import ar.com.hipotecario.backend.servicio.api.inversiones.ApiInversiones;
import ar.com.hipotecario.backend.servicio.api.notificaciones.ApiNotificaciones;
import ar.com.hipotecario.backend.servicio.api.notificaciones.EnvioEmail;
import ar.com.hipotecario.backend.servicio.api.personas.ApiPersonas;
import ar.com.hipotecario.backend.servicio.api.personas.Domicilios;
import ar.com.hipotecario.backend.servicio.api.personas.Persona;
import ar.com.hipotecario.backend.servicio.api.personas.Telefonos;
import ar.com.hipotecario.backend.servicio.api.personas.DataValids;
import ar.com.hipotecario.backend.servicio.api.personas.DataValids.DataValid;
import ar.com.hipotecario.backend.servicio.api.personas.Domicilios.Domicilio;
import ar.com.hipotecario.backend.servicio.api.personas.Telefonos.Telefono;
import ar.com.hipotecario.backend.servicio.api.tarjetasCredito.ApiTarjetasCredito;
import ar.com.hipotecario.backend.servicio.api.tarjetasCredito.NFC;
import ar.com.hipotecario.backend.servicio.api.ventas.ApiVentas;
import ar.com.hipotecario.backend.servicio.api.ventas.Integrantes.Integrante;
import ar.com.hipotecario.backend.servicio.api.ventas.Resolucion;
import ar.com.hipotecario.backend.servicio.api.ventas.RolIntegrantes.RolIntegrante;
import ar.com.hipotecario.backend.servicio.api.ventas.Solicitud;
import ar.com.hipotecario.backend.servicio.api.ventas.Solicitud.SolicitudProducto;
import ar.com.hipotecario.backend.servicio.api.ventas.SolicitudCajaAhorro;
import ar.com.hipotecario.backend.servicio.api.ventas.SolicitudPaquete;
import ar.com.hipotecario.backend.servicio.api.ventas.SolicitudTarjetaDebito;
import ar.com.hipotecario.backend.servicio.sql.SqlBuhoBank;
import ar.com.hipotecario.backend.servicio.sql.SqlEsales;
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBPlantillasDopplerBuhobank;
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBPlantillasDopplerBuhobank.BBPlantillaDopplerBuhobank;
import ar.com.hipotecario.backend.servicio.sql.buhobank.LogsBuhoBank;
import ar.com.hipotecario.backend.servicio.sql.buhobank.LogsBuhoBank.LogBuhoBank;
import ar.com.hipotecario.backend.servicio.sql.esales.SesionesEsales;
import ar.com.hipotecario.backend.servicio.sql.esales.SesionesEsales.SesionEsales;
import ar.com.hipotecario.backend.servicio.sql.esales.SesionesEsalesBB2.SesionEsalesBB2;
import ar.com.hipotecario.backend.servicio.sql.esales.SesionesStandBy;
import ar.com.hipotecario.backend.servicio.sql.esales.SesionesStandBy.SesionStandBy;
import ar.com.hipotecario.backend.util.Transmit;
import ar.com.hipotecario.canal.buhobank.api.BBSalesforce;
import ar.com.hipotecario.canal.buhobank.transmit.TransmitBB;
import ar.com.hipotecario.canal.libreriariesgofraudes.application.dto.RecommendationDTO;
import ar.com.hipotecario.canal.libreriariesgofraudes.audit.model.AuditLogReport;
import ar.com.hipotecario.canal.libreriariesgofraudes.infrastructure.gateway.model.bm.mb.OnboardingMBBMBankProcess;

import static ar.com.hipotecario.canal.buhobank.transmit.TransmitBB.isErrorRecommendationOnboarding;

public class BBAlta extends Modulo {

    public static void evento(ServerSentEvents<ContextoBB> sse, Integer statusCode, Integer progress, String message) {
        Objeto datos = new Objeto().set("code", statusCode);
        sse.evento("message", datos.set("progress", progress).set("message", message));
    }

    public static void mensaje(ServerSentEvents<ContextoBB> sse, ContextoBB contexto, Integer progress, String message) {
        LogBB.eventoHomo(contexto, "200 | " + progress + " | " + message);
        if (sse == null) {
            log(contexto, "200 | " + progress + " | " + message);
            return;
        }
        evento(sse, 200, progress, message);
    }

    public static void error(ServerSentEvents<ContextoBB> sse, ContextoBB contexto, Integer progress, String message) {
        finalizarEjecucion(contexto, "error: " + message);
        LogBB.eventoHomo(contexto, "200 | " + progress + " | " + message);
        if (sse == null) {
            log(contexto, "400 | " + progress + " | " + message);
            return;
        }
        evento(sse, 400, progress, message);
    }

    public static void enviarMailError(ContextoBB contexto, Boolean mandarMailError) {
        if (!mandarMailError)
            return;

        enviarEmail(contexto, BBPlantillasDopplerBuhobank.TIPO_ALTA_ERROR, GeneralBB.ENVIO_MAIL_ALTA_ERROR);
    }

    public static void enviarMailFelicitaciones(ContextoBB contexto, Boolean mandarMailFelicitaciones) {
        if (!mandarMailFelicitaciones)
            return;

        SesionBB sesion = contexto.sesion();

        if (sesion.esFlujoInversiones()) {
            enviarMailFelicitacionesInversiones(contexto);
            return;
        }

        if (sesion.esSoloTdVirtual()) {
            enviarEmail(contexto, BBPlantillasDopplerBuhobank.TIPO_ALTA_TD_VIRTUAL_OK, GeneralBB.ENVIO_MAIL_ALTA_OK);
            return;
        } else if (sesion.esTcVirtual()) {
            enviarEmail(contexto, BBPlantillasDopplerBuhobank.TIPO_ALTA_TC_VIRTUAL_OK, GeneralBB.ENVIO_MAIL_ALTA_OK);
            return;
        }

        if (sesion.esStandalone()) {
            enviarEmail(contexto, BBPlantillasDopplerBuhobank.TIPO_ALTA_TD_OK, GeneralBB.ENVIO_MAIL_ALTA_OK);
            return;
        }

        enviarEmail(contexto, BBPlantillasDopplerBuhobank.TIPO_ALTA_TC_OK, GeneralBB.ENVIO_MAIL_ALTA_OK);
    }

    private static void enviarMailFelicitacionesInversiones(ContextoBB contexto) {
        String tipoPlantilla = BBPlantillasDopplerBuhobank.TIPO_ALTA_TD_OK;
        SesionBB sesion = contexto.sesion();
        BBPlantillasDopplerBuhobank plantillas = SqlBuhoBank.obtenerPlantillasDoppler(contexto, sesion.getFlujo()).tryGet();
        BBPlantillaDopplerBuhobank plantilla = BBPlantillasDopplerBuhobank.buscarTipo(plantillas, tipoPlantilla);

        Boolean estaEnHorarioInversor = BBAplicacion.estaEnHorarioInversor(contexto);

        String contenido = estaEnHorarioInversor ? BBPlantillasDopplerBuhobank.ITEM_CUENTA_INVERSOR_EMAIL_ALTA_OK : BBPlantillasDopplerBuhobank.ITEM_CUENTA_INVERSOR_FUERA_DE_HORARIO_EMAIL_ALTA_OK;
        if (sesion.esTdVirtual()) {
            contenido += BBPlantillasDopplerBuhobank.ITEM_INVERSOR_TD_VIRTUAL_EMAIL_ALTA_OK;
        }

        if (sesion.esTdFisica()) {
            contenido += BBPlantillasDopplerBuhobank.ITEM_INVERSOR_EN_CAMINO_EMAIL_ALTA_OK;
        }

        EnvioEmail envioMail = ApiNotificaciones.postEnvioBBV2(contexto, plantilla.asunto, plantilla.codigo, sesion.mail, sesion.getPrimerNombre(), "https://www.hipotecario.com.ar/beneficios/", contenido).tryGet();
        if (envioMail != null) {
            LogBB.evento(contexto, GeneralBB.ENVIO_MAIL_ALTA_OK, " mail:" + sesion.mail + " |tipo_plantilla:" + tipoPlantilla + " |codigo_plantilla:" + plantilla.codigo + " |asunto:" + plantilla.asunto);
        } else {
            LogBB.evento(contexto, GeneralBB.ENVIO_MAIL_ALTA_OK, " mail:" + sesion.mail + " |tipo_plantilla:" + tipoPlantilla + " |codigo_plantilla:" + plantilla.codigo + " |asunto:" + plantilla.asunto);
        }
    }

    public static Object finalizarStandaloneCore(ServerSentEvents<ContextoBB> sse, ContextoBB contexto, Boolean mandarMailError, Boolean mandarMailFelicitaciones) {
        mensaje(sse, contexto, 0, "Se inicia proceso de venta standalone");
        SesionBB sesion = contexto.sesion();
        String cuil = sesion.cuil;

        mensaje(sse, contexto, 10, "Se solicita la obtencion de la solicitud");

        Solicitud solicitud = BBVentas.obtenerSolicitud(contexto, sesion.idSolicitud);
        String idSolicitud = solicitud != null ? solicitud.Id : null;

        if(solicitud == null || empty(idSolicitud) || !sesion.getVentaTemprana()){

            if (solicitud == null || solicitud.paquete() != null) {

                Solicitud nuevaSolicitud = BBVentas.crearSolicitud(contexto);
                if (nuevaSolicitud != null) {
                    String idNuevaSolicitud = nuevaSolicitud.Id;
                    sesion.idSolicitud = idNuevaSolicitud;
                    sesion.saveSesion();
                    solicitud = BBVentas.obtenerSolicitud(contexto, idNuevaSolicitud);
                }
            }

            if (solicitud == null) {
                sesion.actualizarEstadoError(ErroresBB.ETAPA_FINALIZAR);
                LogBB.error(contexto, ErroresBB.SOLICITUD_NO_CREADA);
                error(sse, contexto, 11, "Solicitud de ventas no creada");
                enviarMailError(contexto, mandarMailError);
                return null;
            }

            if (!actualizarSolicitud(contexto, solicitud)) {
                sesion.actualizarEstadoError(ErroresBB.ETAPA_FINALIZAR);
                LogBB.error(contexto, EstadosBB.ETAPA_POST_ENVIO_A_CONTENEDOR, ErroresBB.SOLICITUD_NO_ACTUALIZADA);
                error(sse, contexto, 11, "solicitud no actualizada");
                enviarMailError(contexto, mandarMailError);
                return null;
            }

            SolicitudProducto existeCajaAhorroPesos = solicitud.cajaAhorroPesos();
            SolicitudProducto existeCajaAhorroDolares = solicitud.cajaAhorroDolares();
            SolicitudProducto existeTarjetaDebito = solicitud.tarjetaDebito();

            mensaje(sse, contexto, 15, "Se obtiene la solicitud correctamente");

            idSolicitud = solicitud.Id;
            mensaje(sse, contexto, 20, "Se solicita que la solicitud en ventas haya sido creada");
            if (empty(idSolicitud)) {
                sesion.actualizarEstadoError(ErroresBB.ETAPA_FINALIZAR);
                LogBB.error(contexto, ErroresBB.SOLICITUD_VACIA);
                error(sse, contexto, 21, "Solicitud de ventas vacia");
                enviarMailError(contexto, mandarMailError);
                return null;
            }
            mensaje(sse, contexto, 25, "Se revisa la existencia de la solicitud correctamente");

            mensaje(sse, contexto, 30, "Se solicita el agregado del integrante titular");

            Integrante integrante = BBVentas.agregarIntegrante(contexto, idSolicitud, cuil, ErroresBB.INTEGRANTE_VACIO);
            if (integrante == null) {

                if (sesion.esErrorFueraDeServicio(contexto)) {
                    return marcarBatchCorriendo(contexto, sse != null);
                }

                sesion.actualizarEstadoError(ErroresBB.ETAPA_FINALIZAR);
                error(sse, contexto, 31, "Agregado de integrante titular no realizado");
                enviarMailError(contexto, mandarMailError);
                return null;
            }

            mensaje(sse, contexto, 35, "Se agrega el integrante titular correctamente");

            if (sesion.casada()) {
                mensaje(sse, contexto, 36, "Se solicita el agregado del integrante cotitular");
                String cuilCotitular = sesion.conyuge.cuil;
                Integrante cotitular = BBVentas.agregarIntegrante(contexto, idSolicitud, cuilCotitular, ErroresBB.INTEGRANTE_COTITULAR_VACIO);
                if (cotitular == null) {

                    if (sesion.esErrorFueraDeServicio(contexto)) {
                        return marcarBatchCorriendo(contexto, sse != null);
                    }

                    sesion.actualizarEstadoError(ErroresBB.ETAPA_FINALIZAR);
                    error(sse, contexto, 37, "Agregado de integrante cotitular no realizado");
                    enviarMailError(contexto, mandarMailError);
                    return null;
                }
                mensaje(sse, contexto, 39, "Se agrega el integrante cotitular correctamente");
            }

            mensaje(sse, contexto, 40, "Se solicita el agregado de la caja de ahorro");

            CompletableFuture<SolicitudCajaAhorro> cajaAhorroFuture = null;
            if(existeCajaAhorroPesos == null){
                String finalIdSolicitud = idSolicitud;
                cajaAhorroFuture = CompletableFuture.supplyAsync(() -> BBVentas.cajaAhorroV2(contexto, finalIdSolicitud));
            }

            CompletableFuture<SolicitudCajaAhorro> cajaAhorroUsdFuture = null;
            if (sesion.buhoInversorAceptada() && existeCajaAhorroDolares == null) {
                String finalIdSolicitud = idSolicitud;
                cajaAhorroUsdFuture = CompletableFuture.supplyAsync(() -> BBVentas.cajaAhorroUsdV2(contexto, finalIdSolicitud));
            }

            SolicitudCajaAhorro cajaAhorro = cajaAhorroFuture != null ? cajaAhorroFuture.join() : null;
            SolicitudCajaAhorro cajaAhorroUsd = cajaAhorroUsdFuture != null ? cajaAhorroUsdFuture.join() : null;

            if(existeCajaAhorroPesos == null && cajaAhorro == null){
                if (sesion.esErrorFueraDeServicio(contexto)) {
                    return marcarBatchCorriendo(contexto, sse != null);
                }

                sesion.actualizarEstadoError(ErroresBB.ETAPA_FINALIZAR);
                error(sse, contexto, 41, "Caja de ahorros no agregada");
                enviarMailError(contexto, mandarMailError);
                return null;
            }

            if (sesion.buhoInversorAceptada() && existeCajaAhorroDolares == null && cajaAhorroUsd == null) {
                if (sesion.esErrorFueraDeServicio(contexto)) {
                    return marcarBatchCorriendo(contexto, sse != null);
                }

                sesion.actualizarEstadoError(ErroresBB.ETAPA_FINALIZAR);
                error(sse, contexto, 43, "Caja de ahorros Dolares no agregada");
                enviarMailError(contexto, mandarMailError);
                return null;
            }

            mensaje(sse, contexto, 45, "Se agrega la caja de ahorro correctamente");

            mensaje(sse, contexto, 50, "Se solicita el agregado de la tarjeta de debito");

            if (existeTarjetaDebito == null) {
                SolicitudTarjetaDebito solicitudTarjetaDebito = BBVentas.tarjetaDebitoV2(contexto, idSolicitud);
                if (solicitudTarjetaDebito == null) {

                    if (sesion.esErrorFueraDeServicio(contexto)) {
                        return marcarBatchCorriendo(contexto, sse != null);
                    }

                    sesion.actualizarEstadoError(ErroresBB.ETAPA_FINALIZAR);
                    error(sse, contexto, 51, "Tarjeta de debito no agregada");
                    enviarMailError(contexto, mandarMailError);
                    return null;
                }
            }

            mensaje(sse, contexto, 55, "Se agrega la tarjeta de debito correctamente");

            mensaje(sse, contexto, 60, "Se solicita la resolucion para los productos solicitados");

            Resolucion resolucionGet = ApiVentas.resolucionesGet(contexto, idSolicitud).tryGet();
            if (resolucionGet != null && resolucionGet.ResolucionId == null) {

                Resolucion resolucion = BBVentas.resolucionesPutStand(contexto, idSolicitud, SolicitudPaquete.TIPO_OPERACION_SOLICITUD);
                if (resolucion == null) {

                    if (sesion.esErrorFueraDeServicio(contexto)) {
                        return marcarBatchCorriendo(contexto, sse != null);
                    }

                    sesion.actualizarEstadoError(ErroresBB.ETAPA_FINALIZAR);
                    BBVentas.guardarRechazoMotor(contexto, null, true);
                    error(sse, contexto, 61, "Resolucion nula");
                    enviarMailError(contexto, mandarMailError);
                    return null;
                }
            }

            mensaje(sse, contexto, 65, "Se revisa la resolucion de los productos correctamente");
       }

        mensaje(sse, contexto, 90, "Se solicita la finalizacion de la solicitud de venta standalone");
        solicitud = BBVentas.finalizarSolicitud(contexto, idSolicitud);
        if (solicitud == null) {

            if (sesion.esErrorFueraDeServicio(contexto)) {
                return marcarBatchCorriendo(contexto, sse != null);
            }

            sesion.actualizarEstadoError(ErroresBB.ETAPA_FINALIZAR);
            error(sse, contexto, 91, "Finalizacion de solicitud de venta standalone con error");
            enviarMailError(contexto, mandarMailError);
            return null;
        }

        mensaje(sse, contexto, 98, "Finaliza la venta standalone");

        //enviarMailFelicitaciones(contexto, mandarMailFelicitaciones);

        mensaje(sse, contexto, 100, "Felicitaciones");

        logProceso(contexto, "Se actualiza el estado de las sesion finalizada");

        sesion.actualizarEstado(EstadosBB.FINALIZAR_OK);
        String idCobis = solicitud.Integrantes.get(0).IdCobis;
        LogBB.evento(contexto, EstadosBB.FINALIZAR_OK, sesion.getCanal() + "_" + sesion.descripcionOferta(contexto));
        registrarSalesforce(contexto,contexto.config.bool("buhobank_prendido_salesforce_onboarding"),idCobis);

        altaCuentaInversorV2(contexto);
        validarDatosContacto(contexto);

        return respuesta();
    }

    public static void ventaTempranaStandalone(ContextoBB contexto) {
        try{
            if(contexto.sesion().getVentaTemprana()
                    || !contexto.sesion().getGuardarCobisTemprano()
                    || !contexto.sesion().esFlujoTcv()
                    || !BBPoliticas.estaEnHorario(contexto, 30)){
                return;
            }

            LogBB.eventoHomo(contexto,"inicio - venta standalone temprana");

            SesionBB sesion = contexto.sesion();
            String cuil = sesion.cuil;

            Solicitud solicitud = BBVentas.obtenerSolicitud(contexto, sesion.idSolicitud);
            if (solicitud == null || solicitud.paquete() != null) {
                Solicitud nuevaSolicitud = BBVentas.crearSolicitud(contexto);
                if (nuevaSolicitud != null) {
                    String idSolicitud = nuevaSolicitud.Id;
                    sesion.idSolicitud = idSolicitud;
                    sesion.saveSesion();
                    solicitud = BBVentas.obtenerSolicitud(contexto, idSolicitud);
                }
            }

            if (solicitud == null) {
                throw new Exception("Error");
            }

            if (!actualizarSolicitud(contexto, solicitud)) {
                throw new Exception("Error");
            }

            SolicitudProducto existeCajaAhorroPesos = solicitud.cajaAhorroPesos();
            SolicitudProducto existeCajaAhorroDolares = solicitud.cajaAhorroDolares();
            SolicitudProducto existeTarjetaDebito = solicitud.tarjetaDebito();

            String idSolicitud = solicitud.Id;
            if (empty(idSolicitud)) {
                throw new Exception("Error");
            }

            Integrante integrante = BBVentas.agregarIntegrante(contexto, idSolicitud, cuil, ErroresBB.INTEGRANTE_VACIO);
            if (integrante == null) {
                throw new Exception("Error");
            }

            CompletableFuture<SolicitudCajaAhorro> cajaAhorroFuture = null;
            if(existeCajaAhorroPesos == null){
                 cajaAhorroFuture = CompletableFuture.supplyAsync(() -> BBVentas.cajaAhorroV2(contexto, idSolicitud));
            }

            CompletableFuture<SolicitudCajaAhorro> cajaAhorroUsdFuture = null;
            if (sesion.buhoInversorAceptada() && existeCajaAhorroDolares == null) {
                cajaAhorroUsdFuture = CompletableFuture.supplyAsync(() -> BBVentas.cajaAhorroUsdV2(contexto, idSolicitud));
            }

            SolicitudCajaAhorro cajaAhorro = cajaAhorroFuture != null ? cajaAhorroFuture.join() : null;
            SolicitudCajaAhorro cajaAhorroUsd = cajaAhorroUsdFuture != null ? cajaAhorroUsdFuture.join() : null;

            if(existeCajaAhorroPesos == null && cajaAhorro == null){
                throw new Exception("Error");
            }

            if(sesion.buhoInversorAceptada() && existeCajaAhorroDolares == null && cajaAhorroUsd == null){
                throw new Exception("Error");
            }

            if (existeTarjetaDebito == null) {
                SolicitudTarjetaDebito solicitudTarjetaDebito = BBVentas.tarjetaDebitoV2(contexto, idSolicitud);
                if (solicitudTarjetaDebito == null) {
                    throw new Exception("Error");
                }
            }

            Resolucion resolucionGet = ApiVentas.resolucionesGet(contexto, idSolicitud).tryGet();
            if (resolucionGet != null && resolucionGet.ResolucionId == null) {
                Resolucion resolucion = BBVentas.resolucionesPutStand(contexto, idSolicitud, SolicitudPaquete.TIPO_OPERACION_SOLICITUD);
                if (resolucion == null) {
                    throw new Exception("Error");
                }
            }

            sesion.ventaTempranaOK();
            LogBB.eventoHomo(contexto, "finalizado - venta standalone temprana");
        }
        catch (Exception e){
            LogBB.eventoHomo(contexto,"error - venta standalone temprana");
        }
    }

    private static void validarDatosContacto(ContextoBB contexto) {
        try {
            String cuil = contexto.sesion().cuil;
            DataValids datavalid = ApiPersonas.obtenerDataValid(contexto, cuil, false).tryGet();
            if (datavalid == null) {
                return;
            }

            boolean tieneTelPersonal = false;
            int secTel = 0;
            int secMail = 0;
            int secDir = 0;

            for (DataValid item : datavalid) {
                if ("EMAIL".equals(item.entidad) && "EMP".equals(item.deTipoMail)) {
                    secMail = item.deIdCore;
                }

                if ("TEL".equals(item.entidad) && "E".equals(item.teIdTipoTelefono)) {
                    secTel = item.teIdCore;
                    secDir = item.teDireccionIdCore;
                    tieneTelPersonal = true;
                }
            }

            if (!tieneTelPersonal) {
                for (DataValid item : datavalid) {
                    if ("TEL".equals(item.entidad) && "P".equals(item.teIdTipoTelefono)) {
                        secTel = item.teIdCore;
                        secDir = item.teDireccionIdCore;
                    }
                }
            }

            ApiPersonas.datavalidOtpTel(contexto, contexto.canalOriginacion3(), cuil, secTel, secDir).get();
            ApiPersonas.datavalidOtpEmail(contexto, contexto.canalOriginacion3(), cuil, secMail, "EMP").get();

        } catch (Exception e) {
            LogBB.error(contexto, "VALIDACION_CONTACTO");
        }
    }

    public static Object finalizarStandalone(ContextoBB contexto, Boolean mandarMailError, Boolean mandarMailFelicitaciones) {
        return ServerSentEvents.crear(contexto, (contextoLocal, sse) -> {
            finalizarStandaloneCore(sse, contextoLocal, mandarMailError, mandarMailFelicitaciones);
        });
    }

    public static Object flujoVentasFinal(ContextoBB contexto, Boolean mandarMailError) {
        return ServerSentEvents.crear(contexto, (contextoLocal, sse) -> {
            mensaje(sse, contextoLocal, 0, "Se inicia proceso de venta final");

            BBPaquetes.obtenerPaquetePorLetra(contextoLocal, "L");
            enviarMailError(contexto, mandarMailError);

            mensaje(sse, contextoLocal, 100, "Finaliza la venta en el core");
        });
    }

    public static Object finalizarOptimizadoCore(ServerSentEvents<ContextoBB> sse, ContextoBB contexto, Boolean mandarMailError, Boolean mandarMailFelicitaciones) {
        mensaje(sse, contexto, 0, "Se inicia proceso de venta optimizado");

        SesionBB sesion = contexto.sesion();
        String idSolicitud = sesion.idSolicitud;

        Solicitud solicitudExistente = ApiVentas.solicitud(contexto, idSolicitud).get();
        if (solicitudExistente != null && !actualizarSolicitud(contexto, solicitudExistente)) {
            sesion.actualizarEstadoError(ErroresBB.ETAPA_FINALIZAR);
            LogBB.error(contexto, EstadosBB.ETAPA_POST_ENVIO_A_CONTENEDOR, ErroresBB.SOLICITUD_NO_ACTUALIZADA);
            error(sse, contexto, 11, "solicitud no actualizada");
            enviarMailError(contexto, mandarMailError);
            return null;
        }

        if(!sesion.getVentaTemprana()){

            mensaje(sse, contexto, 10, "Se solicita la resolucion obtenida previamente");
            if (!sesion.resolucionAprobada()) {
                sesion.actualizarEstadoError(ErroresBB.ETAPA_FINALIZAR);
                LogBB.error(contexto, ErroresBB.RESOLUCION_RECHAZADA, sesion.resolucionMotorDeScoring);
                error(sse, contexto, 11, "Resolucion no aprobada");
                enviarMailError(contexto, mandarMailError);
                return null;
            }
            mensaje(sse, contexto, 15, "Se revisa la resolucion correctamente");

            mensaje(sse, contexto, 20, "Se solicita que la solicitud en ventas haya sido creada");
            if (empty(idSolicitud)) {
                sesion.actualizarEstadoError(ErroresBB.ETAPA_FINALIZAR);
                LogBB.error(contexto, ErroresBB.SOLICITUD_VACIA);
                error(sse, contexto, 21, "Solicitud de ventas vacia");
                enviarMailError(contexto, mandarMailError);
                return null;
            }
            mensaje(sse, contexto, 25, "Se revisa la existencia de la solicitud correctamente");

            mensaje(sse, contexto, 30, "Se solicita el agregado del integrante titular");

            if (!existeIntegranteByCuil(solicitudExistente, sesion.cuil)) {
                Integrante integrante = BBVentas.agregarIntegrante(contexto, idSolicitud, sesion.cuil, ErroresBB.INTEGRANTE_VACIO);
                if (integrante == null) {

                    if (sesion.esErrorFueraDeServicio(contexto)) {
                        return marcarBatchCorriendo(contexto, sse != null);
                    }

                    sesion.actualizarEstadoError(ErroresBB.ETAPA_FINALIZAR);
                    error(sse, contexto, 31, "Agregado de integrante titular no realizado");
                    enviarMailError(contexto, mandarMailError);
                    return null;
                }
            }

            mensaje(sse, contexto, 35, "Se agrega el integrante titular correctamente");

            if (sesion.casada()) {
                mensaje(sse, contexto, 36, "Se solicita el agregado del integrante cotitular");
                String cuilCotitular = sesion.conyuge.cuil;
                Integrante integrante = BBVentas.agregarIntegrante(contexto, idSolicitud, cuilCotitular, ErroresBB.INTEGRANTE_COTITULAR_VACIO);
                if (integrante == null) {

                    if (sesion.esErrorFueraDeServicio(contexto)) {
                        return marcarBatchCorriendo(contexto, sse != null);
                    }

                    sesion.actualizarEstadoError(ErroresBB.ETAPA_FINALIZAR);
                    error(sse, contexto, 37, "Agregado de integrante cotitular no realizado");
                    enviarMailError(contexto, mandarMailError);
                    return null;
                }
                mensaje(sse, contexto, 39, "Se agrega el integrante cotitular correctamente");
            }

            mensaje(sse, contexto, 40, "Se solicita la creacion del paquete con todos los productos nuevos");
            if (solicitudExistente.paquete() == null) {
                SolicitudPaquete paquete = BBVentas.crearPaqueteOptimizado(contexto, idSolicitud);
                if (paquete == null) {
                    sesion.actualizarEstadoError(ErroresBB.ETAPA_FINALIZAR);
                    error(sse, contexto, 61, "Creacion de paquete no realizado");
                    enviarMailError(contexto, mandarMailError);
                    return null;
                }
            }
            mensaje(sse, contexto, 65, "Se crea el paquete correctamente");

            mensaje(sse, contexto, 70, "Se solicita la resolucion para el paquete solicitado");

            Resolucion resolucionGet = ApiVentas.resolucionesGet(contexto, idSolicitud).tryGet();
            if (resolucionGet != null && resolucionGet.ResolucionId == null) {
                Resolucion resolucion = BBVentas.resolucionesPut(contexto, idSolicitud, SolicitudPaquete.TIPO_OPERACION_SOLICITUD);
                if (resolucion == null || !resolucion.aprobado()) {

                    if (sesion.esErrorFueraDeServicio(contexto)) {
                        return marcarBatchCorriendo(contexto, sse != null);
                    }

                    String explicacion = resolucion == null ? null : resolucion.Explicacion + resolucion.CodigoExplicacion;

                    sesion.actualizarEstadoError(ErroresBB.ETAPA_FINALIZAR);
                    LogBB.error(contexto, explicacion, resolucion);
                    BBVentas.guardarRechazoMotor(contexto, resolucion, true);
                    error(sse, contexto, 81, "Resolucion no aprobada o nula");
                    enviarMailError(contexto, mandarMailError);
                    return null;
                }
            }

            mensaje(sse, contexto, 85, "Se revisa la resolucion del paquete correctamente");
        }

        mensaje(sse, contexto, 90, "Se solicita la finalizacion de la solicitud de venta");
        Solicitud solicitud = BBVentas.finalizarSolicitud(contexto, idSolicitud);
        if (solicitud == null) {
            if (contexto.esHomologacion()) {
                LogBB.error(contexto, "PROCESO_VENTA", "error finalizar solicitud");
            }

            if (sesion.esErrorFueraDeServicio(contexto)) {
                return marcarBatchCorriendo(contexto, sse != null);
            }

            sesion.actualizarEstadoError(ErroresBB.ETAPA_FINALIZAR);
            error(sse, contexto, 91, "Finalizacion de solicitud de venta con error");
            enviarMailError(contexto, mandarMailError);
            return null;
        }
        mensaje(sse, contexto, 98, "Finaliza la venta optimizada");

        //enviarMailFelicitaciones(contexto, mandarMailFelicitaciones);

        mensaje(sse, contexto, 100, "Felicitaciones");

        sesion.actualizarEstado(EstadosBB.FINALIZAR_OK);
        String idCobis = solicitud.Integrantes.get(0).IdCobis;
        LogBB.evento(contexto, EstadosBB.FINALIZAR_OK, sesion.getCanal() + "_" + sesion.descripcionOferta(contexto));
        registrarSalesforce(contexto, contexto.config.bool("buhobank_prendido_salesforce_onboarding"),idCobis);

        altaCuentaInversorV2(contexto);
        validarDatosContacto(contexto);

        return respuesta();
    }

    public static void ventaTempranaPaquete(ContextoBB contexto) {
        try{
            if(contexto.sesion().getVentaTemprana()
                    || !contexto.sesion().getGuardarCobisTemprano()
                    || !contexto.sesion().esFlujoTcv()
                    || !BBPoliticas.estaEnHorario(contexto, 30)){
                return;
            }

            LogBB.eventoHomo(contexto, "inicio - venta paquete temprana");

            SesionBB sesion = contexto.sesion();
            String idSolicitud = sesion.idSolicitud;
            if (empty(idSolicitud) || !sesion.resolucionAprobada()) {
                return;
            }

            Solicitud solicitudExistente = ApiVentas.solicitud(contexto, idSolicitud).get();
            if(solicitudExistente == null){
                return;
            }

            if (!actualizarSolicitud(contexto, solicitudExistente)) {
                return;
            }

            if (!existeIntegranteByCuil(solicitudExistente, sesion.cuil)) {
                Integrante integrante = BBVentas.agregarIntegrante(contexto, idSolicitud, sesion.cuil, ErroresBB.INTEGRANTE_VACIO);
                if (integrante == null) {
                    return;
                }
            }

            if (solicitudExistente.paquete() == null) {
                SolicitudPaquete paquete = BBVentas.crearPaqueteOptimizado(contexto, idSolicitud);
                if (paquete == null) {
                    return;
                }
            }

            Resolucion resolucionGet = ApiVentas.resolucionesGet(contexto, idSolicitud).tryGet();
            if (resolucionGet != null && resolucionGet.ResolucionId == null) {
                BBVentas.resolucionesPut(contexto, idSolicitud, SolicitudPaquete.TIPO_OPERACION_SOLICITUD);
            }

            sesion.ventaTempranaOK();
            LogBB.eventoHomo(contexto, "finalizado - venta paquete temprana");
        }
        catch(Exception e){}
    }

    private static boolean actualizarSolicitud(ContextoBB contexto, Solicitud solicitud) {

        if (solicitud.DocumentacionEnContenedor == null || !solicitud.DocumentacionEnContenedor) {
            solicitud.TipoOperacion = "03";
            solicitud.TieneLegajoFisico = false;
            solicitud.OrigenDocumentacion = "digital";
            solicitud.DocumentacionEnContenedor = true;

            Solicitud solicitudActualizada = ApiVentas.actualizarSolicitud(contexto, solicitud).tryGet();
            if (solicitudActualizada == null) {
                return false;
            }
        }

        return true;
    }

    public static Object finalizarOptimizado(ContextoBB contexto, Boolean mandarMailError, Boolean mandarMailFelicitaciones) {
        return ServerSentEvents.crear(contexto, (contextoLocal, sse) -> {
            finalizarOptimizadoCore(sse, contextoLocal, mandarMailError, mandarMailFelicitaciones);
        });
    }

    public static Object errorFinalizar(ContextoBB contexto) {
        finalizarEjecucion(contexto, "errorFinalizar");
        return ServerSentEvents.crear(contexto, (contextoLocal, sse) -> {
            error(sse, contexto, 10, "Error al finalizar el alta");
        });
    }

    public static Object finalizarOk(ContextoBB contexto) {
        finalizarEjecucion(contexto, "finalizarOk");
        return ServerSentEvents.crear(contexto, (contextoLocal, sse) -> {
            mensaje(sse, contexto, 100, "Felicitaciones");
        });
    }

    public static Object batchCorriendoFinalizar(ContextoBB contexto) {
        finalizarEjecucion(contexto, "batchCorriendoFinalizar");
        return ServerSentEvents.crear(contexto, (contextoLocal, sse) -> {
            mensaje(sse, contexto, 100, EstadosBB.BATCH_CORRIENDO);
        });
    }

    public static Object errorFinalizarTransmit(ContextoBB contexto) {
        finalizarEjecucion(contexto, "errorFinalizarTransmit");
        return ServerSentEvents.crear(contexto, (contextoLocal, sse) -> {
            mensaje(sse, contexto, 10, ErroresBB.ERROR_DENY);
        });
    }

    public static Object errorFinalizarSinToken(ContextoBB contexto) {
        finalizarEjecucion(contexto, "errorFinalizarSinToken");
        return ServerSentEvents.crear(contexto, (contextoLocal, sse) -> {
            mensaje(sse, contexto, 10, ErroresBB.SIN_SESSION_TOKEN);
        });
    }

    private static void finalizarEjecucion(ContextoBB contexto, String mensaje){
        contexto.sesion().finalizarEnEjecucion = false;
        contexto.sesion().saveCache();
        LogBB.eventoHomo(contexto, "RESPONSE_FINALIZAR: " + mensaje);
    }

    public static Object finalizar(ContextoBB contexto) {
        return finalizar(contexto, true, false, true, false);
    }

    public static Object finalizar(ContextoBB contexto, Boolean conSse, Boolean mandarMailError, Boolean mandarMailFelicitaciones, Boolean forzarAlta) {
        SesionBB sesion = contexto.sesion();

        LogBB.evento(contexto, "REQUEST_FINALIZAR", sesion.token);

        if (contexto.config.bool("buhobank_prendido_modo_transaccional", false) && conSse) {
            try {
                String sessionToken = contexto.parametros.string("sessionToken", null);
                if (empty(sessionToken) && !contexto.esHomologacion()){
                    return errorFinalizarSinToken(contexto);
                }

                LogBB.evento(contexto, "INICIO_TRANSMIT_RECOMENDACION", "", contexto.idSesionTransmit());

                AuditLogReport auditLogReport = new AuditLogReport(
                        contexto.canal == null ? "" : contexto.canal,
                        contexto.subCanal() == null ? "" : contexto.subCanal(),
                        contexto.idSesion() == null ? "" : contexto.idSesion(),
                        contexto.idSesion() == null ? "" : contexto.idSesion(),
                        contexto.idSesion() == null ? "" : contexto.idSesion(),
                        contexto.config.string("backend_api_auditor"),
                        contexto.sesion().ip == null ? "": contexto.sesion().ip);

                OnboardingMBBMBankProcess onboardingBankProcess = new OnboardingMBBMBankProcess(
                        contexto.idSesionTransmit(),
                        contexto.sesion().resolucionMotorDeScoring == null ? "" : contexto.sesion().resolucionMotorDeScoring,
                        sessionToken);

                LogBB.evento(contexto, "TRANSMIT_resolucionMotorDeScoring", contexto.sesion().resolucionMotorDeScoring );

                RecommendationDTO recommendationDTO = TransmitBB.recomendacionOnboarding(contexto, onboardingBankProcess,auditLogReport);

                LogBB.evento(contexto, "FIN_TRANSMIT_RECOMENDACION", recommendationDTO != null ? recommendationDTO.getRecommendationType() : "", contexto.idSesionTransmit());

                if (recommendationDTO != null) {
                    LogBB.evento(contexto, "TRANSMIT_RESPONSE", recommendationDTO.toString() );
                    if (isErrorRecommendationOnboarding(recommendationDTO) || !recommendationDTO.getRecommendationType().equals(Transmit.ALLOW))
                        return errorFinalizarTransmit(contexto);
                } else
                    LogBB.evento(contexto, ErroresBB.ERROR_TRANSMIT, "", contexto.idSesionTransmit());

            } catch (Exception e) {
                LogBB.evento(contexto, ErroresBB.ERROR_TRANSMIT, e.toString(), contexto.idSesionTransmit());
            }
        }

        if (!sesion.resolucionAprobada() && sesion.getParamPrevencionStandalone(contexto)) {
            LogBB.evento(contexto, "ERROR_PREVENCION_STANDALONE");
            return errorFinalizar(contexto);
        }

        if (!sesion.tieneOtpValidado()) {
            LogBB.error(contexto, "ERROR_SIN_VALIDAR_OTP");
            sesion.actualizarEstado("ERROR_SIN_VALIDAR_OTP");
            return errorFinalizar(contexto);
        }

        if (conSse && sesion.finalizarEnEjecucion) {
            LogBB.evento(contexto, "FINALIZAR_EN_EJECUCION");
            return batchCorriendoFinalizar(contexto);
        }

        sesion.finalizarEnEjecucion = true;
        sesion.saveCache();

        if (EstadosBB.FINALIZAR_OK.equals(sesion.estado)) {
            LogBB.evento(contexto, "USUARIO_YA_FINALIZADO");

            if (conSse) {
                return finalizarOk(contexto);
            }

            return respuesta();
        }

        if (conSse && !forzarAlta && !BBPoliticas.estaEnHorario(contexto, GeneralBB.MINUTOS_ANTICIPACION_BATCH)) {
            LogBB.eventoHomo(contexto, "ESTA_EN_HORARIO_BATCH");
            return marcarBatchCorriendo(contexto, true);
        }

        if(!sesion.getGuardarCobisTemprano()){
            Objeto responseCobis = BBPersona.guardarCobis(contexto);
            if (!BBValidacion.estadoOk(responseCobis)) {
                if (sesion.esErrorFueraDeServicio(contexto)) {
                    return marcarBatchCorriendo(contexto, conSse);
                }

                sesion.actualizarEstadoError(ErroresBB.ETAPA_FINALIZAR);
                enviarMailError(contexto, mandarMailError);
                return errorFinalizar(contexto);
            }
        }

        LogBB.eventoHomo(contexto, "guardado cobis");

        SesionStandBy sesionStandBy = SqlEsales.sesionStandBy(contexto, sesion.cuil).tryGet();
        if (sesionStandBy != null) {

            if (SesionesStandBy.CONTROL_ERROR.equals(sesionStandBy.estado)) {
                sesion.actualizarEstadoError(ErroresBB.ETAPA_FINALIZAR);
                LogBB.error(contexto, ErroresBB.ERROR_USUARIO_STAND_BY, sesion.token);
                return errorFinalizar(contexto);
            }

            if (!SesionesStandBy.FLUJO_VU_OK.equals(sesionStandBy.estado) && !SesionesStandBy.CONTROL_OK.equals(sesionStandBy.estado)) {

                sesionStandBy.token_sesion = sesion.token;
                sesionStandBy.estado = SesionesStandBy.CONTROLAR;
                SqlEsales.actualizarSesionStandBy(contexto, sesionStandBy).tryGet();
                sesion.actualizarEstado(EstadosBB.CONTROLAR_USUARIO);
                LogBB.evento(contexto, GeneralBB.SESION_STAND_BY_CONTROLAR, sesion.token);
                return batchCorriendoFinalizar(contexto);
            }
        }

        if (empty(sesion.ofertaElegida)) {
            LogBB.evento(contexto, GeneralBB.ERROR_OFERTA_ELEGIDA_VACIA, sesion.token);
            sesion.ofertaElegida = GeneralBB.BUHO_INICIA.toString();
            sesion.subProducto = GeneralBB.SUB_BUHO_PUNTOS;
            sesion.saveSesion();
        }

        if (sesion.esStandalone()) {
            if (conSse) {
                LogBB.eventoHomo(contexto, "conSse prendido");
                return finalizarStandalone(contexto, mandarMailError, mandarMailFelicitaciones);
            }

            LogBB.eventoHomo(contexto, "conSse apagado");
            return finalizarStandaloneCore(null, contexto, mandarMailError, mandarMailFelicitaciones);
        }

        if (!contexto.prendidoOfertaMotor()) {
            return flujoVentasFinal(contexto, mandarMailError);
        }

        if (conSse) {
            LogBB.eventoHomo(contexto, "conSse prendido");
            return finalizarOptimizado(contexto, mandarMailError, mandarMailFelicitaciones);
        }

        LogBB.eventoHomo(contexto, "conSse apagado");
        return finalizarOptimizadoCore(null, contexto, mandarMailError, mandarMailFelicitaciones);
    }

    static Object marcarBatchCorriendo(ContextoBB contexto, Boolean conSse) {

        SesionBB sesion = contexto.sesion();

        Fecha fechaDesde = Fecha.ahora().restarDias(GeneralBB.DIAS_ALTA_AUTOMATICA);
        SesionesEsales sesionBatchCorriendo = SqlEsales.obtenerSesionByEstado(contexto, sesion.cuil, EstadosBB.BATCH_CORRIENDO, fechaDesde).tryGet();
        if (sesionBatchCorriendo == null || sesionBatchCorriendo.size() == 0) {

            if (conSse && !empty(sesion.mail)) {
                //enviarEmail(contexto, BBPlantillasDopplerBuhobank.TIPO_AVISO_ALTA_BATCH, GeneralBB.ENVIO_MAIL_AVISO_BATCH);
            }

            sesion.actualizarEstado(EstadosBB.BATCH_CORRIENDO);
            LogBB.evento(contexto, EstadosBB.BATCH_CORRIENDO, sesion.token);
            return batchCorriendoFinalizar(contexto);
        }

        if (sesionBatchCorriendo.size() == 1 && sesion.token.equals(sesionBatchCorriendo.get(0).token)) {
            LogBB.evento(contexto, EstadosBB.BATCH_CORRIENDO, sesion.token);
        } else {
            sesion.actualizarEstado(EstadosBB.PREVIO_BATCH_CORRIENDO);
            LogBB.evento(contexto, EstadosBB.PREVIO_BATCH_CORRIENDO, sesion.token);
        }

        return batchCorriendoFinalizar(contexto);
    }

    public static void finalizarPendientes(ContextoBB contexto) {
        logProceso(contexto, "PROCESO_BATCH. Inicio proceso de alta automatica.");

        Integer count = 0;
        SesionesEsales sesionesEsales = null;
        do {

            Fecha fechaDesde = Fecha.ahora().restarDias(GeneralBB.DIAS_ALTA_AUTOMATICA);
            sesionesEsales = SqlEsales.sesionesPendientes(contexto, EstadosBB.BATCH_CORRIENDO, fechaDesde).tryGet();
            if (sesionesEsales != null && sesionesEsales.size() > 0) {
                logProceso(contexto, "PROCESO_BATCH. Nro de sesiones pendientes: " + sesionesEsales.size());
                finalizarSesionesPendientes(contexto, sesionesEsales, false);
            }

            count++;

        } while (count < 3 && sesionesEsales != null && sesionesEsales.size() > 0);

        logProceso(contexto, "PROCESO_BATCH. Finalizo proceso de alta automatica. " + count);

    }

    public static void finalizarPendiente(ContextoBB contexto, String cuil, Boolean forzarAlta) {

        Fecha fechaDesde = Fecha.ahora().restarDias(GeneralBB.DIAS_ALTA_AUTOMATICA);
        SesionesEsales sesionesEsales = SqlEsales.sesionPendiente(contexto, cuil, EstadosBB.BATCH_CORRIENDO, fechaDesde).tryGet();
        if (sesionesEsales == null || sesionesEsales.size() == 0) {
            return;
        }

        logProceso(contexto, "PROCESO_BATCH. Nro de sesiones pendientes: " + sesionesEsales.size());
        finalizarSesionesPendientes(contexto, sesionesEsales, forzarAlta);
    }

	static void registrarSalesforce(ContextoBB contexto, boolean prendidoSalesforce,String idCobis) {

		try {

            //boolean prendidoSalesforce = HBSalesforce.prendidoSalesforce(contexto.sesion().idCobis);

            if (prendidoSalesforce) {
                Objeto parametros = new Objeto();
                parametros.set("Subject", "Ingreso a BH");

                String nombre = contexto.sesion().nombre;
                String apellido = contexto.sesion().apellido;
                parametros.set("NOMBRE", nombre);
                parametros.set("APELLIDO", apellido);
                Date hoy = new Date();
                parametros.set("FECHA", new SimpleDateFormat("dd/MM/yyyy").format(hoy));
                parametros.set("HORA", new SimpleDateFormat("hh:mm a").format(hoy));
                parametros.set("CANAL", "BuhoBank");
                parametros.set("TITULAR_CANAL", apellido);
                SesionBB.DomicilioBB domicilioLegalObject = contexto.sesion().domicilioLegal;
                SesionBB.DomicilioBB domicilioPostalObject = contexto.sesion().domicilioPostal;

                String domicilioLegal = !empty(domicilioLegalObject.calle) ? domicilioLegalObject.calle : "";
                domicilioLegal += !empty(domicilioLegalObject.numeroCalle) ? " " + domicilioLegalObject.numeroCalle : "";
                domicilioLegal += !empty(domicilioLegalObject.piso) ? ", Piso " + domicilioLegalObject.piso : "";
                domicilioLegal += !empty(domicilioLegalObject.dpto) ? " Dpto " + domicilioLegalObject.dpto : "";
                domicilioLegal += !empty(domicilioLegalObject.cp) ? ", CP " + domicilioLegalObject.cp : "";
                domicilioLegal += !empty(domicilioLegalObject.ciudad) ? ", " + domicilioLegalObject.ciudad : "";
                domicilioLegal += !empty(domicilioLegalObject.provincia) ? ", " + domicilioLegalObject.provincia : "";
                domicilioLegal += !empty(domicilioLegalObject.pais) ? ", " + domicilioLegalObject.pais : "";

                String domicilioPostal = !empty(domicilioPostalObject.calle) ? domicilioPostalObject.calle : "";
                domicilioPostal += !empty(domicilioPostalObject.numeroCalle) ? " " + domicilioPostalObject.numeroCalle : "";
                domicilioPostal += !empty(domicilioPostalObject.piso) ? ", Piso " + domicilioPostalObject.piso : "";
                domicilioPostal += !empty(domicilioPostalObject.dpto) ? " Dpto " + domicilioPostalObject.dpto : "";
                domicilioPostal += !empty(domicilioPostalObject.cp) ? ", CP " + domicilioPostalObject.cp : "";
                domicilioPostal += !empty(domicilioPostalObject.ciudad) ? ", " + domicilioPostalObject.ciudad : "";
                domicilioPostal += !empty(domicilioPostalObject.provincia) ? ", " + domicilioPostalObject.provincia : "";
                domicilioPostal += !empty(domicilioPostalObject.pais) ? ", " + domicilioPostalObject.pais : "";

                SesionBB.ConyugeBB conyugeObject = contexto.sesion().conyuge;
                String conyuge = "";
                if(conyugeObject != null && conyugeObject.numeroDocumento != null) {
                    conyuge = !empty(conyugeObject.nombres) ? conyugeObject.nombres : "";
                    conyuge += !empty(conyugeObject.apellido) ? " " + conyugeObject.apellido : "";
                    conyuge += !empty(conyugeObject.genero) ? " | Género: " + conyugeObject.genero : "";
                    conyuge += !empty(conyugeObject.numeroDocumento) ? " | DNI: " + conyugeObject.numeroDocumento : "";
                    conyuge += !empty(conyugeObject.cuil) ? " | CUIL: " + conyugeObject.cuil : "";
                    conyuge += !empty(conyugeObject.nacionalidad) ? " | Nacionalidad: " + conyugeObject.nacionalidad : "";
                    conyuge += !empty(conyugeObject.paisResidencia) ? " | País de Residencia: " + conyugeObject.paisResidencia : "";
                }
                parametros.set("idcobis", idCobis);//1803121
                parametros.set("ofertaElegida", contexto.sesion().ofertaElegida);
                parametros.set("subProducto", contexto.sesion().subProducto);
                parametros.set("estado", contexto.sesion().estado);
                parametros.set("aceptartyc", contexto.sesion().aceptartyc);
                parametros.set("tdFisica", contexto.sesion().tdFisica);
                parametros.set("idSolicitud", contexto.sesion().idSolicitud);
                parametros.set("idTarjeta", contexto.sesion().idTarjeta);
                parametros.set("letraTC", contexto.sesion().letraTC);
                parametros.set("limite", contexto.sesion().limite);
                parametros.set("resolucionMotorDeScoring", contexto.sesion().resolucionMotorDeScoring);
                parametros.set("modoAprobacion", contexto.sesion().modoAprobacion);
                parametros.set("codigoPaqueteMotor", contexto.sesion().codigoPaqueteMotor);
                parametros.set("mail", contexto.sesion().mail);
                parametros.set("codArea", contexto.sesion().codArea);
                parametros.set("celular", contexto.sesion().celular);
                parametros.set("nombre", contexto.sesion().nombre);
                parametros.set("apellido", contexto.sesion().apellido);
                parametros.set("genero", contexto.sesion().genero);
                parametros.set("fechaNacimiento", contexto.sesion().fechaNacimiento.string("yyyy-MM-dd", null));
                parametros.set("nacionalidad", contexto.sesion().nacionalidad);
                parametros.set("idNacionalidad", contexto.sesion().idNacionalidad);
                parametros.set("paisNacimiento", contexto.sesion().paisNacimiento);
                parametros.set("idPaisNacimiento", contexto.sesion().idPaisNacimiento);
                parametros.set("idTipoIDTributario", contexto.sesion().idTipoIDTributario);
                parametros.set("ejemplar", contexto.sesion().ejemplar);
                parametros.set("domicilioLegal", domicilioLegal);
                parametros.set("domicilioPostal", domicilioPostal);
                parametros.set("formaEntrega", contexto.sesion().formaEntrega);
                parametros.set("idSucursal", contexto.sesion().idSucursal);
                parametros.set("idEstadoCivil", contexto.sesion().idEstadoCivil);
                parametros.set("idSituacionLaboral", contexto.sesion().idSituacionLaboral);
                parametros.set("idCantidadNupcias", contexto.sesion().idCantidadNupcias);
                parametros.set("tipoSitLaboral", contexto.sesion().tipoSitLaboral);
                parametros.set("conyuge", conyuge);
                parametros.set("telefonoOtpValidado", contexto.sesion().telefonoOtpValidado);
                parametros.set("plataforma", contexto.sesion().plataforma);
                parametros.set("bbInversorAceptada", contexto.sesion().bbInversorAceptada);
                parametros.set("esExpuestaPolitica", contexto.sesion().esExpuestaPolitica);
                parametros.set("esSujetoObligado", contexto.sesion().esSujetoObligado);
                parametros.set("esFatcaOcde", contexto.sesion().esFatcaOcde);
                parametros.set("lavadoDinero", contexto.sesion().lavadoDinero);
                parametros.set("versionPlataforma", contexto.sesion().versionPlataforma);
                parametros.set("tdVirtual", contexto.sesion().tdVirtual);
                parametros.set("tcVirtual", contexto.sesion().esTcVirtual());
                parametros.set("atFlujoName", contexto.sesion().sucursalOnboarding);
                parametros.set("solicitoImpresion", contexto.sesion().solicitoImpresion());
                parametros.set("enviarOfertaPaquete", contexto.sesion().enviarOfertaPaquete());
                String salesforce_login = contexto.config.string("buhobank_salesforce_onboarding");
                new Futuro<>(() -> BBSalesforce.registrarEventoSalesforce(contexto, salesforce_login, parametros,idCobis));
            }
        }catch (Exception e){
            LogBB.evento(contexto, "ERROR_SALESFORCE", e.getMessage());
        }
	}

    private static void finalizarSesionesPendientes(ContextoBB contexto, SesionesEsales sesionesEsales, Boolean forzarAlta) {

        Integer count = 0;
        for (SesionEsales sesionEsales : sesionesEsales) {

            count++;

            if (!contexto.esHomologacion() && !forzarAlta) {
                if (Fecha.ahora().restarHoras(1).esAnterior(sesionEsales.fecha_ultima_modificacion)) {
                    continue;
                }
            }

            SesionStandBy sesionStandBy = SqlEsales.sesionStandBy(contexto, sesionEsales.cuil).tryGet();
            if (sesionStandBy != null && SesionesStandBy.CONTROLAR.equals(sesionStandBy.estado)) {
                SqlEsales.actualizarEstadoByToken(contexto, sesionEsales.token, EstadosBB.CONTROLAR_USUARIO).tryGet();
                LogBB.evento(contexto, GeneralBB.SESION_STAND_BY_CONTROLAR, sesionEsales.token);
                continue;
            }

            Fecha fechaDesde = Fecha.ahora().restarDias(10);
            if(!contexto.esHomologacion()){
                LogsBuhoBank logEsInformado = SqlBuhoBank.captarAbandono(contexto, sesionEsales.cuil, EstadosBB.BB_ES_INFORMADO, fechaDesde).tryGet();
                if (logEsInformado != null && logEsInformado.size() > 0) {
                    Integer countInformado = 0;
                    for (LogBuhoBank log : logEsInformado) {
                        if (empty(log.endpoint)) {
                            countInformado++;
                        }
                    }

                    if (countInformado >= 3) {
                        SqlEsales.actualizarEstadoByToken(contexto, sesionEsales.token, EstadosBB.ERROR_FINALIZAR).tryGet();
                        continue;
                    }
                }
            }

            SesionBB sesion = SqlEsales.retomarSesion(contexto, sesionEsales).tryGet();
            if (sesion == null) {
                SqlEsales.actualizarEstadoByToken(contexto, sesionEsales.token, "ERROR_" + ErroresBB.ETAPA_FINALIZAR).tryGet();
                LogBB.evento(contexto, GeneralBB.ERROR_RETOMAR_SESION, sesionEsales.token);
                continue;
            }

            logProceso(contexto, "PROCESO_BATCH. sesion retomada. cuil: " + sesionEsales.cuil);

            Boolean usuarioFinalizado = false;

            try {

                Object respuesta = finalizar(contexto, false, false, false, forzarAlta);

                if (!EstadosBB.BATCH_CORRIENDO.equals(contexto.sesion().estado)) {

                    if (respuesta != null && respuesta.toString().equals(respuesta().toString())) {
                        usuarioFinalizado = true;
                        //enviarEmail(contexto, BBPlantillasDopplerBuhobank.TIPO_ALTA_BATCH_OK, GeneralBB.ENVIO_MAIL_OK_ALTA_BATCH);

                    } else {

                        LogsBuhoBank logEmailEror = SqlBuhoBank.captarAbandono(contexto, sesionEsales.cuil, "BB_ERROR_" + GeneralBB.ENVIO_MAIL_ERROR_ALTA_BATCH, fechaDesde).tryGet();
                        if (logEmailEror != null && logEmailEror.size() > 0) {
                            continue;
                        }

                        //enviarEmail(contexto, BBPlantillasDopplerBuhobank.TIPO_ALTA_BATCH_ERROR, GeneralBB.ENVIO_MAIL_ERROR_ALTA_BATCH);
                    }
                }

            } catch (ApiException e) {

                SqlEsales.actualizarEstadoByToken(contexto, sesionEsales.token, "ERROR_" + ErroresBB.ETAPA_FINALIZAR).tryGet();
                LogBB.evento(contexto, ErroresBB.ERROR_PROCESO_BATCH, e.toString(), sesionEsales.cuil);
            } catch (Exception e) {

                SqlEsales.actualizarEstadoByToken(contexto, sesionEsales.token, "ERROR_" + ErroresBB.ETAPA_FINALIZAR).tryGet();
                LogBB.evento(contexto, ErroresBB.ERROR_PROCESO_BATCH, e.toString(), sesionEsales.cuil);
            }

            try {

                if (usuarioFinalizado) {
                    BBAltaProducFinal.altaOtrosProductos(contexto, null);
                }
            } catch (Exception e) {

            }

        }

        logProceso(contexto, "PROCESO_BATCH. Nro de sesiones depuradas: " + count);

    }

    private static Boolean enviarEmail(ContextoBB contexto, String tipoPlantilla, String eventoEnvio) {
        SesionBB sesion = contexto.sesion();
        BBPlantillasDopplerBuhobank plantillas = SqlBuhoBank.obtenerPlantillasDoppler(contexto, sesion.getFlujo()).tryGet();
        BBPlantillaDopplerBuhobank plantilla = BBPlantillasDopplerBuhobank.buscarTipo(plantillas, tipoPlantilla);

        if (empty(sesion.mail) || empty(plantilla) || empty(plantilla.codigo) || empty(plantilla.asunto)) {
            LogBB.evento(contexto, "ERROR_" + eventoEnvio, " mail:" + sesion.mail + " |tipo_plantilla:" + tipoPlantilla + " |codigo_plantilla:" + plantilla.codigo + " |asunto:" + plantilla.asunto);
            return false;
        }

        EnvioEmail envioMail = ApiNotificaciones.postEnvioBB(contexto, plantilla.asunto, plantilla.codigo, sesion.mail, GeneralBB.URL_GENERAR_CLAVE_USUARIO).tryGet();
        if (envioMail == null) {
            LogBB.evento(contexto, "ERROR_" + eventoEnvio, " mail:" + sesion.mail + " |tipo_plantilla:" + tipoPlantilla + " |codigo_plantilla:" + plantilla.codigo + " |asunto:" + plantilla.asunto);
            return false;
        }

        return true;
    }

    public static Object finalizarPendientesBatch(ContextoBB contexto) {

        String cuil = contexto.parametros.string("cuil", null);
        Boolean forzarAlta = contexto.parametros.bool("forzar_alta", false);

        if (empty(cuil)) {
            finalizarPendientes(contexto);
        } else {
            finalizarPendiente(contexto, cuil, forzarAlta);
        }

        return respuesta();
    }

    public static Boolean existeIntegranteByCuil(Solicitud solicitud, String cuil) {

        if (solicitud == null) {
            return false;
        }

        for (RolIntegrante integrante : solicitud.Integrantes) {
            if (cuil.equals(integrante.NumeroDocumentoTributario)) {
                return true;
            }
        }

        return false;
    }

    public static Object obtenerSesiones(ContextoBB contexto) {

        String cuil = contexto.parametros.string("cuil", null);
        String fechaDesdeStr = contexto.parametros.string("fechaDesde", null);
        Boolean full = contexto.parametros.bool("full", false);

        Fecha fechaDesde = Fecha.ahora().restarDias(200);
        if (!empty(fechaDesdeStr)) {
            fechaDesde = new Fecha(fechaDesdeStr, GeneralBB.FORMATO_FECHA);
        }

        if (empty(cuil)) {
            return respuesta("ERROR_CUIL_VACIO");
        }

        if (full) {
            return obtenerSesionesFull(contexto, cuil, fechaDesde);
        }

        SesionesEsales sesionesEsales = SqlEsales.obtenerSesion(contexto, cuil, fechaDesde).tryGet();
        if (sesionesEsales == null || sesionesEsales.size() == 0) {
            return respuesta("SIN_SESION");
        }

        Objeto respuesta = respuesta();
        respuesta.set("total", sesionesEsales.size());
        respuesta.set("finalizo", false);

        Objeto respuestaSesiones = respuesta.set("sesiones");

        for (SesionEsales sesionEsales : sesionesEsales) {

            Objeto sesion = respuestaSesiones.add();
            sesion.set("token_sesion", sesionEsales.token);
            sesion.set("fecha_inicio", sesionEsales.fecha_inicio);
            sesion.set("estado", sesionEsales.estado);
            sesion.set("ultima_fecha_modificacion", sesionEsales.fecha_ultima_modificacion);

            SesionEsalesBB2 sesionEsalesBB2 = SqlEsales.sesionEsalesBB2(contexto, sesionEsales.id).tryGet();
            if (sesionEsalesBB2 != null) {
                sesion.set("usuario_vu", sesionEsalesBB2.usuario_vu);
            }

            if (EstadosBB.FINALIZAR_OK.equals(sesionEsales.estado)) {
                respuesta.set("finalizo", true);
            }
        }

        return respuesta;
    }

    public static Object obtenerSesionesFull(ContextoBB contexto, String cuil, Fecha fechaDesde) {

        SesionesEsales sesionesEsales = SqlEsales.obtenerSesion(contexto, cuil, fechaDesde).tryGet();

        Objeto respuesta = respuesta();
        respuesta.set("total", sesionesEsales.size());
        respuesta.set("finalizo", false);

        Objeto respuestaSesiones = respuesta.set("sesiones");

        for (SesionEsales sesionEsales : sesionesEsales) {

            Objeto sesion = respuestaSesiones.add();
            sesion.set("sesion_esales", sesionEsales);

            SesionEsalesBB2 sesionEsalesBB2 = SqlEsales.sesionEsalesBB2(contexto, sesionEsales.id).tryGet();
            if (sesionEsalesBB2 != null) {
                sesion.set("sesion_esales_bb2", sesionEsalesBB2);
            }

            if (EstadosBB.FINALIZAR_OK.equals(sesionEsales.estado)) {
                respuesta.set("finalizo", true);
            }
        }

        return respuesta;
    }

    public static Objeto marcarBatch(ContextoBB contexto) {

        String tokenSesion = contexto.parametros.string("token_sesion", null);
        String cuil = contexto.parametros.string("cuil", null);
        Objeto cuils = contexto.parametros.objeto("cuils", null);

        if (empty(tokenSesion) && empty(cuil) && empty(cuils)) {
            return respuesta("PARAMETROS_VACIOS");
        }

        if (cuils != null) {

            List<Object> cuilsAux = cuils.toList();
            List<String> cuilsError = new ArrayList<String>();
            List<String> cuilsOk = new ArrayList<String>();

            for (Object cuilAux : cuilsAux) {

                String cuilAuxStr = (String) cuilAux;
                String tokenFinalizar = buscarTokenSesionFinalizar(contexto, cuilAuxStr);
                Objeto respuesta = marcarBatchByToken(contexto, tokenFinalizar);
                if (respuesta.get("estado").equals("0")) {
                    cuilsOk.add(cuilAuxStr);
                } else {
                    cuilsError.add(cuilAuxStr);
                }
            }

            Objeto respuesta = respuesta();
            respuesta.set("total_cuils_ok", cuilsOk.size());
            respuesta.set("cuils_ok", cuilsOk);
            respuesta.set("total_cuils_error", cuilsError.size());
            respuesta.set("cuils_rror", cuilsError);

            return respuesta;
        }

        String tokenFinalizar = buscarTokenSesionFinalizar(contexto, cuil);
        if (tokenFinalizar != null) {
            tokenSesion = tokenFinalizar;
        }

        return marcarBatchByToken(contexto, tokenSesion);
    }

    private static String buscarTokenSesionFinalizar(ContextoBB contexto, String cuil) {

        if (empty(cuil)) {
            return null;
        }

        Fecha fechaDesde = Fecha.ahora().restarDias(GeneralBB.DIAS_ALTA_AUTOMATICA);
        SesionesEsales sesionesEsales = SqlEsales.obtenerSesion(contexto, cuil, fechaDesde).tryGet();

        for (SesionEsales sesion : sesionesEsales) {

            if (!empty(sesion.nombre) && !empty(sesion.tipo_standalone) && !empty(sesion.telefono_celular_nro) && !empty(sesion.estado_civil_id) && !empty(sesion.resolucion_scoring) && !empty(sesion.dom_cp_envio)) {
                return sesion.token;
            }
        }

        return null;
    }

    private static Objeto marcarBatchByToken(ContextoBB contexto, String tokenSesion) {

        if (empty(tokenSesion)) {
            return respuesta("TOKEN_VACIO");
        }

        SesionEsales sesion = SqlEsales.sesionEsales(contexto, tokenSesion).tryGet();

        SesionStandBy sesionStandBy = SqlEsales.sesionStandBy(contexto, sesion.cuil).tryGet();
        if (sesionStandBy != null && !SesionesStandBy.CONTROL_OK.equals(sesionStandBy.estado) && !SesionesStandBy.FLUJO_VU_OK.equals(sesionStandBy.estado)) {
            return respuesta("CONTROLAR_USUARIO");
        }

        if (sesion == null || empty(sesion.tipo_standalone) || empty(sesion.situacion_laboral_id)) {
            return respuesta("SIN_SESION_FINALIZADA");
        }

        if (!SqlEsales.actualizarEstadoByToken(contexto, tokenSesion, EstadosBB.BATCH_CORRIENDO).tryGet()) {
            return respuesta("ERROR_ACTUALIZAR_ESTADO");
        }

        LogBB.evento(contexto, GeneralBB.TELEMARKETING_MANUAL, tokenSesion, sesion.cuil);

        return respuesta();
    }

    public static Object obtenerSesionesStandByControlar(ContextoBB contexto) {

        String estado = contexto.parametros.string("estado", null);
        String fechaDesdeStr = contexto.parametros.string("fechaDesde", null);
        String fechaHastaStr = contexto.parametros.string("fechaHasta", null);

        Fecha fechaDesde = getFechaDesde(fechaDesdeStr, fechaHastaStr);
        Fecha fechaHasta = getFechaHasta(fechaDesdeStr, fechaHastaStr);

        if (empty(fechaDesde) || empty(fechaHasta) || fechaHasta.esAnterior(fechaDesde)) {
            return respuesta("PARAMETROS_INCORRECTOS");
        }

        SesionesStandBy sesionesStandBy = SqlEsales.sesionesByEstado(contexto, estado, fechaDesde, fechaHasta).tryGet();
        if (sesionesStandBy == null || sesionesStandBy.size() == 0) {
            return respuesta("ERROR_SIN_SESION");
        }

        Objeto respuesta = respuesta();

        respuesta.set("fechaDesde", fechaDesde);
        respuesta.set("fechaHasta", fechaHasta);
        respuesta.set("total", sesionesStandBy.size());

        Objeto respuestaSesiones = respuesta.set("sesiones");

        for (SesionStandBy sesionStandBy : sesionesStandBy) {

            Objeto sesion = respuestaSesiones.add();
            sesion.set("cuil", sesionStandBy.cuil);
            sesion.set("estado", sesionStandBy.estado);
            sesion.set("token_sesion", sesionStandBy.token_sesion);

            SesionEsales ultimaSesionEsales = ultimaSesionEsales(contexto, sesionStandBy.cuil, sesionStandBy.token_sesion);
            if (ultimaSesionEsales != null) {
                sesion.set("estado_sesion", ultimaSesionEsales.estado);
                sesion.set("fecha_inicio_sesion", ultimaSesionEsales.fecha_inicio);
                sesion.set("fecha_ultimo_modificacion_sesion", ultimaSesionEsales.fecha_ultima_modificacion);
            } else {
                sesion.set("estado_sesion", null);
                sesion.set("fecha_inicio_sesion", null);
                sesion.set("fecha_ultimo_modificacion_sesion", null);
            }

            SesionEsales sesionEsales = obtenerDatosDeContacto(contexto, sesionStandBy.cuil);
            if (sesionEsales != null) {
                sesion.set("cod_area", BBPersona.quitarPrimerosCeros(sesionEsales.telefono_celular_ddn));
                sesion.set("celular", sesionEsales.telefono_celular_caract + sesionEsales.telefono_celular_nro);
                sesion.set("mail", sesionEsales.mail);
            } else {
                sesion.set("cod_area", null);
                sesion.set("celular", null);
                sesion.set("mail", null);
            }

            sesion.set("usuario_admin", sesionStandBy.usuario_admin);
            sesion.set("score", sesionStandBy.score);
            sesion.set("fecha_ultima_modificacion", sesionStandBy.fecha_ultima_modificacion);
        }

        return respuesta;
    }

    private static SesionEsales ultimaSesionEsales(ContextoBB contexto, String cuil, String tokenSesion) {

        Fecha fechaDesde = Fecha.ahora().restarDias(60);
        SesionesEsales sesionesEsales = SqlEsales.obtenerSesion(contexto, cuil, fechaDesde).tryGet();
        if (sesionesEsales == null || sesionesEsales.size() == 0) {
            return null;
        }

        SesionEsales sesionBatch = null;
        SesionEsales sesionPorToken = null;

        for (SesionEsales sesionEsales : sesionesEsales) {

            if (EstadosBB.FINALIZAR_OK.equals(sesionEsales.estado)) {
                return sesionEsales;
            }

            if (EstadosBB.BATCH_CORRIENDO.equals(sesionEsales.estado)) {
                sesionBatch = sesionEsales;
            }

            if (tokenSesion != null && sesionEsales.token.equals(tokenSesion)) {
                sesionPorToken = sesionEsales;
            }
        }

        if (sesionBatch != null) {
            return sesionBatch;
        }

        if (sesionPorToken != null) {
            return sesionPorToken;
        }

        return sesionesEsales.get(0);
    }

    static Fecha getFechaDesde(String fechaDesdeStr, String fechaHastaStr) {

        if (empty(fechaDesdeStr) && !empty(fechaHastaStr)) {
            return null;
        }

        if (empty(fechaDesdeStr) && empty(fechaHastaStr)) {
            return Fecha.hoy();
        }

        return new Fecha(fechaDesdeStr, "yyyy-MM-dd");
    }

    static Fecha getFechaHasta(String fechaDesdeStr, String fechaHastaStr) {

        if (empty(fechaDesdeStr) && !empty(fechaHastaStr)) {
            return null;
        }

        if (empty(fechaDesdeStr) && empty(fechaHastaStr)) {
            return Fecha.hoy().sumarDias(1);
        }

        if (!empty(fechaDesdeStr) && empty(fechaHastaStr)) {
            Fecha fechaDesde = new Fecha(fechaDesdeStr, "yyyy-MM-dd");
            return fechaDesde.sumarDias(90);
        }

        return new Fecha(fechaHastaStr, "yyyy-MM-dd");
    }

    private static SesionEsales obtenerDatosDeContacto(ContextoBB contexto, String cuil) {

        Fecha fechaDesde = Fecha.ahora().restarDias(60);
        SesionesEsales sesionesEsales = SqlEsales.obtenerSesion(contexto, cuil, fechaDesde).tryGet();

        for (SesionEsales sesionEsales : sesionesEsales) {

            if (!empty(sesionEsales.mail) && !empty(sesionEsales.telefono_celular_ddn) && !empty(sesionEsales.telefono_celular_caract) && !empty(sesionEsales.telefono_celular_nro)) {
                return sesionEsales;
            }
        }

        return null;
    }

    public static Object cambiarEstadoSesionStandBy(ContextoBB contexto) {

        String cuil = contexto.parametros.string("cuil", null);
        String estado = contexto.parametros.string("estado", null);
        String score = contexto.parametros.string("score", null);

        if (empty(estado) || empty(cuil)) {
            return respuesta("PARAMETROS_INCORRECTOS");
        }

        if (!SesionesStandBy.CONTROL_OK.equals(estado) && !SesionesStandBy.CONTROL_ERROR.equals(estado) && !SesionesStandBy.BORRAR_CONTROL.equals(estado)) {
            return respuesta("PARAMETROS_INCORRECTOS");
        }

        SesionStandBy sesionStandBy = SqlEsales.sesionStandBy(contexto, cuil).tryGet();
        if (sesionStandBy == null) {
            return respuesta("ERROR_SIN_SESION");
        }

        if (SesionesStandBy.BORRAR_CONTROL.equals(estado)) {

            Fecha fechaDesde = Fecha.ahora().restarDias(GeneralBB.DIAS_RETOMAR_SESION);
            SesionesEsales sesiones = SqlEsales.obtenerSesion(contexto, cuil, fechaDesde).tryGet();
            if (sesiones == null || sesiones.size() == 0) {

                SqlEsales.borrarSesionStandBy(contexto, cuil).tryGet();
                LogBB.evento(contexto, GeneralBB.SESION_STAND_BY_BORRADA, null, cuil);
                return respuesta();
            } else {
                return respuesta("TIENE_SESION_ACTIVA");
            }
        }

        sesionStandBy.estado = estado;
        sesionStandBy.score = score;
        sesionStandBy.usuario_admin = BBSeguridad.getUsuarioJWT(contexto);
        if (!SqlEsales.actualizarSesionStandBy(contexto, sesionStandBy).tryGet()) {
            return respuesta("ERROR_ACTUALIZAR_ESTADO");
        }

        if (SesionesStandBy.CONTROL_OK.equals(estado)) {

            LogBB.evento(contexto, GeneralBB.SESION_STAND_BY_CONTROL_OK, sesionStandBy.token_sesion, cuil);
            if (!empty(sesionStandBy.token_sesion)) {
                SqlEsales.actualizarEstadoByToken(contexto, sesionStandBy.token_sesion, EstadosBB.BATCH_CORRIENDO).tryGet();
            }
        } else if (SesionesStandBy.CONTROL_ERROR.equals(estado)) {

            LogBB.evento(contexto, GeneralBB.SESION_STAND_BY_CONTROL_ERROR, sesionStandBy.token_sesion, cuil);
            if (!empty(sesionStandBy.token_sesion)) {
                SqlEsales.actualizarEstadoByToken(contexto, sesionStandBy.token_sesion, EstadosBB.CONTROL_ERROR).tryGet();
            }
        }

        return respuesta();
    }

    public static Object obtenerReporteSesionesStandBy(ContextoBB contexto) {

        String fechaDesdeStr = contexto.parametros.string("fechaDesde", null);
        String fechaHastaStr = contexto.parametros.string("fechaHasta", null);

        Fecha fechaDesde = getFechaDesde(fechaDesdeStr, fechaHastaStr);
        Fecha fechaHasta = getFechaHasta(fechaDesdeStr, fechaHastaStr);

        if (empty(fechaDesde) || empty(fechaHasta) || fechaHasta.esAnterior(fechaDesde)) {
            return respuesta("PARAMETROS_INCORRECTOS");
        }

        SesionesStandBy sesionesStandBy = SqlEsales.sesionesByEstado(contexto, null, fechaDesde, fechaHasta).tryGet();
        if (sesionesStandBy == null || sesionesStandBy.size() == 0) {
            return respuesta("ERROR_SIN_SESION");
        }

        Objeto respuesta = respuesta();

        Integer countControlar = 0;
        Integer countVUok = 0;
        Integer countControlOK = 0;
        Integer countControlError = 0;

        Integer countFinalizados = 0;
        Integer countBatchCorriendo = 0;
        Integer countControlarUsuario = 0;
        Integer countError = 0;
        Integer countAbandono = 0;

        for (SesionStandBy sesionStandBy : sesionesStandBy) {

            if (SesionesStandBy.CONTROLAR.equals(sesionStandBy.estado)) {
                countControlar++;
            } else if (SesionesStandBy.FLUJO_VU_OK.equals(sesionStandBy.estado)) {
                countVUok++;
            } else if (SesionesStandBy.CONTROL_OK.equals(sesionStandBy.estado)) {
                countControlOK++;
            } else if (SesionesStandBy.CONTROL_ERROR.equals(sesionStandBy.estado)) {
                countControlError++;
            }

            SesionEsales ultimaSesionEsales = ultimaSesionEsales(contexto, sesionStandBy.cuil, sesionStandBy.token_sesion);
            if (ultimaSesionEsales != null) {

                if (EstadosBB.FINALIZAR_OK.equals(ultimaSesionEsales.estado)) {
                    countFinalizados++;
                } else if (EstadosBB.BATCH_CORRIENDO.equals(ultimaSesionEsales.estado)) {
                    countBatchCorriendo++;
                } else if (EstadosBB.CONTROLAR_USUARIO.equals(ultimaSesionEsales.estado)) {
                    countControlarUsuario++;
                } else if (!empty(ultimaSesionEsales.estado) && ultimaSesionEsales.estado.contains("ERROR")) {
                    countError++;
                } else {
                    countAbandono++;
                }
            }
        }

        respuesta.set("fechaDesde", fechaDesde);
        respuesta.set("fechaHasta", fechaHasta);
        respuesta.set("total", sesionesStandBy.size());

        Objeto standBy = new Objeto();
        standBy.set("CONTROLAR", countControlar);
        standBy.set("FLUJO_VU_OK", countVUok);
        standBy.set("CONTROL_OK", countControlOK);
        standBy.set("CONTROL_ERROR", countControlError);

        Objeto sesionEsales = new Objeto();
        sesionEsales.set("FINALIZAR_OK", countFinalizados);
        sesionEsales.set("BATCH_CORRIENDO", countBatchCorriendo);
        sesionEsales.set("CONTROLAR_USUARIO", countControlarUsuario);
        sesionEsales.set("ERROR", countError);
        sesionEsales.set("ABANDONO", countAbandono);

        respuesta.set("sesiones_stand_by", standBy);
        respuesta.set("sesiones_esales", sesionEsales);

        return respuesta;
    }

    public static Object obtenerEstadosStandBy(ContextoBB contexto) {

        List<String> estados = new ArrayList<String>();
        estados.add(SesionesStandBy.CONTROLAR);
        estados.add(SesionesStandBy.FLUJO_VU_OK);
        estados.add(SesionesStandBy.CONTROL_OK);
        estados.add(SesionesStandBy.CONTROL_ERROR);

        return respuesta("estados", estados);
    }

    public static Object retomarSesionRm(ContextoBB contexto) {

        String tokenSesion = contexto.parametros.string("token_sesion", null);
        if (empty(tokenSesion)) {
            return respuesta("ERROR_TOKEN_VACIO");
        }

        SesionEsales sesionEsales = SqlEsales.sesionEsales(contexto, tokenSesion).tryGet();
        if (sesionEsales == null) {
            return respuesta("ERROR_SESION_NO_ENCONTRADA");
        }

        if (contexto.sesion().usuarioLogueado()) {
            contexto.sesion().delete();
        }

        SesionBB sesion = SqlEsales.retomarSesion(contexto, sesionEsales).tryGet();
        if (sesion == null) {
            return respuesta("ERROR_RETOMAR_SESION");
        }

        LogBB.evento(contexto, GeneralBB.TELEMARKETING_MANUAL, tokenSesion, sesion.cuil);

        return respuesta("sesion", sesion.getSesion());
    }

    public static Object obtenerSesionesEstado(ContextoBB contexto) {

        String estado = contexto.parametros.string("estado", null);
        String fechaDesdeStr = contexto.parametros.string("fecha_desde", null);

        Fecha fechaDesde = fechaDesdeStr == null ? Fecha.ahora().restarDias(200) : new Fecha(fechaDesdeStr, "yyyy-MM-dd");

        SesionesEsales sesiones = SqlEsales.obtenerSesionesEstado(contexto, estado, fechaDesde).tryGet();
        if (sesiones == null || sesiones.size() == 0) {
            return respuesta("ESTADO_NO_ENCONTRADO");
        }

        List<String> cuils = new ArrayList<String>();

        for (int i = 0; i < sesiones.size(); i++) {
            cuils.add(sesiones.get(i).cuil);
        }

        Objeto respuesta = respuesta();
        respuesta.set("total", cuils.size());
        respuesta.set("cuils", cuils);

        return respuesta;
    }

    private static void altaCuentaInversorV2(ContextoBB contexto) {
        SesionBB sesion = contexto.sesion();
        if (!sesion.buhoInversorAceptada()) {
            return;
        }

        try {

            if (!BBAplicacion.estaEnHorarioInversor(contexto)) {
                sesion.bbInversorAceptada = GeneralBB.FUERA_DE_HORARIO;
                sesion.saveSesionbb2();
                return;
            }

            try {
                Thread.sleep(9000);
            } catch (Exception e) {
            }

            Persona persona = ApiPersonas.persona(contexto, sesion.cuil).tryGet();
            Domicilios domicilios = ApiPersonas.domicilios(contexto, sesion.cuil, false).tryGet();
            Domicilio domicilioPostal = domicilios.postal();
            Telefonos telefonos = ApiPersonas.telefonos(contexto, sesion.cuil, false).tryGet();
            Telefono telefono = telefonos.celularCore();
            if (empty(telefono) || empty(telefono.idCore)) {
                telefono = telefonos.particularCore();
            }

            CuentasBB cuentas = ApiCuentasBB.get(contexto, persona.idCliente).tryGet();
            CuentaBB cuentaPesos = cuentas.obtenerUltimaCajaDeAhorro("80");
            CuentaBB cuentaDolares = cuentas.obtenerUltimaCajaDeAhorro("2");

            ApiResponse cuentaComitenteRes = ApiInversiones.altaCuentaComitente(contexto, sesion, domicilioPostal, telefono, persona, cuentaPesos, cuentaDolares).tryGet();
            if (cuentaComitenteRes == null || cuentaComitenteRes.hayError()) {
                LogBB.error(contexto, "ALTA_CUENTA_COMITENTE");
                return;
            }

            String idPersonaFondo = null;
            ApiResponse personaFondo = ApiInversiones.selectPersonaByDoc(contexto, sesion.numeroDocumento, tipoDocPersonaESCO(sesion.tipoDocumento())).tryGet();
            if (personaFondo != null && !personaFondo.hayError()) {
                idPersonaFondo = cuentaComitenteRes.string("IDPersona", null);
            }

            ApiResponse cuentaCuotapartistaRes = ApiInversiones.altaCuentaCuotapartista(contexto, sesion, domicilioPostal, telefono, persona, cuentaPesos, cuentaDolares, idPersonaFondo).tryGet();
            if (cuentaCuotapartistaRes == null || cuentaCuotapartistaRes.hayError()) {
                LogBB.error(contexto, "ALTA_CUENTA_CUOTAPARTISTA");
                return;
            }

            sesion.bbInversorAceptada = "ALTA_CUENTAS_OK";
            sesion.saveSesionbb2();

        } catch (Exception e) {
            LogBB.error(contexto, "ALTA_CUENTAS_INVERSOR");
        }
    }

    private static String tipoDocPersonaESCO(String numDoc) {
        switch (numDoc) {
            case "01":
                return "4"; //DNI
            case "02":
                return "1"; //L.E.
            case "03":
                return "2"; //L.C.
            case "134":
                return "39"; //DNI EXT
            default:
                return "4";
        }
    }

    // borrado de sesiones no finalizadas, solo se conservan los ultimos 30 dias
    public static void borrarSesionesNoFinalizadas(ContextoBB contexto) {

        logProceso(contexto, "inicio proceso de borrado de sesiones no finalizadas pasado los 30 dias");
        Object retornoSQl = SqlBuhoBank.ejecutarBorradoDeSesiones(contexto, Fecha.ahora().restarDias(30)).tryGet();

        logProceso(contexto, "finaliza proceso de de borrado de sesiones no finalizadas");

    }

    public static void anonimizarClientesVu(ContextoBB contexto) {

        logProceso(contexto, "Inicio proceso de Anonimizado Clientes VU");

        try {
            Boolean ejecuto = SqlBuhoBank.armarLock(contexto, "anonimizarClientesVu2").get();
            if (!ejecuto) {
                logProceso(contexto, "Ya se está ejecutando el proceso de anonimización de clientes VU 2");
                return;
            }
        } catch (Exception ex) {
            logProceso(contexto, "Fallo al armar el lock para anonimizarClientesVu2: " + ex.getMessage());
            return;
        }

        SesionBB sesion = contexto.sesion();
        // int offset = 0;
        //int cantidadRegistros = !contexto.esProduccion() ? 100 : 100;
        // int batchSize = 10 * cantidadRegistros; // bloques de 1.000 en producción / 1000 homo
        int limiteDiario = !contexto.esProduccion() ? 90_000 : 90_000; // Limite diario de 90.000 en producción
        int totalProcesados = 0;
        boolean hayMas = true;

        while (hayMas && totalProcesados < limiteDiario) {
            try {
                Objeto retornoSQl = (Objeto) SqlBuhoBank.obtenerClientesAnonimizarVu(contexto).tryGet();

                if (retornoSQl == null || retornoSQl.objetos() == null || retornoSQl.objetos().isEmpty()) {
                    hayMas = false;
                    break;
                }

                for (Objeto cliente : retornoSQl.objetos()) {
                    if (totalProcesados >= limiteDiario) {
                        hayMas = false;
                        break;
                    }
                    totalProcesados++;
                    Integer id = cliente.integer("Id");
                    Boolean borrado = false;
                    try {
                        String userName = cliente.string("username_cuil");
                        String operationVu = cliente.string("operation_vu");
                        String respuestaVu = "";

                        if (userName == null || operationVu == null || !operationVu.contains("/")) {
                            SqlBuhoBank.actualizarClientesAnonimizarVu(contexto, id, borrado,
                                    "Datos inválidos: username o operation_vu nulos o mal formateados");
                            continue;
                        }

                        String[] operationParts = operationVu.split("/");
                        if (operationParts.length < 2) {
                            SqlBuhoBank.actualizarClientesAnonimizarVu(contexto, id, borrado,
                                    "Datos inválidos: operation_vu mal formateado");
                            continue;
                        }

                        Integer operationId = Integer.valueOf(operationParts[0]);
                        String operationGuid = operationParts[1];

                        if (empty(operationId) || empty(operationGuid)) {
                            SqlBuhoBank.actualizarClientesAnonimizarVu(contexto, id, borrado,
                                    "Datos inválidos: operationId y operationGuid nulos");
                            continue;
                        }

                        String urlBase = sesion.getUrlVU(contexto);
                        String privateKey = sesion.getParamKeyPrivadaVU(contexto);

                        if (empty(urlBase) || empty(privateKey)) {
                            SqlBuhoBank.actualizarClientesAnonimizarVu(contexto, id, borrado,
                                    "URL VU o PRIVATE KEY no válidas");
                            continue;
                        }

                        Objeto objetoResponseVu = BackofficevuBB.anonimizarDatosVU(
                                contexto, urlBase, privateKey, userName, operationId, operationGuid
                        );

                        Integer code = 999;
                        if(objetoResponseVu != null) {
                            code = objetoResponseVu.integer("code");
                            String message = objetoResponseVu.string("message");
                            respuestaVu = code + " - " + message;
                        }

                        if (code == 16007) {
                            borrado = true;
                        }

                        SqlBuhoBank.actualizarClientesAnonimizarVu(contexto, id, borrado, respuestaVu);

                    } catch (Exception e) {
                        try {
                            SqlBuhoBank.actualizarClientesAnonimizarVu(contexto, id, borrado,
                                    "Error procesando cliente, cae en catch general");
                            logProceso(contexto, "Error procesando cliente: " + e.getMessage());
                        } catch (Exception ex) {
                            logProceso(contexto, "Error procesando cliente: " + ex.getMessage());
                        }
                    }
                }
            } catch (Exception e) {
                logProceso(contexto, "Error general en el proceso de anonimización: " + e.getMessage());
                hayMas = false;
            }
        }

        try {
            SqlBuhoBank.desarmarLock(contexto, "anonimizarClientesVu2").get();
        } catch (Exception ex) {
            logProceso(contexto, "Fallo al desarmar el lock para anonimizarClientesVu2: " + ex.getMessage());
            return;
        }

        logProceso(contexto, "Finaliza proceso de Anonimizado Clientes VU");
    }

    public static void configurarPrismaNotificacionesNFC(ContextoBB contexto) {
        String evento = "cron_prismaNotificacionesNFC";
        if(!puedoEjecutarProceso(contexto, evento)) {
            return;
        }

        String paso = "Inicio Ejecucion";
        String detalle = "";

        logProceso(contexto, "Inicio de configuracion en Prisma de Notificaciones NFC");
        SqlBuhoBank.logCron(contexto, evento, paso, detalle);

        try {
            paso = "GetConfiguracionNotificacionNFC Config Antigua";
            getConfiguracionNotificacionNFC(contexto, evento, paso);

            paso = "PostConfiguracionNotificacionNFC Config Nueva";
            postConfiguracionNotificacionNFC(contexto, evento, paso);

            paso = "GetConfiguracionNotificacionNFC Config Final";
            getConfiguracionNotificacionNFC(contexto, evento, paso);
        } catch (Exception ex) {
            logProceso(contexto, "Error al intentar configurar en Prisma de Notificaciones NFC");
            String stack = Arrays.stream(ex.getStackTrace())
                    .limit(8)
                    .map(StackTraceElement::toString)
                    .collect(Collectors.joining("\n"));
            detalle = "Falla por excepcion: " + ex.getMessage() + "\n" + stack;
            paso = "Error proceso cron";
            SqlBuhoBank.logCron(contexto, evento, paso, detalle);
        }

        detalle = "";

        logProceso(contexto, "Fin de configuracion en Prisma de Notificaciones NFC");
        paso = "Fin Ejecucion";
        SqlBuhoBank.logCron(contexto, evento, paso, detalle);

        finalizarProceso(contexto, evento);
    }

    private static boolean puedoEjecutarProceso(ContextoBB contexto, String proceso) {
        try {
            return SqlBuhoBank.armarLock(contexto, proceso).get();
        } catch (Exception ex) {
            logProceso(contexto, "Fallo al armar el lock para " + proceso + ": " + ex.getMessage());
            return false;
        }
    }

    private static void finalizarProceso(ContextoBB contexto, String proceso) {
        try {
            SqlBuhoBank.desarmarLock(contexto, proceso).get();
        } catch (Exception ex) {
            logProceso(contexto, "Fallo al desarmar lock para" + proceso + ": " + ex.getMessage());
        }
    }

    private static void getConfiguracionNotificacionNFC(
        ContextoBB contexto, String evento, String paso) {
        String detalle;

        try {
            NFC nfcGet = ApiTarjetasCredito.getConfiguracionNotificacionNFC(contexto).get();

            if(nfcGet != null) {
                logProceso(contexto, "Se obtuvo response de getConfiguracionNotifiacionNFC");
                detalle = nfcGet.toJson();
                SqlBuhoBank.logCron(contexto, evento, paso, detalle);
            } else {
                logProceso(contexto, "Error al obtener response de getConfiguracionNotificacionNFC");
                detalle = "Response getConfiguracionNotificacionNFC vacío o null";
                SqlBuhoBank.logCron(contexto, evento, paso, detalle);
            }
        } catch (Exception ex) {
            logProceso(contexto, "Error al obtener response de getConfiguracionNotifiacionNFC");
            String stack = Arrays.stream(ex.getStackTrace())
                    .limit(8)
                    .map(StackTraceElement::toString)
                    .collect(Collectors.joining("\n"));
            detalle = "Falla por excepcion: " + ex.getMessage() + "\n" + stack;
            paso = "Excepcion en GetConfiguracionNotificacionNFC";
            SqlBuhoBank.logCron(contexto, evento, paso, detalle);
        }
    }

    private static void postConfiguracionNotificacionNFC(
            ContextoBB contexto, String evento, String paso) {
        String detalle;

        try {
            Map<String, Object> metodoSms = new HashMap<>();
            metodoSms.put("type", "otp_by_sms");
            metodoSms.put("flows", Collections.singletonList("YELLOW"));
            Map<String, Object> metodoAtencion = new HashMap<>();
            metodoAtencion.put("type", "issuer_customer_service");
            metodoAtencion.put("message", "43793465");
            metodoAtencion.put("flows", Collections.singletonList("YELLOW"));
            List<Map<String, Object>> notificationMethods = Arrays.asList(
                    metodoSms,
                    metodoAtencion
            );

            NFC nfcPost = ApiTarjetasCredito.postConfiguracionNotificacionNFC(contexto, notificationMethods).get();

            if(nfcPost != null) {
                logProceso(contexto, "Se obtuvo response de postConfiguracionNotifiacionNFC");

                detalle = nfcPost.toJson();
                SqlBuhoBank.logCron(contexto, evento, paso, detalle);
            } else {
                logProceso(contexto, "Error al obtener response de postConfiguracionNotificacionNFC");

                detalle = "Response postConfiguracionNotificacionNFC vacío o null";
                SqlBuhoBank.logCron(contexto, evento, paso, detalle);
            }

        } catch(Exception ex) {
            logProceso(contexto, "Error al obtener response de postConfiguracionNotifiacionNFC");
            String stack = Arrays.stream(ex.getStackTrace())
                    .limit(8)
                    .map(StackTraceElement::toString)
                    .collect(Collectors.joining("\n"));

            detalle = "Falla por excepcion: " + ex.getMessage() + "\n" + stack;
            paso = "Excepcion en PostConfiguracionNotificacionNFC";
            SqlBuhoBank.logCron(contexto, evento, paso, detalle);
        }
    }
}