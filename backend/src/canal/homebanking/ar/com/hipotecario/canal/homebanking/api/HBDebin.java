package ar.com.hipotecario.canal.homebanking.api;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.servicio.api.transmit.JourneyTransmitEnum;
import ar.com.hipotecario.backend.util.Transmit;
import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.Respuesta;
import ar.com.hipotecario.canal.homebanking.base.ConfigHB;
import ar.com.hipotecario.canal.homebanking.base.Objeto;
import ar.com.hipotecario.canal.homebanking.conector.*;
import ar.com.hipotecario.canal.homebanking.excepcion.ApiException;
import ar.com.hipotecario.canal.homebanking.lib.Fecha;
import ar.com.hipotecario.canal.homebanking.lib.Formateador;
import ar.com.hipotecario.canal.homebanking.lib.Util;
import ar.com.hipotecario.canal.homebanking.negocio.Cuenta;
import ar.com.hipotecario.canal.homebanking.negocio.CuentaTercero;
import ar.com.hipotecario.canal.homebanking.negocio.TarjetaDebito;
import ar.com.hipotecario.canal.homebanking.servicio.*;
import ar.com.hipotecario.canal.libreriariesgofraudes.infrastructure.gateway.model.bm.hb.DebinHBBMBankProcess;
import ar.com.hipotecario.canal.libreriariesgofraudes.infrastructure.gateway.model.bm.hb.TransactionHBBMBankProcess;
import org.apache.poi.sl.usermodel.ObjectMetaData;

public class HBDebin {

    public static Respuesta listaDebinRecibidos(ContextoHB contexto) {
        contexto.parametros.set("tipo", "RECIBIDOS");
        return listaDebin(contexto);
    }

    public static Respuesta listaDebinEnviados(ContextoHB contexto) {
        contexto.parametros.set("tipo", "ENVIADOS");
        return listaDebin(contexto);
    }

    public static Respuesta listaDebin(ContextoHB contexto) {
        Date fechaDesde = contexto.parametros.date("fechaDesde", "d/M/yyyy");
        Date fechaHasta = contexto.parametros.date("fechaHasta", "d/M/yyyy");
        String filtro = contexto.parametros.string("filtro", "");
        String descripcionEstado = contexto.parametros.string("descripcionEstado", "");
        String tipo = contexto.parametros.string("tipo");

        if (Objeto.anyEmpty(fechaDesde, fechaHasta, tipo)) {
            return Respuesta.parametrosIncorrectos();
        }
        if (!Objeto.setOf("RECIBIDOS", "ENVIADOS").contains(tipo)) {
            return Respuesta.parametrosIncorrectos();
        }

        fechaHasta = Fecha.sumarDias(fechaHasta, 1L);

        ApiResponse response = null;

        if ("RECIBIDOS".equals(tipo)) {
            response = RestDebin.listaDebinesRecibidos(contexto, fechaDesde, fechaHasta);
        }
        if ("ENVIADOS".equals(tipo)) {
            response = RestDebin.listaDebinesEnviados(contexto, fechaDesde, fechaHasta);
        }
        if (response.hayError() && !Objeto.setOf("85").contains(response.string("codigo"))) {
            return Respuesta.error();
        }

        Respuesta respuesta = new Respuesta();
        for (Objeto item : response.objetos("debins")) {
            Objeto debin = new Objeto();

            if (item.string("tipo").equals("DEBIN")) {
                Date vencimiento = null;
                try {
                    vencimiento = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(item.string("fechaExpiracion"));
                } catch (Exception e) {
                }

                String descripcionEstadoDebin = RestDebin.descripcionEstado(item.string("estado.codigo"));
                if (!descripcionEstado.isEmpty() && !descripcionEstado.equalsIgnoreCase(descripcionEstadoDebin)) {
                    continue;
                }

                debin.set("id", item.string("id"));
                debin.set("preautorizado", item.bool("preautorizado"));
                debin.set("estado", item.string("estado.codigo"));
                debin.set("descripcionEstado", RestDebin.descripcionEstado(item.string("estado.codigo")));
                debin.set("cuitOrigen", item.string("vendedor.cliente.idTributario"));
                debin.set("cuitOrigenFormateado", Formateador.cuit(item.string("vendedor.cliente.idTributario")));
                if (item.string("vendedor.cliente.idTributario").equals(contexto.persona().cuit())) {
                    debin.set("nombreOrigen", contexto.persona().nombreCompleto().toUpperCase());
                } else {
                    if (!item.string("vendedor.cliente.nombreCompleto").trim().isEmpty()) {
                        debin.set("nombreOrigen", item.string("vendedor.cliente.nombreCompleto").trim());
                    } else {
                        debin.set("nombreOrigen", "");
                    }
                }
                debin.set("cuitDestino", item.string("comprador.cliente.idTributario"));
                debin.set("cuitDestinoFormateado", Formateador.cuit(item.string("comprador.cliente.idTributario")));
                if (item.string("comprador.cliente.idTributario").equals(contexto.persona().cuit())) {
                    debin.set("nombreDestino", contexto.persona().nombreCompleto().toUpperCase());
                } else {
                    if (!item.string("comprador.cliente.nombreCompleto").trim().isEmpty()) {
                        debin.set("nombreDestino", item.string("comprador.cliente.nombreCompleto").trim());
                    } else {
                        debin.set("nombreDestino", "");
                    }
                }
                debin.set("idMoneda", item.string("moneda.id"));
                debin.set("simboloMoneda", Formateador.simboloMoneda(item.string("moneda.id")));
                debin.set("importe", item.bigDecimal("importe"));
                debin.set("importeFormateado", Formateador.importe(item.bigDecimal("importe")));
                debin.set("vencimiento", vencimiento != null ? new SimpleDateFormat("dd/MM/yy HH:mm").format(vencimiento) : "");
                debin.set("orden", vencimiento != null ? Long.MAX_VALUE - vencimiento.getTime() : Long.MAX_VALUE);

                String campos = "";
                if (tipo.equals("RECIBIDOS")) {
                    campos += debin.string("nombreOrigen").toLowerCase() + "|";
                    campos += debin.string("cuitOrigen").toLowerCase() + "|";
                    campos += debin.string("cuitOrigenFormateado").toLowerCase() + "|";
                }
                if (tipo.equals("ENVIADOS")) {
                    campos += debin.string("nombreDestino").toLowerCase() + "|";
                    campos += debin.string("cuitDestino").toLowerCase() + "|";
                    campos += debin.string("cuitDestinoFormateado").toLowerCase() + "|";
                }
                if (!filtro.isEmpty() && !campos.contains(filtro.toLowerCase())) {
                    continue;
                }
            }
            respuesta.add("debines", debin);
        }
        try {
            Objeto debines = ((Objeto) respuesta.get("debines")).ordenar("orden");
            respuesta.set("debines", debines);
        } catch (Exception e) {
        }
        return respuesta;
    }

