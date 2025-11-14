package ar.com.hipotecario.mobile.negocio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import ar.com.hipotecario.backend.servicio.api.transmit.JourneyTransmitEnum;
import com.google.gson.Gson;

import ar.com.hipotecario.mobile.ConfigMB;
import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.RespuestaMB;
import ar.com.hipotecario.mobile.api.MBCatalogo;
import ar.com.hipotecario.mobile.conector.ApiMB;
import ar.com.hipotecario.mobile.conector.ApiRequestMB;
import ar.com.hipotecario.mobile.conector.ApiResponseMB;
import ar.com.hipotecario.mobile.conector.SqlMB;
import ar.com.hipotecario.mobile.conector.SqlRequestMB;
import ar.com.hipotecario.mobile.excepcion.ApiVentaExceptionMB;
import ar.com.hipotecario.mobile.lib.Fecha;
import ar.com.hipotecario.mobile.lib.Objeto;
import ar.com.hipotecario.mobile.lib.Texto;
import ar.com.hipotecario.mobile.negocio.SolicitudPrestamo.FormasDesembolso;
import ar.com.hipotecario.mobile.servicio.RestVenta;

public class Solicitud {

    /* ========== ATRIBUTOS ========== */
    public String Id;
    public String IdSolicitud;
    public String TipoOperacion;
    public String Oficina;
    public String CanalOriginacion1;
    public String CanalOriginacion2;
    public String CanalOriginacion3;
    public CanalOriginacion CanalOriginacionNivel3;
    public String CanalVenta1;
    public String CanalVenta2;
    public String CanalVenta3;
    public String CanalVenta4;
    public CanalVenta CanalVentaNivel4;
    public Object Observaciones;
    public String Estado;
    public String FechaAlta;
    public Object Advertencias;
    public List<SolicitudProducto> Productos;
    public Object FlagSimulacion;
    public Object Simulacion;
    public Object Formularios;
    public Boolean PermiteImpresionDDJJAnses;
    public String CanalVenta;
    public String OficialVenta;
    public List<Integrante> Integrantes;
    public Boolean Finalizada;
    public Object OrigenDocumentacion;
    public String ResolucionCodigo;
    public String DerivarA;

    public static class CanalOriginacion {
        public String Id;
        public String Nombre;
        public String Apellido;
        public String Nivel2;
        public String IdDocbis;
        public Object Advertencias;
    }

    public static class CanalVenta {
        public String Id;
        public String Nombre;
        public String Apellido;
        public String Empresa;
        public String Funcionario;
        public String Estado;
        public Object CBU;
        public String Nivel3;
        public Object Advertencias;
    }

    public static class SolicitudProducto {
        public String Id;
        public String tipoProducto;
        public String IdProductoFrontEnd;
        public String TipoOperacion;
        public String Oficina;
        public String Producto;
        public Boolean Validado;
        public String Oficial;
        public Object Moneda;
        public List<Object> Integrantes;
        public Object Advertencias;
        public Boolean RechazadoMotor;
        public String IdPaqueteProductos;
        public Object MontoAprobado;
        public Object ModoAprobacionId;
        public Object ConSeguroVida;
        public Object IdProductoPadre;
        public Boolean EsMicrocredito;
        public String Nemonico;
        public Integer NroDesembolsosPropuestos;
        public Integer NroUltimoDesembolsoLiquidado;
        public List<DesembolsoPropuesto> DesembolsosPropuestos;
    }

    public static class DesembolsoPropuesto {
        public String Id;
        public Integer NroDesembolso;
        public String IdPrestamo;
        public BigDecimal Monto;
    }

    public static final String cuitAnses = "33637617449";

    /* ========== OBTENER SOLICITUDES EXISTENTES ========== */
    public static Solicitud solicitud(ContextoMB contexto, String idSolicitud) {
        ApiRequestMB request = ApiMB.request("VentasConsultarSolicitud", "ventas_windows", "GET", "/solicitudes/{idSolicitud}", contexto);
        request.headers.put("X-Handle", idSolicitud);
        request.path("idSolicitud", idSolicitud);

        ApiResponseMB response = ApiMB.response(request, idSolicitud);
        logOriginacion(contexto, idSolicitud, "obtenerSolicitud", response, "");
        if (response.hayError() || !response.objetos("Errores").isEmpty()) {
            throw new ApiVentaExceptionMB(response);
        }

        Objeto datos = response.objetos("Datos").get(0);
        Solicitud solicitud = (new Gson()).fromJson(datos.toJson(), Solicitud.class);
        return solicitud;
    }

    public static List<Solicitud> solicitudes(ContextoMB contexto, String cuit, String estado) {
        ApiRequestMB request = ApiMB.request("VentasConsultarSolicitudes", "ventas_windows", "GET", "/solicitudes", contexto);
        request.query("cuil", cuit);

        if (!"segundodesembolso".equals(estado)) {
            request.query("fechadesde", Fecha.restarDias(new Date(), 90L, "yyyyMMdd"));
        }
        if (estado != null) {
            request.query("estado", estado);
        }

        ApiResponseMB response = ApiMB.response(request, cuit);
        if (response.hayError() || !response.objetos("Errores").isEmpty()) {
            throw new ApiVentaExceptionMB(response);
        }

        List<Solicitud> solicitudes = new ArrayList<>();
        for (Objeto datos : response.objetos("Datos")) {
            Solicitud solicitud = (new Gson()).fromJson(datos.toJson(), Solicitud.class);
            solicitudes.add(solicitud);
        }
        return solicitudes;
    }

    public static Solicitud solicitudProcrearRefaccion(ContextoMB contexto, String cuit) {
        List<Solicitud> solicitudes = solicitudes(contexto, cuit, null);
        for (Solicitud solicitud : solicitudes) {
            if (solicitud.contienePrestamo()) {
                SolicitudPrestamo prestamo = solicitud.prestamo(contexto);
                if (prestamo.esProcrearRefaccion()) {
                    return solicitud;
                }
            }
        }
        return null;
    }

    public static Solicitud solicitudSegundoDesembolsoProcrearRefaccion(ContextoMB contexto, String cuit) {
        List<Solicitud> solicitudes = solicitudes(contexto, cuit, "segundodesembolso");
        for (Solicitud solicitud : solicitudes) {
            if (solicitud.contienePrestamo()) {
                SolicitudPrestamo prestamo = solicitud.prestamo(contexto);
                if (prestamo.esProcrearRefaccion()) {
                    return solicitud;
                }
            }
        }
        return null;
    }

