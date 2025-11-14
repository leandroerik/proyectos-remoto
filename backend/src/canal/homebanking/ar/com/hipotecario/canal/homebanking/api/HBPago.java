package ar.com.hipotecario.canal.homebanking.api;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.servicio.api.transmit.JourneyTransmitEnum;
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
import ar.com.hipotecario.canal.homebanking.lib.Concurrencia;
import ar.com.hipotecario.canal.homebanking.lib.Formateador;
import ar.com.hipotecario.canal.homebanking.lib.Pdf;
import ar.com.hipotecario.canal.homebanking.negocio.Cuenta;
import ar.com.hipotecario.canal.homebanking.negocio.TarjetaDebito;
import ar.com.hipotecario.canal.homebanking.servicio.PagoServicioService;
import ar.com.hipotecario.canal.homebanking.servicio.ProductosService;
import ar.com.hipotecario.canal.homebanking.servicio.RestCatalogo;
import ar.com.hipotecario.canal.homebanking.servicio.RestNotificaciones;
import ar.com.hipotecario.canal.homebanking.servicio.RestPostventa;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import static ar.com.hipotecario.canal.homebanking.negocio.Constantes.SEPARADOR_COMPROBANTE_AMEX;

public class HBPago {

    /* ========== CONSOLIDADA ========== */
    public static Respuesta consolidadaPagos(ContextoHB contexto) {
        Boolean buscarPagables = contexto.parametros.bool("buscarPagables", false);
        Boolean buscarAgendaPagos = contexto.parametros.bool("buscarAgendaPagos", false);
        Boolean buscarVeps = contexto.parametros.bool("buscarVeps", false);
        String filtrarPorCodigoEnte = contexto.parametros.string("filtrarPorEnte", null);
        String filtrarPorCodigoLink = contexto.parametros.string("filtrarPorCodigoLink", null);
        String cuitOriginanteVeps = contexto.parametros.string("cuitOriginante", null);

        List<TarjetaDebito> tarjetasDebito = contexto.tarjetasDebito();
        if (tarjetasDebito.isEmpty()) {
            return Respuesta.estado("SIN_TARJETA_DEBITO");
        }

        ApiResponse responseProdConCancelados = ProductosService.productosConCancelados(contexto);
        List<Objeto> listTarjetasCanceladas = new ArrayList<>();

        for (Objeto unaTarjetaDebito : responseProdConCancelados.objetos("tarjetasDebito")) {
            if (unaTarjetaDebito.string("estado").trim().equals("C")) {
                listTarjetasCanceladas.add(unaTarjetaDebito);
            }
        }

        Integer size = listTarjetasCanceladas.size();

        if (!responseProdConCancelados.hayError() && size != 0) {

            Objeto sqlData = new Objeto();
            Objeto sqlParameters = new Objeto();
            sqlData.set("from", "[pagoServiciosTDMigradas]");
            sqlData.set("where", "numeroTarjetaDebito?");

            Objeto sqlDataCancelada = new Objeto();
            Objeto sqlParametersCancelada = new Objeto();
            sqlDataCancelada.set("from", "[PagoServiciosPerteneceA]");
            sqlDataCancelada.set("where", "psp_tarjetaDebito?");

            for (TarjetaDebito unaTarjetaDebitoHabilitadas : tarjetasDebito) {
                if (unaTarjetaDebitoHabilitadas.activacionTemprana()) {
                    continue;
                }

                sqlParameters.set("numeroTarjetaDebito", unaTarjetaDebitoHabilitadas.numero());
                sqlData.set("parameters", sqlParameters);

                SqlResponse sqlResponseTDMigradas = selectAllSqlHomeBanking(sqlData);

                if (sqlResponseTDMigradas.registros.size() == 0) {

                    sqlParametersCancelada.set("psp_tarjetaDebito", listTarjetasCanceladas.get(size - 1).string("numeroProducto"));
                    sqlDataCancelada.set("parameters", sqlParametersCancelada);

                    SqlResponse sqlResponsePagoServicioPeteneceA = selectAllSqlHomeBanking(sqlDataCancelada);
                    Integer sizeResgistrosServicioPerteneceA = sqlResponsePagoServicioPeteneceA.registros.size();

                    if (sizeResgistrosServicioPerteneceA != 0) {
                        Integer contador = 0;

                        for (Objeto unRegistroServicio : sqlResponsePagoServicioPeteneceA.registros) {
                            String psp_codigoPago = unRegistroServicio.string("psp_codigoPago");
                            String psp_perteneceA = unRegistroServicio.string("psp_perteneceA");

                            SqlResponse sqlReponse = insertOrUpdateDescripcionAgendaPagosLink(unaTarjetaDebitoHabilitadas.numero(), psp_codigoPago, psp_perteneceA);

                            if (!sqlReponse.hayError && contador < sizeResgistrosServicioPerteneceA - 1) {
                                Objeto sqlDataMigrads = new Objeto();
                                Objeto sqlDataValuesMigrads = new Objeto();
                                sqlDataMigrads.set("insert", "[pagoServiciosTDMigradas] ([numeroTarjetaDebito])");
                                sqlDataValuesMigrads.set("numeroTarjetaDebito", unaTarjetaDebitoHabilitadas.numero());
                                sqlDataMigrads.set("values", sqlDataValuesMigrads);

                                SqlResponse sqlReponseMigrada = insertOrUpdateHomeBanking(sqlDataMigrads);
                                contador = sqlReponseMigrada.hayError ? 0 : contador++;
                            }
                        }
                    }
                }
            }
        }

        Respuesta respuesta = new Respuesta();
        Set<String> codigosPagables = new HashSet<>();

        if (ConfigHB.esDesarrollo())
            buscarVeps = false;

        if (buscarVeps) {
            ApiRequest request = null;
            request = Api.request("LinkGetVepsPendientes", "veps", "GET", "/v1/veps/{idTributarioCliente}/pendientes", contexto);
            request.header("x-usuariocanal", ConfigHB.string("configuracion_usuario"));

            request.path("idTributarioCliente", contexto.persona().cuit());
            request.query("numeroTarjeta", contexto.tarjetaDebitoPorDefecto().numero());
            request.query("idTributarioContribuyente", contexto.persona().cuit());
            request.query("idTributarioOriginante", cuitOriginanteVeps != null ? cuitOriginanteVeps : contexto.persona().cuit());
            if (cuitOriginanteVeps == null || cuitOriginanteVeps.equals("")) {
                request.query("tipoConsultaLink", "1");
            } else {
                request.query("tipoConsultaLink", "2");
            }

            ApiResponse response = Api.response(request, contexto.persona().cuit());
            if (response.hayError() && response.codigo != 404) {
                respuesta.setEstadoExistenErrores();
            }

            if (!(response.hayError() && response.codigo != 404)) {
                for (Objeto item : response.objetos("veps")) {
                    String id = "A";
                    id += "_" + String.format("%012d", item.integer("nroVep"));
                    id += "_" + item.string("token");
                    id += "_" + item.bigDecimal("informacionVep.importe");

                    Objeto vep = new Objeto();
                    vep.set("id", id);
                    vep.set("codigo", item.string("nroVep"));
                    vep.set("tipo", "vep");
                    vep.set("servicio", item.string("informacionVep.pagoDesc"));
                    vep.set("informacionAdicional", "");
                    vep.set("pagable", true);
                    vep.set("vencimiento", item.date("informacionVep.fechaExpiracion", "yyyy-MM-dd", "dd/MM"));
                    vep.set("importe", item.bigDecimal("informacionVep.importe"));
                    vep.set("importeFormateado", Formateador.importe(item.bigDecimal("informacionVep.importe")));
                    vep.set("fechaVencimiento", item.date("informacionVep.fechaExpiracion", "yyyy-MM-dd", "yyyy-MM-dd"));
                    vep.set("orden", "A");
                    respuesta.add("consolidadaPago", vep.ordenar("id"));
                }
            }
        }

        if (buscarPagables) {
            Map<TarjetaDebito, ApiResponse> mapaResponse = PagoServicioService.pendientes(contexto);
            for (TarjetaDebito tarjetaDebito : mapaResponse.keySet()) {
                if (tarjetaDebito.activacionTemprana()) {
                    continue;
                }

                ApiResponse response = mapaResponse.get(tarjetaDebito);
                if (response.hayError()) {
                    respuesta.setEstadoExistenErrores();
                    continue;
                }

                for (Objeto item : response.objetos()) {
                    List<Objeto> lista = new ArrayList<>();
                    lista.addAll(item.objetos("vencimiento"));
                    lista.addAll(item.objetos("conceptos"));

                    for (Objeto subitem : lista) {
                        Objeto concepto = subitem.existe("concepto") ? subitem.objeto("concepto") : subitem;
                        Objeto vencimiento = subitem.existe("concepto") ? subitem : null;

                        String id = vencimiento == null ? "C" : "V";
                        id += "_" + tarjetaDebito.id();
                        id += "_" + item.string("codigoAdhesion");
                        id += "_" + item.string("ente.codigo");
                        id += "_" + item.string("ususarioLP");
                        id += "_" + concepto.string("codigo");
                        if (vencimiento != null) {
                            id += "_" + vencimiento.string("id");
                            id += "_" + vencimiento.string("importe");
                        }

                        Objeto itemAgenda = new Objeto();
                        itemAgenda.set("id", id);
                        itemAgenda.set("codigo", item.string("codigoAdhesion"));
                        itemAgenda.set("codigoLink", item.string("ususarioLP"));
                        itemAgenda.set("servicio", item.string("ente.descripcion") + (lista.size() > 1 ? " - " + concepto.string("descripcion").trim() : ""));
                        itemAgenda.set("descripcionConcepto", concepto.string("descripcion").trim());
                        itemAgenda.set("ingresaReferencia", concepto.bool("isIngresoReferencia"));
                        itemAgenda.set("ingresaImporte", item.bool("ente.isIngresoImporte"));
                        itemAgenda.set("tieneImporte", !item.bool("ente.isIngresoImporte"));

                        // emm-20200402-desde
                        // para el caso que es con vencimiento pero que desde
                        // itemAgenda.set("tieneImporte", true);
                        boolean esVencimientoQueIngresaImporte = false;
                        if (vencimiento != null && item.bool("ente.isIngresoImporte")) {
                            if (item.string("ente.rubro.codigo").equals("09")) {
                                itemAgenda.set("ingresaImporte", true);
                                itemAgenda.set("tieneImporte", false);
                                id = "C" + id.substring(1);
                                itemAgenda.set("id", id);
                                esVencimientoQueIngresaImporte = true;
                            } else {
                                itemAgenda.set("ingresaImporte", false);
                                itemAgenda.set("tieneImporte", true);
                            }
                        }

                        // emm-20200402-hasta

                        itemAgenda.set("informacionAdicional", "");
                        itemAgenda.set("pagable", true);
                        if ("09".equals(item.string("ente.rubro.codigo"))) {
                            if (!"938".equals(item.string("ente.codigo"))) {
                                itemAgenda.set("tarjetaCredito", "XXXX-" + Formateador.ultimos4digitos(item.string("ususarioLP", "")));
                            }
                        }

                        if (vencimiento != null && !esVencimientoQueIngresaImporte) {
                            itemAgenda.set("tipo", "servicio-vencimiento");
                            itemAgenda.set("orden", "B");
                            itemAgenda.set("importe", vencimiento.bigDecimal("importe"));
                            itemAgenda.set("importeFormateado", Formateador.importe(vencimiento.bigDecimal("importe")));
                            itemAgenda.set("vencimiento", vencimiento.date("fecha", "yyMMdd", "dd/MM"));
                            itemAgenda.set("fechaVencimiento", vencimiento.date("fecha", "yyMMdd", "yyyy-MM-dd"));
                            itemAgenda.set("vence", true);
                        } else {
                            itemAgenda.set("tipo", "servicio");
                            itemAgenda.set("orden", "C");
                        }

                        Boolean agendar = true;
                        agendar &= filtrarPorCodigoEnte == null || filtrarPorCodigoEnte.equals(item.string("ente.codigo"));
                        agendar &= filtrarPorCodigoLink == null || filtrarPorCodigoLink.equals(item.string("ususarioLP")) || (filtrarPorCodigoLink.length() > 3 && filtrarPorCodigoLink.endsWith(item.string("ususarioLP"))) || (filtrarPorCodigoLink.length() > 3 && item.string("ususarioLP").endsWith(filtrarPorCodigoLink));

                        if (agendar) {
                            codigosPagables.add(item.string("ususarioLP"));
                            respuesta.add("consolidadaPago", itemAgenda.ordenar("id"));
                        }
                    }
                }
            }
        }

        if (buscarAgendaPagos) {
            Map<TarjetaDebito, ApiResponse> mapaResponse = PagoServicioService.linkGetAdhesiones(contexto);
            for (TarjetaDebito tarjetaDebito : mapaResponse.keySet()) {
                if (tarjetaDebito.activacionTemprana()) {
                    continue;
                }

                ApiResponse response = mapaResponse.get(tarjetaDebito);
                if (response.hayError()) {
                    respuesta.setEstadoExistenErrores();
                    continue;
                }

                for (Objeto item : response.objetos()) {
                    if (!codigosPagables.contains(item.string("codigoPagoElectronico"))) {
                        String id = "N";
                        id += "_" + tarjetaDebito.numero();
                        id += "_" + item.string("codigoAdhesion");
                        id += "_" + item.string("ente.codigo");
                        id += "_" + item.string("codigoPagoElectronico");

                        Objeto itemAgenda = new Objeto();
                        itemAgenda.set("id", id);
                        itemAgenda.set("codigo", item.string("codigoAdhesion"));
                        itemAgenda.set("codigoLink", item.string("codigoPagoElectronico"));
                        itemAgenda.set("tipo", "servicio");
                        itemAgenda.set("servicio", item.string("ente.descripcion"));
                        itemAgenda.set("informacionAdicional", "");
                        itemAgenda.set("orden", "D");
                        if (buscarPagables || buscarVeps) {
                            itemAgenda.set("pagable", false);
                        }

                        Boolean agendar = true;
                        agendar &= filtrarPorCodigoEnte == null || filtrarPorCodigoEnte.equals(item.string("ente.codigo"));
                        agendar &= filtrarPorCodigoLink == null || filtrarPorCodigoLink.equals(item.string("codigoPagoElectronico"));
                        if (agendar) {
                            respuesta.add("consolidadaPago", itemAgenda.ordenar("id"));
                        }
                    }
                }
            }
        }

        if (!respuesta.objetos("consolidadaPago").isEmpty()) {
            SqlResponse sqlResponse = selectDescripcionAgendaPagosLink(contexto);
            if (sqlResponse.hayError) {
                respuesta.setEstadoExistenErrores();
            }

            for (Objeto item : respuesta.objetos("consolidadaPago")) {
                for (Objeto registro : sqlResponse.registros) {
                    if (registro.string("psp_codigoPago").equals(item.string("codigoLink"))) {
                        item.set("informacionAdicional", registro.string("psp_perteneceA"));
                    }
                }
            }
        }

        respuesta.objeto("consolidadaPago").ordenar("orden", "fechaVencimiento", "servicio", "_bigdecimal_importe");
        return respuesta;
    }

