package ar.com.hipotecario.canal.homebanking.api;

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

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.servicio.api.transmit.JourneyTransmitEnum;
import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.Respuesta;
import ar.com.hipotecario.canal.homebanking.api.HBInversion.EnumPropuestasInversion;
import ar.com.hipotecario.canal.homebanking.base.ConfigHB;
import ar.com.hipotecario.canal.homebanking.base.Objeto;
import ar.com.hipotecario.canal.homebanking.conector.Api;
import ar.com.hipotecario.canal.homebanking.conector.ApiRequest;
import ar.com.hipotecario.canal.homebanking.conector.ApiResponse;
import ar.com.hipotecario.canal.homebanking.conector.Sql;
import ar.com.hipotecario.canal.homebanking.conector.SqlRequest;
import ar.com.hipotecario.canal.homebanking.conector.SqlResponse;
import ar.com.hipotecario.canal.homebanking.lib.Concurrencia;
import ar.com.hipotecario.canal.homebanking.lib.Fecha;
import ar.com.hipotecario.canal.homebanking.lib.Formateador;
import ar.com.hipotecario.canal.homebanking.lib.Util;
import ar.com.hipotecario.canal.homebanking.negocio.Cuenta;
import ar.com.hipotecario.canal.homebanking.negocio.CuentaTercero;
import ar.com.hipotecario.canal.homebanking.negocio.TarjetaDebito;
import ar.com.hipotecario.canal.homebanking.servicio.*;

public class HBTransferencia {
    private final static String DIA_HABIL = "1";

    /* ========== BENEFICIARIOS ========== */
    public static Respuesta beneficiarios(ContextoHB contexto) {
        contexto.sesion.setChallengeOtp(false);
        SqlTransferencia sqlTransferencia = new SqlTransferencia();
        SqlResponse sqlResponse = sqlTransferencia.contactosAgendados(contexto.idCobis());
        if (sqlResponse.hayError) {
            return Respuesta.error();
        }

        Objeto beneficiarios = new Objeto();
        for (Objeto registro : sqlResponse.registros) {
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

//        CuentaTercero cuentaTercero = new CuentaTercero(contexto, id);
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
            if (HBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_beneficiario_bh_nombre_apellido")
                    && registro.string("cuenta_del_banco").trim().equals("1")) {
                mostrarCuentaDelBancoConsolidada &= registro.string("titular", "Banco Hipotecario")
                        .equals("Banco Hipotecario");
            }
            item.set("mostrar_cuenta_del_banco_consolidada", mostrarCuentaDelBancoConsolidada);
            item.set("esCuentaBH", esCuentaBH);
            beneficiarios.add(item);
        }

        beneficiarios.ordenar("beneficiarioMinuscula");
        return Respuesta.exito("beneficiarios", beneficiarios);
    }

    public static Respuesta beneficiariosV2(ContextoHB contexto) {
        int pagina = contexto.parametros.integer("pagina", 1);
        int limite = contexto.parametros.integer("limite", 5);
        String filtro = contexto.parametros.string("filtro", "");

        //Migrado a api-canales para optimizar tiempos
        ApiRequest request = Api.request("CanalesAgenda", "canales", "GET", "/beneficiarios-transferencias", contexto);

        request.query("pagina", String.valueOf(pagina));
        request.query("limite", String.valueOf(limite));
        request.query("filtro", filtro);
        request.query("idCobis", contexto.idCobis());

        ApiResponse response = Api.response(request);
        if (response.hayError()) {
            throw new RuntimeException();
        }

        return Respuesta.exito("titulares", response.get("titulares"))
                .set("beneficiarios", response.get("beneficiarios"))
                .set("total", response.get("total"))
                .set("nroPaginas", response.get("nroPaginas"));
    }


