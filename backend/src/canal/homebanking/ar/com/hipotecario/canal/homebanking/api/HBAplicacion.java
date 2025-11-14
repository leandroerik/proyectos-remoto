package ar.com.hipotecario.canal.homebanking.api;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.sql.Connection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import ar.com.hipotecario.canal.homebanking.lib.*;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import ar.com.hipotecario.backend.Eventos;
import ar.com.hipotecario.backend.Servidor;
import ar.com.hipotecario.canal.homebanking.CanalHomeBanking;
import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.Respuesta;
import ar.com.hipotecario.canal.homebanking.base.ConfigHB;
import ar.com.hipotecario.canal.homebanking.base.Objeto;
import ar.com.hipotecario.canal.homebanking.conector.Api;
import ar.com.hipotecario.canal.homebanking.conector.ApiRequest;
import ar.com.hipotecario.canal.homebanking.conector.ApiResponse;
import ar.com.hipotecario.canal.homebanking.conector.Sql;
import ar.com.hipotecario.canal.homebanking.conector.SqlRequest;
import ar.com.hipotecario.canal.homebanking.conector.SqlResponse;
import ar.com.hipotecario.canal.homebanking.excepcion.UnauthorizedException;
import ar.com.hipotecario.canal.homebanking.servicio.CuentasService;
import ar.com.hipotecario.canal.homebanking.servicio.RestContexto;
import ar.com.hipotecario.canal.homebanking.servicio.RestPersona;
import ar.com.hipotecario.canal.homebanking.servicio.SqlHomebanking;

public class HBAplicacion {

    public static Objeto error(ContextoHB contexto) {
        throw new RuntimeException();
    }

    public static Object eventos(ContextoHB contexto) {
        String idCobis = contexto.parametros.string("idCobis");

        StringBuilder html = new StringBuilder();
        List<String> eventos = Eventos.consultar(idCobis);
//			Collections.reverse(eventos);
        html.append("<pre>");
        for (String evento : eventos) {
            html.append(evento).append("<br/>");
        }
        html.append("</pre>");
        return html;
    }

