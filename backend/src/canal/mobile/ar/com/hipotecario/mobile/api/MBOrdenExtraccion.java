package ar.com.hipotecario.mobile.api;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import ar.com.hipotecario.backend.servicio.api.transmit.JourneyTransmitEnum;
import ar.com.hipotecario.mobile.ConfigMB;
import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.RespuestaMB;
import ar.com.hipotecario.mobile.conector.ApiMB;
import ar.com.hipotecario.mobile.conector.ApiRequestMB;
import ar.com.hipotecario.mobile.conector.ApiResponseMB;
import ar.com.hipotecario.mobile.lib.Formateador;
import ar.com.hipotecario.mobile.lib.Objeto;
import ar.com.hipotecario.mobile.lib.Texto;
import ar.com.hipotecario.mobile.negocio.Cuenta;
import ar.com.hipotecario.mobile.negocio.TarjetaDebito;
import ar.com.hipotecario.mobile.servicio.RestLogsFraudes;
import ar.com.hipotecario.mobile.servicio.RestLogsFraudes.Ode;
import ar.com.hipotecario.mobile.servicio.RestNotificaciones;

public class MBOrdenExtraccion {

    public static RespuestaMB consolidada(ContextoMB contexto) {
        String fechaDesde = contexto.parametros.date("fechaDesde", "d/M/yyyy", "ddMMyyyy");
        String fechaHasta = contexto.parametros.date("fechaHasta", "d/M/yyyy", "ddMMyyyy");
        String filtrarPorDni = contexto.parametros.string("filtrarPorDni", "");
        String filtrarPorEstado = contexto.parametros.string("filtrarPorEstado", "");

        Boolean flag = !ConfigMB.esOpenShift() && "true".equals(contexto.headers().get("ode"));

        if (Objeto.anyEmpty(fechaDesde, fechaHasta)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        RespuestaMB respuesta = new RespuestaMB();
        for (TarjetaDebito tarjetaDebito : contexto.tarjetasDebito()) {
            if (tarjetaDebito.activacionTemprana()) {
                continue;
            }

            ApiResponseMB response = !flag ? odes(contexto, tarjetaDebito.numero(), fechaDesde, fechaHasta) : odes(contexto, "4998590000000261", fechaDesde, fechaHasta);
            if (response.hayError()) {
                continue;
            }

            for (Objeto item : response.objetos("odesGeneradas")) {
                item = item.objeto("odeGenerada");

                if (!filtrarPorDni.isEmpty() && !item.string("documento.numeroDocumento").equals(filtrarPorDni)) {
                    continue;
                }
                if (!filtrarPorEstado.isEmpty() && !item.string("estadoODE").equalsIgnoreCase(filtrarPorEstado)) {
                    continue;
                }

                String id = tarjetaDebito.numero() + "_";
                id += item.string("cuenta.tipoCuenta") + "_";
                id += item.string("cuenta.cuentaPBF") + "_";
                id += item.string("fechaCreacion") + "_";
                id += item.string("numeroSecuencia") + "";

                Objeto datos = new Objeto();
                datos.set("id", id);
                datos.set("referencia", item.string("referencia"));
                datos.set("dni", item.string("documento.numeroDocumento"));
                datos.set("fechaVencimiento", item.date("fechaVencimiento", "ddMMyyyy", "dd/MM/yyyy"));
                datos.set("horaVencimiento", item.date("horaCreacion", "HHmmss", "HH:mm"));
                datos.set("simboloMoneda", "$");
                datos.set("monto", item.bigDecimal("importe"));
                datos.set("montoFormateado", Formateador.importe(item.bigDecimal("importe")));
                datos.set("estado", item.string("estadoODE"));
                datos.set("estadoFormateado", Texto.primeraMayuscula(item.string("estadoODE")));
                datos.set("puedeCancelar", Objeto.setOf("PENDIENTE").contains(item.string("estadoODE")));
                datos.set("puedeDescargarComprobante", Objeto.setOf("EXTRAIDA").contains(item.string("estadoODE")));
                if (item.string("cuenta.cuentaPBF").startsWith("3")) {
                    datos.set("cuentaDebitoFormateada", "CC XXXX-" + Formateador.ultimos4digitos(item.string("cuenta.cuentaPBF")));
                } else {
                    datos.set("cuentaDebitoFormateada", "CA XXXX-" + Formateador.ultimos4digitos(item.string("cuenta.cuentaPBF")));
                }
                datos.set("fechaAlta", item.date("fechaCreacion", "ddMMyyyy", "dd/MM/yyyy"));
                datos.set("horaAlta", item.date("horaCreacion", "HHmmss", "HH:mm"));
                respuesta.add("datos", datos);
            }
            if (flag) {
                break;
            }
        }
        return respuesta;
    }

    public static RespuestaMB crear(ContextoMB contexto) {
        String numeroDocumento = contexto.parametros.string("numeroDocumento");
        String nombreCompleto = contexto.parametros.string("nombreCompleto");
        String idCuenta = contexto.parametros.string("idCuenta");
        BigDecimal importe = contexto.parametros.bigDecimal("importe");
        Boolean enviarMail = contexto.parametros.bool("enviarMail");
        String mail = contexto.parametros.string("mail");
        String csmId = contexto.parametros.string("csmId", "");
        String checksum = contexto.parametros.string("checksum", "");

        if (Objeto.anyEmpty(numeroDocumento, nombreCompleto, idCuenta, importe))
            return RespuestaMB.parametrosIncorrectos();

        boolean esMigrado = contexto.esMigrado(contexto);
        if (esMigrado && Objeto.anyEmpty(csmId, checksum))
            return RespuestaMB.parametrosIncorrectos();

        Boolean flag = !ConfigMB.esOpenShift() && "true".equals(contexto.headers().get("ode"));

        if (nombreCompleto.length() > 20) {
            return RespuestaMB.estado("NOMBRE_COMPLETO_DEMASIADO_LARGO");
        }

        /* valido que de front no llegue mal el importe */
        Integer montoMinimoOrdenExtraccion = ConfigMB.integer("monto_minimo_orden_extraccion", 500);
        Integer montoMaximoOrdenExtraccion = ConfigMB.integer("monto_maximo_orden_extraccion", 10000);
        Integer stepOrdenExtraccion = ConfigMB.integer("step_orden_extraccion", 100);
        if (importe.compareTo(new BigDecimal(montoMinimoOrdenExtraccion)) < 0) {
            return RespuestaMB.estado("ERROR_MONTO_MINIMO_ODE");
        }
        if (importe.compareTo(new BigDecimal(montoMaximoOrdenExtraccion)) > 0) {
            return RespuestaMB.estado("ERROR_MONTO_MAXIMO_ODE");
        }
        if (importe.intValue() % stepOrdenExtraccion != 0) {
            return RespuestaMB.estado("ERROR_STEP_ODE");
        }

        RespuestaMB respuestaValidaTransaccion = contexto.validarTransaccion(contexto, esMigrado, "orden-extraccion", JourneyTransmitEnum.MB_INICIO_SESION);
        if (respuestaValidaTransaccion.hayError())
            return respuestaValidaTransaccion;

        Cuenta cuenta = contexto.cuenta(idCuenta);
        if (cuenta == null) {
            return RespuestaMB.estado("CUENTA_NO_ENCONTRADA", contexto.csmIdAuth);
        }

        RespuestaMB respuestaPausada = MBTarjetas.verificarTarjetaDebitoPausadaEnCuenta(cuenta, contexto);
        if (respuestaPausada != null)
            return respuestaPausada.set("csmIdAuth", contexto.csmIdAuth);

        TarjetaDebito tarjetaDebito = contexto.tarjetaDebitoAsociadaHabilitadaLink(cuenta);
        if (tarjetaDebito == null) {
            return RespuestaMB.estado("TARJETA_DEBITO_NO_ENCONTRADA", contexto.csmIdAuth);
        }
        if (!MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendida_ode_td_virtual")) {
            if (tarjetaDebito.virtual()) {
                return RespuestaMB.estado("OPERACION_NO_PERMITIDA_PARA_TD_VIRTUAL", contexto.csmIdAuth);
            }
        }
        ApiRequestMB request = ApiMB.request("CrearOde", "link", "POST", "/v1/puntoefectivo/{numeroTarjeta}/ode", contexto);
        request.path("numeroTarjeta", !flag ? tarjetaDebito.numero() : "4998590000000261");
        request.body("importe", importe.setScale(2));
        request.body("tipoDocumento", "1");
        request.body("numeroDocumento", numeroDocumento);
        request.body("referencia", nombreCompleto);
        request.body("pin", "");

        if (cuenta.esCajaAhorro() && cuenta.esPesos()) {
            request.body("tipoCuenta", "11");
        }
        if (cuenta.esCuentaCorriente() && cuenta.esPesos()) {
            request.body("tipoCuenta", "01");
        }
        if (cuenta.esCajaAhorro() && cuenta.esDolares()) {
            request.body("tipoCuenta", "07");
        }
        request.body("cuentaPBF", !flag ? cuenta.numero() : "403400016795278");

        ApiResponseMB response = ApiMB.response(request);

        try {

            Ode registroOde = new Ode();
            registroOde.importe = importe.setScale(2);
            registroOde.numeroDocumento = numeroDocumento;
            registroOde.referencia = nombreCompleto;

            RestLogsFraudes.insertLogODE(registroOde, response, request);

        } catch (Exception e) {
        }

        if (response.hayError()) {
            if (response.string("codigo").equals("602")) {
                return RespuestaMB.estado("NUMERO_DOCUMENTO_INVALIDO", contexto.csmIdAuth);
            }
            if (response.string("codigo").equals("114")) {
                return RespuestaMB.estado("IMPORTE_INVALIDO", contexto.csmIdAuth);
            }
            if (response.string("codigo").equals("B1")) {
                return RespuestaMB.estado("LIMITE_DIARIO", contexto.csmIdAuth);
            }
            if (response.string("codigo").equals("B2")) {
                return RespuestaMB.estado("LIMITE_CANTIDAD_DIARIA", contexto.csmIdAuth);
            }
            return RespuestaMB.error(contexto.csmIdAuth);
        }

        // comprobante
        Map<String, String> comprobante = new HashMap<>();
        comprobante.put("FECHA_HORA", response.date("fechaCreacion", "yyMMdd", "dd/MM/yyyy") + " " + response.date("horaCreacion", "HHmmss", "HH:mm") + " hs");
        comprobante.put("ID_COMPROBANTE", response.string("numeroSecuencia"));
        comprobante.put("NOMBRE_BENEFICIARIO", nombreCompleto);
        comprobante.put("IMPORTE", "$" + " " + Formateador.importe(importe));
        comprobante.put("CUENTA", cuenta.descripcionCorta() + " " + cuenta.numero());
        comprobante.put("DOCUMENTO", numeroDocumento);
        comprobante.put("PIN", response.string("pinODE"));
        comprobante.put("FECHA_VENCIMIENTO", response.date("fechaVencimiento", "yyMMdd", "dd/MM/yyyy"));

        String idComprobante = "ode" + "_" + response.string("numeroSecuencia");
        contexto.sesion().setComprobante(idComprobante, comprobante);

        // se envía mail origen y destinatario
        if (enviarMail) {
            try {
                // envío mail origen
                Objeto parametrosOrigen = new Objeto();
                parametrosOrigen.set("Subject", "Orden de extracción.");
                parametrosOrigen.set("NOMBRE_APELLIDO", contexto.persona().nombre() + " " + contexto.persona().apellido());
                Date hoy = new Date();
                parametrosOrigen.set("FECHA", new SimpleDateFormat("dd/MM/yyyy").format(hoy));
                parametrosOrigen.set("HORA", new SimpleDateFormat("HH:mm").format(hoy));
                parametrosOrigen.set("DESTINATARIO", nombreCompleto);
                parametrosOrigen.set("IMPORTE", Formateador.importe(importe));
                parametrosOrigen.set("CANAL", "Home Banking");
                RestNotificaciones.envioMail(contexto, ConfigMB.string("doppler_ode_origen"), parametrosOrigen);

                // envío mail destino
                if (!mail.equals("")) {
                    Objeto parametrosDestino = new Objeto();
                    parametrosDestino.set("Subject", "Orden de extracción.");
                    parametrosDestino.set("NOMBRE_APELLIDO", contexto.persona().nombre() + " " + contexto.persona().apellido());
                    parametrosDestino.set("FECHA", new SimpleDateFormat("dd/MM/yyyy").format(hoy));
                    parametrosDestino.set("HORA", new SimpleDateFormat("HH:mm").format(hoy));
                    parametrosDestino.set("DESTINATARIO", nombreCompleto);
                    parametrosDestino.set("IMPORTE", Formateador.importe(importe));
                    parametrosDestino.set("CANAL", "Home Banking");

                    ApiRequestMB requestMailDestino = ApiMB.request("LoginCorreoElectronico", "notificaciones", "POST", "/v1/correoelectronico", contexto);
                    requestMailDestino.body("de", "aviso@mail-hipotecario.com.ar");
                    requestMailDestino.body("para", mail);
                    requestMailDestino.body("plantilla", ConfigMB.string("doppler_ode_destino"));
                    requestMailDestino.body("parametros", parametrosDestino);
                    requestMailDestino.permitirSinLogin = true;

                    ApiMB.response(requestMailDestino, new Date().getTime());
                }
            } catch (Exception e) {
            }
        }

        // respuesta
        Objeto datos = new Objeto();
        datos.set("pin", response.string("pinODE"));
        datos.set("importe", importe);
        datos.set("importeFormateado", Formateador.importe(importe));
        return RespuestaMB.exito().set("idComprobante", idComprobante).set("datos", datos).set("csmIdAuth", contexto.csmIdAuth);
    }

    public static RespuestaMB eliminar(ContextoMB contexto) {
        String id = contexto.parametros.string("id");

        if (Objeto.anyEmpty(id)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        String numeroTarjetaDebito = id.split("_")[0];
        Objeto ode = ode(contexto, id);
        if (ode == null) {
            return RespuestaMB.error();
        }

        ApiRequestMB request = ApiMB.request("EliminarOde", "link", "DELETE", "/v1/puntoefectivo/{numeroTarjeta}/ode", contexto);
        request.path("numeroTarjeta", numeroTarjetaDebito);
        request.query("idOde", ode.string("idODE"));

        ApiResponseMB response = ApiMB.response(request);
        if (response.hayError()) {
            return RespuestaMB.error();
        }

        return RespuestaMB.exito();
    }

    public static Object comprobante(ContextoMB contexto) {
        String id = contexto.parametros.string("id");

        if (Objeto.anyEmpty(id)) {
            contexto.setHeader("estado", "PARAMETROS_INCORRRECTOS");
            return RespuestaMB.parametrosIncorrectos();
        }

        Objeto ode = ode(contexto, id);
        if (ode == null) {
            contexto.setHeader("estado", "PARAMETROS_INCORRRECTOS");
            return RespuestaMB.error();
        }

        if (!Objeto.setOf("EXTRAIDA").contains(ode.string("estadoODE"))) {
            contexto.setHeader("estado", "ESTADO_INVALIDO");
            return RespuestaMB.error();
        }

        Map<String, String> comprobante = new HashMap<>();
        comprobante.put("FECHA_HORA", ode.date("fechaCreacion", "ddMMyyyy", "dd/MM/yyyy") + " " + ode.date("horaCreacion", "HHmmss", "HH:mm") + " hs");
        comprobante.put("ID_COMPROBANTE", ode.string("numeroSecuencia"));
        comprobante.put("NOMBRE_BENEFICIARIO", ode.string("referencia"));
        comprobante.put("IMPORTE", "$" + " " + Formateador.importe(ode.bigDecimal("importe")));
        comprobante.put("CUENTA", (ode.string("cuenta.cuentaPBF").startsWith("3") ? "CC" : "CA") + " " + ode.string("cuenta.cuentaPBF"));
        comprobante.put("DOCUMENTO", ode.string("documento.numeroDocumento"));
        comprobante.put("PIN", ode.string("pin"));
        comprobante.put("FECHA_VENCIMIENTO", ode.date("fechaVencimiento", "ddMMyyyy", "dd/MM/yyyy"));
        comprobante.put("FECHA_EXTRACCION", ode.date("fechaExtracion", "ddMMyyyy", "dd/MM/yyyy") + " " + ode.date("horaCreacion", "HHmmss", "HH:mm") + " hs");
        comprobante.put("TERMINAL", ode.string("terminalExtracion"));

        String idComprobante = "ode-extraido" + "_" + ode.string("numeroSecuencia");
        contexto.sesion().setComprobante(idComprobante, comprobante);
        contexto.parametros.set("id", idComprobante);
        contexto.setHeader("estado", "0");
        return MBArchivo.comprobante(contexto);
    }

    /* ========== METODOS AUXILIARES ========== */
    private static ApiResponseMB odes(ContextoMB contexto, String numeroTarjetaDebito, String fechaDesde, String fechaHasta) {
        ApiRequestMB request = ApiMB.request("ListarOdes", "link", "GET", "/v1/puntoefectivo/{numeroTarjeta}/odes", contexto);
        request.path("numeroTarjeta", numeroTarjetaDebito);
        request.query("paginaActual", "1");
        request.query("cantidadPorPagina", "100");
        request.query("fechaDesde", fechaDesde);
        request.query("fechaHasta", fechaHasta);

        ApiResponseMB response = ApiMB.response(request);
        return response;
    }

    private static Objeto ode(ContextoMB contexto, String id) {
        String[] datos = id.split("_");
        String numeroTarjetaDebito = datos[0];
        String tipoCuenta = datos[1];
        String cuentaPBF = datos[2];
        String fechaCreacion = datos[3];
        String numeroSecuencia = datos[4];

        ApiRequestMB request = ApiMB.request("BuscarOde", "link", "GET", "/v1/puntoefectivo/{numeroTarjeta}/datosodes", contexto);
        request.path("numeroTarjeta", numeroTarjetaDebito);
        request.query("paginaActual", "1");
        request.query("cantidadPorPagina", "100");
        request.query("fechaDesde", fechaCreacion);
        request.query("fechaHasta", fechaCreacion);
        request.query("tipoCuenta", tipoCuenta);
        request.query("cuenta", cuentaPBF);

        ApiResponseMB response = ApiMB.response(request);
        if (response.hayError()) {
            return null;
        }
        for (Objeto item : response.objetos("odes")) {
            item = item.objeto("ode");
            if (item.string("numeroSecuencia").equals(numeroSecuencia)) {
                return item;
            }
        }
        return null;
    }
}