    public static Respuesta beneficiariosDetalle(ContextoHB contexto) {
        String cuilBeneficiario = contexto.parametros.string("cuil", "");
        String idContacto = contexto.parametros.string("id", "");
        boolean esCuentaPropia = false;


        if (cuilBeneficiario.isEmpty() && idContacto.isEmpty()) {
            cuilBeneficiario = contexto.persona().cuit();
            esCuentaPropia = true;
        }

        SqlResponse sqlResponse = obtenerAgenda(contexto, esCuentaPropia, cuilBeneficiario, idContacto);
        if (sqlResponse.hayError) {
            return Respuesta.error();
        }

        final String[] nombre = {""};
        final String[] apodo = {""};
        List<Objeto> cuentas = new ArrayList<>();

        for (Objeto registro : sqlResponse.registros) {
            String id = registro.stringNotEmpty("cbu_destino", registro.string("nro_cuenta_destino")).trim();
            if (!id.isEmpty()) {
                if (nombre[0].isEmpty()) {
                    nombre[0] = registro.string("titular").trim();
                }

                if (apodo[0].isEmpty()) {
                    apodo[0] = registro.string("apodo").trim();
                }
                if (id.length() == 22) {
                    String nroBanco = registro.stringNotEmpty("cbu_destino", "");
                    if (!nroBanco.isEmpty()) {
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
                }  else {
                    String cbu = (String) registro.get("cbu_destino","");
                    String cuit = (String) registro.get("documento_beneficiario","");
                    String titular = (String) registro.get("titular","");
                    String moneda = (String) registro.get("moneda_cuenta_destino","");
                    if(cbu.isEmpty() || cuit.isEmpty() || titular.isEmpty() || moneda.isEmpty()){
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
                        cuenta.set("nroCuenta",registro.string("nro_cuenta_destino"));
                    }
                }
                cuentas.add(cuenta);
            }
        }

        updateFaltantesAgenda(contexto, sqlResponse.registros);

        List<Objeto> cuentaPesos = new ArrayList<>();
        List<Objeto> cuentaDolares = new ArrayList<>();

        for (Objeto cuenta : cuentas) {
            Objeto aux = new Objeto();
            aux.set("cbu", cuenta.string("cbu"));
            aux.set("alias", cuenta.string("alias"));
            aux.set("idMoneda", cuenta.string("idMoneda"));
            aux.set("banco", cuenta.string("banco"));
            aux.set("logo", cuenta.string("logo"));
            aux.set("comentario", cuenta.string("comentario"));

            if (cuilBeneficiario.equals(contexto.persona().cuit()) &&
                    ("044".equals(cuenta.string("nroBanco")) ||
                            cuenta.string("cbu").startsWith("044")
                    || !(cuenta.string("nroCuenta").isEmpty()))) {
                continue;
            }


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

        return Respuesta.exito("nombre", nombre[0])
                .set("cuil", cuilMostrar)
                .set("apodo", apodo[0])
                .set("cuentaPesos", cuentaPesos)
                .set("cuentaDolares", cuentaDolares);
    }

    public static SqlResponse obtenerAgenda(ContextoHB contexto, boolean esCuentaPropia, String
            cuilBeneficiario, String idContacto) {
        SqlTransferencia sqlTransferencia = new SqlTransferencia();
        SqlResponse sqlResponse = null;
        if (esCuentaPropia) {
            return sqlTransferencia.obtenerAgendaCuit(contexto.idCobis(), cuilBeneficiario);
        } else if (cuilBeneficiario.isEmpty()) {
            return sqlTransferencia.obtenerAgendaTransferencia(contexto.idCobis(), idContacto);
        } else {
            return sqlTransferencia.obtenerAgendaCuit(contexto.idCobis(), cuilBeneficiario);
        }
    }

    public static void updateFaltantesAgenda(ContextoHB contexto, List<Objeto> registros) {
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

            sqlTransferencia.actualizarAlias(contexto.idCobis(), idAgenda, alias);
        }

    }

    public static Respuesta getCuentaDetalleCbu(ContextoHB contexto, Objeto registro) {
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
            cuitTitular = cuentaTercero.cuit();

        }

        return Respuesta.exito("cbu", cbu)
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

    public static Respuesta getCuentaDetalleOtros(ContextoHB contexto, Objeto registro) {
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

        alias = cuentaTercero.alias();

        if (!cbu.isEmpty()) {
            if (cbu.startsWith("0000") || cbu.startsWith("4530")) { // Es CVU
                descripcion = RestCatalogo.bancoCVU(cbu.substring(0, 7));
            } else {
                descripcion = RestCatalogo.bancoFiltrado(cbu.substring(0, 3));
            }
        }

        codigoBanco = RestCatalogo.nroBancoInterno(descripcion);

        if (idMoneda.isEmpty()) {
            idMoneda = cuentaTercero.idMoneda();
        }
        if (!esBiMonetaria) {
            esBiMonetaria = cuentaTercero.esBiMonetaria();
        }
        nombreTitular = cuentaTercero.titular();
        cuitTitular = cuentaTercero.cuit();


        return Respuesta.exito("cbu", cbu)
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

    public static Respuesta agendarBeneficiario(ContextoHB contexto) {
        String cuentaDestino = contexto.parametros.string("cuentaDestino");
        String email = contexto.parametros.string("email");
        String comentario = contexto.parametros.string("comentario");
        String descripcion = contexto.parametros.string("descripcion");
        String concepto = contexto.parametros.string("concepto", "VAR");
        String apodo = contexto.parametros.string("apodo", "");

        if (Objeto.anyEmpty(cuentaDestino)) {
            return Respuesta.parametrosIncorrectos();
        }

        CuentaTercero cuentaTercero = new CuentaTercero(contexto, cuentaDestino);
        if (cuentaTercero.cuentaLink.hayError()) {
            return Respuesta.error();
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

        SqlRequest sqlRequest = Sql.request("InsertAgendaTransferenciasComentario", "hbs");
        sqlRequest.sql = "INSERT INTO [Hbs].[dbo].[agenda_transferencias] ([cbu_destino],[nro_cliente],[comentario],[concepto],[descripcion],[nro_cuenta_destino],[documento_beneficiario],[tipo_cuenta_destino],[titular],[autorizada],[cuenta_del_banco],[tipo_documento],[email_destinatario],[alias], [apodo], [moneda_cuenta_destino], [banco_destino]) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        sqlRequest.parametros.add(cuentaTercero.cbu()); // cbu_destino
        sqlRequest.parametros.add(contexto.idCobis()); // nro_cliente
        sqlRequest.parametros.add(comentario); // comentario
        sqlRequest.parametros.add(concepto); // concepto
        sqlRequest.parametros.add(descripcion); // descripcion
        sqlRequest.parametros.add(cuentaTercero.esCuentaBH() ? cuentaTercero.numero() : null); // nro_cuenta_destino
        sqlRequest.parametros.add(cuentaTercero.cuit()); // documento_beneficiario
        sqlRequest.parametros.add(cuentaTercero.tipo()); // tipo_cuenta_destino
        sqlRequest.parametros.add(cuentaTercero.titular());
        sqlRequest.parametros.add(0); // autorizada
        sqlRequest.parametros.add(cuentaTercero.esCuentaBH() ? 1 : 0); // cuenta_del_banco
        sqlRequest.parametros.add(null); // tipo_documento
        sqlRequest.parametros.add(email); // email_destinatario
        sqlRequest.parametros.add(cuentaTercero.alias()); // alias
        sqlRequest.parametros.add(apodo); // apodo
        sqlRequest.parametros.add(cuentaTercero.idMoneda()); // moneda_cuenta_destino
        sqlRequest.parametros.add(cuentaTercero.nrobanco()); // banco_destino

        SqlResponse sqlResponse = Sql.response(sqlRequest);
        if (sqlResponse.hayError) {
            return Respuesta.error();
        }

        if (actualizarApodo) {
            SqlTransferencia sqlTransferencia = new SqlTransferencia();
            sqlTransferencia.actualizarApodoAgendado(contexto.idCobis(), cuentaTercero.cuit(), apodo);
        }

        contexto.insertarContador("AGENDA_BENEFICIARIO");
        String nombreTitular = cuentaTercero.titular() == null ? "" : cuentaTercero.titular();
        try {
            if (true) {
                Objeto parametros = new Objeto();
                parametros.set("Subject", "Agenda de beneficiario");
                parametros.set("NOMBRE_APELLIDO", contexto.persona().nombre() + " " + contexto.persona().apellido());
                Date hoy = new Date();
                parametros.set("FECHA", new SimpleDateFormat("dd/MM/yyyy").format(hoy));
                parametros.set("HORA", new SimpleDateFormat("hh:mm").format(hoy));
                parametros.set("CANAL", "Home Banking");
                parametros.set("NOMBRE_BENEFICIARIO", nombreTitular + " (" + comentario + ")");

                if (HBSalesforce.prendidoSalesforceAmbienteBajoConFF(contexto)) {
                    String salesforce_agenda_beneficiario = ConfigHB.string("salesforce_agenda_beneficiario");
                    parametros.set("IDCOBIS", contexto.idCobis());
                    parametros.set("ISMOBILE", contexto.esMobile());
                    parametros.set("NOMBRE", contexto.persona().nombre());
                    parametros.set("APELLIDO", contexto.persona().apellido());
                    new Futuro<>(() -> HBSalesforce.registrarEventoSalesforce(contexto, salesforce_agenda_beneficiario, parametros));
                } else {
                    RestNotificaciones.envioMail(contexto, ConfigHB.string("doppler_agenda_beneficiario"), parametros);
                }
            }
        } catch (Exception e) {
        }

        contexto.insertarLogBeneficiario(contexto, cuentaTercero.esCuentaBH() ? null : cuentaTercero.cbu(),
                cuentaTercero.esCuentaBH() ? cuentaTercero.numero() : null,
                cuentaTercero.esCuentaBH() ? null : cuentaTercero.cuit(), cuentaTercero.titular(), "A");
        if (HBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_monitoreo_transaccional_2")) {
            HBMonitoring monitoringApi = new HBMonitoring();
            monitoringApi.sendMonitoringAgendaBeneficiario(contexto, cuentaTercero.cbu(), comentario,
                    cuentaTercero.cuit());
        }
        return Respuesta.exito();
    }

    private static String getApodoBeneficiario(ContextoHB contexto, String cuil) {
        try {
            SqlTransferencia sqlTransferencia = new SqlTransferencia();
            SqlResponse sqlResponse = sqlTransferencia.contactosAgendados(contexto.idCobis());
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

    public static Respuesta modificarBeneficiario(ContextoHB contexto) {
        SqlTransferencia sqlTransferencia = new SqlTransferencia();
        String idAgenda = contexto.parametros.string("idAgenda");
        String comentario = contexto.parametros.string("comentario", null);
        String descripcion = contexto.parametros.string("descripcion", null);
        String email = contexto.parametros.string("email", null);

        SqlResponse sqlResponse = sqlTransferencia.actualizarContactoAgendado(contexto.idCobis(), idAgenda, comentario,
                descripcion, email);
        return (sqlResponse.hayError) ? Respuesta.error() : Respuesta.exito();
    }

    public static Respuesta eliminarBeneficiario(ContextoHB contexto) {
        SqlTransferencia sqlTranferencia = new SqlTransferencia();
        String idAgenda = contexto.parametros.string("idAgenda");

        if (Objeto.anyEmpty(idAgenda)) {
            return Respuesta.parametrosIncorrectos();
        }
        SqlResponse sqlResponseConsulta = sqlTranferencia.obtenerContactoAgendadoById(contexto.idCobis(), idAgenda,
                idAgenda);
        if (sqlResponseConsulta.hayError) {
            return Respuesta.error();
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
        SqlResponse sqlResponse = sqlTranferencia.eliminarAgendaContactos(contexto.idCobis(), idAgenda);
        if (sqlResponse.hayError) {
            return Respuesta.error();
        }
        contexto.insertarLogBeneficiario(contexto, cbu, cuenta, documento, nombre, "B");
        return Respuesta.exito();
    }

    /* ========== AUMENTO LIMITE ========== */
    public static Respuesta limites(ContextoHB contexto) {
        Boolean buscarAumentoLimite = contexto.parametros.bool("buscarAumentoLimite", false);
        Boolean buscarSoloAumentoLimiteHoy = contexto.parametros.bool("buscarSoloAumentoLimiteHoy", false);

        BigDecimal limitePesos = new BigDecimal(ConfigHB.string("configuracion_limite_transferencia_pesos"));
        BigDecimal limiteDolares = new BigDecimal(ConfigHB.string("configuracion_limite_transferencia_dolares"));

        Respuesta respuesta = new Respuesta();
        respuesta.set("tieneCuentaPropia", contexto.tieneCuentaPropia());
        respuesta.set("limiteTransferenciaPesos", limitePesos);
        respuesta.set("limiteTransferenciaDolares", limiteDolares);
        respuesta.set("ingresoLimiteMaximoPesos",
                ConfigHB.integer("ingreso_limite_maximo_transferencia_pesos", 20000000));

        if (buscarAumentoLimite || buscarSoloAumentoLimiteHoy) {
            respuesta.set("tieneAumentoLimite", false);
            ExecutorService executorService = Concurrencia.executorService(contexto.cuentas());
            for (Cuenta cuenta : contexto.cuentas()) {
                executorService.submit(() -> {
                    String desde = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                    ApiResponse response = RestTransferencia.cuentasGetLimites(contexto, cuenta, false, desde);

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
                        limite.set("fecha", item.date("fecha", "yyyy-MM-dd", "dd/MM/yyyy"));
                        limite.set("esHoy", item.date("fecha", "yyyy-MM-dd", "dd/MM/yyyy")
                                .equals(new SimpleDateFormat("dd/MM/yyyy").format(new Date())));
                        limite.set("idMoneda", item.string("idMoneda"));
                        limite.set("tipo", cuenta.descripcionCorta());
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

    public static Respuesta aumentarLimite(ContextoHB contexto) {

        String idCuenta = contexto.parametros.string("idCuenta");
        String fecha = contexto.parametros.date("fecha", "d/M/yyyy", "yyyy-MM-dd");
        BigDecimal monto = contexto.parametros.bigDecimal("monto");

        Respuesta condicionError = validateAumentarLimite(contexto);
        if (Objects.nonNull(condicionError)) {
            return condicionError;
        }
        Cuenta cuenta = contexto.cuenta(idCuenta);
        if (cuenta == null) {
            return Respuesta.estado("CUENTA_NO_EXISTE", contexto.csmIdAuth);
        }
        ApiResponse responseLimiteByCuenta = RestTransferencia.cuentasGetLimites(contexto, cuenta, true, fecha);

        if (responseLimiteByCuenta.objetos().size() >= ConfigHB.integer("nro_maximo_aumento_litime_diario", 1)) {
            return Respuesta.estado("MAXIMO_SOLICITUDES_FECHA_PROGRAMADA", contexto.csmIdAuth);
        }

        if (!esFechaPosteriorHoy(fecha))
            return Respuesta.estado("FECHA_ANTERIOR_A_HOY", contexto.csmIdAuth);

        ApiResponse response = null;

        if (HBAplicacion.funcionalidadPrendida("hb_prendido_transf_importe_superior")) {
            TarjetaDebito tarjetaDebitoAsociada = contexto.tarjetaDebitoAsociada(cuenta);
            response = RestTransferencia.CuentasTISLimites(contexto, cuenta, fecha, monto, tarjetaDebitoAsociada);
            if (!response.hayError()) {
                response = RestTransferencia.CuentasPostLimites(contexto, cuenta, fecha, monto);
            }
        } else {
            response = RestTransferencia.CuentasPostLimites(contexto, cuenta, fecha, monto);
        }

        if (response.hayError()) {
            return Respuesta.estado("codigo", contexto.csmIdAuth);
        }

        registroSolicitudLimite(contexto, cuenta);

        Objeto parametros = new Objeto();
        parametros.set("NOMBRE_APELLIDO", contexto.persona().nombre() + " " + contexto.persona().apellido());
        Date hoy = new Date();
        parametros.set("FECHA", new SimpleDateFormat("dd/MM/yyyy").format(hoy));
        parametros.set("HORA", new SimpleDateFormat("HH:mm").format(hoy));
        parametros.set("CANAL", "Home Banking");

        if (HBSalesforce.prendidoSalesforceAmbienteBajoConFF(contexto)) {
            String salesforce_aumento_limite = ConfigHB.string("salesforce_aumento_limite");
            parametros.set("NOMBRE", contexto.persona().nombre());
            parametros.set("APELLIDO", contexto.persona().apellido());
            parametros.set("IDCOBIS", contexto.idCobis());
            parametros.set("ISMOBILE", contexto.esMobile());
            new Futuro<>(() -> HBSalesforce.registrarEventoSalesforce(contexto, salesforce_aumento_limite, parametros));
        } else {
            new Futuro<>(() -> RestNotificaciones.envioMail(contexto, ConfigHB.string("doppler_aumento_limite"), parametros));
        }

        return Respuesta.exito().set("csmIdAuth", contexto.csmIdAuth);
    }

    private static boolean esFechaHoy(String date) {
        return date.equals(new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime()));
    }

    private static boolean noActivoSoftTokenEnElUltimoDia(ContextoHB contexto) {
        return !HBSoftToken.activoSoftTokenEnElUltimoDia(contexto);
    }

    private static boolean noModificoDatosPersonalesEnElUltimoDia(ContextoHB contexto) {
        Respuesta respuesta = isRiskForChangeInformation(contexto);
        if (respuesta.hayError())
            return false;
        return (boolean) respuesta.get("enableb_operator");
    }

    private static boolean esFechaPosteriorHoy(String date) {
        return LocalDate.parse(date).isAfter(LocalDate.now());
    }

    private static SqlResponse registroSolicitudLimite(ContextoHB contexto, Cuenta cuenta) {
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

    private static Respuesta validateAumentarLimite(ContextoHB contexto) {
        String pattern = "yyyy-MM-dd HH:mm:ss";
        SqlTransferencia sqlTransferencia = new SqlTransferencia();
        String idCuenta = contexto.parametros.string("idCuenta");
        String fecha = contexto.parametros.date("fecha", "d/M/yyyy", "yyyy-MM-dd");
        BigDecimal monto = contexto.parametros.bigDecimal("monto");
        String csmId = contexto.parametros.string("csmId", "");
        String checksum = contexto.parametros.string("checksum", "");

        if (Objeto.anyEmpty(idCuenta, fecha, monto))
            return Respuesta.parametrosIncorrectos();

        boolean esMigrado = contexto.esMigrado(contexto);

        if (esMigrado && Objeto.anyEmpty(csmId, checksum))
            return Respuesta.parametrosIncorrectos();

        Respuesta respuestaValidaTransaccion = contexto.validarTransaccion(contexto, esMigrado, "aumento-limite-transferencia", JourneyTransmitEnum.HB_INICIO_SESION);
        if (respuestaValidaTransaccion.hayError())
            return respuestaValidaTransaccion;

        ApiResponse responseCatalogo = RestCatalogo.calendarioFecha(contexto, fecha);
        if (!responseCatalogo.hayError()) {
            try {
                if (!DIA_HABIL.equals(responseCatalogo.objetos().get(0).string("esDiaHabil"))) {
                    return Respuesta.estado("DIA_NO_HABIL", contexto.csmIdAuth);
                }
            } catch (Exception e) {
            }
        }

        LocalDateTime fechaProgramada = getLocalDatetimeByFormat(fecha + " 00:00:00", pattern);
        if (!isFechaInicioValida(fechaProgramada))
            return Respuesta.estado("DIA_NO_PERMITIDO", contexto.csmIdAuth);

        LocalDateTime diaCreacion = LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(pattern);
        SqlResponse response = sqlTransferencia.obtenerSolicitudPorDiaCreacion(contexto.idCobis(),
                diaCreacion.format(dtf), idCuenta);

        if (response.hayError)
            return Respuesta.error(contexto.csmIdAuth);

        if (response.registros.size() >= ConfigHB.integer("nro_peticiones_diarias", 1))
            return Respuesta.estado("SOLICITUD_X_HOY_DIA_ALCANZADA", contexto.csmIdAuth);

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
    public static Respuesta conceptos(ContextoHB contexto) {
        Boolean traerSoloItemsPagoSueldo = contexto.parametros.bool("traerSoloItemsPagoSueldo", false);
        Boolean traerSinItemsPagoSueldo = contexto.parametros.bool("traerSinItemsPagoSueldo", false);
        Objeto conceptos = new Objeto();
        Map<String, String> mapa = TransferenciaService.conceptos();
        for (String clave : mapa.keySet()) {
            if (traerSoloItemsPagoSueldo) {
                if (!clave.equals("HAB")) {
                    continue;
                }
            }
            if (traerSinItemsPagoSueldo) {
                if (clave.equals("HAB")) {
                    continue;
                }
            }
            String descripcion = mapa.get(clave);
            conceptos.add(new Objeto().set("id", clave).set("descripcion", descripcion));
        }
        return Respuesta.exito("conceptos", conceptos);
    }

    public static Respuesta cuentaTercero(ContextoHB contexto) {

        String cuentaDestino = contexto.parametros.string("cuentaDestino");
        Boolean prioridadCoelsa = contexto.parametros.bool("prioridadCoelsa", false);
        Boolean ignorarLink = contexto.parametros.bool("ignorarLink", false);

        if (Objeto.anyEmpty(cuentaDestino)) {
            return Respuesta.parametrosIncorrectos();
        }

        if (!prioridadCoelsa) {
            TarjetaDebito TDdefect = contexto.tarjetaDebitoPorDefecto();

            if (TDdefect == null) {
                return Respuesta.estado("SIN_TARJETA_DEBITO");
            }
        }
        CuentaTercero cuentaTercero;
        if (ignorarLink) {
            cuentaTercero = new CuentaTercero(contexto, cuentaDestino, ignorarLink);
        } else {
            cuentaTercero = new CuentaTercero(contexto, cuentaDestino);
        }
        if (cuentaTercero.esCuentaBH() && cuentaTercero.cuentaCoelsa.hayError()) {
            return Respuesta.estado("ALIAS_NO_EXISTE");
        }
        if (prioridadCoelsa) {
            if (cuentaTercero.cuentaCoelsa.string("codigo").equals("9904")) {
                return Respuesta.estado("ALIAS_NO_EXISTE");
            }
            if (cuentaTercero.cuentaCoelsa.string("codigo").equals("0110")) {
                return Respuesta.estado("ALIAS_NO_EXISTE");
            }
            if (cuentaTercero.cuentaCoelsa.string("codigo").equals("0160")) {
                return Respuesta.estado("CUENTA_INACTIVA");
            }
            if (cuentaTercero.cuentaCoelsa.string("codigo").equals("0170")) {
                return Respuesta.estado("ALIAS_NO_EXISTE");
            }
            if (cuentaTercero.cuentaLink.hayError() && cuentaTercero.cuentaCoelsa.hayError()) {
                return Respuesta.estado("CUENTA_INACTIVA");
            }
        }
        if (!cuentaTercero.cuentaEncontrada && prioridadCoelsa) {
            // devuelvo lo mismo que antes para debin, asi no cambio esa funcionalidad

            return Respuesta.estado("ALIAS_NO_EXISTE");
        }

        if (!cuentaTercero.cuentaEncontrada && !prioridadCoelsa) {
            if (CuentasService.esAlias(cuentaDestino)) {
                return Respuesta.estado("ALIAS_NO_EXISTE");
            }
            if (cuentaTercero.cuentaCoelsa.json == null && cuentaTercero.cuentaBH.json == null
                    && cuentaTercero.cuentaLink.json == null) {
                return Respuesta.estado("NO_EXISTE_DATO"); // en este caso no trajo nada
            }
            return Respuesta.error();
        }

        if (!CuentasService.esAlias(cuentaDestino) && !CuentasService.esCuentaBH(cuentaDestino) && !CuentasService.esCbuBH(cuentaDestino)) {
            boolean cbuConError = !cuentaTercero.esCvu() && cuentaTercero.cuentaLink.hayError();
            if (cbuConError && !prioridadCoelsa) {
                if (CuentasService.esCbu(cuentaDestino) && cuentaTercero.cuentaLink.hayError() && "CBU INCORRECTO".equals(cuentaTercero.cuentaCoelsa.string("mensajeAlUsuario"))) {
                    return Respuesta.estado("CBU_NO_EXISTE");
                }
                return Respuesta.error();
            }
        }

        if (cuentaTercero.cuentaCoelsa.hayError()
                && (prioridadCoelsa || CuentasService.esAlias(cuentaDestino) || cuentaTercero.esCvu())) {
            if ("ALIAS NO EXISTE".equals(cuentaTercero.cuentaCoelsa.string("mensajeAlUsuario"))) {
                return Respuesta.estado("ALIAS_NO_EXISTE");
            } else if (cuentaTercero.cuentaCoelsa.string("codigo").equals("0160")) {
                return Respuesta.estado("CUENTA_INACTIVA");
            } else if (cuentaTercero.cuentaCoelsa.string("codigo").equals("0170")) {
                return Respuesta.estado("ALIAS_NO_EXISTE");
            } else if ("CBU NO EXISTE".equals(cuentaTercero.cuentaCoelsa.string("mensajeAlUsuario"))) {
                return Respuesta.estado("CBU_NO_EXISTE");
            } else
                return Respuesta.estado("ERROR_COELSA");
        }

        Objeto item = new Objeto();
        String id = cuentaTercero.cbu();
        item.set("id", id);
        item.set("beneficiario", cuentaTercero.titular());
        item.set("cbu", id);
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
        item.set("idMoneda", cuentaTercero.idMoneda());
        String descripcion = "";
        if (id.startsWith("0000")) { // Es CVU
            descripcion = RestCatalogo.bancoCVU(id.substring(0, 7));
        } else {
            descripcion = RestCatalogo.bancoFiltrado(id.substring(0, 3));
        }
        String codigobanco = RestCatalogo.nroBancoInterno(descripcion);

        item.set("bancoP", cuentaTercero.banco());
        item.set("logo", RestCatalogo.bancoLogo(codigobanco));
        item.set("banco", descripcion);
        item.set("idMonedas", cuentaTercero.idMonedas());

        if (prioridadCoelsa) {

            String[] bancosLink = ConfigHB.string("bancos_link",
                            "011_014_020_029_044_045_056_083_086_093_094_097_203_247_254_268_277_281_301_309_311_312_315_321_330_341_386_426_431_432")
                    .split("_");

            Cuenta cuenta = contexto.cuentas().get(0);

            String fechaAlta = cuenta.fechaAlta("dd/MM/yyyy");
            String fechaActual = new SimpleDateFormat("dd/MM/yyyy").format(new Date());

            if (fechaAlta.equals(fechaActual))
                return Respuesta.estado("FECHA_ALTA_CUENTA_HOY");

            Boolean esMismoTitular = cuentaTercero.mismoTitularColesa(contexto.persona().cuit());

            item.set("esBancoHipotecario", Objeto.setOf("044").contains(cuentaTercero.cbu().substring(0, 3)));
            item.set("esMismoTitular", esMismoTitular);
            item.set("pickUpAutomatico", Objeto.setOf(bancosLink).contains(cuentaTercero.cbu().substring(0, 3)));

            if (!esMismoTitular) {
                Boolean esCotitular = cuentaTercero.esCotitularCoelsa(contexto.persona().cuit());

                item.set("esCotitular", esCotitular);
            }
        }

        Objeto infoAdicional = new Objeto();
        if (cuentaTercero != null) {
            EnumPropuestasInversion propuesta = (EnumPropuestasInversion) HBInversion
                    .evaluarPropuestaInversionPreTransferencia(contexto, cuentaTercero)
                    .get("propuesta");
            infoAdicional.set("codMensajeUsuario", propuesta.getCodigoNemonicoPropuesta());
        }

        if ("".equals(item.string("id")) && "".equals(item.string("beneficiario")) && "".equals(item.string("cbu")) && "".equals(item.string("alias")) && "".equals(item.string("cuit"))) {
            return Respuesta.error();
        }
        if (HBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendidoIdentificaCbuCVu")) {
            item.set("esCvu", cuentaTercero.esCvu());
            item.set("esCbu", cuentaTercero.esCuentaBH() || (!cuentaTercero.esCuentaBH() && !cuentaTercero.esCvu()));
        }

        Respuesta respuesta = new Respuesta();
        respuesta.set("cuenta", item);
        respuesta.set("infoAdicional", infoAdicional);
        return respuesta;
    }

    public static Respuesta cuentaTerceroCoelsa(ContextoHB contexto) {
        contexto.parametros.set("prioridadCoelsa", true);
        contexto.parametros.set("ignorarLink", true);
        return cuentaTercero(contexto);
    }

    /* emm-20190416-desde */
    public static Respuesta compraVentaDolarFueraDeHorario(ContextoHB contexto) {
        String horaCompraVentaDolarDesde = ConfigHB.string("horario_compra_venta_dolares_inicio", "09:30");
        String horaCompraVentaDolarHasta = ConfigHB.string("horario_compra_venta_dolares_fin", "21:00");
        String horaDesdeFormateado = horaCompraVentaDolarDesde;
        String horaHastaFormateado = horaCompraVentaDolarHasta;

        Respuesta respuesta = new Respuesta();
        respuesta.set("horaDesde", horaCompraVentaDolarDesde);
        respuesta.set("horaHasta", horaCompraVentaDolarHasta);
        respuesta.set("horaDesdeFormateado", horaDesdeFormateado);
        respuesta.set("horaHastaFormateado", horaHastaFormateado);

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        Date hoy = new Date();
        String hora = sdf.format(hoy);
        if (ConfigHB.esProduccion() || ConfigHB.esHomologacion()) {
            if (hora.compareTo(horaCompraVentaDolarDesde) < 0 || hora.compareTo(horaCompraVentaDolarHasta) > 0) {
                respuesta.setEstado("FUERA_DE_HORARIO");
            }
        }

        SimpleDateFormat sdfDayOfWeek = new SimpleDateFormat("u");
        String diaSemana = sdfDayOfWeek.format(hoy);

        if (ConfigHB.esProduccion() || ConfigHB.esHomologacion()) {
            if (diaSemana.equals("6") || diaSemana.equals("7")) {
                respuesta.setEstado("FUERA_DE_HORARIO");
            }
        }

        return respuesta;
    }
    /* emm-20190416-hasta */

    public static Respuesta isRiskForChangeInformation(ContextoHB contexto) {
        if (Objeto.anyEmpty(contexto.idCobis())) {
            return Respuesta.estado("ERROR");
        }
        DisableService disableService = new DisableService();
        LocalDateTime nowTime = LocalDateTime.now();
        nowTime = nowTime.plusHours(-1 * disableService.calculateHourDelay(nowTime));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String inicio = nowTime.format(formatter);

        String tipo = ConfigHB.string(esClienteNuevo(contexto, inicio) ? "cambio_information_no_permitido_nuevo" : "cambio_information_no_permitido");

        List<Objeto> registros = RestContexto.obtenerContador(contexto.idCobis(), tipo, inicio);
        if (Objects.isNull(registros))
            return Respuesta.estado("ERROR");

        Boolean permission = disableService.getEnabledToOperator(registros);
        return Respuesta.exito("enableb_operator", permission);
    }

    public static Respuesta puedeTransferirEspecialMismoDia(ContextoHB contexto) {
        if (Objeto.anyEmpty(contexto.idCobis()))
            return Respuesta.estado("SIN_PSEUDO_SESION");

        return Respuesta.exito("puede_transferir_especial_mismo_dia",
                noActivoSoftTokenEnElUltimoDia(contexto) && noModificoDatosPersonalesEnElUltimoDia(contexto));
    }

    private static boolean esClienteNuevo(ContextoHB contexto, String fechaInicio) {
        return SqlClientesOperadores.esUsuarioNuevo(contexto, fechaInicio);
    }

    public static Object modificarBeneficiarioApodo(ContextoHB ctx) {
        String apodo = ctx.parametros.string("apodo", null);
        String cuilBeneficiario = ctx.parametros.string("cuil", null);

        if (Objeto.anyEmpty(apodo, cuilBeneficiario)) {
            return Respuesta.parametrosIncorrectos();
        }

        SqlTransferencia sqlTransferencia = new SqlTransferencia();
        SqlResponse sqlResponse = sqlTransferencia.actualizarApodoAgendado(ctx.idCobis(), cuilBeneficiario, apodo);
        return (sqlResponse.hayError) ? Respuesta.error() : Respuesta.exito();
    }
}