    public static Objeto estado(ContextoHB contexto) {
        Objeto objeto = new Objeto();
        objeto.set("status", "UP");
        objeto.set("maxMemory", Runtime.getRuntime().maxMemory() / 1024 / 1024);
        objeto.set("totalMemory", Runtime.getRuntime().totalMemory() / 1024 / 1024);
        objeto.set("freeMemory", Runtime.getRuntime().freeMemory() / 1024 / 1024);
        objeto.set("allocMemory", (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024);
        contexto.responseHeader("Content-Type", "application/json");
        return objeto;
    }

    private static List<String> lineas(File base, String value) {
        try {
            File file = new File(base.getAbsolutePath(), value);
            if (file.exists()) {
                return Files.readAllLines(file.toPath());
            }
        } catch (Exception e) {
        }
        return new ArrayList<>();
    }

    public static Object version(ContextoHB contexto) {
        File path1 = new File("/app/.git/");
        File path2 = new File(System.getProperty("user.dir"), "../.git/");
        File path = path1.exists() ? path1 : path2.exists() ? path2 : null;
        Objeto respuesta = new Objeto();

        String tag = "";
        String hash = "";
        String shortHash = "";
        for (String linea : lineas(path, "HEAD")) {
            if (linea.contains("ref")) {
                tag = linea.substring(linea.lastIndexOf('/') + 1);
            } else {
                hash = linea;
            }
        }
        for (String linea : lineas(path, "packed-refs")) {
            if (!hash.isEmpty() && linea.contains(hash)) {
                tag = linea.substring(linea.lastIndexOf('/') + 1);
//					break;
            }
            if (!tag.isEmpty() && linea.contains("/" + tag)) {
                hash = linea.split(" ")[0];
//					break;
            }
        }
        if (hash.length() > 7) {
            shortHash = hash.substring(0, 7);
        }

        String buildTime = "";
        for (String linea : lineas(path, "BUILD_TIME")) {
            buildTime = linea;
        }

        respuesta.set("version", tag);
        respuesta.set("hash", shortHash);
        respuesta.set("full-hash", hash);
        respuesta.set("build", buildTime);
        respuesta.set("inicio-pod", Servidor.tiempoInicio);
        return respuesta;
    }

    public static Objeto headers(ContextoHB contexto) {
        Objeto objeto = new Objeto();
        for (String clave : contexto.request.headers()) {
            objeto.set(clave, contexto.request.headers(clave));
        }
        return objeto;
    }

    public static Object cliente(ContextoHB contexto) {
        String id = contexto.parametros.string("id");
//		ContextoHB nuevoContexto = new ContextoHB(id, contexto.idSesion(), contexto.ip());
        ContextoHB nuevoContexto = new ContextoHB(contexto.request, contexto.response, contexto.canal, contexto.ambiente);
        nuevoContexto.parametros.set("llamadoInterno", false);
        nuevoContexto.sesion().idCobis = id;
        Respuesta respuesta = new Respuesta();
        respuesta.set("permitirPP", !RestContexto.cambioDetectadoParaNormativoPPV2(nuevoContexto, true));
        respuesta.set("bloqueadoRiesgoNet", nuevoContexto.bloqueadoRiesgoNet());
        respuesta.set("bloqueadoRiesgoFraude", SqlHomebanking.bloqueadoPorFraude(nuevoContexto.idCobis()));
        return respuesta;
    }

    public static Object usuario(ContextoHB contexto) {
        UnauthorizedException.ifNot(contexto.requestHeader("Authorization"));

        String buscar = contexto.parametros.string("buscar");

        Boolean esNumerico = buscar.chars().allMatch(Character::isDigit);
        Integer tamaño = buscar.length();

        Boolean posibleCuit = esNumerico && tamaño == 11;
        Boolean posibleCobis = esNumerico && tamaño > 0 && tamaño != 11;
        Boolean posibleDni = esNumerico && tamaño >= 7 && tamaño <= 8;
        Boolean posibleNombre = !esNumerico && tamaño > 0;

        List<String> cuits = new ArrayList<>();

        if (posibleCuit) {
            cuits.add(buscar);
        }

        if (posibleCobis) {
            ApiRequest request = Api.request("Cliente", "personas", "GET", "/clientes/{idCliente}", contexto);
            request.header("x-usuario", ConfigHB.string("configuracion_usuario"));
            request.path("idCliente", buscar);
            request.permitirSinLogin = true;

            ApiResponse response = Api.response(request, contexto.idCobis());
            if (!response.hayError()) {
                for (Objeto cliente : response.objetos()) {
                    cuits.add(cliente.string("cuit"));
                }
            }
        }

        if (posibleDni) {
            ApiRequest request = Api.request("ConsultaPersonas", "personas", "GET", "/cuils", contexto);
            request.header("x-usuario", ConfigHB.string("configuracion_usuario"));
            request.query("dni", buscar);
            request.permitirSinLogin = true;

            ApiResponse response = Api.response(request);
            if (!response.hayError()) {
                for (Objeto cuil : response.objetos()) {
                    cuits.add(cuil.string("cuil"));
                }
            }
        }

        if (posibleNombre) {
            ApiRequest request = Api.request("ConsultaPersonas", "personas", "GET", "/cuils", contexto);
            request.header("x-usuario", ConfigHB.string("configuracion_usuario"));
            request.query("apYNom", buscar);
            request.permitirSinLogin = true;

            ApiResponse response = Api.response(request);
            if (!response.hayError()) {
                for (Objeto cuil : response.objetos()) {
                    cuits.add(cuil.string("cuil"));
                }
            }
        }

        Respuesta respuesta = new Respuesta();
        for (String cuit : cuits) {
            ApiRequest request = Api.request("Persona", "personas", "GET", "/personas/{id}", contexto);
            request.header("x-usuario", ConfigHB.string("configuracion_usuario"));
            request.path("id", cuit);
            request.permitirSinLogin = true;

            ApiResponse response = Api.response(request);
            if (!response.hayError()) {
                Objeto item = new Objeto();
                item.set("cobis", response.string("idCliente"));
                item.set("dni", response.string("numeroDocumento"));
                item.set("cuit", response.string("cuit"));
                item.set("nombre", response.string("apellidos") + " " + response.string("nombres"));
                try {
                    item.set("ultimaConexion", new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(SqlHomebanking.fechaHoraUltimaConextion(response.string("idCliente"))));
                } catch (Exception e) {
                    item.set("ultimaConexion", "***");
                }
                respuesta.add(item);
            }
        }
        return respuesta;
    }

    public static Respuesta configuracion(ContextoHB contexto) {
        Objeto config = new Objeto();
        config.set("drs_client", ConfigHB.string("app_drs_client_id"));
        config.set("google_analytics_id", ConfigHB.string("configuracion_google_analytics_id"));
        config.set("mantenimiento_titulo", ConfigHB.string("mantenimiento_titulo", "Servicio momentáneamente suspendido."));
        config.set("mantenimiento_mensaje", ConfigHB.string("mantenimiento_mensaje", "Disculpá las molestias ocasionadas, intentá más tarde por favor."));
        config.set("permitirLetrasDocumento", "true".equals(ConfigHB.string("permitir-letras-documento", "false")));
        if (ConfigHB.string("mostrar_aviso_mantenimiento", "false").equals("true")) {
            config.set("mantenimiento_futuro", ConfigHB.string("aviso_mantenimiento_futuro", null));
        }
        Boolean prendidoLineaRoja = true;
        if (prendidoLineaRoja) {
            config.set("prendido_linea_roja", prendidoLineaRoja);
        }

        Boolean prendidoSeguro = true;
        if (prendidoSeguro) {
            config.set("prendido_seguro", prendidoSeguro);
        }

        Boolean prendidoSeguroVida = true;
        if (prendidoSeguroVida) {
            config.set("prendido_seguro_vida", prendidoSeguroVida);
        }

        Boolean prendidoSeguroSalud = true;
        if (prendidoSeguroSalud) {
            config.set("prendido_seguro_salud", prendidoSeguroSalud);
        }

        Boolean prendidoSeguroAP = "true".equals(ConfigHB.string("prendido_seguro_ap"));
        if (prendidoSeguroAP) {
            config.set("prendido_seguro_ap", prendidoSeguroAP);
        }

        Boolean prendidoSeguroBM = "true".equals(ConfigHB.string("prendido_seguro_bm"));
        if (prendidoSeguroBM) {
            config.set("prendido_seguro_bm", prendidoSeguroBM);
        }

        Boolean prendidoSeguroHogar = "true".equals(ConfigHB.string("prendido_seguro_hogar"));
        if (prendidoSeguroHogar) {
            config.set("prendido_seguro_hogar", prendidoSeguroHogar);
        }

        Boolean prendidoSeguroATM = "true".equals(ConfigHB.string("prendido_seguro_atm"));
        if (prendidoSeguroATM) {
            config.set("prendido_seguro_atm", prendidoSeguroATM);
        }

        Boolean prendidoSeguroCompraProtegida = "true".equals(ConfigHB.string("prendido_seguro_compra_protegida"));
        if (prendidoSeguroCompraProtegida) {
            config.set("prendido_seguro_compra_protegida", prendidoSeguroCompraProtegida);
        }
        Boolean prendidoSeguroAPMayores = "true".equals(ConfigHB.string("prendido_seguro_ap_mayores"));
        if (prendidoSeguroAPMayores)
            config.set("prendido_seguro_ap_mayores", prendidoSeguroAPMayores);

        Boolean prendidoSeguroAuto= "true".equals(ConfigHB.string("prendido_seguro_auto"));
        if (prendidoSeguroAuto)
            config.set("prendido_seguro_auto", prendidoSeguroAuto);
        Boolean prendidoSeguroCombo= "true".equals(ConfigHB.string("prendido_seguro_combo"));
        if (prendidoSeguroCombo)
            config.set("prendido_seguro_combo", prendidoSeguroCombo);

        Boolean prendidoSeguroComboBM= "true".equals(ConfigHB.string("prendido_seguro_combo_bm"));
        if (prendidoSeguroComboBM)
            config.set("prendido_seguro_combo_bm", prendidoSeguroComboBM);

        Boolean prendidoBuhoBot = "true".equals(ConfigHB.string("prendidoBuhoBot"));
        if (prendidoBuhoBot) {
            config.set("prendidoBuhoBot", prendidoBuhoBot);
        }
        Boolean prendidoBuhoBotRelease = "true".equals(ConfigHB.string("prendidoBuhoBotRelease"));
        if (prendidoBuhoBotRelease) {
            config.set("prendidoBuhoBotRelease", prendidoBuhoBotRelease);
        }

        Boolean prendidoSeguroMovilidad = true;
        if (prendidoSeguroMovilidad) {
            config.set("prendido_seguro_movilidad", prendidoSeguroMovilidad);
        }

        config.set("prendidoLineaRojaNuevoFlujo", variablePrendida("prendido_linea_roja_nuevo_flujo"));

        Boolean prendidoSeguroMascotas = "true".equals(ConfigHB.string("prendido_seguro_mascotas"));
        if (prendidoSeguroMascotas) {
            config.set("prendido_seguro_mascotas", prendidoSeguroMascotas);
        }

        Boolean prendidoSeguroCaucion = "true".equals(ConfigHB.string("prendido_seguro_caucion"));
        if (prendidoSeguroCaucion)
            config.set("prendido_seguro_caucion", prendidoSeguroCaucion);


        Boolean prendidoSeguroSaludSenior = "true".equals(ConfigHB.string("prendido_seguro_salud_senior"));
        if (prendidoSeguroSaludSenior) {
            config.set("prendido_seguro_salud_senior", prendidoSeguroSaludSenior);
        }


        Boolean prendidoDrs = "true".equals(ConfigHB.string("app_drs"));
        config.set("prendido_drs", prendidoDrs);
        config.set("alta_usuario_transmit", funcionalidadPrendida("alta_usuario_transmit"));

        return Respuesta.exito("config", config);
    }

    public static Respuesta consultarLegalDefensaConsumidor(ContextoHB contexto) {

        Respuesta respuesta = new Respuesta();

        Date hoy = new Date();
        SqlRequest sqlRequest = Sql.request("SelectLegalDefensaConsumidorTexto", "homebanking");

        sqlRequest.sql = "SELECT t.id, t.data, u.id_cobis FROM [homebanking].[dbo].[legal_defensa_consumidor_texto] t ";
        sqlRequest.sql += "LEFT JOIN  [homebanking].[dbo].[legal_defensa_consumidor_usuarios] u ON t.id = u.id_legal AND u.id_cobis = ? ";
        sqlRequest.sql += "WHERE ";
        sqlRequest.sql += "? BETWEEN vigencia_desde AND vigencia_hasta";

        sqlRequest.parametros.add(contexto.idCobis());
        sqlRequest.parametros.add(new SimpleDateFormat("yyyyMMdd").format(hoy));

        SqlResponse sqlResponse = Sql.response(sqlRequest);
        for (Objeto item : sqlResponse.registros) {
            respuesta.set("legalDefensaConsumidor", item.string("data"));
            respuesta.set("mostrarLegalDefensaConsumidor", false);
            if ("".equals(item.string("id_cobis"))) {
                respuesta.set("mostrarLegalDefensaConsumidor", true);
            }
        }
        return respuesta;
    }

    public static Respuesta aceptarLegalDefensaConsumidor(ContextoHB contexto) {
        if (contexto.idCobis() == null) {
            return Respuesta.estado("SIN_SESION");
        }

        Respuesta respuesta = new Respuesta();
        try {
            Date hoy = new Date();
            SqlRequest sqlRequestConsulta = Sql.request("SelectLegalDefensaConsumidorTexto", "homebanking");

            sqlRequestConsulta.sql = "SELECT t.id, t.data, u.id_cobis FROM [homebanking].[dbo].[legal_defensa_consumidor_texto] t ";
            sqlRequestConsulta.sql += "LEFT JOIN  [homebanking].[dbo].[legal_defensa_consumidor_usuarios] u ON t.id = u.id_legal AND u.id_cobis = ? ";
            sqlRequestConsulta.sql += "WHERE ";
            sqlRequestConsulta.sql += "? BETWEEN vigencia_desde AND vigencia_hasta";

            sqlRequestConsulta.parametros.add(contexto.idCobis());
            sqlRequestConsulta.parametros.add(new SimpleDateFormat("yyyyMMdd").format(hoy));
            Integer idTexto = null;
            SqlResponse sqlResponse = Sql.response(sqlRequestConsulta);
            for (Objeto item : sqlResponse.registros) {
                if (!"".equals(item.string("id_cobis"))) {
                    respuesta.setEstado("ERROR_YA_LO_ACEPTO");
                    return Respuesta.estado("ERROR_YA_LO_ACEPTO");
                }
                idTexto = item.integer("id");
            }

            SqlRequest sqlRequest = Sql.request("InsertDefensaConsumidorTexto", "homebanking");
            sqlRequest.sql = "INSERT INTO [Homebanking].[dbo].[legal_defensa_consumidor_usuarios] (id_legal, id_cobis, fecha) VALUES (?, ?, GETDATE())";
            sqlRequest.add(idTexto);
            sqlRequest.add(contexto.idCobis());

            Sql.response(sqlRequest);
        } catch (Exception e) {
            return Respuesta.estado("ERROR");
        }

        return respuesta;
    }

    public static Respuesta consultarLegalBymaCotizacionesOnline(ContextoHB contexto) {
        Respuesta respuesta = new Respuesta();

        Date hoy = new Date();
        SqlRequest sqlRequest = Sql.request("SelectLegalBymaCotizacionesOnlineTexto", "homebanking");

        sqlRequest.sql = "SELECT t.id, t.data, u.id_cobis FROM [homebanking].[dbo].[legal_byma_cotizaciones_online_texto] t ";
        sqlRequest.sql += "LEFT JOIN  [homebanking].[dbo].[legal_byma_cotizaciones_online_usuarios] u ON t.id = u.id_legal AND u.id_cobis = ? ";
        sqlRequest.sql += "WHERE ";
        sqlRequest.sql += "? BETWEEN vigencia_desde AND vigencia_hasta";

        sqlRequest.parametros.add(contexto.idCobis());
        sqlRequest.parametros.add(new SimpleDateFormat("yyyyMMdd").format(hoy));

        SqlResponse sqlResponse = Sql.response(sqlRequest);
        for (Objeto item : sqlResponse.registros) {
            respuesta.set("legalBymaCotizacionesOnline", item.string("data"));
            respuesta.set("mostrarLegalBymaCotizacionesOnline", false);
            if ("".equals(item.string("id_cobis"))) {
                respuesta.set("mostrarLegalBymaCotizacionesOnline", true);
            }
        }
        return respuesta;
    }

    public static Respuesta aceptarLegalBymaCotizacionesOnline(ContextoHB contexto) {
        if (contexto.idCobis() == null) {
            return Respuesta.estado("SIN_SESION");
        }

        Respuesta respuesta = new Respuesta();
        try {
            Date hoy = new Date();
            SqlRequest sqlRequestConsulta = Sql.request("SelectLegalBymaCotizacionesOnlineTexto", "homebanking");

            sqlRequestConsulta.sql = "SELECT t.id, t.data, u.id_cobis FROM [homebanking].[dbo].[legal_byma_cotizaciones_online_texto] t ";
            sqlRequestConsulta.sql += "LEFT JOIN  [homebanking].[dbo].[legal_byma_cotizaciones_online_usuarios] u ON t.id = u.id_legal AND u.id_cobis = ? ";
            sqlRequestConsulta.sql += "WHERE ";
            sqlRequestConsulta.sql += "? BETWEEN vigencia_desde AND vigencia_hasta";

            sqlRequestConsulta.parametros.add(contexto.idCobis());
            sqlRequestConsulta.parametros.add(new SimpleDateFormat("yyyyMMdd").format(hoy));
            Integer idTexto = null;
            SqlResponse sqlResponse = Sql.response(sqlRequestConsulta);
            for (Objeto item : sqlResponse.registros) {
                if (!"".equals(item.string("id_cobis"))) {
                    respuesta.setEstado("ERROR_YA_LO_ACEPTO");
                    return Respuesta.estado("ERROR_YA_LO_ACEPTO");
                }
                idTexto = item.integer("id");
            }

            SqlRequest sqlRequest = Sql.request("InsertBymaCotizacionesOnlineTexto", "homebanking");
            sqlRequest.sql = "INSERT INTO [Homebanking].[dbo].[legal_byma_cotizaciones_online_usuarios] (id_legal, id_cobis, fecha) VALUES (?, ?, GETDATE())";
            sqlRequest.add(idTexto);
            sqlRequest.add(contexto.idCobis());

            Sql.response(sqlRequest);
        } catch (Exception e) {
            return Respuesta.estado("ERROR");
        }

        return respuesta;
    }

    public static Respuesta configuracionUsuario(ContextoHB contexto, String idCobis) {
        Objeto configuracion = new Objeto();
        Boolean habilitadoPagoTarjeta = true;
        configuracion.set("prendidoPagoTarjetas", habilitadoPagoTarjeta);
        Boolean habilitadoPaquetes = true;
        Boolean habilitadoLicitaciones = true;
        Boolean habilitadoBlanqueoPin = true;
        Boolean habilitadoPlataNueva = true;
        Boolean habilitadoTodopagoVisahome = true;
        Boolean habilitadoMailSms = true;
        Boolean habilitadoPrestamos = true;
        Boolean habilitadoPaquetesOriginacion = true;
        Boolean habilitadoPrestamosOriginacion = true;
        Boolean habilitadoCambioPasswordLogueado = true;
        Boolean habilitadoCambioUsuarioLogueado = true;
        Boolean habilitadoAltaCajaAhorro = true;
        Boolean habilitadoConsultaComprobantes = true;
        Boolean habilitadoBajaProductos = true;
        Boolean habilitadoDebin = true;
        Boolean habilitadoBuhoPuntos = true;
        Boolean habilitadoResumenCuenta = true;
        Boolean habilitadoPagoPrestamos = true;
        Boolean prendidoDetalleMovCuentas = true;
        Boolean prendidoDetalleMovCuentasNueva = true;
        Boolean prendidoDetalleMovTarjetas = true;
        Boolean prendidoExportaComprobanteMovimientosCuentas = true;
        Boolean prendidoExportaComprobantePagosServicios = true;
        Boolean prendidoExportaComprobantePagosTarjetas = true;

        Boolean prendidoMapaDetalleTc = true;
        Boolean prendidoMapaDetalleCuenta = true;
        Boolean habilitadoFiltrosMovimientosTC = true;
        Boolean habilitadoDetalleMovTarjetasComercio = true;

        Boolean habilitadoFiltrosFechasCuentas = false;
        Boolean habilitadoConsolidadoMovimientos = true;
        Boolean prendidoEncuestaCuentas = true;
        Boolean prendidoEncuestaTarjeta = true;
        Boolean prendidoAccesosDirectoInicio = true;
        Boolean prendidoProximosVencimientos = true;

        Boolean prendidoDetalleMovCuentaComercio = true;
        Boolean habilitadoCategorizacionTC = "true".equals(ConfigHB.string("prendido_categorizacion_tc"));
        Boolean prendidoComprobantePagoPPenDA = true;
        Boolean prendidoHistoricoMovimientos = true;
        Boolean habilitadoConsolidadoSaldos = true;
        Boolean prendidoAccesosDirectos = true;
        Boolean prendidoInicioTC = "true".equals(ConfigHB.string("prendido_inicio_tc"));
        Boolean prendidoTDVirtual = "true".equals(ConfigHB.string("prendidoTDVirtual"));

        Boolean prendidoPrestamoCambioFormaPago = true;

        Integer concurrenciaPagos = ConfigHB.integer("concurrencia_pagos");

        Boolean prendidoBotonUltimaActividad = true;

        Boolean prendidoPruebaErrorSinMov = true;
        Boolean prendidoPruebaCambioClave = true;

        boolean extractoV2Global = "true".equals(ConfigHB.string("extracto_comitente_V2"));
        boolean clienteExcluido  = HBAplicacion.esClienteExcluido(idCobis, "extracto_comitente_V2");
        boolean usarV2           = extractoV2Global && !clienteExcluido;

        Boolean prendidoCampanaNotificaciones = true;

        Boolean prendidoTagueoServiciosVisa = "true".equals(ConfigHB.string("prendidoTagueoServiciosVisa"));

        Boolean prendidoResumenesAnteriores = "true".equals(ConfigHB.string("prendidoResumenesAnteriores"));

        Boolean prendidoActivacionTokenSMS = "true".equals(ConfigHB.string("prendidoActivacionTokenSMS"));
        Boolean prendidoBotonTusClaves = "true".equals(ConfigHB.string("prendidoBotonTusClaves"));

        Boolean prendidoBuhoBot = "true".equals(ConfigHB.string("prendidoBuhoBot"));
        Boolean prendidoMensajeErrorTdInhabilitada = "true".equals(ConfigHB.string("prendidoMensajeErrorTdInhabilitada"));
        Boolean prendidoPantallaAyudaLink = "true".equals(ConfigHB.string("prendidoPantallaAyudaLink"));
        Boolean prendidoIdentificaCbuCVu = "true".equals(ConfigHB.string("prendidoIdentificaCbuCVu"));
        Boolean prendidoRedisenoTc = "true".equals(ConfigHB.string("prendidoRedisenoTc"));
        Boolean prendidoDistribucionTc = "true".equals(ConfigHB.string("prendidoDistribucionTc"));
        Boolean prendidoMsgOlvideUsuarioClave = "true".equals(ConfigHB.string("prendidoMsgOlvideUsuarioClave"));
        Boolean prendidoAdelantobh = "true".equals(ConfigHB.string("prendido_adelanto_bh"));
        Boolean prendidoAumentoLimite = "true".equals(ConfigHB.string("prendidoAumentoLimite"));
        Boolean prendidoAumentoLimiteDocumentacion = "true".equals(ConfigHB.string("prendidoAumentoLimiteDocumentacion"));
        Boolean prendidoBuhoBotRelease = "true".equals(ConfigHB.string("prendidoBuhoBotRelease"));
        Boolean prendidoEstadosTarjeta = "true".equals(ConfigHB.string("prendidoEstadosTarjeta"));
        Boolean prendidoEmptyStateTC = "true".equals(ConfigHB.string("prendidoEmptyStateTC"));
        Boolean prendidoEstadosDeuda = "true".equals(ConfigHB.string("prendidoEstadosDeuda"));
//		Boolean prendidoReposicionTD = "true".equals(Config.string("prendidoReposicionTD"));
        Boolean prendidoHorarioDolarMep = "true".equals(ConfigHB.string("prendidoHorarioDolarMep"));
        Boolean prendidoCanalAmarilloPP = "true".equals(ConfigHB.string("prendido_canal_amarillo_pp"));
        Boolean prendidoContingenciaSoftToken = "true".equals(ConfigHB.string("prendido_contingencia_soft_token"));
        Boolean prendidoPausadoTd = "true".equals(ConfigHB.string("prendido_pausado_td"));
        Boolean prendidoPlanV = "true".equals(ConfigHB.string("prendido_plan_v"));
        Boolean prendidoLiberacionHipoteca = "true".equals(ConfigHB.string("prendido_liberacion_hipoteca"));
        // Boolean prendidoFondosFueraDeHorario =
        // "true".equals(Config.string("prendido_fondos_fuera_de_horario"));

        configuracion.set("prendidoPaquetes", habilitadoPaquetes);
        if (ConfigHB.string("prendido_paquetes_cobis") != null && !habilitadoPaquetes) {
            Set<String> cobisHabilitadoPaquetes = Objeto.setOf(ConfigHB.string("prendido_paquetes_cobis").split("_"));
            configuracion.set("prendidoPaquetes", habilitadoPaquetes || cobisHabilitadoPaquetes.contains(contexto.idCobis()) ? true : false);
        }
        configuracion.set("prendidoLicitaciones", habilitadoLicitaciones);
        if (ConfigHB.string("prendido_licitaciones_cobis") != null && !habilitadoLicitaciones) {
            Set<String> cobisHabilitadoLicitaciones = Objeto.setOf(ConfigHB.string("prendido_licitaciones_cobis").split("_"));
            configuracion.set("prendidoLicitaciones", habilitadoLicitaciones || cobisHabilitadoLicitaciones.contains(contexto.idCobis()) ? true : false);
        }
        configuracion.set("prendidoBlanqueoPin", habilitadoBlanqueoPin);
        if (ConfigHB.string("prendido_blanqueo_pin_cobis") != null && !habilitadoBlanqueoPin) {
            Set<String> cobisHabilitadoBlanqueoPin = Objeto.setOf(ConfigHB.string("prendido_blanqueo_pin_cobis").split("_"));
            configuracion.set("prendidoBlanqueoPin", habilitadoBlanqueoPin || cobisHabilitadoBlanqueoPin.contains(contexto.idCobis()) ? true : false);
        }
        configuracion.set("prendidoPlataNueva", habilitadoPlataNueva);
        if (ConfigHB.string("prendido_plata_nueva_cobis") != null && !habilitadoPlataNueva) {
            Set<String> cobisHabilitadoPlataNueva = Objeto.setOf(ConfigHB.string("prendido_plata_nueva_cobis").split("_"));
            configuracion.set("prendidoPlataNueva", habilitadoPlataNueva || cobisHabilitadoPlataNueva.contains(contexto.idCobis()) ? true : false);
        }
        configuracion.set("habilitadoTodopagoVisahome", habilitadoTodopagoVisahome);
        if (ConfigHB.string("prendido_todopago_visahome_cobis") != null && !habilitadoTodopagoVisahome) {
            Set<String> cobisHabilitadoTodopagoVisahome = Objeto.setOf(ConfigHB.string("prendido_plata_nueva_cobis").split("_"));
            configuracion.set("prendidoTodopagoVisahome", habilitadoTodopagoVisahome || cobisHabilitadoTodopagoVisahome.contains(contexto.idCobis()) ? true : false);
        }
        configuracion.set("prendidoHabilitacionMailSms", habilitadoMailSms);
        if (ConfigHB.string("prendido_habilitacion_mail_sms_cobis") != null && !habilitadoMailSms) {
            Set<String> cobisHabilitadoMailSms = Objeto.setOf(ConfigHB.string("prendido_habilitacion_mail_sms_cobis").split("_"));
            configuracion.set("prendidoHabilitacionMailSms", habilitadoMailSms || cobisHabilitadoMailSms.contains(contexto.idCobis()) ? true : false);
        }

        Integer valorOtpSegundoFactor = RestPersona.sugerirOtpSegundoFactor(idCobis);
        configuracion.set("sugerirOtpSegundoFactor", valorOtpSegundoFactor == null || valorOtpSegundoFactor.equals(0));

        configuracion.set("otpSegundoFactorHabilitadoPorUsuario", valorOtpSegundoFactor != null && valorOtpSegundoFactor.equals(1));

        configuracion.set("prendidoPrestamos", habilitadoPrestamos);
        if (ConfigHB.string("prendido_prestamos_cobis") != null && !habilitadoPrestamos) {
            Set<String> cobisHabilitadoPrestamos = Objeto.setOf(ConfigHB.string("prendido_prestamos_cobis").split("_"));
            configuracion.set("prendidoPrestamos", habilitadoPrestamos || cobisHabilitadoPrestamos.contains(contexto.idCobis()) ? true : false);
        }

        configuracion.set("prendidoPrestamosOriginacion", habilitadoPrestamosOriginacion);
        if (ConfigHB.string("prendido_prestamos_originacion_cobis") != null && !habilitadoPrestamosOriginacion) {
            Set<String> cobisHabilitadoPrestamosOriginacion = Objeto.setOf(ConfigHB.string("prendido_prestamos_originacion_cobis").split("_"));
            configuracion.set("prendidoPrestamosOriginacion", habilitadoPrestamosOriginacion || cobisHabilitadoPrestamosOriginacion.contains(contexto.idCobis()) ? true : false);
        }

        configuracion.set("prendidoPaquetesOriginacion", habilitadoPaquetesOriginacion);
        if (ConfigHB.string("prendido_paquetes_originacion_cobis") != null && !habilitadoPaquetesOriginacion) {
            Set<String> cobisHabilitadoPaquetesOriginacion = Objeto.setOf(ConfigHB.string("prendido_paquetes_originacion_cobis").split("_"));
            configuracion.set("prendidoPaquetesOriginacion", habilitadoPaquetesOriginacion || cobisHabilitadoPaquetesOriginacion.contains(contexto.idCobis()) ? true : false);
        }

        configuracion.set("prendidoCambioPasswordLogueado", habilitadoCambioPasswordLogueado);
        if (ConfigHB.string("prendido_cambio_password_logueado_cobis") != null && !habilitadoCambioPasswordLogueado) {
            Set<String> cobisHabilitadoCambioPasswordLogueado = Objeto.setOf(ConfigHB.string("prendido_cambio_password_logueado_cobis").split("_"));
            configuracion.set("prendidoCambioPasswordLogueado", habilitadoCambioPasswordLogueado || cobisHabilitadoCambioPasswordLogueado.contains(contexto.idCobis()) ? true : false);
        }
        configuracion.set("prendidoCambioUsuarioLogueado", habilitadoCambioUsuarioLogueado);
        if (ConfigHB.string("prendido_cambio_usuario_logueado_cobis") != null && !habilitadoCambioUsuarioLogueado) {
            Set<String> cobisHabilitadoCambioUsuarioLogueado = Objeto.setOf(ConfigHB.string("prendido_cambio_usuario_logueado_cobis").split("_"));
            configuracion.set("prendidoCambioUsuarioLogueado", habilitadoCambioUsuarioLogueado || cobisHabilitadoCambioUsuarioLogueado.contains(contexto.idCobis()) ? true : false);
        }

        configuracion.set("prendidoAltaCA", habilitadoAltaCajaAhorro);
        if (ConfigHB.string("prendido_alta_caja_ahorro_cobis") != null && !habilitadoAltaCajaAhorro) {
            Set<String> cobisHabilitadoAltaCA = Objeto.setOf(ConfigHB.string("prendido_alta_caja_ahorro_cobis").split("_"));
            configuracion.set("prendidoAltaCA", habilitadoAltaCajaAhorro || cobisHabilitadoAltaCA.contains(contexto.idCobis()) ? true : false);
        }

        configuracion.set("prendidoConsultaComprobantes", habilitadoConsultaComprobantes);
        if (ConfigHB.string("prendido_consulta_comprobantes_cobis") != null && !habilitadoConsultaComprobantes) {
            Set<String> cobisHabilitadoConsultaComprobantes = Objeto.setOf(ConfigHB.string("prendido_consulta_comprobantes_cobis").split("_"));
            configuracion.set("prendidoConsultaComprobantes", habilitadoConsultaComprobantes || cobisHabilitadoConsultaComprobantes.contains(contexto.idCobis()) ? true : false);
        }

        configuracion.set("prendidoBajaProductos", habilitadoBajaProductos);
        if (ConfigHB.string("prendido_baja_productos_cobis") != null && !habilitadoBajaProductos) {
            Set<String> cobisHabilitadoBajaProductos = Objeto.setOf(ConfigHB.string("prendido_baja_productos_cobis").split("_"));
            configuracion.set("prendidoBajaProductos", habilitadoBajaProductos || cobisHabilitadoBajaProductos.contains(contexto.idCobis()) ? true : false);
        }

        configuracion.set("prendidoDebin", habilitadoDebin);
        if (ConfigHB.string("prendido_debin_cobis") != null && !habilitadoDebin) {
            Set<String> cobisHabilitadoDebin = Objeto.setOf(ConfigHB.string("prendido_debin_cobis").split("_"));
            configuracion.set("prendidoDebin", habilitadoDebin || cobisHabilitadoDebin.contains(contexto.idCobis()) ? true : false);
        }

        configuracion.set("prendidoBuhoPuntos", habilitadoBuhoPuntos);
        if (ConfigHB.string("prendido_buho_puntos_cobis") != null && !habilitadoBuhoPuntos) {
            Set<String> cobisHabilitadoBuhoPuntos = Objeto.setOf(ConfigHB.string("prendido_buho_puntos_cobis").split("_"));
            configuracion.set("prendidoBuhoPuntos", habilitadoBuhoPuntos || cobisHabilitadoBuhoPuntos.contains(contexto.idCobis()) ? true : false);
        }

        configuracion.set("prendidoResumenCuenta", habilitadoResumenCuenta);
        if (ConfigHB.string("prendido_resumen_cuenta_cobis") != null && !habilitadoResumenCuenta) {
            Set<String> cobisHabilitadoResumenCuenta = Objeto.setOf(ConfigHB.string("prendido_resumen_cuenta_cobis").split("_"));
            configuracion.set("prendidoResumenCuenta", habilitadoResumenCuenta || cobisHabilitadoResumenCuenta.contains(contexto.idCobis()) ? true : false);
        }

        configuracion.set("prendidoPagoPrestamos", habilitadoPagoPrestamos);
        if (ConfigHB.string("prendido_pago_prestamos_cobis") != null && !habilitadoPagoPrestamos) {
            Set<String> cobisHabilitadoPagoPrestamos = Objeto.setOf(ConfigHB.string("prendido_pago_prestamos_cobis").split("_"));
            configuracion.set("prendidoPagoPrestamos", habilitadoPagoPrestamos || cobisHabilitadoPagoPrestamos.contains(contexto.idCobis()) ? true : false);
        }

        configuracion.set("prendidoFiltrosMovimientosTC", habilitadoFiltrosMovimientosTC);
        if (ConfigHB.string("prendido_filtros_movimientos_TC_cobis") != null && !habilitadoFiltrosMovimientosTC) {
            Set<String> cobisHabilitadoFiltrosMovimientosTC = Objeto.setOf(ConfigHB.string("prendido_filtros_movimientos_TC_cobis").split("_"));
            configuracion.set("prendidoFiltrosMovimientosTC", habilitadoFiltrosMovimientosTC || cobisHabilitadoFiltrosMovimientosTC.contains(contexto.idCobis()) ? true : false);
        }

        configuracion.set("prendidoFiltrosFechasCuentas", habilitadoFiltrosFechasCuentas);
        if (ConfigHB.string("prendido_filtros_fechas_cuentas_cobis") != null && !habilitadoFiltrosFechasCuentas) {
            Set<String> cobisHabilitadoFiltrosFechasCuentas = Objeto.setOf(ConfigHB.string("prendido_filtros_fechas_cuentas_cobis").split("_"));
            configuracion.set("prendidoFiltrosFechasCuentas", habilitadoFiltrosFechasCuentas || cobisHabilitadoFiltrosFechasCuentas.contains(contexto.idCobis()) ? true : false);
        }

        configuracion.set("prendidoDetalleMovTarjetasComercio", habilitadoDetalleMovTarjetasComercio);
        if (ConfigHB.string("prendido_detalle_mov_tarjetas_comercio_cobis") != null && !habilitadoDetalleMovTarjetasComercio) {
            Set<String> cobisHabilitadoDetalleMovTarjetasComercio = Objeto.setOf(ConfigHB.string("prendido_detalle_mov_tarjetas_comercio_cobis").split("_"));
            configuracion.set("prendidoDetalleMovTarjetasComercio", habilitadoDetalleMovTarjetasComercio || cobisHabilitadoDetalleMovTarjetasComercio.contains(contexto.idCobis()) ? true : false);
        }

        configuracion.set("prendidoConsolidadoMovimientos", habilitadoConsolidadoMovimientos);
        if (ConfigHB.string("prendido_consolidado_movimientos_cobis") != null && !habilitadoConsolidadoMovimientos) {
            Set<String> cobisHabilitadoConsolidadoMovimientos = Objeto.setOf(ConfigHB.string("prendido_consolidado_movimientos_cobis").split("_"));
            configuracion.set("prendidoConsolidadoMovimientos", habilitadoConsolidadoMovimientos || cobisHabilitadoConsolidadoMovimientos.contains(contexto.idCobis()) ? true : false);
        }

        configuracion.set("prendidoConsolidadoSaldos", habilitadoConsolidadoSaldos);
        if (ConfigHB.string("prendido_consolidado_saldos_cobis") != null && !habilitadoConsolidadoSaldos) {
            Set<String> cobisHabilitadoConsolidadoSaldos = Objeto.setOf(ConfigHB.string("prendido_consolidado_saldos_cobis").split("_"));
            configuracion.set("prendidoConsolidadoSaldos", habilitadoConsolidadoSaldos || cobisHabilitadoConsolidadoSaldos.contains(contexto.idCobis()) ? true : false);
        }

        configuracion.set("prendidoCategorizacionTC", habilitadoCategorizacionTC);
        if (ConfigHB.string("prendido_categorizacion_tc_cobis") != null && !habilitadoCategorizacionTC) {
            Set<String> cobisHabilitadoCategorizacionTC = Objeto.setOf(ConfigHB.string("prendido_categorizacion_tc_cobis").split("_"));
            configuracion.set("prendidoCategorizacionTC", habilitadoCategorizacionTC || cobisHabilitadoCategorizacionTC.contains(contexto.idCobis()) ? true : false);
        }

        configuracion.set("prendidoDetalleMovCuentaComercio", prendidoDetalleMovCuentaComercio);
        if (ConfigHB.string("prendido_detalle_mov_cuentas_comercio_cobis") != null && !prendidoDetalleMovCuentaComercio) {
            Set<String> cobisPrendidoDetalleMovCuentaComercio = Objeto.setOf(ConfigHB.string("prendido_detalle_mov_cuentas_comercio_cobis").split("_"));
            configuracion.set("prendidoDetalleMovCuentaComercio", prendidoDetalleMovCuentaComercio || cobisPrendidoDetalleMovCuentaComercio.contains(contexto.idCobis()) ? true : false);
        }

        configuracion.set("prendidoEncuestaCuentas", prendidoEncuestaCuentas);
        if (ConfigHB.string("prendido_encuesta_cuentas_cobis") != null && !prendidoEncuestaCuentas) {
            Set<String> prendidoEncuestaCuentasCobis = Objeto.setOf(ConfigHB.string("prendido_encuesta_cuentas_cobis").split("_"));
            configuracion.set("prendidoEncuestaCuentas", prendidoEncuestaCuentas || prendidoEncuestaCuentasCobis.contains(contexto.idCobis()) ? true : false);
        }

        configuracion.set("prendidoEncuestaTarjeta", prendidoEncuestaTarjeta);
        if (ConfigHB.string("prendido_encuesta_tarjeta_cobis") != null && !prendidoEncuestaTarjeta) {
            Set<String> prendidoEncuestaTarjetaCobis = Objeto.setOf(ConfigHB.string("prendido_encuesta_tarjeta_cobis").split("_"));
            configuracion.set("prendidoEncuestaTarjeta", prendidoEncuestaTarjeta || prendidoEncuestaTarjetaCobis.contains(contexto.idCobis()) ? true : false);
        }

        configuracion.set("prendidoAccesosDirectoInicio", prendidoAccesosDirectoInicio);
        if (ConfigHB.string("prendido_accesos_directo_inicio_cobis") != null && !prendidoAccesosDirectoInicio) {
            Set<String> prendidoAccesosDirectoInicioCobis = Objeto.setOf(ConfigHB.string("prendido_accesos_directo_inicio_cobis").split("_"));
            configuracion.set("prendidoAccesosDirectoInicio", prendidoAccesosDirectoInicio || prendidoAccesosDirectoInicioCobis.contains(contexto.idCobis()) ? true : false);
        }

        configuracion.set("prendidoInicioTC", prendidoInicioTC);
        if (ConfigHB.string("prendido_inicio_tc_cobis") != null && !prendidoInicioTC) {
            Set<String> cobisPrendidoInicioTC = Objeto.setOf(ConfigHB.string("prendido_inicio_tc_cobis").split("_"));
            configuracion.set("prendidoInicioTC", prendidoInicioTC || cobisPrendidoInicioTC.contains(contexto.idCobis()) ? true : false);
        }

        configuracion.set("prendidoProximosVencimientos", prendidoProximosVencimientos);
        if (ConfigHB.string("prendido_proximos_vencimientos_cobis") != null && !prendidoProximosVencimientos) {
            Set<String> prendidoProximosVencimientosCobis = Objeto.setOf(ConfigHB.string("prendido_proximos_vencimientos_cobis").split("_"));
            configuracion.set("prendidoProximosVencimientos", prendidoProximosVencimientos || prendidoProximosVencimientosCobis.contains(contexto.idCobis()) ? true : false);
        }

        configuracion.set("prendidoDetalleMovCuentasNueva", prendidoDetalleMovCuentasNueva);
        if (ConfigHB.string("prendido_detalle_mov_cuentas_nueva_cobis") != null && !prendidoDetalleMovCuentasNueva) {
            Set<String> prendidoDetalleMovCuentasNuevaCobis = Objeto.setOf(ConfigHB.string("prendido_detalle_mov_cuentas_nueva_cobis").split("_"));
            configuracion.set("prendidoDetalleMovCuentasNueva", prendidoDetalleMovCuentasNueva || prendidoDetalleMovCuentasNuevaCobis.contains(contexto.idCobis()) ? true : false);
        }

        configuracion.set("prendidoPrestamoCambioFormaPago", prendidoPrestamoCambioFormaPago);
        if (ConfigHB.string("prendido_prestamo_cambio_forma_pago_cobis") != null && !prendidoPrestamoCambioFormaPago) {
            Set<String> prendidoPrestamoCambioFormaPagoCobis = Objeto.setOf(ConfigHB.string("prendido_prestamo_cambio_forma_pago_cobis").split("_"));
            configuracion.set("prendidoPrestamoCambioFormaPago", prendidoPrestamoCambioFormaPago || prendidoPrestamoCambioFormaPagoCobis.contains(contexto.idCobis()) ? true : false);
        }

        configuracion.set("prendidoTagueoServiciosVisa", prendidoTagueoServiciosVisa);
        if (ConfigHB.string("prendidoTagueoServiciosVisa_cobis") != null && !prendidoTagueoServiciosVisa) {
            Set<String> cobisPrendidoTagueoServiciosVisa = Objeto.setOf(ConfigHB.string("prendidoTagueoServiciosVisa_cobis").split("_"));
            configuracion.set("prendidoTagueoServiciosVisa", prendidoTagueoServiciosVisa || cobisPrendidoTagueoServiciosVisa.contains(contexto.idCobis()) ? true : false);
        }
        configuracion.set("prendidoResumenesAnteriores", prendidoResumenesAnteriores);
        if (ConfigHB.string("prendidoResumenesAnteriores_cobis") != null && !prendidoResumenesAnteriores) {
            Set<String> cobisPrendidoResumenesAnteriores = Objeto.setOf(ConfigHB.string("prendidoResumenesAnteriores_cobis").split("_"));
            configuracion.set("prendidoResumenesAnteriores", prendidoResumenesAnteriores || cobisPrendidoResumenesAnteriores.contains(contexto.idCobis()) ? true : false);
        }
        configuracion.set("prendidoAumentoLimite", prendidoAumentoLimite);
        if (ConfigHB.string("prendidoAumentoLimite_cobis") != null && !prendidoAumentoLimite) {
            Set<String> cobisPrendidoAumentoLimite = Objeto.setOf(ConfigHB.string("prendidoAumentoLimite_cobis").split("_"));
            configuracion.set("prendidoAumentoLimite", prendidoAumentoLimite || cobisPrendidoAumentoLimite.contains(contexto.idCobis()) ? true : false);
        }
        configuracion.set("prendidoAumentoLimiteDocumentacion", prendidoAumentoLimiteDocumentacion);
        if (ConfigHB.string("prendidoAumentoLimiteDocumentacion_cobis") != null && !prendidoAumentoLimiteDocumentacion) {
            Set<String> cobisPrendidoAumentoLimiteDocumentacion = Objeto.setOf(ConfigHB.string("prendidoAumentoLimiteDocumentacion_cobis").split("_"));
            configuracion.set("prendidoAumentoLimiteDocumentacion", prendidoAumentoLimiteDocumentacion || cobisPrendidoAumentoLimiteDocumentacion.contains(contexto.idCobis()) ? true : false);
        }
        configuracion.set("prendidoEstadosTarjeta", prendidoEstadosTarjeta);
        configuracion.set("prendidoEstadosDeuda", prendidoEstadosDeuda);
        if (ConfigHB.string("prendidoEstadosTarjeta_cobis") != null && !prendidoEstadosTarjeta) {
            Set<String> cobisPrendidoEstadosTarjeta = Objeto.setOf(ConfigHB.string("prendidoEstadosTarjeta_cobis").split("_"));
            configuracion.set("prendidoEstadosTarjeta", prendidoEstadosTarjeta || cobisPrendidoEstadosTarjeta.contains(contexto.idCobis()) ? true : false);
        }

        Boolean prendidoScriptMaze = "true".equals(ConfigHB.string("prendidoScriptMaze"));
        configuracion.set("prendidoScriptMaze", prendidoScriptMaze);
        if (ConfigHB.string("prendidoScriptMaze_cobis") != null && !prendidoScriptMaze) {
            Set<String> cobisMaze = Objeto.setOf(ConfigHB.string("prendidoScriptMaze_cobis").split("_"));
            configuracion.set("prendidoScriptMaze", prendidoScriptMaze || cobisMaze.contains(contexto.idCobis()) ? true : false);
        }
        Boolean prendidoMockRescate = "true".equals(ConfigHB.string("prendido_mock_rescate"));
        configuracion.set("prendidoMockRescate", prendidoMockRescate);
        if (ConfigHB.string("prendido_mock_rescate_cobis") != null && !prendidoMockRescate) {
            Set<String> cobis = Objeto.setOf(ConfigHB.string("prendido_mock_rescate_cobis").split("_"));
            configuracion.set("prendidoMockRescate", prendidoMockRescate || cobis.contains(contexto.idCobis()) ? true : false);
        }

        configuracion.set("forzarOTP", ConfigHB.bool("forzar_otp", false));

        configuracion.set("prendidoEmptyStateTC", prendidoEmptyStateTC);

        configuracion.set("prendidoReposicionTD", funcionalidadPrendida(contexto.idCobis(), "prendidoReposicionTD"));

        configuracion.set("prendidoGestiones", funcionalidadPrendida(contexto.idCobis(), "prendidoGestiones"));

        configuracion.set("prendidoAumentoLimiteEmpleados", funcionalidadPrendida(contexto.idCobis(), "prendidoAumentoLimiteEmpleados"));

        configuracion.set("prendidoRedisenoCuotasTc", funcionalidadPrendida(contexto.idCobis(), "prendido_rediseno_cuotas_tc", "prendido_rediseno_cuotas_tc_cobis"));

        configuracion.set("prendidoAutorizacionConsolidada", false);

        configuracion.set("prendidoBajaProductosPaquetes", funcionalidadPrendida(contexto.idCobis(), "prendido_baja_productos_paquetes", "prendido_baja_productos_paquetes_cobis"));

        configuracion.set("prendidoBajaProductosTarjetaCredito", true);

        configuracion.set("prendidoCambioFormaPagoTC", true);

        configuracion.set("prendidoHBviejo", false);

        configuracion.set("prendidoTCAdicional", funcionalidadPrendida(contexto.idCobis(), "prendido_tc_adicional"));

        configuracion.set("prendidoAlertaNotificaciones", true);

        configuracion.set("prendidoHistorialActividades", funcionalidadPrendida(contexto.idCobis(), "prendido_historial_actividades", "prendido_historial_actividades_cobis"));

        configuracion.set("prendidoCuentaPrincipal", funcionalidadPrendida(contexto.idCobis(), "prendido_cuenta_principal", "prendido_cuenta_principal_cobis"));

        configuracion.set("prendidoCuentaPrincipalExterior", funcionalidadPrendida(contexto.idCobis(), "prendido_cuenta_principal_exterior", "prendido_cuenta_principal_exterior_cobis"));

        configuracion.set("prendidoTCAdicionalModificacionLimite", true);

        configuracion.set("prendidoRentaFinanciera", true);

        configuracion.set("prendidoPrestamoNSP", true);

        configuracion.set("prendidoCheques", funcionalidadPrendida(contexto.idCobis(), "prendido_cheques", "prendido_cheques_cobis"));

        configuracion.set("prendidoLibreDeuda", funcionalidadPrendida(contexto.idCobis(), "prendido_libre_deuda"));

        configuracion.set("prendidoSoftToken", funcionalidadPrendida(contexto.idCobis(), "prendido_soft_token", "prendido_soft_token_cobis"));

        configuracion.set("prendidoActivacionTokenSMS", prendidoActivacionTokenSMS);
        configuracion.set("prendidoBotonTusClaves", prendidoBotonTusClaves);
        configuracion.set("prendidoBuhoBot", prendidoBuhoBot);
        configuracion.set("prendidoMensajeErrorTdInhabilitada", prendidoMensajeErrorTdInhabilitada);
        configuracion.set("prendidoPantallaAyudaLink", prendidoPantallaAyudaLink);
        configuracion.set("prendidoIdentificaCbuCVu", prendidoIdentificaCbuCVu);
        configuracion.set("prendidoRedisenoTc", prendidoRedisenoTc);
        if (ConfigHB.string("prendidoRedisenoTc") != null && !prendidoRedisenoTc) {
            Set<String> cobisPrendidoRedisenoTC = Objeto.setOf(ConfigHB.string("prendidoRedisenoTc_cobis").split("_"));
            configuracion.set("prendidoRedisenoTc", prendidoRedisenoTc || cobisPrendidoRedisenoTC.contains(contexto.idCobis()) ? true : false);
        }
        configuracion.set("prendidoDistribucionTc", prendidoDistribucionTc);
        configuracion.set("prendidoAdelantobh", prendidoAdelantobh);
        configuracion.set("prendidoBuhoBotRelease", prendidoBuhoBotRelease);

        // if (Config.esProduccion()) {
        // configuracion.set("prendidoPrestamoTasaCero", true);
        // } else {
        // configuracion.set("prendidoPrestamoTasaCero", true);
        // }

        // Apagado tasa cero por VE
        configuracion.set("prendidoPrestamoTasaCero", funcionalidadPrendida(contexto.idCobis(), "prendidoPrestamoTasaCero"));
        configuracion.set("prendidoPrestamoTasaCeroCultura", funcionalidadPrendida(contexto.idCobis(), "prendidoPrestamoTasaCeroCultura"));

        // if (Config.esProduccion()) {
        // configuracion.set("prendidoPrestamoTasaCeroCultura", false);
        // } else {
        // configuracion.set("prendidoPrestamoTasaCeroCultura", true);
        // }

        configuracion.set("prendidoArgentinaConstruye", true);

        configuracion.set("prendidoProcrearRefaccion", true);

        configuracion.set("prendidoPrestamoComplementario", true);

        configuracion.set("prendidoOrdenesExtraccion", true);

        configuracion.set("prendidoExportaMovimientos", true);

        configuracion.set("prendidoExportaMovimientosPdf", true);

        configuracion.set("prendidoExportaMovimientosTC", true);

        configuracion.set("prendidoExportaMovimientosTCPdf", true);

        configuracion.set("prendidoDetalleMovTarjetasComercio", true);

        configuracion.set("prendidoValidaUrlFront", funcionalidadPrendida(contexto.idCobis(), "prendido_valida_url_front", "prendido_valida_url_front_cobis"));

        configuracion.set("prendidoDebitoAutomatico", true);

        configuracion.set("prendidoNormativo7072", true);

        configuracion.set("prendidoLey7105_7106", funcionalidadPrendida("prendido_ley_7105_7106"));

        configuracion.set("prendidoCompraVenta7105_7106", false);

        configuracion.set("prendidoSubidaDocAlFinalProcrear", true);

        configuracion.set("prendidoTransferenciasHaberes", true);

        configuracion.set("prendidoLicitacionesByma", true);

        configuracion.set("prendidoCotizacionesByma", true);

        configuracion.set("prendidoMaterialesSeleccionPlazo", true);

        configuracion.set("prendidoRefaccionSeleccionPlazo", true);

        configuracion.set("prendidoArregloTcCotizacionDolar", true);

        configuracion.set("prendidoAdhesionGire", true);

        configuracion.set("prendidoNormativoBaja", true);

        configuracion.set("prendidoDecreto767", true);

        configuracion.set("prendidoCompraVentaTenencias", false);

        configuracion.set("prendidoFciConsolidada", true);

        configuracion.set("prendidoOperarCotizaciones", false);

        configuracion.set("prendidoNormativoPrestamoPersonal", true);

        configuracion.set("prendidoDolarMep", true);

        configuracion.set("prendidoHorarioDolarMep", prendidoHorarioDolarMep);

        configuracion.set("prendidoGestionPlasticos", funcionalidadPrendida(contexto.idCobis(), "prendido_gestion_plasticos"));

        configuracion.set("prendidoGestionTarjetasSinLlegar", false);

        configuracion.set("prendidoConsultaUltimaActividad", false);

        configuracion.set("normativoDebinA7326", true);

        configuracion.set("prendidoDocumentacion", true);

        configuracion.set("prendidoSeguro", true);

        configuracion.set("prendidoSeguroVida", true);

        configuracion.set("prendidoSeguroSalud", true);

        configuracion.set("prendidoSeguroAP", funcionalidadPrendida(contexto.idCobis(), "prendido_seguro_ap"));

        configuracion.set("prendidoSeguroBM", funcionalidadPrendida(contexto.idCobis(), "prendido_seguro_bm"));

        configuracion.set("prendidoSeguroHogar", funcionalidadPrendida(contexto.idCobis(), "prendido_seguro_hogar"));

        configuracion.set("prendidoSeguroATM", funcionalidadPrendida(contexto.idCobis(), "prendido_seguro_atm"));

        configuracion.set("prendidoSeguroCompraProtegida", funcionalidadPrendida(contexto.idCobis(), "prendido_seguro_compra_protegida"));
        configuracion.set("prendidoSeguroAPMayores", funcionalidadPrendida(contexto.idCobis(), "prendido_seguro_ap_mayores"));
        configuracion.set("prendidoSeguroAuto", funcionalidadPrendida(contexto.idCobis(), "prendido_seguro_auto"));
        configuracion.set("prendidoSeguroComboMascotas", funcionalidadPrendida(contexto.idCobis(), "prendido_seguro_combo"));
        configuracion.set("prendidoSeguroComboBM", funcionalidadPrendida(contexto.idCobis(), "prendido_seguro_combo_bm"));

        configuracion.set("prendidoSeguroMovilidad", "true".equals(ConfigHB.string("prendido_seguro_movilidad")));
        configuracion.set("prendidoSeguroMascotas", "true".equals(ConfigHB.string("prendido_seguro_mascotas")));
        configuracion.set("prendidoSeguroCaucion", "true".equals(ConfigHB.string("prendido_seguro_caucion")));
        configuracion.set("prendidoSeguroSaludSenior", "true".equals(ConfigHB.string("prendido_seguro_salud_senior")));

        configuracion.set("prendidoCuotificacion", true);

        configuracion.set("prendidoDuracionOtp", true);

        configuracion.set("prendidoRecuperoClave", true);

        configuracion.set("telefonoCentroAtencion", ConfigHB.string("telefono_centro_atencion", "0810-222-7777"));

        configuracion.set("prendidoManejo302", true);

        configuracion.set("concurrencia_pagos", concurrenciaPagos);
        configuracion.set("leyendaImpuestoCompraVentaDolares", ConfigHB.string("leyenda_impuesto_compra_venta_dolares"));
        configuracion.set("leyendaImpuestoCompraVentaDolaresTarjeta", ConfigHB.string("leyenda_impuesto_compra_venta_dolares_tarjeta"));
        configuracion.set("leyendaImpuestoCompraVentaDolaresTarjetaApagado", ConfigHB.string("leyenda_impuesto_compra_venta_dolares_tarjeta_apagado"));
        configuracion.set("leyendaGarantiaPlazoFijo", ConfigHB.string("leyenda_garantia_plazo_fijo"));
        configuracion.set("leyendaRenovacionPlazoFijo", ConfigHB.string("leyenda_renovacion_plazo_fijo"));

        configuracion.set("leyendaVentaBonosDolares", ConfigHB.string("leyenda_venta_bonos_dolares"));
        configuracion.set("leyendaCompraBonosDolares", ConfigHB.string("leyenda_compra_bonos_dolares"));
        configuracion.set("leyendaVentaBonosPesos", ConfigHB.string("leyenda_venta_bonos_pesos"));
        configuracion.set("leyendaCompraBonosPesos", ConfigHB.string("leyenda_compra_bonos_pesos"));
        configuracion.set("leyendaImpuestoResumen35", ConfigHB.string("leyenda_impuesto_resumen_35"));

        BigDecimal impuestoCompraVentaDolar = ConfigHB.bigDecimal("porcentaje_impuesto_compra_venta_dolares", new BigDecimal(0));
        configuracion.set("impuestoCompraVentaDolar", impuestoCompraVentaDolar);
        configuracion.set("impuestoCompraVentaDolarFormateado", Formateador.importe(impuestoCompraVentaDolar));

        configuracion.set("prendidoDetalleMovCuentas", prendidoDetalleMovCuentas);
        configuracion.set("prendidoDetalleMovTarjetas", prendidoDetalleMovTarjetas);
        configuracion.set("prendidoExportaComprobanteMovimientosCuentas", prendidoExportaComprobanteMovimientosCuentas);
        configuracion.set("prendidoExportaComprobantePagosServicios", prendidoExportaComprobantePagosServicios);
        configuracion.set("prendidoExportaComprobantePagosTarjetas", prendidoExportaComprobantePagosTarjetas);

        configuracion.set("prendidoMapaDetalleTc", prendidoMapaDetalleTc);

        configuracion.set("leyendaNormativo7072TransferenciaLink", ConfigHB.string("leyenda_normativo_7072_transferencia_link"));
        configuracion.set("leyendaNormativo7072TransferenciaIntraBH", ConfigHB.string("leyenda_normativo_7072_transferencia_intra_bh"));
        configuracion.set("mensajeTooltipFechaMov", ConfigHB.string("mensaje_tooltip_fecha_mov"));

        configuracion.set("mensajeFueraHorarioBatchNoche", ConfigHB.string("mensaje_fuera_horario_batch_noche", "Esta es la fecha en la que ingres&oacute; en este resumen, puede no coincidir con el d&iacute;a en que hiciste el movimiento."));

        configuracion.set("prendidoMapaDetalleCuenta", prendidoMapaDetalleCuenta);

        configuracion.set("prendidoComprobantePagoPPenDA", prendidoComprobantePagoPPenDA);
        configuracion.set("prendidoHistoricoMovimientos", prendidoHistoricoMovimientos);

        configuracion.set("cantidadCuotasSeguroCaucion", ConfigHB.integer("cantidad_cuotas_seguro_caucion"));
        configuracion.set("porcentajeDescuentoSeguroCaucion", ConfigHB.integer("porcetaje_descuento_seguro_caucion"));

        configuracion.set("montoMinimoOrdenExtraccion", ConfigHB.integer("monto_minimo_orden_extraccion", 500));
        configuracion.set("montoMaximoOrdenExtraccion", ConfigHB.integer("monto_maximo_orden_extraccion", 10000));
        configuracion.set("stepOrdenExtraccion", ConfigHB.integer("step_orden_extraccion", 100));
        configuracion.set("prendidoAccesosDirectos", prendidoAccesosDirectos);
        configuracion.set("prendidoBloqueo48", true);
        configuracion.set("prendidoCuentaEnRiesgo", true);
        configuracion.set("prendidoSfRescateFondos", funcionalidadPrendida(contexto.idCobis(), "prendido_sf_rescate_fondos"));
        configuracion.set("prendidoClaveExpirada", true);

        configuracion.set("prendidoClaveBuhoFacil", true);
        configuracion.set("prendidoPruebaCambioClave", prendidoPruebaCambioClave);
        configuracion.set("prendidoPruebaErrorSinMov", prendidoPruebaErrorSinMov);
        if (ConfigHB.string("prendido_prueba_error_sin_mov_cobis") != null && !prendidoPruebaErrorSinMov) {
            Set<String> cobisPruebaErrorSinMov = Objeto.setOf(ConfigHB.string("prendido_prueba_error_sin_mov_cobis").split("_"));
            configuracion.set("prendidoPruebaErrorSinMov", prendidoPruebaErrorSinMov || cobisPruebaErrorSinMov.contains(contexto.idCobis()) ? true : false);
        }

        try {
            Boolean prendidoPickUp = true;
            configuracion.set("prendidoPickUp", prendidoPickUp);
        } catch (Exception e) {
            configuracion.set("prendidoPickUp", false);
        }

        configuracion.set("arrepentimientoUrl", ConfigHB.string("arrepentimiento_url", "https://www.hipotecario.com.ar/arrepentimiento/"));

        configuracion.set("montoMinimoPlanSueldoSinPaquete", Formateador.importe(ConfigHB.bigDecimal("monto_minimo_plan_sueldo_sin_paquete")));
        configuracion.set("metaBonificacionPlanSueldoFacilPackFormateado", Formateador.importe(ConfigHB.bigDecimal("meta_bonificacion_plan_sueldo_facil_pack")));
        configuracion.set("metaBonificacionPlanSueldoBuhoPackFormateado", Formateador.importe(ConfigHB.bigDecimal("meta_bonificacion_plan_sueldo_buho_pack")));
        configuracion.set("metaBonificacionPlanSueldoGoldPackFormateado", Formateador.importe(ConfigHB.bigDecimal("meta_bonificacion_plan_sueldo_gold_pack")));
        configuracion.set("metaBonificacionPlanSueldoPlatinumPackFormateado", Formateador.importe(ConfigHB.bigDecimal("meta_bonificacion_plan_sueldo_platinum_pack")));
        configuracion.set("metaBonificacionPlanSueldoBlackPackFormateado", Formateador.importe(ConfigHB.bigDecimal("meta_bonificacion_plan_sueldo_black_pack")));

        configuracion.set("prendidoTDVirtual", prendidoTDVirtual);

        configuracion.set("keyGoogleMaps", ConfigHB.string("key_google_maps"));

        configuracion.set("prendidoBotonUltimaActividad", prendidoBotonUltimaActividad);

        configuracion.set("prendidoCampanaNotificaciones", prendidoCampanaNotificaciones);
        configuracion.set("prendidoMsgOlvideUsuarioClave", prendidoMsgOlvideUsuarioClave);

        configuracion.set("prendidoSoftTokenConsultarAltaEnGuard", funcionalidadPrendida(contexto.idCobis(), "prendido_soft_token_consultar_alta_en_guard"));
        configuracion.set("prendidoSoftTokenCambioUsuarioLogeado", funcionalidadPrendida(contexto.idCobis(), "prendido_soft_token_cambio_usuario_logeado"));
        configuracion.set("prendidoSoftTokenCambioClaveLogeado", funcionalidadPrendida(contexto.idCobis(), "prendido_soft_token_cambio_clave_logeado"));
        configuracion.set("prendidoSoftTokenDatosPersonales", funcionalidadPrendida(contexto.idCobis(), "prendido_soft_token_datos_personales"));
        configuracion.set("prendidoSoftTokenDebin", funcionalidadPrendida(contexto.idCobis(), "prendido_soft_token_debin"));
        configuracion.set("prendidoSoftTokenOrdenExtraccion", funcionalidadPrendida(contexto.idCobis(), "prendido_soft_token_orden_extraccion"));
        configuracion.set("prendidoSoftTokenPrestamoPersonal", funcionalidadPrendida(contexto.idCobis(), "prendido_soft_token_prestamo_personal"));
        configuracion.set("prendidoSoftTokenOriginacionDatosPersonales", funcionalidadPrendida(contexto.idCobis(), "prendido_soft_token_originacion_datos_personales"));
        configuracion.set("prendidoSoftTokenAltaPagoImpuestoServicio", funcionalidadPrendida(contexto.idCobis(), "prendido_soft_token_alta_pago_impuesto_servicio"));
        configuracion.set("prendidoSoftTokenAumentoLimiteTransferencia", funcionalidadPrendida(contexto.idCobis(), "prendido_soft_token_aumento_limite_transferencia"));
        configuracion.set("prendidoSoftTokenRescateFondos", funcionalidadPrendida(contexto.idCobis(), "prendido_soft_token_rescate_fondos"));
        configuracion.set("prendidoStopDebit", funcionalidadPrendida(contexto.idCobis(), "prendido_stop_debit"));
        configuracion.set("prendidoPrestamosMora", funcionalidadPrendida(contexto.idCobis(), "prendido_prestamos_mora"));

        configuracion.set("prendidoCanalAmarilloPP", prendidoCanalAmarilloPP);
        configuracion.set("prendidoContingenciaSoftToken", prendidoContingenciaSoftToken);
        configuracion.set("prendidoCambioVencimiento", funcionalidadPrendida(contexto.idCobis(), "prendidoCambioVencimiento"));
        configuracion.set("prendidoPromesaPago", funcionalidadPrendida(contexto.idCobis(), "prendido_promesa_pago"));

        // TODO GB DEPRECAR: TODO03092417 se reemplazan por las 2 siguientes xx_escalar...
        configuracion.set("invComisionMercadoVenta", ConfigHB.bigDecimal("inv_comision_mercado_venta").abs());
        configuracion.set("invComisionMercadoCompra", ConfigHB.bigDecimal("inv_comision_mercado_compra").abs());
        // Mandamos el escalar positivo a FE.
        configuracion.set("escMercAccionesCompra", ConfigHB.bigDecimal("escalar_precio_mercado_compra_acciones_bonos").abs());
        configuracion.set("escMercAccionesVenta", ConfigHB.bigDecimal("escalar_precio_mercado_venta_acciones_bonos").abs());

        configuracion.set("invComisionGeneralBonos", ConfigHB.bigDecimal("inv_comision_general_bonos"));
        configuracion.set("invComisionGeneralAccionesCedears", ConfigHB.bigDecimal("inv_comision_general_acciones_cedears"));
        configuracion.set("prendidoPlanV", prendidoPlanV);

        Boolean prendidoFondosFueraDeHorario = "true".equals(ConfigHB.string("prendido_fondos_fuera_de_horario"));
        configuracion.set("prendidoFondosFueraDeHorario", funcionalidadPrendida(contexto.idCobis(), "prendido_fondos_fuera_de_horario"));
        if (ConfigHB.string("prendido_fondos_fuera_de_horario_cobis") != null && !prendidoFondosFueraDeHorario) {
            Set<String> cobisFondosFueraDeHorario = Objeto.setOf(ConfigHB.string("prendido_fondos_fuera_de_horario_cobis").split("_"));
            configuracion.set("prendidoFondosFueraDeHorario", prendidoFondosFueraDeHorario || cobisFondosFueraDeHorario.contains(contexto.idCobis()) ? true : false);
        }

        configuracion.set("prendidoHaceRendirTuDinero", HBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_hace_rendir_tu_dinero"));

        configuracion.set("prendidoAltaAdicional", funcionalidadPrendida(contexto.idCobis(), "prendido_alta_adicional"));
        configuracion.set("prendidoVencimientoBuhoFacil", funcionalidadPrendida(contexto.idCobis(), "verificacion_vencimiento_clave"));
        configuracion.set("prendidoAutogestionDeuda", funcionalidadPrendida(contexto.idCobis(), "hb_prendido_bloque_mora"));
        configuracion.set("prendidoNuevaGestion", funcionalidadPrendida(contexto.idCobis(), "hb_prendido_nueva_gestion"));
        configuracion.set("prendidoReintegroPromocion", funcionalidadPrendida(contexto.idCobis(), "hb_prendido_reintegro_promocion"));
        configuracion.set("prendidoDesconocimientoConsumo", funcionalidadPrendida(contexto.idCobis(), "hb_prendido_desconocimiento_consumo"));
        configuracion.set("prendidoProfundidadMercado", funcionalidadPrendida(contexto.idCobis(), "prendido_profundidad_mercado", "prendido_profundidad_mercado_cobis"));
        configuracion.set("prendidoBajaProductosTarjetaCreditoV2", funcionalidadPrendida(contexto.idCobis(), "hb_prendidoBajaProductosTarjetaCreditoV2"));
        configuracion.set("prendidoPausadoTd", prendidoPausadoTd);
        configuracion.set("prendidoCedip", funcionalidadPrendida(contexto.idCobis(), "hb_prendido_cedip"));

        configuracion.set("prendidoPausadoTc", funcionalidadPrendida(contexto.idCobis(), "prendido_pausado_tc"));
        configuracion.set("prendidoBajaTCConOfertaCRM", funcionalidadPrendida(contexto.idCobis(), "prendido_baja_tc_con_oferta_crm"));

        configuracion.set("prendidoPlazoFijoLogroConsolidada", "true".equals(ConfigHB.string("prendido_plazo_fijo_logro_consolidada")));

        configuracion.set("amexMontoMaximo", ConfigHB.bigDecimal("amex_maximo_monto_pago"));

        configuracion.set("reCaptchaKeyPublic", ConfigHB.string("recaptcha_key_public", ""));
        configuracion.set("prendidoReCaptcha", funcionalidadPrendida(contexto.idCobis(), "prendido_recaptcha"));
        configuracion.set("reCaptchaActionLogin", ConfigHB.string("recaptcha_action_login", ""));

        configuracion.set("prendidoCaTdAltaOnline", funcionalidadPrendida("prendido_alta_ca_td_online"));
        configuracion.set("prendidoDebinRecurrente", funcionalidadPrendida(contexto.idCobis(), "prendido_debin_recurrente"));
        configuracion.set("prendidoDataValidOtp", funcionalidadPrendida(contexto.idCobis(), "prendido_data_valid_otp"));
        configuracion.set("prendidoAgendaTransferencia", funcionalidadPrendida(contexto.idCobis(), "prendido_agenda_transferencia"));
        configuracion.set("prendidoPaginadoMovimientosCuenta", funcionalidadPrendida(contexto.idCobis(), "prendido_paginado_movimientos_cuenta"));


        configuracion.set("prendidoOnboarding", !Util.tieneMuestreoNemonico(contexto, "ONBOARDING_FCI"));
        configuracion.set("prendidoModalTcv", funcionalidadPrendida(contexto.idCobis(), "prendido_modal_tcv"));
        configuracion.set("prendidoLiberacionHipoteca", prendidoLiberacionHipoteca);
        configuracion.set("prendidoOfertaCajaAhorroDolar", funcionalidadPrendida(contexto.idCobis(), "prendido_oferta_caja_ahorro_dolar"));
        configuracion.set("extractoComitenteVersion", usarV2 ? "v2" : "v1");


        configuracion.set("prendidoCertificadoCuenta", funcionalidadPrendida(contexto.idCobis(), "prendido_certificado_cuenta"));
        configuracion.set("prendidoAlertaSos", funcionalidadPrendida(contexto.idCobis(), "prendido_alerta_sos"));
        configuracion.set("FFvariacionRendimiento", funcionalidadPrendida(contexto.idCobis(), "FFvariacionRendimiento", "FFvariacionRendimiento_cobis"));
        configuracion.set("prendidoGestionesv3", funcionalidadPrendida(contexto.idCobis(), "prendido_gestiones_v3"));


        return Respuesta.exito("configuracion", configuracion);
    }

    public static boolean funcionalidadPrendida(String idCobis, String flag, String flagCobis) {
        Boolean habilitado = "true".equals(ConfigHB.string(flag));
        if (ConfigHB.string(flagCobis) != null && !habilitado) {
            Set<String> cobisHabilitado = Objeto.setOf(ConfigHB.string(flagCobis).split("_"));
            habilitado = cobisHabilitado.contains(idCobis) ? true : false;
        }
        return habilitado;
    }

    public static boolean funcionalidadPrendida(String idCobis, String flag) {

        String flagCobis = flag + "_cobis";
        Boolean habilitado = "true".equals(ConfigHB.string(flag));
        if (ConfigHB.string(flagCobis) != null && !habilitado) {
            Set<String> cobisHabilitado = Objeto.setOf(ConfigHB.string(flagCobis).split("_"));
            habilitado = cobisHabilitado.contains(idCobis) ? true : false;
        }
        return habilitado;

    }

    public static boolean esClienteExcluido(String idCobis, String flag) {

        boolean habilitado = "true".equals(ConfigHB.string(flag));
        String flagExclusion = flag + "_cobis";
        String excluir = ConfigHB.string(flagExclusion);

        if (habilitado && excluir != null) {
            Set<String> cobisExcluidos = Objeto.setOf(excluir.split("_"));
            return cobisExcluidos.contains(idCobis);
        }
        return false;
    }




    public static Respuesta log(ContextoHB contexto) {
        UnauthorizedException.ifNot(contexto.requestHeader("Authorization"));
        String idCobis = contexto.parametros.string("idCobis");

        if (Objeto.anyEmpty(idCobis)) {
            return Respuesta.parametrosIncorrectos();
        }

        Respuesta respuesta = new Respuesta();
        if (true) {
            SqlRequest request = Sql.request("SelectAuditorCompraVentaDolares", "hbs");
            request.sql = "SELECT * FROM [hbs].[dbo].[auditor_compra_venta_dolares] WHERE cobis = ?";
            request.add(idCobis);
            SqlResponse response = Sql.response(request);
            for (Objeto item : response.registros) {
                item.set("momento", item.string("momento"));
            }
            respuesta.set("compraVentaDolares", response.registros);
        }
        if (true) {
            SqlRequest request = Sql.request("SelectAuditorPagoServicio", "hbs");
            request.sql = "SELECT * FROM [hbs].[dbo].[auditor_pago_servicio] WHERE cobis = ?";
            request.add(idCobis);
            SqlResponse response = Sql.response(request);
            for (Objeto item : response.registros) {
                item.set("momento", item.string("momento"));
            }
            respuesta.set("pagoServicio", response.registros);
        }
        if (true) {
            SqlRequest request = Sql.request("SelectAuditorPagoTarjeta", "hbs");
            request.sql = "SELECT * FROM [hbs].[dbo].[auditor_pago_tarjeta] WHERE cobis = ?";
            request.add(idCobis);
            SqlResponse response = Sql.response(request);
            for (Objeto item : response.registros) {
                item.set("momento", item.string("momento"));
            }
            respuesta.set("pagoTarjeta", response.registros);
        }
        if (true) {
            SqlRequest request = Sql.request("SelectAuditorPagoVep", "hbs");
            request.sql = "SELECT * FROM [hbs].[dbo].[auditor_pago_vep] WHERE cobis = ?";
            request.add(idCobis);
            SqlResponse response = Sql.response(request);
            for (Objeto item : response.registros) {
                item.set("momento", item.string("momento"));
            }
            respuesta.set("pagosVep", response.registros);
        }
        if (true) {
            SqlRequest request = Sql.request("SelectAuditorTransferencia", "hbs");
            request.sql = "SELECT * FROM [hbs].[dbo].[auditor_transferencia] WHERE cobis = ?";
            request.add(idCobis);
            SqlResponse response = Sql.response(request);
            for (Objeto item : response.registros) {
                item.set("momento", item.string("momento"));
            }
            respuesta.set("transferencias", response.registros);
        }
        if (true) {
            SqlRequest request = Sql.request("SelectAuditorTitulosValores", "hbs");
            request.sql = "SELECT * FROM [hbs].[dbo].[auditor_titulos_valores] WHERE cobis = ?";
            request.add(idCobis);
            SqlResponse response = Sql.response(request);
            for (Objeto item : response.registros) {
                item.set("momento", item.string("momento"));
            }
            respuesta.set("titulosValores", response.registros);
        }
        contexto.responseHeader("Content-Type", "application/json");
        return respuesta;
    }

    public static Respuesta logPrestamosAma(ContextoHB contexto) {
        UnauthorizedException.ifNot(contexto.requestHeader("Authorization"));
        Respuesta respuesta = new Respuesta();
        if (true) {
            SqlRequest request = Sql.request("PrestamosAma", "homebanking");
            request.sql = "SELECT * FROM [homebanking].[dbo].[solicitudPrestamoTasaCero_2021]";
//			request.sql += " union all SELECT * FROM [homebanking].[dbo].[solicitudPrestamoTasaCero_2021]";
            SqlResponse response = Sql.response(request);
            if (response.hayError) {
                return Respuesta.error();
            }
            for (Objeto item : response.registros) {
                item.set("momento", item.string("momento"));
            }
            respuesta.set("solicitudes", response.registros);
        }
        contexto.responseHeader("Content-Type", "application/json");
        return respuesta;
    }

    public static Respuesta logErrores(ContextoHB contexto) {
        UnauthorizedException.ifNot(contexto.requestHeader("Authorization"));
        Respuesta respuesta = new Respuesta();
        if (true) {
            SqlRequest request = Sql.request("SelectLogErrores", "homebanking");
            request.sql = "SELECT * FROM [homebanking].[dbo].[log_errores]";
            SqlResponse response = Sql.response(request);
            if (response.hayError) {
                return Respuesta.error();
            }
            for (Objeto item : response.registros) {
                item.set("momento", item.string("momento"));
            }
            respuesta.set("datos", response.registros);
        }
        contexto.responseHeader("Content-Type", "application/json");
        return respuesta;
    }

    public static String logErroresTxt(ContextoHB contexto) {
        UnauthorizedException.ifNot(contexto.requestHeader("Authorization"));
        StringBuilder sb = new StringBuilder();
        if (true) {
            SqlRequest request = Sql.request("SelectLogErrores", "homebanking");
            request.sql = "SELECT * FROM [homebanking].[dbo].[log_errores]";
            SqlResponse response = Sql.response(request);
            if (response.hayError) {
                return "ERROR";
            }
            for (Objeto item : response.registros) {
                sb.append(item.string("servicio")).append(",");
                sb.append(item.string("http")).append(",");
                sb.append(item.string("codigo")).append(",");
                sb.append(item.string("mensajeAlUsuario")).append("");
                sb.append("\r\n");
            }
        }
        contexto.responseHeader("Content-Type", "text/plain");
        return sb.toString();
    }

    public static String logApiVentas(ContextoHB contexto) {
        UnauthorizedException.ifNot(contexto.requestHeader("Authorization"));
        String buscar = contexto.parametros.string("buscar");
        if (!Objeto.anyEmpty(buscar)) {
            StringBuilder sb = new StringBuilder();
            if (true) {
                SqlRequest request = Sql.request("SelectLogErrores", "homebanking");
                request.sql = "SELECT * FROM  [Homebanking].[dbo].[log_api_ventas] WHERE idCobis = ? OR numeroDocumento = ? OR numeroSolicitud = ? ORDER BY momento DESC";
                request.add(buscar);
                request.add(buscar);
                request.add(buscar);
                SqlResponse response = Sql.response(request);
                if (response.hayError) {
                    return "ERROR";
                }
                sb.append("<div style='font-family: calibri; font-size: 15px'>");
                for (Objeto item : response.registros) {
                    sb.append("<b>Momento: </b>").append(item.date("momento", "yyyy-MM-dd HH:mm:ss", "dd/MM/yyyy HH:mm:ss")).append("<br/>");
                    sb.append("<b>Cobis:   </b>").append(item.string("idCobis")).append("<br/>");
                    sb.append("<b>Documento: </b>").append(item.string("numeroDocumento")).append("<br/>");
                    sb.append("<b>Solicitud: </b>").append(item.string("numeroSolicitud")).append("<br/>");
                    sb.append("<b>Canal: </b>").append(item.string("canal")).append("<br/>");
                    sb.append("<b>Servicio:   </b>").append(item.string("servicio")).append("<br/>");
                    if (!item.string("resolucionMotor").isEmpty()) {
                        sb.append("<b>Resolucion Motor: </b>").append(item.string("resolucionMotor")).append("<br/>");
                        sb.append("<b>Explicacion Motor: </b>").append(item.string("explicacionMotor")).append("<br/>");
                    }
                    if (!item.string("mensajeCliente").isEmpty() && !item.string("servicio").equals("generarSolicitud")) {
                        sb.append("<b>Explicacion: </b>").append(item.string("mensajeCliente")).append("<br/>");
                        sb.append("<b>Detalle: </b>").append(item.string("mensajeDesarrollador")).append("<br/>");
                    }
                    sb.append("<br/>");
                }
                sb.append("</div>");
            }
            contexto.responseHeader("Content-Type", "text/html");
            return sb.toString();
        }
        return "";
    }

    public static String logApiVentasFecha(ContextoHB contexto) {
        UnauthorizedException.ifNot(contexto.requestHeader("Authorization"));

        Date fecha = contexto.parametros.date("fecha", "d/M/yyyy");
        if (fecha == null) {
            try {
                String formato = "yyyy-MM-dd";
                String fechaFormateada = new SimpleDateFormat(formato).format(new Date());
                fecha = new SimpleDateFormat(formato).parse(fechaFormateada);
            } catch (ParseException e) {
            }
        }

        if (!Objeto.anyEmpty(fecha)) {
            StringBuilder sb = new StringBuilder();
            if (true) {
                SqlRequest request = Sql.request("SelectLogErrores", "homebanking");
                request.sql = "SELECT * FROM  [Homebanking].[dbo].[log_api_ventas] WHERE momento BETWEEN ? AND ? ORDER BY momento DESC";
                request.add(new java.sql.Date(fecha.getTime()));
                request.add(new java.sql.Date(fecha.getTime() + 24 * 60 * 60 * 1000L));
                SqlResponse response = Sql.response(request);
                if (response.hayError) {
                    return "ERROR";
                }
                sb.append("<div style='font-family: calibri; font-size: 15px'>");
                for (Objeto item : response.registros) {
                    sb.append("<b>Momento: </b>").append(item.date("momento", "yyyy-MM-dd HH:mm:ss", "dd/MM/yyyy HH:mm:ss")).append("<br/>");
                    sb.append("<b>Cobis:   </b>").append(item.string("idCobis")).append("<br/>");
                    sb.append("<b>Documento: </b>").append(item.string("numeroDocumento")).append("<br/>");
                    sb.append("<b>Solicitud: </b>").append(item.string("numeroSolicitud")).append("<br/>");
                    sb.append("<b>Canal: </b>").append(item.string("canal")).append("<br/>");
                    sb.append("<b>Servicio:   </b>").append(item.string("servicio")).append("<br/>");
                    if (!item.string("resolucionMotor").isEmpty()) {
                        sb.append("<b>Resolucion Motor: </b>").append(item.string("resolucionMotor")).append("<br/>");
                        sb.append("<b>Explicacion Motor: </b>").append(item.string("explicacionMotor")).append("<br/>");
                    }
                    if (!item.string("mensajeCliente").isEmpty() && !item.string("servicio").equals("generarSolicitud")) {
                        sb.append("<b>Explicacion: </b>").append(item.string("mensajeCliente")).append("<br/>");
                        sb.append("<b>Detalle: </b>").append(item.string("mensajeDesarrollador")).append("<br/>");
                    }
                    sb.append("<br/>");
                }
                sb.append("</div>");
            }
            contexto.responseHeader("Content-Type", "text/html");
            return sb.toString();
        }
        return "";
    }

    public static String logCambiosDatos(ContextoHB contexto) {

        UnauthorizedException.ifNot(contexto.requestHeader("Authorization"));
        String idCobis = contexto.parametros.string("idCobis");
        StringBuilder stringBuilder = new StringBuilder();
        Objeto registrosMostrar = new Objeto();
        stringBuilder.append("<div style='font-family: calibri; font-size: 15px'>");

        if (!idCobis.isEmpty()) {
            ApiResponse usuarioByCobis = RestPersona.consultarClienteLogsCambios(contexto, idCobis);

            String cuit = usuarioByCobis.objetos().isEmpty() ? null : usuarioByCobis.objetos().get(0).string("cuit");

            Objeto dataCambioMail = new Objeto();
            Objeto dataCambioTel = new Objeto();
            Boolean cambioMail = true;
            Boolean cambioTel = true;

            if (cuit != null) {

                Objeto responseCambioMail = RestPersona.email(contexto, cuit);

                if (responseCambioMail != null) {
                    String momentoModificacion = responseCambioMail.string("fechaModificacion").replace("T", " ");
                    String fecha = Fecha.formato(momentoModificacion, "yyyy-MM-dd hh:mm:ss", "dd/MM/yyyy");
                    dataCambioMail.set("tipo", "CAMBIO_MAIL_SERVICIO");
                    dataCambioMail.set("fecha", fecha);
                    dataCambioMail.set("hora", momentoModificacion.substring(11));
                    dataCambioMail.set("canal", responseCambioMail.string("canalModificacion"));
                    cambioTel = false;
                }

                Objeto responseCambioTel = RestPersona.celular(contexto, cuit);

                if (responseCambioTel != null) {
                    String momentoModificacion = responseCambioTel.string("fechaModificacion").replace("T", " ");
                    String fecha = Fecha.formato(momentoModificacion, "yyyy-MM-dd hh:mm:ss", "dd/MM/yyyy");
                    dataCambioTel.set("fecha", fecha);
                    dataCambioTel.set("tipo", "CAMBIO_TELEFONO_SERVICIO");
                    dataCambioTel.set("hora", momentoModificacion.substring(11));
                    dataCambioTel.set("canal", responseCambioTel.string("canalModificacion"));
                    cambioMail = false;
                }

            }

            String fechaAnterior = "";
            Integer cantidadDiasNormativoPrestamo = ConfigHB.integer("cantidad_dias_normativo_prestamo", 10) * (-1);
            SqlRequest sqlRequest = Sql.request("ConsultaTipoContador", "homebanking");

            sqlRequest.sql = "SELECT * FROM [Homebanking].[dbo].[contador] WITH (NOLOCK) " + "WHERE idCobis = ? AND momento > DATEADD(day, " + cantidadDiasNormativoPrestamo.toString() + ", GETDATE()) " + "AND tipo in ('CAMBIO_CLAVE', 'CAMBIO_USUARIO', 'CAMBIO_TELEFONO', 'CAMBIO_MAIL')";

            sqlRequest.add(idCobis);

            List<Objeto> registrosContador = Sql.response(sqlRequest).registros;

            if (registrosContador != null) {

                for (Objeto unRegistro : registrosContador) {
                    String fechaActual = unRegistro.date("momento", "yyyy-MM-dd HH:mm:ss", "dd/MM/yyyy");
                    Objeto mostrar = new Objeto();

                    mostrar.set("tipo", unRegistro.string("tipo"));
                    mostrar.set("canal", "HB");
                    mostrar.set("hora", unRegistro.date("momento", "yyyy-MM-dd HH:mm:ss", "HH:mm:ss"));

                    fechaAnterior = !fechaAnterior.equals(fechaActual) ? fechaActual : fechaAnterior;

                    if (dataCambioTel.string("fecha").equals(fechaAnterior) && !cambioTel) {
                        cambioTel = true;
                        registrosMostrar.add(fechaAnterior, dataCambioTel);
                    }

                    if (dataCambioMail.string("fecha").equals(fechaAnterior) && !cambioMail) {
                        cambioMail = true;
                        registrosMostrar.add(fechaAnterior, dataCambioMail);
                    }
                    registrosMostrar.add(fechaAnterior, mostrar);
                }

                for (String clave : registrosMostrar.claves()) {

                    stringBuilder.append("<h2>").append(clave).append("</h2>").append("<hr/>");

                    for (Objeto registro : registrosMostrar.objetos(clave)) {
                        stringBuilder.append("<br/>");
                        stringBuilder.append("<b> Momento: </b>").append(registro.get("hora")).append("<br/>");
                        stringBuilder.append("<b> Tipo: </b>").append(registro.get("tipo")).append("<br/>");
                        stringBuilder.append("<b> Canal: </b>").append(registro.string("canal")).append("<br/>");
                    }
                }
            }

            if (!cambioMail) {
                stringBuilder.append("<h2>").append(dataCambioMail.get("fecha")).append("</h2>").append("<hr/>");
                stringBuilder.append("<br/>");
                stringBuilder.append("<b> Momento: </b>").append(dataCambioMail.get("hora")).append("<br/>");
                stringBuilder.append("<b> Tipo: </b>").append(dataCambioMail.get("tipo")).append("<br/>");
                stringBuilder.append("<b> Canal: </b>").append(dataCambioMail.string("canal")).append("<br/>");
            }

            if (!cambioTel) {
                stringBuilder.append("<h2>").append(dataCambioTel.get("fecha")).append("</h2>").append("<hr/>");
                stringBuilder.append("<br/>");
                stringBuilder.append("<b> Momento: </b>").append(dataCambioTel.get("hora")).append("<br/>");
                stringBuilder.append("<b> Tipo: </b>").append(dataCambioTel.get("tipo")).append("<br/>");
                stringBuilder.append("<b> Canal: </b>").append(dataCambioTel.string("canal")).append("<br/>");
            }
        } else {
            stringBuilder.append("<h2> Parametros incorrectos </h2>");
        }

        return stringBuilder.toString();
    }

    public static String logDatos(ContextoHB contexto) {
        UnauthorizedException.ifNot(contexto.requestHeader("Authorization"));
        String buscar = contexto.parametros.string("buscar");
        if (Objeto.anyEmpty(buscar)) {
            return "";
        }

        SqlResponse sqlResponseBeneficiarios = getBeneficiarios(buscar);
        SqlResponse response = getRegistroTransacciones(buscar);

        if (response.hayError) {
            return "ERROR";
        }
        System.out.println("Registros totales: " + response.registros.size());
        String fechaAnterior = "";
        StringBuilder sb = new StringBuilder();
        sb.append("<div style='font-family: calibri; font-size: 15px'>");
        for (Objeto item : response.registros) {
            String fechaActual = item.date("momento", "yyyy-MM-dd HH:mm:ss", "dd/MM/yyyy");

            if (!fechaAnterior.equals(fechaActual)) {
                fechaAnterior = fechaActual;
                // esto era la fecha anterior que era igual a la fecha actual
                List<Objeto> registros = response.registros.stream().filter(itemConteo -> itemConteo.date("momento", "yyyy-MM-dd HH:mm:ss", "dd/MM/yyyy").equals(fechaActual)).filter(itemConteo -> "VALIDACION_OTP".equals(itemConteo.string("tipo")) || "PEDIDO_OTP".equals(itemConteo.string("tipo"))).collect(Collectors.toList());

                List<Objeto> riesgoNetFallido = registros.stream().filter(registro -> registro.integer("riesgoNet", 0) == 1 && registro.string("estado").equals("R")).collect(Collectors.toList());

                List<Objeto> riesgoNetCorrecto = registros.stream().filter(registro -> registro.integer("riesgoNet", 0) == 1 && registro.string("estado").equals("A")).collect(Collectors.toList());

                sb.append("<h2> ").append(fechaAnterior).append(" </h2>");
                sb.append("<h3> INTENTOS RIESGO NET: ");
                sb.append("fallidos: ").append(riesgoNetFallido.size()).append(" - ");
                sb.append("aceptados: ").append(riesgoNetCorrecto.size()).append(" - ");
                sb.append("TOTAL: ").append(riesgoNetFallido.size() + riesgoNetCorrecto.size()).append(" </h3> <hr/>");
            }

            sb.append("<b>Momento: </b>").append(item.date("momento", "yyyy-MM-dd HH:mm:ss", "dd/MM/yyyy HH:mm:ss")).append("<br/>");
            if ("VALIDACION_OTP".equals(item.string("tipo")) && item.integer("riesgoNet", 0) == 1) {
                sb.append("<b>Tipo: </b>").append("VALIDACION_RIESGONET").append("<br/>");
            } else {
                if ("PEDIDO_OTP".equals(item.string("tipo")) && item.integer("riesgoNet", 0) == 1) {
                    sb.append("<b>Tipo: </b>").append("PEDIDO_RIESGO_NET").append("<br/>");
                } else {
                    sb.append("<b>Tipo: </b>").append(item.string("tipo")).append("<br/>");
                }
            }
            sb.append("<b>Canal: </b>").append(item.string("canal")).append("<br/>");
            sb.append("<b>DireccionIp: </b>").append(item.string("direccionIp")).append("<br/>");

            if ("PEDIDO_OTP".equals(item.string("tipo")) || "VALIDACION_OTP".equals(item.string("tipo"))) {
                if (!"".equals(item.string("celular"))) {
                    sb.append("<b>Medio: </b>").append("CELULAR").append("<br/>");
                    sb.append("<b>Celular: </b>").append(item.string("celular")).append("<br/>");
                }
                if (!"".equals(item.string("email"))) {
                    sb.append("<b>Medio: </b>").append("EMAIL").append("<br/>");
                    sb.append("<b>Email: </b>").append(item.string("email")).append("<br/>");
                }
                if (item.integer("riesgoNet") != null && item.integer("riesgoNet") == 1) {
                    sb.append("<b>Medio: </b>").append("RIESGO NET").append("<br/>");
                }
                if (item.integer("link") != null && item.integer("link") == 1) {
                    sb.append("<b>Medio: </b>").append("LINK").append("<br/>");
                }
                sb = convertState(sb, item.string("estado"));
            }

            if ("PRESTAMO_PERSONAL".equals(item.string("tipo"))) {
                sb.append("<b>Cuenta: </b>").append(item.string("cuenta")).append("<br/>");
                sb.append("<b>Importe: </b>").append(Formateador.importe(item.bigDecimal("importe"))).append("<br/>");
                sb.append("<b>Plazo: </b>").append(item.string("plazo")).append("<br/>");
            }
            if ("TRANSFERENCIA".equals(item.string("tipo"))) {
                String nombreBeneficiario = "DESCONOCIDO";
                if (!"".equals(item.string("cuentaDestino"))) {
                    for (Objeto registroBeneficiario : sqlResponseBeneficiarios.registros) {
                        if (item.string("cuentaDestino").equals(registroBeneficiario.string("cbu_destino")) || item.string("cuentaDestino").equals(registroBeneficiario.string("nro_cuenta_destino"))) {
                            nombreBeneficiario = registroBeneficiario.string("titular", "DESCONOCIDO");
                        }
                    }
                }
                sb.append("<b>Tipo de transferencia: </b>").append(item.string("tipoTransferencia")).append("<br/>");
                sb.append("<b>Cuenta Origen: </b>").append(item.string("cuentaOrigen")).append("<br/>");
                if (CuentasService.esCbu(item.string("cuentaDestino"))) {
                    sb.append("<b>Cbu Destino: </b>").append(item.string("cuentaDestino")).append("<br/>");
                } else if (CuentasService.esCvu(item.string("cuentaDestino"))) {
                    sb.append("<b>Cvu Destino: </b>").append(item.string("cuentaDestino")).append("<br/>");
                } else {
                    sb.append("<b>Cuenta Destino: </b>").append(item.string("cuentaDestino")).append("<br/>");
                }

                sb.append("<b>Titular: </b>").append(nombreBeneficiario).append("<br/>");
                sb.append("<b>Importe: </b>").append(Formateador.importe(item.bigDecimal("importe"))).append("<br/>");
                sb.append("<b>Moneda: </b>").append("80".equals(item.string("moneda")) ? "$" : ("2".equals(item.string("moneda")) ? "USD" : "")).append("<br/>");
                sb.append("<b>Concepto: </b>").append(item.string("concepto")).append("<br/>");
                sb.append("<b>Servicio Domestico: </b>").append(item.string("servicioDomestico")).append("<br/>");
                sb.append("<b>Especial: </b>").append(item.string("especial")).append("<br/>");
                definirResultado(item, sb);
            }
            if ("DEBIN".equals(item.string("tipo"))) {
                sb.append("<b>Tipo de debin: </b>").append(item.string("tipoTransferencia")).append("<br/>");
                sb.append("<b>Cuenta Origen: </b>").append(item.string("cuentaOrigen")).append("<br/>");
                sb.append("<b>Cuenta Destino: </b>").append(item.string("cuentaDestino")).append("<br/>");
                sb.append("<b>Importe: </b>").append(Formateador.importe(item.bigDecimal("importe"))).append("<br/>");
                sb.append("<b>Moneda: </b>").append("80".equals(item.string("moneda")) ? "$" : ("2".equals(item.string("moneda")) ? "USD" : "")).append("<br/>");
                definirResultado(item, sb);
            }
            if ("CAMBIO_CELULAR".equals(item.string("tipo"))) {
                sb.append("<b>Celular Anterior: </b>").append(item.string("celularAnterior")).append("<br/>");
                sb.append("<b>Celular Nuevo: </b>").append(item.string("celularNuevo")).append("<br/>");
            }
            if ("CAMBIO_MAIL".equals(item.string("tipo"))) {
                sb.append("<b>Mail Anterior: </b>").append(item.string("mailAnterior")).append("<br/>");
                sb.append("<b>Mail Nuevo: </b>").append(item.string("mailNuevo")).append("<br/>");
            }
            if ("BENEFICIARIO".equals(item.string("tipo"))) {
                if (!item.string("cbu_beneficiario").equals("")) {
                    sb.append("<b>Cbu: </b>").append(item.string("cbu_beneficiario")).append("<br/>");
                }
                if (!item.string("cuenta_beneficiario").equals("")) {
                    sb.append("<b>Cuenta: </b>").append(item.string("cuenta_beneficiario")).append("<br/>");
                }
                if (!item.string("documento_beneficiario").equals("")) {
                    sb.append("<b>Documento: </b>").append(item.string("documento_beneficiario")).append("<br/>");
                }
                if (!item.string("nombre_beneficiario").equals("")) {
                    sb.append("<b>Nombre: </b>").append(item.string("nombre_beneficiario")).append("<br/>");
                }
                sb.append("<b>Acci&oacute;n: </b>").append(item.string("accion_beneficiario").equals("A") ? "ALTA" : item.string("accion_beneficiario").equals("B") ? "BAJA" : "").append("<br/>");
            }

            if ("ODE".equals(item.string("tipo"))) {
                sb.append("<b>Importe: </b>").append(Formateador.importe(item.bigDecimal("importe"))).append("<br/>");
                sb.append("<b>Referencia: </b>").append(item.string("nombre_beneficiario")).append("<br/>");

                if (!item.string("documento_beneficiario").equals("")) {
                    sb.append("<b>Documento beneficiario: </b>").append(item.string("documento_beneficiario")).append("<br/>");
                }

                sb.append("<b>Cuenta: </b>").append(item.string("cuenta_beneficiario")).append("<br/>");

                definirResultado(item, sb);
            }

            if ("PreguntasRiesgoNet".equals(item.string("tipo"))) {
                sb.append("<b> Id cobis: </b>").append(item.string("idCobis")).append("<br/>");
                sb.append("<b> Id proceso: </b>").append(item.string("idProceso")).append("<br/>");
            }

            if ("BIOMETRIA".equals(item.string("tipo"))) {
                sb.append("<b>Metodo de Acceso: </b>").append(item.string("metodo")).append("<br/>");
                sb.append("<b>Tipo de Acceso: </b>").append(item.string("tipo_metodo")).append("<br/>");
                sb.append("<b>Acceso: </b>").append(item.string("acceso")).append("<br/>");
                sb.append("<b>Dispositivo: </b>").append(item.string("id_dispositivo")).append("<br/>");
                if (!item.string("refres_token").equals("")) {
                    sb.append("<b>Ultimo token ISVA: </b>").append(item.string("refres_token")).append("<br/>");
                }
                sb.append("<b>Estado: </b>").append(item.string("estado_acceso").equals("0") ? "Ok" : item.string("estado_acceso")).append("<br/>");
            }
            if ("TRANSFERENCIA_MODO".equals(item.string("tipo"))) {
                sb.append("<b>Tipo Transferencia: </b>").append(item.string("tipoTransferencia")).append("<br/>");
                sb.append("<b>Cuenta Origen: </b>").append(item.string("cuentaOrigen")).append("<br/>");
                sb.append("<b>Cbu Destino: </b>").append(item.string("cuentaDestino")).append("<br/>");
                sb.append("<b>Titular: </b>").append(item.string("nombre_beneficiario")).append("<br/>");
                sb.append("<b>Celular: </b>").append(item.string("celular")).append("<br/>");
                sb.append("<b>Importe: </b>").append(Formateador.importe(item.bigDecimal("importe"))).append("<br/>");
                sb.append("<b>Moneda: </b>").append(item.string("moneda")).append("<br/>");
                sb.append("<b>Concepto: </b>").append(item.string("concepto")).append("<br/>");
                sb.append("<b>ID Transaccion: </b>").append(item.string("idProceso")).append("<br/>");
                sb.append("<b>ID Modo Transaccion: </b>").append(item.string("transaccion")).append("<br/>");
            }

            if ("PAGO-QR".equals(item.string("tipo"))) {
                sb.append("<b>Card ID en MODO: </b>").append(item.string("cuentaOrigen")).append("<br/>");
                sb.append("<b>Transaccion ID en MODO: </b>").append(item.string("transaccion")).append("<br/>");
                sb.append("<b>Importe: </b>").append(Formateador.importe(item.bigDecimal("importe"))).append("<br/>");
                sb.append("<b>Moneda: </b>").append(item.string("moneda")).append("<br/>");
                sb.append("<b>Cuotas: </b>").append(item.string("plazo")).append("<br/>");
                sb.append("<b>Comercio: </b>").append(item.string("nombre_beneficiario")).append("<br/>");
            }

            if ("REGISTRO_DISPOSITIVO".equals(item.string("tipo"))) {
                sb.append("<b>Fecha Alta: </b>").append(item.string("momento")).append("<br/>");
                sb.append("<b>Alias y Id Dispositivo: </b>").append(item.string("id_dispositivo")).append("<br/>");
            }

            if ("SOFT_TOKEN_ALTA".equals(item.string("tipo"))) {
                sb.append("<b>Fecha Alta: </b>").append(item.string("momento")).append("<br/>");
                sb.append("<b>Id Dispositivo: </b>").append(item.string("id_dispositivo")).append("<br/>");
                sb.append("<b>Id Tarjeta Débito: </b>").append(item.string("tarjetaDebito")).append("<br/>");
            }

            sb.append("<br/>");
        }
        sb.append("</div>");
        contexto.responseHeader("Content-Type", "text/html");
        return sb.toString();
    }

    private static StringBuilder convertState(StringBuilder sb, String estado) {
        String state = convertStateMessage(estado);
        if (!state.isEmpty()) {
            sb.append("<b>Estado: </b>").append(state).append("<br/>");
        }
        return sb;
    }

    private static String convertStateMessage(String estado) {
        String state = "";
        switch (estado) {
            case "P":
                state = "PEDIDO";
                break;
            case "A":
                state = "ACEPTADO";
                break;
            case "R":
                state = "RECHAZADO";
                break;
            case "E":
                state = "ERROR API";
                break;
            case "T":
                state = "ERROR Api Envio Timeout";
                break;
            default:
        }
        return state;
    }

    private static SqlResponse getBeneficiarios(String idCobis) {
        SqlRequest sqlRequestBeneficiario = Sql.request("SelectAgendaTransferencias", "hbs");
        sqlRequestBeneficiario.sql = "SELECT * FROM [Hbs].[dbo].[agenda_transferencias] WHERE nro_cliente = ? ";
        sqlRequestBeneficiario.parametros.add(idCobis);
        return Sql.response(sqlRequestBeneficiario);
    }

    private static SqlResponse getRegistroTransacciones(String idCobis) {
        SqlRequest request = Sql.request("SelectLogDatos", "homebanking");
        request.sql = obtenerConsultaFraude();
        request.add(idCobis);
        request.add(idCobis);
        request.add(idCobis);
        request.add(idCobis);
        request.add(idCobis);
        request.add(idCobis);
        request.add(idCobis);
        request.add(idCobis);
        request.add(idCobis);
        request.add(idCobis);
        request.add(idCobis);
        request.add(idCobis);
        request.add(idCobis);
        request.add(idCobis);
        request.add(idCobis);
        request.add(idCobis);
        request.add(idCobis);
        request.add(idCobis);
        return Sql.response(request);
    }

    private static String obtenerConsultaFraude() {
        StringBuilder sb = new StringBuilder();
        sb.append("select * ");
        sb.append("from ( ");
        sb.append("select 'LOGIN' tipo, idCobis, canal, momento, direccionIp, null celular, null email, null riesgoNet, null link, null estado, null importe, null plazo, null cuenta, null tipoTransferencia, null cuentaOrigen, null cuentaDestino, null moneda, null concepto, null cuentaPropia, null servicioDomestico, null especial, null tarjetaDebito, null transaccion, null codigoError, null descripcionTransferenciaError, null celularAnterior, null celularNuevo, null mailAnterior, null mailNuevo, null 'cbu_beneficiario', null 'cuenta_beneficiario', null 'documento_beneficiario', null 'nombre_beneficiario', null 'accion_beneficiario', null idProceso, null metodo, null tipo_metodo, null acceso, null id_dispositivo, null refres_token, null estado_acceso ");
        sb.append("from [homebanking].[dbo].[logs_login] ");
        sb.append("where idCobis = ? ");
        sb.append("union all ");
        sb.append("select 'CAMBIO_CLAVE' tipo, idCobis, canal, momento, direccionIp, null celular, null email, null riesgoNet, null link, null estado, null importe, null plazo, null cuenta, null tipoTransferencia, null cuentaOrigen, null cuentaDestino, null moneda, null concepto, null cuentaPropia, null servicioDomestico, null especial, null tarjetaDebito, null transaccion, null codigoError, null descripcionTransferenciaError, null celularAnterior, null celularNuevo, null mailAnterior, null mailNuevo, null 'cbu_beneficiario', null 'cuenta_beneficiario', null 'documento_beneficiario', null 'nombre_beneficiario', null 'accion_beneficiario', null idProceso, null metodo, null tipo_metodo, null acceso, null id_dispositivo, null refres_token, null estado_acceso ");
        sb.append("from [homebanking].[dbo].[logs_cambio_clave] ");
        sb.append("where idCobis = ? ");
        sb.append("union all ");
        sb.append("select 'CAMBIO_USUARIO' tipo, idCobis, canal, momento, direccionIp, null celular, null email, null riesgoNet, null link, null estado, null importe, null plazo, null cuenta, null tipoTransferencia, null cuentaOrigen, null cuentaDestino, null moneda, null concepto, null cuentaPropia, null servicioDomestico, null especial, null tarjetaDebito, null transaccion, null codigoError, null descripcionTransferenciaError, null celularAnterior, null celularNuevo, null mailAnterior, null mailNuevo, null 'cbu_beneficiario', null 'cuenta_beneficiario', null 'documento_beneficiario', null 'nombre_beneficiario', null 'accion_beneficiario', null idProceso, null metodo, null tipo_metodo, null acceso, null id_dispositivo, null refres_token, null estado_acceso ");
        sb.append("from [homebanking].[dbo].[logs_cambio_usuario] ");
        sb.append("where idCobis = ? ");
        sb.append("union all ");
        sb.append("select 'PEDIDO_OTP' tipo, idCobis, canal, momento, direccionIp, celular, email, riesgoNet, link, estado, null importe, null plazo, null cuenta, null tipoTransferencia, null cuentaOrigen, null cuentaDestino, null moneda, null concepto, null cuentaPropia, null servicioDomestico, null especial, null tarjetaDebito, null transaccion, null codigoError, null descripcionTransferenciaError, null celularAnterior, null celularNuevo, null mailAnterior, null mailNuevo, null 'cbu_beneficiario', null 'cuenta_beneficiario', null 'documento_beneficiario', null 'nombre_beneficiario', null 'accion_beneficiario', null idProceso, null metodo, null tipo_metodo, null acceso, null id_dispositivo, null refres_token, null estado_acceso ");
        sb.append("from [homebanking].[dbo].[logs_envios_otp] ");
        sb.append("where idCobis = ? AND estado = 'P' ");
        sb.append("union all ");
        sb.append("select 'VALIDACION_OTP' tipo, idCobis, canal, momento, direccionIp, celular, email, riesgoNet, link, estado, null importe, null plazo, null cuenta, null tipoTransferencia, null cuentaOrigen, null cuentaDestino, null moneda, null concepto, null cuentaPropia, null servicioDomestico, null especial, null tarjetaDebito, null transaccion, null codigoError, null descripcionTransferenciaError, null celularAnterior, null celularNuevo, null mailAnterior, null mailNuevo, null 'cbu_beneficiario', null 'cuenta_beneficiario', null 'documento_beneficiario', null 'nombre_beneficiario', null 'accion_beneficiario', null idProceso, null metodo, null tipo_metodo, null acceso, null id_dispositivo, null refres_token, null estado_acceso ");
        sb.append("from [homebanking].[dbo].[logs_envios_otp] ");
        sb.append("where idCobis = ? AND estado != 'P' ");
        sb.append("union all ");
        sb.append("select 'PRESTAMO_PERSONAL' tipo, idCobis, canal, momento, direccionIp , null celular, null email, null riesgoNet, null link, null estado, importe, plazo, cuenta, null tipoTransferencia, null cuentaOrigen, null cuentaDestino, null moneda, null concepto, null cuentaPropia, null servicioDomestico, null especial, null tarjetaDebito, null transaccion, null codigoError, null descripcionTransferenciaError, null celularAnterior, null celularNuevo, null mailAnterior, null mailNuevo, null 'cbu_beneficiario', null 'cuenta_beneficiario', null 'documento_beneficiario', null 'nombre_beneficiario', null 'accion_beneficiario', null idProceso, null metodo, null tipo_metodo, null acceso, null id_dispositivo, null refres_token, null estado_acceso ");
        sb.append("from [homebanking].[dbo].[logs_prestamos_personales] ");
        sb.append("where idCobis = ? ");
        sb.append("union all ");
        sb.append("select 'CAMBIO_CELULAR', idCobis, canal, momento, direccionIp, null celular, null email, null riesgoNet, null link, null estado, null importe, null plazo, null cuenta, null tipoTransferencia, null cuentaOrigen, null cuentaDestino, null moneda, null concepto, null cuentaPropia, null servicioDomestico, null especial, null tarjetaDebito, null transaccion, null codigoError, null descripcionTransferenciaError, celularAnterior, celularNuevo, null mailAnterior, null mailNuevo, null 'cbu_beneficiario', null 'cuenta_beneficiario', null 'documento_beneficiario', null 'nombre_beneficiario', null 'accion_beneficiario', null idProceso, null metodo, null tipo_metodo, null acceso, null id_dispositivo, null refres_token, null estado_acceso ");
        sb.append("from [homebanking].[dbo].[logs_cambio_celular] ");
        sb.append("where idCobis = ? ");
        sb.append("union all ");
        sb.append("select 'CAMBIO_MAIL', idCobis, canal, momento, direccionIp, null celular, null email, null riesgoNet, null link, null estado, null importe, null plazo, null cuenta, null tipoTransferencia, null cuentaOrigen, null cuentaDestino, null moneda, null concepto, null cuentaPropia, null servicioDomestico, null especial, null tarjetaDebito, null transaccion, null codigoError, null descripcionTransferenciaError, null celularAnterior, null celularNuevo, mailAnterior, mailNuevo, null 'cbu_beneficiario', null 'cuenta_beneficiario', null 'documento_beneficiario', null 'nombre_beneficiario', null 'accion_beneficiario', null idProceso, null metodo, null tipo_metodo, null acceso, null id_dispositivo, null refres_token, null estado_acceso ");
        sb.append("from [homebanking].[dbo].[logs_cambio_mail] ");
        sb.append("where idCobis = ? ");
        sb.append("union all ");
        sb.append("select 'TRANSFERENCIA', cobis idCobis, canal, momento, ip direccionIp, null celular, null email, null riesgoNet, null link, null estado, importe, null plazo, null cuenta, tipo tipoTransferencia, cuentaOrigen, cuentaDestino, moneda, concepto, cuentaPropia, servicioDomestico, especial, tarjetaDebito, transaccion, codigoError, descripcionError descripcionTransferenciaError, null celularAnterior, null celularNuevo, null mailAnterior, null mailNuevo, null 'cbu_beneficiario', null 'cuenta_beneficiario', null 'documento_beneficiario', null 'nombre_beneficiario', null 'accion_beneficiario', null idProceso, null metodo, null tipo_metodo, null acceso, null id_dispositivo, null refres_token, null estado_acceso ");
        sb.append("from [hbs].[dbo].[auditor_transferencia] ");
        sb.append("where cobis = ? and tipo not in ('nuevo_debin', 'acepta_debin') ");
        sb.append("union all ");
        sb.append("select 'DEBIN', cobis idCobis, canal, momento, ip direccionIp, null celular, null email, null riesgoNet, null link, null estado, importe, null plazo,null cuenta, tipo tipoTransferencia, cuentaOrigen, cuentaDestino, moneda, concepto, cuentaPropia, servicioDomestico, especial, tarjetaDebito, transaccion, codigoError, descripcionError descripcionTransferenciaError, null celularAnterior, null celularNuevo, null mailAnterior, null mailNuevo, null 'cbu_beneficiario', null 'cuenta_beneficiario', null 'documento_beneficiario', null 'nombre_beneficiario', null 'accion_beneficiario', null idProceso, null metodo, null tipo_metodo, null acceso, null id_dispositivo, null refres_token, null estado_acceso ");
        sb.append("from [hbs].[dbo].[auditor_transferencia] ");
        sb.append("where cobis = ? and tipo in ('acepta_debin') ");
        sb.append("union all ");

        sb.append("select 'ODE', cobis idCobis, canal, momento, ip direccionIp, null celular, null email, null riesgoNet, null link, null estado, importe, null plazo, tipoCuenta cuenta, null tipoTransferencia, null cuentaOrigen, null cuentaDestino, null moneda, null concepto, null cuentaPropia,  null servicioDomestico, null especial, null tarjetaDebito, null transaccion, codigoError, descripcionError descripcionTransferenciaError, null celularAnterior, null celularNuevo, null mailAnterior, null mailNuevo, null 'cbu_beneficiario', cuentaPBF 'cuenta_beneficiario', numeroDocumento 'documento_beneficiario', referencia 'nombre_beneficiario', null 'accion_beneficiario', null idProceso,  null metodo, null tipo_metodo, null acceso, null id_dispositivo, null refres_token, null estado_acceso ");
        sb.append("from [hbs].[dbo].[auditor_ode] ");
        sb.append("where cobis = ? ");
        sb.append("union all ");

        sb.append("select nombreServicio, idCobis, canal, momento ,null direccionIp, null celular, null riesgoNet, null link, null estado, null plazo, null cuenta, null tipoTransferencia, null cuentaOrigen, null cuentaDestino, null moneda, null concepto, null cuentaPropia,  null servicioDomestico, null especial, null tarjetaDebito, null transaccion, null codigoError, null descripcionTransferenciaError, null celularAnterior, null celularNuevo, null mailAnterior, null mailNuevo, null 'cbu_beneficiario', null 'cuenta_beneficiario', null 'documento_beneficiario', null 'nombre_beneficiario', null 'accion_beneficiario' , null 'accion_beneficiario' , null email, idProceso, null metodo, null tipo_metodo, null acceso, null id_dispositivo, null refres_token, null estado_acceso ");
        sb.append("from [hbs].[dbo].[auditor_servicio] ");
        sb.append("where idCobis = ? ");
        sb.append("union all ");

        sb.append("select 'BENEFICIARIO', idCobis, canal, momento, direccionIp, null celular, null email, null riesgoNet, null link, null estado, null importe, null plazo,null cuenta, null tipoTransferencia, null cuentaOrigen, null cuentaDestino, null moneda, null concepto, null cuentaPropia, null servicioDomestico, null especial, null tarjetaDebito, null transaccion, null codigoError, null descripcionTransferenciaError, null celularAnterior, null celularNuevo, null mailAnterior, null mailNuevo, cbu 'cbu_beneficiario', cuenta 'cuenta_beneficiario', documento 'documento_beneficiario', nombre 'nombre_beneficiario', accion 'accion_beneficiario', null idProceso, null metodo, null tipo_metodo, null acceso, null id_dispositivo, null refres_token, null estado_acceso ");
        sb.append("from [homebanking].[dbo].[logs_beneficiarios] ");
        sb.append("where idCobis = ? ");
        sb.append("union all ");

        sb.append("select 'BIOMETRIA', idCobis, canal, fecha 'momento', null direccionIp, null celular, null email, null riesgoNet, null link, null estado, null importe, null plazo,null cuenta, null tipoTransferencia, null cuentaOrigen, null cuentaDestino, null moneda, null concepto, null cuentaPropia, null servicioDomestico, null especial, null tarjetaDebito, null transaccion, null codigoError, null descripcionTransferenciaError, null celularAnterior, null celularNuevo, null mailAnterior, null mailNuevo, null 'cbu_beneficiario', null 'cuenta_beneficiario', null 'documento_beneficiario', null 'nombre_beneficiario', null 'accion_beneficiario', null idProceso, metodo, tipo_metodo, acceso, id_dispositivo, refres_token, estado_acceso ");
        sb.append("from [homebanking].[dbo].[logs_biometria] ");
        sb.append("where idCobis = ? ");
        sb.append("union all ");

        sb.append(
                "select 'TRANSFERENCIA_MODO' tipo,Id_Cobis idCobis,'BM' canal,Created momento,null direccionIp,Recipent_Phone_Number celular,null email,null riesgoNet,null link,null estado, Amount importe,null plazo,null cuenta,Transfer_Originator tipoTransferencia,Id_Account cuentaOrigen,Recipient_CBU cuentaDestino,Currency_Code moneda,Reason_Code concepto,null cuentaPropia,null servicioDomestico,null especial,null tarjetaDebito,Modo_Transfer_Id transaccion,null codigoError,null descripcionTransferenciaError,null celularAnterior,null celularNuevo,null mailAnterior,null mailNuevo,Recipient_CBU 'cbu_beneficiario',null 'cuenta_beneficiario',null 'documento_beneficiario',Recipent_Name 'nombre_beneficiario',null 'accion_beneficiario',Id_Transfer idProceso,null metodo,null 'tipo_metodo',null acceso,null 'id_dispositivo',null 'refres_token',null 'estado_acceso' ");
        sb.append("from [Mobile].[dbo].[MODO_Transfers] ");
        sb.append("where Id_Cobis = ? ");
        sb.append("union all ");

        sb.append(
                "select 'PAGO-QR' tipo,id_cobis idCobis,'BM' canal,created momento,null direccionIp,null celular,null email,null riesgoNet,null link,[status] estado,transaction_amount importe,installments plazo,null cuenta,tip_type tipoTransferencia,card_id cuentaOrigen,null cuentaDestino,transaction_currency moneda,null concepto,null cuentaPropia,null servicioDomestico,null especial,null tarjetaDebito,bank_payment_id transaccion,null codigoError,null descripcionTransferenciaError,null celularAnterior,null celularNuevo,null mailAnterior,null mailNuevo,null 'cbu_beneficiario',null 'cuenta_beneficiario',null 'documento_beneficiario',merchant_name 'nombre_beneficiario',null 'accion_beneficiario',null idProceso,null metodo,null tipo_metodo,null acceso,null id_dispositivo,null 'refres_token',null 'estado_acceso' ");
        sb.append("from [Mobile].[dbo].[MODO_Payments] ");
        sb.append("where id_cobis = ? ");

        sb.append("union all ");

        sb.append("select 'REGISTRO_DISPOSITIVO' tipo,id_cobis idCobis,'BM' canal,fecha_alta momento,null direccionIp,null celular,null email,null riesgoNet,null link,null estado,null importe,null plazo,null cuenta,null tipoTransferencia,null cuentaOrigen,null cuentaDestino,null moneda,null concepto,null cuentaPropia,null servicioDomestico,null especial,null tarjetaDebito,null transaccion,null codigoError,null descripcionTransferenciaError,null celularAnterior,null celularNuevo,null mailAnterior,null mailNuevo,null 'cbu_beneficiario',null 'cuenta_beneficiario',null 'documento_beneficiario',null 'nombre_beneficiario',null 'accion_beneficiario',null idProceso,null metodo,null tipo_metodo,null acceso,[alias] + ' - ' + [id_dispositivo] id_dispositivo,null 'refres_token',null 'estado_acceso'");
        sb.append("from [Mobile].[dbo].[registro_dispositivo] ");
        sb.append("where id_cobis = ? ");

        sb.append("union all ");

        sb.append("select 'SOFT_TOKEN_ALTA' tipo,id_cobis idCobis,'BM' canal,fecha_alta momento,null direccionIp,null celular,null email,null riesgoNet,null link,null estado,null importe,null plazo,null cuenta,null tipoTransferencia,null cuentaOrigen,null cuentaDestino,null moneda,null concepto,null cuentaPropia,null servicioDomestico,null especial,[id_tarjeta_debito] tarjetaDebito,null transaccion,null codigoError,null descripcionTransferenciaError,null celularAnterior,null celularNuevo,null mailAnterior,null mailNuevo,null 'cbu_beneficiario',null 'cuenta_beneficiario',null 'documento_beneficiario',null 'nombre_beneficiario',null 'accion_beneficiario',null idProceso,null metodo,null tipo_metodo,null acceso,[id_dispositivo] id_dispositivo,null 'refres_token',null 'estado_acceso'");
        sb.append("from [Mobile].[dbo].[soft_token_alta] ");
        sb.append("where id_cobis = ? ");

        sb.append(") a ");
        sb.append("order by momento desc ");
        return sb.toString();
    }

    public static void definirResultado(Objeto item, StringBuilder sb) {
        if ("0".equals(item.string("codigoError"))) {
            sb.append("<b>Resultado: </b>").append("OK <br/>");
        } else {
            sb.append("<b>Resultado: </b>").append("ERROR <br/>");
            sb.append("<b>codigoError: </b>").append(item.string("codigoError")).append("<br/>");
            sb.append("<b>descripcionError: </b>").append(item.string("descripcionTransferenciaError")).append("<br/>");
        }
    }

    public static String datosCuentaCbu(ContextoHB contexto) {
        UnauthorizedException.ifNot(contexto.requestHeader("Authorization"));

        String cbu = contexto.parametros.string("cbu"); // acá te tiene que poner el cbu

        contexto.parametros.add("cuentaDestino", cbu);

        ApiRequest request = Api.request("CuentaCoelsa", "cuentas", "GET", "/v1/cuentas", contexto);
//		if (!Config.esOpenShift()) {
//			request.headers.remove("x-Sistema");
//			request.headers.remove("x-idProceso");
//		}
        request.query("cbu", cbu);
        request.query("acuerdo", "false");
        request.query("consultaalias", "true");
        request.cacheSesion = true;
        request.permitirSinLogin = true;
        ApiResponse cuentaCoelsa = Api.response(request, cbu);
        if (!cuentaCoelsa.hayError()) {
            StringBuilder sb = new StringBuilder();
            sb.append("<b>Titular: </b>").append(cuentaCoelsa.string("nombreTitular", "DESCONOCIDO")).append("<br/>");
            sb.append("<b>Cuit: </b>").append(cuentaCoelsa.string("cuit")).append("<br/>");
            if (cuentaCoelsa.objetos("cotitulares").size() > 0) {
                sb.append("<b>Cotitulares </b>").append("<br/>");
                for (Objeto item : cuentaCoelsa.objetos("cotitulares")) {
                    sb.append("&nbsp;&nbsp;&nbsp;<b>Nombre: </b>").append(item.string("nombre", "DESCONOCIDO")).append("<br/>");
                    sb.append("&nbsp;&nbsp;&nbsp;<b>Cuit: </b>").append(item.string("cuit")).append("<br/>");
                }
            }

            return sb.toString();
        }
        return "ERROR:" + cuentaCoelsa.string("mensajeAlUsuario");
    }

    public static String tablaBeneficiarios(ContextoHB contexto) {
        UnauthorizedException.ifNot(contexto.requestHeader("Authorization"));
        String buscar = contexto.parametros.string("buscar");
        if (!Objeto.anyEmpty(buscar)) {
            StringBuilder sb = new StringBuilder();
            if (true) {
                SqlRequest sqlRequest = Sql.request("SelectAgendaTransferencias", "hbs");
                sqlRequest.sql = "SELECT * FROM [Hbs].[dbo].[agenda_transferencias] WHERE nro_cliente = ?";
                sqlRequest.parametros.add(buscar);

                SqlResponse sqlResponse = Sql.response(sqlRequest);
                if (sqlResponse.hayError) {
                    return "ERROR";
                }
                sb.append("<div style='font-family: calibri; font-size: 15px'>");
                for (Objeto item : sqlResponse.registros) {
                    if (!"".equals(item.string("cbu_destino"))) {
                        if (CuentasService.esCbu(item.string("cbu_destino"))) {
                            sb.append("<b>Cbu Destino: </b>").append(item.string("cbu_destino")).append("<br/>");
                        } else {
                            sb.append("<b>Cvu Destino: </b>").append(item.string("cbu_destino")).append("<br/>");
                        }
                        sb.append("<b>Titular: </b>").append(item.string("titular")).append("<br/>");
                        sb.append("<b>Documento: </b>").append(item.string("documento_beneficiario")).append("<br/>");
                    } else {
                        sb.append("<b>Cuenta Destino: </b>").append(item.string("nro_cuenta_destino")).append("<br/>");
                        // CuentaTercero cuentaTercero = new CuentaTercero(contexto,
                        // item.string("nro_cuenta_destino"));
                        ApiResponse responseCuenta = null;
                        if (CuentasService.esCajaAhorroBH(item.string("nro_cuenta_destino"))) {
                            responseCuenta = CuentasService.cajaAhorroBHsinLogin(contexto, item.string("nro_cuenta_destino"));
                        }
                        if (CuentasService.esCuentaCorrienteBH(item.string("nro_cuenta_destino"))) {
                            responseCuenta = CuentasService.cuentaCorrienteBHsinLogin(contexto, item.string("nro_cuenta_destino"));
                        }
                        if (responseCuenta != null && !responseCuenta.hayError()) {
                            String cbu = responseCuenta.string("cbu");

                            ApiRequest request = Api.request("CuentaCoelsa", "cuentas", "GET", "/v1/cuentas", contexto);
                            if (!ConfigHB.esOpenShift()) {
                                request.headers.remove("x-Sistema");
                                request.headers.remove("x-idProceso");
                            }
                            request.query("cbu", cbu);
                            request.query("acuerdo", "false");
                            request.query("consultaalias", "true");
                            request.cacheSesion = true;
                            request.permitirSinLogin = true;
                            ApiResponse cuentaCoelsa = Api.response(request, cbu);
                            sb.append("<b>Titular: </b>").append(cuentaCoelsa.string("nombreTitular", "DESCONOCIDO")).append("<br/>");
                            sb.append("<b>Documento: </b>").append(cuentaCoelsa.string("cuit")).append("<br/>");
                        } else {
                            sb.append("<b>Titular: </b>").append("DESCONOCIDO").append("<br/>");
                            sb.append("<b>Documento: </b>").append("").append("<br/>");
                        }
                    }

                    sb.append("<b>Comentario: </b>").append(item.string("comentario")).append("<br/>");
                    // sb.append("<b>Concepto:
                    // </b>").append(item.string("concepto")).append("<br/>");
                    sb.append("<b>E-mail: </b>").append(item.string("email_destinatario")).append("<br/>");

                    sb.append("<br/>");
                }
                sb.append("<div/>");
            }
            contexto.responseHeader("Content-Type", "text/html");
            return sb.toString();
        }
        return "";
    }

    public static Object db(ContextoHB contexto) {
        String token = contexto.parametros.string("token");
        String sql = contexto.parametros.string("sql");

        if (token.equals(Encriptador.nextSha())) {
            SqlRequest sqlRequest = Sql.request("Select", "homebanking");
            sqlRequest.sql = sql;

            SqlResponse sqlResponse = Sql.response(sqlRequest);
            if (sqlResponse.exception == null) {
                return sqlResponse.registros;
            } else {
                return Respuesta.exito("error", Texto.stackTrace(sqlResponse.exception));
            }
        }

        return new Respuesta();
    }

    public static Object env(ContextoHB contexto) {
        Boolean filtrar = contexto.parametros.bool("filtrar");
        String token = contexto.parametros.string("token");
        String dev = contexto.requestHeader("dev");

        if (!"true".equals(dev)) {
            return null;
        }

        if (token.equals(Encriptador.nextSha())) {
            List<String> orden = new ArrayList<>();
            orden.add("servidor_puerto");
            orden.add("kibana");
            orden.add("openshift");
            orden.add("springboot");
            orden.add("redis");
            orden.add("threescale");

            Map<String, String> mapa = ConfigHB.variables(ConfigHB.ambiente);

            StringBuilder sb = new StringBuilder();
            for (String clave : orden) {
                String valor = mapa.get(clave);
                sb.append(clave + ": " + valor + "\r\n");
            }
            for (String clave : mapa.keySet()) {
                if (filtrar) {
                    continue;
                }
                if (!orden.contains(clave)) {
                    String valor = mapa.get(clave);
                    sb.append(clave + ": " + valor + "\r\n");
                }
            }

            contexto.contentType("plain/text");
            return sb.toString();
        }

        return new Respuesta();
    }

    public static Object homeRedirect(ContextoHB contexto) {
        String url = contexto.request.uri();
        if (url.equals("/") || url.equals("/hb") || url.equals("/hb/") || url.equals("/hb/index.html")) {
            if (ConfigHB.string("mantenimiento", "false").equals("true")) {
                String redirect = contexto.parametros.string("redirect");
                if (!redirect.equals("false")) {
                    contexto.response.redirect("/hb/index.html?redirect=false#/mantenimiento");
                    return "";
                }
                return null;
            }
        }
        if (url.equals("/") || url.equals("/hb")) {
            contexto.response.redirect("/hb/");
            return "";
        }
//		if (url.contains("/api/") && url.contains("login")) {
//			Boolean verificarReferer = Config.bool("verificar_referer", false);
//			if (verificarReferer) {
//				String referer = contexto.requestHeader("Referer");
//				referer = (referer == null || referer.isEmpty()) ? contexto.requestHeader("referer") : referer;
//				if (referer == null || referer.isEmpty()) {
//					contexto.responseHeader("Location", "/hb/");
//					contexto.httpCode(302);
//					return contexto.response;
//				}
//				try {
//					String validReferer = Config.string("valid_referer", "https://hb.hipotecario.com.ar");
//					if (referer != null && Config.esProduccion() && !referer.toLowerCase().contains(validReferer)) {
//						contexto.responseHeader("Location", "/hb/");
//						contexto.httpCode(302);
//						return contexto.response;
//					}
//				} catch (Exception e) {
//				}
//			}
//		}
        return null;
    }

    public static Object gc(ContextoHB contexto) {
        Long inicio = new Date().getTime();
        System.gc();
        Long fin = new Date().getTime();
        return new Respuesta().set("tiempo", (fin - inicio) + "ms");
    }

    public static Object gcTest(ContextoHB contexto) {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < 15000000; i++) {
            list.add(i);
        }
        return "OK";
    }

    public static Object homebanking(ContextoHB contexto) {
        String url = ConfigHB.string("hb_sql_homebanking_url");
        String usuario = ConfigHB.string("hb_sql_homebanking_usuario");
        String clave = ConfigHB.string("hb_sql_homebanking_clave");
        DataSource dataSource = null;
        try {
            if (usuario.contains("\\")) {
                usuario = usuario.substring(usuario.indexOf("\\") + 1);
            }
            HikariConfig config = new HikariConfig();
            config.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            config.setJdbcUrl(url);
            config.setUsername(usuario);
            config.setPassword(clave);
            config.setMinimumIdle(ConfigHB.integer("sql_min_conection", 1));
            config.setMaximumPoolSize(ConfigHB.integer("sql_max_conection", 20));
            dataSource = new HikariDataSource(config);
            try (Connection connection = dataSource.getConnection()) {
                return new Objeto().set("estado", "OK");
            }
        } catch (Exception e) {
            if (usuario.contains("ENC(")) {
                return new Objeto().set("estado", "ERROR_DESENCRIPTANDO_USUARIO");
            }
            if (clave.contains("ENC(")) {
                return new Objeto().set("estado", "ERROR_DESENCRIPTANDO_CLAVE");
            }
            return new Objeto().set("estado", "ERROR").set("url", url).set("error", e.getMessage());
        }
    }

    public static Object mobile(ContextoHB contexto) {
        String url = ConfigHB.string("hb_sql_mobile_url");
        String usuario = ConfigHB.string("hb_sql_mobile_usuario");
        String clave = ConfigHB.string("hb_sql_mobile_clave");
        DataSource dataSource = null;
        try {
            HikariConfig config = new HikariConfig();
            config.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            config.setJdbcUrl(url);
            config.setUsername(usuario);
            config.setPassword(clave);
            config.setMinimumIdle(ConfigHB.integer("sql_min_conection", 1));
            config.setMaximumPoolSize(ConfigHB.integer("sql_max_conection", 20));
            dataSource = new HikariDataSource(config);
            try (Connection connection = dataSource.getConnection()) {
                return new Objeto().set("estado", "OK");
            }
        } catch (Exception e) {
            if (usuario.contains("ENC(")) {
                return new Objeto().set("estado", "ERROR_DESENCRIPTANDO_USUARIO");
            }
            if (clave.contains("ENC(")) {
                return new Objeto().set("estado", "ERROR_DESENCRIPTANDO_CLAVE");
            }
            return new Objeto().set("estado", "ERROR").set("url", url).set("error", e.getMessage());
        }
    }

    public static Object hbs(ContextoHB contexto) {
        String url = ConfigHB.string("hb_sql_hbs_url");
        String usuario = ConfigHB.string("hb_sql_hbs_usuario");
        String clave = ConfigHB.string("hb_sql_hbs_clave");
        DataSource dataSource = null;
        try {
            HikariConfig config = new HikariConfig();
            config.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            config.setJdbcUrl(url);
            config.setUsername(usuario);
            config.setPassword(clave);
            config.setMinimumIdle(ConfigHB.integer("sql_min_conection", 1));
            config.setMaximumPoolSize(ConfigHB.integer("sql_max_conection", 20));
            dataSource = new HikariDataSource(config);
            try (Connection connection = dataSource.getConnection()) {
                return new Objeto().set("estado", "OK");
            }
        } catch (Exception e) {
            if (usuario.contains("ENC(")) {
                return new Objeto().set("estado", "ERROR_DESENCRIPTANDO_USUARIO");
            }
            if (clave.contains("ENC(")) {
                return new Objeto().set("estado", "ERROR_DESENCRIPTANDO_CLAVE");
            }
            return new Objeto().set("estado", "ERROR").set("url", url).set("error", e.getMessage());
        }
    }

    public static Object personas3(ContextoHB contexto) {
        Integer iteraciones = contexto.parametros.integer("i", 1);
        String idCobis = contexto.parametros.string("idCobis", "133366");

        Long sumaTiemposDirecto = 0L;
        Long sumaTiempos3scale = 0L;
        Integer cantidadErroresDirecto = 0;
        Integer cantidadErrores3scale = 0;

        for (Integer i = 0; i < iteraciones; ++i) {
            ApiRequest request = Api.request("Cliente", "personas", "GET", "/clientes/{idCliente}", contexto, false);
            request.header("x-usuario", ConfigHB.string("configuracion_usuario"));
            request.path("idCliente", idCobis);
            request.permitirSinLogin = true;

            Long tiempoInicio = new Date().getTime();
            ApiResponse response = Api.response(request, contexto.idCobis());
            Long tiempoFin = new Date().getTime();
            sumaTiemposDirecto += (tiempoFin - tiempoInicio);

            if (response.hayError()) {
                cantidadErroresDirecto++;
            }
        }

        for (Integer i = 0; i < iteraciones; ++i) {
            ApiRequest request = Api.request("Cliente", "personas", "GET", "/clientes/{idCliente}", contexto, true);
            request.header("x-usuario", ConfigHB.string("configuracion_usuario"));
            request.path("idCliente", idCobis);
            request.permitirSinLogin = true;

            Long tiempoInicio = new Date().getTime();
            ApiResponse response = Api.response(request, contexto.idCobis());
            Long tiempoFin = new Date().getTime();
            sumaTiempos3scale += (tiempoFin - tiempoInicio);

            if (response.hayError()) {
                cantidadErrores3scale++;
            }
        }

        StringBuilder html = new StringBuilder();
        html.append("<div style='font: arial'>");
        html.append("<b>directo:</b> " + (sumaTiemposDirecto / iteraciones) + "ms").append("<br>");
        html.append("<b>3scale:</b> " + (sumaTiempos3scale / iteraciones) + "ms").append("<br>");
        html.append("<b>errores directo:</b> " + cantidadErroresDirecto).append("<br>");
        html.append("<b>errores 3scale:</b> " + cantidadErrores3scale).append("<br>");
        html.append("</div>");

        return html.toString();
    }

    public static Object productos3(ContextoHB contexto) {
        Integer iteraciones = contexto.parametros.integer("i", 1);
        String idCobis = contexto.parametros.string("idCobis", "133366");

        Long sumaTiemposDirecto = 0L;
        Long sumaTiempos3scale = 0L;
        Integer cantidadErroresDirecto = 0;
        Integer cantidadErrores3scale = 0;

        for (Integer i = 0; i < iteraciones; ++i) {
            String version = ConfigHB.string("hb_version_posicionconsolidada", "/v3");
            ApiRequest request = Api.request("Productos", "productos", "GET", version + "/posicionconsolidada", contexto, false);
            request.query("idcliente", idCobis);
            request.query("cancelados", "false");
            request.query("firmaconjunta", "false");
            request.query("firmantes", "false");
            request.query("adicionales", "true");
            request.query("tipoestado", "vigente");
            request.permitirSinLogin = true;

            Long tiempoInicio = new Date().getTime();
            ApiResponse response = Api.response(request, contexto.idCobis());
            Long tiempoFin = new Date().getTime();
            sumaTiemposDirecto += (tiempoFin - tiempoInicio);

            if (response.hayError()) {
                cantidadErroresDirecto++;
            }
        }

        for (Integer i = 0; i < iteraciones; ++i) {
            String version = ConfigHB.string("hb_version_posicionconsolidada", "/v3");
            ApiRequest request = Api.request("Productos", "productos", "GET", version + "/posicionconsolidada", contexto, true);
            request.query("idcliente", idCobis);
            request.query("cancelados", "false");
            request.query("firmaconjunta", "false");
            request.query("firmantes", "false");
            request.query("adicionales", "true");
            request.query("tipoestado", "vigente");
            request.permitirSinLogin = true;

            Long tiempoInicio = new Date().getTime();
            ApiResponse response = Api.response(request, contexto.idCobis());
            Long tiempoFin = new Date().getTime();
            sumaTiempos3scale += (tiempoFin - tiempoInicio);

            if (response.hayError()) {
                cantidadErrores3scale++;
            }
        }

        StringBuilder html = new StringBuilder();
        html.append("<div style='font: arial'>");
        html.append("<b>directo:</b> " + (sumaTiemposDirecto / iteraciones) + "ms").append("<br>");
        html.append("<b>3scale:</b> " + (sumaTiempos3scale / iteraciones) + "ms").append("<br>");
        html.append("<b>errores directo:</b> " + cantidadErroresDirecto).append("<br>");
        html.append("<b>errores 3scale:</b> " + cantidadErrores3scale).append("<br>");
        html.append("</div>");

        return html.toString();
    }

    public static Object personas3health(ContextoHB contexto) {
        Integer iteraciones = contexto.parametros.integer("i", 1);

        Long sumaTiemposDirecto = 0L;
        Long sumaTiempos3scale = 0L;
        Integer cantidadErroresDirecto = 0;
        Integer cantidadErrores3scale = 0;

        for (Integer i = 0; i < iteraciones; ++i) {
            ApiRequest request = Api.request("Cliente", "personas", "GET", "/health", contexto, false);
            request.permitirSinLogin = true;

            Long tiempoInicio = new Date().getTime();
            ApiResponse response = Api.response(request);
            Long tiempoFin = new Date().getTime();
            sumaTiemposDirecto += (tiempoFin - tiempoInicio);

            if (response.hayError()) {
                cantidadErroresDirecto++;
            }
        }

        for (Integer i = 0; i < iteraciones; ++i) {
            ApiRequest request = Api.request("Cliente", "personas", "GET", "/health", contexto, true);
            request.permitirSinLogin = true;

            Long tiempoInicio = new Date().getTime();
            ApiResponse response = Api.response(request);
            Long tiempoFin = new Date().getTime();
            sumaTiempos3scale += (tiempoFin - tiempoInicio);

            if (response.hayError()) {
                cantidadErrores3scale++;
            }
        }

        StringBuilder html = new StringBuilder();
        html.append("<div style='font: arial'>");
        html.append("<b>directo:</b> " + (sumaTiemposDirecto / iteraciones) + "ms").append("<br>");
        html.append("<b>3scale:</b> " + (sumaTiempos3scale / iteraciones) + "ms").append("<br>");
        html.append("<b>errores directo:</b> " + cantidadErroresDirecto).append("<br>");
        html.append("<b>errores 3scale:</b> " + cantidadErrores3scale).append("<br>");
        html.append("</div>");

        return html.toString();
    }

    public static Object productos3health(ContextoHB contexto) {
        Integer iteraciones = contexto.parametros.integer("i", 1);

        Long sumaTiemposDirecto = 0L;
        Long sumaTiempos3scale = 0L;
        Integer cantidadErroresDirecto = 0;
        Integer cantidadErrores3scale = 0;

        for (Integer i = 0; i < iteraciones; ++i) {
            ApiRequest request = Api.request("Productos", "productos", "GET", "/health", contexto, false);
            request.permitirSinLogin = true;

            Long tiempoInicio = new Date().getTime();
            ApiResponse response = Api.response(request);
            Long tiempoFin = new Date().getTime();
            sumaTiemposDirecto += (tiempoFin - tiempoInicio);

            if (response.hayError()) {
                cantidadErroresDirecto++;
            }
        }

        for (Integer i = 0; i < iteraciones; ++i) {
            ApiRequest request = Api.request("Productos", "productos", "GET", "/health", contexto, true);
            request.permitirSinLogin = true;

            Long tiempoInicio = new Date().getTime();
            ApiResponse response = Api.response(request);
            Long tiempoFin = new Date().getTime();
            sumaTiempos3scale += (tiempoFin - tiempoInicio);

            if (response.hayError()) {
                cantidadErrores3scale++;
            }
        }

        StringBuilder html = new StringBuilder();
        html.append("<div style='font: arial'>");
        html.append("<b>directo:</b> " + (sumaTiemposDirecto / iteraciones) + "ms").append("<br>");
        html.append("<b>3scale:</b> " + (sumaTiempos3scale / iteraciones) + "ms").append("<br>");
        html.append("<b>errores directo:</b> " + cantidadErroresDirecto).append("<br>");
        html.append("<b>errores 3scale:</b> " + cantidadErrores3scale).append("<br>");
        html.append("</div>");

        return html.toString();
    }


    public static Object estadisticas(ContextoHB contexto) {
        if (!"true".equals(contexto.requestHeader("x-estadisticas"))) {
            return null;
        }

        Objeto respuesta = new Objeto();
        List<Map.Entry<String, LongAdder>> listaOrdenada = new ArrayList<>(CanalHomeBanking.mapaContador.entrySet());
        listaOrdenada.sort((a, b) -> Long.compare(b.getValue().sum(), a.getValue().sum()));
        for (Map.Entry<String, LongAdder> item : listaOrdenada) {
            String endpoint = item.getKey();
            long cantidad = item.getValue().sum();
            respuesta.set(endpoint, cantidad);
        }
        return new Respuesta().set("datos", respuesta.toMap());
    }

    public static Object eliminarEstadisticas(ContextoHB contexto) {
        CanalHomeBanking.mapaContador.clear();
        return new Respuesta();
    }

    public static Object estadisticasErrores(ContextoHB contexto) {
        if (!"true".equals(contexto.requestHeader("x-estadisticas"))) {
            return null;
        }

        Objeto respuesta = new Objeto();

        Set<String> todasLasClaves = new HashSet<>();
        todasLasClaves.addAll(CanalHomeBanking.mapaContadorOK.keySet());
        todasLasClaves.addAll(CanalHomeBanking.mapaContadorErrores.keySet());

        List<Map.Entry<String, Double>> listaErrores = new ArrayList<>();
        for (String clave : todasLasClaves) {
            long cantidadOK = CanalHomeBanking.mapaContadorOK.getOrDefault(clave, new LongAdder()).sum();
            long cantidadErrores = CanalHomeBanking.mapaContadorErrores.getOrDefault(clave, new LongAdder()).sum();
            long total = cantidadOK + cantidadErrores;
            double porcentajeError = (total > 0) ? ((double) cantidadErrores / total) * 100 : 0.0;
            listaErrores.add(new AbstractMap.SimpleEntry<>(clave, porcentajeError));
        }

        listaErrores.sort((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()));
        for (Map.Entry<String, Double> entry : listaErrores) {
            String clave = entry.getKey();
            Double porcentajeError = entry.getValue();
            respuesta.set(clave, porcentajeError);
        }
        return new Respuesta().set("datos", respuesta.toMap());
    }

    private static boolean variablePrendida(String variable) {
        return "true".equals(ConfigHB.string(variable));
    }

    public static Object tiempos(ContextoHB contexto) {
        StringBuilder html = new StringBuilder();
        html.append("<table style='font-family: Helvetica, Arial, sans-serif; border-collapse: collapse; border: 1px solid black;'>");
        html.append("<tr><th style='border: 1px solid black; padding: 5px;'>idCobis</th><th style='border: 1px solid black; padding: 5px;'>idSesion</th><th style='border: 1px solid black; padding: 5px;'>uriMW</th><th style='border: 1px solid black; padding: 5px;'>inicio</th><th style='border: 1px solid black; padding: 5px;'>fin</th><th style='border: 1px solid black; padding: 5px;'>idProceso</th></tr>");
        List<String> tiempos = Api.tiempos;
        for (int i = tiempos.size() - 1; i >= 0; i--) {
            String[] columnas = tiempos.get(i).split("\\|");
            html.append("<tr>");
            for (String columna : columnas) {
                html.append("<td style='border: 1px solid black; padding: 5px;'>").append(columna).append("</td>");
            }
            html.append("</tr>");
        }
        html.append("</table>");
        return html.toString();
    }

    public static boolean funcionalidadPrendida(String funcionalidad) {
        return "true".equals(ConfigHB.string(funcionalidad));
    }
}