    /* ========== PAGOS VEP POR CUIT ========== */
    public static Respuesta comprobantesPendientesVepPorCuit(ContextoHB contexto) {
        String cuitOriginanteVeps = contexto.parametros.string("cuitOriginante", null);

        List<TarjetaDebito> tarjetasDebito = contexto.tarjetasDebito();
        if (tarjetasDebito.isEmpty()) {
            return Respuesta.estado("SIN_TARJETA_DEBITO");
        }

        Respuesta respuesta = new Respuesta();
        // Set<String> codigosPagables = new HashSet<>();

        // if (buscarVeps) {

        ApiRequest request = null;
        request = Api.request("LinkGetVepsPendientes", "veps", "GET", "/v1/veps/{idTributarioCliente}/pendientes", contexto);
        request.header("x-usuariocanal", ConfigHB.string("configuracion_usuario"));

        request.path("idTributarioCliente", contexto.persona().cuit());
        request.query("numeroTarjeta", contexto.tarjetaDebitoPorDefecto().numero());
        if (cuitOriginanteVeps != null) {
            if (cuitOriginanteVeps.equals(contexto.persona().cuit()))
                cuitOriginanteVeps = null;
            else
                request.query("idTributarioContribuyente", cuitOriginanteVeps != null ? cuitOriginanteVeps : contexto.persona().cuit());
        }
        request.query("idTributarioOriginante", cuitOriginanteVeps != null ? cuitOriginanteVeps : contexto.persona().cuit());
        if (cuitOriginanteVeps == null || cuitOriginanteVeps.equals("")) {
            request.query("tipoConsultaLink", "1");
        } else {
            request.query("tipoConsultaLink", "2");
        }

        ApiResponse response = Api.response(request, contexto.persona().cuit());
        if (response.hayError() && response.codigo != 404) {
            respuesta.setEstadoExistenErrores();
        }

        if (!(response.hayError() && response.codigo != 404)) {
            for (Objeto item : response.objetos("veps")) {
                String id = "A";
                id += "_" + String.format("%012d", item.integer("nroVep"));
                id += "_" + item.string("token");
                id += "_" + item.bigDecimal("informacionVep.importe");

                Objeto vep = new Objeto();
                vep.set("id", id);
                vep.set("codigo", item.string("nroVep"));
                vep.set("tipo", "vep");
                vep.set("servicio", item.string("informacionVep.pagoDesc"));
                vep.set("informacionAdicional", "");
                vep.set("pagable", true);
                vep.set("vencimiento", item.date("informacionVep.fechaExpiracion", "yyyy-MM-dd", "dd/MM"));
                vep.set("importe", item.bigDecimal("informacionVep.importe"));
                vep.set("importeFormateado", Formateador.importe(item.bigDecimal("informacionVep.importe")));
                vep.set("fechaVencimiento", item.date("informacionVep.fechaExpiracion", "yyyy-MM-dd", "yyyy-MM-dd"));
                vep.set("orden", "A");
                respuesta.add("consolidadaPago", vep.ordenar("id"));
            }
        }
        // }

        respuesta.objeto("consolidadaPago").ordenar("orden", "fechaVencimiento", "servicio", "_bigdecimal_importe");
        return respuesta;
    }

    public static Respuesta consultaDebitosAutomaticos(ContextoHB contexto) {

        Respuesta respuesta = new Respuesta();
        // for (Cuenta cuenta : contexto.cuentasPesos()) {
        ApiRequest request = Api.request("CuentasDebitosAutomaticos", "cuentas", "GET", "/v1/debitoautomatico", contexto);

        request.query("codigoBanco", "044");
        request.query("codigoCliente", contexto.idCobis());
        request.query("convenio", "11");
        request.query("filial", "0");
        request.query("modo", "0");
        request.query("origen", "M");
        // request.query("tipoCuenta", cuenta.numero().substring(0,1));
        // request.query("tope", "0");

        ApiResponse response = Api.response(request, contexto.idCobis());
        if (response.hayError()) {
            return respuesta;
        }

        for (Objeto item : response.objetos()) {
            if ("V".equals(item.string("estado"))) {
                Objeto servicio = new Objeto();
                servicio.set("codigoAdhesion", item.string("codigoAdhesion"));
                servicio.set("descripcionAdhesion", item.string("descripcionAdhesion"));
                servicio.set("nombreEmpresa", item.string("nombreEmpresa"));
                servicio.set("servicio", item.string("servicio"));
                servicio.set("cuenta", Cuenta.descripcionCuentaComprobante(Cuenta.tipo(item.string("tipoCuenta")), Cuenta.idMoneda(item.string("codigoCuenta")), "XXXX" + "-" + Formateador.ultimos4digitos(item.string("codigoCuenta"))));
                servicio.set("numeroCuenta", item.string("codigoCuenta"));
                servicio.set("cuitEmpresa", item.string("cuitEmpresa"));
                servicio.set("estado", "activo");
                servicio.set("estadoDescripcion", "");

                /*
                 * if ("V".equals(item.string("estado"))) { // comento esta parte, ya que solo
                 * voy a cargar los vigentes servicio.set("estado", "activo");
                 * servicio.set("estadoDescripcion", ""); } else { if
                 * ("I".equals(item.string("estado"))) { servicio.set("estado", "ingresado");
                 * servicio.set("estadoDescripcion",
                 * "Solicitud en curso. Este proceso puede demorar unos días."); } else {
                 * servicio.set("estado", "con errores"); servicio.set("estadoDescripcion",
                 * "Adhesión con errores"); } }
                 */

                respuesta.add("servicios", servicio);
            }
        }
        // }
        return respuesta;
    }

