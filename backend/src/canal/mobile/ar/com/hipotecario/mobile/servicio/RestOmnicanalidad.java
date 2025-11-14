package ar.com.hipotecario.mobile.servicio;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ar.com.hipotecario.mobile.ConfigMB;
import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.api.MBOmnicanalidad;
import ar.com.hipotecario.mobile.conector.ApiMB;
import ar.com.hipotecario.mobile.conector.ApiRequestMB;
import ar.com.hipotecario.mobile.conector.ApiResponseMB;
import ar.com.hipotecario.mobile.lib.Fecha;
import ar.com.hipotecario.mobile.lib.Objeto;
import ar.com.hipotecario.mobile.negocio.Solicitud;
import ar.com.hipotecario.mobile.negocio.TarjetaCredito;
import ar.com.hipotecario.mobile.negocio.TarjetaDebito;

public class RestOmnicanalidad {

    /* ========== SOLICITUDES ========== */
    public static ApiResponseMB consultarSolicitudes(ContextoMB contexto, Long cantidadDias) {
        ApiRequestMB request = ApiMB.request("VentasConsultarSolicitudes", "ventas_windows", "GET", "/solicitudes", contexto);
        request.query("cuil", contexto.persona().cuit());
        request.query("fechadesde", Fecha.restarDias(new Date(), cantidadDias, "yyyyMMdd"));
        return ApiMB.response(request, contexto.idCobis());
    }

    public static ApiResponseMB consultarSolicitudesXEstado(ContextoMB contexto, Long cantidadDias, String estado) {
        ApiRequestMB request = ApiMB.request("VentasConsultarSolicitudes", "ventas_windows", "GET", "/solicitudesestados", contexto);
        request.query("cuil", contexto.persona().cuit());
        request.query("fechadesde", Fecha.restarDias(new Date(), cantidadDias, "yyyyMMdd"));
        request.query("estado", estado);
        return ApiMB.response(request, contexto.idCobis());
    }

    public static ApiResponseMB consultarSolicitud(ContextoMB contexto, String idSolicitud) {
        ApiRequestMB request = ApiMB.request("VentasConsultarSolicitud", "ventas_windows", "GET", "/solicitudes/{idSolicitud}", contexto);
        request.headers.put("X-Handle", idSolicitud);
        request.path("idSolicitud", idSolicitud);
        return ApiMB.response(request, contexto.idCobis(), idSolicitud);
    }

    public static ApiResponseMB desistirSolicitudBPM(ContextoMB contexto, String idSolicitud) {
        ApiRequestMB request = ApiMB.request("ProcesosDesistirSolicitud", "procesos", "POST", "/procesos-de-negocio/v1/solicitudes/{idSolicitud}/notificacion/accion", contexto);
        request.headers.put("X-Handle", idSolicitud);
        request.path("idSolicitud", idSolicitud);
        request.body("idSolicitud", Integer.valueOf(idSolicitud));
        request.body("idAccion", 3);
        return ApiMB.response(request, contexto.idCobis(), idSolicitud);
    }

    public static ApiResponseMB actualizarCanalSolicitud(ContextoMB contexto, String idSolicitud) {
        ApiRequestMB requestSolicitud = ApiMB.request("VentasConsultarSolicitud", "ventas_windows", "GET", "/solicitudes/{idSolicitud}", contexto);
        requestSolicitud.headers.put("X-Handle", idSolicitud);
        requestSolicitud.path("idSolicitud", idSolicitud);
        ApiResponseMB responseSolicitud = ApiMB.response(requestSolicitud, contexto.idCobis(), idSolicitud);
        if (responseSolicitud.hayError()) {
            return responseSolicitud;
        }

        Objeto datos = responseSolicitud.objetos("Datos").get(0);

        ApiRequestMB request = ApiMB.request("VentasPutCanal", "ventas_windows", "PUT", "/solicitudes/{SolicitudId}", contexto);
        request.headers.put("X-Handle", idSolicitud);
        request.path("SolicitudId", idSolicitud);
        request.body("TipoOperacion", "03");
        request.body("CanalOriginacion1", ConfigMB.integer("api_venta_canalOriginacion1"));
        request.body("CanalOriginacion2", ConfigMB.integer("api_venta_canalOriginacion2"));
        request.body("CanalOriginacion3", ConfigMB.string("api_venta_canalOriginacion3"));
        request.body("CanalVenta1", datos.string("CanalVenta1"));
        request.body("CanalVenta2", datos.string("CanalVenta2"));
        request.body("CanalVenta3", datos.string("CanalVenta3"));
        request.body("CanalVenta4", datos.string("CanalVenta4"));
        request.body("Oficina", datos.string("Oficina"));
        return ApiMB.response(request, contexto.idCobis(), idSolicitud);
    }

    public static ApiResponseMB generarSolicitud(ContextoMB contexto) {
        ApiRequestMB request = ApiMB.request("VentasGenerarSolicitud", "ventas_windows", "POST", "/solicitudes", contexto);
        request.headers.put("X-Handle", "0");
        request.body("TipoOperacion", "03");
        request.body("CanalOriginacion1", ConfigMB.integer("api_venta_canalOriginacion1"));
        request.body("CanalOriginacion2", ConfigMB.integer("api_venta_canalOriginacion2"));
        request.body("CanalOriginacion3", ConfigMB.string("api_venta_canalOriginacion3"));
        request.body("CanalVenta1", ConfigMB.string("api_venta_canalVenta1"));
        request.body("CanalVenta2", ConfigMB.string("api_venta_canalVenta2"));
        request.body("CanalVenta3", ConfigMB.string("api_venta_canalVenta3"));
        request.body("CanalVenta4", ConfigMB.string("api_venta_canalVenta4"));
        request.body("Oficina", "0");
        return ApiMB.response(request, contexto.idCobis());
    }