    public static Respuesta detalleDebin(ContextoHB contexto) {
        String id = contexto.parametros.string("id");
        Boolean buscarCuentaPropia = contexto.parametros.bool("buscarCuentaPropia", false);

        contexto.sesion.setChallengeOtp(false);

        if (Objeto.anyEmpty(id)) {
            return Respuesta.parametrosIncorrectos();
        }

        ApiResponse response = RestDebin.detalleDebin(contexto, id);
        if (response.hayError()) {
            return Respuesta.error();
        }

        ApiResponse cuentaOrigen = CuentasService.cuentaLink(contexto, response.string("vendedor.cliente.cuenta.cbu"), "80");
        ApiResponse cuentaDestino = CuentasService.cuentaLink(contexto, response.string("comprador.cliente.cuenta.cbu"), "80");

        Objeto debin = new Objeto();
        debin.set("titularDestino", response.string("comprador.cliente.nombreCompleto").trim());
        if (debin.string("titularDestino").isEmpty()) {
            debin.set("titularDestino", "");
            if (response.string("comprador.cliente.idTributario").equals(contexto.persona().cuit())) {
                debin.set("titularDestino", contexto.persona().nombreCompleto().toUpperCase());
            }
        }
        debin.set("cuitDestino", response.string("comprador.cliente.idTributario"));
        debin.set("cuitDestinoFormateado", Formateador.cuit(response.string("comprador.cliente.idTributario")));
        if (!cuentaDestino.hayError()) {
            debin.set("descripcionCuentaDestino", cuentaDestino.string("tipoProducto").replace("CTE", "CC").replace("AHO", "CA") + " XXXX-" + Formateador.ultimos4digitos(cuentaDestino.string("cuenta")));
        } else {
            debin.set("descripcionCuentaDestino", "***");
        }
        debin.set("cbuDestino", response.string("comprador.cliente.cuenta.cbu"));
        debin.set("aliasDestino", response.string("comprador.cliente.cuenta.alias"));
        debin.set("bancoDestino", RestCatalogo.banco(response.string("comprador.cliente.cuenta.banco")));

        debin.set("titularOrigen", response.string("vendedor.cliente.nombreCompleto").trim());
        if (debin.string("titularOrigen").isEmpty()) {
            debin.set("titularOrigen", "");
            if (response.string("vendedor.cliente.idTributario").equals(contexto.persona().cuit())) {
                debin.set("titularOrigen", contexto.persona().nombreCompleto().toUpperCase());
            }
        }
        debin.set("cuitOrigen", response.string("vendedor.cliente.idTributario"));
        debin.set("cuitOrigenFormateado", Formateador.cuit(response.string("vendedor.cliente.idTributario")));
        if (!cuentaOrigen.hayError()) {
            debin.set("descripcionCuentaOrigen", cuentaOrigen.string("tipoProducto").replace("CTE", "CC").replace("AHO", "CA") + " XXXX-" + Formateador.ultimos4digitos(cuentaOrigen.string("cuenta")));
        } else {
            debin.set("descripcionCuentaOrigen", "***");
        }
        debin.set("cbuOrigen", response.string("vendedor.cliente.cuenta.cbu"));
        debin.set("aliasOrigen", response.string("vendedor.cliente.cuenta.alias"));
        debin.set("bancoOrigen", RestCatalogo.banco(response.string("vendedor.cliente.cuenta.banco")));

        debin.set("idMoneda", response.string("detalle.moneda.id"));
        debin.set("simboloMoneda", Formateador.simboloMoneda(response.string("detalle.moneda.id")));
        debin.set("monto", response.bigDecimal("detalle.importe"));
        debin.set("montoFormateado", Formateador.importe(response.bigDecimal("detalle.importe")));
        debin.set("fechaAlta", response.date("detalle.fecha", "yyyy-MM-dd'T'HH:mm:ss", "dd/MM/yy HH:mm", ""));
        debin.set("fechaVencimiento", response.date("detalle.fechaExpiracion", "yyyy-MM-dd'T'HH:mm:ss", "dd/MM/yy HH:mm", ""));
        debin.set("concepto", TransferenciaService.conceptosDebin().get(response.string("detalle.concepto")));
        debin.set("descripcion", response.string("detalle.descripcion"));
        debin.set("estado", response.string("estado.codigo"));

        Objeto cuentaPropia = new Objeto();
        if (buscarCuentaPropia) {
            Cuenta cuenta = contexto.cuentaPorCBU(response.string("vendedor.cliente.cuenta.cbu"));
            cuenta = cuenta == null ? contexto.cuentaPorCBU(response.string("comprador.cliente.cuenta.cbu")) : cuenta;
            if (cuenta != null) {
                cuentaPropia.set("id", cuenta.id());
                cuentaPropia.set("descripcionCorta", cuenta.descripcionCorta());
                cuentaPropia.set("ultimos4digitos", cuenta.ultimos4digitos());
                cuentaPropia.set("cbu", cuenta.cbu());
                cuentaPropia.set("cbuFormateado", cuenta.cbuFormateado());
                cuentaPropia.set("comentario", cuenta.comentario());
                cuentaPropia.set("alias", cuenta.alias());
                cuentaPropia.set("idMoneda", cuenta.idMoneda());
                cuentaPropia.set("simboloMoneda", cuenta.simboloMoneda());
                cuentaPropia.set("numero", cuenta.numero());
                cuentaPropia.set("saldo", cuenta.saldo());
                cuentaPropia.set("saldoFormateado", cuenta.saldoFormateado());
                cuentaPropia.set("acuerdo", cuenta.acuerdo());
                cuentaPropia.set("acuerdoFormateado", cuenta.acuerdoFormateado());
                cuentaPropia.set("disponible", cuenta.saldo().add(cuenta.acuerdo() != null ? cuenta.acuerdo() : new BigDecimal("0")));
                cuentaPropia.set("disponibleFormateado", Formateador.importe(cuentaPropia.bigDecimal("disponible")));
                cuentaPropia.set("unipersonal", cuenta.unipersonal());
                cuentaPropia.set("fechaAlta", cuenta.fechaAlta("dd/MM/yyyy"));
                cuentaPropia.set("paquetizada", !"".equals(cuenta.idPaquete()));
                try {
                    Boolean activa = RestDebin.cuentaActivaVendedor(contexto, cuenta);
                    cuentaPropia.set("estadoVendedorCoelsa", activa ? "ACTIVA" : "INACTIVA");
                } catch (Exception e) {
                    cuentaPropia.set("estadoVendedorCoelsa", "DESCONOCIDO");
                }
            }
        }

        Respuesta respuesta = new Respuesta();
        respuesta.set("debin", debin);
        if (buscarCuentaPropia) {
            respuesta.set("cuentaPropia", cuentaPropia);
        }
        return respuesta;
    }

