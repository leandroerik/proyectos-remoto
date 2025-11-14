package ar.com.hipotecario.canal.homebanking.servicio;

import java.math.BigDecimal;
import java.util.*;

import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.api.HBOmnicanalidad;
import ar.com.hipotecario.canal.homebanking.base.ConfigHB;
import ar.com.hipotecario.canal.homebanking.base.Objeto;
import ar.com.hipotecario.canal.homebanking.conector.Api;
import ar.com.hipotecario.canal.homebanking.conector.ApiRequest;
import ar.com.hipotecario.canal.homebanking.conector.ApiResponse;
import ar.com.hipotecario.canal.homebanking.lib.Fecha;
import ar.com.hipotecario.canal.homebanking.negocio.TarjetaCredito;
import ar.com.hipotecario.canal.homebanking.negocio.TarjetaDebito;

public class RestOmnicanalidad {

    /* ========== SOLICITUDES ========== */
    public static ApiResponse consultarSolicitudes(ContextoHB contexto, Long cantidadDias) {
        ApiRequest request = Api.request("VentasConsultarSolicitudes", "ventas_windows", "GET", "/solicitudes", contexto);
        request.query("cuil", contexto.persona().cuit());
        String fechaDesde = Fecha.restarDias(new Date(), cantidadDias, "yyyyMMdd");
        request.query("fechadesde", fechaDesde);
        return Api.response(request, contexto.idCobis());
    }

    public static ApiResponse consultarSolicitudesXEstado(ContextoHB contexto, Long cantidadDias, String estado) {
        ApiRequest request = Api.request("VentasConsultarSolicitudes", "ventas_windows", "GET", "/solicitudesestados", contexto);
        request.query("cuil", contexto.persona().cuit());
        request.query("fechadesde", Fecha.restarDias(new Date(), cantidadDias, "yyyyMMdd"));
        request.query("estado", estado);
        return Api.response(request, contexto.idCobis());
    }

    public static ApiResponse consultarSolicitud(ContextoHB contexto, String idSolicitud) {
        ApiRequest request = Api.request("VentasConsultarSolicitud", "ventas_windows", "GET", "/solicitudes/{idSolicitud}", contexto);
        request.headers.put("X-Handle", idSolicitud);
        request.path("idSolicitud", idSolicitud);
        return Api.response(request, contexto.idCobis(), idSolicitud);
    }

    public static ApiResponse actualizarCanalSolicitud(ContextoHB contexto, String idSolicitud) {
        ApiRequest requestSolicitud = Api.request("VentasConsultarSolicitud", "ventas_windows", "GET", "/solicitudes/{idSolicitud}", contexto);
        requestSolicitud.headers.put("X-Handle", idSolicitud);
        requestSolicitud.path("idSolicitud", idSolicitud);
        ApiResponse responseSolicitud = Api.response(requestSolicitud, contexto.idCobis(), idSolicitud);
        if (responseSolicitud.hayError()) {
            return responseSolicitud;
        }

        Objeto datos = responseSolicitud.objetos("Datos").get(0);

        ApiRequest request = Api.request("VentasPutCanal", "ventas_windows", "PUT", "/solicitudes/{SolicitudId}", contexto);
        request.headers.put("X-Handle", idSolicitud);
        request.path("SolicitudId", idSolicitud);
        request.body("TipoOperacion", "03");
        request.body("TieneCuentaInversor", datos.bool("TieneCuentaInversor") != null && datos.bool("TieneCuentaInversor"));
        request.body("CanalOriginacion1", ConfigHB.integer("api_venta_canalOriginacion1"));
        request.body("CanalOriginacion2", ConfigHB.integer("api_venta_canalOriginacion2"));
        request.body("CanalOriginacion3", ConfigHB.string("api_venta_canalOriginacion3"));
        request.body("CanalVenta1", datos.string("CanalVenta1"));
        request.body("CanalVenta2", datos.string("CanalVenta2"));
        request.body("CanalVenta3", datos.string("CanalVenta3"));
        request.body("CanalVenta4", datos.string("CanalVenta4"));
        request.body("Oficina", datos.string("Oficina"));
        return Api.response(request, contexto.idCobis(), idSolicitud);
    }