    public static ApiResponseMB evaluarSolicitud(ContextoMB contexto, String idSolicitud) {
        RestOmnicanalidad.actualizarCanalSolicitud(contexto, idSolicitud);

        ApiRequestMB request = ApiMB.request("VentasEvaluarSolicitud", "ventas_windows", "PUT", "/solicitudes/{SolicitudId}/resoluciones", contexto);
        request.headers.put("X-Handle", idSolicitud);
        request.path("SolicitudId", idSolicitud);
        request.body("TipoOperacion", "03");
        request.body("IdSolicitud", idSolicitud);
        return ApiMB.response(request, contexto.idCobis());
    }

    public static ApiResponseMB finalizarSolicitud(ContextoMB contexto, String idSolicitud) {
        ApiRequestMB request = ApiMB.request("VentasFinalizarSolicitud", "ventas_windows", "GET", "/solicitudes/{SolicitudId}", contexto);
        request.headers.put("X-Handle", idSolicitud);
        request.path("SolicitudId", idSolicitud);
        request.query("estado", "finalizar");
        return ApiMB.response(request, contexto.idCobis());
    }

    public static ApiResponseMB desistirSolicitud(ContextoMB contexto, String idSolicitud) {
        ApiRequestMB request = ApiMB.request("VentasDesistirSolicitud", "ventas_windows", "GET", "/solicitudes/{idSolicitud}", contexto);
        request.headers.put("X-Handle", idSolicitud);
        request.path("idSolicitud", idSolicitud);
        request.query("estado", "desistir");
        return ApiMB.response(request, contexto.idCobis());
    }

    public static String limpiarSolicitudes(ContextoMB contexto, Long cantidadDias, Boolean limpiarPrestamos, Boolean limpiarPaquetes, Boolean eliminarProductos) {
        ApiResponseMB response = RestOmnicanalidad.consultarSolicitudes(contexto, cantidadDias);
        if (response.hayError() || !response.objetos("Errores").isEmpty()) {
            return "";
        }

        List<Objeto> objetos = response.objetos("Datos").stream().filter(d -> d.objetos("Integrantes").stream().map(i -> i.string("IdCobis")).toList().contains(contexto.idCobis())).toList();

        List<String> idSolicitudes = new ArrayList<>();
        for (Objeto datos : objetos) {
            String estado = datos.string("Estado");
            if (Objeto.setOf("O").contains(estado)) {
                String idSolicitud = datos.string("Id");
                idSolicitudes.add(idSolicitud);
            }
        }

//		Boolean existeSolicitudPrestamoMasPaquete = false;
        for (String idSolicitud : idSolicitudes) {
            String idPaquete = null;
            String idPrestamo = null;
            ApiResponseMB responseSolicitud = RestOmnicanalidad.consultarSolicitud(contexto, idSolicitud);
            for (Objeto producto : responseSolicitud.objetos("Datos").get(0).objetos("Productos")) {
                idPaquete = producto.string("tipoProducto").equals("32") ? producto.string("Id") : idPaquete;
                idPrestamo = producto.string("tipoProducto").equals("2") ? producto.string("Id") : idPrestamo;
            }
            if (idPaquete != null && idPrestamo != null) {
//				existeSolicitudPrestamoMasPaquete = true;
                if (limpiarPrestamos && eliminarProductos) {
                    String originalIdSolicitud = contexto.parametros.string("idSolicitud", null);
                    String originalCodigoPaquete = contexto.parametros.string("codigoPaquete", null);
                    Boolean originalEjecutarMotor = contexto.parametros.bool("originalEjecutarMotor", null);

                    contexto.parametros.set("idSolicitud", idSolicitud);
                    contexto.parametros.set("codigoPaquete", "0");
                    contexto.parametros.set("ejecutarMotor", false);
                    MBOmnicanalidad.actualizarSolicitudPaquete(contexto);
                    contexto.parametros.set("idSolicitud", originalIdSolicitud);
                    contexto.parametros.set("codigoPaquete", originalCodigoPaquete);
                    contexto.parametros.set("ejecutarMotor", originalEjecutarMotor);
                }
            }
        }

        String idSolicitudProxima = "";
        for (String idSolicitud : idSolicitudes) {
            String idPaquete = null;
            String idPrestamo = null;
            String idCajaAhorro = null;
            String idCuentaCorriente = null;
            String idTarjetaDebito = null;
            String idSeguroDesempleo = null;
            ApiResponseMB responseSolicitud = RestOmnicanalidad.consultarSolicitud(contexto, idSolicitud);
            for (Objeto producto : responseSolicitud.objetos("Datos").get(0).objetos("Productos")) {
                idPaquete = producto.string("tipoProducto").equals("32") ? producto.string("Id") : idPaquete;
                idPrestamo = producto.string("tipoProducto").equals("2") ? producto.string("Id") : idPrestamo;
                idCajaAhorro = producto.string("tipoProducto").equals("8") ? producto.string("Id") : idCajaAhorro;
                idCuentaCorriente = producto.string("tipoProducto").equals("7") ? producto.string("Id") : idCuentaCorriente;
                idTarjetaDebito = producto.string("tipoProducto").equals("11") ? producto.string("Id") : idTarjetaDebito;
                idSeguroDesempleo = producto.string("tipoProducto").equals("31") ? producto.string("Id") : idSeguroDesempleo;
            }
            if (idPaquete != null && idPrestamo == null && (limpiarPaquetes)) {
                RestOmnicanalidad.desistirSolicitud(contexto, idSolicitud);
            }
            if (idPaquete == null && idPrestamo != null && (limpiarPrestamos)) {
                RestOmnicanalidad.desistirSolicitud(contexto, idSolicitud);
            }
            if (idCuentaCorriente != null && !limpiarPaquetes && !limpiarPrestamos && eliminarProductos) {
                RestOmnicanalidad.eliminarProducto(contexto, idSolicitud, recursos().get("7"), idCuentaCorriente);
                idSolicitudProxima = idSolicitud;
            }
            if (idPaquete != null && idPrestamo != null) {
                if (limpiarPaquetes && !limpiarPrestamos && eliminarProductos) {
                    RestOmnicanalidad.eliminarProducto(contexto, idSolicitud, recursos().get("32"), idPaquete);
                    idSolicitudProxima = idSolicitud;
                }
                if (!limpiarPaquetes && limpiarPrestamos && eliminarProductos) {
                    RestOmnicanalidad.eliminarProducto(contexto, idSolicitud, recursos().get("2"), idPrestamo);
                    if (idCajaAhorro != null) {
                        RestOmnicanalidad.eliminarProducto(contexto, idSolicitud, recursos().get("8"), idCajaAhorro);
                    }
                    if (idCuentaCorriente != null) {
                        RestOmnicanalidad.eliminarProducto(contexto, idSolicitud, recursos().get("7"), idCuentaCorriente);
                    }
                    if (idTarjetaDebito != null) {
                        RestOmnicanalidad.eliminarProducto(contexto, idSolicitud, recursos().get("11"), idTarjetaDebito);
                    }
                    if (idSeguroDesempleo != null) {
                        RestOmnicanalidad.eliminarProducto(contexto, idSolicitud, recursos().get("31"), idSeguroDesempleo);
                    }
                    idSolicitudProxima = idSolicitud;
                }
                if (limpiarPaquetes && limpiarPrestamos) {
                    Solicitud.logOriginacion(contexto, idSolicitud, "limpiarSolicitudes", null, "desistirSolicitud");
                    RestOmnicanalidad.desistirSolicitud(contexto, idSolicitud);
                }
            }
        }

        return idSolicitudProxima;
    }