    public static Solicitud solicitudSegundoDesembolsoPrestamoHipotecario(ContextoMB contexto, String cuit, String numeroDesembolso) {
        // List<Solicitud> solicitudes = solicitudes(contexto, cuit,
        // "segundodesembolso", numero);

        ApiRequestMB request = ApiMB.request("VentasSolicitudesSegundoDesembolsoHipotecario", "ventas_windows", "GET", "/solicitudes", contexto);
        request.query("cuil", cuit);
        request.query("estado", "desembolso");
        if (!numeroDesembolso.equals("")) {
            request.query("nro", numeroDesembolso);
        }

        ApiResponseMB response = ApiMB.response(request, cuit);
        if (response.hayError() || !response.objetos("Errores").isEmpty()) {
            throw new ApiVentaExceptionMB(response);
        }

        List<Solicitud> solicitudes = new ArrayList<>();
        for (Objeto datos : response.objetos("Datos")) {
            Solicitud solicitud = (new Gson()).fromJson(datos.toJson(), Solicitud.class);
            solicitudes.add(solicitud);
        }
        /*
         * for (Solicitud solicitud : solicitudes) { if
         * (solicitud.contienePrestamoHipotecario()) { SolicitudPrestamo prestamo =
         * solicitud.prestamo(contexto); if (prestamo.esProcrearRefaccion()) { return
         * solicitud; } } }
         */

        if (solicitudes != null && solicitudes.size() > 0) {
            return solicitudes.get(0);
        }
        return null;
    }

    /* ========== ID PRODUCTOS ========== */
    public String idProducto(String idProducto) {
        if (this.Productos != null) {
            for (SolicitudProducto producto : this.Productos) {
                if (idProducto.equals(producto.IdProductoFrontEnd)) {
                    return producto.Id;
                }
            }
        }
        return null;
    }

    public Boolean tipoNemonico(String nemonico) {
        if (this.Productos != null) {
            for (SolicitudProducto producto : this.Productos) {
                return nemonico.equalsIgnoreCase(producto.Nemonico);
            }
        }
        return false;
    }

    public Boolean esAdelanto() {
        return tipoNemonico("PPADELANTO");
    }

    public Boolean esPrestamoPersonal() {
        return tipoNemonico("PERSP");
    }

    public String idCajaAhorroPesos() {
        return idProducto("8");
    }

    public String idTarjetaDebito() {
        return idProducto("11");
    }

    public String idPrestamo() {
        return idProducto("2");
    }

    /* ========== PRODUCTOS CONTENIDOS EN LA SOLICITUD ========== */
    public Boolean contieneCajaAhorroPesos() {
        return idProducto("8") != null;
    }

    public Boolean contieneTarjetaDebito() {
        return idProducto("11") != null;
    }

    public Boolean contienePrestamo() {
        return idProducto("2") != null;
    }

    public Boolean contienePrestamoHipotecario() {
        return idProducto("1") != null;
    }

    /* ========== DETALLES DE PRODUCTOS ========== */
//	public SolicitudCajaAhorro cajaAhorroPesos(Contexto contexto) {
//		if (contieneCajaAhorroPesos()) {
//			ApiRequest request = Api.request("VentasConsultarCajaAhorroPesos", "ventas_windows", "GET", "/solicitudes/{idSolicitud}/cajaAhorro/{idProducto}", contexto);
//			request.headers.put("X-Handle", this.IdSolicitud);
//			request.path("idSolicitud", this.IdSolicitud);
//			request.path("idProducto", idCajaAhorroPesos());
//
//			ApiResponse response = Api.response(request, contexto.idCobis());
//			if (response.hayError() || !response.objetos("Errores").isEmpty()) {
//				throw new ApiVentaException(response);
//			}
//
//			Objeto datos = response.objetos("Datos").get(0);
//			SolicitudCajaAhorro cajaAhorroPesos = (new Gson()).fromJson(datos.toJson(), SolicitudCajaAhorro.class);
//			return cajaAhorroPesos;
//		}
//		return null;
//	}

//	public SolicitudTarjetaDebito tarjetaDebito(Contexto contexto) {
//		if (contieneTarjetaDebito()) {
//			ApiRequest request = Api.request("VentasConsultarTarjetaDebito", "ventas_windows", "GET", "/solicitudes/{idSolicitud}/tarjetaDebito/{idProducto}", contexto);
//			request.headers.put("X-Handle", this.IdSolicitud);
//			request.path("idSolicitud", this.IdSolicitud);
//			request.path("idProducto", idTarjetaDebito());
//
//			ApiResponse response = Api.response(request, contexto.idCobis());
//			if (response.hayError() || !response.objetos("Errores").isEmpty()) {
//				throw new ApiVentaException(response);
//			}
//
//			Objeto datos = response.objetos("Datos").get(0);
//			SolicitudTarjetaDebito tarjetaDebito = (new Gson()).fromJson(datos.toJson(), SolicitudTarjetaDebito.class);
//			return tarjetaDebito;
//		}
//		return null;
//	}

    public SolicitudPrestamo prestamo(ContextoMB contexto) {
        if (contienePrestamo()) {
            ApiRequestMB request = ApiMB.request("VentasConsultarPrestamo", "ventas_windows", "GET", "/solicitudes/{idSolicitud}/prestamoPersonal/{idProducto}", contexto);
            request.headers.put("X-Handle", this.IdSolicitud);
            request.path("idSolicitud", this.IdSolicitud);
            request.path("idProducto", idPrestamo());

            ApiResponseMB response = ApiMB.response(request, contexto.idCobis());
            if (response.hayError() || !response.objetos("Errores").isEmpty()) {
                throw new ApiVentaExceptionMB(response);
            }

            Objeto datos = response.objetos("Datos").get(0);
            SolicitudPrestamo prestamo = (new Gson()).fromJson(datos.toJson(), SolicitudPrestamo.class);
            return prestamo;
        }
        return null;
    }

    /* ========== GENERAR SOLICITUD ========== */
    public static Solicitud generarSolicitud(ContextoMB contexto) {
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

        ApiResponseMB response = ApiMB.response(request, contexto.idCobis());
        if (response.hayError() || !response.objetos("Errores").isEmpty()) {
            logApiVentas(contexto, "-", "generarSolicitud", response);
            throw new ApiVentaExceptionMB(response);
        }
        logApiVentas(contexto, response.objetos("Datos").get(0).string("IdSolicitud"), "generarSolicitud", response);

        Objeto datos = response.objetos("Datos").get(0);
        Solicitud solicitud = (new Gson()).fromJson(datos.toJson(), Solicitud.class);
        return solicitud;
    }

    /* ========== GENERAR INTEGRANTE ========== */
    public Solicitud generarIntegrantes(ContextoMB contexto, String... cuits) {
        for (String cuit : cuits) {
            if (cuit != null) {
                ApiRequestMB request = ApiMB.request("VentasGenerarIntegrante", "ventas_windows", "POST", "/solicitudes/{SolicitudId}/integrantes", contexto);
                request.headers.put("X-Handle", this.IdSolicitud);
                request.path("SolicitudId", this.IdSolicitud);
                request.body("NumeroTributario", Long.valueOf(cuit));
                request.body("TipoOperacion", "03");

                ApiResponseMB response = ApiMB.response(request, contexto.idCobis());
                logApiVentas(contexto, this.IdSolicitud, "generarIntegrantes", response);
                if (response.hayError() || !response.objetos("Errores").isEmpty()) {
                    throw new ApiVentaExceptionMB(response);
                }
            }
        }
        Solicitud solicitud = solicitud(contexto, this.IdSolicitud);
        return solicitud;
    }

