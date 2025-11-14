package ar.com.hipotecario.mobile.api;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;

import ar.com.hipotecario.backend.servicio.api.transmit.JourneyTransmitEnum;
import ar.com.hipotecario.mobile.ConfigMB;
import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.RespuestaMB;
import ar.com.hipotecario.mobile.conector.ApiMB;
import ar.com.hipotecario.mobile.conector.ApiRequestMB;
import ar.com.hipotecario.mobile.conector.ApiResponseMB;
import ar.com.hipotecario.mobile.conector.SqlMB;
import ar.com.hipotecario.mobile.conector.SqlRequestMB;
import ar.com.hipotecario.mobile.conector.SqlResponseMB;
import ar.com.hipotecario.mobile.lib.Concurrencia;
import ar.com.hipotecario.mobile.lib.Formateador;
import ar.com.hipotecario.mobile.lib.Objeto;
import ar.com.hipotecario.mobile.lib.Pdf;
import ar.com.hipotecario.mobile.negocio.Cuenta;
import ar.com.hipotecario.mobile.negocio.TarjetaDebito;
import ar.com.hipotecario.mobile.servicio.PagoServicioService;
import ar.com.hipotecario.mobile.servicio.ProductosService;
import ar.com.hipotecario.mobile.servicio.RestCatalogo;
import ar.com.hipotecario.mobile.servicio.RestPostventa;
import org.apache.commons.lang3.StringUtils;

public class MBPago {

    private static final String SIN_TARJETA_DEBITO = "SIN_TARJETA_DEBITO";

