package ar.com.hipotecario.mobile.api;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.servicio.api.transmit.JourneyTransmitEnum;
import ar.com.hipotecario.canal.homebanking.conector.Sql;
import ar.com.hipotecario.canal.homebanking.conector.SqlRequest;
import ar.com.hipotecario.mobile.ConfigMB;
import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.RespuestaMB;
import ar.com.hipotecario.mobile.conector.*;
import ar.com.hipotecario.mobile.helper.OmnicanalidadHelper;
import ar.com.hipotecario.mobile.lib.*;
import ar.com.hipotecario.mobile.negocio.Cuenta;
import ar.com.hipotecario.mobile.negocio.TarjetaCredito;
import ar.com.hipotecario.mobile.negocio.TarjetaDebito;
import ar.com.hipotecario.mobile.servicio.*;
import org.apache.commons.lang3.StringUtils;

public class MBOmnicanalidad {

    private final static String OBTENER_DETALLE = "ERROR_OBTENER_DETALLE";
    private final static String ERRORES = "Errores";
    private final static String IR_A_SUCURSAL = "ERROR_IR_A_SUCURSAL";
    private final static String EN_PROCESO_ACTUALIZACION = "ERROR_EN_PROCESO_ACTUALIZACION";
    private final static String FINALIZAR_SOLICITUD = "ERROR_FINALIZAR_SOLICITUD";
    private final static String MENSAJE_CLIENTE = "MensajeCliente";
    private final static String MENSAJE_DESARROLLADOR = "MensajeDesarrollador";
    private final static String ERROR_RESPUESTA = "error";
    private final static String CODIGO_RESPUESTA = "Codigo";
    private final static String DATOS_RESPUESTA = "Datos";
    private final static String CREAR_CAJA_AHORRO = "ERROR_CREAR_CAJA_AHORRO";
    private final static String ACTUALIZAR_CAJA_AHORRO = "ERROR_ACTUALIZAR_CAJA_AHORRO";
    private final static String GENERAR_TARJETA_DEBITO = "ERROR_GENERAR_TARJETA_DEBITO";
    private final static String ACTUALIZAR_TARJETA_DEBITO = "ERROR_ACTUALIZAR_TARJETA_DEBITO";

    /* ========== SOLICITUDES ========== */
    public static RespuestaMB solicitudes(ContextoMB contexto) {
        Long cantidadDias = contexto.parametros.longer("cantidadDias", ConfigMB.longer("solicitud_dias_vigente", 30L));

        if (contexto.persona().esEmpleado() || contexto.persona().esMenor()) {
            return new RespuestaMB();
        }

        ApiResponseMB response = RestOmnicanalidad.consultarSolicitudes(contexto, cantidadDias);
        if (response.hayError() || !response.objetos("Errores").isEmpty()) {
            return new RespuestaMB().setEstado("ERROR").set("error", !response.objetos("Errores").isEmpty() ? response.objetos("Errores").get(0).string("MensajeCliente") : null);
        }

        List<Objeto> objetos = response.objetos("Datos").stream().filter(d -> d.objetos("Integrantes").stream().map(i -> i.string("IdCobis")).toList().contains(contexto.idCobis())).toList();

        RespuestaMB respuesta = new RespuestaMB();
        for (Objeto datos : objetos) {
            String estado = datos.string("Estado");
            String motor = datos.string("ResolucionCodigo");
            Boolean desembolsoOnline = datos.bool("DesembolsoOnline", false);
            if (!Objeto.setOf("RE").contains(motor) && !Objeto.setOf("AA").contains(motor)) {
                if (Objeto.setOf("O").contains(estado) && ConfigMB.bool("prendido_omnicanalidad_solicitudes") && !desembolsoOnline) {
                    String idSolicitud = datos.string("Id");
                    respuesta.add("solicitudes", idSolicitud);
                }
                if (Objeto.setOf("O").contains(estado) && desembolsoOnline) {
                    String idSolicitud = datos.string("Id");
                    respuesta.add("solicitudes", idSolicitud);
                }
            }
        }
        return respuesta;
    }

    public static RespuestaMB solicitudesDesembolsoOnline(ContextoMB contexto) {
        Long cantidadDias = contexto.parametros.longer("cantidadDias", ConfigMB.longer("solicitud_dias_vigente", 30L));

        if (contexto.persona().esEmpleado() || contexto.persona().esMenor()) {
            return new RespuestaMB();
        }

        ApiResponseMB response = RestOmnicanalidad.consultarSolicitudes(contexto, cantidadDias);
        if (response.hayError() || !response.objetos("Errores").isEmpty()) {
            return new RespuestaMB().setEstado("ERROR").set("error", !response.objetos("Errores").isEmpty() ? response.objetos("Errores").get(0).string("MensajeCliente") : null);
        }

        List<Objeto> objetos = response.objetos("Datos").stream().filter(d -> d.objetos("Integrantes").stream().map(i -> i.string("IdCobis")).toList().contains(contexto.idCobis())).toList();

        RespuestaMB respuesta = new RespuestaMB();

        Optional.ofNullable(objetos).map(Collection::stream).orElseGet(Stream::empty).findFirst().ifPresent(dato -> {
            String estado = dato.string("Estado");
            String motor = dato.string("ResolucionCodigo");
            Boolean desembolso = dato.bool("DesembolsoOnline", false);
            Date fechaAlta = dato.date("FechaAlta", "yyyy-MM-dd'T'HH:mm:ss");
            if (!Objeto.setOf("RE").contains(motor) && !Objeto.setOf("AA").contains(motor)) {
                if (Objeto.setOf("O").contains(estado) && desembolso) {
                    int horas = horasRestantes(fechaAlta);
                    if (horas > 0) {
                        String idSolicitud = dato.string("Id");
                        respuesta.set("solicitudes", idSolicitud);
                        respuesta.set("mostrar", true);
                        respuesta.set("mostrarModal", true);
                        respuesta.set("horasRestantes", horas);
                    } else {
                        desistirSolicitud(contexto);
                    }

                }
            }
        });

        return respuesta;
    }

    public static int horasRestantes(Date date) {
        try {
            Long difSeg = new Date().getTime() - date.getTime();
            Double difMin = Math.ceil((double) TimeUnit.MINUTES.convert(difSeg, TimeUnit.MILLISECONDS) / 60);
            return (int) (difMin > 6 ? 0 : (6 - difMin));
        } catch (Exception e) {
            return 0;
        }
    }

    public static Objeto detalleSolicitudesPrestamos(ContextoMB contexto) {
        if (contexto.persona().esEmpleado() || contexto.persona().esMenor()) {
            return new RespuestaMB();
        }

        RespuestaMB respuesta = solicitudes(contexto);
        List<Object> solicitudes = respuesta.objeto("solicitudes").toList();

        if (ConfigMB.bool("prendido_canal_amarillo_pp")) {
            RespuestaMB respuestaCanalAmarillo = solicitudesCanalAmarillo(contexto);
            List<Object> solicitudesCanalAmarillo = respuestaCanalAmarillo.objeto("solicitudes").toList();

            if (solicitudesCanalAmarillo.size() >= 1) { // si tiene solicitud de Canal Amarillo, se desestiman las anteriores
                String idSolicitud = solicitudesCanalAmarillo.get(0).toString();
                if (MBOriginacion.esSolicitudEliminada(contexto, idSolicitud)) {
                    solicitudesCanalAmarillo.remove(0);
                    if (solicitudes.size() >= 1) {
                        idSolicitud = solicitudes.get(0).toString();
                        RestOmnicanalidad.desistirSolicitud(contexto, idSolicitud);
                    }
                }
                solicitudes = solicitudesCanalAmarillo;
            }
        }

        RespuestaMB respuestaFinal = new RespuestaMB();
        Objeto datos = OmnicanalidadHelper.solicitudesPrestamos(solicitudes, contexto);

        Map<String, Objeto> mapa = new LinkedHashMap<>();
        for (Objeto objeto : datos.objetos()) {
            String tipo = objeto.string("tipo");
            mapa.put(tipo, objeto);
        }

        if (mapa.get("ADELANTO") != null) {
            mapa.remove("PRESTAMO");
        }

        for (String clave : mapa.keySet()) {
            String idSolicitud = mapa.get(clave).string("id");
            contexto.parametros.set("idSolicitud", idSolicitud);
            Objeto detalles = MBOmnicanalidad.detalleSolicitud(contexto);
            if (detalles != null && detalles.existe("solicitud")) {
                respuestaFinal.add(detalles.objeto("solicitud"));
            }
        }

        return respuestaFinal;
    }

    public static RespuestaMB detalleSolicitud(ContextoMB contexto) {
        String idSolicitud = contexto.parametros.string("idSolicitud");

        ApiResponseMB response = RestOmnicanalidad.consultarSolicitud(contexto, idSolicitud);
        if (response.hayError()) {
            return RespuestaMB.error();
        }
        Objeto datos = response.objetos("Datos").get(0);

        Objeto solicitud = new Objeto();
        solicitud.set("id", datos.string("IdSolicitud"));
        solicitud.set("fechaHora", datos.date("FechaAlta", "yyyy-MM-dd'T'HH:mm:ss", "dd/MM/yyyy HH:mm:ss"));
        solicitud.set("canal", RestOmnicanalidad.canales().get(datos.string("CanalVenta")));
        solicitud.set("desembolsoOnline", datos.bool("DesembolsoOnline", false));
        solicitud.set("horasRestantes", horasRestantes(datos.date("FechaAlta", "yyyy-MM-dd'T'HH:mm:ss")));
        if (datos.bool("Finalizada") && "AA".equalsIgnoreCase(datos.string("ResolucionCodigo"))) {
            solicitud.set("resolucionCanalAmarillo", true);
        }
        if (datos.bool("Finalizada") && "AA".equalsIgnoreCase(datos.string("ResolucionCodigo")) && "R".equalsIgnoreCase(datos.string("Estado"))) {
            solicitud.set("estadoCanalAmarillo", "Rechazada");
        }

        solicitud.set("productos", productoPrestamo(contexto, idSolicitud, datos));

        RespuestaMB respuesta = new RespuestaMB();
        respuesta.set("solicitud", solicitud);
        return respuesta;
    }