    public static Respuesta agregarDebitoAutomatico(ContextoHB contexto) {
        String numeroCuenta = contexto.parametros.string("numeroCuenta");
        String codigoAdhesion = contexto.parametros.string("codigoAdhesion");
        String descripcionAdhesion = contexto.parametros.string("descripcionAdhesion");
        String servicio = contexto.parametros.string("servicio");
        String csmId = contexto.parametros.string("csmId", "");
        String checksum = contexto.parametros.string("checksum", "");

        if (Objeto.anyEmpty(numeroCuenta, codigoAdhesion, descripcionAdhesion, servicio)) {
            return Respuesta.parametrosIncorrectos();
        }

        boolean esMigrado = contexto.esMigrado(contexto);

        if (esMigrado && Objeto.anyEmpty(csmId, checksum))
            return Respuesta.parametrosIncorrectos();

        Cuenta cuenta = contexto.cuenta(numeroCuenta);
        if (cuenta == null) {
            return Respuesta.error();
        }

        TarjetaDebito tarjetaDebito = contexto.tarjetaDebitoAsociada(cuenta);
        if (tarjetaDebito == null) {
            return Respuesta.estado("SIN_TARJETA_DEBITO");
        }

        Respuesta respuestaValidaTransaccion = contexto.validarTransaccion(contexto, esMigrado, "adhesion-pago", JourneyTransmitEnum.HB_INICIO_SESION);
        if (respuestaValidaTransaccion.hayError())
            return respuestaValidaTransaccion;

        Respuesta respuesta = new Respuesta();

        ApiRequest request = Api.request("CuentasDebitosAutomaticosAlta", "cuentas", "POST", "/v1/debitoautomatico", contexto);

        request.body("codigoAdhesion", codigoAdhesion);
        request.body("codigoBanco", 44);
        request.body("codigoCliente", contexto.idCobis());
        request.body("codigoCuenta", cuenta.numero());
        request.body("convenio", 11);
        request.body("descripcionAdhesion", descripcionAdhesion);
        request.body("filial", 0);
        request.body("modo", 0);
        request.body("origen", "M");
        request.body("servicio", servicio);
        request.body("tope", 0);

        ApiResponse response = Api.response(request, cuenta.id());
        if (response.hayError())
            return Respuesta.error(contexto.csmIdAuth);

        return respuesta.set("csmIdAuth", contexto.csmIdAuth);
    }

    public static Respuesta eliminarDebitoAutomatico(ContextoHB contexto) {
        String numeroCuenta = contexto.parametros.string("numeroCuenta");
        String codigoAdhesion = contexto.parametros.string("codigoAdhesion");
        // String servicio = contexto.parametros.string("servicio");
        // Boolean dadoDeAltaCanal = contexto.parametros.bool("dadoDeAltaCanal");

        Respuesta respuesta = new Respuesta();
        try {
            if (Objeto.anyEmpty(numeroCuenta, codigoAdhesion)) {
                return Respuesta.parametrosIncorrectos();
            }

            if (contexto.cuenta(numeroCuenta) == null) {
                return Respuesta.parametrosIncorrectos();
            }
            Cuenta cuenta = contexto.cuenta(numeroCuenta);

            String tipificacion = contexto.cuenta(cuenta.numero()).esCajaAhorro() ? "BAJA_ADHESION_CA_PEDIDO" : "BAJA_ADHESION_CC_PEDIDO";
            String cuitEmpresa = getCuitEmpresa(contexto);
            if (cuitEmpresa.isEmpty()) {
                codigoAdhesion = getCodigoAdhesion(contexto);
                contexto.parametros.set("codigoAdhesion", codigoAdhesion);
                cuitEmpresa = getCuitEmpresa(contexto);
                if (cuitEmpresa.isEmpty()) {
                    return Respuesta.estado("ERROR_GENERANDO_RECLAMO");
                }
            }

            // agregar validacion de numeroCuenta y codigoAdhesion
            Objeto obj = new Objeto();
            obj.set("numeroCuenta", cuenta.numero());
            obj.set("codigoAdhesion", codigoAdhesion);

            if (RestPostventa.tieneSolicitudEnCurso(contexto, tipificacion, obj, true)) {
                return Respuesta.estado("SOLICITUD_EN_CURSO");
            }

            /*
             * if (ApiProducto.tieneSolicitudEnCurso(contexto, "BAJA_SERV", codigoAdhesion,
             * cuenta.numero())) { return Respuesta.estado("SOLICITUD_EN_CURSO"); }
             */

            ApiResponse responseReclamo = RestPostventa.bajaDebitoAutomatico(contexto, tipificacion, cuenta.numero(), codigoAdhesion, cuitEmpresa);

            if (responseReclamo == null || responseReclamo.hayError()) {
                return Respuesta.estado("ERROR_GENERANDO_RECLAMO");
            }

            SqlResponse sqlResponse = HBProducto.insertarReclamo(contexto, "BAJA_SERV", cuenta.numero(), codigoAdhesion, "", "");
            if (sqlResponse.hayError) {
                return Respuesta.estado("ERROR_GENERANDO_RECLAMO");
            }

            return respuesta;
        } catch (Exception e) {
            return Respuesta.error();
        }
    }

    public static Respuesta consultaRubrosDebitosAutomaticos(ContextoHB contexto) {
        Respuesta respuesta = new Respuesta();
        Integer ultimaPagina = 1;
        Integer paginaActual = 1;
        while (paginaActual <= ultimaPagina) {
            ApiResponse response = RestCatalogo.consultaRubrosDebitosAutomaticos(contexto, paginaActual);
            if (response.hayError()) {
                return Respuesta.error();
            }
            ultimaPagina = response.integer("cantidadPaginas");
            for (Objeto item : response.objetos("result")) {
                Objeto rubro = new Objeto();
                rubro.set("id", item.string("codigoRubro"));
                rubro.set("descripcion", item.string("descripcionRubro"));
                respuesta.add("rubros", rubro);
            }
            paginaActual++;
        }
        respuesta.ordenar("descripcion");
        return respuesta;
    }

    public static Respuesta consultaEmpresasDebitosAutomaticos(ContextoHB contexto) {
        String idRubro = contexto.parametros.string("idRubro");
        if (Objeto.anyEmpty(idRubro)) {
            return Respuesta.parametrosIncorrectos();
        }
        Respuesta respuesta = new Respuesta();
        Integer ultimaPagina = 1;
        Integer paginaActual = 1;

        // PRIMER LLAMADA
        Map<Integer, Futuro<ApiResponse>> mapaFuturos = new HashMap<>();
        Futuro<ApiResponse> futuroResponse = new Futuro<>(() -> RestCatalogo.consultaEmpresasDebitosAutomaticos(contexto, idRubro, 1));
        ApiResponse response = futuroResponse.get();
        if (response.hayError()) {
            return Respuesta.error();
        }
        ultimaPagina = response.integer("cantidadPaginas");
        mapaFuturos.put(paginaActual, futuroResponse);
        paginaActual++;

        // LLAMADAS SIGUIENTES
        while (paginaActual <= ultimaPagina) {
            int paginaActualFinal = paginaActual;
            Futuro<ApiResponse> futuro = new Futuro<>(() -> RestCatalogo.consultaEmpresasDebitosAutomaticos(contexto, idRubro, paginaActualFinal));
            mapaFuturos.put(paginaActual, futuro);
            paginaActual++;
        }

        paginaActual = 1;
        while (paginaActual <= ultimaPagina) {
            response = mapaFuturos.get(paginaActual).get();
            if (response.hayError()) {
                if ("84100130".equals(response.string("codigo"))) {
                    break;
                }
                return Respuesta.error();
            }
            ultimaPagina = response.integer("cantidadPaginas");
            for (Objeto item : response.objetos("result")) {
                Objeto entes = new Objeto();
                entes.set("id", item.string("codigoRubro") + "_" + item.string("cuit") + "_" + item.string("nombreEmpresa"));
                entes.set("cuit", item.string("cuit"));
                entes.set("descripcion", item.string("nombreEmpresa"));
                entes.set("nombreFantasia", item.string("nombreFantasia"));
                respuesta.add("entes", entes);
            }
            paginaActual++;
        }
        return respuesta;
    }

    public static Respuesta tycAdhesionDebitosAutomaticos(ContextoHB contexto) {
        Respuesta respuesta = new Respuesta();
        respuesta.set("texto", ConfigHB.string("tyc_adhesion_debito_automatico", ""));

        return respuesta;
    }

    public static Respuesta consultaServiciosDebitosAutomaticos(ContextoHB contexto) {
        String idEmpresa = contexto.parametros.string("idEmpresa");
        if (Objeto.anyEmpty(idEmpresa)) {
            return Respuesta.parametrosIncorrectos();
        }

        String codigoRubro = idEmpresa.split("_")[0];
        String cuitEmpresa = idEmpresa.split("_")[1];

        Respuesta respuesta = new Respuesta();
        Integer ultimaPagina = 1;
        Integer paginaActual = 1;
        while (paginaActual <= ultimaPagina) {
            ApiResponse response = RestCatalogo.consultaServiciosDebitosAutomaticos(contexto, codigoRubro, cuitEmpresa, paginaActual);
            if (response.hayError()) {
                return Respuesta.error();
            }
            ultimaPagina = response.integer("cantidadPaginas");
            for (Objeto item : response.objetos("result")) {
                Objeto servicio = new Objeto();
                servicio.set("id", item.string("codigoServicio"));
                servicio.set("descripcion", item.string("descripcionServicio"));
                servicio.set("leyenda", item.string("leyenda"));
                servicio.set("longitudClave", item.string("longitudClave"));
                respuesta.add("servicios", servicio);
            }
            paginaActual++;
        }
        return respuesta;
    }

    /* ========== PAGOS ========== */
    public static Respuesta rubrosLink(ContextoHB contexto) {
        TarjetaDebito tarjetaDebito = contexto.tarjetaDebitoPorDefecto();
        if (tarjetaDebito == null) {
            return Respuesta.estado("SIN_TARJETA_DEBITO");
        }

        ApiResponse response = PagoServicioService.linkGetRubros(contexto);
        if (response.hayError()) {
            return Respuesta.error();
        }

        Objeto rubros = new Objeto();
        boolean hayRubros = false;
        for (Objeto item : response.objetos()) {
            Objeto rubro = new Objeto();
            rubro.set("id", item.string("codigo"));
            rubro.set("descripcion", item.string("descripcion"));
            rubro.set("descripcionAbreviada", item.string("descripcionAbreviada"));
            hayRubros = true;
            rubros.add(rubro);
        }
        rubros.ordenar("descripcion");
        if (!hayRubros) {
            return Respuesta.error();
        }

        return Respuesta.exito("rubros", rubros);
    }

