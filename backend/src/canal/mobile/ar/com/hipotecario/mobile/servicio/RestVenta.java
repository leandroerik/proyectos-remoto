package ar.com.hipotecario.mobile.servicio;

import java.math.BigDecimal;
import java.util.Date;

import ar.com.hipotecario.mobile.ConfigMB;
import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.conector.ApiMB;
import ar.com.hipotecario.mobile.conector.ApiRequestMB;
import ar.com.hipotecario.mobile.conector.ApiResponseMB;
import ar.com.hipotecario.mobile.lib.Fecha;
import ar.com.hipotecario.mobile.lib.Objeto;
import ar.com.hipotecario.mobile.negocio.Cuenta;
import ar.com.hipotecario.mobile.negocio.TarjetaCredito;
import ar.com.hipotecario.mobile.negocio.TarjetaDebito;

public class RestVenta {

    /* ========== GENERAL ========== */
    public static ApiResponseMB consultarSolicitudes(ContextoMB contexto) {

        String idSolicitud = contexto.parametros.string("idSolicitud");

        ApiRequestMB request = ApiMB.request("VentasConsultarSolicitudes", "ventas_windows", "GET", "/solicitudes/{SolicitudId}", contexto);
        request.query("cuil", contexto.persona().cuit());
        request.query("fechadesde", Fecha.restarDias(new Date(), 5L, "yyyyMMdd"));
        request.path("SolicitudId", idSolicitud);
        return ApiMB.response(request, contexto.idCobis());
    }

    public static ApiResponseMB consultarSolicitudes(ContextoMB contexto, String idSolicitud) {
        ApiRequestMB request = ApiMB.request("VentasConsultarSolicitudes", "ventas_windows", "GET", "/solicitudes/{SolicitudId}", contexto);
        request.query("cuil", contexto.persona().cuit());
        request.query("fechadesde", Fecha.restarDias(new Date(), 5L, "yyyyMMdd"));
        request.path("SolicitudId", idSolicitud);
        return ApiMB.response(request, contexto.idCobis());
    }