    public static ApiResponse actualizarUpgradePaquete(ContextoHB contexto, String idSolicitud, String paqueteDestino, String nroPaquete, Objeto integrante, String idUpgradePaquete, Objeto detalle) {

        ApiResponse responseSolicitud = consultarSolicitud(contexto, idSolicitud);
        if (responseSolicitud.hayError()) {
            return responseSolicitud;
        }
        ApiRequest request = Api.request("VentasUpgradePaquete", "ventas_windows", "PUT", "/solicitudes/{SolicitudId}/UpgradePaquete/{id}", contexto);
        request.headers.put("X-Handle", idSolicitud);
        request.path("SolicitudId", idSolicitud);
        request.path("id", idUpgradePaquete);
        request.body("TipoOperacion", "03");
        request.body("IdProductoFrontEnd", "35");
        request.body("Oficial", 6);
        request.body("Oficina", "0");
        request.body("CanalVenta", null);
        request.body("OficialVenta", 0);
        request.body("IdCobisTitular", null);
        request.body("NumeroPaquete", nroPaquete);
        request.body("TipoPaqueteDestino", paqueteDestino);
        request.add("Integrantes", integrante);
        request.add("Detalles", detalle);
        return Api.response(request, contexto.idCobis());
    }

    public static ApiResponse actualizarCanalSolicitudV2(ContextoHB contexto, String idSolicitud, boolean tieneCuentaInversor) {
        ApiRequest requestSolicitud = Api.request("VentasConsultarSolicitud", "ventas_windows", "GET", "/solicitudes/{idSolicitud}", contexto);
        requestSolicitud.headers.put("X-Handle", idSolicitud);
        requestSolicitud.path("idSolicitud", idSolicitud);
        ApiResponse responseSolicitud = Api.response(requestSolicitud, contexto.idCobis(), idSolicitud);
        if (responseSolicitud.hayError()) {
            return responseSolicitud;
        }

        Objeto datos = responseSolicitud.objetos("Datos").get(0);

        ApiRequest request = Api.request("VentasPutCanal", "ventas_windows", "PUT", "/solicitudes/{SolicitudId}", contexto);
        request.headers.put("X-Handle", idSolicitud);
        request.path("SolicitudId", idSolicitud);
        request.body("TipoOperacion", "03");
        request.body("TieneCuentaInversor", tieneCuentaInversor);
        request.body("CanalOriginacion1", ConfigHB.integer("api_venta_canalOriginacion1"));
        request.body("CanalOriginacion2", ConfigHB.integer("api_venta_canalOriginacion2"));
        request.body("CanalOriginacion3", ConfigHB.string("api_venta_canalOriginacion3"));
        request.body("CanalVenta1", datos.string("CanalVenta1"));
        request.body("CanalVenta2", datos.string("CanalVenta2"));
        request.body("CanalVenta3", datos.string("CanalVenta3"));
        request.body("CanalVenta4", datos.string("CanalVenta4"));
        request.body("Oficina", datos.string("Oficina"));
        return Api.response(request, contexto.idCobis(), idSolicitud);
    }

    public static ApiResponse generarSolicitud(ContextoHB contexto) {
        ApiRequest request = Api.request("VentasGenerarSolicitud", "ventas_windows", "POST", "/solicitudes", contexto);
        request.headers.put("X-Handle", "0");
        request.body("TipoOperacion", "03");
        request.body("CanalOriginacion1", ConfigHB.integer("api_venta_canalOriginacion1"));
        request.body("CanalOriginacion2", ConfigHB.integer("api_venta_canalOriginacion2"));
        request.body("CanalOriginacion3", ConfigHB.string("api_venta_canalOriginacion3"));
        request.body("CanalVenta1", ConfigHB.string("api_venta_canalVenta1"));
        request.body("CanalVenta2", ConfigHB.string("api_venta_canalVenta2"));
        request.body("CanalVenta3", ConfigHB.string("api_venta_canalVenta3"));
        request.body("CanalVenta4", ConfigHB.string("api_venta_canalVenta4"));
        request.body("Oficina", "0");
        return Api.response(request, contexto.idCobis());
    }

    public static ApiResponse evaluarSolicitud(ContextoHB contexto, String idSolicitud) {
        RestOmnicanalidad.actualizarCanalSolicitud(contexto, idSolicitud);

        ApiRequest request = Api.request("VentasEvaluarSolicitud", "ventas_windows", "PUT", "/solicitudes/{SolicitudId}/resoluciones", contexto);
        request.headers.put("X-Handle", idSolicitud);
        request.path("SolicitudId", idSolicitud);
        request.body("TipoOperacion", "03");
        request.body("IdSolicitud", idSolicitud);
        return Api.response(request, contexto.idCobis());
    }