    /* ========== GENERAR CAJA AHORRO ========== */
    public Solicitud generarCajaAhorro(ContextoMB contexto, String idMoneda, String categoria, String... cuitsIntegrantes) {
        ApiRequestMB request = ApiMB.request("VentasGenerarCajaAhorro", "ventas_windows", "POST", "/solicitudes/{SolicitudId}/cajaAhorro", contexto);
        request.headers.put("X-Handle", this.IdSolicitud);
        request.path("SolicitudId", this.IdSolicitud);
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
        for (Integer i = 0; i < cuitsIntegrantes.length; ++i) {
            if (cuitsIntegrantes[i] != null) {
                Objeto integrante = new Objeto();
                integrante.set("numeroDocumentoTributario", cuitsIntegrantes[i]);
                integrante.set("rol", i == 0 ? "T" : "A");
                request.add("Integrantes", integrante);
            }
        }
        request.body("CuentaLegales").set("Uso", "PER").set("RealizaTransferencias", false);
        request.body("TipoOperacion", "03");

        ApiResponseMB response = ApiMB.response(request, contexto.idCobis());
        logApiVentas(contexto, this.IdSolicitud, "generarCajaAhorro", response);
        if (response.hayError() || !response.objetos("Errores").isEmpty()) {
            throw new ApiVentaExceptionMB(response);
        }

        Solicitud solicitud = solicitud(contexto, this.IdSolicitud);
        return solicitud;
    }

    /* ========== GENERAR TARJETA DEBITO ========== */
    public Solicitud generarTarjetaDebito(ContextoMB contexto, String tipo, Integer grupo, String... cuitsIntegrantes) {
        ApiRequestMB request = ApiMB.request("VentasGenerarTarjetaDebito", "ventas_windows", "POST", "/solicitudes/{SolicitudId}/tarjetaDebito", contexto);
        request.headers.put("X-Handle", this.IdSolicitud);
        request.path("SolicitudId", this.IdSolicitud);
        request.body("Tipo", tipo);
        request.body("Domicilio").set("Tipo", "DP");
        request.body("Grupo", grupo);
        request.body("TipoCuentaComision", "4");
        request.body("NumeroCtaComision", "0");
        request.body("Moneda", "80");
        request.add("TarjetaDebitoCuentasOperativas", new Objeto().set("Producto", 4).set("Cuenta", 0).set("Moneda", "80").set("Principal", true));
        for (Integer i = 0; i < cuitsIntegrantes.length; ++i) {
            if (cuitsIntegrantes[i] != null) {
                Objeto integrante = new Objeto();
                integrante.set("numeroDocumentoTributario", cuitsIntegrantes[i]);
                integrante.set("rol", i == 0 ? "T" : "A");
                request.add("Integrantes", integrante);
            }
        }
        request.body("TipoOperacion", "03");

        ApiResponseMB response = ApiMB.response(request, contexto.idCobis());
        logApiVentas(contexto, this.IdSolicitud, "generarTarjetaDebito", response);
        if (response.hayError() || !response.objetos("Errores").isEmpty()) {
            throw new ApiVentaExceptionMB(response);
        }

        Solicitud solicitud = solicitud(contexto, this.IdSolicitud);
        return solicitud;
    }

    /* ========== GENERAR PRESTAMO ========== */
    public Solicitud generarPrestamoProcrearRefaccion(ContextoMB contexto, Integer valorOferta, BigDecimal montoSolicitado, Integer plazo, String... cuitsIntegrantes) {
        String subProducto = "";
        /*
         * subProducto = valorOferta.equals(100000) ? (Config.esDesarrollo() ? "36" :
         * "23") : subProducto; subProducto = valorOferta.equals(250000) ?
         * (Config.esDesarrollo() ? "37" : "24") : subProducto; subProducto =
         * valorOferta.equals(500000) ? (Config.esDesarrollo() ? "38" : "25") :
         * subProducto;
         */
        subProducto = valorOferta.equals(100000) ? (ConfigMB.esDesarrollo() ? "41" : "28") : subProducto;
        subProducto = valorOferta.equals(240000) ? (ConfigMB.esDesarrollo() ? "42" : "29") : subProducto;
        subProducto = valorOferta.equals(500000) ? (ConfigMB.esDesarrollo() ? "38" : "25") : subProducto;
        if (subProducto.isEmpty()) {
            throw new RuntimeException();
        }

        ApiRequestMB request = ApiMB.request("VentasGenerarPrestamoProcrearRefaccion", "ventas_windows", "POST", "/solicitudes/{SolicitudId}/prestamoPersonal", contexto);
        request.headers.put("X-Handle", this.IdSolicitud);
        request.path("SolicitudId", this.IdSolicitud);
        request.body(new Objeto());

        Objeto datos = request.body();
        datos.set("TipoOperacion", "02");
        datos.set("SubProducto", subProducto);
        datos.set("MontoSolicitado", montoSolicitado);
        datos.set("Amortizacion", "01");
        datos.set("TipoTasa", "01");
        datos.set("DestinoBien", ConfigMB.esDesarrollo() ? 148 : 25);
        /*
         * if(subProducto.equals("28")) { datos.set("DestinoBien", Config.esDesarrollo()
         * ? 148 : 26); }else { datos.set("DestinoBien", Config.esDesarrollo() ? 148 :
         * 27); }
         */
        datos.set("DestinoBienVivienda", "01");
        datos.set("DescripcionDestinoFondos", "Mejoramiento Materiales");
        datos.set("Mercado", "01");
        datos.set("destinoVivienda", "0");
        datos.set("destino", "042");
        datos.set("Domicilio", new Objeto().set("Tipo", "DP"));
        if (plazo != null) {
            datos.set("PlazoSolicitado", plazo);
        }

        for (Integer i = 0; i < cuitsIntegrantes.length; ++i) {
            if (cuitsIntegrantes[i] != null) {
                Objeto integrante = new Objeto();
                integrante.set("numeroDocumentoTributario", cuitsIntegrantes[i]);
                integrante.set("rol", i == 0 ? "D" : "C");
                request.add("Integrantes", integrante);
            }
        }

        ApiResponseMB response = ApiMB.response(request, contexto.idCobis());
        logApiVentas(contexto, this.IdSolicitud, "generarPrestamoProcrearRefaccion", response);
        if (response.hayError() || !response.objetos("Errores").isEmpty()) {
            throw new ApiVentaExceptionMB(response);
        }

        Solicitud solicitud = solicitud(contexto, this.IdSolicitud);
        return solicitud;
    }