    /* ========== INTEGRANTES ========== */
    public static ApiResponseMB generarIntegrante(ContextoMB contexto, String idSolicitud) {
        ApiRequestMB request = ApiMB.request("VentasGenerarIntegrante", "ventas_windows", "POST", "/solicitudes/{SolicitudId}/integrantes", contexto);
        request.headers.put("X-Handle", idSolicitud);
        request.path("SolicitudId", idSolicitud);
        request.body("NumeroTributario", Long.valueOf(contexto.persona().cuit()));
        request.body("TipoOperacion", "03");
        ApiResponseMB response = ApiMB.response(request, contexto.idCobis());

        if (contexto.persona().idEstadoCivil().equals("C")) {
            String cuitConyuge = RestPersona.cuitConyuge(contexto);
            if (cuitConyuge != null) {
                ApiRequestMB requestConyuge = ApiMB.request("VentasGenerarIntegrante", "ventas_windows", "POST", "/solicitudes/{SolicitudId}/integrantes", contexto);
                requestConyuge.headers.put("X-Handle", idSolicitud);
                requestConyuge.path("SolicitudId", idSolicitud);
                requestConyuge.body("NumeroTributario", Long.valueOf(cuitConyuge));
                requestConyuge.body("TipoOperacion", "03");
                ApiMB.response(requestConyuge, contexto.idCobis());
            }
        }

        return response;
    }

    public static ApiResponseMB generarIntegrante(ContextoMB contexto, String idSolicitud, String cuit) {
        ApiRequestMB request = ApiMB.request("VentasGenerarIntegrante", "ventas_windows", "POST", "/solicitudes/{SolicitudId}/integrantes", contexto);
        request.headers.put("X-Handle", idSolicitud);
        request.path("SolicitudId", idSolicitud);
        request.body("NumeroTributario", Long.valueOf(cuit));
        request.body("TipoOperacion", "03");
        ApiResponseMB response = ApiMB.response(request, contexto.idCobis());
        return response;
    }

    /* ========== PRODUCTOS ========== */
    public static ApiResponseMB buscarProducto(ContextoMB contexto, String idSolicitud, String recurso, String idProducto) {
        ApiRequestMB request = ApiMB.request("VentasConsultarProducto", "ventas_windows", "GET", "/solicitudes/{idSolicitud}/{recurso}/{idProducto}", contexto);
        request.headers.put("X-Handle", idSolicitud);
        request.path("idSolicitud", idSolicitud);
        request.path("recurso", recurso);
        request.path("idProducto", idProducto);
        return ApiMB.response(request, contexto.idCobis());
    }

    public static ApiResponseMB generarCajaAhorro(ContextoMB contexto, String idSolicitud, String idMoneda, String idTarjetaDebito, String categoria) {
        TarjetaDebito tarjetaDebito = contexto.tarjetaDebito(idTarjetaDebito);

        Objeto integrante = new Objeto();
        integrante.set("numeroDocumentoTributario", contexto.persona().cuit());
        integrante.set("idCobis", Long.valueOf(contexto.idCobis()));
        if (tarjetaDebito != null) {
            integrante.set("NumeroTarjetaDebito", tarjetaDebito.numero());
        }
        integrante.set("rol", "T");

        ApiRequestMB request = ApiMB.request("VentasGenerarCajaAhorro", "ventas_windows", "POST", "/solicitudes/{SolicitudId}/cajaAhorro", contexto);
        request.headers.put("X-Handle", idSolicitud);
        request.path("SolicitudId", idSolicitud);
        request.body("Moneda", idMoneda);
        request.body("Categoria", categoria);
        request.body("DomicilioResumen").set("tipo", "DP");
        request.body("Oficial", 0);
        request.body("Origen", "29");
        request.body("ProductoBancario", 3);
        request.body("CobroPrimerMantenimiento", true);
        request.body("TransfiereAcredHab", false);
        request.body("Ciclo", 5);
        request.body("UsoFirma", "U");
        request.body("ResumenMagnetico", false);
        request.add("Integrantes", integrante);
        request.body("CuentaLegales").set("Uso", "PER").set("RealizaTransferencias", false);
        request.body("TipoOperacion", "03");
        return ApiMB.response(request, contexto.idCobis());
    }