    public static RespuestaMB eliminarProductosExtraordinarios(ContextoMB contexto) {
        String idSolicitud = contexto.parametros.string("idSolicitud");

        if (Objeto.anyEmpty(idSolicitud)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        ApiResponseMB responseSolicitud = RestOmnicanalidad.consultarSolicitud(contexto, idSolicitud);
        if (responseSolicitud.hayError() || !responseSolicitud.objetos("Errores").isEmpty()) {
            return new RespuestaMB().setEstado("ERROR").set("error", !responseSolicitud.objetos("Errores").isEmpty() ? responseSolicitud.objetos("Errores").get(0).string("MensajeCliente") : null);
        }

        for (Objeto producto : responseSolicitud.objetos("Datos").get(0).objetos("Productos")) {
            String tipoProducto = producto.string("tipoProducto");
            if (!RestOmnicanalidad.productosOperables().contains(tipoProducto)) {
                String recurso = RestOmnicanalidad.recursos().get(tipoProducto);
                String idProducto = producto.string("Id");

                ApiResponseMB responseEliminar = RestOmnicanalidad.eliminarProducto(contexto, idSolicitud, recurso, idProducto);
                if (responseEliminar.hayError() || !responseEliminar.objetos("Errores").isEmpty()) {
                    return new RespuestaMB().setEstado("ERROR").set("error", !responseEliminar.objetos("Errores").isEmpty() ? responseEliminar.objetos("Errores").get(0).string("MensajeCliente") : null);
                }
            }
        }

        return RespuestaMB.exito();
    }

    public static RespuestaMB desistirSolicitud(ContextoMB contexto) {
        String idSolicitud = contexto.parametros.string("idSolicitud");

        if (Objeto.anyEmpty(idSolicitud)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        ApiResponseMB response = RestOmnicanalidad.desistirSolicitud(contexto, idSolicitud);
        if (response.hayError() || !response.objetos("Errores").isEmpty()) {
            return new RespuestaMB().setEstado("ERROR").set("error", !response.objetos("Errores").isEmpty() ? response.objetos("Errores").get(0).string("MensajeCliente") : null);
        }

        return RespuestaMB.exito();
    }

    public static RespuestaMB desistirSolicitudBPM(ContextoMB contexto) {
        String idSolicitud = contexto.parametros.string("idSolicitud");

        if (Objeto.anyEmpty(idSolicitud)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        ApiResponseMB response = RestOmnicanalidad.desistirSolicitudBPM(contexto, idSolicitud);
        if (response.hayError() || !response.objetos("Errores").isEmpty()) {
            return new RespuestaMB().setEstado("ERROR").set("error", !response.objetos("Errores").isEmpty() ? response.objetos("Errores").get(0).string("MensajeCliente") : null);
        }

        return RespuestaMB.exito();
    }

    /* ========== CAJA AHORRO ========== */
    public static RespuestaMB crearSolicitudCajaAhorro(ContextoMB contexto) {
        String idMoneda = contexto.parametros.string("idMoneda");

        if (Objeto.anyEmpty(idMoneda)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        if (contexto.persona().esMenor()) {
            return RespuestaMB.estado("MENOR_EDAD");
        }

        String categoria = "MOV";
        if ("2".equals(idMoneda)) {
            categoria = "SC";
        }

        // Solicitud
        ApiResponseMB generarSolicitud = RestOmnicanalidad.generarSolicitud(contexto);
        if (generarSolicitud.hayError() || !generarSolicitud.objetos("Errores").isEmpty()) {
            return new RespuestaMB().setEstado("ERROR").set("error", !generarSolicitud.objetos("Errores").isEmpty() ? generarSolicitud.objetos("Errores").get(0).string("MensajeCliente") : null);
        }
        String idSolicitud = generarSolicitud.objetos("Datos").get(0).string("IdSolicitud");

        // Integrante
        ApiResponseMB generarIntegrante = RestOmnicanalidad.generarIntegrante(contexto, idSolicitud);
        if (generarIntegrante.hayError() || !generarIntegrante.objetos("Errores").isEmpty()) {
            return new RespuestaMB().setEstado("ERROR").set("error", !generarIntegrante.objetos("Errores").isEmpty() ? generarIntegrante.objetos("Errores").get(0).string("MensajeCliente") : null);
        }

        // Caja Ahorro
        ApiResponseMB generarCajaAhorro = RestOmnicanalidad.generarCajaAhorro(contexto, idSolicitud, idMoneda, null, categoria);
        if (generarCajaAhorro.hayError() || !generarCajaAhorro.objetos("Errores").isEmpty()) {
            return new RespuestaMB().setEstado("ERROR").set("error", !generarCajaAhorro.objetos("Errores").isEmpty() ? generarCajaAhorro.objetos("Errores").get(0).string("MensajeCliente") : null);
        }

        RespuestaMB respuesta = new RespuestaMB();
        respuesta.set("idSolicitud", idSolicitud);
        respuesta.set("esMonoProductoTC", contexto.esMonoProductoTC());

        return respuesta;
    }

    public static RespuestaMB seguroAtmProductos(ContextoMB contexto) {
        String idSolicitud = contexto.parametros.string("idSolicitud");
        ApiResponseMB productos = RestOmnicanalidad.solicitudProductos(contexto, idSolicitud);
        if (productos.hayError() || !productos.objetos("Errores").isEmpty()) {
            return new RespuestaMB().setEstado("ERROR").set("error", !productos.objetos("Errores").isEmpty() ? productos.objetos("Errores").get(0).string("MensajeCliente") : null);
        }

        List<Objeto> datos = productos.objetos("Datos");
        for (Objeto objeto : datos) {
            objeto.set("valor1.Descripcion", "Prima");
            objeto.set("valor2.Descripcion", "1er Evento Robo Cajero Automático.");
            objeto.set("valor3.Descripcion", "2o Evento Robo Cajero Automático.");
            objeto.set("valor4.Descripcion", "1er Evento Robo Línea Cajas.");
            objeto.set("valor5.Descripcion", "Reposición Documentación.");
        }
        RespuestaMB respuesta = new RespuestaMB();
        respuesta.set("Datos", datos);
        return respuesta;
    }

    public static RespuestaMB postSolicitudCajaAhorro(ContextoMB contexto) {
        String idSolicitud = contexto.parametros.string("idSolicitud");
        ApiResponseMB postSeguroATM = RestOmnicanalidad.postSeguroATM(contexto, idSolicitud);
        if (postSeguroATM.hayError() || !postSeguroATM.objetos("Errores").isEmpty()) {
            return new RespuestaMB().setEstado("ERROR").set("error", !postSeguroATM.objetos("Errores").isEmpty() ? postSeguroATM.objetos("Errores").get(0).string("MensajeCliente") : null);
        }
        Objeto datos = postSeguroATM.objetos("Datos").get(0);
        RespuestaMB respuesta = new RespuestaMB();
        respuesta.set("idProducto", datos.get("Id").toString());
        return respuesta;
    }

    public static RespuestaMB crearSolicitudSeguroATM(ContextoMB contexto) {

        // Solicitud
        ApiResponseMB generarSolicitud = RestOmnicanalidad.generarSolicitud(contexto);
        if (generarSolicitud.hayError() || !generarSolicitud.objetos("Errores").isEmpty()) {
            return new RespuestaMB().setEstado("ERROR").set("error", !generarSolicitud.objetos("Errores").isEmpty() ? generarSolicitud.objetos("Errores").get(0).string("MensajeCliente") : null);
        }

        String idSolicitud = generarSolicitud.objetos("Datos").get(0).string("IdSolicitud");

        // Integrante
        ApiResponseMB generarIntegrante = RestOmnicanalidad.generarIntegrante(contexto, idSolicitud);
        if (generarIntegrante.hayError() || !generarIntegrante.objetos("Errores").isEmpty()) {
            return new RespuestaMB().setEstado("ERROR").set("error", !generarIntegrante.objetos("Errores").isEmpty() ? generarIntegrante.objetos("Errores").get(0).string("MensajeCliente") : null);
        }

        RespuestaMB respuesta = new RespuestaMB();
        respuesta.set("idSolicitud", idSolicitud);
        return respuesta;
    }

    public static RespuestaMB putResolucionesATM(ContextoMB contexto) {
        String idSolicitud = contexto.parametros.string("idSolicitud");
        ApiResponseMB putResoluciones = RestOmnicanalidad.putResoluciones(contexto, idSolicitud);
        if (putResoluciones.hayError() || !putResoluciones.objetos("Errores").isEmpty()) {
            return new RespuestaMB().setEstado("ERROR").set("error", !putResoluciones.objetos("Errores").isEmpty() ? putResoluciones.objetos("Errores").get(0).string("MensajeCliente") : null);
        }
        RespuestaMB respuesta = new RespuestaMB();
        respuesta.set("Datos", putResoluciones.objetos("Datos"));
        return respuesta;
    }

    public static RespuestaMB altaSeguroAtm(ContextoMB contexto) {
        String idSolicitud = contexto.parametros.string("idSolicitud");
        String idProducto = contexto.parametros.string("idProducto");
        String tipoProducto = contexto.parametros.string("tipoProducto");
        String idCajaAhorro = contexto.parametros.string("idCajaAhorro");
        String idTarjetaDebito = contexto.parametros.string("idTarjetaDebito");
        ApiResponseMB altaSeguroAtm = RestOmnicanalidad.altaSeguroAtm(contexto, idSolicitud, idProducto, tipoProducto, idCajaAhorro, idTarjetaDebito, false);
        if (altaSeguroAtm.hayError() || !altaSeguroAtm.objetos("Errores").isEmpty()) {
            return new RespuestaMB().setEstado("ERROR").set("error", !altaSeguroAtm.objetos("Errores").isEmpty() ? altaSeguroAtm.objetos("Errores").get(0).string("MensajeCliente") : null);
        }
        RespuestaMB respuesta = new RespuestaMB();
        respuesta.set("Datos", altaSeguroAtm.objetos("Datos"));
        return respuesta;
    }

    public static RespuestaMB actualizarSolicitudCajaAhorro(ContextoMB contexto) {
        String idSolicitud = contexto.parametros.string("idSolicitud");
        String idTarjetaDebito = contexto.parametros.string("idTarjetaDebito");
        String idCajaAhorroExistente = contexto.parametros.string("idCajaAhorro");
        Boolean virtual = contexto.parametros.bool("virtual", false);

        if (Objeto.anyEmpty(idSolicitud, idTarjetaDebito)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        if (contexto.persona().esMenor()) {
            return RespuestaMB.estado("MENOR_EDAD");
        }

        // Recuperar datos de la solicitud
        String idMoneda = "";
        String idCajaAhorro = "";
        String categoriaCA = "";
        Boolean tieneTD = false;
        RespuestaMB detalle = MBOmnicanalidad.detalleSolicitud(contexto);
        if (!detalle.string("estado").equals("0")) {
            return RespuestaMB.error();
        }
        idCajaAhorro = detalle.string("solicitud.productos.cajaAhorroPesos.id");
        if (!idCajaAhorro.isEmpty()) {
            idMoneda = "80";
            categoriaCA = "MOV";
        } else {
            idCajaAhorro = detalle.string("solicitud.productos.cajaAhorroDolares.id");
            idMoneda = "2";
            categoriaCA = "SC";
        }
        tieneTD = !detalle.string("solicitud.productos.tarjetaDebito.id").isEmpty() || !idTarjetaDebito.equals("0");
        if (idCajaAhorro.isEmpty()) {
            return RespuestaMB.estado("SOLICITUD_SIN_CA");
        }

        // Caja Ahorro
        ApiResponseMB generarCajaAhorro = RestOmnicanalidad.actualizarCajaAhorro(contexto, idSolicitud, idMoneda, idCajaAhorro, idTarjetaDebito, categoriaCA);
        if (generarCajaAhorro.hayError() || !generarCajaAhorro.objetos("Errores").isEmpty()) {
            return new RespuestaMB().setEstado("ERROR").set("error", !generarCajaAhorro.objetos("Errores").isEmpty() ? generarCajaAhorro.objetos("Errores").get(0).string("MensajeCliente") : null);
        }

        List<Objeto> cuentasOperativas = new ArrayList<Objeto>();
        Objeto cuentasOperativasPesos = new Objeto().set("Producto", 4).set("Cuenta", 0).set("Moneda", "80").set("Principal", true);
        Objeto cuentasOperativasUsd = new Objeto().set("Producto", 4).set("Cuenta", 0).set("Moneda", "2").set("Principal", true);

        if (!idMoneda.equals("2")) {

            if (!tieneTD) {
                //crear td
                cuentasOperativas.add(cuentasOperativasPesos);
                ApiResponseMB generarTarjetaDebito = RestOmnicanalidad.generarTarjetaDebito(contexto, idSolicitud, "NC", virtual, cuentasOperativas);
                if (generarTarjetaDebito.hayError() || !generarTarjetaDebito.objetos("Errores").isEmpty()) {
                    if (!generarTarjetaDebito.objetos("Errores").isEmpty()) {
                        return new RespuestaMB().setEstado("ERROR").set("error", !generarTarjetaDebito.objetos("Errores").isEmpty() ? generarTarjetaDebito.objetos("Errores").get(0).string("MensajeCliente") : null);
                    }
                    return new RespuestaMB().setEstado("ERROR").set("error", !generarTarjetaDebito.objetos("Errores").isEmpty() ? generarTarjetaDebito.objetos("Errores").get(0).string("MensajeCliente") : null);
                }
            }
        } else if (tieneTD) {

            if (!contexto.tieneCajaAhorroActivaPesos()) {
                //crear caPesos + caUsd
                ApiResponseMB generarCajaAhorroPesos = RestOmnicanalidad.generarCajaAhorro(contexto, idSolicitud, "80", null, "MOV");
                if (generarCajaAhorroPesos.hayError() || !generarCajaAhorroPesos.objetos("Errores").isEmpty()) {
                    return new RespuestaMB().setEstado("ERROR").set("error", !generarCajaAhorroPesos.objetos("Errores").isEmpty() ? generarCajaAhorroPesos.objetos("Errores").get(0).string("MensajeCliente") : null);
                }

                String idCajaAhorroPesos = generarCajaAhorroPesos.objetos("Datos").get(0).string("Id");
                ApiResponseMB actualizarCajaAhorroPesos = RestOmnicanalidad.actualizarCajaAhorro(contexto, idSolicitud, "80", idCajaAhorroPesos, idTarjetaDebito, "MOV");
                if (actualizarCajaAhorroPesos.hayError() || !actualizarCajaAhorroPesos.objetos("Errores").isEmpty()) {
                    if (!actualizarCajaAhorroPesos.objetos("Errores").isEmpty()) {
                        return new RespuestaMB().setEstado("ERROR").set("error", !actualizarCajaAhorroPesos.objetos("Errores").isEmpty() ? actualizarCajaAhorroPesos.objetos("Errores").get(0).string("MensajeCliente") : null);
                    }
                    return new RespuestaMB().setEstado("ERROR").set("error", !actualizarCajaAhorroPesos.objetos("Errores").isEmpty() ? actualizarCajaAhorroPesos.objetos("Errores").get(0).string("MensajeCliente") : null);
                }
            }
        } else {

            Cuenta cuentaExistente = contexto.cuenta(idCajaAhorroExistente);
            if (cuentaExistente != null) {
                //pedir caPesos y crear td + caUsd
                Objeto cuentasOperativaExistente = new Objeto().set("Producto", 4).set("Cuenta", Long.parseLong(cuentaExistente.numero())).set("Moneda", "80").set("Principal", true);
                cuentasOperativas.add(cuentasOperativasUsd);
                cuentasOperativas.add(cuentasOperativaExistente);
            } else {
                //crear td + caPesos + caUsd
                ApiResponseMB generarCajaAhorroPesos = RestOmnicanalidad.generarCajaAhorro(contexto, idSolicitud, "80", null, "MOV");
                if (generarCajaAhorroPesos.hayError() || !generarCajaAhorroPesos.objetos("Errores").isEmpty()) {
                    return new RespuestaMB().setEstado("ERROR").set("error", !generarCajaAhorroPesos.objetos("Errores").isEmpty() ? generarCajaAhorroPesos.objetos("Errores").get(0).string("MensajeCliente") : null);
                }

                String idCajaAhorroPesos = generarCajaAhorroPesos.objetos("Datos").get(0).string("Id");
                ApiResponseMB actualizarCajaAhorroPesos = RestOmnicanalidad.actualizarCajaAhorro(contexto, idSolicitud, "80", idCajaAhorroPesos, idTarjetaDebito, "MOV");
                if (actualizarCajaAhorroPesos.hayError() || !actualizarCajaAhorroPesos.objetos("Errores").isEmpty()) {
                    return new RespuestaMB().setEstado("ERROR").set("error", !actualizarCajaAhorroPesos.objetos("Errores").isEmpty() ? actualizarCajaAhorroPesos.objetos("Errores").get(0).string("MensajeCliente") : null);
                }

                cuentasOperativas.add(cuentasOperativasUsd);
                cuentasOperativas.add(cuentasOperativasPesos);
            }

            //crear td
            ApiResponseMB generarTarjetaDebito = RestOmnicanalidad.generarTarjetaDebito(contexto, idSolicitud, "NC", virtual, cuentasOperativas);
            if (generarTarjetaDebito.hayError() || !generarTarjetaDebito.objetos("Errores").isEmpty()) {
                if (!generarTarjetaDebito.objetos("Errores").isEmpty()) {
                    return new RespuestaMB().setEstado("ERROR").set("error", !generarTarjetaDebito.objetos("Errores").isEmpty() ? generarTarjetaDebito.objetos("Errores").get(0).string("MensajeCliente") : null);
                }
                return new RespuestaMB().setEstado("ERROR").set("error", !generarTarjetaDebito.objetos("Errores").isEmpty() ? generarTarjetaDebito.objetos("Errores").get(0).string("MensajeCliente") : null);
            }
        }

        // Motor
        ApiResponseMB responseMotor = RestOmnicanalidad.evaluarSolicitud(contexto, idSolicitud);
        if (responseMotor.hayError() || !responseMotor.objetos("Errores").isEmpty()) {
            return RespuestaMB.error().set("error", responseMotor.objetos("Errores").get(0).string("MensajeCliente"));
        }
        if (!responseMotor.objetos("Datos").get(0).string("ResolucionId").equals("AV")) {
            String estado = "ERROR";
            estado = responseMotor.objetos("Datos").get(0).string("ResolucionId").equals("AA") ? "APROBADO_AMARILLO" : estado;
            estado = responseMotor.objetos("Datos").get(0).string("ResolucionId").equals("CT") ? "AMARILLO" : estado;
            estado = responseMotor.objetos("Datos").get(0).string("ResolucionId").equals("RE") ? "ROJO" : estado;
            return new RespuestaMB().setEstado(estado).set("error", responseMotor.objetos("Datos").get(0).string("Explicacion"));
        }
        if (responseMotor.objetos("Datos").get(0).string("DerivarA").equals("S")) {
            if (ConfigMB.esOpenShift()) {
                return RespuestaMB.estado("AMARILLO");
            }
        }

        // Alta de seguro atm
        String idProductoAtm = contexto.parametros.string("idProducto");
        String tipoProducto = contexto.parametros.string("tipoProducto");
        if (idProductoAtm != null && !"".equals(idProductoAtm)) {
            // Si recibo un idProductoAtm entonces llamo al alta de seguro atm.
            ApiResponseMB altaSeguroAtm = RestOmnicanalidad.altaSeguroAtm(contexto, idSolicitud, idProductoAtm, tipoProducto, idCajaAhorro, idTarjetaDebito, true);
            if (altaSeguroAtm.hayError() || !altaSeguroAtm.objetos("Errores").isEmpty()) {
                return new RespuestaMB().setEstado("ERROR").set("error", !altaSeguroAtm.objetos("Errores").isEmpty() ? altaSeguroAtm.objetos("Errores").get(0).string("MensajeCliente") : null);
            }

        }

        RespuestaMB respuesta = new RespuestaMB();
        respuesta.set("idSolicitud", idSolicitud);
        return respuesta;
    }

    /* ========== PAQUETES ========== */
    public static RespuestaMB ofertasPaquete(ContextoMB contexto) {
        String idSolicitud = contexto.parametros.string("idSolicitud", null);
        String requerido = contexto.parametros.string("requerido", null);

        String letraTarjetaCreditoActual = null;
        String letraTarjetaCreditoMaxima = null;
        BigDecimal limiteCompraUnPago = null;
        Boolean nuevaTC = false;
        Boolean esPaqueteFacil = Objeto.setOf("34", "39").contains(requerido);

        if (contexto.persona().esEmpleado()) {
            return RespuestaMB.estado("ES_EMPLEADO");
        }

        TarjetaCredito tarjetaCredito = contexto.tarjetaCreditoTitular();

        if (tarjetaCredito != null && !tarjetaCredito.esHML()) {
            limiteCompraUnPago = tarjetaCredito.limiteCompra();
            letraTarjetaCreditoActual = tarjetaCredito.idTipo();
            ApiResponseMB responseSegmentacion = RestPersona.segmentacion(contexto);
            if (responseSegmentacion.hayError()) {
                return RespuestaMB.error();
            }
            if (responseSegmentacion.codigo != 204) {
                String idSegmento = responseSegmentacion.objetos().get(0).string("idRxReglaSegmentoRenta");
                if (!Objeto.empty(idSegmento)) {
                    ApiResponseMB responseTiposTarjetas = TarjetaCreditoService.consultaTiposTarjeta(contexto, tarjetaCredito.limiteCompra().intValue(), idSegmento);
                    if (responseTiposTarjetas.hayError()) {
                        return RespuestaMB.error();
                    }
                    if (responseTiposTarjetas.codigo != 204) {
                        letraTarjetaCreditoMaxima = responseTiposTarjetas.objetos().get(0).string("tarjeTipo");
                    }
                }
            }
        }

        if (tarjetaCredito == null && idSolicitud == null) {
            if (ConfigMB.esOpenShift()) {
                RestOmnicanalidad.limpiarSolicitudes(contexto, ConfigMB.longer("solicitud_dias_vigente", 30L), false, true, false);
            }

            ApiResponseMB generarSolicitud = RestOmnicanalidad.generarSolicitud(contexto);
            if (generarSolicitud.hayError() || !generarSolicitud.objetos("Errores").isEmpty()) {
                return new RespuestaMB().setEstado("ERROR").set("error", !generarSolicitud.objetos("Errores").isEmpty() ? generarSolicitud.objetos("Errores").get(0).string("MensajeCliente") : null);
            }
            idSolicitud = generarSolicitud.objetos("Datos").get(0).string("IdSolicitud");

            ApiResponseMB generarIntegrante = RestOmnicanalidad.generarIntegrante(contexto, idSolicitud);
            if (generarIntegrante.hayError() || !generarIntegrante.objetos("Errores").isEmpty()) {
                return new RespuestaMB().setEstado("ERROR").set("error", !generarIntegrante.objetos("Errores").isEmpty() ? generarIntegrante.objetos("Errores").get(0).string("MensajeCliente") : null);
            }

            ApiResponseMB generarTarjetaCreditoPaquetizada = RestOmnicanalidad.generarPaquete(contexto, idSolicitud, 0, true);
            if (generarTarjetaCreditoPaquetizada.hayError() || !generarTarjetaCreditoPaquetizada.objetos("Errores").isEmpty()) {
                return new RespuestaMB().setEstado("ERROR").set("error", !generarTarjetaCreditoPaquetizada.objetos("Errores").isEmpty() ? generarTarjetaCreditoPaquetizada.objetos("Errores").get(0).string("MensajeCliente") : null);
            }

            ApiResponseMB responseCanal = RestOmnicanalidad.actualizarCanalSolicitud(contexto, idSolicitud);
            if (responseCanal.hayError() || !responseCanal.objetos("Errores").isEmpty()) {
                return new RespuestaMB().setEstado("ERROR").set("error", !responseCanal.objetos("Errores").isEmpty() ? responseCanal.objetos("Errores").get(0).string("MensajeCliente") : null);
            }

            ApiResponseMB evaluarSolicitud = RestVenta.evaluarSolicitud(contexto, idSolicitud);
            if (evaluarSolicitud.hayError() || !evaluarSolicitud.objetos("Errores").isEmpty()) {
                return RespuestaMB.error().set("error", evaluarSolicitud.objetos("Errores").get(0).string("MensajeCliente"));
            }
            if (!evaluarSolicitud.objetos("Datos").get(0).string("ResolucionId").equals("AV")) {
                String estado = "ERROR";
                estado = evaluarSolicitud.objetos("Datos").get(0).string("ResolucionId").equals("AA") ? "APROBADO_AMARILLO" : estado;
                estado = evaluarSolicitud.objetos("Datos").get(0).string("ResolucionId").equals("CT") ? "AMARILLO" : estado;
                estado = evaluarSolicitud.objetos("Datos").get(0).string("ResolucionId").equals("RE") ? "ROJO" : estado;
                return new RespuestaMB().setEstado(estado).set("error", evaluarSolicitud.objetos("Datos").get(0).string("Explicacion"));
            }
            if (evaluarSolicitud.objetos("Datos").get(0).string("DerivarA").equals("S")) {
                if (ConfigMB.esOpenShift()) {
                    return RespuestaMB.estado("AMARILLO");
                }
            }

            String idTarjetaCreditoGenerada = generarTarjetaCreditoPaquetizada.objetos("Datos").get(0).string("Paquete.ProductosNuevos.TarjetaCredito.Id");
            ApiResponseMB buscarProducto = RestOmnicanalidad.buscarProducto(contexto, idSolicitud, "tarjetaCredito", idTarjetaCreditoGenerada);
            if (buscarProducto.hayError() || !buscarProducto.objetos("Errores").isEmpty()) {
                return new RespuestaMB().setEstado("ERROR").set("error", !buscarProducto.objetos("Errores").isEmpty() ? buscarProducto.objetos("Errores").get(0).string("MensajeCliente") : null);
            }

            letraTarjetaCreditoMaxima = buscarProducto.objetos("Datos").get(0).string("Letra");
            limiteCompraUnPago = buscarProducto.objetos("Datos").get(0).bigDecimal("Limite");
            nuevaTC = true;
        }

        if (tarjetaCredito == null && idSolicitud != null) {
            ApiResponseMB consultarSolicitud = RestOmnicanalidad.consultarSolicitud(contexto, idSolicitud);
            if (consultarSolicitud.hayError() || !consultarSolicitud.objetos("Errores").isEmpty()) {
                return new RespuestaMB().setEstado("ERROR").set("error", !consultarSolicitud.objetos("Errores").isEmpty() ? consultarSolicitud.objetos("Errores").get(0).string("MensajeCliente") : null);
            }

            for (Objeto item : consultarSolicitud.objetos("Datos").get(0).objetos("Productos")) {
                if (item.string("tipoProducto").equals("5")) {
                    String idTarjetaCreditoGenerada = item.string("Id");
                    ApiResponseMB buscarProducto = RestOmnicanalidad.buscarProducto(contexto, idSolicitud, "tarjetaCredito", idTarjetaCreditoGenerada);
                    if (buscarProducto.hayError() || !buscarProducto.objetos("Errores").isEmpty()) {
                        return new RespuestaMB().setEstado("ERROR").set("error", !buscarProducto.objetos("Errores").isEmpty() ? buscarProducto.objetos("Errores").get(0).string("MensajeCliente") : null);
                    }
                    letraTarjetaCreditoMaxima = buscarProducto.objetos("Datos").get(0).string("Letra");
                    limiteCompraUnPago = buscarProducto.objetos("Datos").get(0).bigDecimal("Limite");
                }
            }
            nuevaTC = true;
        }

        if (TarjetaCredito.peso(letraTarjetaCreditoActual) >= TarjetaCredito.peso(letraTarjetaCreditoMaxima)) {
            letraTarjetaCreditoMaxima = letraTarjetaCreditoActual;
        }

        // TODAS
        ApiRequestMB request = ApiMB.request("ConsultarOfertasPaquetes", "paquetes", "GET", "/v1/infoParametrias/paquetes/consolidados", contexto);
        request.query("canal", "26");
        request.query("empleado", contexto.idCobis());
        request.query("letraTarjeta", null);
        request.query("numeroSucursal", contexto.persona().sucursal());

        ApiResponseMB response = ApiMB.response(request, contexto.idCobis());
        if (response.hayError()) {
            return RespuestaMB.error();
        }

        // ACTUAL
        Set<String> codigosActual = new LinkedHashSet<>();
        if (letraTarjetaCreditoActual != null) {
            ApiRequestMB requestActual = ApiMB.request("ConsultarOfertasPaquetes", "paquetes", "GET", "/v1/infoParametrias/paquetes/consolidados", contexto);
            requestActual.query("canal", "26");
            requestActual.query("empleado", contexto.idCobis());
            requestActual.query("letraTarjeta", letraTarjetaCreditoActual);
            requestActual.query("numeroSucursal", contexto.persona().sucursal());

            ApiResponseMB responseActual = ApiMB.response(requestActual, contexto.idCobis());
            if (responseActual.hayError()) {
                return RespuestaMB.error();
            }

            for (Objeto item : responseActual.objetos()) {
                codigosActual.add(item.string("codigo"));
            }
        }

        // MAXIMA
        Set<String> codigosMaxima = new LinkedHashSet<>();
        if (letraTarjetaCreditoMaxima != null) {
            ApiRequestMB requestMaxima = ApiMB.request("ConsultarOfertasPaquetes", "paquetes", "GET", "/v1/infoParametrias/paquetes/consolidados", contexto);
            requestMaxima.query("canal", "26");
            requestMaxima.query("empleado", contexto.idCobis());
            requestMaxima.query("letraTarjeta", letraTarjetaCreditoMaxima);
            requestMaxima.query("numeroSucursal", contexto.persona().sucursal());

            ApiResponseMB responseMaxima = ApiMB.response(requestMaxima, contexto.idCobis());
            if (responseMaxima.hayError()) {
                return RespuestaMB.error();
            }

            for (Objeto item : responseMaxima.objetos()) {
                codigosMaxima.add(item.string("codigo"));
            }
        }

        // CODIGOS
        Boolean esClientePackSueldo = contexto.persona().esPackSueldo();
        Boolean esClienteEmprendedor = !esClientePackSueldo ? contexto.persona().tieneCuit() : false;
        Boolean esHML = tarjetaCredito != null && tarjetaCredito.esHML();

        Set<String> codigos = new LinkedHashSet<>();
        codigos.addAll(codigosActual);
        codigos.addAll(codigosMaxima);
        if (esClienteEmprendedor && !Objeto.setOf("L", "S").contains(letraTarjetaCreditoActual) && !Objeto.setOf("L", "S").contains(letraTarjetaCreditoMaxima)) {
            codigos.add("47");
        }

        // PROCESAR
        Set<String> codigosAgregados = new HashSet<>();
        RespuestaMB respuesta = new RespuestaMB();
        respuesta.set("idSolicitud", idSolicitud != null ? idSolicitud : "");
        respuesta.set("esHML", esHML);
        for (Integer i = 0; i < 2; ++i) {
            if (i > 0) {
                Set<String> codigosOfertasVisibles = new HashSet<>();
                for (Objeto item : respuesta.objetos("paquetes")) {
                    codigosOfertasVisibles.add(item.string("codigo"));
                }
                codigos = Objeto.setOf(requerido);
                if (requerido == null || codigosOfertasVisibles.contains(requerido)) {
                    break;
                }
            }
            for (String codigo : codigos) {
                String letra = "";
                letra = codigosMaxima.contains(codigo) ? letraTarjetaCreditoMaxima : letra;
                letra = codigosActual.contains(codigo) ? letraTarjetaCreditoActual : letra;
                letra = Objeto.empty(letra.isEmpty()) ? letraTarjetaCreditoMaxima : letra;
                letra = Objeto.empty(letra.isEmpty()) ? letraTarjetaCreditoActual : letra;

                if (Objeto.empty(letra) && i < 1) {
                    continue;
                }

                for (Objeto itemPaquete : response.objetos()) {
                    if (!codigo.equals(itemPaquete.string("codigo"))) {
                        continue;
                    }

                    if (itemPaquete.string("empleado").equals("S")) {
                        continue;
                    }
                    Boolean esPaqueteEmprendedor = itemPaquete.string("descripcion").toLowerCase().contains("emprendedor");
                    Boolean esPaquetePlanSueldo = false;
                    for (Objeto itemProducto : itemPaquete.objetos("productos").get(0).objetos("productos")) {
                        if ("AHO".equals(itemProducto.string("tipo")) && "80".equals(itemProducto.objeto("moneda").string("id"))) {
                            if (Objeto.setOf("K", "EV", "M").contains(itemProducto.string("categoriaDefault"))) {
                                esPaquetePlanSueldo = true;
                            }
                        }
                    }

                    if (i < 1) {
                        if (esClienteEmprendedor && !esPaqueteEmprendedor) {
                            continue;
                        }
                        if (!esClienteEmprendedor && esPaqueteEmprendedor) {
                            continue;
                        }
                        if (esClientePackSueldo && !esPaquetePlanSueldo) {
                            continue;
                        }
                        if (!esClientePackSueldo && esPaquetePlanSueldo) {
                            continue;
                        }
                    }

                    Integer cantidadMesesBonificado = null;
                    BigDecimal porcentajeBonificado = null;
                    for (Objeto bonificacion : itemPaquete.objetos("bonificaciones")) {
                        if (bonificacion.string("tipo").contains("BIENVENIDA") && bonificacion.string("bonificacion").split(" ").length >= 6) {
                            cantidadMesesBonificado = Integer.parseInt(bonificacion.string("bonificacion").split(" ")[6]);
                            porcentajeBonificado = new BigDecimal(bonificacion.string("bonificacion").split(" ")[4].replace("%", ""));
                        }
                    }

                    BigDecimal costo = new BigDecimal(0);
                    if (!itemPaquete.objetos("costos").isEmpty()) {
                        costo = itemPaquete.objetos("costos").get(0).bigDecimal("sinIVA");
                    }

                    Objeto paquete = new Objeto();
                    paquete.set("codigo", itemPaquete.string("codigo"));
                    paquete.set("letraTarjetaCredito", letra);
                    paquete.set("descripcion", RestCatalogo.mapaPaquetes().get(itemPaquete.integer("codigo")));
                    paquete.set("mostrarBuhoPuntos", itemPaquete.string("programaRecompensa").contains("1"));
                    paquete.set("mostrarAerolineaPlus", itemPaquete.string("programaRecompensa").contains("2"));
                    paquete.set("montoAerolineasPlus", ConfigMB.bigDecimal("monto_aerolineas_plus"));
                    paquete.set("montoAerolineasPlusFormateado", Formateador.importe(ConfigMB.bigDecimal("monto_aerolineas_plus")));
                    paquete.set("bonificadoBuhoPuntos", itemPaquete.string("programaRecompensaCanal").contains("1"));
//					paquete.set("bonificadoAerolineaPlus", itemPaquete.string("programaRecompensaCanal").contains("2"));
                    paquete.set("bonificadoAerolineaPlus", Objeto.setOf("38", "43", "53").contains(codigo));
                    paquete.set("tipoCliente", esClientePackSueldo ? "PACK SUELDO" : (esClienteEmprendedor ? "EMPRENDEDOR" : "RESTO"));
                    paquete.set("tipoPaquete", esPaquetePlanSueldo ? "PACK SUELDO" : (esPaqueteEmprendedor ? "EMPRENDEDOR" : "RESTO"));
                    paquete.set("esPaquetePlanSueldo", esPaquetePlanSueldo);
                    paquete.set("cantidadMesesBonificado", cantidadMesesBonificado);
                    paquete.set("costoMantenimientoMensual", costo);
                    paquete.set("costoMantenimientoMensualFormateado", Formateador.importe(costo));
                    paquete.set("mantenimientoPorcentajeBonificado", porcentajeBonificado);
                    paquete.set("mantenimientoPorcentajeBonificadoFormateado", Formateador.importe(porcentajeBonificado));

                    paquete.set("tieneCajaAhorroPesos", false);
                    paquete.set("tieneCajaAhorroDolares", false);
                    paquete.set("tieneCuentaCorriente", false);
                    paquete.set("tieneTarjetaDebito", false);
                    paquete.set("tieneTarjetaCredito", false);
                    paquete.set("crearNuevaTarjetaCredito", nuevaTC);
                    for (Objeto itemProducto : itemPaquete.objetos("productos").get(0).objetos("productos")) {
                        paquete.set("tieneCajaAhorroPesos", itemProducto.string("tipo").equals("AHO") && itemProducto.string("moneda.id").equals("80") ? true : paquete.get("tieneCajaAhorroPesos"));
                        paquete.set("tieneCajaAhorroDolares", itemProducto.string("tipo").equals("AHO") && itemProducto.string("moneda.id").equals("2") ? true : paquete.get("tieneCajaAhorroDolares"));
                        paquete.set("tieneCuentaCorriente", itemProducto.string("tipo").equals("CTE") ? true : paquete.get("tieneCuentaCorriente"));
                        paquete.set("tieneTarjetaDebito", itemProducto.string("tipo").equals("ATM") ? true : paquete.get("tieneTarjetaDebito"));
                        paquete.set("tieneTarjetaCredito", itemProducto.string("tipo").equals("SMA") && !itemProducto.string("categoriaDefault").isEmpty() ? true : paquete.get("tieneTarjetaCredito"));
                        paquete.set("tipoTarjetaCredito", itemProducto.string("tipo").equals("SMA") && !itemProducto.string("categoriaDefault").isEmpty() ? itemProducto.string("descripcion") : paquete.get("tipoTarjetaCredito"));
                    }

                    if (!Objeto.empty(letra)) {
                        BigDecimal limiteCompraCuotas = limiteCompraUnPago.multiply(RestOmnicanalidad.factorLimiteCompraCuotas(letra));
                        BigDecimal limiteCompra = limiteCompraUnPago.add(limiteCompraCuotas);

                        paquete.set("limiteTarjetaCreditoUnPago", limiteCompraUnPago);
                        paquete.set("limiteTarjetaCreditoUnPagoFormateada", Formateador.importe(limiteCompraUnPago));

                        paquete.set("limiteTarjetaCreditoCuotas", limiteCompraCuotas);
                        paquete.set("limiteTarjetaCreditoCuotasFormateada", Formateador.importe(limiteCompraCuotas));

                        paquete.set("limiteTarjetaCredito", limiteCompra);
                        paquete.set("limiteTarjetaCreditoFormateada", Formateador.importe(limiteCompra));

                        if (ConfigMB.bool("originacion_limite_unificado", false)) {
                            paquete.set("limiteUnificadoTarjetaCredito", limiteCompraUnPago);
                            paquete.set("limiteUnificadoTarjetaCreditoFormateada", Formateador.importe(limiteCompraUnPago));
                        }
                    }

                    paquete.set("personaTieneTarjetaCredito", false);
                    if (contexto.tarjetaCreditoTitular() != null && !contexto.tarjetaCreditoTitular().esHML()) {
                        paquete.set("personaTieneTarjetaCredito", true);
                        paquete.set("numeroTarjetaCreditoEnmascarado", "XXXX-" + Formateador.ultimos4digitos(contexto.tarjetaCreditoTitular().numero()));
                    }

                    SqlResponseMB sqlBeneficios = RestCatalogo.paquetesBeneficios(contexto, paquete.string("codigo"));
                    Objeto beneficios = new Objeto();
                    for (Objeto registro : sqlBeneficios.registros) {
                        Objeto item = new Objeto();
                        item.set("codigo", registro.string("numero_paquete"));
                        item.set("descripcionBeneficio", registro.string("desc_beneficio"));
                        item.set("descripcionBeneficioHtml", registro.string("desc_beneficio_html"));
                        beneficios.add(item);
                    }
                    paquete.set("beneficios", beneficios);
                    paquete.set("mostrar", (i < 1) || (i > 0 && esPaqueteFacil));

                    // mostrar
                    if (!codigosAgregados.contains(codigo)) {
                        codigosAgregados.add(codigo);
                        respuesta.add("paquetes", paquete);
                    }
                }
            }
        }

        return respuesta;
    }

    public static RespuestaMB productosPaquetizables(ContextoMB contexto) {
        String codigoPaquete = contexto.parametros.string("codigoPaquete");

        if (Objeto.anyEmpty(codigoPaquete)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        ApiRequestMB request = ApiMB.request("ProductosPaquetizables", "paquetes", "GET", "/v1/infoParametrias/productos/existentes", contexto);
        request.query("idCliente", contexto.idCobis());
        request.query("numeroPaquete", codigoPaquete);

        ApiResponseMB response = ApiMB.response(request, contexto.idCobis(), codigoPaquete);
        if (response.hayError()) {
            return RespuestaMB.error();
        }

        Objeto paquete = new Objeto();
        paquete.set("tieneCajaAhorroPesos", false);
        paquete.set("tieneCajaAhorroDolares", false);
        paquete.set("tieneCuentaCorriente", false);
        paquete.set("tieneTarjetaDebito", false);
        paquete.set("tieneTarjetaCredito", false);

        Objeto productos = new Objeto();
        productos.set("cajasAhorroPesos", new ArrayList<>());
        productos.set("cajasAhorroDolares", new ArrayList<>());
        productos.set("cuentasCorrientes", new ArrayList<>());
        productos.set("tarjetasDebito", new ArrayList<>());
        productos.set("tarjetasCredito", new ArrayList<>());

        TarjetaDebito tarjetaDebito = null;
        for (Objeto item : response.objetos().get(0).objetos("productos")) {
            String tipo = item.string("tipo");
            String moneda = item.string("moneda.id");
            if (tipo.equals("AHO") && moneda.equals("80")) {
                paquete.set("tieneCajaAhorroPesos", true);
            }
            if (tipo.equals("AHO") && moneda.equals("2")) {
                paquete.set("tieneCajaAhorroDolares", true);
            }
            if (tipo.equals("CTE")) {
                paquete.set("tieneCuentaCorriente", true);
            }
            if (tipo.equals("ATM")) {
                paquete.set("tieneTarjetaDebito", true);
            }
            if (tipo.equals("SMA")) {
                paquete.set("tieneTarjetaCredito", true);
            }
            for (Objeto subitem : item.objetos("existentes")) {
                if (tipo.equals("AHO") && moneda.equals("80")) {
                    String id = subitem.string("id");
                    Cuenta producto = contexto.cuenta(id);
                    if (producto != null && producto.unipersonal()) {
                        if (contexto.persona().esPackSueldo()) {
                            ApiResponseMB responseCuenta = CuentasService.cuentaBH(contexto, producto.numero());
                            if (!Objeto.setOf("EM", "K", "EV").contains(responseCuenta.string("categoria"))) {
                                continue;
                            }
                        }
                        if (contexto.persona().tieneCuit() && !contexto.persona().esPackSueldo()) {
                            ApiResponseMB responseCuenta = CuentasService.cuentaBH(contexto, producto.numero());
                            if (!Objeto.setOf("D").contains(responseCuenta.string("categoria"))) {
                                continue;
                            }
                        }
                        Objeto datos = new Objeto();
                        datos.set("id", producto.id());
                        datos.set("descripcion", producto.producto());
                        datos.set("descripcionCorta", producto.descripcionCorta());
                        datos.set("numeroFormateado", producto.numeroFormateado());
                        datos.set("numeroEnmascarado", producto.numeroEnmascarado());
                        datos.set("ultimos4digitos", producto.ultimos4digitos());
                        datos.set("titularidad", producto.titularidad());
                        datos.set("idMoneda", producto.idMoneda());
                        datos.set("moneda", producto.moneda());
                        datos.set("simboloMoneda", producto.simboloMoneda());
                        datos.set("estado", producto.descripcionEstado());
                        datos.set("saldo", producto.saldo());
                        datos.set("saldoFormateado", producto.saldoFormateado());
                        datos.set("acuerdo", producto.acuerdo());
                        datos.set("acuerdoFormateado", producto.acuerdoFormateado());
                        datos.set("disponible", producto.saldo().add(producto.acuerdo() != null ? producto.acuerdo() : new BigDecimal("0")));
                        datos.set("disponibleFormateado", Formateador.importe(item.bigDecimal("disponible")));
                        productos.add("cajasAhorroPesos", datos);
                    }
                }
                if (tipo.equals("AHO") && moneda.equals("2")) {
                    String id = subitem.string("id");
                    Cuenta producto = contexto.cuenta(id);
                    if (producto != null && producto.unipersonal()) {
                        if (contexto.persona().esPackSueldo()) {
                            ApiResponseMB responseCuenta = CuentasService.cuentaBH(contexto, producto.numero());
                            if (!Objeto.setOf("EM", "K", "EV").contains(responseCuenta.string("categoria"))) {
                                continue;
                            }
                        }
                        if (contexto.persona().tieneCuit() && !contexto.persona().esPackSueldo()) {
                            ApiResponseMB responseCuenta = CuentasService.cuentaBH(contexto, producto.numero());
                            if (!Objeto.setOf("D").contains(responseCuenta.string("categoria"))) {
                                continue;
                            }
                        }
                        Objeto datos = new Objeto();
                        datos.set("id", producto.id());
                        datos.set("descripcion", producto.producto());
                        datos.set("descripcionCorta", producto.descripcionCorta());
                        datos.set("numeroFormateado", producto.numeroFormateado());
                        datos.set("numeroEnmascarado", producto.numeroEnmascarado());
                        datos.set("ultimos4digitos", producto.ultimos4digitos());
                        datos.set("titularidad", producto.titularidad());
                        datos.set("idMoneda", producto.idMoneda());
                        datos.set("moneda", producto.moneda());
                        datos.set("simboloMoneda", producto.simboloMoneda());
                        datos.set("estado", producto.descripcionEstado());
                        datos.set("saldo", producto.saldo());
                        datos.set("saldoFormateado", producto.saldoFormateado());
                        datos.set("acuerdo", producto.acuerdo());
                        datos.set("acuerdoFormateado", producto.acuerdoFormateado());
                        datos.set("disponible", producto.saldo().add(producto.acuerdo() != null ? producto.acuerdo() : new BigDecimal("0")));
                        datos.set("disponibleFormateado", Formateador.importe(item.bigDecimal("disponible")));
                        productos.add("cajasAhorroDolares", datos);
                    }
                }
                if (tipo.equals("CTE")) {
                    String id = subitem.string("id");
                    Cuenta producto = contexto.cuenta(id);
                    if (producto != null && producto.unipersonal()) {
                        if (contexto.persona().esPackSueldo()) {
                            ApiResponseMB responseCuenta = CuentasService.cuentaBH(contexto, producto.numero());
                            if (!Objeto.setOf("K", "EV").contains(responseCuenta.string("categoria"))) {
                                continue;
                            }
                        }
                        if (contexto.persona().tieneCuit() && !contexto.persona().esPackSueldo()) {
                            ApiResponseMB responseCuenta = CuentasService.cuentaBH(contexto, producto.numero());
                            if (!Objeto.setOf("ASC").contains(responseCuenta.string("categoria"))) {
                                continue;
                            }
                        }
                        Objeto datos = new Objeto();
                        datos.set("id", producto.id());
                        datos.set("descripcion", producto.producto());
                        datos.set("descripcionCorta", producto.descripcionCorta());
                        datos.set("numeroFormateado", producto.numeroFormateado());
                        datos.set("numeroEnmascarado", producto.numeroEnmascarado());
                        datos.set("ultimos4digitos", producto.ultimos4digitos());
                        datos.set("titularidad", producto.titularidad());
                        datos.set("idMoneda", producto.idMoneda());
                        datos.set("moneda", producto.moneda());
                        datos.set("simboloMoneda", producto.simboloMoneda());
                        datos.set("estado", producto.descripcionEstado());
                        datos.set("saldo", producto.saldo());
                        datos.set("saldoFormateado", producto.saldoFormateado());
                        datos.set("acuerdo", producto.acuerdo());
                        datos.set("acuerdoFormateado", producto.acuerdoFormateado());
                        datos.set("disponible", producto.saldo().add(producto.acuerdo() != null ? producto.acuerdo() : new BigDecimal("0")));
                        datos.set("disponibleFormateado", Formateador.importe(item.bigDecimal("disponible")));
                        productos.add("cuentasCorrientes", datos);
                    }
                }
                if (tipo.equals("ATM")) {
                    String id = subitem.string("id");
                    TarjetaDebito producto = contexto.tarjetaDebito(id);
                    if (tarjetaDebito == null || producto.fechaAlta().getTime() > tarjetaDebito.fechaAlta().getTime()) {
                        tarjetaDebito = producto;
                    }
                }
                if (tipo.equals("SMA")) {
                    String id = subitem.string("id");
                    TarjetaCredito producto = contexto.tarjetaCredito(id);
                    if (producto != null && !producto.esHML()) {
                        Objeto datos = new Objeto();
                        datos.set("id", producto.idEncriptado());
                        datos.set("descripcion", producto.producto());
                        datos.set("tipo", producto.tipo());
                        datos.set("idTipo", producto.idTipo());
                        datos.set("numeroEnmascarado", "XXXX-" + Formateador.ultimos4digitos(producto.numero()));
                        datos.set("estado", producto.estado());
                        datos.set("titularidad", producto.titularidad());
                        productos.add("tarjetasCredito", datos);
                    }
                }
            }
        }

        if (tarjetaDebito != null) {
            Objeto datos = new Objeto();
            datos.set("id", tarjetaDebito.id());
            datos.set("descripcion", tarjetaDebito.producto());
            datos.set("numeroEnmascarado", "XXXX-" + Formateador.ultimos4digitos(tarjetaDebito.numero()));
            datos.set("titularidad", tarjetaDebito.titularidad());
            productos.add("tarjetasDebito", datos);
        }

        TarjetaCredito tarjetaCredito = contexto.tarjetaCreditoTitular();
        if (tarjetaCredito != null) {
            if (productos.objetos("tarjetasCredito").isEmpty()) {
                Objeto datos = new Objeto();
                datos.set("id", tarjetaCredito.idEncriptado());
                datos.set("descripcion", tarjetaCredito.producto());
                datos.set("tipo", tarjetaCredito.tipo());
                datos.set("idTipo", tarjetaCredito.idTipo());
                datos.set("numeroEnmascarado", "XXXX-" + Formateador.ultimos4digitos(tarjetaCredito.numero()));
                datos.set("estado", tarjetaCredito.estado());
                datos.set("titularidad", tarjetaCredito.titularidad());
                productos.add("tarjetasCredito", datos);
            }
        }

        RespuestaMB respuesta = new RespuestaMB();
        respuesta.set("paquete", paquete);
        respuesta.set("productos", productos);
        return respuesta;
    }

    public static RespuestaMB crearSolicitudPaquete(ContextoMB contexto) {
        String idSolicitud = contexto.parametros.string("idSolicitud");
        Integer codigoPaquete = 0;

        if (Objeto.empty(idSolicitud)) {
            if (ConfigMB.esOpenShift()) {
                RestOmnicanalidad.limpiarSolicitudes(contexto, ConfigMB.longer("solicitud_dias_vigente", 30L), false, true, false);
            }

            ApiResponseMB generarSolicitud = RestOmnicanalidad.generarSolicitud(contexto);
            if (generarSolicitud.hayError() || !generarSolicitud.objetos("Errores").isEmpty()) {
                return new RespuestaMB().setEstado("ERROR").set("error", !generarSolicitud.objetos("Errores").isEmpty() ? generarSolicitud.objetos("Errores").get(0).string("MensajeCliente") : null);
            }
            idSolicitud = generarSolicitud.objetos("Datos").get(0).string("IdSolicitud");

            ApiResponseMB generarIntegrante = RestOmnicanalidad.generarIntegrante(contexto, idSolicitud);
            if (generarIntegrante.hayError() || !generarIntegrante.objetos("Errores").isEmpty()) {
                return new RespuestaMB().setEstado("ERROR").set("error", !generarIntegrante.objetos("Errores").isEmpty() ? generarIntegrante.objetos("Errores").get(0).string("MensajeCliente") : null);
            }

            ApiResponseMB generarPaquete = RestOmnicanalidad.generarPaquete(contexto, idSolicitud, codigoPaquete, false);
            if (generarPaquete.hayError() || !generarPaquete.objetos("Errores").isEmpty()) {
                return new RespuestaMB().setEstado("ERROR").set("error", !generarPaquete.objetos("Errores").isEmpty() ? generarPaquete.objetos("Errores").get(0).string("MensajeCliente") : null);
            }
        }

        RespuestaMB respuesta = new RespuestaMB();
        respuesta.set("idSolicitud", idSolicitud);
        return respuesta;
    }

    public static RespuestaMB actualizarSolicitudPaquete(ContextoMB contexto) {
        String idSolicitud = contexto.parametros.string("idSolicitud");
        Integer codigoPaquete = contexto.parametros.integer("codigoPaquete");
        String idCajaAhorroPesos = contexto.parametros.string("idCajaAhorroPesos");
        String idCajaAhorroDolares = contexto.parametros.string("idCajaAhorroDolares");
        String idCuentaCorriente = contexto.parametros.string("idCuentaCorriente");
        String idTarjetaDebito = contexto.parametros.string("idTarjetaDebito");
        String idTarjetaCredito = contexto.parametros.string("idTarjetaCredito");
        String idSucursal = contexto.parametros.string("idSucursal");
        String idRecompensa = contexto.parametros.string("idRecompensa");
        String letraTarjetaCredito = contexto.parametros.string("letraTarjetaCredito");
        Boolean ejecutarMotor = contexto.parametros.bool("ejecutarMotor", true);

        if (Objeto.anyEmpty(idSolicitud, codigoPaquete)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        // Datos Solicitud
        ApiResponseMB response = RestOmnicanalidad.consultarSolicitud(contexto, idSolicitud);
        if (response.hayError()) {
            return RespuestaMB.error();
        }
        Objeto datos = response.objetos("Datos").get(0);

        // Datos Paquete
        String idPaquete = null;
        for (Objeto producto : datos.objetos("Productos")) {
            String tipoProducto = producto.string("IdProductoFrontEnd");
            if (tipoProducto.equals("32")) {
                idPaquete = producto.string("Id");
            }
        }
        if (idPaquete == null) {
            return RespuestaMB.estado("SOLICITUD_SIN_PAQUETE");
        }

        // Datos Tarjeta Credito
        ApiResponseMB datosTarjeta = null;
        for (Objeto producto : datos.objetos("Productos")) {
            String idProducto = producto.string("Id");
            String tipoProducto = producto.string("IdProductoFrontEnd");
            String recurso = RestOmnicanalidad.recursos().get(tipoProducto);
            if (tipoProducto.equals("5")) {
                datosTarjeta = RestOmnicanalidad.buscarProducto(contexto, idSolicitud, recurso, idProducto);
            }
        }

        // Integrante
        Objeto integrante = new Objeto();
        integrante.set("NumeroDocumentoTributario", contexto.persona().cuit());
        integrante.set("IdCobis", Long.valueOf(contexto.idCobis()));
        integrante.set("Rol", "T");

        // Productos
        Cuenta cajaAhorroPesos = contexto.cuenta(idCajaAhorroPesos);
        Cuenta cajaAhorroDolares = contexto.cuenta(idCajaAhorroDolares);
        Cuenta cuentaCorriente = contexto.cuenta(idCuentaCorriente);
        TarjetaDebito tarjetaDebito = contexto.tarjetaDebito(idTarjetaDebito);
        TarjetaCredito tarjetaCredito = contexto.tarjetaCredito(idTarjetaCredito);

        // Actualizar Paquete
        ApiRequestMB request = ApiMB.request("VentasActualizarPaquete", "ventas_windows", "PUT", "/solicitudes/{idSolicitud}/solicitudPaquete/{idPaquete}", contexto);
        request.headers.put("X-Handle", idSolicitud);
        request.path("idSolicitud", idSolicitud);
        request.path("idPaquete", idPaquete);
        request.body("EjecutaMotor", false);
        request.body("TipoOperacion", "03");
        request.body("Resolucion").set("FlagSolicitaExcepcion", false).set("MotivoExcepcion", "");
        request.body("Paquete", new Objeto());

        Objeto paquete = request.body.objeto("Paquete");
        paquete.set("TipoPaquete", codigoPaquete);
        paquete.set("ProductoBancario", codigoPaquete);
        paquete.set("OrigenCaptacion", "10");
        paquete.set("Oficina", "0");
        paquete.set("Oficial", "1");
        paquete.set("Ciclo", "4");
        paquete.set("ResumenMagnetico", 1);
        paquete.set("UsoFirma", "U");
        if (Objeto.empty(idSucursal)) {
            paquete.set("EleccionDistribucion", "CLIENTE");
        } else {
            paquete.set("EleccionDistribucion", "BANCO");
            paquete.set("DestinoDistribucion", idSucursal);
        }
        paquete.set("ProductoCobisCobro", 4);
        paquete.set("ProductoBancarioCobro", 3);
        paquete.set("MonedaCobro", "80");
        paquete.set("DomicilioResumen").set("Tipo", "DP");
        paquete.add("Integrantes", integrante);

        // Productos Existentes
        Objeto productosExistentes = new Objeto();
        if (cajaAhorroPesos != null) {
            Objeto item = new Objeto();
            item.set("IdProducto", 8);
            item.set("NumeroProducto", cajaAhorroPesos.numero());
            item.set("Integrante", integrante);
            productosExistentes.add(item);
        }
        if (cajaAhorroDolares != null) {
            Objeto item = new Objeto();
            item.set("IdProducto", 9);
            item.set("NumeroProducto", cajaAhorroDolares.numero());
            item.set("Integrante", integrante);
            productosExistentes.add(item);
        }
        if (cuentaCorriente != null) {
            Objeto item = new Objeto();
            item.set("IdProducto", 7);
            item.set("NumeroProducto", cuentaCorriente.numero());
            item.set("Integrante", integrante);
            productosExistentes.add(item);
        }
        if (tarjetaDebito != null) {
            Objeto item = new Objeto();
            item.set("IdProducto", 11);
            item.set("NumeroProducto", tarjetaDebito.numero());
            item.set("Integrante", integrante);
            productosExistentes.add(item);
        }
        if (tarjetaCredito != null) {
            Objeto item = new Objeto();
            item.set("IdProducto", 5);
            item.set("NumeroProducto", tarjetaCredito.numero());
            item.set("Marca", "2");
            item.set("Afinidad", tarjetaCredito.grupoAfinidad());
            item.set("Letra", tarjetaCredito.idTipo());
            item.set("Oficina", tarjetaCredito.sucursal());
            item.set("Integrante", integrante);
            item.set("Recompensa", idRecompensa);
            if (!Objeto.empty(letraTarjetaCredito) && !tarjetaCredito.idTipo().equals(letraTarjetaCredito)) {
                item.set("Upgrade", letraTarjetaCredito);
            }
            productosExistentes.add(item);
        }
        paquete.set("ProductosExistentes", productosExistentes);

        // Productos Nuevos
        Objeto productosNuevos = new Objeto();
        if ("0".equals(idCajaAhorroPesos)) {
            String categoria = "MOV";
            if (contexto.persona().tieneCuit()) {
                categoria = "D";
            }

            Objeto item = new Objeto();
            item.set("Moneda", "80");
            item.set("Categoria", categoria);
            item.set("DomicilioResumen").set("Tipo", "DP");
            item.set("Oficial", 0);
            item.set("Origen", "29");
            item.set("ProductoBancario", 3);
            item.set("CobroPrimerMantenimiento", true);
            item.set("TransfiereAcredHab", false);
            item.set("Ciclo", 5);
            item.set("UsoFirma", "U");
            item.set("ResumenMagnetico", false);
            item.add("Integrantes", integrante);
            item.set("CuentaLegales").set("Uso", "PER").set("realizaTransferencias", false);
            productosNuevos.set("CajaAhorro", item);
        }
        if ("0".equals(idCajaAhorroDolares)) {
            String categoria = "MOV";
            if (contexto.persona().tieneCuit()) {
                categoria = "D";
            }

            Objeto item = new Objeto();
            item.set("Moneda", "2");
            item.set("Categoria", categoria);
            item.set("DomicilioResumen").set("Tipo", "DP");
            item.set("Oficial", 0);
            item.set("Origen", "29");
            item.set("ProductoBancario", 3);
            item.set("CobroPrimerMantenimiento", true);
            item.set("TransfiereAcredHab", false);
            item.set("Ciclo", 5);
            item.set("UsoFirma", "U");
            item.set("ResumenMagnetico", false);
            item.add("Integrantes", integrante);
            item.set("CuentaLegales").set("Uso", "PER").set("realizaTransferencias", false);
            productosNuevos.set("CajaAhorroDolares", item);
        }
        if ("0".equals(idCuentaCorriente)) {
            String categoria = "D";
            if (contexto.persona().tieneCuit()) {
                categoria = "ASC";
            }
            if (contexto.persona().esPackSueldo()) {
                categoria = "EV";
            }

            Objeto item = new Objeto();
            item.set("ProductoBancario", 1);
            item.set("Categoria", categoria);
            item.set("DomicilioResumen").set("Tipo", "DP");
            item.set("Oficial", 0);
            item.set("CobroPrimerMantenimiento", false);
            item.set("Moneda", "80");
            item.set("Origen", "10");
            item.set("UsoFirma", "U");
            item.set("Ciclo", "6");
            item.set("ResumenMagnetico", true);
            item.set("EmpresaAseguradora", "40");
            item.set("Acuerdo");
            item.set("CuentaLegales").set("Uso", "PER").set("realizaTransferencias", false);
            item.add("Integrantes", integrante);
            productosNuevos.set("CuentaCorriente", item);
        }
        if ("0".equals(idTarjetaDebito)) {
            Objeto item = new Objeto();
            String tipoTarjeta = ConfigMB.bool("prendido_tarjeta_debito_contactless") ? "NC" : "NV";
            item.set("Tipo", tipoTarjeta);
            item.set("Domicilio").set("Tipo", "DP");
            item.set("Grupo", 3);
            item.set("TipoCuentaComision", "4");
            item.set("NumeroCtaComision", cajaAhorroPesos != null ? cajaAhorroPesos.numero() : "0");
            item.add("TarjetaDebitoCuentasOperativas", new Objeto().set("Producto", 4).set("Cuenta", 0).set("Moneda", 80).set("Principal", true));
            item.add("Integrantes", integrante);
            productosNuevos.set("TarjetaDebito", item);
        }
        if (datosTarjeta != null) {
            Objeto item = new Objeto();
            for (Objeto datosTC : datosTarjeta.objetos("Datos")) {
                item.set("Embozado", ContextoMB.embozado(contexto.persona().nombres(), contexto.persona().apellidos()));
                item.set("Caracteristica", idRecompensa == null || !idRecompensa.equals("2") ? "02" : "05");
                item.set("Domicilio").set("Tipo", "DP");
                item.set("Telefono", "E");
                item.set("FormaPago", 5);
                item.set("TipoCuenta", "4");
                item.set("EmpresaAseguradora", "40");
                item.set("NumeroCuenta", cajaAhorroPesos != null ? cajaAhorroPesos.numero() : "0");
                item.set("SucursalCuenta", cajaAhorroPesos != null ? cajaAhorroPesos.sucursal() : "0"); // ver que pasa cuando no tengo CA
                item.set("IdProductoFrontEnd", datosTC.integer("IdProductoFrontEnd"));
                item.set("Producto", datosTC.string("Producto"));
                item.set("CarteraGrupo", datosTC.string("CarteraGrupo"));
                item.set("Afinidad", datosTC.string("Afinidad"));
                item.set("ModeloLiquidacion", datosTC.string("ModeloLiquidacion"));
                item.set("Distribucion", datosTC.string("Distribucion"));
                item.set("Limite", datosTC.string("Limite"));
                item.set("AvisosViaMail", datosTC.string("AvisosViaMail"));
                item.set("AvisosCorreoTradicional", datosTC.string("AvisosCorreoTradicional"));
                item.set("Letra", datosTC.string("Letra"));
                item.add("Integrantes", integrante);
                item.set("MailAvisos").set("Tipo", "EMP");
                item.set("Recompensa", idRecompensa);
            }
            productosNuevos.set("TarjetaCredito", item);
        }
        paquete.set("ProductosNuevos", productosNuevos);

        ApiResponseMB responsePaquete = ApiMB.response(request, contexto.idCobis());
        if (responsePaquete.hayError() || !responsePaquete.objetos("Errores").isEmpty()) {
            return new RespuestaMB().setEstado("ERROR").set("error", !responsePaquete.objetos("Errores").isEmpty() ? responsePaquete.objetos("Errores").get(0).string("MensajeCliente") : null);
        }

        ApiResponseMB responseCanal = RestOmnicanalidad.actualizarCanalSolicitud(contexto, idSolicitud);
        if (responseCanal.hayError() || !responseCanal.objetos("Errores").isEmpty()) {
            return new RespuestaMB().setEstado("ERROR").set("error", !responseCanal.objetos("Errores").isEmpty() ? responseCanal.objetos("Errores").get(0).string("MensajeCliente") : null);
        }

        if (ejecutarMotor) {
            ApiResponseMB responseMotor = RestOmnicanalidad.evaluarSolicitud(contexto, idSolicitud);
            if (responseMotor.hayError() || !responseMotor.objetos("Errores").isEmpty()) {
                return RespuestaMB.error().set("error", responseMotor.objetos("Errores").get(0).string("MensajeCliente"));
            }
            if (!responseMotor.objetos("Datos").get(0).string("ResolucionId").equals("AV")) {
                String estado = "ERROR";
                estado = responseMotor.objetos("Datos").get(0).string("ResolucionId").equals("AA") ? "APROBADO_AMARILLO" : estado;
                estado = responseMotor.objetos("Datos").get(0).string("ResolucionId").equals("CT") ? "AMARILLO" : estado;
                estado = responseMotor.objetos("Datos").get(0).string("ResolucionId").equals("RE") ? "ROJO" : estado;
                return new RespuestaMB().setEstado(estado).set("error", responseMotor.objetos("Datos").get(0).string("Explicacion"));
            }
            if (responseMotor.objetos("Datos").get(0).string("DerivarA").equals("S")) {
                if (ConfigMB.esOpenShift()) {
                    return RespuestaMB.estado("AMARILLO");
                }
            }
        }

        RespuestaMB respuesta = new RespuestaMB();
        respuesta.set("idSolicitud", idSolicitud);
        return respuesta;
    }

    /* ========== FINALIZAR ========== */
    public static RespuestaMB finalizarSolicitudPaquete(ContextoMB contexto) {
        contexto.parametros.set("eliminarSolicitudPaquetes", true);
        return finalizarSolicitudVenta(contexto);
    }

    public static RespuestaMB finalizarSolicitudVenta(ContextoMB contexto) {
        String idSolicitud = contexto.parametros.string("idSolicitud");
        Boolean ejecutarMotor = contexto.parametros.bool("ejecutarMotor", true);
        Boolean eliminarSolicitudPaquetes = contexto.parametros.bool("eliminarSolicitudPaquetes", false);
        Boolean eliminarSolicitudPrestamo = contexto.parametros.bool("eliminarSolicitudPrestamo", false);

        if (Objeto.anyEmpty(idSolicitud))
            return RespuestaMB.parametrosIncorrectos();

        boolean esMigrado = contexto.esMigrado(contexto);

        if (esMigrado) {
            String csmId = contexto.parametros.string("csmId", "");
            String checksum = contexto.parametros.string("checksum", "");
            if (Objeto.anyEmpty(csmId, checksum))
                return RespuestaMB.parametrosIncorrectos();

            RespuestaMB respuestaValidaTransaccion = contexto.validarTransaccion(contexto, true, "", JourneyTransmitEnum.MB_INICIO_SESION);
            if (respuestaValidaTransaccion.hayError())
                return respuestaValidaTransaccion;
        }

        if (contexto.persona().esMenor()) {
            return RespuestaMB.estado("MENOR_EDAD", contexto.csmIdAuth);
        }

        if (ejecutarMotor) {
            ApiResponseMB evaluarSolicitud = RestVenta.evaluarSolicitud(contexto, idSolicitud);
            if (evaluarSolicitud.hayError() || !evaluarSolicitud.objetos("Errores").isEmpty()) {
                return RespuestaMB.error(contexto.csmIdAuth).set("error", evaluarSolicitud.objetos("Errores").get(0).string("MensajeCliente"));
            }
            if (!evaluarSolicitud.objetos("Datos").get(0).string("ResolucionId").equals("AV")) {
                String estado = "ERROR";
                estado = evaluarSolicitud.objetos("Datos").get(0).string("ResolucionId").equals("AA") ? "APROBADO_AMARILLO" : estado;
                estado = evaluarSolicitud.objetos("Datos").get(0).string("ResolucionId").equals("CT") ? "AMARILLO" : estado;
                estado = evaluarSolicitud.objetos("Datos").get(0).string("ResolucionId").equals("RE") ? "ROJO" : estado;
                return RespuestaMB.estado(estado, contexto.csmIdAuth).set("error", evaluarSolicitud.objetos("Datos").get(0).string("Explicacion"));
            }
            if (evaluarSolicitud.objetos("Datos").get(0).string("DerivarA").equals("S")) {
                if (ConfigMB.esOpenShift()) {
                    return RespuestaMB.estado("AMARILLO", contexto.csmIdAuth);
                }
            }
        }

        RestOmnicanalidad.actualizarCanalSolicitud(contexto, idSolicitud);

        RestOmnicanalidad.cargarDatosConozcaSuCliente(contexto);
        RestOmnicanalidad.cargarDatosPerfilPatrimonial(contexto);

        ApiResponseMB response = RestOmnicanalidad.finalizarSolicitud(contexto, idSolicitud);
        if (response.hayError() || !response.objetos("Errores").isEmpty()) {
            String estado = "ERROR";
            if (!response.objetos("Errores").isEmpty() && response.objetos("Errores").get(0).string("Codigo").equals("1831609")) {
                estado = "IR_A_SUCURSAL";
            }
            if (!response.objetos("Errores").isEmpty() && response.objetos("Errores").get(0).string("Codigo").equals("1831602")) {
                estado = "EN_PROCESO_ACTUALIZACION";
            }
            return RespuestaMB.estado(estado, contexto.csmIdAuth).set("error", !response.objetos("Errores").isEmpty() ? response.objetos("Errores").get(0).string("MensajeCliente") : null);
        }

        ProductosService.eliminarCacheProductos(contexto);

        String idSolicitudProxima = "";
        idSolicitudProxima = RestOmnicanalidad.limpiarSolicitudes(contexto, ConfigMB.longer("solicitud_dias_vigente", 30L), eliminarSolicitudPrestamo, eliminarSolicitudPaquetes, true);

        RespuestaMB respuesta = new RespuestaMB();
        respuesta.set("idSolicitud", idSolicitud);
        respuesta.set("idSolicitudProxima", idSolicitudProxima);
        return respuesta.set("csmIdAuth", contexto.csmIdAuth);
    }

    public static Objeto productoPrestamo(ContextoMB contexto, String idSolicitud, Objeto datosSolicitud) {
        Objeto productos = new Objeto();
        for (Objeto producto : datosSolicitud.objetos("Productos")) {
            if (Objeto.empty(producto.string("IdPaqueteProductos"))) {
                String idProducto = producto.string("Id");
                String tipoProducto = producto.string("IdProductoFrontEnd");
                String recurso = RestOmnicanalidad.recursos().get(tipoProducto);

                ApiResponseMB responseProducto = RestOmnicanalidad.buscarProducto(contexto, idSolicitud, recurso, idProducto);
                if (responseProducto.hayError()) {
                    return RespuestaMB.error();
                }
                Objeto datosProducto = responseProducto.objetos("Datos").get(0);

                String clave = RestOmnicanalidad.productos().get(tipoProducto);
                Objeto item = new Objeto();
                item.set("id", producto.string("Id"));
                if (tipoProducto.equals("32")) {
                    item.set("motor", datosProducto.string("Resolucion.ResolucionCodigo"));
                    item.set("tipoPaquete", datosProducto.string("Paquete.TipoPaquete"));
                    item.set("faltanDatos", validaDatosFaltantes(datosProducto));
                    validaCuenta(datosProducto, item);
                    validaProductosExistentes(datosProducto, contexto, item);

                }
                if (tipoProducto.equals("2")) {
                    Cuenta cuenta = contexto.cuenta(datosProducto.string("cuenta", datosProducto.string("Cuenta")));
                    item.set("monto", datosProducto.bigDecimal("MontoAprobado"));
                    item.set("montoFormateado", Formateador.importe(datosProducto.bigDecimal("MontoAprobado")));
                    item.set("plazo", datosProducto.integer("Plazo"));
                    item.set("tasa", datosProducto.bigDecimal("Tasa"));
                    item.set("tasaFormateada", Formateador.importe(datosProducto.bigDecimal("Tasa")));
                    item.set("cft", datosProducto.bigDecimal("CFT"));
                    item.set("cftFormateado", Formateador.importe(datosProducto.bigDecimal("CFT")));
                    item.set("diaCobro", datosProducto.integer("DiaCobro", 1));
                    item.set("idEmpresaAseguradora", datosProducto.integer("EmpresaAseguradora"));
                    if (cuenta != null) {
                        item.set("idCuenta", cuenta.id());
                        item.set("cuenta", cuenta.numero());
                    }
                    item.set("cuotaPura", datosProducto.bigDecimal("importeCuota"));
                    item.set("cuotaPuraFormateada", Formateador.importe(datosProducto.bigDecimal("importeCuota")));
                    item.set("cuota", datosProducto.bigDecimal("importeCuota"));
                    item.set("cuotaFormateada", Formateador.importe(datosProducto.bigDecimal("importeCuota")));
                    item.set("seguroDesempleo", false);
                    item.set("producto", datosProducto.string("producto"));
                    validaSeguroDesempleo(contexto, datosSolicitud, item, productos, idSolicitud, datosProducto);
                }

                productos.set(clave, item);
            }
        }
        return productos;
    }

    private static Boolean validaDatosFaltantes(Objeto datosProducto) {
        Boolean faltanDatos = false;
        faltanDatos |= Objeto.empty(datosProducto.string("Paquete.OrigenCaptacion"));
        faltanDatos |= Objeto.empty(datosProducto.string("Paquete.UsoFirma"));
        faltanDatos |= Objeto.empty(datosProducto.string("Paquete.EleccionDistribucion"));
        faltanDatos |= datosProducto.string("Paquete.TipoPaquete").equals("0");
        return faltanDatos;
    }

    private static void validaCuenta(Objeto datosProducto, Objeto item) {
        if (!datosProducto.string("Paquete.ProductosNuevos.CajaAhorro.Id").isEmpty()) {
            Objeto subItem = new Objeto();
            subItem.set("esNueva", true);
            subItem.set("numero", "0");
            item.set("cajaAhorroPesos", subItem);
        }
        if (!datosProducto.string("Paquete.ProductosNuevos.CajaAhorroDolares.Id").isEmpty()) {
            Objeto subItem = new Objeto();
            subItem.set("esNueva", true);
            subItem.set("numero", "0");
            item.set("cajaAhorroDolares", subItem);
        }
        if (!datosProducto.string("Paquete.ProductosNuevos.CuentaCorriente.Id").isEmpty()) {
            Objeto subItem = new Objeto();
            subItem.set("esNueva", true);
            subItem.set("numero", "0");
            item.set("cuentaCorriente", subItem);
        }
        if (!datosProducto.string("Paquete.ProductosNuevos.TarjetaDebito.Id").isEmpty()) {
            Objeto subItem = new Objeto();
            subItem.set("esNueva", true);
            subItem.set("numero", "0");
            item.set("tarjetaDebito", subItem);
        }
        if (!datosProducto.string("Paquete.ProductosNuevos.TarjetaCredito.Id").isEmpty()) {
            String caracteristica = datosProducto.string("Paquete.ProductosNuevos.TarjetaCredito.Caracteristica");
            Objeto subItem = new Objeto();
            subItem.set("esNueva", true);
            subItem.set("numero", "0");
            subItem.set("limite", datosProducto.bigDecimal("Paquete.ProductosNuevos.TarjetaCredito.Limite"));
            subItem.set("limiteFormateado", Formateador.importe(datosProducto.bigDecimal("Paquete.ProductosNuevos.TarjetaCredito.Limite")));
            subItem.set("letra", datosProducto.string("Paquete.ProductosNuevos.TarjetaCredito.Letra"));
            if (caracteristica.equals("02")) {
                subItem.set("idRecompensa", "1");
            } else if (caracteristica.equals("05")) {
                subItem.set("idRecompensa", "2");
            } else {
                subItem.set("idRecompensa", "");
            }
            item.set("tarjetaCredito", subItem);
        }

    }

    private static void validaProductosExistentes(Objeto datosProducto, ContextoMB contexto, Objeto item) {
        for (Objeto existente : datosProducto.objetos("Paquete.ProductosExistentes")) {
            String subclave = RestOmnicanalidad.productos().get(existente.string("productoFrontEnd"));
            Objeto subItem = new Objeto();
            String numero = existente.string("NumeroProducto");
            subItem.set("esNueva", false);
            if (subclave.equals("cajaAhorroPesos")) {
                Cuenta cuenta = contexto.cuenta(numero);
                subItem.set("id", cuenta.id());
            }
            if (subclave.equals("cajaAhorroDolares")) {
                Cuenta cuenta = contexto.cuenta(numero);
                subItem.set("id", cuenta.id());
            }
            if (subclave.equals("cuentaCorriente")) {
                Cuenta cuenta = contexto.cuenta(numero);
                subItem.set("id", cuenta.id());
            }
            if (subclave.equals("tarjetaDebito")) {
                TarjetaDebito tarjetaDebito = contexto.tarjetaDebito(numero);
                if (tarjetaDebito == null) {
                    continue;
                }
                subItem.set("id", tarjetaDebito.id());
            }
            if (subclave.equals("tarjetaCredito")) {
                TarjetaCredito tarjetaCredito = contexto.tarjetaCredito(numero);
                subItem.set("id", tarjetaCredito.idEncriptado());
                subItem.set("idRecompensa", "1");
            }
            subItem.set("numero", "XXXX-" + Formateador.ultimos4digitos(numero));
            item.set(subclave, subItem);
        }
    }

    private static void validaSeguroDesempleo(ContextoMB contexto, Objeto datosSolicitud, Objeto item, Objeto producto, String idSolicitud, Objeto datosProducto) {
        for (Objeto producto2 : datosSolicitud.objetos("Productos")) {
            if (Objeto.empty(producto2.string("IdPaqueteProductos"))) {
                if (producto2.string("IdProductoFrontEnd").equals("31")) {
                    item.set("seguroDesempleo", true);
                    String idSeguroDesempleo = producto.string("Id");
                    RespuestaMB respuesta = seguroDesempleo(contexto, idSolicitud, idSeguroDesempleo, datosProducto);

                    item.set("cuota", datosProducto.bigDecimal("importeCuota", "0").add(respuesta.bigDecimal("importeSeguro")));
                    item.set("cuotaFormateada", Formateador.importe(datosProducto.bigDecimal("importeCuota", "0").add(respuesta.bigDecimal("importeSeguro"))));

                }
            }
        }
    }

    public static RespuestaMB seguroDesempleo(ContextoMB contexto, String idSolicitud, String idSeguroDesempleo, Objeto datosProducto) {
        ApiResponseMB seguroDesempleoGet = RestVenta.consultarMontoSeguroDesempleo(contexto, idSolicitud, idSeguroDesempleo);
        if (seguroDesempleoGet.hayError()) {
            return RespuestaMB.error();
        }

        // TODO: esto hay que refactorizarlo para hacerlo en un solo lugar. Porque lo
        // estoy haciendo en otro lado también
        BigDecimal primaSeguro = new BigDecimal(0);
        BigDecimal sumaAsegurada = new BigDecimal(0);
        if (!seguroDesempleoGet.objetos("Datos").isEmpty()) {
            primaSeguro = new BigDecimal(seguroDesempleoGet.objetos("Datos").get(0).string("Valor1").replace(",", "."));
            sumaAsegurada = new BigDecimal(seguroDesempleoGet.objetos("Datos").get(0).string("Valor2").replace(",", "."));
        }

        BigDecimal importeSeguro = new BigDecimal(0);
        if (primaSeguro != null && sumaAsegurada != null && !sumaAsegurada.equals(new BigDecimal(0))) {
            importeSeguro = datosProducto.bigDecimal("importeCuota", "0").divide(sumaAsegurada, RoundingMode.CEILING).multiply(primaSeguro);
        }
        return new RespuestaMB().set("primaSeguro", primaSeguro).set("importeSeguro", importeSeguro);
    }

    public static RespuestaMB solicitudesCanalAmarillo(ContextoMB contexto) {
        Long cantidadDias = contexto.parametros.longer("cantidadDias", ConfigMB.longer("solicitud_dias_vigente", 30L));
        String rechazada = "R";
        String solicitudAmarilla = "AA";
        Integer diasTopeParaMostrar = 35; // 35 dias-> tiempo tope para mostrar solicitudes

        if (contexto.persona().esEmpleado() || contexto.persona().esMenor()) {
            return new RespuestaMB();
        }

        ApiResponseMB response = RestOmnicanalidad.consultarSolicitudesXEstado(contexto, cantidadDias, "P,R");
        if (response.hayError() || !response.objetos("Errores").isEmpty()) {
            return new RespuestaMB().setEstado("ERROR").set("error", !response.objetos("Errores").isEmpty() ? response.objetos("Errores").get(0).string("MensajeCliente") : null);
        }

        RespuestaMB respuesta = new RespuestaMB();
        for (Objeto datos : response.objetos("Datos")) {
            Boolean finalizada = datos.bool("Finalizada");
            String motor = datos.string("ResolucionCodigo");
            String estado = datos.string("Estado");
            Date fechaAlta = Fecha.stringToDate(datos.string("FechaAlta"), "yyyy-MM-dd'T'hh:mm:ss");
            Integer dias = Fecha.cantidadDias(new Date(), fechaAlta) * -1;
            Boolean mostrarSolicitud = (dias >= 0 && dias <= diasTopeParaMostrar);

            if (finalizada && Objeto.setOf(solicitudAmarilla).contains(motor) && rechazada.equalsIgnoreCase(estado) && !mostrarSolicitud) {
                continue;
            }
            if (finalizada && Objeto.setOf("AA").contains(motor)) {
                String idSolicitud = datos.string("Id");
                respuesta.add("solicitudes", idSolicitud);
            }
        }
        return respuesta;
    }

    public static Boolean insertarLogApiVentas(ContextoMB contexto, String numeroSolicitud, String servicio, String mensajeCliente, String mensajeDesarrollador, String http) {
        try {
            String sql = "";
            sql += " INSERT INTO [Homebanking].[dbo].[log_api_ventas] (momento,idCobis,numeroDocumento,numeroSolicitud,servicio,resolucionMotor,explicacionMotor,mensajeCliente,mensajeDesarrollador,canal)";
            sql += " VALUES (GETDATE(), ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            SqlRequestMB sqlRequest = SqlMB.request("InsertLogApiVentas", "homebanking");
            sqlRequest.sql = sql;
            sqlRequest.parametros.add(Texto.substring(contexto.idCobis(), 250));
            sqlRequest.parametros.add(Texto.substring(contexto.persona().numeroDocumento(), 250));
            sqlRequest.parametros.add(Texto.substring(numeroSolicitud, 250));
            sqlRequest.parametros.add(Texto.substring(servicio, 250));
            sqlRequest.parametros.add(null);
            sqlRequest.parametros.add(null);
            sqlRequest.parametros.add(Texto.substring(http + " - " + mensajeCliente, 990));
            sqlRequest.parametros.add(Texto.substring(mensajeDesarrollador, 990));
            sqlRequest.parametros.add("HB");
            SqlMB.response(sqlRequest);
        } catch (Exception e) {
            System.out.println("LA EXCEPCION FUE: " + e);
        }
        return true;
    }

    public static RespuestaMB altaCajaAhorroTarjetaDebitoOnline(ContextoMB contexto) {
        String idSolicitud = contexto.parametros.string("idSolicitud", null);
        Boolean virtual = contexto.parametros.bool("virtual", true);
        Boolean requiereEmbozado = contexto.parametros.bool("requiereEmbozado", false);

        if (Objeto.anyEmpty(idSolicitud, virtual, requiereEmbozado))
            return RespuestaMB.parametrosIncorrectos();

        if (Objeto.empty(contexto.idCobis()))
            return RespuestaMB.sinPseudoSesion();

        ApiResponseMB apiResponse;

        RespuestaMB respuesta = MBOmnicanalidad.detalleSolicitud(contexto);
        if (respuesta.hayError())
            return RespuestaMB.estado(OBTENER_DETALLE);

        if (StringUtils.isNotBlank(respuesta.string("solicitud.productos.cajaAhorroDolares.id"))) {
            apiResponse = RestOmnicanalidad.generarCajaAhorro(contexto, idSolicitud, "80", null, "MOV");
            if (chequearErrores(apiResponse))
                return castearRespuestaMBError(contexto, idSolicitud, "generarCajaAhorro", true, apiResponse, CREAR_CAJA_AHORRO);

            String idCajaAhorroPesos = apiResponse.objetos(DATOS_RESPUESTA).get(0).string("Id");
            apiResponse = RestOmnicanalidad.actualizarCajaAhorro(contexto, idSolicitud, "80", idCajaAhorroPesos, "", "MOV");
            if (chequearErrores(apiResponse))
                return castearRespuestaMBError(contexto, idSolicitud, "actualizarCajaAhorro", true, apiResponse, ACTUALIZAR_CAJA_AHORRO);
        }

        apiResponse = RestOmnicanalidad.generarTarjetaDebito(contexto, idSolicitud, "NC", 3, virtual, obtenerCuentasOperativas(), true, requiereEmbozado);
        if (chequearErrores(apiResponse))
            return castearRespuestaMBError(contexto, idSolicitud, "generarTarjetaDebito", true, apiResponse, GENERAR_TARJETA_DEBITO);

        String idProducto = apiResponse.objetos(DATOS_RESPUESTA).get(0).string("Id");

        apiResponse = RestOmnicanalidad.actualizarTarjetaDebito(contexto, idSolicitud, "NC", 3, virtual, obtenerCuentasOperativas(), true, requiereEmbozado, idProducto);
        if (chequearErrores(apiResponse))
            return castearRespuestaMBError(contexto, idSolicitud, "actualizarTarjetaDebito", true, apiResponse, ACTUALIZAR_TARJETA_DEBITO);

        apiResponse = RestOmnicanalidad.finalizarSolicitud(contexto, idSolicitud);
        if (chequearErrores(apiResponse)) {
            String estado = FINALIZAR_SOLICITUD;
            boolean erroresObjetoNotEmpty = chequearErroresObjeto(apiResponse);
            if (erroresObjetoNotEmpty) {
                Objeto error = apiResponse.objetos(ERRORES).get(0);
                insertLogApiVentas(contexto, idSolicitud, "finalizarSolicitud", error);
                if (error.string(CODIGO_RESPUESTA).equals("1831609"))
                    estado = IR_A_SUCURSAL;
                if (error.string(CODIGO_RESPUESTA).equals("1831602"))
                    estado = EN_PROCESO_ACTUALIZACION;
            }
            return new RespuestaMB().setEstado(estado).set(ERROR_RESPUESTA, erroresObjetoNotEmpty ? apiResponse.objetos(ERRORES).get(0).string(MENSAJE_CLIENTE) : null);
        }

        ProductosService.eliminarCacheProductos(contexto);

        return RespuestaMB.exito();
    }

    private static List<Objeto> obtenerCuentasOperativas() {
        return new ArrayList<>(Arrays.asList(
                new Objeto().set("Producto", 4).set("Cuenta", 0).set("Moneda", "80").set("Principal", true),
                new Objeto().set("Producto", 4).set("Cuenta", 0).set("Moneda", "2").set("Principal", true)));
    }

    private static RespuestaMB castearRespuestaMBError(ContextoMB contexto, String idSolicitud, String servicio, boolean loguerApiVentas, ApiResponseMB apiResponse, String error) {
        if (loguerApiVentas && !apiResponse.objetos(ERRORES).isEmpty())
            insertLogApiVentas(contexto, idSolicitud, servicio, apiResponse.objetos(ERRORES).get(0));
        return RespuestaMB.estado(error).set(ERROR_RESPUESTA, !apiResponse.objetos(ERRORES).isEmpty() ? apiResponse.objetos(ERRORES).get(0).string(MENSAJE_CLIENTE) : null);
    }

    private static boolean chequearErrores(ApiResponseMB apiResponse) {
        return apiResponse.hayError() || chequearErroresObjeto(apiResponse);
    }

    private static boolean chequearErroresObjeto(ApiResponseMB apiResponse) {
        return !apiResponse.objetos(ERRORES).isEmpty();
    }

    private static String obtenerCategoriaPorMoneda(String idMoneda) {
        return "2".equals(idMoneda) ? "SC" : "MOV";
    }

    private static void insertLogApiVentas(ContextoMB contexto, String idSolicitud, String servicio, Objeto error) {
        new Futuro<>(() -> insertarLogApiVentas(contexto, idSolicitud, servicio, error.string(MENSAJE_CLIENTE), error.string(MENSAJE_DESARROLLADOR), error.string(CODIGO_RESPUESTA)));
    }

    /* ========== UTIL ========== */
    public static Boolean insertarLogMotor(ContextoMB contexto, String numeroSolicitud, String resolucion, String explicacion) {
        try {
            String sql = "";
            sql += " INSERT INTO [Homebanking].[dbo].[log_api_ventas] (momento,idCobis,numeroDocumento,numeroSolicitud,servicio,resolucionMotor,explicacionMotor,mensajeCliente,mensajeDesarrollador,canal)";
            sql += " VALUES (GETDATE(), ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            SqlRequest sqlRequest = Sql.request("InsertLogApiVentas", "homebanking");
            sqlRequest.sql = sql;
            sqlRequest.parametros.add(ar.com.hipotecario.canal.homebanking.lib.Texto.substring(contexto.idCobis(), 250));
            sqlRequest.parametros.add(ar.com.hipotecario.canal.homebanking.lib.Texto.substring(contexto.persona().numeroDocumento(), 250));
            sqlRequest.parametros.add(ar.com.hipotecario.canal.homebanking.lib.Texto.substring(numeroSolicitud, 250));
            sqlRequest.parametros.add(ar.com.hipotecario.canal.homebanking.lib.Texto.substring("motor", 250));
            sqlRequest.parametros.add(ar.com.hipotecario.canal.homebanking.lib.Texto.substring(resolucion, 990));
            sqlRequest.parametros.add(ar.com.hipotecario.canal.homebanking.lib.Texto.substring(explicacion, 990));
            sqlRequest.parametros.add(null);
            sqlRequest.parametros.add(null);
            sqlRequest.parametros.add("HB");
            Sql.response(sqlRequest);
        } catch (Exception e) {
        }
        return true;
    }

}