    public SolicitudPrestamo consultarPrestamoPersonal(ContextoMB contexto, String idProducto) {
        ApiRequestMB request = ApiMB.request("VentasConsultarSolicitudPrestamoPersonal", "ventas_windows", "GET", "/solicitudes/{SolicitudId}/prestamoPersonal/{ProductoId}", contexto);
        request.headers.put("X-Handle", this.IdSolicitud);
        request.path("SolicitudId", this.IdSolicitud);
        request.path("ProductoId", idProducto);
        ApiResponseMB response = ApiMB.response(request, contexto.idCobis());
        logApiVentas(contexto, this.IdSolicitud, "consultaPrestamoPersonal", response);
        if (response.hayError() || !response.objetos("Errores").isEmpty()) {
            // ApiOmnicanalidad.insertarLogApiVentas(contexto, this.IdSolicitud,
            // "VentasConsultaPrestamo",
            // response.objetos("Errores").get(0).string("MensajeCliente"),
            // response.objetos("Errores").get(0).string("MensajeDesarrollador"),
            // response.objetos("Errores").get(0).string("Codigo"));
            logApiVentas(contexto, IdSolicitud, "VentasConsultaPrestamo", response);
            throw new ApiVentaExceptionMB(response);
        }

        Objeto datos = response.objetos("Datos").get(0);
        SolicitudPrestamo solicitudPrestamo = (new Gson()).fromJson(datos.toJson(), SolicitudPrestamo.class);
        return solicitudPrestamo;
    }

    public Solicitud actualizarPrestamoProcrearRefaccion(ContextoMB contexto, Integer valorOferta, BigDecimal montoSolicitado, Integer plazo, String... cuitsIntegrantes) {
        String subProducto = "";
        /*
         * subProducto = valorOferta.equals(100000) ? (Config.esDesarrollo() ? "36" :
         * "23") : subProducto; subProducto = valorOferta.equals(250000) ?
         * (Config.esDesarrollo() ? "37" : "24") : subProducto; subProducto =
         * valorOferta.equals(500000) ? (Config.esDesarrollo() ? "38" : "25") :
         * subProducto;
         */
        subProducto = valorOferta.equals(100000) ? (ConfigMB.esDesarrollo() ? "41" : "28") : subProducto;
        subProducto = valorOferta.equals(240000) ? (ConfigMB.esDesarrollo() ? "42" : "29") : subProducto;
        subProducto = valorOferta.equals(500000) ? (ConfigMB.esDesarrollo() ? "38" : "25") : subProducto;
        if (subProducto.isEmpty()) {
            throw new RuntimeException();
        }

        ApiRequestMB request = ApiMB.request("VentasActualizarPrestamoProcrearRefaccion", "ventas_windows", "PUT", "/solicitudes/{SolicitudId}/prestamoPersonal/{PrestamoId}", contexto);
        request.headers.put("X-Handle", this.IdSolicitud);
        request.path("SolicitudId", this.IdSolicitud);
        request.path("PrestamoId", idPrestamo());
        request.body(new Objeto());

        Objeto datos = request.body();
        datos.set("TipoOperacion", "02");
        datos.set("SubProducto", subProducto);
        datos.set("MontoSolicitado", montoSolicitado);
        datos.set("Amortizacion", "01");
        datos.set("TipoTasa", "01");
        datos.set("DestinoBien", ConfigMB.esDesarrollo() ? 148 : 25);
        /*
         * if(subProducto.equals("28")) { datos.set("DestinoBien", Config.esDesarrollo()
         * ? 148 : 26); }else { datos.set("DestinoBien", Config.esDesarrollo() ? 148 :
         * 27); }
         */
        datos.set("DestinoBienVivienda", "01");
        datos.set("DescripcionDestinoFondos", "Mejoramiento Materiales");
        datos.set("Mercado", "01");
        datos.set("destinoVivienda", "0");
        datos.set("destino", "042");
        datos.set("Domicilio", new Objeto().set("Tipo", "DP"));
        if (plazo != null) {
            datos.set("PlazoSolicitado", plazo);
        }
        for (Integer i = 0; i < cuitsIntegrantes.length; ++i) {
            if (cuitsIntegrantes[i] != null) {
                Objeto integrante = new Objeto();
                integrante.set("numeroDocumentoTributario", cuitsIntegrantes[i]);
                integrante.set("rol", i == 0 ? "D" : "C");
                request.add("Integrantes", integrante);
            }
        }

        ApiResponseMB response = ApiMB.response(request, contexto.idCobis());
        logApiVentas(contexto, this.IdSolicitud, "actualizarPrestamoProcrearRefaccion", response);
        if (response.hayError() || !response.objetos("Errores").isEmpty()) {
            throw new ApiVentaExceptionMB(response);
        }

        Solicitud solicitud = solicitud(contexto, this.IdSolicitud);
        return solicitud;
    }

    public Solicitud actualizarPrestamo(ContextoMB contexto, String numeroCuenta, String idEmpresaAseguradora, BigDecimal monto, String detalleTrabajo, Boolean esBatch) {
        String nombreCompleto = this.Integrantes.get(0).Apellido + " " + this.Integrantes.get(0).Nombres;
        nombreCompleto = nombreCompleto.substring(0, nombreCompleto.length() < 40 ? nombreCompleto.length() - 1 : 39);

        SolicitudPrestamo prestamo = prestamo(contexto);
        prestamo.TipoOperacion = "03";
        prestamo.Mercado = "01";
        prestamo.FormaCobroTipo = numeroCuenta.startsWith("3") ? "NDMNCC" : "NDMNCA";
        prestamo.FormaCobroCuenta = numeroCuenta;
        prestamo.Domicilio.Tipo = "DP";
        prestamo.EmpresaAseguradora = Integer.valueOf(idEmpresaAseguradora);
        prestamo.DetalleTrabajo = detalleTrabajo;
        prestamo.Desembolsos.NroDesembolso = 1;
        prestamo.Desembolsos.FormasDesembolso.add(new FormasDesembolso());
        prestamo.Desembolsos.FormasDesembolso.get(0).NroDesembolso = 1;
        prestamo.Desembolsos.FormasDesembolso.get(0).Forma = numeroCuenta.startsWith("3") ? "NCMNCC" : "NCMNCA";
        prestamo.Desembolsos.FormasDesembolso.get(0).Referencia = numeroCuenta;
        prestamo.Desembolsos.FormasDesembolso.get(0).Beneficiario = nombreCompleto;
        prestamo.Desembolsos.FormasDesembolso.get(0).Valor = monto;
        prestamo.Desembolsos.FormasDesembolso.get(0).EsDesembolsoBatch = esBatch;
        prestamo.MontoManoObra = monto.divide(new BigDecimal("2"), RoundingMode.UP);
        prestamo.MontoMateriales = monto.divide(new BigDecimal("2"), RoundingMode.DOWN);
        prestamo.MontoArtefactos = new BigDecimal("0");
        prestamo.MailAvisos = prestamo.new MailAvisos();
        prestamo.MailAvisos.Tipo = "EMP";
        prestamo.AvisosCorreoTradicional = false;
        prestamo.AvisosViaMail = true;

        ApiRequestMB request = ApiMB.request("VentasActualizarPrestamoProcrearRefaccion", "ventas_windows", "PUT", "/solicitudes/{numeroSolicitud}/prestamoPersonal/{idProducto}", contexto);
        request.headers.put("X-Handle", this.IdSolicitud);
        request.path("numeroSolicitud", this.IdSolicitud);
        request.path("idProducto", prestamo.Id);
        request.body(Objeto.fromJson((new Gson()).toJson(prestamo)));

        ApiResponseMB response = ApiMB.response(request, contexto.idCobis());
        logApiVentas(contexto, this.IdSolicitud, "actualizarPrestamo", response);
        if (response.hayError() || !response.objetos("Errores").isEmpty()) {
            throw new ApiVentaExceptionMB(response);
        }

        Solicitud solicitud = solicitud(contexto, this.IdSolicitud);
        return solicitud;
    }