    public static ApiResponse finalizarSolicitud(ContextoHB contexto, String idSolicitud) {
        ApiRequest request = Api.request("VentasFinalizarSolicitud", "ventas_windows", "GET", "/solicitudes/{SolicitudId}", contexto);
        request.headers.put("X-Handle", idSolicitud);
        request.path("SolicitudId", idSolicitud);
        request.query("estado", "finalizar");
        return Api.response(request, contexto.idCobis());
    }

    public static ApiResponse desistirSolicitud(ContextoHB contexto, String idSolicitud) {
        ApiRequest request = Api.request("VentasDesistirSolicitud", "ventas_windows", "GET", "/solicitudes/{idSolicitud}", contexto);
        request.headers.put("X-Handle", idSolicitud);
        request.path("idSolicitud", idSolicitud);
        request.query("estado", "desistir");
        return Api.response(request, contexto.idCobis());
    }

    public static ApiResponse desistirSolicitudBPM(ContextoHB contexto, String idSolicitud) {
        ApiRequest request = Api.request("ProcesosDesistirSolicitud", "procesos", "POST", "/procesos-de-negocio/v1/solicitudes/{idSolicitud}/notificacion/accion", contexto);
        request.headers.put("X-Handle", idSolicitud);
        request.path("idSolicitud", idSolicitud);
        request.body("idSolicitud", Integer.valueOf(idSolicitud));
        request.body("idAccion", 3);
        return Api.response(request, contexto.idCobis(), idSolicitud);
    }

    public static String limpiarSolicitudes(ContextoHB contexto, Long cantidadDias, Boolean limpiarPrestamos, Boolean limpiarPaquetes, Boolean eliminarProductos) {
        ApiResponse response = RestOmnicanalidad.consultarSolicitudes(contexto, cantidadDias);
        if (response.hayError() || !response.objetos("Errores").isEmpty()) {
            return "";
        }

        List<String> idSolicitudes = new ArrayList<>();
        for (Objeto datos : response.objetos("Datos")) {
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
            ApiResponse responseSolicitud = RestOmnicanalidad.consultarSolicitud(contexto, idSolicitud);
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

//					ApiResponse datosPaquete = RestOmnicanalidad.buscarProducto(contexto, idSolicitud, RestOmnicanalidad.recursos().get("32"), idPaquete);
//					String codigo = datosPaquete.objetos("Datos").get(0).string("Paquete.TipoPaquete");

                    contexto.parametros.set("idSolicitud", idSolicitud);
                    contexto.parametros.set("codigoPaquete", "0");
                    contexto.parametros.set("ejecutarMotor", false);
                    HBOmnicanalidad.actualizarSolicitudPaquete(contexto);
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
            String idTarjetaDebito = null;
            String idSeguroDesempleo = null;
            ApiResponse responseSolicitud = RestOmnicanalidad.consultarSolicitud(contexto, idSolicitud);
            for (Objeto producto : responseSolicitud.objetos("Datos").get(0).objetos("Productos")) {
                idPaquete = producto.string("tipoProducto").equals("32") ? producto.string("Id") : idPaquete;
                idPrestamo = producto.string("tipoProducto").equals("2") ? producto.string("Id") : idPrestamo;
                idCajaAhorro = producto.string("tipoProducto").equals("8") ? producto.string("Id") : idCajaAhorro;
                idTarjetaDebito = producto.string("tipoProducto").equals("11") ? producto.string("Id") : idTarjetaDebito;
                idSeguroDesempleo = producto.string("tipoProducto").equals("31") ? producto.string("Id") : idSeguroDesempleo;
            }
            if (idPaquete != null && idPrestamo == null && (limpiarPaquetes)) {
                RestOmnicanalidad.desistirSolicitud(contexto, idSolicitud);
            }
            if (idPaquete == null && idPrestamo != null && (limpiarPrestamos)) {
                RestOmnicanalidad.desistirSolicitud(contexto, idSolicitud);
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
                    if (idTarjetaDebito != null) {
                        RestOmnicanalidad.eliminarProducto(contexto, idSolicitud, recursos().get("11"), idTarjetaDebito);
                    }
                    if (idSeguroDesempleo != null) {
                        RestOmnicanalidad.eliminarProducto(contexto, idSolicitud, recursos().get("31"), idSeguroDesempleo);
                    }
                    idSolicitudProxima = idSolicitud;
                }
                if (limpiarPaquetes && limpiarPrestamos) {
                    RestOmnicanalidad.desistirSolicitud(contexto, idSolicitud);
                }
            }
        }

