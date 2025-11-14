package ar.com.hipotecario.canal.homebanking.api;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import ar.com.hipotecario.backend.servicio.api.transmit.JourneyTransmitEnum;
import ar.com.hipotecario.backend.servicio.api.transmit.LibreriaFraudes;
import ar.com.hipotecario.backend.util.Transmit;
import ar.com.hipotecario.canal.homebanking.servicio.*;
import ar.com.hipotecario.canal.libreriariesgofraudes.infrastructure.gateway.model.bm.hb.TransactionHBBMBankProcess;

import org.codehaus.plexus.util.StringUtils;

import ar.com.hipotecario.backend.base.Futuro;
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
import ar.com.hipotecario.canal.homebanking.excepcion.ApiException;
import ar.com.hipotecario.canal.homebanking.lib.Formateador;
import ar.com.hipotecario.canal.homebanking.lib.Util;
import ar.com.hipotecario.canal.homebanking.negocio.Cuenta;
import ar.com.hipotecario.canal.homebanking.negocio.CuentaTercero;
import ar.com.hipotecario.canal.homebanking.negocio.TarjetaDebito;

public class HBTransferenciaV2 {

    /* ========== TRANSFERENCIA ========== */
    public static String validacionCuentaDestino(ContextoHB contexto, Cuenta cuenta, String cuentaDestino) {
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

    public static Respuesta transferir(ContextoHB contexto) {
        SqlTransferencia sqlTransferencia = new SqlTransferencia();
        DisableService disableService = new DisableService();
        String cuentaOrigen = contexto.parametros.string("cuentaOrigen");
        String cuentaDestino = contexto.parametros.string("cuentaDestino");
        String beneficiaryId = contexto.parametros.string("beneficiarioId");
        BigDecimal monto = contexto.parametros.bigDecimal("monto");
        String concepto = contexto.parametros.string("concepto", "VAR");
        Boolean empleadoDomestico = contexto.parametros.bool("empleadoDomestico", false);
        Boolean agendar = contexto.parametros.bool("agendar", false);
        String comentario = contexto.parametros.string("comentario", "");
        String descripcion = contexto.parametros.string("descripcion", "");
        String email = contexto.parametros.string("email", "");
        String forzarEmailOrigen = contexto.parametros.string("forzarEmailOrigen", null);
        Boolean aceptaDDJJ = contexto.parametros.bool("aceptaDDJJ", false);
        String csmId = contexto.parametros.string("csmId", "");
        String checksum = contexto.parametros.string("checksum", "");

        boolean hb_salesforce_prendido_transferencia_in_out = ConfigHB.string("salesforce_prendido_transferencia_in_out").equals("true");

        if (contexto.idCobis() == null) {
            return Respuesta.estado("SIN_PSEUDO_SESION");
        }

        if (Objeto.anyEmpty(cuentaOrigen, cuentaDestino, monto))
            return Respuesta.parametrosIncorrectos();

        boolean esMigrado = contexto.esMigrado(contexto);

        if (HBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_modo_transaccional_nuevo_beneficiario",
                "prendido_modo_transaccional_nuevo_beneficiario_cobis") && !TransmitHB.isChallengeOtp(contexto, "transferencia")) {
            try {
                String sessionToken = contexto.parametros.string(Transmit.getSessionToken(), null);
                if (Objeto.empty(sessionToken))
                    return Respuesta.parametrosIncorrectos();

                Cuenta cuentaPayer = contexto.cuenta(cuentaOrigen);
                Futuro<CuentaTercero> futuroCuentaPayee = new Futuro<>(() -> new CuentaTercero(contexto, cuentaDestino));
                CuentaTercero cuentaPayee = futuroCuentaPayee.tryGet();

                if (Objeto.anyEmpty(cuentaPayer, cuentaPayee)) {
                    return Respuesta.error();
                }

                Futuro<SqlResponse> futuroSqlResponse = new Futuro<>(() -> new SqlTransferencia().obtenerContactoAgendadoById(contexto.idCobis(),
                        cuentaPayee.cbu(), cuentaPayee.numero() == null ? "" : cuentaPayee.numero()));
                SqlResponse sqlResponse = futuroSqlResponse.tryGet();

                if (!sqlResponse.hayError && !sqlResponse.registros.isEmpty()) {
                    TransactionHBBMBankProcess transactionHBBMBankProcess = new TransactionHBBMBankProcess(contexto.idCobis(),
                            sessionToken,
                            monto,
                            Util.obtenerDescripcionMonedaTransmit(cuentaPayee.idMoneda()), TransmitHB.REASON_TRANSFERENCIA,
                            new TransactionHBBMBankProcess.Payer(contexto.persona().cuit(), cuentaPayer.numero(), Util.BH_CODIGO, TransmitHB.CANAL),
                            new TransactionHBBMBankProcess.Payee(cuentaPayee.cuit(), cuentaPayer.cbu(), Util.BH_CODIGO));

                    Respuesta respuesta = TransmitHB.recomendacionTransmit(contexto, transactionHBBMBankProcess, "transferencia");
                    if (respuesta.hayError())
                        return respuesta;
                }
            } catch (Exception e) {
            }
        }

        // LLAMADAS EN PARALELO
        Futuro<SqlResponse> futuroResDispositivo = new Futuro<>(
                () -> obtenerForzadoRegistroDispositivo(contexto.idCobis()));
        Futuro<Boolean> futuroFueraHorarioFraudes = new Futuro<>(() -> Util.fueraHorarioFraudes(contexto));
        String fechaInicio = LocalDateTime.now().plusHours(-1 * disableService.calculateHourDelay(LocalDateTime.now()))
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        Futuro<List<Objeto>> futuroRegistros = new Futuro<>(() -> RestContexto.obtenerContador(contexto.idCobis(),
                ConfigHB.string(esClienteNuevo(contexto, fechaInicio) ? "cambio_information_no_permitido_nuevo" : "cambio_information_no_permitido"),
                fechaInicio));
        Futuro<Boolean> futuroIsContactoTransferenciaAgendado = new Futuro<>(
                () -> RestContexto.agendada(contexto, beneficiaryId));
        Futuro<Boolean> futuroPermission = new Futuro<>(
                () -> disableService.getEnabledToOperator(futuroRegistros.get()));
        Futuro<String> futuroValidacionCuentaDestino = new Futuro<>(
                () -> validacionCuentaDestino(contexto, contexto.cuenta(cuentaOrigen), cuentaDestino));
        Futuro<ApiResponse> futuroResponseLimiteByCuenta = new Futuro<>(() -> RestTransferencia.cuentasGetLimites(
                contexto, contexto.cuenta(cuentaOrigen), true, new SimpleDateFormat("yyyy-MM-dd").format(new Date())));
        // FIN LLAMADAS EN PARALELO

        // TODO bloquear en determinados dias y horarios la funcionalidad
        if (HBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendidoHorarioFraude")) {
            SqlResponse resDisposivito = futuroResDispositivo.get();
            if (resDisposivito.registros.isEmpty()) {
                if (futuroFueraHorarioFraudes.get()) {
                    SqlRequest sqlRequestConsulta = Sql.request("Select2AgendaTransferencias", "hbs");
                    sqlRequestConsulta.sql = "SELECT * FROM [Hbs].[dbo].[agenda_transferencias] WHERE nro_cliente = ? AND (cbu_destino = ? OR nro_cuenta_destino = ? OR nro_cuenta_destino = ?)";
                    sqlRequestConsulta.parametros.add(contexto.idCobis());
                    sqlRequestConsulta.parametros.add(cuentaDestino);
                    sqlRequestConsulta.parametros.add(cuentaDestino);
                    sqlRequestConsulta.parametros.add(beneficiaryId);
                    SqlResponse res = Sql.response(sqlRequestConsulta);
                    if (res.registros.isEmpty()) {
                        return Respuesta.estado("FUERA_HORARIO_F", contexto.csmIdAuth);
                    }
                }
            }
        }

        Boolean prendidoTransferenciasHaberes = true;

        Cuenta cuenta = contexto.cuenta(cuentaOrigen);
        if (cuenta == null) {
            return Respuesta.error();
        }

        Boolean transferenciaCuentaPropia = contexto.cuenta(cuentaDestino) != null;

        // ==========INICIO===========
        if (!transferenciaCuentaPropia) {
            if (Objeto.anyEmpty(beneficiaryId)) {
                return Respuesta.parametrosIncorrectos();
            }

            List<Objeto> registros = futuroRegistros.get();
            Boolean isContactoTransferenciaAgendado = futuroIsContactoTransferenciaAgendado.get();
            if (Objects.isNull(registros) || Objects.isNull(isContactoTransferenciaAgendado)) {
                return Respuesta.estado("ERROR");
            }
            Boolean permission = futuroPermission.get();
            if (!permission && !isContactoTransferenciaAgendado) {
                return Respuesta.estado("ERROR_TRANSFER_BLOCK");
            }
        }
        // ============FIN============
        if (empleadoDomestico) {
            concepto = "HAB";
        }

        String validacionCuentaDestino = futuroValidacionCuentaDestino.get();

        if (transferenciaCuentaPropia) {
            Cuenta cuentaPropiaDestino = contexto.cuenta(cuentaDestino);
            if (!cuenta.idMoneda().equals(cuentaPropiaDestino.idMoneda())) {
                return Respuesta.estado("MONEDAS_DISTINTAS", contexto.csmIdAuth);
            }

            if (!"".equals(validacionCuentaDestino)) {
                return Respuesta.estado(validacionCuentaDestino, contexto.csmIdAuth);
            }

            ApiRequest request = Api.request("TransferenciaCuentaPropia", "cuentas", "POST",
                    "/v2/cuentas/{idcuenta}/transferencias", contexto);

            request.path("idcuenta", cuenta.numero());
            request.query("cuentapropia", "true");
            request.query("inmediata", "false");
            request.query("aceptaDDJJ", aceptaDDJJ.toString());
            request.body("cuentaOrigen", cuenta.numero());
            request.body("importe", monto);
            request.body("reverso", false);
            request.body("cuentaDestino", cuentaPropiaDestino.numero());
            request.body("tipoCuentaOrigen", cuenta.idTipo());
            request.body("tipoCuentaDestino", cuentaPropiaDestino.idTipo());
            request.body("idMoneda", cuenta.idMoneda());
            request.body("idMonedaDestino", cuentaPropiaDestino.idMoneda());
            request.body("modoSimulacion", false);
            request.body("concepto", concepto);
            request.body("descripcionConcepto", concepto);
            request.body("idCliente", contexto.idCobis());

            ApiResponse response = null;
            try {
                response = Api.response(request, new Date().getTime());
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                try {
                    String codigoError = response == null ? "ERROR"
                            : response.hayError() ? response.string("codigo") : "0";
                    String transaccion = codigoError.equals("0") ? response.string("recibo") : null;

                    String descripcionError = "";
                    if (response != null && !codigoError.equals("0")) {
                        descripcionError += response.string("codigo") + ".";
                        descripcionError += response.string("mensajeAlUsuario") + ".";
                    }
                    descripcionError = descripcionError.length() > 990 ? descripcionError.substring(0, 990)
                            : descripcionError;

                    SqlRequest sqlRequest = Sql.request("InsertAuditorTransferenciaCuentaPropia", "hbs");
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
                    sqlRequest.add("propia"); // tipo
                    sqlRequest.add(cuenta.numero()); // cuentaOrigen
                    sqlRequest.add(cuentaPropiaDestino.numero()); // cuentaDestino
                    sqlRequest.add(monto.toString()); // importe
                    sqlRequest.add(cuenta.idMoneda()); // moneda
                    sqlRequest.add(concepto); // concepto
                    sqlRequest.add("true"); // cuentaPropia
                    sqlRequest.add("false"); // servicioDomestico
                    sqlRequest.add("false"); // especial
                    sqlRequest.add(null); // tarjetaDebito
                    sqlRequest.add(transaccion); // transaccion

                    new Futuro<>(() -> Sql.response(sqlRequest));
                } catch (Exception e) {
                }
            }
            ProductosService.eliminarCacheProductos(contexto);
            if (response.hayError()) {
                if (response.string("codigo").equals("258149")) {
                    return Respuesta.estado("ERROR_FUNCIONAL").set("mensaje",
                            ConfigHB.string("leyenda_normativo_7072_transferencia_intra_bh"));
                }
                String error = "ERROR";
                error = response.string("mensajeAlUsuario").contains("CAPTURAR TARJETA USO NO AUTORIZADO")
                        ? "TARJETA_USO_NO_AUTORIZADO"
                        : error;
                if ("400".equals(response.string("codigo")) || "503".equals(response.string("codigo"))) {
                    return Respuesta.estado("ERROR_CONFIRMACION");
                }
                if (response.string("mensajeAlUsuario").equals("Supera el maximo diario de transferencia")) {
                    return Respuesta.estado("LIMITE_DIARIO_TRANSFERENCIA").set("ingresoLimiteMaximoPesos",
                            ConfigHB.integer("ingreso_limite_maximo_transferencia_pesos", 20000000));
                }
                if (response.string("mensajeAlUsuario").contains("Limite No Disponible para realizar la operacion.")) {
                    return Respuesta.estado("LIMITE_DIARIO_TRANSFERENCIA").set("ingresoLimiteMaximoPesos",
                            ConfigHB.integer("ingreso_limite_maximo_transferencia_pesos", 20000000));
                }
                return Respuesta.estado(error);
            }

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

            String simboloMoneda = Formateador.simboloMoneda(response.string("monedaOrigen"));
            Map<String, String> comprobante = new HashMap<>();
            comprobante.put("FECHA_HORA", fechaHora);
            comprobante.put("ID_COMPROBANTE", response.string("recibo"));
            comprobante.put("NOMBRE_BENEFICIARIO", contexto.persona().nombreCompleto());
            comprobante.put("IMPORTE", simboloMoneda + " " + Formateador.importe(response.bigDecimal("monto")));
            comprobante.put("TIPO_TRANSFERENCIA", "A cuenta propia");
            comprobante.put("CUENTA_ORIGEN", request.body().string("cuentaOrigen"));
            comprobante.put("CUENTA_DESTINO", cuentaPropiaDestino.cbu());
            comprobante.put("CUIT_DESTINO", contexto.persona().cuit());
            comprobante.put("CONCEPTO",
                    TransferenciaService.conceptos().get(request.body().string("descripcionConcepto")));
            comprobante.put("COMISION", simboloMoneda + " 0,00");
            comprobante.put("IMPUESTOS", simboloMoneda + " 0,00");

            comprobante.put("NOMBRE_ORIGEN", contexto.persona().nombreCompleto().toUpperCase());
            comprobante.put("NOMBRE_BANCO", "BANCO HIPOTECARIO S.A.");
            comprobante.put("MENSAJE", comentario);

            try {
                String cdestino = request.body().string("cuentaDestino");
                if (RestContexto.primeraTransferencia(contexto, cdestino)) {
                    new Futuro<>(() -> RestContexto.registrarTransferencia(contexto, cdestino));
                }
            } catch (Exception e) {
            }

            if (HBSalesforce.prendidoSalesforce(contexto.idCobis()) && hb_salesforce_prendido_transferencia_in_out) {
                try {
                    Objeto parametros = new Objeto();
                    parametros.set("IDCOBIS", contexto.idCobis());
                    parametros.set("NOMBRE", contexto.persona().nombre());
                    parametros.set("APELLIDO", contexto.persona().apellido());
                    parametros.set("CANAL", "Home Banking");
                    parametros.set("CUENTA_ORIGEN", cuenta.numero().concat(" ").concat(contexto.persona().apellidos())
                            .concat(" ").concat(contexto.persona().nombres()));
                    parametros.set("CUENTA_DESTINO", request.body().string("cuentaDestino").concat(" ").concat(contexto.persona().apellidos())
                            .concat(" ").concat(contexto.persona().nombres()));
                    parametros.set("IMPORTE", simboloMoneda + " " + Formateador.importe(response.bigDecimal("monto")));
                    parametros.set("FECHA", new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
                    parametros.set("NUMERO_OPERACION", response.get("operacion"));
                    parametros.set("TIPO_TRANSFERENCIA", "Propia");
                    parametros.set("MENSAJE", comentario);
                    parametros.set("EMAIL", email);
                    parametros.set("CONCEPTO", concepto);
                    parametros.set("ES_DESTINO", false);
                    parametros.set("CONCEPTO",
                            TransferenciaService.conceptos().get(request.body().string("descripcionConcepto")));

                    new Futuro<>(() -> HBSalesforce.registrarEventoSalesforce(contexto, ConfigHB.string("salesforce_transferencia_in_out"), parametros));
                } catch (Exception e) {

                }
            }

            String idComprobante = "transferencia" + "_" + response.string("recibo");
            contexto.sesion.comprobantes.put(idComprobante, comprobante);
            return Respuesta.exito("idComprobante", idComprobante).set("csmIdAuth", contexto.csmIdAuth);
        }

        Respuesta respuestaPausada = HBTarjetas.verificarTarjetaDebitoPausadaEnCuenta(cuenta, contexto);
        if (respuestaPausada != null)
            return respuestaPausada;

        if (!transferenciaCuentaPropia && cuentaDestino != null) {
            CuentaTercero cuentaTercero = new CuentaTercero(contexto, cuentaDestino);

            String parametroCBU = contexto.parametros.string("cbu", null);
            contexto.parametros.set("cbu", cuentaTercero.cbu());

            if (esMigrado && Objeto.anyEmpty(csmId, checksum))
                return Respuesta.parametrosIncorrectos();

            Respuesta respuestaValidaTransaccion = contexto.validarTransaccion(contexto, esMigrado, "transferencia", JourneyTransmitEnum.HB_INICIO_SESION);
            if (respuestaValidaTransaccion.hayError())
                return respuestaValidaTransaccion;

            contexto.parametros.set("cbu", parametroCBU);

            SqlResponse sqlResponse = sqlTransferencia.obtenerContactoAgendadoById(contexto.idCobis(),
                    cuentaTercero.cbu(), cuentaTercero.numero() == null ? "" : cuentaTercero.numero());

            if (!sqlResponse.hayError && sqlResponse.registros.isEmpty())
                return Respuesta.estado("ERROR_NUEVO_AGENDADO", contexto.csmIdAuth);

            if (agendar && cuentaDestino != null && email != null && comentario != null && concepto != null
                    && descripcion != null) {
                Integer sizeRegistros = sqlResponse.registros.size();
                if (sizeRegistros.equals(0) && !sqlResponse.hayError)
                    new Futuro<>(() -> HBTransferencia.agendarBeneficiario(contexto));
            }

            if (!esMigrado && (contexto.sesion.cbuDestinoValidacionSegundoFactor == null
                    || !cuentaTercero.cbu().equals(contexto.sesion.cbuDestinoValidacionSegundoFactor))) {
                return Respuesta.estado("REQUIERE_SEGUNDO_FACTOR_CBU");
            }

            if (!"".equals(validacionCuentaDestino)) {
                return Respuesta.estado(validacionCuentaDestino, contexto.csmIdAuth);
            }

            Boolean esCuentaBH = cuentaTercero.esCuentaBH();
            Boolean esCuentaCVU = cuentaTercero.esCvu();
            Boolean esCuentaOtroBanco = !esCuentaBH && !esCuentaCVU;

            Boolean esEspecial = esTransferenciaEspecial(contexto, futuroResponseLimiteByCuenta, cuenta, monto);

            Boolean forzadoOldTrxEsp = forzadoOldTrxEsp(contexto);

            if (esCuentaBH) {
                ApiRequest request = null;

                if (HBAplicacion.funcionalidadPrendida("hb_prendido_transf_importe_superior") && esEspecial && !forzadoOldTrxEsp) {
                    request = Api.request("TransferenciaBH", "cuentas", "POST",
                            "/v1/especiales/{cbu}", contexto);
                    TarjetaDebito tarjetaDebitoAsociada = contexto.tarjetaDebitoAsociada(cuenta);
                    request.path("cbu", cuenta.cbu());

                    request.body("mismoTitular", "false");
                    request.body("cuit", contexto.persona().cuit());
                    request.body("cuentaOrigen", cuenta.numero());
                    request.body("importe", monto);
                    request.body("cbu", cuenta.cbu());
                    request.body("cbuDestino", cuentaTercero.cbu());
                    request.body("cuentaDestino", cuentaTercero.numero());
                    request.body("tipoCuentaDestino", cuentaTercero.tipo());
                    request.body("tipoCuentaOrigen", cuenta.idTipo());
                    request.body("cuitDestino", cuentaTercero.cuit());
                    request.body("nombreClienteOrigen", contexto.persona().nombreCompleto());
                    request.body("nombreClienteDestino", cuentaTercero.titular());
                    request.body("idMoneda", cuenta.idMoneda());
                    request.body("idMonedaOrigen", cuenta.idMoneda());
                    request.body("idMonedaDestino", cuentaTercero.idMoneda());
                    request.body("motivo", concepto);
                    request.body("numeroTarjeta", tarjetaDebitoAsociada.numero());
                    request.body("idCliente", contexto.idCobis());
                    request.body("numeroCuentaOrigenPBF", cuenta.numero());
                    request.body("numeroCuentaDestinoPBF", cuentaTercero.cuentaLink.string("cuentaPBF"));
                    request.body("referencia", "REF");
                    Long timestamp = new Date().getTime();
                    request.body("timeStampTransferencia", timestamp);
                } else {
                    request = Api.request("TransferenciaBH", "cuentas", "POST",
                            "/v2/cuentas/{idcuenta}/transferencias", contexto);

                    request.path("idcuenta", cuenta.numero());
                    request.query("cuentapropia", "false");
                    request.query("inmediata", "false");
                    request.query("especial", esEspecial.toString());
                    request.query("aceptaDDJJ", aceptaDDJJ.toString());
                    request.body("cuentaOrigen", cuenta.numero());
                    request.body("importe", monto);
                    request.body("reverso", false);
                    request.body("cuentaDestino", cuentaTercero.numero());
                    request.body("tipoCuentaOrigen", cuenta.idTipo());
                    request.body("tipoCuentaDestino", cuentaTercero.tipo());
                    request.body("idMoneda", cuenta.idMoneda());
                    request.body("idMonedaDestino", cuentaTercero.idMoneda());
                    request.body("modoSimulacion", false);
                    request.body("descripcionConcepto", concepto);
                    request.body("idCliente", contexto.idCobis());
                    request.body("servicio", empleadoDomestico ? "99" : ("HAB".equals(concepto) ? "98" : null));
                }

                ApiResponse response = null;
                try {
                    response = Api.response(request, new Date().getTime());
                } finally {
                    try {
                        String codigoError = response == null ? "ERROR"
                                : response.hayError() ? response.string("codigo") : "0";
                        String transaccion = codigoError.equals("0") ? response.string("recibo") : null;

                        String descripcionError = "";
                        if (response != null && !codigoError.equals("0")) {
                            descripcionError += response.string("codigo") + ".";
                            descripcionError += response.string("mensajeAlUsuario") + ".";
                        }
                        descripcionError = descripcionError.length() > 990 ? descripcionError.substring(0, 990)
                                : descripcionError;

                        SqlRequest sqlRequest = Sql.request("InsertAuditorTransferenciaBH", "hbs");
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
                        sqlRequest.add("bh"); // tipo
                        sqlRequest.add(cuenta.numero()); // cuentaOrigen
                        sqlRequest.add(cuentaTercero.numero()); // cuentaDestino
                        sqlRequest.add(monto); // importe
                        sqlRequest.add(cuentaTercero.idMoneda()); // moneda
                        sqlRequest.add(concepto); // concepto
                        sqlRequest.add("false"); // cuentaPropia
                        sqlRequest.add(empleadoDomestico.toString()); // servicioDomestico
                        sqlRequest.add(esEspecial.toString()); // especial
                        sqlRequest.add(null); // tarjetaDebito
                        sqlRequest.add(transaccion); // transaccion

                        // Sql.response(sqlRequest);
                        new Futuro<>(() -> Sql.response(sqlRequest));
                    } catch (Exception e) {
                    }
                }
                ProductosService.eliminarCacheProductos(contexto);
                if (response.hayError()) {
                    if (response.string("mensajeAlUsuario").contains("FONDOS INSUFICIENTES")) {
                        return Respuesta.estado("SALDO_INSUFICIENTE", contexto.csmIdAuth);
                    }
                    if (response.string("codigo").equals("258149")) {
                        return Respuesta.estado("ERROR_FUNCIONAL", contexto.csmIdAuth).set("mensaje",
                                ConfigHB.string("leyenda_normativo_7072_transferencia_intra_bh"));
                    }
                    if (response.string("mensajeAlUsuario").equals("Supera el maximo diario de transferencia")) {
                        return Respuesta.estado("LIMITE_DIARIO_TRANSFERENCIA", contexto.csmIdAuth).set("ingresoLimiteMaximoPesos",
                                ConfigHB.integer("ingreso_limite_maximo_transferencia_pesos", 20000000));
                    }
                    if (response.string("mensajeAlUsuario")
                            .contains("Limite No Disponible para realizar la operacion.")) {
                        return Respuesta.estado("LIMITE_DIARIO_TRANSFERENCIA", contexto.csmIdAuth).set("ingresoLimiteMaximoPesos",
                                ConfigHB.integer("ingreso_limite_maximo_transferencia_pesos", 20000000));
                    }
                    String error = "ERROR";
                    error = response.string("mensajeAlUsuario").contains("CAPTURAR TARJETA USO NO AUTORIZADO")
                            ? "TARJETA_USO_NO_AUTORIZADO"
                            : error;
                    if ("400".equals(response.string("codigo")) || "503".equals(response.string("codigo"))) {
                        return Respuesta.estado("ERROR_CONFIRMACION", contexto.csmIdAuth);
                    }
                    return Respuesta.estado(error, contexto.csmIdAuth);
                }

                String emailOrigen = forzarEmailOrigen != null && !ConfigHB.esProduccion() ? forzarEmailOrigen
                        : contexto.persona().email();
                String emailDestino = email;

                boolean isSalesforce = HBSalesforce.prendidoSalesforce(contexto.idCobis());
                if (isSalesforce && hb_salesforce_prendido_transferencia_in_out) {
                    Objeto parametros = new Objeto();
                    parametros.set("IDCOBIS", contexto.idCobis());
                    parametros.set("FECHA", new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
                    parametros.set("TIPO_TRANSFERENCIA", "CBU");
                    parametros.set("MENSAJE", comentario);
                    parametros.set("NOMBRE", contexto.persona().nombre());
                    parametros.set("APELLIDO", contexto.persona().apellido());
                    parametros.set("CANAL", "Home Banking");
                    parametros.set("IMPORTE", "$" + Formateador.importe(monto));
                    parametros.set("TIPO_TRANSFERENCIA", "CBU");
                    parametros.set("NUMERO_OPERACION",
                            StringUtils.isNotBlank(response.string("idProceso")) ? response.string("idProceso")
                                    : response.string("operacion"));
                    parametros.set("CONCEPTO",
                            TransferenciaService.conceptos().get(request.body().string("descripcionConcepto")));
                    parametros.set("CUENTA_ORIGEN", cuenta.numero().concat(" ").concat(contexto.persona().apellidos())
                            .concat(" ").concat(contexto.persona().nombres()));
                    parametros.set("CUENTA_DESTINO", cuentaTercero.numero().concat(" ")
                            .concat(StringUtils.capitaliseAllWords(cuentaTercero.titular().toLowerCase())));
                    parametros.set("EMAIL", emailDestino);

                    boolean mandaMailOrigen = emailOrigen != null && !emailOrigen.isEmpty();
                    boolean mandaMailDestino = emailDestino != null && !emailDestino.isEmpty();

                    if (mandaMailOrigen || mandaMailDestino) {
                        if (mandaMailOrigen) {
                            parametros.set("EMAIL", email);
                            parametros.set("ES_DESTINO", false);
                            new Futuro<>(() -> HBSalesforce.registrarEventoSalesforce(contexto, ConfigHB.string("salesforce_transferencia_in_out"), parametros));

                        }
                        if (mandaMailDestino) {
                            parametros.set("ES_DESTINO", true);
                            new Futuro<>(() -> HBSalesforce.registrarEventoSalesforce(contexto, ConfigHB.string("salesforce_transferencia_in_out"), parametros));
                        }
                    } else {
                        new Futuro<>(() -> HBSalesforce.registrarEventoSalesforce(contexto, ConfigHB.string("salesforce_transferencia_in_out"), parametros));
                    }


                } else {
                    if (emailOrigen != null && !emailOrigen.isEmpty()) {
                        ApiRequest requestMail = Api.request("NotificacionesPostCorreoElectronico", "notificaciones",
                                "POST", "/v1/correoelectronico", contexto);
                        requestMail.body("de", "aviso@mail-hipotecario.com.ar");
                        requestMail.body("para", emailOrigen);
                        requestMail.body("plantilla", ConfigHB.string("doppler_transferencia_origen"));
                        Objeto parametros = requestMail.body("parametros");
                        parametros.set("Subject", "Transferencia BH");
                        parametros.set("NOMBRE", contexto.persona().nombres());
                        parametros.set("APELLIDO", contexto.persona().apellidos());
                        parametros.set("CANAL", "Home Banking");
                        parametros.set("CUENTA_ORIGEN", cuenta.numero().concat(" ").concat(contexto.persona().apellidos())
                                .concat(" ").concat(contexto.persona().nombres()));
                        parametros.set("CUENTA_DESTINO", cuentaTercero.numero().concat(" ")
                                .concat(StringUtils.capitaliseAllWords(cuentaTercero.titular().toLowerCase())));
                        parametros.set("IMPORTE", Formateador.importe(monto));
                        parametros.set("NRO_OPERACION",
                                StringUtils.isNotBlank(response.string("idProceso")) ? response.string("idProceso")
                                        : response.string("operacion"));

                        new Futuro<>(() -> Api.response(requestMail, new Date().getTime()));
                    }

                    if (emailDestino != null && !emailDestino.isEmpty()) {
                        ApiRequest requestMail = Api.request("NotificacionesPostCorreoElectronico", "notificaciones",
                                "POST", "/v1/correoelectronico", contexto);
                        requestMail.body("de", "aviso@mail-hipotecario.com.ar");
                        requestMail.body("para", emailDestino);
                        requestMail.body("plantilla", ConfigHB.string("doppler_transferencia_destino"));
                        Objeto parametros = requestMail.body("parametros");
                        parametros.set("Subject", "Transferencia BH");
                        parametros.set("NOMBRE_USUARIO", contexto.persona().nombreCompleto());
                        parametros.set("IMPORTE", Formateador.importe(monto));
                        parametros.set("CUENTA_ORIGEN", request.body().string("cuentaOrigen"));
                        parametros.set("CUENTA_DESTINO", request.body().string("cuentaDestino"));
                        parametros.set("CUENTA_DESTINO_TITULAR",
                                StringUtils.capitaliseAllWords(cuentaTercero.titular().toLowerCase()));
                        parametros.set("FECHA", new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
                        parametros.set("NRO_OPERACION",
                                StringUtils.isNotBlank(response.string("idProceso")) ? response.string("idProceso")
                                        : response.string("operacion"));
                        parametros.set("COMENTARIOS", descripcion);

                        new Futuro<>(() -> Api.response(requestMail, new Date().getTime()));
                    }
                }

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

                String simboloMoneda = Formateador.simboloMoneda(response.string("monedaOrigen"));
                Map<String, String> comprobante = new HashMap<>();
                comprobante.put("FECHA_HORA", fechaHora);
                comprobante.put("ID_COMPROBANTE", response.string("recibo"));
                comprobante.put("NOMBRE_BENEFICIARIO", cuentaTercero.titular());
                comprobante.put("IMPORTE", simboloMoneda + " " + Formateador.importe(response.bigDecimal("monto")));
                if (prendidoTransferenciasHaberes) {
                    comprobante.put("TIPO_TRANSFERENCIA", empleadoDomestico ? "Sueldos – Serv Dom."
                            : ("HAB".equals(concepto) ? "Sueldos - Haberes" : "A cuenta tercero"));
                } else {
                    comprobante.put("TIPO_TRANSFERENCIA",
                            !empleadoDomestico ? "A cuenta tercero" : "Otra Cuenta – Servicio Doméstico");
                }
                comprobante.put("CUENTA_ORIGEN", request.body().string("cuentaOrigen"));
                comprobante.put("CUENTA_DESTINO", cuentaTercero.cbu());
                comprobante.put("CUIT_DESTINO", cuentaTercero.cuit());
                if (prendidoTransferenciasHaberes) {
                    comprobante.put("CONCEPTO",
                            empleadoDomestico ? "Sueldos – Serv Dom."
                                    : ("HAB".equals(concepto) ? "Sueldos - Haberes"
                                    : TransferenciaService.conceptos()
                                    .get(request.body().string("descripcionConcepto"))));
                } else {
                    comprobante.put("CONCEPTO", empleadoDomestico ? "Haberes"
                            : TransferenciaService.conceptos().get(request.body().string("descripcionConcepto")));
                }
                comprobante.put("COMISION", simboloMoneda + " 0,00");
                comprobante.put("IMPUESTOS", simboloMoneda + " 0,00");

                comprobante.put("NOMBRE_ORIGEN", contexto.persona().nombreCompleto().toUpperCase());
                comprobante.put("NOMBRE_BANCO", Util.obtenerNombreBanco(contexto, cuentaTercero.cbu(), ""));
                comprobante.put("MENSAJE", comentario);


                try {
                    String cdestino = request.body().string("cuentaDestino");
                    if (RestContexto.primeraTransferencia(contexto, cdestino)) {
                        new Futuro<>(() -> RestContexto.registrarTransferencia(contexto, cdestino));
                    }
                } catch (Exception e) {
                }

                String idComprobante = "transferencia" + "_" + response.string("recibo");
                contexto.sesion.comprobantes.put(idComprobante, comprobante);
                contexto.limpiarSegundoFactor();
                return Respuesta.exito("idComprobante", idComprobante).set("csmIdAuth", contexto.csmIdAuth);
            }

            if (esCuentaOtroBanco) {
                if (cuentaTercero.cuentaLink.hayError()) {
                    return Respuesta.error();
                }

                TarjetaDebito tarjetaDebitoAsociada = contexto.tarjetaDebitoAsociada(cuenta);
                if (tarjetaDebitoAsociada == null) {
                    return Respuesta.estado("SIN_TARJETA_DEBITO", contexto.csmIdAuth);
                }

                String cuitOrigen = contexto.persona().cuit();
                String cuitDestino = cuentaTercero.cuentaLink.objetos("titulares").get(0).string("idTributario");

                Long timestamp = new Date().getTime();

                ApiRequest request = null;

                if (HBAplicacion.funcionalidadPrendida("hb_prendido_transf_importe_superior") && esEspecial && !forzadoOldTrxEsp) {
                    request = Api.request("TransferenciaBH", "cuentas", "POST",
                            "/v1/especiales/{cbu}", contexto);

                    request.path("cbu", cuenta.cbu());
                } else {

                    request = Api.request("TransferenciaOtroBanco", "cuentas", "POST",
                            "/v2/cuentas/{idcuenta}/transferencias", contexto);

                    request.path("idcuenta", cuenta.numero());
                    request.query("cuentapropia", "false");
                    request.query("inmediata", "true");
                    request.query("especial", esEspecial.toString());
                    request.query("aceptaDDJJ", aceptaDDJJ.toString());
                }
                request.body("cuentaOrigen", cuenta.numero());
                request.body("idMoneda", cuenta.idMoneda());
                request.body("motivo", concepto);

                String descripcionConcepto = ("HAB".equals(concepto)) ? "Sueldos - Haberes"
                        : TransferenciaService.conceptos().get(concepto);

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
                request.body("cbu", cuenta.cbu());
                request.body("cbuDestino", cuentaTercero.cuentaLink.string("cbu"));
                request.body("tipoCuentaDestino", cuentaTercero.cuentaLink.string("tipoProducto"));
                request.body("tipoCuentaOrigen", cuenta.idTipo());
                request.body("notificacionDestinatario", false);
                request.body("numeroTarjeta", tarjetaDebitoAsociada.numero());
                request.body("importe", monto);
                request.body("cuit", cuitOrigen);
                request.body("nombreClienteOrigen", contexto.persona().nombreCompleto());
                request.body("numeroCuentaOrigenPBF", cuenta.numero());
                request.body("timeStampTransferencia", timestamp);
                request.body("idMonedaDestino", cuenta.idMoneda());
                request.body("idMonedaOrigen", cuenta.idMoneda());
                request.body("referencia", "REF");
                request.body("servicio", empleadoDomestico ? "99" : ("HAB".equals(concepto) ? "98" : null));
                request.body("idCliente", contexto.idCobis());

                ApiResponse response = null;
                try {
                    response = Api.response(request, new Date().getTime());
                    ProductosService.eliminarCacheProductos(contexto);
                    if (response.hayError()) {
                        if (response.string("mensajeAlUsuario").contains("SALDOS INSUFICIENTES")) {
                            return Respuesta.estado("SALDO_INSUFICIENTE", contexto.csmIdAuth);
                        }
                        if (response.string("mensajeAlUsuario").contains("CAPTURAR TARJETA USO NO AUTORIZADO")) {
                            return Respuesta.estado("TARJETA_USO_NO_AUTORIZADO", contexto.csmIdAuth);
                        }
                        if (response.string("codigo").equals("258149")) {
                            return Respuesta.estado("ERROR_FUNCIONAL", contexto.csmIdAuth).set("mensaje",
                                    ConfigHB.string("leyenda_normativo_7072_transferencia_intra_bh"));
                        }
                        if (response.string("mensajeAlUsuario").equals("Supera el maximo diario de transferencia")) {
                            return Respuesta.estado("LIMITE_DIARIO_TRANSFERENCIA", contexto.csmIdAuth).set("ingresoLimiteMaximoPesos",
                                    ConfigHB.integer("ingreso_limite_maximo_transferencia_pesos", 20000000));
                        }
                        if (response.string("mensajeAlUsuario")
                                .contains("Limite No Disponible para realizar la operacion.")) {
                            return Respuesta.estado("LIMITE_DIARIO_TRANSFERENCIA", contexto.csmIdAuth).set("ingresoLimiteMaximoPesos",
                                    ConfigHB.integer("ingreso_limite_maximo_transferencia_pesos", 20000000));
                        }
                        throw new ApiException(response);
                    }
                } catch (Exception e) {
                    // TODO: el orquestado de la pescadora lo hace el servicio
//					String idRequerimiento = response.headers.get("x-idtransaccion");
//					try {
//						if (idRequerimiento != null && !idRequerimiento.equals("null")) {
//							ApiRequest requestPescadora = Api.request("PescadoraTransferencia", "cuentas", "GET", "/v1/transferencias", contexto);
//							requestPescadora.query("idrequerimiento", idRequerimiento);
//							requestPescadora.query("numeroTarjeta", tarjetaDebitoAsociada.numero());
//							requestPescadora.query("timestamptransferencia", timestamp.toString());
//							ApiResponse responsePescadora = Api.response(requestPescadora, new Date().getTime());
//							Boolean transferenciaOK = !responsePescadora.hayError() && responsePescadora.string("respuestaTransferencia.codigoRespuesta").equals("00");
//							if (transferenciaOK) {
//								return Respuesta.estado("OK_SIN_COMPROBANTE");
//							}
//						}
//					} catch (Exception ex) {
//					}
                    if (response.string("mensajeAlUsuario").contains("SALDOS INSUFICIENTES")) {
                        return Respuesta.estado("SALDO_INSUFICIENTE", contexto.csmIdAuth);
                    }
                    if (response.string("codigo").equals("258149")) {
                        return Respuesta.estado("ERROR_FUNCIONAL", contexto.csmIdAuth).set("mensaje",
                                ConfigHB.string("leyenda_normativo_7072_transferencia_intra_bh"));
                    }
                    if (response.string("mensajeAlUsuario").equals("Supera el maximo diario de transferencia")) {
                        return Respuesta.estado("LIMITE_DIARIO_TRANSFERENCIA", contexto.csmIdAuth).set("ingresoLimiteMaximoPesos",
                                ConfigHB.integer("ingreso_limite_maximo_transferencia_pesos", 20000000));
                    }
                    if (response.string("mensajeAlUsuario")
                            .contains("Limite No Disponible para realizar la operacion.")) {
                        return Respuesta.estado("LIMITE_DIARIO_TRANSFERENCIA", contexto.csmIdAuth).set("ingresoLimiteMaximoPesos",
                                ConfigHB.integer("ingreso_limite_maximo_transferencia_pesos", 20000000));
                    }
                    String error = "ERROR";
                    error = response.string("mensajeAlUsuario").contains("CAPTURAR TARJETA USO NO AUTORIZADO")
                            ? "TARJETA_USO_NO_AUTORIZADO"
                            : error;
                    if ("400".equals(response.string("codigo")) || "503".equals(response.string("codigo"))) {
                        return Respuesta.estado("ERROR_CONFIRMACION", contexto.csmIdAuth);
                    }
                    return Respuesta.estado(error, contexto.csmIdAuth);
                } finally {
                    try {
                        String codigoError = response == null ? "ERROR"
                                : response.hayError() ? response.string("codigo") : "0";
                        String transaccion = codigoError.equals("0") ? response.string("transaccion") : null;

                        String descripcionError = "";
                        if (response != null && !codigoError.equals("0")) {
                            descripcionError += response.string("codigo") + ".";
                            descripcionError += response.string("mensajeAlUsuario") + ".";
                        }
                        descripcionError = descripcionError.length() > 990 ? descripcionError.substring(0, 990)
                                : descripcionError;

                        SqlRequest sqlRequest = Sql.request("InsertAuditorTransferenciaOtroBanco", "hbs");
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
                        sqlRequest.add("link"); // tipo
                        sqlRequest.add(cuenta.numero()); // cuentaOrigen
                        sqlRequest.add(cuentaTercero.cuentaLink.string("cbu")); // cuentaDestino
                        sqlRequest.add(monto); // importe
                        sqlRequest.add(cuenta.idMoneda()); // moneda
                        sqlRequest.add(concepto); // concepto
                        sqlRequest.add(Boolean.valueOf(cuitOrigen.equals(cuitDestino)).toString()); // cuentaPropia
                        sqlRequest.add(empleadoDomestico.toString()); // servicioDomestico
                        sqlRequest.add(esEspecial.toString()); // especial
                        sqlRequest.add(tarjetaDebitoAsociada.numero()); // tarjetaDebito
                        sqlRequest.add(transaccion); // transaccion

                        // Sql.response(sqlRequest);
                        new Futuro<>(() -> Sql.response(sqlRequest));
                    } catch (Exception e) {
                    }
                }

                String emailOrigen = forzarEmailOrigen != null && !ConfigHB.esProduccion() ? forzarEmailOrigen
                        : contexto.persona().email();
                String emailDestino = email;

                boolean isSalesforce = HBSalesforce.prendidoSalesforce(contexto.idCobis());
                if (isSalesforce && hb_salesforce_prendido_transferencia_in_out) {
                    Objeto parametros = new Objeto();
                    parametros.set("IDCOBIS", contexto.idCobis());
                    parametros.set("FECHA", new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
                    parametros.set("TIPO_TRANSFERENCIA", "CBU");
                    parametros.set("MENSAJE", comentario);
                    parametros.set("NOMBRE", contexto.persona().nombre());
                    parametros.set("APELLIDO", contexto.persona().apellido());
                    parametros.set("CANAL", "Home Banking");
                    parametros.set("IMPORTE", "$" + Formateador.importe(monto));
                    parametros.set("TIPO_TRANSFERENCIA", "CBU");
                    parametros.set("NUMERO_OPERACION",
                            StringUtils.isNotBlank(response.string("idProceso")) ? response.string("idProceso")
                                    : response.string("operacion"));
                    parametros.set("CONCEPTO",
                            TransferenciaService.conceptos().get(request.body().string("descripcionConcepto")));
                    parametros.set("CUENTA_ORIGEN", cuenta.numero().concat(" ").concat(contexto.persona().apellidos())
                            .concat(" ").concat(contexto.persona().nombres()));
                    parametros.set("CUENTA_DESTINO", cuentaTercero.numero().concat(" ")
                            .concat(StringUtils.capitaliseAllWords(cuentaTercero.titular().toLowerCase())));
                    parametros.set("EMAIL", emailDestino);

                    boolean mandaMailOrigen = emailOrigen != null && !emailOrigen.isEmpty();
                    boolean mandaMailDestino = emailDestino != null && !emailDestino.isEmpty();

                    if (mandaMailOrigen || mandaMailDestino) {
                        if (mandaMailOrigen) {
                            parametros.set("EMAIL", email);
                            parametros.set("ES_DESTINO", false);
                            new Futuro<>(() -> HBSalesforce.registrarEventoSalesforce(contexto, ConfigHB.string("salesforce_transferencia_in_out"), parametros));

                        }
                        if (mandaMailDestino) {
                            parametros.set("EMAIL", emailDestino);
                            parametros.set("ES_DESTINO", true);
                            new Futuro<>(() -> HBSalesforce.registrarEventoSalesforce(contexto, ConfigHB.string("salesforce_transferencia_in_out"), parametros));
                        }
                    } else {
                        new Futuro<>(() -> HBSalesforce.registrarEventoSalesforce(contexto, ConfigHB.string("salesforce_transferencia_in_out"), parametros));
                    }
                } else {
                    //A CUENTA DE TERCERO
                    if (emailOrigen != null && !emailOrigen.isEmpty()) {
                        ApiRequest requestMail = Api.request("NotificacionesPostCorreoElectronico", "notificaciones",
                                "POST", "/v1/correoelectronico", contexto);
                        requestMail.body("de", "aviso@mail-hipotecario.com.ar");
                        requestMail.body("para", emailOrigen);
                        requestMail.body("plantilla", ConfigHB.string("doppler_transferencia_origen"));
                        Objeto parametros = requestMail.body("parametros");
                        parametros.set("Subject", "Transferencia BH");
                        parametros.set("NOMBRE", contexto.persona().nombres());
                        parametros.set("APELLIDO", contexto.persona().apellidos());
                        parametros.set("CANAL", "Home Banking");
                        parametros.set("CUENTA_ORIGEN",
                                cuenta.numero().concat(" ").concat(contexto.persona().apellidos().toLowerCase()).concat(" ")
                                        .concat(contexto.persona().nombres().toLowerCase()));
                        parametros.set("CUENTA_DESTINO", cuentaTercero.cuentaLink.string("cuenta").concat(" ")
                                .concat(StringUtils.capitaliseAllWords(cuentaTercero.titularCuentaLink().toLowerCase())));
                        parametros.set("IMPORTE", Formateador.importe(monto));
                        parametros.set("NRO_OPERACION",
                                StringUtils.isNotBlank(response.string("idProceso")) ? response.string("idProceso")
                                        : response.string("operacion"));

                        new Futuro<>(() -> Api.response(requestMail, new Date().getTime()));

                    }


                    if (emailDestino != null && !emailDestino.isEmpty()) {
                        ApiRequest requestMail = Api.request("NotificacionesPostCorreoElectronico", "notificaciones",
                                "POST", "/v1/correoelectronico", contexto);
                        requestMail.body("de", "aviso@mail-hipotecario.com.ar");
                        requestMail.body("para", emailDestino);
                        requestMail.body("plantilla", ConfigHB.string("doppler_transferencia_destino"));
                        Objeto parametros = requestMail.body("parametros");
                        parametros.set("Subject", "Transferencia BH");
                        parametros.set("NOMBRE_USUARIO", contexto.persona().nombreCompleto());
                        parametros.set("IMPORTE", Formateador.importe(monto));
                        parametros.set("CUENTA_DESTINO", cuentaTercero.cuentaLink.string("cuenta"));
                        parametros.set("CUENTA_DESTINO_TITULAR",
                                StringUtils.capitaliseAllWords(cuentaTercero.titular().toLowerCase()));
                        parametros.set("d", new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
                        parametros.set("NRO_OPERACION",
                                StringUtils.isNotBlank(response.string("idProceso")) ? response.string("idProceso")
                                        : response.string("operacion"));
                        parametros.set("COMENTARIOS", descripcion);
                        // Api.response(requestMail, new Date().getTime());
                        new Futuro<>(() -> Api.response(requestMail, new Date().getTime()));

                    }
                }

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

                String simboloMoneda = Formateador.simboloMoneda(response.string("monedaOrigen"));
                Map<String, String> comprobante = new HashMap<>();

                comprobante.put("FECHA_HORA", fechaHora);
                comprobante.put("ID_COMPROBANTE", response.string("operacion"));
                comprobante.put("NOMBRE_BENEFICIARIO", cuentaTercero.titular());
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
                comprobante.put("CONCEPTO", descripcionConcepto);
                comprobante.put("COMISION", "***");
                comprobante.put("IMPUESTOS", "***");

                comprobante.put("NOMBRE_ORIGEN", contexto.persona().nombreCompleto().toUpperCase());
                comprobante.put("NOMBRE_BANCO", Util.obtenerNombreBanco(contexto, request.body().string("cbuDestino"), ""));
                comprobante.put("MENSAJE", comentario);


                try {
                    String cdestino = request.body().string("cbuDestino");
                    if (RestContexto.primeraTransferencia(contexto, cdestino)) {
                        RestContexto.registrarTransferencia(contexto, cdestino);
                    }
                } catch (Exception e) {
                }

                String idComprobante = "transferencia" + "_" + response.string("operacion");
                contexto.sesion.comprobantes.put(idComprobante, comprobante);
                contexto.limpiarSegundoFactor();
                return Respuesta.exito("idComprobante", idComprobante).set("csmIdAuth", contexto.csmIdAuth);
            }

            if (esCuentaCVU) {
                if (cuentaTercero.cuentaCoelsa.hayError()) {
                    if ("ALIAS NO EXISTE".equals(cuentaTercero.cuentaCoelsa.string("mensajeAlUsuario"))) {
                        return Respuesta.estado("ALIAS_NO_EXISTE", contexto.csmIdAuth);
                    } else if (cuentaTercero.cuentaCoelsa.string("codigo").equals("0160")) {
                        return Respuesta.estado("CUENTA_INACTIVA", contexto.csmIdAuth);
                    } else if (cuentaTercero.cuentaCoelsa.string("codigo").equals("0170")) {
                        return Respuesta.estado("ALIAS_NO_EXISTE", contexto.csmIdAuth);
                    } else if ("CBU NO EXISTE".equals(cuentaTercero.cuentaCoelsa.string("mensajeAlUsuario"))) {
                        return Respuesta.estado("CBU_NO_EXISTE", contexto.csmIdAuth);
                    } else
                        return Respuesta.estado("ERROR_COELSA", contexto.csmIdAuth);
                }

                ApiRequest request = null;
                request = Api.request("TransferenciaCVU", "debin", "POST", "/v1/credin/autorizar", contexto);
                Boolean isSalesForce = HBSalesforce.prendidoSalesforce(contexto.idCobis());

                request.query("enviarMail", isSalesForce.toString());

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


                Objeto originante = request.body("originante");
                originante.set("idTributario", contexto.persona().cuit());
                originante.set("nombreCompleto", contexto.persona().nombreCompleto());
                originante.set("mail", contexto.persona().email());
                Objeto cuentaOriginante = originante.set("cuenta");
                cuentaOriginante.set("cbu", cuenta.cbu());
                cuentaOriginante.set("numero", cuenta.numero());
                cuentaOriginante.set("tipo", cuenta.idTipo());
                Objeto sucursalCuentaOriginante = cuentaOriginante.set("sucursal");
                sucursalCuentaOriginante.set("id", String.format("%04d", Integer.valueOf(cuenta.sucursal())));

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

                ApiResponse response = null;
                try {
                    response = Api.response(request, new Date().getTime());
                } finally {
                    try {
                        String codigoError = response == null ? "ERROR"
                                : response.hayError() ? response.string("codigo") : "0";
                        String transaccion = codigoError.equals("0") ? response.string("idCoelsa") : null;

                        String descripcionError = "";
                        if (response != null && !codigoError.equals("0")) {
                            descripcionError += response.string("codigo") + ".";
                            descripcionError += response.string("mensajeAlUsuario") + ".";
                        }
                        descripcionError = descripcionError.length() > 990 ? descripcionError.substring(0, 990)
                                : descripcionError;

                        SqlRequest sqlRequest = Sql.request("InsertAuditorTransferenciaCVU", "hbs");
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
                        sqlRequest.add("cvu"); // tipo
                        sqlRequest.add(cuenta.numero()); // cuentaOrigen
                        sqlRequest.add(cuentaTercero.cuentaCoelsa.string("cbu")); // cuentaDestino
                        sqlRequest.add(monto); // importe
                        sqlRequest.add("80"); // moneda
                        sqlRequest.add(concepto); // concepto
                        sqlRequest.add(null); // cuentaPropia
                        sqlRequest.add("false"); // servicioDomestico
                        sqlRequest.add("false"); // especial
                        sqlRequest.add(null); // tarjetaDebito
                        sqlRequest.add(transaccion); // transaccion

                        // Sql.response(sqlRequest);
                        new Futuro<>(() -> Sql.response(sqlRequest));
                    } catch (Exception e) {
                    }
                }
                ProductosService.eliminarCacheProductos(contexto);
                if (response.hayError()) {
                    if (response.string("codigo").equals("258149")) {
                        return Respuesta.estado("ERROR_FUNCIONAL", contexto.csmIdAuth).set("mensaje",
                                ConfigHB.string("leyenda_normativo_7072_transferencia_intra_bh"));
                    }
                    String error = "ERROR";
                    error = response.string("mensajeAlUsuario").contains("CAPTURAR TARJETA USO NO AUTORIZADO")
                            ? "TARJETA_USO_NO_AUTORIZADO"
                            : error;
                    if ("400".equals(response.string("codigo")) || "503".equals(response.string("codigo"))) {
                        return Respuesta.estado("ERROR_CONFIRMACION", contexto.csmIdAuth);
                    }
                    if (response.string("mensajeAlUsuario").equals("Supera el maximo diario de transferencia")) {
                        return Respuesta.estado("LIMITE_DIARIO_TRANSFERENCIA", contexto.csmIdAuth).set("ingresoLimiteMaximoPesos",
                                ConfigHB.integer("ingreso_limite_maximo_transferencia_pesos", 20000000));
                    }
                    if (response.string("mensajeAlUsuario")
                            .contains("Limite No Disponible para realizar la operacion.")) {
                        return Respuesta.estado("LIMITE_DIARIO_TRANSFERENCIA", contexto.csmIdAuth).set("ingresoLimiteMaximoPesos",
                                ConfigHB.integer("ingreso_limite_maximo_transferencia_pesos", 20000000));
                    }
                    if (response.string("codigo").equals("1875053")) {
                        if (HBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendidoIdentificaCbuCVu")) {
                            return Respuesta.estado("LIMITE_CVU_TRANSFERENCIA", contexto.csmIdAuth);
                        } else {
                            return Respuesta.estado("LIMITE_DIARIO_TRANSFERENCIA", contexto.csmIdAuth).set("ingresoLimiteMaximoPesos",
                                    ConfigHB.integer("ingreso_limite_maximo_transferencia_pesos", 20000000));

                        }

                    }
                    return Respuesta.estado(error, contexto.csmIdAuth);
                }

                String simboloMoneda = "$";
                Map<String, String> comprobante = new HashMap<>();
                comprobante.put("FECHA_HORA",
                        response.date("fechaEjecucion", "yyyy-MM-dd'T'HH:mm:ss", "dd/MM/yyyy HH:mm"));
                comprobante.put("ID_COMPROBANTE", response.string("idCoelsa"));
                comprobante.put("NOMBRE_BENEFICIARIO", cuentaTercero.titular());
                comprobante.put("IMPORTE",
                        simboloMoneda + " " + Formateador.importe(request.body().bigDecimal("detalle.importe")));
                comprobante.put("TIPO_TRANSFERENCIA", "A cuenta virtual");
                comprobante.put("CUENTA_ORIGEN", request.body().string("originante.cuenta.numero"));
                comprobante.put("CUENTA_DESTINO", request.body().string("destinatario.cuenta.cbu"));
                comprobante.put("CUIT_DESTINO", request.body().string("destinatario.idTributario"));
                comprobante.put("CONCEPTO", descripcionConcepto/* request.body().string("detalle.concepto") */);
                comprobante.put("ESTADO", response.string("descripcion"));
                comprobante.put("IMPUESTOS", "***");

                comprobante.put("NOMBRE_ORIGEN", contexto.persona().nombreCompleto().toUpperCase());
                comprobante.put("NOMBRE_BANCO", Util.obtenerNombreBanco(contexto, request.body().string("destinatario.cuenta.cbu"), ""));
                comprobante.put("MENSAJE", comentario);

                String idComprobante = "transferencia-cvu" + "_" + response.string("idCoelsa");

                String cdestino = request.body().string("destinatario.cuenta.cbu");
                try {
                    if (RestContexto.primeraTransferencia(contexto, cdestino))
                        RestContexto.registrarTransferencia(contexto, cdestino);
                } catch (Exception e) {
                }
                if (isSalesForce) {
                    try {
                        Objeto parametros = new Objeto();
                        parametros.set("IDCOBIS", contexto.idCobis());
                        parametros.set("NOMBRE", contexto.persona().nombre());
                        parametros.set("APELLIDO", contexto.persona().apellido());
                        parametros.set("CANAL", "Home Banking");
                        parametros.set("CUENTA_ORIGEN", request.body().string("originante.cuenta.numero").concat(" ").concat(contexto.persona().apellidos().toLowerCase()).concat(" ")
                                .concat(contexto.persona().nombres().toLowerCase()));
						parametros.set("CUENTA_DESTINO", request.body().string("destinatario.cuenta.cbu").concat(" ")
                                .concat(StringUtils.capitaliseAllWords(cuentaTercero.titular().toLowerCase())));
						parametros.set("IMPORTE", simboloMoneda + " " + Formateador.importe(request.body().bigDecimal("detalle.importe")));
						parametros.set("FECHA", response.date("fechaEjecucion", "yyyy-MM-dd'T'HH:mm:ss", "dd/MM/yyyy HH:mm"));
						parametros.set("ES_DESTINO", true);
						parametros.set("TIPO_TRANSFERENCIA", "CVU");
						parametros.set("CONCEPTO", descripcionConcepto);
						parametros.set("MENSAJE", comentario);
						parametros.set("NUMERO_OPERACION", response.string("idCoelsa"));
						parametros.set("EMAIL", email);
						new Futuro<>(() -> HBSalesforce.registrarEventoSalesforce(contexto, ConfigHB.string("salesforce_transferencia_in_out"), parametros));
					}
					catch(Exception e) {
						
					}
				}
                insertarDatosComprobantes(contexto, idComprobante.split("_")[1], cuentaTercero.banco(), cdestino);

                contexto.sesion.comprobantes.put(idComprobante, comprobante);
                contexto.limpiarSegundoFactor();
                return Respuesta.exito("idComprobante", idComprobante).set("csmIdAuth", contexto.csmIdAuth);

            }
        }

        return Respuesta.error(contexto.csmIdAuth);
    }

    // Debido a que fueron agendadas anteriormente al switch propuesto por Link para las transferencias especiales,
    //  se genera una contingencia sólo por el día 22-04-2025 para que estas transferencias vayan por el flujo anterior 
    //y puedan ser realizadas correctamente.    
    public static Boolean forzadoOldTrxEsp(ContextoHB contexto) {
        String cobisHabilitados = ConfigHB.string("hb_prendido_tis_cobis");

        List<String> idCobisHabilitados = Arrays.asList(cobisHabilitados.split("_"));
        boolean habilitado = idCobisHabilitados.contains(contexto.idCobis());

        LocalDate fechaHoy = LocalDate.now();
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate fechaObjetivo = LocalDate.parse("22/04/2025", formato);
        boolean fechaCoincide = fechaHoy.equals(fechaObjetivo);

        if (habilitado && fechaCoincide) {
            return true;
        } else {
            return false;
        }

    }

    public static Boolean esTransferenciaEspecial(ContextoHB contexto, Futuro<ApiResponse> futuroResponseLimiteByCuenta, Cuenta cuenta, BigDecimal monto) {
        Boolean esEspecial = false;

        try {
            ApiResponse responseLimiteByCuenta = futuroResponseLimiteByCuenta.get();

            if (responseLimiteByCuenta.objetos().isEmpty()) {
                esEspecial |= "80".equals(cuenta.idMoneda())
                        && ConfigHB.bigDecimal("configuracion_limite_transferencia_pesos").compareTo(monto) < 0;
                esEspecial |= "2".equals(cuenta.idMoneda())
                        && ConfigHB.bigDecimal("configuracion_limite_transferencia_dolares").compareTo(monto) < 0;
            } else {
                if (!HBAplicacion.funcionalidadPrendida("hb_prendido_transf_importe_superior")) {
                    BigDecimal importe = responseLimiteByCuenta.objetos().get(0).bigDecimal("importe");
                    esEspecial = importe.compareTo(monto) >= 0 ? true : false;
                } else {
                    esEspecial = true;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return esEspecial;

    }

    public static SqlResponse obtenerForzadoRegistroDispositivo(String idCobis) {
        SqlRequest sqlRequest = Sql.request("ConsultarForzadoDispositivosRegistrados", "mobile");
        sqlRequest.sql = "SELECT * FROM [Mobile].[dbo].[registro_dispositivo_forzado] WHERE [id_cobis] = ? AND [forzar_alta] = 1";
        sqlRequest.add(idCobis);
        return Sql.response(sqlRequest);
    }

    private static void insertarDatosComprobantes(ContextoHB contexto, String idComprobante, String nombreBanco, String cuentaNumero) {
        new SqlTransferencia().insertarDatosComprobanteTransferencia(contexto, idComprobante, nombreBanco, cuentaNumero);
    }

    private static boolean esClienteNuevo(ContextoHB contexto, String fechaInicio) {
        return SqlClientesOperadores.esUsuarioNuevo(contexto, fechaInicio);
    }

}