package ar.com.hipotecario.mobile.api;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ar.com.hipotecario.backend.servicio.api.seguridad.MigracionUsuario;
import ar.com.hipotecario.backend.servicio.api.transmit.JourneyTransmitEnum;
import ar.com.hipotecario.backend.util.Transmit;
import ar.com.hipotecario.canal.libreriariesgofraudes.infrastructure.gateway.model.bm.mb.TransactionMBBMBankProcess;
import ar.com.hipotecario.mobile.servicio.*;
import ar.com.hipotecario.mobile.lib.*;
import org.codehaus.plexus.util.StringUtils;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.mobile.ConfigMB;
import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.RespuestaMB;
import ar.com.hipotecario.mobile.api.MBInversion.EnumPropuestasInversion;
import ar.com.hipotecario.mobile.conector.ApiMB;
import ar.com.hipotecario.mobile.conector.ApiRequestMB;
import ar.com.hipotecario.mobile.conector.ApiResponseMB;
import ar.com.hipotecario.mobile.conector.SqlMB;
import ar.com.hipotecario.mobile.conector.SqlRequestMB;
import ar.com.hipotecario.mobile.conector.SqlResponseMB;
import ar.com.hipotecario.mobile.excepcion.ApiExceptionMB;
import ar.com.hipotecario.mobile.negocio.Cuenta;
import ar.com.hipotecario.mobile.negocio.CuentaTercero;
import ar.com.hipotecario.mobile.negocio.TarjetaDebito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MBTransferencia {

    private static Logger log = LoggerFactory.getLogger(MBTransferencia.class);
    private static MBSoftToken apiSoftToken = new MBSoftToken();
    private final static String DIA_HABIL = "1";

    /* ========== BENEFICIARIOS ========== */
    public static RespuestaMB beneficiarios(ContextoMB contexto) {
        SqlTransferencia sqlTransferencia = new SqlTransferencia();
        if (contexto.idCobis() == null) {
            return RespuestaMB.estado("SIN_PSEUDO_SESION");
        }
        Futuro<SqlResponseMB> sqlResponse = new Futuro<>(() -> sqlTransferencia.contactosAgendados(contexto.idCobis()));
        if (sqlResponse.get().hayError) {
            return RespuestaMB.error();
        }
        Objeto beneficiarios = new Objeto();
        for (Objeto registro : sqlResponse.get().registros) {
            String id = registro.string("cbu_destino", registro.string("nro_cuenta_destino")).trim();
            if (id.isEmpty()) {
                continue;
            }

            String comentario = registro.string("comentario").trim();
            Boolean esCuentaBH = !registro.string("nro_cuenta_destino").isEmpty();
            if (comentario.isEmpty() && esCuentaBH) {
                try {
                    CuentaTercero cuentaTercero = new CuentaTercero(contexto, registro.string("nro_cuenta_destino"));
                    if (cuentaTercero.esCuentaBH()) {
                        comentario = cuentaTercero.titular();
                        sqlTransferencia.actualizarComentarioContactoAgendado(comentario, registro.string("id"));
                    }
                } catch (Exception e) {
                }
            }

            Objeto item = new Objeto();
            item.set("id", id);
            item.set("beneficiario", registro.string("titular", "Banco Hipotecario"));
            item.set("beneficiarioMinuscula", registro.string("titular", "Banco Hipotecario").toLowerCase()); // emm-20190425
            item.set("cuenta", Cuenta.numeroEnmascarado(registro.string("nro_cuenta_destino")));
            item.set("tipoCuenta", registro.string("tipo_cuenta_destino"));
            item.set("comentario", comentario);
            item.set("descripcion", registro.string("descripcion").trim());
            item.set("email", registro.string("email_destinatario").trim());
            boolean mostrarCuentaDelBancoConsolidada = true;
            if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_beneficiario_bh_nombre_apellido")
                    && registro.string("cuenta_del_banco").trim().equals("1")) {
                mostrarCuentaDelBancoConsolidada &= registro.string("titular", "Banco Hipotecario")
                        .equals("Banco Hipotecario");
            }
            item.set("mostrar_cuenta_del_banco_consolidada", mostrarCuentaDelBancoConsolidada);

            Boolean esBH = id != null && id.length() == 15
                    && (id.startsWith("2") || id.startsWith("3") || id.startsWith("4"));
            esBH |= id != null && id.length() == 22 && id.startsWith("044");
            item.set("esCuentaBH", esBH);
            beneficiarios.add(item);
        }
        beneficiarios.ordenar("beneficiarioMinuscula");
        return RespuestaMB.exito("beneficiarios", beneficiarios);
    }

    public static RespuestaMB agendarBeneficiario(ContextoMB contexto) {
        String cuentaDestino = contexto.parametros.string("cuentaDestino");
        String email = contexto.parametros.string("email");
        String comentario = contexto.parametros.string("comentario");
        String descripcion = contexto.parametros.string("descripcion");
        String concepto = contexto.parametros.string("concepto", "VAR");
        String apodo = contexto.parametros.string("apodo", "");

        if (contexto.idCobis() == null) {
            return RespuestaMB.estado("SIN_PSEUDO_SESION");
        }
        if (Objeto.anyEmpty(cuentaDestino)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        if (CuentasService.esCvu(cuentaDestino) && !ConfigMB.bool("transferencia_agendar_beneficiario_cvu", false)) {
            return RespuestaMB.estado("AGENDA_CVU_NO_IMPLEMENTADO");
        }

        CuentaTercero cuentaTercero = new CuentaTercero(contexto, cuentaDestino);
        if (cuentaTercero.cuentaBH.hayError()) {
            return RespuestaMB.estado("ERROR_CUENTA_TERCERO");
        }

        boolean actualizarApodo = true;
        if (apodo.isEmpty()) {
            String apodoBeneficiario = getApodoBeneficiario(contexto, cuentaTercero.cuit());
            if (!apodoBeneficiario.isEmpty()) {
                apodo = apodoBeneficiario;
            } else {
                actualizarApodo = false;
            }
        }

        Boolean esCuentaBH = cuentaTercero.esCuentaBH();
        String titular = cuentaTercero.titular();

        if (esCuentaBH) {
            String cbu = cuentaTercero.cbu();
            //String cuenta = cuentaTercero.cuentaBH.string("cuenta");
            cuentaTercero = new CuentaTercero(contexto, cbu);
        }

        SqlTransferencia sqlTransferencia = new SqlTransferencia();
        SqlResponseMB sqlResponseAgenda = sqlTransferencia.obtenerContactoAgendadoById(contexto.idCobis(), cuentaTercero.cbu(), cuentaTercero.numero() == null ? "" : cuentaTercero.numero());
        Integer sizeRegistros = sqlResponseAgenda.registros.size();

        if (sizeRegistros.equals(0) && !sqlResponseAgenda.hayError && (cuentaTercero.cbu() != null || cuentaTercero.numero() != null)) {

            SqlRequestMB sqlRequest = SqlMB.request("UpdateAgendaTransferenciasComentario", "hbs");
            sqlRequest.sql = "INSERT INTO [Hbs].[dbo].[agenda_transferencias] ([cbu_destino],[nro_cliente],[comentario],[concepto],[descripcion],[nro_cuenta_destino],[documento_beneficiario],[tipo_cuenta_destino],[titular],[autorizada],[cuenta_del_banco],[tipo_documento],[email_destinatario],[alias], [apodo], [moneda_cuenta_destino], [banco_destino]) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            sqlRequest.parametros.add(cuentaTercero.cbu()); // cbu_destino
            sqlRequest.parametros.add(contexto.idCobis()); // nro_cliente
            sqlRequest.parametros.add(comentario); // comentario
            sqlRequest.parametros.add(concepto); // concepto
            sqlRequest.parametros.add(descripcion); // descripcion
            sqlRequest.parametros.add(esCuentaBH ? cuentaTercero.numero() : null); // nro_cuenta_destino
            sqlRequest.parametros.add(cuentaTercero.cuit()); // documento_beneficiario
            sqlRequest.parametros.add(cuentaTercero.tipo()); // tipo_cuenta_destino
            sqlRequest.parametros.add(titular); // titular
            sqlRequest.parametros.add(0); // autorizada
            sqlRequest.parametros.add(esCuentaBH ? 1 : 0); // cuenta_del_banco
            sqlRequest.parametros.add(null); // tipo_documento
            sqlRequest.parametros.add(email); // email_destinatario
            sqlRequest.parametros.add(cuentaTercero.alias()); // alias
            sqlRequest.parametros.add(apodo); // apodo
            sqlRequest.parametros.add(cuentaTercero.idMoneda()); // moneda_cuenta_destino
            sqlRequest.parametros.add(cuentaTercero.nrobanco()); // banco_destino

            SqlResponseMB sqlResponse = SqlMB.response(sqlRequest);
            if (sqlResponse.hayError) {
                return RespuestaMB.error();
            }

            if (actualizarApodo) {
                sqlTransferencia.actualizarApodoAgendado(contexto.idCobis(), cuentaTercero.cuit(), apodo);
            }

            contexto.insertarContador("AGENDA_BENEFICIARIO");
            String nombreTitular = (titular == null) ? "" : titular;

            // mando el mail de adhesión
            try {
                if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_notificaciones_mail",
                        "prendido_notificaciones_mail_cobis")) {
                    Objeto parametros = new Objeto();
                    parametros.set("Subject", "Agenda de beneficiario");
                    parametros.set("NOMBRE_APELLIDO", contexto.persona().nombre() + " " + contexto.persona().apellido());
                    parametros.set("NOMBRE", contexto.persona().nombre());
                    parametros.set("APELLIDO", contexto.persona().apellido());
                    Date hoy = new Date();
                    parametros.set("FECHA", new SimpleDateFormat("dd/MM/yyyy").format(hoy));
                    parametros.set("HORA", new SimpleDateFormat("hh:mm").format(hoy));
                    parametros.set("CANAL", "Banca Móvil");
                    parametros.set("NOMBRE_BENEFICIARIO", nombreTitular + " (" + comentario + ")");

                    if (MBSalesforce.prendidoSalesforceAmbienteBajoConFF(contexto)) {
                        String salesforce_agenda_beneficiario = ConfigMB.string("salesforce_agenda_beneficiario");
                        parametros.set("IDCOBIS", contexto.idCobis());
                        parametros.set("CANAL", ConfigMB.string("salesforce_nombre_canal"));
                        new Futuro<>(() -> MBSalesforce.registrarEventoSalesforce(contexto, salesforce_agenda_beneficiario, parametros));
                    } else {
                        RestNotificaciones.envioMail(contexto, ConfigMB.string("doppler_agenda_beneficiario"), parametros);
                    }
                }
            } catch (Exception e) {
            }


            contexto.insertarLogBeneficiario(contexto, esCuentaBH ? null : cuentaTercero.cbu(),
                    esCuentaBH ? cuentaTercero.numero() : null, esCuentaBH ? null : cuentaTercero.cuit(), titular, "A");
            if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_monitoreo_transaccional_2")) {
                MBMonitoring monitoringApi = new MBMonitoring();
                monitoringApi.sendMonitoringAgendaBeneficiario(contexto, cuentaTercero.cbu(), comentario,
                        cuentaTercero.cuit());
            }
        }

        return RespuestaMB.exito();
    }

    private static String getApodoBeneficiario(ContextoMB contexto, String cuil) {
        try {
            SqlTransferencia sqlTransferencia = new SqlTransferencia();
            SqlResponseMB sqlResponse = sqlTransferencia.contactosAgendados(contexto.idCobis());
            if (sqlResponse.hayError) {
                return "";
            }

            for (Objeto registro : sqlResponse.registros) {
                if (cuil.equals(registro.string("documento_beneficiario").trim())) {
                    return registro.string("apodo");
                }
            }
        } catch (Exception e) {
        }
        return "";
    }

    public static RespuestaMB modificarBeneficiario(ContextoMB contexto) {
        String idAgenda = contexto.parametros.string("idAgenda");
        String comentario = contexto.parametros.string("comentario", null);
        String descripcion = contexto.parametros.string("descripcion", null);
        String email = contexto.parametros.string("email", null);
        if (contexto.idCobis() == null) {
            return RespuestaMB.estado("SIN_PSEUDO_SESION");
        }
        SqlRequestMB sqlRequest = SqlMB.request("UpdateAgendaTransferencias", "hbs");
        sqlRequest.sql = "UPDATE [Hbs].[dbo].[agenda_transferencias] ";
        if (comentario != null) {
            sqlRequest.sql += "SET comentario = ? ";
            sqlRequest.parametros.add(comentario);
        }
        if (descripcion != null) {
            sqlRequest.sql += ", descripcion = ? ";
            sqlRequest.parametros.add(descripcion);
        }
        if (email != null) {
            sqlRequest.sql += ", email_destinatario = ? ";
            sqlRequest.parametros.add(email);
        }
        sqlRequest.sql += "WHERE nro_cliente = ? AND (cbu_destino = ? OR nro_cuenta_destino = ?)";
        sqlRequest.parametros.add(contexto.idCobis());
        sqlRequest.parametros.add(idAgenda);
        sqlRequest.parametros.add(idAgenda);

        SqlResponseMB sqlResponse = SqlMB.response(sqlRequest);
        if (sqlResponse.hayError) {
            return RespuestaMB.error();
        }
        return RespuestaMB.exito();
    }

    public static RespuestaMB eliminarBeneficiario(ContextoMB contexto) {
        SqlTransferencia sqlTransferencia = new SqlTransferencia();
        String idAgenda = contexto.parametros.string("idAgenda");
        if (contexto.idCobis() == null) {
            return RespuestaMB.estado("SIN_PSEUDO_SESION");
        }
        if (Objeto.anyEmpty(idAgenda)) {
            return RespuestaMB.parametrosIncorrectos();
        }
        SqlResponseMB sqlResponseConsulta = sqlTransferencia.obtenerAgendaTransferencia(contexto.idCobis(), idAgenda);
        if (sqlResponseConsulta.hayError) {
            return RespuestaMB.error();
        }

        String cuenta = "";
        String cbu = "";
        String nombre = "";
        String documento = "";
        for (Objeto registro : sqlResponseConsulta.registros) {
            if (registro.string("cbu_destino", registro.string("nro_cuenta_destino")).trim().isEmpty()) {
                continue;
            }

            cbu = registro.string("cbu_destino");
            cuenta = registro.string("nro_cuenta_destino");
            documento = registro.string("documento_beneficiario");
            nombre = registro.string("titular", "Banco Hipotecario");
        }
        SqlResponseMB sqlResponse = sqlTransferencia.eliminarAgendaTransferencia(contexto.idCobis(), idAgenda);
        if (sqlResponse.hayError) {
            return RespuestaMB.error();
        }

        contexto.insertarLogBeneficiario(contexto, cbu, cuenta, documento, nombre, "B");

        return RespuestaMB.exito();
    }

    /* ========== AUMENTO LIMITE ========== */
    public static RespuestaMB limites(ContextoMB contexto) {
        Boolean buscarAumentoLimite = contexto.parametros.bool("buscarAumentoLimite", false);
        Boolean buscarSoloAumentoLimiteHoy = contexto.parametros.bool("buscarSoloAumentoLimiteHoy", false);

        BigDecimal limitePesos = new BigDecimal(ConfigMB.string("configuracion_limite_transferencia_pesos"));
        BigDecimal limiteDolares = new BigDecimal(ConfigMB.string("configuracion_limite_transferencia_dolares"));
        if (contexto.idCobis() == null) {
            return RespuestaMB.estado("SIN_PSEUDO_SESION");
        }
        RespuestaMB respuesta = new RespuestaMB();
        respuesta.set("limiteTransferenciaPesos", limitePesos);
        respuesta.set("limiteTransferenciaDolares", limiteDolares);

        if (buscarAumentoLimite || buscarSoloAumentoLimiteHoy) {
            respuesta.set("tieneAumentoLimite", false);
            ExecutorService executorService = Concurrencia.executorService(contexto.cuentas());
            for (Cuenta cuenta : contexto.cuentas()) {
                executorService.submit(() -> {
                    ApiRequestMB request = ApiMB.request("CuentasGetLimites", "cuentas", "GET",
                            "/v1/cuentas/{idcuenta}/limites", contexto);

                    request.path("idcuenta", cuenta.numero());
                    request.query("idcliente", contexto.idCobis());
                    request.query("fechadesde", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
                    request.query("fechahasta", "2099-01-01");
                    request.query("idmoneda", cuenta.idMoneda());

                    ApiResponseMB response = ApiMB.response(request, cuenta.numero());
                    if (response.hayError()) {
                        respuesta.setEstadoExistenErrores();
                        return;
                    }

                    for (Objeto item : response.objetos()) {
                        BigDecimal importe = item.bigDecimal("importe");
                        if (cuenta.esPesos() && limitePesos.longValue() > importe.longValue()) {
                            continue;
                        } else if (cuenta.esDolares() && limiteDolares.longValue() > importe.longValue()) {
                            continue;
                        }

                        Objeto limite = new Objeto();
                        limite.set("idCuenta", cuenta.id());
                        limite.set("cuenta", cuenta.numero());
                        limite.set("numeroEnmascarado", cuenta.numeroEnmascarado());
                        limite.set("importe", importe.longValue());
                        limite.set("idMoneda", item.integer("idMoneda"));
                        limite.set("tipo", cuenta.tipoCorto());
                        limite.set("fecha", item.date("fecha", "yyyy-MM-dd", "dd/MM/yyyy"));
                        limite.set("esHoy", item.date("fecha", "yyyy-MM-dd", "dd/MM/yyyy")
                                .equals(new SimpleDateFormat("dd/MM/yyyy").format(new Date())));
                        if (!buscarSoloAumentoLimiteHoy || (buscarSoloAumentoLimiteHoy && limite.bool("esHoy") && estaEnHorarioAumentoLimite())) {
                            respuesta.add("limites", limite);
                            respuesta.set("tieneAumentoLimite", true);
                        }
                    }
                });
            }
            Concurrencia.esperar(executorService, respuesta);
        }

        return respuesta;
    }

    private static boolean estaEnHorarioAumentoLimite() {
        try {
            String horaActual = Fecha.horaActual();
            int hora = Integer.parseInt(horaActual.substring(0, 2));
            return hora >= 9 && hora <= 18;
        } catch (Exception e) {
        }
        return false;
    }

    public static RespuestaMB aumentarLimite(ContextoMB contexto) {
        if (contexto.idCobis() == null) {
            return RespuestaMB.estado("SIN_PSEUDO_SESION");
        }
        boolean isEnalble = MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_refactor_aumento_limite");
        return (isEnalble) ? aumentarLimite2(contexto) : aumentarLimite1(contexto);
    }

    public static RespuestaMB aumentarLimite1(ContextoMB contexto) {
        String idCuenta = contexto.parametros.string("idCuenta");
        String fecha = contexto.parametros.date("fecha", "d/M/yyyy", "yyyy-MM-dd");
        BigDecimal monto = contexto.parametros.bigDecimal("monto");

        if (Objeto.anyEmpty(idCuenta, fecha, monto)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        Cuenta cuenta = contexto.cuenta(idCuenta);
        if (cuenta == null) {
            return RespuestaMB.estado("CUENTA_NO_EXISTE");
        }
        boolean isEnableSf = MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_sf_aumento_limite");
        if (isEnableSf && !contexto.validaSegundoFactor("aumento-limite-transferencia")) {
            return RespuestaMB.estado("REQUIERE_SEGUNDO_FACTOR");
        }
        ApiResponseMB responseCatalogo = RestCatalogo.calendarioFecha(contexto, fecha);
        if (!responseCatalogo.hayError()) {
            try {
                if (!"1".equals(responseCatalogo.objetos().get(0).string("esDiaHabil"))) {
                    return RespuestaMB.estado("DIA_NO_HABIL");
                }
            } catch (Exception e) {
            }
        }

        if (!esFechaPosteriorHoy(fecha))
            return RespuestaMB.estado("FECHA_ANTERIOR_A_HOY");

        ApiRequestMB request = ApiMB.request("CuentasPostLimites", "cuentas", "POST", "/v1/cuentas/{idcuenta}/limites",
                contexto);
        request.path("idcuenta", cuenta.numero());
        request.body("fecha", fecha);
        request.body("idCliente", contexto.idCobis());
        request.body("idMoneda", cuenta.idMoneda());
        request.body("importe", monto);

        ApiResponseMB response = ApiMB.response(request);
        if (response.hayError())
            return RespuestaMB.error();

        return RespuestaMB.exito();
    }

    public static RespuestaMB aumentarLimite2(ContextoMB contexto) {
        RestTransferencia restTransferencia = new RestTransferencia();
        String idCuenta = contexto.parametros.string("idCuenta");
        String fecha = contexto.parametros.date("fecha", "d/M/yyyy", "yyyy-MM-dd");
        BigDecimal monto = contexto.parametros.bigDecimal("monto");

        RespuestaMB condicionError = validateAumentarLimite(contexto);
        if (Objects.nonNull(condicionError)) {
            return condicionError;
        }
        Cuenta cuenta = contexto.cuenta(idCuenta);
        if (cuenta == null) {
            return RespuestaMB.estado("CUENTA_NO_EXISTE", contexto.csmIdAuth);
        }

        ApiResponseMB responseLimiteByCuenta = restTransferencia.cuentasGetLimites(contexto, cuenta, true, fecha);

        if (responseLimiteByCuenta.objetos().size() >= ConfigMB.integer("nro_maximo_aumento_litime_diario", 1)) {
            return RespuestaMB.estado("MAXIMO_SOLICITUDES_FECHA_PROGRAMADA", contexto.csmIdAuth);
        }

        ApiResponseMB response = null;

        if (MBAplicacion.funcionalidadPrendida("mb_prendido_transf_importe_superior")) {
            TarjetaDebito tarjetaDebitoAsociada = contexto.tarjetaDebitoAsociada(cuenta);
            response = restTransferencia.CuentasTISLimites(contexto, cuenta, fecha, monto, tarjetaDebitoAsociada);
            if (!response.hayError()) {
                response = restTransferencia.CuentasPostLimites(contexto, cuenta, fecha, monto);
            }
        } else {
            response = restTransferencia.CuentasPostLimites(contexto, cuenta, fecha, monto);
        }

        if (response.hayError()) {
            if (response.string("mensajeAlUsuario").contains("La fecha debe ser superior a la fecha actual")) {
                return RespuestaMB.estado("FECHA_ANTERIOR_A_HOY", contexto.csmIdAuth);
            }
            return RespuestaMB.error(contexto.csmIdAuth);
        }

        registroSolicitudLimite(contexto, cuenta);

        Objeto parametros = new Objeto();
        parametros.set("NOMBRE_APELLIDO", contexto.persona().nombre() + " " + contexto.persona().apellido());
        parametros.set("NOMBRE", contexto.persona().nombre());
        parametros.set("APELLIDO", contexto.persona().apellido());
        Date hoy = new Date();
        parametros.set("FECHA", new SimpleDateFormat("dd/MM/yyyy").format(hoy));
        parametros.set("HORA", new SimpleDateFormat("HH:mm").format(hoy));
        parametros.set("CANAL", ConfigMB.string("salesforce_nombre_canal"));

        if (MBSalesforce.prendidoSalesforceAmbienteBajoConFF(contexto)) {
            String salesforce_aumento_limite = ConfigMB.string("salesforce_aumento_limite");
            parametros.set("IDCOBIS", contexto.idCobis());
            new Futuro<>(() -> MBSalesforce.registrarEventoSalesforce(contexto, salesforce_aumento_limite, parametros));
        } else {
            new Futuro<>(() -> RestNotificaciones.envioMail(contexto, ConfigMB.string("doppler_aumento_limite"), parametros));
        }

        return RespuestaMB.exito().set("csmIdAuth", contexto.csmIdAuth);
    }

    private static boolean esFechaHoy(String date) {
        return date.equals(new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime()));
    }

    private static boolean noActivoSoftTokenEnElUltimoDia(ContextoMB contexto) {
        return !apiSoftToken.activoSoftTokenEnElUltimoDia(contexto);
    }

    private static boolean noModificoDatosPersonalesEnElUltimoDia(ContextoMB contexto) {
        RespuestaMB respuesta = isRiskForChangeInformation(contexto);
        if (respuesta.hayError())
            return false;
        return (boolean) respuesta.get("enableb_operator");
    }

    private static boolean esFechaPosteriorHoy(String date) {
        return LocalDate.parse(date).isAfter(LocalDate.now());
    }

    private static SqlResponseMB registroSolicitudLimite(ContextoMB contexto, Cuenta cuenta) {
        String idCuenta = contexto.parametros.string("idCuenta");
        String fecha = contexto.parametros.date("fecha", "d/M/yyyy", "yyyy-MM-dd");
        BigDecimal monto = contexto.parametros.bigDecimal("monto");
        SqlTransferencia sqlTransferencia = new SqlTransferencia();

        Date dateProgramado = Date.from(getLocalDatetimeByFormat(fecha + " 00:00:00", "yyyy-MM-dd HH:mm:ss")
                .atZone(ZoneId.systemDefault()).toInstant());
        LocalDateTime diaCreacion = LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT);
        Date dateCreacion = Date.from(diaCreacion.atZone(ZoneId.systemDefault()).toInstant());
        return sqlTransferencia.insertSolicitudLimiteTransferencia(new Date(), dateCreacion, contexto.idCobis(), null,
                contexto.ip(), "HB", null, null, idCuenta, monto.doubleValue(), cuenta.moneda(), dateProgramado);
    }

    private static RespuestaMB validateAumentarLimite(ContextoMB contexto) {
        String pattern = "yyyy-MM-dd HH:mm:ss";
        SqlTransferencia sqlTransferencia = new SqlTransferencia();
        String idCuenta = contexto.parametros.string("idCuenta");
        String fecha = contexto.parametros.date("fecha", "d/M/yyyy", "yyyy-MM-dd");
        BigDecimal monto = contexto.parametros.bigDecimal("monto");
        String csmId = contexto.parametros.string("csmId", "");
        String checksum = contexto.parametros.string("checksum", "");

        if (Objeto.anyEmpty(idCuenta, fecha, monto))
            return RespuestaMB.parametrosIncorrectos();

        boolean esMigrado = contexto.esMigrado(contexto);

        if (esMigrado && Objeto.anyEmpty(csmId, checksum))
            return RespuestaMB.parametrosIncorrectos();

        boolean isEnableSf = MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_sf_aumento_limite");

        if (isEnableSf) {
            RespuestaMB respuestaValidaTransaccion = contexto.validarTransaccion(contexto, esMigrado, "aumento-limite-transferencia", JourneyTransmitEnum.MB_INICIO_SESION);
            if (respuestaValidaTransaccion.hayError())
                return respuestaValidaTransaccion;
        }

        ApiResponseMB responseCatalogo = RestCatalogo.calendarioFecha(contexto, fecha);
        if (!responseCatalogo.hayError()) {
            try {
                if (!DIA_HABIL.equals(responseCatalogo.objetos().get(0).string("esDiaHabil"))) {
                    return RespuestaMB.estado("DIA_NO_HABIL", contexto.csmIdAuth);
                }
            } catch (Exception e) {
            }
        }

        LocalDateTime fechaProgramada = getLocalDatetimeByFormat(fecha + " 00:00:00", pattern);
        if (!isFechaInicioValida(fechaProgramada)) {
            return RespuestaMB.estado("DIA_NO_PERMITIDO", contexto.csmIdAuth);
        }

        LocalDateTime diaCreacion = LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(pattern);
        SqlResponseMB response = sqlTransferencia.obtenerSolicitudPorDiaCreacion(contexto.idCobis(),
                diaCreacion.format(dtf), idCuenta);

        if (response.hayError) {
            return RespuestaMB.error();
        }

        if (response.registros.size() >= ConfigMB.integer("nro_peticiones_diarias", 1)) {
            return RespuestaMB.estado("SOLICITUD_X_HOY_DIA_ALCANZADA", contexto.csmIdAuth);
        }

        /*
         * Agrego las validaciones correspondientes al flujo para que se puedan realizar
         * transferencias especiales el mismo día caso contrario sigue flujo de
         * validaciones normal
         */
        /*
         * 1 - Las condiciones son: fecha sea del día de hoy
         *
         * 2 - no haya activado soft token en las últimas 24 horas
         *
         * 3 - no haya modificado datos personales en las últimas 24 horas
         */
        /*
        // SE SACA PARA EVITAR FRAUDES
        if (!(esFechaHoy(fecha) && noActivoSoftTokenEnElUltimoDia(contexto)
                && noModificoDatosPersonalesEnElUltimoDia(contexto))) {
            // agrego validacion antes de ejecutar sp porque ya no tendrá la validación
            if (!esFechaPosteriorHoy(fecha))
                return RespuestaMB.estado("FECHA_ANTERIOR_A_HOY");
        }
        */

        if (!esFechaPosteriorHoy(fecha))
            return RespuestaMB.estado("FECHA_ANTERIOR_A_HOY", contexto.csmIdAuth);

        return null;
    }

    private static boolean isFechaInicioValida(LocalDateTime fechaProgramada) {
        LocalDateTime nowDatetime = LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT);
        // modifico por el desarrollo de trasnferencias especiales que puedan el mismo
        // día
