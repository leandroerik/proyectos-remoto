package ar.com.hipotecario.mobile.api;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.servicio.api.transmit.JourneyTransmitEnum;
import ar.com.hipotecario.backend.util.Transmit;
import ar.com.hipotecario.canal.libreriariesgofraudes.infrastructure.gateway.model.bm.mb.DebinMBBMBankProcess;
import ar.com.hipotecario.canal.libreriariesgofraudes.infrastructure.gateway.model.bm.mb.TransactionMBBMBankProcess;
import ar.com.hipotecario.mobile.ConfigMB;
import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.RespuestaMB;
import ar.com.hipotecario.mobile.conector.*;
import ar.com.hipotecario.mobile.lib.Fecha;
import ar.com.hipotecario.mobile.lib.Formateador;
import ar.com.hipotecario.mobile.lib.Objeto;
import ar.com.hipotecario.mobile.lib.Util;
import ar.com.hipotecario.mobile.negocio.Cuenta;
import ar.com.hipotecario.mobile.negocio.CuentaTercero;
import ar.com.hipotecario.mobile.servicio.*;

public class MBDebin {

    public static RespuestaMB listaDebinRecibidos(ContextoMB contexto) {
        contexto.parametros.set("tipo", "RECIBIDOS");
        return listaDebin(contexto);
    }

    public static RespuestaMB listaDebinEnviados(ContextoMB contexto) {
        contexto.parametros.set("tipo", "ENVIADOS");
        return listaDebin(contexto);
    }