    public static ApiResponseMB solicitudProductos(ContextoMB contexto, String idSolicitud) {
        ApiRequestMB request = ApiMB.request("SegurosParametria", "ventas_windows", "GET", "/solicitudes/{idSolicitud}/parametria/22?campo=producto", contexto);
        request.path("idSolicitud", idSolicitud);
        return ApiMB.response(request, contexto.idCobis());
    }

    public static ApiResponseMB postSeguroATM(ContextoMB contexto, String idSolicitud) {
        ApiRequestMB request = ApiMB.request("PostSeguroAtm", "ventas_windows", "POST", "/Solicitudes/{idSolicitud}/SeguroATM/", contexto);
        request.path("idSolicitud", idSolicitud);
        request.body("tipoOperacion", "02");

        Objeto integrante = new Objeto();
        integrante.set("numeroDocumentoTributario", contexto.persona().cuit());
        integrante.set("rol", "T");
        request.add("integrantes", integrante);

        return ApiMB.response(request, contexto.idCobis());
    }

    public static ApiResponseMB putResoluciones(ContextoMB contexto, String idSolicitud) {
        ApiRequestMB request = ApiMB.request("PostSeguroAtm", "ventas_windows", "PUT", "/Solicitudes/{idSolicitud}/resoluciones", contexto);
        request.path("idSolicitud", idSolicitud);
        request.body("solicitudId", idSolicitud);
        request.body("tipoOperacion", "03");
        request.body("flagSolicitaAprobacionCentralizada", false);
        request.body("flagSolicitaValidarIdentidad", false);
        request.body("flagSolicitaComprobarIngresos", false);
        request.body("flagSolicitaAprobacionEstandard", false);
        request.body("flagSolicitaExcepcion", false);
        request.body("flagSolicitaEvaluarMercadoAbierto", false);
        request.body("EsPlanSueldo", "false");

        return ApiMB.response(request, contexto.idCobis());
    }

    public static ApiResponseMB altaSeguroAtm(ContextoMB contexto, String idSolicitud, String idProducto, String tipoProducto, String numeroCajaAhorro, String idTarjetaDebito, boolean isproductoEnTramite) {
        TarjetaDebito tarjetaDebito = contexto.tarjetaDebito(idTarjetaDebito);
        ApiRequestMB request = ApiMB.request("PostSeguroAtm", "ventas_windows", "PUT", "/solicitudes/{idSolicitud}/seguroatm/{idProducto}", contexto);
        request.headers.put("X-Handle", idSolicitud);
        request.path("idSolicitud", idSolicitud);
        request.path("idProducto", idProducto);
        request.body("producto", tipoProducto);
        Objeto integrante = new Objeto();
        integrante.set("numeroDocumentoTributario", contexto.persona().cuit());
        integrante.set("rol", "T");
        if (tarjetaDebito != null && !tarjetaDebito.numero().equals("0")) {
            integrante.set("NumeroTarjetaDebito", tarjetaDebito.numero());
        }
        request.add("integrantes", integrante);
        Objeto domicilioEnvio = new Objeto();
        domicilioEnvio.set("tipo", "DP");
        request.body("domicilioEnvio", domicilioEnvio);
        Objeto domicilioLaboral = new Objeto();
        domicilioLaboral.set("tipo", "LA");
        request.body("domicilioLaboral", domicilioLaboral);
        Objeto seguroMedioPago = new Objeto();
        seguroMedioPago.set("tipoMedioPago", "BH");
        Objeto pagoBH = new Objeto();
        pagoBH.set("esProductoTramite", isproductoEnTramite);
        pagoBH.set("tipoProducto", "AHO");
        pagoBH.set("NumeroProducto", numeroCajaAhorro);
        if (!isproductoEnTramite) {
            pagoBH.set("BHCBU", contexto.cajaAhorroTitularPesos().cbu());
        }
        seguroMedioPago.set("pagoBH", pagoBH);
        request.body("seguroMedioPago", seguroMedioPago);
        request.body("tipoOperacion", "03");

        return ApiMB.response(request, contexto.idCobis());
    }

    public static ApiResponseMB actualizarCajaAhorro(ContextoMB contexto, String idSolicitud, String idMoneda, String id, String idTarjetaDebito, String categoria) {
        TarjetaDebito tarjetaDebito = contexto.tarjetaDebito(idTarjetaDebito);

        Objeto integrante = new Objeto();
        integrante.set("numeroDocumentoTributario", contexto.persona().cuit());
        integrante.set("idCobis", Long.valueOf(contexto.idCobis()));
        if (tarjetaDebito != null) {
            integrante.set("NumeroTarjetaDebito", tarjetaDebito.numero());
        }
        integrante.set("rol", "T");

        ApiRequestMB request = ApiMB.request("VentasActualizarCajaAhorro", "ventas_windows", "PUT", "/solicitudes/{SolicitudId}/cajaAhorro/{id}", contexto);
        request.headers.put("X-Handle", idSolicitud);
        request.path("SolicitudId", idSolicitud);
        request.path("id", id);
        request.body("Moneda", idMoneda);
        request.body("Categoria", categoria);
        request.body("DomicilioResumen").set("tipo", "DP");
        request.body("Oficial", 0);
        request.body("Origen", "29");
        request.body("ProductoBancario", 3);
        request.body("CobroPrimerMantenimiento", true);
        request.body("TransfiereAcredHab", false);
        request.body("Ciclo", 5);
        request.body("UsoFirma", "U");
        request.body("ResumenMagnetico", false);
        request.add("Integrantes", integrante);
        request.body("CuentaLegales").set("Uso", "PER").set("RealizaTransferencias", false);
        request.body("TipoOperacion", "03");
        return ApiMB.response(request, contexto.idCobis());
    }