    public static Respuesta entesLink(ContextoHB contexto) {
        String idRubro = contexto.parametros.string("idRubro", null);

        if (Objeto.anyEmpty(idRubro)) {
            return Respuesta.parametrosIncorrectos();
        }

        TarjetaDebito tarjetaDebito = contexto.tarjetaDebitoPorDefecto();
        if (tarjetaDebito == null) {
            return Respuesta.estado("SIN_TARJETA_DEBITO");
        }

        ApiRequest request = Api.request("LinkGetEntes", "link", "GET", "/v1/servicios/{numeroTarjeta}/entes/{idRubro}", contexto);
        request.path("numeroTarjeta", tarjetaDebito.numero());
        request.path("idRubro", idRubro);

        ApiResponse response = Api.response(request, tarjetaDebito.numero(), idRubro);
        if (response.hayError()) {
            Map<String, String> mapa = new HashMap<>();
            mapa.put("107", "RUBRO_INVALIDO");
            mapa.put("112", "RUBRO_NO_HABILITADO");
            return Respuesta.estado(mapa.get(response.string("codigo")));
        }

        Objeto entes = new Objeto();
        Set<String> descripciones = new HashSet<>();
        for (Objeto item : response.objetos()) {
            if ("true".equals(item.string("isHabilitado"))) {
                String id = item.string("codigo") + "_" + (item.bool("isBaseDeuda") ? "1" : "0");
                String codigo = item.string("codigo");
                String descripcion = item.string("descripcion");
                while (descripciones.contains(descripcion)) {
                    descripcion += "*";
                }
                descripciones.add(descripcion);

                Objeto ente = new Objeto();
                ente.set("id", id);
                ente.set("codigo", codigo);
                ente.set("descripcion", descripcion);
                ente.set("descripcionMinuscula", descripcion.toLowerCase());
                entes.add(ente);
            }
        }
        entes.ordenar("descripcionMinuscula");

        return Respuesta.exito("entes", entes);
    }

    public static Respuesta agendarPagoServicio(ContextoHB contexto) {
        String idEnte = contexto.parametros.string("idEnte", null);
        String codigoLink = contexto.parametros.string("codigoLink", null);
        String descripcionEnte = contexto.parametros.string("descripcionEnte");
        String informacionAdicional = contexto.parametros.string("informacionAdicional", null);
        String csmId = contexto.parametros.string("csmId", "");
        String checksum = contexto.parametros.string("checksum", "");

        if (Objeto.anyEmpty(idEnte, codigoLink)) {
            return Respuesta.parametrosIncorrectos();
        }

        boolean esMigrado = contexto.esMigrado(contexto);

        if (esMigrado && Objeto.anyEmpty(csmId, checksum))
            return Respuesta.parametrosIncorrectos();

        TarjetaDebito tarjetaDebito = contexto.tarjetaDebitoPorDefecto();
        if (tarjetaDebito == null)
            return Respuesta.estado("SIN_TARJETA_DEBITO");

        Respuesta respuestaPausado = HBTarjetas.verificarTarjetaDebitoPausada(tarjetaDebito, contexto);
        if (respuestaPausado != null)
            return respuestaPausado;

        Objeto ente = ente(idEnte);
        boolean encontroServicio = false;
        Map<TarjetaDebito, ApiResponse> mapaTD = PagoServicioService.linkGetAdhesiones(contexto);
        for (TarjetaDebito tarjetaDebitoEnte : mapaTD.keySet()) {
            if (tarjetaDebito.activacionTemprana()) {
                continue;
            }

            ApiResponse response = mapaTD.get(tarjetaDebitoEnte);
            if (response.hayError()) {
                continue;
            }
            for (Objeto item : response.objetos()) {
                // item.string("ente.codigo")
                if (ente.string("codigo").equals(item.string("ente.codigo")) && codigoLink.equals(item.string("codigoPagoElectronico"))) {
                    encontroServicio = true;
                }
            }
        }

        Respuesta respuestaValidaTransaccion = contexto.validarTransaccion(contexto, esMigrado, "adhesion-pago", JourneyTransmitEnum.HB_INICIO_SESION);
        if (respuestaValidaTransaccion.hayError())
            return respuestaValidaTransaccion;

        ApiResponse response = PagoServicioService.linkPostAdhesion(contexto, tarjetaDebito.numero(), ente.string("codigo"), ente.bool("esBaseDeuda"), codigoLink);

        // me fijo si es track2 invalido pruebo con el resto de las tarjetas
        if (response.hayError() && "52".equals(response.string("codigo"))) {
            List<TarjetaDebito> lista = contexto.tarjetasDebito();
            ListIterator<TarjetaDebito> iter = lista.listIterator(lista.size());
            while (iter.hasPrevious()) {
                TarjetaDebito tarjetaAux = iter.previous();
                if (!tarjetaDebito.numero().equals(tarjetaAux.numero())) {
                    response = PagoServicioService.linkPostAdhesion(contexto, tarjetaAux.numero(), ente.string("codigo"), ente.bool("esBaseDeuda"), codigoLink);
                    // me quedo con esta tarjeta solo si da ok el response
                    // o si no da ok, pero dio un error distinto al 52
                    if (!response.hayError() || (response.hayError() && !"52".equals(response.string("codigo")))) {
                        tarjetaDebito = tarjetaAux;
                        break;
                    }
                }
            }
        }

        if (response.hayError() || !"true".equals(response.string("ok"))) {
            Map<String, String> mapa = new HashMap<>();
            mapa.put("66", "SIN_DEUDA_INFORMADA");
            mapa.put("82", "CODIGO_LINK_INVALIDO");
            mapa.put("118", "ENTE_INVALIDO");
            mapa.put("119", "ENTE_NO_HABILITADO");
            mapa.put("-6", "ENTE_NO_HABILITADO");
            mapa.put("97", "MAXIMA_CANTIDAD_ADHESIONES_ALCANZADA");
            mapa.put("BF", "MAXIMA_CANTIDAD_ADHESIONES_ALCANZADA");
            mapa.put("BH", "MAXIMA_CANTIDAD_ADHESIONES_ALCANZADA");
            String estado = mapa.get(response.string("codigo"));
            return estado != null ? Respuesta.estado(estado, contexto.csmIdAuth) : Respuesta.error(contexto.csmIdAuth);
        }

        if (informacionAdicional != null) {
            insertOrUpdateDescripcionAgendaPagosLink(tarjetaDebito.numero(), codigoLink, informacionAdicional);
        }

        PagoServicioService.eliminarCachePendientes(contexto);
        PagoServicioService.eliminarCacheLinkGetAdhesiones(contexto);
        contexto.limpiarSegundoFactor();

        // mando el mail de adhesión
        try {
            if (!encontroServicio) {
                Objeto parametros = new Objeto();
                parametros.set("Subject", "Adhesión de servicio a link");
                parametros.set("NOMBRE_APELLIDO", contexto.persona().nombre() + " " + contexto.persona().apellido());
                Date hoy = new Date();
                parametros.set("FECHA", new SimpleDateFormat("dd/MM/yyyy").format(hoy));
                parametros.set("HORA", new SimpleDateFormat("hh:mm").format(hoy));
                parametros.set("DESCRIPCION_SERVICIO", descripcionEnte);
                parametros.set("CANAL", "Home Banking");

                new Futuro<>(() -> RestNotificaciones.envioMail(contexto, ConfigHB.string("doppler_adhesion_link"), parametros));
            }
        } catch (Exception e) {
        }

        return Respuesta.exito().set("csmIdAuth", contexto.csmIdAuth);
    }

    public static Respuesta modificarPagoServicio(ContextoHB contexto) {
        String idAgenda = contexto.parametros.string("idAgenda", null);
        String informacionAdicional = contexto.parametros.string("informacionAdicional", null);

        if (Objeto.anyEmpty(idAgenda)) {
            return Respuesta.parametrosIncorrectos();
        }

        Objeto itemAgenda = itemAgenda(idAgenda);
        String codigoLink = itemAgenda.string("codigoLink");
        TarjetaDebito tarjetaDebito = contexto.tarjetaDebito(itemAgenda.string("idTarjetaDebito"));

        if (informacionAdicional != null) {
            SqlResponse sqlResponse = insertOrUpdateDescripcionAgendaPagosLink(tarjetaDebito.numero(), codigoLink, informacionAdicional);
            if (sqlResponse.hayError) {
                return Respuesta.error();
            }
        }

        return Respuesta.exito();
    }

    public static Respuesta desagendarPagoServicio(ContextoHB contexto) {
        String idAgenda = contexto.parametros.string("idAgenda", null);

        if (Objeto.anyEmpty(idAgenda)) {
            return Respuesta.parametrosIncorrectos();
        }

        Objeto itemAgenda = itemAgenda(idAgenda);
        TarjetaDebito tarjetaDebito = contexto.tarjetaDebito(itemAgenda.string("idTarjetaDebito"));

        ApiRequest request = Api.request("LinkDeleteAdhesiones", "link", "DELETE", "/v1/servicios/{numeroTarjeta}/adhesiones", contexto);
        request.path("numeroTarjeta", tarjetaDebito.numero());
        request.query("codigoAdhesion", itemAgenda.string("codigoAdhesion"));

        ApiResponse response = Api.response(request, tarjetaDebito.numero(), itemAgenda.string("codigoAdhesion"));
        if (response.hayError() || !"true".equals(response.string("ok"))) {
            return Respuesta.error();
        }

        PagoServicioService.eliminarCachePendientes(contexto);
        PagoServicioService.eliminarCacheLinkGetAdhesiones(contexto);
        return Respuesta.exito();
    }

    public static Respuesta pagarServicio(ContextoHB contexto) {
        String idAgenda = contexto.parametros.string("idAgenda", null);
        String idCuenta = contexto.parametros.string("idCuenta", null);
        BigDecimal importe = contexto.parametros.bigDecimal("importe");
        String codigoReferencia = contexto.parametros.string("codigoReferencia", null);

        if (Objeto.anyEmpty(idAgenda, idCuenta))
            return Respuesta.parametrosIncorrectos();

        Objeto itemAgenda = itemAgenda(idAgenda);
        TarjetaDebito tarjetaDebito = contexto.tarjetaDebito(itemAgenda.string("idTarjetaDebito"));
        Cuenta cuenta = contexto.cuenta(idCuenta);

        Respuesta respuestaPausada = HBTarjetas.verificarTarjetaDebitoPausadaEnCuenta(cuenta, contexto);
        if (respuestaPausada != null)
            return respuestaPausada;

        ApiRequest request = Api.request("LinkPostPago", "link", "POST", "/v1/servicios/{numeroTarjeta}/pagos", contexto);
        request.path("numeroTarjeta", tarjetaDebito.numero());
        request.body("idMoneda", cuenta.idMoneda());

        Objeto bodyCuenta = request.body("cuenta");
        bodyCuenta.set("numero", cuenta.numero());
        bodyCuenta.set("tipo", cuenta.idTipo());

        String referencia = codigoReferencia == null ? itemAgenda.string("codigoLink") : codigoReferencia;

        Objeto bodyPago = request.body("pago");
        bodyPago.set("codigoConcepto", itemAgenda.string("codigoConcepto"));
        bodyPago.set("codigoEnte", itemAgenda.string("codigoEnte"));
        bodyPago.set("referencia", referencia);
        bodyPago.set("usuarioLP", itemAgenda.string("codigoLink"));

        if ("V".equals(itemAgenda.string("tipoPago"))) {
            bodyPago.set("idDeuda", itemAgenda.string("idVencimiento"));
            bodyPago.set("importe", itemAgenda.bigDecimal("importe"));
        }

        if ("C".equals(itemAgenda.string("tipoPago")))
            bodyPago.set("importe", importe);

        if ("C".equals(itemAgenda.string("tipoPago")) && !"".equals(itemAgenda.string("idVencimiento")))
            bodyPago.set("idDeuda", itemAgenda.string("idVencimiento"));

        String idComprobante = "";

        boolean pagoAmexDividido = ConfigHB.bool("prendido_pago_amex_dividido", false) && Arrays.stream(ConfigHB.string("codigos_amex_pago", "").split("_"))
                .anyMatch(c -> c.equals(itemAgenda.get("codigoEnte"))) && importe.compareTo(ConfigHB.bigDecimal("amex_maximo_monto_pago")) > 0;

        if (!pagoAmexDividido) {
            // Flujo normal de pago
            String idPago = UUID.randomUUID().toString();
            bodyPago.set("identificadorPago", idPago);
            ApiResponse response = pagarServicio(contexto, importe, itemAgenda, cuenta, request, idPago, referencia);
            if (response.hayError())
                return casterRespuestaErrorPagarServicio(response.string("detalle"));
            idComprobante = getComprobante(contexto, response, "");
            eliminarCachePago(contexto);

            return Respuesta.exito("idComprobante", idComprobante);
        } else {
            // Flujo especial para pagos amex
            Respuesta respuesta = pagarServicioAmexDividido(contexto, importe, itemAgenda, cuenta, request, referencia);
            if (!respuesta.hayError())
                eliminarCachePago(contexto);

            return respuesta;
        }
    }