    /* ========== CONSOLIDADA ========== */
    public static RespuestaMB consolidadaPagos(ContextoMB contexto) {
        Boolean buscarPagables = contexto.parametros.bool("buscarPagables", false);
        Boolean buscarAgendaPagos = contexto.parametros.bool("buscarAgendaPagos", false);
        Boolean buscarVeps = contexto.parametros.bool("buscarVeps", false);
        String filtrarPorCodigoEnte = contexto.parametros.string("filtrarPorEnte", null);
        String filtrarPorCodigoLink = contexto.parametros.string("filtrarPorCodigoLink", null);
        String cuitOriginanteVeps = contexto.parametros.string("cuitOriginante", null);

        List<TarjetaDebito> tarjetasDebito = contexto.tarjetasDebito();
        if (tarjetasDebito.isEmpty()) {
            return RespuestaMB.estado("SIN_TARJETA_DEBITO");
        }

        RespuestaMB respuesta = new RespuestaMB();
        Set<String> codigosPagables = new HashSet<>();

        if (buscarVeps) {
            ApiRequestMB request = ApiMB.request("LinkGetVepsPendientes", "veps", "GET", "/v1/veps/{idTributarioCliente}/pendientes", contexto);
            request.header("x-usuariocanal", ConfigMB.string("configuracion_usuario"));

            String cuit = contexto.persona().cuit();
            String numeroTarjeta = contexto.tarjetaDebitoPorDefecto().numero();

            request.path("idTributarioCliente", cuit);
            request.query("numeroTarjeta", numeroTarjeta);
            if (!Objeto.empty(cuitOriginanteVeps)) {
                if (cuitOriginanteVeps.equals(cuit))
                    cuitOriginanteVeps = null;
                else
                    request.query("idTributarioContribuyente", cuitOriginanteVeps);
            }
            request.query("idTributarioOriginante", StringUtils.isNotBlank(cuitOriginanteVeps) ? cuitOriginanteVeps : cuit);
            request.query("tipoConsultaLink", !StringUtils.isNotBlank(cuitOriginanteVeps) ? "1" : "2");

            ApiResponseMB response = ApiMB.response(request, cuit);
            if (response.hayError() && response.codigo != 404)
                respuesta.setEstadoExistenErrores();

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
            Map<TarjetaDebito, ApiResponseMB> mapaResponse = PagoServicioService.pendientes(contexto);
            for (TarjetaDebito tarjetaDebito : mapaResponse.keySet()) {

                if (tarjetaDebito.activacionTemprana()) {
                    continue;
                }

                ApiResponseMB response = mapaResponse.get(tarjetaDebito);
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

//						// emm-20200402-desde
//						// para el caso que es con vencimiento pero que desde
//						boolean esVencimientoQueIngresaImporte = false;
//						if (vencimiento != null && item.bool("ente.isIngresoImporte")) {
//							itemAgenda.set("ingresaImporte", true);
//							itemAgenda.set("tieneImporte", false);
//							id = "C" + id.substring(1);
//							itemAgenda.set("id", id);
//							esVencimientoQueIngresaImporte = true;
//						}
//						// emm-20200402-hasta

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

                        Boolean prendidoCpe = MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_link_cpe", "prendido_link_cpe_cobis");

                        Boolean agendar = true;
                        agendar &= filtrarPorCodigoEnte == null || filtrarPorCodigoEnte.equals(item.string("ente.codigo"));
                        if (!prendidoCpe) {
                            agendar &= filtrarPorCodigoLink == null || filtrarPorCodigoLink.equals(item.string("ususarioLP"));
                        } else {
                            agendar &= filtrarPorCodigoLink == null || filtrarPorCodigoLink.equals(item.string("ususarioLP")) || (filtrarPorCodigoLink.length() > 3 && filtrarPorCodigoLink.endsWith(item.string("ususarioLP"))) || (filtrarPorCodigoLink.length() > 3 && item.string("ususarioLP").endsWith(filtrarPorCodigoLink));
                        }

                        if (agendar) {
                            codigosPagables.add(item.string("ususarioLP"));
                            respuesta.add("consolidadaPago", itemAgenda.ordenar("id"));
                        }
                    }
                }
            }
        }

        if (buscarAgendaPagos) {
            Map<TarjetaDebito, ApiResponseMB> mapaResponse = PagoServicioService.linkGetAdhesiones(contexto);
            for (TarjetaDebito tarjetaDebito : mapaResponse.keySet()) {

                if (tarjetaDebito.activacionTemprana()) {
                    continue;
                }

                ApiResponseMB response = mapaResponse.get(tarjetaDebito);
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
                        if ("09".equals(item.string("ente.rubro.codigo"))) {
                            if (!"938".equals(item.string("ente.codigo"))) {
                                itemAgenda.set("tarjetaCredito", "XXXX-" + Formateador.ultimos4digitos(item.string("codigoPagoElectronico", "")));
                            }
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
            SqlResponseMB sqlResponse = selectDescripcionAgendaPagosLink(contexto);
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
    public static RespuestaMB comprobantesPendientesVepPorCuit(ContextoMB contexto) {
        String cuitOriginanteVeps = contexto.parametros.string("cuitOriginante", null);

        if (Objeto.empty(contexto.idCobis()))
            return RespuestaMB.sinPseudoSesion();

        TarjetaDebito tarjetaDebitoPorDefecto = contexto.tarjetaDebitoPorDefecto();
        if (Objeto.empty(tarjetaDebitoPorDefecto))
            return RespuestaMB.estado(SIN_TARJETA_DEBITO);

        String cuit = contexto.persona().cuit();

        RespuestaMB respuesta = new RespuestaMB();
        ApiRequestMB request = ApiMB.request("LinkGetVepsPendientes", "veps", "GET", "/v1/veps/{idTributarioCliente}/pendientes", contexto);
        request.header("x-usuariocanal", ConfigMB.string("configuracion_usuario"));

        request.path("idTributarioCliente", cuit);
        request.query("numeroTarjeta", tarjetaDebitoPorDefecto.numero());
        if (!Objeto.empty(cuitOriginanteVeps)) {
            if (cuitOriginanteVeps.equals(cuit))
                cuitOriginanteVeps = null;
            else
                request.query("idTributarioContribuyente", cuitOriginanteVeps);
        }
        request.query("idTributarioOriginante", StringUtils.isNotBlank(cuitOriginanteVeps) ? cuitOriginanteVeps : cuit);
        request.query("tipoConsultaLink", !StringUtils.isNotBlank(cuitOriginanteVeps) ? "1" : "2");

        ApiResponseMB response = ApiMB.response(request, cuit);
        if (response.hayError() && response.codigo != 404)
            return RespuestaMB.existenErrores();

        if (!response.hayError()) {
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

        respuesta.objeto("consolidadaPago").ordenar("orden", "fechaVencimiento", "servicio", "_bigdecimal_importe");
        return respuesta;
    }

    /* ========== PAGOS ========== */
    public static RespuestaMB rubrosLink(ContextoMB contexto) {
        TarjetaDebito tarjetaDebito = contexto.tarjetaDebitoPorDefecto();
        if (tarjetaDebito == null) {
            return RespuestaMB.estado("SIN_TARJETA_DEBITO");
        }

        ApiResponseMB response = PagoServicioService.linkGetRubros(contexto);
        if (response.hayError()) {
            return RespuestaMB.error();
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
            return RespuestaMB.error();
        }

        return RespuestaMB.exito("rubros", rubros);
    }

    public static RespuestaMB entesLink(ContextoMB contexto) {
        String idRubro = contexto.parametros.string("idRubro", null);
        if (contexto.idCobis() == null) {
            return RespuestaMB.estado("SIN_PSEUDO_SESION");
        }
        if (Objeto.anyEmpty(idRubro)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        TarjetaDebito tarjetaDebito = contexto.tarjetaDebitoPorDefecto();
        if (tarjetaDebito == null) {
            return RespuestaMB.estado("SIN_TARJETA_DEBITO");
        }

        ApiRequestMB request = ApiMB.request("LinkGetEntes", "link", "GET", "/v1/servicios/{numeroTarjeta}/entes/{idRubro}", contexto);
        request.path("numeroTarjeta", tarjetaDebito.numero());
        request.path("idRubro", idRubro);

        ApiResponseMB response = ApiMB.response(request, tarjetaDebito.numero(), idRubro);
        if (response.hayError()) {
            Map<String, String> mapa = new HashMap<>();
            mapa.put("107", "RUBRO_INVALIDO");
            mapa.put("112", "RUBRO_NO_HABILITADO");
            return RespuestaMB.estado(mapa.get(response.string("codigo")));
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

        return RespuestaMB.exito("entes", entes);
    }

    public static RespuestaMB agendarPagoServicio(ContextoMB contexto) {
        String idEnte = contexto.parametros.string("idEnte", null);
        String codigoLink = contexto.parametros.string("codigoLink", null);
        String informacionAdicional = contexto.parametros.string("informacionAdicional", null);
        String csmId = contexto.parametros.string("csmId", "");
        String checksum = contexto.parametros.string("checksum", "");

        if (Objeto.anyEmpty(idEnte, codigoLink)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        boolean esMigrado = contexto.esMigrado(contexto);

        if (esMigrado && Objeto.anyEmpty(csmId, checksum))
            return RespuestaMB.parametrosIncorrectos();

        RespuestaMB respuestaValidaTransaccion = contexto.validarTransaccion(contexto, esMigrado, "adhesion-pago", JourneyTransmitEnum.MB_INICIO_SESION);
        if (respuestaValidaTransaccion.hayError())
            return respuestaValidaTransaccion;

        TarjetaDebito tarjetaDebito = contexto.tarjetaDebitoPorDefecto();
        if (tarjetaDebito == null)
            return RespuestaMB.estado("SIN_TARJETA_DEBITO");

        RespuestaMB respuestaPausada = MBTarjetas.verificarTarjetaDebitoPausada(tarjetaDebito, contexto);
        if (respuestaPausada != null)
            return respuestaPausada;

        Objeto ente = ente(idEnte);

        ApiResponseMB response = PagoServicioService.linkPostAdhesion(contexto, tarjetaDebito.numero(), ente.string("codigo"), ente.bool("esBaseDeuda"), codigoLink);

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
            return estado != null ? RespuestaMB.estado(estado) : RespuestaMB.error();
        }

        if (informacionAdicional != null) {
            insertOrUpdateDescripcionAgendaPagosLink(tarjetaDebito.numero(), codigoLink, informacionAdicional);
        }

        PagoServicioService.eliminarCachePendientes(contexto);
        PagoServicioService.eliminarCacheLinkGetAdhesiones(contexto);
        contexto.limpiarSegundoFactor();
        return RespuestaMB.exito();
    }

    public static RespuestaMB modificarPagoServicio(ContextoMB contexto) {
        String idAgenda = contexto.parametros.string("idAgenda", null);
        String informacionAdicional = contexto.parametros.string("informacionAdicional", null);

        if (Objeto.anyEmpty(idAgenda)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        Objeto itemAgenda = itemAgenda(idAgenda);
        String codigoLink = itemAgenda.string("codigoLink");
        TarjetaDebito tarjetaDebito = contexto.tarjetaDebito(itemAgenda.string("idTarjetaDebito"));

        if (informacionAdicional != null) {
            SqlResponseMB sqlResponse = insertOrUpdateDescripcionAgendaPagosLink(tarjetaDebito.numero(), codigoLink, informacionAdicional);
            if (sqlResponse.hayError) {
                return RespuestaMB.error();
            }
        }

        return RespuestaMB.exito();
    }

    public static RespuestaMB desagendarPagoServicio(ContextoMB contexto) {
        String idAgenda = contexto.parametros.string("idAgenda", null);

        if (Objeto.anyEmpty(idAgenda)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        Objeto itemAgenda = itemAgenda(idAgenda);
        TarjetaDebito tarjetaDebito = contexto.tarjetaDebito(itemAgenda.string("idTarjetaDebito"));

        ApiRequestMB request = ApiMB.request("LinkDeleteAdhesiones", "link", "DELETE", "/v1/servicios/{numeroTarjeta}/adhesiones", contexto);
        request.path("numeroTarjeta", tarjetaDebito.numero());
        request.query("codigoAdhesion", itemAgenda.string("codigoAdhesion"));

        ApiResponseMB response = ApiMB.response(request, tarjetaDebito.numero(), itemAgenda.string("codigoAdhesion"));
        if (response.hayError() || !"true".equals(response.string("ok"))) {
            return RespuestaMB.error();
        }

        PagoServicioService.eliminarCachePendientes(contexto);
        PagoServicioService.eliminarCacheLinkGetAdhesiones(contexto);
        return RespuestaMB.exito();
    }

    // Migrado 19/9/22
    public static RespuestaMB consultaRubrosDebitosAutomaticos(ContextoMB contexto) {
        RespuestaMB respuesta = new RespuestaMB();
        Integer ultimaPagina = 1;
        Integer paginaActual = 1;
        while (paginaActual <= ultimaPagina) {
            ApiResponseMB response = RestCatalogo.consultaRubrosDebitosAutomaticos(contexto, paginaActual);
            if (response.hayError()) {
                return RespuestaMB.error();
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

    public static RespuestaMB consultaEmpresasDebitosAutomaticos(ContextoMB contexto) {
        String idRubro = contexto.parametros.string("idRubro");
        if (Objeto.anyEmpty(idRubro)) {
            return RespuestaMB.parametrosIncorrectos();
        }
        RespuestaMB respuesta = new RespuestaMB();
        Integer ultimaPagina = 1;
        Integer paginaActual = 1;
        while (paginaActual <= ultimaPagina) {
            ApiResponseMB response = RestCatalogo.consultaEmpresasDebitosAutomaticos(contexto, idRubro, paginaActual);
            if (response.hayError()) {
                if ("84100130".equals(response.string("codigo"))) {
                    break;
                }
                return RespuestaMB.error();
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

    public static RespuestaMB consultaServiciosDebitosAutomaticos(ContextoMB contexto) {
        String idEmpresa = contexto.parametros.string("idEmpresa");
        if (Objeto.anyEmpty(idEmpresa)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        String codigoRubro = idEmpresa.split("_")[0];
        String cuitEmpresa = idEmpresa.split("_")[1];

        RespuestaMB respuesta = new RespuestaMB();
        Integer ultimaPagina = 1;
        Integer paginaActual = 1;
        while (paginaActual <= ultimaPagina) {
            ApiResponseMB response = RestCatalogo.consultaServiciosDebitosAutomaticos(contexto, codigoRubro, cuitEmpresa, paginaActual);
            if (response.hayError()) {
                return RespuestaMB.error();
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

    public static RespuestaMB tycAdhesionDebitosAutomaticos(ContextoMB contexto) {
        RespuestaMB respuesta = new RespuestaMB();
        respuesta.set("texto", ConfigMB.string("tyc_adhesion_debito_automatico", ""));

        return respuesta;
    }

    public static RespuestaMB agregarDebitoAutomatico(ContextoMB contexto) {
        String numeroCuenta = contexto.parametros.string("numeroCuenta");
        String codigoAdhesion = contexto.parametros.string("codigoAdhesion");
        String descripcionAdhesion = contexto.parametros.string("descripcionAdhesion");
        String servicio = contexto.parametros.string("servicio");
        String csmId = contexto.parametros.string("csmId", "");
        String checksum = contexto.parametros.string("checksum", "");

        if (Objeto.anyEmpty(numeroCuenta, codigoAdhesion, descripcionAdhesion, servicio)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        boolean esMigrado = contexto.esMigrado(contexto);

        if (esMigrado && Objeto.anyEmpty(csmId, checksum))
            return RespuestaMB.parametrosIncorrectos();

        RespuestaMB respuestaValidaTransaccion = contexto.validarTransaccion(contexto, esMigrado, "adhesion-pago", JourneyTransmitEnum.MB_INICIO_SESION);
        if (respuestaValidaTransaccion.hayError())
            return respuestaValidaTransaccion;

        Cuenta cuenta = contexto.cuenta(numeroCuenta);
        if (cuenta == null) {
            return RespuestaMB.error();
        }

        TarjetaDebito tarjetaDebito = contexto.tarjetaDebitoAsociada(cuenta);
        if (tarjetaDebito == null) {
            return RespuestaMB.estado("SIN_TARJETA_DEBITO");
        }

        RespuestaMB respuesta = new RespuestaMB();

        ApiRequestMB request = ApiMB.request("CuentasDebitosAutomaticosAlta", "cuentas", "POST", "/v1/debitoautomatico", contexto);

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

        ApiResponseMB response = ApiMB.response(request, cuenta.id());
        if (response.hayError()) {
            return RespuestaMB.error();
        }

        return respuesta.set("csmIdAuth",contexto.csmIdAuth);
    }

    public static RespuestaMB pagarServicio(ContextoMB contexto) {
        String idAgenda = contexto.parametros.string("idAgenda", null);
        String idCuenta = contexto.parametros.string("idCuenta", null);
        BigDecimal importe = contexto.parametros.bigDecimal("importe");
        String codigoReferencia = contexto.parametros.string("codigoReferencia", null);

        if (Objeto.anyEmpty(idAgenda, idCuenta)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        Objeto itemAgenda = itemAgenda(idAgenda);
        TarjetaDebito tarjetaDebito = contexto.tarjetaDebito(itemAgenda.string("idTarjetaDebito"));
        Cuenta cuenta = contexto.cuenta(idCuenta);

        RespuestaMB respuestaPausada = MBTarjetas.verificarTarjetaDebitoPausadaEnCuenta(cuenta, contexto);
        if (respuestaPausada != null)
            return respuestaPausada;

        ApiRequestMB request = ApiMB.request("LinkPostPago", "link", "POST", "/v1/servicios/{numeroTarjeta}/pagos", contexto);
        request.path("numeroTarjeta", tarjetaDebito.numero());
        request.body("idMoneda", cuenta.idMoneda());

        Objeto bodyCuenta = request.body("cuenta");
        bodyCuenta.set("numero", cuenta.numero());
        bodyCuenta.set("tipo", cuenta.idTipo());

        String idPago = UUID.randomUUID().toString();
        String referencia = codigoReferencia == null ? itemAgenda.string("codigoLink") : codigoReferencia;

        Objeto bodyPago = request.body("pago");
        bodyPago.set("codigoConcepto", itemAgenda.string("codigoConcepto"));
        bodyPago.set("codigoEnte", itemAgenda.string("codigoEnte"));
        bodyPago.set("identificadorPago", idPago);
        bodyPago.set("referencia", referencia);
        bodyPago.set("usuarioLP", itemAgenda.string("codigoLink"));

//		boolean ingresarCpe = false;
        if ("V".equals(itemAgenda.string("tipoPago"))) {
            bodyPago.set("idDeuda", itemAgenda.string("idVencimiento"));
            bodyPago.set("importe", itemAgenda.bigDecimal("importe"));
//			ingresarCpe = true;
        }

        if ("C".equals(itemAgenda.string("tipoPago"))) {
            bodyPago.set("importe", importe);
        }

        if ("C".equals(itemAgenda.string("tipoPago")) && !"".equals(itemAgenda.string("idVencimiento"))) {
            bodyPago.set("idDeuda", itemAgenda.string("idVencimiento"));
//			ingresarCpe = true;
        }

//		if (ingresarCpe) {
//			if (ApiAplicacion.funcionalidadPrendida(
//					contexto.idCobis(),
//					"prendido_link_cpe", 
//					"prendido_link_cpe_cobis")
//			) {
//				request.query("validarcpe", "true");
//				request.body("idEntidadEmisora", "0044");
//				request.body("tipoTerminal", "74");
//				request.body("cpe", itemAgenda.string("codigoLink"));
//			}
//		}

        ApiResponseMB response = null;
        try {
            // if (1==1)
            // throw new RuntimeException();
            // return Respuesta.error();

            response = ApiMB.response(request);

        } finally {
            if (ConfigMB.bool("log_transaccional", false)) {
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

                    SqlRequestMB sqlRequest = SqlMB.request("InsertAuditorPagoServicio", "hbs");
                    sqlRequest.sql = "INSERT INTO [hbs].[dbo].[auditor_pago_servicio] ";
                    sqlRequest.sql += "([momento],[cobis],[idProceso],[ip],[canal],[codigoError],[descripcionError],[cuenta],[concepto],[ente],[idPago],[referencia],[usuarioLP],[idDeuda],[importe],[codigoSeguridad],[numeroSecuencial],[numeroTerminal],[esBaseDeuda],[esIngresoReferencia],[rubro]) ";
                    sqlRequest.sql += "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                    sqlRequest.add(new Date()); // momento
                    sqlRequest.add(contexto.idCobis()); // cobis
                    sqlRequest.add(request.idProceso()); // idProceso
                    sqlRequest.add(request.ip()); // ip
                    sqlRequest.add("MB"); // canal
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
                    sqlRequest.add(codigoError.equals("0") ? response.string("codigoSeguridad") : null); // codigoSeguridad
                    sqlRequest.add(codigoError.equals("0") ? response.string("numeroSecuencial") : null); // numeroSecuencial
                    sqlRequest.add(codigoError.equals("0") ? response.string("numeroTerminal") : null); // numeroTerminal
                    sqlRequest.add(codigoError.equals("0") ? response.string("ente.isBaseDeuda") : null); // esBaseDeuda
                    sqlRequest.add(codigoError.equals("0") ? response.string("ente.isIngresoReferencia") : null); // esIngresoReferencia
                    sqlRequest.add(codigoError.equals("0") ? response.string("ente.rubro.codigo") : null); // rubro

                    SqlMB.response(sqlRequest);
                } catch (Exception e) {
                }
            }
        }
        if (response.hayError()) {
            Map<String, String> mapa = new HashMap<>();
            mapa.put("58", "SALDO_INSUFICIENTE");
            mapa.put("59", "SALDO_INSUFICIENTE");
            mapa.put("60", "CANTIDAD_MAXIMA_PAGOS_DIARIO_ALCANZADO");
            mapa.put("61", "IMPORTE_MAXIMO_SUPERADO");
            mapa.put("63", "LIMITE_DEBITOS_DIARIO_SUPERADO");
            mapa.put("64", "IMPORTE_INVALIDO");
            mapa.put("67", "IMPORTE_INVALIDO");
            String estado = mapa.get(response.string("detalle").split("-")[0]);
            return estado != null ? RespuestaMB.estado(estado) : RespuestaMB.error();
        }

        PagoServicioService.eliminarCachePendientes(contexto);
        ProductosService.eliminarCacheProductos(contexto);

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

        String idComprobante = "pago-servicio" + "_" + response.string("usuarioLP").trim();
        contexto.sesion().setComprobante(idComprobante, comprobante);
        return RespuestaMB.exito("idComprobante", idComprobante);

//		{
//		  "idMoneda": "80",
//		  "cuenta": {
//		    "numero": "400400011740843",
//		    "tipo": "AHO"
//		  },
//		  "pago": {
//		    "codigoConcepto": "001",
//		    "codigoEnte": "901",
//		    "identificadorPago": "68b3c6c0-50cb-46d8-8932-cc3c93dc7f58",
//		    "referencia": "5010361347380100",
//		    "usuarioLP": "5010361738000408",
//		    "importe": 0.01
//		  }
//		}

//		{
//		  "pagoOriginal" : {
//		    "usuarioLP" : "5010361738000408",
//		    "codigoEnte" : "901",
//		    "importe" : 0.01,
//		    "idDeuda" : null,
//		    "referencia" : "5010361347380100",
//		    "codigoConcepto" : "001",
//		    "identificadorPago" : "68b3c6c0-50cb-46d8-8932-cc3c93dc7f58"
//		  },
//		  "usuarioLP" : "5010361738000408   ",
//		  "fechaPago" : "190411",
//		  "horaPago" : "155628",
//		  "codigoSeguridad" : "343",
//		  "importe" : 0.01,
//		  "numeroSecuencial" : "00415669",
//		  "numeroTerminal" : "003400074       ",
//		  "fechaRendicion" : "190411",
//		  "ente" : {
//		    "codigo" : "901",
//		    "descripcion" : "Mastercard",
//		    "isBaseDeuda" : false,
//		    "isMultipleConcepto" : false,
//		    "isIngresoReferencia" : false,
//		    "isIngresoImporte" : true,
//		    "isHabilitado" : true,
//		    "rubro" : {
//		      "codigo" : "09",
//		      "descripcion" : "Tarjetas de Crédito",
//		      "descripcionAbreviada" : "TARJ CREDITO"
//		    }
//		  },
//		  "concepto" : {
//		    "codigo" : "001",
//		    "descripcion" : "Cuota",
//		    "tipoPago" : "PAGO_INDISTINTO",
//		    "isIngresoReferencia" : false,
//		    "isLongitudReferencia" : false,
//		    "longitudMinimaTextoReferencia" : 0,
//		    "longitudMaximaTextoReferencia" : 0,
//		    "importeFijo" : 0.00,
//		    "importeMinimo" : 0.00,
//		    "importeMaximo" : 0.00,
//		    "ingresoImportes" : true,
//		    "rango" : false
//		  },
//		  "lineasTicket" : [ "PAGO DE ARGEN MASTERCARD                ", "NRO. DE CLIENTE: 5010361738000408       ", "                                        ", "CON DEBITO EN: CAJA DE AHORRO EN PESOS  ", "NRO DE CUENTA: 400400011740843          ", "                                        ", "IMPORTE:                   $        0,01", "                                        ", "   ESTE RECIBO ES CONSTANCIA DE PAGO    " ]
//		}
    }

    public static RespuestaMB entesAgendadosLink(ContextoMB contexto) {
        RespuestaMB respuesta = new RespuestaMB();
        Set<String> idsEntes = new HashSet<>();

        Map<TarjetaDebito, ApiResponseMB> mapa = PagoServicioService.linkGetAdhesiones(contexto);
        for (TarjetaDebito tarjetaDebito : mapa.keySet()) {

            if (tarjetaDebito.activacionTemprana()) {
                continue;
            }

            ApiResponseMB response = mapa.get(tarjetaDebito);
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

    public static RespuestaMB comprobantesPorEntePagosLink(ContextoMB contexto) {
        String idEnte = contexto.parametros.string("idEnte", null);
        String fechaDesde = contexto.parametros.date("fechaDesde", "d/M/yyyy", "ddMMyyyy", null);
        String fechaHasta = contexto.parametros.date("fechaHasta", "d/M/yyyy", "ddMMyyyy", null);

        if (Objeto.anyEmpty(idEnte, fechaDesde, fechaHasta)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        RespuestaMB respuesta = new RespuestaMB();
        Set<String> idsAgenda = new HashSet<>();

        Map<TarjetaDebito, ApiResponseMB> mapa = PagoServicioService.linkGetAdhesiones(contexto);
        for (TarjetaDebito tarjetaDebito : mapa.keySet()) {

            if (tarjetaDebito.activacionTemprana()) {
                continue;
            }

            ApiResponseMB response = mapa.get(tarjetaDebito);
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

        List<RespuestaMB> lista = new CopyOnWriteArrayList<>();
        ExecutorService executorService = Concurrencia.executorService(idsAgenda);
        for (String idAgenda : idsAgenda) {
            executorService.submit(() -> {
                ContextoMB subcontexto = contexto.clonar();
                subcontexto.parametros.set("idAgenda", idAgenda);
                RespuestaMB datos = comprobantesPagoServicio(subcontexto);
                lista.add(datos);
//				respuesta.unir(datos);
            });
        }
        Concurrencia.esperar(executorService, respuesta);
        Objeto historicosPago = new Objeto();
        for (RespuestaMB item : lista) {
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

    public static RespuestaMB comprobantesPagoServicio(ContextoMB contexto) {
        String idAgenda = contexto.parametros.string("idAgenda", null);
        String fechaDesde = contexto.parametros.date("fechaDesde", "d/M/yyyy", "ddMMyyyy", null);
        String fechaHasta = contexto.parametros.date("fechaHasta", "d/M/yyyy", "ddMMyyyy", null);

        if (Objeto.anyEmpty(idAgenda, fechaDesde, fechaHasta)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        Objeto itemAgenda = itemAgenda(idAgenda);
        TarjetaDebito tarjetaDebito = contexto.tarjetaDebito(itemAgenda.string("idTarjetaDebito"));

        if (tarjetaDebito == null) {
            return RespuestaMB.error();
        }

        ApiMB.eliminarCache(contexto, "LinkGetPagos", tarjetaDebito.numero(), itemAgenda.string("codigoLink")); // elimino cache ya que lo uso cada vez que consulto un comprobante
        ApiRequestMB request = ApiMB.request("LinkGetPagos", "link", "GET", "/v1/servicios/{numeroTarjeta}/pagos", contexto);
        request.path("numeroTarjeta", tarjetaDebito.numero());
        request.query("usuarioLP", itemAgenda.string("codigoLink"));
        request.query("codigoEnte", itemAgenda.string("codigoEnte"));
        request.query("paginaActual", "1");
        request.query("cantidadPagina", "100");
        request.query("fechaDesde", fechaDesde);
        request.query("fechaHasta", fechaHasta);
        request.cacheSesion = true;

        ApiResponseMB response = ApiMB.response(request, tarjetaDebito.numero(), itemAgenda.string("codigoLink"));
        if (response.hayError()) {
            return RespuestaMB.error();
        }

        Objeto historicoPago = new Objeto();
        for (Objeto item : response.objetos("pagoRealizados")) {
            Objeto itemPago = new Objeto();
            itemPago.set("idComprobante", idAgenda + "__" + fechaDesde + "__" + fechaHasta + "__" + item.string("idDeuda"));
            itemPago.set("idDeuda", item.string("idDeuda"));
            itemPago.set("fecha", item.date("fecha", "yyyyMMdd", "dd/MM/yyyy"));
            itemPago.set("servicio", response.string("ente.descripcion"));
            itemPago.set("descripcion", "");
            itemPago.set("importe", item.bigDecimal("importe"));
            itemPago.set("importeFormateado", Formateador.importe(item.bigDecimal("importe")));
            itemPago.set("textoLegal", ConfigMB.string("mensaje_texto_legal_pago_servicios"));
            historicoPago.add(itemPago);
        }

        SqlResponseMB sqlResponse = selectDescripcionAgendaPagosLink(contexto);
        if (sqlResponse.hayError) {
            return RespuestaMB.error();
        }

        for (Objeto item : historicoPago.objetos()) {
            for (Objeto registro : sqlResponse.registros) {
                if (itemAgenda.string("codigoLink").equals(registro.string("psp_codigoPago"))) {
                    item.set("descripcion", registro.string("psp_perteneceA"));
                }
            }
        }

        return RespuestaMB.exito("historicoPago", historicoPago);
    }

    /* ========== VEPS ========== */
    public static RespuestaMB tokenAfip(ContextoMB contexto) {
        String cuit = contexto.persona().cuit();
        TarjetaDebito tarjetaDebito = contexto.tarjetaDebitoPorDefecto();

        if (tarjetaDebito == null)
            return RespuestaMB.estado("SIN_TARJETA_DEBITO");

        ApiRequestMB request = ApiMB.request("LinkPostTokenAfip", "veps", "POST", "/v1/tokenAFIP", contexto);
        request.body("cliente").set("idTributario", cuit);
        request.body("empresa").set("idTributario", cuit);
        request.body("tarjetaDebito").set("numero", tarjetaDebito.numero());

        ApiResponseMB response = ApiMB.response(request, tarjetaDebito.numero());
        if (response.hayError())
            return RespuestaMB.error();

        Objeto afip = new Objeto();
        afip.set("url", response.string("urlAfip"));
        afip.set("sign", response.string("firma"));
        afip.set("token", response.string("token"));
        afip.set("action", response.string("accion"));

        return RespuestaMB.exito("afip", afip);
    }

    public static RespuestaMB pagarVep(ContextoMB contexto) {
        String idVep = contexto.parametros.string("idVep", "");
        String idCuenta = contexto.parametros.string("idCuenta", "");

        if (Objeto.anyEmpty(idVep))
            return RespuestaMB.parametrosIncorrectos();

        if (Objeto.empty(contexto.idCobis()))
            return RespuestaMB.sinPseudoSesion();

        String cuit = contexto.persona().cuit();
        Cuenta cuenta = contexto.cuenta(idCuenta);
        Objeto vep = vep(idVep);

        TarjetaDebito tarjetaDebito = contexto.tarjetaDebitoAsociada(cuenta);
        if (Objeto.anyEmpty(tarjetaDebito))
            return RespuestaMB.estado(SIN_TARJETA_DEBITO);

        ApiRequestMB request = ApiMB.request("LinkPostPagoVeps", "veps", "POST", "/v1/pagoVeps", contexto);

        request.header("x-usuariocanal", ConfigMB.string("configuracion_usuario"));
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

        ApiResponseMB response = null;
        try {
            response = ApiMB.response(request, vep.string("numero"));
        } finally {
            if (ConfigMB.bool("log_transaccional", false)) {
                try {
                    String codigoError = response == null ? "ERROR" : response.hayError() ? response.string("codigo") : "0";

                    String descripcion = "";
                    if (response != null && !codigoError.equals("0")) {
                        descripcion += response.string("codigo") + ".";
                        descripcion += response.string("mensajeAlUsuario") + ".";
                    }
                    descripcion = descripcion.length() > 990 ? descripcion.substring(0, 990) : descripcion;

                    SqlRequestMB sqlRequest = SqlMB.request("InsertAuditorPagoVep", "hbs");
                    sqlRequest.sql = "INSERT INTO [hbs].[dbo].[auditor_pago_vep] ";
                    sqlRequest.sql += "([momento],[cobis],[idProceso],[ip],[canal],[codigoError],[descripcionError],[numeroVep],[tarjetaDebito],[importe],[token],[cuenta],[descripcionPago],[descripcionConcepto],[periodoFiscal],[numeroTerminal],[numeroSecuencial],[codigoSeguridad],[estadoPago],[estadoVep]) ";
                    sqlRequest.sql += "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                    sqlRequest.add(new Date()); // momento
                    sqlRequest.add(contexto.idCobis()); // cobis
                    sqlRequest.add(request.idProceso()); // idProceso
                    sqlRequest.add(request.ip()); // ip
                    sqlRequest.add("MB"); // canal
                    sqlRequest.add(codigoError); // codigoError
                    sqlRequest.add(descripcion); // descripcionError
                    sqlRequest.add(vep.string("numero")); // numeroVep
                    sqlRequest.add(tarjetaDebito.numero()); // tarjetaDebito
                    sqlRequest.add(vep.bigDecimal("importe").toString()); // importe
                    sqlRequest.add(vep.string("token")); // token
                    sqlRequest.add(cuenta.numero()); // cuenta
                    sqlRequest.add(codigoError.equals("0") ? response.string("pagoDesc") : null); // descripcionPago
                    sqlRequest.add(codigoError.equals("0") ? response.string("conceptoDesc") : null); // descripcionConcepto
                    sqlRequest.add(codigoError.equals("0") ? response.string("periodoFiscal") : null); // periodoFiscal
                    sqlRequest.add(codigoError.equals("0") ? response.string("pagoNroTerminal") : null); // numeroTerminal
                    sqlRequest.add(codigoError.equals("0") ? response.string("pagoNroSec") : null); // numeroSecuencial
                    sqlRequest.add(codigoError.equals("0") ? response.string("pagoCodSeg") : null); // codigoSeguridad
                    sqlRequest.add(codigoError.equals("0") ? response.string("pagoEstado") : null); // estadoPago
                    sqlRequest.add(codigoError.equals("0") ? response.string("vepEstado") : null); // estadoVep

                    SqlMB.response(sqlRequest);
                } catch (Exception e) {
                }
            }
        }
        if (response.hayError())
            return RespuestaMB.error();

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
        contexto.sesion().setComprobante(idComprobante, comprobante);
        return RespuestaMB.exito("idComprobante", idComprobante);
    }

    public static RespuestaMB eliminarVep(ContextoMB contexto) {
        String idVep = contexto.parametros.string("idVep", "");

        if (Objeto.empty(contexto.idCobis()))
            return RespuestaMB.sinPseudoSesion();

        if (Objeto.anyEmpty(idVep))
            return RespuestaMB.parametrosIncorrectos();

        String cuit = contexto.persona().cuit();
        String codigoVep = vep(idVep).string("numero");
        TarjetaDebito tarjetaDebito = contexto.tarjetaDebitoPorDefecto();
        if (Objeto.empty(tarjetaDebito))
            return RespuestaMB.estado(SIN_TARJETA_DEBITO);

        ApiRequestMB request = ApiMB.request("LinkDeleteVeps", "veps", "DELETE", "/v1/veps", contexto);
        request.header("x-usuariocanal", ConfigMB.string("configuracion_usuario"));
        request.query("idTributarioCliente", cuit);
        request.query("numeroTarjeta", tarjetaDebito.numero());
        request.query("numeroVep", codigoVep);

        ApiResponseMB response = ApiMB.response(request, codigoVep);
        if (response.hayError())
            return RespuestaMB.error();

        return RespuestaMB.exito();
    }

    public static byte[] comprobantePago(ContextoMB contexto) {
        String idComprobante = contexto.parametros.string("idComprobante");
        if ("VEP".equals(idComprobante.split("_")[0]))
            return comprobantePagoVep(contexto);
        else
            return comprobantePagoServicio(contexto);

    }

    public static byte[] comprobantePagoServicio(ContextoMB contexto) {
        String idComprobante = contexto.parametros.string("idComprobante");

        String idAgenda = idComprobante.split("__")[0];
        String fechaDesde = idComprobante.split("__")[1];
        String fechaHasta = idComprobante.split("__")[2];

        Objeto itemAgenda = itemAgenda(idAgenda);
        TarjetaDebito tarjetaDebito = contexto.tarjetaDebito(itemAgenda.string("idTarjetaDebito"));

        if (tarjetaDebito == null) {
            return null;
        }
        ApiRequestMB request = ApiMB.request("LinkGetPagos", "link", "GET", "/v1/servicios/{numeroTarjeta}/pagos", contexto);
        request.path("numeroTarjeta", tarjetaDebito.numero());
        request.query("usuarioLP", itemAgenda.string("codigoLink"));
        request.query("codigoEnte", itemAgenda.string("codigoEnte"));
        request.query("paginaActual", "1");
        request.query("cantidadPagina", "100");
        request.query("fechaDesde", fechaDesde);
        request.query("fechaHasta", fechaHasta);
        request.cacheSesion = true;

        ApiResponseMB response = ApiMB.response(request, tarjetaDebito.numero(), itemAgenda.string("codigoLink"));
        if (response.hayError()) {
            return null;
        }

        String idDeuda = idComprobante.split("__")[3];
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
        contexto.sesion().setComprobante(comprobanteImpreso, parametros);
        contexto.setHeader("Content-Type", "application/pdf; name=comprobante.pdf");
        return Pdf.generar("pago-servicio", parametros);
    }

    public static byte[] comprobantePagoVep(ContextoMB contexto) {
        String idComprobante = contexto.parametros.string("idComprobante");

        // VEP_2019-05-25_2019-06-25_20105176512_034000038777
        String fechaDesde = idComprobante.split("_")[1];
        String fechaHasta = idComprobante.split("_")[2];
        String cuitOriginanteVeps = idComprobante.split("_")[3];
        String nroSec = idComprobante.split("_")[4];

        TarjetaDebito tarjetaDebito = contexto.tarjetaDebitoPorDefecto();
        String cuit = cuitOriginanteVeps != null && !"".equals(cuitOriginanteVeps) ? cuitOriginanteVeps : contexto.persona().cuit();

        if (tarjetaDebito == null)
            return null;

        ApiRequestMB request = ApiMB.request("LinkGetVepsPagados", "veps", "GET", "/v1/veps/{idTributarioCliente}/pagados", contexto);
        request.header("x-usuariocanal", ConfigMB.string("configuracion_usuario"));

        request.path("idTributarioCliente", cuit);
        request.query("numeroTarjeta", tarjetaDebito.numero());
        request.query("fechaDesde", fechaDesde);
        request.query("fechaHasta", fechaHasta);
        request.query("tipoConsultaLink", "1");
        request.cacheSesion = true;

        String codigoSeguridad = "";
        String fechaHoraCreacion = "";
        String servicio = "";
        BigDecimal importe = new BigDecimal(0);
        String cuitContribuyente = "";
        String tipoCuenta = "";
        String cuenta = "";
        String nroVep = "";
        String periodo = "";
        String descripcion = "";

        boolean encontro = false;

        ApiResponseMB response = ApiMB.response(request, cuit);
        if (response.codigo != 404) {
            if (response.hayError()) {
                return null;
            }
            for (Objeto item : response.objetos("veps")) {
                if (item.string("pagoNroSec") != null && item.string("pagoNroSec").equals(nroSec)) {
                    fechaHoraCreacion = item.date("pagoFecha", "yyyy-MM-dd", "dd/MM/yyyy");
                    codigoSeguridad = item.string("pagoCodSeg");
                    servicio = item.string("conceptoDesc");
                    importe = item.bigDecimal("importe");
                    cuitContribuyente = item.string("contribuyente.idTributario");
                    tipoCuenta = item.string("pagoTipoCuenta");
                    cuenta = item.string("pagoNroCuenta");
                    nroVep = item.string("nroVepOriginal");
                    periodo = item.string("periodoFiscal");
                    descripcion = item.string("pagoDesc");
                    encontro = true;
                    break;
                }
            }
        }

        if (!encontro) {
            return null;
        }

        Map<String, String> parametros = new HashMap<>();
        parametros.put("FECHA_HORA", fechaHoraCreacion);
        parametros.put("ID", codigoSeguridad);
        parametros.put("SERVICIO", servicio);
        parametros.put("IMPORTE", "$ " + Formateador.importe(importe));
        parametros.put("CUIT_CONTRIBUYENTE", cuitContribuyente);
        parametros.put("CUENTA", tipoCuenta + " $ " + cuenta);
        parametros.put("NUMERO_VEP", nroVep);
        parametros.put("PERIODO", periodo);
        parametros.put("DESCRIPCION", descripcion);
        parametros.put("TRANSACCION", nroSec);

        String comprobanteImpreso = "pago-vep" + "_" + idComprobante;
        contexto.sesion().setComprobante(comprobanteImpreso, parametros);
        contexto.setHeader("Content-Type", "application/pdf; name=comprobante.pdf");
        return Pdf.generar("pago-vep", parametros);
    }

    public static RespuestaMB comprobantesVep(ContextoMB contexto) {
        String fechaDesde = contexto.parametros.date("fechaDesde", "d/M/yyyy", "yyyy-MM-dd", null);
        String fechaHasta = contexto.parametros.date("fechaHasta", "d/M/yyyy", "yyyy-MM-dd", null);
        String cuitOriginanteVeps = contexto.parametros.string("cuitOriginante", null);

        TarjetaDebito tarjetaDebito = contexto.tarjetaDebitoPorDefecto();
        String cuit = StringUtils.isNotBlank(cuitOriginanteVeps) ? cuitOriginanteVeps : contexto.persona().cuit();

        ApiMB.eliminarCache(contexto, "LinkWindowsGetVepsPagados", cuit); // elimino cache ya que lo uso cada vez que consulto un comprobante
        ApiMB.eliminarCache(contexto, "LinkGetVepsPagados", cuit);

        if (Objeto.empty(tarjetaDebito))
            return RespuestaMB.estado(SIN_TARJETA_DEBITO);

        ApiRequestMB request = ApiMB.request("LinkGetVepsPagados", "veps", "GET", "/v1/veps/{idTributarioCliente}/pagados", contexto);

        request.path("idTributarioCliente", cuit);
        request.query("numeroTarjeta", tarjetaDebito.numero());
        request.query("fechaDesde", fechaDesde);
        request.query("fechaHasta", fechaHasta);
        request.query("tipoConsultaLink", "1");
        request.cacheSesion = true;

        ApiResponseMB response = ApiMB.response(request, cuit);

        if (response.hayError() && response.codigo != 404)
            return RespuestaMB.error();

        Objeto comprobantesVeps = new Objeto();
        if (!response.hayError()) {
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
        }

        return RespuestaMB.exito("comprobantesVeps", comprobantesVeps);
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
    public static SqlResponseMB selectDescripcionAgendaPagosLink(ContextoMB contexto) {
        List<TarjetaDebito> tarjetasDebito = contexto.tarjetasDebito();
        SqlRequestMB sqlRequest = SqlMB.request("SelectDescripcionAgendaPagosLink", "homebanking");
        sqlRequest.sql = "SELECT [psp_tarjetaDebito], [psp_codigoPago], [psp_perteneceA] ";
        sqlRequest.sql += "FROM [Homebanking].[dbo].[PagoServiciosPerteneceA] ";
        sqlRequest.sql += "WHERE [psp_tarjetaDebito] IN ( ";
        for (int i = 0; i < tarjetasDebito.size(); ++i) {
            sqlRequest.sql += (i > 0 ? ", " : "") + "?";
            sqlRequest.add(tarjetasDebito.get(i).numero());
        }
        sqlRequest.sql += ") ";
        return SqlMB.response(sqlRequest);
    }

    public static SqlResponseMB insertOrUpdateDescripcionAgendaPagosLink(String psp_tarjetaDebito, String psp_codigoPago, String psp_perteneceA) {
        SqlRequestMB sqlRequest = SqlMB.request("InsertOrUpdateDescripcionAgendaPagosLink", "homebanking");
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

        return SqlMB.response(sqlRequest);
    }

    public static RespuestaMB eliminarDebitoAutomatico(ContextoMB contexto) {
        String numeroCuenta = contexto.parametros.string("numeroCuenta");
        String codigoAdhesion = contexto.parametros.string("codigoAdhesion");

        RespuestaMB respuesta = new RespuestaMB();
        try {
            if (Objeto.anyEmpty(numeroCuenta, codigoAdhesion)) {
                return RespuestaMB.parametrosIncorrectos();
            }

            if (contexto.cuenta(numeroCuenta) == null) {
                return RespuestaMB.parametrosIncorrectos();
            }
            Cuenta cuenta = contexto.cuenta(numeroCuenta);

            String cuitEmpresa = getCuitEmpresa(contexto);
            if (cuitEmpresa.isEmpty()) {
                codigoAdhesion = getCodigoAdhesion(contexto);
                contexto.parametros.set("codigoAdhesion", codigoAdhesion);
                cuitEmpresa = getCuitEmpresa(contexto);
                if (cuitEmpresa.isEmpty()) {
                    return RespuestaMB.estado("ERROR_GENERANDO_RECLAMO");
                }
            }

            /*
             * if( ApiProducto.tieneSolicitudEnCurso(contexto, "BAJA_SERV", codigoAdhesion,
             * cuenta.numero()) ) { return Respuesta.estado("SOLICITUD_EN_CURSO"); }
             */

            String tipificacion = cuenta.esCajaAhorro() ? "BAJA_ADHESION_CA_PEDIDO" : "BAJA_ADHESION_CC_PEDIDO";

            Objeto obj = new Objeto();
            obj.set("numeroCuenta", numeroCuenta);
            obj.set("codigoAdhesion", codigoAdhesion);

            if (RestPostventa.tieneSolicitudEnCurso(contexto, tipificacion, obj, true)) {
                return RespuestaMB.estado("SOLICITUD_EN_CURSO");
            }

            ApiResponseMB responseReclamo = RestPostventa.bajaDebitoAutomatico(contexto, tipificacion, cuenta.numero(), codigoAdhesion, cuitEmpresa);

            if (responseReclamo == null || responseReclamo.hayError()) {
                return RespuestaMB.estado("ERROR_GENERANDO_RECLAMO");
            }

            SqlResponseMB sqlResponse = MBProducto.insertarReclamo(contexto, "BAJA_SERV", cuenta.numero(), codigoAdhesion, "", "");
            if (sqlResponse.hayError) {
                return RespuestaMB.estado("ERROR_GENERANDO_RECLAMO");
            }

            return respuesta;
        } catch (Exception e) {
            return RespuestaMB.error();
        }
    }

    private static String getCuitEmpresa(ContextoMB contexto) {
        String numeroCuenta = contexto.parametros.string("numeroCuenta");
        String codigoAdhesion = contexto.parametros.string("codigoAdhesion");
        String cuitEmpresa = "";
        try {
            RespuestaMB debitos = consultaDebitosAutomaticos(contexto);
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

    public static RespuestaMB consultaDebitosAutomaticos(ContextoMB contexto) {
        RespuestaMB respuesta = new RespuestaMB();
        ApiRequestMB request = ApiMB.request("CuentasDebitosAutomaticos", "cuentas", "GET", "/v1/debitoautomatico", contexto);

        request.query("codigoBanco", "044");
        request.query("codigoCliente", contexto.idCobis());
        request.query("convenio", "11");
        request.query("filial", "0");
        request.query("modo", "0");
        request.query("origen", "M");

        ApiResponseMB response = ApiMB.response(request, contexto.idCobis());
        if (response.hayError()) {
            return respuesta;
        }

        for (Objeto item : response.objetos()) {
            // para validar de solo cargar los vigentes
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
                respuesta.add("servicios", servicio);
            }
        }
        return respuesta;
    }

    private static String getCodigoAdhesion(ContextoMB contexto) {
        String numeroCuenta = contexto.parametros.string("numeroCuenta");
        String codigoAdhesion = contexto.parametros.string("codigoAdhesion");
        try {
            RespuestaMB debitos = consultaDebitosAutomaticos(contexto);

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
}