    /* ========== MOTOR ========== */
    public ResolucionMotor ejecutarMotor(ContextoMB contexto) {
        Boolean mejorarOferta = contexto.parametros.bool("mejorarOferta", false);
        if (mejorarOferta && ConfigMB.bool("prendido_canal_amarillo_pp")) {
            return ejecutarMotorMejorarOferta(contexto);
        } else {
            return ejecutarMotor(contexto, null);
        }
    }

    public ResolucionMotor consultarMotor(ContextoMB contexto, Boolean solicitaComprobarIngresos) {
        ApiRequestMB request = ApiMB.request("VentasConsultarMotor", "ventas_windows", "GET", "/solicitudes/{SolicitudId}/resoluciones", contexto);
        request.headers.put("X-Handle", this.IdSolicitud);
        request.path("SolicitudId", this.IdSolicitud);

        ApiResponseMB response = ApiMB.response(request, contexto.idCobis());
        logMotor(contexto, this.IdSolicitud, response);
        if (response.hayError() || !response.objetos("Errores").isEmpty()) {
            throw new ApiVentaExceptionMB(response);
        }

        Objeto datos = response.objetos("Datos").get(0);
        ResolucionMotor resolucionMotor = (new Gson()).fromJson(datos.toJson(), ResolucionMotor.class);
        return resolucionMotor;
    }

    public ResolucionMotor ejecutarMotor(ContextoMB contexto, Boolean solicitaComprobarIngresos) {

        ApiRequestMB request = ApiMB.request("VentasEvaluarSolicitud", "ventas_windows", "PUT", "/solicitudes/{SolicitudId}/resoluciones", contexto);
        request.headers.put("X-Handle", this.IdSolicitud);
        request.path("SolicitudId", this.IdSolicitud);
        request.body("TipoOperacion", "03");
        request.body("FlagSolicitaComprobarIngresos", solicitaComprobarIngresos);

        if (contexto.parametros.bool("esCuotificacion", false))
            request.body("FlagSolicitaAprobacionEstandard", true);

        if (contexto.esJubilado() && contexto.tieneCuentaCategoriaB()) {
            request.body("EsPlanSueldo", true);
            request.body("situacionLaboral", "11");
        }

        ApiResponseMB response = ApiMB.response(request, contexto.idCobis());
        logMotor(contexto, this.IdSolicitud, response);
        if (response.hayError() || !response.objetos("Errores").isEmpty()) {
            logMotor(contexto, this.IdSolicitud, response);
            throw new ApiVentaExceptionMB(response);
        }

        Objeto datos = response.objetos("Datos").get(0);
        ResolucionMotor resolucionMotor = (new Gson()).fromJson(datos.toJson(), ResolucionMotor.class);
        return resolucionMotor;
    }

    public ResolucionMotor ejecutarMotorMejorarOferta(ContextoMB contexto) {

        ApiRequestMB request = ApiMB.request("VentasEvaluarSolicitud", "ventas_windows", "PUT", "/solicitudes/{SolicitudId}/resoluciones", contexto);
        request.headers.put("X-Handle", this.IdSolicitud);
        request.path("SolicitudId", this.IdSolicitud);
        request.body("TipoOperacion", "03");
        request.body("FlagSolicitaComprobarIngresos", 1);
        request.body("FlagSolicitaAprobacionCentralizada", 1);

        requestCanalAmarillo(contexto, request);

        ApiResponseMB response = ApiMB.response(request, contexto.idCobis());
        logMotor(contexto, this.IdSolicitud, response);
        if (response.hayError() || !response.objetos("Errores").isEmpty()) {
            logMotor(contexto, this.IdSolicitud, response);
            throw new ApiVentaExceptionMB(response);
        }

        Objeto datos = response.objetos("Datos").get(0);
        ResolucionMotor resolucionMotor = (new Gson()).fromJson(datos.toJson(), ResolucionMotor.class);
        return resolucionMotor;
    }

    private static void requestCanalAmarillo(ContextoMB contexto, ApiRequestMB request) {
        String idSituacionLaboral = contexto.parametros.string("idSituacionLaboral");
        String ingresoNeto = contexto.parametros.string("ingresoNeto");
        String cuitEmpleador = (idSituacionLaboral.equals("11")) ? cuitAnses : contexto.parametros.string("cuit", "");
        String fecha = contexto.parametros.string("fecha", "");
        String categoriaMonotributista = contexto.parametros.string("letra", "").toUpperCase();

        SituacionLaboral situacionLaboralCobis = SituacionLaboral.situacionLaboralPrincipal(contexto);
        if (Objeto.empty(fecha)) {
            fecha = situacionLaboralCobis.fechaInicioActividad;
        }

        if (Objeto.empty(cuitEmpleador)) {
            cuitEmpleador = "".equals(situacionLaboralCobis.cuitEmpleador) ? "NO TIENE" : situacionLaboralCobis.cuitEmpleador;
        }

        if (("6".equals(idSituacionLaboral)) || "66".equals(idSituacionLaboral)) {
            request.body("SituacionLaboral", idSituacionLaboral);
            request.body("FechaCategoriaMonotributo", Fecha.formato(fecha, "dd/MM/yyyy", "yyyy-MM-dd"));
            request.body("FechaInicio", Fecha.formato(fecha, "dd/MM/yyyy", "yyyy-MM-dd"));
            request.body("CategoriaMonotributo", MBCatalogo.idCategoriaMonotributo(contexto, categoriaMonotributista));
            request.body("IngresosMensuales", MBCatalogo.montoMonotributo(contexto, categoriaMonotributista));
            request.body("CuitEmpleador", contexto.persona().cuit());
        }
        if (("11".equals(idSituacionLaboral) || "1".equals(idSituacionLaboral))) {
            request.body("IngresosMensuales", ingresoNeto);
            request.body("SituacionLaboral", idSituacionLaboral);
            request.body("FechaInicio", Fecha.formato(fecha, "dd/MM/yyyy", "yyyy-MM-dd"));
            request.body("RazonSocial", "11".equals(idSituacionLaboral) ? cuitAnses : cuitEmpleador);
            request.body("CuitEmpleador", "11".equals(idSituacionLaboral) ? cuitAnses : cuitEmpleador); // para jubilados enviar por defecto Anses
        }

        request.body("cargo", "1501"); // No Tiene
        request.body("profesion", "11900"); // otros
        request.body("ramo", "040307"); // otros
        request.body("FlagSolicitaEvaluacionCanalAmarillo", 1);
        return;

    }