    public static ApiResponseMB consultarTarjetaCredito(ContextoMB contexto, String idSolicitud, String idTarjetaCredito) {
        ApiRequestMB request = ApiMB.request("VentasConsultarSolicitudes", "ventas_windows", "GET", "/solicitudes/{idSolicitud}/tarjetaCredito/{idTarjetaCredito}", contexto);
        request.path("idSolicitud", idSolicitud);
        request.path("idTarjetaCredito", idTarjetaCredito);
        return ApiMB.response(request, contexto.idCobis());
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

    public static ApiResponseMB generarIntegrante(ContextoMB contexto, String idSolicitud) {
        return RestOmnicanalidad.generarIntegrante(contexto, idSolicitud);
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

    static Objeto integrante(ContextoMB contexto/* , String idTarjetaDebito */) {
//		TarjetaDebito tarjetaDebito = contexto.tarjetaDebito(idTarjetaDebito);
        Objeto item = new Objeto();
        item.set("NumeroDocumentoTributario", contexto.persona().cuit());
        item.set("IdCobis", Long.valueOf(contexto.idCobis()));
//		item.set("NumeroTarjetaDebito", tarjetaDebito != null ? tarjetaDebito.numero() : null);
        item.set("Rol", "T");
        return item;
    }

    public static ApiResponseMB agregarSeguroDesempleo(ContextoMB contexto, String idSolicitud, String idProducto) {
        Objeto integrante = new Objeto();
        integrante.set("numeroDocumentoTributario", contexto.persona().cuit());

        integrante.set("rol", "T");

        ApiRequestMB request = ApiMB.request("VentasGenerarSeguroDesempleo", "ventas_windows", "POST", "/solicitudes/{SolicitudId}/seguroDesempleo", contexto);
        request.headers.put("X-Handle", idSolicitud);
        request.path("SolicitudId", idSolicitud);
        request.add("Integrantes", integrante);

        request.body("TipoOperacion", "02");
        return ApiMB.response(request, contexto.idCobis());
    }

    public static ApiResponseMB modificarSeguroDesempleo(ContextoMB contexto, String idSolicitud, String idSeguro, String idPrestamo) {
        TarjetaDebito tarjetaDebito = contexto.tarjetaDebitoPorDefecto();

        Objeto integrante = new Objeto();
        integrante.set("numeroDocumentoTributario", contexto.persona().cuit());
        integrante.set("idCobis", Long.valueOf(contexto.idCobis()));
        integrante.set("numeroTarjetaDebito", tarjetaDebito != null ? tarjetaDebito.numero() : null);
        integrante.set("rol", "T");

        ApiRequestMB request = ApiMB.request("VentasGenerarSeguroDesempleo", "ventas_windows", "PUT", "/solicitudes/{SolicitudId}/seguroDesempleo/{ProductoId}", contexto);
        request.headers.put("X-Handle", idSolicitud);
        request.path("SolicitudId", idSolicitud);
        request.path("ProductoId", idSeguro);
        request.add("Integrantes", integrante);

        ApiResponseMB responsePersona = RestPersona.consultarClienteEspecifico(contexto, contexto.idCobis());
        // El Producto Depende de la actividad laboral del cliente, si es relación de
        // dependencia fijo enviar "03278002" sino enviar "18178002"
        if ("1".equals(responsePersona.objetos().get(0).string("idSituacionLaboral"))) {
            request.body("Producto", "03278002");
        } else {
            request.body("Producto", "18178002");
        }
        request.body("DomicilioEnvio").set("Tipo", "DP");
        request.body("DomicilioLaboral").set("Tipo", "LA");
        Objeto ddjj = new Objeto();
        ddjj.set("PreguntaNoAsegurable1", false);
        ddjj.set("PreguntaNoAsegurable2", false);
        ddjj.set("PreguntaNoAsegurable3", false);
        ddjj.set("PreguntaNoAsegurable4", false);
        request.body("Ddjj", ddjj);
        Objeto seguroMedioPago = new Objeto();
        seguroMedioPago.set("TipoMedioPago", "BH");
        Objeto pagoBh = new Objeto();
        pagoBh.set("EsProductoTramite", true);
        pagoBh.set("TipoProducto", "CCA");
        pagoBh.set("NumeroProducto", idPrestamo);
        seguroMedioPago.set("PagoBH", pagoBh);
        request.body("SeguroMedioPago", seguroMedioPago);
        request.body("TipoOperacion", "03");
        return ApiMB.response(request, contexto.idCobis());
    }

    public static ApiResponseMB consultarSeguroDesempleo(ContextoMB contexto, String idSolicitud, String idProducto) {
        ApiRequestMB request = ApiMB.request("VentasConsultarSolicitudPrestamoPersonal", "ventas_windows", "GET", "/solicitudes/{SolicitudId}/seguroDesempleo/{ProductoId}", contexto);
        request.headers.put("X-Handle", idSolicitud);
        request.path("SolicitudId", idSolicitud);
        request.path("ProductoId", idProducto);
        return ApiMB.response(request, contexto.idCobis());
    }

    public static ApiResponseMB eliminarSeguroDesempleo(ContextoMB contexto, String idSolicitud, String idProducto) {
        ApiRequestMB request = ApiMB.request("VentasEliminarSeguroDesempleo", "ventas_windows", "DELETE", "/solicitudes/{SolicitudId}/seguroDesempleo/{ProductoId}", contexto);
        request.headers.put("X-Handle", idSolicitud);
        request.path("SolicitudId", idSolicitud);
        request.path("ProductoId", idProducto);
        return ApiMB.response(request, contexto.idCobis());
    }

    public static ApiResponseMB consultarMontoSeguroDesempleo(ContextoMB contexto, String idSolicitud, String idProducto) {
        ApiRequestMB request = ApiMB.request("VentasMontoSeguroDesempleo", "ventas_windows", "GET", "/solicitudes/{SolicitudId}/parametria/31?campo=producto", contexto);
        request.headers.put("X-Handle", idSolicitud);
        request.path("SolicitudId", idSolicitud);
        return ApiMB.response(request, contexto.idCobis());
    }

    /* ========== PRODUCTOS ========== */
    public static ApiResponseMB generarCajaAhorroPesos(ContextoMB contexto, String idSolicitud) {
        TarjetaDebito tarjetaDebito = contexto.tarjetaDebitoPorDefecto();

        Objeto integrante = new Objeto();
        integrante.set("numeroDocumentoTributario", contexto.persona().cuit());
        integrante.set("idCobis", Long.valueOf(contexto.idCobis()));
        integrante.set("numeroTarjetaDebito", tarjetaDebito != null ? tarjetaDebito.numero() : null);
        integrante.set("rol", "T");

        ApiRequestMB request = ApiMB.request("VentasGenerarCajaAhorroPesos", "ventas_windows", "POST", "/solicitudes/{SolicitudId}/cajaAhorro", contexto);
        request.headers.put("X-Handle", idSolicitud);
        request.path("SolicitudId", idSolicitud);
        request.body("Categoria", "MOV");
        request.body("ProductoBancario", "3");
        request.body("DomicilioResumen").set("tipo", "DP");
        request.body("Oficial", "0");
        request.body("CobroPrimerMantenimiento", false);
        request.body("Moneda", "80");
        request.body("Origen", "10");
        request.body("UsoFirma", "U");
        request.body("Ciclo", "6");
        request.body("ResumenMagnetico", true);
        request.body("TransfiereAcredHab", false);
        request.add("Integrantes", integrante);
        request.body("CuentaLegales").set("Uso", "PER").set("RealizaTransferencias", false);
        request.body("IdSolicitud", idSolicitud);
        request.body("TipoOperacion", "03");
        return ApiMB.response(request, contexto.idCobis());
    }

    public static ApiResponseMB eliminarCajaAhorroPesos(ContextoMB contexto, String idSolicitud, String idProducto) {
        ApiRequestMB request = ApiMB.request("VentasEliminarCajaAhorroPesos", "ventas_windows", "DELETE", "/solicitudes/{SolicitudId}/cajaAhorro/{ProductoId}", contexto);
        request.headers.put("X-Handle", idSolicitud);
        request.path("SolicitudId", idSolicitud);
        request.path("ProductoId", idProducto);
        return ApiMB.response(request, contexto.idCobis());
    }

    public static ApiResponseMB generarCajaAhorroDolares(ContextoMB contexto, String idSolicitud) {
        TarjetaDebito tarjetaDebito = contexto.tarjetaDebitoPorDefecto();

        Objeto integrante = new Objeto();
        integrante.set("numeroDocumentoTributario", contexto.persona().cuit());
        integrante.set("idCobis", Long.valueOf(contexto.idCobis()));
        integrante.set("numeroTarjetaDebito", tarjetaDebito != null ? tarjetaDebito.numero() : null);
        integrante.set("rol", "T");

        ApiRequestMB request = ApiMB.request("VentasGenerarCajaAhorroDolares", "ventas_windows", "POST", "/solicitudes/{SolicitudId}/cajaAhorro", contexto);
        request.headers.put("X-Handle", idSolicitud);
        request.path("SolicitudId", idSolicitud);
        request.body("Categoria", "MOV");
        request.body("ProductoBancario", "3");
        request.body("DomicilioResumen").set("tipo", "DP");
        request.body("Oficial", "0");
        request.body("CobroPrimerMantenimiento", false);
        request.body("Moneda", "2");
        request.body("Origen", "10");
        request.body("UsoFirma", "U");
        request.body("Ciclo", "6");
        request.body("ResumenMagnetico", true);
        request.body("TransfiereAcredHab", false);
        request.add("Integrantes", integrante);
        request.body("CuentaLegales").set("Uso", "PER").set("RealizaTransferencias", false);
        request.body("IdSolicitud", idSolicitud);
        request.body("TipoOperacion", "03");
        return ApiMB.response(request, contexto.idCobis());
    }

    public static ApiResponseMB generarCuentaCorriente(ContextoMB contexto, String idSolicitud, String categoria, String numeroCA) {
        TarjetaDebito tarjetaDebito = contexto.tarjetaDebitoPorDefecto();
        // categoria Acuerdo "D"
        // categoria Adelanto "ADE"

        Objeto integrante = new Objeto();
        integrante.set("numeroDocumentoTributario", contexto.persona().cuit());
        integrante.set("idCobis", Long.valueOf(contexto.idCobis()));
        integrante.set("numeroTarjetaDebito", tarjetaDebito != null ? tarjetaDebito.numero() : null);
        integrante.set("rol", "T");

        ApiRequestMB request = ApiMB.request("VentasGenerarCuentaCorriente", "ventas_windows", "POST", "/solicitudes/{SolicitudId}/cuentaCorriente", contexto);
        request.headers.put("X-Handle", idSolicitud);
        request.path("SolicitudId", idSolicitud);
        request.body("Categoria", categoria);
        request.body("ProductoBancario", "1");
        request.body("IdProductoFrontEnd", "7");
        request.body("CajaAhorroExistente", numeroCA);
        request.body("DomicilioResumen").set("tipo", "DP");
        request.body("Oficial", "0");
        request.body("CobroPrimerMantenimiento", false);
        request.body("Moneda", "80");
        request.body("Origen", "10");
        request.body("UsoFirma", "U");
        request.body("Ciclo", "6");
        request.body("ResumenMagnetico", true);
        request.body("TransfiereAcredHab", false);
        request.add("Integrantes", integrante);
        request.body("CuentaLegales").set("Uso", "PER").set("RealizaTransferencias", false);
        request.body("IdSolicitud", idSolicitud);
        request.body("TipoOperacion", "03");
        request.body("EmpresaAseguradora", "40");
        return ApiMB.response(request, contexto.idCobis());
    }

    public static ApiResponseMB generarTarjetaDebito(ContextoMB contexto, String idSolicitud, String... numerosCuentasAsociadas) {
        Objeto integrante = new Objeto();
        integrante.set("numeroDocumentoTributario", contexto.persona().cuit());
        integrante.set("idCobis", Long.valueOf(contexto.idCobis()));
        integrante.set("rol", "T");

        ApiRequestMB request = ApiMB.request("VentasWindowsPostTarjetaDebito", "ventas_windows", "POST", "/solicitudes/{SolicitudId}/tarjetaDebito", contexto);
        request.headers.put("X-Handle", idSolicitud);
        request.path("SolicitudId", idSolicitud);
        request.body("Tipo", "NV");
        request.body("Domicilio").set("Tipo", "DP");
        request.body("Grupo", "3");
        request.body("TipoCuentaComision", "4");
        request.body("NumeroCtaComision", "0");
        request.add("Integrantes", integrante);
        request.body("IdSolicitud", idSolicitud);
        request.body("TipoOperacion", "03");

        for (String numeroCuentaAsociada : numerosCuentasAsociadas) {
            Objeto item = new Objeto();
            item.set("producto", numeroCuentaAsociada.startsWith("3") ? "3" : "4");
            item.set("cuenta", numeroCuentaAsociada);
            item.set("moneda", numeroCuentaAsociada.startsWith("2") ? "2" : "80");
            item.set("principal", true);
            request.add("TarjetaDebitoCuentasOperativas", item);

            request.body("NumeroCtaComision", numeroCuentaAsociada); // emm: agrego esto que faltaba por si trae cuenta
        }
        return ApiMB.response(request, contexto.idCobis());
    }

    public static ApiResponseMB eliminarTarjetaDebito(ContextoMB contexto, String idSolicitud, String idProducto) {
        ApiRequestMB request = ApiMB.request("VentasEliminarTarjetaDebito", "ventas_windows", "DELETE", "/solicitudes/{SolicitudId}/tarjetaDebito/{ProductoId}", contexto);
        request.headers.put("X-Handle", idSolicitud);
        request.path("SolicitudId", idSolicitud);
        request.path("ProductoId", idProducto);
        return ApiMB.response(request, contexto.idCobis());
    }

    /* ========== PAQUETES ========== */
    public static ApiResponseMB generarPaquete(ContextoMB contexto, String idSolicitud, String codigoPaquete, String idCajaAhorroPesos, String idCajaAhorroDolares, String idCuentaCorriente, String idTarjetaDebito, String idTarjetaCredito) {
        Cuenta cajaAhorroPesos = contexto.cuenta(idCajaAhorroPesos, contexto.cajaAhorroTitularPesos());
        Cuenta cajaAhorroDolares = contexto.cuenta(idCajaAhorroDolares, contexto.cajaAhorroTitularDolares());
        Cuenta cuentaCorriente = contexto.cuenta(idCuentaCorriente, contexto.cuentaCorrienteTitular());
        TarjetaDebito tarjetaDebito = contexto.tarjetaDebito(idTarjetaDebito, contexto.tarjetaDebitoPorDefecto());
        TarjetaCredito tarjetaCredito = contexto.tarjetaCredito(idTarjetaCredito, contexto.tarjetaCreditoTitular());

        ApiRequestMB request = ApiMB.request("VentasGenerarPaquete", "ventas_windows", "POST", "/solicitudes/{SolicitudId}/solicitudPaquete", contexto);
        request.headers.put("X-Handle", idSolicitud);
        request.path("SolicitudId", idSolicitud);
        request.body("EjecutaMotor", true);
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
        paquete.set("EleccionDistribucion", "CLIENTE");
        paquete.set("ProductoCobisCobro", 4);
        paquete.set("ProductoBancarioCobro", 3);
        paquete.set("MonedaCobro", "80");
        paquete.set("DomicilioResumen").set("Tipo", "DP");
        paquete.add("Integrantes", integrante(contexto));

        Objeto productosExistentes = new Objeto();
        productosExistentes = cajaAhorroPesos != null ? productosExistentes.add(existenteCajaAhorroPesos(integrante(contexto), cajaAhorroPesos)) : productosExistentes;
        productosExistentes = cajaAhorroDolares != null ? productosExistentes.add(existenteCajaAhorroDolares(integrante(contexto), cajaAhorroDolares)) : productosExistentes;
        productosExistentes = cuentaCorriente != null ? productosExistentes.add(existenteCuentaCorriente(integrante(contexto), cuentaCorriente)) : productosExistentes;
        productosExistentes = tarjetaDebito != null ? productosExistentes.add(existenteTarjetaDebito(integrante(contexto), tarjetaDebito)) : productosExistentes;
        productosExistentes = tarjetaCredito != null ? productosExistentes.add(existenteTarjetaCredito(integrante(contexto), tarjetaCredito)) : productosExistentes;
        paquete.set("ProductosExistentes", productosExistentes);

        Objeto productosNuevos = new Objeto();
        productosNuevos = cajaAhorroPesos == null ? productosNuevos.set("CajaAhorro", nuevaCajaAhorroPesos(integrante(contexto))) : productosNuevos;
        productosNuevos = cajaAhorroDolares == null ? productosNuevos.set("CajaAhorroDolares", nuevaCajaAhorroDolares(integrante(contexto))) : productosNuevos;
        productosNuevos = cuentaCorriente == null ? productosNuevos.set("CuentaCorriente", nuevaCuentaCorriente(integrante(contexto))) : productosNuevos;
        productosNuevos = tarjetaDebito == null ? productosNuevos.set("TarjetaDebito", nuevaTarjetaDebito(integrante(contexto), cajaAhorroPesos != null ? cajaAhorroPesos.numero() : "0")) : productosNuevos;
        productosNuevos = tarjetaCredito == null ? productosNuevos.set("TarjetaCredito", nuevaTarjetaCredito(contexto, integrante(contexto))) : productosNuevos;
        paquete.set("ProductosNuevos", productosNuevos);

        return ApiMB.response(request, contexto.idCobis());
    }

    public static ApiResponseMB ofertaTarjetaCreditoPaquete(ContextoMB contexto, String idSolicitud) {
        ApiRequestMB request = ApiMB.request("VentasGenerarPaquete", "ventas_windows", "POST", "/solicitudes/{SolicitudId}/solicitudPaquete", contexto);
        request.headers.put("X-Handle", idSolicitud);
        request.path("SolicitudId", idSolicitud);
        request.body("EjecutaMotor", false);
        request.body("TipoOperacion", "02");
        request.body("Resolucion").set("FlagSolicitaExcepcion", false).set("MotivoExcepcion", "");
        request.body("Paquete", new Objeto());

        Objeto paquete = request.body.objeto("Paquete");
        paquete.set("TipoPaquete", 0);
        paquete.set("ProductoBancario", 0);
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
        paquete.add("Integrantes", integrante(contexto));

        Objeto productosNuevos = new Objeto();
        productosNuevos = productosNuevos.set("TarjetaCredito", nuevaTarjetaCredito(contexto, integrante(contexto)));
        paquete.set("ProductosNuevos", productosNuevos);

        return ApiMB.response(request, contexto.idCobis());
    }

    public static ApiResponseMB actualizarPaquete(ContextoMB contexto, String idSolicitud, String codigoPaquete, String idPaquete, String idCajaAhorroPesos, String idCajaAhorroDolares, String idCuentaCorriente, String idTarjetaDebito, String idTarjetaCreditoGenerada) {
        ApiResponseMB getTarjeta = consultarTarjetaCredito(contexto, idSolicitud, idTarjetaCreditoGenerada);

        Cuenta cajaAhorroPesos = contexto.cuenta(idCajaAhorroPesos, contexto.cajaAhorroTitularPesos());
        Cuenta cajaAhorroDolares = contexto.cuenta(idCajaAhorroDolares, contexto.cajaAhorroTitularDolares());
        Cuenta cuentaCorriente = contexto.cuenta(idCuentaCorriente, contexto.cuentaCorrienteTitular());
        TarjetaDebito tarjetaDebito = contexto.tarjetaDebito(idTarjetaDebito, contexto.tarjetaDebitoPorDefecto());

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
        paquete.set("EleccionDistribucion", "CLIENTE");
        paquete.set("ProductoCobisCobro", 4);
        paquete.set("ProductoBancarioCobro", 3);
        paquete.set("MonedaCobro", "80");
        paquete.set("DomicilioResumen").set("Tipo", "DP");
        paquete.add("Integrantes", integrante(contexto));

        Objeto productosExistentes = new Objeto();
        productosExistentes = cajaAhorroPesos != null ? productosExistentes.add(existenteCajaAhorroPesos(integrante(contexto), cajaAhorroPesos)) : productosExistentes;
        productosExistentes = cajaAhorroDolares != null ? productosExistentes.add(existenteCajaAhorroDolares(integrante(contexto), cajaAhorroDolares)) : productosExistentes;
        productosExistentes = cuentaCorriente != null ? productosExistentes.add(existenteCuentaCorriente(integrante(contexto), cuentaCorriente)) : productosExistentes;
        productosExistentes = tarjetaDebito != null ? productosExistentes.add(existenteTarjetaDebito(integrante(contexto), tarjetaDebito)) : productosExistentes;
        paquete.set("ProductosExistentes", productosExistentes);

        Objeto productosNuevos = new Objeto();
        productosNuevos = cajaAhorroPesos == null ? productosNuevos.set("CajaAhorro", nuevaCajaAhorroPesos(integrante(contexto))) : productosNuevos;
        productosNuevos = cajaAhorroDolares == null ? productosNuevos.set("CajaAhorroDolares", nuevaCajaAhorroDolares(integrante(contexto))) : productosNuevos;
        productosNuevos = cuentaCorriente == null ? productosNuevos.set("CuentaCorriente", nuevaCuentaCorriente(integrante(contexto))) : productosNuevos;
        productosNuevos = tarjetaDebito == null ? productosNuevos.set("TarjetaDebito", nuevaTarjetaDebito(integrante(contexto), cajaAhorroPesos != null ? cajaAhorroPesos.numero() : "0")) : productosNuevos;
        paquete.set("ProductosNuevos", productosNuevos);

        // http://api-paquetes-microservicios-desa.appd.bh.com.ar/v1/infoParametrias/productos/existentes?idCliente=417292&numeroPaquete=34

        Objeto item = new Objeto();
        for (Objeto datos : getTarjeta.objetos("Datos")) {
            item.set("Embozado", ContextoMB.embozado(contexto.persona().nombres(), contexto.persona().apellidos()));
            item.set("Caracteristica", "02");
            item.set("Domicilio").set("Tipo", "DP");
            item.set("Telefono", "E");
            item.set("FormaPago", 5);
            item.set("TipoCuenta", "4");
            item.set("EmpresaAseguradora", "40");
            item.set("NumeroCuenta", cajaAhorroPesos != null ? cajaAhorroPesos.numero() : "0");
            item.set("SucursalCuenta", cajaAhorroPesos != null ? cajaAhorroPesos.sucursal() : "0"); // ver que pasa cuando no tengo CA

//			item.set("tipoProducto", datos.string("Letra"));
            item.set("IdProductoFrontEnd", datos.integer("IdProductoFrontEnd"));
            item.set("Producto", datos.string("Producto"));
            item.set("CarteraGrupo", datos.string("CarteraGrupo"));
            item.set("Afinidad", datos.string("Afinidad"));
            item.set("ModeloLiquidacion", datos.string("ModeloLiquidacion"));
            item.set("Distribucion", datos.string("Distribucion"));
            item.set("Limite", datos.string("Limite"));
            item.set("AvisosViaMail", datos.string("AvisosViaMail"));
            item.set("AvisosCorreoTradicional", datos.string("AvisosCorreoTradicional"));
            item.set("Letra", datos.string("Letra"));
            item.add("Integrantes", integrante(contexto));
            item.set("MailAvisos").set("Tipo", "EMP");
        }
        productosNuevos.set("TarjetaCredito", item);

        return ApiMB.response(request, contexto.idCobis());
    }

    static Objeto existenteCajaAhorroPesos(Objeto integrante, Cuenta cajaAhorroPesos) {
        Objeto item = new Objeto();
        item.set("IdProducto", 8);
        item.set("NumeroProducto", cajaAhorroPesos.numero());
        item.set("Integrante", integrante);
        return item;
    }

    static Objeto existenteCajaAhorroDolares(Objeto integrante, Cuenta cajaAhorroDolares) {
        Objeto item = new Objeto();
        item.set("IdProducto", 9);
        item.set("NumeroProducto", cajaAhorroDolares.numero());
        item.set("Integrante", integrante);
        return item;
    }

    static Objeto existenteCuentaCorriente(Objeto integrante, Cuenta cuentaCorriente) {
        Objeto item = new Objeto();
        item.set("IdProducto", 7);
        item.set("NumeroProducto", cuentaCorriente.numero());
        item.set("Integrante", integrante);
        return item;
    }

    static Objeto existenteTarjetaDebito(Objeto integrante, TarjetaDebito tarjetaDebito) {
        Objeto item = new Objeto();
        item.set("IdProducto", 11);
        item.set("NumeroProducto", tarjetaDebito.numero());
        item.set("Integrante", integrante);
        return item;
    }

    static Objeto existenteTarjetaCredito(Objeto integrante, TarjetaCredito tarjetaCredito) {
        Objeto item = new Objeto();
        item.set("IdProducto", 5);
        item.set("NumeroProducto", tarjetaCredito.numero());
        item.set("Marca", "2");
        item.set("Afinidad", tarjetaCredito.grupoAfinidad());
        item.set("Letra", tarjetaCredito.idTipo());
        item.set("Oficina", tarjetaCredito.sucursal());
        item.set("Integrante", integrante);
        return item;
    }

    static Objeto nuevaCajaAhorroPesos(Objeto integrante) {
        Objeto item = new Objeto();
        item.set("Moneda", "80");
        item.set("Categoria", "MOV");
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
        return item;
    }

    static Objeto nuevaCajaAhorroDolares(Objeto integrante) {
        Objeto item = new Objeto();
        item.set("Moneda", "2");
        item.set("Categoria", "MOV");
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
        return item;
    }

    static Objeto nuevaCuentaCorriente(Objeto integrante) {
        Objeto item = new Objeto();
        item.set("ProductoBancario", 1);
        item.set("Categoria", "D");
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
        return item;
    }

    static Objeto nuevaTarjetaDebito(Objeto integrante, String numeroCuentaCobro) {
        Objeto item = new Objeto();
        item.set("Tipo", "NV");
        item.set("Domicilio").set("Tipo", "DP");
        item.set("Grupo", 3);
        item.set("TipoCuentaComision", "4");
        item.set("NumeroCtaComision", numeroCuentaCobro);
        item.add("TarjetaDebitoCuentasOperativas", new Objeto().set("Producto", 4).set("Cuenta", 0).set("Moneda", 80).set("Principal", true));
        item.add("Integrantes", integrante);
        return item;
    }

    static Objeto nuevaTarjetaCredito(ContextoMB contexto, Objeto integrante) {
        String embozado = (contexto.persona().apellidos() + "/" + contexto.persona().nombres()).toUpperCase();
        embozado = embozado.length() > 19 ? embozado.substring(0, 19) : embozado;
//		item.set("Embozado", embozado);

        Objeto item = new Objeto();
//		item.set("Embozado", embozado);
//		item.set("Producto", "05");
//		item.set("IdProductoFrontEnd", "05");
//		item.set("AvisosViaMail", true);
//		item.set("AvisosCorreoTradicional", false);
//		item.set("EmpresaAseguradora", "40");
//		item.set("MailAvisos").set("Tipo", "EMP");

//		item.set("OrigenCaptacion", "");
//		item.set("Oficina", "");
//		item.set("Oficial", "");
//		item.set("UsoFirma", "");
//		item.set("Ciclo", "");
//		item.set("EleccionDistribucion", "");
//		item.set("ProductoCobisCobro", "");
//		item.set("ProductoBancarioCobro", "");
//		item.add("Integrantes", integrante);
//		item.set("DomicilioResumen", "");

        item.add("Integrantes", integrante);
        item.set("TipoOperacion", "02");
        return item;
    }

    /* ========== PRESTAMOS ========== */
    public static ApiResponseMB consultarSolicitudPrestamoPersonal(ContextoMB contexto, String idSolicitud, String idProducto) {
        ApiRequestMB request = ApiMB.request("VentasConsultarSolicitudPrestamoPersonal", "ventas_windows", "GET", "/solicitudes/{SolicitudId}/prestamoPersonal/{ProductoId}", contexto);
        request.headers.put("X-Handle", idSolicitud);
        request.path("SolicitudId", idSolicitud);
        request.path("ProductoId", idProducto);
        return ApiMB.response(request, contexto.idCobis());
    }

    /* ========== PRESTAMOS ========== */
    public static ApiResponseMB consultarSolicitudAdelanto(ContextoMB contexto, String idSolicitud, String cuentaCorrienteId) {
        ApiRequestMB request = ApiMB.request("VentasConsultarSolicitudAdelantoBH", "ventas_windows", "GET", "/solicitudes/{SolicitudId}/cuentaCorriente/{cuentaCorrienteId}", contexto);
        request.headers.put("X-Handle", idSolicitud);
        request.path("SolicitudId", idSolicitud);
        request.path("cuentaCorrienteId", cuentaCorrienteId);
        return ApiMB.response(request, contexto.idCobis());
    }

    public static ApiResponseMB agregarPrestamoPersonal(ContextoMB contexto, String idSolicitud) {
        TarjetaDebito tarjetaDebito = contexto.tarjetaDebitoPorDefecto();
        Objeto integrante = new Objeto();
        integrante.set("numeroDocumentoTributario", contexto.persona().cuit());
        integrante.set("idCobis", Long.valueOf(contexto.idCobis()));
        integrante.set("numeroTarjetaDebito", tarjetaDebito != null ? tarjetaDebito.numero() : null);
        integrante.set("rol", "D");
        ApiRequestMB request = ApiMB.request("VentasAgregarPrestamoPersonal", "ventas_windows", "POST", "/solicitudes/{SolicitudId}/prestamoPersonal", contexto);
        request.headers.put("X-Handle", idSolicitud);
        request.path("SolicitudId", idSolicitud);
        request.body("Amortizacion", "01");
        request.body("TipoTasa", "01");
        request.body("DestinoBien", ConfigMB.esDesarrollo() ? "143" : "20");
        request.body("DescripcionDestinoFondos", "Libre destino");
        request.add("Integrantes", integrante);
        request.body("Moneda", "80");
        // request.body("SubProducto", "21");

        if (contexto.parametros.bigDecimal("montoCuotificacion") != null) {
            BigDecimal montoCuotificacion = contexto.parametros.bigDecimal("montoCuotificacion");
            request.body("MontoSolicitado", montoCuotificacion);
            request.body("SubProducto", ConfigMB.esDesarrollo() ? "44" : "31");
        }

        request.body("TipoOperacion", "02"); // Se manda 02 para que no haga todas las validaciones
        return ApiMB.response(request, contexto.idCobis());
    }

    public static ApiResponseMB modificarSolicitudPrestamoPersonal(ContextoMB contexto, String idSolicitud, String idProducto, BigDecimal montoSolicitado, Integer plazoSolicitado, Integer diaCobro, String idCuenta, String tipoOperacion // 02: no tiene en cuenta varios errores (ejemplo: que no este la cuenta),
    ) {
        TarjetaDebito tarjetaDebito = contexto.tarjetaDebitoPorDefecto();
        Boolean esAdelanto = contexto.parametros.bool("esAdelanto", false);
        Boolean esCuotificacion = contexto.parametros.bool("esCuotificacion", false);

        Objeto integrante = new Objeto();
        integrante.set("numeroDocumentoTributario", contexto.persona().cuit());
        integrante.set("idCobis", Long.valueOf(contexto.idCobis()));
        integrante.set("numeroTarjetaDebito", tarjetaDebito != null ? tarjetaDebito.numero() : null);
        integrante.set("rol", "D");

        ApiRequestMB request = ApiMB.request("VentasModificarPrestamoPersonal", "ventas_windows", "PUT", "/solicitudes/{SolicitudId}/prestamoPersonal/{ProductoId}", contexto);
        request.headers.put("X-Handle", idSolicitud);
        request.path("SolicitudId", idSolicitud);
        request.path("ProductoId", idProducto);
        request.body("Amortizacion", "01");
        request.body("TipoTasa", "01");
        request.body("DestinoBien", ConfigMB.esDesarrollo() ? "143" : "20");
        request.body("DescripcionDestinoFondos", "Libre destino");
        request.body("MontoSolicitado", montoSolicitado);
        if (esAdelanto) {
            request.body("Subproducto", ConfigMB.esDesarrollo() ? "46" : "33");
        }

        if (esCuotificacion)
            request.body("Subproducto", "31");

        request.body("PlazoSolicitado", esAdelanto ? "1" : plazoSolicitado);
        // request.body("CuotaSolicitada", null);
        request.body("mercado", "01");
        request.body("FormaCobroTipo", "NDMNCA");
        request.body("FormaCobroCuenta", idCuenta);
        Objeto domicilio = new Objeto();
        domicilio.set("Tipo", "DP");
        request.body("Domicilio", domicilio);
        request.body("FechaCobroFija", true);
        request.body("DiaCobro", diaCobro); // pasar por parametro
        request.body("EmpresaAseguradora", "40");

        // DESEMBOLSOS
        Objeto desembolsos = new Objeto();
        desembolsos.set("NroDesembolso", 1);
        desembolsos.set("Capital", montoSolicitado);
        Objeto formasDesembolso = new Objeto();
        formasDesembolso.set("NroDesembolso", 1);
        formasDesembolso.set("Forma", "NCMNCA");
        formasDesembolso.set("Referencia", idCuenta);
        String nombreCompleto = contexto.persona().nombreCompleto();
        formasDesembolso.set("Beneficiario", nombreCompleto.substring(0, nombreCompleto.length() < 40 ? nombreCompleto.length() - 1 : 39));
        formasDesembolso.set("Valor", montoSolicitado); // pasar por parametro
        desembolsos.add("FormasDesembolso", formasDesembolso);
        request.body("Desembolsos", desembolsos);

        // MAIL AVISOS
        Objeto mailAvisos = new Objeto();
        mailAvisos.set("Tipo", "EMP");
        request.body("MailAvisos", mailAvisos);

        // INTEGRANTES
        request.add("Integrantes", integrante);

        request.body("TipoOperacion", tipoOperacion);

        return ApiMB.response(request, contexto.idCobis());
    }

}