    public static Respuesta entesAgendadosLink(ContextoHB contexto) {
        Respuesta respuesta = new Respuesta();
        Set<String> idsEntes = new HashSet<>();

        Map<TarjetaDebito, ApiResponse> mapa = PagoServicioService.linkGetAdhesiones(contexto);
        for (TarjetaDebito tarjetaDebito : mapa.keySet()) {
            if (tarjetaDebito.activacionTemprana()) {
                continue;
            }

            ApiResponse response = mapa.get(tarjetaDebito);
            if (response.hayError()) {
                respuesta.setEstadoExistenErrores();
                continue;
            }

            Set<String> descripciones = new HashSet<>();
            for (Objeto item : response.objetos()) {
                String id = item.string("ente.codigo") + "_" + (item.bool("ente.isBaseDeuda") ? "1" : "0");
                String codigo = item.string("ente.codigo");
                String descripcion = item.string("ente.descripcion");
                while (descripciones.contains(descripcion)) {
                    descripcion += "*";
                }
                descripciones.add(descripcion);

                Objeto ente = new Objeto();
                ente.set("id", id);
                ente.set("codigo", codigo);
                ente.set("descripcion", descripcion);
                if (!idsEntes.contains(id)) {
                    idsEntes.add(id);
                    respuesta.add("entes", ente);
                }
            }
        }

        respuesta.objeto("entes").ordenar("descripcion");
        return respuesta;
    }

    public static Respuesta comprobantesPorEntePagosLink(ContextoHB contexto) {
        String idEnte = contexto.parametros.string("idEnte", null);
        String fechaDesde = contexto.parametros.date("fechaDesde", "d/M/yyyy", "ddMMyyyy", null);
        String fechaHasta = contexto.parametros.date("fechaHasta", "d/M/yyyy", "ddMMyyyy", null);

        if (Objeto.anyEmpty(idEnte, fechaDesde, fechaHasta)) {
            return Respuesta.parametrosIncorrectos();
        }

        Respuesta respuesta = new Respuesta();
        Set<String> idsAgenda = new HashSet<>();

        Map<TarjetaDebito, ApiResponse> mapa = PagoServicioService.linkGetAdhesiones(contexto);
        for (TarjetaDebito tarjetaDebito : mapa.keySet()) {
            if (tarjetaDebito.activacionTemprana()) {
                continue;
            }

            ApiResponse response = mapa.get(tarjetaDebito);
            if (response.hayError()) {
                respuesta.setEstadoExistenErrores();
                continue;
            }

            for (Objeto item : response.objetos()) {
                String idAgenda = "N";
                idAgenda += "_" + tarjetaDebito.numero();
                idAgenda += "_" + item.string("codigoAdhesion");
                idAgenda += "_" + item.string("ente.codigo");
                idAgenda += "_" + item.string("codigoPagoElectronico");

                if (ente(idEnte).string("codigo").equals(item.string("ente.codigo"))) {
                    idsAgenda.add(idAgenda);
                }
            }
        }

        List<Respuesta> lista = new CopyOnWriteArrayList<>();
        ExecutorService executorService = Concurrencia.executorService(idsAgenda);
        for (String idAgenda : idsAgenda) {
            executorService.submit(() -> {
                try {
                    // ContextoHB subcontexto = (ContextoHB) contexto.clone();
                    ContextoHB subcontexto = contexto.clonar();
                    subcontexto.parametros.set("idAgenda", idAgenda);
                    Respuesta datos = comprobantesPagoServicio(subcontexto);
                    System.out.println("datos" + datos);
                    lista.add(datos);
                } catch (Exception CloneNotSupportedException) {
                    System.out.println("###### Clone Exception ######");
                }
            });
        }
        Concurrencia.esperar(executorService, respuesta);
        Objeto historicosPago = new Objeto();
        for (Respuesta item : lista) {
            if (item.get("historicoPago") != null) {
                for (Objeto itemPago : item.objetos("historicoPago")) {
                    if (itemPago.date("fecha", "dd/MM/yyyy") != null)
                        itemPago.set("orden", Long.MAX_VALUE - itemPago.date("fecha", "dd/MM/yyyy").getTime());
                    else
                        itemPago.set("orden", Long.MAX_VALUE);
                    historicosPago.add(itemPago);
                }
            }
        }
        respuesta.set("historicoPago", historicosPago.ordenar("orden"));
        return respuesta;
    }

    public static Respuesta comprobantesPagoServicio(ContextoHB contexto) {
        String idAgenda = contexto.parametros.string("idAgenda", null);
        String fechaDesde = contexto.parametros.date("fechaDesde", "d/M/yyyy", "ddMMyyyy", null);
        String fechaHasta = contexto.parametros.date("fechaHasta", "d/M/yyyy", "ddMMyyyy", null);

        if (Objeto.anyEmpty(idAgenda, fechaDesde, fechaHasta)) {
            return Respuesta.parametrosIncorrectos();
        }

        Objeto itemAgenda = itemAgenda(idAgenda);
        TarjetaDebito tarjetaDebito = contexto.tarjetaDebito(itemAgenda.string("idTarjetaDebito"));

        if (tarjetaDebito == null) {
            return Respuesta.error();
        }

        Api.eliminarCache(contexto, "LinkGetPagos", tarjetaDebito.numero(), itemAgenda.string("codigoLink")); // elimino cache ya que lo uso cada vez que consulto un comprobante
        ApiRequest request = Api.request("LinkGetPagos", "link", "GET", "/v1/servicios/{numeroTarjeta}/pagos", contexto);
        request.path("numeroTarjeta", tarjetaDebito.numero());
        request.query("usuarioLP", itemAgenda.string("codigoLink"));
        request.query("codigoEnte", itemAgenda.string("codigoEnte"));
        request.query("paginaActual", "1");
        request.query("cantidadPagina", "100");
        request.query("fechaDesde", fechaDesde);
        request.query("fechaHasta", fechaHasta);
        request.cacheSesion = true;

        ApiResponse response = Api.response(request, tarjetaDebito.numero(), itemAgenda.string("codigoLink"));
        if (response.hayError()) {
            return Respuesta.error();
        }

        Objeto historicoPago = new Objeto();
        for (Objeto item : response.objetos("pagoRealizados")) {
            Objeto itemPago = new Objeto();
            itemPago.set("idComprobante", idAgenda + "__" + fechaDesde + "__" + fechaHasta + "__" + item.string("idDeuda") + "__" + item.string("numeroSecuencia"));
            itemPago.set("idDeuda", item.string("idDeuda"));
            itemPago.set("fecha", item.date("fecha", "yyyyMMdd", "dd/MM/yyyy"));
            itemPago.set("servicio", response.string("ente.descripcion"));
            itemPago.set("descripcion", "");
            itemPago.set("importe", item.bigDecimal("importe"));
            itemPago.set("importeFormateado", Formateador.importe(item.bigDecimal("importe")));
            historicoPago.add(itemPago);
        }

        SqlResponse sqlResponse = selectDescripcionAgendaPagosLink(contexto);
        if (sqlResponse.hayError) {
            return Respuesta.error();
        }

        for (Objeto item : historicoPago.objetos()) {
            for (Objeto registro : sqlResponse.registros) {
                if (itemAgenda.string("codigoLink").equals(registro.string("psp_codigoPago"))) {
                    item.set("descripcion", registro.string("psp_perteneceA"));
                }
            }
        }

        return Respuesta.exito("historicoPago", historicoPago);
    }

    /* ========== VEPS ========== */
    public static Respuesta tokenAfip(ContextoHB contexto) {
        String cuit = contexto.persona().cuit();
        TarjetaDebito tarjetaDebito = contexto.tarjetaDebitoPorDefecto();

        if (tarjetaDebito == null) {
            return Respuesta.estado("SIN_TARJETA_DEBITO");
        }

        Boolean prendidoFixTokenAfip = HBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_fix_token_afip");

        ApiRequest request = Api.request("LinkPostTokenAfip", prendidoFixTokenAfip ? "veps" : "link", "POST", "/v1/tokenAFIP", contexto);
        request.body("cliente").set("idTributario", cuit);
        request.body("empresa").set("idTributario", cuit);
        request.body("tarjetaDebito").set("numero", tarjetaDebito.numero());

        ApiResponse response = Api.response(request, tarjetaDebito.numero());
        if (response.hayError()) {
            return Respuesta.error();
        }

        Objeto afip = new Objeto();
        afip.set("url", response.string(prendidoFixTokenAfip ? "urlAfip" : "urlafip"));
        afip.set("sign", response.string("firma"));
        afip.set("token", response.string("token"));
        afip.set("action", response.string("accion"));

        return Respuesta.exito("afip", afip);
    }