    public static Respuesta nuevoDebin(ContextoHB contexto) {
        String idCuentaOrigen = contexto.parametros.string("idCuentaOrigen");
        String cbuAliasCuentaDestino = contexto.parametros.string("cbuAliasCuentaDestino");
        BigDecimal monto = contexto.parametros.bigDecimal("monto");
        String concepto = contexto.parametros.string("concepto", "VAR");
        Integer horasVigencia = contexto.parametros.integer("horasVigencia", 1);
        String descripcion = contexto.parametros.string("descripcion", "");

        if (Objeto.anyEmpty(idCuentaOrigen, cbuAliasCuentaDestino, monto)) {
            return Respuesta.parametrosIncorrectos();
        }

        Cuenta cuentaOrigen = contexto.cuenta(idCuentaOrigen);
        if (cuentaOrigen == null) {
            return Respuesta.estado("ERROR_CUENTA_ORIGEN");
        }

        Respuesta respuestaPausada = HBTarjetas.verificarTarjetaDebitoPausadaEnCuenta(cuentaOrigen, contexto);
        if (respuestaPausada != null)
            return respuestaPausada;

        ApiResponse cuentaDestino = CuentasService.cuentaCoelsa(contexto, cbuAliasCuentaDestino);
        if (cuentaDestino.hayError()) {
            return Respuesta.estado("ERROR_CUENTA_DESTINO");
        }

        try {
            Boolean cuentaActivaVendedor = RestDebin.cuentaActivaVendedor(contexto, cuentaOrigen);
            if (!cuentaActivaVendedor) {
                ApiResponse activacionCuenta = RestDebin.activarCuentaVendedor(contexto, cuentaOrigen);
                if (activacionCuenta.hayError()) {
                    return Respuesta.estado("ERROR_ACTIVANDO_CUENTA");
                }
            }
        } catch (ApiException respon) {
            return Respuesta.estado("VENDEDOR_NO_ENCONTRADO");
        }

        Objeto vendedor = new Objeto();
        Objeto clienteVendedor = vendedor.set("cliente");
        clienteVendedor.set("idTributario", contexto.persona().cuit());
        clienteVendedor.set("cuenta").set("cbu", cuentaOrigen.cbu()).set("sucursal").set("id", cuentaOrigen.sucursal());

        Objeto comprador = new Objeto();
        Objeto clienteComprador = comprador.set("cliente");
        clienteComprador.set("idTributario", cuentaDestino.string("cuit").trim());
        clienteComprador.set("cuenta").set("cbu", cuentaDestino.string("cbu").trim()).set("alias", cuentaDestino.string("nuevoAlias").trim());

        Objeto moneda = new Objeto();
        moneda.set("id", cuentaOrigen.idMoneda());
        moneda.set("descripcion", cuentaOrigen.idMoneda().equals("80") ? "Pesos" : "Dolares");
        moneda.set("signo", cuentaOrigen.idMoneda().equals("80") ? "$" : "USD");

        ApiRequest request = Api.request("NuevoDebin", "debin", "POST", "/v1/debin/", contexto);
        request.body("vendedor", vendedor);
        request.body("comprador", comprador);
        request.body("concepto", concepto);
        request.body("moneda", moneda);
        request.body("importe", monto);
        request.body("tiempoExpiracion", horasVigencia * 60);
        request.body("descripcion", descripcion);
        request.body("recurrencia", false);
        ApiResponse response = null;
        try {
            response = Api.response(request, contexto.idCobis());
        } finally {
            try {
                String codigoError = response == null ? "ERROR" : response.hayError() ? response.string("codigo") : "0";
                String transaccion = response != null && codigoError.equals("0") ? response.string("recibo") : "";
                // transaccion += "|" + contexto.persona().cuit() + "|" +
                // cuentaDestino.string("cuit").trim();

                String descripcionError = "";
                if (response != null && !codigoError.equals("0")) {
                    descripcionError += response.string("codigo") + ".";
                    descripcionError += response.string("mensajeAlUsuario") + ".";
                }
                descripcionError = descripcionError.length() > 990 ? descripcionError.substring(0, 990) : descripcionError;

                SqlRequest sqlRequest = Sql.request("InsertAuditorDebinAlta", "hbs");
                sqlRequest.sql = "INSERT INTO [hbs].[dbo].[auditor_transferencia] ";
                sqlRequest.sql += "([momento],[cobis],[idProceso],[ip],[canal],[codigoError],[descripcionError],[tipo],[cuentaOrigen],[cuentaDestino],[importe],[moneda],[concepto],[cuentaPropia],[servicioDomestico],[especial],[tarjetaDebito],[transaccion]) ";
                sqlRequest.sql += "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                sqlRequest.add(new Date()); // momento
                sqlRequest.add(contexto.idCobis()); // cobis
                sqlRequest.add(request.idProceso()); // idProceso
                sqlRequest.add(request.ip()); // ip
                sqlRequest.add("HB"); // canal
                sqlRequest.add(codigoError); // codigoError
                sqlRequest.add(descripcionError); // descripcionError
                sqlRequest.add("nuevo_debin"); // tipo
                sqlRequest.add(cuentaOrigen.numero()); // cuentaOrigen
                sqlRequest.add(cuentaDestino.string("cbu").trim()); // cuentaDestino
                sqlRequest.add(monto); // importe
                sqlRequest.add(cuentaOrigen.idMoneda()); // moneda
                sqlRequest.add(concepto); // concepto
                sqlRequest.add("false"); // cuentaPropia
                sqlRequest.add(null); // servicioDomestico
                sqlRequest.add(null); // especial
                sqlRequest.add(null); // tarjetaDebito
                sqlRequest.add(transaccion); // transaccion

                Sql.response(sqlRequest);
            } catch (Exception e) {
            }
        }
        if (response.hayError() && response.string("detalle").contains("MONEDA DEL VENDEDOR DIFERENTE A LA REQUERIDA")) {
            return Respuesta.estado("MONEDA_INCORRECTA");
        }
        if (response.hayError() && response.string("detalle").contains("CBU DESTINO Y ORIGEN IDENTICOS")) {
            return Respuesta.estado("ORIGEN_DESTINO_IDENTICO");
        }
        if (response.hayError()) {
            return Respuesta.error();
        }

        if (HBSalesforce.prendidoSalesforce(contexto.idCobis())) {
            Objeto parametros = new Objeto();
            parametros.set("NOMBRE_APELLIDO", contexto.persona().nombre() + " " + contexto.persona().apellido());
            parametros.set("NOMBRE", contexto.persona().nombre());
            parametros.set("APELLIDO", contexto.persona().apellido());
            Date hoy = new Date();
            parametros.set("FECHA", new SimpleDateFormat("dd/MM/yyyy").format(hoy));
            parametros.set("HORA", new SimpleDateFormat("hh:mm").format(hoy));
            parametros.set("CLIENTE_PRESTACION", cuentaOrigen.cbu());
            parametros.set("COMPRADOR_PRESTACION", cuentaDestino.get("cbu") + "(" + cuentaDestino.get("nombreTitular") + ")");
            parametros.set("MONEDA", moneda.get("descripcion"));
            parametros.set("IMPORTE", monto);
            parametros.set("CANAL", "Home Banking");

            String salesforce_orden_debin = ConfigHB.string("salesforce_orden_debin");
            parametros.set("IDCOBIS", contexto.idCobis());
            parametros.set("ISMOBILE", contexto.esMobile());
            new Futuro<>(() -> HBSalesforce.registrarEventoSalesforce(contexto, salesforce_orden_debin, parametros));
        }


        Respuesta respuesta = new Respuesta();
        respuesta.set("idDebin", response.string("debin.id"));
        return respuesta;
    }