    /* ========== FINALIZAR ========== */
    public Solicitud simularFinalizar(ContextoMB contexto) {
        ApiRequestMB request = ApiMB.request("VentasSimularFinalizarSolicitud", "ventas_windows", "GET", "/solicitudes/{SolicitudId}", contexto);
        request.headers.put("X-Handle", this.IdSolicitud);
        request.path("SolicitudId", this.IdSolicitud);
        request.query("estado", "validar");

        ApiResponseMB response = ApiMB.response(request, contexto.idCobis());
        logApiVentas(contexto, this.IdSolicitud, "simularFinalizar", response);
        if (response.hayError() || !response.objetos("Errores").isEmpty()) {
            throw new ApiVentaExceptionMB(response);
        }

        Solicitud solicitud = solicitud(contexto, this.IdSolicitud);
        return solicitud;
    }

    public Solicitud finalizar(ContextoMB contexto) {
        ApiRequestMB request = ApiMB.request("VentasFinalizarSolicitud", "ventas_windows", "GET", "/solicitudes/{SolicitudId}", contexto);
        request.headers.put("X-Handle", this.IdSolicitud);
        request.path("SolicitudId", this.IdSolicitud);
        request.query("estado", "finalizar");

        ApiResponseMB response = ApiMB.response(request, contexto.idCobis());
        logApiVentas(contexto, this.IdSolicitud, "finalizar", response);
        if (response.hayError() || !response.objetos("Errores").isEmpty()) {
            throw new ApiVentaExceptionMB(response);
        }

        Solicitud solicitud = solicitud(contexto, this.IdSolicitud);
        return solicitud;
    }

    /* ========== DESISTIR ========== */
    public void desistirSolicitud(ContextoMB contexto) {
        ApiRequestMB request = ApiMB.request("VentasDesistirSolicitud", "ventas_windows", "GET", "/solicitudes/{idSolicitud}", contexto);
        request.headers.put("X-Handle", this.IdSolicitud);
        request.path("idSolicitud", this.IdSolicitud);
        request.query("estado", "desistir");

        ApiResponseMB response = ApiMB.response(request, contexto.idCobis());
        logApiVentas(contexto, this.IdSolicitud, "desistirSolicitud", response);
        if (response.hayError() || !response.objetos("Errores").isEmpty()) {
            throw new ApiVentaExceptionMB(response);
        }
    }

    public static void desistirTodasSolicitudes(ContextoMB contexto, String cuit) {
        List<Solicitud> solicitudes = solicitudes(contexto, cuit, null);
        for (Solicitud solicitud : solicitudes) {
            solicitud.desistirSolicitud(contexto);
        }
    }