    public static ApiResponseMB generarTarjetaDebito(ContextoMB contexto, String idSolicitud, String idMoneda, Boolean virtual, List<Objeto> cuentasOperativas) {
        Objeto integrante = new Objeto();
        integrante.set("numeroDocumentoTributario", contexto.persona().cuit());
        integrante.set("idCobis", Long.valueOf(contexto.idCobis()));
        integrante.set("rol", "T");

        ApiRequestMB request = ApiMB.request("VentasGenerarTarjetaDebito", "ventas_windows", "POST", "/solicitudes/{SolicitudId}/tarjetaDebito", contexto);
        request.headers.put("X-Handle", idSolicitud);
        request.path("SolicitudId", idSolicitud);
        String tipoTarjeta = ConfigMB.bool("prendido_tarjeta_debito_contactless") ? "NC" : "NV";
        request.body("Tipo", tipoTarjeta);
        request.body("Domicilio").set("Tipo", "DP");
        request.body("Grupo", 3);
        request.body("TipoCuentaComision", "4");
        request.body("NumeroCtaComision", "0");
        request.body("Moneda", idMoneda);
        request.body("TarjetaDebitoCuentasOperativas", cuentasOperativas);
        request.add("Integrantes", integrante);
        request.body("TipoOperacion", "03");
        request.body("EsVirtual", virtual);

        return ApiMB.response(request, contexto.idCobis());
    }

    public static ApiResponseMB generarPaquete(ContextoMB contexto, String idSolicitud, Integer codigoPaquete, Boolean generarTarjetaCredito) {
        ApiRequestMB request = ApiMB.request("VentasGenerarPaquete", "ventas_windows", "POST", "/solicitudes/{SolicitudId}/solicitudPaquete", contexto);
        request.headers.put("X-Handle", idSolicitud);
        request.path("SolicitudId", idSolicitud);
        request.body("EjecutaMotor", false);
        request.body("TipoOperacion", "02");
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
        paquete.set("EleccionDistribucion", "CLIENTE");
        paquete.set("ProductoCobisCobro", 4);
        paquete.set("ProductoBancarioCobro", 3);
        paquete.set("MonedaCobro", "80");
        paquete.set("DomicilioResumen").set("Tipo", "DP");

        Objeto integrante = new Objeto();
        integrante.set("NumeroDocumentoTributario", contexto.persona().cuit());
        integrante.set("IdCobis", Long.valueOf(contexto.idCobis()));
        integrante.set("Rol", "T");
        paquete.add("Integrantes", integrante);

        if (generarTarjetaCredito) {
            Objeto productosNuevos = new Objeto();
            Objeto nuevaTarjetaCredito = new Objeto();
            nuevaTarjetaCredito.add("Integrantes", integrante);
            nuevaTarjetaCredito.set("TipoOperacion", "02");
            productosNuevos = productosNuevos.set("TarjetaCredito", nuevaTarjetaCredito);
            paquete.set("ProductosNuevos", productosNuevos);
        }

        return ApiMB.response(request, contexto.idCobis());
    }

    public static ApiResponseMB generarTarjetaCreditoAdicional(ContextoMB contexto, String idSolicitud, String cuitAdicional) {
        TarjetaCredito tarjetaCredito = contexto.tarjetaCreditoTitular();

        ApiRequestMB request = ApiMB.request("VentasGenerarTarjetaCreditoAdicional", "ventas_windows", "POST", "/solicitudes/{SolicitudId}/inclusionadicionaltc", contexto);
        request.headers.put("X-Handle", idSolicitud);
        request.path("SolicitudId", idSolicitud);
        request.body("NumeroCuenta", tarjetaCredito.numeroCuenta());
        request.add("Integrantes", new Objeto().set("NumeroDocumentoTributario", contexto.persona().cuit()).set("Rol", "T"));
        request.add("Integrantes", new Objeto().set("NumeroDocumentoTributario", cuitAdicional).set("Rol", "A"));
        request.body("TipoOperacion", "02");
        return ApiMB.response(request, contexto.idCobis());
    }

    public static ApiResponseMB actualizarTarjetaCreditoAdicional(ContextoMB contexto, String idSolicitud, String idTarjeta, String cuitAdicional, String embozado, Integer porcentaje) {
        TarjetaCredito tarjetaCredito = contexto.tarjetaCreditoTitular();

        Objeto adicional = new Objeto();
        adicional.set("NumeroDocumentoTributario", cuitAdicional);
        adicional.set("Limite", porcentaje);
        adicional.set("LimiteCompra", porcentaje);
        adicional.set("LimiteAdelanto", porcentaje);
        adicional.set("Embozado", embozado);

        ApiRequestMB request = ApiMB.request("VentasActualizarTarjetaCreditoAdicional", "ventas_windows", "PUT", "/solicitudes/{SolicitudId}/inclusionadicionaltc/{ProductoId}", contexto);
        request.headers.put("X-Handle", idSolicitud);
        request.path("SolicitudId", idSolicitud);
        request.path("ProductoId", idTarjeta);
        request.body("NumeroCuenta", tarjetaCredito.numeroCuenta());
        request.add("Integrantes", new Objeto().set("NumeroDocumentoTributario", contexto.persona().cuit()).set("Rol", "T"));
        request.add("Integrantes", new Objeto().set("NumeroDocumentoTributario", cuitAdicional).set("Rol", "A"));
        request.add("TarjetaCreditoAdicionales", adicional);
        request.body("TipoOperacion", "03");
        return ApiMB.response(request, contexto.idCobis());
    }

    public static ApiResponseMB eliminarProducto(ContextoMB contexto, String idSolicitud, String recurso, String idProducto) {
        ApiRequestMB request = ApiMB.request("VentasEliminarProducto", "ventas_windows", "DELETE", "/solicitudes/{idSolicitud}/{recurso}/{idProducto}", contexto);
        request.headers.put("X-Handle", idSolicitud);
        request.path("idSolicitud", idSolicitud);
        request.path("recurso", recurso);
        request.path("idProducto", idProducto);
        return ApiMB.response(request, contexto.idCobis(), idSolicitud, idProducto);
    }

    /* ========== UTILITARIO ========== */
    public static Set<String> productosPaquetizables() {
        return Objeto.setOf("8", "9", "6", "7", "11", "5");
    }