    public static Respuesta aceptarDebin(ContextoHB contexto) {
        String id = contexto.parametros.string("id");
        String csmId = contexto.parametros.string("csmId", "");
        String checksum = contexto.parametros.string("checksum", "");

        if (Objeto.anyEmpty(id)) {
            return Respuesta.parametrosIncorrectos();
        }

        Boolean esMigrado = contexto.esMigrado(contexto);

        if (esMigrado && Objeto.anyEmpty(csmId, checksum))
            return Respuesta.parametrosIncorrectos();

        Respuesta respuestaValidaTransaccion = contexto.validarTransaccion(contexto, esMigrado, "debin", JourneyTransmitEnum.HB_INICIO_SESION);
        if (respuestaValidaTransaccion.hayError())
            return respuestaValidaTransaccion;

        if (HBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendidoHorarioFraude")) {
            if (Util.fueraHorarioFraudes(contexto)) {
                return Respuesta.estado("FUERA_HORARIO_F_D", contexto.csmIdAuth);
            }
        }

        ApiResponse detalle = RestDebin.detalleDebin(contexto, id);
        if (detalle.hayError()) {
            return Respuesta.error();
        }
        if (!detalle.string("estado.codigo").equals("INICIADO")) {
            return Respuesta.estado("ESTADO_DEBIN_INCORRECTO", contexto.csmIdAuth);
        }

        String cbu = detalle.string("comprador.cliente.cuenta.cbu");
        Cuenta cuentaUsuario = contexto.cuentaPorCBU(cbu);
        if (cuentaUsuario == null) {
            return Respuesta.estado("CUENTA_NO_ENCONTRADA", contexto.csmIdAuth);
        }

        if (HBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_modo_transaccional_debin",
                "prendido_modo_transaccional_debin_cobis") && !TransmitHB.isChallengeOtp(contexto, "debin")) {
            try {
                String sessionToken = contexto.parametros.string(Transmit.getSessionToken(), null);
                if (Objeto.empty(sessionToken))
                    return Respuesta.parametrosIncorrectos();

                Futuro<CuentaTercero> futuroCuentaPayee = new Futuro<>(() -> new CuentaTercero(contexto, detalle.string("comprador.cliente.cuenta.cbu")));
                CuentaTercero cuentaPayee = futuroCuentaPayee.tryGet();

                DebinHBBMBankProcess debinHBBMBankProcess = new DebinHBBMBankProcess(contexto.idCobis(),
                        sessionToken,
                        detalle.bigDecimal("detalle.importe"),
                        Util.obtenerDescripcionMonedaTransmit(cuentaPayee.idMoneda()), TransmitHB.REASON_DEBIN,
                        new DebinHBBMBankProcess.Payer(contexto.persona().cuit(), cuentaUsuario.numero(), Util.BH_CODIGO, TransmitHB.CANAL),
                        new DebinHBBMBankProcess.Payee(cuentaPayee.cuit(), cuentaPayee.cbu(), Util.BH_CODIGO));

                Respuesta respuesta = TransmitHB.recomendacionTransmit(contexto, debinHBBMBankProcess, "debin");
                if (respuesta.hayError())
                    return respuesta;
            } catch (Exception e) {
            }
        }