    public static RespuestaMB listaDebin(ContextoMB contexto) {
        Date fechaDesde = contexto.parametros.date("fechaDesde", "d/M/yyyy");
        Date fechaHasta = contexto.parametros.date("fechaHasta", "d/M/yyyy");
        String filtro = contexto.parametros.string("filtro", "");
        String descripcionEstado = contexto.parametros.string("descripcionEstado", "");
        String tipo = contexto.parametros.string("tipo");

        if (Objeto.anyEmpty(fechaDesde, fechaHasta, tipo)) {
            return RespuestaMB.parametrosIncorrectos();
        }
        if (!Objeto.setOf("RECIBIDOS", "ENVIADOS").contains(tipo)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        fechaHasta = Fecha.sumarDias(fechaHasta, 1L);

        ApiResponseMB response = null;
        if ("RECIBIDOS".equals(tipo)) {
            response = RestDebin.listaDebinesRecibidos(contexto, fechaDesde, fechaHasta);
        }
        if ("ENVIADOS".equals(tipo)) {
            response = RestDebin.listaDebinesEnviados(contexto, fechaDesde, fechaHasta);
        }
        if (response.hayError() && !Objeto.setOf("85").contains(response.string("codigo"))) {
            return RespuestaMB.error();
        }

        RespuestaMB respuesta = new RespuestaMB();
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
                debin.set("vencimiento",
                        vencimiento != null ? new SimpleDateFormat("dd/MM/yy HH:mm").format(vencimiento) : "");
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
            if (!"true".equals(ConfigMB.string("prendido_debin_recurrente")) && item.bool("preautorizado")) {
                continue;
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

    public static RespuestaMB detalleDebin(ContextoMB contexto) {
        String id = contexto.parametros.string("id");
        Boolean buscarCuentaPropia = contexto.parametros.bool("buscarCuentaPropia", false);

        if (Objeto.anyEmpty(id)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        ApiResponseMB response = RestDebin.detalleDebin(contexto, id);
        if (response.hayError()) {
            return RespuestaMB.error();
        }

        ApiResponseMB cuentaOrigen = CuentasService.cuentaLink(contexto, response.string("vendedor.cliente.cuenta.cbu"),
                "80");
        ApiResponseMB cuentaDestino = CuentasService.cuentaLink(contexto,
                response.string("comprador.cliente.cuenta.cbu"), "80");

        Objeto debin = new Objeto();
        debin.set("id", response.string("id").trim());

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
            debin.set("descripcionCuentaDestino",
                    cuentaDestino.string("tipoProducto").replace("CTE", "CC").replace("AHO", "CA") + " XXXX-"
                            + Formateador.ultimos4digitos(cuentaDestino.string("cuenta")));
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
            debin.set("descripcionCuentaOrigen",
                    cuentaOrigen.string("tipoProducto").replace("CTE", "CC").replace("AHO", "CA") + " XXXX-"
                            + Formateador.ultimos4digitos(cuentaOrigen.string("cuenta")));
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
        debin.set("fechaVencimiento",
                response.date("detalle.fechaExpiracion", "yyyy-MM-dd'T'HH:mm:ss", "dd/MM/yy HH:mm", ""));
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
                cuentaPropia.set("disponible",
                        cuenta.saldo().add(cuenta.acuerdo() != null ? cuenta.acuerdo() : new BigDecimal("0")));
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

        RespuestaMB respuesta = new RespuestaMB();
        respuesta.set("debin", debin);
        if (buscarCuentaPropia) {
            respuesta.set("cuentaPropia", cuentaPropia);
        }
        return respuesta;
    }

    public static RespuestaMB nuevoDebin(ContextoMB contexto) {
        String idCuentaOrigen = contexto.parametros.string("idCuentaOrigen");
        String cbuAliasCuentaDestino = contexto.parametros.string("cbuAliasCuentaDestino");
        BigDecimal monto = contexto.parametros.bigDecimal("monto");
        String concepto = contexto.parametros.string("concepto", "VAR");
        Integer horasVigencia = contexto.parametros.integer("horasVigencia", 1);
        String descripcion = contexto.parametros.string("descripcion", "");

        if (Objeto.anyEmpty(idCuentaOrigen, cbuAliasCuentaDestino, monto)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        Cuenta cuentaOrigen = contexto.cuenta(idCuentaOrigen);
        if (cuentaOrigen == null) {
            return RespuestaMB.estado("ERROR_CUENTA_ORIGEN");
        }

        RespuestaMB respuestaPausada = MBTarjetas.verificarTarjetaDebitoPausadaEnCuenta(cuentaOrigen, contexto);
        if (respuestaPausada != null)
            return respuestaPausada;

        ApiResponseMB cuentaDestino = CuentasService.cuentaCoelsa(contexto, cbuAliasCuentaDestino);
        if (cuentaDestino.hayError()) {
            return RespuestaMB.estado("ERROR_CUENTA_DESTINO");
        }

        if (!RestDebin.cuentaActivaVendedor(contexto, cuentaOrigen)) {
            ApiResponseMB activacionCuenta = RestDebin.activarCuentaVendedor(contexto, cuentaOrigen);
            if (activacionCuenta.hayError()) {
                return RespuestaMB.estado("ERROR_ACTIVANDO_CUENTA");
            }
        }

        Objeto vendedor = new Objeto();
        Objeto clienteVendedor = vendedor.set("cliente");
        clienteVendedor.set("idTributario", contexto.persona().cuit());
        clienteVendedor.set("cuenta").set("cbu", cuentaOrigen.cbu()).set("sucursal").set("id", cuentaOrigen.sucursal());

        Objeto comprador = new Objeto();
        Objeto clienteComprador = comprador.set("cliente");
        clienteComprador.set("idTributario", cuentaDestino.string("cuit").trim());
        clienteComprador.set("cuenta").set("cbu", cuentaDestino.string("cbu").trim()).set("alias",
                cuentaDestino.string("nuevoAlias").trim());

        Objeto moneda = new Objeto();
        moneda.set("id", cuentaOrigen.idMoneda());
        moneda.set("descripcion", cuentaOrigen.idMoneda().equals("80") ? "Pesos" : "Dolares");
        moneda.set("signo", cuentaOrigen.idMoneda().equals("80") ? "$" : "USD");

        ApiRequestMB request = ApiMB.request("NuevoDebin", "debin", "POST", "/v1/debin/", contexto);
        request.body("vendedor", vendedor);
        request.body("comprador", comprador);
        request.body("concepto", concepto);
        request.body("moneda", moneda);
        request.body("importe", monto);
        request.body("tiempoExpiracion", horasVigencia * 60);
        request.body("descripcion", descripcion);
        request.body("recurrencia", false);

        ApiResponseMB response = ApiMB.response(request, contexto.idCobis());

        if (ConfigMB.bool("log_transaccional", false)) {
            try {
                String codigoError = response == null ? "ERROR" : response.hayError() ? response.string("codigo") : "0";

                String transaccion = codigoError.equals("0") ? response.string("recibo") : "";

                String descripcionError = "";
                if (response != null && !codigoError.equals("0")) {
                    descripcionError += response.string("codigo") + ".";
                    descripcionError += response.string("mensajeAlUsuario") + ".";
                }
                descripcionError = descripcionError.length() > 990 ? descripcionError.substring(0, 990)
                        : descripcionError;

                InsertAuditorTransferencia(contexto, "InsertAuditorDebinAlta", "nuevo_debin", request, codigoError,
                        descripcionError, transaccion, monto, cuentaDestino.string("cbu").trim(), concepto,
                        cuentaOrigen);
            } catch (Exception e) {
            }
        }

        if (response.hayError()
                && response.string("detalle").contains("MONEDA DEL VENDEDOR DIFERENTE A LA REQUERIDA")) {
            return RespuestaMB.estado("MONEDA_INCORRECTA");
        }
        if (response.hayError() && response.string("detalle").contains("CBU DESTINO Y ORIGEN IDENTICOS")) {
            return RespuestaMB.estado("ORIGEN_DESTINO_IDENTICO");
        }
        if (response.hayError()) {
            return RespuestaMB.error();
        }

        if (MBSalesforce.prendidoSalesforce(contexto.idCobis())) {
            Objeto parametros = new Objeto();
            parametros.set("NOMBRE", contexto.persona().nombre());
            parametros.set("APELLIDO", contexto.persona().apellido());
            Date hoy = new Date();
            parametros.set("FECHA", new SimpleDateFormat("dd/MM/yyyy").format(hoy));
            parametros.set("HORA", new SimpleDateFormat("hh:mm").format(hoy));
            parametros.set("CLIENTE_PRESTACION", cuentaOrigen.cbu());
            parametros.set("COMPRADOR_PRESTACION", cuentaDestino.get("cbu") + "(" + cuentaDestino.get("nombreTitular") + ")");
            parametros.set("MONEDA", moneda.get("descripcion"));
            parametros.set("IMPORTE", monto);
            parametros.set("CANAL", ConfigMB.string("salesforce_nombre_canal"));

            String salesforce_orden_debin = ConfigMB.string("salesforce_orden_debin");
            parametros.set("IDCOBIS", contexto.idCobis());
            parametros.set("ISMOBILE", true);
            new Futuro<>(() -> MBSalesforce.registrarEventoSalesforce(contexto, salesforce_orden_debin, parametros));
        }

        RespuestaMB respuesta = new RespuestaMB();
        respuesta.set("idDebin", response.string("debin.id"));
        return respuesta;
    }

    public static RespuestaMB aceptarDebin(ContextoMB contexto) {
        String id = contexto.parametros.string("id");
        String csmId = contexto.parametros.string("csmId");
        String checksum = contexto.parametros.string("checksum");

        // TODO bloquear en determinados dias y horarios la funcionalidad
        if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendidoHorarioFraude")) {
            if (Util.fueraHorarioFraudes(contexto)) {
                return RespuestaMB.estado("FUERA_HORARIO_F_D");
            }
        }

        // TODO buscar prendido_aceptar_debin
        if (!MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_aceptar_debin")) {
            return RespuestaMB.estado("FUNCIONALIDAD_APAGADA");
        }

        if (Objeto.anyEmpty(id)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        ApiResponseMB detalle = RestDebin.detalleDebin(contexto, id);
        if (detalle.hayError()) {
            return RespuestaMB.error();
        }
        if (!detalle.string("estado.codigo").equals("INICIADO")) {
            return RespuestaMB.estado("ESTADO_DEBIN_INCORRECTO");
        }

        String cbu = detalle.string("comprador.cliente.cuenta.cbu");
        Cuenta cuentaUsuario = contexto.cuentaPorCBU(cbu);
        if (cuentaUsuario == null) {
            return RespuestaMB.estado("CUENTA_NO_ENCONTRADA");
        }

        if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_modo_transaccional_debin",
                "prendido_modo_transaccional_debin_cobis") && !TransmitMB.isChallengeOtp(contexto, "transferencia")) {
            try {
                String sessionToken = contexto.parametros.string(Transmit.getSessionToken(), null);
                if (Objeto.empty(sessionToken))
                    return RespuestaMB.parametrosIncorrectos();

                Futuro<CuentaTercero> futuroCuentaPayee = new Futuro<>(() -> new CuentaTercero(contexto, detalle.string("comprador.cliente.cuenta.cbu")));
                CuentaTercero cuentaPayee = futuroCuentaPayee.tryGet();

                DebinMBBMBankProcess debinMBBMBankProcess = new DebinMBBMBankProcess(contexto.idCobis(),
                        sessionToken,
                        detalle.bigDecimal("detalle.importe"),
                        Util.obtenerDescripcionMonedaTransmit(cuentaPayee.idMoneda()), TransmitMB.REASON_DEBIN,
                        new TransactionMBBMBankProcess.Payer(contexto.persona().cuit(), cuentaUsuario.numero(), Util.getBhCodigo(), TransmitMB.CANAL),
                        new TransactionMBBMBankProcess.Payee(cuentaPayee.cuit(), cuentaPayee.cbu(), cuentaPayee.codigoBanco()));

                RespuestaMB respuesta = TransmitMB.recomendacionTransmit(contexto, debinMBBMBankProcess, "transferencia");
                if (respuesta.hayError())
                    return respuesta;
            } catch (Exception e) {
            }
        }

        boolean esMigrado = contexto.esMigrado(contexto);

        if (esMigrado && Objeto.anyEmpty(csmId, checksum))
            return RespuestaMB.parametrosIncorrectos();

        RespuestaMB respuestaValidaTransaccion = contexto.validarTransaccion(contexto, esMigrado, "transferencia", JourneyTransmitEnum.MB_INICIO_SESION);
        if (respuestaValidaTransaccion.hayError())
            return respuestaValidaTransaccion;

        RespuestaMB respuestaPausada = MBTarjetas.verificarTarjetaDebitoPausadaEnCuenta(cuentaUsuario, contexto);
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
            return RespuestaMB.estado("SIN_SALDO", contexto.csmIdAuth);
        }

        ApiRequestMB request = ApiMB.request("AceptarDebin", "debin", "POST", "/v1/debin/autorizar", contexto);
        request.body("debin", debin);

        ApiResponseMB response = ApiMB.response(request, contexto.idCobis());
        if (response.hayError() && response.string("codigo").equals("201017")) {
            return RespuestaMB.estado("SIN_SALDO", contexto.csmIdAuth);
        }
        if (response.hayError()) {
            return RespuestaMB.error(contexto.csmIdAuth);
        }

        if (ConfigMB.bool("log_transaccional", false)) {
            try {
                String codigoError = (response == null) ? "ERROR"
                        : response.hayError() ? response.string("codigo") : "0";

                String transaccion = codigoError.equals("0") ? response.string("recibo") : "";
                transaccion += "|" + contexto.persona().cuit() + "|" + detalle.string("comprador.cliente.idTributario");

                String descripcionError = "";
                if (response != null && !codigoError.equals("0")) {
                    descripcionError += response.string("codigo") + ".";
                    descripcionError += response.string("mensajeAlUsuario") + ".";
                }
                descripcionError = descripcionError.length() > 990 ? descripcionError.substring(0, 990)
                        : descripcionError;

                BigDecimal importe = detalle.bigDecimal("detalle.importe");
                String cuentaDestinoCbu = detalle.string("vendedor.cliente.cuenta.cbu");

                InsertAuditorTransferencia(contexto, "InsertAuditorDebinAlta", "acepta_debin", request, codigoError,
                        descripcionError, transaccion, importe, cuentaDestinoCbu, null, cuentaUsuario);
            } catch (Exception e) {
            }
        }


        if (MBSalesforce.prendidoSalesforce(contexto.idCobis())) {
            Objeto parametros = new Objeto();
            parametros.set("NOMBRE_APELLIDO", contexto.persona().nombre() + " " + contexto.persona().apellido());
            parametros.set("NOMBRE", contexto.persona().nombre());
            parametros.set("APELLIDO", contexto.persona().apellido());
            Date hoy = new Date();
            parametros.set("FECHA", new SimpleDateFormat("dd/MM/yyyy").format(hoy));
            parametros.set("HORA", new SimpleDateFormat("hh:mm").format(hoy));
            parametros.set("CUENTA_TIPO", cuentaUsuario.idTipo());
            parametros.set("MONEDA", ((Objeto) cuenta.get("moneda")).get("descripcion"));
            parametros.set("CANAL", ConfigMB.string("salesforce_nombre_canal"));

            String salesforce_aceptacion_debin = ConfigMB.string("salesforce_aceptacion_debin");
            parametros.set("IDCOBIS", contexto.idCobis());
            parametros.set("ISMOBILE", true);
            new Futuro<>(() -> MBSalesforce.registrarEventoSalesforce(contexto, salesforce_aceptacion_debin, parametros));
        }

        contexto.limpiarSegundoFactor();
        RestDebin.eliminarCacheDetalleDebin(contexto, id);
        ProductosService.eliminarCacheProductos(contexto);

        // Comprobante
        Map<String, String> comprobante = new HashMap<>();
        String idComprobante = "debin" + "_" + id;
        contexto.sesion().setComprobante(idComprobante, comprobante);
        contexto.limpiarSegundoFactor();
        return RespuestaMB.exito("idComprobante", idComprobante).set("csmIdAuth", contexto.csmIdAuth);

    }

    public static RespuestaMB rechazarDebin(ContextoMB contexto) {
        String id = contexto.parametros.string("id");

        if (Objeto.anyEmpty(id)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        ApiResponseMB detalle = RestDebin.detalleDebin(contexto, id);
        RestDebin.eliminarCacheDetalleDebin(contexto, id);
        if (detalle.hayError()) {
            return RespuestaMB.error();
        }
        if (!detalle.string("estado.codigo").equals("INICIADO")) {
            return RespuestaMB.estado("ESTADO_DEBIN_INCORRECTO");
        }

        if (detalle.string("vendedor.cliente.idTributario").equals(contexto.persona().cuit())) {
            ApiRequestMB request = ApiMB.request("CancelarDebin", "debin", "DELETE", "/v1/debin", contexto);
            request.query("id", id);
            request.query("idTributario", contexto.persona().cuit());

            ApiResponseMB response = ApiMB.response(request, contexto.idCobis());
            if (response.hayError()) {
                return RespuestaMB.error();
            }
        }

        if (detalle.string("comprador.cliente.idTributario").equals(contexto.persona().cuit())) {
            String cbu = detalle.string("comprador.cliente.cuenta.cbu");
            Cuenta cuentaUsuario = contexto.cuentaPorCBU(cbu);
            if (cuentaUsuario == null) {
                return RespuestaMB.estado("CUENTA_NO_ENCONTRADA");
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

            ApiRequestMB request = ApiMB.request("RechazarDebin", "debin", "POST", "/v1/debin/autorizar", contexto);
            request.body("debin", debin);

            ApiResponseMB response = ApiMB.response(request, contexto.idCobis());
            if (response.hayError() && response.string("codigo").equals("201017")) {
                return RespuestaMB.estado("SIN_SALDO");
            }
            if (response.hayError()) {
                return RespuestaMB.error();
            }
        }

        return new RespuestaMB();
    }

    public static RespuestaMB activarCuentaDebin(ContextoMB contexto) {
        String idCuenta = contexto.parametros.string("idCuenta");

        if (Objeto.anyEmpty(idCuenta)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        Cuenta cuenta = contexto.cuenta(idCuenta);
        if (cuenta == null) {
            return RespuestaMB.estado("CUENTA_NO_ENCONTRADA");
        }

        ApiResponseMB response = RestDebin.activarCuentaVendedor(contexto, cuenta);
        if (response.hayError()) {
            return RespuestaMB.error();
        }

        RespuestaMB respuesta = new RespuestaMB();
        return respuesta;
    }

    public static RespuestaMB desactivarCuentaDebin(ContextoMB contexto) {
        String idCuenta = contexto.parametros.string("idCuenta");

        if (Objeto.anyEmpty(idCuenta)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        Cuenta cuenta = contexto.cuenta(idCuenta);
        if (cuenta == null) {
            return RespuestaMB.estado("CUENTA_NO_ENCONTRADA");
        }

        ApiResponseMB response = RestDebin.desactivarCuenta(contexto, cuenta);
        if (response.hayError()) {
            return RespuestaMB.error();
        }

        RespuestaMB respuesta = new RespuestaMB();
        return respuesta;
    }

    public static void InsertAuditorTransferencia(ContextoMB contexto, String servicio, String tipo,
                                                  ApiRequestMB request, String codigoError, String descripcionError, String transaccion, BigDecimal importe,
                                                  String cuentaDestinoCbu, String concepto, Cuenta cuentaOrigen) {

        SqlRequestMB sqlRequest = SqlMB.request(servicio, "hbs");
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
        sqlRequest.add(tipo); // tipo
        sqlRequest.add(cuentaOrigen.numero()); // cuentaOrigen
        sqlRequest.add(cuentaDestinoCbu); // cuentaDestino
        sqlRequest.add(importe); // importe
        sqlRequest.add(cuentaOrigen.idMoneda()); // moneda
        sqlRequest.add(concepto); // concepto
        sqlRequest.add("false"); // cuentaPropia
        sqlRequest.add(null); // servicioDomestico
        sqlRequest.add(null); // especial
        sqlRequest.add(null); // tarjetaDebito
        sqlRequest.add(transaccion); // transaccion

        SqlMB.response(sqlRequest);
    }

    public static RespuestaMB consultaRecurrencias(ContextoMB contexto) {

        if (!"true".equals(ConfigMB.string("prendido_debin_recurrente"))) {
            RespuestaMB respuesta = null;
            respuesta.setEstado("0");
            respuesta.add("recurrencias", new Objeto());
            return respuesta;
        }

        Date fechaDesde = contexto.parametros.date("fechaDesde", "d/M/yyyy");
        Date fechaHasta = contexto.parametros.date("fechaHasta", "d/M/yyyy");
        String filtro = contexto.parametros.string("filtro", "");
        String descripcionEstado = contexto.parametros.string("descripcionEstado", "");
        String tipo = contexto.parametros.string("tipo");
        Date currentDate = new Date(System.currentTimeMillis());
        Boolean fechasVacias = false;
        String cantidadPaginas = "";

        if (Objeto.anyEmpty(fechaDesde, fechaHasta)) {
            fechasVacias = true;
            fechaDesde = new Date("01/01/1900");
            fechaHasta = currentDate;
            //return RespuestaMB.parametrosIncorrectos();
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
        Futuro<ApiResponseMB> response = new Futuro<>(() -> RestDebin.consultaRecurrencias(contexto, finalFechaDesde, finalFechaHasta));
        cantidadPaginas = response.get().string("listado.paginas_totales");
        RespuestaMB respuesta = new RespuestaMB();

        if (response.get().hayError()) {
            String codigoRespuesta = response.get().string("codigo");

            if (!"5100".equals(codigoRespuesta)) {
                respuesta.setEstado("0");
                respuesta.add("recurrencias", new Objeto());
                return respuesta;
            }

            return RespuestaMB.error();
        }

        for (Objeto item : response.get().objetos("result.recurrencia")) {
            Objeto recurrencia = new Objeto();

            if (!item.string("comprador.cbu").substring(0, 3).equals("044")) {
                continue;
            }

            String documento = item.string("vendedor.cuit").substring(2, 10).replaceFirst("^0+", "");
            String nombreVendedor = "";
            String aliasComprador = "";

            Futuro<ApiResponseMB> responsePersona = new Futuro<>(() -> RestPersona.consultarPersonaCuitPadron(contexto, documento));
            Futuro<ApiResponseMB> responseCoelsa = new Futuro<>(() -> CuentasService.cuentaCoelsa(contexto, item.string("comprador.cbu")));

            if (!responsePersona.get().hayError()) {
                aliasComprador = responseCoelsa.get().string("aliasValorOriginal");
            }

            if (!responsePersona.get().hayError()) {
                for (Objeto itemPersona : responsePersona.get().objetos()) {
                    if (itemPersona.string("cuil").equals(item.string("vendedor.cuit")))
                        nombreVendedor = itemPersona.string("apellidoYNombre");
                }
            }

            Date fechaCreacion = null;
            try {
                fechaCreacion = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(item.string("fecha_creacion"));
            } catch (Exception e) {
            }

            recurrencia.set("id", item.string("id"));
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

            if (item.string("autorizado").equals("AUTORIZADO") && item.string("estado").equals("INACTIVA")) {
                recurrencia.set("autorizado", "RECHAZADO");
            } else {
                recurrencia.set("autorizado", item.string("autorizado"));
            }

            recurrencia.set("fechaCreacion",
                    fechaCreacion != null ? new SimpleDateFormat("dd/MM/yy").format(fechaCreacion) : "");
            recurrencia.set("orden", fechaCreacion != null ? Long.MAX_VALUE - fechaCreacion.getTime() : Long.MAX_VALUE);

            String campos = "";
            campos += recurrencia.string("nombreDestino").toLowerCase() + "|";
            campos += recurrencia.string("cuitDestino").toLowerCase() + "|";
            campos += recurrencia.string("cuitDestinoFormateado").toLowerCase() + "|";
            campos += recurrencia.string("autorizado").toLowerCase() + "|";

            if (fechasVacias) {
                if (!filtro.equals("NO_PENDIENTE")) {
                    if (!campos.contains(filtro.toLowerCase())) {
                        continue;
                    }
                } else {
                    if (item.string("autorizado").equals("PENDIENTE")) {
                        continue;
                    }
                }
            }

            if (!filtro.isEmpty() && !fechasVacias) {
                if (!filtro.equals("NO_PENDIENTE")) {
                    if (!campos.contains(filtro.toLowerCase())) {
                        continue;
                    }
                } else {
                    if (item.string("autorizado").equals("PENDIENTE")) {
                        continue;
                    }
                }
            }

            respuesta.add("recurrencias", recurrencia);

        }

        return respuesta;

    }

    public static RespuestaMB aceptarRecurrencia(ContextoMB contexto) {
        contexto.parametros.set("id", contexto.parametros.string("id"));
        contexto.parametros.set("aceptaRecurrencia", true);

        return confirmarRecurrencia(contexto);
    }

    public static RespuestaMB rechazarRecurrencia(ContextoMB contexto) {
        contexto.parametros.set("id", contexto.parametros.string("id"));
        contexto.parametros.set("aceptaRecurrencia", false);

        return confirmarRecurrencia(contexto);
    }

    public static RespuestaMB confirmarRecurrencia(ContextoMB contexto) {

        String id = contexto.parametros.string("id");
        Boolean aceptaRecurrencia = contexto.parametros.bool("aceptaRecurrencia", false);
        String csmId = contexto.parametros.string("csmId");
        String checksum = contexto.parametros.string("checksum");

        //TODO: ACA busqueda de la recurrecia a aceptar/rechazar.
        Date fechaDesde = new Date("01/01/1900");
        Date currentDate = new Date(System.currentTimeMillis());

        Futuro<ApiResponseMB> responseLista = new Futuro<>(() -> RestDebin.consultaRecurrencias(contexto, fechaDesde, currentDate));

        if (responseLista.get().hayError()) {
            String codigoRespuesta = responseLista.get().string("codigo");

            if (!"5100".equals(codigoRespuesta)) {
                return RespuestaMB.error();
            }
        }

        Objeto objRecurrencia = new Objeto();
        for (Objeto item : responseLista.get().objetos("result.recurrencia")) {
            if (item.string("id").equals(id)) {
                objRecurrencia = item;
            }
        }
        //TODO: FIN BUSQUEDA
        if (Objeto.anyEmpty(id)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        if (aceptaRecurrencia) {
            boolean esMigrado = Util.migracionCompleta() || (!Util.migracionCompleta() && TransmitMB.esUsuarioMigrado(contexto,
                    contexto.idCobis(),
                    ar.com.hipotecario.backend.base.Util.documento(contexto.persona().numeroDocumento())));

            if (esMigrado && Objeto.anyEmpty(csmId, checksum))
                return RespuestaMB.parametrosIncorrectos();

            RespuestaMB respuestaValidaTransaccion = contexto.validarTransaccion(contexto, esMigrado, "debin", JourneyTransmitEnum.MB_INICIO_SESION);
            if (respuestaValidaTransaccion.hayError())
                return respuestaValidaTransaccion;
        }

        if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendidoHorarioFraude")) {
            if (Util.fueraHorarioFraudes(contexto)) {
                return RespuestaMB.estado("FUERA_HORARIO_F_D", contexto.csmIdAuth);
            }
        }

        ApiResponseMB response = null;
        try {

            response = RestDebin.confirmarRecurrecia(contexto, id, aceptaRecurrencia, objRecurrencia);

        } catch (Exception e) {
        }

        if (response.hayError()) {
            String codigoRespuesta = response.string("codigo");
            if (!"6321".equals(codigoRespuesta)) {
                return RespuestaMB.error(contexto.csmIdAuth);
            }
        }

        contexto.limpiarSegundoFactor();
        RestDebin.eliminarCacheDetalleDebin(contexto, id);
        ProductosService.eliminarCacheProductos(contexto);

        // Comprobante
        if (aceptaRecurrencia) {
            Map<String, String> comprobante = new HashMap<>();
            String idComprobante = "debinRecurrente" + "_" + id;
            contexto.sesion().setComprobante(idComprobante, comprobante);
            return RespuestaMB.exito("idComprobante", idComprobante).set("csmIdAuth", contexto.csmIdAuth);
        }

        return RespuestaMB.exito().set("csmIdAuth", contexto.csmIdAuth);
    }

    public static Object bajaRecurrencia(ContextoMB contextoMB) {
        String id = contextoMB.parametros.string("id");

        if (Objeto.anyEmpty(id)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        if (MBAplicacion.funcionalidadPrendida(contextoMB.idCobis(), "prendidoHorarioFraude")) {
            if (Util.fueraHorarioFraudes(contextoMB)) {
                return RespuestaMB.estado("FUERA_HORARIO_F_D");
            }
        }

        //TODO: ACA busqueda de la recurrecia a aceptar/rechazar.
        Date fechaDesde = new Date("01/01/1900");
        Date currentDate = new Date(System.currentTimeMillis());

        Futuro<ApiResponseMB> responseLista = new Futuro<>(() -> RestDebin.consultaRecurrencias(contextoMB, fechaDesde, currentDate));

        if (responseLista.get().hayError()) {
            String codigoRespuesta = responseLista.get().string("codigo");

            if (!"5100".equals(codigoRespuesta)) {
                return RespuestaMB.error();
            }
        }

        Objeto objRecurrencia = new Objeto();
        for (Objeto item : responseLista.get().objetos("result.recurrencia")) {
            if (item.string("id").equals(id)) {
                objRecurrencia = item;
            }
        }
        //TODO: FIN BUSQUEDA

        ApiResponseMB response = null;
        try {
            response = RestDebin.bajaRecurrencia(contextoMB, id, objRecurrencia);
        } catch (Exception e) {

        }

        if (response.hayError()) {
            return RespuestaMB.estado("ERROR_BAJA_RECURRENCIA");
        }

        contextoMB.limpiarSegundoFactor();
        RestDebin.eliminarCacheDetalleDebin(contextoMB, id);
        ProductosService.eliminarCacheProductos(contextoMB);

        return RespuestaMB.exito();
    }

    public static RespuestaMB consultaDebinesProgramados(ContextoMB contextoMB) {
        if (!"true".equals(ConfigMB.string("prendido_debin_programado"))) {
            RespuestaMB respuesta = null;
            respuesta.setEstado("0");
            respuesta.add("debinesProgramados", new Objeto());
            return respuesta;
        }

        Date fechaDesde = contextoMB.parametros.date("fechaDesde", "d/M/yyyy");
        Date fechaHasta = contextoMB.parametros.date("fechaHasta", "d/M/yyyy");
        String filtro = contextoMB.parametros.string("filtro", "");
        Date currentDate = new Date(System.currentTimeMillis());
        Boolean fechasVacias = false;
        String cantidadPaginas = "";

        if (Objeto.anyEmpty(fechaDesde, fechaHasta)) {
            fechasVacias = true;
            fechaDesde = new Date("01/01/1900");
            fechaHasta = currentDate;
        }

        if (filtro.equals("PENDIENTE")) {
            Calendar c = Calendar.getInstance();
            c.setTime(currentDate);
            c.add(Calendar.DATE, -2);

            fechaDesde = c.getTime();
            fechaHasta = currentDate;
        }

        fechaHasta = Fecha.sumarDias(fechaHasta, 1L);

        Date finalFechaDesde = fechaDesde;
        Date finalFechaHasta = fechaHasta;
        Futuro<ApiResponseMB> response = new Futuro<>(() -> RestDebin.consultaDebinesProgramados(contextoMB, finalFechaDesde, finalFechaHasta));
        cantidadPaginas = response.get().string("listado.paginas_totales");
        RespuestaMB respuesta = new RespuestaMB();

		/*if (response.get().hayError()) {
			String codigoRespuesta = response.get().string("codigo");

			if (!"5100".equals(codigoRespuesta)) {
				respuesta.setEstado("0");
				respuesta.add("debinesProgramados", new Objeto());
				return respuesta;
			}

			return RespuestaMB.error();
		}*/

        if (response.get().hayError()) {
            String codigoRespuesta = response.get().string("codigo");

            if (!"5100".equals(codigoRespuesta)) {
                String descripcionEstadoDebin = RestDebin.descripcionRespuestaCodigoDebin(codigoRespuesta);
                return respuesta.setEstado(descripcionEstadoDebin);
            }

            return RespuestaMB.error();
        }

        for (Objeto item : response.get().objetos("result.recurrencia")) {
            Objeto debinProgramado = new Objeto();

            String documento = item.string("vendedor.cuit").substring(2, 10).replaceFirst("^0+", "");
            String nombreVendedor = "";
            String aliasComprador = "";

            Futuro<ApiResponseMB> responsePersona = new Futuro<>(() -> RestPersona.consultarPersonaCuitPadron(contextoMB, documento));
            Futuro<ApiResponseMB> responseCoelsa = new Futuro<>(() -> CuentasService.cuentaCoelsa(contextoMB, item.string("comprador.cbu")));

            if (!responsePersona.get().hayError()) {
                aliasComprador = responseCoelsa.get().string("aliasValorOriginal");
            }

            if (!responsePersona.get().hayError()) {
                for (Objeto itemPersona : responsePersona.get().objetos()) {
                    if (itemPersona.string("cuil").equals(item.string("vendedor.cuit")))
                        nombreVendedor = itemPersona.string("apellidoYNombre");
                }
            }

            Date fechaCreacion = null;
            try {
                fechaCreacion = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(item.string("fecha_creacion"));
            } catch (Exception e) {
            }

            nombreVendedor = RestDebin.nombreVendedor(contextoMB, item.string("vendedor.cuit"));

            debinProgramado.set("id", item.string("id"));
            debinProgramado.set("estado", item.string("estado"));

            //TODO: vendedor = origen
            debinProgramado.set("bancoOrigen", nombreVendedor);
            debinProgramado.set("cbuOrigen", "");
            debinProgramado.set("cbuOrigenFormateado", "");
            debinProgramado.set("aliasOrigen", "");
            debinProgramado.set("cuitOrigen", item.string("vendedor.cuit"));
            debinProgramado.set("cuitOrigenFormateado", Formateador.cuit(item.string("vendedor.cuit")));

            //TODO: comprador = destino
            debinProgramado.set("bancoDestino", item.string("comprador.nombre"));
            debinProgramado.set("cbuDestino", item.string("comprador.cbu"));
            debinProgramado.set("cbuDestinoFormateado", Formateador.cbu(item.string("comprador.cbu")));
            debinProgramado.set("aliasDestino", aliasComprador);
            debinProgramado.set("cuitDestino", item.string("comprador.cuit"));
            debinProgramado.set("cuitDestinoFormateado", Formateador.cuit(item.string("comprador.cuit")));
            debinProgramado.set("autorizado", item.string("autorizado"));

            if (item.string("autorizado").equals("AUTORIZADO") &&
                    (item.string("estado").equals("INACTIVA") || item.string("estado").equals("INACTIVA DEFINITIVA"))) {
                debinProgramado.set("baja", true);
            } else {
                debinProgramado.set("baja", false);
            }
            String simboloMoneda = item.string("debin.moneda").equals("840") ? "USD" : "$";
            debinProgramado.set("simboloMoneda", simboloMoneda);
            debinProgramado.set("moneda", item.string("debin.moneda").equals("840") ? "Dolar" : "Peso");
            debinProgramado.set("limiteCuotas", item.string("debin.limite_cuotas"));
            debinProgramado.set("importe", item.string("debin.importe"));
            debinProgramado.set("importeFormateado", Formateador.importe(item.bigDecimal("debin.importe")));
            debinProgramado.set("importeConMoneda", simboloMoneda.concat(" ").concat(Formateador.importe(item.bigDecimal("debin.importe"))));

            debinProgramado.set("fechaCreacion",
                    fechaCreacion != null ? new SimpleDateFormat("dd/MM/yy").format(fechaCreacion) : "");
            debinProgramado.set("orden", fechaCreacion != null ? Long.MAX_VALUE - fechaCreacion.getTime() : Long.MAX_VALUE);

            String campos = "";
            campos += debinProgramado.string("nombreDestino").toLowerCase() + "|";
            campos += debinProgramado.string("cuitDestino").toLowerCase() + "|";
            campos += debinProgramado.string("cuitDestinoFormateado").toLowerCase() + "|";
            campos += debinProgramado.string("autorizado").toLowerCase() + "|";

            if (fechasVacias) {
                if (!filtro.equals("NO_PENDIENTE")) {
                    if (!campos.contains(filtro.toLowerCase())) {
                        continue;
                    }
                } else {
                    if (item.string("autorizado").equals("PENDIENTE")) {
                        continue;
                    }
                }
            }

            if (!filtro.isEmpty() && !fechasVacias) {
                if (!filtro.equals("NO_PENDIENTE")) {
                    if (!campos.contains(filtro.toLowerCase())) {
                        continue;
                    }
                } else {
                    if (item.string("autorizado").equals("PENDIENTE")) {
                        continue;
                    }
                }
            }

            respuesta.add("debinesProgramados", debinProgramado);

        }

        return respuesta;
    }

    public static RespuestaMB aceptarDebinProgramado(ContextoMB contexto) {
        contexto.parametros.set("id", contexto.parametros.string("id"));
        contexto.parametros.set("aceptaDebinProgramado", true);

        return confirmarDebinProgramado(contexto);
    }

    public static RespuestaMB rechazarDebinProgramado(ContextoMB contexto) {
        contexto.parametros.set("id", contexto.parametros.string("id"));
        contexto.parametros.set("aceptaDebinProgramado", false);

        return confirmarDebinProgramado(contexto);
    }

    public static RespuestaMB confirmarDebinProgramado(ContextoMB contexto) {

        String id = contexto.parametros.string("id");
        Boolean aceptaDebinProgramado = contexto.parametros.bool("aceptaDebinProgramado", false);
        String csmId = contexto.parametros.string("csmId");
        String checksum = contexto.parametros.string("checksum");

        String mensajeRespuesta = "";
        //TODO: ACA busqueda de la recurrecia a aceptar/rechazar.
        Date fechaDesde = new Date("01/01/1900");
        Date currentDate = new Date(System.currentTimeMillis());

        Futuro<ApiResponseMB> responseLista = new Futuro<>(() -> RestDebin.consultaDebinesProgramados(contexto, fechaDesde, currentDate));

        if (responseLista.get().hayError()) {
            String codigoRespuesta = responseLista.get().string("codigo");

            if (!"5100".equals(codigoRespuesta)) {
                return RespuestaMB.error();
            }
        }

        Objeto objDebinProgramado = new Objeto();
        for (Objeto item : responseLista.get().objetos("result.recurrencia")) {
            if (item.string("id").equals(id)) {
                objDebinProgramado = item;
            }
        }
        //TODO: FIN BUSQUEDA
        if (Objeto.anyEmpty(id)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        if (aceptaDebinProgramado) {
            boolean esMigrado = Util.migracionCompleta() || (!Util.migracionCompleta() && TransmitMB.esUsuarioMigrado(contexto,
                    contexto.idCobis(),
                    ar.com.hipotecario.backend.base.Util.documento(contexto.persona().numeroDocumento())));

            if (esMigrado && Objeto.anyEmpty(csmId, checksum))
                return RespuestaMB.parametrosIncorrectos();

            RespuestaMB respuestaValidaTransaccion = contexto.validarTransaccion(contexto, esMigrado, "debin", JourneyTransmitEnum.MB_INICIO_SESION);
            if (respuestaValidaTransaccion.hayError())
                return respuestaValidaTransaccion;
        }

        if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendidoHorarioFraude")) {
            if (Util.fueraHorarioFraudes(contexto)) {
                return RespuestaMB.estado("FUERA_HORARIO_F_D", contexto.csmIdAuth);
            }
        }

        Objeto body = new Objeto();

        Objeto comprador = body.set("comprador");
        Objeto compradorCliente = comprador.set("cliente");
        compradorCliente.set("idTributario", objDebinProgramado.string("comprador.cuit"));
        Objeto compradorClienteCuenta = compradorCliente.set("cuenta");
        compradorClienteCuenta.set("cbu", objDebinProgramado.string("comprador.cbu"));

        Objeto vendedor = body.set("vendedor");
        Objeto vendedorCliente = vendedor.set("cliente");
        vendedorCliente.set("idTributario", objDebinProgramado.string("vendedor.cuit"));

        Objeto debin = body.set("debin");
        Objeto moneda = debin.set("moneda");
        moneda.set("id", objDebinProgramado.string("debin.moneda"));
        moneda.set("descripcion", objDebinProgramado.string("debin.moneda").equals("032") ? "Pesos" : "Dolar");
        moneda.set("signo", objDebinProgramado.string("debin.moneda").equals("032") ? "$" : "u$s");
        debin.set("prestacion", objDebinProgramado.string("debin.prestacion"));
        debin.set("referencia", objDebinProgramado.string("debin.referencia"));

        body.set("id", id);
        body.set("autorizacion", aceptaDebinProgramado);

        ApiRequestMB request = ApiMB.request("ConfirmarDebinProgramado", "debin", "POST", "/v1/compradores/confirmarrecurrencia", contexto);
        request.body(body);

        ApiResponseMB response = ApiMB.response(request, contexto.idCobis());

        String cbu = objDebinProgramado.string("comprador.cbu");
        Cuenta cuentaUsuario = contexto.cuentaPorCBU(cbu);
        if (cuentaUsuario == null) {
            return RespuestaMB.estado("CUENTA_NO_ENCONTRADA", contexto.csmIdAuth);
        }

        if (ConfigMB.bool("log_transaccional", false)) {
            try {

                String codigoError = (response == null) ? "ERROR"
                        : response.hayError() ? response.string("codigo") : "0";

                String transaccion = codigoError.equals("0") ? response.string("recibo") : "";
                transaccion += "|" + contexto.persona().cuit() + "|" + objDebinProgramado.string("comprador.cuit");

                String descripcionError = "";
                if (response != null && !codigoError.equals("0")) {
                    descripcionError += response.string("codigo") + ".";
                    descripcionError += response.string("mensajeAlUsuario") + ".";
                }
                descripcionError = descripcionError.length() > 990 ? descripcionError.substring(0, 990)
                        : descripcionError;

                BigDecimal importe = objDebinProgramado.bigDecimal("debin.importe");
                String cuentaDestinoCbu = objDebinProgramado.existe("vendedor.cbu") ?
                        objDebinProgramado.string("vendedor.cbu") : objDebinProgramado.string("vendedor.cuit");

                InsertAuditorTransferencia(contexto, "InsertAuditorDebinAlta", "acepta_debin_programado", request, codigoError,
                        descripcionError, transaccion, importe, cuentaDestinoCbu, null, cuentaUsuario);
            } catch (Exception e) {
            }
        }
        String codigoRespuesta = response != null ? response.string("codigo") : "6399";
        if (!response.hayError()) {
            codigoRespuesta = "6300";
        }

        if (codigoRespuesta != null) {
            if (ConfigMB.string("debin_programado_codigos_exito").contains(codigoRespuesta)) {
                mensajeRespuesta = RestDebin.descripcionRespuestaCodigoDebin(codigoRespuesta);
            }
            if (ConfigMB.string("debin_programado_codigos_warning").contains(codigoRespuesta)) {
                mensajeRespuesta = RestDebin.descripcionRespuestaCodigoDebin(codigoRespuesta);
                return RespuestaMB.estado("WARNING", contexto.csmIdAuth).set("mensaje", mensajeRespuesta);
            }
            if (ConfigMB.string("debin_programado_codigos_error").contains(codigoRespuesta)) {
                mensajeRespuesta = RestDebin.descripcionRespuestaCodigoDebin(codigoRespuesta);
                return RespuestaMB.error(contexto.csmIdAuth).set("mensaje", mensajeRespuesta);
            }
        } else {
            mensajeRespuesta = RestDebin.descripcionRespuestaCodigoDebin("6399");
            return RespuestaMB.error(contexto.csmIdAuth).set("mensaje", mensajeRespuesta);
        }

        contexto.limpiarSegundoFactor();
        RestDebin.eliminarCacheDetalleDebin(contexto, id);
        ProductosService.eliminarCacheProductos(contexto);

        // Comprobante
        if (aceptaDebinProgramado) {
            Map<String, String> comprobante = new HashMap<>();
            String idComprobante = "debinProgramado" + "_" + id;
            contexto.sesion().setComprobante(idComprobante, comprobante);
            return RespuestaMB.exito()
                    .set("idComprobante", idComprobante)
                    .set("mensaje", mensajeRespuesta)
                    .set("csmIdAuth", contexto.csmIdAuth);

        }

        return RespuestaMB.exito().set("mensaje", mensajeRespuesta).set("csmIdAuth", contexto.csmIdAuth);
    }

    public static Object bajaDebinProgramado(ContextoMB contextoMB) {
        String id = contextoMB.parametros.string("id");
        if (Objeto.anyEmpty(id)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        if (MBAplicacion.funcionalidadPrendida(contextoMB.idCobis(), "prendidoHorarioFraude")) {
            if (Util.fueraHorarioFraudes(contextoMB)) {
                return RespuestaMB.estado("FUERA_HORARIO_F_D");
            }
        }

        //TODO: ACA busqueda de la recurrecia a aceptar/rechazar.
        Date fechaDesde = new Date("01/01/1900");
        Date currentDate = new Date(System.currentTimeMillis());

        Futuro<ApiResponseMB> responseLista = new Futuro<>(() -> RestDebin.consultaDebinesProgramados(contextoMB, fechaDesde, currentDate));

        if (responseLista.get().hayError()) {
            String codigoRespuesta = responseLista.get().string("codigo");

            if (!"5100".equals(codigoRespuesta)) {
                return RespuestaMB.error();
            }
        }

        Objeto objDebinProgramado = new Objeto();
        for (Objeto item : responseLista.get().objetos("result.recurrencia")) {
            if (item.string("id").equals(id)) {
                objDebinProgramado = item;
            }
        }
        //TODO: FIN BUSQUEDA

        ApiResponseMB response = null;
        try {
            response = RestDebin.bajaDebinProgramado(contextoMB, id, objDebinProgramado);
        } catch (Exception e) {
        }

        if (response != null && response.hayError()) {
            return RespuestaMB.estado("ERROR_BAJA_RECURRENCIA");
        }

        contextoMB.limpiarSegundoFactor();
        RestDebin.eliminarCacheDetalleDebin(contextoMB, id);
        ProductosService.eliminarCacheProductos(contextoMB);

        Map<String, String> comprobante = new HashMap<>();
        String idComprobante = "debinProgramadoBaja" + "_" + id;
        contextoMB.sesion().setComprobante(idComprobante, comprobante);
        return RespuestaMB.exito()
                .set("idComprobante", idComprobante);

    }

    public static Object cancelarDebinProgramado(ContextoMB contextoMB) {
        String id = contextoMB.parametros.string("id");
        Boolean cancelacionTotal = contextoMB.parametros.bool("cancelacionTotal", false);
        Boolean adelantoCuotas = contextoMB.parametros.bool("adelantoCuotas", false);
        String csmId = contextoMB.parametros.string("csmId");
        String checksum = contextoMB.parametros.string("checksum");

        Futuro<List<Cuenta>> cuentas = new Futuro<>(contextoMB::cuentas);
        List<Cuenta> cuentasPeso = new ArrayList<>();
        List<Cuenta> cuentasDolar = new ArrayList<>();

        if (Objeto.anyEmpty(id)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        if (MBAplicacion.funcionalidadPrendida(contextoMB.idCobis(), "prendidoHorarioFraude")) {
            if (Util.fueraHorarioFraudes(contextoMB)) {
                return RespuestaMB.estado("FUERA_HORARIO_F_D");
            }
        }

        boolean esMigrado = Util.migracionCompleta() || (!Util.migracionCompleta() && TransmitMB.esUsuarioMigrado(contextoMB,
                contextoMB.idCobis(),
                ar.com.hipotecario.backend.base.Util.documento(contextoMB.persona().numeroDocumento())));

        if (esMigrado && Objeto.anyEmpty(csmId, checksum))
            return RespuestaMB.parametrosIncorrectos();

        RespuestaMB respuestaValidaTransaccion = contextoMB.validarTransaccion(contextoMB, esMigrado, "debin", JourneyTransmitEnum.MB_INICIO_SESION);
        if (respuestaValidaTransaccion.hayError())
            return respuestaValidaTransaccion;

        ApiResponseMB responseDetalleDebin = RestDebin.detalleDebin(contextoMB, id);
        if (responseDetalleDebin.hayError()) {
            return RespuestaMB.error();
        }

        for (Cuenta cuenta : cuentas.get()) {
            if (cuenta.esCajaAhorro() && cuenta.esPesos() && cuenta.titularidad().equals("T")) {
                cuentasPeso.add(cuenta);
            }

            if (cuenta.esCajaAhorro() && cuenta.esDolares() && cuenta.titularidad().equals("T")) {
                cuentasDolar.add(cuenta);
            }
        }

        //TODO: ACA busqueda de la recurrecia a aceptar/rechazar.
        Date fechaDesde = new Date("01/01/1900");
        Date currentDate = new Date(System.currentTimeMillis());

        Futuro<ApiResponseMB> responseLista = new Futuro<>(() -> RestDebin.consultaDebinesProgramados(contextoMB, fechaDesde, currentDate));

        if (responseLista.get().hayError()) {
            String codigoRespuesta = responseLista.get().string("codigo");

            if (!"5100".equals(codigoRespuesta)) {
                return RespuestaMB.error();
            }
        }

        Objeto objDebinProgramado = new Objeto();
        for (Objeto item : responseLista.get().objetos("result.recurrencia")) {
            if (item.string("id").equals(id)) {
                objDebinProgramado = item;
            }
        }
        //TODO: FIN BUSQUEDA

        ApiResponseMB response = null;
        try {
            response = RestDebin.bajaDebinProgramado(contextoMB, id, objDebinProgramado);
        } catch (Exception e) {

        }

        if (response.hayError()) {
            return RespuestaMB.estado("ERROR_BAJA_RECURRENCIA");
        }

        contextoMB.limpiarSegundoFactor();
        RestDebin.eliminarCacheDetalleDebin(contextoMB, id);
        ProductosService.eliminarCacheProductos(contextoMB);

        return RespuestaMB.exito().set("csmIdAuth", contextoMB.csmIdAuth);
    }
}