    /* ========== UTIL ========== */
    public static void logApiVentas(ContextoMB contexto, String numeroSolicitud, String servicio, ApiResponseMB response) {
        String mensajeCliente = "";
        String mensajeDesarrollador = "";
        String http = "";

        try {
            if (response != null) {
                mensajeCliente = response.objetos("Errores").get(0).string("MensajeCliente");
                mensajeDesarrollador = response.objetos("Errores").get(0).string("MensajeDesarrollador");
                http = response.objetos("Errores").get(0).string("Codigo");
            } else {
                mensajeCliente = "ERROR";
                mensajeDesarrollador = "Respuesta de Api Ventas es NULL";
            }
        } catch (Exception e) {
        }

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
            sqlRequest.parametros.add("MB");
            SqlMB.response(sqlRequest);
        } catch (Exception e) {
        }
    }

    public static void logMotor(ContextoMB contexto, String numeroSolicitud, ApiResponseMB response) {
        String resolucion = "";
        String explicacion = "";
        String mensajeCliente = "";
        String mensajeDesarrollador = "";
        String http = "";

        try {
            if (response != null) {
                resolucion = !response.objetos("Datos").isEmpty() ? response.objetos("Datos").get(0).string("ResolucionId") : resolucion;
                explicacion = !response.objetos("Datos").isEmpty() ? response.objetos("Datos").get(0).string("Explicacion") : explicacion;
                mensajeCliente = !response.objetos("Errores").isEmpty() ? response.objetos("Errores").get(0).string("MensajeCliente") : mensajeCliente;
                mensajeDesarrollador = !response.objetos("Errores").isEmpty() ? response.objetos("Errores").get(0).string("MensajeDesarrollador") : mensajeDesarrollador;
                http = !response.objetos("Errores").isEmpty() ? response.objetos("Errores").get(0).string("Codigo") : http;
            } else {
                mensajeCliente = "ERROR";
                mensajeDesarrollador = "Respuesta de Motor es NULL";
            }
        } catch (Exception e) {
        }

        try {
            String sql = "";
            sql += " INSERT INTO [Homebanking].[dbo].[log_api_ventas] (momento,idCobis,numeroDocumento,numeroSolicitud,servicio,resolucionMotor,explicacionMotor,mensajeCliente,mensajeDesarrollador,canal)";
            sql += " VALUES (GETDATE(), ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            SqlRequestMB sqlRequest = SqlMB.request("InsertLogApiVentas", "homebanking");
            sqlRequest.sql = sql;
            sqlRequest.parametros.add(Texto.substring(contexto.idCobis(), 250));
            sqlRequest.parametros.add(Texto.substring(contexto.persona().numeroDocumento(), 250));
            sqlRequest.parametros.add(Texto.substring(numeroSolicitud, 250));
            sqlRequest.parametros.add(Texto.substring("motor", 250));
            sqlRequest.parametros.add(Texto.substring(resolucion, 990));
            sqlRequest.parametros.add(Texto.substring(explicacion, 990));
            sqlRequest.parametros.add(Texto.substring(http + " - " + mensajeCliente, 990));
            sqlRequest.parametros.add(Texto.substring(mensajeDesarrollador, 990));
            sqlRequest.parametros.add("MB");
            SqlMB.response(sqlRequest);
        } catch (Exception e) {
        }
    }

    public static Solicitud solicitudPrestamoComplementario(ContextoMB contexto, String cuit) {
        List<Solicitud> solicitudes = solicitudes(contexto, cuit, null);
        for (Solicitud solicitud : solicitudes) {
            if (solicitud.contienePrestamo()) {
                SolicitudPrestamo prestamo = solicitud.prestamo(contexto);
                if (prestamo.esPrestamoComplementario()) {
                    return solicitud;
                }
            }
        }
        return null;
    }

    public SolicitudPrestamo generarPrestamoPersonal(ContextoMB contexto, String numeroTarjetaDebito, String... cuitsIntegrantes) {
        Boolean esAdelanto = contexto.parametros.bool("esAdelanto", false);
        BigDecimal montoCuotificacion = contexto.parametros.bigDecimal("montoCuotificacion");

        Objeto integrante = new Objeto();
        integrante.set("numeroDocumentoTributario", contexto.persona().cuit());
        integrante.set("idCobis", Long.valueOf(contexto.idCobis()));
        integrante.set("numeroTarjetaDebito", numeroTarjetaDebito);
        integrante.set("rol", "D");

        ApiRequestMB request = ApiMB.request("VentasGenerarPrestamoPersonal", "ventas_windows", "POST", "/solicitudes/{SolicitudId}/prestamoPersonal", contexto);
        request.headers.put("X-Handle", this.IdSolicitud);
        request.path("SolicitudId", this.IdSolicitud);
        request.body("Amortizacion", "01");
        request.body("TipoTasa", "01");
        request.body("DestinoBien", ConfigMB.esDesarrollo() ? "143" : "20");

        if (esAdelanto)
            request.body("Subproducto", ConfigMB.esDesarrollo() ? "46" : "33");

        if (montoCuotificacion != null)
            request.body("Subproducto", "31");

        request.body("DescripcionDestinoFondos", "Libre destino");
        request.add("Integrantes", integrante);
        request.body("Moneda", "80");

        request.body("TipoOperacion", "02"); // Se manda 02 para que no haga todas las validaciones
        ApiResponseMB response = ApiMB.response(request, contexto.idCobis());
        logApiVentas(contexto, this.IdSolicitud, "generarPrestamoPersonal", response);
        if (response.hayError() || !response.objetos("Errores").isEmpty()) {
            // ApiOmnicanalidad.insertarLogApiVentas(contexto, this.IdSolicitud,
            // "VentasGenerarPrestamo",
            // response.objetos("Errores").get(0).string("MensajeCliente"),
            // response.objetos("Errores").get(0).string("MensajeDesarrollador"),
            // response.objetos("Errores").get(0).string("Codigo"));
            logApiVentas(contexto, this.IdSolicitud, "VentasGenerarPrestamo", response);
            throw new ApiVentaExceptionMB(response);
        }

        Objeto datos = response.objetos("Datos").get(0);
        SolicitudPrestamo solicitudPrestamo = (new Gson()).fromJson(datos.toJson(), SolicitudPrestamo.class);
        return solicitudPrestamo;
    }

    public Solicitud generarPrestamoComplementario(ContextoMB contexto, BigDecimal montoSolicitado, Integer plazo, String... cuitsIntegrantes) {
        String subProducto = ConfigMB.esDesarrollo() ? "43" : "30";
        if (subProducto.isEmpty()) {
            throw new RuntimeException();
        }

        ApiRequestMB request = ApiMB.request("VentasGenerarPrestamoComplementario", "ventas_windows", "POST", "/solicitudes/{SolicitudId}/prestamoPersonal", contexto);
        request.headers.put("X-Handle", this.IdSolicitud);
        request.path("SolicitudId", this.IdSolicitud);
        request.body(new Objeto());

        Objeto datos = request.body();
        datos.set("TipoOperacion", "02");
        datos.set("SubProducto", subProducto);
        datos.set("MontoSolicitado", montoSolicitado);
        datos.set("Amortizacion", "01");
        datos.set("TipoTasa", "01");
        datos.set("DestinoBien", ConfigMB.esDesarrollo() ? 167 : 42);

        datos.set("DestinoBienVivienda", "01");
        datos.set("DescripcionDestinoFondos", "Mejoramiento Materiales");
        datos.set("Mercado", "01");
        datos.set("destinoVivienda", "0");
        datos.set("destino", "044");
        datos.set("Domicilio", new Objeto().set("Tipo", "DP"));

        if (Objects.nonNull(plazo)) {
            datos.set("PlazoSolicitado", plazo);
        }

        for (Integer i = 0; i < cuitsIntegrantes.length; ++i) {
            if (Objects.nonNull(cuitsIntegrantes[i])) {
                Objeto integrante = new Objeto();
                integrante.set("numeroDocumentoTributario", cuitsIntegrantes[i]);
                integrante.set("rol", i == 0 ? "D" : "C");
                request.add("Integrantes", integrante);
            }
        }

        ApiResponseMB response = ApiMB.response(request, contexto.idCobis());
        logApiVentas(contexto, this.IdSolicitud, "generarPrestamoComplementario", response);
        if (response.hayError() || !response.objetos("Errores").isEmpty()) {
            // ApiOmnicanalidad.insertarLogApiVentas(contexto, this.IdSolicitud,
            // "VentasGenerarPrestamoComplementario",
            // response.objetos("Errores").get(0).string("MensajeCliente"),
            // response.objetos("Errores").get(0).string("MensajeDesarrollador"),
            // response.objetos("Errores").get(0).string("Codigo"));
            logApiVentas(contexto, this.IdSolicitud, "VentasGenerarPrestamoComplementario", response);
            throw new ApiVentaExceptionMB(response);
        }

        Solicitud solicitud = solicitud(contexto, this.IdSolicitud);
        return solicitud;
    }

    public Solicitud actualizarPrestamoComplementario(ContextoMB contexto, BigDecimal montoSolicitado, Integer plazo, String... cuitsIntegrantes) {
        String subProducto = ConfigMB.esDesarrollo() ? "43" : "30";
        if (subProducto.isEmpty()) {
            throw new RuntimeException();
        }

        ApiRequestMB request = ApiMB.request("VentasActualizarPrestamoComplementario", "ventas_windows", "PUT", "/solicitudes/{SolicitudId}/prestamoPersonal/{PrestamoId}", contexto);
        request.headers.put("X-Handle", this.IdSolicitud);
        request.path("SolicitudId", this.IdSolicitud);
        request.path("PrestamoId", idPrestamo());
        request.body(new Objeto());

        Objeto datos = request.body();
        datos.set("TipoOperacion", "02");
        datos.set("SubProducto", subProducto);
        datos.set("MontoSolicitado", montoSolicitado);
        datos.set("Amortizacion", "01");
        datos.set("TipoTasa", "01");
        datos.set("DestinoBien", ConfigMB.esDesarrollo() ? 167 : 42);

        datos.set("DestinoBienVivienda", "01");
        datos.set("DescripcionDestinoFondos", "Mejoramiento Materiales");
        datos.set("Mercado", "01");
        datos.set("destinoVivienda", "0");
        datos.set("destino", "044");
        datos.set("Domicilio", new Objeto().set("Tipo", "DP"));

        if (Objects.nonNull(plazo)) {
            datos.set("PlazoSolicitado", plazo);
        }
        for (Integer i = 0; i < cuitsIntegrantes.length; ++i) {
            if (Objects.nonNull(cuitsIntegrantes[i])) {
                Objeto integrante = new Objeto();
                integrante.set("numeroDocumentoTributario", cuitsIntegrantes[i]);
                integrante.set("rol", i == 0 ? "D" : "C");
                request.add("Integrantes", integrante);
            }
        }

        ApiResponseMB response = ApiMB.response(request, contexto.idCobis());
        logApiVentas(contexto, this.IdSolicitud, "actualizarPrestamoComplementario", response);
        if (response.hayError() || !response.objetos("Errores").isEmpty()) {
            // ApiOmnicanalidad.insertarLogApiVentas(contexto, this.IdSolicitud,
            // "VentasActualizarPrestamoComplementario",
            // response.objetos("Errores").get(0).string("MensajeCliente"),
            // response.objetos("Errores").get(0).string("MensajeDesarrollador"),
            // response.objetos("Errores").get(0).string("Codigo"));
            logApiVentas(contexto, this.IdSolicitud, "VentasActualizarPrestamoComplementario", response);
            throw new ApiVentaExceptionMB(response);
        }

        Solicitud solicitud = solicitud(contexto, this.IdSolicitud);
        return solicitud;
    }

    /* ========== UTIL ========== */
    public static void logOriginacion(ContextoMB contexto, String numeroSolicitud, String servicio, ApiResponseMB response, String mensaje) {
        String mensajeCliente = "";
        String mensajeDesarrollador = "";
        String http = "";

        try {
            mensajeCliente = mensaje;
            if (response != null) {
                mensajeCliente = response.objetos("Errores").get(0).string("MensajeCliente");
                mensajeDesarrollador = response.objetos("Errores").get(0).string("MensajeDesarrollador");
                http = response.objetos("Errores").get(0).string("Codigo");
            }

        } catch (Exception e) {
        }

        try {
            String sql = "";
            sql += " INSERT INTO [Homebanking].[dbo].[log_originacion] (momento,idCobis,numeroDocumento,numeroSolicitud,servicio,resolucionMotor,explicacionMotor,mensajeCliente,mensajeDesarrollador,canal)";
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
            sqlRequest.parametros.add("MB");
            SqlMB.response(sqlRequest);
        } catch (Exception e) {
        }
    }

    public SolicitudCuentaCorriente generarAdelanto(ContextoMB contexto, String idSolicitud, String numeroCajaAhorro) {

        ApiResponseMB response = RestVenta.generarCuentaCorriente(contexto, idSolicitud, "ADE", numeroCajaAhorro);
        logApiVentas(contexto, this.IdSolicitud, "generarAdelanto", response);
        if (response.hayError() || !response.objetos("Errores").isEmpty()) {
            logApiVentas(contexto, this.IdSolicitud, "VentasGenerarAdelanto", response);
            throw new ApiVentaExceptionMB(response);
        }

        Objeto datos = response.objetos("Datos").get(0);
        SolicitudCuentaCorriente solicitudAdelanto = (new Gson()).fromJson(datos.toJson(), SolicitudCuentaCorriente.class);
        return solicitudAdelanto;
    }

    public static RespuestaMB validarFinalizarSolicitud(ContextoMB contexto) {
        String idSolicitud = contexto.parametros.string("idSolicitud");
        Boolean mejorarOferta = contexto.parametros.bool("mejorarOferta", false);
        Boolean esAdelanto = contexto.parametros.bool("esAdelanto", false);
        String csmId = contexto.parametros.string("csmId", "");
        String checksum = contexto.parametros.string("checksum", "");

        if (!ConfigMB.bool("prendido_alta_prestamos") || (esAdelanto && !ConfigMB.bool("prendido_adelanto_bh")))
            return RespuestaMB.estado("OPERACION_INHABILITADA");

        if (Objeto.anyEmpty(idSolicitud))
            return RespuestaMB.parametrosIncorrectos();

        if (!contexto.sesion().aceptaTyC())
            return RespuestaMB.estado("REQUIERE_ACEPTAR_TyC");

        boolean esMigrado = contexto.esMigrado(contexto);
        if (esMigrado && Objeto.anyEmpty(csmId, checksum))
            return RespuestaMB.parametrosIncorrectos();

        if (!esMigrado && !contexto.sesion().isChallengeOtp() && !contexto.sesion().validaSegundoFactorSoftToken() && !contexto.sesion().validaRiesgoNet()) {
            return RespuestaMB.estado("REQUIERE_VALIDAR_RIESGO_NET_O_SOFTOKEN");
        }

        RespuestaMB respuestaValidaTransaccion = contexto.validarTransaccion(contexto, esMigrado, esAdelanto ? "adelanto" : "prestamo-personal", JourneyTransmitEnum.MB_INICIO_SESION);
        if (respuestaValidaTransaccion.hayError())
            return respuestaValidaTransaccion;

        if (mejorarOferta && ConfigMB.bool("prendido_canal_amarillo_pp") && !contexto.sesion().adjuntaDocumentacion())
            return RespuestaMB.estado("REQUIERE_ADJUNTAR_DOCUMENTOS");

        if (!ConfigMB.bool("prendido_alta_prestamos") || (esAdelanto && !ConfigMB.bool("prendido_adelanto_bh"))) {
            return RespuestaMB.estado("OPERACION_INHABILITADA");
        }

        if (Objeto.anyEmpty(idSolicitud)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        if (!contexto.sesion().aceptaTyC()) {
            return RespuestaMB.estado("REQUIERE_ACEPTAR_TyC");
        }

        if (mejorarOferta && ConfigMB.bool("prendido_canal_amarillo_pp") && !contexto.sesion().adjuntaDocumentacion()) {
            return RespuestaMB.estado("REQUIERE_ADJUNTAR_DOCUMENTOS");
        }

        if (ContextoMB.cambioDetectadoParaNormativoPP(contexto, true)) {
            Solicitud.logOriginacion(contexto, idSolicitud, "finalizarSolicitud", null, "CAMBIO_INFO_PERSONAL_IMPORTANTE_PRESTAMO");
            return RespuestaMB.estado("CAMBIO_INFO_PERSONAL_IMPORTANTE_PRESTAMO");
        }

        return RespuestaMB.exito();
    }

}