    public static Respuesta pagarVep(ContextoHB contexto) {
        String idVep = contexto.parametros.string("idVep", "");
        String idCuenta = contexto.parametros.string("idCuenta", "");

        if (Objeto.anyEmpty(idVep)) {
            return Respuesta.parametrosIncorrectos();
        }

        String cuit = contexto.persona().cuit();
        Cuenta cuenta = contexto.cuenta(idCuenta);
        Objeto vep = vep(idVep);

        TarjetaDebito tarjetaDebito = contexto.tarjetaDebitoAsociada(cuenta);
        if (tarjetaDebito == null) {
            return Respuesta.estado("SIN_TARJETA_DEBITO");
        }

        ApiRequest request = null;

        // TODO: cuando lo pruebe tengo que tener en cuenta que en el request de la
        // nueva api piden más cosas (innecesariamente)
        // Primero tendría que probar si mandando el request como ahora anda (capaz que
        // pusieron mal el request en la documentación)
        request = Api.request("LinkPostPagoVeps", "veps", "POST", "/v1/pagoVeps", contexto);
        request.header("x-usuariocanal", ConfigHB.string("configuracion_usuario"));

        request.body("cliente").set("idTributario", cuit);
        request.body("empresa").set("idTributario", cuit);
        request.body("tarjetaDebito").set("numero", tarjetaDebito.numero());
        request.body("numeroVep", vep.string("numero"));
        request.body("contribuyente").set("idTributario", cuit);
        request.body("importe", vep.bigDecimal("importe"));
        request.body("token", vep.string("token"));
        Objeto bodyCuenta = request.body("cuenta");
        bodyCuenta.set("numero", cuenta.numero());
        bodyCuenta.set("tipo", cuenta.idTipo());
        bodyCuenta.set("descripcion", null);
        bodyCuenta.set("moneda").set("id", cuenta.idMoneda());

        ApiResponse response = null;
        try {
            response = Api.response(request, vep.string("numero"));
        } finally {
            try {
                String codigoError = response == null ? "ERROR" : response.hayError() ? response.string("codigo") : "0";

                String descripcion = "";
                if (response != null && !codigoError.equals("0")) {
                    descripcion += response.string("codigo") + ".";
                    descripcion += response.string("mensajeAlUsuario") + ".";
                }
                descripcion = descripcion.length() > 990 ? descripcion.substring(0, 990) : descripcion;

                SqlRequest sqlRequest = Sql.request("InsertAuditorPagoVep", "hbs");
                sqlRequest.sql = "INSERT INTO [hbs].[dbo].[auditor_pago_vep] ";
                sqlRequest.sql += "([momento],[cobis],[idProceso],[ip],[canal],[codigoError],[descripcionError],[numeroVep],[tarjetaDebito],[importe],[token],[cuenta],[descripcionPago],[descripcionConcepto],[periodoFiscal],[numeroTerminal],[numeroSecuencial],[codigoSeguridad],[estadoPago],[estadoVep]) ";
                sqlRequest.sql += "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                sqlRequest.add(new Date()); // momento
                sqlRequest.add(contexto.idCobis()); // cobis
                sqlRequest.add(request.idProceso()); // idProceso
                sqlRequest.add(request.ip()); // ip
                sqlRequest.add("HB"); // canal
                sqlRequest.add(codigoError); // codigoError
                sqlRequest.add(descripcion); // descripcionError
                sqlRequest.add(vep.string("numero")); // numeroVep
                sqlRequest.add(tarjetaDebito.numero()); // tarjetaDebito
                sqlRequest.add(vep.bigDecimal("importe").toString()); // importe
                sqlRequest.add(vep.string("token")); // token
                sqlRequest.add(cuenta.numero()); // cuenta
                sqlRequest.add(response != null && codigoError.equals("0") ? response.string("pagoDesc") : null); // descripcionPago
                sqlRequest.add(response != null && codigoError.equals("0") ? response.string("conceptoDesc") : null); // descripcionConcepto
                sqlRequest.add(response != null && codigoError.equals("0") ? response.string("periodoFiscal") : null); // periodoFiscal
                sqlRequest.add(response != null && codigoError.equals("0") ? response.string("pagoNroTerminal") : null); // numeroTerminal
                sqlRequest.add(response != null && codigoError.equals("0") ? response.string("pagoNroSec") : null); // numeroSecuencial
                sqlRequest.add(response != null && codigoError.equals("0") ? response.string("pagoCodSeg") : null); // codigoSeguridad
                sqlRequest.add(response != null && codigoError.equals("0") ? response.string("pagoEstado") : null); // estadoPago
                sqlRequest.add(response != null && codigoError.equals("0") ? response.string("vepEstado") : null); // estadoVep

                Sql.response(sqlRequest);
            } catch (Exception e) {
            }
        }
        if (response.hayError()) {
            return Respuesta.error();
        }

        ProductosService.eliminarCacheProductos(contexto);

        Map<String, String> comprobante = new HashMap<>();
        comprobante.put("FECHA_HORA", new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()));
        comprobante.put("ID", response.string("pagoCodSeg"));
        comprobante.put("SERVICIO", response.string("conceptoDesc"));
        comprobante.put("IMPORTE", "$ " + Formateador.importe(response.bigDecimal("importe")));
        comprobante.put("CUIT_CONTRIBUYENTE", response.string("contribuyente.idTributario"));
        comprobante.put("CUENTA", response.string("pagoTipoCuenta") + " - " + response.string("pagoNroCuenta"));
        comprobante.put("NUMERO_VEP", response.string("nroVepOriginal"));
        comprobante.put("PERIODO", response.string("periodoFiscal"));
        comprobante.put("DESCRIPCION", response.string("pagoDesc"));
        comprobante.put("TRANSACCION", response.string("pagoNroSec"));