        Respuesta respuestaPausada = HBTarjetas.verificarTarjetaDebitoPausadaEnCuenta(cuentaUsuario, contexto);
        if (respuestaPausada != null)
            return respuestaPausada.set("csmIdAuth", contexto.csmIdAuth);

        Objeto debin = new Objeto();
        debin.set("id", id);
        debin.set("importe", detalle.bigDecimal("detalle.importe"));
        debin.set("estado").set("codigo", "00");
        debin.set("comprador").set("cliente");

        Objeto cliente = debin.objeto("comprador.cliente");
        cliente.set("idTributario", contexto.persona().cuit());
        cliente.set("nombreCompleto", contexto.persona().nombreCompleto().toUpperCase());

        Objeto cuenta = debin.objeto("cuenta");
        cuenta.set("cbu", detalle.string("comprador.cliente.cuenta.cbu"));
        cuenta.set("numero", cuentaUsuario.numero());
        cuenta.set("tipo", cuentaUsuario.idTipo());
        if (cuentaUsuario.esPesos()) {
            cuenta.set("moneda").set("id", "80").set("descripcion", "Pesos").set("signo", "$");
        } else {
            cuenta.set("moneda").set("id", "2").set("descripcion", "Dolares").set("signo", "USD");
        }
        cuenta.set("sucursal").set("id", cuentaUsuario.sucursal()).set("description", cuentaUsuario.sucursal());
        cliente.set("cuenta", cuenta);

        if (cuentaUsuario.saldo().compareTo(detalle.bigDecimal("detalle.importe")) < 0) {
            return Respuesta.estado("SIN_SALDO", contexto.csmIdAuth);
        }

        ApiRequest request = Api.request("AceptarDebin", "debin", "POST", "/v1/debin/autorizar", contexto);
        request.body("debin", debin);

        ApiResponse response = null;
        try {
            response = Api.response(request, contexto.idCobis());
        } finally {
            try {
                String codigoError = response == null ? "ERROR" : response.hayError() ? response.string("codigo") : "0";
                String transaccion = response != null && codigoError.equals("0") ? response.string("recibo") : "";
                transaccion += "|" + contexto.persona().cuit() + "|" + detalle.string("comprador.cliente.idTributario");

                String descripcionError = "";
                if (response != null && !codigoError.equals("0")) {
                    descripcionError += response.string("codigo") + ".";
                    descripcionError += response.string("mensajeAlUsuario") + ".";
                }
                descripcionError = descripcionError.length() > 990 ? descripcionError.substring(0, 990) : descripcionError;

                SqlRequest sqlRequest = Sql.request("InsertAuditorDebinAlta", "hbs");
                sqlRequest.sql = "INSERT INTO [hbs].[dbo].[auditor_transferencia] ";
                sqlRequest.sql += "([momento],[cobis],[idProceso],[ip],[canal],[codigoError],[descripcionError],[tipo],[cuentaOrigen],[cuentaDestino],[importe],[moneda],[concepto],[cuentaPropia],[servicioDomestico],[especial],[tarjetaDebito],[transaccion]) ";
                sqlRequest.sql += "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                sqlRequest.add(new Date()); // momento
                sqlRequest.add(contexto.idCobis()); // cobis
                sqlRequest.add(request.idProceso()); // idProceso
                sqlRequest.add(request.ip()); // ip
                sqlRequest.add("HB"); // canal
                sqlRequest.add(codigoError); // codigoError
                sqlRequest.add(descripcionError); // descripcionError
                sqlRequest.add("acepta_debin"); // tipo
                sqlRequest.add(cuentaUsuario.numero()); // cuentaOrigen
                sqlRequest.add(detalle.string("vendedor.cliente.cuenta.cbu")); // cuentaDestino
                sqlRequest.add(detalle.bigDecimal("detalle.importe")); // importe
                sqlRequest.add(cuentaUsuario.idMoneda()); // moneda
                sqlRequest.add(null); // concepto
                sqlRequest.add("false"); // cuentaPropia
                sqlRequest.add(null); // servicioDomestico
                sqlRequest.add(null); // especial
                sqlRequest.add(null); // tarjetaDebito
                sqlRequest.add(transaccion); // transaccion

                new Futuro<>(() -> Sql.response(sqlRequest));
            } catch (Exception e) {
            }
        }
        if (response.hayError() && response.string("codigo").equals("201017")) {
            return Respuesta.estado("SIN_SALDO", contexto.csmIdAuth);
        }
        if (response.hayError() && response.string("codigo").equals("1875053")) {
            return Respuesta.estado("EXCEDE_MONTO", contexto.csmIdAuth);
        }
        if (response.hayError()) {
            return Respuesta.error(contexto.csmIdAuth);
        }

        if (HBSalesforce.prendidoSalesforce(contexto.idCobis())) {
            Objeto parametros = new Objeto();
            parametros.set("NOMBRE_APELLIDO", contexto.persona().nombre() + " " + contexto.persona().apellido());
            parametros.set("NOMBRE", contexto.persona().nombre());
            parametros.set("APELLIDO", contexto.persona().apellido());
            Date hoy = new Date();
            parametros.set("FECHA", new SimpleDateFormat("dd/MM/yyyy").format(hoy));
            parametros.set("HORA", new SimpleDateFormat("hh:mm").format(hoy));
            parametros.set("CUENTA_TIPO", cuentaUsuario.idTipo());
            parametros.set("MONEDA", ((Objeto) cuenta.get("moneda")).get("descripcion"));
            parametros.set("CANAL", "Home Banking");

            String salesforce_aceptacion_debin = ConfigHB.string("salesforce_aceptacion_debin");
            parametros.set("IDCOBIS", contexto.idCobis());
            parametros.set("ISMOBILE", contexto.esMobile());
            new Futuro<>(() -> HBSalesforce.registrarEventoSalesforce(contexto, salesforce_aceptacion_debin, parametros));
        }

        contexto.limpiarSegundoFactor();
        RestDebin.eliminarCacheDetalleDebin(contexto, id);
        ProductosService.eliminarCacheProductos(contexto);