    public static Set<String> productosOperables() {
        return Objeto.setOf("2", "5", "6", "7", "8", "9", "11", "31", "32");
    }

    public static Map<String, String> descripcionProducto() {
        Map<String, String> mapa = new HashMap<>();
        mapa.put("1", "Prestamo Hipotecario");
        mapa.put("2", "Prestamo Personal");
        mapa.put("5", "Tarjeta de Credito");
        mapa.put("6", "Cuenta Corriente");
        mapa.put("7", "Cuenta Corriente");
        mapa.put("8", "Caja de Ahorro");
        mapa.put("9", "Caja de Ahorro en Dolares");
        mapa.put("11", "Tarjeta de Debito");
        mapa.put("12", "Caja de Seguridad");
        mapa.put("16", "inclusionModificacion");
        mapa.put("17", "inclusionModificacion");
        mapa.put("18", "inclusionAdicionalTC");
        mapa.put("19", "inclusionModificacion");
        mapa.put("20", "Seguro de Vivienda");
        mapa.put("21", "Seguro de Vida");
        mapa.put("22", "Seguro ATM");
        mapa.put("23", "Seguro de Salud");
        mapa.put("24", "seguroAP");
        mapa.put("25", "Seguro de Autos");
        mapa.put("28", "recategorizacion");
        mapa.put("29", "seguroCompraProtegidaTC");
        mapa.put("31", "Seguro de Desempleo");
        mapa.put("32", "Paquete");
        mapa.put("33", "adicionCotitular");
        mapa.put("34", "Seguro de Bienes Moviles");
        return mapa;
    }

    public static Map<String, String> recursos() {
        Map<String, String> mapa = new HashMap<>();
        mapa.put("33", "adicionCotitular");
        mapa.put("8", "cajaAhorro");
        mapa.put("9", "cajaAhorro");
        mapa.put("12", "cajaSeguridad");
        mapa.put("6", "cuentaCorriente");
        mapa.put("7", "cuentaCorriente");
        mapa.put("18", "inclusionAdicionalTC");
        mapa.put("16", "inclusionModificacion");
        mapa.put("17", "inclusionModificacion");
        mapa.put("19", "inclusionModificacion");
        mapa.put("1", "prestamoHipotecario");
        mapa.put("2", "prestamoPersonal");
        mapa.put("28", "recategorizacion");
        mapa.put("24", "seguroAP");
        mapa.put("22", "seguroAtm");
        mapa.put("25", "seguroAutos");
        mapa.put("34", "seguroBienesMoviles");
        mapa.put("29", "seguroCompraProtegidaTC");
        mapa.put("31", "seguroDesempleo");
        mapa.put("20", "segurohogar");
        mapa.put("23", "seguroSalud");
        mapa.put("21", "seguroVida24");
        mapa.put("32", "solicitudPaquete");
        mapa.put("5", "tarjetaCredito");
        mapa.put("11", "tarjetaDebito");
        return mapa;
    }

    public static Map<String, String> productos() {
        Map<String, String> mapa = new HashMap<>();
        mapa.put("33", "adicionCotitular");
        mapa.put("8", "cajaAhorroPesos");
        mapa.put("9", "cajaAhorroDolares");
        mapa.put("12", "cajaSeguridad");
        mapa.put("6", "cuentaCorriente");
        mapa.put("7", "cuentaCorriente");
        mapa.put("18", "inclusionAdicionalTC");
        mapa.put("16", "inclusionModificacion");
        mapa.put("17", "inclusionModificacion");
        mapa.put("19", "inclusionModificacion");
        mapa.put("1", "prestamoHipotecario");
        mapa.put("2", "prestamoPersonal");
        mapa.put("28", "recategorizacion");
        mapa.put("24", "seguroAP");
        mapa.put("22", "seguroAtm");
        mapa.put("25", "seguroAutos");
        mapa.put("34", "seguroBienesMoviles");
        mapa.put("29", "seguroCompraProtegidaTC");
        mapa.put("31", "seguroDesempleo");
        mapa.put("20", "segurohogar");
        mapa.put("23", "seguroSalud");
        mapa.put("21", "seguroVida24");
        mapa.put("32", "paquete");
        mapa.put("5", "tarjetaCredito");
        mapa.put("11", "tarjetaDebito");
        return mapa;
    }

    public static Map<String, String> canales() {
        Map<String, String> mapa = new HashMap<>();
        mapa.put("1", "FUERZA DE VENTAS");
        mapa.put("2", "TELEMARKETING");
        mapa.put("6", "COMMUNICOM");
        mapa.put("7", "MONOPRODUCTO");
        mapa.put("11", "CALL CENTER EXTERNO");
        mapa.put("12", "SUCURSAL");
        mapa.put("14", "CODIGO DESCUENTO PROPIO");
        mapa.put("15", "GCIA GESTION DE COBRANZAS");
        mapa.put("16", "CODIGO DESCUENTO PROPIO HIPOT. COBIS");
        mapa.put("20", "TELECOBRANZAS");
        mapa.put("21", "MORA AVANZADA");
        mapa.put("22", "AGENCIAS");
        mapa.put("23", "CO BRANDING");
        mapa.put("24", "MUTUAL");
        mapa.put("25", "E-SALES");
        mapa.put("26", "HOME BANKING");
        mapa.put("28", "RETAILERS");
        mapa.put("29", "OPERADORES INMOBILIARIOS");
        mapa.put("30", "MUNICIPIOS");
        mapa.put("34", "SERVICIO A CLIENTES");
        mapa.put("35", "REFERIDORES PF");
        mapa.put("36", "CONSUMO");
        mapa.put("37", "TMK PROCREAR");
        mapa.put("38", "TAS");
        mapa.put("39", "MOBILE");
        mapa.put("40", "IVR");
        mapa.put("41", "PRISMA");
        mapa.put("42", "ATENCION AL CLIENTE");
        mapa.put("50", "MICRO-CREDITO PROCREAR");
        mapa.put("51", "PRUEBA");
        return mapa;
    }