        String idComprobante = "pago-vep" + "_" + response.string("nroVepOriginal");
        contexto.sesion.comprobantes.put(idComprobante, comprobante);
        return Respuesta.exito("idComprobante", idComprobante);

//		{
//		  "cliente": {
//		    "idTributario": "20105176512"
//		  },
//		  "empresa": {
//		    "idTributario": "20105176512"
//		  },
//		  "tarjetaDebito": {
//		    "numero": "4998590015392208"
//		  },
//		  "numeroVep": "000054569072",
//		  "contribuyente": {
//		    "idTributario": "20105176512"
//		  },
//		  "importe": 3.0,
//		  "token": "2649c706-2467-4740-b6d7-640aef7c6de5",
//		  "cuenta": {
//		    "numero": "400400011740843",
//		    "tipo": "AHO",
//		    "moneda": {
//		      "id": "80"
//		    }
//		  }
//		}

//		{
//			"fechaHoraCreacion": "2019-11-04T01:05:00.000-0300",
//			"pagoDesc": "Garantias",
//			"usuario": {
//				"idTributario": "20105176512"
//			},
//			"autorizante": {
//				"idTributario": "20105176512"
//			},
//			"contribuyente": {
//				"idTributario": "20105176512"
//			},
//			"establecimiento": "0",
//			"conceptoDesc": "ADUANA DOMICILIARIA Y FACTORIA",
//			"subconceptoDesc": "ADUANA DOMICILIARIA Y FACTORIA",
//			"periodoFiscal": "",
//			"anticipoCuota": "0",
//			"importe": 3.0,
//			"pagoBancoEmisor": "0044",
//			"pagoNroTerminal": "004400074",
//			"pagoNroSec": "044000001045",
//			"pagoCodSeg": "555",
//			"pagoFecha": "2019-04-11",
//			"pagoFechaPost": "2019-04-11",
//			"pagoHora": "150317",
//			"pagoTipoCuenta": "AHO",
//			"pagoNroCuenta": "400400011740843",
//			"pagoCodAbre": "",
//			"nroVepOriginal": "54569072",
//			"pagoEstado": "PA",
//			"vepEstado": "AL"
//		}
    }

    public static Respuesta eliminarVep(ContextoHB contexto) {
        String idVep = contexto.parametros.string("idVep", "");

        if (Objeto.anyEmpty(idVep)) {
            return Respuesta.parametrosIncorrectos();
        }

        String cuit = contexto.persona().cuit();
        String codigoVep = vep(idVep).string("numero");
        TarjetaDebito tarjetaDebito = contexto.tarjetaDebitoPorDefecto();

        ApiRequest request = null;
        request = Api.request("LinkDeleteVeps", "veps", "DELETE", "/v1/veps", contexto);
        request.header("x-usuariocanal", ConfigHB.string("configuracion_usuario"));

        request.query("idTributarioCliente", cuit);
        request.query("numeroTarjeta", tarjetaDebito.numero());
        request.query("numeroVep", codigoVep);

        ApiResponse response = Api.response(request, codigoVep);
        if (response.hayError()) {
            return Respuesta.error();
        }

        return Respuesta.exito();
    }

    public static byte[] comprobantePagoServicio(ContextoHB contexto) {
        String idComprobante = contexto.parametros.string("idComprobante");

        String idAgenda = idComprobante.split("__")[0];
        String fechaDesde = idComprobante.split("__")[1];
        String fechaHasta = idComprobante.split("__")[2];

        Objeto itemAgenda = itemAgenda(idAgenda);
        TarjetaDebito tarjetaDebito = contexto.tarjetaDebito(itemAgenda.string("idTarjetaDebito"));

        if (tarjetaDebito == null) {
            return null;
        }
        ApiRequest request = Api.request("LinkGetPagos", "link", "GET", "/v1/servicios/{numeroTarjeta}/pagos", contexto);
        request.path("numeroTarjeta", tarjetaDebito.numero());
        request.query("usuarioLP", itemAgenda.string("codigoLink"));
        request.query("codigoEnte", itemAgenda.string("codigoEnte"));
        request.query("paginaActual", "1");
        request.query("cantidadPagina", "100");
        request.query("fechaDesde", fechaDesde);
        request.query("fechaHasta", fechaHasta);
        request.cacheSesion = true;

        ApiResponse response = Api.response(request, tarjetaDebito.numero(), itemAgenda.string("codigoLink"));
        if (response.hayError()) {
            return null;
        }

        String idDeuda = idComprobante.split("__")[3];
        String nroSecuencia = "";
        if (idComprobante.split("__").length > 4) {
            nroSecuencia = idComprobante.split("__")[4];
            if (nroSecuencia == null)
                nroSecuencia = "";
        }
        boolean encontro = false;

        String sFecha = "";
        String codigoSeguridad = "";
        String servicio = "";
        BigDecimal importe = new BigDecimal(0);
        String cuota = "";
        String codigoPago = "";
        Boolean baseDeuda = false;
        for (Objeto item : response.objetos("pagoRealizados")) {
            if (idDeuda.equals(item.string("idDeuda"))) {
                if (!"".equals(nroSecuencia) && !item.string("numeroSecuencia").equals(nroSecuencia)) {
                    continue;
                }
                sFecha = item.date("fecha", "yyyyMMdd", "dd/MM/yyyy");
                servicio = response.string("ente.descripcion");
                importe = item.bigDecimal("importe");
                baseDeuda = response.bool("ente.isBaseDeuda");
                cuota = item.string("idDeuda");
                codigoPago = response.string("usuarioLP");
                codigoSeguridad = item.string("codigoSeguridad");
                encontro = true;
            }
        }
        if (!encontro) {
            return null;
        }

        Map<String, String> parametros = new HashMap<>();
        parametros.put("FECHA", sFecha);
        parametros.put("ID", codigoSeguridad);
        parametros.put("SERVICIO", servicio);
        parametros.put("IMPORTE", "$ " + Formateador.importe(importe));
        if (baseDeuda) {
            parametros.put("LABEL_CUOT", "Cuota:");
        } else {
            parametros.put("LABEL_CUOT", "Id:");
        }
        parametros.put("CUOTA", cuota);
        parametros.put("NUMERO_CLIENTE", codigoPago);

        String comprobanteImpreso = "pago-servicio" + "_" + idComprobante;
        contexto.sesion.comprobantes.put(comprobanteImpreso, parametros);
        contexto.responseHeader("Content-Type", "application/pdf; name=comprobante.pdf");
        return Pdf.generar("pago-servicio", parametros);
    }

    public static byte[] comprobantePagoVep(ContextoHB contexto) {
        String idComprobante = contexto.parametros.string("idComprobante");
        String pagina = "0";

        // VEP_2019-05-25_2019-06-25_20105176512_034000038777
        String fechaDesde = idComprobante.split("_")[1];
        String fechaHasta = idComprobante.split("_")[2];
        String cuitOriginanteVeps = idComprobante.split("_")[3];
        String nroSec = idComprobante.split("_")[4];

        TarjetaDebito tarjetaDebito = contexto.tarjetaDebitoPorDefecto();
        String cuit = cuitOriginanteVeps != null && !"".equals(cuitOriginanteVeps) ? cuitOriginanteVeps : contexto.persona().cuit();

        if (tarjetaDebito == null) {
            return null;
        }

        while (!pagina.isEmpty()) {
            ApiResponse response = vepsPagados(contexto, cuit, pagina, tarjetaDebito, fechaDesde, fechaHasta);

            if (response.codigo != 404) {
                if (response.hayError()) {
                    return null;
                }
                for (Objeto item : response.objetos("veps")) {

                    if (item.string("pagoNroSec") != null && item.string("pagoNroSec").equals(nroSec)) {

                        Map<String, String> parametros = new HashMap<>();
                        parametros.put("FECHA_HORA", item.date("pagoFecha", "yyyy-MM-dd", "dd/MM/yyyy"));
                        parametros.put("ID", item.string("pagoCodSeg"));
                        parametros.put("SERVICIO", item.string("conceptoDesc"));
                        parametros.put("IMPORTE", "$ " + Formateador.importe(item.bigDecimal("importe")));
                        parametros.put("CUIT_CONTRIBUYENTE", item.string("contribuyente.idTributario"));
                        parametros.put("CUENTA", item.string("pagoTipoCuenta") + " $ " + item.string("pagoNroCuenta"));
                        parametros.put("NUMERO_VEP", item.string("nroVepOriginal"));
                        parametros.put("PERIODO", item.string("periodoFiscal"));
                        parametros.put("DESCRIPCION", item.string("pagoDesc"));
                        parametros.put("TRANSACCION", nroSec);

                        String comprobanteImpreso = "pago-vep" + "_" + idComprobante;
                        contexto.sesion.comprobantes.put(comprobanteImpreso, parametros);
                        contexto.responseHeader("Content-Type", "application/pdf; name=comprobante.pdf");
                        return Pdf.generar("pago-vep", parametros);
                    }
                }

                if (!response.string("paginaSiguiente").equals(pagina)) {
                    pagina = response.string("paginaSiguiente");
                    continue;
                }
            }

        }
        return null;
    }

    public static Respuesta comprobantesVep(ContextoHB contexto) {
        String fechaDesde = contexto.parametros.date("fechaDesde", "d/M/yyyy", "yyyy-MM-dd", null);
        String fechaHasta = contexto.parametros.date("fechaHasta", "d/M/yyyy", "yyyy-MM-dd", null);
        String cuitOriginanteVeps = contexto.parametros.string("cuitOriginante", null);
        String pagina = "0";

        TarjetaDebito tarjetaDebito = contexto.tarjetaDebitoPorDefecto();
        String cuit = cuitOriginanteVeps != null ? cuitOriginanteVeps : contexto.persona().cuit();

        Api.eliminarCache(contexto, "LinkWindowsGetVepsPagados", cuit); // elimino cache ya que lo uso cada vez que consulto un comprobante
        Api.eliminarCache(contexto, "LinkGetVepsPagados", cuit);

        Objeto comprobantesVeps = new Objeto();

        if (tarjetaDebito == null) {
            return Respuesta.estado("SIN_TARJETA_DEBITO");
        }

        while (!pagina.isEmpty()) {

            ApiResponse response = vepsPagados(contexto, cuit, pagina, tarjetaDebito, fechaDesde, fechaHasta);

            if (response.codigo != 404) {
                if (response.hayError()) {
                    return Respuesta.error();
                }

                for (Objeto item : response.objetos("veps")) {
                    Objeto vep = new Objeto();
                    vep.set("idComprobante", "VEP_" + fechaDesde + "_" + fechaHasta + "_" + cuit + "_" + item.string("pagoNroSec"));
                    vep.set("fecha", item.date("pagoFecha", "yyyy-MM-dd", "dd/MM/yyyy"));
                    vep.set("servicio", item.string("pagoDesc"));
                    vep.set("descripcion", item.string("conceptoDesc"));
                    vep.set("importe", item.bigDecimal("importe"));
                    vep.set("importeFormateado", Formateador.importe(item.bigDecimal("importe")));
                    vep.set("pagoNumero", item.string("pagoNroSec"));
                    vep.set("pagoTipoCuenta", item.string("pagoTipoCuenta"));
                    vep.set("pagoNroCuenta", "");
                    if (!"".equals(item.string("pagoNroCuenta"))) {
                        vep.set("pagoNroCuenta", " XXXX-" + Formateador.ultimos4digitos(item.string("pagoNroCuenta")));
                    }
                    comprobantesVeps.add(vep);
                }

                if (!response.string("paginaSiguiente").equals(pagina)) {
                    pagina = response.string("paginaSiguiente");
                    continue;
                }

                break;
            } else {
                break;
            }

        }

        return Respuesta.exito("comprobantesVeps", comprobantesVeps);
    }

    public static ApiResponse vepsPagados(ContextoHB contexto, String cuit, String pagina, TarjetaDebito tarjetaDebito, String fechaDesde, String fechaHasta) {
        ApiRequest request = null;
        request = Api.request("LinkGetVepsPagados", "veps", "GET", "/v1/veps/{idTributarioCliente}/pagados", contexto);

        request.path("idTributarioCliente", cuit);
        request.query("numeroTarjeta", tarjetaDebito.numero());
        request.query("fechaDesde", fechaDesde);
        request.query("fechaHasta", fechaHasta);
        request.query("tipoConsultaLink", "1");

        if (!pagina.equals("0")) {
            request.query("pagina", pagina);
        }
        request.cacheSesion = true;

        return Api.response(request, cuit, pagina, fechaDesde, fechaHasta);
    }

    /* ========== OBJETOS ========== */
    public static Objeto ente(String id) {
        String partes[] = id.split("_");
        Objeto ente = new Objeto();
        ente.set("codigo", partes[0]);
        ente.set("esBaseDeuda", "1".equals(partes[1]));
        return ente;
    }

    public static Objeto vep(String id) {
        String partes[] = id.split("_");
        Objeto vep = new Objeto();
        vep.set("numero", partes[1]);
        vep.set("token", partes[2]);
        vep.set("importe", partes[3]);
        return vep;
    }

    public static Objeto itemAgenda(String id) {
        String partes[] = id.split("_");
        Objeto item = new Objeto();
        item.set("tipoPago", partes.length > 0 ? partes[0] : null);
        item.set("idTarjetaDebito", partes.length > 1 ? partes[1] : null);
        item.set("codigoAdhesion", partes.length > 2 ? partes[2] : null);
        item.set("codigoEnte", partes.length > 3 ? partes[3] : null);
        item.set("codigoLink", partes.length > 4 ? partes[4] : null);
        item.set("codigoConcepto", partes.length > 5 ? partes[5] : null);
        item.set("idVencimiento", partes.length > 6 ? partes[6] : null);
        item.set("importe", partes.length > 7 ? partes[7] : null);

        return item;
    }

    /* ========== SQL ========== */
    public static SqlResponse selectAllSqlHomeBanking(Objeto objetoData) {
        SqlRequest sqlRequest = Sql.request("SelectDescripcionAgendaPagosLink", "homebanking");

        String sql = "SELECT *".concat("FROM [Homebanking].[dbo].".concat(objetoData.string("from")));
        sql += " WHERE ".concat(objetoData.string("where").replace("?", " = ? "));

        sqlRequest.sql = sql;
        Objeto parameters = objetoData.objeto("parameters");
        for (String key : parameters.claves()) {
            sqlRequest.add(parameters.string(key));
        }

        return Sql.response(sqlRequest);
    }

    public static SqlResponse insertOrUpdateHomeBanking(Objeto objetoData) {
        SqlRequest sqlRequest = Sql.request("InsertOrUpdateDescripcionAgendaPagosLink", "homebanking");
        String sql = "INSERT INTO [Homebanking].[dbo].".concat(objetoData.string("insert"));
        sql += "VALUES (";

        for (String keys : objetoData.objeto("values").claves()) {
            sql += "?";
            sqlRequest.add(objetoData.objeto("values").get(keys));
        }

        sql += ")".replace("?", "?,").replace(",)", ")");
        sqlRequest.sql = sql;
        return Sql.response(sqlRequest);
    }

    public static SqlResponse selectDescripcionAgendaPagosLink(ContextoHB contexto) {
        List<TarjetaDebito> tarjetasDebito = contexto.tarjetasDebito();
        SqlRequest sqlRequest = Sql.request("SelectDescripcionAgendaPagosLink", "homebanking");
        sqlRequest.sql = "SELECT [psp_tarjetaDebito], [psp_codigoPago], [psp_perteneceA] ";
        sqlRequest.sql += "FROM [Homebanking].[dbo].[PagoServiciosPerteneceA] ";
        sqlRequest.sql += "WHERE [psp_tarjetaDebito] IN ( ";
        for (int i = 0; i < tarjetasDebito.size(); ++i) {
            sqlRequest.sql += (i > 0 ? ", " : "") + "?";
            sqlRequest.add(tarjetasDebito.get(i).numero());
        }
        sqlRequest.sql += ") ";
        return Sql.response(sqlRequest);
    }

    public static SqlResponse insertOrUpdateDescripcionAgendaPagosLink(String psp_tarjetaDebito, String psp_codigoPago, String psp_perteneceA) {
        SqlRequest sqlRequest = Sql.request("InsertOrUpdateDescripcionAgendaPagosLink", "homebanking");
        sqlRequest.sql = "UPDATE [Homebanking].[dbo].[PagoServiciosPerteneceA] ";
        sqlRequest.sql += "SET [psp_perteneceA] = ? ";
        sqlRequest.sql += "WHERE [psp_tarjetaDebito] = ? AND [psp_codigoPago] = ? ";
        sqlRequest.add(psp_perteneceA);
        sqlRequest.add(psp_tarjetaDebito);
        sqlRequest.add(psp_codigoPago);

        sqlRequest.sql += "IF @@ROWCOUNT = 0 ";
        sqlRequest.sql += "INSERT INTO [Homebanking].[dbo].[PagoServiciosPerteneceA] ([psp_tarjetaDebito], [psp_codigoPago], [psp_perteneceA]) ";
        sqlRequest.sql += "VALUES (?, ?, ?) ";
        sqlRequest.add(psp_tarjetaDebito);
        sqlRequest.add(psp_codigoPago);
        sqlRequest.add(psp_perteneceA);

        return Sql.response(sqlRequest);
    }

    private static String getCuitEmpresa(ContextoHB contexto) {
        String numeroCuenta = contexto.parametros.string("numeroCuenta");
        String codigoAdhesion = contexto.parametros.string("codigoAdhesion");
        String cuitEmpresa = "";
        try {
            Respuesta debitos = consultaDebitosAutomaticos(contexto);
            for (Objeto obj : debitos.objetos("servicios")) {
                if (obj.get("codigoAdhesion").equals(codigoAdhesion) && obj.get("numeroCuenta").equals(numeroCuenta)) {
                    cuitEmpresa = (String) obj.get("cuitEmpresa");
                    break;
                }
            }
        } catch (Exception e) {
            return "";
        }
        return cuitEmpresa;
    }

    private static String getCodigoAdhesion(ContextoHB contexto) {
        String numeroCuenta = contexto.parametros.string("numeroCuenta");
        String codigoAdhesion = contexto.parametros.string("codigoAdhesion");
        try {
            Respuesta debitos = consultaDebitosAutomaticos(contexto);
            for (Objeto obj : debitos.objetos("servicios")) {
                if (obj.get("servicio").equals(codigoAdhesion) && obj.get("numeroCuenta").equals(numeroCuenta)) {
                    codigoAdhesion = (String) obj.get("codigoAdhesion");
                    break;
                }
            }
        } catch (Exception e) {
            return "";
        }
        return codigoAdhesion;
    }

    private static void logError(ContextoHB contexto, BigDecimal importe, Objeto itemAgenda, Cuenta cuenta, ApiRequest request, String idPago, String referencia, ApiResponse response) {
        try {
            String codigoError = response == null ? "ERROR" : response.hayError() ? response.string("detalle").split("-")[0] : "0";

            String descripcion = "";
            if (response != null && !codigoError.equals("0")) {
                descripcion += response.string("codigo") + ".";
                descripcion += response.string("mensajeAlUsuario") + ".";
                descripcion += response.string("mensajeAlDesarrollador") + ".";
                descripcion += response.string("detalle") + ".";
            }
            descripcion = descripcion.length() > 990 ? descripcion.substring(0, 990) : descripcion;

            SqlRequest sqlRequest = Sql.request("InsertAuditorPagoServicio", "hbs");
            sqlRequest.sql = "INSERT INTO [hbs].[dbo].[auditor_pago_servicio] ";
            sqlRequest.sql += "([momento],[cobis],[idProceso],[ip],[canal],[codigoError],[descripcionError],[cuenta],[concepto],[ente],[idPago],[referencia],[usuarioLP],[idDeuda],[importe],[codigoSeguridad],[numeroSecuencial],[numeroTerminal],[esBaseDeuda],[esIngresoReferencia],[rubro]) ";
            sqlRequest.sql += "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            sqlRequest.add(new Date()); // momento
            sqlRequest.add(contexto.idCobis()); // cobis
            sqlRequest.add(request.idProceso()); // idProceso
            sqlRequest.add(request.ip()); // ip
            sqlRequest.add("HB"); // canal
            sqlRequest.add(codigoError); // codigoError
            sqlRequest.add(descripcion); // descripcionError
            sqlRequest.add(cuenta.numero()); // cuenta
            sqlRequest.add(itemAgenda.string("codigoConcepto")); // concepto
            sqlRequest.add(itemAgenda.string("codigoEnte")); // ente
            sqlRequest.add(idPago); // idPago
            sqlRequest.add(referencia); // referencia
            sqlRequest.add(itemAgenda.string("codigoLink")); // usuarioLP
            sqlRequest.add("V".equals(itemAgenda.string("tipoPago")) ? itemAgenda.string("idVencimiento") : null); // idDeuda
            sqlRequest.add("V".equals(itemAgenda.string("tipoPago")) ? itemAgenda.bigDecimal("importe") : importe); // importe
            sqlRequest.add(response != null && codigoError.equals("0") ? response.string("codigoSeguridad") : null); // codigoSeguridad
            sqlRequest.add(response != null && codigoError.equals("0") ? response.string("numeroSecuencial") : null); // numeroSecuencial
            sqlRequest.add(response != null && codigoError.equals("0") ? response.string("numeroTerminal") : null); // numeroTerminal
            sqlRequest.add(response != null && codigoError.equals("0") ? response.string("ente.isBaseDeuda") : null); // esBaseDeuda
            sqlRequest.add(response != null && codigoError.equals("0") ? response.string("ente.isIngresoReferencia") : null); // esIngresoReferencia
            sqlRequest.add(response != null && codigoError.equals("0") ? response.string("ente.rubro.codigo") : null); // rubro

            Sql.response(sqlRequest);
        } catch (Exception e) {
        }
    }

    private static String getEstadoErrorPago(String detalle) {
        Map<String, String> mapa = new HashMap<>();
        mapa.put("58", "SALDO_INSUFICIENTE");
        mapa.put("59", "SALDO_INSUFICIENTE");
        mapa.put("60", "CANTIDAD_MAXIMA_PAGOS_DIARIO_ALCANZADO");
        mapa.put("61", "IMPORTE_MAXIMO_SUPERADO");
        mapa.put("63", "LIMITE_DEBITOS_DIARIO_SUPERADO");
        mapa.put("64", "IMPORTE_INVALIDO");
        mapa.put("67", "IMPORTE_INVALIDO");
        return mapa.get(detalle.split("-")[0]);
    }

    private static String getComprobante(ContextoHB contexto, ApiResponse response, String numero) {
        String idComprobante = "pago-servicio_".concat(response.string("usuarioLP").trim()).concat(StringUtils.isNotBlank(numero) ? numero : "");
        setearComprobanteEnSesion(contexto, idComprobante, generarComprobante(response));
        return idComprobante;
    }

    private static void setearComprobanteEnSesion(ContextoHB contexto, String idComprobante, Map<String, String> comprobante) {
        contexto.sesion.comprobantes.put(idComprobante, comprobante);
    }

    private static Map<String, String> generarComprobante(ApiResponse response) {
        Map<String, String> comprobante = new HashMap<>();
        comprobante.put("FECHA", response.date("fechaPago", "yyMMdd", "dd/MM/yyyy"));
        comprobante.put("ID", response.string("codigoSeguridad"));
        comprobante.put("SERVICIO", response.string("ente.descripcion"));
        comprobante.put("IMPORTE", "$ " + Formateador.importe(response.bigDecimal("importe")));

        if ("".equals(response.string("pagoOriginal.idDeuda"))) {
            comprobante.put("LABEL_CUOT", response.string(""));
            comprobante.put("CUOTA", response.string(""));
        } else {
            comprobante.put("LABEL_CUOT", "Cuota:");
            comprobante.put("CUOTA", response.string("pagoOriginal.idDeuda"));
        }

        comprobante.put("NUMERO_CLIENTE", response.string("usuarioLP").trim());

        return comprobante;
    }

    private static Respuesta casterRespuestaErrorPagarServicio(String detalle) {
        String estado = getEstadoErrorPago(detalle);
        return !Objeto.empty(estado) ? Respuesta.estado(estado) : Respuesta.error();
    }

    private static ApiResponse pagarServicio(ContextoHB contexto, BigDecimal importe, Objeto itemAgenda, Cuenta cuenta, ApiRequest request, String idPago, String referencia) {
        ApiResponse response = null;
        try {
            response = Api.response(request);
        } finally {
            logError(contexto, importe, itemAgenda, cuenta, request, idPago, referencia, response);
        }
        return response;
    }

    private static Respuesta pagarServicioAmexDividido(ContextoHB contexto, BigDecimal importe, Objeto itemAgenda, Cuenta cuenta, ApiRequest request, String referencia) {
        BigDecimal importeAmexMaximo = ConfigHB.bigDecimal("amex_maximo_monto_pago");
        int cantidadPagos = importe.divide(importeAmexMaximo, RoundingMode.UP).setScale(0, RoundingMode.UP).intValue();
        boolean falloAlgunPago = false;
        boolean okAlgunPago = false;
        BigDecimal importeFaltante = new BigDecimal(0);
        BigDecimal importePagado = new BigDecimal(0);
        StringBuilder idComprobante = new StringBuilder();
        ApiResponse response;

        for (int i = 1; i <= cantidadPagos; i++) {
            BigDecimal importePagar = i != cantidadPagos ? importeAmexMaximo : importe.subtract(importePagado);

            Objeto bodyPago = request.body().objeto("pago");
            String idPago = UUID.randomUUID().toString();
            bodyPago.set("identificadorPago", idPago);
            bodyPago.set("importe", importePagar);

            response = pagarServicio(contexto, importePagar, itemAgenda, cuenta, request, idPago, referencia);
            if (response.hayError()) {
                falloAlgunPago = true;
                importeFaltante = importeFaltante.add(importePagar);
            }
            if (!response.hayError()) {
                okAlgunPago = true;
                idComprobante.append(getComprobante(contexto, response, idPago).concat(SEPARADOR_COMPROBANTE_AMEX));
            }
            importePagado = importePagado.add(importePagar);
        }

        if (okAlgunPago)
            return Respuesta.exito()
                    .set("falloAlgunPago", falloAlgunPago)
                    .set("importeFaltante", falloAlgunPago ? importeFaltante : null)
                    .set("idComprobante", idComprobante.substring(0, idComprobante.length() - SEPARADOR_COMPROBANTE_AMEX.length()));

        return Respuesta.error();
    }

    private static void eliminarCachePago(ContextoHB contexto) {
        PagoServicioService.eliminarCachePendientes(contexto);
        ProductosService.eliminarCacheProductos(contexto);
    }

}