        // Monitoreo Debin
        if (HBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_monitoreo_transaccional_4")) {
            TarjetaDebito tarjetaDebitoAsociada = contexto.tarjetaDebitoAsociada(cuentaUsuario);
            String crncyde = cuentaUsuario.esPesos() ? "032" : "840";
            String codigoTransaccion = cuentaUsuario.esCajaAhorro() ? cuentaUsuario.esPesos() ? "F31100" : "F31500" : cuentaUsuario.esPesos() ? "F30100" : "F30700";
            new Futuro<>(() -> new HBMonitoring().sendMonitoringDebin(contexto, tarjetaDebitoAsociada == null ? contexto.persona().cuit() : tarjetaDebitoAsociada.numero(), Formateador.importeTlf(detalle.bigDecimal("detalle.importe"), 12), crncyde, "00", "0210", cuentaUsuario.numero(), cbu.trim(), request.idProceso(), codigoTransaccion));
        }

        // Comprobante
        Map<String, String> comprobante = new HashMap<>();
        String idComprobante = "debin" + "_" + id;
        contexto.sesion.comprobantes.put(idComprobante, comprobante);
        contexto.limpiarSegundoFactor();
        return Respuesta.exito("idComprobante", idComprobante).set("csmIdAuth", contexto.csmIdAuth);
    }

    public static Respuesta rechazarDebin(ContextoHB contexto) {
        String id = contexto.parametros.string("id");

        if (Objeto.anyEmpty(id)) {
            return Respuesta.parametrosIncorrectos();
        }

        ApiResponse detalle = RestDebin.detalleDebin(contexto, id);
        RestDebin.eliminarCacheDetalleDebin(contexto, id);
        if (detalle.hayError()) {
            return Respuesta.error();
        }
        if (!detalle.string("estado.codigo").equals("INICIADO")) {
            return Respuesta.estado("ESTADO_DEBIN_INCORRECTO");
        }

        if (detalle.string("vendedor.cliente.idTributario").equals(contexto.persona().cuit())) {
            ApiRequest request = Api.request("CancelarDebin", "debin", "DELETE", "/v1/debin", contexto);
            request.query("id", id);
            request.query("idTributario", contexto.persona().cuit());

            ApiResponse response = Api.response(request, contexto.idCobis());
            if (response.hayError()) {
                return Respuesta.error();
            }
        }

        if (detalle.string("comprador.cliente.idTributario").equals(contexto.persona().cuit())) {
            String cbu = detalle.string("comprador.cliente.cuenta.cbu");
            Cuenta cuentaUsuario = contexto.cuentaPorCBU(cbu);
            if (cuentaUsuario == null) {
                return Respuesta.estado("CUENTA_NO_ENCONTRADA");
            }

            Objeto debin = new Objeto();
            debin.set("id", id);
            debin.set("importe", detalle.bigDecimal("detalle.importe"));
            debin.set("estado").set("codigo", "10");
            debin.set("comprador").set("cliente");

            Objeto cliente = debin.objeto("comprador.cliente");
            cliente.set("idTributario", contexto.persona().cuit());
            cliente.set("nombreCompleto", contexto.persona().nombreCompleto().toUpperCase());

            Objeto cuenta = debin.objeto("cuenta");
            cuenta.set("cbu", detalle.string("comprador.cliente.cuenta.cbu"));
            cuenta.set("numero", cuentaUsuario.numero());
            cuenta.set("tipo", cuentaUsuario.idTipo());
            if (cuentaUsuario.esPesos()) {
                cuenta.set("moneda").set("id", "80").set("descripcion", "Pesos").set("signo", "$");
            } else {
                cuenta.set("moneda").set("id", "2").set("descripcion", "Dolares").set("signo", "USD");
            }
            cuenta.set("sucursal").set("id", cuentaUsuario.sucursal()).set("description", cuentaUsuario.sucursal());
            cliente.set("cuenta", cuenta);

            ApiRequest request = Api.request("RechazarDebin", "debin", "POST", "/v1/debin/autorizar", contexto);
            request.body("debin", debin);

            ApiResponse response = Api.response(request, contexto.idCobis());
            if (response.hayError() && response.string("codigo").equals("201017")) {
                return Respuesta.estado("SIN_SALDO");
            }
            if (response.hayError() && response.string("codigo").equals("1875053")) {
                return Respuesta.estado("EXCEDE_MONTO");
            }
            if (response.hayError()) {
                return Respuesta.error();
            }
        }

        return new Respuesta();
    }

    public static Respuesta activarCuentaDebin(ContextoHB contexto) {
        String idCuenta = contexto.parametros.string("idCuenta");

        if (Objeto.anyEmpty(idCuenta)) {
            return Respuesta.parametrosIncorrectos();
        }

        Cuenta cuenta = contexto.cuenta(idCuenta);
        if (cuenta == null) {
            return Respuesta.estado("CUENTA_NO_ENCONTRADA");
        }

        ApiResponse response = RestDebin.activarCuentaVendedor(contexto, cuenta);
        if (response.hayError()) {
            return Respuesta.error();
        }

        Respuesta respuesta = new Respuesta();
        return respuesta;
    }

    public static Respuesta desactivarCuentaDebin(ContextoHB contexto) {
        String idCuenta = contexto.parametros.string("idCuenta");

        if (Objeto.anyEmpty(idCuenta)) {
            return Respuesta.parametrosIncorrectos();
        }

        Cuenta cuenta = contexto.cuenta(idCuenta);
        if (cuenta == null) {
            return Respuesta.estado("CUENTA_NO_ENCONTRADA");
        }

        ApiResponse response = RestDebin.desactivarCuenta(contexto, cuenta);
        if (response.hayError()) {
            return Respuesta.error();
        }

        Respuesta respuesta = new Respuesta();
        return respuesta;
    }

	/*public static Respuesta listaDebinRecurrentes(ContextoHB contexto) {
		contexto.parametros.set("tipo", "RECURRENTES");
		return listaDebinesRecurrentes(contexto);
	}*/

    public static Respuesta consultaRecurrencias(ContextoHB contexto) {

        if (!ConfigHB.bool("prendido_debin_recurrente")) {
            Respuesta respuesta = new Respuesta();
            respuesta.setEstado("0");
            respuesta.add("recurrencias", new Objeto());
            return respuesta;
        }

        Date fechaDesde = contexto.parametros.date("fechaDesde", "d/M/yyyy");
        Date fechaHasta = contexto.parametros.date("fechaHasta", "d/M/yyyy");
        String filtro = contexto.parametros.string("filtro", "");
        String descripcionEstado = contexto.parametros.string("descripcionEstado", "");
        String tipo = contexto.parametros.string("tipo");
        String cantidadPaginas = "";
        Date currentDate = new Date(System.currentTimeMillis());

        if (Objeto.anyEmpty(filtro)) {
            fechaDesde = new Date("01/01/1900");
            fechaHasta = currentDate;
        }

        if (filtro.equals("PENDIENTE")) {
            //Date currentDate = new Date(System.currentTimeMillis());
            Calendar c = Calendar.getInstance();
            c.setTime(currentDate);
            c.add(Calendar.DATE, -2);

            fechaDesde = c.getTime();
            fechaHasta = currentDate;
        }

        fechaHasta = Fecha.sumarDias(fechaHasta, 1L);

        Date finalFechaDesde = fechaDesde;
        Date finalFechaHasta = fechaHasta;

        Futuro<ApiResponse> apiResponseFuturo = new Futuro<>(() -> RestDebin.consultaRecurrencias(contexto, finalFechaDesde, finalFechaHasta));

        cantidadPaginas = apiResponseFuturo.get().string("result.listado.paginas_totales");

        Respuesta respuesta = new Respuesta();
        int contadorPendientes = 0;

        if (apiResponseFuturo.get().hayError()) {
            String codigoRespuesta = apiResponseFuturo.get().string("codigo");

            if (!"5100".equals(codigoRespuesta)) {
                respuesta.setEstado("0");
                respuesta.add("recurrencias", new Objeto());
                return respuesta;
            }

            return Respuesta.error();
        }

        for (Objeto item : apiResponseFuturo.get().objetos("result.recurrencia")) {
            Objeto recurrencia = new Objeto();

            if (!item.string("comprador.cbu").substring(0, 3).equals("044")) {
                continue;
            }

            Date fechaCreacion = null;
            try {
                fechaCreacion = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(item.string("fecha_creacion"));
            } catch (Exception e) {
            }

            String documento = item.string("vendedor.cuit").substring(2, 10).replaceFirst("^0+", "");
            String nombreVendedor = "";
            String aliasComprador = "";

            Futuro<ApiResponse> responsePersona = new Futuro<>(() -> RestPersona.consultarPersonaCuitPadron(contexto, documento));
            Futuro<ApiResponse> responseCoelsa = new Futuro<>(() -> CuentasService.cuentaCoelsa(contexto, item.string("comprador.cbu")));

            if (!responsePersona.get().hayError()) {
                aliasComprador = responseCoelsa.get().string("aliasValorOriginal");
            }

            if (!responsePersona.get().hayError()) {
                for (Objeto itemPersona : responsePersona.get().objetos()) {
                    if (itemPersona.string("cuil").equals(item.string("vendedor.cuit")))
                        nombreVendedor = itemPersona.string("apellidoYNombre");
                }
            }

            if (item.string("autorizado").equals("PENDIENTE")) {

                contadorPendientes++;
                Objeto card = new Objeto();
                card.set("id", contadorPendientes);
                card.set("icono", ConfigHB.string("icono_card_recurrencia")); //VE "assets/img/watch.svg"
                card.set("texto", "Tenés un <b>Debin de Recurrencia</b> de " + nombreVendedor);
                card.set("objeto").set("name", "Debin de Recurrencia").set("id", item.string("id"));
                card.set("entidad", nombreVendedor);
                card.set("subTexto", "");
                Objeto acciones = new Objeto();
                acciones.set("caption", "IR A SOLICITUD").set("letsdoit", item.string("id"));
                card.add("acciones", acciones);
                recurrencia.set("card", card);

            }

            recurrencia.set("idRecurrencia", item.string("id"));
            recurrencia.set("estado", item.string("estado"));

            //TODO: vendedor = origen
            recurrencia.set("bancoOrigen", nombreVendedor);
            recurrencia.set("cbuOrigen", "");
            recurrencia.set("cbuOrigenFormateado", "");
            recurrencia.set("aliasOrigen", "");
            recurrencia.set("cuitOrigen", item.string("vendedor.cuit"));
            recurrencia.set("cuitOrigenFormateado", Formateador.cuit(item.string("vendedor.cuit")));

            //TODO: comprador = destino
            recurrencia.set("bancoDestino", RestCatalogo.banco(item.string("comprador.cbu").substring(0, 3)));
            recurrencia.set("cbuDestino", item.string("comprador.cbu"));
            recurrencia.set("cbuDestinoFormateado", Formateador.cbu(item.string("comprador.cbu")));
            recurrencia.set("aliasDestino", aliasComprador);
            recurrencia.set("cuitDestino", item.string("comprador.cuit"));
            recurrencia.set("cuitDestinoFormateado", Formateador.cuit(item.string("comprador.cuit")));

            recurrencia.set("autorizado", item.string("autorizado"));
            recurrencia.set("fechaCreacion",
                    fechaCreacion != null ? new SimpleDateFormat("dd/MM/yy").format(fechaCreacion) : "");
            recurrencia.set("orden", fechaCreacion != null ? Long.MAX_VALUE - fechaCreacion.getTime() : Long.MAX_VALUE);

            String campos = "";
            campos += recurrencia.string("nombreDestino").toLowerCase() + "|";
            campos += recurrencia.string("cuitDestino").toLowerCase() + "|";
            campos += recurrencia.string("cuitDestinoFormateado").toLowerCase() + "|";
            campos += recurrencia.string("autorizado").toLowerCase() + "|";

            if (!filtro.isEmpty() && !filtro.equals("NOPENDING") && !campos.contains(filtro.toLowerCase())) {
                continue;
            }

            if ((filtro.isEmpty() || filtro.equals("NOPENDING")) && item.string("autorizado").equals("PENDIENTE")) {
                continue;
            }

            respuesta.add("recurrencias", recurrencia);

        }

        return respuesta;
    }

    public static Respuesta aceptarRecurrencia(ContextoHB contexto) {
        contexto.parametros.set("id", contexto.parametros.string("id"));
        contexto.parametros.set("aceptaRecurrencia", true);

        return confirmarRecurrecia(contexto);
    }

    public static Respuesta rechazarRecurrencia(ContextoHB contexto) {
        contexto.parametros.set("id", contexto.parametros.string("id"));
        contexto.parametros.set("aceptaRecurrencia", false);

        return confirmarRecurrecia(contexto);

    }

    public static Respuesta confirmarRecurrecia(ContextoHB contexto) {

        String id = contexto.parametros.string("id");
        Boolean aceptaRecurrencia = contexto.parametros.bool("aceptaRecurrencia", false);
        String csmId = contexto.parametros.string("csmId", "");
        String checksum = contexto.parametros.string("checksum", "");

        if (Objeto.anyEmpty(id)) {
            return Respuesta.parametrosIncorrectos();
        }

        boolean esMigrado = contexto.esMigrado(contexto);

        if (esMigrado && Objeto.anyEmpty(csmId, checksum))
            return Respuesta.parametrosIncorrectos();

        Respuesta respuestaValidaTransaccion = contexto.validarTransaccion(contexto, esMigrado, "debin", JourneyTransmitEnum.HB_INICIO_SESION);
        if (respuestaValidaTransaccion.hayError())
            return respuestaValidaTransaccion;

        if (HBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendidoHorarioFraude")) {
            if (Util.fueraHorarioFraudes(contexto)) {
                return Respuesta.estado("FUERA_HORARIO_F_D", contexto.csmIdAuth);
            }
        }

        //TODO: ACA busqueda de la recurrecia a aceptar/rechazar.
        Date fechaDesde = new Date("01/01/1900");
        Date currentDate = new Date(System.currentTimeMillis());

        Futuro<ApiResponse> responseLista = new Futuro<>(() -> RestDebin.consultaRecurrencias(contexto, fechaDesde, currentDate));

        if (responseLista.get().hayError()) {
            String codigoRespuesta = responseLista.get().string("codigo");

            if (!"5100".equals(codigoRespuesta)) {
                return Respuesta.error(contexto.csmIdAuth);
            }
        }

        Objeto objRecurrencia = new Objeto();
        for (Objeto item : responseLista.get().objetos("result.recurrencia")) {
            if (item.string("id").equals(id)) {
                objRecurrencia = item;
            }
        }
        //TODO: FIN BUSQUEDA

        ApiResponse response = null;

        try {
            response = RestDebin.confirmarRecurrecia(contexto, id, aceptaRecurrencia, objRecurrencia);

        } catch (Exception ignored) {
        }
		/*finally {

		}*/

        if (response.hayError()) {
            String codigoRespuesta = response.string("codigo");

            if (!"6321".equals(codigoRespuesta)) {
                return Respuesta.error(contexto.csmIdAuth);
            }
        }

        RestDebin.eliminarCacheDetalleDebin(contexto, id);
        ProductosService.eliminarCacheProductos(contexto);
        contexto.limpiarSegundoFactor();

        // Comprobante
        if (aceptaRecurrencia) {
            Map<String, String> comprobante = new HashMap<>();
            String idComprobante = "debinRecurrente" + "_" + id;
            contexto.sesion.comprobantes.put(idComprobante, comprobante);
            return Respuesta.exito("idComprobante", idComprobante).set("csmIdAuth", contexto.csmIdAuth);
        }

        return Respuesta.exito().set("csmIdAuth", contexto.csmIdAuth);
    }

    public static Object bajaRecurrencia(ContextoHB contextoHB) {

        String id = contextoHB.parametros.string("id");
        String csmId = contextoHB.parametros.string("csmId", "");
        String checksum = contextoHB.parametros.string("checksum", "");

        if (Objeto.anyEmpty(id)) {
            return Respuesta.parametrosIncorrectos();
        }

        boolean esMigrado = contextoHB.esMigrado(contextoHB);

        if (esMigrado && Objeto.anyEmpty(csmId, checksum))
            return Respuesta.parametrosIncorrectos();

        Respuesta respuestaValidaTransaccion = contextoHB.validarTransaccion(contextoHB, esMigrado, "debin", JourneyTransmitEnum.HB_INICIO_SESION);
        if (respuestaValidaTransaccion.hayError())
            return respuestaValidaTransaccion;

        if (HBAplicacion.funcionalidadPrendida(contextoHB.idCobis(), "prendidoHorarioFraude")) {
            if (Util.fueraHorarioFraudes(contextoHB)) {
                return Respuesta.estado("FUERA_HORARIO_F_D", contextoHB.csmIdAuth);
            }
        }

        //TODO: ACA busqueda de la recurrecia a aceptar/rechazar.
        Date fechaDesde = new Date("01/01/1900");
        Date currentDate = new Date(System.currentTimeMillis());

        Futuro<ApiResponse> responseLista = new Futuro<>(() -> RestDebin.consultaRecurrencias(contextoHB, fechaDesde, currentDate));

        if (responseLista.get().hayError()) {
            String codigoRespuesta = responseLista.get().string("codigo");

            if (!"5100".equals(codigoRespuesta)) {
                return Respuesta.error(contextoHB.csmIdAuth);
            }
        }

        Objeto objRecurrencia = new Objeto();
        for (Objeto item : responseLista.get().objetos("result.recurrencia")) {
            if (item.string("id").equals(id)) {
                objRecurrencia = item;
            }
        }
        //TODO: FIN BUSQUEDA

        ApiResponse response = null;
        try {
            response = RestDebin.bajaRecurrencia(contextoHB, id, objRecurrencia);
        } catch (Exception e) {

        }

        if (response == null || response.hayError()) {
            return Respuesta.estado("ERROR_BAJA_RECURRENCIA", contextoHB.csmIdAuth);
        }

        contextoHB.limpiarSegundoFactor();
        RestDebin.eliminarCacheDetalleDebin(contextoHB, id);
        ProductosService.eliminarCacheProductos(contextoHB);

        return Respuesta.exito().set("csmIdAuth", contextoHB.csmIdAuth);
    }

}