    public static BigDecimal factorLimiteCompraCuotas(String letra) {
        BigDecimal factor = new BigDecimal("1");
        if (letra.equals("I")) {
            factor = ConfigMB.bigDecimal("originacion_factor_internacional", "1");
        }
        if (letra.equals("P")) {
            factor = ConfigMB.bigDecimal("originacion_factor_gold", "1");
        }
        if (letra.equals("L")) {
            factor = ConfigMB.bigDecimal("originacion_factor_platinum", "1.5");
        }
        if (letra.equals("S")) {
            factor = ConfigMB.bigDecimal("originacion_factor_signature", "1.5");
        }
        return factor;
    }

    public static void cargarDatosConozcaSuCliente(ContextoMB contexto) {
        ApiRequestMB request = ApiMB.request("GetConozcaSuCliente", "personas", "GET", "/personas/{cuit}/preguntas", contexto);
        request.path("cuit", contexto.persona().cuit());
        ApiResponseMB response = ApiMB.response(request, contexto.idCobis());
        if (response.hayError()) {
            return;
        }

        Boolean pregunta01cargada = false;
        Boolean pregunta02cargada = false;
        Boolean pregunta03cargada = false;
        Boolean pregunta04cargada = false;
        Boolean pregunta14cargada = false;
        Boolean pregunta15cargada = false;
        Boolean pregunta16cargada = false;
        Boolean pregunta17cargada = false;
        for (Objeto item : response.objetos()) {
            pregunta01cargada = item.integer("idPregunta").equals(1) && item.bool("respuesta").equals(true) ? true : pregunta01cargada;
            pregunta02cargada = item.integer("idPregunta").equals(2) && item.bool("respuesta").equals(true) ? true : pregunta02cargada;
            pregunta03cargada = item.integer("idPregunta").equals(3) && item.bool("respuesta").equals(true) ? true : pregunta03cargada;
            pregunta04cargada = item.integer("idPregunta").equals(4) && item.bool("respuesta").equals(true) ? true : pregunta04cargada;
            pregunta14cargada = item.integer("idPregunta").equals(14) && item.bool("respuesta").equals(true) ? true : pregunta14cargada;
            pregunta15cargada = item.integer("idPregunta").equals(15) && item.bool("respuesta").equals(true) ? true : pregunta15cargada;
            pregunta16cargada = item.integer("idPregunta").equals(16) && item.bool("respuesta").equals(true) ? true : pregunta16cargada;
            pregunta17cargada = item.integer("idPregunta").equals(17) && item.bool("respuesta").equals(true) ? true : pregunta17cargada;
        }

        Objeto preguntas = new Objeto();

        Boolean invocar = false;
        if (!pregunta01cargada) {
            Objeto pregunta = new Objeto();
            pregunta.set("idPregunta", 1);
            pregunta.set("pregunta", "Se cumplio con la politica Conozca a su Cliente?");
            pregunta.set("canalModificacion", "HB");
            pregunta.set("usuarioModificacion", "HB");
            pregunta.set("respuesta", true);
            preguntas.add(pregunta);
            invocar = true;
        }
        if (!pregunta02cargada) {
            Objeto pregunta = new Objeto();
            pregunta.set("idPregunta", 2);
            pregunta.set("pregunta", "El cliente presenta toda la documentacion solicitada para el trazado del perfil?");
            pregunta.set("canalModificacion", "HB");
            pregunta.set("usuarioModificacion", "HB");
            pregunta.set("respuesta", true);
            preguntas.add(pregunta);
            invocar = true;
        }
        if (!pregunta03cargada) {
            Objeto pregunta = new Objeto();
            pregunta.set("idPregunta", 3);
            pregunta.set("pregunta", "Las operaciones que realizara el cliente son razonables al perfil trazado?");
            pregunta.set("canalModificacion", "HB");
            pregunta.set("usuarioModificacion", "HB");
            pregunta.set("respuesta", true);
            preguntas.add(pregunta);
            invocar = true;
        }
        if (!pregunta04cargada) {
            Objeto pregunta = new Objeto();
            pregunta.set("idPregunta", 4);
            pregunta.set("pregunta", "La actividad economica del cliente es razonable con el volumen y frecuencia de operaciones que realizara con el banco?");
            pregunta.set("canalModificacion", "HB");
            pregunta.set("usuarioModificacion", "HB");
            pregunta.set("respuesta", true);
            preguntas.add(pregunta);
            invocar = true;
        }
        if (!pregunta14cargada) {
            Objeto pregunta = new Objeto();
            pregunta.set("idPregunta", 14);
            pregunta.set("pregunta", "Se cumplio con la politica Conozca a su Cliente?");
            pregunta.set("canalModificacion", "HB");
            pregunta.set("usuarioModificacion", "HB");
            pregunta.set("respuesta", true);
            preguntas.add(pregunta);
            invocar = true;
        }
        if (!pregunta15cargada) {
            Objeto pregunta = new Objeto();
            pregunta.set("idPregunta", 15);
            pregunta.set("pregunta", "El cliente presenta toda la documentacion solicitada para el trazado del perfil?");
            pregunta.set("canalModificacion", "HB");
            pregunta.set("usuarioModificacion", "HB");
            pregunta.set("respuesta", true);
            preguntas.add(pregunta);
            invocar = true;
        }
        if (!pregunta16cargada) {
            Objeto pregunta = new Objeto();
            pregunta.set("idPregunta", 16);
            pregunta.set("pregunta", "Las operaciones que realizara el cliente son razonables al perfil trazado?");
            pregunta.set("canalModificacion", "HB");
            pregunta.set("usuarioModificacion", "HB");
            pregunta.set("respuesta", true);
            preguntas.add(pregunta);
            invocar = true;
        }
        if (!pregunta17cargada) {
            Objeto pregunta = new Objeto();
            pregunta.set("idPregunta", 17);
            pregunta.set("pregunta", "La actividad economica del cliente es razonable con el volumen y frecuencia de operaciones que realizara con el banco?");
            pregunta.set("canalModificacion", "HB");
            pregunta.set("usuarioModificacion", "HB");
            pregunta.set("respuesta", true);
            preguntas.add(pregunta);
            invocar = true;
        }

        if (invocar) {
            ApiRequestMB requestPost = ApiMB.request("PostConozcaSuCliente", "personas", "POST", "/personas/{cuit}/preguntas", contexto);
            requestPost.header("x-usuario", ConfigMB.string("configuracion_usuario"));
            requestPost.path("cuit", contexto.persona().cuit());
            requestPost.body(preguntas);

            ApiResponseMB responsePost = ApiMB.response(requestPost, contexto.idCobis());
            if (responsePost.hayError()) {
                return;
            }
        }
    }