//		LocalDateTime LimitDay = nowDatetime.plusDays(1);
//		return !fechaProgramada.isBefore(LimitDay);
        return !fechaProgramada.isBefore(nowDatetime);
    }

    private static LocalDateTime getLocalDatetimeByFormat(String fecha, String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return LocalDateTime.parse(fecha, formatter);
    }

    /* ========== TRANSFERENCIA ========== */
    public static RespuestaMB conceptos(ContextoMB contexto) {
        Boolean traerSoloItemsPagoSueldo = contexto.parametros.bool("traerSoloItemsPagoSueldo", false);
        Boolean traerSinItemsPagoSueldo = contexto.parametros.bool("traerSinItemsPagoSueldo", false);
        Objeto conceptos = new Objeto();

        if (contexto.idCobis() == null)
            return RespuestaMB.sinPseudoSesion();

        Map<String, String> mapa = traerSoloItemsPagoSueldo ? TransferenciaService.conceptosPagoSueldos()
                : traerSinItemsPagoSueldo ? TransferenciaService.conceptosTransferencias()
                : TransferenciaService.conceptos();
        for (String clave : mapa.keySet()) {
            String descripcion = mapa.get(clave);
            conceptos.add(new Objeto().set("id", clave).set("descripcion", descripcion));
        }

        return RespuestaMB.exito("conceptos", conceptos);
    }

    public static RespuestaMB cuentaTercero(ContextoMB contexto) {
        String cuentaDestino = contexto.parametros.string("cuentaDestino");
        Boolean prioridadCoelsa = contexto.parametros.bool("prioridadCoelsa", false);
        Boolean ignorarLink = contexto.parametros.bool("ignorarLink", false);

        if (contexto.idCobis() == null) {
            return RespuestaMB.estado("SIN_PSEUDO_SESION");
        }
        if (Objeto.anyEmpty(cuentaDestino)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        if (!prioridadCoelsa) {
            TarjetaDebito TDdefect = contexto.tarjetaDebitoPorDefecto();

            if (TDdefect == null) {
                return RespuestaMB.estado("SIN_TARJETA_DEBITO");
            }
        }

        CuentaTercero cuentaTercero;

        if (ignorarLink) {
            cuentaTercero = new CuentaTercero(contexto, cuentaDestino, ignorarLink);
        } else {
            cuentaTercero = new CuentaTercero(contexto, cuentaDestino);
        }

        if (prioridadCoelsa) {
            if (cuentaTercero.cuentaCoelsa.string("codigo").equals("9904")) {
                return RespuestaMB.estado("ALIAS_NO_EXISTE");
            }
            if (cuentaTercero.cuentaCoelsa.string("codigo").equals("0110")) {
                return RespuestaMB.estado("ALIAS_NO_EXISTE");
            }
            if (cuentaTercero.cuentaCoelsa.string("codigo").equals("0160")) {
                return RespuestaMB.estado("CUENTA_INACTIVA");
            }
            if (cuentaTercero.cuentaCoelsa.string("codigo").equals("0170")) {
                return RespuestaMB.estado("ALIAS_NO_EXISTE");
            }
            if (cuentaTercero.cuentaLink.hayError() && cuentaTercero.cuentaCoelsa.hayError()) {
                return RespuestaMB.estado("CUENTA_INACTIVA");
            }
        }
        if (!cuentaTercero.cuentaEncontrada && prioridadCoelsa) { // devuelvo lo mismo que antes para debin, asi no
            // cambio
            // esa funcionalidad
            return RespuestaMB.estado("ALIAS_NO_EXISTE");
        }

        if (!cuentaTercero.cuentaEncontrada && !prioridadCoelsa) {
            if (CuentasService.esAlias(cuentaDestino)) {
                return RespuestaMB.estado("ALIAS_NO_EXISTE");
            } else if (cuentaTercero.cuentaCoelsa.json == null && cuentaTercero.cuentaBH.json == null
                    && cuentaTercero.cuentaLink.json == null) {
                return RespuestaMB.estado("NO_EXISTE_DATO"); // en este caso no trajo nada
            } else {
                return RespuestaMB.error();
            }
        }

        if (!CuentasService.esAlias(cuentaDestino) && !CuentasService.esCuentaBH(cuentaDestino)
                && !CuentasService.esCbuBH(cuentaDestino)) {
            if (!cuentaTercero.esCvu() && cuentaTercero.cuentaLink.hayError() && !prioridadCoelsa) {
                if (CuentasService.esCbu(cuentaDestino) && cuentaTercero.cuentaLink.hayError()
                        && "CBU INCORRECTO".equals(cuentaTercero.cuentaCoelsa.string("mensajeAlUsuario"))) {
                    return RespuestaMB.estado("CBU_NO_EXISTE");
                }
                return RespuestaMB.error();
            }
        }

        if (cuentaTercero.cuentaCoelsa.hayError() && (prioridadCoelsa || CuentasService.esAlias(cuentaDestino))) {
            if ("ALIAS NO EXISTE".equals(cuentaTercero.cuentaCoelsa.string("mensajeAlUsuario")))
                return RespuestaMB.estado("ALIAS_NO_EXISTE");
            else if ("CBU NO EXISTE".equals(cuentaTercero.cuentaCoelsa.string("mensajeAlUsuario")))
                return RespuestaMB.estado("CBU_NO_EXISTE");
            else
                return RespuestaMB.estado("ERROR_COELSA");
        }

        Objeto item = new Objeto();
        item.set("id", cuentaTercero.cbu());
        item.set("beneficiario", cuentaTercero.titular());
        item.set("cbu", cuentaTercero.cbu());
        item.set("cbuFormateada", Formateador.cbu(cuentaTercero.cbu()));
        item.set("alias", cuentaTercero.alias());
        item.set("cuit", cuentaTercero.cuit());
        item.set("tipo", cuentaTercero.tipo());
        if ("AHO".equals(cuentaTercero.tipo())) {
            item.set("descripcionCorta", "CA");
        } else if ("CTE".equals(cuentaTercero.tipo())) {
            item.set("descripcionCorta", "CC");
        } else {
            item.set("descripcionCorta", "***");
        }

        item.set("numeroCuenta", cuentaTercero.numero());
        Set<String> idMonedas = cuentaTercero.idMonedas();

        if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_cuentaTercero_moneda_pordefecto")
                && idMonedas != null && idMonedas.size() == 2) {
            item.set("idMoneda", "80");
        } else {
            item.set("idMoneda", cuentaTercero.idMoneda());
        }

        item.set("bancoP", cuentaTercero.banco());
        String codigoBanco = !Objeto.empty(cuentaTercero.cuentaCoelsa) && !cuentaTercero.cuentaCoelsa.hayError()
                ? cuentaTercero.cuentaCoelsa.string("nroBco")
                : Formateador.cbu(cuentaTercero.cbu()).substring(0, 3);

        item.set("codigoBanco", codigoBanco);
        item.set("banco", RestCatalogo.bancoFiltrado(codigoBanco));
        item.set("logo", RestCatalogo.bancoLogo(codigoBanco));
        item.set("cuentaVirtual",
                !Objeto.empty(cuentaTercero.cuentaCoelsa) && !cuentaTercero.cuentaCoelsa.hayError()
                        ? cuentaTercero.cuentaCoelsa.bool("cuentaVirtual")
                        : false);
        item.set("idMonedas", idMonedas);

        if (prioridadCoelsa) {
            String[] bancosLink = ConfigMB.string("bancos_link",
                            "011_014_020_029_044_045_056_083_086_093_094_097_203_247_254_268_277_281_301_309_311_312_315_321_330_341_386_426_431_432")
                    .split("_");

            Cuenta cuenta = contexto.cuentas().get(0);

            String fechaAlta = cuenta.fechaAlta("dd/MM/yyyy");
            String fechaActual = new SimpleDateFormat("dd/MM/yyyy").format(new Date());

            if (fechaAlta.equals(fechaActual))
                return RespuestaMB.estado("FECHA_ALTA_CUENTA_HOY");

            Boolean esMismoTitular = cuentaTercero.mismoTitularColesa(contexto.persona().cuit());

            item.set("esBancoHipotecario", Objeto.setOf("044").contains(cuentaTercero.cbu().substring(0, 3)));
            item.set("esMismoTitular", cuentaTercero.mismoTitularColesa(contexto.persona().cuit()));
            item.set("pickUpAutomatico", Objeto.setOf(bancosLink).contains(cuentaTercero.cbu().substring(0, 3)));

            if (!esMismoTitular) {
                Boolean esCotitular = cuentaTercero.esCotitularCoelsa(contexto.persona().cuit());

                item.set("esCotitular", esCotitular);
            }
        }

        Objeto infoAdicional = new Objeto();
        if (cuentaTercero != null) {
            EnumPropuestasInversion propuesta = (EnumPropuestasInversion) MBInversion
                    .evaluarPropuestaInversionPreTransferencia(contexto, cuentaTercero).get("propuesta");
            infoAdicional.set("codMensajeUsuario", propuesta.getCodigoNemonicoPropuesta());
        }

        if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendidoIdentificaCbuCVu")) {
            item.set("esCvu", cuentaTercero.esCvu());
            item.set("esCbu", cuentaTercero.esCuentaBH() || (!cuentaTercero.esCuentaBH() && !cuentaTercero.esCvu()));
        }

        if ("".equals(item.string("id")) && "".equals(item.string("beneficiario")) && "".equals(item.string("cbu"))
                && "".equals(item.string("alias")) && "".equals(item.string("cuit"))) {
            return RespuestaMB.error();
        }

        RespuestaMB respuesta = new RespuestaMB();
        respuesta.set("cuenta", item);
        respuesta.set("infoAdicional", infoAdicional);
        return respuesta;
    }

    public static RespuestaMB cuentaTerceroCoelsa(ContextoMB contexto) {
        contexto.parametros.set("prioridadCoelsa", true);
        contexto.parametros.set("ignorarLink", true);
        return cuentaTercero(contexto);
    }

    private static RespuestaMB bloqueo24Horas(ContextoMB contexto, Boolean transferenciaCuentaPropia) {
        DisableService disableService = new DisableService();
        String beneficiariId = contexto.parametros.string("beneficiarioId");
        if (!transferenciaCuentaPropia) {
            if (Objeto.anyEmpty(beneficiariId)) {
                return RespuestaMB.parametrosIncorrectos();
            }

            LocalDateTime nowTime = LocalDateTime.now();
            nowTime = nowTime.plusHours(-1 * disableService.calculateHourDelay(nowTime));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String inicio = nowTime.format(formatter);
            String tipo = ConfigMB.string(esClienteNuevo(contexto, inicio) ? "cambio_information_no_permitido_nuevo" : "cambio_information_no_permitido");
            List<Objeto> registros = ContextoMB.obtenerContador(contexto.idCobis(), tipo, inicio);
            Boolean isContactoTransferenciaAgendado = contexto.agendada(beneficiariId);
            if (Objects.isNull(registros) || Objects.isNull(isContactoTransferenciaAgendado)) {
                return RespuestaMB.estado("ERROR");
            }
            Boolean permission = disableService.getEnabledToOperator(registros);
            if (!permission && !isContactoTransferenciaAgendado) {
                return RespuestaMB.estado("ERROR_TRANSFER_BLOCK");
            }
        }
        return null;
    }

    public static Objeto transferir(ContextoMB contexto) {
        String cuentaOrigen = contexto.parametros.string("cuentaOrigen");
        String cuentaDestino = contexto.parametros.string("cuentaDestino");
        BigDecimal monto = contexto.parametros.bigDecimal("monto");
        String concepto = contexto.parametros.string("concepto", "VAR");
        Boolean empleadoDomestico = contexto.parametros.bool("empleadoDomestico", false);
        String email = contexto.parametros.string("email", "");
        String forzarEmailOrigen = contexto.parametros.string("forzarEmailOrigen", null);
        String beneficiarioId = contexto.parametros.string("beneficiarioId", null);
        String comentario = contexto.parametros.string("comentario", "");
        Boolean prendidoTIS = MBAplicacion.funcionalidadPrendida("mb_prendido_transf_importe_superior");
        String cobisHabilitados = ConfigMB.string("mb_prendido_tis_cobis");
        String csmId = contexto.parametros.string("csmId", "");
        String checksum = contexto.parametros.string("checksum", "");

        if (contexto.idCobis() == null)
            return RespuestaMB.sinPseudoSesion();

        if (Objeto.anyEmpty(cuentaOrigen, cuentaDestino, monto))
            return RespuestaMB.parametrosIncorrectos();

        if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_modo_transaccional_nuevo_beneficiario",
                "prendido_modo_transaccional_nuevo_beneficiario_cobis") && !TransmitMB.isChallengeOtp(contexto, "transferencia") &&
                contexto.cuenta(cuentaDestino) == null) {
            try {
                String sessionToken = contexto.parametros.string(Transmit.getSessionToken(), null);
                if (Objeto.empty(sessionToken))
                    return RespuestaMB.parametrosIncorrectos();

                Cuenta cuentaPayer = contexto.cuenta(cuentaOrigen);
                Futuro<CuentaTercero> futuroCuentaPayee = new Futuro<>(() -> new CuentaTercero(contexto, cuentaDestino));
                CuentaTercero cuentaPayee = futuroCuentaPayee.tryGet();

                if (Objeto.anyEmpty(cuentaPayer, cuentaPayee))
                    return RespuestaMB.error();

                TransactionMBBMBankProcess transactionMBBMBankProcess = new TransactionMBBMBankProcess(contexto.idCobis(),
                        sessionToken,
                        monto,
                        Util.obtenerDescripcionMonedaTransmit(cuentaPayee.idMoneda()), TransmitMB.REASON_TRANSFERENCIA,
                        new TransactionMBBMBankProcess.Payer(contexto.persona().cuit(), cuentaPayer.numero(), Util.getBhCodigo(), TransmitMB.CANAL),
                        new TransactionMBBMBankProcess.Payee(cuentaPayee.cuit(), cuentaPayer.cbu(), cuentaPayee.codigoBanco()));

                RespuestaMB respuesta = TransmitMB.recomendacionTransmit(contexto, transactionMBBMBankProcess, "transferencia");
                if (respuesta.hayError())
                    return respuesta;

            } catch (Exception e) {
            }
        }

        boolean esMigrado = contexto.esMigrado(contexto);
        Boolean transferenciaCuentaPropia = contexto.cuenta(cuentaDestino) != null;

        if (esMigrado && !transferenciaCuentaPropia && Objeto.anyEmpty(csmId, checksum))
            return RespuestaMB.parametrosIncorrectos();

        if (ConfigMB.string("mb_apicanales_transferir", "true").equals("true")) {
            ApiRequestMB request = ApiMB.request("CanalesTransferir", "canales", "POST", "/mb/api/transferir", contexto);
            contexto.parametros.set("idCobis", contexto.idCobis());

            request.body = contexto.parametros;
            request.body.set("prendidoTIS", prendidoTIS);
            request.body.set("cobisHabilitados", cobisHabilitados);

            boolean validaSoftToken = true;
            if (!transferenciaCuentaPropia && cuentaDestino != null)
                validaSoftToken = validarTransaccion(contexto, esMigrado);

            log.info("idCobis: {}, validaSoftToken {}", contexto.idCobis(), validaSoftToken);
            request.body.set("validaSoftToken", validaSoftToken);

            if (ConfigMB.string("mb_apicanales_transferir_cache", "false").equals("true"))
                request.body.set("cache", contexto.sesion().cache);

            log.info("idCobis: {}, body {}", contexto.idCobis(), request.body.toString());

            ApiResponseMB response = ApiMB.response(request);

            log.info("idCobis: {}, response {}", contexto.idCobis(), response);

            if (response.string("path").equals("A")) {
                contexto.limpiarSegundoFactorTransferencia();
                String idComprobante = "transferencia" + "_" + response.string("recibo");
                contexto.sesion().setComprobante(idComprobante, toMap(response.objeto("datos").toMap()));
                ProductosService.eliminarCacheProductos(contexto);
                return RespuestaMB.exito("idComprobante", idComprobante).set("csmIdAuth", contexto.csmIdAuth);
            }
            if (response.string("path").equals("B")) {
                contexto.limpiarSegundoFactorTransferencia();
                String idComprobante = "transferencia" + "_" + response.string("recibo");
                contexto.sesion().setComprobante(idComprobante, toMap(response.objeto("datos").toMap()));
                ProductosService.eliminarCacheProductos(contexto);
                return RespuestaMB.exito("idComprobante", idComprobante).set("csmIdAuth", contexto.csmIdAuth);
            }
            if (response.string("path").equals("C")) {
                contexto.limpiarSegundoFactorTransferencia();
                String idComprobante = "transferencia" + "_" + response.string("operacion");
                contexto.sesion().setComprobante(idComprobante, toMap(response.objeto("datos").toMap()));
                ProductosService.eliminarCacheProductos(contexto);
                return RespuestaMB.exito("idComprobante", idComprobante).set("csmIdAuth", contexto.csmIdAuth);
            }
            if (response.string("path").equals("D")) {
                contexto.limpiarSegundoFactorTransferencia();
                String idComprobante = "transferencia-cvu" + "_" + response.string("idCoelsa");
                contexto.sesion().setComprobante(idComprobante, toMap(response.objeto("datos").toMap()));
                ProductosService.eliminarCacheProductos(contexto);
                return RespuestaMB.exito("idComprobante", idComprobante).set("csmIdAuth", contexto.csmIdAuth);
            }
            ProductosService.eliminarCacheProductos(contexto);
            log.info("Error Json api canales: {}", response.toJson());
            return RespuestaMB.fromJson(response.toJson()).set("csmIdAuth", contexto.csmIdAuth);
        }

        // TODO bloquear en determinados dias y horarios la funcionalidad
        if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendidoHorarioFraude")) {
            SqlResponseMB resDisposivito = SqlRegistroDispositivo.obtenerForzadoRegistroDispositivo(contexto.idCobis());
            if (resDisposivito.registros.size() == 0) {
                if (Util.fueraHorarioFraudes(contexto)) {
                    SqlRequestMB sqlRequestConsulta = SqlMB.request("Select2AgendaTransferencias", "hbs");
                    sqlRequestConsulta.sql = "SELECT * FROM [Hbs].[dbo].[agenda_transferencias] WHERE nro_cliente = ? AND (cbu_destino = ? OR nro_cuenta_destino = ? OR nro_cuenta_destino = ?)";
                    sqlRequestConsulta.parametros.add(contexto.idCobis());
                    sqlRequestConsulta.parametros.add(cuentaDestino);
                    sqlRequestConsulta.parametros.add(cuentaDestino);
                    sqlRequestConsulta.parametros.add(beneficiarioId);
                    SqlResponseMB res = SqlMB.response(sqlRequestConsulta);
                    if (res.registros.size() == 0) {
                        return RespuestaMB.estado("FUERA_HORARIO_F");
                    }
                }
            }
        }

        Boolean prendidoTransferenciasHaberes = MBAplicacion.funcionalidadPrendida(contexto.idCobis(),
                "prendido_transferencias_haberes");

        Cuenta cuenta = contexto.cuenta(cuentaOrigen);
        if (cuenta == null) {
            return RespuestaMB.error();
        }

        if (empleadoDomestico) {
            concepto = "HAB";
        }

        String validacionCuentaDestino = validacionCuentaDestino(contexto, cuenta, cuentaDestino);

        RespuestaMB respuesta = bloqueo24Horas(contexto, transferenciaCuentaPropia);
        if (respuesta != null) {
            return respuesta;
        }

        if (transferenciaCuentaPropia) {
            Cuenta cuentaPropiaDestino = contexto.cuenta(cuentaDestino);
            if (!cuenta.idMoneda().equals(cuentaPropiaDestino.idMoneda())) {
                return RespuestaMB.estado("MONEDAS_DISTINTAS");
            }

            if (!"".equals(validacionCuentaDestino)) {
                return RespuestaMB.estado(validacionCuentaDestino);
            }

            ApiResponseMB response = null;
            ApiRequestMB request = requestTransferenciaCuenta(contexto, cuenta, cuentaPropiaDestino, concepto, null,
                    false);
            try {
                response = ApiMB.response(request, new Date().getTime());
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                if (ConfigMB.bool("log_transaccional", false)) {
                    setearLogTransaccional(response, request, contexto, cuenta, cuentaPropiaDestino, concepto, null);
                }
            }
            ProductosService.eliminarCacheProductos(contexto);
            if (response.hayError()) {
                if (response.string("codigo").equals("258149")) {
                    return RespuestaMB.estado("ERROR_FUNCIONAL").set("mensaje",
                            ConfigMB.string("leyenda_normativo_7072_transferencia_intra_bh"));
                }
                String error = "ERROR";
                error = response.string("mensajeAlUsuario").contains("CAPTURAR TARJETA USO NO AUTORIZADO")
                        ? "TARJETA_USO_NO_AUTORIZADO"
                        : error;

                if (response.string("codigo").equals("2")) {
                    return RespuestaMB.estado("LIMITE_SUPERADO");
                }
                if (response.string("mensajeAlUsuario").equals("Supera el maximo diario de transferencia")) {
                    return RespuestaMB.estado("LIMITE_SUPERADO");
                }
                if (response.string("mensajeAlUsuario").contains("Limite No Disponible para realizar la operacion.")) {
                    return RespuestaMB.estado("LIMITE_SUPERADO");
                }

                if ("400".equals(response.string("codigo")) || "503".equals(response.string("codigo"))) {
                    return RespuestaMB.estado("ERROR_CONFIRMACION");
                }

                return RespuestaMB.estado(error);
            }

            if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_ocultar_mail_cvu")) {
                String cdestino = request.body().string("cuentaDestino");
                if (contexto.primeraTransferencia(cdestino)) {
                    contexto.registrarTransferencia(cdestino);
                }
            }

            if (MBSalesforce.prendidoSalesforce(contexto.idCobis())) {
                try {
                    String simboloMoneda = Formateador.simboloMoneda(response.string("monedaOrigen"));
                    Objeto parametros = new Objeto();
                    parametros.set("IDCOBIS", contexto.idCobis());
                    parametros.set("NOMBRE", contexto.persona().nombre());
                    parametros.set("APELLIDO", contexto.persona().apellido());
                    parametros.set("CANAL", ConfigMB.string("salesforce_nombre_canal"));
                    parametros.set("CUENTA_ORIGEN", request.body().string("cuentaOrigen").concat(" ").concat(contexto.persona().apellidos())
                            .concat(" ").concat(contexto.persona().nombres()));
                    parametros.set("CUENTA_DESTINO", request.body().string("cuentaDestino").concat(" ").concat(contexto.persona().apellidos())
                            .concat(" ").concat(contexto.persona().nombres()));
                    parametros.set("IMPORTE", simboloMoneda + " " + Formateador.importe(response.bigDecimal("monto")));
                    parametros.set("FECHA", new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
                    parametros.set("NUMERO_OPERACION",
                            StringUtils.isNotBlank(response.string("idProceso")) ? response.string("idProceso")
                                    : response.string("operacion"));
                    parametros.set("TIPO_TRANSFERENCIA", "Propia");
                    parametros.set("MENSAJE", comentario);
                    parametros.set("EMAIL", email);
                    parametros.set("ES_DESTINO", false);
                    parametros.set("CONCEPTO", concepto);
                    new Futuro<>(() -> MBSalesforce.registrarEventoSalesforce(contexto, ConfigMB.string("salesforce_transferencia_in_out"), parametros));
                } catch (Exception e) {

                }
            }
            String idComprobante = "transferencia" + "_" + response.string("recibo");
            setearComprobanteTransferencia(response, request, contexto, idComprobante, null, "", true, comentario);
            return RespuestaMB.exito("idComprobante", idComprobante).set("csmIdAuth", contexto.csmIdAuth);
        }

        RespuestaMB respuestaPausada = MBTarjetas.verificarTarjetaDebitoPausadaEnCuenta(cuenta, contexto);
        if (respuestaPausada != null)
            return respuestaPausada;

        if (!transferenciaCuentaPropia && cuentaDestino != null) { // transferencia a terceros
            CuentaTercero cuentaTercero = new CuentaTercero(contexto, cuentaDestino);

            contexto.parametros.set("cbu", cuentaTercero.cbu());

            RespuestaMB respuestaValidaTransaccion = contexto.validarTransaccion(contexto, esMigrado, "transferencia", JourneyTransmitEnum.MB_INICIO_SESION);
            if (respuestaValidaTransaccion.hayError())
                return respuestaValidaTransaccion;

            if (!"".equals(validacionCuentaDestino)) {
                return RespuestaMB.estado(validacionCuentaDestino, contexto.csmIdAuth);
            }

            Boolean esCuentaBH = cuentaTercero.esCuentaBH();
            Boolean esCuentaCVU = cuentaTercero.esCvu();
            Boolean esCuentaOtroBanco = !esCuentaBH && !esCuentaCVU;
            Boolean esEspecial = transferenciaEspecial(contexto, cuenta, monto);

            if (esCuentaBH) { // del mismo banco
                ApiRequestMB request = requestTransferenciaCuenta(contexto, cuenta, null, concepto, cuentaTercero,
                        esEspecial);
                if (prendidoTransferenciasHaberes) {
                    request.body("servicio", empleadoDomestico ? "99" : ("HAB".equals(concepto) ? "98" : null));
                } else {
                    request.body("servicio", empleadoDomestico ? "99" : null);
                }

                ApiResponseMB response = null;
                try {
                    response = ApiMB.response(request, new Date().getTime());
                } finally {
                    if (ConfigMB.bool("log_transaccional", false)) {
                        setearLogTransaccional(response, request, contexto, cuenta, null, concepto, cuentaTercero);
                    }
                }
                ProductosService.eliminarCacheProductos(contexto);
                if (response.hayError()) {
                    if (response.string("mensajeAlUsuario").contains("FONDOS INSUFICIENTES")) {
                        return RespuestaMB.estado("SALDO_INSUFICIENTE", contexto.csmIdAuth);
                    }
                    if (response.string("codigo").equals("258149")) {
                        return RespuestaMB.estado("ERROR_FUNCIONAL", contexto.csmIdAuth).set("mensaje",
                                ConfigMB.string("leyenda_normativo_7072_transferencia_intra_bh"));
                    }

                    if (response.string("codigo").equals("2")) {
                        return RespuestaMB.estado("LIMITE_SUPERADO", contexto.csmIdAuth);
                    }
                    if (response.string("mensajeAlUsuario").equals("Supera el maximo diario de transferencia")) {
                        return RespuestaMB.estado("LIMITE_SUPERADO", contexto.csmIdAuth);
                    }
                    if (response.string("mensajeAlUsuario")
                            .contains("Limite No Disponible para realizar la operacion.")) {
                        return RespuestaMB.estado("LIMITE_SUPERADO", contexto.csmIdAuth);
                    }

                    String error = "ERROR";
                    error = response.string("mensajeAlUsuario").contains("CAPTURAR TARJETA USO NO AUTORIZADO")
                            ? "TARJETA_USO_NO_AUTORIZADO"
                            : error;
                    if ("400".equals(response.string("codigo")) || "503".equals(response.string("codigo"))) {
                        return RespuestaMB.estado("ERROR_CONFIRMACION", contexto.csmIdAuth);
                    }
                    return RespuestaMB.estado(error, contexto.csmIdAuth);
                }

                String emailOrigen = forzarEmailOrigen != null && !ConfigMB.esProduccion() ? forzarEmailOrigen
                        : contexto.persona().email();
                String emailDestino = email;

                if (emailOrigen != null && !emailOrigen.isEmpty()) {
                    emailAOrigenYDestino(contexto, cuenta.numero(), cuentaTercero,
                            StringUtils.isNotBlank(response.string("idProceso")) ? response.string("idProceso")
                                    : response.string("operacion"),
                            emailOrigen, "origen", "", cuenta.simboloMoneda(), request);
                }

                if (emailDestino != null && !emailDestino.isEmpty()) {
                    emailAOrigenYDestino(contexto, cuenta.numero(), cuentaTercero,
                            StringUtils.isNotBlank(response.string("idProceso")) ? response.string("idProceso")
                                    : response.string("operacion"),
                            emailDestino, "destino", "", cuenta.simboloMoneda(), request);
                }

                if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_ocultar_mail_cvu")) {
                    String cdestino = request.body().string("cuentaDestino");
                    if (contexto.primeraTransferencia(cdestino)) {
                        contexto.registrarTransferencia(cdestino);
                    }
                }

                String idComprobante = "transferencia" + "_" + response.string("recibo");
                setearComprobanteTransferencia(response, request, contexto, idComprobante, cuentaTercero, concepto, false, comentario);
                return RespuestaMB.exito("idComprobante", idComprobante).set("csmIdAuth", contexto.csmIdAuth);
            }

            if (esCuentaOtroBanco) { // de otros bancos
                if (cuentaTercero.cuentaLink.hayError()) {
                    return RespuestaMB.error();
                }

                TarjetaDebito tarjetaDebitoAsociada = contexto.tarjetaDebitoAsociada(cuenta);
                if (tarjetaDebitoAsociada == null) {
                    return RespuestaMB.estado("SIN_TARJETA_DEBITO", contexto.csmIdAuth);
                }

                if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "tarjeta_habilitada_link")) {
                    String tarjetaHabilitada = tarjetaHabilitada(contexto, tarjetaDebitoAsociada.numero());
                    if (!"".equals(tarjetaHabilitada)) {
                        return RespuestaMB.estado(tarjetaHabilitada, contexto.csmIdAuth);
                    }
                }

                ApiRequestMB request = requestTransferenciaOtrasCuenta(contexto, cuenta, concepto, cuentaTercero,
                        esEspecial, tarjetaDebitoAsociada.numero());
                ApiResponseMB response = null;
                try {
                    response = ApiMB.response(request, new Date().getTime());
                    ProductosService.eliminarCacheProductos(contexto);
                    if (response.hayError()) {
                        if (response.string("mensajeAlUsuario").contains("SALDOS INSUFICIENTES")) {
                            return RespuestaMB.estado("SALDO_INSUFICIENTE", contexto.csmIdAuth);
                        }
                        if (response.string("mensajeAlUsuario").contains("CAPTURAR TARJETA USO NO AUTORIZADO")) {
                            return RespuestaMB.estado("TARJETA_USO_NO_AUTORIZADO", contexto.csmIdAuth);
                        }

                        if (response.string("codigo").equals("2")) {
                            return RespuestaMB.estado("LIMITE_SUPERADO", contexto.csmIdAuth);
                        }
                        if (response.string("mensajeAlUsuario").equals("Supera el maximo diario de transferencia")) {
                            return RespuestaMB.estado("LIMITE_SUPERADO", contexto.csmIdAuth);
                        }
                        if (response.string("mensajeAlUsuario")
                                .contains("Limite No Disponible para realizar la operacion.")) {
                            return RespuestaMB.estado("LIMITE_SUPERADO", contexto.csmIdAuth);
                        }

                        if (response.string("codigo").equals("258149")) {
                            return RespuestaMB.estado("ERROR_FUNCIONAL", contexto.csmIdAuth).set("mensaje",
                                    ConfigMB.string("leyenda_normativo_7072_transferencia_intra_bh"));
                        }
                        throw new ApiExceptionMB(response);
                    }
                } catch (Exception e) {
//					String transfRecuperada = pescandoTransferencia(response, contexto, tarjetaDebitoAsociada.numero());
//					if (!"".equals(transfRecuperada)) {
//						return RespuestaMB.estado(transfRecuperada);
//					}
                    if (response.string("mensajeAlUsuario").contains("SALDOS INSUFICIENTES")) {
                        return RespuestaMB.estado("SALDO_INSUFICIENTE", contexto.csmIdAuth);
                    }
                    if (response.string("codigo").equals("258149")) {
                        return RespuestaMB.estado("ERROR_FUNCIONAL", contexto.csmIdAuth).set("mensaje",
                                ConfigMB.string("leyenda_normativo_7072_transferencia_intra_bh"));
                    }

                    if (response.string("codigo").equals("2")) {
                        return RespuestaMB.estado("LIMITE_SUPERADO", contexto.csmIdAuth);
                    }
                    if (response.string("mensajeAlUsuario").equals("Supera el maximo diario de transferencia")) {
                        return RespuestaMB.estado("LIMITE_SUPERADO", contexto.csmIdAuth);
                    }
                    if (response.string("mensajeAlUsuario")
                            .contains("Limite No Disponible para realizar la operacion.")) {
                        return RespuestaMB.estado("LIMITE_SUPERADO", contexto.csmIdAuth);
                    }

                    String error = "ERROR";
                    error = response.string("mensajeAlUsuario").contains("CAPTURAR TARJETA USO NO AUTORIZADO")
                            ? "TARJETA_USO_NO_AUTORIZADO"
                            : error;
                    if ("400".equals(response.string("codigo")) || "503".equals(response.string("codigo"))) {
                        return RespuestaMB.estado("ERROR_CONFIRMACION", contexto.csmIdAuth);
                    }
                    return RespuestaMB.estado(error, contexto.csmIdAuth);
                } finally {
                    if (ConfigMB.bool("log_transaccional", false)) {
                        setearLogTransaccionalOtrosCasos(response, request, contexto, cuenta, concepto, cuentaTercero,
                                tarjetaDebitoAsociada.numero(), "link");
                    }
                }

                String emailOrigen = forzarEmailOrigen != null && !ConfigMB.esProduccion() ? forzarEmailOrigen
                        : contexto.persona().email();
                String emailDestino = email;

                if (emailOrigen != null && !emailOrigen.isEmpty()) {
                    emailAOrigenYDestino(contexto, cuenta.numero(), cuentaTercero,
                            StringUtils.isNotBlank(response.string("idProceso")) ? response.string("idProceso")
                                    : response.string("operacion"),
                            emailOrigen, "origen", "link", cuenta.simboloMoneda(), request);
                }

                if (emailDestino != null && !emailDestino.isEmpty()) {
                    emailAOrigenYDestino(contexto, cuenta.numero(), cuentaTercero,
                            StringUtils.isNotBlank(response.string("idProceso")) ? response.string("idProceso")
                                    : response.string("operacion"),
                            emailDestino, "destino", "link", cuenta.simboloMoneda(), request);
                }

                if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_ocultar_mail_cvu")) {
                    String cdestino = request.body().string("cbuDestino");
                    if (contexto.primeraTransferencia(cdestino)) {
                        contexto.registrarTransferencia(cdestino);
                    }
                }

                String idComprobante = "transferencia" + "_" + response.string("operacion");
                setearComprobanteTransferenciaOtrosCasos(response, request, contexto, idComprobante, cuentaTercero,
                        "link", comentario);
                return RespuestaMB.exito("idComprobante", idComprobante).set("csmIdAuth", contexto.csmIdAuth);
            }

            if (esCuentaCVU) {
                if (!MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_aceptar_cvu"))
                    return RespuestaMB.estado("FUNCIONALIDAD_APAGADA", contexto.csmIdAuth);

                if (cuentaTercero.cuentaCoelsa.hayError())
                    return RespuestaMB.error(contexto.csmIdAuth);

                ApiRequestMB request = requestTransferenciaCuentaCVU(contexto, cuenta, concepto, cuentaTercero);

                ApiResponseMB response = null;
                try {
                    response = ApiMB.response(request, new Date().getTime());
                } finally {
                    if (ConfigMB.bool("log_transaccional", false)) {
                        setearLogTransaccionalOtrosCasos(response, request, contexto, cuenta, concepto, cuentaTercero,
                                null, "cvu");
                    }
                }
                ProductosService.eliminarCacheProductos(contexto);
                if (response.hayError()) {
                    if (response.string("codigo").equals("258149")) {
                        return RespuestaMB.estado("ERROR_FUNCIONAL", contexto.csmIdAuth).set("mensaje",
                                ConfigMB.string("leyenda_normativo_7072_transferencia_intra_bh"));
                    }

                    if (response.string("mensajeAlUsuario").equals("Supera el maximo diario de transferencia")) {
                        return RespuestaMB.estado("LIMITE_SUPERADO", contexto.csmIdAuth);
                    }
                    if (response.string("mensajeAlUsuario")
                            .contains("Limite No Disponible para realizar la operacion.")) {
                        return RespuestaMB.estado("LIMITE_SUPERADO", contexto.csmIdAuth);
                    }

                    String error = "ERROR";
                    error = response.string("mensajeAlUsuario").contains("CAPTURAR TARJETA USO NO AUTORIZADO")
                            ? "TARJETA_USO_NO_AUTORIZADO"
                            : error;
                    if ("400".equals(response.string("codigo")) || "503".equals(response.string("codigo"))) {
                        return RespuestaMB.estado("ERROR_CONFIRMACION", contexto.csmIdAuth);
                    }

                    if (response.string("codigo").equals("1875053")) {
                        if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendidoIdentificaCbuCVu")) {
                            return RespuestaMB.estado("LIMITE_CVU_TRANSFERENCIA", contexto.csmIdAuth);
                        } else {
                            return RespuestaMB.estado("LIMITE_DIARIO_TRANSFERENCIA", contexto.csmIdAuth).set("ingresoLimiteMaximoPesos",
                                    ConfigMB.integer("ingreso_limite_maximo_transferencia_pesos", 20000000));

                        }

                    }
                    return RespuestaMB.estado(error, contexto.csmIdAuth);
                }

                if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_ocultar_mail_cvu")) {
                    String cdestino = request.body().string("destinatario.cuenta.cbu");
                    if (contexto.primeraTransferencia(cdestino)) {
                        contexto.registrarTransferencia(cdestino);
                    }
                }

                if (MBSalesforce.prendidoSalesforce(contexto.idCobis())) {
                    try {
                        String simboloMoneda = Formateador.simboloMoneda(response.string("monedaOrigen"));
                        Objeto parametros = new Objeto();
                        parametros.set("IDCOBIS", contexto.idCobis());
                        parametros.set("NOMBRE", contexto.persona().nombre());
                        parametros.set("APELLIDO", contexto.persona().apellido());
                        parametros.set("CANAL", ConfigMB.string("salesforce_nombre_canal"));
                        parametros.set("CUENTA_ORIGEN", request.body().string("originante.cuenta.numero").concat(contexto.persona().apellidos()).concat(" ")
                                .concat(contexto.persona().nombres()));
                        parametros.set("CUENTA_DESTINO", request.body().string("destinatario.cuenta.cbu").concat(" ")
                                .concat(StringUtils.capitaliseAllWords(cuentaTercero.titular().toLowerCase())));
                        parametros.set("IMPORTE", simboloMoneda + " " + Formateador.importe(request.body().bigDecimal("detalle.importe")));
                        parametros.set("FECHA", response.date("fechaEjecucion", "yyyy-MM-dd'T'HH:mm:ss", "dd/MM/yyyy HH:mm"));
                        parametros.set("ES_DESTINO", false);
                        parametros.set("TIPO_TRANSFERENCIA", "CVU");
                        parametros.set("CONCEPTO", concepto);
                        parametros.set("MENSAJE", comentario);
                        parametros.set("EMAIL", email);
                        parametros.set("NUMERO_OPERACION", response.string("idCoelsa"));
                        new Futuro<>(() -> MBSalesforce.registrarEventoSalesforce(contexto, ConfigMB.string("salesforce_transferencia_in_out"), parametros));
                    } catch (Exception e) {

                    }
                }

                String idComprobante = "transferencia-cvu" + "_" + response.string("idCoelsa");
                setearComprobanteTransferenciaOtrosCasos(response, request, contexto, idComprobante, cuentaTercero,
                        "cvu", comentario);
                return RespuestaMB.exito("idComprobante", idComprobante).set("csmIdAuth", contexto.csmIdAuth);
            }
        }
        return RespuestaMB.error(contexto.csmIdAuth);
    }

    /* emm-20190416-desde */
    public static RespuestaMB compraVentaDolarFueraDeHorario(ContextoMB contexto) {
        String horaCompraVentaDolarDesde = ConfigMB.string("horario_compra_venta_dolares_inicio", "09:30");
        String horaCompraVentaDolarHasta = ConfigMB.string("horario_compra_venta_dolares_fin", "21:00");
        String horaDesdeFormateado = horaCompraVentaDolarDesde;
        String horaHastaFormateado = horaCompraVentaDolarHasta;

        RespuestaMB respuesta = new RespuestaMB();
        respuesta.set("horaDesde", horaCompraVentaDolarDesde);
        respuesta.set("horaHasta", horaCompraVentaDolarHasta);
        respuesta.set("horaDesdeFormateado", horaDesdeFormateado);
        respuesta.set("horaHastaFormateado", horaHastaFormateado);

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        Date hoy = new Date();
        String hora = sdf.format(hoy);
        if (ConfigMB.esProduccion()) {
            if (hora.compareTo(horaCompraVentaDolarDesde) < 0 || hora.compareTo(horaCompraVentaDolarHasta) > 0) {
                respuesta.setEstado("FUERA_DE_HORARIO");
            }
        }

        SimpleDateFormat sdfDayOfWeek = new SimpleDateFormat("u");
        String diaSemana = sdfDayOfWeek.format(hoy);

        if (ConfigMB.esProduccion()) {
            if (diaSemana.equals("6") || diaSemana.equals("7")) {
                respuesta.setEstado("FUERA_DE_HORARIO");
            }
        }

        return respuesta;
    }
    /* emm-20190416-hasta */

    public static String validacionCuentaDestino(ContextoMB contexto, Cuenta cuenta, String cuentaDestino) {

        if (!MBAplicacion.funcionalidadPrendida(contexto.idCobis(),
                "prendido_validacion_transferencia_cuenta_destino")) {
            return "";
        }

        CuentaTercero cuentaTercero = new CuentaTercero(contexto, cuentaDestino);
        if (!cuentaTercero.esCvu() && cuentaTercero.cuentaLink != null
                && !"J".equals(cuentaTercero.cuentaLink.string("tipoPersona"))) {
            if (cuentaTercero.cuentaLink.string("estadoCuenta").equals("DTL")) {
                return (cuenta.esPesos() ? "NO_APTA_TRANSFERENCIA_PESOS" : "NO_APTA_TRANSFERENCIA_DOLARES");
            }
            if (cuentaTercero.cuentaLink.string("estadoCuenta").equals("CL")) {
                return (cuenta.esPesos() ? "CUENTA_CERRADA_PESOS" : "CUENTA_CERRADA_DOLARES");
            }
            if ("2".equals(cuenta.idMoneda()) && cuentaTercero.cuentaLink.string("estadoCuenta").equals("CD")) {
                if (!contexto.persona().cuit()
                        .equals(cuentaTercero.cuentaLink.objetos("titulares").get(0).string("idTributario"))) {
                    return "CUENTA_DESTINO_NO_HABILITADA";
                }
            }
        }
        return "";
    }

    private static ApiRequestMB requestTransferenciaCuenta(ContextoMB contexto, Cuenta cuentaOrigen,
                                                           Cuenta cuentaPropiaDestino, String concepto, CuentaTercero cuentaTercero, Boolean esEspecial) {
        BigDecimal monto = contexto.parametros.bigDecimal("monto");
        Boolean aceptaDDJJ = contexto.parametros.bool("aceptaDDJJ", false);
        String descripcionTransaccion = cuentaTercero == null ? "TransferenciaCuentaPropia" : "TransferenciaBH";

        ApiRequestMB request = null;
        if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_api_transferencias_2")) {
            if (MBAplicacion.funcionalidadPrendida("mb_prendido_transf_importe_superior")) {
                request = ApiMB.request(descripcionTransaccion, "cuentas", "POST", "/v1/especiales/{cbu}",
                        contexto);
                request.path("cbu", cuentaOrigen.cbu());

            } else {
                request = ApiMB.request(descripcionTransaccion, "cuentas", "POST", "/v2/cuentas/{idcuenta}/transferencias",
                        contexto);
            }
        } else {
            request = ApiMB.request(descripcionTransaccion, "cuentas", "POST", "/v1/cuentas/{idcuenta}/transferencias",
                    contexto);
        }

        request.path("idcuenta", cuentaOrigen.numero());
        request.query("cuentapropia", cuentaTercero == null ? "true" : "false");
        request.query("inmediata", "false");
        request.query("aceptaDDJJ", aceptaDDJJ.toString());
        request.body("cuentaOrigen", cuentaOrigen.numero());
        request.body("importe", monto);
        request.body("reverso", false);
        request.body("cuentaDestino", cuentaTercero == null ? cuentaPropiaDestino.numero() : cuentaTercero.numero());
        request.body("tipoCuentaOrigen", cuentaOrigen.idTipo());
        request.body("tipoCuentaDestino", cuentaTercero == null ? cuentaPropiaDestino.idTipo() : cuentaTercero.tipo());
        request.body("idMoneda", cuentaOrigen.idMoneda());
        request.body("modoSimulacion", false);
        request.body("idMonedaDestino",
                cuentaTercero == null ? cuentaPropiaDestino.idMoneda() : cuentaTercero.idMoneda());
        if (cuentaTercero != null) {
            request.query("especial", esEspecial.toString());
        } else {
            request.body("concepto", concepto);
        }

        request.body("descripcionConcepto", concepto);
        request.body("idCliente", contexto.idCobis());
        return request;
    }

    private static void setearLogTransaccional(ApiResponseMB response, ApiRequestMB request, ContextoMB contexto,
                                               Cuenta cuenta, Cuenta cuentaPropiaDestino, String concepto, CuentaTercero cuentaTercero) {
        BigDecimal monto = contexto.parametros.bigDecimal("monto");
        Boolean empleadoDomestico = contexto.parametros.bool("empleadoDomestico", false);
        Boolean esEspecial = transferenciaEspecial(contexto, cuenta, monto);

        try {
            String codigoError = response == null ? "ERROR" : response.hayError() ? response.string("codigo") : "0";
            String transaccion = codigoError.equals("0") ? response.string("recibo") : null;

            String descripcionError = "";
            if (response != null && !codigoError.equals("0")) {
                descripcionError += response.string("codigo") + ".";
                descripcionError += response.string("mensajeAlUsuario") + ".";
            }
            descripcionError = descripcionError.length() > 990 ? descripcionError.substring(0, 990) : descripcionError;

            SqlRequestMB sqlRequest = (cuentaTercero == null)
                    ? SqlMB.request("InsertAuditorTransferenciaCuentaPropia", "hbs")
                    : SqlMB.request("InsertAuditorTransferenciaBH", "hbs");

            sqlRequest.sql = "INSERT INTO [hbs].[dbo].[auditor_transferencia] ";
            sqlRequest.sql += "([momento],[cobis],[idProceso],[ip],[canal],[codigoError],[descripcionError],[tipo],[cuentaOrigen],[cuentaDestino],[importe],[moneda],[concepto],[cuentaPropia],[servicioDomestico],[especial],[tarjetaDebito],[transaccion]) ";
            sqlRequest.sql += "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            sqlRequest.add(new Date()); // momento
            sqlRequest.add(contexto.idCobis()); // cobis
            sqlRequest.add(request.idProceso()); // idProceso
            sqlRequest.add(request.ip()); // ip
            sqlRequest.add("MB"); // canal
            sqlRequest.add(codigoError); // codigoError
            sqlRequest.add(descripcionError); // descripcionError
            sqlRequest.add(cuentaTercero == null ? "propia" : "bh"); // tipo
            sqlRequest.add(cuenta.numero()); // cuentaOrigen
            sqlRequest.add(cuentaTercero == null ? cuentaPropiaDestino.numero() : cuentaTercero.numero()); // cuentaDestino
            sqlRequest.add(monto.toString()); // importe
            sqlRequest.add(cuentaTercero == null ? cuenta.idMoneda() : cuentaTercero.idMoneda()); // moneda
            sqlRequest.add(concepto); // concepto
            sqlRequest.add(cuentaTercero == null ? "true" : "false"); // cuentaPropia
            sqlRequest.add(cuentaTercero == null ? "false" : empleadoDomestico.toString()); // servicioDomestico
            sqlRequest.add(cuentaTercero == null ? "false" : esEspecial.toString()); // especial
            sqlRequest.add(null); // tarjetaDebito
            sqlRequest.add(transaccion); // transaccion

            SqlMB.response(sqlRequest);
        } catch (Exception e) {
        }
    }

    private static void setearComprobanteTransferenciaOtrosCasos(ApiResponseMB response, ApiRequestMB request,
                                                                 ContextoMB contexto, String idComprobante, CuentaTercero cuentaTercero, String caso, String comentario) {
        Boolean prendidoTransferenciasHaberes = MBAplicacion.funcionalidadPrendida(contexto.idCobis(),
                "prendido_transferencias_haberes");
        Boolean empleadoDomestico = contexto.parametros.bool("empleadoDomestico", false);
        String simboloMoneda = Formateador.simboloMoneda(response.string("monedaOrigen"));
        String descripcionConcepto = empleadoDomestico ? "Haberes"
                : TransferenciaService.conceptos().get(request.body().string("motivo"));

        Map<String, String> comprobante = new HashMap<>();
        comprobante.put("NOMBRE_BENEFICIARIO", cuentaTercero.titular());

        if ("link".equalsIgnoreCase(caso)) {
            comprobante.put("FECHA_HORA", validaFechaHora(response));
            comprobante.put("ID_COMPROBANTE", response.string("operacion"));
            comprobante.put("IMPORTE", simboloMoneda + " " + Formateador.importe(response.bigDecimal("importe")));
            if (prendidoTransferenciasHaberes) {
                comprobante.put("TIPO_TRANSFERENCIA",
                        !empleadoDomestico ? "A otro banco" : "Otra Cuenta – Servicio Doméstico");
            } else {
                comprobante.put("TIPO_TRANSFERENCIA", "A otro banco");
            }
            comprobante.put("CUENTA_ORIGEN", request.body().string("cuentaOrigen"));
            comprobante.put("CUENTA_DESTINO", request.body().string("cbuDestino"));
            comprobante.put("CUIT_DESTINO", cuentaTercero.cuit());
            comprobante.put("COMISION", "***");
        } else {
            simboloMoneda = "$";
            comprobante.put("FECHA_HORA", response.date("fechaEjecucion", "yyyy-MM-dd'T'HH:mm:ss", "dd/MM/yyyy HH:mm"));
            comprobante.put("ID_COMPROBANTE", response.string("idCoelsa"));
            comprobante.put("NOMBRE_BENEFICIARIO", cuentaTercero.titular());
            comprobante.put("IMPORTE",
                    simboloMoneda + " " + Formateador.importe(request.body().bigDecimal("detalle.importe")));
            comprobante.put("TIPO_TRANSFERENCIA", "A cuenta virtual");
            comprobante.put("CUENTA_ORIGEN", request.body().string("originante.cuenta.numero"));
            comprobante.put("CUENTA_DESTINO", request.body().string("destinatario.cuenta.cbu"));
            comprobante.put("CUIT_DESTINO", request.body().string("destinatario.idTributario"));
            comprobante.put("ESTADO", response.string("descripcion"));
        }

        comprobante.put("CONCEPTO", descripcionConcepto);
        comprobante.put("IMPUESTOS", "***");

        comprobante.put("NOMBRE_ORIGEN", contexto.persona().nombreCompleto().toUpperCase());
        comprobante.put("NOMBRE_BANCO", Util.obtenerNombreBanco(contexto, request.body().string("cbuDestino"), ""));
        comprobante.put("MENSAJE", comentario);

        insertarDatosComprobantes(contexto, idComprobante.split("_")[1], cuentaTercero.banco(), request.body().string("destinatario.cuenta.cbu"));

        contexto.sesion().setComprobante(idComprobante, comprobante);
        contexto.limpiarSegundoFactor();
    }

    private static void setearComprobanteTransferencia(ApiResponseMB response, ApiRequestMB request,
                                                       ContextoMB contexto, String idComprobante, CuentaTercero cuentaTercero, String concepto, boolean cuentaPropioDestino, String comentario) {
        Boolean empleadoDomestico = contexto.parametros.bool("empleadoDomestico", false);
        String simboloMoneda = Formateador.simboloMoneda(response.string("monedaOrigen"));
        Boolean prendidoTransferenciasHaberes = MBAplicacion.funcionalidadPrendida(contexto.idCobis(),
                "prendido_transferencias_haberes");

        Map<String, String> comprobante = new HashMap<>();
        comprobante.put("FECHA_HORA", validaFechaHora(response));
        comprobante.put("ID_COMPROBANTE", response.string("recibo"));
        comprobante.put("NOMBRE_BENEFICIARIO",
                cuentaTercero == null ? contexto.persona().nombreCompleto() : cuentaTercero.titular());
        comprobante.put("IMPORTE", simboloMoneda + " " + Formateador.importe(response.bigDecimal("monto")));
        comprobante.put("TIPO_TRANSFERENCIA", "A cuenta propia");
        comprobante.put("CUENTA_ORIGEN", request.body().string("cuentaOrigen"));
        comprobante.put("CUENTA_DESTINO", cuentaTercero == null ? request.body().string("cuentaDestino") : cuentaTercero.cbu());
        comprobante.put("CUIT_DESTINO", cuentaTercero == null ? contexto.persona().cuit() : cuentaTercero.cuit());
        comprobante.put("COMISION", simboloMoneda + " 0,00");
        comprobante.put("IMPUESTOS", simboloMoneda + " 0,00");

        if (cuentaTercero != null) {
            if (prendidoTransferenciasHaberes) {
                comprobante.put("TIPO_TRANSFERENCIA", empleadoDomestico ? "Sueldos – Serv Dom."
                        : ("HAB".equals(concepto) ? "Sueldos - Haberes" : "A cuenta tercero"));
                comprobante.put("CONCEPTO", empleadoDomestico ? "Sueldos – Serv Dom."
                        : ("HAB".equals(concepto) ? "Sueldos - Haberes"
                        : TransferenciaService.conceptos().get(request.body().string("descripcionConcepto"))));

            } else {
                comprobante.put("TIPO_TRANSFERENCIA",
                        !empleadoDomestico ? "A cuenta tercero" : "Otra Cuenta – Servicio Doméstico");
                comprobante.put("CONCEPTO", empleadoDomestico ? "Haberes"
                        : TransferenciaService.conceptos().get(request.body().string("descripcionConcepto")));

            }
        } else {
            comprobante.put("CONCEPTO",
                    TransferenciaService.conceptos().get(request.body().string("descripcionConcepto")));
        }

        comprobante.put("NOMBRE_ORIGEN", contexto.persona().nombreCompleto().toUpperCase());
        comprobante.put("NOMBRE_BANCO", Util.obtenerNombreBanco(contexto, request.body().string("cuentaDestino"), ""));
        comprobante.put("MENSAJE", comentario);

        contexto.sesion().setComprobante(idComprobante, comprobante);
        if (cuentaTercero != null) {
            contexto.limpiarSegundoFactor();
        }
    }

    private static String validaFechaHora(ApiResponseMB response) {
        String fechaHora = response.date("fechaHora", "yyyy-MM-dd HH:mm:ss", "dd/MM/yyyy HH:mm");
        if (fechaHora.isEmpty()) {
            fechaHora = response.date("fechaHora", "yyyy/MM/dd HH:mm:ss", "dd/MM/yyyy HH:mm");
        }
        if (fechaHora.isEmpty()) {
            fechaHora = response.date("fecha", "yyyy-MM-dd", "dd/MM/yyyy");
        }
        if (fechaHora.isEmpty()) {
            fechaHora = response.date("fecha", "yyyy/MM/dd", "dd/MM/yyyy");
        }
        if (fechaHora.isEmpty()) {
            fechaHora = response.date("fecha", "yyyy/MM/dd HH:mm:ss", "dd/MM/yyyy");
        }
        if (fechaHora.isEmpty()) {
            fechaHora = response.date("fecha", "yyyy-MM-dd HH:mm:ss", "dd/MM/yyyy");
        }
        return fechaHora;
    }

    public static ApiResponseMB cuentasGetLimites(ContextoMB contexto, Cuenta cuenta, Boolean fechaActual) {

        ApiRequestMB request = ApiMB.request("CuentasGetLimites", "cuentas", "GET", "/v1/cuentas/{idcuenta}/limites",
                contexto);
        request.path("idcuenta", cuenta.numero());
        request.query("idcliente", contexto.idCobis());
        request.query("fechadesde", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        request.query("fechahasta", fechaActual ? new SimpleDateFormat("yyyy-MM-dd").format(new Date()) : "2099-01-01");
        request.query("idmoneda", cuenta.idMoneda());

        ApiResponseMB response = ApiMB.response(request);

        if (response.hayError()) {
            throw new ApiExceptionMB(response);
        }
        return response;
    }

    public static ApiResponseMB consultarTIS(ContextoMB contexto, Cuenta cuenta, Boolean fechaActual) {
        TarjetaDebito tdAsociada = contexto.tarjetaDebitoAsociada(cuenta);

        ApiRequestMB request = ApiMB.request("ConsultarTIS", "cuentas", "POST", "/v1/especial/consultar",
                contexto);
        request.path("cbu", cuenta.cbu());
        request.query("fecha", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        request.query("nroTarjeta", tdAsociada.numero());

        ApiResponseMB response = ApiMB.response(request);

        if (response.hayError()) {
            throw new ApiExceptionMB(response);
        }
        return response;
    }

    private static Boolean transferenciaEspecial(ContextoMB contexto, Cuenta cuenta, BigDecimal monto) {
        Boolean esEspecial = false;
        ApiResponseMB responseLimiteByCuenta = null;

        try {
            if (MBAplicacion.funcionalidadPrendida("mb_prendido_transf_importe_superior")) {
                responseLimiteByCuenta = consultarTIS(contexto, cuenta, true);
            } else {
                responseLimiteByCuenta = cuentasGetLimites(contexto, cuenta, true);
            }

            if (responseLimiteByCuenta.objetos().isEmpty()) {
                esEspecial |= "80".equals(cuenta.idMoneda())
                        && ConfigMB.bigDecimal("configuracion_limite_transferencia_pesos").compareTo(monto) < 0;
                esEspecial |= "2".equals(cuenta.idMoneda())
                        && ConfigMB.bigDecimal("configuracion_limite_transferencia_dolares").compareTo(monto) < 0;
            } else {
                BigDecimal importe = responseLimiteByCuenta.objetos().get(0).bigDecimal("importe");

                esEspecial = importe.compareTo(monto) >= 0 ? true : false;
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return esEspecial;
    }

    protected static Boolean validaCuentaPermitida(String cuentaDestino, CuentaTercero cuentaTercero) {
        return (CuentasService.esCbu(cuentaDestino) || CuentasService.esAlias(cuentaDestino))
                && cuentaTercero.cuentaLink.hayError();
    }

    private static void emailAOrigenYDestino(ContextoMB contexto, String cuentaNumero, CuentaTercero cuentaTercero,
                                             String proceso, String emailPara, String tipo, String link, String simboloMoneda, ApiRequestMB request) {
        BigDecimal monto = contexto.parametros.bigDecimal("monto");
        ApiRequestMB requestMail = ApiMB.request("NotificacionesPostCorreoElectronico", "notificaciones", "POST",
                "/v1/correoelectronico", contexto);

        requestMail.body("de", "aviso@mail-hipotecario.com.ar");
        requestMail.body("para", emailPara);
        requestMail.body("plantilla", ConfigMB.string("doppler_transferencia_" + tipo));
        Objeto parametros = requestMail.body("parametros");
        parametros.set("Subject", "Transferencia BH");
        if ("origen".equalsIgnoreCase(tipo)) {
            parametros.set("NOMBRE", contexto.persona().nombres());
            parametros.set("APELLIDO", contexto.persona().apellidos());
            parametros.set("CANAL", ConfigMB.string("salesforce_nombre_canal"));
            parametros.set("CUENTA_ORIGEN", cuentaNumero.concat(" ").concat(contexto.persona().apellidos()).concat(" ")
                    .concat(contexto.persona().nombres()));
        } else {
            String descripcion = contexto.parametros.string("descripcion", "");
            parametros.set("NOMBRE_USUARIO", contexto.persona().nombreCompleto());
            parametros.set("CUENTA_DESTINO_TITULAR",
                    StringUtils.capitaliseAllWords(cuentaTercero.titular().toLowerCase()));
            parametros.set("FECHA", new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
            parametros.set("COMENTARIOS", descripcion);
        }

        parametros.set("IMPORTE", simboloMoneda.concat(" ").concat(Formateador.importe(monto)));
        parametros.set("CUENTA_DESTINO",
                "link".equalsIgnoreCase(link)
                        ? cuentaTercero.cuentaLink.string("cuenta").concat(" ")
                        .concat(StringUtils.capitaliseAllWords(cuentaTercero.titularCuentaLink().toLowerCase()))
                        : cuentaTercero.numero().concat(" ")
                        .concat(StringUtils.capitaliseAllWords(cuentaTercero.titular().toLowerCase())));
        parametros.set("NRO_OPERACION", proceso);
        if (MBSalesforce.prendidoSalesforce(contexto.idCobis())) {
            parametros.set("CANAL", ConfigMB.string("salesforce_nombre_canal"));
            parametros.set("IDCOBIS", contexto.idCobis());
            parametros.set("FECHA", new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
            parametros.set("CONCEPTO",
                    TransferenciaService.conceptos().get(request.body().string("descripcionConcepto")));
            parametros.set("CUENTA_ORIGEN", request.body().string("cuentaOrigen").concat(" ").concat(contexto.persona().apellidos())
                    .concat(" ").concat(contexto.persona().nombres()));
            parametros.set("NUMERO_OPERACION", proceso);
            parametros.set("TIPO_TRANSFERENCIA", "CBU");
            parametros.set("NOMBRE", contexto.persona().nombres());
            parametros.set("APELLIDO", contexto.persona().apellidos());
            parametros.set("MENSAJE", contexto.parametros.string("descripcion", ""));
            parametros.set("EMAIL", emailPara);
            new Futuro<>(() -> MBSalesforce.registrarEventoSalesforce(contexto, ConfigMB.string("salesforce_transferencia_in_out"), parametros));

        } else
            ApiMB.response(requestMail, new Date().getTime());
    }

    private static ApiRequestMB requestTransferenciaOtrasCuenta(ContextoMB contexto, Cuenta cuentaOrigen,
                                                                String concepto, CuentaTercero cuentaTercero, Boolean esEspecial, String tarjetaDebitoAsociada) {
        ApiRequestMB request = null;
        String cuitOrigen = contexto.persona().cuit();
        String cuitDestino = cuentaTercero.cuentaLink.objetos("titulares").get(0).string("idTributario");
        Long timestamp = new Date().getTime();
        Boolean empleadoDomestico = contexto.parametros.bool("empleadoDomestico", false);
        Boolean aceptaDDJJ = contexto.parametros.bool("aceptaDDJJ", false);
        BigDecimal monto = contexto.parametros.bigDecimal("monto");

        request = ApiMB.request("TransferenciaOtroBanco", "cuentas", "POST", "/v2/cuentas/{idcuenta}/transferencias",
                contexto);

        request.path("idcuenta", cuentaOrigen.numero());
        request.query("cuentapropia", "false");
        request.query("inmediata", "true");
        request.query("especial", esEspecial.toString());
        request.query("aceptaDDJJ", aceptaDDJJ.toString());

        request.body("cuentaOrigen", cuentaOrigen.numero());
        request.body("idMoneda", cuentaOrigen.idMoneda());
        request.body("motivo", concepto);

        String descripcionConcepto = empleadoDomestico ? "Haberes"
                : TransferenciaService.conceptos().get(request.body().string("motivo"));

        if ("HAB".equals(concepto)) {
            descripcionConcepto = "Sueldos - Haberes";
        } else {
            descripcionConcepto = TransferenciaService.conceptos().get(concepto);
        }
        if (empleadoDomestico) {
            descripcionConcepto = "Sueldos - Serv.Dom.";
        }
        request.body("descripcionConcepto", descripcionConcepto);
        request.body("cuentaDestino", cuentaTercero.cuentaLink.string("cuenta"));
        request.body("estadoDestino", cuentaTercero.cuentaLink.string("estadoCuenta"));
        request.body("cuitDestino", cuitDestino);
        request.body("tipoPersonaDestino", cuentaTercero.cuentaLink.string("tipoPersona"));
        request.body("nombreClienteDestino",
                cuentaTercero.cuentaLink.objetos("titulares").get(0).string("denominacion"));
        request.body("mismoTitular", cuitOrigen.equals(cuitDestino) ? "true" : "false");
        request.body("correoElectronicoDestino", "@");
        request.body("numeroCuentaDestinoPBF", cuentaTercero.cuentaLink.string("cuentaPBF"));
        request.body("cbu", cuentaOrigen.cbu());
        request.body("cbuDestino", cuentaTercero.cuentaLink.string("cbu"));
        request.body("tipoCuentaDestino", cuentaTercero.cuentaLink.string("tipoProducto"));
        request.body("notificacionDestinatario", false);
        request.body("numeroTarjeta", tarjetaDebitoAsociada);
        request.body("importe", monto);
        request.body("cuit", cuitOrigen);
        request.body("nombreClienteOrigen", contexto.persona().nombreCompleto());
        request.body("numeroCuentaOrigenPBF", cuentaOrigen.numero());
        request.body("tipoCuentaOrigen", cuentaOrigen.idTipo());
        request.body("timeStampTransferencia", timestamp);
        request.body("idMonedaDestino", cuentaOrigen.idMoneda());
        request.body("idMonedaOrigen", cuentaOrigen.idMoneda());
        request.body("referencia", "REF");
        request.body("servicio", empleadoDomestico ? "99" : ("HAB".equals(concepto) ? "98" : null));
        request.body("idCliente", contexto.idCobis());

        return request;
    }

//	private static String pescandoTransferencia(ApiResponseMB response, ContextoMB contexto, String tarjetaDebitoAsoci)ada) {
//		String idRequerimiento = response.headers.get("x-idtransaccion");
//		Long timestamp = new Date().getTime();
//		try {
//			if (idRequerimiento != null && !idRequerimiento.equals("null")) {
//				ApiRequestMB requestPescadora = ApiMB.request("PescadoraTransferencia", "cuentas", "GET", "/v1/transferencias", contexto);
//				requestPescadora.query("idrequerimiento", idRequerimiento);
//				requestPescadora.query("numeroTarjeta", tarjetaDebitoAsociada);
//				requestPescadora.query("timestamptransferencia", timestamp.toString());
//				ApiResponseMB responsePescadora = ApiMB.response(requestPescadora, new Date().getTime());
//				Boolean transferenciaOK = !responsePescadora.hayError() && responsePescadora.string("respuestaTransferencia.codigoRespuesta").equals("00");
//				if (transferenciaOK) {
//					return "OK_SIN_COMPROBANTE";
//				}
//			}
//		} catch (Exception ex) {
//			return "";
//		}
//		return "";
//	}

    private static void setearLogTransaccionalOtrosCasos(ApiResponseMB response, ApiRequestMB request,
                                                         ContextoMB contexto, Cuenta cuenta, String concepto, CuentaTercero cuentaTercero,
                                                         String tarjetaDebitoAsociada, String caso) {
        BigDecimal monto = contexto.parametros.bigDecimal("monto");
        Boolean empleadoDomestico = contexto.parametros.bool("empleadoDomestico", false);
        Boolean esEspecial = transferenciaEspecial(contexto, cuenta, monto);
        String tipo = "idCoelsa";
        String descripTrans = "CVU";
        String cuitOrigen = "";
        String cuitDestino = "";

        if ("link".equalsIgnoreCase(caso)) {
            cuitOrigen = contexto.persona().cuit();
            cuitDestino = cuentaTercero.cuentaLink.objetos("titulares").get(0).string("idTributario");
            tipo = "transaccion";
            descripTrans = "OtroBanco";
        }

        try {
            String codigoError = response == null ? "ERROR" : response.hayError() ? response.string("codigo") : "0";
            String transaccion = codigoError.equals("0") ? response.string(tipo) : null;

            String descripcionError = "";
            if (response != null && !codigoError.equals("0")) {
                descripcionError += response.string("codigo") + ".";
                descripcionError += response.string("mensajeAlUsuario") + ".";
            }
            descripcionError = descripcionError.length() > 990 ? descripcionError.substring(0, 990) : descripcionError;

            SqlRequestMB sqlRequest = SqlMB.request("InsertAuditorTransferencia" + descripTrans, "hbs");
            sqlRequest.sql = "INSERT INTO [hbs].[dbo].[auditor_transferencia] ";
            sqlRequest.sql += "([momento],[cobis],[idProceso],[ip],[canal],[codigoError],[descripcionError],[tipo],[cuentaOrigen],[cuentaDestino],[importe],[moneda],[concepto],[cuentaPropia],[servicioDomestico],[especial],[tarjetaDebito],[transaccion]) ";
            sqlRequest.sql += "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            sqlRequest.add(new Date()); // momento
            sqlRequest.add(contexto.idCobis()); // cobis
            sqlRequest.add(request.idProceso()); // idProceso
            sqlRequest.add(request.ip()); // ip
            sqlRequest.add("MB"); // canal
            sqlRequest.add(codigoError); // codigoError
            sqlRequest.add(descripcionError); // descripcionError
            sqlRequest.add(caso); // tipo
            sqlRequest.add(cuenta.numero()); // cuentaOrigen
            sqlRequest.add("link".equalsIgnoreCase(caso) ? cuentaTercero.cuentaLink.string("cbu")
                    : cuentaTercero.cuentaCoelsa.string("cbu")); // cuentaDestino
            sqlRequest.add(monto); // importe
            sqlRequest.add("link".equalsIgnoreCase(caso) ? cuenta.idMoneda() : "80"); // moneda
            sqlRequest.add(concepto); // concepto
            sqlRequest.add(
                    "link".equalsIgnoreCase(caso) ? Boolean.valueOf(cuitOrigen.equals(cuitDestino)).toString() : null); // cuentaPropia
            sqlRequest.add("link".equalsIgnoreCase(caso) ? empleadoDomestico.toString() : "false"); // servicioDomestico
            sqlRequest.add("link".equalsIgnoreCase(caso) ? esEspecial.toString() : "false"); // especial
            sqlRequest.add("link".equalsIgnoreCase(caso) ? tarjetaDebitoAsociada : null); // tarjetaDebito
            sqlRequest.add(transaccion); // transaccion

            SqlMB.response(sqlRequest);
        } catch (Exception e) {
        }
    }

    private static ApiRequestMB requestTransferenciaCuentaCVU(ContextoMB contexto, Cuenta cuentaOrigen,

                                                              String concepto, CuentaTercero cuentaTercero) {

        BigDecimal monto = contexto.parametros.bigDecimal("monto");
        Boolean habilitarDebinApi = MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_debin_api");
        Boolean empleadoDomestico = contexto.parametros.bool("empleadoDomestico", false);
        Boolean prendidoTransferenciasHaberes = MBAplicacion.funcionalidadPrendida(contexto.idCobis(),
                "prendido_transferencias_haberes");

        ApiRequestMB request = null;
        if (habilitarDebinApi) {
            request = ApiMB.request("TransferenciaCVU", "debin", "POST", "/v1/credin/autorizar", contexto);
        } else {
            request = ApiMB.request("TransferenciaWindowsCVU", "debin_windows", "POST", "/v1/credin/autorizar",
                    contexto);
        }
        Boolean isSalesforce = MBSalesforce.prendidoSalesforce(contexto.idCobis());
        request.query("enviarMail", isSalesforce.toString());

        String descripcionConcepto = empleadoDomestico ? "Haberes"
                : TransferenciaService.conceptos().get(request.body().string("motivo"));
        if (prendidoTransferenciasHaberes) {
            if ("HAB".equals(concepto)) {
                descripcionConcepto = "Sueldos - Haberes";
            } else {
                descripcionConcepto = TransferenciaService.conceptos().get(concepto);
            }
            if (empleadoDomestico) {
                descripcionConcepto = "Sueldos - Serv.Dom.";
            }
        }

        Objeto originante = request.body("originante");
        originante.set("idTributario", contexto.persona().cuit());
        originante.set("nombreCompleto", contexto.persona().nombreCompleto());
        originante.set("mail", contexto.persona().email());
        Objeto cuentaOriginante = originante.set("cuenta");
        cuentaOriginante.set("cbu", cuentaOrigen.cbu());
        cuentaOriginante.set("numero", cuentaOrigen.numero());
        cuentaOriginante.set("tipo", cuentaOrigen.idTipo());
        Objeto sucursalCuentaOriginante = cuentaOriginante.set("sucursal");
        sucursalCuentaOriginante.set("id", String.format("%04d", Integer.valueOf(cuentaOrigen.sucursal())));

        Objeto destinatario = request.body("destinatario");
        destinatario.set("idTributario", cuentaTercero.cuentaCoelsa.string("cuit"));
        destinatario.set("nombreCompleto", cuentaTercero.cuentaCoelsa.string("nombreTitular"));
        destinatario.set("mail", null);
        Objeto cuentaDestinatario = destinatario.set("cuenta");
        cuentaDestinatario.set("cbu", cuentaTercero.cuentaCoelsa.string("cbu"));
        cuentaDestinatario.set("banco", cuentaTercero.cuentaCoelsa.string("nroBco"));

        Objeto detalle = request.body("detalle");
        detalle.set("concepto", concepto);
        detalle.set("importe", monto);
        if (prendidoTransferenciasHaberes) {
            detalle.set("descripcion", descripcionConcepto);
        }

        Objeto monedaDetalle = detalle.set("moneda");
        monedaDetalle.set("id", "80");
        monedaDetalle.set("descripcion", "PESOS");
        monedaDetalle.set("signo", "$");

        return request;
    }

    private static String tarjetaHabilitada(ContextoMB contexto, String tarjetaDebitoAsociada) {
        ApiResponseMB responseTD = TarjetaDebitoService.tarjetaDebitoGetEstado(contexto, tarjetaDebitoAsociada);
        if (responseTD.hayError()) {
            return "TARJETA_NO_VALIDA";
        }
        String estadoTarjeta = responseTD.string("estadoTarjeta");
        if (estadoTarjeta == null || (!"HABILITADA".equals(estadoTarjeta.toUpperCase())
                && !"ACTIVA".equals(estadoTarjeta.toUpperCase()))) {
            return "TARJETA_NO_VALIDA";
        }
        return "";
    }

    public static RespuestaMB isRiskForChangeInformation(ContextoMB contexto) {
        if (Objeto.anyEmpty(contexto.idCobis())) {
            return RespuestaMB.estado("ERROR");
        }
        Boolean disable_48hrs = MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_disable_48hrs");
        if (!disable_48hrs) {
            return RespuestaMB.exito("enableb_operator", Boolean.TRUE);
        }

        DisableService disableService = new DisableService();
        LocalDateTime nowTime = LocalDateTime.now();
        nowTime = nowTime.plusHours(-1 * disableService.calculateHourDelay(nowTime));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String inicio = nowTime.format(formatter);

        String tipo = ConfigMB.string(esClienteNuevo(contexto, inicio) ? "cambio_information_no_permitido_nuevo" : "cambio_information_no_permitido");

        List<Objeto> registros = ContextoMB.obtenerContador(contexto.idCobis(), tipo, inicio);
        if (Objects.isNull(registros)) {
            return RespuestaMB.estado("ERROR");
        }
        Boolean permission = disableService.getEnabledToOperator(registros);
        return RespuestaMB.exito("enableb_operator", permission);
    }

    public static RespuestaMB puedeTransferirEspecialMismoDia(ContextoMB contexto) {
        if (Objeto.anyEmpty(contexto.idCobis()))
            return RespuestaMB.estado("SIN_PSEUDO_SESION");

        return RespuestaMB.exito("puede_transferir_especial_mismo_dia",
                noActivoSoftTokenEnElUltimoDia(contexto) && noModificoDatosPersonalesEnElUltimoDia(contexto));
    }

    private static boolean esClienteNuevo(ContextoMB contexto, String fechaInicio) {
        return SqlClientesOperadores.esUsuarioNuevo(contexto, fechaInicio);
    }

    private static void insertarDatosComprobantes(ContextoMB contexto, String idComprobante, String nombreBanco, String cuentaNumero) {
        new SqlTransferencia().insertarDatosComprobanteTransferencia(contexto, idComprobante, nombreBanco, cuentaNumero);
    }

    public static Map<String, String> toMap(Map<String, Object> inputMap) {
        Map<String, String> resultMap = new HashMap<>();

        for (Map.Entry<String, Object> entry : inputMap.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            // Convertir el valor a String, manejando null
            String stringValue = (value != null) ? value.toString() : "null";

            resultMap.put(key, stringValue);
        }

        return resultMap;
    }


    private static boolean validarTransaccion(ContextoMB contexto, boolean migrado) {
        return migrado ? TransmitMB.validarCsmTransaccion(contexto, JourneyTransmitEnum.MB_INICIO_SESION) : contexto.validaSegundoFactor("transferencia");
    }

    //DEPRECAR EN 2026
    public static RespuestaMB beneficiariosV1(ContextoMB contexto) {
        int pagina = contexto.parametros.integer("pagina", 1);
        int limite = contexto.parametros.integer("limite", 5);
        String filtro = contexto.parametros.string("filtro", "");


        //Migrado a api-canales para optimizar tiempos
        ApiRequestMB request = ApiMB.request("CanalesAgenda", "canales", "GET", "/beneficiarios-transferencias-anterior", contexto);

        request.query("pagina", String.valueOf(pagina));
        request.query("limite", String.valueOf(limite));
        request.query("filtro", filtro);
        request.query("idCobis", contexto.idCobis());

        ApiResponseMB response = ApiMB.response(request);
        if (response.hayError()) {
            throw new RuntimeException();
        }

        return RespuestaMB.exito("titulares", response.get("titulares"))
                .set("beneficiarios", response.get("beneficiarios"))
                .set("total", response.get("total"))
                .set("nroPaginas", response.get("nroPaginas"));
    }


    public static RespuestaMB beneficiariosV2(ContextoMB contexto) {
        int pagina = contexto.parametros.integer("pagina", 1);
        int limite = contexto.parametros.integer("limite", 5);
        String filtro = contexto.parametros.string("filtro", "");

        //Migrado a api-canales para optimizar tiempos

        ApiRequestMB request = ApiMB.request("CanalesAgenda", "canales", "GET", "/beneficiarios-transferencias", contexto);

        request.query("pagina", String.valueOf(pagina));
        request.query("limite", String.valueOf(limite));
        request.query("filtro", filtro);
        request.query("idCobis", contexto.idCobis());

        ApiResponseMB response = ApiMB.response(request);
        if (response.hayError() || response.get("estado").equals("ERROR")) {
            RespuestaMB.error();
        }

        return RespuestaMB.exito("titulares", response.get("titulares"))
                .set("beneficiarios", response.get("beneficiarios"))
                .set("total", response.get("total"))
                .set("nroPaginas", response.get("nroPaginas"));
    }

    public static RespuestaMB beneficiariosDetalleV1(ContextoMB contexto) {
        String cuilBeneficiario = contexto.parametros.string("cuil");

        if (Objeto.anyEmpty(cuilBeneficiario)) {
            cuilBeneficiario = contexto.persona().cuit();
        }

        SqlTransferencia sqlTransferencia = new SqlTransferencia();
        SqlResponseMB sqlResponse = sqlTransferencia.contactosAgendados(contexto.idCobis());
        if (sqlResponse.hayError) {
            return RespuestaMB.error();
        }

        final String[] nombre = {""};
        final String[] apodo = {""};
        List<Objeto> cuentas = new ArrayList<>();

        try {
            ExecutorService executorService = Executors.newCachedThreadPool();
            for (Objeto registro : sqlResponse.registros) {
                String finalCuilBeneficiario = cuilBeneficiario;
                executorService.submit(() -> {
                    String id = registro.stringNotEmpty("cbu_destino", registro.string("nro_cuenta_destino")).trim();
                    if (!id.isEmpty()) {
                        if (finalCuilBeneficiario.equals(registro.string("documento_beneficiario").trim())) {
                            if (nombre[0].isEmpty()) {
                                nombre[0] = registro.string("titular").trim();
                            }

                            if (apodo[0].isEmpty()) {
                                apodo[0] = registro.string("apodo").trim();
                            }

                            String descripcion = "";
                            if (id.startsWith("0000")) { // Es CVU
                                descripcion = RestCatalogo.bancoCVU(id.substring(0, 7));
                            } else {
                                descripcion = RestCatalogo.bancoFiltrado(id.substring(0, 3));
                            }
                            String nroBancoFormateado = RestCatalogo.nroBancoInterno(descripcion);

                            if (RestCatalogo.bancoFiltrado(nroBancoFormateado).isEmpty()) {
                                registro.set("banco_destino", "");
                            }

                            Objeto cuenta = getCuentaDetalle(contexto, registro);
                            registro.set("cbuTercero", cuenta.stringNotEmpty("cbu", registro.string("cbuTercero")));
                            registro.set("aliasTercero", cuenta.stringNotEmpty("alias", registro.string("aliasTercero")));
                            registro.set("nroBancoTercero", cuenta.stringNotEmpty("nroBanco", registro.string("nroBancoTercero")));
                            registro.set("idMonedaTercero", cuenta.stringNotEmpty("idMoneda", registro.string("idMonedaTercero")));
                            cuentas.add(cuenta);
                        }
                    }
                });
            }

            Concurrencia.esperar(executorService, null, 60);
        } catch (Exception e) {
            return RespuestaMB.error();
        }

        updateFaltantesAgenda(contexto, sqlResponse.registros);

        List<Objeto> cuentaPesos = new ArrayList<>();
        List<Objeto> cuentaDolares = new ArrayList<>();

        for (Objeto cuenta : cuentas) {
            if (cuilBeneficiario.equals(contexto.persona().cuit())
                    && "044".equals(cuenta.string("nroBanco"))) {
                continue;
            }

            Objeto aux = new Objeto();
            aux.set("cbu", cuenta.string("cbu"));
            aux.set("alias", cuenta.string("alias"));
            aux.set("idMoneda", cuenta.string("idMoneda"));
            aux.set("banco", cuenta.string("banco"));
            aux.set("logo", cuenta.string("logo"));
            aux.set("comentario", cuenta.string("comentario"));

            if (cuenta.bool("esBiMonetaria")) {
                if (cuenta.string("idMoneda").equals("2")) {
                    cuentaDolares.add(aux);

                    Objeto auxPesos = new Objeto();
                    auxPesos.set("cbu", cuenta.string("cbu"));
                    auxPesos.set("alias", cuenta.string("alias"));
                    auxPesos.set("idMoneda", "80");
                    auxPesos.set("banco", cuenta.string("banco"));
                    auxPesos.set("logo", cuenta.string("logo"));
                    auxPesos.set("comentario", cuenta.string("comentario"));
                    cuentaPesos.add(auxPesos);
                } else {
                    cuentaPesos.add(aux);

                    Objeto auxDolares = new Objeto();
                    auxDolares.set("cbu", cuenta.string("cbu"));
                    auxDolares.set("alias", cuenta.string("alias"));
                    auxDolares.set("idMoneda", "2");
                    auxDolares.set("banco", cuenta.string("banco"));
                    auxDolares.set("logo", cuenta.string("logo"));
                    auxDolares.set("comentario", cuenta.string("comentario"));
                    cuentaDolares.add(auxDolares);
                }
            } else {
                if (cuenta.string("idMoneda").equals("2")) {
                    cuentaDolares.add(aux);
                } else {
                    cuentaPesos.add(aux);
                }
            }
        }

        return RespuestaMB.exito("nombre", nombre[0])
                .set("apodo", apodo[0])
                .set("cuentaPesos", cuentaPesos)
                .set("cuentaDolares", cuentaDolares);
    }

    public static RespuestaMB beneficiariosDetalleV2(ContextoMB contexto) {
        String cuilBeneficiario = contexto.parametros.string("cuil", "");
        String idContacto = contexto.parametros.string("id", "");
        boolean esCuentaPropia = false;

        if (!(contexto.sesion() != null && contexto.sesion().idCobisReal() != null)) {
            return RespuestaMB.estado("ERROR_SIN_SESION");
        }


        if (cuilBeneficiario.isEmpty() && idContacto.isEmpty()) {
            cuilBeneficiario = contexto.persona().cuit();
            esCuentaPropia = true;
        }

        SqlResponseMB sqlResponse = obtenerAgenda(contexto, esCuentaPropia, cuilBeneficiario, idContacto);
        if (sqlResponse.hayError) {
            return RespuestaMB.error();
        }

        final String[] nombre = {""};
        final String[] cuil = {""};
        final String[] apodo = {""};
        List<Objeto> cuentas = new ArrayList<>();

        for (Objeto registro : sqlResponse.registros) {
            String id = registro.stringNotEmpty("cbu_destino", registro.string("nro_cuenta_destino")).trim();
            if (!id.isEmpty()) {
                if (nombre[0].isEmpty()) {
                    nombre[0] = registro.string("titular").trim();
                }

                if (cuil[0].isEmpty()) {
                    cuil[0] = registro.string("cuil").trim();
                }

                if (apodo[0].isEmpty()) {
                    apodo[0] = registro.string("apodo").trim();
                }

                if (id.length() == 22) {
                    String nroBanco = registro.stringNotEmpty("cbu_destino","");
                    if(!nroBanco.isEmpty()) {
                        registro.set("banco_destino", RestCatalogo.banco(nroBanco));
                    }

                } else {
                    registro.set("banco_destino", "044"); //nro de banco
                }

                Objeto cuenta;
                if (id.length() != 22) {
                    cuenta = getCuentaDetalleCbu(contexto, registro); // Si tiene nro cuenta
                    registro.set("nombreTercero", cuenta.stringNotEmpty("nombreTitular", registro.string("titular")));
                    registro.set("cuilTercero", cuenta.stringNotEmpty("cuitTitular", registro.string("documento_beneficiario")));
                    String cuilTercero = cuenta.stringNotEmpty("cuitTitular", "");
                    if (cuilTercero.isEmpty() && !(registro.string("documento_beneficiario").isEmpty())) {
                        //Si el proveedor no tiene cuil uso el que está guardado en la Base
                        cuenta.set("cuitTitular", registro.string("documento_beneficiario"));
                    }
                    registro.set("cbuTercero", cuenta.stringNotEmpty("cbu", registro.string("cbu_destino")));
                    registro.set("alias", cuenta.stringNotEmpty("alias", registro.string("alias")));
                    String alias = cuenta.stringNotEmpty("alias", "");
                    //Si el proveedor no tiene alias uso el que está guardado en la Base
                    if (alias.isEmpty() && !(registro.string("alias").isEmpty())) {
                        cuenta.set("alias", registro.string("alias"));
                    }
                    registro.set("nroBancoTercero", cuenta.stringNotEmpty("nroBanco", registro.string("banco_destino")));
                    registro.set("idMonedaTercero", cuenta.stringNotEmpty("idMoneda", registro.string("moneda_cuenta_destino")));
                } else {
                    String cbu = (String) registro.get("cbu_destino","");
                    String cuit = (String) registro.get("documento_beneficiario","");
                    String titular = (String) registro.get("titular","");
                    String moneda = (String) registro.get("moneda_cuenta_destino","");
                    if( cbu.isEmpty() || cuit.isEmpty() || titular.isEmpty() || moneda.isEmpty()){
                        cuenta = getCuentaDetalleOtros(contexto, registro);
                        if (registro.string("titular").isEmpty()) {
                            registro.set("nombreTercero", cuenta.stringNotEmpty("nombreTitular", registro.string("titular")));
                        }
                        if (registro.string("documento_beneficiario").isEmpty()) {
                            //Si el proveedor no tiene cuil uso el que está guardado en la Base
                            registro.set("cuilTercero", cuenta.stringNotEmpty("cuitTitular", registro.string("documento_beneficiario")));
                        }
                        String cuilTercero = cuenta.stringNotEmpty("cuitTitular", "");
                        if (cuilTercero.isEmpty() && !(registro.string("documento_beneficiario").isEmpty())) {
                            cuenta.set("cuitTitular", registro.string("documento_beneficiario"));
                        }
                        String alias = cuenta.stringNotEmpty("alias", "");
                        if (alias.isEmpty() && !(registro.string("alias").isEmpty())) {
                            //Si el proveedor no tiene alias uso el que está guardado en la Base
                            cuenta.set("alias", registro.string("alias"));
                        }
                        if (registro.string("banco_destino").isEmpty()) {
                            registro.set("nroBancoTercero", cuenta.stringNotEmpty("nroBanco", registro.string("banco_destino")));
                        }
                        if (registro.string("moneda_cuenta_destino").isEmpty()) {
                            registro.set("idMonedaTercero", cuenta.stringNotEmpty("idMoneda", registro.string("moneda_cuenta_destino")));
                        }
                    }else {
                        cuenta = new Objeto();
                        cuenta.set("cuitTitular",registro.get("documento_beneficiario"));
                        cuenta.set("cbu",registro.string("cbu_destino"));
                        cuenta.set("alias",registro.string("alias"));
                        cuenta.set("idMoneda",registro.string("moneda_cuenta_destino"));
                        String descripcion = "";
                        if (cbu.startsWith("0000") || cbu.startsWith("4530")) { // Es CVU
                            descripcion = RestCatalogo.bancoCVU(cbu.substring(0, 7));
                        } else {
                            descripcion = RestCatalogo.bancoFiltrado(cbu.substring(0, 3));
                        }
                        String codigoBanco = RestCatalogo.nroBancoInterno(descripcion);
                        cuenta.set("banco",descripcion);
                        cuenta.set("logo",RestCatalogo.bancoLogo(codigoBanco));
                        cuenta.set("comentario",registro.string("comentario"));
                        cuenta.set("cuenta",registro.get("nro_cuenta_destino",""));
                        if(registro.string("cbu_destino").startsWith("072")){
                            cuenta.set("esBiMonetaria",true);
                        }
                    }
                }

                cuentas.add(cuenta);
            }
        }

        updateFaltantesAgenda(contexto, sqlResponse.registros);

        List<Objeto> cuentaPesos = new ArrayList<>();
        List<Objeto> cuentaDolares = new ArrayList<>();

        for (Objeto cuenta : cuentas) {
            if (cuilBeneficiario.equals(contexto.persona().cuit()) &&
                    ("044".equals(cuenta.string("nroBanco")) || cuenta.string("cbu").startsWith("044"))) {
                continue;
            }

            Objeto aux = new Objeto();
            aux.set("cbu", cuenta.string("cbu"));
            aux.set("alias", cuenta.string("alias"));
            aux.set("idMoneda", cuenta.string("idMoneda"));
            aux.set("banco", cuenta.string("banco"));
            aux.set("logo", cuenta.string("logo"));
            aux.set("comentario", cuenta.string("comentario"));

            if (cuenta.bool("esBiMonetaria")) {
                if (cuenta.string("idMoneda").equals("2")) {
                    cuentaDolares.add(aux);

                    Objeto auxPesos = new Objeto();
                    auxPesos.set("cbu", cuenta.string("cbu"));
                    auxPesos.set("alias", cuenta.string("alias"));
                    auxPesos.set("idMoneda", "80");
                    auxPesos.set("banco", cuenta.string("banco"));
                    auxPesos.set("logo", cuenta.string("logo"));
                    auxPesos.set("comentario", cuenta.string("comentario"));
                    cuentaPesos.add(auxPesos);
                } else {
                    cuentaPesos.add(aux);

                    Objeto auxDolares = new Objeto();
                    auxDolares.set("cbu", cuenta.string("cbu"));
                    auxDolares.set("alias", cuenta.string("alias"));
                    auxDolares.set("idMoneda", "2");
                    auxDolares.set("banco", cuenta.string("banco"));
                    auxDolares.set("logo", cuenta.string("logo"));
                    auxDolares.set("comentario", cuenta.string("comentario"));
                    cuentaDolares.add(auxDolares);
                }
            } else {
                if (cuenta.string("idMoneda").equals("2")) {
                    cuentaDolares.add(aux);
                } else {
                    cuentaPesos.add(aux);
                }
            }
        }
        String cuilMostrar = "";
        if(!(cuentas.size() == 0)){
            cuilMostrar = (String) cuentas.get(0).get("cuitTitular");
        }

        return RespuestaMB.exito("nombre", nombre[0])
                .set("cuil", cuilMostrar)
                .set("apodo", apodo[0])
                .set("cuentaPesos", cuentaPesos)
                .set("cuentaDolares", cuentaDolares);
    }

    public static SqlResponseMB obtenerAgenda(ContextoMB contexto, boolean esCuentaPropia, String cuilBeneficiario, String idContacto) {
        SqlTransferencia sqlTransferencia = new SqlTransferencia();
        SqlResponseMB sqlResponse = null;
        if (esCuentaPropia) {
            return sqlTransferencia.obtenerAgendaCuit(contexto.idCobis(), cuilBeneficiario);
        } else if (cuilBeneficiario.isEmpty()) {
            return sqlTransferencia.obtenerAgendaTransferencia(contexto.idCobis(), idContacto);
        } else {
            return sqlTransferencia.obtenerAgendaCuit(contexto.idCobis(), cuilBeneficiario);
        }
    }


    public static void updateFaltantesAgenda(ContextoMB contexto, List<Objeto> registros) {
        for (Objeto registro : registros) {
            String cuil = registro.string("documento_beneficiario").trim();
            String nombre = registro.string("titular").trim();
            String cbu = registro.string("cbu_destino").trim();
            String idMoneda = registro.string("moneda_cuenta_destino").trim();

            String cuilTercero = registro.stringNotEmpty("documento_beneficiario", registro.string("cuilTercero")).trim();
            String nombreTercero = registro.stringNotEmpty("titular", registro.string("nombreTercero")).trim();
            String cbuTercero = registro.stringNotEmpty("cbu_destino", registro.string("cbuTercero")).trim();
            String idMonedaTercero = registro.stringNotEmpty("moneda_cuenta_destino", registro.string("idMonedaTercero")).trim();
            String alias = registro.stringNotEmpty("alias", registro.string("alias")).trim();

            String idAgenda = registro.string("id").trim();
            SqlTransferencia sqlTransferencia = new SqlTransferencia();
            if ((cuil.isEmpty() && !cuilTercero.isEmpty())
                    || (nombre.isEmpty() && !nombreTercero.isEmpty())
                    || (cbu.isEmpty() && !cbuTercero.isEmpty())
                    || (idMoneda.isEmpty() && !idMonedaTercero.isEmpty())) {

                sqlTransferencia.actualizarFaltantesAgenda(contexto.idCobis(), idAgenda,
                        cuilTercero, nombreTercero.toUpperCase(), cbuTercero, idMonedaTercero);
            }
            if (alias.isEmpty()) {
                sqlTransferencia.actualizarAlias(contexto.idCobis(), idAgenda, alias);
            }
        }
    }

    public static RespuestaMB getCuentaDetalleCbu(ContextoMB contexto, Objeto registro) {
        String nroCuenta = registro.string("nro_cuenta_destino").trim();
        String cbu = registro.stringNotEmpty("cbu_destino", registro.string("cbuTercero")).trim();
        String nroBanco = registro.stringNotEmpty("banco_destino", registro.string("nroBancoTercero")).trim();
        String idMoneda = registro.stringNotEmpty("moneda_cuenta_destino", registro.string("idMonedaTercero")).trim();
        String alias = "";
        boolean esBiMonetaria = false;
        String descripcion = "";
        String codigobanco = "";
        String nombreTitular = "";
        String cuitTitular = "";

        if (cbu.isEmpty()) {
            CuentaTercero cuentaTercero = new CuentaTercero(contexto, nroCuenta);
            cbu = cuentaTercero.cbu();
            alias = cuentaTercero.alias();
            esBiMonetaria = cuentaTercero.esBiMonetaria();
            nroBanco = cuentaTercero.nrobanco();
            if (!cbu.isEmpty()) {
                if (cbu.startsWith("0000") || cbu.startsWith("4530")) { // Es CVU
                    descripcion = RestCatalogo.bancoCVU(cbu.substring(0, 7));
                } else {
                    descripcion = RestCatalogo.bancoFiltrado(cbu.substring(0, 3));
                }
                codigobanco = RestCatalogo.nroBancoInterno(descripcion);
            }
            if (idMoneda.isEmpty()) {
                idMoneda = cuentaTercero.idMoneda();
            }
            nombreTitular = cuentaTercero.titular();
            cuitTitular = cuentaTercero.cuit().isEmpty() ? cuentaTercero.cuitCoelsa() : cuentaTercero.cuit();

        }

        return RespuestaMB.exito("cbu", cbu)
                .set("alias", alias)
                .set("nroBanco", RestCatalogo.formatearCodigo((nroBanco)))
                .set("banco", descripcion)
                .set("logo", RestCatalogo.bancoLogo(codigobanco))
                .set("idMoneda", idMoneda)
                .set("comentario", registro.string("comentario".trim()))
                .set("esBiMonetaria", esBiMonetaria)
                .set("nombreTitular", nombreTitular)
                .set("cuitTitular", cuitTitular);
    }

    public static RespuestaMB getCuentaDetalleOtros(ContextoMB contexto, Objeto registro) {
        String cbu = registro.stringNotEmpty("cbu_destino", registro.string("cbuTercero")).trim();
        String nroBanco = registro.stringNotEmpty("banco_destino", registro.string("nroBancoTercero")).trim();
        String idMoneda = registro.stringNotEmpty("moneda_cuenta_destino", registro.string("idMonedaTercero")).trim();
        String alias = registro.stringNotEmpty("alias", registro.string("alias")).trim();
        boolean esBiMonetaria = false;
        String descripcion = "";
        String codigoBanco = "";
        String nombreTitular = "";
        String cuitTitular = "";

        CuentaTercero cuentaTercero = new CuentaTercero(contexto, cbu);

        if (alias.isEmpty()) {
            alias = cuentaTercero.alias();
        }

        if (cbu.startsWith("0000") || cbu.startsWith("4530")) { // Es CVU
            descripcion = RestCatalogo.bancoCVU(cbu.substring(0, 7));
        } else {
            descripcion = RestCatalogo.bancoFiltrado(cbu.substring(0, 3));
        }
        codigoBanco = RestCatalogo.nroBancoInterno(descripcion);

        if (idMoneda.isEmpty()) {
            idMoneda = cuentaTercero.idMoneda();
        }
        esBiMonetaria = cuentaTercero.esBiMonetaria();
        nombreTitular = cuentaTercero.titular();
        cuitTitular = cuentaTercero.cuit().isEmpty() ? cuentaTercero.cuitCoelsa() : cuentaTercero.cuit();


        return RespuestaMB.exito("cbu", cbu)
                .set("alias", alias)
                .set("nroBanco", RestCatalogo.formatearCodigo((nroBanco)))
                .set("banco", descripcion)
                .set("logo", RestCatalogo.bancoLogo(codigoBanco))
                .set("idMoneda", idMoneda)
                .set("comentario", registro.string("comentario".trim()))
                .set("esBiMonetaria", esBiMonetaria)
                .set("nombreTitular", nombreTitular)
                .set("cuitTitular", cuitTitular);
    }

    public static Object modificarBeneficiarioApodo(ContextoMB ctx) {
        String apodo = ctx.parametros.string("apodo", null);
        String cuilBeneficiario = ctx.parametros.string("cuil", null);

        if (Objeto.anyEmpty(apodo, cuilBeneficiario)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        SqlTransferencia sqlTransferencia = new SqlTransferencia();
        SqlResponseMB sqlResponse = sqlTransferencia.actualizarApodoAgendado(ctx.idCobis(), cuilBeneficiario, apodo);
        return (sqlResponse.hayError) ? RespuestaMB.error() : RespuestaMB.exito();
    }

    //DEPRECAR EN 2026
    public static RespuestaMB getCuentaDetalle(ContextoMB contexto, Objeto registro) {
        String nroCuenta = registro.string("nro_cuenta_destino").trim();
        String cbu = registro.stringNotEmpty("cbu_destino", registro.string("cbuTercero")).trim();
        String nroBanco = registro.stringNotEmpty("banco_destino", registro.string("nroBancoTercero")).trim();
        String idMoneda = registro.stringNotEmpty("moneda_cuenta_destino", registro.string("idMonedaTercero")).trim();
        String alias = "";
        boolean esBiMonetaria = false;
        String descripcion = "";
        String codigobanco = "";

        if (cbu.isEmpty()) {
            CuentaTercero cuentaTercero = new CuentaTercero(contexto, nroCuenta);
            cbu = cuentaTercero.cbu();
            alias = cuentaTercero.alias();
            esBiMonetaria = cuentaTercero.esBiMonetaria();


            nroBanco = cuentaTercero.nrobanco();
            if (!cbu.isEmpty()) {
                if (cbu.startsWith("0000")) { // Es CVU
                    descripcion = RestCatalogo.bancoCVU(cbu.substring(0, 7));
                } else {
                    descripcion = RestCatalogo.bancoFiltrado(cbu.substring(0, 3));
                }

                codigobanco = RestCatalogo.nroBancoInterno(descripcion);
            }

            if (idMoneda.isEmpty()) {
                idMoneda = cuentaTercero.idMoneda();
            }
        }

        if (!cbu.isEmpty() && (alias.isEmpty() || nroBanco.isEmpty() || idMoneda.isEmpty())) {
            CuentaTercero cuentaTercero = new CuentaTercero(contexto, cbu);

            if (alias.isEmpty()) {
                alias = cuentaTercero.alias();
            }

            if (!cbu.isEmpty()) {
                if (cbu.startsWith("0000")) { // Es CVU
                    descripcion = RestCatalogo.bancoCVU(cbu.substring(0, 7));
                } else {
                    descripcion = RestCatalogo.bancoFiltrado(cbu.substring(0, 3));
                }
            }

            codigobanco = RestCatalogo.nroBancoInterno(descripcion);

            if (idMoneda.isEmpty()) {
                idMoneda = cuentaTercero.idMoneda();
            }
            if (!esBiMonetaria) {
                esBiMonetaria = cuentaTercero.esBiMonetaria();
            }
        }

        return RespuestaMB.exito("cbu", cbu)
                .set("alias", alias)
                .set("nroBanco", RestCatalogo.formatearCodigo((nroBanco)))
                .set("banco", descripcion)
                .set("logo", RestCatalogo.bancoLogo(codigobanco))
                .set("idMoneda", idMoneda)
                .set("comentario", registro.string("comentario".trim()))
                .set("esBiMonetaria", esBiMonetaria);
    }


}