        return idSolicitudProxima;
    }

    /* ========== INTEGRANTES ========== */
    public static ApiResponse generarIntegrante(ContextoHB contexto, String idSolicitud) {
        ApiRequest request = Api.request("VentasGenerarIntegrante", "ventas_windows", "POST", "/solicitudes/{SolicitudId}/integrantes", contexto);
        request.headers.put("X-Handle", idSolicitud);
        request.path("SolicitudId", idSolicitud);
        request.body("NumeroTributario", Long.valueOf(contexto.persona().cuit()));
        request.body("TipoOperacion", "03");
        ApiResponse response = Api.response(request, contexto.idCobis());

        if (contexto.persona().idEstadoCivil().equals("C")) {
            String cuitConyuge = RestPersona.cuitConyuge(contexto);
            if (cuitConyuge != null) {
                ApiRequest requestConyuge = Api.request("VentasGenerarIntegrante", "ventas_windows", "POST", "/solicitudes/{SolicitudId}/integrantes", contexto);
                requestConyuge.headers.put("X-Handle", idSolicitud);
                requestConyuge.path("SolicitudId", idSolicitud);
                requestConyuge.body("NumeroTributario", Long.valueOf(cuitConyuge));
                requestConyuge.body("TipoOperacion", "03");
                Api.response(requestConyuge, contexto.idCobis());
            }
        }

        return response;
    }

    public static ApiResponse generarIntegrante(ContextoHB contexto, String idSolicitud, String cuit) {
        ApiRequest request = Api.request("VentasGenerarIntegrante", "ventas_windows", "POST", "/solicitudes/{SolicitudId}/integrantes", contexto);
        request.headers.put("X-Handle", idSolicitud);
        request.path("SolicitudId", idSolicitud);
        request.body("NumeroTributario", Long.valueOf(cuit));
        request.body("TipoOperacion", "03");
        ApiResponse response = Api.response(request, contexto.idCobis());
        return response;
    }

    /* ========== PRODUCTOS ========== */
    public static ApiResponse buscarProducto(ContextoHB contexto, String idSolicitud, String recurso, String idProducto) {
        ApiRequest request = Api.request("VentasConsultarProducto", "ventas_windows", "GET", "/solicitudes/{idSolicitud}/{recurso}/{idProducto}", contexto);
        request.headers.put("X-Handle", idSolicitud);
        request.path("idSolicitud", idSolicitud);
        request.path("recurso", recurso);
        request.path("idProducto", idProducto);
        return Api.response(request, contexto.idCobis());
    }

    public static ApiResponse generarCajaAhorro(ContextoHB contexto, String idSolicitud, String idMoneda, String idTarjetaDebito, String categoria) {
        TarjetaDebito tarjetaDebito = contexto.tarjetaDebito(idTarjetaDebito);

        Objeto integrante = new Objeto();
        integrante.set("numeroDocumentoTributario", contexto.persona().cuit());
        integrante.set("idCobis", Long.valueOf(contexto.idCobis()));
        if (tarjetaDebito != null) {
            integrante.set("NumeroTarjetaDebito", tarjetaDebito.numero());
        }
        integrante.set("rol", "T");

        ApiRequest request = Api.request("VentasGenerarCajaAhorro", "ventas_windows", "POST", "/solicitudes/{SolicitudId}/cajaAhorro", contexto);
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
        return Api.response(request, contexto.idCobis());
    }

    public static ApiResponse actualizarCajaAhorro(ContextoHB contexto, String idSolicitud, String idMoneda, String id, String idTarjetaDebito, String categoria) {
        TarjetaDebito tarjetaDebito = contexto.tarjetaDebito(idTarjetaDebito);

        Objeto integrante = new Objeto();
        integrante.set("numeroDocumentoTributario", contexto.persona().cuit());
        integrante.set("idCobis", Long.valueOf(contexto.idCobis()));
        if (tarjetaDebito != null) {
            integrante.set("NumeroTarjetaDebito", tarjetaDebito.numero());
        }
        integrante.set("rol", "T");

        ApiRequest request = Api.request("VentasActualizarCajaAhorro", "ventas_windows", "PUT", "/solicitudes/{SolicitudId}/cajaAhorro/{id}", contexto);
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
        return Api.response(request, contexto.idCobis());
    }

    public static ApiResponse generarTarjetaDebito(ContextoHB contexto, String idSolicitud, String tipo, Integer grupo, Boolean virtual, List<Objeto> cuentasOperativas, Boolean altaOnline, Boolean requiereEmbozado) {
        Objeto integrante = new Objeto();
        integrante.set("numeroDocumentoTributario", contexto.persona().cuit());
        integrante.set("idCobis", Long.valueOf(contexto.idCobis()));
        integrante.set("rol", "T");

        ApiRequest request = Api.request("VentasGenerarTarjetaDebito", "ventas_windows", "POST", "/solicitudes/{SolicitudId}/tarjetaDebito", contexto);
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

        return Api.response(request, contexto.idCobis());
    }

    public static ApiResponse generarTarjetaDebito(ContextoHB contexto, String idSolicitud, Boolean virtual, Boolean altaOnline, Boolean requiereEmbozado) {
        Objeto integrante = new Objeto();
        integrante.set("numeroDocumentoTributario", contexto.persona().cuit());
        integrante.set("idCobis", Long.valueOf(contexto.idCobis()));
        integrante.set("rol", "T");

        ApiRequest request = Api.request("VentasGenerarTarjetaDebito", "ventas_windows", "POST", "/solicitudes/{SolicitudId}/tarjetaDebito", contexto);
        request.headers.put("X-Handle", idSolicitud);
        request.path("SolicitudId", idSolicitud);
        request.add("Integrantes", integrante);
        request.body("TipoOperacion", "03");
        request.body("EsVirtual", virtual);
        if (altaOnline != null && altaOnline) {
            request.body("VisualizaVirtual", virtual ? "S" : "N");
            request.body("RequiereEmbozado", requiereEmbozado ? "S" : "N");
        }

        return Api.response(request, contexto.idCobis());
    }

    public static ApiResponse actualizarTarjetaDebito(ContextoHB contexto, String idSolicitud, String tipo, Integer grupo, Boolean virtual, List<Objeto> cuentasOperativas, Boolean altaOnline, Boolean requiereEmbozado, String idProducto) {
        Objeto integrante = new Objeto();
        integrante.set("numeroDocumentoTributario", contexto.persona().cuit());
        integrante.set("idCobis", Long.valueOf(contexto.idCobis()));
        integrante.set("rol", "T");

        ApiRequest request = Api.request("VentasGenerarTarjetaDebito", "ventas_windows", "PUT", "/solicitudes/{SolicitudId}/tarjetaDebito/{ProductoId}", contexto);
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

        return Api.response(request, contexto.idCobis());
    }

    public static ApiResponse generarPaquete(ContextoHB contexto, String idSolicitud, Integer codigoPaquete, Boolean generarTarjetaCredito) {
        ApiRequest request = Api.request("VentasGenerarPaquete", "ventas_windows", "POST", "/solicitudes/{SolicitudId}/solicitudPaquete", contexto);
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

        return Api.response(request, contexto.idCobis());
    }

    public static ApiResponse generarTarjetaCreditoAdicional(ContextoHB contexto, String idSolicitud, String cuitAdicional) {
        TarjetaCredito tarjetaCredito = contexto.tarjetaCreditoTitular();

        ApiRequest request = Api.request("VentasGenerarTarjetaCreditoAdicional", "ventas_windows", "POST", "/solicitudes/{SolicitudId}/inclusionadicionaltc", contexto);
        request.headers.put("X-Handle", idSolicitud);
        request.path("SolicitudId", idSolicitud);
        request.body("NumeroCuenta", tarjetaCredito.numeroCuenta());
        request.add("Integrantes", new Objeto().set("NumeroDocumentoTributario", contexto.persona().cuit()).set("Rol", "T"));
        request.add("Integrantes", new Objeto().set("NumeroDocumentoTributario", cuitAdicional).set("Rol", "A"));
        request.body("TipoOperacion", "02");
        return Api.response(request, contexto.idCobis());
    }

    public static ApiResponse actualizarTarjetaCreditoAdicional(ContextoHB contexto, String idSolicitud, String idTarjeta, String cuitAdicional, String embozado, Integer porcentaje) {
        TarjetaCredito tarjetaCredito = contexto.tarjetaCreditoTitular();

        Objeto adicional = new Objeto();
        adicional.set("NumeroDocumentoTributario", cuitAdicional);
        adicional.set("Limite", porcentaje);
        adicional.set("LimiteCompra", porcentaje);
        adicional.set("LimiteAdelanto", porcentaje);
        adicional.set("Embozado", embozado);

        ApiRequest request = Api.request("VentasActualizarTarjetaCreditoAdicional", "ventas_windows", "PUT", "/solicitudes/{SolicitudId}/inclusionadicionaltc/{ProductoId}", contexto);
        request.headers.put("X-Handle", idSolicitud);
        request.path("SolicitudId", idSolicitud);
        request.path("ProductoId", idTarjeta);
        request.body("NumeroCuenta", tarjetaCredito.numeroCuenta());
        request.add("Integrantes", new Objeto().set("NumeroDocumentoTributario", contexto.persona().cuit()).set("Rol", "T"));
        request.add("Integrantes", new Objeto().set("NumeroDocumentoTributario", cuitAdicional).set("Rol", "A"));
        request.add("TarjetaCreditoAdicionales", adicional);
        request.body("TipoOperacion", "03");
        return Api.response(request, contexto.idCobis());
    }

    public static ApiResponse eliminarProducto(ContextoHB contexto, String idSolicitud, String recurso, String idProducto) {
        ApiRequest request = Api.request("VentasEliminarProducto", "ventas_windows", "DELETE", "/solicitudes/{idSolicitud}/{recurso}/{idProducto}", contexto);
        request.headers.put("X-Handle", idSolicitud);
        request.path("idSolicitud", idSolicitud);
        request.path("recurso", recurso);
        request.path("idProducto", idProducto);
        return Api.response(request, contexto.idCobis(), idSolicitud, idProducto);
    }

    /* ========== UTILITARIO ========== */
    public static Set<String> productosPaquetizables() {
        return Objeto.setOf("8", "9", "6", "7", "11", "5");
    }

    public static Set<String> productosOperables() {
        return Objeto.setOf("2", "5", "6", "7", "8", "9", "11", "31", "32", "35");
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
        mapa.put("35", "Upgrade paquete");
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
        mapa.put("35", "upgradePaquete");
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
        mapa.put("35", "upgradePaquete");
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
            factor = ConfigHB.bigDecimal("originacion_factor_internacional", "1");
        }
        if (letra.equals("P")) {
            factor = ConfigHB.bigDecimal("originacion_factor_gold", "1");
        }
        if (letra.equals("L")) {
            factor = ConfigHB.bigDecimal("originacion_factor_platinum", "1.5");
        }
        if (letra.equals("S")) {
            factor = ConfigHB.bigDecimal("originacion_factor_signature", "1.5");
        }
        return factor;
    }

    public static Boolean cargarDatosConozcaSuCliente(ContextoHB contexto) {
        ApiRequest request = Api.request("GetConozcaSuCliente", "personas", "GET", "/personas/{cuit}/preguntas", contexto);
        request.path("cuit", contexto.persona().cuit());
        ApiResponse response = Api.response(request, contexto.idCobis());
        if (response.hayError()) {
            return false;
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
            ApiRequest requestPost = Api.request("PostConozcaSuCliente", "personas", "POST", "/personas/{cuit}/preguntas", contexto);
            requestPost.header("x-usuario", ConfigHB.string("configuracion_usuario"));
            requestPost.path("cuit", contexto.persona().cuit());
            requestPost.body(preguntas);

            ApiResponse responsePost = Api.response(requestPost, contexto.idCobis());
            if (responsePost.hayError()) {
                return false;
            }
        }

        return true;
    }

    public static Boolean cargarDatosPerfilPatrimonial(ContextoHB contexto) {
        ApiRequest request = Api.request("GetPerfilPatrimonial", "personas", "GET", "/personas/{cuit}/perfilesPatrimoniales", contexto);
        request.path("cuit", contexto.persona().cuit());
        ApiResponse response = Api.response(request, contexto.idCobis());
        if (response.hayError()) {
            return false;
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

                    ApiRequest requestPost = Api.request("PostPerfilPatrimonial", "personas", "POST", "/personas/{cuit}/perfilesPatrimoniales", contexto);
                    requestPost.path("cuit", contexto.persona().cuit());
                    requestPost.body(body);

                    ApiResponse responsePost = Api.response(requestPost, contexto.idCobis());
                    if (responsePost.hayError()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