    public static void cargarDatosPerfilPatrimonial(ContextoMB contexto) {
        ApiRequestMB request = ApiMB.request("GetPerfilPatrimonial", "personas", "GET", "/personas/{cuit}/perfilesPatrimoniales", contexto);
        request.path("cuit", contexto.persona().cuit());
        ApiResponseMB response = ApiMB.response(request, contexto.idCobis());
        if (response.hayError()) {
            return;
        }

        for (Integer idProducto : Objeto.listOf(6, 8, 9)) {
            for (String idConcepto : Objeto.listOf("1", "2", "3")) {
                Boolean crear = true;
                for (Objeto item : response.objetos()) {
                    if (idProducto.equals(item.integer("idProducto")) && idConcepto.equals(item.string("idConcepto"))) {
                        if (item.string("idFrecuencia", null) != null && item.string("idVolumen", null) != null) {
                            crear = false;
                            break;
                        }
                    }
                }
                if (crear) {
                    Objeto body = new Objeto();
                    body.set("idProducto", idProducto);
                    body.set("idConcepto", idConcepto);
                    body.set("idFrecuencia", 2);
                    body.set("idVolumen", 2);

                    ApiRequestMB requestPost = ApiMB.request("PostPerfilPatrimonial", "personas", "POST", "/personas/{cuit}/perfilesPatrimoniales", contexto);
                    requestPost.path("cuit", contexto.persona().cuit());
                    requestPost.body(body);

                    ApiResponseMB responsePost = ApiMB.response(requestPost, contexto.idCobis());
                    if (responsePost.hayError()) {
                        return;
                    }
                }
            }
        }
    }

    public static Boolean esSolicitudHB(ContextoMB contexto, String idSolicitud) {
        ApiResponseMB consultarSolicitud = consultarSolicitud(contexto, idSolicitud);
        if (consultarSolicitud.hayError()) {
            return false;
        }
        return consultarSolicitud.objetos("Datos").get(0).string("CanalOriginacion1").equals(ConfigMB.string("api_venta_canalOriginacion1"));
    }

    public static ApiResponseMB actualizarTarjetaDebito(ContextoMB contexto, String idSolicitud, String tipo, Integer grupo, Boolean virtual, List<Objeto> cuentasOperativas, Boolean altaOnline, Boolean requiereEmbozado, String idProducto) {
        Objeto integrante = new Objeto();
        integrante.set("numeroDocumentoTributario", contexto.persona().cuit());
        integrante.set("idCobis", Long.valueOf(contexto.idCobis()));
        integrante.set("rol", "T");

        ApiRequestMB request = ApiMB.request("VentasGenerarTarjetaDebito", "ventas_windows", "PUT", "/solicitudes/{SolicitudId}/tarjetaDebito/{ProductoId}", contexto);
        request.headers.put("X-Handle", idSolicitud);
        request.path("SolicitudId", idSolicitud);
        request.path("ProductoId", idProducto);

        request.body("Tipo", tipo);
        request.body("Domicilio").set("Tipo", "DP");
        request.body("Grupo", grupo);
        request.body("TipoCuentaComision", "4");
        request.body("NumeroCtaComision", "0");
        request.body("TarjetaDebitoCuentasOperativas", cuentasOperativas);
        request.add("Integrantes", integrante);
        request.body("TipoOperacion", "03");
        request.body("EsVirtual", virtual);
        if (altaOnline != null && altaOnline) {
            request.body("VisualizaVirtual", virtual ? "S" : "N");
            request.body("RequiereEmbozado", requiereEmbozado ? "S" : "N");
        }

        return ApiMB.response(request, contexto.idCobis());
    }

    public static ApiResponseMB generarTarjetaDebito(ContextoMB contexto, String idSolicitud, String tipo, Integer grupo, Boolean virtual, List<Objeto> cuentasOperativas, Boolean altaOnline, Boolean requiereEmbozado) {
        Objeto integrante = new Objeto();
        integrante.set("numeroDocumentoTributario", contexto.persona().cuit());
        integrante.set("idCobis", Long.valueOf(contexto.idCobis()));
        integrante.set("rol", "T");

        ApiRequestMB request = ApiMB.request("VentasGenerarTarjetaDebito", "ventas_windows", "POST", "/solicitudes/{SolicitudId}/tarjetaDebito", contexto);
        request.headers.put("X-Handle", idSolicitud);
        request.path("SolicitudId", idSolicitud);
        request.body("Tipo", tipo);
        request.body("Domicilio").set("Tipo", "DP");
        request.body("Grupo", grupo);
        request.body("TipoCuentaComision", "4");
        request.body("NumeroCtaComision", "0");
        request.body("TarjetaDebitoCuentasOperativas", cuentasOperativas);
        request.add("Integrantes", integrante);
        request.body("TipoOperacion", "03");
        request.body("EsVirtual", virtual);
        if (altaOnline != null && altaOnline) {
            request.body("VisualizaVirtual", virtual ? "S" : "N");
            request.body("RequiereEmbozado", requiereEmbozado ? "S" : "N");
        }

        return ApiMB.response(request, contexto.idCobis());
    }


}
